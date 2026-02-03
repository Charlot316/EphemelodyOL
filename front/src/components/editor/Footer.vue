<template>
  <div class="footer-container">
    <div class="footer-header">
      <div class="footer-toolbar">
        <!-- 分组 1: 视图与排序 -->
        <div class="toolbar-group">
          <el-tooltip content="切换排序方式" placement="top">
            <el-button
              circle
              size="small"
              class="tool-btn"
              @click="global.timeSort = !global.timeSort; updateTrack();"
            >
              <el-icon><Sort v-if="!global.timeSort"/><Timer v-else/></el-icon>
            </el-button>
          </el-tooltip>

          <el-tooltip :content="showNoRemain ? '过滤无音符轨道' : '显示所有轨道'" placement="top">
            <el-button
              circle
              size="small"
              :class="['tool-btn', { 'is-active': !showNoRemain }]"
              @click="showNoRemain = !showNoRemain"
            >
              <el-icon><Filter /></el-icon>
            </el-button>
          </el-tooltip>

          <el-tooltip :content="showCurrent ? '显示完整列表' : '只看当前时机轨道'" placement="top">
            <el-button
              circle
              size="small"
              :class="['tool-btn', { 'is-active': showCurrent }]"
              @click="showCurrent = !showCurrent"
            >
              <el-icon><Aim /></el-icon>
            </el-button>
          </el-tooltip>

          <el-tooltip :content="autoScroll ? '关闭自动跟随' : '开启自动跟随'" placement="top">
            <el-button
              circle
              size="small"
              :class="['tool-btn', { 'is-active': autoScroll }]"
              @click="autoScroll = !autoScroll"
            >
              <el-icon><Compass /></el-icon>
            </el-button>
          </el-tooltip>
        </div>

        <div class="toolbar-divider"></div>

        <!-- 分组 2: 轨道类型过滤 -->
        <div class="toolbar-group">
          <el-button
            size="small"
            class="toggle-btn"
            :class="{ 'is-active': showReal }"
            @click="showReal = !showReal"
          >
            <el-icon><CircleCheck /></el-icon>实轨
          </el-button>
          <el-button
            size="small"
            class="toggle-btn"
            :class="{ 'is-active': showFake }"
            @click="showFake = !showFake"
          >
            <el-icon><MagicStick /></el-icon>虚轨
          </el-button>
        </div>

        <div class="toolbar-divider"></div>

        <!-- 分组 3: 点击行为设置 -->
        <div class="toolbar-group mode-selector">
          <el-button
            v-for="(label, idx) in ['短键', '长键', '滑键']"
            :key="idx"
            size="small"
            :class="['mode-btn', { 'is-selected': currentNoteType == idx }]"
            @click="currentNoteType = idx"
          >
            <span class="mode-dot" :class="'mode-' + idx"></span>
            {{ label }}
          </el-button>
          <el-button
            size="small"
            class="delete-mode-btn"
            :class="{ 'is-selected': currentNoteType == 3 }"
            @click="currentNoteType = 3"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>

        <div class="toolbar-divider"></div>

        <!-- 分组 4: 杂项 -->
        <div class="toolbar-group">
          <el-button
            size="small"
            class="tool-btn-rect"
            :class="{ 'is-warning': !enableEdit }"
            @click="enableEdit = !enableEdit"
          >
            <el-icon><EditPen /></el-icon>
            {{ enableEdit ? '编辑弹窗:开' : '编辑弹窗:关' }}
          </el-button>
        </div>
      </div>

      <div class="footer-header-right">
        <div class="time-display-wrapper">
          <span class="current-time">{{ Math.floor(global.currentTime) }}</span>
          <span class="time-separator">/</span>
          <span class="total-time">{{ Math.floor(chart.songLength) }}</span>
          <span class="unit">ms</span>
        </div>
        <div class="toolbar-divider"></div>
        <div class="custom-slider-container">
          <el-icon><ZoomIn /></el-icon>
          <input 
            type="range" 
            v-model.number="displayAreaTime" 
            :min="1000" 
            :max="chart.songLength > 1000 ? chart.songLength : 1001"
            class="custom-range"
          />
        </div>
      </div>
    </div>
    <div v-if="chart.tracks">
      <div class="footer-left">
        <div
          class="footer-track-container"
          id="footer-left-scroll"
          @scroll="leftScroll"
        >
          <transition-group
            name="list"
            enter-active-class="animate__animated animate__fadeInUp"
            leave-active-class="animate__animated animate__fadeOutUp"
          >
            <div v-for="track in displayTracks.filter(isVisible)" :key="track.index">
              <TrackCard
                :chart="chart"
                :track="track"
                :global="global"
                @currentTrack="handleCurrentTrack"
              />
            </div>
          </transition-group>
        </div>
      </div>
      <div
        class="footer-right"
        id="footer-right-scroll"
        @scroll="rightScroll"
        @mousemove="rightMouseMove($event)"
        @mousedown="
          rightClick($event);
          rightClicked = true;
        "
      >
        <div
          class="beat-line-wrapper-absolute"
          :style="{ top: scrollTop + 'px' }"
        >
          <BeatLine
            :chart="chart"
            :global="global"
            :displayAreaTime="displayAreaTime"
          />
        </div>
        <div style="position:absolute;left:0;top:0;">
          <transition-group
            name="list"
            enter-active-class="animate__animated animate__fadeInUp"
            leave-active-class="animate__animated animate__fadeOutUp"
          >
            <div v-for="track in displayTracks.filter(isVisible)" :key="track.index">
              <TrackCardPanel
                :currentNoteType="currentNoteType"
                :id="'trackCardPanel' + track.index"
                :chart="chart"
                :track="track"
                :global="global"
                :scrollLeft="scrollLeft"
                :displayAreaTime="displayAreaTime"
                :enableEdit="enableEdit"
                @currentTrack="handleCurrentTrack"
              />
            </div>
          </transition-group>
        </div>

        <div
          class="time-indicater"
          id="time-indicater"
          :style="{
            width: '1px',
            background: 'rgb(255,255,0)',
            height: '100%',
            position: 'absolute',
            pointerEvents: 'none',
            top: scrollTop + 'px',
            left: (global.currentTime / displayAreaTime) * (global.documentWidth - 300) + 'px',
          }"
        ></div>
        <div
          class="time-indicater-false"
          id="time-indicater-false"
          :style="{
            width: '1px',
            background: 'rgb(255,255,255)',
            height: '100%',
            position: 'absolute',
            pointerEvents: 'none',
            top: scrollTop + 'px',
            left: indicatorLeft + 'px',
          }"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, defineProps, defineEmits, onMounted } from 'vue';
