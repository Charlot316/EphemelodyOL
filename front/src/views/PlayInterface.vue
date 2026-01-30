<template>
  <div class="play-interface select" id="play-interface-container">
    <Prepare
      :loadingStatus="loadingStatus"
      :chart="chart"
      @startMusic="startMusic"
    />
    <Play
      :loadingStatus="loadingStatus"
      :chart="chart"
      :global="global"
      :imagePath="imagePath"
      :whiteLineLength="whiteLineLength"
      :score="score"
      @audioLoaded="audioLoaded"
      @imageLoaded="imageLoaded"
      @addCount="addCount"
      @pause="pause"
      @restart="reStart"
      @continuePlay="continuePlay"
    />
    <Result :loadingStatus="loadingStatus" :chart="chart" :global="global" />
    <el-dialog
      v-model="pauseVisible"
      title="暂停"
      top="30vh"
      :center="true"
      :show-close="false"
      :close-on-press-escape="false"
      :close-on-click-modal="false"
    >
      <div style="text-align: center;">
        <el-button
          icon="el-icon-caret-left"
          @click="$router.go(-1)"
          circle
        ></el-button>
        <el-button
          icon="el-icon-refresh-left"
          circle
          @click="reStart"
        ></el-button>
        <el-button
          icon="el-icon-caret-right"
          circle
          @click="continuePlay"
        ></el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount, getCurrentInstance } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { ElNotification } from 'element-plus';
import "animate.css";
import Play from "@/components/game/Play.vue";
import Prepare from "@/components/game/Prepare.vue";
import Result from "@/components/game/Result.vue";
import { Axios as $http } from "@/plugins/axios";

const route = useRoute();
const router = useRouter();
const store = useStore();

const chart = ref({
  songLength: 0,
});

const global = reactive({
  screenWidth: 0,
  screenHeight: 0,
  remainingTime: 1000,
  finalY: 0.8,
  currentTime: 0,
  lostTime: 150,
  pureTime: 50,
  farTime: 100,
  isEdit: false,
  keyPressTime: {},
  keyIsHold: {},
  keyUsed: {},
  keyMap: {},
  pureCount: 0,
  farCount: 0,
  lostCount: 0,
  combo: 0,
  maxCombo: 0,
  score: 0,
  notePainter: null,
  trackPainter: null,
  judgePainter: null,
  noteCanvas: null,
  trackCanvas: null,
  judgeCanvas: null,
  repaint: false,
});

const imagePath = ref([]);
const pauseVisible = ref(false);
const audio = ref(null);
const playInterface = ref(null);

const loadingStatus = reactive({
  chart: false,
  audio: false,
  image: false,
  canRun: false,
  runReady: false,
  runStart: false,
  beforeFinished: false,
  finished: false,
  imageCurrentCount: 0,
});

const whiteLineLength = computed(() => {
  const time = 200;
  const waitLoad = 1000;
  if (
    global.currentTime > time + waitLoad &&
    global.currentTime < chart.value.songLength - time
  ) {
    return global.screenWidth;
  } else {
    if (global.currentTime <= time + waitLoad) {
      if (global.currentTime < waitLoad) {
        return 0;
      } else
        return (
          ((global.currentTime - waitLoad) * global.screenWidth) /
          time
        );
    } else {
      return (
        ((chart.value.songLength - global.currentTime) *
          global.screenWidth) /
        time
      );
    }
  }
});

const score = computed(() => {
  if (global.score)
    return global.score.toString().padStart(8, "0");
  else return "00000000";
});

const calculateScore = () => {
  let singleScore = 10000000 / chart.value.notesCount;
  global.score = Math.floor(
    global.pureCount * singleScore +
      global.farCount * 0.5 * singleScore
  );
  if (global.score > 10000000) global.score = 10000000;
};

watch(() => global.pureCount, calculateScore);
watch(() => global.farCount, calculateScore);

const resize = () => {
  playInterface.value = document.getElementById("play-interface-container");
  global.screenWidth = document.documentElement.clientWidth;
  global.screenHeight = document.documentElement.clientHeight;
  if (global.noteCanvas) {
    global.noteCanvas.height = playInterface.value.offsetHeight;
    global.noteCanvas.width = playInterface.value.offsetWidth;
  }
  if (global.trackCanvas) {
    global.trackCanvas.height = playInterface.value.offsetHeight;
    global.trackCanvas.width = playInterface.value.offsetWidth;
  }
  if (global.judgeCanvas) {
    global.judgeCanvas.height = playInterface.value.offsetHeight;
    global.judgeCanvas.width = playInterface.value.offsetWidth;
  }
};

