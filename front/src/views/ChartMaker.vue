<template>
  <div class="play-interface select">
    <!-- 侧边栏 -->
    <transition
      name="fade"
      enter-active-class="animate__animated animate__fadeInLeft"
      leave-active-class="animate__animated animate__fadeOutLeft"
    >
      <div
        v-if="menuOpened"
        :class="menuOpened ? 'sider-opened' : 'sider-closed'"
        :style="siderStyle"
      >
        <MenuPanel
          key="menupanel"
          :Height="global.documentHeight - footerHeight"
          :footerHeight="footerHeight"
          :global="global"
          :chart="chart"
        /></div>
    </transition>

    <!-- 谱面展示 -->
    <div class="select">
      <div
        :class="menuOpened ? 'container-small' : 'container-big'"
        id="play-interface-container"
        :style="containerStyle"
      >
        <BeatPlayer
          ref="playerRef"
          :chart="chart"
          :global="global"
          mode="edit"
          :selectedTrackId="currentSelectTrack ? (currentSelectTrack.trackId || currentSelectTrack.index) : null"
          :displayRange="[displayStart, displayEnd]"
          :volume="volume"
          @track-click="handleTrackClick"
          @audio-loaded="onAudioLoaded"
          playerId="editor-player"
        />
      </div>
    </div>

    <!-- 进度条 -->
    <div
      v-if="chartGot"
      :class="menuOpened ? 'time-controller-small' : 'time-controller-big'"
      :style="containerStyle"
    >
      <div class="time-control-buttons">
        <el-button
          size="small"
          class="header-button"
          @click="changeMenuDisplay"
          type="text"
        >
          <el-icon><FullScreen /></el-icon>
        </el-button>
        <el-button
          size="small"
          @click="reStart"
          class="header-button"
          type="text"
        >
          <el-icon><RefreshLeft /></el-icon>
        </el-button>
        <el-button
          v-if="isRunning"
          size="small"
          class="header-button"
          @click="pause"
          type="text"
        >
          <el-icon><VideoPause /></el-icon>
        </el-button>
        <el-button
          v-else
          size="small"
          class="header-button"
          @click="play"
          type="text"
        >
          <el-icon><VideoPlay /></el-icon>
        </el-button>
        <el-button
          size="small"
          type="text"
          class="header-button"
          @click="globalSetting = true"
        >
          <el-icon><Setting /></el-icon>
        </el-button>
      </div>
      <div class="header-slide">
        <div class="header-slide-item">
          <el-slider
            v-model="global.currentTime"
            :min="displayStart"
            :max="displayEnd"
            @change="changeTime"
            @mousedown="SlideMouseDown"
            @mouseup="SlideMouseUp"
            @touchstart="SlideMouseDown"
            @touchend="SlideMouseUp"
          ></el-slider>
        </div>
      </div>
      <el-dialog
        v-model="globalSetting"
        @close="checkbpm"
        width="650px"
        title="全局设置"
      >
        <el-form :model="form" label-width="200px" style="padding: 20px;">
          <el-form-item label="音量">
            <el-input-number
              v-model="volume"
              :min="0"
              :max="100"
              @change="changeVolume"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="按键盘上下键同样可以调节音量"
              placement="top-start"
              style="margin-left:10px;"
            >
              <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item label="首拍偏移(单位:ms)">
            <el-input-number
              v-model="chart.firstBeatDelay"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="第一拍的偏移，在一拍间隔部分可以顺带测量，你也可以选择使用音频软件观察声波自行填写"
              placement="top-start"
            >
              <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item label="末拍偏移(单位:ms)">
            <el-input-number
              v-model="chart.lastBeatDelay"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="最后一拍的偏移，你可以填写好精确的第一拍和最后一拍的偏移和节拍数，然后直接计算出一拍间隔"
              placement="top-start"
            >
              <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item label="节拍数">
            <el-input-number
              v-model="chart.beatsCount"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="用以计算一拍间隔"
              placement="top-start"
            >
              <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
            <div style="margin-top:15px; width: 100%;">
              <el-button @click="ManualCalculatebpm">
                精确计算一拍间隔
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="一拍间隔(单位:ms)">
            <el-input-number
              v-model="chart.bpm"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="一个节拍的长度"
              placement="top-start"
            >
              <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
            <div style="margin-top:15px; width: 100%;">
              <el-button @mousedown="calculatebpm">{{
                !bpmStart ? "粗略估算一拍间隔" : "请在节奏点处按下"
              }}</el-button>
              <el-button @click="endbpm" v-if="bpmStart">
                结束或重新测量
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="显示时间区域">
            <div style="display: flex; gap: 10px;">
              <el-input-number v-model="displayStart" :min="0" :max="chart.songLength" placeholder="开始" />
              <el-input-number v-model="displayEnd" :min="0" :max="chart.songLength" placeholder="结束" />
            </div>
          </el-form-item>
          <el-form-item label="">
            <el-slider
              v-model="displayRange"
              range
              :min="0"
              :max="chart.songLength"
              @change="changeDisplayArea"
            ></el-slider>
          </el-form-item>
        </el-form>
      </el-dialog>
    </div>

    <!-- 时间轴 -->
    <transition
      name="fade"
      enter-active-class="animate__animated animate__fadeInUp"
      leave-active-class="animate__animated animate__fadeOutDown"
    >
      <div
        v-if="menuOpened && chartGot"
        :class="menuOpened ? 'footer-opened' : 'footer-closed'"
        :style="footerStyle"
      >
        <div
          class="footer-resizer-bar"
          id="footer-resizer"
          @mousedown="canDrag = true"
        >
          <div class="drag-handle"></div>
        </div>
        <div class="footer-component-wrapper">
          <Footer
            :chart="chart"
            :global="global"
            @currentTrack="handleCurrentTrack"
          />
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount, provide } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { Axios } from '@/plugins/axios';
import { ElNotification } from 'element-plus';
import { FullScreen, RefreshLeft, VideoPause, VideoPlay, Setting, QuestionFilled } from '@element-plus/icons-vue';
import Track from "@/components/game/Track.vue";
import BeatPlayer from "@/components/game/BeatPlayer.vue";
import MenuPanel from "@/components/editor/MenuPanel.vue";
import Footer from "@/components/editor/Footer.vue";
import "animate.css";
import { useChartEditor } from '@/composables/useChartEditor';
import { useBpmTool } from '@/composables/useBpmTool';
import { useCommandHistory } from '@/composables/useCommandHistory';
import { useWebSocket } from '@/composables/useWebSocket';
import { v4 as uuidv4 } from 'uuid';

