<template>
  <div
    @click="selfClicked"
    :style="{
      position: 'absolute',
      top: '20px',
      left: left - 20 + 'px',
      zIndex: note.zIndex,
    }"
    @mousedown="setZIndex"
  >
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
          @click="saveNote"
        >
          <el-icon><CircleCheck /></el-icon>
        </el-button>
        <el-button
          type="text"
          class="delete-button"
          @click="deleteNote"
        >
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
      <el-form
        :model="tempNote"
        :rules="rules"
        ref="formRef"
        @submit.prevent="saveNote"
      >
        <el-form-item label="音符类别" label-width="80px" prop="noteType">
          <el-radio-group
            v-model="tempNote.noteType"
            size="small"
            style="width:130px;line-height: 20px;"
          >
            <el-radio :label="0">短键</el-radio>
            <el-radio :label="1">长键</el-radio>
            <el-radio :label="2">滑键</el-radio>
          </el-radio-group>
          <el-tooltip
            class="item"
            effect="dark"
            content="设置音符的类别"
            placement="top-start"
          >
             <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="按键" label-width="80px" prop="key">
          <el-input
            :disabled="track.type == 1"
            @keydown.enter="saveNote"
            v-model="tempNote.key"
            style="width:130px"
          />
          <el-tooltip
            class="item"
            effect="dark"
            content="设置音符的按键"
            placement="top-start"
          >
             <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="时机" label-width="80px" prop="timing">
          <el-input
            @keydown.enter="saveNote"
            v-model="tempNote.timing"
            style="width:130px"
          />
        </el-form-item>
        <el-form-item
          label="结束时机"
          label-width="80px"
          prop="endTiming"
          v-if="tempNote.noteType == 1"
        >
          <el-input
            @keydown.enter="saveNote"
            v-model="tempNote.endTiming"
            style="width:130px"
          />
        </el-form-item>
      </el-form>
      <template #reference>
        <div>
          <div v-if="note.noteType == 0">
            <el-image
              @dragstart.prevent
              @mousedown="canMove = true"
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
              @mousedown="leftMove = true"
              style="width:40px;height:40px;position:absolute;left:0;top:0;user-select: none;cursor:w-resize;"
              :src="hitNoteLeftImage"
            />
            <el-image
              @dragstart.prevent
              @mousedown="rightMove = true"
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
              @mousedown="canMove = true"
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

const longNoteCanMove = () => {
  passedTime.value = Math.ceil(props.global.currentTime - props.note.timing);
  canMove.value = true;
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
  canMove.value = false;
  leftMove.value = false;
  rightMove.value = false;
});

watch(() => props.global.mouseMove, () => {
  if (canMove.value) {
    if (props.global.currentTime > props.track.startTiming && props.global.currentTime < props.track.endTiming) {
      if (props.note.noteType === 1) {
        const duration = props.note.endTiming - props.note.timing;
        props.note.timing = roundTime(props.global.currentTime - passedTime.value);
        props.note.endTiming = props.note.timing + duration;
      } else {
        props.note.timing = roundTime(props.global.currentTime);
        props.note.endTiming = props.note.timing + 150;
      }
      updateTemp();
    }
  } else if (leftMove.value) {
    if (props.global.currentTime > props.track.startTiming && props.global.currentTime < props.note.endTiming - 150) {
      props.note.timing = roundTime(props.global.currentTime);
      updateTemp();
    }
  } else if (rightMove.value) {
    if (props.global.currentTime > props.note.timing + 150 && props.global.currentTime < props.track.endTiming) {
      props.note.endTiming = roundTime(props.global.currentTime);
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
</script>
