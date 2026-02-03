<template>
  <div :class="currentClass" class="glass-card">
    <div class="card-header">
      <div class="thumbnail-container">
        <img :src="operation.background" class="thumbnail-img" alt="bg-preview" />
      </div>

      <div class="info-side">
        <div class="title-row">
          <div class="op-index">操作 {{ operation.index + 1 }}</div>
          <div class="action-buttons">
            <button v-if="!operation.edit" class="icon-btn edit" @click.stop="startEdit" title="编辑">
              <Setting class="svg-icon" />
            </button>
            
            <button v-if="operation.edit && !operation.isNew" class="icon-btn cancel" @click.stop="operation.edit = false" title="取消">
              <CircleClose class="svg-icon" />
            </button>
            
            <button v-if="operation.edit" class="icon-btn save" @click.stop="saveOperation" title="保存">
              <CircleCheck class="svg-icon" />
            </button>
            
            <button class="icon-btn delete" @click.stop="deleteOperation" title="删除">
              <Delete class="svg-icon" />
            </button>
          </div>
        </div>
        <div class="timing-row">
          <span class="label">时机</span>
          <span class="value">{{ operation.startTime }}</span>
        </div>
      </div>
    </div>

    <div v-if="operation.edit" class="edit-form animate__animated animate__fadeInDown">
      <div class="form-grid">
        <div class="form-item full">
          <label>触发时机</label>
          <input type="number" v-model.number="tempOperation.startTime" class="custom-input" @keydown.enter="saveOperation" />
        </div>

        <div class="form-item half">
          <label>结束时机 (可选)</label>
          <input type="number" v-model.number="tempOperation.endTime" class="custom-input" placeholder="留空则持续至下一个" @keydown.enter="saveOperation" />
        </div>

        <div class="form-item full">
          <label>背景 URL</label>
          <input type="text" v-model="tempOperation.background" class="custom-input" @keydown.enter="saveOperation" />
        </div>

        <div class="form-item full">
          <label>更换背景</label>
          <div class="upload-area">
             <label class="upload-btn" :class="{ disabled: !tempOperation.startTime }">
                <input 
                  type="file" 
                  accept="image/*" 
                  @change="handleFileChange" 
                  :disabled="!tempOperation.startTime"
                />
                <span>{{ !tempOperation.startTime ? '请先填写时机' : '点击上传本地图片' }}</span>
             </label>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject, ref, reactive, computed, defineProps, defineEmits } from 'vue';
import { Setting, CircleClose, CircleCheck, Delete } from '@element-plus/icons-vue';
import { ElMessageBox, ElNotification } from 'element-plus';
import axios from 'axios';

const props = defineProps({
  operation: Object,
  global: Object,
  chart: Object
});

const emit = defineEmits(["editStatus"]);
const syncAction = inject('syncAction');
const tempOperation = reactive({});

const currentClass = computed(() => {
  let cls = props.operation.edit ? "edit " : "not-edit ";
  const { currentTime } = props.global;
  const { startTime, endTime } = props.operation;
  
  if (currentTime > startTime && currentTime < (endTime || Infinity)) {
    cls += "current-operation ";
  } else if (currentTime > (endTime || Infinity)) {
    cls += "passed-operation ";
  } else {
    cls += "to-come-operation ";
  }

  if (props.operation.isPending || props.operation.isDeleting) {
    cls += "pending-operation ";
  }
  
  return cls;
});

const updateOperation = () => {
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};

const startEdit = () => {
  if (props.operation.isPending || props.operation.isDeleting) return;
  props.operation.edit = true;
  Object.assign(tempOperation, JSON.parse(JSON.stringify(props.operation)));
};

const handleFileChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append('background', file);
  formData.append('songId', props.chart.songId);
  formData.append('startTime', tempOperation.startTime);

  try {
    const response = await axios.post('/api/chart/uploadBackground', formData, {
      withCredentials: true,
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    
    if (response.data.code === 0) {
      tempOperation.background = response.data.data.background;
      ElNotification({ title: "上传成功", message: "图片已上传", type: "success" });
      saveOperation();
    }
  } catch (err) {
    ElNotification({ title: "错误", message: "上传失败", type: "error" });
  }
};

const saveOperation = () => {
  if (tempOperation.startTime === undefined || tempOperation.startTime === '') {
    ElNotification({ title: "错误", message: "请填写时机", type: "error" });
    return;
  }
  
  if (tempOperation.endTime === "") tempOperation.endTime = null;
  Object.assign(props.operation, tempOperation);
  props.operation.edit = false;

  if (syncAction) syncAction("UPDATE_BG_OP", props.operation);
  
  updateOperation();
  emit("editStatus", true);
  props.operation.isNew = false;
};

const deleteOperation = () => {
  if (props.operation.isDeleting) return;
  ElMessageBox.confirm("您确定删除该操作?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    props.operation.isDeleting = true;
    if (syncAction) syncAction("DELETE_BG_OP", props.operation.id);
  }).catch(() => {});
};
</script>

<style scoped>
.glass-card {
  box-sizing: border-box;
  background: rgba(35, 35, 35, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  margin: 5px 12px;
  padding: 5px 12px;
  height: 80px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.glass-card:hover {
  background: rgba(60, 60, 60, 0.7);
  border-color: rgba(255, 255, 255, 0.2);
}

.edit.glass-card {
  height: 300px; /* Refined expanded height */
}

.current-operation {
  border-color: rgba(64, 158, 255, 0.6);
  box-shadow: 0 0 15px rgba(64, 158, 255, 0.2);
}

.passed-operation {
  opacity: 0.6;
}

.pending-operation {
  opacity: 0.5;
  filter: grayscale(100%);
  pointer-events: none;
}

.card-header {
  display: flex;
  gap: 12px;
}

.thumbnail-container {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  background: #1a1a1a;
}

.thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-side {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.op-index {
  font-size: 14px;
  font-weight: 600;
  color: #eee;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.icon-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  color: #999;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.icon-btn:hover {
  background: rgba(255,255,255,0.1);
  color: white;
}

.svg-icon {
  width: 16px;
  height: 16px;
}

.timing-row {
  font-size: 13px;
  color: #aaa;
}

.timing-row .value {
  color: #fff;
  margin-left: 8px;
  font-family: monospace;
}

.edit-form {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255,255,255,0.05);
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-item label {
  font-size: 12px;
  color: #888;
  display: block;
  margin-bottom: 4px;
}

.custom-input {
  width: 100%;
  box-sizing: border-box;
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  color: white;
  padding: 8px 10px;
  font-size: 13px;
  outline: none;
}

.upload-area {
  width: 100%;
}

.upload-btn {
  display: block;
  background: rgba(255, 255, 255, 0.05);
  border: 1px dashed rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  padding: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
  color: #bbb;
}

.upload-btn:hover:not(.disabled) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.4);
  color: white;
}

.upload-btn input {
  display: none;
}

.upload-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