import { 
  Sort, Timer, Filter, Aim, Compass, CircleCheck, 
  MagicStick, Delete, EditPen, ZoomIn 
} from '@element-plus/icons-vue';
import TrackCard from "./TrackCard.vue";
import TrackCardPanel from "./TrackCardPanel.vue";
import BeatLine from "./BeatLine.vue";
import "animate.css";

const props = defineProps({
  chart: Object,
  global: Object
});

const emit = defineEmits(["currentTrack"]);

const scrollLeft = ref(0);
const scrollTop = ref(0);
const displayAreaTime = ref(10000);
const indicatorLeft = ref(0);
const rightClicked = ref(false);
const autoScroll = ref(false);
const showReal = ref(true);
const showFake = ref(true);
const showNoRemain = ref(true);
const currentNoteType = ref(0);
const enableEdit = ref(true);
const showCurrent = ref(false);

let rightScrollElement = null;
let audio = null;

const updateTrack = () => {
  props.global.reCalculateTrack = !props.global.reCalculateTrack;
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};

const handleCurrentTrack = (param) => {
  emit("currentTrack", param);
};

const leftScroll = () => {
  const leftElem = document.getElementById("footer-left-scroll");
  const rightElem = document.getElementById("footer-right-scroll");
  if (leftElem && rightElem) {
    rightElem.scrollTop = leftElem.scrollTop;
  }
};

