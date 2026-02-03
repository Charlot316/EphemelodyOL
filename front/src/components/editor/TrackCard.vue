<template>
  <div :class="[currentClass, 'track-row-container']">
    <div class="glass-card" @dblclick="handleCurrentTrack">
      <!-- Spatial Indicator Bar (Spatial Preview) -->
      <div
        v-if="global.currentTime > track.startTiming && global.currentTime < track.endTiming"
        class="spatial-indicator"
        :style="{
          left: (track.tempPositionX - track.tempWidth) * 160 + 75 + 'px',
          width: 2 * track.tempWidth * 160 + 'px',
          background: `rgba(${track.tempR}, ${track.tempG}, ${track.tempB}, 0.6)`
        }"
      ></div>

      <div class="card-header">
        <div class="thumbnail-container">
          <img :src="trackImage" class="thumbnail-img" alt="track-preview" />
          <div class="key-overlay">
            {{ track.type == 1 ? track.key?.toUpperCase() : "虚" }}
          </div>
        </div>

        <div class="info-side">
          <div class="title-row">
            <div class="track-index">轨道 {{ track.index + 1 }}</div>
            <div class="action-buttons">
              <button
                class="icon-btn"
                :title="track.showInTimeline ? '轴上隐藏' : '轴上显示'"
                @click.stop="track.showInTimeline = !track.showInTimeline"
                :class="{ 'dimmed': !track.showInTimeline }"
              >
                <component :is="track.showInTimeline ? 'View' : 'Hide'" class="svg-icon" />
              </button>
              
              <button v-if="!track.edit" class="icon-btn" @click.stop="startEdit" title="编辑">
                <Setting class="svg-icon" />
              </button>
              
              <button v-if="track.edit && !track.isNew" class="icon-btn" @click.stop="track.edit = false" title="取消">
                <CircleClose class="svg-icon" />
              </button>
              
              <button v-if="track.edit" class="icon-btn save" @click.stop="saveTrack" title="保存">
                <CircleCheck class="svg-icon" />
              </button>
              
            </div>
          </div>
          <div class="timing-info">
            <span class="label">TIMING</span>
            <span class="value">{{ track.startTiming }}</span>
          </div>
        </div>
      </div>

      <div v-if="track.edit" class="edit-form-scrollable animate__animated animate__fadeIn">
        <div class="form-vertical-layout">
          <div class="form-item">
            <label>轨道类别</label>
            <div class="custom-radio-group">
              <label class="radio-label">
                <input type="radio" v-model="tempTrack.type" :value="0" />
                <span>虚轨</span>
              </label>
              <label class="radio-label">
                <input type="radio" v-model="tempTrack.type" :value="1" />
                <span>实轨</span>
              </label>
            </div>
          </div>

          <div class="form-item">
            <label>映射按键</label>
            <input type="text" v-model="tempTrack.key" class="custom-input" @keydown.enter="saveTrack" />
          </div>

          <div class="form-item">
            <label>开始时机 (ms)</label>
            <input type="number" v-model.number="tempTrack.startTiming" class="custom-input" @keydown.enter="saveTrack" />
          </div>

          <div class="form-item">
            <label>结束时机 (ms)</label>
            <input type="number" v-model.number="tempTrack.endTiming" class="custom-input" @keydown.enter="saveTrack" />
          </div>

          <div class="form-item">
            <label>横坐标 (X-Offset)</label>
            <input type="number" step="0.01" v-model.number="tempTrack.positionX" class="custom-input" @keydown.enter="saveTrack" />
          </div>

          <div class="form-item">
            <label>宽度 (Width)</label>
            <input type="number" step="0.01" v-model.number="tempTrack.width" class="custom-input" @keydown.enter="saveTrack" />
          </div>

          <div class="form-item">
            <label>主题色彩</label>
            <div class="color-picker-row">
               <input type="color" v-model="tempTrack.hexColor" class="custom-color-input" @change="onColorChange" />
               <input type="text" v-model="tempTrack.hexColor" class="custom-input hex-input" @change="onColorChange" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, defineProps, defineEmits, onMounted } from 'vue';
import { Setting, CircleClose, CircleCheck, Delete, QuestionFilled, Minus, View, Hide } from '@element-plus/icons-vue';
import { ElMessageBox, ElNotification } from 'element-plus';
import trackImage from '@/assets/img/EpheTrack.jpg';

const props = defineProps({
  track: Object,
  global: Object,
  chart: Object
});

const emit = defineEmits(["editStatus", "currentTrack"]);

const syncAction = inject('syncAction');
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
  
  if (currentTime > startTiming && currentTime < endTiming) cls += "current-track ";
  else if (currentTime > endTiming) cls += "passed-track ";
  else cls += "to-come-track ";

  if (props.track.isPending || props.track.isDeleting) cls += "pending-track ";
  
  return cls;
});

const updateTrack = () => {
  props.global.reCalculateTrack = !props.global.reCalculateTrack;
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};

const rgbToHex = (r, g, b) => {
  return "#" + ((1 << 24) + (parseInt(r) << 16) + (parseInt(g) << 8) + parseInt(b)).toString(16).slice(1);
};

const hexToRgb = (hex) => {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
  } : null;
};

const onColorChange = () => {
  const rgb = hexToRgb(tempTrack.hexColor);
  if (rgb) {
    tempTrack.R = rgb.r;
    tempTrack.G = rgb.g;
    tempTrack.B = rgb.b;
  }
};

