<template>
  <div class="footer-container">
    <div class="footer-header"></div>
    <div v-if="chart.tracks">
      <div class="footer-left">
        <div
          class="footer-track-container"
          id="footer-left-scroll"
          @scroll="leftScroll"
        >
          <div v-for="track in chart.tracks" :key="track">
            <TrackCard
              :chart="chart"
              :track="track"
              :global="global"
              @currentTrack="currentTrack"
            />
          </div>
        </div>
      </div>
      <div class="footer-right" id="footer-right-scroll" @scroll="rightScroll">
        <div v-for="track in chart.tracks" :key="track">
          <TrackCardPanel
            :chart="chart"
            :track="track"
            :global="global"
            :scrollLeft="scrollLeft"
            @currentTrack="currentTrack"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import TrackCard from "./TrackCard";
import TrackCardPanel from "./TrackCardPanel";
export default {
  components: {
    TrackCard,
    TrackCardPanel,
  },
  props: ["chart", "global"],
  data() {
    return {
      myChart: this.chart,
      myGlobal: this.global,
      scrollLeft:0,
    };
  },
  watch: {
    chart() {
      this.myChart = this.chart;
    },
  },
  methods: {
    currentTrack(param) {
      this.$emit("currentTrack", param);
    },
    leftScroll() {
      document.getElementById(
        "footer-right-scroll"
      ).scrollTop = document.getElementById("footer-left-scroll").scrollTop;
    },
    rightScroll() {
      var element=document.getElementById("footer-right-scroll");
      document.getElementById("footer-left-scroll").scrollTop = element.scrollTop;
      this.scrollLeft=element.scrollLeft;
    },
  },
};
</script>

<style scope>
.footer-container {
  height: 100%;
  width: 100vw;
  position: relative;
}
.footer-header {
  height: 50px;
  width: 100vw;
  position: absolute;
  top: 0px;
  left: 0px;
}
.footer-left {
  height: calc(100% - 50px);
  width: 300px;
  position: absolute;
  top: 50px;
  left: 0px;
}
.footer-track-container {
  width: calc(90% - 1px);
  height: 100%;
  padding: 0% 5%;
  border: 0px solid rgba(255, 255, 255, 0.2);
  background: rgb(32, 32, 32);
  border-right-width: 1px;
  overflow: auto;
}
.footer-right {
  height: calc(100% - 55px);
  width: calc(100vw - 300px);
  background: rgb(32, 32, 32);
  position: absolute;
  top: 50px;
  left: 300px;
  padding-top: 5px;
  overflow: auto;
}

.footer-track-container::-webkit-scrollbar {
  width: 0 !important;
}
</style>