const route = useRoute();
const router = useRouter();
const store = useStore();
const playerRef = ref(null);
const canDrag = ref(false);
let resizeTimer = null;

const history = useCommandHistory();
provide('commandHistory', history);

const {
  chart,
  chartGot,
  isRunning,
  sliding,
  displayStart,
  displayEnd,
  displayRange,
  sortTrack,
  getChart,
  saveChart,
  onAudioLoaded,
  onlineUsers,
  onlineCount,
  resetChart,
  syncAction
} = useChartEditor(route, router);

provide('syncAction', syncAction);
provide('uuid', uuidv4);
provide('onlineUsers', onlineUsers);
provide('onlineCount', onlineCount);
provide('resetChart', resetChart);
provide('saveChart', saveChart);
provide('router', router);

const global = reactive({
  currentTime: 0,
  beatLine: true,
  screenWidth: 0,
  screenHeight: 0,
  remainingTime: 700,
  finalY: 0.8,
  lostTime: 150,
  pureTime: 50,
  farTime: 100,
  isEdit: true,
  keyPressTime: {},
  keyIsHold: {},
  keyUsed: {},
  notePainter: null,
  trackPainter: null,
  judgePainter: null,
  noteCanvas: null,
  trackCanvas: null,
  judgeCanvas: null,
  repaint: false,
  reCalculateTrack: false,
  reCalculateChartMaker: false,
  mouseDown: false,
  mouseUp: true,
  mouseMove: false,
  clientX: 0,
  clientY: 0,
  timeSort: true,
  documentHeight: 0,
  documentWidth: 0
});

const menuOpened = ref(true);
const volume = ref(store.state.volume || 100);
const currentSelectTrack = ref(null);
const currentTracks = ref([]);
const globalSetting = ref(false);
const footerHeight = ref(426);
const form = reactive({});

// Define functions before usage in composables to avoid TDZ issues
function pause() {
  playerRef.value?.pause();
  isRunning.value = false;
}

function play() {
  sliding.value = false;
  isRunning.value = true;
  playerRef.value?.play();
}

function changeVolume() {
  // Volume is now handled via prop and watcher in BeatPlayer,
  // but we keep the value in state.
  volume.value = Math.max(0, Math.min(100, volume.value));
}

