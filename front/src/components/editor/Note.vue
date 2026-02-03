<template>
  <div
    @click="selfClicked"
    @contextmenu.prevent.stop="openDeleteMenu"
    :style="{
      position: 'absolute',
      top: '20px',
      left: left - 20 + 'px',
      zIndex: note.zIndex,
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
      <div class="delete-menu-item" @click="deleteNote">删除</div>
    </div>

    <el-popover
      v-model:visible="edit"
      placement="top"
      :width="320"
      trigger="manual"
      popper-class="note-editor-popover"
    >
      <div class="note-edit-container">
        <div class="note-edit-header">
          <span class="edit-title">编辑音符</span>
          <div class="header-actions">
            <el-button
              circle
              size="small"
              class="action-btn delete"
              @click="deleteNote"
              title="删除音符"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
            <el-button
              circle
              size="small"
              class="action-btn cancel"
              @click="edit = false"
              title="取消更改"
            >
              <el-icon><CircleClose /></el-icon>
            </el-button>
            <el-button
              circle
              size="small"
              class="action-btn save"
              @click="saveNote"
              title="保存音符"
            >
              <el-icon><CircleCheck /></el-icon>
            </el-button>
          </div>
        </div>

        <el-form
          :model="tempNote"
          :rules="rules"
          ref="formRef"
          label-position="left"
          @submit.prevent="saveNote"
          class="note-edit-form"
        >
          <el-form-item label="音符类别" prop="noteType">
            <el-radio-group
              v-model="tempNote.noteType"
              size="small"
              class="custom-radio-group"
            >
              <el-radio-button :label="0">短键</el-radio-button>
              <el-radio-button :label="1">长键</el-radio-button>
              <el-radio-button :label="2">滑键</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="对应按键" prop="key">
            <div class="input-with-tip">
              <el-input
                :disabled="track.type == 1"
                @keydown.enter="saveNote"
                v-model="tempNote.key"
                placeholder="A-Z"
                class="custom-input small-input"
              />
              <el-tooltip content="设置音符触发的按键" placement="top">
                <el-icon class="input-tip"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </el-form-item>

          <el-form-item label="触发时机" prop="timing">
            <el-input-number
              v-model="tempNote.timing"
              :controls="false"
              class="custom-input-number"
            />
            <span class="unit-text">ms</span>
          </el-form-item>

          <el-form-item
            label="结束时机"
            prop="endTiming"
            v-if="tempNote.noteType == 1"
          >
            <el-input-number
              v-model="tempNote.endTiming"
              :controls="false"
              class="custom-input-number"
            />
            <span class="unit-text">ms</span>
          </el-form-item>
        </el-form>
      </div>
      <template #reference>
        <div>
          <div v-if="note.noteType == 0">
            <el-image
              @dragstart.prevent
              @mousedown="longNoteCanMove"
              style="width:40px;height:40px;user-select:none;cursor: move;"
              :src="hitNoteImage"
            />
          </div>
          <div v-if="note.noteType == 1">
            <div
              @mousedown="longNoteCanMove"
              :style="{
                userSelect: 'none',
                height: '38px',
                position: 'absolute',
                background: 'rgb(22, 22, 14)',
                cursor: 'move',
                width:
                  ((note.endTiming - note.timing) / displayAreaTime) *
                    (global.documentWidth - 300) +
                  'px',
                left: '20px',
                top: '1px',
              }"
            ></div>
            <el-image
              @dragstart.prevent
              @mousedown="startLeftMove"
              style="width:40px;height:40px;position:absolute;left:0;top:0;user-select: none;cursor:w-resize;"
              :src="hitNoteLeftImage"
            />
            <el-image
              @dragstart.prevent
              @mousedown="startRightMove"
              :style="{
                userSelect: 'none',
                height: '40px',
                width: '40px',
                position: 'absolute',
                cursor: 'e-resize',
                left:
                  ((note.endTiming - note.timing) / displayAreaTime) *
                    (global.documentWidth - 300) +
                  'px',
                top: '0px',
              }"
              :src="hitNoteRightImage"
            />
          </div>
          <div v-if="note.noteType == 2">
            <el-image
              @mousedown="longNoteCanMove"
              @dragstart.prevent
              style="width:40px;height:40px;cursor: move;"
              :src="slideNoteImage"
            />
          </div>
        </div>
      </template>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, defineProps, onMounted } from 'vue';
import { CircleClose, CircleCheck, Delete, QuestionFilled } from '@element-plus/icons-vue';
import { ElMessageBox, ElNotification } from 'element-plus';
import hitNoteImage from '@/assets/img/EpheHitNote.png';
import hitNoteLeftImage from '@/assets/img/EpheHitNoteLeft.png';
import hitNoteRightImage from '@/assets/img/EpheHitNoteRight.png';
import slideNoteImage from '@/assets/img/EpheSlideNote.png';

