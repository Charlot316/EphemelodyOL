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

// Old particles init removed

const particles = [];
// Initialize particles - MORE particles
for (let i = 0; i < 30; i++) {
  const angle = Math.random() * Math.PI * 2;
  const speed = Math.random() * 0.5 + 0.3; // Faster
  particles.push({
    vx: Math.cos(angle) * speed,
    vy: Math.sin(angle) * speed,
    size: Math.random() * 3 + 2, // Base size
    offset: Math.random() * 20,
    life: Math.random() * 0.6 + 0.4, // Random life
    growth: Math.random() * 0.2 + 0.1 // Growth rate for diamond particles
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
  painter.fillStyle = `rgba(${baseColor}, 0.9)`; // Brighter particles
  
  particles.forEach(p => {
    // Physics
    const moveDist = p.offset + (Math.sqrt(p.vx*p.vx + p.vy*p.vy) * dt * 2.5); // Fast expansion
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
      painter.fill();
    }
  });
  painter.globalAlpha = 1.0;

  // === 2. Draw Main Expanding Diamond ===
  const progress = dt / duration;
  const easeOutCubic = t => 1 - Math.pow(1 - t, 3);
  const easedProgress = easeOutCubic(progress);
  
  const baseRadius = 120 * scaleFactor; // Size adjustment
  const currentRadius = easedProgress * baseRadius;
  const ringAlpha = Math.max(0, 1 - easedProgress); // Fade out
  
  if (ringAlpha > 0.01) {
    const halfR = currentRadius;
    painter.beginPath();
    // Diamond shape centered at props.middle, props.Y
    painter.moveTo(props.middle, props.Y - halfR);
    painter.lineTo(props.middle + halfR, props.Y);
    painter.lineTo(props.middle, props.Y + halfR);
    painter.lineTo(props.middle - halfR, props.Y);
    painter.closePath();
    
    painter.lineWidth = 4 * ringAlpha;
    painter.strokeStyle = `rgba(${baseColor}, ${ringAlpha})`;
    painter.stroke();

    // Inner Diamond (Echo) - slightly smaller/delayed
    if (progress > 0.05) {
      const delayR = currentRadius * 0.7;
      painter.beginPath();
      painter.moveTo(props.middle, props.Y - delayR);
      painter.lineTo(props.middle + delayR, props.Y);
      painter.lineTo(props.middle, props.Y + delayR);
      painter.lineTo(props.middle - delayR, props.Y);
      painter.closePath();
      
      painter.lineWidth = 2 * ringAlpha;
      painter.strokeStyle = `rgba(${baseColor}, ${ringAlpha * 0.5})`;
      painter.stroke();
    }
  }

  // === 3. Center Flash (Diamond Shape) ===
  if (dt < duration * 0.3) {
    const flashProgress = dt / (duration * 0.3);
    const flashAlpha = 1 - flashProgress;
    const flashSize = (40 * scaleFactor) * (1 - flashProgress);
    
    painter.globalCompositeOperation = "lighter";
    painter.fillStyle = `rgba(${baseColor}, ${flashAlpha})`; // Solid flash
    
    painter.beginPath();
    painter.moveTo(props.middle, props.Y - flashSize);
    painter.lineTo(props.middle + flashSize, props.Y);
    painter.lineTo(props.middle, props.Y + flashSize);
    painter.lineTo(props.middle - flashSize, props.Y);
    painter.closePath();
    painter.fill();
    
    painter.globalCompositeOperation = "source-over";
  }
};

watch(() => props.global.currentTime, () => {
  paintJudge(props.judge);
});
</script>

<template>
  <div style="display: none"></div>
</template>
