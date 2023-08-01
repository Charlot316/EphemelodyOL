<template>
  <div class="play-interface select">
    <div class="header">
      <div class="header-buttons">
        <div>
          <el-button
            size="small"
            type="text"
            class="header-button"
            @click="$router.go(-1)"
            >返回</el-button
          >
        </div>
        <div>
          <el-button
            size="small"
            type="text"
            class="header-button"
            @click="saveChart(false)"
            >保存</el-button
          >
          <el-button
            size="small"
            type="text"
            class="header-button"
            @click="saveChart(true)"
            >保存并返回</el-button
          >
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
        :style="{
          '--footerHeight': footerHeight + 'px',
          '--documentHeight': global.documentHeight + 'px',
        }"
      >
        <MenuPanel
          key="menupanel"
          :Height="global.documentHeight - footerHeight - 50"
          :footerHeight="footerHeight"
          :global="global"
          :chart="chart"
        /></div
    ></transition>
    <transition
      name="fade"
      enter-active-class="animate__animated animate__fadeInLeft"
      leave-active-class="animate__animated animate__fadeOutLeft"
    >
      <div
        v-if="menuOpened"
        :class="menuOpened ? 'sider-opened-track' : 'sider-closed-track'"
        :style="{
          '--footerHeight': footerHeight + 'px',
          '--documentHeight': global.documentHeight + 'px',
        }"
      >
        <TrackPanel
          key="trackpanel"
          :Height="global.documentHeight - footerHeight - 50"
          :Track="currentSelectTrack"
          :global="global"
          :chart="chart"
          @currentTrack="currentTrack"
        /></div
    ></transition>

    <!-- 谱面展示 -->
    <div class="select">
      <div
        :class="menuOpened ? 'container-small' : 'container-big'"
        id="play-interface-container"
        :style="{
          '--footerHeight': footerHeight + 'px',
          '--documentHeight': global.documentHeight + 'px',
        }"
      >
        <!-- 音频 -->
        <audio
          id="audioSong"
          preload="auto"
          controls
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

    <!-- 进度条 -->
    <div
      v-if="chartGot"
      :class="menuOpened ? 'time-controller-small' : 'time-controller-big'"
      :style="{
        '--footerHeight': footerHeight + 'px',
        '--documentHeight': global.documentHeight + 'px',
      }"
    >
      <div class="time-control-buttons">
        <el-button
          v-if="!menuOpened"
          size="small"
          class="header-button"
          @click="changeMenuDisplay"
          type="text"
          ><i class="el-icon-full-screen"
        /></el-button>
        <el-button
          v-else
          size="small"
          class="header-button"
          @click="changeMenuDisplay"
          type="text"
          ><i class="el-icon-full-screen"
        /></el-button>
        <el-button
          size="small"
          @click="reStart"
          class="header-button"
          type="text"
          ><i class="el-icon-refresh-left"
        /></el-button>
        <el-button
          v-if="isRunning"
          size="small"
          class="header-button"
          @click="pause"
          type="text"
          ><i class="el-icon-video-pause"
        /></el-button>
        <el-button
          v-else
          size="small"
          class="header-button"
          @click="play"
          type="text"
          ><i class="el-icon-video-play"
        /></el-button>
        <el-button
          size="small"
          type="text"
          class="header-button"
          @click="globalSetting = true"
          ><i class="el-icon-setting"
        /></el-button>
      </div>
      <div class="header-slide">
        <div class="header-slide-item">
          <el-slider
            v-model="global.currentTime"
            :min="displayStart"
            :max="displayEnd"
            @change="changeTime"
            @mousedown="SlideMouseDown"
            @mouseup="SlideMouseUp"
            @touchstart="SlideMouseDown"
            @touchend="SlideMouseUp"
          ></el-slider>
        </div>
      </div>
      <el-dialog
        v-model="globalSetting"
        @close="checkbpm"
        @closed="globalSetting = false"
        width="650px"
      >
        <el-form :model="form" label-width="200px" style="padding: 20px;">
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
          <el-form-item label="首拍偏移(单位:ms)">
            <el-input-number
              v-model="chart.firstBeatDelay"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="第一拍的偏移，在一拍间隔部分可以顺带测量，你也可以选择使用音频软件观察声波自行填写"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
          </el-form-item>
          <el-form-item label="末拍偏移(单位:ms)">
            <el-input-number
              v-model="chart.lastBeatDelay"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="最后一拍的偏移，你可以填写好精确的第一拍和最后一拍的偏移和节拍数，然后直接计算出一拍间隔"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
          </el-form-item>
          <el-form-item label="节拍数">
            <el-input-number
              v-model="chart.beatsCount"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="用以计算一拍间隔,注意填写的是拍与拍的间隔数（线段)，而不是节奏点数（端点)"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
            <div style="margin-top:15px;">
              <el-tooltip
                class="item"
                effect="dark"
                content="计算公式为（末拍偏移-首拍偏移)/节拍数"
                placement="top-start"
              >
                <el-button @click="ManualCalculatebpm">
                  精确计算一拍间隔
                </el-button>
              </el-tooltip>
            </div>
          </el-form-item>
          <el-form-item label="一拍间隔(单位:ms)">
            <el-input-number
              v-model="chart.bpm"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="一个节拍的长度，拖动音符时，音符将会依附到最近的1/16节拍时间点上，轻点下方按钮即可大致估算出一拍的长度，但仍然建议自己精确填写首拍和末拍偏移计算"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
            <br />
            <div style="margin-top:15px;">
              <el-button @mousedown="calculatebpm">{{
                !bpmStart ? "粗略估算一拍间隔" : "请在节奏点处按下"
              }}</el-button>
              <el-button @click="endbpm" v-if="bpmStart">
                结束或重新测量
              </el-button>
            </div>
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

    <!-- 时间轴 -->
    <transition
      name="fade"
      enter-active-class="animate__animated animate__fadeInUp"
      leave-active-class="animate__animated animate__fadeOutDown"
    >
      <div
        v-if="menuOpened && chartGot"
        :class="menuOpened ? 'footer-opened' : 'footer-closed'"
        :style="{
          '--footerHeight': footerHeight + 'px',
          '--documentHeight': global.documentHeight + 'px',
        }"
      >
        <div
          style="height:15px;width:100%;cursor:ns-resize;text-align: center;padding-top:5px;font-size: 15px;"
          id="footer-resizer"
          @mousedown="canDrag = true"
        >
          <span style="color:rgb(200,200,200)">{{ global.currentTime }}</span
          ><span style="color:rgb(150,150,150)">/{{ chart.songLength }}</span>
        </div>
        <div style="height:calc(100% - 20px);width:100%;">
          <Footer
            :chart="chart"
            :global="global"
            @currentTrack="currentTrack"
          />
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import Track from "@/components/PlayInterface/Track";
import MenuPanel from "@/components/ChartMaker/MenuPanel";
import TrackPanel from "@/components/ChartMaker/TrackPanel";
import Footer from "@/components/ChartMaker/Footer";
import "animate.css";
export default {
  components: {
    Track,
    MenuPanel,
    TrackPanel,
    Footer,
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
      if (this.sliding) {
        document.querySelector("#time-indicater").scrollIntoView({ behavior: "smooth"});
      }
    },
    "global.reCalculateChartMaker"() {
      if (this.chart.changeBackgroundOperations) {
        this.generateImagePath();
        this.sortTrack();
        this.resetTrack();
      }
    },
  },
  data() {
    return {
      canDrag: false,
      chart: {
        songLength: 0,
      },
      chartGot: false,
      bpmStart: false,
      bpmcount: 0,
      bpmtotal: 0,
      startTotal: 0,
      lastTime: 0,
      startTime: 0,
      global: { currentTime: 0 },
      imagePath: [],
      pauseVisible: false,
      audio: null,
      playInterface: null,
      isRunning: false,
      sliding: false,
      menuOpened: true,
      volume: 100,
      keyStep: 10,
      currentSelectTrack: null,
      currentTracks: [],
      globalSetting: false, //是否显示全局设置
      displayStart: 0,
      displayEnd: 10,
      form: {},
      footerHeight: 426,
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
  },
  created() {
    this.loadingStatus = {
      chart: false,
      audio: false,
      image: false,
      canRun: false,
    };
    this.global = {
      beatLine: true,
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
      notePainter: null,
      trackPainter: null,
      judgePainter: null,
      noteCanvas: null,
      trackCanvas: null,
      judgeCanvas: null,
      repaint: false,
      reCalculateTrack: false,
      reCalculateChartMaker: false,
      mouseDown: false,
      mouseUp: true,
      mouseMove: false,
      clientX: 0,
      clientY: 0,
      timeSort: true,
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
    this.global.documentHeight = document.documentElement.clientHeight;
    this.global.documentWidth = document.documentElement.clientWidth;
    window.onresize = () => {
      return (() => {
        that.global.documentHeight = document.documentElement.clientHeight;
        that.global.documentWidth = document.documentElement.clientWidth;
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
      }
      // else if (e.key == "ArrowLeft") {
      //   e.preventDefault();
      //   that.minusTime(that.keyStep / 1000);
      // } else if (e.key == "ArrowRight") {
      //   e.preventDefault();
      //   that.audio.currentTime += that.keyStep / 1000;
      //   that.plusTime(that.keyStep / 1000);
      // }
    };

    document.onkeyup = function(e) {
      that.global.keyIsHold[e.key.toUpperCase()] = false;
    };
    this.playInterface.onmousedown = function(e) {
      if (that.currentSelectTrack != null) {
        that.currentSelectTrack.edit = false;
        that.currentSelectTrack = null;
      }
      that.calculateCurrentTracks();
      for (var i = 0; i < that.currentTracks.length; i++) {
        var track = that.currentTracks[i];
        var left =
          (track.tempPositionX - track.tempWidth) * that.global.screenWidth;
        var right =
          (track.tempPositionX + track.tempWidth) * that.global.screenWidth;
        if (e.offsetX > left && e.offsetX < right) {
          that.currentSelectTrack = track;

          document
            .querySelector("#trackCard" + track.index)
            .scrollIntoView({ behavior: "smooth"});
          document
            .querySelector("#trackCardPanel" + track.index)
            .scrollIntoView({ behavior: "smooth"});

          setTimeout(() => {
            that.currentSelectTrack.edit = true;
          }, 10);
          that.$forceUpdate();
        }
      }
    };
    this.initiate();

    document.onmousemove = function(e) {
      that.global.clientX = e.clientX;
      that.global.clientY = e.clientY;
      that.global.mouseMove = !that.global.mouseMove;
      if (that.canDrag) {
        if (e.clientY > 130 && e.clientY < that.global.documentHeight - 100) {
          that.footerHeight = that.global.documentHeight - e.clientY;
          that.resize();
          setTimeout(() => {
            that.repaint();
          }, 10);
        }
      }
    };
    document.onmouseup = function() {
      if (that.sliding) {
        that.SlideMouseUp();
      }
      that.canDrag = false;
      that.global.mouseUp = !that.global.mouseUp;
    };
    document.onmousedown = function(e) {
      that.global.clientX = e.clientX;
      that.global.clientY = e.clientY;
      that.global.mouseDown = !that.global.mouseDown;
    };
  },

  methods: {
    async saveChart(back) {
      try {
        const { data: res } = await this.$http.post(
          "/chart/editChartContent",
          this.chart
        );
        if (res.code !== 0) {
          this.$notify({
            title: "失败",
            message: "谱面上传失败！",
            type: "error",
          });
        }
        this.$notify({
          title: "成功",
          message: "谱面上传成功！",
          type: "success",
        });
        if (back) {
          this.$router.push("/admin");
        }
      } catch (err) {
        return this.$notify({
          title: "错误",
          message: "网络异常",
          type: "error",
        });
      }
    },
    ManualCalculatebpm() {
      if (
        this.chart.beatsCount &&
        this.chart.lastBeatDelay &&
        this.chart.firstBeatDelay
      ) {
        this.chart.bpm =
          (this.chart.lastBeatDelay - this.chart.firstBeatDelay) /
          this.chart.beatsCount;
      } else {
        this.$notify({
          title: "错误",
          message: "请先输入首拍、末拍偏移和节拍数",
          type: "error",
        });
      }
    },
    calculatebpm() {
      if (this.bpmStart == false) {
        this.global.currentTime = 0;
        this.audio.currentTime = 0;
        this.bpmcount = 0;
        this.bpmStart = false;
        this.lastTime = 0;
        this.bpmtotal = 0;
        this.startTotal = 0;
        this.resetTrack();
        this.lastTime = this.global.currentTime;
        this.bpmStart = true;
        this.audio.play();
      } else {
        if (this.bpmcount <= 3) {
          this.lastTime = this.global.currentTime;
        } else if (this.bpmcount < 10) {
          var now = this.global.currentTime;
          this.bpmtotal += now - this.lastTime;
          this.startTotal += now - (now - this.lastTime) * this.bpmcount;
          this.lastTime = now;
        } else {
          now = this.global.currentTime;
          this.bpmtotal += now - this.lastTime;
          this.startTotal += now - (now - this.lastTime) * this.bpmcount;
          this.lastTime = now;
          this.chart.bpm = this.bpmtotal / (this.bpmcount - 3);
          this.chart.firstBeatDelay = Math.round(
            this.startTotal / (this.bpmcount - 3)
          );
        }
        this.bpmcount++;
      }
    },
    endbpm() {
      this.audio.pause();
      this.global.currentTime = 0;
      this.audio.currentTime = 0;
      this.startTotal = 0;
      this.bpmcount = 0;
      this.bpmStart = false;
      this.lastTime = 0;
      this.bpmtotal = 0;
      this.resetTrack();
    },
    checkbpm() {
      this.endbpm();
      if (this.chart.bpm == 0 || !this.chart.bpm) {
        setTimeout(() => {
          this.$notify({
            title: "提示",
            message: "请设置节拍",
            type: "warning",
          });
          this.globalSetting = true;
        }, 1000);
      }
    },
    currentTrack(param) {
      if (this.currentSelectTrack != null) this.currentSelectTrack.edit = false;
      this.currentSelectTrack = param;
      this.$forceUpdate();
    },
    minusTime(value) {
      this.audio.currentTime -= value;
      this.global.currentTime = this.audio.currentTime;
      this.resetTrack();
    },
    plusTime(value) {
      this.audio.currentTime += value;
      this.global.currentTime = this.audio.currentTime;
      this.resetTrack();
    },
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
      for (var i = 0; i < 1000; i += 16) {
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
        this.displayStart = 0;
        this.sortTrack();
        this.audio = document.getElementById("audioSong");
        this.volume = this.$store.state.volume;
        let that = this;
        this.audio.oncanplay = function() {
          that.chart.songLength = Math.round(1000 * that.audio.duration);
          that.generateImagePath();
          that.displayEnd = that.chart.songLength;
        };
        this.chartGot = true;
        this.$forceUpdate();
        this.audio.volume = this.$store.state.volume / 100;
        this.run();
        console.log(this.chart);
        if (!this.chart.bpm || this.chart.bpm == 0) {
          this.globalSetting = true; //请设置节拍
          this.$notify({
            type: "warning",
            title: "提示",
            message: "请设置节拍",
          });
        }
        if (!this.chart.bpm) {
          this.chart.bpm = 0;
        }
        if (!this.chart.firstBeatDelay) {
          this.chart.firstBeatDelay = 0;
        }

        setTimeout(() => {
          this.resize();
        }, 1000);
      } catch (err) {
        return this.$notify({
          title: "错误",
          message: "网络异常",
          type: "error",
        });
      }
    },
    //给轨道排序
    sortTrack() {
      if (this.global.timeSort) {
        this.chart.tracks.sort(function(a, b) {
          return a.startTiming - b.startTiming;
        });
      } else {
        this.chart.tracks.sort(function(a, b) {
          return a.positionX - b.positionX;
        });
      }

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
      document.querySelector("#time-indicater").scrollIntoView({ behavior: "smooth"});
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
    reCalculateTrack() {
      this.global.reCalculateTrack = !this.global.reCalculateTrack;
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
<style scoped>
.animate__animated.animate__fadeInLeft {
  --animate-duration: 0.5s;
}
.animate__animated.animate__fadeOutLeft {
  --animate-duration: 0.5s;
}

.animate__animated.animate__fadeInUp {
  --animate-duration: 0.5s;
}
.animate__animated.animate__fadeOutDown {
  --animate-duration: 0.5s;
}
.play-interface {
  height: 100vh;
  width: 100vw;
  background: rgb(55, 55, 55);
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
  height: 50px;
  width: 100%;
  background: rgb(39, 39, 39);
}
.header-buttons {
  padding: 10px 10px 0px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#play-interface-container {
  position: absolute;
  top: 50px;
  background: rgb(32, 32, 32);
}

.sider-closed {
  position: absolute;
  top: 50px;
  height: calc(var(--documentHeight) - 50px - var(--footerHeight));
  background: rgb(32, 32, 32);
  width: 0px;
  left: 0px;
}

.sider-opened {
  position: absolute;
  top: 50px;
  height: calc(var(--documentHeight) - 50px - var(--footerHeight));
  background: rgb(32, 32, 32);
  width: 300px;
  left: 0px;
  overflow: auto;
  -ms-overflow-style: none;
  padding-bottom: 20px;
}

.sider-closed-track {
  position: absolute;
  top: 50px;
  height: calc(var(--documentHeight) - 50px - var(--footerHeight));
  background: rgb(32, 32, 32);
  width: 0px;
  left: 0px;
}

.sider-opened-track {
  position: absolute;
  top: 50px;
  height: calc(var(--documentHeight) - 50px - var(--footerHeight));
  background: rgb(32, 32, 32);
  width: 300px;
  left: 300px;
  overflow: auto;
  -ms-overflow-style: none;
  padding-bottom: 20px;
}

.footer-closed {
  position: absolute;
  bottom: 0px;
  height: 0px;
  width: 100vw;
  left: 0px;
}

.footer-opened {
  position: absolute;
  bottom: 0px;
  height: var(--footerHeight);
  background: rgb(55, 55, 55);
  width: 100vw;
  left: 0px;
}
.container-small {
  left: 600px;
  top: 50px;
  width: calc(100vw - 600px);
  height: calc(var(--documentHeight) - 120px - var(--footerHeight));
}

.container-big {
  left: 0px;
  top: 50px;
  width: 100vw;
  height: calc(var(--documentHeight) - 120px);
  transition: 0.5s;
}

.header-slide {
  width: 100%;
  height: 40px;
}

.time-controller-small {
  position: absolute;
  left: 600px;
  bottom: var(--footerHeight);
  height: 80px;
  width: calc(100vw - 600px);
  background: rgb(32, 30, 32);
}
.time-controller-big {
  position: absolute;
  left: 0px;
  bottom: 0px;
  height: 80px;
  width: 100vw;
  background: rgb(32, 30, 32);
}
.time-control-buttons .header-button {
  font-size: 30px;
}
.time-control-buttons {
  height: 40px;
  text-align: center;
  align-items: center;
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
::v-deep .el-slider__bar {
  background-color: rgb(138, 138, 138);
}

::v-deep .el-slider__button {
  border: 2px solid rgb(138, 138, 138);
}
</style>