const startEdit = () => {
  if (props.track.isPending || props.track.isDeleting) return;
  props.track.edit = true;
  document.querySelector("#trackCard" + props.track.index)?.scrollIntoView({ behavior: "auto" });
  Object.assign(tempTrack, JSON.parse(JSON.stringify(props.track)));
  tempTrack.key = tempTrack.key?.toUpperCase();
  tempTrack.hexColor = rgbToHex(props.track.R, props.track.G, props.track.B);
};

const handleCurrentTrack = () => {
  if (props.track.isPending || props.track.isDeleting) return;
  emit("currentTrack", props.track);
  startEdit();
};

const saveTrack = () => {
  if (!tempTrack.key || tempTrack.key.length !== 1) {
    ElNotification({ title: "错误", message: "按键必须为单个字母", type: "error" });
    return;
  }
  
  Object.assign(props.track, tempTrack);
  props.track.key = props.track.key.toUpperCase();
  props.track.edit = false;
  
  if (syncAction) syncAction("UPDATE_TRACK", props.track);
  
  if (props.track.isNew) emit("editStatus", true);
  props.track.isNew = false;
  
  updateTrack();
};


onMounted(() => {
  if (props.track.showInTimeline === undefined) {
    props.track.showInTimeline = true;
  }
  // Initialize tempTrack to avoid empty inputs before first edit
  Object.assign(tempTrack, JSON.parse(JSON.stringify(props.track)));
  tempTrack.hexColor = rgbToHex(props.track.R, props.track.G, props.track.B);
});
</script>

<style scoped>
.track-row-container {
  height: 90px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.track-row-container.edit-track {
  height: 525px;
  align-items: flex-start;
  padding-top: 8px;
}

.glass-card {
  width: 100%;
  height: 100%;
  background: rgba(45, 45, 45, 0.45);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 10px 12px;
  box-sizing: border-box;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: visible; /* Crucial for spatial indicator */
}

.track-row-container:hover .glass-card {
  background: rgba(55, 55, 55, 0.6);
  border-color: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.4);
}

.current-track .glass-card {
  border-color: rgba(64, 158, 255, 0.4);
  background: rgba(60, 60, 60, 0.6);
  box-shadow: 0 0 15px rgba(64, 158, 255, 0.1);
}

.pending-track .glass-card {
  opacity: 0.5;
  filter: grayscale(100%);
  pointer-events: none;
}

/* Spatial Indicator Bar */
.spatial-indicator {
  position: absolute;
  height: 2px;
  bottom: 0px;
  z-index: 100;
  pointer-events: none;
  border-radius: 1px;
  box-shadow: 0 0 8px rgba(255,255,255,0.3);
}

.card-header {
  display: flex;
  gap: 14px;
  height: 60px;
  align-items: center;
}

.thumbnail-container {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  background: #111;
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}

.thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.glass-card:hover .thumbnail-img {
  transform: scale(1.05);
}

.key-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 26px;
  font-weight: 900;
  text-shadow: 0 2px 8px rgba(0,0,0,0.8);
  background: rgba(0,0,0,0.15);
}

.info-side {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.track-index {
  font-size: 13px;
  font-weight: 700;
  color: #efefef;
  text-transform: uppercase;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.icon-btn {
  background: rgba(255,255,255,0.03);
  border: none;
  cursor: pointer;
  padding: 5px;
  border-radius: 6px;
  color: #999;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  background: rgba(255,255,255,0.1);
  color: #fff;
}

.icon-btn.delete:hover { color: #ff5e5e; }
.icon-btn.save:hover { color: #2ecc71; }
.icon-btn.dimmed { opacity: 0.3; }

.svg-icon {
  width: 15px;
  height: 15px;
}

.timing-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 10px;
  color: #777;
  font-weight: 500;
}

.timing-info .value {
  color: #51cf66;
  font-family: 'Consolas', monospace;
}

/* Scrollable Single Column Form */
.edit-form-scrollable {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid rgba(255,255,255,0.08);
  height: calc(100% - 75px);
  overflow-y: auto;
  overflow-x: hidden;
}

.edit-form-scrollable::-webkit-scrollbar {
  width: 4px;
}

.edit-form-scrollable::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.1);
  border-radius: 2px;
}

.form-vertical-layout {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 2px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.form-item label {
  font-size: 10px;
  font-weight: 700;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.custom-input {
  background: rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  color: #eee;
  padding: 8px 10px;
  font-size: 12px;
  outline: none;
  width: 100%;
  box-sizing: border-box;
}

.custom-input:focus {
  border-color: rgba(64, 158, 255, 0.4);
  background: rgba(0, 0, 0, 0.35);
}

.custom-radio-group {
  display: flex;
  gap: 14px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #bbb;
  cursor: pointer;
  font-size: 12px;
}

.color-picker-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.custom-color-input {
  appearance: none;
  -webkit-appearance: none;
  border: none;
  width: 38px;
  height: 32px;
  background: transparent;
  cursor: pointer;
  border-radius: 6px;
  flex-shrink: 0;
}

.custom-color-input::-webkit-color-swatch-wrapper { padding: 0; }
.custom-color-input::-webkit-color-swatch { border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 6px; }

.hex-input { font-family: monospace; font-size: 11px; }

/* Animation overrides */
.animate__animated {
  --animate-duration: 0.4s;
}
</style>
