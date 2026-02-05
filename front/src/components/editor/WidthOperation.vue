<template>
  <div @click="selfClicked" @contextmenu.prevent.stop="startEdit" :style="opStyle" @mousedown="setZIndex">

    <el-popover v-model:visible="edit" placement="top" :width="300" trigger="manual">
      <div style="text-align:right;">
        <el-button type="text" class="cancel-button" @click="edit = false">
          <el-icon>
            <CircleClose />
          </el-icon>
        </el-button>
        <el-button type="text" class="ok-button" @click="saveOperation">
          <el-icon>
            <CircleCheck />
          </el-icon>
        </el-button>
        <el-button type="text" class="delete-button" @click="deleteOperation">
          <el-icon>
            <Delete />
          </el-icon>
        </el-button>
      </div>
      <el-form :model="tempOperation" :rules="rules" ref="formRef" @submit.prevent="saveOperation">
        <el-form-item label="开始时机" label-width="80px" prop="startTiming">
          <el-input @keydown.enter="saveOperation" v-model="tempOperation.startTiming" style="width:130px" />
        </el-form-item>
        <el-form-item label="结束时机" label-width="80px" prop="endTiming">
          <el-input @keydown.enter="saveOperation" v-model="tempOperation.endTiming" style="width:130px" />
        </el-form-item>
        <el-form-item label="开始宽度" label-width="80px" prop="startWidth">
          <el-input @keydown.enter="saveOperation" v-model="tempOperation.startWidth" style="width:130px" />
        </el-form-item>
        <el-form-item label="结束宽度" label-width="80px" prop="endWidth">
          <el-input @keydown.enter="saveOperation" v-model="tempOperation.endWidth" style="width:130px" />
        </el-form-item>
      </el-form>
      <template #reference>
        <div>
          <el-tooltip class="item" effect="dark" placement="top-start">
            <template #content>
              <div style="text-align:center">
                {{ operation.startTiming + "→" + operation.endTiming }}
                <br />
                {{ operation.startWidth + "→" + operation.endWidth }}
              </div>
            </template>
            <div>
              <div @mousedown="longOperationCanMove($event)" :style="{
                userSelect: 'none',
                height: '40px',
                position: 'absolute',
                background: 'rgb(184, 223, 107)',
                cursor: 'move',
                width:
                  ((operation.endTiming - operation.startTiming) /
                    displayAreaTime) *
                  (global.documentWidth - 300) +
                  'px',
                left: '-1px',
                top: '1px',
                overflow: 'hidden',
                lineHeight: '40px',
                fontSize: '20px',
                border: '0px solid #fff',
                borderLeftWidth: '1px',
                borderRightWidth: '1px',
              }">
                <div style="text-align:center;color:rgb(255,255,255)">
                  {{ operation.startWidth }}→{{ operation.endWidth }}
                </div>
              </div>
              <div @mousedown="startLeftMove($event)"
                style="width:1px;height:40px;position:absolute;left:0px;top:0;cursor:w-resize;background:transparent;" />
              <div @mousedown="startRightMove($event)" :style="{
                userSelect: 'none',
                height: '40px',
                width: '1px',
                position: 'absolute',
                cursor: 'e-resize',
                left:
                  ((operation.endTiming - operation.startTiming) /
                    displayAreaTime) *
                  (global.documentWidth - global.siderWidth) +
                  1 +
                  'px',
                top: '0px',
                background: 'transparent'
              }" />
            </div>
          </el-tooltip>
        </div>
      </template>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, defineProps, onMounted, inject } from 'vue';
import { CircleClose, CircleCheck, Delete, QuestionFilled } from '@element-plus/icons-vue';
import { ElMessageBox, ElNotification } from 'element-plus';

const props = defineProps({
  operation: Object,
  global: Object,
  track: Object,
  displayAreaTime: Number,
  currentNoteType: Number,
  enableEdit: Boolean,
  chart: Object
});

const commandHistory = inject('commandHistory');
const syncAction = inject('syncAction');

const edit = ref(false);
const canMove = ref(false);
const leftMove = ref(false);
const rightMove = ref(false);
const passedTime = ref(0);
const formRef = ref(null);
const tempOperation = reactive({});

const checkStartTiming = (rule, value, callback) => {
  if (value !== 0 && !value) return callback(new Error("开始时机不能为空"));
  const val = parseFloat(value);
  if (isNaN(val)) callback(new Error("请输入数字值"));
  else if (val < 0) callback(new Error("不能小于0"));
  else if (val < props.track.startTiming) callback(new Error("不能小于轨道开始时机"));
  else if (val > props.track.endTiming) callback(new Error("不能大于轨道结束时机"));
  else callback();
};

const checkEndTiming = (rule, value, callback) => {
  if (value !== 0 && !value) return callback(new Error("结束时机不能为空"));
  const val = parseFloat(value);
  if (isNaN(val)) callback(new Error("请输入数字值"));
  else if (val < 0) callback(new Error("不能小于0"));
  else if (val < props.track.startTiming) callback(new Error("不能小于轨道开始时机"));
  else if (val > props.track.endTiming) callback(new Error("不能大于轨道结束时机"));
  else callback();
};

