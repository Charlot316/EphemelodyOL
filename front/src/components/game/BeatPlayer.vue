<template>
  <div class="beat-player-container" :id="playerId">
    <!-- 背景图层 -->
    <div class="background-base">
      <img :src="normalizeUrl(chart.defaultBackground)" class="background-image" alt="default-bg" />
    </div>
    <div v-for="(op, index) in chart.changeBackgroundOperations" :key="index">
      <img
        :src="normalizeUrl(op.background)"
        v-show="
          global.currentTime >= op.startTime &&
          global.currentTime <= (op.endTime || 0)
        "
        class="background-image background-overlay"
        alt="op-bg"
      />
    </div>

    <!-- 判定线 -->
    <div
      class="judgment-line-container"
      :style="{
        top: global.screenHeight * global.finalY - 1 + 'px',
        width: global.screenWidth + 'px',
      }"
    >
      <div
        class="white-line"
        :style="{
          left: (global.screenWidth - whiteLineLength) / 2 + 'px',
          width: whiteLineLength + 'px',
        }"
      ></div>
    </div>

    <!-- Canvas 层 -->
    <canvas :id="playerId + '-track-canvas'" class="player-canvas" />
    <canvas :id="playerId + '-note-canvas'" class="player-canvas" />
    <canvas :id="playerId + '-judge-canvas'" class="player-canvas" />

    <!-- 轨道组件层 -->
    <div
      class="track-container"
      v-for="(trackItem, idx) in chart.tracks"
      :key="trackItem.trackId || ('track-' + idx)"
    >
      <Track
        :Track="trackItem"
        :global="global"
        @addCount="handleAddCount"
        v-if="
          global.currentTime > trackItem.startTiming &&
          global.currentTime < trackItem.endTiming
        "
      />
    </div>

    <!-- 编辑模式下的选中高亮 -->
    <div
      v-if="mode === 'edit' && selectedTrack"
      class="selected-track-overlay"
      :style="selectedTrackOverlayStyle"
    ></div>

    <!-- 音频控制（隐藏） -->
    <audio
      ref="audioRef"
      preload="auto"
      :src="normalizeUrl(chart.songUrl)"
      style="display:none"
      @canplaythrough="handleAudioLoaded"
    />
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  computed,
  watch,
  onMounted,
  onBeforeUnmount,
  defineProps,
  defineEmits,
  defineExpose
} from 'vue';
import Track from "./Track.vue";

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  global: {
    type: Object,
    required: true
  },
  mode: {
    type: String,
    default: 'play' // 'play' or 'edit'
  },
  volume: {
    type: Number,
    default: 100
  },
  selectedTrackId: {
    type: [String, Number],
    default: null
  },
  playerId: {
    type: String,
    default: 'beat-player'
  },
  // 外部控制时间（主要用于编辑器滑动）
  externalTime: {
    type: Number,
    default: null
  },
  // 显示区域限制（编辑器功能）
  displayRange: {
    type: Array,
    default: () => [0, -1] // [start, end], -1 means songLength
  }
});

const emit = defineEmits([
  'audio-loaded',
  'image-loaded',
  'add-count',
  'time-update',
  'track-click',
  'finished'
]);

const audioRef = ref(null);
const imagePath = ref([]);
const isRunning = ref(false);
const imageLoadedCount = ref(0);

// 使用外部传入的 global
const global = props.global;

// 计算属性
const whiteLineLength = computed(() => {
  const time = 200;
  const waitLoad = 1000;
  const songLen = props.chart.songLength || 0;
  
  if (global.currentTime > time + waitLoad && global.currentTime < songLen - time) {
    return global.screenWidth;
  } else {
    if (global.currentTime <= time + waitLoad) {
      if (global.currentTime < waitLoad) return 0;
      return ((global.currentTime - waitLoad) * global.screenWidth) / time;
    } else {
      return ((songLen - global.currentTime) * global.screenWidth) / time;
    }
  }
});

const selectedTrack = computed(() => {
  if (!props.selectedTrackId || !props.chart.tracks) return null;
  return props.chart.tracks.find(t => (t.trackId || t.index) === props.selectedTrackId);
});

