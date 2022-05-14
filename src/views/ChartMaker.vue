<template>
  <div class="play-interface">
    <div class="header">
      <div class="header-button"></div>
      <div class="header-slide">
        <div class="header-slide-item">
          <el-slider
            v-model="global.currentTime"
            :min="0"
            :max="chart.songLength"
            @change="changeTime"
            @mousedown="SlideMouseDown"
            @mouseup="SlideMouseUp"
          ></el-slider>
        </div>
        <div class="header-slide-button">
          <el-button icon="el-icon-refresh-left" circle @click="reStart"></el-button>
          <el-button v-if="isRunning" icon="el-icon-video-pause" circle @click="pause"></el-button>
          <el-button v-else icon="el-icon-video-play" circle @click="play"></el-button>
        </div>
      </div>
    </div>
    <div class="select">
      <div id="play-interface-container">
        <!-- 音频 -->
        <audio
          id="audioSong"
          preload="auto"
          controls
          autoplay
          :src="chart.songUrl"
          style="display:none"
        />
        <!-- 背景 -->
        <div v-for="image in imagePath" :key="image">
          <img
            :src="image.url"
            v-show="
              global.currentTime >= image.startTime &&
                global.currentTime <= image.endTime
            "
            style="position:absolute;left:0;top:0;width:100%;height:100%;object-fit:fill;user-drag:none;"
          />
        </div>
        <!-- 判定线 -->
        <div
          :style="{
            height: '2px',
            position: 'absolute',
            left: '0px',
            top: global.screenHeight * global.finalY - 1 + 'px',
            width: global.screenWidth + 'px',
          }"
        >
          <div
            class="white-line"
            :style="{
              height: '100%',
              position: 'absolute',
              left: (global.screenWidth - whiteLineLength) / 2 + 'px',
              width: whiteLineLength + 'px',
              background: 'rgba(255,255,255,1)',
            }"
          ></div>
        </div>

        <!-- 轨道 -->
        <div
          class="play-interface-track-container"
          v-for="Track in chart.tracks"
          :key="Track"
        >
          <Track
            :Track="Track"
            :Global="global"
            v-if="
              global.currentTime > Track.startTiming &&
                global.currentTime < Track.endTiming
            "
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Track from "@/components/PlayInterface/Track";
import "animate.css";
import { chart } from "@/utils/chart.js";
export default {
  components: {
    Track,
  },
  data() {
    return {
      chart: {
        songLength: 0,
      },
      global: {},
      imagePath: [],
      pauseVisible: false,
      audio: null,
      playInterface: null,
      isRunning: false,
      sliding: false,
    };
  },
  computed: {
    whiteLineLength() {
      const time = 200;
      const waitLoad = 1000;
      if (
        this.global.currentTime > time + waitLoad &&
        this.global.currentTime < this.chart.songLength - time
      ) {
        return this.global.screenWidth;
      } else {
        if (this.global.currentTime < time + waitLoad) {
          if (this.global.currentTime < waitLoad) {
            return 0;
          } else
            return (
              ((this.global.currentTime - waitLoad) * this.global.screenWidth) /
              time
            );
        } else {
          return (
            ((this.chart.songLength - this.global.currentTime) *
              this.global.screenWidth) /
            time
          );
        }
      }
    },
  },
  created() {
    this.loadingStatus = {
      chart: false,
      audio: false,
      image: false,
      canRun: false,
    };
    this.global = {
      screenWidth: 0,
      screenHeight: 0,
      remainingTime: 1000,
      finalY: 0.8,
      currentTime: 0,
      lostTime: 150,
      pureTime: 50,
      farTime: 100,
      isEdit: true,
      keyPressTime: [],
      keyIsHold: [],
      keyUsed: [],
      pureCount: 0,
      farCount: 0,
      lostCount: 0,
      combo: 0,
      maxCombo: 0,
      score: 0,
    };
  },
  mounted() {
    const that = this;
    this.playInterface = document.getElementById("play-interface-container");
    this.global.screenWidth = this.playInterface.offsetWidth;
    this.global.screenHeight = this.playInterface.offsetHeight;
    window.onresize = () => {
      return (() => {
        that.global.screenWidth = that.playInterface.offsetWidth;
        that.global.screenHeight = that.playInterface.offsetHeight;
      })();
    };
    document.onkeydown = function(e) {
      if (!e.repeat) {
        that.global.keyPressTime[e.key.toUpperCase()] = that.global.currentTime;
        that.global.keyIsHold[e.key.toUpperCase()] = true;
        that.global.keyUsed[e.key.toUpperCase()] = false;
      }
    };
    document.onkeyup = function(e) {
      that.global.keyIsHold[e.key.toUpperCase()] = false;
    };
    this.initiate();
  },

  methods: {
    //获取谱面信息
    getChart() {
      this.chart = chart;
    },
    //给轨道排序
    sortTrack() {
      this.chart.tracks.sort(function(a, b) {
        return b.startTiming - a.startTiming;
      });
    },
    //运行
    run() {
      if (!this.sliding) {
        this.global.currentTime = Math.floor(this.audio.currentTime * 1000);
      }
      if (this.global.currentTime >= this.chart.songLength) {
        this.isRunning = false;
      }
      setTimeout(() => {
        this.run();
      }, 1000 / this.$store.state.refreshRate);
    },

    //打印控制台信息
    log(message) {
      console.log(message);
    },

    //加载
    initiate() {
      this.getChart();
      this.loadingStatus.chart = true;
      this.sortTrack();
      this.generateImagePath();
      this.audio = document.getElementById("audioSong");
      this.run();
    },

    //生成图片路径
    generateImagePath() {
      this.imagePath = [];
      let length = this.chart.changeBackgroundOperations.length;
      let start = 0;
      let end = start;
      let defaultBackground = this.chart.defaultBackground;
      if (length == 0) {
        end = this.chart.songLength;
      } else {
        this.chart.changeBackgroundOperations.sort(function(a, b) {
          return a.startTime - b.startTime;
        });
        end = this.chart.changeBackgroundOperations[0].startTime;
      }
      this.imagePath.push({
        url: defaultBackground,
        startTime: 0,
        endTime: end,
      });
      for (let i = 0; i < length; i++) {
        let operation = this.chart.changeBackgroundOperations[i];
        start = operation.startTime;
        if (i != length - 1) {
          let nextOperation = this.chart.changeBackgroundOperations[i + 1];
          this.imagePath.push({
            url: operation.background,
            startTime: start,
            endTime: nextOperation.startTime,
          });
        } else {
          this.imagePath.push({
            url: defaultBackground,
            startTime: start,
            endTime: this.chart.songLength + 1000,
          });
        }
      }
    },

    //变换slide的时间
    changeTime() {
      this.audio.currentTime = this.global.currentTime / 1000;
      this.resetTrack();
    },

    SlideMouseDown() {
      this.audio.pause();
      this.sliding = true;
      this.resetTrack();
    },

    SlideMouseUp() {
      this.sliding = false;
      this.audio.currentTime = this.global.currentTime / 1000;
      this.resetTrack();
      if (this.isRunning) {
        this.sliding = false;
        this.audio.play();
      }
    },

    pause() {
      this.audio.pause();
      this.isRunning = false;
    },

    play() {
      this.audio.play();
      this.resetTrack();
      this.sliding = false;
      this.isRunning = true;
    },

    reStart() {
      this.resetTrack();
      this.audio.currentTime = 0;
      this.global.currentTime=0;
      if(this.isRunning)
      {
        this.audio.play();
      }
    },

    resetTrack() {
      for (var i = 0; i < this.chart.tracks.length; i++) {
        var track = this.chart.tracks[i];
        var index = 0;
        for (var j = track.notes.length - 1; j >= 0; j--) {
          track.notes[j].judged = false;
          if (track.notes[j].timing > this.global.currentTime) {
            index = j;
          }
        }
        track.currentNote = index;
      }
    },
  },
};
</script>

<style scoped>
.play-interface {
  height: 100%;
  width: 100%;
  background: white;
  overflow: auto;
}
.select {
  -webkit-user-select: none;

  -moz-user-select: none;

  -ms-user-select: none;

  user-select: none;
}

.header {
  position: absolute;
  top: 0;
  height: 90px;
  width: 100%;
  padding: 5px 0;
  background: white;
}
.header-slide {
  width: 100%;
}

#play-interface-container {
  position: absolute;
  top: 100px;
  height: calc(100vh - 100px);
  width: 100%;
}
.header-slide-item {
  width: 76%;
  padding: 0px 2%;
  float: left;
}
.header-slide-button {
  width: 18%;
  padding: 0px 1%;
  float: left;
  text-align: center;
}
</style>
