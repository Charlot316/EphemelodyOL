<template>
  <div class="play-interface select" id="play-interface-container">
    <Prepare
      :loadingStatus="loadingStatus"
      :chart="chart"
      @startMusic="startMusic"
    />
    <Play
      :loadingStatus="loadingStatus"
      :chart="chart"
      :global="global"
      :imagePath="imagePath"
      :whiteLineLength="whiteLineLength"
      :score="score"
      @audioLoaded="audioLoaded"
      @imageLoaded="imageLoaded"
      @addCount="addCount"
      @pause="pause"
      @restart="reStart"
      @continuePlay="continuePlay"
    />
    <Result :loadingStatus="loadingStatus" :chart="chart" :global="global" />
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
        <el-button icon="el-icon-caret-left" @click="$router.go(-1)" circle></el-button>
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
</template>

<script>
import "animate.css";
import Play from "@/components/PlayInterface/Play";
import Prepare from "@/components/PlayInterface/Prepare";
import Result from "@/components/PlayInterface/Result";
export default {
  components: {
    Play,
    Prepare,
    Result,
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
      chartGot: false,
      loadingStatus: {
        chart: false,
        audio: false,
        image: false,
        canRun: false,
        runReady: false,
        runStart: false,
        beforeFinished: false,
        finished: false,
        imageCurrentCount: 0,
      },
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
        if (this.global.currentTime <= time + waitLoad) {
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
      keyUsed: [],
      keyMap: [],
      pureCount: 0,
      farCount: 0,
      lostCount: 0,
      combo: 0,
      maxCombo: 0,
      score: 0,
      notePainter: null,
      trackPainter: null,
      judgePainter: null,
      noteCanvas: null,
      trackCanvas: null,
      judgeCanvas: null,
      repaint: false,
    };
  },
  mounted() {
    const that = this;
    this.playInterface = document.getElementById("play-interface-container");
    this.global.noteCanvas = document.getElementById("note-canvas");
    this.global.trackCanvas = document.getElementById("track-canvas");
    this.global.judgeCanvas = document.getElementById("judge-canvas");
    this.global.notePainter = this.global.noteCanvas.getContext("2d");
    this.global.trackPainter = this.global.trackCanvas.getContext("2d");
    this.global.judgePainter = this.global.judgeCanvas.getContext("2d");
    this.resize();
    window.onresize = () => {
      return (() => {
        that.resize();
      })();
    };

    document.onkeydown = function(e) {
      if (!e.repeat) {
        that.global.keyPressTime[e.key.toUpperCase()] = that.global.currentTime;
        that.global.keyIsHold[e.key.toUpperCase()] = true;
        that.global.keyUsed[e.key.toUpperCase()] = false;
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
    document.addEventListener("gesturestart", function(e) {
      e.preventDefault();
    });
    this.playInterface.ontouchstart = function(e) {
      if (that.chart.tracks) {
        var currentTime = that.global.currentTime;
        var currentTracks = [];
        for (var i = 0; i < that.chart.tracks.length; i++) {
          var track = that.chart.tracks[i];
          if (
            currentTime > track.startTiming &&
            currentTime < track.endTiming &&
            track.type == 1
          ) {
            currentTracks.push(track);
          }
        }
        for (var j = 0; j < e.touches.length; j++) {
          for (var k = 0; k < currentTracks.length; k++) {
            track = currentTracks[k];
            var touch = e.touches[j];
            var left =
              (track.tempPositionX - track.tempWidth) * that.global.screenWidth;
            var right =
              (track.tempPositionX + track.tempWidth) * that.global.screenWidth;

            if (touch.clientX > left && touch.clientX < right) {
              var key = track.key.toUpperCase();
              that.global.keyPressTime[key] = currentTime;
              that.global.keyIsHold[key] = true;
              that.global.keyUsed[key] = false;
              that.global.keyMap[touch.identifier] = key;
            }
          }
        }
      }
      if (e.touches.length > 1) {
        e.preventDefault();
      }
    };
    var lastTouchEnd = 0;
    this.playInterface.ontouchend = function(e) {
      var now = new Date().getTime();
      if (now - lastTouchEnd <= 300) {
        e.preventDefault();
      }
      lastTouchEnd = now;
      if (that.chart.tracks) {
        for (var j = 0; j < e.changedTouches.length; j++) {
          var touch = e.changedTouches[j];
          that.global.keyIsHold[that.global.keyMap[touch.identifier]] = false;
        }
      }
    };
    this.initiate();
  },

  methods: {
    resize() {
      const that = this;
      this.playInterface = document.getElementById("play-interface-container");
      that.global.screenWidth = document.documentElement.clientWidth;
      that.global.screenHeight = document.documentElement.clientHeight;
      that.global.noteCanvas.height = that.playInterface.offsetHeight;
      that.global.trackCanvas.height = that.playInterface.offsetHeight;
      that.global.judgeCanvas.height = that.playInterface.offsetHeight;
      that.global.noteCanvas.width = that.playInterface.offsetWidth;
      that.global.trackCanvas.width = that.playInterface.offsetWidth;
      that.global.judgeCanvas.width = that.playInterface.offsetWidth;
    },

    //加载
    initiate() {
      this.getChart();
    },

    //获取谱面信息
    async getChart() {
      try {
        const { data: res } = await this.$http.get(
          `/user/getChart?songId=${this.$route.query.songId}`
        );
        if (res.code !== 0) {
          this.$notify({
            title: "失败",
            message: "登录失败！",
            type: "error",
          });
        }
        this.chart = res.data;
        this.loadingStatus.chart = true;
        this.sortTrack();
        this.generateImagePath();
      } catch (err) {
        return this.$notify({
          title: "错误",
          message: "网络异常",
          type: "error",
        });
      }
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
        return a.startTiming - b.startTiming;
      });
    },
    async finish() {
      try {
        const { data: res } = await this.$http.post("/play/uploadRecord", {
          score: this.global.score,
          songId: this.$route.query.songId,
          pure: this.global.pureCount,
          far: this.global.farCount,
          lost: this.global.lostCount,
          combo: this.global.maxCombo,
        });
        // console.log(JSON.stringify({
        //   score: this.global.score,
        //   songId: this.$route.query.songId,
        //   pure: this.global.pureCount,
        //   far: this.global.farCount,
        //   lost: this.global.lostCount,
        //   combo: this.global.maxCombo,
        // }))
        if (res.code !== 0) {
          this.$notify({
            title: "失败",
            message: "成绩上传失败！",
            type: "error",
          });
        }
        this.global.formerBestScore = res.data.formerBestScore;
        this.$store.commit("changeParam", {key:'potential',value:res.data.formerBestScore});
      } catch (err) {
        return this.$notify({
          title: "错误",
          message: "网络异常",
          type: "error",
        });
      }
    },
    //运行
    run() {
      if (
        document.documentElement.clientWidth != this.global.screenWidth ||
        document.documentElement.clientHeight != this.global.screenHeight
      ) {
        this.resize();
      }
      this.global.notePainter.clearRect(
        0,
        0,
        this.global.noteCanvas.width,
        this.global.noteCanvas.height
      );
      this.global.trackPainter.clearRect(
        0,
        0,
        this.global.trackCanvas.width,
        this.global.trackCanvas.height
      );
      this.global.judgePainter.clearRect(
        0,
        0,
        this.global.judgeCanvas.width,
        this.global.judgeCanvas.height
      );
      this.global.currentTime = Math.floor(this.audio.currentTime * 1000);
      if (this.global.currentTime < this.chart.songLength - 150) {
        requestAnimationFrame(this.run);
      } else {
        this.global.currentTime = this.chart.songLength;
        this.loadingStatus.beforeFinished = true;

        this.calculateScore();
        this.finish();
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
      this.chart.songLength = Math.round(1000 * this.audio.duration);
      this.generateImagePath();
      this.audio.muted = true;
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
          this.audio.volume = this.$store.state.volume / 100;
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
            url: operation.background,
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
      if (param.type == "lost") {
        this.global.combo = 0;
      } else {
        this.global.combo++;
        this.global.maxCombo = Math.max(
          this.global.maxCombo,
          this.global.combo
        );
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
      this.global.keyUsed = [];
      this.global.keyMap = [];
      this.global.pureCount = 0;
      this.global.farCount = 0;
      this.global.lostCount = 0;
      this.global.combo = 0;
      this.global.maxCombo = 0;
      this.global.score = 0;
      this.resetTrack();
      this.audio.currentTime = 0;
      this.audio.play();
    },
    resetTrack() {
      this.global.keyPressTime = [];
      this.global.keyIsHold = [];
      this.global.keyUsed = [];
      for (var i = 0; i < this.chart.tracks.length; i++) {
        var track = this.chart.tracks[i];
        var index = 0;
        var last = track.notes.length;
        for (var j = track.notes.length - 1; j >= 0; j--) {
          track.notes[j].judged = false;
          if (
            track.notes[j].timing + this.global.lostTime >
            this.global.currentTime
          ) {
            index = j;
          }
          if (
            this.global.currentTime <
            track.notes[j].timing - this.global.remainingTime
          ) {
            last = j;
          }
        }
        track.judges = [];
        track.currentNote = index;
        track.lastNote = last - 1;
        if (track.currentNote != track.notes.length) {
          track.judgeFinished = false;
        } else {
          track.judgeFinished = true;
        }
      }
    },
  },
};
</script>

<style scoped>
.play-interface {
  height: 100vh;
  width: 100vw;
  background: white;
  overflow: auto;
}
.select {
  -webkit-user-select: none;

  -moz-user-select: none;

  -ms-user-select: none;

  user-select: none;
}
</style>
