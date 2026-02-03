<template>
  <div
    :class="track.edit ? 'panel-edit' : 'panel-no-edit'"
    :style="{
      width: (chart.songLength / displayAreaTime) * (global.documentWidth - siderWidth) + 'px',
    }"
    @contextmenu.prevent="showContextMenu($event)"
  >
    <div class="track-tracks">
      <div
        @click="newNote($event)"
        :class="track.edit ? 'note-track-edit' : 'note-track'"
        :style="{
          width: (chart.songLength / displayAreaTime) * (global.documentWidth - siderWidth) + 'px',
        }"
      >
        <div class="track-range">
          <div>
            <div
              :style="{
                position: 'absolute',
                left: (track.startTiming / displayAreaTime) * (global.documentWidth - siderWidth) + 'px',
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
                  width: ((track.endTiming - track.startTiming) / displayAreaTime) * (global.documentWidth - siderWidth) + 'px',
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
                @mousedown="startDragLeft"
                @click.stop
                style="width:10px;height:80px;position:absolute;left:-5px;top:0;cursor:w-resize;z-index:100;background:transparent;"
              />
              <div
                @mousedown="startDragRight"
                @click.stop
                :style="{
                  userSelect: 'none',
                  height: '80px',
                  width: '10px',
                  position: 'absolute',
                  cursor: 'e-resize',
                  left: ((track.endTiming - track.startTiming) / displayAreaTime) * (global.documentWidth - siderWidth) - 5 + 'px',
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
              width: (chart.songLength / displayAreaTime) * (global.documentWidth - siderWidth) + 'px',
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
              width: (chart.songLength / displayAreaTime) * (global.documentWidth - siderWidth) + 'px',
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
              width: (chart.songLength / displayAreaTime) * (global.documentWidth - siderWidth) + 'px',
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
    <div
      v-if="contextMenuVisible"
      :style="{
        position: 'fixed',
        top: contextMenuData.y + 'px',
        left: contextMenuData.x + 'px',
        zIndex: 9999
      }"
      class="context-menu"
      @click.stop
    >
      <div v-if="contextMenuData.type === 'note'" class="menu-group">
        <div class="menu-item" @click="contextAction('note-0')">添加短键</div>
        <div class="menu-item" @click="contextAction('note-1')">添加长键</div>
        <div class="menu-item" @click="contextAction('note-2')">添加滑键</div>
      </div>
      <div v-else-if="contextMenuData.type === 'move'" class="menu-group">
        <div class="menu-item" @click="contextAction('op-move')">添加移动操作</div>
      </div>
      <div v-else-if="contextMenuData.type === 'width'" class="menu-group">
        <div class="menu-item" @click="contextAction('op-width')">添加宽度操作</div>
      </div>
      <div v-else-if="contextMenuData.type === 'color'" class="menu-group">
        <div class="menu-item" @click="contextAction('op-color')">添加颜色操作</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits, inject } from 'vue';
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
  enableEdit: Boolean,
  siderWidth: {
    type: Number,
    default: 300
  }
});

const emit = defineEmits(["currentTrack"]);
const syncAction = inject('syncAction');
const uuid = inject('uuid');

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

const newNote = (e) => {
  // Check if click is near start/end handles. We assume handles are near ends?
  // The user says "Track has open/close animation area", which usually means start/end timing adjustment areas.
  // In our template, we have visible handles for start/end adjustment.
  // We can just rely on the click event not propagating if it hit those elements if they stop propagation.
  // But here we are on the main track div.
  // Let's assume the click event provides the timing. We need to calculate timing from mouse position.
  
  if (props.currentNoteType !== 3 && props.currentNoteType !== undefined && props.currentNoteType >= 0) {
      if (props.global.currentTime < props.track.startTiming || props.global.currentTime > props.track.endTiming) {
          return; // Outside active track area
      }
      
      // Calculate timing based on click X if we wanted exact position, 
      // BUT current implementation uses global.currentTime which is the seeker position.
      // The user said: "Create note at the CLICKED time point".
      // So we need to calculate time from e.offsetX
      
      const rect = e.currentTarget.getBoundingClientRect();
      const clickX = e.clientX - rect.left;
      const timeMs = (clickX / (props.global.documentWidth - props.siderWidth)) * props.displayAreaTime;
      const quantizedTime = roundTime(timeMs);
      
      if (quantizedTime < props.track.startTiming || quantizedTime > props.track.endTiming) {
          // Clicked outside efficient range
          return;
      }
      
      addNoteAt(props.currentNoteType, quantizedTime);
  }
};