const rules = {
  startTiming: [{ required: true, validator: checkStartTiming, trigger: "blur" }],
  endTiming: [{ required: true, validator: checkEndTiming, trigger: "blur" }],
  startWidth: [{ required: true, message: "起始宽度不能为空", trigger: "blur" }],
  endWidth: [{ required: true, message: "终止宽度不能为空", trigger: "blur" }],
};

const left = computed(() => {
  return (props.operation.startTiming / props.displayAreaTime) * (props.global.documentWidth - props.global.siderWidth);
});

const opStyle = computed(() => {
  let opacity = 1;
  let filter = 'none';
  if (props.operation.isPending || props.operation.isDeleting) {
    opacity = 0.5;
    filter = 'grayscale(100%)';
  }
  return {
    position: 'absolute',
    top: '20px',
    left: left.value + 'px',
    zIndex: props.operation.zIndex,
    opacity,
    filter,
    pointerEvents: (props.operation.isPending || props.operation.isDeleting) ? 'none' : 'auto'
  };
});

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

const updateTemp = () => {
  Object.assign(tempOperation, JSON.parse(JSON.stringify(props.operation)));
};

const updateTrack = () => {
  props.global.reCalculateTrack = !props.global.reCalculateTrack;
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};

const setZIndex = () => {
  if (props.global.currentOperation) props.global.currentOperation.zIndex = 0;
  props.global.currentOperation = props.operation;
  props.operation.zIndex = 10;
};

const dragStartX = ref(0);
const dragStartTiming = ref(0);
const dragEndTiming = ref(0);
const snapPoints = ref([]);

const collectSnapPoints = (currentOp) => {
  const points = [];
  if (props.chart.tracks) {
    props.chart.tracks.forEach(track => {
      if (track.notes) track.notes.forEach(n => {
        if (n.timing !== undefined) points.push(n.timing);
        if (n.endTiming !== undefined) points.push(n.endTiming);
      });
      ['moveOperations', 'changeWidthOperations', 'changeColorOperations'].forEach(key => {
        if (track[key]) track[key].forEach(op => {
          if (op !== currentOp) {
            if (op.startTiming !== undefined) points.push(op.startTiming);
            if (op.endTiming !== undefined) points.push(op.endTiming);
          }
        });
      });
    });
  }
  if (props.chart.changeBackgroundOperations) {
    props.chart.changeBackgroundOperations.forEach(op => {
      if (op.startTiming !== undefined) points.push(op.startTiming);
      if (op.endTiming !== undefined) points.push(op.endTiming);
    });
  }
  if (props.chart.bpm > 0) {
    const msPerBeat = props.chart.bpm;
    const offset = props.chart.firstBeatDelay || 0;
    const songLen = props.chart.songLength || 300000;
    for (let t = offset; t <= songLen; t += msPerBeat) points.push(t);
  }
  return points;
};

const getSnappedTime = (time, points) => {
  const pxThreshold = 10;
  const msThreshold = (pxThreshold / (props.global.documentWidth - props.global.siderWidth)) * props.displayAreaTime;
  let bestTime = time;
  let minDiff = msThreshold;
  for (const point of points) {
    const diff = Math.abs(time - point);
    if (diff < minDiff) {
      minDiff = diff;
      bestTime = point;
    }
  }
  return bestTime;
};

const checkOverlap = (start, end, exclude) => {
  if (props.track.changeWidthOperations) {
    for (const op of props.track.changeWidthOperations) {
      if (op === exclude) continue;
      if (start < op.endTiming && end > op.startTiming) return true;
    }
  }
  return false;
};

const longOperationCanMove = (e) => {
  if (e.button !== 0 || edit.value || props.operation.isPending || props.operation.isDeleting) return;
  canMove.value = true;
  dragStartX.value = props.global.clientX;
  dragStartTiming.value = props.operation.startTiming;
  dragEndTiming.value = props.operation.endTiming;
  snapPoints.value = collectSnapPoints(props.operation);
};

const startLeftMove = (e) => {
  if (e.button !== 0 || edit.value || props.operation.isPending || props.operation.isDeleting) return;
  leftMove.value = true;
  dragStartX.value = props.global.clientX;
  dragStartTiming.value = props.operation.startTiming;
  dragEndTiming.value = props.operation.endTiming;
  snapPoints.value = collectSnapPoints(props.operation);
};

const startRightMove = (e) => {
  if (e.button !== 0 || edit.value || props.operation.isPending || props.operation.isDeleting) return;
  rightMove.value = true;
  dragStartX.value = props.global.clientX;
  dragStartTiming.value = props.operation.startTiming;
  dragEndTiming.value = props.operation.endTiming;
  snapPoints.value = collectSnapPoints(props.operation);
};

