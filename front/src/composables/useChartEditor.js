import { ref, reactive, computed } from 'vue';
import { Axios } from "@/plugins/axios";
import { ElNotification } from 'element-plus';

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

  const setIndex = () => {
    chart.tracks?.forEach((t, i) => (t.index = i));
    chart.changeBackgroundOperations?.forEach((op, i) => (op.index = i));
  };

  const migrateBackgroundOperations = () => {
    if (!chart.changeBackgroundOperations || chart.changeBackgroundOperations.length === 0) return;

    // 1. Sort by startTime
    chart.changeBackgroundOperations.sort((a, b) => a.startTime - b.startTime);

    // 2. Fill EndTime (Legacy Migration)
    for (let i = 0; i < chart.changeBackgroundOperations.length; i++) {
      const current = chart.changeBackgroundOperations[i];
      const next = chart.changeBackgroundOperations[i + 1];
      
      if (next) {
        current.endTime = next.startTime;
      } else {
        // Last one ends at song length or far in future if unknown
        current.endTime = chart.songLength || (current.startTime + 5000);
      }
    }

    // 3. Merging/Cleanup Logic: Remove if background equals default
    // We do this AFTER calculating endTimes so the gap remains correct
    if (chart.defaultBackground) {
      chart.changeBackgroundOperations = chart.changeBackgroundOperations.filter(op => {
        // Normalizing URLs might be needed, but simple string compare for now
        return op.background !== chart.defaultBackground;
      });
    }

    setIndex();
  };

  const sortTrack = (timeSort = true) => {
    if (timeSort) {
      chart.tracks?.sort((a, b) => a.startTiming - b.startTiming);
    } else {
      chart.tracks?.sort((a, b) => a.positionX - b.positionX);
    }
    setIndex();
  };

  const getChart = async (globalSettingsCallback) => {
    try {
      const { data: res } = await Axios.get(`/user/getChart?songId=${route.query.songId}`);
      if (res.code !== 0) {
        ElNotification({ title: "失败", message: "谱面获取失败！", type: "error" });
        return;
      }
      Object.assign(chart, res.data);
      chartGot.value = true;
      displayStart.value = 0;
      
      // Delay migration until audio loaded if songLength is 0
      if (chart.songLength > 0) {
        migrateBackgroundOperations();
      }
      
      sortTrack();
      
      if (!chart.bpm || chart.bpm === 0) {
        globalSettingsCallback?.();
        ElNotification({ type: "warning", title: "提示", message: "请设置节拍" });
      }
    } catch (err) {
      ElNotification({ title: "错误", message: "网络异常", type: "error" });
    }
  };

  const saveChart = async (back) => {
    try {
      const { data: res } = await Axios.post("/chart/editChartContent", chart);
      if (res.code !== 0) {
        ElNotification({ title: "失败", message: "谱面保存失败！", type: "error" });
        return;
      }
      ElNotification({ title: "成功", message: "谱面保存成功！", type: "success" });
      if (back) router.push("/admin");
    } catch (err) {
      ElNotification({ title: "错误", message: "网络异常", type: "error" });
    }
  };

  const onAudioLoaded = (audioEl) => {
    chart.songLength = Math.round(1000 * audioEl.duration);
    displayEnd.value = chart.songLength;
    // Perform migration now that we have the song length
    migrateBackgroundOperations();
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
    migrateBackgroundOperations
  };
}
