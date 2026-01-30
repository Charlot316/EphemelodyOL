<template>
  <div :class="currentClass" @dblclick="handleCurrentTrack">
    <div
      style="width:100%;display: flex;justify-content: space-between;border: none;"
    >
      <div
        style="width: 70px;height:70px;border-radius: 5px;position:relative;pointer-events: none;"
      >
        <el-image
          style="position:absolute;top:0;left:0;width: 70px;height:70px;border-radius: 5px;"
          :src="trackImage"
          fit="fit"
          class="image"
        />
        <div
          style="position:absolute;top:0;left:0;width: 70px;height:70px;border-radius: 5px;
        text-align:center; line-height: 70px; color:white; text-shadow:2px 2px 5px black; font-size: 50px;"
        >
          {{ track.type == 1 ? track.key?.toUpperCase() : "虚" }}
        </div>
        <div
          v-if="
            global.currentTime > track.startTiming &&
              global.currentTime < track.endTiming
          "
          :style="{
            position: 'absolute',
            height: '70px',
            top: 0,
            left: (track.tempPositionX - track.tempWidth) * 160 + 75 + 'px',
            width: 2 * track.tempWidth * 160 + 'px',
            background:
              'rgba(' +
              track.tempR +
              ',' +
              track.tempG +
              ',' +
              track.tempB +
              ',0.5)',
          }"
        ></div>
      </div>
      <div style="width:calc(100% - 80px);">
        <div
          style="width:100%;height:20px; display: flex;justify-content: space-between; align-items: center;line-height: 20px;"
        >
          <div style="font-weight:800">轨道{{ track.index + 1 }}</div>
          <div style="display: flex; align-items: center;">
            <el-tooltip
              class="item"
              effect="dark"
              :content="
                track.showInTimeline ? '在时间轴中隐去' : '在时间轴中显示'
              "
              placement="top"
            >
              <el-button
                type="text"
                class="hide-button"
                @click="track.showInTimeline = !track.showInTimeline"
              >
                <el-icon><component :is="track.showInTimeline ? 'Minus' : 'View'" /></el-icon>
              </el-button>
            </el-tooltip>
            <el-button
              v-if="!track.edit"
              type="text"
              class="edit-button"
              @click="startEdit"
            >
              <el-icon><Setting /></el-icon>
            </el-button>
            <el-button
              v-if="track.edit && !track.isNew"
              type="text"
              class="cancel-button"
              @click="track.edit = false"
            >
              <el-icon><CircleClose /></el-icon>
            </el-button>
            <el-button
              v-if="track.edit"
              type="text"
              class="ok-button"
              @click="saveTrack"
            >
              <el-icon><CircleCheck /></el-icon>
            </el-button>
            <el-button
              type="text"
              class="delete-button"
              @click="deleteTrack"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        <div style="width:100%;margin-top:10px;">
          <h4>时机 {{ track.startTiming }}</h4>
        </div>
      </div>
    </div>
    <transition
      name="flip-list"
      enter-active-class="animate__animated animate__fadeInDown"
      leave-active-class="animate__animated animate__fadeOutUp"
    >
      <div v-show="track.edit" style="margin-top:20px;">
        <el-form
          :model="tempTrack"
          :rules="rules"
          ref="formRef"
          @submit.prevent="saveTrack"
        >
          <el-form-item label="轨道类别" label-width="80px" prop="type">
            <el-radio-group
              v-model="tempTrack.type"
              style="width:130px;line-height: 20px;"
            >
              <el-radio :label="0">虚轨</el-radio>
              <el-radio :label="1">实轨</el-radio>
            </el-radio-group>

            <el-tooltip
              class="item"
              effect="dark"
              content="设置轨道的类别"
              placement="top-start"
            >
               <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item label="按键" label-width="80px" prop="key">
            <el-input
              @keydown.enter="saveTrack"
              v-model="tempTrack.key"
              style="width:130px"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="设置轨道的按键"
              placement="top-start"
            >
               <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item label="开始时机" label-width="100px" prop="startTiming">
            <el-input
              @keydown.enter="saveTrack"
              v-model="tempTrack.startTiming"
              style="width:130px"
            />
          </el-form-item>
          <el-form-item label="结束时机" label-width="100px" prop="endTiming">
            <el-input
              @keydown.enter="saveTrack"
              v-model="tempTrack.endTiming"
              style="width:130px"
            />
          </el-form-item>
          <el-form-item label="横坐标" label-width="80px" prop="positionX">
            <el-input
              @keydown.enter="saveTrack"
              v-model="tempTrack.positionX"
              style="width:130px"
            />
          </el-form-item>
          <el-form-item label="宽度" label-width="80px" prop="width">
            <el-input
              @keydown.enter="saveTrack"
              v-model="tempTrack.width"
              style="width:130px"
            />
          </el-form-item>
          <el-form-item label="默认颜色" label-width="80px" prop="color">
            <el-color-picker
              v-model="tempTrack.color"
              color-format="rgb"
            />
          </el-form-item>
        </el-form>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, defineProps, defineEmits, onMounted } from 'vue';
import { Setting, CircleClose, CircleCheck, Delete, QuestionFilled, Minus, View } from '@element-plus/icons-vue';
import { ElMessageBox, ElNotification } from 'element-plus';
import trackImage from '@/assets/img/EpheTrack.jpg';