const newMoveOperations = () => {
  if (props.currentNoteType !== 3) {
    if (props.global.currentTime >= props.track.startTiming && props.global.currentTime < props.track.endTiming - 150) {
      addOpAt('move', props.global.currentTime);
    } else {
       ElNotification({ title: "错误", message: "请在轨道范围内添加操作", type: "error" });
    }
  }
};

const newWidthOperations = () => {
  if (props.currentNoteType !== 3) {
    if (props.global.currentTime >= props.track.startTiming && props.global.currentTime < props.track.endTiming - 150) {
      addOpAt('width', props.global.currentTime);
    } else {
       ElNotification({ title: "错误", message: "请在轨道范围内添加操作", type: "error" });
    }
  }
};

const newColorOperations = () => {
  if (props.currentNoteType !== 3) {
    if (props.global.currentTime >= props.track.startTiming && props.global.currentTime < props.track.endTiming - 150) {
      addOpAt('color', props.global.currentTime);
    } else {
       ElNotification({ title: "错误", message: "请在轨道范围内添加操作", type: "error" });
    }
  }
};

const dragStartTiming = ref(0);
const dragEndTiming = ref(0);

const startDragLeft = () => {
  leftMove.value = true;
  dragStartTiming.value = props.track.startTiming;
};

const startDragRight = () => {
  rightMove.value = true;
  dragEndTiming.value = props.track.endTiming;
};

const commandHistory = inject('commandHistory');