const startEdit = () => {
  if (props.operation.isPending || props.operation.isDeleting) return;
  edit.value = true;
  updateTemp();
};

const saveOperation = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      const oldState = JSON.parse(JSON.stringify(props.operation));
      const newState = JSON.parse(JSON.stringify(tempOperation));
      Object.assign(props.operation, newState);
      edit.value = false;
      updateTrack();

      if (syncAction) syncAction("UPDATE_WIDTH_OP", props.operation);

      if (commandHistory) {
        commandHistory.pushCommand({
          description: 'Edit Width Op',
          undo: () => {
            Object.assign(props.operation, oldState);
            if (syncAction) syncAction("UPDATE_WIDTH_OP", props.operation);
            updateTrack();
          },
          redo: () => {
            Object.assign(props.operation, newState);
            if (syncAction) syncAction("UPDATE_WIDTH_OP", props.operation);
            updateTrack();
          }
        });
      }
    }
  });
};

const deleteSelf = () => {
  if (props.operation.isDeleting) return;
  props.operation.isDeleting = true;
  if (syncAction) syncAction("DELETE_WIDTH_OP", props.operation.id);

  if (props.global.currentOperation === props.operation) {
    props.global.currentOperation = null;
  }
};

const deleteOperation = () => {
  ElMessageBox.confirm("您确定删除该操作?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    deleteSelf();
  }).catch(() => { });
};

const selfClicked = () => {
  if (props.currentNoteType === 3) deleteSelf();
};

watch(() => props.global.mouseUp, () => {
  if (canMove.value || leftMove.value || rightMove.value) {
    props.track.changeWidthOperations.sort((a, b) => a.startTiming - b.startTiming);
    updateTrack();

    const finalStart = props.operation.startTiming;
    const finalEnd = props.operation.endTiming;

    if (finalStart !== dragStartTiming.value || finalEnd !== dragEndTiming.value) {
      const oldS = dragStartTiming.value;
      const oldE = dragEndTiming.value;

      if (syncAction) syncAction("UPDATE_WIDTH_OP", props.operation);

      if (commandHistory) {
        commandHistory.pushCommand({
          description: 'Move Width Op',
          undo: () => {
            props.operation.startTiming = oldS;
            props.operation.endTiming = oldE;
            props.track.changeWidthOperations.sort((a, b) => a.startTiming - b.startTiming);
            if (syncAction) syncAction("UPDATE_WIDTH_OP", props.operation);
            updateTrack();
            updateTemp();
          },
          redo: () => {
            props.operation.startTiming = finalStart;
            props.operation.endTiming = finalEnd;
            props.track.changeWidthOperations.sort((a, b) => a.startTiming - b.startTiming);
            if (syncAction) syncAction("UPDATE_WIDTH_OP", props.operation);
            updateTrack();
            updateTemp();
          }
        });
      }
    }

    canMove.value = false;
    leftMove.value = false;
    rightMove.value = false;
  }
});

watch(() => props.global.mouseMove, () => {
  if (canMove.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - props.global.siderWidth)) * props.displayAreaTime);
    const duration = dragEndTiming.value - dragStartTiming.value;
    let newStart = roundTime(dragStartTiming.value + deltaTime);
    newStart = getSnappedTime(newStart, snapPoints.value);
    if (newStart < props.track.startTiming) newStart = props.track.startTiming;
    if (newStart + duration > props.track.endTiming) newStart = props.track.endTiming - duration;
    props.operation.startTiming = newStart;
    props.operation.endTiming = newStart + duration;
    updateTemp();
  } else if (leftMove.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - props.global.siderWidth)) * props.displayAreaTime);
    let newStart = roundTime(dragStartTiming.value + deltaTime);
    newStart = getSnappedTime(newStart, snapPoints.value);
    if (newStart < props.track.startTiming) newStart = props.track.startTiming;
    if (newStart > props.operation.endTiming - 100) newStart = props.operation.endTiming - 100;
    props.operation.startTiming = newStart;
    updateTemp();
  } else if (rightMove.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - props.global.siderWidth)) * props.displayAreaTime);
    let newEnd = roundTime(dragEndTiming.value + deltaTime);
    newEnd = getSnappedTime(newEnd, snapPoints.value);
    if (newEnd < props.operation.startTiming + 100) newEnd = props.operation.startTiming + 100;
    if (newEnd > props.track.endTiming) newEnd = props.track.endTiming;
    props.operation.endTiming = newEnd;
    updateTemp();
  }
});

onMounted(() => {
  props.operation.zIndex = 0;
  updateTemp();
});
</script>

<style scoped>
.delete-context-menu {
  position: absolute;
  z-index: 9999;
  background: rgba(40, 40, 40, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  padding: 4px;
  min-width: 60px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.delete-menu-item {
  padding: 4px 8px;
  cursor: pointer;
  color: #eee;
  font-size: 12px;
  border-radius: 2px;
  text-align: center;
}

.delete-menu-item:hover {
  background: rgba(245, 108, 108, 0.8);
  color: white;
}
</style>