const rightScroll = () => {
  const leftElem = document.getElementById("footer-left-scroll");
  const rightElem = document.getElementById("footer-right-scroll");
  if (rightElem) {
    if (leftElem) leftElem.scrollTop = rightElem.scrollTop;
    scrollLeft.value = rightElem.scrollLeft;
    scrollTop.value = rightElem.scrollTop;
  }
};

const rightClick = (e) => {
  const x = e.clientX - 300 + scrollLeft.value;
  const currentTime = (x / (props.global.documentWidth - 300)) * displayAreaTime.value;
  if (audio) audio.currentTime = currentTime / 1000;
  props.global.currentTime = currentTime;
  updateTrack();
};

const rightMouseMove = (e) => {
  const x = e.clientX - 300 + scrollLeft.value;
  indicatorLeft.value = x;
  if (rightClicked.value) {
    const currentTime = (x / (props.global.documentWidth - 300)) * displayAreaTime.value;
    if (audio) audio.currentTime = currentTime / 1000;
    props.global.currentTime = currentTime;
    updateTrack();
  }
};

const displayTracks = computed(() => {
  if (!props.chart.tracks) return [];
  const tracks = [...props.chart.tracks];
  if (props.global.timeSort) {
    tracks.sort((a, b) => a.startTiming - b.startTiming);
  } else {
    tracks.sort((a, b) => a.positionX - b.positionX);
  }
  return tracks;
});

const isVisible = (track) => {
  // If hidden via the eye icon, always hide
  if (track.showInTimeline === false) return false;
  
  const showByNote = !showNoRemain.value ? (track.notes.length > 0) : true;
  const showByType = track.type === 1 ? showReal.value : showFake.value;
  
  // Only show relevant tracks if 'Show Current Only' is active
  const showByTime = showCurrent.value 
    ? (props.global.currentTime >= track.startTiming && props.global.currentTime <= track.endTiming) 
    : true;
    
  return showByType && showByNote && showByTime;
};

watch(() => props.global.mouseUp, () => {
  rightClicked.value = false;
});

watch(() => props.global.currentTime, (newVal) => {
  if (!rightScrollElement) rightScrollElement = document.getElementById("footer-right-scroll");
  if (!audio) audio = document.getElementById("audioSong");

  if (audio && !audio.paused) {
    let sl = (newVal / displayAreaTime.value) * (props.global.documentWidth - 300) - (props.global.documentWidth - 300) / 2;
    if (sl < 0) sl = 0;
    if (rightScrollElement) {
      rightScrollElement.scrollLeft = sl;
      scrollLeft.value = sl;
    }
  }
  
  if (autoScroll.value) {
    const tracks = props.chart.tracks;
    for (let i = 0; i < tracks.length; i++) {
        const trackElem = document.querySelector("#trackCardPanel" + tracks[i].index);
        if (trackElem) {
          trackElem.scrollIntoView({ 
            behavior: "smooth",
            block: "center"
          });
        }
    }
  }
});

onMounted(() => {
  audio = document.getElementById("audioSong");
  setTimeout(() => {
    displayAreaTime.value = 10000;
  }, 100);
});
</script>

<style scoped>
.footer-container { 
  height: 100%; 
  width: 100vw; 
  position: relative; 
  overflow: hidden; /* Added to prevent child scroll-into-view from bubbling */
}
.footer-header {
  height: 48px;
  width: 100vw;
  position: absolute;
  top: 0px;
  left: 0px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  background: rgba(20, 20, 20, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  z-index: 100;
}

.footer-left { height: calc(100% - 48px); width: 300px; position: absolute; top: 48px; left: 0px; }
.footer-track-container { 
  width: 100%; 
  height: 100%; 
  border-right: 1px solid rgba(255, 255, 255, 0.05); 
  background: rgb(32, 32, 32); 
  overflow-y: auto; 
  overflow-x: hidden;
}
.footer-track-container::-webkit-scrollbar { width: 0 !important; }

.footer-right { 
  height: calc(100% - 48px); 
  width: calc(100vw - 300px); 
  background: rgb(25, 25, 25); 
  position: absolute; 
  top: 48px; 
  left: 300px; 
  overflow: auto; 
  padding-top: 0px;
  z-index: 10; 
}
.footer-right::-webkit-scrollbar { height: 6px; background: rgba(0,0,0,0.2); }
.footer-right::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 3px; }