const props = defineProps({
  track: Object,
  global: Object,
  chart: Object
});

const emit = defineEmits(["editStatus", "currentTrack"]);

const formRef = ref(null);
const tempTrack = reactive({});

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
  else if (val < 0) callback(new Error("开始时机不能小于0"));
  else if (val > props.chart.songLength) callback(new Error("开始时机不能超过歌曲长度"));
  else if (val > tempTrack.endTiming) callback(new Error("开始时机不能超过结束时机"));
  else callback();
};

const checkEndTime = (rule, value, callback) => {
  if (value !== 0 && !value) return callback(new Error("结束时机不能为空"));
  const val = parseFloat(value);
  if (isNaN(val)) callback(new Error("请输入数字值"));
  else if (val < 0) callback(new Error("时机不能小于0"));
  else if (val > props.chart.songLength) callback(new Error("时机不能超过歌曲长度"));
  else callback();
};

const rules = {
  type: [{ required: true, message: "请选择轨道类别", trigger: "blur" }],
  key: [{ required: true, validator: checkKey, trigger: "blur" }],
  startTiming: [{ required: true, validator: checkStartTime, trigger: "blur" }],
  endTiming: [{ required: true, validator: checkEndTime, trigger: "blur" }],
  width: [{ required: true, message: "宽度不能为空", trigger: "blur" }],
  positionX: [{ required: true, message: "横坐标不能为空", trigger: "blur" }],
  color: [{ required: true, message: "请选择颜色", trigger: "blur" }],
};

const currentClass = computed(() => {
  let cls = props.track.edit ? "edit-track " : "not-edit-track ";
  const { currentTime } = props.global;
  const { startTiming, endTiming } = props.track;
  
  if (currentTime > startTiming && currentTime < endTiming) cls += "current-track";
  else if (currentTime > endTiming) cls += "passed-track";
  else cls += "to-come-track";
  return cls;
});

const updateTrack = () => {
  props.global.reCalculateTrack = !props.global.reCalculateTrack;
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};

const startEdit = () => {
  props.track.edit = true;
  document.querySelector("#trackCard" + props.track.index)?.scrollIntoView({ behavior: "smooth" });
  Object.assign(tempTrack, JSON.parse(JSON.stringify(props.track)));
  tempTrack.key = tempTrack.key?.toUpperCase();
  tempTrack.color = `rgb(${props.track.R},${props.track.G},${props.track.B})`;
};

const handleCurrentTrack = () => {
  emit("currentTrack", props.track);
  startEdit();
};

const saveTrack = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      setTimeout(updateTrack, 500);
      Object.assign(props.track, tempTrack);
      props.track.key = props.track.key.toUpperCase();
      const rgb = tempTrack.color.match(/\d+/g);
      if (rgb) {
        props.track.R = rgb[0];
        props.track.G = rgb[1];
        props.track.B = rgb[2];
      }
      props.track.edit = false;
      if (props.track.isNew) emit("editStatus", true);
      props.track.isNew = false;
    }
  });
};

const deleteTrack = () => {
  ElMessageBox.confirm("您确定删除该轨道?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    if (props.track.isNew) emit("editStatus", true);
    props.chart.tracks.splice(props.track.index, 1);
    updateTrack();
    ElNotification({ title: "成功", message: "删除成功", type: "success" });
  }).catch(() => {});
};

onMounted(() => {
  if (props.track.isNew) {
     props.track.showInTimeline = true;
  }
});
</script>

<style scoped>
.not-edit-track {
  height: 70px;
  width: calc(100% - 30px);
  margin: 10px;
  padding: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.edit-track {
  height: 505px;
  width: calc(100% - 30px);
  margin: 10px;
  padding: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.hide-button { color: rgb(218, 218, 218); }
.hide-button:hover { color: rgb(239, 239, 239); }
.hide-button:active { color: rgb(183, 183, 183); }
.delete-button { color: #f56c6c; }
.delete-button:hover { color: #f89898; }
.delete-button:active { color: #c45656; }
.ok-button { color: #67c23a; }
.ok-button:hover { color: #95d475; }
.ok-button:active { color: #529b2e; }
.cancel-button { color: #909399; }
.cancel-button:hover { color: #b1b3b8; }
.cancel-button:active { color: #73767a; }

.current-track {
  background: rgb(47, 47, 47);
  color: rgb(171, 171, 171);
  box-shadow: 0 0 5px 2px rgba(255, 255, 255, 0.5);
  transition: 0.5s;
}

:deep(.current-track .el-form-item__label) {
  color: rgb(171, 171, 171);
}

.passed-track {
  background: rgb(30, 30, 30);
  color: rgb(100, 100, 100);
  box-shadow: 0 0 0px 0px rgba(127, 127, 127, 0.5);
  transition: 0.5s;
}

:deep(.passed-track .el-form-item__label) {
  color: rgb(171, 171, 171);
}

.to-come-track {
  background: #2f2f2f;
  color: rgb(171, 171, 171);
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
  transition: 0.5s;
}

:deep(.to-come-track .el-form-item__label) {
  color: rgb(171, 171, 171);
}
</style>
