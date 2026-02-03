<template>
  <div class="body">
    <div class="container">
      <div class="bubbles">
        <span
          v-for="count in 50"
          :key="count"
          :style="{
            '--i': Math.floor(Math.random() * 20 + 10),
            '--duration': Math.floor(Math.random() * 20 + 120) + 's',
            minWidth: '100px',
            height: '100px',
          }"
          >
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
.body {
  min-height: 100vh;
  width: 100vw;
  background: #0c192c;
  color: white;
}
.container {
  position: relative;
  width: 100vw;
  height: 100vh;
}
.bubbles {
  position: relative;
  display: flex;
}
.bubbles span {
  position: absolute;
  bottom: -100px;
  background: radial-gradient(circle, var(--accent-cyan) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.15;
  animation: floatUp 20s infinite linear;
}

.bubbles span:nth-child(even) {
  background: radial-gradient(circle, var(--accent-pink) 0%, transparent 70%);
  opacity: 0.1;
}

@keyframes floatUp {
  0% {
    transform: translateY(0) scale(1);
    opacity: 0;
  }
  20% {
    opacity: 0.15;
  }
  80% {
    opacity: 0.15;
  }
  100% {
    transform: translateY(-120vh) scale(1.5);
    opacity: 0;
  }
}
</style>