const props = defineProps({
  note: Object,
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
const tempNote = reactive({});

const checkKey = (rule, value, callback) => {
  if (!value) return callback(new Error("按键不能为空"));
  const reg = /^[A-Za-z]$/;
  if (reg.test(value)) callback();
  else callback(new Error("按键必须是单个字母"));
};

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
  else if (val < parseFloat(tempNote.timing) + 100) callback(new Error("长键长度不得小于100"));
  else callback();
};

const rules = {
  noteType: [{ required: true, message: "请选择音符类别", trigger: "blur" }],
  key: [{ required: true, validator: checkKey, trigger: "blur" }],
  timing: [{ required: true, validator: checkStartTime, trigger: "blur" }],
  endTiming: [{ required: true, validator: checkEndTime, trigger: "blur" }],
};

const left = computed(() => {
  return (props.note.timing / props.displayAreaTime) * (props.global.documentWidth - 300);
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
  Object.assign(tempNote, JSON.parse(JSON.stringify(props.note)));
  tempNote.key = tempNote.key?.toUpperCase();
};

const updateTrack = () => {
  props.global.reCalculateTrack = !props.global.reCalculateTrack;
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};

const setZIndex = () => {
  if (props.global.currentNote) props.global.currentNote.zIndex = 0;
  props.global.currentNote = props.note;
  props.note.zIndex = 10;
};

const dragStartX = ref(0);
const dragStartTiming = ref(0);
const dragEndTiming = ref(0);
const snapPoints = ref([]);

const collectSnapPoints = (currentNote) => {
  const points = [];
  
  // 1. All Track Elements
  if (props.chart.tracks) {
    props.chart.tracks.forEach(track => {
      // Notes
      if (track.notes) {
        track.notes.forEach(n => {
          if (n !== currentNote) {
            if (n.timing !== undefined) points.push(n.timing);
            if (n.endTiming !== undefined) points.push(n.endTiming);
          }
        });
      }
      // Operations
      ['moveOperations', 'changeWidthOperations', 'changeColorOperations'].forEach(key => {
        if (track[key]) {
          track[key].forEach(op => {
            if (op.startTime !== undefined) points.push(op.startTime);
            if (op.endTime !== undefined) points.push(op.endTime);
          });
        }
      });
    });
  }
  
  // 2. Background Operations
  if (props.chart.changeBackgroundOperations) {
    props.chart.changeBackgroundOperations.forEach(op => {
      if (op.startTime !== undefined) points.push(op.startTime);
      if (op.endTime !== undefined) points.push(op.endTime);
    });
  }
  
  // 3. Beat Lines
  if (props.chart.bpm > 0) {
    // Generate beat points around the current view roughly
    // optimization: only generate beats in valid rang e.g. 0 to songLength
    const msPerBeat = props.chart.bpm; // Assuming chart.bpm is ms per beat from memories
    const offset = props.chart.firstBeatDelay || 0;
    const songLen = props.chart.songLength || 300000;
    
    for (let t = offset; t <= songLen; t += msPerBeat) {
      points.push(t);
    }
  }
  
  return points;
};

const getSnappedTime = (time, points) => {
  // Threshold: ~10px converted to time
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
  for (const n of props.track.notes) {
    if (n === exclude) continue;
    // Check if collision with 100ms buffer
    // Overlap condition: start < other.end + 100 AND end > other.start - 100
    if (start < n.endTiming + 100 && end > n.timing - 100) return true;
  }
  return false;
};

const longNoteCanMove = () => {
  canMove.value = true;
  dragStartX.value = props.global.clientX;
  dragStartTiming.value = props.note.timing;
  dragEndTiming.value = props.note.endTiming;
  
  snapPoints.value = collectSnapPoints(props.note);
};

const startLeftMove = () => {
  leftMove.value = true;
  dragStartX.value = props.global.clientX;
  dragStartTiming.value = props.note.timing;
  dragEndTiming.value = props.note.endTiming;
  
  snapPoints.value = collectSnapPoints(props.note);
};

const startRightMove = () => {
  rightMove.value = true;
  dragStartX.value = props.global.clientX;
  dragStartTiming.value = props.note.timing;
  dragEndTiming.value = props.note.endTiming;
  
  snapPoints.value = collectSnapPoints(props.note);
};

const startEdit = () => {
  edit.value = true;
  updateTemp();
  if (props.note.noteType !== 1) {
    props.note.endTiming = parseInt(props.note.timing) + 150;
  }
};

const saveNote = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      Object.assign(props.note, tempNote);
      props.note.key = props.note.key.toUpperCase();
      edit.value = false;
      updateTrack();
    }
  });
};

