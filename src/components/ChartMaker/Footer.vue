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
      <div
        class="footer-right"
        id="footer-right-scroll"
        @scroll="rightScroll"
        @click="rightClick($event)"
        @mousemove="rightMouseMove($event)"
      >
        <div v-for="track in chart.tracks" :key="track">
          <TrackCardPanel
            :id="'trackCardPanel' + track.index"
            :chart="chart"
            :track="track"
            :global="global"
            :scrollLeft="scrollLeft"
            :displayAreaTime="displayAreaTime"
            @currentTrack="currentTrack"
          />
        </div>
        <div
          class="time-indicater"
          id="time-indicater"
          :style="{
            width: '1px',
            background: 'rgb(255,255,0)',
            height: '100%',
            position: 'absolute',
            pointerEvents: 'none',
            top: scrollTop + 'px',
            left:
              (global.currentTime / displayAreaTime) *
                (global.documentWidth - 300) +
              'px',
          }"
        ></div>
        <div
          class="time-indicater-false"
          id="time-indicater-false"
          :style="{
            width: '1px',
            background: 'rgb(255,255,255)',
            height: '100%',
            position: 'absolute',
            pointerEvents: 'none',
            top: scrollTop + 'px',
            left: indicatorLeft + 'px',
          }"
        ></div>
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
      scrollLeft: 0,
      scrollTop: 0,
      displayAreaTime: 1000,
      audio: null,
      indicatorLeft: 0,
      rightScrollElement: null,
    };
  },
  mounted() {
    this.audio = document.getElementById("audioSong");
  },
  watch: {
    "global.currentTime"() {
      if (this.rightScrollElement == null) {
        this.rightScrollElement = document.getElementById(
          "footer-right-scroll"
        );
      }
      if (
        this.global.currentTime <=
        this.chart.songLength - this.displayAreaTime / 2
      ) {
        var scrollLeft =
          (this.global.currentTime / this.displayAreaTime) *
            (this.global.documentWidth - 300) -
          (this.global.documentWidth - 300) / 2;
        if (scrollLeft < 0) scrollLeft = 0;
        if (!this.audio.paused) {
          this.rightScrollElement.scrollLeft = scrollLeft;
          this.scrollLeft = scrollLeft;
        }

        // }
      }

      for (var i = 0; i < this.chart.tracks.length; i++) {
        if (
          this.global.currentTime > this.chart.tracks[i].startTiming &&
          this.global.currentTime < this.chart.tracks[i].endTiming
        ) {
          setTimeout(() => {
            document
              .querySelector("#trackCardPanel" + this.chart.tracks[i].index)
              .scrollIntoView(true);
          }, 200);

          break;
        }
      }
    },
    chart() {
      this.myChart = this.chart;
    },
  },
  methods: {
    rightClick(e) {
      let x = e.clientX - 300 + this.scrollLeft;
      var currentTime =
        (x / (this.global.documentWidth - 300)) * this.displayAreaTime;
      this.audio.currentTime = currentTime / 1000;
      this.myGlobal.currentTime = currentTime;
    },
    rightMouseMove(e) {
      let x = e.clientX - 300 + this.scrollLeft;
      this.indicatorLeft = x;
    },
    currentTrack(param) {
      this.$emit("currentTrack", param);
    },
    leftScroll() {
      if (this.rightScrollElement == null) {
        this.rightScrollElement = document.getElementById(
          "footer-right-scroll"
        );
      }
      this.rightScrollElement.scrollTop = document.getElementById(
        "footer-left-scroll"
      ).scrollTop;
    },
    rightScroll() {
      if (this.rightScrollElement == null) {
        this.rightScrollElement = document.getElementById(
          "footer-right-scroll"
        );
      }
      document.getElementById(
        "footer-left-scroll"
      ).scrollTop = this.rightScrollElement.scrollTop;
      this.scrollLeft = this.rightScrollElement.scrollLeft;
      this.scrollTop = this.rightScrollElement.scrollTop;
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
