<template>
  <div
    @click="selfClicked"
    :style="{
      position: 'absolute',
      top: '20px',
      left: left + 'px',
      zIndex: operation.zIndex,
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
                @mousedown="leftMove = true"
                style="width:1px;height:40px;position:absolute;left:0px;top:0;cursor:w-resize;background:transparent;"
              />
              <div
                @mousedown="rightMove = true"
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

const longOperationCanMove = () => {
  setTimeout(() => {
    passedTime.value = Math.ceil(props.global.currentTime - props.operation.startTime);
  }, 10);
  canMove.value = true;
};

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
});

watch(() => props.global.mouseMove, () => {
  if (canMove.value) {
    if (props.global.currentTime > props.track.startTiming && props.global.currentTime < props.track.endTiming) {
      const duration = props.operation.endTime - props.operation.startTime;
      props.operation.startTime = roundTime(props.global.currentTime - passedTime.value);
      props.operation.endTime = props.operation.startTime + duration;
      updateTemp();
    }
  } else if (leftMove.value) {
    if (roundTime(props.global.currentTime) <= props.operation.endTime) {
      props.operation.startTime = roundTime(props.global.currentTime);
      updateTemp();
    }
  } else if (rightMove.value) {
    if (roundTime(props.global.currentTime) >= props.operation.startTime) {
      props.operation.endTime = roundTime(props.global.currentTime);
      updateTemp();
    }
  }
});

onMounted(() => {
  props.operation.zIndex = 0;
  updateTemp();
});
</script>