const deleteSelf = () => {
  const index = props.track.notes.indexOf(props.note);
  if (index !== -1) {
    props.track.notes.splice(index, 1);
    updateTrack();
  }
};

const deleteNote = () => {
  ElMessageBox.confirm("您确定删除该音符?", "提示", {
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
  if (canMove.value || leftMove.value || rightMove.value) {
    canMove.value = false;
    leftMove.value = false;
    rightMove.value = false;
    snapPoints.value = [];
    
    // Re-sort track notes to keep order correct
    // (Though drag constraints prevent swapping, sorting is safe)
    props.track.notes.sort((a,b) => a.timing - b.timing);
    
    updateTrack();
  }
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
    
    // Original duration
    const duration = dragEndTiming.value - dragStartTiming.value;
    let newStart = dragStartTiming.value + deltaTime;
    
    // Snap
    newStart = getSnappedTime(newStart, snapPoints.value);
    let newEnd = newStart + duration;
    
    if (newStart < props.track.startTiming) {
       newStart = props.track.startTiming;
       newEnd = newStart + duration;
    }
    if (newEnd > props.track.endTiming) {
       newEnd = props.track.endTiming;
       newStart = newEnd - duration;
       if (newStart < props.track.startTiming) newStart = props.track.startTiming;
    }
    
    if (!checkOverlap(newStart, newEnd, props.note)) {
      props.note.timing = newStart;
      props.note.endTiming = newEnd;
      updateTemp();
    }
    
  } else if (leftMove.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - 300)) * props.displayAreaTime);
    
    let newStart = dragStartTiming.value + deltaTime;
    newStart = getSnappedTime(newStart, snapPoints.value);
    
    if (newStart < props.track.startTiming) newStart = props.track.startTiming;
    if (newStart >= props.note.endTiming - 50) newStart = props.note.endTiming - 50; 
    
    if (!checkOverlap(newStart, props.note.endTiming, props.note)) {
      props.note.timing = newStart;
      updateTemp();
    }
    
  } else if (rightMove.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - 300)) * props.displayAreaTime);
    
    let newEnd = dragEndTiming.value + deltaTime;
    newEnd = getSnappedTime(newEnd, snapPoints.value);
    
    if (newEnd > props.track.endTiming) newEnd = props.track.endTiming;
    if (newEnd <= props.note.timing + 50) newEnd = props.note.timing + 50;
    
    if (!checkOverlap(props.note.timing, newEnd, props.note)) {
      props.note.endTiming = newEnd;
      updateTemp();
    }
  }
});

onMounted(() => {
  props.note.zIndex = 0;
  if (props.note.noteType !== 1) {
    props.note.endTiming = parseInt(props.note.timing) + 150;
  }
  updateTemp();
});

onMounted(() => {
  props.note.zIndex = 0;
  if (props.note.noteType !== 1) {
    props.note.endTiming = parseInt(props.note.timing) + 150;
  }
  updateTemp();
});
</script>

<style scoped>
.note-edit-container {
  color: #fff;
}

.note-edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.edit-title {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
}

.action-btn:hover {
  transform: translateY(-2px);
}

.action-btn.delete:hover { border-color: #f56c6c !important; color: #f56c6c !important; }
.action-btn.cancel:hover { border-color: #909399 !important; color: #909399 !important; }
.action-btn.save:hover { border-color: #67c23a !important; color: #67c23a !important; }

.note-edit-form :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.7) !important;
  font-size: 13px;
}

.custom-radio-group :deep(.el-radio-button__inner) {
  background: rgba(0, 0, 0, 0.2) !important;
  color: #888 !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.custom-radio-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--accent-cyan, #00f3ff) !important;
  color: #000 !important;
  border-color: var(--accent-cyan, #00f3ff) !important;
}

.custom-input :deep(.el-input__wrapper) {
  background: rgba(0, 0, 0, 0.2) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.custom-input :deep(.el-input__inner) {
  color: #fff !important;
  text-align: center;
}

.small-input {
  width: 100px;
}

.custom-input-number {
  width: 120px;
}

.custom-input-number :deep(.el-input__wrapper) {
  background: rgba(0, 0, 0, 0.2) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.custom-input-number :deep(.el-input__inner) {
  color: #fff !important;
  text-align: left;
}

.unit-text {
  margin-left: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.input-tip {
  color: rgba(255, 255, 255, 0.3);
  cursor: help;
}

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

<style>
/* Global popover styling for glass effect */
.note-editor-popover {
  background: rgba(25, 25, 25, 0.85) !important;
  backdrop-filter: blur(12px) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 12px !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5) !important;
  padding: 16px !important;
}

.note-editor-popover .el-popper__arrow::before {
  background: rgba(25, 25, 25, 0.85) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}
</style>
