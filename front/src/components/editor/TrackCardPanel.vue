<template>
  <div
    :class="track.edit ? 'panel-edit' : 'panel-no-edit'"
    :style="{
      width: (chart.songLength / displayAreaTime) * (global.documentWidth - 300) + 'px',
    }"
  >
    <div class="track-tracks">
      <div
        @dblclick="newNote"
        :class="track.edit ? 'note-track-edit' : 'note-track'"
        :style="{
          width: (chart.songLength / displayAreaTime) * (global.documentWidth - 300) + 'px',
        }"
      >
        <div class="track-range">
          <div>
            <div
              :style="{
                position: 'absolute',
                left: (track.startTiming / displayAreaTime) * (global.documentWidth - 300) + 'px',
                top: 0,
                height: '80px',
                width: '1px',
                background: 'rgb(255,255,255)',
              }"
            >
              <div
                :style="{
                  userSelect: 'none',
                  height: '80px',
                  position: 'absolute',
                  background: 'rgb(70, 70, 70)',
                  width: ((track.endTiming - track.startTiming) / displayAreaTime) * (global.documentWidth - 300) + 'px',
                  left: '-1px',
                  top: '1px',
                  overflow: 'hidden',
                  lineHeight: '40px',
                  fontSize: '20px',
                  border: '0px solid #fff',
                  borderLeftWidth: '1px',
                  borderRightWidth: '1px',
                }"
              ></div>
              <div
                @mousedown="leftMove = true"
                style="width:10px;height:80px;position:absolute;left:-5px;top:0;cursor:w-resize;z-index:100;background:transparent;"
              />
              <div
                @mousedown="rightMove = true"
                :style="{
                  userSelect: 'none',
                  height: '80px',
                  width: '10px',
                  position: 'absolute',
                  cursor: 'e-resize',
                  left: ((track.endTiming - track.startTiming) / displayAreaTime) * (global.documentWidth - 300) - 5 + 'px',
                  top: '0px',
                  zIndex: 100,
                  background: 'transparent'
                }"
              />
            </div>
          </div>
        </div>
        <div v-for="(noteItem, index) in track.notes" :key="index">
          <Note
            :currentNoteType="currentNoteType"
            :track="track"
            :global="global"
            :chart="chart"
            :note="noteItem"
            :enableEdit="enableEdit"
            :displayAreaTime="displayAreaTime"
          />
        </div>
      </div>
      <transition
        name="fade"
        enter-active-class="animate__animated animate__fadeInDown"
        leave-active-class="animate__animated animate__fadeOutUp"
      >
        <div v-show="track.edit">
          <div
            @dblclick="newMoveOperations"
            class="positionX-track"
            :style="{
              width: (chart.songLength / displayAreaTime) * (global.documentWidth - 300) + 'px',
            }"
          >
            <div v-for="(operation, index) in track.moveOperations" :key="index">
              <MoveOperation
                :currentNoteType="currentNoteType"
                :track="track"
                :global="global"
                :chart="chart"
                :operation="operation"
                :enableEdit="enableEdit"
                :displayAreaTime="displayAreaTime"
              />
            </div>
          </div>
          <div
            @dblclick="newWidthOperations"
            class="width-track"
            :style="{
              width: (chart.songLength / displayAreaTime) * (global.documentWidth - 300) + 'px',
            }"
          >
            <div v-for="(operation, index) in track.changeWidthOperations" :key="index">
              <WidthOperation
                :currentNoteType="currentNoteType"
                :track="track"
                :global="global"
                :chart="chart"
                :operation="operation"
                :enableEdit="enableEdit"
                :displayAreaTime="displayAreaTime"
              />
            </div>
          </div>
          <div
            @dblclick="newColorOperations"
            class="color-track"
            :style="{
              width: (chart.songLength / displayAreaTime) * (global.documentWidth - 300) + 'px',
            }"
          >
            <div v-for="(operation, index) in track.changeColorOperations" :key="index">
              <ColorOperation
                :currentNoteType="currentNoteType"
                :track="track"
                :global="global"
                :chart="chart"
                :operation="operation"
                :enableEdit="enableEdit"
                :displayAreaTime="displayAreaTime"
              />
            </div>
          </div>
        </div>
      </transition>
    </div>
    <div class="track-labels">
      <transition
        name="fade"
        enter-active-class="animate__animated animate__fadeInDown"
        leave-active-class="animate__animated animate__fadeOutUp"
      >
        <div v-show="track.edit">
          <div class="note-track-label" :style="{ left: scrollLeft + 'px' }">
            音符轨道
          </div>
          <div
            class="positionX-track-label"
            :style="{ left: scrollLeft + 'px' }"
          >
            坐标操作
          </div>
          <div class="width-track-label" :style="{ left: scrollLeft + 'px' }">
            宽度操作
          </div>
          <div class="color-track-label" :style="{ left: scrollLeft + 'px' }">
            色彩操作
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue';
import { ElNotification } from 'element-plus';
import Note from "./Note.vue";
import MoveOperation from "./MoveOperation.vue";
import WidthOperation from "./WidthOperation.vue";
import ColorOperation from "./ColorOperation.vue";
import "animate.css";

