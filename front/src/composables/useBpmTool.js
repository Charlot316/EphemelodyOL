import { ref } from 'vue';
import { ElNotification } from 'element-plus';

export function useBpmTool(chart, global, playerActions) {
  const bpmStart = ref(false);
  const bpmcount = ref(0);
  const lastTime = ref(0);
  const bpmtotal = ref(0);
  const startTotal = ref(0);

  const ManualCalculatebpm = () => {
    if (chart.beatsCount && chart.lastBeatDelay && chart.firstBeatDelay) {
      chart.bpm = (chart.lastBeatDelay - chart.firstBeatDelay) / chart.beatsCount;
    } else {
      ElNotification({ title: "错误", message: "请先输入首拍、末拍偏移和节拍数", type: "error" });
    }
  };

  const calculatebpm = () => {
    if (!bpmStart.value) {
      playerActions.seek(0);
      bpmcount.value = 0;
      lastTime.value = 0;
      bpmtotal.value = 0;
      startTotal.value = 0;
      lastTime.value = global.currentTime;
      bpmStart.value = true;
      playerActions.play();
    } else {
      const now = global.currentTime;
      if (bpmcount.value <= 3) {
        lastTime.value = now;
      } else {
        bpmtotal.value += now - lastTime.value;
        startTotal.value += now - (now - lastTime.value) * bpmcount.value;
        lastTime.value = now;
        
        if (bpmcount.value >= 10) {
          chart.bpm = bpmtotal.value / (bpmcount.value - 3);
          chart.firstBeatDelay = Math.round(startTotal.value / (bpmcount.value - 3));
        }
      }
      bpmcount.value++;
    }
  };

  const endbpm = () => {
    playerActions.pause();
    playerActions.seek(0);
    startTotal.value = 0;
    bpmcount.value = 0;
    bpmStart.value = false;
    lastTime.value = 0;
    bpmtotal.value = 0;
  };

  return {
    bpmStart,
    bpmcount,
    ManualCalculatebpm,
    calculatebpm,
    endbpm
  };
}
