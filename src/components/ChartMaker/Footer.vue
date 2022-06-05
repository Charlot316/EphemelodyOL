<template>
  <div class="footer-container">
    <div class="footer-header">
      <div class="footer-header-left">
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="
            myGlobal.timeSort = !myGlobal.timeSort;
            updateTrack();
          "
          >{{ myGlobal.timeSort ? "改为坐标排序" : "改为时间排序" }}</el-button
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
          class="show-button"
          style="margin-right:5px;"
          @click="showCurrent = !showCurrent"
          >{{ showCurrent ? "显示全部轨道" : "显示当前轨道" }}</el-button
        >
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
        <el-button
          type="text"
          class="show-button"
          style="margin-right:5px;"
          @click="enableEdit = !enableEdit"
          >{{ enableEdit ? "禁用编辑弹窗" : "开启编辑弹窗" }}</el-button
        >
      </div>
      <div class="footer-header-right">
        <el-tooltip
          class="item"
          effect="dark"
          content="设置时间轴部分显示的时间范围"
          placement="top-start"
          ><el-slider
            v-model="displayAreaTime"
            :min="1000"
            :max="chart.songLength"
          ></el-slider>
        </el-tooltip>
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
                    (track.showInTimeline
                      ? !showNoRemain
                        ? track.notes.length == 0
                          ? false
                          : true
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
                      : false) &&
                      (showCurrent
                        ? global.currentTime >= track.startTiming &&
                          global.currentTime <= track.endTiming
                        : true)
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
        <div
          :style="{
            position: 'absolute',
            left: 0,
            top: scrollTop + 'px',
            height: '20px',
            width: '100%',
          }"
        >
          <BeatLine
            :chart="chart"
            :global="global"
            :displayAreaTime="displayAreaTime"
          />
        </div>
        <div style="position:absolute;left:0;top:0;">
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
                    (track.showInTimeline
                      ? !showNoRemain
                        ? track.notes.length == 0
                          ? false
                          : true
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
                      : false) &&
                      (showCurrent
                        ? global.currentTime >= track.startTiming &&
                          global.currentTime <= track.endTiming
                        : true)
                  "
                  :currentNoteType="currentNoteType"
                  :id="'trackCardPanel' + track.index"
                  :chart="chart"
                  :track="track"
                  :global="global"
                  :scrollLeft="scrollLeft"
                  :displayAreaTime="displayAreaTime"
                  :enableEdit="enableEdit"
                  @currentTrack="currentTrack"
                />
              </transition>
            </div>
          </transition-group>
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
import BeatLine from "./BeatLine";
export default {
  components: {
    TrackCard,
    TrackCardPanel,
    BeatLine,
  },
  props: ["chart", "global"],
  data() {
    return {
      myChart: this.chart,
      myGlobal: this.global,
      scrollLeft: 0,
      scrollTop: 0,
      displayAreaTime: 3000,
      audio: null,
      indicatorLeft: 0,
      rightScrollElement: null,
      rightClicked: false,
      autoScroll: false,
      showReal: true,
      showFake: true,
      showNoRemain: true,
      currentNoteType: 0,
      enableEdit: true,
      showCurrent: false,
    };
  },
  mounted() {
    this.audio = document.getElementById("audioSong");
    setTimeout(() => {
      this.displayAreaTime = 1000;
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
    updateTrack() {
      this.myGlobal.reCalculateTrack = !this.myGlobal.reCalculateTrack;
      this.myGlobal.reCalculateChartMaker = !this.myGlobal
        .reCalculateChartMaker;
    },
    rightClick(e) {
      let x = e.clientX - 300 + this.scrollLeft;
      var currentTime =
        (x / (this.global.documentWidth - 300)) * this.displayAreaTime;
      this.audio.currentTime = currentTime / 1000;
      this.myGlobal.currentTime = currentTime;
      this.updateTrack();
    },
    rightMouseMove(e) {
      let x = e.clientX - 300 + this.scrollLeft;
      this.indicatorLeft = x;
      if (this.rightClicked) {
        var currentTime =
          (x / (this.global.documentWidth - 300)) * this.displayAreaTime;
        this.audio.currentTime = currentTime / 1000;
        this.myGlobal.currentTime = currentTime;
        this.updateTrack();
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
  min-width: 960px;
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
.show-button {
  color: #b9b9b9;
}
.show-button:hover {
  color: #dfdfdf;
}
.show-button:active {
  color: #808080;
}
</style>
