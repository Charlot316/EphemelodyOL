<template>
  <div class="play-interface">
    <div class="header">
      <div class="header-buttons">
        <div>
          <el-button
            v-if="!menuOpened"
            icon="el-icon-s-unfold"
            size="small"
            class="header-button"
            @click="changeMenuDisplay"
            type="text"
            >打开菜单</el-button
          >
          <el-button
            v-else
            icon="el-icon-s-fold"
            size="small"
            class="header-button"
            @click="changeMenuDisplay"
            type="text"
            >关上菜单</el-button
          >
          <el-button
            icon="el-icon-refresh-left"
            size="small"
            @click="reStart"
            class="header-button"
            type="text"
            >重播</el-button
          >
          <el-button
            v-if="isRunning"
            size="small"
            icon="el-icon-video-pause"
            class="header-button"
            @click="pause"
            type="text"
            >暂停</el-button
          >
          <el-button
            v-else
            icon="el-icon-video-play"
            size="small"
            class="header-button"
            @click="play"
            type="text"
            >播放</el-button
          >
          <el-button
            icon="el-icon-s-operation"
            size="small"
            type="text"
            class="header-button"
            @click="
              globalSetting = true;
              log(globalSetting);
            "
            >全局设置</el-button
          >
          <el-dialog
            v-model="globalSetting"
            @closed="globalSetting = false"
            width="650px"
          >
            <el-form :model="form" label-width="120px" style="padding: 20px;">
              <el-form-item label="音量">
                <el-input-number
                  v-model="volume"
                  :min="0"
                  :max="100"
                  @change="changeVolume"
                />
                <el-tooltip
                  class="item"
                  effect="dark"
                  content="按键盘上下键同样可以调节音量"
                  placement="top-start"
                  style="margin-left:10px;"
                >
                  <i class="el-icon-question" />
                </el-tooltip>
              </el-form-item>
              <el-form-item label="快进最小间隔">
                <el-input-number
                  v-model="keyStep"
                  :min="1"
                  :max="chart.songLength"
                />
                <el-tooltip
                  class="item"
                  effect="dark"
                  content="按键盘左右键可以调整时间轴，本项可以调整按一次左键或者右键快进的时间多少"
                  placement="top-start"
                  style="margin-left:10px;"
                >
                  <i class="el-icon-question" />
                </el-tooltip>
              </el-form-item>
              <el-form-item label="音符最小间隔">
                <el-input-number
                  v-model="minStep"
                  :min="10"
                  :max="chart.songLength"
                />
                <el-tooltip
                  class="item"
                  effect="dark"
                  content="以毫秒为单位。当您使用播放敲谱模式键入音符的时候，音符会吸附到最近的断点上。您可以根据音乐的最小节拍时长设置本项，让音符的时机更加贴合音乐的节奏。"
                  placement="top-start"
                  style="margin-left:10px;"
                >
                  <i class="el-icon-question" />
                </el-tooltip>
              </el-form-item>
              <el-form-item label="显示时间区域">
                <el-col :span="12">
                  开始时间点<el-input-number
                    v-model="displayStart"
                    :min="0"
                    :max="chart.songLength"
                  />
                </el-col>
                <el-col :span="12">
                  结束时间点<el-input-number
                    v-model="displayEnd"
                    :min="0"
                    :max="chart.songLength"
                  />
                </el-col>
              </el-form-item>
              <el-form-item label="">
                <el-slider
                  v-model="global.currentTime"
                  range
                  :min="0"
                  :max="chart.songLength"
                  @change="changeDisplayArea"
                ></el-slider>
              </el-form-item>
            </el-form>
            <div></div>
          </el-dialog>
        </div>
        <div>
          <el-button size="small" type="text" class="header-button"
            >保存</el-button
          >
          <el-button size="small" type="text" class="header-button"
            >保存并返回</el-button
          >
        </div>
      </div>
      <div class="header-slide">
        <div class="header-slide-item">
          <el-slider
            v-model="global.currentTime"
            :min="displayStart"
            :max="displayEnd"
            :step="minStep"
            @change="changeTime"
            @mousedown="SlideMouseDown"
            @mouseup="SlideMouseUp"
          ></el-slider>
        </div>
      </div>
    </div>
    <!-- 侧边栏 -->
    <transition
      name="fade"
      enter-active-class="animate__animated animate__fadeInLeft"
      leave-active-class="animate__animated animate__fadeOutLeft"
    >
      <div
        v-if="menuOpened"
        :class="menuOpened ? 'sider-opened' : 'sider-closed'"
      >
        <transition
          name="fade"
          enter-active-class="animate__animated animate__fadeInLeft"
          leave-active-class="animate__animated animate__fadeOutLeft"
          ><TrackPanel
            key="trackpanel"
            v-show="currentSelectTrack != null"
            :Track="currentSelectTrack"
            :global="global"
            @returnMenu="returnMenu"
        /></transition>

        <MenuPanel
          key="menupanel"
          :global="global"
          :chart="chart"
          v-show="currentSelectTrack == null"
        /></div
    ></transition>

    <!-- 谱面展示 -->
    <div class="select">
      <div
        :class="menuOpened ? 'container-small' : 'container-big'"
        id="play-interface-container"
      >
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
        <canvas id="track-canvas" style="position:absolute;top:0;left:0;" />
        <canvas id="note-canvas" style="position:absolute;top:0;left:0;" />
        <canvas id="judge-canvas" style="position:absolute;top:0;left:0;" />
        <!-- 轨道 -->
        <div
          class="play-interface-track-container"
          v-for="Track in chart.tracks"
          :key="Track"
        >
          <div>
            <Track
              :Track="Track"
              :global="global"
              v-if="
                global.currentTime > Track.startTiming &&
                  global.currentTime < Track.endTiming
              "
            />
          </div>
        </div>
        <div
          class="selected-track"
          v-if="
            currentSelectTrack != null &&
              global.currentTime > currentSelectTrack.startTiming &&
              global.currentTime < currentSelectTrack.endTiming &&
              currentSelectTrack.tempPositionX >= 0 &&
              currentSelectTrack.tempPositionX <= 1
          "
          :style="{
            position: 'absolute',
            top: '0px',
            left:
              (currentSelectTrack.tempPositionX -
                currentSelectTrack.tempWidth) *
                global.screenWidth +
              'px',
            width:
              2 * currentSelectTrack.tempWidth * global.screenWidth - 4 + 'px',
            height: global.finalY * global.screenHeight - 2 + 'px',
            border: '2px solid rgba(255,255,255,1)',
            background: 'rgba(255,255,255,0.2)',
          }"
        ></div>
      </div>
    </div>
  </div>
