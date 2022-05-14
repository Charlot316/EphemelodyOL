<template>
  <div
    class="select"
    :style="{
      position: 'absolute',
      height: '100%',
      width: width + 'px',
      left: left + 'px',
    }"
  >
    <div
      class="black-line"
      :style="{
        width: '2px',
        position: 'absolute',
        left: 0.5 * width + 'px',
        top: top + 'px',
        height: height + 'px',
        background: 'rgba(0,0,0,0.2)',
      }"
    ></div>
    <div class="key">
      <div
        class="key-background"
        :style="{
          width: lengthForKey + 'px',
          position: 'absolute',
          left: 0.5 * width - (Math.sqrt(2) - 1) * lengthForKey - 5.5 + 'px',
          top: (finalHeight * 9) / 8 - (Math.sqrt(2) - 1) * lengthForKey + 'px',
          margin: 'auto 0',
          border: '1px solid rgba(244,244,244,1)',
          height: lengthForKey + 'px',
          background: [
            Track.type == 1 ? 'rgba(255,255,255,0.4)' : 'rgba(255,255,255,0)',
          ],
          transform: 'rotateZ(45deg)',
          boxShadow: boxShadow,
        }"
      ></div>
      <div
        v-if="Track.type == 1"
        :style="{
          width: lengthForKey + 'px',
          position: 'absolute',
          left: 0.5 * width - 0.5 * lengthForKey + 'px',
          top: (finalHeight * 9) / 8 - (Math.sqrt(2) - 1) * lengthForKey + 'px',
          margin: 'auto 0',
          height: lengthForKey + 'px',
          textAlign: 'center',
          textShadow: '1px 1px 0 rgba(0,0,0,0.25)',
          fontSize: lengthForKey * 0.7 + 'px',
          color: 'rgba(255,255,255,0.8)',
        }"
      >
        {{ Track.key.toUpperCase() + " " }}
      </div>
    </div>

    <div
      class="track-body"
      :style="{
        height: height + 'px',
        width: width - 4 + 'px',
        position: 'absolute',
        borderTop: '0px',
        borderBottom: '0px',
        borderRight: '2px solid rgba(244,244,244,1)',
        borderLeft: '2px solid rgba(244,244,244,1)',
        top: top + 'px',
        background: [isActive ? activeStyle : inactiveStyle],
      }"
    >
      <div v-for="Note in Track.notes" :key="Note">
        <Note
          :Note="Note"
          v-if="
            Global.currentTime > Note.timing - Global.remainingTime &&
              Global.currentTime < Note.timing + Global.lostTime &&
              !Note.judged
          "
          :left="0.5 * width"
          :Global="Global"
        />
      </div>
    </div>
    <div
      class="black-point"
      :style="{
        width: lengthForBlackPoint + 'px',
        position: 'absolute',
        left: 0.5 * width - offsetDiagonal - 0.5 + 'px',
        top: finalHeight - offsetDiagonal + 'px',
        margin: 'auto 0',
        height: lengthForBlackPoint + 'px',
        background: 'rgb(22, 22, 14)',
        transform: 'rotateZ(45deg)',
      }"
    ></div>
  </div>
</template>