watch(() => props.global.mouseUp, () => {
  if (leftMove.value || rightMove.value) {
    const finalStart = props.track.startTiming;
    const finalEnd = props.track.endTiming;
    const oldStart = dragStartTiming.value;
    const oldEnd = dragEndTiming.value;

    if (finalStart !== oldStart || finalEnd !== oldEnd) {
      if (syncAction) syncAction("UPDATE_TRACK", props.track);
      
      if (commandHistory) {
        commandHistory.pushCommand({
          description: 'Adjust Track Timing',
          undo: () => {
            props.track.startTiming = oldStart;
            props.track.endTiming = oldEnd;
            if (syncAction) syncAction("UPDATE_TRACK", props.track);
            updateTrack();
          },
          redo: () => {
            props.track.startTiming = finalStart;
            props.track.endTiming = finalEnd;
            if (syncAction) syncAction("UPDATE_TRACK", props.track);
            updateTrack();
          }
        });
      }
    }
    
    leftMove.value = false;
    rightMove.value = false;
  }
  if (contextMenuVisible.value) closeContextMenu();
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
const contextMenuVisible = ref(false);
const contextMenuData = ref({ x: 0, y: 0, time: 0 });

const showContextMenu = (e) => {
  if (props.track.edit) {
      // If editing, allow context menu for specific operations?
      // User said: "On operation track, right click adds operation".
      // "On default track (note track), selects 3 note types".
      // We are in TrackCardPanel which handles both Note Track (top) and Op Tracks (bottom).
      // We need to know WHICH track area was clicked.
      // The Note Track is .note-track / .note-track-edit
      // The Op Tracks are .positionX-track, .width-track, etc.
      // However, the event bubbles to parent.
      // e.target.closest('.positionX-track') etc can differentiate.
  }
  // This logic is getting complex for a simple click handler.
  // Let's implement logic:
  
  const rect = e.currentTarget.getBoundingClientRect();
  const clickX = e.clientX - rect.left;
  const timeMs = (clickX / (props.global.documentWidth - props.siderWidth)) * props.displayAreaTime;
  const quantizedTime = roundTime(timeMs);
  
  contextMenuData.value = {
      x: e.clientX,
      y: e.clientY,
      time: quantizedTime,
      type: 'note' // default
  };
  
  if (e.target.closest('.positionX-track')) contextMenuData.value.type = 'move';
  else if (e.target.closest('.width-track')) contextMenuData.value.type = 'width';
  else if (e.target.closest('.color-track')) contextMenuData.value.type = 'color';
  else contextMenuData.value.type = 'note';
  
  contextMenuVisible.value = true;
};

const closeContextMenu = () => { contextMenuVisible.value = false; };

const contextAction = (action) => {
    // Action logic
    const time = contextMenuData.value.time;
    if (action === 'note-0') addNoteAt(0, time);
    else if (action === 'note-1') addNoteAt(1, time);
    else if (action === 'note-2') addNoteAt(2, time);
    else if (action === 'op-move') addOpAt('move', time);
    else if (action === 'op-width') addOpAt('width', time);
    else if (action === 'op-color') addOpAt('color', time);
    closeContextMenu();
};

const addNoteAt = (type, time) => {
    let duration = (type === 1) ? 500 : 0;
    if (time + duration > props.track.endTiming) duration = props.track.endTiming - time;
    const clientId = uuid();
    const newNoteObj = {
        noteType: type,
        key: props.track.key,
        timing: time,
        endTiming: time + duration,
        basedTrack: props.track.id,
        clientId: clientId,
        isPending: true
    };
    props.track.notes.push(newNoteObj);
    if (syncAction) syncAction("ADD_NOTE", newNoteObj, clientId);
    updateTrack();
};

const addOpAt = (type, time) => {
    const endTiming = Math.min(time + 150, props.track.endTiming);
    const clientId = uuid();
    let op = { startTiming: time, endTiming: endTiming, basedTrack: props.track.id, clientId, isPending: true };
    
    if (type === 'move') {
        op.startX = props.track.tempPositionX || 0; op.endX = props.track.tempPositionX || 0;
        if (!props.track.moveOperations) props.track.moveOperations = [];
        props.track.moveOperations.push(op);
        if (syncAction) syncAction("ADD_MOVE_OP", op, clientId);
    } else if (type === 'width') {
        op.startWidth = props.track.tempWidth || 1; op.endWidth = props.track.tempWidth || 1;
        if (!props.track.changeWidthOperations) props.track.changeWidthOperations = [];
        props.track.changeWidthOperations.push(op);
        if (syncAction) syncAction("ADD_WIDTH_OP", op, clientId);
    } else if (type === 'color') {
        op.startR = props.track.tempR || 255; op.startG = props.track.tempG || 255; op.startB = props.track.tempB || 255; 
        op.endR = props.track.tempR || 255; op.endG = props.track.tempG || 255; op.endB = props.track.tempB || 255;
        if (!props.track.changeColorOperations) props.track.changeColorOperations = [];
        props.track.changeColorOperations.push(op);
        if (syncAction) syncAction("ADD_COLOR_OP", op, clientId);
    }
    updateTrack();
};

// Close menu on click anywhere else
watch(() => props.global.mouseUp, () => {
    if (contextMenuVisible.value) closeContextMenu();
});

// Reuse existing logic from newNote/newMoveOperations etc but parameterized
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

.note-track-label { color: rgba(255, 255, 255, 0.3); position: absolute; left: 10px; top: 5px; width: var(--sider-width, 300px); font-size: 11px; }
.positionX-track-label { color: rgba(255, 255, 255, 0.3); position: absolute; left: 10px; top: 110px; width: var(--sider-width, 300px); font-size: 11px; }
.width-track-label { color: rgba(255, 255, 255, 0.3); position: absolute; left: 10px; top: 230px; width: var(--sider-width, 300px); font-size: 11px; }
.color-track-label { color: rgba(255, 255, 255, 0.3); position: absolute; left: 10px; top: 350px; width: var(--sider-width, 300px); font-size: 11px; }

.context-menu {
  background: rgba(40, 40, 40, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  padding: 4px;
  min-width: 120px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
}

.menu-group {
  display: flex;
  flex-direction: column;
}

.menu-item {
  padding: 8px 12px;
  cursor: pointer;
  color: #eee;
  font-size: 13px;
  transition: background 0.2s;
  border-radius: 4px;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}
</style>
