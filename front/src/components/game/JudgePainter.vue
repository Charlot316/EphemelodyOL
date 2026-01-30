<script setup>
import { defineProps, watch } from 'vue';

const colorOpacity = 0.04;
const judgeSize = 300;
const judgeAnimationTime = 300;

const props = defineProps({
  Y: Number,
  middle: Number,
  global: Object,
  judge: Object
});

const colorList = [
  "rgba(0,0,0," + colorOpacity + ")",
  "rgba(255,255,255," + colorOpacity + ")",
  "rgba(255,215,0," + colorOpacity + ")",
  "rgba(218,165,32," + colorOpacity + ")",
  "rgba(173,255,47," + colorOpacity + ")",
  "rgba(100,149,237," + colorOpacity + ")",
  "rgba(0,191,255," + colorOpacity + ")",
  "rgba(255,0,255," + colorOpacity + ")",
  "rgba(72,61,139," + colorOpacity + ")",
  "rgba(0,0,0," + colorOpacity + ")",
  "rgba(0,0,0," + colorOpacity + ")",
];

const paintJudge = (judge) => {
  const painter = props.global.judgePainter;
  if (!painter) return;
  
  let currentTime = props.global.currentTime;
  let size = 0;
  let width = 0;
  
  if (currentTime < judge.timing) currentTime = judge.timing;
  
  if (currentTime < judge.timing + judgeAnimationTime * 0.75) {
    size = ((0.9 * judgeSize) / (judgeAnimationTime * 0.75)) * (currentTime - judge.timing);
    width = 40;
  } else if (currentTime < judge.timing + judgeAnimationTime) {
    const k = (0.4 * judgeSize) / judgeAnimationTime;
    const b = judgeSize - k * (judge.timing + judgeAnimationTime);
    size = k * currentTime + b;
    width = (40 / (judgeAnimationTime * 0.25)) * (judge.timing + judgeAnimationTime - currentTime);
  } else {
    size = 0;
    width = 0;
  }
  
  if (judge.type === "far") {
    size *= 0.6;
  }

  painter.beginPath();
  painter.moveTo(props.middle, props.Y - size);
  painter.lineTo(props.middle + size, props.Y);
  painter.lineTo(props.middle, props.Y + size);
  painter.lineTo(props.middle - size, props.Y);
  painter.lineTo(props.middle, props.Y - size);
  painter.closePath();
  
  if (judge.type === "pure") {
    const gradient = painter.createLinearGradient(
      props.middle - size,
      props.Y - size,
      props.middle + size,
      props.Y + size
    );
    colorList.forEach((color, i) => {
      if (i <= 10) gradient.addColorStop(i / 10, color);
    });
    painter.strokeStyle = gradient;
  } else if (judge.type === "far") {
    painter.strokeStyle = `rgba(100,149,237,${colorOpacity})`;
  }
  
  painter.globalCompositeOperation = "lighter";
  for (let temp = 2; temp < width; temp++) {
    painter.lineWidth = temp / 2;
    temp++; // Original code has temp++ both in for and inside block
    painter.stroke();
  }
  
  painter.beginPath();
  const scale = 1.5;
  painter.moveTo(props.middle, props.Y - scale * size);
  painter.lineTo(props.middle + scale * size, props.Y);
  painter.lineTo(props.middle, props.Y + scale * size);
  painter.lineTo(props.middle - scale * size, props.Y);
  painter.lineTo(props.middle, props.Y - scale * size);
  painter.closePath();
  painter.strokeStyle = `rgba(255,255,255,${0.2 * colorOpacity})`;
  for (let temp = 2; temp < 3 * width; temp++) {
    painter.lineWidth = temp;
    painter.stroke();
    temp += 5;
  }
  painter.globalCompositeOperation = "source-over";
};

watch(() => props.global.currentTime, () => {
  paintJudge(props.judge);
});
</script>

<template>
  <div style="display: none"></div>
</template>
