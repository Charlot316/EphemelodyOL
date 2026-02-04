<script setup>
import { defineProps, watch } from 'vue';

const colorOpacity = 0.04;
const judgeSize = 300;
const judgeAnimationTime = 400;

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

// Old particles init removed

const particles = [];
// Initialize particles - REDUCED and CONTAINED
for (let i = 0; i < 25; i++) {
  const angle = Math.random() * Math.PI * 2;
  // Slower speed range: 0.2 to 0.6 (was 0.4 to 1.2)
  const speed = Math.random() * 0.4 + 0.2;
  particles.push({
    vx: Math.cos(angle) * speed,
    vy: Math.sin(angle) * speed,
    // Size: 3 to 10 pixels (slightly reduced max)
    size: Math.random() * 7 + 3,
    offset: Math.random() * 10, // Reduced offset
    life: Math.random() * 0.6 + 0.4,
    // Slower expansion
    growth: Math.random() * 0.2 + 0.1
  });
}

const paintJudge = (judge) => {
  const painter = props.global.judgePainter;
  if (!painter) return;

  let currentTime = props.global.currentTime;

  if (currentTime < judge.timing) currentTime = judge.timing;
  const dt = currentTime - judge.timing;

  // Animation duration
  const duration = judgeAnimationTime;
  if (dt > duration) return;

  // Base Color determination
  let baseColor = "255, 255, 255";
  let strokeColor = "rgba(255, 255, 255, 0.8)";
  let scaleFactor = 1.0;

  if (judge.type === "pure") {
    baseColor = "255, 215, 0"; // Gold
    strokeColor = "rgba(255, 215, 0, 0.8)";
    scaleFactor = 1.4; // Largest
  } else if (judge.type === "far") {
    baseColor = "100, 149, 237"; // Blue
    strokeColor = "rgba(100, 149, 237, 0.8)";
    scaleFactor = 1.0; // Normal
  } else if (judge.type === "lost") {
    baseColor = "255, 69, 0"; // Red
    strokeColor = "rgba(255, 69, 0, 0.8)";
    scaleFactor = 0.6; // Smallest
  }

  // === 1. Draw Diamond Particles (Expanding) ===
  const alpha = Math.max(0, 1 - dt / duration);
  painter.globalAlpha = alpha;
  // Use strokeStyle instead of fillStyle for outlined particles
  painter.strokeStyle = `rgba(${baseColor}, 0.9)`;
  painter.lineWidth = 1.5; // Stroke width for particles

  particles.forEach(p => {
    // Physics
    const moveDist = p.offset + (Math.sqrt(p.vx * p.vx + p.vy * p.vy) * dt * 2.5); // Fast expansion
    const x = props.middle + Math.cos(Math.atan2(p.vy, p.vx)) * moveDist;
    const y = props.Y + Math.sin(Math.atan2(p.vy, p.vx)) * moveDist;

    // Size grows then possibly shrinks or fades
    // Let's make them expand diamonds
    const currentSize = (p.size + p.growth * dt * 0.1) * alpha * p.life;
    const halfSize = currentSize / 2;

    if (currentSize > 0) {
      painter.beginPath();
      // Draw Diamond Shape
      painter.moveTo(x, y - halfSize);
      painter.lineTo(x + halfSize, y);
      painter.lineTo(x, y + halfSize);
      painter.lineTo(x - halfSize, y);
      painter.closePath();
      // STROKE instead of FILL
      painter.stroke();
    }
  });
  painter.globalAlpha = 1.0;

  // === 2. Draw Main Expanding Diamond ===
  const progress = dt / duration;
  const easeOutCubic = t => 1 - Math.pow(1 - t, 3);
  const easedProgress = easeOutCubic(progress);

  const baseRadius = 120 * scaleFactor;
  const currentRadius = easedProgress * baseRadius;
  const ringAlpha = Math.max(0, 1 - easedProgress);

  if (ringAlpha > 0.01) {
    const halfR = currentRadius;
    painter.beginPath();

    painter.moveTo(props.middle, props.Y - halfR);
    painter.lineTo(props.middle + halfR, props.Y);
    painter.lineTo(props.middle, props.Y + halfR);
    painter.lineTo(props.middle - halfR, props.Y);
    painter.closePath();

    // Thicker line: Increased from 4 to 8
    painter.lineWidth = 16 * ringAlpha;
    painter.strokeStyle = `rgba(${baseColor}, ${ringAlpha})`;
    painter.stroke();

    // Inner Diamond (Echo)
    if (progress > 0.05) {
      const delayR = currentRadius * 0.75; // Slightly closer
      painter.beginPath();
      painter.moveTo(props.middle, props.Y - delayR);
      painter.lineTo(props.middle + delayR, props.Y);
      painter.lineTo(props.middle, props.Y + delayR);
      painter.lineTo(props.middle - delayR, props.Y);
      painter.closePath();

      // Thicker echo line too
      painter.lineWidth = 8 * ringAlpha;
      painter.strokeStyle = `rgba(${baseColor}, ${ringAlpha * 0.5})`;
      painter.stroke();
    }
  }

  // Removed Center Flash Block
};

watch(() => props.global.currentTime, () => {
  paintJudge(props.judge);
});
</script>

<template>
  <div style="display: none"></div>
</template>