const {
  bpmStart,
  bpmcount,
  ManualCalculatebpm,
  calculatebpm,
  endbpm
} = useBpmTool(chart, global, {
  play,
  pause,
  seek: (t) => playerRef.value?.seek(t)
});

const siderStyle = computed(() => ({
  '--footerHeight': footerHeight.value + 'px',
  '--documentHeight': global.documentHeight + 'px',
}));

const containerStyle = computed(() => ({
  '--footerHeight': footerHeight.value + 'px',
  '--documentHeight': global.documentHeight + 'px',
}));

const footerStyle = computed(() => ({
  '--footerHeight': footerHeight.value + 'px',
  '--documentHeight': global.documentHeight + 'px',
}));

const changeTime = () => {
  global.currentTime = Math.floor(global.currentTime);
  playerRef.value?.seek(global.currentTime);
  document.querySelector("#time-indicater")?.scrollIntoView({ behavior: "auto", block: "nearest", inline: "center" });
};

const SlideMouseDown = () => {
  pause();
  sliding.value = true;
};

const SlideMouseUp = () => {
  sliding.value = false;
  playerRef.value?.seek(global.currentTime);
  if (isRunning.value) {
    play();
  }
};

// Redundant declarations removed as they are moved up

const reStart = () => {
  playerRef.value?.reStart();
};

const changeDisplayArea = (val) => {
  displayStart.value = val[0];
  displayEnd.value = val[1];
  playerRef.value?.seek(displayStart.value);
};

const checkbpm = () => {
  endbpm();
  if (!chart.bpm || chart.bpm === 0) {
    setTimeout(() => {
      ElNotification({ title: "提示", message: "请设置节拍", type: "warning" });
      globalSetting.value = true;
    }, 1000);
  }
};

watch(() => global.currentTime, () => {
  if (sliding.value) {
    // 拖拽时使用 auto 滚动，避免 smooth 滚动的动画开销和延迟感
    document.querySelector("#time-indicater")?.scrollIntoView({ behavior: "auto", block: "nearest", inline: "center" });
  }
});

const handleTrackClick = (track) => {
  if (currentSelectTrack.value) currentSelectTrack.value.edit = false;
  currentSelectTrack.value = track;
  document.querySelector("#trackCard" + track.index)?.scrollIntoView({ behavior: "auto" });
  document.querySelector("#trackCardPanel" + track.index)?.scrollIntoView({ behavior: "auto" });
  track.edit = true;
};

const handleCurrentTrack = (track) => {
  if (currentSelectTrack.value) currentSelectTrack.value.edit = false;
  currentSelectTrack.value = track;
};

watch(() => global.reCalculateChartMaker, () => {
  sortTrack();
  playerRef.value?.seek(global.currentTime);
});

onMounted(() => {
  global.documentHeight = document.documentElement.clientHeight;
  global.documentWidth = document.documentElement.clientWidth;
  
  window.onkeydown = (e) => {
    // Undo/Redo Shortcuts
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z') {
      e.preventDefault();
      if (e.shiftKey) history.redo();
      else history.undo();
      return;
    }
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'y') {
       e.preventDefault();
       history.redo();
       return;
    }

    if (!e.repeat) {
      global.keyPressTime[e.key.toUpperCase()] = global.currentTime;
      global.keyIsHold[e.key.toUpperCase()] = true;
      global.keyUsed[e.key.toUpperCase()] = false;
      if (e.key === " ") {
        isRunning.value ? pause() : play();
      }
    }
    if (e.key === "ArrowUp") {
      volume.value = Math.min(100, volume.value + 10);
      changeVolume();
    } else if (e.key === "ArrowDown") {
      volume.value = Math.max(0, volume.value - 10);
      changeVolume();
    }
  };

  document.onkeyup = (e) => {
    global.keyIsHold[e.key.toUpperCase()] = false;
  };

  // Click selection is now handled by BeatPlayer emitting track-click

  document.onmousemove = (e) => {
    global.clientX = e.clientX;
    global.clientY = e.clientY;
    global.mouseMove = !global.mouseMove;
    if (canDrag.value) {
      if (e.clientY > 130 && e.clientY < global.documentHeight - 100) {
        footerHeight.value = global.documentHeight - e.clientY;
        
        // Debounce resize for performance
        if (resizeTimer) clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
          playerRef.value?.resize();
          playerRef.value?.repaint();
        }, 50); // 50ms buffer for smooth dragging
      }
    }
  };

  document.onmouseup = () => {
    if (sliding.value) SlideMouseUp();
    canDrag.value = false;
    global.mouseUp = !global.mouseUp;
  };

  document.onmousedown = (e) => {
    global.clientX = e.clientX;
    global.clientY = e.clientY;
    global.mouseDown = !global.mouseDown;
  };

  getChart(() => {
    globalSetting.value = true;
    history.clear(); // Clear history after loading chart
  });
  
  setTimeout(() => {
    playerRef.value?.resize();
  }, 1000);
});