const props = defineProps({
  track: Object,
  global: Object,
  chart: Object,
  scrollLeft: Number,
  displayAreaTime: Number,
  currentNoteType: Number,
  enableEdit: Boolean
});

const emit = defineEmits(["currentTrack"]);

const canMove = ref(false);
const leftMove = ref(false);
const rightMove = ref(false);
const passedTime = ref(0);

const roundTime = (timing) => {
  if (props.global.beatLine) {
    const bpm = props.chart.bpm / 16;
    if (bpm > 0) {
      const mod = (timing - props.chart.firstBeatDelay) % bpm;
      if (mod > bpm / 2) timing += bpm - mod;
      else timing -= mod;
    }
  }
  return Math.ceil(timing);
};

const updateTrack = () => {
  props.global.reCalculateTrack = !props.global.reCalculateTrack;
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};

const newNote = () => {
  if (props.currentNoteType !== 3) {
    if (props.global.currentTime >= props.track.startTiming && props.global.currentTime < props.track.endTiming - 150) {
      props.track.notes.push({
        noteType: props.currentNoteType,
        key: props.track.key,
        timing: props.global.currentTime,
        endTiming: props.global.currentTime + 150,
      });
      updateTrack();
    } else {
      ElNotification({ title: "错误", message: "请在轨道范围内添加音符", type: "error" });
    }
  }
};

const newMoveOperations = () => {
  if (props.currentNoteType !== 3) {
    if (props.global.currentTime >= props.track.startTiming && props.global.currentTime < props.track.endTiming - 150) {
      props.track.moveOperations.push({
        startX: props.track.tempPositionX,
        endX: props.track.tempPositionX,
        startTime: props.global.currentTime,
        endTime: props.global.currentTime + 150,
      });
      updateTrack();
    } else {
       ElNotification({ title: "错误", message: "请在轨道范围内添加操作", type: "error" });
    }
  }
};

const newWidthOperations = () => {
  if (props.currentNoteType !== 3) {
    if (props.global.currentTime >= props.track.startTiming && props.global.currentTime < props.track.endTiming - 150) {
      props.track.changeWidthOperations.push({
        startWidth: props.track.tempWidth,
        endWidth: props.track.tempWidth,
        startTime: props.global.currentTime,
        endTime: props.global.currentTime + 150,
      });
      updateTrack();
    } else {
       ElNotification({ title: "错误", message: "请在轨道范围内添加操作", type: "error" });
    }
  }
};

const newColorOperations = () => {
  if (props.currentNoteType !== 3) {
    if (props.global.currentTime >= props.track.startTiming && props.global.currentTime < props.track.endTiming - 150) {
      props.track.changeColorOperations.push({
        startR: props.track.tempR,
        startG: props.track.tempG,
        startB: props.track.tempB,
        endR: props.track.tempR,
        endG: props.track.tempG,
        endB: props.track.tempB,
        startTime: props.global.currentTime,
        endTime: props.global.currentTime + 150,
      });
      updateTrack();
    } else {
       ElNotification({ title: "错误", message: "请在轨道范围内添加操作", type: "error" });
    }
  }
};

watch(() => props.global.mouseUp, () => {
  canMove.value = false;
  leftMove.value = false;
  rightMove.value = false;
});

watch(() => props.global.mouseMove, () => {
  if (canMove.value) {
    // Logic for moving the whole track start/end - if implemented
  } else if (leftMove.value) {
    const val = roundTime(props.global.currentTime);
    if (val <= props.track.endTiming) {
      if (props.track.notes.length > 0) {
        if (val <= props.track.notes[0].timing) props.track.startTiming = val;
      } else props.track.startTiming = val;
    }
  } else if (rightMove.value) {
    const val = roundTime(props.global.currentTime);
    if (val >= props.track.startTiming) {
      if (props.track.notes.length > 0) {
        if (val >= props.track.notes[props.track.notes.length - 1].timing) props.track.endTiming = val;
      } else props.track.endTiming = val;
    }
  }
});
</script>

<style scoped>
.panel-no-edit {
  box-sizing: border-box;
  height: 90px;
  padding: 0;
  position: relative;
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05); /* Match Left Side */
}
.panel-edit {
  box-sizing: border-box;
  height: 525px;
  padding: 0;
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}
.note-track { position: absolute; top: 5px; left: 0px; height: 80px; width: 100%; }
.note-track-edit { position: absolute; left: 0px; top: 10px; height: 80px; width: 100%; }
.positionX-track { position: absolute; left: 0px; top: 130px; height: 80px; }
.width-track { position: absolute; left: 0px; top: 250px; height: 80px; }
.color-track { position: absolute; left: 0px; top: 370px; height: 80px; }

.note-track-label { color: rgba(255, 255, 255, 0.3); position: absolute; left: 10px; top: 5px; width: 300px; font-size: 11px; }
.positionX-track-label { color: rgba(255, 255, 255, 0.3); position: absolute; left: 10px; top: 110px; width: 300px; font-size: 11px; }
.width-track-label { color: rgba(255, 255, 255, 0.3); position: absolute; left: 10px; top: 230px; width: 300px; font-size: 11px; }
.color-track-label { color: rgba(255, 255, 255, 0.3); position: absolute; left: 10px; top: 350px; width: 300px; font-size: 11px; }
</style>
