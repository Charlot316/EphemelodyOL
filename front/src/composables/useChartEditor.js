import { ref, reactive, computed, watch } from 'vue';
import { Axios } from "@/plugins/axios";
import { ElNotification, ElMessageBox } from 'element-plus';
import { useWebSocket } from './useWebSocket';
import { v4 as uuidv4 } from 'uuid';

export function useChartEditor(route, router) {
  const chartGot = ref(false);
  const isRunning = ref(false);
  const sliding = ref(false);
  const chart = reactive({
    tracks: [],
    changeBackgroundOperations: [],
    songLength: 0,
    bpm: 0,
    beatsCount: 0,
    firstBeatDelay: 0,
    lastBeatDelay: 0,
    songId: null,
    songName: "",
    songWriter: "",
    uploader: "",
    defaultBackground: null,
    songUrl: "",
    songCover: "",
    difficulty: 0,
    assets: [],
  });

  const displayStart = ref(0);
  const displayEnd = ref(10);

  const displayRange = computed({
    get: () => [displayStart.value, displayEnd.value],
    set: (val) => {
      displayStart.value = val[0];
      displayEnd.value = val[1];
    }
  });

  const onlineUsers = ref([]);
  const onlineCount = computed(() => onlineUsers.value.length);

  const handleSocketMessage = (msg) => {
    const { type, payload, clientId } = msg;

    switch (type) {
      case "ONLINE_STATUS":
        onlineUsers.value = payload.users;
        break;
      case "PUBLISH_READY_SOLO":
        ElMessageBox.confirm("确定发布谱面吗？发布后将持久化归档，之前的操作将无法撤回。", "系统提示", {
          confirmButtonText: "确定发布",
          cancelButtonText: "取消",
          type: "warning"
        }).then(() => {
          executeFinalSave(true);
        });
        break;
      case "PUBLISH_PROPOSAL":
        ElMessageBox.confirm(`${payload.from} 请求发布谱面。发布后将持久化并清空撤回历史，是否同意？`, "发布表决", {
          confirmButtonText: "同意发布",
          cancelButtonText: "拒绝",
          distinguishCancelAndClose: true,
          type: "info"
        }).then(() => {
          syncAction("PUBLISH_VOTE", true);
        }).catch((action) => {
          if (action === 'cancel') syncAction("PUBLISH_VOTE", false);
        });
        break;
      case "PUBLISH_PERMITTED":
        ElNotification({ title: "表决通过", message: "所有人已同意，正在执行发布...", type: "success" });
        executeFinalSave(true);
        break;
      case "PUBLISH_REJECTED":
        ElMessageBox.alert(`发布请求被 ${payload} 拒绝。`, "发布已取消", { type: "error" });
        break;
      case "PUBLISH_STARTING":
        ElNotification({ title: "正在发布", message: "发起人正在执行发布操作，请稍候...", type: "info" });
        break;
      case "RESET_READY_SOLO":
        ElMessageBox.confirm("确定要重置当前谱面吗？DB中的所有未发布更改将丢失，还原到上一次发布的JSON状态。", "危险操作", {
          confirmButtonText: "确定重置",
          cancelButtonText: "取消",
          type: "danger"
        }).then(() => {
          executeFinalReset();
        });
        break;
      case "RESET_PROPOSAL":
        ElMessageBox.confirm(`${payload.from} 请求重置谱面。所有未发布的实时更改将丢失并还原到最新备份，是否同意？`, "重置表决", {
          confirmButtonText: "同意重置",
          cancelButtonText: "拒绝",
          type: "warning"
        }).then(() => {
          syncAction("RESET_VOTE", true);
        }).catch(() => {
          syncAction("RESET_VOTE", false);
        });
        break;
      case "RESET_PERMITTED":
        ElNotification({ title: "表决通过", message: "所有人已同意，正在恢复谱面...", type: "success" });
        executeFinalReset();
        break;
      case "RESET_REJECTED":
        ElMessageBox.alert(`重置请求被 ${payload} 拒绝。`, "重置已取消", { type: "error" });
        break;
      case "RESET_STARTING":
        ElNotification({ title: "正在重置", message: "发起人正在执行重置操作，页面即将刷新...", type: "info" });
        break;
      // --- NOTES ---
      case "ADD_NOTE_ACK":
        chart.tracks.forEach(track => {
          const note = track.notes.find(n => n.clientId === clientId);
          if (note) {
            note.id = payload.id;
            note.isPending = false;
          }
        });
        break;
      case "ADD_NOTE":
        const trackToAddNote = chart.tracks.find(t => t.id === payload.basedTrack);
        if (trackToAddNote && !trackToAddNote.notes.find(n => n.id === payload.id)) {
          trackToAddNote.notes.push(payload);
        }
        break;
      case "UPDATE_NOTE":
        chart.tracks.forEach(track => {
          const note = track.notes.find(n => n.id === payload.id);
          if (note && note.clientId !== clientId) {
             Object.assign(note, payload);
          }
        });
        break;
      case "DELETE_NOTE_ACK":
      case "DELETE_NOTE":
        chart.tracks.forEach(track => {
          const idx = track.notes.findIndex(n => n.id === payload || (n.clientId === clientId && clientId));
          if (idx !== -1) track.notes.splice(idx, 1);
        });
        break;

      // --- TRACKS ---
      case "ADD_TRACK_ACK":
        const trPending = chart.tracks.find(t => t.clientId === clientId);
        if (trPending) {
          trPending.id = payload.id;
          trPending.isPending = false;
        }
        break;
      case "ADD_TRACK":
        if (!chart.tracks.find(t => t.id === payload.id)) {
          chart.tracks.push(payload);
          sortTrack();
        }
        break;
      case "UPDATE_TRACK_ACK":
        const trUpdating = chart.tracks.find(t => t.id === payload.id || t.clientId === clientId);
        if (trUpdating) trUpdating.isPending = false;
        break;
      case "UPDATE_TRACK":
        const trToSync = chart.tracks.find(t => t.id === payload.id);
        if (trToSync && clientId !== msg.clientId) Object.assign(trToSync, payload);
        break;
      case "DELETE_TRACK_ACK":
      case "DELETE_TRACK":
        const trIdx = chart.tracks.findIndex(t => t.id === payload || (t.clientId === clientId && clientId));
        if (trIdx !== -1) chart.tracks.splice(trIdx, 1);
        break;

      // --- MOVE OPERATIONS ---
      case "ADD_MOVE_OP_ACK":
        chart.tracks.forEach(t => {
          const op = t.moveOperations?.find(o => o.clientId === clientId);
          if (op) { op.id = payload.id; op.isPending = false; }
        });
        break;
      case "UPDATE_MOVE_OP_ACK":
        chart.tracks.forEach(t => {
          const op = t.moveOperations?.find(o => o.id === payload.id || o.clientId === clientId);
          if (op) op.isPending = false;
        });
        break;
      case "DELETE_MOVE_OP_ACK":
      case "DELETE_MOVE_OP":
        chart.tracks.forEach(t => {
          const idx = t.moveOperations?.findIndex(o => o.id === payload || (o.clientId === clientId && clientId));
          if (idx !== -1) t.moveOperations.splice(idx, 1);
        });
        break;
      case "ADD_MOVE_OP":
        const trMove = chart.tracks.find(t => t.id === payload.basedTrack);
        if (trMove && !trMove.moveOperations?.find(o => o.id === payload.id)) {
          if (!trMove.moveOperations) trMove.moveOperations = [];
          trMove.moveOperations.push(payload);
        }
        break;
      case "UPDATE_MOVE_OP":
        chart.tracks.forEach(t => {
          const op = t.moveOperations?.find(o => o.id === payload.id);
          if (op && clientId !== msg.clientId) Object.assign(op, payload);
        });
        break;

      // --- WIDTH OPERATIONS ---
      case "ADD_WIDTH_OP_ACK":
        chart.tracks.forEach(t => {
          const op = t.changeWidthOperations?.find(o => o.clientId === clientId);
          if (op) { op.id = payload.id; op.isPending = false; }
        });
        break;
      case "UPDATE_WIDTH_OP_ACK":
        chart.tracks.forEach(t => {
          const op = t.changeWidthOperations?.find(o => o.id === payload.id || o.clientId === clientId);
          if (op) op.isPending = false;
        });
        break;
      case "DELETE_WIDTH_OP_ACK":
      case "DELETE_WIDTH_OP":
        chart.tracks.forEach(t => {
          const idx = t.changeWidthOperations?.findIndex(o => o.id === payload || (o.clientId === clientId && clientId));
          if (idx !== -1) t.changeWidthOperations.splice(idx, 1);
        });
        break;
      case "ADD_WIDTH_OP":
        const trWidth = chart.tracks.find(t => t.id === payload.basedTrack);
        if (trWidth && !trWidth.changeWidthOperations?.find(o => o.id === payload.id)) {
          if (!trWidth.changeWidthOperations) trWidth.changeWidthOperations = [];
          trWidth.changeWidthOperations.push(payload);
        }
        break;
      case "UPDATE_WIDTH_OP":
        chart.tracks.forEach(t => {
          const op = t.changeWidthOperations?.find(o => o.id === payload.id);
          if (op && clientId !== msg.clientId) Object.assign(op, payload);
        });
        break;

      // --- COLOR OPERATIONS ---
      case "ADD_COLOR_OP_ACK":
        chart.tracks.forEach(t => {
          const op = t.changeColorOperations?.find(o => o.clientId === clientId);
          if (op) { op.id = payload.id; op.isPending = false; }
        });
        break;
      case "UPDATE_COLOR_OP_ACK":
        chart.tracks.forEach(t => {
          const op = t.changeColorOperations?.find(o => o.id === payload.id || o.clientId === clientId);
          if (op) op.isPending = false;
        });
        break;
      case "DELETE_COLOR_OP_ACK":
      case "DELETE_COLOR_OP":
        chart.tracks.forEach(t => {
          const idx = t.changeColorOperations?.findIndex(o => o.id === payload || (o.clientId === clientId && clientId));
          if (idx !== -1) t.changeColorOperations.splice(idx, 1);
        });
        break;
      case "ADD_COLOR_OP":
        const trColor = chart.tracks.find(t => t.id === payload.basedTrack);
        if (trColor && !trColor.changeColorOperations?.find(o => o.id === payload.id)) {
          if (!trColor.changeColorOperations) trColor.changeColorOperations = [];
          trColor.changeColorOperations.push(payload);
        }
        break;
      case "UPDATE_COLOR_OP":
        chart.tracks.forEach(t => {
          const op = t.changeColorOperations?.find(o => o.id === payload.id);
          if (op && clientId !== msg.clientId) Object.assign(op, payload);
        });
        break;

      // --- BACKGROUND OPERATIONS ---
      case "ADD_BG_OP_ACK":
        const bgPending = chart.changeBackgroundOperations.find(o => o.clientId === clientId);
        if (bgPending) { bgPending.id = payload.id; bgPending.isPending = false; }
        break;
      case "UPDATE_BG_OP_ACK":
        const bgUpd = chart.changeBackgroundOperations.find(o => o.id === payload.id || o.clientId === clientId);
        if (bgUpd) bgUpd.isPending = false;
        break;
      case "DELETE_BG_OP_ACK":
      case "DELETE_BG_OP":
        const bgIdx = chart.changeBackgroundOperations.findIndex(o => o.id === payload || (o.clientId === clientId && clientId));
        if (bgIdx !== -1) chart.changeBackgroundOperations.splice(bgIdx, 1);
        break;
      case "ADD_BG_OP":
        if (!chart.changeBackgroundOperations.find(o => o.id === payload.id)) {
          chart.changeBackgroundOperations.push(payload);
          migrateBackgroundOperations();
        }
        break;
      case "UPDATE_BG_OP":
        const bgSync = chart.changeBackgroundOperations.find(o => o.id === payload.id);
        if (bgSync && clientId !== msg.clientId) Object.assign(bgSync, payload);
        break;

      case "ERROR":
        ElNotification({ title: "同步错误", message: payload, type: "error" });
        break;
    }
  };

  const targetSongId = route.query.songId || route.query.id;
  const { send, isConnected } = useWebSocket(targetSongId, handleSocketMessage);

  const syncAction = (type, payload, clientId = null) => {
    if (isConnected.value) {
      send(type, payload, clientId);
    }
  };

  const setIndex = () => {
    chart.tracks?.forEach((t, i) => (t.index = i));
    chart.changeBackgroundOperations?.forEach((op, i) => (op.index = i));
  };

  const migrateBackgroundOperations = () => {
    if (!chart.changeBackgroundOperations || chart.changeBackgroundOperations.length === 0) return;
    chart.changeBackgroundOperations.sort((a, b) => a.startTiming - b.startTiming);
    for (let i = 0; i < chart.changeBackgroundOperations.length; i++) {
        const current = chart.changeBackgroundOperations[i];
        const next = chart.changeBackgroundOperations[i + 1];
        if (current.endTiming === null || current.endTiming === undefined || current.endTiming === 0) {
          if (next) current.endTiming = next.startTiming;
          else current.endTiming = chart.songLength || (current.startTiming + 5000);
        }
    }
    if (chart.defaultBackground) {
      chart.changeBackgroundOperations = chart.changeBackgroundOperations.filter(op => op.background !== chart.defaultBackground);
    }
    setIndex();
  };

  const sortTrack = (byTime = true) => {
    chart.tracks?.sort((a, b) => {
      if (byTime) {
        if (a.startTiming !== b.startTiming) return a.startTiming - b.startTiming;
      } else {
        const posXA = a.positionX || 0;
        const posXB = b.positionX || 0;
        if (posXA !== posXB) return posXA - posXB;
      }
      return (a.id || 0) - (b.id || 0) || (a.index || 0) - (b.index || 0);
    });
    setIndex();
  };

  const getChart = async (globalSettingsCallback) => {
    try {
      const songId = route.query.songId || route.query.id;
      if (!songId) {
        ElNotification({ title: "错误", message: "缺少 songId 参数", type: "error" });
        return;
      }
      const { data: res } = await Axios.get(`/user/getChart?songId=${songId}&fromDb=true`);
      if (res.code !== 0) {
        ElNotification({ title: "失败", message: "谱面获取失败！", type: "error" });
        return;
      }
      Object.assign(chart, res.data);
      // 后端返回的数据可能缺失部分数组字段，需手动补正以防 Vue 渲染崩溃
      if (chart.tracks) {
        chart.tracks.forEach(track => {
          if (!track.notes) track.notes = [];
          if (!track.moveOperations) track.moveOperations = [];
          if (!track.changeWidthOperations) track.changeWidthOperations = [];
          if (!track.changeColorOperations) track.changeColorOperations = [];
        });
      }
      if (!chart.changeBackgroundOperations) chart.changeBackgroundOperations = [];
      if (!chart.assets) chart.assets = [];

      chartGot.value = true;
      displayStart.value = 0;
      if (chart.songLength > 0) migrateBackgroundOperations();
      
      // Force sort everything from DB to ensure consistency
      if (chart.tracks) {
        chart.tracks.forEach(track => {
          if (track.notes) track.notes.sort((a, b) => a.timing - b.timing);
          if (track.moveOperations) track.moveOperations.sort((a, b) => a.startTiming - b.startTiming);
          if (track.changeWidthOperations) track.changeWidthOperations.sort((a, b) => a.startTiming - b.startTiming);
          if (track.changeColorOperations) track.changeColorOperations.sort((a, b) => a.startTiming - b.startTiming);
        });
      }
      // Default to time sort on load
      sortTrack(true);

      if (!chart.bpm || chart.bpm === 0) {
        globalSettingsCallback?.();
        ElNotification({ type: "warning", title: "提示", message: "请设置节拍" });
      }
    } catch (err) {
      ElNotification({ title: "错误", message: "网络异常", type: "error" });
    }
  };

  const saveChart = async (back) => {
    if (onlineCount.value <= 1) {
      // Solo publish
      syncAction("PUBLISH_REQUEST", null);
      // Logic for PUBLISH_READY_SOLO will continue this
    } else {
      // Multi-user consensus
      ElNotification({ title: "正在发起表决", message: "由于当前存在多人在线，需要所有人同意后方可发布。", type: "info" });
      syncAction("PUBLISH_REQUEST", null);
    }
  };

  const resetChart = async () => {
    if (onlineCount.value <= 1) {
      syncAction("RESET_REQUEST", null);
    } else {
      ElNotification({ title: "正在发起重置表决", message: "重置是危险操作，正在等待其他协作者同意...", type: "warning" });
      syncAction("RESET_REQUEST", null);
    }
  };

  const executeFinalReset = async () => {
    try {
      const { data: res } = await Axios.post(`/chart/resetChart?songId=${route.query.songId}`);
      if (res.code !== 0) {
        ElNotification({ title: "失败", message: "谱面恢复失败！", type: "error" });
        return;
      }
      ElNotification({ title: "成功", message: "谱面已恢复，正在刷新页面...", type: "success" });
      setTimeout(() => {
        window.location.reload();
      }, 1000);
    } catch (err) {
      ElNotification({ title: "错误", message: "网络异常", type: "error" });
    }
  };

  const executeFinalSave = async (back) => {
    try {
      const { data: res } = await Axios.post("/chart/editChartContent", chart);
      if (res.code !== 0) {
        ElNotification({ title: "失败", message: "谱面发布失败！", type: "error" });
        return;
      }
      ElNotification({ title: "成功", message: "谱面发布成功（游玩文件已生成）！", type: "success" });
      if (back) router.push("/admin");
    } catch (err) {
      ElNotification({ title: "错误", message: "网络异常", type: "error" });
    }
  };

  const onAudioLoaded = (audioEl) => {
    const len = Math.round(1000 * audioEl.duration);
    if (!chart.songLength || chart.songLength === 0) {
        chart.songLength = len;
        displayEnd.value = len;
        migrateBackgroundOperations();
    }
  };

  return {
    chart,
    chartGot,
    isRunning,
    sliding,
    displayStart,
    displayEnd,
    displayRange,
    sortTrack,
    getChart,
    saveChart,
    onAudioLoaded,
    migrateBackgroundOperations,
    syncAction,
    onlineUsers,
    onlineCount,
    resetChart,
    uuid: uuidv4
  };
}