onBeforeUnmount(() => {
  window.onkeydown = null;
  window.onkeyup = null;
});
</script>

<style scoped>
.animate__animated.animate__fadeInLeft { --animate-duration: 0.5s; }
.animate__animated.animate__fadeOutLeft { --animate-duration: 0.5s; }
.animate__animated.animate__fadeInUp { --animate-duration: 0.5s; }
.animate__animated.animate__fadeOutDown { --animate-duration: 0.5s; }

.play-interface {
  height: 100vh;
  width: 100vw;
  background: rgb(55, 55, 55);
  overflow: hidden; /* Changed from auto to prevent layout breakdown */
}
.select {
  user-select: none;
}
.header-button { color: white; }
.header-button:hover { color: rgb(234, 234, 234); }
.header-button:active { color: rgb(212, 212, 212); }

.header {
  position: absolute;
  top: 0;
  height: 50px;
  width: 100%;
  background: rgb(39, 39, 39);
  z-index: 100;
}
.header-buttons {
  padding: 10px 10px 0px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#play-interface-container {
  position: absolute;
  top: 0px;
  background: rgb(32, 32, 32);
}

.sider-closed {
  position: absolute;
  top: 0px;
  height: calc(var(--documentHeight) - var(--footerHeight));
  background: rgb(32, 32, 32);
  width: 0px;
  left: 0px;
}
.sider-opened {
  position: absolute;
  top: 0px;
  height: calc(var(--documentHeight) - var(--footerHeight));
  background: rgb(32, 32, 32);
  width: 400px;
  left: 0px;
  overflow: auto;
  padding-bottom: 20px;
  z-index: 50;
}

.sider-closed-track {
  display: none;
}
.sider-opened-track {
  display: none;
}

.footer-closed {
  position: absolute;
  bottom: 0px;
  height: 0px;
  width: 100vw;
  left: 0px;
}
.footer-opened {
  position: absolute;
  bottom: 0px;
  height: var(--footerHeight);
  background: rgb(55, 55, 55);
  width: 100vw;
  left: 0px;
  z-index: 60;
}

.container-small {
  left: 400px;
  top: 0px;
  width: calc(100vw - 400px);
  height: calc(var(--documentHeight) - 80px - var(--footerHeight));
}
.container-big {
  left: 0px;
  top: 0px;
  width: 100vw;
  height: calc(var(--documentHeight) - 120px);
  transition: 0.5s;
}

.time-controller-small {
  position: absolute;
  left: 400px;
  bottom: var(--footerHeight);
  height: 80px;
  width: calc(100vw - 400px);
  background: rgb(32, 30, 32);
  z-index: 70;
}
.time-controller-big {
  position: absolute;
  left: 0px;
  bottom: 0px;
  height: 80px;
  width: 100vw;
  background: rgb(32, 30, 32);
  z-index: 70;
}
.time-control-buttons {
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}
.time-control-buttons .el-button {
  font-size: 24px;
}
.header-slide-item {
  width: 96%;
  padding: 0px 2%;
}

.selected-track {
  box-shadow: 0 0 20px 10px rgba(0, 0, 0, 0.5);
  pointer-events: none;
}

:deep(.el-slider__bar) { background-color: rgb(138, 138, 138); }
:deep(.el-slider__button) { border: 2px solid rgb(138, 138, 138); }

.footer-resizer-bar {
  height: 8px;
  width: 100%;
  cursor: ns-resize;
  background: rgba(255, 255, 255, 0.02);
  display: flex;
  justify-content: center;
  align-items: center;
  transition: background 0.2s;
}
.footer-resizer-bar:hover {
  background: rgba(255, 255, 255, 0.08);
}
.drag-handle {
  width: 40px;
  height: 3px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.2);
}
.footer-resizer-bar:hover .drag-handle {
  background: rgba(255, 255, 255, 0.5);
}
.footer-component-wrapper {
  height: calc(100% - 8px);
  width: 100%;
}
</style>
