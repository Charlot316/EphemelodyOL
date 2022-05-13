<template>
  <div class="play-interface select" id="play-interface-container">
    <div v-if="!loadingStatus.runStart" class="show-info">
      <img :src="chart.defaultBackground" class="loading-background" />
      <div
        :class="
          loadingStatus.runReady ? 'info-container-out' : 'info-container'
        "
      >
        <div
          style="display: flex;justify-content: space-between;align-items: center;"
        >
          <div class="songcover-container">
            <img class="songcover-img" :src="chart.songCover" />
          </div>
          <div class="info" style="padding:20px;">
            <div
              class="song-uploader"
              style="text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:30px;color:rgb(255,250,235);"
            >
              {{ chart.uploader }}
            </div>
            <div
              class="song-name"
              style="text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:70px;color:rgb(255,255,255);background-color:rgba(54, 144, 240, 0.5);"
            >
              {{ chart.songName }}
            </div>
            <div
              class="song-writer"
              style="text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:50px;color:rgb(255,255,255);background-color:rgba(255, 255, 255, 0.5);"
            >
              {{ chart.songWriter }}
            </div>
          </div>
        </div>
        <div
          class="loading-container"
          style="text-align: center;margin-right:50px;"
        >
          <div
            :class="
              loadingStatus.canRun ? 'play-button' : 'play-button-disabled'
            "
            style="width:150px;height:150px;line-height:150px;margin:20px auto;border-radius:50%;"
            @click="startMusic"
          >
            {{ loadingStatus.canRun ? "开始" : "加载中" }}
          </div>
          <div
            class="loading-text"
            v-if="!loadingStatus.canRun"
            style="padding:10px;min-width:400px;text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:20px;color:rgb(255,255,255);"
          >
            {{ chart.loadingText }}
          </div>
          <div
            v-if="loadingStatus.canRun"
            style="padding:10px;min-width:400px;text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:20px;color:rgb(255,255,255);"
          >
            {{ chart.loadedText }}
          </div>
          <div
            class="loaded-text"
            v-if="loadingStatus.canRun"
            style="height:2px;min-width:400px;text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:20px;color:rgb(255,255,255);"
          ></div>
        </div>
      </div>
    </div>
    <div
      v-show="loadingStatus.runStart && !loadingStatus.finished"
      class="play-interface"
    >
      <!-- 音频 -->
      <audio
        id="audioSong"
        preload="auto"
        controls
        autoplay
        :src="chart.songUrl"
        style="display:none"
        @canplaythrough="audioLoaded"
      />
      <!-- 背景 -->
      <div v-for="image in imagePath" :key="image">
        <img
          :src="image.url"
          v-show="
            global.currentTime >= image.startTime &&
              global.currentTime <= image.endTime
          "
          @load="imageLoaded"
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
          @addCount="addCount"
          v-if="
            global.currentTime > Track.startTiming &&
              global.currentTime < Track.endTiming
          "
        />
      </div>
      <!-- 记分板 -->
      <div
        :class="
          loadingStatus.beforeFinished
            ? 'play-interface-scoreboard-container-upward'
            : 'play-interface-scoreboard-container-downward'
        "
        :style="{
          height: '200px',
          position: 'absolute',
          left: '0px',
          width: global.screenWidth + 'px',
          background: [
            '-webkit-linear-gradient(90deg, rgba(0,0,0,0) 0, rgba(0,0,0,1) 100%)',
            '-moz-linear-gradient(0deg, rgba(0,0,0,0) 0, rgba(0,0,0,1) 100%)',
            'linear-gradient(0deg, rgba(0,0,0,0) 0, rgba(0,0,0,1) 100%)',
          ],
        }"
      >
        <div
          class="score-counter"
          style="text-align:center;
          position:absolute;
          left:0px;
          width: 50px;
          margin: 0 auto;
          text-shadow: 1px 1px 0 rgba(0,0,0,0.25);
          font-size:30px;
          color:rgb(255,255,255);
          cursor: pointer;"
          @click="pause"
        >
          ||
        </div>
        <div
          class="score-counter"
          style="text-align:center;
          position:absolute;
          right:0px;
          width: 200px;
          margin: 0 auto;
          text-shadow: 1px 1px 0 rgba(0,0,0,0.25);
          font-size:40px;
          color:rgb(255,255,255)"
        >
          {{ score }}
        </div>
        <div class="combo-counter" v-if="global.combo > 1">
          <div
            style="text-align:center;
          width: 200px;
          margin: 0 auto;
          text-shadow: 1px 1px 0 rgba(0,0,0,0.25);
          font-size:70px;
          color:rgb(255,255,255)"
          >
            {{ global.combo }}
          </div>
          <div
            style="text-align:center;
          width: 200px;
          margin: 0 auto;
          text-shadow: 1px 1px 0 rgba(0,0,0,0.25);
          font-size:20px;
          color:rgb(255,255,255)"
          >
            <span
              :style="{
                color: [
                  global.combo == global.pureCount && global.lostCount == 0
                    ? 'rgb(247, 199, 9)'
                    : global.lostCount == 0
                    ? 'rgb(135, 206, 250)'
                    : 'rgb(255, 255, 255)',
                ],
              }"
              >{{ global.lostCount == 0 ? "⬥" : "⬦" }}</span
            >
            COMBO
            <span
              :style="{
                color: [
                  global.combo == global.pureCount && global.lostCount == 0
                    ? 'rgb(247, 199, 9)'
                    : global.lostCount == 0
                    ? 'rgb(135, 206, 250)'
                    : 'rgb(255, 255, 255)',
                ],
              }"
              >{{ global.lostCount == 0 ? "⬥" : "⬦" }}</span
            >
          </div>
        </div>
      </div>
      <el-dialog
        v-model="pauseVisible"
        title="暂停"
        top="30vh"
        :center="true"
        :show-close="false"
        :close-on-press-escape="false"
        :close-on-click-modal="false"
      >
        <div style="text-align: center;">
          <el-button icon="el-icon-caret-left" circle></el-button>
          <el-button
            icon="el-icon-refresh-left"
            circle
            @click="reStart"
          ></el-button>
          <el-button
            icon="el-icon-caret-right"
            circle
            @click="continuePlay"
          ></el-button>
        </div>
      </el-dialog>
    </div>
    <div v-if="loadingStatus.finished" class="show-record">
      <img :src="chart.defaultBackground" class="loading-background" />
      <div class="record-container">


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
    score() {
      if (this.global.score)
        return this.global.score.toString().padStart(8, "0");
      else return "00000000";
    },
  },
  watch: {
    "global.pureCount"() {
      this.calculateScore();
    },
    "global.farCount"() {
      this.calculateScore();
    },
  },
  created() {
    this.loadingStatus = {
      chart: false,
      audio: false,
      image: false,
      canRun: false,
      runReady: false,
      runStart: false,
      beforeFinished: false,
      finished: false,
      imageCurrentCount: 0,
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
      isEdit: false,
      keyPressTime: [],
      keyIsHold: [],
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
      }
      if (e.key == "Escape") {
        that.pause();
      }
      if (e.key == "Enter") {
        that.continuePlay();
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

    calculateScore() {
      let singleScore = 10000000 / this.chart.notesCount;
      this.global.score = Math.floor(
        this.global.pureCount * singleScore +
          this.global.farCount * 0.5 * singleScore
      );
      if (this.global.score > 10000000) this.global.score = 10000000;
    },
    //给轨道排序
    sortTrack() {
      this.chart.tracks.sort(function(a, b) {
        return b.startTiming - a.startTiming;
      });
    },

    //运行
    run() {
      this.global.currentTime = Math.floor(this.audio.currentTime * 1000);
      if (this.global.currentTime < this.chart.songLength) {
        setTimeout(() => {
          this.run();
        }, 1000 / this.$store.state.refreshRate);
      } else {
        this.global.currentTime = this.chart.songLength;
        this.loadingStatus.beforeFinished = true;
        this.$forceUpdate();
        setTimeout(() => {
          this.loadingStatus.finished = true;
          this.$forceUpdate();
        }, 2000);
      }
    },

    //audio加载完毕
    audioLoaded() {
      this.audio = document.getElementById("audioSong");
      this.loadingStatus.audio = true;
      console.log("audio loaded");
      this.checkIfLoaded();
    },

    //图片加载完毕
    imageLoaded() {
      this.loadingStatus.imageCurrentCount++;
      if (this.loadingStatus.imageCurrentCount == this.imagePath.length) {
        this.loadingStatus.image = true;
        console.log("image loaded");
        this.checkIfLoaded();
      }
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
    },

    //开始播放音乐
    startMusic() {
      if (this.loadingStatus.canRun) {
        this.audio.play();
        this.audio.muted = true;

        this.loadingStatus.runReady = true;
        this.$forceUpdate();
        setTimeout(() => {
          this.loadingStatus.runStart = true;
          this.$forceUpdate();
          this.audio.currentTime = 0;
          this.audio.muted = false;
          this.run();
        }, 500);
      }
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

    //父组件提供的方法
    addCount(param) {
      this.global[param.key] += 1;
      console.log(
        param.message +
          ", 点击时机:" +
          param.judgeTime +
          ", 音符时机:" +
          param.timing
      );
      if (param.key == "lostCount") {
        this.global.combo = 0;
        this.global.maxCombo = Math.max(
          this.global.maxCombo,
          this.global.combo
        );
      } else {
        this.global.combo++;
      }
    },

    //当所有都加载完毕的时候开始运行
    checkIfLoaded() {
      if (
        this.loadingStatus.chart &&
        this.loadingStatus.audio &&
        this.loadingStatus.image
      ) {
        setTimeout(() => {
          this.loadingStatus.canRun = true;
          this.$forceUpdate();
        }, 1000);
      }
    },
    //暂停
    pause() {
      if (this.global.currentTime < this.chart.songLength) {
        this.audio.pause();
        this.pauseVisible = true;
      }
    },
    //继续
    continuePlay() {
      this.pauseVisible = false;
      this.audio.play();
    },
    //重新开始
    reStart() {
      this.pauseVisible = false;
      this.global.keyPressTime = [];
      this.global.keyIsHold = [];
      this.global.pureCount = 0;
      this.global.farCount = 0;
      this.global.lostCount = 0;
      this.global.combo = 0;
      this.global.maxCombo = 0;
      this.global.score = 0;
      for (var i = 0; i < this.chart.tracks.length; i++) {
        var track = this.chart.tracks[i];
        for (var j = 0; j < track.notes.length; j++) {
          track.notes[j].judged = false;
        }
      }
      this.audio.currentTime = 0;
      this.audio.play();
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

@keyframes backgroung-image {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes linearGradientMove {
  100% {
    background-position: 4px 0, -4px 100%, 0 -4px, 100% 4px;
  }
}

.loading-text {
  background: linear-gradient(90deg, rgb(255, 255, 255) 50%, transparent 0)
      repeat-x,
    linear-gradient(90deg, rgb(255, 255, 255) 50%, transparent 0) repeat-x,
    linear-gradient(0deg, rgb(255, 255, 255) 50%, transparent 0) repeat-y,
    linear-gradient(0deg, rgb(255, 255, 255) 50%, transparent 0) repeat-y;
  background-size: 4px 1px, 4px 1px, 1px 4px, 1px 4px;
  background-position: 0 0, 0 100%, 0 0, 100% 0;
  animation: linearGradientMove 0.3s infinite linear;
}

@keyframes loadedText {
  0% {
    background-size: 0% 2px;
  }
  100% {
    background-size: 100% 2px;
  }
}
.loaded-text {
  background-image: linear-gradient(rgb(255, 255, 255), rgb(255, 255, 255));
  background-position: center bottom;
  background-repeat: no-repeat;
  animation-duration: 1s;
  animation-name: loadedText;
}

@keyframes playbutton {
  0% {
    box-shadow: 0px 0px 0px 0px rgba(54, 144, 240, 0.8);
  }
  100% {
    box-shadow: 0px 0px 0px 40px rgba(169, 213, 252, 0.1);
  }
}
.play-button {
  animation-name: playbutton;
  animation-duration: 2s;
  animation-iteration-count: infinite;
  transform: scale(1);
  cursor: pointer;
  font-size: 30px;
  color: rgb(255, 255, 255);
  background-image: -webkit-linear-gradient(
    -240.9453959009229deg,
    rgba(169, 213, 252, 1) 0,
    rgba(169, 213, 252, 1) 6%,
    rgba(84, 163, 238, 1) 53%,
    rgba(54, 144, 240, 1) 100%
  );
  background-image: -moz-linear-gradient(
    330.9453959009229deg,
    rgba(169, 213, 252, 1) 0,
    rgba(169, 213, 252, 1) 6%,
    rgba(84, 163, 238, 1) 53%,
    rgba(54, 144, 240, 1) 100%
  );
  background-image: linear-gradient(
    330.9453959009229deg,
    rgba(169, 213, 252, 1) 0,
    rgba(169, 213, 252, 1) 6%,
    rgba(84, 163, 238, 1) 53%,
    rgba(54, 144, 240, 1) 100%
  );
  transform: scale(1);
  transition: 0.5s;
}

.play-button-disabled {
  transform: scale(1);
  cursor: wait;
  font-size: 30px;
  color: rgb(255, 255, 255);
  background-image: -webkit-linear-gradient(
    -240.9453959009229deg,
    rgb(82, 105, 125) 0,
    rgb(65, 82, 97) 6%,
    rgb(27, 55, 80) 53%,
    rgb(17, 48, 81) 100%
  );
  background-image: -moz-linear-gradient(
    330.9453959009229deg,
    rgb(82, 105, 125) 0,
    rgb(65, 82, 97) 6%,
    rgb(27, 55, 80) 53%,
    rgb(17, 48, 81) 100%
  );
  background-image: linear-gradient(
    330.9453959009229deg,
    rgb(82, 105, 125) 0,
    rgb(65, 82, 97) 6%,
    rgb(27, 55, 80) 53%,
    rgb(17, 48, 81) 100%
  );
  transform: scale(1);
  transition: 0.5s;
}
.play-button:hover {
  transform: scale(0.9);
  transition: 0.5s;
}

.play-button:active {
  transform: scale(0.85);
  transition: 0.1s;
}

.loading-background {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  object-fit: fill;
  -webkit-user-drag: none;
  animation-name: backgroung-image;
  animation-duration: 10s;
  animation-iteration-count: infinite;
}

@keyframes info-container-upward {
  0% {
    bottom: -400px;
  }
  100% {
    bottom: 0px;
  }
}

.info-container {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 300px;
  padding: 40px;
  object-fit: fill;
  -webkit-user-drag: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
  animation: info-container-upward 0.5s ease-out;
}

@keyframes info-container-downward {
  0% {
    bottom: 0px;
  }
  100% {
    bottom: -400px;
  }
}
.info-container-out {
  animation: info-container-downward 0.5s ease-out;
  position: absolute;
  left: 0;
  bottom: -400px;
  width: 100%;
  height: 300px;
  padding: 40px;
  object-fit: fill;
  -webkit-user-drag: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.songcover-container {
  width: 300px;
  height: 300px;
  margin: 0;
}

.songcover-img {
  width: 100%;
  height: 100%;
  object-fit: fill;
  -webkit-user-drag: none;
  border: 2px solid rgb(255, 255, 255);
}

@keyframes scoreboard-container-downward {
  0% {
    top: -200px;
  }
  100% {
    top: 0px;
  }
}

@keyframes scoreboard-container-upward {
  0% {
    top: 0px;
  }
  100% {
    top: -200px;
  }
}
.play-interface-scoreboard-container-downward {
  top: 0;
  animation: scoreboard-container-downward 0.5s ease-out;
}

.play-interface-scoreboard-container-upward {
  top: -200px;
  animation: scoreboard-container-upward 0.5s ease-out;
}
</style>