const selectedTrackOverlayStyle = computed(() => {
  if (!selectedTrack.value) return {};
  const t = selectedTrack.value;
  // 只有在时间范围内才显示
  if (global.currentTime < t.startTiming || global.currentTime > t.endTiming) return { display: 'none' };
  
  return {
    position: 'absolute',
    top: '0px',
    left: (t.tempPositionX - t.tempWidth) * global.screenWidth + 'px',
    width: 2 * t.tempWidth * global.screenWidth - 4 + 'px',
    height: global.finalY * global.screenHeight - 2 + 'px',
    border: '2px solid rgba(255,255,255,1)',
    background: 'rgba(255,255,255,0.2)',
    pointerEvents: 'none',
    zIndex: 10
  };
});

// URL 规格化：处理旧谱面中的硬编码域名
const normalizeUrl = (url) => {
  if (!url) return "";
  if (url.includes("pic.mcatk.com")) {
    // 将旧域名替换为当前后端的访问地址
    // 假设后端直接映射了文件名到根目录或特定路径
    const fileName = url.split('/').pop();
    // 这里使用相对路径或拼接当前的后端 baseURL
    return "http://localhost:8090/" + fileName;
  }
  return url;
};

// 方法
const resize = () => {
  const container = document.getElementById(props.playerId);
  if (!container) return;
  
  global.screenWidth = container.offsetWidth;
  global.screenHeight = container.offsetHeight;
  
  const setupCanvas = (idSuffix, painterKey, canvasKey) => {
    const canvas = document.getElementById(props.playerId + idSuffix);
    if (canvas) {
      canvas.width = global.screenWidth;
      canvas.height = global.screenHeight;
      global[canvasKey] = canvas;
      global[painterKey] = canvas.getContext('2d');
    }
  };
  
  setupCanvas('-track-canvas', 'trackPainter', 'trackCanvas');
  setupCanvas('-note-canvas', 'notePainter', 'noteCanvas');
  setupCanvas('-judge-canvas', 'judgePainter', 'judgeCanvas');
  
  repaint();
};

const repaint = () => {
  if (global.notePainter) global.notePainter.clearRect(0, 0, global.screenWidth, global.screenHeight);
  if (global.trackPainter) global.trackPainter.clearRect(0, 0, global.screenWidth, global.screenHeight);
  if (global.judgePainter) global.judgePainter.clearRect(0, 0, global.screenWidth, global.screenHeight);
  global.repaint = !global.repaint;
};

const generateImagePath = () => {
  // Now handled directly in template for simplicity and fallback logic
};

const resetTrackStates = () => {
  if (!props.chart.tracks) return;
  
  props.chart.tracks.forEach((track) => {
    let index = 0;
    let last = track.notes.length;
    for (let j = track.notes.length - 1; j >= 0; j--) {
      track.notes[j].judged = false;
      if (track.notes[j].timing + global.lostTime > global.currentTime) index = j;
      if (global.currentTime < track.notes[j].timing - global.remainingTime) last = j;
    }
    track.currentNote = index;
    track.lastNote = last - 1;
    track.judges = [];
    track.judgeFinished = track.currentNote === track.notes.length;
  });
};

const handleAddCount = (param) => {
  // 更新内部统计
  global[param.key] += 1;
  if (param.type === "lost") {
    global.combo = 0;
  } else {
    global.combo++;
    global.maxCombo = Math.max(global.maxCombo, global.combo);
  }
  // 向外抛出事件
  emit('add-count', param);
};

const handleAudioLoaded = () => {
  if (audioRef.value) {
    emit('audio-loaded', audioRef.value);
  }
};

const handleImageLoaded = () => {
  imageLoadedCount.value++;
  if (imageLoadedCount.value >= imagePath.value.length) {
    emit('image-loaded');
  }
};

// 游戏循环
let animationId = null;
const run = () => {
  if (!isRunning.value) return;
  
  if (audioRef.value) {
    global.currentTime = Math.floor(audioRef.value.currentTime * 1000);
  }

  // 编辑器区域限制逻辑
  const endLimit = props.displayRange[1] === -1 ? (props.chart.songLength || Infinity) : props.displayRange[1];
  if (global.currentTime >= endLimit) {
    pause();
    if (props.mode === 'play') {
      emit('finished');
    }
  }

  emit('time-update', global.currentTime);
  repaint();
  animationId = requestAnimationFrame(run);
};

