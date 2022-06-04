<template>
  <div class="footer-container">
    <div class="footer-header">
      <div class="footer-header-left">
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
          style="margin-right:5px;"
          @click="showReal = !showReal"
          >{{ showReal ? "关闭实轨" : "显示实轨" }}</el-button
        >
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="showFake = !showFake"
          >{{ showFake ? "关闭虚轨" : "显示虚轨" }}</el-button
        >
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="showNoRemain = !showNoRemain"
          >{{ showNoRemain ? "关闭无音符轨道" : "显示无音符轨道" }}</el-button
        >
        <el-button
          type="text"
          :class="currentNoteType == 0 ? 'show-button-selected' : 'show-button'"
          style="margin-right:5px;"
          @click="currentNoteType = 0"
          >双击短键</el-button
        >
        <el-button
          type="text"
          :class="currentNoteType == 1 ? 'show-button-selected' : 'show-button'"
          style="margin-right:5px;"
          @click="currentNoteType = 1"
          >双击长键</el-button
        >
        <el-button
          type="text"
          :class="currentNoteType == 2 ? 'show-button-selected' : 'show-button'"
          style="margin-right:5px;"
          @click="currentNoteType = 2"
          >双击滑键</el-button
        >
        <el-button
          type="text"
          :class="currentNoteType == 3 ? 'delete-button' : 'show-button'"
          style="margin-right:5px;"
          @click="currentNoteType = 3"
          >单击删除</el-button
        >
      </div>
      <div class="footer-header-right">
        <el-slider
          v-model="displayAreaTime"
          :min="1000"
          :max="chart.songLength"
        ></el-slider>
      </div>
    </div>
    <div v-if="chart.tracks">
      <div class="footer-left">
        <div
          class="footer-track-container"
          id="footer-left-scroll"
          @scroll="leftScroll"
        >
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
                  v-if="
                    track.showInTimeline
                      ? !this.showNoRemain
                        ? track.notes.length == 0
                          ? false
                          : track.notes[track.notes.length - 1].noteType == 1
                          ? global.currentTime >
                            track.notes[track.notes.length - 1].endTiming
                            ? false
                            : track.type == 1
                            ? this.showReal
                              ? true
                              : false
                            : this.showFake
                            ? true
                            : false
                          : global.currentTime >
                            track.notes[track.notes.length - 1].timing
                          ? false
                          : track.type == 1
                          ? this.showReal
                            ? true
                            : false
                          : this.showFake
                          ? true
                          : false
                        : track.type == 1
                        ? this.showReal
                          ? true
                          : false
                        : this.showFake
                        ? true
                        : false
                      : false
                  "
                  :chart="chart"
                  :track="track"
                  :global="global"
                  @currentTrack="currentTrack"
                />
              </transition>
            </div>
          </transition-group>
        </div>
      </div>
      <div
        class="footer-right"
        id="footer-right-scroll"
        @scroll="rightScroll"
        @mousemove="rightMouseMove($event)"
        @mousedown="
          rightClick($event);
          rightClicked = true;
        "
      >
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
              <TrackCardPanel
                v-if="
                  track.showInTimeline
                    ? !this.showNoRemain
                      ? track.notes.length == 0
                        ? false
                        : track.notes[track.notes.length - 1].noteType == 1
                        ? global.currentTime >
                          track.notes[track.notes.length - 1].endTiming
                          ? false
                          : track.type == 1
                          ? this.showReal
                            ? true
                            : false
                          : this.showFake
                          ? true
                          : false
                        : global.currentTime >
                          track.notes[track.notes.length - 1].timing
                        ? false
                        : track.type == 1
                        ? this.showReal
                          ? true
                          : false
                        : this.showFake
                        ? true
                        : false
                      : track.type == 1
                      ? this.showReal
                        ? true
                        : false
                      : this.showFake
                      ? true
                      : false
                    : false
                "
                :currentNoteType="currentNoteType"
                :id="'trackCardPanel' + track.index"
                :chart="chart"
                :track="track"
                :global="global"
                :scrollLeft="scrollLeft"
                :displayAreaTime="displayAreaTime"
                @currentTrack="currentTrack"
              />
            </transition>
          </div>
        </transition-group>

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
      displayAreaTime: 10000,
      audio: null,
      indicatorLeft: 0,
      rightScrollElement: null,
      rightClicked: false,
      autoScroll: false,
      showReal: true,
      showFake: true,
      showNoRemain: true,
      currentNoteType: 0,
    };
  },
  mounted() {
    this.audio = document.getElementById("audioSong");
    setTimeout(() => {
      this.displayAreaTime = 10000;
    }, 100);
  },
  watch: {
    "global.mouseUp"() {
      this.rightClicked = false;
    },
    "global.currentTime"() {
      if (this.rightScrollElement == null) {
        this.rightScrollElement = document.getElementById(
          "footer-right-scroll"
        );
      }

      if (!this.audio.paused) {
        var scrollLeft =
          (this.global.currentTime / this.displayAreaTime) *
            (this.global.documentWidth - 300) -
          (this.global.documentWidth - 300) / 2;
        if (scrollLeft < 0) scrollLeft = 0;
        this.rightScrollElement.scrollLeft = scrollLeft;
        this.scrollLeft = this.rightScrollElement.scrollLeft;
      }
      if (this.autoScroll) {
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
      }
    },
    chart() {
      this.myChart = this.chart;
    },
  },
  computed: {},
  methods: {
    rightClick(e) {
      let x = e.clientX - 300 + this.scrollLeft;
      var currentTime =
        (x / (this.global.documentWidth - 300)) * this.displayAreaTime;
      this.audio.currentTime = currentTime / 1000;
      this.myGlobal.currentTime = currentTime;
      this.resetTrack();
    },
    rightMouseMove(e) {
      let x = e.clientX - 300 + this.scrollLeft;
      this.indicatorLeft = x;
      if (this.rightClicked) {
        var currentTime =
          (x / (this.global.documentWidth - 300)) * this.displayAreaTime;
        this.audio.currentTime = currentTime / 1000;
        this.myGlobal.currentTime = currentTime;
        this.resetTrack();
      }
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
    resetTrack() {
      this.myGlobal.keyPressTime = [];
      this.myGlobal.keyIsHold = [];
      this.myGlobal.keyUsed = [];
      for (var i = 0; i < this.chart.tracks.length; i++) {
        var track = this.chart.tracks[i];
        var index = 0;
        var last = track.notes.length;
        for (var j = track.notes.length - 1; j >= 0; j--) {
          track.notes[j].judged = false;
          track.notes[j].index = j;
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

<style scope>
.footer-container {
  height: 100%;
  width: 100vw;
  position: relative;
}
.footer-header {
  height: 35px;
  padding-bottom: 5px;
  width: 100vw;
  position: absolute;
  top: 0px;
  left: 0px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.footer-left {
  height: calc(100% - 35px);
  width: 300px;
  position: absolute;
  top: 35px;
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
  height: calc(100% - 40px);
  width: calc(100vw - 300px);
  background: rgb(32, 32, 32);
  position: absolute;
  top: 35px;
  left: 300px;
  padding-top: 5px;
  overflow: auto;
}

.footer-track-container::-webkit-scrollbar {
  width: 0 !important;
}
.footer-header-left {
  padding-left: 25px;
}
.footer-header-right {
  padding-right: 25px;
  width: 200px;
}
.show-button-selected {
  color: #67c23a;
}
.show-button-selected:hover {
  color: #95d475;
}
.show-button-selected:active {
  color: #529b2e;
}
</style>