</template>

<script>
import Track from "@/components/PlayInterface/Track";
import MenuPanel from "@/components/ChartMaker/MenuPanel";
import TrackPanel from "@/components/ChartMaker/TrackPanel";
import { chart } from "@/utils/chart.js";
import "animate.css";
export default {
  components: {
    Track,
    MenuPanel,
    TrackPanel,
  },
  watch: {
    "global.currentTime"() {
      if (this.global.notePainter) {
        this.global.notePainter.clearRect(
          0,
          0,
          this.global.noteCanvas.width,
          this.global.noteCanvas.height
        );
      }
      if (this.global.trackPainter) {
        this.global.trackPainter.clearRect(
          0,
          0,
          this.global.trackCanvas.width,
          this.global.trackCanvas.height
        );
      }
      if (this.global.judgePainter) {
        this.global.judgePainter.clearRect(
          0,
          0,
          this.global.judgeCanvas.width,
          this.global.judgeCanvas.height
        );
      }
    },
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
      menuOpened: true,
      volume: 100,
      minStep: 10,
      keyStep: 10,
      currentSelectTrack: null,
      currentTracks: [],
      globalSetting: false, //是否显示全局设置
      displayStart: 0,
      displayEnd: 0,
      form: {},
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
      remainingTime: 700,
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
      notePainter: null,
      trackPainter: null,
      judgePainter: null,
      noteCanvas: null,
      trackCanvas: null,
      judgeCanvas: null,
      repaint: false,
      recalculateTrack: false,
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
        for (var i = 0; i < 510; i += 16) {
          setTimeout(() => {
            if (this.global.trackPainter) {
              this.global.trackPainter.clearRect(
                0,
                0,
                this.global.trackCanvas.width,
                this.global.trackCanvas.height
              );
            }
            this.resize();
            this.repaint();
          }, i);
        }
        setTimeout(() => {
          this.repaint();
        }, 520);
      })();
    };
    document.onmouseup = function() {
      if (that.sliding) {
        that.SlideMouseUp();
      }
    };
    document.onkeydown = function(e) {
      if (!e.repeat) {
        that.global.keyPressTime[e.key.toUpperCase()] = that.global.currentTime;
        that.global.keyIsHold[e.key.toUpperCase()] = true;
        that.global.keyUsed[e.key.toUpperCase()] = false;
        if (e.key == " ") {
          if (that.isRunning) {
            that.pause();
          } else {
            that.play();
          }
        }
      }
      if (e.key == "ArrowUp") {
        if (that.volume <= 90) that.volume += 10;
        else that.volume = 100;
        that.audio.volume = that.volume / 100;
      } else if (e.key == "ArrowDown") {
        if (that.volume >= 10) that.volume -= 10;
        else that.volume = 0;
        that.audio.volume = that.volume / 100;
      } else if (e.key == "ArrowLeft") {
        that.audio.currentTime -= that.keyStep / 1000;
        that.resetTrack();
      } else if (e.key == "ArrowRight") {
        that.audio.currentTime += that.keyStep / 1000;
        that.resetTrack();
      }
    };
    document.onkeyup = function(e) {
      that.global.keyIsHold[e.key.toUpperCase()] = false;
    };
    this.playInterface.onmousedown = function(e) {
      that.calculateCurrentTracks();
      for (var i = 0; i < that.currentTracks.length; i++) {
        var track = that.currentTracks[i];
        var left =
          (track.tempPositionX - track.tempWidth) * that.global.screenWidth;
        var right =
          (track.tempPositionX + track.tempWidth) * that.global.screenWidth;
        if (e.offsetX > left && e.offsetX < right) {
          that.currentSelectTrack = track;
          that.$forceUpdate();
        }
      }
    };
    this.initiate();
  },

  methods: {
    //计算当前轨道
    calculateCurrentTracks() {
      this.currentTracks = [];
      for (var i = 0; i < this.chart.tracks.length; i++) {
        if (
          this.global.currentTime > this.chart.tracks[i].startTiming &&
          this.global.currentTime < this.chart.tracks[i].endTiming
        ) {
          this.currentTracks.push(this.chart.tracks[i]);
        }
      }
    },
    //设置轨道index
    setIndex() {
      for (var i = 0; i < this.chart.tracks.length; i++) {
        this.chart.tracks[i].index = i;
      }
      for (var j = 0; j < this.chart.changeBackgroundOperations.length; j++) {
        this.chart.changeBackgroundOperations[j].index = j;
      }
    },
    //调整画布
    resize() {
      const that = this;
      this.playInterface = document.getElementById("play-interface-container");
      that.global.screenWidth = that.playInterface.offsetWidth;
      that.global.screenHeight = that.playInterface.offsetHeight;
      that.global.noteCanvas.height = that.playInterface.offsetHeight;
      that.global.trackCanvas.height = that.playInterface.offsetHeight;
      that.global.judgeCanvas.height = that.playInterface.offsetHeight;
      that.global.noteCanvas.width = that.playInterface.offsetWidth;
      that.global.trackCanvas.width = that.playInterface.offsetWidth;
      that.global.judgeCanvas.width = that.playInterface.offsetWidth;
    },
    //调整画布
    changeMenuDisplay() {
      this.menuOpened = !this.menuOpened;
      for (var i = 0; i < 510; i += 16) {
        setTimeout(() => {
          if (this.global.trackPainter) {
            this.global.trackPainter.clearRect(
              0,
              0,
              this.global.trackCanvas.width,
              this.global.trackCanvas.height
            );
          }
          this.resize();
          this.repaint();
        }, i);
      }
      setTimeout(() => {
        this.repaint();
      }, 520);
    },
    //获取谱面信息
    getChart() {
      this.chart = chart;
      this.setIndex();
      this.displayStart = 0;
      this.displayEnd = this.chart.songLength;
    },
    //给轨道排序
    sortTrack() {
      this.chart.tracks.sort(function(a, b) {
        return b.startTiming - a.startTiming;
      });
      this.setIndex();
    },
    //运行
    run() {
      // var refresh=1000/( Math.floor(this.audio.currentTime * 1000)- this.global.currentTime)
      // if(refresh<50) console.log(refresh)
      if (!this.sliding) {
        this.global.currentTime = Math.floor(this.audio.currentTime * 1000);
      } else {
        this.resetTrack();
      }
      if (this.global.currentTime >= this.chart.songLength) {
        this.isRunning = false;
      }
      if (this.global.currentTime >= this.displayEnd) {
        this.audio.currentTime = this.displayEnd / 1000;
        this.audio.pause();
        this.pause();
        this.isRunning = false;
      }
      requestAnimationFrame(this.run);
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
      this.volume = this.$store.state.volume;
      this.audio.volume = this.$store.state.volume / 100;
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
          operation.endTime = nextOperation.startTime;
        } else {
          this.imagePath.push({
            url: operation.background,
            startTime: start,
            endTime: this.chart.songLength + 1000,
          });
          operation.endTime = this.chart.songLength + 1000;
        }
      }
    },

    //变换slide的时间
    changeTime() {
      this.resetTrack();
      this.global.currentTime = this.global.currentTime + 1;
      this.global.currentTime = this.global.currentTime - 1;
      this.audio.currentTime = this.global.currentTime / 1000;
    },
    //鼠标按下时间轴
    SlideMouseDown() {
      this.audio.pause();
      this.sliding = true;
      this.resetTrack();
    },
    //鼠标从时间轴上抬起
    SlideMouseUp() {
      this.sliding = false;
      this.audio.currentTime = this.global.currentTime / 1000;
      this.resetTrack();
      if (this.isRunning) {
        this.sliding = false;
        this.resetTrack();
        setTimeout(() => {
          this.resetTrack();
          this.audio.play();
        }, 50);
      }
    },
    //暂停
    pause() {
      this.audio.pause();
      this.isRunning = false;
    },
    //播放
    play() {
      this.resetTrack();
      this.sliding = false;
      this.isRunning = true;
      setTimeout(() => {
        this.resetTrack();
        this.audio.play();
      }, 50);
    },
    //重新开始
    reStart() {
      this.resetTrack();
      this.audio.currentTime = this.displayStart / 1000;
      this.global.currentTime = this.displayStart;
      if (this.isRunning) {
        setTimeout(() => {
          this.resetTrack();
          this.audio.play();
        }, 50);
      }
    },
    //重置轨道
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
    //重新绘制
    repaint() {
      if (this.global.notePainter) {
        this.global.notePainter.clearRect(
          0,
          0,
          this.global.noteCanvas.width,
          this.global.noteCanvas.height
        );
      }
      if (this.global.trackPainter) {
        this.global.trackPainter.clearRect(
          0,
          0,
          this.global.trackCanvas.width,
          this.global.trackCanvas.height
        );
      }
      if (this.global.judgePainter) {
        this.global.judgePainter.clearRect(
          0,
          0,
          this.global.judgeCanvas.width,
          this.global.judgeCanvas.height
        );
      }
      this.global.repaint = !this.global.repaint;
    },
    //重新绘制
    recalculateTrack() {
      this.global.recalculateTrack = !this.global.recalculateTrack;
      this.repaint();
    },
    //时间区域调整
    changeDisplayArea(values) {
      this.displayStart = values[0];
      this.displayEnd = values[1];
      this.global.currentTime = this.displayStart;
      this.audio.currentTime = this.global.currentTime / 1000;
      this.resetTrack();
    },
    //改变音量
    changeVolume() {
      this.audio.volume = this.volume / 100;
    },
    //返回到菜单
    returnMenu() {
      this.currentSelectTrack = null;
    },
  },
};
</script>
<style>
.animate__animated.animate__fadeInLeft {
  --animate-duration: 0.5s;
}
.animate__animated.animate__fadeOutLeft {
  --animate-duration: 0.2s;
}
</style>
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
.header-button {
  color: white;
}

.header-button :hover {
  color: rgb(234, 234, 234);
}

.header-button :active {
  color: rgb(212, 212, 212);
}

.header {
  position: absolute;
  top: 0;
  height: 80px;
  width: 100%;
  background: #242f42;
}
.header-buttons {
  padding: 10px 10px 0px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-slide {
  width: 100%;
}

#play-interface-container {
  position: absolute;
  top: 80px;
  height: calc(100vh - 80px);
  background-color: white;
}

.sider-closed {
  position: absolute;
  top: 80px;
  height: calc(100vh - 80px);
  background: white;
  width: 0px;
  left: 0px;
}

.sider-opened {
  position: absolute;
  top: 80px;
  height: calc(100vh - 80px);
  width: 300px;
  left: 0px;
}
.container-small {
  left: 300px;
  width: calc(100vw - 300px);
  transition: 0.5s;
}

.container-big {
  left: 0px;
  width: 100vw;
  transition: 0.5s;
}
.header-slide-item {
  width: 96%;
  padding: 0px 2%;
  float: left;
}
.selected-track {
  -webkit-box-shadow: 0 0 20px 10px rgba(0, 0, 0, 0.5);
  box-shadow: 0 0 20px 10px rgba(0, 0, 0, 0.5);
}
</style>