const sortTrack = () => {
  if (chart.value.tracks) {
    chart.value.tracks.sort((a, b) => a.startTiming - b.startTiming);
  }
};

const generateImagePath = () => {
  imagePath.value = [];
  if (!chart.value.changeBackgroundOperations) return;
  
  let length = chart.value.changeBackgroundOperations.length;
  let start = 0;
  let end = start;
  let defaultBackground = chart.value.defaultBackground;
  
  if (length === 0) {
    end = chart.value.songLength;
  } else {
    chart.value.changeBackgroundOperations.sort((a, b) => a.startTime - b.startTime);
    end = chart.value.changeBackgroundOperations[0].startTime;
  }
  
  imagePath.value.push({
    url: defaultBackground,
    startTime: 0,
    endTime: end,
  });
  
  for (let i = 0; i < length; i++) {
    let operation = chart.value.changeBackgroundOperations[i];
    start = operation.startTime;
    if (i !== length - 1) {
      let nextOperation = chart.value.changeBackgroundOperations[i + 1];
      imagePath.value.push({
        url: operation.background,
        startTime: start,
        endTime: nextOperation.startTime,
      });
    } else {
      imagePath.value.push({
        url: operation.background,
        startTime: start,
        endTime: chart.value.songLength + 1000,
      });
    }
  }
};

const getChart = async () => {
  try {
    const { data: res } = await $http.get(
      `/user/getChart?songId=${route.query.songId}`
    );
    if (res.code !== 0) {
      ElNotification({
        title: "失败",
        message: "获取谱面失败！",
        type: "error",
      });
      return;
    }
    chart.value = res.data;
    loadingStatus.chart = true;
    sortTrack();
    generateImagePath();
  } catch (err) {
    ElNotification({
      title: "错误",
      message: "网络异常",
      type: "error",
    });
  }
};

const finish = async () => {
  try {
    const { data: res } = await $http.post("/play/uploadRecord", {
      score: global.score,
      songId: route.query.songId,
      pure: global.pureCount,
      far: global.farCount,
      lost: global.lostCount,
      combo: global.maxCombo,
    });
    if (res.code !== 0) {
      ElNotification({
        title: "失败",
        message: "成绩上传失败！",
        type: "error",
      });
    }
    global.formerBestScore = res.data.formerBestScore;
    store.commit("changeParam", {
      key: "potential",
      value: res.data.potential,
    });
  } catch (err) {
    ElNotification({
      title: "错误",
      message: "网络异常",
      type: "error",
    });
  }
};

let runAnimationId = null;
const run = () => {
  if (
    document.documentElement.clientWidth !== global.screenWidth ||
    document.documentElement.clientHeight !== global.screenHeight
  ) {
    resize();
  }
  if (global.notePainter) {
    global.notePainter.clearRect(0, 0, global.noteCanvas.width, global.noteCanvas.height);
  }
  if (global.trackPainter) {
    global.trackPainter.clearRect(0, 0, global.trackCanvas.width, global.trackCanvas.height);
  }
  if (global.judgePainter) {
    global.judgePainter.clearRect(0, 0, global.judgeCanvas.width, global.judgeCanvas.height);
  }
  
  if (audio.value) {
    global.currentTime = Math.floor(audio.value.currentTime * 1000);
  }
  
  if (global.currentTime < chart.value.songLength - 150) {
    setTimeout(run, 10);
  } else {
    global.currentTime = chart.value.songLength;
    loadingStatus.beforeFinished = true;
    calculateScore();
    finish();
    setTimeout(() => {
      loadingStatus.finished = true;
    }, 2000);
  }
};

const audioLoaded = () => {
  audio.value = document.getElementById("audioSong");
  chart.value.songLength = Math.round(1000 * audio.value.duration);
  generateImagePath();
  loadingStatus.audio = true;
  checkIfLoaded();
};

const imageLoaded = () => {
  loadingStatus.imageCurrentCount++;
  if (loadingStatus.imageCurrentCount === imagePath.value.length) {
    loadingStatus.image = true;
    checkIfLoaded();
  }
};

const checkIfLoaded = () => {
  if (
    loadingStatus.chart &&
    loadingStatus.audio &&
    loadingStatus.image
  ) {
    setTimeout(() => {
      loadingStatus.canRun = true;
    }, 1000);
  }
};

const startMusic = () => {
  if (loadingStatus.canRun && audio.value) {
    audio.value.play();
    audio.value.muted = true;
    loadingStatus.runReady = true;
    setTimeout(() => {
      loadingStatus.runStart = true;
      audio.value.currentTime = 0;
      audio.value.muted = false;
      audio.value.volume = store.state.volume / 100;
      run();
    }, 500);
  }
};