// 公开接口 (Exposed)
const play = () => {
  if (audioRef.value) {
    isRunning.value = true;
    audioRef.value.play();
    run();
  }
};

const pause = () => {
  if (audioRef.value) {
    isRunning.value = false;
    audioRef.value.pause();
    if (animationId) cancelAnimationFrame(animationId);
  }
};

const seek = (time) => {
  global.currentTime = time;
  if (audioRef.value) audioRef.value.currentTime = time / 1000;
  resetTrackStates();
  repaint();
  emit('time-update', global.currentTime);
};

const reStart = () => {
  const startTime = props.displayRange[0] || 0;
  global.score = 0;
  global.pureCount = 0;
  global.farCount = 0;
  global.lostCount = 0;
  global.combo = 0;
  global.maxCombo = 0;
  seek(startTime);
  if (isRunning.value) play();
};

// 初始化逻辑
onMounted(() => {
  generateImagePath();
  resize();
  window.addEventListener('resize', resize);
  
  // 处理键盘输入
  const handleKeyDown = (e) => {
    if (!e.repeat) {
      const key = e.key.toUpperCase();
      global.keyPressTime[key] = global.currentTime;
      global.keyIsHold[key] = true;
      global.keyUsed[key] = false;
    }
  };
  
  const handleKeyUp = (e) => {
    const key = e.key.toUpperCase();
    global.keyIsHold[key] = false;
  };

  document.addEventListener('keydown', handleKeyDown);
  document.addEventListener('keyup', handleKeyUp);
  
  // 自动重绘和音频同步：当非播放状态下 currentTime 改变时
  watch(() => global.currentTime, (newTime) => {
    if (!isRunning.value) {
      if (audioRef.value) {
        const diff = Math.abs(audioRef.value.currentTime * 1000 - newTime);
        if (diff > 10) { 
          audioRef.value.currentTime = newTime / 1000;
        }
      }
      repaint();
    }
  });

  watch(() => props.chart.songId, () => {
    generateImagePath();
    resetTrackStates();
    repaint();
  });

  watch(() => props.volume, (newVol) => {
    if (audioRef.value) {
      audioRef.value.volume = newVol / 100;
    }
  }, { immediate: true });

  // 处理轨道点击 (编辑器功能)
  const container = document.getElementById(props.playerId);
  if (container) {
    container.onmousedown = (e) => {
      if (props.mode !== 'edit') return;
      
      const rect = container.getBoundingClientRect();
      const offsetX = e.clientX - rect.left;
      
      const visibleTracks = props.chart.tracks.filter(t => 
        global.currentTime > t.startTiming && 
        global.currentTime < t.endTiming
      );
      
      for (const track of visibleTracks) {
        const left = (track.tempPositionX - track.tempWidth) * global.screenWidth;
        const right = (track.tempPositionX + track.tempWidth) * global.screenWidth;
        if (offsetX > left && offsetX < right) {
          emit('track-click', track);
          break;
        }
      }
    };
  }

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resize);
    document.removeEventListener('keydown', handleKeyDown);
    document.removeEventListener('keyup', handleKeyUp);
    if (animationId) cancelAnimationFrame(animationId);
  });
});

// 暴露给外部调用
defineExpose({
  play,
  pause,
  seek,
  reStart,
  resize,
  repaint,
  global // 暴露给父组件读取分数统计
});
</script>

<style scoped>
.beat-player-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #000;
}

.background-image {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  object-fit: cover; /* Changed from fill to cover for better look */
  pointer-events: none;
}

.background-overlay {
  z-index: 1;
}

.judgment-line-container {
  height: 2px;
  position: absolute;
  left: 0px;
  z-index: 5;
  pointer-events: none;
}

.white-line {
  height: 100%;
  position: absolute;
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
}

.player-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 4;
}

.track-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 3;
}

.selected-track-overlay {
  pointer-events: none;
}
</style>
