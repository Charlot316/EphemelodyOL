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
    chart.tracks.forEach((t, i) => (t.index = i));
    chart.changeBackgroundOperations?.forEach((op, i) => (op.index = i));
  };

  const sortTrack = (timeSort = true) => {
    if (timeSort) {
      chart.tracks.sort((a, b) => a.startTiming - b.startTiming);
    } else {
      chart.tracks.sort((a, b) => a.positionX - b.positionX);
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
    onAudioLoaded
  };
}
