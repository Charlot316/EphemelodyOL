<template>
  <div class="menu-panel-container">
    <el-collapse>
      <el-collapse-item title=" 背景操作" name="1">
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
                global.currentTime > operation.startTime &&
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
</style>
