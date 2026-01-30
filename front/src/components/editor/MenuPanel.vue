<template>
  <div class="menu-panel-container">
    <div style="background:rgb(32, 32, 32);height:70px;">
      <h4
        style="padding:5px;padding-top:10px;padding-bottom:5x;color:rgb(225,225,225);padding-left:10px;"
      >
        背景操作
      </h4>
      <hr style="border:0.5px solid rgba(100,100,100,0.1)" />
      <div
        style="width:100%;height:20px;padding:5px;display: flex;
          justify-content: space-between; align-items: center;"
      >
        <div>
          <el-button
            type="text"
            style="margin-left:10px;"
            class="plus-button"
            @click="newOperation"
          >
            <el-icon><CirclePlus /></el-icon>
            新增
          </el-button>
        </div>
        <div>
          <el-button
            type="text"
            class="show-button"
            style="margin-right:5px;"
            @click="autoScroll = !autoScroll"
          >{{ autoScroll ? "关闭滚动" : "开启滚动" }}</el-button>
          <el-button
            type="text"
            class="show-button"
            style="margin-right:13px;"
            @click="operationShowAll = !operationShowAll"
          >{{ operationShowAll ? "显示当前" : "显示全部" }}</el-button>
        </div>
      </div>
    </div>
    <div
      class="menu-container-container"
      :style="{ height: Height - 70 + 'px' }"
    >
      <transition-group
        name="flip-list"
        enter-active-class="animate__animated animate__fadeInUp"
        leave-active-class="animate__animated animate__fadeOutUp"
      >
        <div
          v-for="operation in chart.changeBackgroundOperations"
          :key="operation.index || operation.startTime"
        >
          <transition
            name="flip-list"
            enter-active-class="animate__animated animate__fadeInUp"
            leave-active-class="animate__animated animate__fadeOutUp"
          >
            <BackgroundOperation
              v-show="
                operationShowAll ||
                  (global.currentTime > operation.startTime &&
                    global.currentTime < operation.endTime)
              "
              :id="'backgroundOperation' + operation.index"
              :chart="chart"
              :operation="operation"
              :global="global"
              @editStatus="handleEditStatus"
            />
          </transition>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, watch } from 'vue';
import { CirclePlus } from '@element-plus/icons-vue';
import { ElNotification } from 'element-plus';
import BackgroundOperation from "./BackgroundOperation.vue";
import "animate.css";

const props = defineProps({
  chart: Object,
  global: Object,
  Height: Number
});

const operationShowAll = ref(true);
const autoScroll = ref(true);
const editFinished = ref(true);

const updateOperation = () => {
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};

const handleEditStatus = (param) => {
  editFinished.value = param;
};

const newOperation = () => {
  if (editFinished.value) {
    editFinished.value = false;
    const op = {
      startTime: 0,
      isNew: true,
      background: props.chart.defaultBackground,
      edit: true
    };
    props.chart.changeBackgroundOperations.push(op);
    updateOperation();
    
    setTimeout(() => {
      document.querySelector("#backgroundOperation0")?.scrollIntoView({ behavior: "smooth" });
    }, 50);
  } else {
    ElNotification({
      title: "提示",
      message: "请先完成正在编辑的操作",
      type: "warning",
    });
    const editIndex = props.chart.changeBackgroundOperations.findIndex(op => op.edit);
    if (editIndex !== -1) {
      document.querySelector("#backgroundOperation" + editIndex)?.scrollIntoView({ behavior: "smooth" });
    }
  }
};

watch(() => props.global.currentTime, (newVal) => {
  if (autoScroll.value) {
    const ops = props.chart.changeBackgroundOperations;
    const index = ops.findIndex(op => newVal > op.startTime && newVal < op.endTime);
    if (index !== -1) {
      const scrollIndex = Math.max(0, index - 1);
      setTimeout(() => {
        document.querySelector("#backgroundOperation" + ops[scrollIndex].index)?.scrollIntoView({ behavior: "smooth" });
      }, 200);
    }
  }
});
</script>

<style scoped>
.menu-panel-container {
  padding: 0% 5%;
  width: calc(90% - 1px);
  background: rgb(32, 32, 32);
  border-right: 1px solid rgba(255, 255, 255, 0.2);
}

.flip-list-move {
  transition: transform 1s;
}

.animate__animated.animate__fadeInUp { --animate-duration: 0.2s; }
.animate__animated.animate__fadeOutUp { --animate-duration: 0.2s; }

.menu-container-container::-webkit-scrollbar {
  width: 0 !important;
}

.menu-container-container {
  overflow-y: scroll;
}
</style>