<script>
import Note from "./Note";
export default {
  props: ["Track", "Global"],
  components: {
    Note,
  },
  data() {
    return {
      myTrack: this.Track,
      myGlobal: this.Global,
      lengthForBlackPoint: 15,
      refreshTime: 1000 / this.$store.state.refreshRate,
      widthPath: [],
      positionXPath: [],
      RGBPath: [],
      widthIndex: 0,
      positionXIndex: 0,
      RGBIndex: 0,
      opacity: 0.3,
      animationTime: 50,
      height: 0,
      top: 0,
      currentNote: 0,
      judgeFinished: false,
      boxShadowColor: "rgba(0,0,0,0.2)",
      boxShadowSize: 0,
    };
  },
  watch: {
    "Global.currentTime"() {
      if (this.Global.currentTime > 0) {
        this.myTrack.tempPositionX = this.getPositionX();
        this.myTrack.tempWidth = this.getWidth();
        this.myTrack.tempR = this.getRGB()[0];
        this.myTrack.tempG = this.getRGB()[1];
        this.myTrack.tempB = this.getRGB()[2];
        this.setHeightAndTop();
      }
      this.judge();
      if (this.boxShadowSize >= 1) {
        this.boxShadowSize += 1;
      }
      if (this.boxShadowSize > 40) {
        this.boxShadowSize = 0;
      }
    },
    "Track.moveOperations"(){
      this.generatePositionXPath();
    },
    "Track.changeWidthOperations"(){
      this.generateWidthPath();
    },
    "Track.changeColorOperations"(){
      this.generateRGBPath();
    },
  },
  created() {
    this.initiate();
  },
  computed: {
    boxShadow() {
      return (
        "0px 0px " +
        this.boxShadowSize +
        "px " +
        this.boxShadowSize +
        "px " +
        this.boxShadowColor
      );
    },
    isActive() {
      if (this.Track.type == 1) {
        var currentTime = this.Global.currentTime;
        var keyPressTime = this.Global.keyPressTime[
          this.Track.key.toUpperCase()
        ];
        var isHolding = this.Global.keyIsHold[this.Track.key.toUpperCase()];
        return (
          isHolding ||
          (currentTime - keyPressTime > 0 && currentTime - keyPressTime < 175)
        );
      } else return false;
    },
    lengthForKey() {
      if (this.Global.screenHeight * 0.1 > 50) {
        return 50;
      } else {
        return this.Global.screenHeight * 0.1;
      }
    },
    finalHeight() {
      return this.Global.screenHeight * this.Global.finalY;
    },
    width() {
      return 2 * this.Track.tempWidth * this.Global.screenWidth;
    },
    left() {
      return (
        (this.Track.tempPositionX - this.Track.tempWidth) *
        this.Global.screenWidth
      );
    },
    TrackColor() {
      return (
        "rgba(" +
        this.Track.tempR +
        "," +
        this.Track.tempG +
        "," +
        this.Track.tempB +
        "," +
        this.opacity +
        ")"
      );
    },
    TrackColorWithoutA() {
      return (
        "rgba(" +
        this.Track.tempR +
        "," +
        this.Track.tempG +
        "," +
        this.Track.tempB
      );
    },
    activeStyle() {
      return [
        "-webkit-linear-gradient(-90deg, " +
          this.TrackColorWithoutA +
          ",0.1)" +
          " 0, " +
          this.TrackColor +
          " 25%,  rgba(255,255,255,1) 100%)",
        " -moz-linear-gradient(180deg, " +
          this.TrackColorWithoutA +
          ",0.1)" +
          " 0, " +
          this.TrackColor +
          " 25%,  rgba(255,255,255,1) 100%)",
        "linear-gradient(180deg, " +
          this.TrackColorWithoutA +
          ",0.1)" +
          " 0, " +
          this.TrackColor +
          " 25%,  rgba(255,255,255,1) 100%)",
      ];
    },
    inactiveStyle() {
      return [
        "-webkit-linear-gradient(-90deg, " +
          this.TrackColorWithoutA +
          ",0.1)" +
          " 0, " +
          this.TrackColor +
          " 90%, rgba(255,255,255,1) 100%)",
        " -moz-linear-gradient(180deg, " +
          this.TrackColorWithoutA +
          ",0.1)" +
          " 0, " +
          this.TrackColor +
          " 90%, rgba(255,255,255,1) 100%)",
        "linear-gradient(180deg, " +
          this.TrackColorWithoutA +
          ",0.1)" +
          " 0, " +
          this.TrackColor +
          " 90%, rgba(255,255,255,1) 100%)",
      ];
    },
    offsetDiagonal() {
      return (
        Math.sqrt(this.lengthForBlackPoint * this.lengthForBlackPoint * 2) -
        this.lengthForBlackPoint
      );
    },
  },
  methods: {
    initiate() {
      this.setHeightAndTop();
      this.generateWidthPath();
      this.generatePositionXPath();
      this.generateRGBPath();
      this.myTrack.notes.sort(function(a, b) {
        return a.timing - b.timing;
      });
      this.myTrack.currentNote = 0;
      this.judgeFinished = false;
      this.myTrack.tempPositionX = this.getPositionX();
      this.myTrack.tempWidth = this.getWidth();
      this.myTrack.tempR = this.getRGB()[0];
      this.myTrack.tempG = this.getRGB()[1];
      this.myTrack.tempB = this.getRGB()[2];
    },
    judge() {
      if (this.Track.notes.length > 0 && !this.judgeFinished) {
        let currentKey = "";
        if (this.Track.type == 1) {
          currentKey = this.Track.key.toUpperCase();
        } else
          currentKey = this.Track.notes[
            this.myTrack.currentNote
          ].key.toUpperCase();
        let currentJudge = this.Global.keyPressTime[currentKey];
        let currentTime = this.Global.currentTime;
        let timing = this.Track.notes[this.myTrack.currentNote].timing;
        let pureTime = this.Global.pureTime;
        let farTime = this.Global.farTime;
        let lostTime = this.Global.lostTime;
        let isUsed = this.Global.keyUsed[currentKey];
        if (this.Track.notes[this.myTrack.currentNote].noteType == 1) {
          if (currentTime > timing - lostTime) {
            if (currentTime > timing + lostTime) {
              this.addCount({
                key: "lostCount",
                message: "因超时没有按到而判定为Lost",
                judgeTime: currentTime,
                timing: timing,
              });
              this.addNoteCount();
            } else if (this.Global.keyIsHold[currentKey]) {
              this.addCount({
                key: "pureCount",
                message: "pure",
                judgeTime: currentTime,
                timing: timing,
              });
              this.myTrack.notes[this.myTrack.currentNote].judged = true;
              this.$forceUpdate();
              this.addNoteCount();
            }
          }
        } else {
          if (currentTime > timing - lostTime) {
            if (currentTime > timing + lostTime) {
              this.addCount({
                key: "lostCount",
                message: "因超时没有按到而判定为Lost",
                judgeTime: currentTime,
                timing: timing,
              });
              this.addNoteCount();
            } else if (!isUsed) {
              if (
                currentJudge > timing - pureTime &&
                currentJudge < timing + pureTime
              ) {
                this.addCount({
                  key: "pureCount",
                  message: "pure",
                  judgeTime: currentJudge,
                  timing: timing,
                });
                this.myTrack.notes[this.myTrack.currentNote].judged = true;
                this.$forceUpdate();
                this.myGlobal.keyUsed[currentKey] = true;
                this.addNoteCount();
              } else if (
                currentJudge > timing - farTime &&
                currentJudge < timing + farTime
              ) {
                this.addCount({
                  key: "farCount",
                  message:
                    currentJudge < timing
                      ? "因为过早按下而判定为far(early)"
                      : "因为过晚按下而判定为far(late)",
                  judgeTime: currentJudge,
                  timing: timing,
                });
                this.myTrack.notes[this.myTrack.currentNote].judged = true;
                this.$forceUpdate();
                this.myGlobal.keyUsed[currentKey] = true;
                this.addNoteCount();
              } else if (
                currentJudge > timing - lostTime &&
                currentJudge < timing + lostTime
              ) {
                this.addCount({
                  key: "lostCount",
                  message:
                    currentJudge < timing
                      ? "因为过早按下而判定为lost(early)"
                      : "因为过晚按下而判定为lost(late)",
                  judgeTime: currentJudge,
                  timing: timing,
                });
                this.myTrack.notes[this.myTrack.currentNote].judged = true;
                this.$forceUpdate();
                this.myGlobal.keyUsed[currentKey] = true;
                this.addNoteCount();
              }
            }
          }
        }
      }
    },
    //主进程+1
    addCount(param) {
      if (param.key == "pureCount") {
        this.boxShadowColor = "rgba(234,161,86,0.6)";
        this.boxShadowSize = 1;
      } else if (param.key == "farCount") {
        this.boxShadowColor = "rgba(100,149,237,0.6)";
        this.boxShadowSize = 1;
      } else if (param.key == "lostCount") {
        this.boxShadowColor = "rgba(0,0,0,0.5)";
        if (!this.Global.isEdit) {
          this.boxShadowSize = 1;
        }
      }

      this.$emit("addCount", param);
    },
    //判定note+1
    addNoteCount() {
      if (this.myTrack.currentNote < this.Track.notes.length - 1) {
        this.myTrack.currentNote++;
      } else {
        this.judgeFinished = true;
      }
    },

    //生成轨道宽度路径，包含出场和退场的宽度动画
    generateWidthPath() {
      this.widthPath = [];
      this.widthIndex = 0;
      let length = this.myTrack.changeWidthOperations.length;
      let start = this.Track.startTiming;
      let end = start;
      if (length == 0) {
        end = this.Track.endTiming - this.animationTime;
      } else {
        this.myTrack.changeWidthOperations.sort(function(a, b) {
          return a.startTime - b.startTime;
        });
        end = this.myTrack.changeWidthOperations[0].startTime;
      }

      this.widthPath.push({
        type: 0,
        width: 0,
        startTime: 0,
        endTime: start,
      });
      let tempK1 = this.myTrack.width / this.animationTime;
      this.widthPath.push({
        type: 1,
        k: tempK1,
        b: -tempK1 * this.myTrack.startTiming,
        startTime: start,
        endTime: start + this.animationTime,
      });
      this.widthPath.push({
        type: 0,
        width: this.myTrack.width,
        startTime: start + this.animationTime,
        endTime: end,
      });

      for (let i = 0; i < length; i++) {
        let operation = this.myTrack.changeWidthOperations[i];
        start = operation.startTime;
        end = operation.endTime;
        if (operation.startTime != operation.endTime) {
          let k =
            (operation.endWidth - operation.startWidth) /
            (operation.endTime - operation.startTime);
          let b = operation.endWidth - k * operation.endTime;
          this.widthPath.push({
            type: 1,
            k: k,
            b: b,
            startTime: start,
            endTime: end,
          });
        }
        if (i != length - 1) {
          let nextOperation = this.myTrack.changeWidthOperations[i + 1];
          if (end < nextOperation.startTime) {
            this.widthPath.push({
              type: 0,
              width: operation.endWidth,
              startTime: end,
              endTime: nextOperation.startTime,
            });
          }
        } else {
          this.widthPath.push({
            type: 0,
            width: operation.endWidth,
            startTime: end,
            endTime: this.Track.endTiming - this.animationTime,
          });
          let tempK2 = -operation.endWidth / this.animationTime;
          this.widthPath.push({
            type: 1,
            k: tempK2,
            b: -tempK2 * this.myTrack.endTiming,
            startTime: this.Track.endTiming - this.animationTime,
            endTime: this.Track.endTiming,
          });
        }
      }
      if (length == 0) {
        let tempK3 = -this.myTrack.width / this.animationTime;
        this.widthPath.push({
          type: 1,
          k: tempK3,
          b: -tempK3 * this.myTrack.endTiming,
          startTime: this.Track.endTiming - this.animationTime,
          endTime: this.Track.endTiming,
        });
      }
    },
    //生成轨道位置路径
    generatePositionXPath() {
      this.positionXPath = [];
      this.positionXIndex = 0;
      let length = this.myTrack.moveOperations.length;
      let start = this.Track.startTiming;
      let end = start;
      if (length == 0) {
        end = this.Track.endTiming;
      } else {
        this.myTrack.moveOperations.sort(function(a, b) {
          return a.startTime - b.startTime;
        });
        end = this.myTrack.moveOperations[0].startTime;
      }
      this.positionXPath.push({
        type: 0,
        positionX: this.myTrack.positionX,
        startTime: 0,
        endTime: end,
      });
      for (let i = 0; i < length; i++) {
        let operation = this.myTrack.moveOperations[i];
        start = operation.startTime;
        end = operation.endTime;
        if (operation.startTime != operation.endTime) {
          let k =
            (operation.endX - operation.startX) /
            (operation.endTime - operation.startTime);
          let b = operation.endX - k * operation.endTime;
          this.positionXPath.push({
            type: 1,
            k: k,
            b: b,
            startTime: start,
            endTime: end,
          });
        }
        if (i != length - 1) {
          let nextOperation = this.myTrack.moveOperations[i + 1];
          if (end < nextOperation.startTime) {
            this.positionXPath.push({
              type: 0,
              positionX: operation.endX,
              startTime: end,
              endTime: nextOperation.startTime,
            });
          }
        } else {
          if (end < this.Track.endTiming) {
            this.positionXPath.push({
              type: 0,
              positionX: operation.endX,
              startTime: end,
              endTime: this.Track.endTiming,
            });
          }
        }
      }
    },

    //生成轨道路径
    generateRGBPath() {
      this.RGBPath = [];
      this.RGBIndex = 0;
      let length = this.myTrack.changeColorOperations.length;
      let start = this.Track.startTiming;
      let end = start;
      if (length == 0) {
        end = this.Track.endTiming;
      } else {
        this.myTrack.changeColorOperations.sort(function(a, b) {
          return a.startTime - b.startTime;
        });
        end = this.myTrack.changeColorOperations[0].startTime;
      }
      this.RGBPath.push({
        type: 0,
        R: this.myTrack.R,
        G: this.myTrack.G,
        B: this.myTrack.B,
        startTime: 0,
        endTime: end,
      });
      for (let i = 0; i < length; i++) {
        let operation = this.myTrack.changeColorOperations[i];
        start = operation.startTime;
        end = operation.endTime;
        if (operation.startTime != operation.endTime) {
          let Rk =
            (operation.endR - operation.startR) /
            (operation.endTime - operation.startTime);
          let Rb = operation.endR - Rk * operation.endTime;
          let Gk =
            (operation.endG - operation.startG) /
            (operation.endTime - operation.startTime);
          let Gb = operation.endG - Gk * operation.endTime;
          let Bk =
            (operation.endB - operation.startB) /
            (operation.endTime - operation.startTime);
          let Bb = operation.endB - Bk * operation.endTime;
          this.RGBPath.push({
            type: 1,
            Rk: Rk,
            Rb: Rb,
            Gk: Gk,
            Gb: Gb,
            Bk: Bk,
            Bb: Bb,
            startTime: start,
            endTime: end,
          });
        }
        if (i != length - 1) {
          let nextOperation = this.myTrack.changeColorOperations[i + 1];
          if (end < nextOperation.startTime) {
            this.RGBPath.push({
              type: 0,
              R: operation.endR,
              G: operation.endG,
              B: operation.endB,
              startTime: end,
              endTime: nextOperation.startTime,
            });
          }
        } else {
          if (end < this.Track.endTiming) {
            this.RGBPath.push({
              type: 0,
              R: operation.endR,
              G: operation.endG,
              B: operation.endB,
              startTime: end,
              endTime: this.Track.endTiming,
            });
          }
        }
      }
    },

    //获取当前index
    binaryGetCurrentIndex(currentTime, path) {
      let right = path.length - 1;
      if (right == 0) {
        return 0;
      }
      let left = 0;
      let mid = Math.floor((right + left) / 2);
      while (right > left) {
        if (
          path[mid].startTime <= currentTime &&
          path[mid].endTime >= currentTime
        ) {
          return mid;
        }
        if (path[mid].startTime > currentTime) {
          right = mid - 1;
        }
        if (path[mid].endTime < currentTime) {
          left = mid + 1;
        }
        mid = Math.floor((right + left) / 2);
      }
      return mid;
    },

    //获取当前位置
    getPositionX() {
      var currentTime = this.Global.currentTime;
      var currentX = this.positionXPath[this.positionXIndex];
      if (
        !(currentTime <= currentX.endTime && currentTime >= currentX.startTime)
      ) {
        this.positionXIndex = this.binaryGetCurrentIndex(
          currentTime,
          this.positionXPath
        );
        currentX = this.positionXPath[this.positionXIndex];
      }
      if (currentX.type == 0) {
        return currentX.positionX;
      }
      if (currentX.type == 1) {
        return currentX.k * currentTime + currentX.b;
      }
    },

    //获取当前宽度
    getWidth() {
      var currentTime = this.Global.currentTime;
      var currentWidth = this.widthPath[this.widthIndex];
      if (
        !(
          currentTime <= currentWidth.endTime &&
          currentTime >= currentWidth.startTime
        )
      ) {
        this.widthIndex = this.binaryGetCurrentIndex(
          currentTime,
          this.widthPath
        );
        currentWidth = this.widthPath[this.widthIndex];
      }
      if (currentWidth.type == 0) {
        return currentWidth.width;
      }
      if (currentWidth.type == 1) {
        return currentWidth.k * currentTime + currentWidth.b;
      }
    },

    //获取当前颜色
    getRGB() {
      var currentTime = this.Global.currentTime;
      var currentRGB = this.RGBPath[this.RGBIndex];
      if (
        !(
          currentTime <= currentRGB.endTime &&
          currentTime >= currentRGB.startTime
        )
      ) {
        this.RGBIndex = this.binaryGetCurrentIndex(currentTime, this.RGBPath);
        currentRGB = this.RGBPath[this.RGBIndex];
      }
      if (currentRGB.type == 0) {
        return [currentRGB.R, currentRGB.G, currentRGB.B];
      }
      if (currentRGB.type == 1) {
        return [
          currentRGB.Rk * currentTime + currentRGB.Rb,
          currentRGB.Gk * currentTime + currentRGB.Gb,
          currentRGB.Bk * currentTime + currentRGB.Bb,
        ];
      }
    },

    //轨道出场退场的高度动画
    setHeightAndTop() {
      let k = this.Global.finalY / this.animationTime;
      if (
        this.Global.currentTime <
        this.myTrack.startTiming + this.animationTime
      ) {
        this.top =
          this.finalHeight -
          (k * this.Global.currentTime - k * this.myTrack.startTiming) *
            this.Global.screenHeight;
      } else if (
        this.Global.currentTime >
        this.myTrack.endTiming - this.animationTime
      ) {
        this.top =
          this.finalHeight -
          (-k * this.Global.currentTime + k * this.myTrack.endTiming) *
            this.Global.screenHeight;
      } else {
        this.top = 0;
      }
      this.height = this.finalHeight - this.top;
    },
    log() {
      console.log(this.myTrack.tempWidth);
    },
  },
};
</script>

<style scope>
.select {
  -webkit-user-select: none;

  -moz-user-select: none;

  -ms-user-select: none;

  user-select: none;
}
</style>