.footer-toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.03);
  padding: 4px 8px;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: rgba(255, 255, 255, 0.1);
  margin: 0 8px;
}

.tool-btn {
  background: transparent !important;
  border: none !important;
  color: #888 !important;
  transition: all 0.2s ease;
}

.tool-btn:hover { color: #fff !important; transform: scale(1.1); }
.tool-btn.is-active { color: var(--accent-cyan, #00f3ff) !important; text-shadow: 0 0 8px rgba(0, 243, 255, 0.5); }

.toggle-btn {
  background: transparent !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: #999 !important;
  border-radius: 6px !important;
  padding: 4px 10px !important;
}

.toggle-btn.is-active {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
  border-color: rgba(255, 255, 255, 0.3) !important;
}

.mode-selector { padding: 3px !important; }
.mode-btn {
  background: transparent !important;
  border: none !important;
  color: #777 !important;
  font-weight: 600 !important;
}

.mode-btn.is-selected { color: #fff !important; }

.mode-dot { width: 6px; height: 6px; border-radius: 50%; margin-right: 6px; }
.mode-0 { background: #409eff; box-shadow: 0 0 5px #409eff; }
.mode-1 { background: #f5b041; box-shadow: 0 0 5px #f5b041; }
.mode-2 { background: #ec7063; box-shadow: 0 0 5px #ec7063; }

.delete-mode-btn { background: transparent !important; border: none !important; color: #777 !important; }
.delete-mode-btn.is-selected { color: #f56c6c !important; }

.tool-btn-rect {
  background: rgba(103, 194, 58, 0.1) !important;
  border: 1px solid rgba(103, 194, 58, 0.2) !important;
  color: #67c23a !important;
}

.tool-btn-rect.is-warning {
  background: rgba(230, 162, 60, 0.1) !important;
  border: 1px solid rgba(230, 162, 60, 0.2) !important;
  color: #e6a23c !important;
}

.footer-header-right { 
  margin-left: auto; 
  display: flex;
  align-items: center;
  gap: 16px;
}
.time-display-wrapper {
  display: flex;
  align-items: baseline;
  background: rgba(0, 0, 0, 0.3);
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  font-family: 'Inter', monospace;
}
.current-time {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  min-width: 50px;
  text-align: right;
}
.time-separator {
  color: rgba(255, 255, 255, 0.3);
  margin: 0 4px;
}
.total-time {
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}
.unit {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.2);
  margin-left: 4px;
  text-transform: uppercase;
}
.custom-slider-container { color: #888; display: flex; align-items: center; gap: 12px; }
.custom-range { appearance: none; background: rgba(255,255,255,0.1); height: 4px; border-radius: 2px; outline: none; width: 120px; }
.custom-range::-webkit-slider-thumb { appearance: none; width: 12px; height: 12px; background: #67c23a; border-radius: 50%; cursor: pointer; }
.show-button-selected { color: #67c23a; }
.show-button-selected:hover { color: #95d475; }
.show-button-selected:active { color: #529b2e; }
.show-button { color: #b9b9b9; }
.show-button:hover { color: #dfdfdf; }
.show-button:active { color: #808080; }
.delete-button { color: #f56c6c; }

.beat-line-wrapper-absolute {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
  z-index: 5;
  pointer-events: none;
}

.animate__animated {
  --animate-duration: 0.2s;
}

.list-move {
  transition: transform 0.2s ease;
}
</style>
