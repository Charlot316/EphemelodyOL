<template>
  <div class="footer-container">
    <div class="footer-header">
      <div class="footer-header-left">
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="
            global.timeSort = !global.timeSort;
            updateTrack();
          "
        >{{ global.timeSort ? "改为坐标排序" : "改为时间排序" }}</el-button>
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="showNoRemain = !showNoRemain"
        >{{ showNoRemain ? "关闭无音符轨道" : "显示无音符轨道" }}</el-button>
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="showCurrent = !showCurrent"
        >{{ showCurrent ? "显示全部轨道" : "显示当前轨道" }}</el-button>
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="autoScroll = !autoScroll"
        >{{ autoScroll ? "关闭滚动" : "开启滚动" }}</el-button>
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="showReal = !showReal"
        >{{ showReal ? "关闭实轨" : "显示实轨" }}</el-button>
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="showFake = !showFake"
        >{{ showFake ? "关闭虚轨" : "显示虚轨" }}</el-button>

        <el-button
          type="text"
          :class="currentNoteType == 0 ? 'show-button-selected' : 'show-button'"
          style="margin-right:5px;"
          @click="currentNoteType = 0"
        >双击短键</el-button>
        <el-button
          type="text"
          :class="currentNoteType == 1 ? 'show-button-selected' : 'show-button'"
          style="margin-right:5px;"
          @click="currentNoteType = 1"
        >双击长键</el-button>
        <el-button
          type="text"
          :class="currentNoteType == 2 ? 'show-button-selected' : 'show-button'"
          style="margin-right:5px;"
          @click="currentNoteType = 2"
        >双击滑键</el-button>
        <el-button
          type="text"
          :class="currentNoteType == 3 ? 'delete-button' : 'show-button'"
          style="margin-right:5px;"
          @click="currentNoteType = 3"
        >单击删除</el-button>
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="enableEdit = !enableEdit"
        >{{ enableEdit ? "禁用编辑弹窗" : "开启编辑弹窗" }}</el-button>
      </div>
      <div class="footer-header-right">
        <div class="custom-slider-container">
          <label>预览范围</label>
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
            name="flip-list"
            enter-active-class="animate__animated animate__fadeInUp"
            leave-active-class="animate__animated animate__fadeOutUp"
          >
            <div v-for="track in chart.tracks" :key="track.index">
              <transition
                name="flip-list"
                enter-active-class="animate__animated animate__fadeInUp"
                leave-active-class="animate__animated animate__fadeOutUp"
              >
                <TrackCard
                  v-if="isVisible(track)"
                  :chart="chart"
                  :track="track"
                  :global="global"
                  @currentTrack="handleCurrentTrack"
                />
              </transition>
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
        >
          <BeatLine
            :chart="chart"
            :global="global"
            :displayAreaTime="displayAreaTime"
          />
        </div>
        <div style="position:absolute;left:0;top:0;">
          <transition-group
            name="flip-list"
            enter-active-class="animate__animated animate__fadeInUp"
            leave-active-class="animate__animated animate__fadeOutUp"
          >
            <div v-for="track in chart.tracks" :key="track.index">
              <transition
                name="flip-list"
                enter-active-class="animate__animated animate__fadeInUp"
                leave-active-class="animate__animated animate__fadeOutUp"
              >
                <TrackCardPanel
                  v-if="isVisible(track)"
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
              </transition>
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
import { ref, watch, defineProps, defineEmits, onMounted } from 'vue';
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
  if (!rightScrollElement) rightScrollElement = document.getElementById("footer-right-scroll");
  if (rightScrollElement) rightScrollElement.scrollTop = document.getElementById("footer-left-scroll").scrollTop;
};

const rightScroll = () => {
  if (!rightScrollElement) rightScrollElement = document.getElementById("footer-right-scroll");
  if (rightScrollElement) {
    document.getElementById("footer-left-scroll").scrollTop = rightScrollElement.scrollTop;
    scrollLeft.value = rightScrollElement.scrollLeft;
    scrollTop.value = rightScrollElement.scrollTop;
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

const isVisible = (track) => {
  const showByNote = !showNoRemain.value ? (track.notes.length > 0) : true;
  const showByType = track.type === 1 ? showReal.value : showFake.value;
  const showByTime = showCurrent.value ? (props.global.currentTime >= track.startTiming && props.global.currentTime <= track.endTiming) : true;
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
      if (newVal >= tracks[i].startTiming && newVal <= tracks[i].endTiming) {
        document.querySelector("#trackCardPanel" + tracks[i].index)?.scrollIntoView({ behavior: "auto" });
        break;
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
.footer-container { height: 100%; width: 100vw; position: relative; }
.footer-header { height: 35px; padding-bottom: 5px; width: 100vw; position: absolute; top: 0px; left: 0px; display: flex; justify-content: space-between; align-items: center; }
.footer-left { height: calc(100% - 35px); width: 300px; position: absolute; top: 35px; left: 0px; }
.footer-track-container { width: 100%; height: 100%; border-right: 1px solid rgba(255,255,255,0.1); background: rgb(32, 32, 32); overflow: auto; padding-top: 0px; }
.footer-right { height: calc(100% - 35px); width: calc(100vw - 300px); background: rgb(32, 32, 32); position: absolute; top: 35px; left: 300px; overflow: auto; padding-top: 0px; }
.footer-track-container::-webkit-scrollbar { width: 0 !important; }
.footer-header-left { padding-left: 25px; min-width: 900px; }

.custom-slider-container { display: flex; align-items: center; gap: 10px; color: #aaa; font-size: 12px; }
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
</style>
