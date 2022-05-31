<template>
  <div class="track-panel-container">
    <h4
      style="padding:5px;margin-top:10px;margin-bottom:5x;color:rgb(225,225,225);margin-left:10px;"
    >
      轨道列表
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
          icon="el-icon-circle-plus"
          @click="newTrack"
          >新增</el-button
        >
      </div>
      <div>
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="autoScroll = !autoScroll"
          >{{ autoScroll ? "关闭滚动" : "开启滚动" }}</el-button
        >
        <el-button
          type="text"
          class="show-button"
          style="margin-right:13px;"
          @click="trackShowAll = !trackShowAll"
          >{{ trackShowAll ? "显示当前" : "显示全部" }}</el-button
        >
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
              :id="'trackCard' + track.index"
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
      trackShowAll: true,
      autoScroll: true,
      myChart: this.chart,
      myGlobal: this.global,
    };
  },
  components: { TrackCard },
  watch: {
    "global.currentTime"() {
      if (this.autoScroll) {
        for (var i = this.chart.tracks.length - 1; i >= 0; i--) {
          if (
            this.global.currentTime > this.chart.tracks[i].startTiming &&
            this.global.currentTime < this.chart.tracks[i].endTiming
          ) {
            if (i > 2)
              setTimeout(() => {
                document
                  .querySelector("#trackCard" + this.chart.tracks[i - 2].index)
                  .scrollIntoView(true);
              }, 200);
            else
              setTimeout(() => {
                document
                  .querySelector("#trackCard" + this.chart.tracks[0].index)
                  .scrollIntoView(true);
              }, 200);
            break;
          }
        }
      }
    },
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
        endTiming:150,
        type:1,
        key:'D',
        R:'160',
        G:'160',
        B:'160',
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
.track-panel-container {
  width: calc(90% - 1px);
  padding: 0% 5%;
  border: 0px solid rgba(255, 255, 255, 0.2);
  background: rgb(32, 32, 32);
  border-left-width: 1px;
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

.track-panel-container::-webkit-scrollbar {
  width: 0 !important;
}
</style>
