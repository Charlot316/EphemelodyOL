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
// Initialize particles - MORE particles for better effect
for (let i = 0; i < 20; i++) {
  const angle = Math.random() * Math.PI * 2;
  const speed = Math.random() * 0.4 + 0.2; // Faster
  particles.push({
    vx: Math.cos(angle) * speed,
    vy: Math.sin(angle) * speed,
    size: Math.random() * 3 + 2,
    offset: Math.random() * 10,
    life: Math.random() * 0.5 + 0.5 // Random life variation
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
  let baseColor = "255, 255, 255"; // RGB string for easier alpha manipulation
  let strokeColor = "rgba(255, 255, 255, 0.8)";
  
  if (judge.type === "pure") {
    baseColor = "255, 215, 0"; // Gold
    strokeColor = "rgba(255, 215, 0, 0.8)";
  } else if (judge.type === "far") {
    baseColor = "100, 149, 237"; // Blue
    strokeColor = "rgba(100, 149, 237, 0.8)";
  } else if (judge.type === "lost") {
    baseColor = "255, 69, 0"; // Red
    strokeColor = "rgba(255, 69, 0, 0.8)";
  }

  // === 1. Draw Particles (Enhanced) ===
  const alpha = Math.max(0, 1 - dt / duration);
  painter.globalAlpha = alpha;
  painter.fillStyle = `rgba(${baseColor}, 0.8)`;
  
  particles.forEach(p => {
    // Physics: Simple friction/drag
    const drag = 0.95; 
    const moveDist = p.offset + (Math.sqrt(p.vx*p.vx + p.vy*p.vy) * dt * 2.0); // Faster expansion
    
    // Add some random wobble or curl? Keep simple for now but fast.
    const x = props.middle + Math.cos(Math.atan2(p.vy, p.vx)) * moveDist;
    const y = props.Y + Math.sin(Math.atan2(p.vy, p.vx)) * moveDist; 
    
    // Size shrinks over time
    const currentSize = p.size * alpha * p.life;
    
    if (currentSize > 0) {
      painter.beginPath();
      // Using rect for "digital/pixel" feel, or arc for "sparkle"
      // painter.fillRect(x - currentSize/2, y - currentSize/2, currentSize, currentSize);
      painter.arc(x, y, currentSize / 1.5, 0, Math.PI * 2);
      painter.fill();
    }
  });
  painter.globalAlpha = 1.0;

  // === 2. Draw Ripple / Shockwave (Replacing Diamond) ===
  // A fast expanding ring that fades out
  // Time: 0 -> duration
  // Radius: 0 -> maxRadius
  // Opacity: 1 -> 0
  
  const progress = dt / duration;
  const easeOutQuad = t => t * (2 - t); // Easing for smoother expansion
  const easedProgress = easeOutQuad(progress);
  
  const maxRadius = 100; // Adjust based on track width if needed
  const currentRadius = easedProgress * maxRadius;
  const ringAlpha = Math.max(0, 1 - easedProgress); // Fade out
  
  if (ringAlpha > 0.01) {
    painter.beginPath();
    painter.arc(props.middle, props.Y, currentRadius, 0, Math.PI * 2);
    painter.lineWidth = 4 * ringAlpha; // Thinner as it expands
    painter.strokeStyle = `rgba(${baseColor}, ${ringAlpha})`;
    painter.stroke();
    
    // Optional: Second smaller ring for "echo" effect, slightly delayed
    if (progress > 0.1) {
      const delayProgress = (dt - duration * 0.1) / (duration * 0.9);
      const easedDelay = easeOutQuad(delayProgress);
      const delayRadius = easedDelay * (maxRadius * 0.8);
      const delayAlpha = Math.max(0, (1 - easedDelay) * 0.5);
      
      painter.beginPath();
      painter.arc(props.middle, props.Y, delayRadius, 0, Math.PI * 2);
      painter.lineWidth = 2 * delayAlpha;
      painter.strokeStyle = `rgba(${baseColor}, ${delayAlpha})`;
      painter.stroke();
    }
  }

  // === 3. Center Flash (Impact) ===
  // A brief flash at the center point at the very beginning
  if (dt < duration * 0.3) {
    const flashProgress = dt / (duration * 0.3);
    const flashAlpha = 1 - flashProgress;
    const flashSize = 30 * (1 - flashProgress); // Shrinks rapidly
    
    // Glow
    const gradient = painter.createRadialGradient(props.middle, props.Y, 0, props.middle, props.Y, flashSize);
    gradient.addColorStop(0, `rgba(255, 255, 255, ${flashAlpha})`);
    gradient.addColorStop(1, `rgba(${baseColor}, 0)`);
    
    painter.globalCompositeOperation = "lighter"; // Additive blending for glow
    painter.fillStyle = gradient;
    painter.beginPath();
    painter.arc(props.middle, props.Y, flashSize, 0, Math.PI * 2);
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
