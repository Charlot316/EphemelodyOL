<template>
  <div
    class="track-slice"
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
        top: '0px',
        height: height + 'px',
        background: 'rgba(0,0,0,0.3)',
      }"
    ></div>
    <div
      class="black-point"
      :style="{
        width: lengthForBlackPoint + 'px',
        position: 'absolute',
        left: 0.5 * width - offsetDiagonal - 1.2 + 'px',
        top: height - offsetDiagonal + 'px',
        margin: 'auto 0',
        height: lengthForBlackPoint + 'px',
        background: 'rgb(0,0,0)',
        transform: 'rotateZ(45deg)',
      }"
    ></div>
    <div
      class="track-body"
      @click="isActive = !isActive"
      :style="{
        height: height + 'px',
        width: width - 4 + 'px',
        margin: 'auto 0',
        borderTop: '0px',
        borderBottom: '0px',
        borderRight: '2px solid rgba(255,255,255,1)',
        borderLeft: '2px solid rgba(255,255,255,1)',
        top: 0,
        background: [
          isActive
            ? [
                'linear-gradient(0deg, rgba(255,255,255,0.8) 0, rgba(' +
                  Track.tempR +
                  ',' +
                  Track.tempG +
                  ',' +
                  Track.tempB +
                  ',0.8) 100%)',
                '-moz-linear-gradient(0deg, rgba(255,255,255,0.8) 0, rgba(' +
                  Track.tempR +
                  ',' +
                  Track.tempG +
                  ',' +
                  Track.tempB +
                  ',0.8) 100%)',
                '-webkit-linear-gradient(90deg, rgba(255,255,255,0.8) 0, rgba(' +
                  Track.tempR +
                  ',' +
                  Track.tempG +
                  ',' +
                  Track.tempB +
                  ',0.8) 100%)',
              ]
            : 'rgba(' +
              Track.tempR +
              ',' +
              Track.tempG +
              ',' +
              Track.tempB +
              ',0.8)',
        ],
      }"
    >
      <div v-for="Note in Track.notes" :key="Note">
        <Note
          :Note="Note"
          v-if="
            Global.currentTime > Note.timing - Global.remainingTime &&
              Global.currentTime < Note.timing + Global.lostTime
          "
          :left="0.5 * width"
          :Global="Global"
        />
      </div>
    </div>
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
      isActive: false,
      lengthForBlackPoint: 15,
      refreshTime: 1000 / this.$store.state.refreshRate,
      widthPath: [],
      positionXPath: [],
      RGBPath: [],
      widthIndex: 0,
      positionXIndex: 0,
      RGBIndex: 0,
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
      }
    },
  },
  created() {
    this.initiate();
  },
  computed: {
    height() {
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
    offsetDiagonal() {
      return (
        Math.sqrt(this.lengthForBlackPoint * this.lengthForBlackPoint * 2) -
        this.lengthForBlackPoint
      );
    },
  },
  methods: {
    initiate() {
      this.myTrack.tempPositionX = this.Track.positionX;
      this.myTrack.tempWidth = this.Track.width;
      this.myTrack.tempR = this.Track.R;
      this.myTrack.tempG = this.Track.G;
      this.myTrack.tempB = this.Track.B;
      this.generateWidthPath();
      this.generatePositionXPath();
      this.generateRGBPath();
    },
    generateWidthPath() {
      let length = this.myTrack.changeWidthOperations.length;
      let start = this.Track.startTiming;
      let end = start;
      if (length == 0) {
        end = this.Track.endTiming;
      } else {
        this.myTrack.changeWidthOperations.sort(function(a, b) {
          return a.startTime - b.startTime;
        });
        end = this.myTrack.changeWidthOperations[0].startTime;
      }
      this.widthPath.push({
        type: 0,
        width: this.myTrack.width,
        startTime: 0,
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
          if (end < this.Track.endTiming) {
            this.widthPath.push({
              type: 0,
              width: operation.endWidth,
              startTime: end,
              endTime: this.Track.endTiming,
            });
          }
        }
      }
    },
    generatePositionXPath() {
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
    generateRGBPath() {
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
    returnResult(path, currentTime) {
      let index = this.binaryGetCurrentIndex(currentTime, path);
      if (path[index].type == 0) {
        return path[index].value;
      } else {
        let k = path[index].k;
        let b = path[index].b;
        return k * currentTime + b;
      }
    },
  },
};
</script>

<style scope></style>
