<template>
  <div :class="currentClass">
    <div
      style="width:100%;display: flex;justify-content: space-between;border: none;"
    >
      <div>
        <el-image
          style="width: 70px;height:70px;border-radius: 5px;"
          :src="operation.background"
          fit="fit"
          class="image"
          :preview-src-list="[operation.background]"
        />
      </div>
      <div style="width:calc(100% - 80px);">
        <div
          style="width:100%;height:20px; display: flex;justify-content: space-between; align-items: center;line-height: 20px;"
        >
          <div style="font-weight:800">操作{{ operation.index + 1 }}</div>
          <div style="display: flex; align-items: center;">
            <el-button
              v-if="!operation.edit"
              type="text"
              class="edit-button"
              @click="startEdit"
            >
              <el-icon><Setting /></el-icon>
            </el-button>
            <el-button
              v-if="operation.edit && !operation.isNew"
              type="text"
              class="cancel-button"
              @click="operation.edit = false"
            >
              <el-icon><CircleClose /></el-icon>
            </el-button>
            <el-button
              v-if="operation.edit"
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
        </div>
        <div style="width:100%;margin-top:10px;">
          <h4>时机 {{ operation.startTime }}</h4>
        </div>
      </div>
    </div>
    <transition
      name="flip-list"
      enter-active-class="animate__animated animate__fadeInDown"
      leave-active-class="animate__animated animate__fadeOutUp"
    >
      <div v-show="operation.edit">
        <el-form
          :model="tempOperation"
          :rules="rules"
          ref="formRef"
          @submit.prevent="saveOperation"
        >
          <el-form-item label="时机" label-width="80px" prop="startTime">
            <el-input
              @keydown.enter="saveOperation"
              v-model="tempOperation.startTime"
              style="width:100px"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="设置操作的时机"
              placement="top-start"
            >
               <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item label="背景" label-width="80px" prop="background">
            <el-input
              @keydown.enter="saveOperation"
              v-model="tempOperation.background"
              style="width:100px"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="输入背景图片的url"
              placement="top-start"
            >
               <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item label="手动上传" label-width="80px">
            <el-upload
              class="upload-demo"
              action="/api/chart/uploadBackground"
              :with-credentials="true"
              name="background"
              :data="{
                songId: $route.query.songId,
                startTime: tempOperation.startTime,
              }" 
              :on-success="handleUploadSuccess"
              :disabled="!tempOperation.startTime"
            >
              <el-button
                size="small"
                :disabled="!tempOperation.startTime"
              >
                {{ !tempOperation.startTime ? "请先填写时机" : "点击上传" }}
              </el-button>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, defineProps, defineEmits } from 'vue';
import { Setting, CircleClose, CircleCheck, Delete, QuestionFilled } from '@element-plus/icons-vue';
import { ElMessageBox, ElNotification } from 'element-plus';

const props = defineProps({
  operation: Object,
  global: Object,
  chart: Object
});

const emit = defineEmits(["editStatus"]);

const formRef = ref(null);
const tempOperation = reactive({});

const checkStartTime = (rule, value, callback) => {
  if (!value && value !== 0) {
    return callback(new Error("时机不能为空"));
  }
  const val = parseFloat(value);
  if (isNaN(val)) {
    callback(new Error("请输入数字值"));
  } else {
    if (val < 0) {
      callback(new Error("时机不能小于0"));
    } else if (val > props.chart.songLength) {
      callback(new Error("时机不能超过歌曲长度"));
    } else {
      callback();
    }
  }
};

const rules = {
  startTime: [{ required: true, validator: checkStartTime, trigger: "blur" }],
  background: [{ required: true, message: '背景不能为空', trigger: "blur" }],
};

const currentClass = computed(() => {
  let cls = props.operation.edit ? "edit " : "not-edit ";
  const { currentTime } = props.global;
  const { startTime, endTime } = props.operation;
  
  if (currentTime > startTime && currentTime < endTime) {
    cls += "current-operation";
  } else if (currentTime > endTime) {
    cls += "passed-operation";
  } else {
    cls += "to-come-operation ";
  }
  return cls;
});

const updateOperation = () => {
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};

const startEdit = () => {
  props.operation.edit = true;
  Object.assign(tempOperation, JSON.parse(JSON.stringify(props.operation)));
};

const handleUploadSuccess = (response) => {
  tempOperation.background = response.data.background;
  ElNotification({ title: "上传成功", message: "图片已上传至服务器", type: "success" });
  saveOperation();
};

const saveOperation = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      setTimeout(updateOperation, 500);
      emit("editStatus", true);
      Object.assign(props.operation, tempOperation);
      props.operation.edit = false;
      props.operation.isNew = false;
    }
  });
};

const deleteOperation = () => {
  ElMessageBox.confirm("您确定删除该操作?", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    if (props.operation.isNew) emit("editStatus", true);
    props.chart.changeBackgroundOperations.splice(props.operation.index, 1);
    updateOperation();
    ElNotification({ title: "成功", message: "删除成功", type: "success" });
  }).catch(() => {});
};
</script>

<style scoped>
.not-edit {
  height: 70px;
  width: calc(100% - 30px);
  margin: 10px;
  padding: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.edit {
  height: 270px;
  width: calc(100% - 30px);
  margin: 10px;
  padding: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.current-operation {
  background: rgb(47, 47, 47);
  color: rgb(171, 171, 171);
  box-shadow: 0 0 5px 2px rgba(255, 255, 255, 0.5);
  transition: 0.5s;
}

:deep(.current-operation .el-form-item__label) {
  color: rgb(171, 171, 171);
}

.passed-operation {
  background: rgb(30, 30, 30);
  color: rgb(100, 100, 100);
  box-shadow: 0 0 0px 0px rgba(127, 127, 127, 0.5);
  transition: 0.5s;
}

:deep(.passed-operation .el-form-item__label) {
  color: rgb(171, 171, 171);
}

.to-come-operation {
  background: #2f2f2f;
  color: rgb(171, 171, 171);
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
  transition: 0.5s;
}

:deep(.to-come-operation .el-form-item__label) {
  color: rgb(171, 171, 171);
}
.delete-button { color: #f56c6c; }
.delete-button:hover { color: #f89898; }
.delete-button:active { color: #c45656; }
.ok-button { color: #67c23a; }
.ok-button:hover { color: #95d475; }
.ok-button:active { color: #529b2e; }
.cancel-button { color: #909399; }
.cancel-button:hover { color: #b1b3b8; }
.cancel-button:active { color: #73767a; }
</style>