const addCount = (param) => {
  global[param.key] += 1;
  if (param.type === "lost") {
    global.combo = 0;
  } else {
    global.combo++;
    global.maxCombo = Math.max(global.maxCombo, global.combo);
  }
};

const pause = () => {
  if (global.currentTime < chart.value.songLength && audio.value) {
    audio.value.pause();
    pauseVisible.value = true;
  }
};

const continuePlay = () => {
  pauseVisible.value = false;
  if (audio.value) audio.value.play();
};

const resetTrack = () => {
  global.keyPressTime = {};
  global.keyIsHold = {};
  global.keyUsed = {};
  
  if (!chart.value.tracks) return;
  
  for (let i = 0; i < chart.value.tracks.length; i++) {
    let track = chart.value.tracks[i];
    let index = 0;
    let last = track.notes.length;
    for (let j = track.notes.length - 1; j >= 0; j--) {
      track.notes[j].judged = false;
      if (
        track.notes[j].timing + global.lostTime >
        global.currentTime
      ) {
        index = j;
      }
      if (
        global.currentTime <
        track.notes[j].timing - global.remainingTime
      ) {
        last = j;
      }
    }
    track.judges = [];
    track.currentNote = index;
    track.lastNote = last - 1;
    track.judgeFinished = track.currentNote === track.notes.length;
  }
};

const reStart = () => {
  pauseVisible.value = false;
  global.keyPressTime = {};
  global.keyIsHold = {};
  global.keyUsed = {};
  global.keyMap = {};
  global.pureCount = 0;
  global.farCount = 0;
  global.lostCount = 0;
  global.combo = 0;
  global.maxCombo = 0;
  global.score = 0;
  resetTrack();
  if (audio.value) {
    audio.value.currentTime = 0;
    audio.value.play();
  }
};

onMounted(() => {
  global.noteCanvas = document.getElementById("note-canvas");
  global.trackCanvas = document.getElementById("track-canvas");
  global.judgeCanvas = document.getElementById("judge-canvas");
  global.notePainter = global.noteCanvas.getContext("2d");
  global.trackPainter = global.trackCanvas.getContext("2d");
  global.judgePainter = global.judgeCanvas.getContext("2d");
  
  resize();
  window.addEventListener('resize', resize);
  
  document.onkeydown = (e) => {
    if (!e.repeat) {
      global.keyPressTime[e.key.toUpperCase()] = global.currentTime;
      global.keyIsHold[e.key.toUpperCase()] = true;
      global.keyUsed[e.key.toUpperCase()] = false;
    }
    if (e.key === "Escape") pause();
    if (e.key === "Enter") continuePlay();
  };
  
  document.onkeyup = (e) => {
    global.keyIsHold[e.key.toUpperCase()] = false;
  };
  
  const container = document.getElementById("play-interface-container");
  container.ontouchstart = (e) => {
    if (chart.value.tracks) {
      let currentTime = global.currentTime;
      let currentTracks = chart.value.tracks.filter(track => 
        currentTime > track.startTiming && 
        currentTime < track.endTiming && 
        track.type === 1
      );
      
      for (let j = 0; j < e.touches.length; j++) {
        for (let k = 0; k < currentTracks.length; k++) {
          let track = currentTracks[k];
          let touch = e.touches[j];
          let left = (parseFloat(track.tempPositionX) - parseFloat(track.tempWidth)) * global.screenWidth;
          let right = (parseFloat(track.tempPositionX) + parseFloat(track.tempWidth)) * global.screenWidth;
          
          if (touch.pageX > left && touch.pageX < right) {
            let key = track.key.toUpperCase();
            global.keyPressTime[key] = currentTime;
            global.keyIsHold[key] = true;
            global.keyUsed[key] = false;
            global.keyMap[touch.identifier] = key;
          }
        }
      }
    }
    if (e.touches.length > 1) e.preventDefault();
  };
  
  let lastTouchEnd = 0;
  container.ontouchend = (e) => {
    let now = new Date().getTime();
    if (now - lastTouchEnd <= 300) e.preventDefault();
    lastTouchEnd = now;
    if (chart.value.tracks) {
      for (let j = 0; j < e.changedTouches.length; j++) {
        let touch = e.changedTouches[j];
        global.keyIsHold[global.keyMap[touch.identifier]] = false;
      }
    }
  };
  
  getChart();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize);
  document.onkeydown = null;
  document.onkeyup = null;
});
</script>

<style scoped>
.play-interface {
  height: 100vh;
  width: 100vw;
  background: white;
  overflow: auto;
}
.select {
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}
</style>
