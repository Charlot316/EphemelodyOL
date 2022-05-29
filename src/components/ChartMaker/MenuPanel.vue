<template>
  <div class="menu-panel-container">
    <el-collapse>
      <el-collapse-item title=" 背景操作" name="1">
        <div
          style="width:100%;height:20px;display: flex;margin-bottom:20px;
          justify-content: space-between; align-items: center;"
        >
          <div>
            <el-button
              type="text"
              class="plus-button"
              icon="el-icon-circle-plus"
            >新增</el-button>
          </div>
          <div>
            <el-switch v-model="operationShowAll" active-text="显示全部" />
          </div>
        </div>
        <div
          v-for="operation in chart.changeBackgroundOperations"
          :key="operation"
        >
          <transition
            name="fade"
            enter-active-class="animate__animated animate__fadeInLeft"
            leave-active-class="animate__animated animate__fadeOutLeft"
          >
            <BackgroundOperation
              v-show="
                operationShowAll||global.currentTime > operation.startTime &&
                  global.currentTime < operation.endTime
              "
              :chart="chart"
              :operation="operation"
              :global="global"
          /></transition>
        </div>
      </el-collapse-item>

      <el-collapse-item title=" 当前轨道" name="2">
        <TrackCard
      /></el-collapse-item>

      <el-collapse-item title=" 全局插入音符" name="3"> </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
import BackgroundOperation from "./BackgroundOperation";
import TrackCard from "./TrackCard";
import "animate.css";
export default {
  props: ["chart", "global"],
  data() {
    return { operationShowAll: true, trackShowAll: true };
  },
  components: { BackgroundOperation, TrackCard },
};
</script>
<style>
.animate__animated.animate__fadeInLeft {
  --animate-duration: 0.5s;
}
.animate__animated.animate__fadeOutLeft {
  --animate-duration: 0.5s;
}
</style>
<style scope>
.menu-panel-container {
  height: calc(100vh - 80px);
  width: 90%;
  padding: 0% 5%;
  overflow: auto;
}
.plus-button {
  color: #67c23a;
}
.plus-button:hover {
  color: #95d475;
}
.plus-button:active {
  color: #529b2e;
}
</style>
