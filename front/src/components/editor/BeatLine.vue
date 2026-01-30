<template>
  <div
    v-if="display"
    :style="{
      position: 'absolute',
      left: 0,
      width: (chart.songLength / displayAreaTime) * (global.documentWidth - 300) + 'px',
      height: '30px',
      zIndex: 10,
      overflow: 'hidden',
    }"
  >
    <div v-for="count in LineCount" :key="count">
      <div
        v-if="(count - 1) % 16 == 0 && (count - 1) * singleWidth + left > 0"
        :style="{
          position: 'absolute',
          top: 0,
          left: (count - 1) * singleWidth + left + 'px',
          width: '1px',
          height: '30px',
          background: 'rgb(255,255,255)',
        }"
      ></div>
      <div
        v-else-if="
          (count - 1) % 8 == 0 &&
            display4 &&
            (count - 1) * singleWidth + left > 0
        "
        :style="{
          position: 'absolute',
          top: 0,
          left: (count - 1) * singleWidth + left + 'px',
          width: '1px',
          height: '20px',
          background: 'rgb(255,255,255)',
        }"
      ></div>
      <div
        v-else-if="
          (count - 1) % 4 == 0 &&
            display8 &&
            (count - 1) * singleWidth + left > 0
        "
        :style="{
          position: 'absolute',
          top: 0,
          left: (count - 1) * singleWidth + left + 'px',
          width: '1px',
          height: '12px',
          background: 'rgb(255,255,255)',
        }"
      ></div>
      <div
        v-else-if="display16 && (count - 1) * singleWidth + left > 0"
        :style="{
          position: 'absolute',
          top: 0,
          left: (count - 1) * singleWidth + left + 'px',
          width: '1px',
          height: '8px',
          background: 'rgb(255,255,255)',
        }"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, watch, onMounted } from 'vue';

const props = defineProps({
  global: Object,
  chart: Object,
  displayAreaTime: Number
});

const display = ref(false);
const display4 = ref(false);
const display8 = ref(false);
const display16 = ref(false);
const LineCount = ref(0);
const left = ref(0);
const singleWidth = ref(0);

const setDisplay = () => {
  const bpm = props.chart.bpm || 600; // Fallback if bpm is 0
  const bpm16 = bpm / 16;
  LineCount.value = Math.ceil(props.chart.songLength / bpm16) + 16;
  
  const bpm8 = bpm / 8;
  const bpm4 = bpm / 4;
  const wholeLength = props.global.documentWidth - 300;
  
  if ((wholeLength * bpm) / props.displayAreaTime > 100) {
    display.value = true;
    const delay = props.chart.firstBeatDelay || 0;
    const d = delay % bpm;
    left.value = (d / props.displayAreaTime) * wholeLength;
    left.value -= (bpm / props.displayAreaTime) * wholeLength;
    singleWidth.value = (wholeLength * bpm16) / props.displayAreaTime;

    display4.value = (wholeLength * bpm4) / props.displayAreaTime > 25;
    display8.value = (wholeLength * bpm8) / props.displayAreaTime > 20;
    display16.value = (wholeLength * bpm16) / props.displayAreaTime > 15;
  } else {
    display.value = false;
    display4.value = false;
    display8.value = false;
    display16.value = false;
  }
};

watch(() => props.displayAreaTime, () => {
  setDisplay();
});

watch(() => props.chart.bpm, setDisplay);
watch(() => props.chart.firstBeatDelay, setDisplay);
watch(() => props.chart.songLength, setDisplay);

onMounted(() => {
  setDisplay();
});
</script>
