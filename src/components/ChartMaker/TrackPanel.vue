<template>
  <div class="menu-panel-container">
    <h4 style="padding:5px;margin-top:10px;margin-bottom:5x;">轨道列表</h4>
    <hr style="border:0.5px solid rgba(100,100,100,0.1)" />
    <div
      style="width:100%;height:20px;padding:5px;display: flex;
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
    <div v-if="chart.tracks">
      <transition-group
        name="flip-list"
        enter-active-class="animate__animated animate__fadeInUp"
        leave-active-class="animate__animated animate__fadeOutUp"
      >
        <div v-for="track in chart.tracks" :key="track">
          <transition
            name="flip-list"
            enter-active-class="animate__animated animate__fadeInUp"
            leave-active-class="animate__animated animate__fadeOutUp"
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
    </div>
  </div>
</template>

<script>
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
  components: { TrackCard },
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
    newTrack() {
      var track = {
        startTiming: 0,
        background: this.myChart.defaultBackground,
      };
      this.myChart.tracks.push(track);
      this.updateOperation();
      setTimeout(() => {
        this.myChart.tracks[0].edit = true;
      }, 10);
    },
  },
};
</script>
<style scope>
.menu-panel-container {
  height: calc(100vh - 350px);
  width: 90%;
  padding: 0% 5%;
  overflow: auto;
  -ms-overflow-style: none;
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
.animate__animated.animate__fadeInUp {
  --animate-duration: 0.2s;
}
.animate__animated.animate__fadeOutUp {
  --animate-duration: 0.2s;
}

.menu-panel-container::-webkit-scrollbar {
  width: 0 !important;
}
</style>
