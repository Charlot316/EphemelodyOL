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

const particles = [];
// Initialize particles
for (let i = 0; i < 16; i++) {
  const angle = Math.random() * Math.PI * 2;
  const speed = Math.random() * 0.2 + 0.1;
  particles.push({
    vx: Math.cos(angle) * speed,
    vy: Math.sin(angle) * speed,
    size: Math.random() * 3 + 2,
    offset: Math.random() * 20
  });
}

const paintJudge = (judge) => {
  const painter = props.global.judgePainter;
  if (!painter) return;
  
  let currentTime = props.global.currentTime;
  let size = 0;
  let width = 0;
  
  if (currentTime < judge.timing) currentTime = judge.timing;
  const dt = currentTime - judge.timing;
  
  // Main Shape Logic
  if (dt < judgeAnimationTime * 0.75) {
    size = ((0.9 * judgeSize) / (judgeAnimationTime * 0.75)) * dt;
    width = 40;
  } else if (dt < judgeAnimationTime) {
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
  } else if (judge.type === "lost") {
    size *= 0.4; // Smaller for lost
  }

  // Draw Particles
  if (dt > 0 && dt < judgeAnimationTime) {
    const alpha = 1 - dt / judgeAnimationTime;
    painter.globalAlpha = alpha;
    
    let particleColor = "rgba(255, 255, 255, 0.8)";
    if (judge.type === "pure") particleColor = "rgba(255, 215, 0, 0.8)";
    else if (judge.type === "far") particleColor = "rgba(100, 149, 237, 0.8)";
    else if (judge.type === "lost") particleColor = "rgba(255, 69, 0, 0.8)";
    
    painter.fillStyle = particleColor;
    
    particles.forEach(p => {
      // Add some outward movement logic
      const moveDist = p.offset + (Math.sqrt(p.vx*p.vx + p.vy*p.vy) * dt * 1.5); 
      const x = props.middle + Math.cos(Math.atan2(p.vy, p.vx)) * moveDist;
      const y = props.Y + Math.sin(Math.atan2(p.vy, p.vx)) * moveDist;
      
      painter.beginPath();
      // painter.arc(x, y, p.size, 0, Math.PI * 2);
      painter.fillRect(x - p.size/2, y - p.size/2, p.size, p.size);
      painter.fill();
    });
    painter.globalAlpha = 1.0;
  }

  // Draw Main Diamond
  // Skip main diamond for lost if we only want particles, but user said "range smaller" so keep it.
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
  } else if (judge.type === "lost") {
    painter.strokeStyle = `rgba(255,69,0,${colorOpacity})`;
  }
  
  painter.globalCompositeOperation = "lighter";
  for (let temp = 2; temp < width; temp++) {
    painter.lineWidth = temp / 2;
    temp++; 
    painter.stroke();
  }
  
  // Outer/Inner Glow/Detail
  painter.beginPath();
  const scale = 1.5;
  painter.moveTo(props.middle, props.Y - scale * size);
  painter.lineTo(props.middle + scale * size, props.Y);
  painter.lineTo(props.middle, props.Y + scale * size);
  painter.lineTo(props.middle - scale * size, props.Y);
  painter.lineTo(props.middle, props.Y - scale * size);
  painter.closePath();
  
  if (judge.type === "lost") {
     painter.strokeStyle = `rgba(255,69,0,${0.2 * colorOpacity})`;
  } else {
     painter.strokeStyle = `rgba(255,255,255,${0.2 * colorOpacity})`;
  }
  
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
