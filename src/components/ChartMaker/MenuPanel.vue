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
              @click="newOperation"
              >新增</el-button
            >
          </div>
          <div>
            <el-switch v-model="operationShowAll" active-text="显示全部" />
          </div>
        </div>
        <transition-group
          name="flip-list"
          enter-active-class="animate__animated animate__fadeInLeft"
          leave-active-class="animate__animated animate__fadeOutLeft"
        >
          <div
            v-for="operation in chart.changeBackgroundOperations"
            :key="operation"
          >
            <transition
              name="flip-list"
              enter-active-class="animate__animated animate__fadeInLeft"
              leave-active-class="animate__animated animate__fadeOutLeft"
            >
              <BackgroundOperation
                v-show="
                  operationShowAll ||
                    (global.currentTime > operation.startTime &&
                      global.currentTime < operation.endTime)
                "
                :chart="chart"
                :operation="operation"
                :global="global"
              />
            </transition>
          </div>
        </transition-group>
      </el-collapse-item>

      <el-collapse-item title=" 当前轨道" name="2">
         <div
          style="width:100%;height:20px;display: flex;margin-bottom:20px;
          justify-content: space-between; align-items: center;"
        >
          <div>
            <el-button
              type="text"
              class="plus-button"
              icon="el-icon-circle-plus"
              @click="newTrack"
              >新增</el-button
            >
          </div>
          <div>
            <el-switch v-model="trackShowAll" active-text="显示全部" />
          </div>
        </div>
        <transition-group
          name="flip-list"
          enter-active-class="animate__animated animate__fadeInLeft"
          leave-active-class="animate__animated animate__fadeOutLeft"
        >
          <div
            v-for="track in chart.tracks"
            :key="track"
          >
            <transition
              name="flip-list"
              enter-active-class="animate__animated animate__fadeInLeft"
              leave-active-class="animate__animated animate__fadeOutLeft"
            >
              <TrackCard
                v-show="
                  trackShowAll ||
                    (global.currentTime > track.startTiming &&
                      global.currentTime < track.endTiming)
                "
                :chart="chart"
                :track="track"
                :global="global"
              />
            </transition>
          </div>
        </transition-group>
      </el-collapse-item>

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
    return {
      operationShowAll: true,
      trackShowAll: true,
      myChart: this.chart,
      myGlobal: this.global,
    };
  },
  components: { BackgroundOperation, TrackCard },
  watch: {
    chart() {
      this.myChart = this.chart;
    },
  },
  methods: {
    updateOperation() {
      this.myGlobal.reCalculateChartMaker = !this.myGlobal
        .reCalculateChartMaker;
    },
    newOperation() {
      var operation = {
        startTime: 0,
        background: this.myChart.defaultBackground,
      };
      this.myChart.changeBackgroundOperations.push(operation);
      this.updateOperation();
      setTimeout(() => {
        this.myChart.changeBackgroundOperations[0].edit = true;
      }, 10);
    },
     newTrack() {
      var operation = {
        startTime: 0,
        background: this.myChart.defaultBackground,
      };
      this.myChart.changeBackgroundOperations.push(operation);
      this.updateOperation();
      setTimeout(() => {
        this.myChart.changeBackgroundOperations[0].edit = true;
      }, 10);
    },
  },
};
</script>
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
.flip-list-move {
  transition: transform 1s;
}

.animate__animated.animate__fadeInLeft {
  --animate-duration: 0.2s;
}
.animate__animated.animate__fadeOutLeft {
  --animate-duration: 0.2s;
}
</style>
