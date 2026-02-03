<template>
  <div
    @click="selfClicked"
    @contextmenu.prevent.stop="openDeleteMenu"
    :style="{
      position: 'absolute',
      top: '20px',
      left: left + 'px',
      zIndex: operation.zIndex,
    }"
    @mousedown="setZIndex"
  >
    <!-- Delete Context Menu -->
    <div
      v-if="deleteMenuVisible"
      class="delete-context-menu"
      :style="{ left: '10px', top: '10px' }"
      @mousedown.stop
    >
      <div class="delete-menu-item" @click="deleteOperation">删除</div>
    </div>

    <el-popover
      v-model:visible="edit"
      placement="top"
      :width="300"
      trigger="manual"
    >
      <div style="text-align:right;">
        <el-button
          type="text"
          class="cancel-button"
          @click="edit = false"
        >
          <el-icon><CircleClose /></el-icon>
        </el-button>
        <el-button
          type="text"
          class="ok-button"
          @click="saveOperation"
        >
          <el-icon><CircleCheck /></el-icon>
        </el-button>
        <el-button
          type="text"
          class="delete-button"
          @click="deleteOperation"
        >
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
      <el-form
        :model="tempOperation"
        :rules="rules"
        ref="formRef"
        @submit.prevent="saveOperation"
      >
        <el-form-item label="开始时机" label-width="80px" prop="startTime">
          <el-input
            @keydown.enter="saveOperation"
            v-model="tempOperation.startTime"
            style="width:130px"
          />
        </el-form-item>
        <el-form-item label="结束时机" label-width="80px" prop="endTime">
          <el-input
            @keydown.enter="saveOperation"
            v-model="tempOperation.endTime"
            style="width:130px"
          />
        </el-form-item>
        <el-form-item label="开始宽度" label-width="80px" prop="startWidth">
          <el-input
            @keydown.enter="saveOperation"
            v-model="tempOperation.startWidth"
            style="width:130px"
          />
        </el-form-item>
        <el-form-item label="结束宽度" label-width="80px" prop="endWidth">
          <el-input
            @keydown.enter="saveOperation"
            v-model="tempOperation.endWidth"
            style="width:130px"
          />
        </el-form-item>
      </el-form>
      <template #reference>
        <div>
          <el-tooltip class="item" effect="dark" placement="top-start">
            <template #content>
              <div style="text-align:center">
                {{ operation.startTime + "→" + operation.endTime }}
                <br />
                {{ operation.startWidth + "→" + operation.endWidth }}
              </div>
            </template>
            <div>
              <div
                @mousedown="longOperationCanMove"
                :style="{
                  userSelect: 'none',
                  height: '40px',
                  position: 'absolute',
                  background: 'rgb(184, 223, 107)',
                  cursor: 'move',
                  width:
                    ((operation.endTime - operation.startTime) /
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
                }"
              >
                <div style="text-align:center;color:rgb(255,255,255)">
                  {{ operation.startWidth }}→{{ operation.endWidth }}
                </div>
              </div>
              <div
                @mousedown="startLeftMove"
                style="width:1px;height:40px;position:absolute;left:0px;top:0;cursor:w-resize;background:transparent;"
              />
              <div
                @mousedown="startRightMove"
                :style="{
                  userSelect: 'none',
                  height: '40px',
                  width: '1px',
                  position: 'absolute',
                  cursor: 'e-resize',
                  left:
                    ((operation.endTime - operation.startTime) /
                      displayAreaTime) *
                      (global.documentWidth - 300) +
                    1 +
                    'px',
                  top: '0px',
                  background: 'transparent'
                }"
              />
            </div>
          </el-tooltip>
        </div>
      </template>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, defineProps, onMounted } from 'vue';
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

const edit = ref(false);
const canMove = ref(false);
const leftMove = ref(false);
const rightMove = ref(false);
const passedTime = ref(0);
const formRef = ref(null);
const tempOperation = reactive({});

const checkStartTime = (rule, value, callback) => {
  if (value !== 0 && !value) return callback(new Error("开始时机不能为空"));
  const val = parseFloat(value);
  if (isNaN(val)) callback(new Error("请输入数字值"));
  else if (val < 0) callback(new Error("不能小于0"));
  else if (val < props.track.startTiming) callback(new Error("不能小于轨道开始时机"));
  else if (val > props.track.endTiming) callback(new Error("不能大于轨道结束时机"));
  else callback();
};

const checkEndTime = (rule, value, callback) => {
  if (value !== 0 && !value) return callback(new Error("结束时机不能为空"));
  const val = parseFloat(value);
  if (isNaN(val)) callback(new Error("请输入数字值"));
  else if (val < 0) callback(new Error("不能小于0"));
  else if (val < props.track.startTiming) callback(new Error("不能小于轨道开始时机"));
  else if (val > props.track.endTiming) callback(new Error("不能大于轨道结束时机"));
  else callback();
};

const rules = {
  startTime: [{ required: true, validator: checkStartTime, trigger: "blur" }],
  endTime: [{ required: true, validator: checkEndTime, trigger: "blur" }],
  startWidth: [{ required: true, message: "起始宽度不能为空", trigger: "blur" }],
  endWidth: [{ required: true, message: "终止宽度不能为空", trigger: "blur" }],
};

const left = computed(() => {
  return (props.operation.startTime / props.displayAreaTime) * (props.global.documentWidth - 300);
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
            if (op.startTime !== undefined) points.push(op.startTime);
            if (op.endTime !== undefined) points.push(op.endTime);
          }
        });
      });
    });
  }
  if (props.chart.changeBackgroundOperations) {
    props.chart.changeBackgroundOperations.forEach(op => {
      if (op.startTime !== undefined) points.push(op.startTime);
      if (op.endTime !== undefined) points.push(op.endTime);
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
  const msThreshold = (pxThreshold / (props.global.documentWidth - 300)) * props.displayAreaTime;
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
      if (start < op.endTime && end > op.startTime) return true;
    }
  }
  return false;
};

const longOperationCanMove = () => {
  canMove.value = true;
  dragStartX.value = props.global.clientX;
  dragStartTiming.value = props.operation.startTime;
  dragEndTiming.value = props.operation.endTime;
  
  snapPoints.value = collectSnapPoints(props.operation);
};

const startLeftMove = () => {
  leftMove.value = true;
  dragStartX.value = props.global.clientX;
  dragStartTiming.value = props.operation.startTime;
  dragEndTiming.value = props.operation.endTime;
  
  snapPoints.value = collectSnapPoints(props.operation);
};

const startRightMove = () => {
  rightMove.value = true;
  dragStartX.value = props.global.clientX;
  dragStartTiming.value = props.operation.startTime;
  dragEndTiming.value = props.operation.endTime;
  
  snapPoints.value = collectSnapPoints(props.operation);
};

watch(() => props.global.mouseMove, () => {
  if (canMove.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - 300)) * props.displayAreaTime);
    
    const duration = dragEndTiming.value - dragStartTiming.value;
    let newStart = dragStartTiming.value + deltaTime;
    
    newStart = getSnappedTime(newStart, snapPoints.value);
    let newEnd = newStart + duration;
    
    if (newStart < props.track.startTiming) {
      newStart = props.track.startTiming;
      newEnd = newStart + duration;
    }
    if (newEnd > props.track.endTiming) {
      newEnd = props.track.endTiming;
      newStart = newEnd - duration;
    }

    if (!checkOverlap(newStart, newEnd, props.operation)) {
      props.operation.startTime = newStart;
      props.operation.endTime = newEnd;
      updateTemp();
    }
    
  } else if (leftMove.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - 300)) * props.displayAreaTime);
    
    let newStart = dragStartTiming.value + deltaTime;
    newStart = getSnappedTime(newStart, snapPoints.value);
    
    if (newStart < props.track.startTiming) newStart = props.track.startTiming;
    if (newStart > props.operation.endTime - 20) newStart = props.operation.endTime - 20;
    
    if (!checkOverlap(newStart, props.operation.endTime, props.operation)) {
      props.operation.startTime = newStart;
      updateTemp();
    }
    
  } else if (rightMove.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - 300)) * props.displayAreaTime);
    
    let newEnd = dragEndTiming.value + deltaTime;
    newEnd = getSnappedTime(newEnd, snapPoints.value);
    
    if (newEnd > props.track.endTiming) newEnd = props.track.endTiming;
    if (newEnd < props.operation.startTime + 20) newEnd = props.operation.startTime + 20;
    
    if (!checkOverlap(props.operation.startTime, newEnd, props.operation)) {
      props.operation.endTime = newEnd;
      updateTemp();
    }
  }
});

const startEdit = () => {
  edit.value = true;
  updateTemp();
};

const saveOperation = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      Object.assign(props.operation, tempOperation);
      edit.value = false;
      updateTrack();
    }
  });
};

const deleteSelf = () => {
  const index = props.track.changeWidthOperations.indexOf(props.operation);
  if (index !== -1) {
    props.track.changeWidthOperations.splice(index, 1);
    updateTrack();
  }
};

const deleteOperation = () => {
  ElMessageBox.confirm("您确定删除该操作?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    deleteSelf();
    ElNotification({ title: "成功", message: "删除成功", type: "success" });
  }).catch(() => {});
};

const selfClicked = () => {
  if (props.currentNoteType === 3) deleteSelf();
  else if (props.enableEdit) startEdit();
};

watch(() => props.global.mouseUp, () => {
  canMove.value = false;
  leftMove.value = false;
  rightMove.value = false;
  deleteMenuVisible.value = false;
});

const deleteMenuVisible = ref(false);
const openDeleteMenu = () => {
  deleteMenuVisible.value = true;
};

watch(() => props.global.mouseMove, () => {
  if (canMove.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - 300)) * props.displayAreaTime);
    
    const duration = dragEndTiming.value - dragStartTiming.value;
    let newStart = roundTime(dragStartTiming.value + deltaTime);
    
    if (newStart < props.track.startTiming) newStart = props.track.startTiming;
    if (newStart + duration > props.track.endTiming) newStart = props.track.endTiming - duration;
    
    props.operation.startTime = newStart;
    props.operation.endTime = newStart + duration;
    updateTemp();
  } else if (leftMove.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - 300)) * props.displayAreaTime);
    
    let newStart = roundTime(dragStartTiming.value + deltaTime);
    if (newStart < props.track.startTiming) newStart = props.track.startTiming;
    if (newStart > dragEndTiming.value - 100) newStart = dragEndTiming.value - 100;
    
    props.operation.startTime = newStart;
    updateTemp();
  } else if (rightMove.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - 300)) * props.displayAreaTime);
    
    let newEnd = roundTime(dragEndTiming.value + deltaTime);
    if (newEnd < dragStartTiming.value + 100) newEnd = dragStartTiming.value + 100;
    if (newEnd > props.track.endTiming) newEnd = props.track.endTiming;
    
    props.operation.endTime = newEnd;
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
