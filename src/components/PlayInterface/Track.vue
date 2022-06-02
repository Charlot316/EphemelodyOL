<template>
  <div>
    <div v-for="judge in myTrack.judges" :key="judge">
      <judge-painter :middle="middle" :Y="Y" :global="global" :judge="judge" />
    </div>
  </div>
</template>

<script>
import JudgePainter from "./JudgePainter";
export default {
  props: ["Track", "global"],
  components: {
    JudgePainter,
  },
  data() {
    return {
      myTrack: this.Track,
      myglobal: this.global,
      lengthForBlackPoint: 10,
      widthPath: [],
      positionXPath: [],
      RGBPath: [],
      widthIndex: 0,
      positionXIndex: 0,
      RGBIndex: 0,
      opacity: 0.3,
      animationTime: 100,
      height: 0,
      top: 0,
      blackLength: 28,
      pinkLength: 20,
      whiteLength: 15,
      mirrorOpacity: 0.1,
      isJudgingHold: false,
      tempJudge: {},
    };
  },
  watch: {
    "global.currentTime"() {
      if (this.global.currentTime > 0) {
        this.myTrack.tempPositionX = this.getPositionX();
        this.myTrack.tempWidth = this.getWidth();
        var RGB = this.getRGB();
        this.myTrack.tempR = RGB[0];
        this.myTrack.tempG = RGB[1];
        this.myTrack.tempB = RGB[2];
        this.setHeightAndTop();
        while (
          this.myTrack.judges.length > 0 &&
          this.myTrack.judges[0].timing + this.judgeAnimationTime <
            this.global.currentTime
        ) {
          this.myTrack.judges = this.myTrack.judges.slice(1);
        }
        this.paintTrack();
      }
      while (
        this.myTrack.lastNote < this.myTrack.notes.length - 1 &&
        this.global.currentTime >
          this.myTrack.notes[this.myTrack.lastNote + 1].timing -
            this.global.remainingTime
      ) {
        this.myTrack.lastNote++;
      }
      this.judge();
    },
    "global.screenHeight"() {
      this.setHeightAndTop();
      this.paintTrack();
    },
    "global.screenWidth"() {
      this.paintTrack();
    },
    "global.repaint"() {
      this.paintTrack();
    },
    "global.reCalculateTrack"() {
      this.setIndex();
      this.generatePositionXPath();
      this.myTrack.tempPositionX = this.getPositionX();
      this.generateWidthPath();
      this.myTrack.tempWidth = this.getWidth();
      this.generateRGBPath();
      var RGB = this.getRGB();
      this.myTrack.tempR = RGB[0];
      this.myTrack.tempG = RGB[1];
      this.myTrack.tempB = RGB[2];
      this.myTrack.notes.sort(function(a, b) {
        return a.timing - b.timing;
      });
      this.paintTrack();
    },
  },
  created() {
    this.initiate();
  },
  computed: {
    isActive() {
      if (this.Track.type == 1) {
        var currentTime = this.global.currentTime;
        var keyPressTime = this.global.keyPressTime[
          this.Track.key.toUpperCase()
        ];
        var isHolding = this.global.keyIsHold[this.Track.key.toUpperCase()];
        return (
          isHolding ||
          (currentTime - keyPressTime > 0 && currentTime - keyPressTime < 175)
        );
      } else return false;
    },
    lengthForKey() {
      if (this.global.screenHeight * 0.1 > 30) {
        return 30;
      } else {
        return this.global.screenHeight * 0.1;
      }
    },
    finalHeight() {
      return this.global.screenHeight * this.global.finalY;
    },
    width() {
      return 2 * this.Track.tempWidth * this.global.screenWidth;
    },
    halfWidth() {
      return this.Track.tempWidth * this.global.screenWidth;
    },
    left() {
      return (
        (this.Track.tempPositionX - this.Track.tempWidth) *
        this.global.screenWidth
      );
    },
    middle() {
      return this.Track.tempPositionX * this.global.screenWidth;
    },
    Y() {
      return this.global.finalY * this.global.screenHeight;
    },
  },
  methods: {
    //画轨道
    async paintTrack() {
      //画判定结果
      await this.paintNotes();
      var painter = this.global.trackPainter;
      if (this.width > 4 && this.height > 0) {
        var longerThanScreen = this.height > this.global.screenHeight - this.Y;
        //填充长方形主体
        painter.beginPath();
        painter.rect(this.left + 2, this.top, this.width - 4, this.height);
        painter.fillStyle =
          "rgba(" +
          this.Track.tempR +
          "," +
          this.Track.tempG +
          "," +
          this.Track.tempB +
          "," +
          this.opacity +
          ")";
        painter.fill();
        if (this.isActive) {
          painter.fillStyle = "rgba(255,255,255,0.4)";
          painter.fill();
        }

        painter.beginPath();
        painter.rect(
          this.left + 2,
          this.Y,
          this.width - 4,
          longerThanScreen ? this.global.screenHeight - this.Y : this.height
        );
        painter.fillStyle =
          "rgba(" +
          this.Track.tempR +
          "," +
          this.Track.tempG +
          "," +
          this.Track.tempB +
          "," +
          0.1 +
          ")";
        painter.fill();
        if (this.isActive) {
          painter.fillStyle = "rgba(255,255,255,0.2)";
          painter.fill();
        }
        //画左线
        painter.beginPath();
        painter.moveTo(this.left, this.top);
        painter.lineTo(this.left, this.Y);
        painter.strokeStyle = "rgba(255,255,255,0.8)";
        painter.lineWidth = 2;
        painter.stroke();

        painter.beginPath();
        painter.moveTo(this.left, this.Y);
        painter.lineTo(
          this.left,
          longerThanScreen ? this.global.screenHeight : this.Y + this.height
        );
        painter.strokeStyle = "rgba(255,255,255,0.1)";
        painter.lineWidth = 2;
        painter.stroke();
        //画右线
        painter.beginPath();
        painter.moveTo(this.left + this.width, this.top);
        painter.lineTo(this.left + this.width, this.Y);
        painter.strokeStyle = "rgba(255,255,255,0.8)";
        painter.lineWidth = 2;
        painter.stroke();

        painter.beginPath();
        painter.moveTo(this.left + this.width, this.Y);
        painter.lineTo(
          this.left + this.width,
          longerThanScreen ? this.global.screenHeight : this.Y + this.height
        );
        painter.strokeStyle = "rgba(255,255,255,0.1)";
        painter.lineWidth = 2;
        painter.stroke();

        //画中线
        painter.beginPath();
        painter.moveTo(this.middle, this.top);
        painter.lineTo(this.middle, this.Y);
        painter.strokeStyle = "rgba(0,0,0,0.3)";
        painter.lineWidth = 1;
        painter.stroke();

        painter.beginPath();
        painter.moveTo(this.middle, this.Y);
        painter.lineTo(
          this.middle,
          longerThanScreen ? this.global.screenHeight : this.Y + this.height
        );
        painter.strokeStyle = "rgba(0,0,0,0.05)";
        painter.lineWidth = 1;
        painter.stroke();

        //画小黑方块
        painter.beginPath();
        painter.moveTo(this.middle, this.Y - this.lengthForBlackPoint);
        painter.lineTo(this.middle + this.lengthForBlackPoint, this.Y);
        painter.lineTo(this.middle, this.Y + this.lengthForBlackPoint);
        painter.lineTo(this.middle - this.lengthForBlackPoint, this.Y);
        painter.lineTo(this.middle, this.Y - this.lengthForBlackPoint);
        painter.closePath();
        painter.fillStyle = "rgb(22, 22, 14)";
        painter.fill();

        if (this.Track.type == 1) {
          //画key
          var keyY = (this.Y * 9) / 8;
          painter.beginPath();
          painter.moveTo(this.middle, keyY - this.lengthForKey);
          painter.lineTo(this.middle + this.lengthForKey, keyY);
          painter.lineTo(this.middle, keyY + this.lengthForKey);
          painter.lineTo(this.middle - this.lengthForKey, keyY);
          painter.lineTo(this.middle, keyY - this.lengthForKey);
          painter.closePath();
          painter.strokeStyle = "rgb(255,255,255)";
          painter.lineWidth = 1;
          painter.stroke();
          if (this.isActive) {
            painter.fillStyle = "rgba(255,255,255,0.4)";
            painter.fill();
          }
          painter.fillStyle = "rgba(255, 255, 255,0.2)";
          painter.fill();
          painter.beginPath();
          painter.font = "" + this.lengthForKey + "px Arial";
          painter.shadowColor = "rgba(0, 0, 0, 1)";
          painter.shadowBlur = 2;
          painter.fillStyle = "rgba(255, 255, 255,1)";
          painter.textAlign = "center";
          painter.textBaseline = "middle";
          painter.fillText(this.myTrack.key.toUpperCase(), this.middle, keyY);
          painter.shadowBlur = 0;
        }
      }
    },

    //画所有note
    paintNotes() {
      if (!this.myTrack.judgeFinished) {
        for (
          var i = this.myTrack.lastNote;
          i >= this.myTrack.currentNote;
          i--
        ) {
          this.paintNote(this.myTrack.notes[i]);
        }
      }
    },

    //画单个note
    paintNote(note) {
      var painter = this.global.notePainter;
      var currentTime = this.global.currentTime;
      var y =
        ((this.global.finalY / this.global.remainingTime) * currentTime -
          (this.global.finalY / this.global.remainingTime) *
            (note.timing - this.global.remainingTime)) *
        this.global.screenHeight;
      var canMirror =
        (note.noteType != 1 &&
          y / this.global.screenHeight >= 0.6 &&
          y / this.global.screenHeight < 0.8) ||
        (note.noteType == 1 && y / this.global.screenHeight > 0.6);
      if (note.noteType == 0) {
        if (canMirror) {
          var tempY = 2 * this.Y - y;
          painter.beginPath();
          painter.moveTo(this.middle, tempY - this.blackLength);
          painter.lineTo(this.middle + this.blackLength, tempY);
          painter.lineTo(this.middle, tempY + this.blackLength);
          painter.lineTo(this.middle - this.blackLength, tempY);
          painter.lineTo(this.middle, tempY - this.blackLength);
          painter.closePath();
          painter.fillStyle = "rgba(22, 22, 14,0.1)";
          painter.fill();
          painter.beginPath();
          painter.moveTo(this.middle, tempY - this.pinkLength);
          painter.lineTo(this.middle + this.pinkLength, tempY);
          painter.lineTo(this.middle, tempY + this.pinkLength);
          painter.lineTo(this.middle - this.pinkLength, tempY);
          painter.lineTo(this.middle, tempY - this.pinkLength);
          painter.closePath();
          painter.globalCompositeOperation = "destination-out";
          painter.fillStyle = "rgba(203, 105, 121,1)";
          painter.fill();
          painter.globalCompositeOperation = "source-over";
          painter.fillStyle = "rgba(203, 105, 121,0.1)";
          painter.fill();
        }
        painter.beginPath();
        painter.moveTo(this.middle, y - this.blackLength);
        painter.lineTo(this.middle + this.blackLength, y);
        painter.lineTo(this.middle, y + this.blackLength);
        painter.lineTo(this.middle - this.blackLength, y);
        painter.lineTo(this.middle, y - this.blackLength);
        painter.closePath();
        painter.fillStyle = "rgb(22, 22, 14)";
        painter.fill();
        painter.beginPath();
        painter.moveTo(this.middle, y - this.pinkLength);
        painter.lineTo(this.middle + this.pinkLength, y);
        painter.lineTo(this.middle, y + this.pinkLength);
        painter.lineTo(this.middle - this.pinkLength, y);
        painter.lineTo(this.middle, y - this.pinkLength);
        painter.closePath();
        painter.fillStyle = "rgb(203, 105, 121)";
        painter.fill();
      } else if (note.noteType == 1) {
        var keyLength = 0;
        if (currentTime < note.timing)
          keyLength =
            ((note.endTiming - note.timing) / this.global.remainingTime) *
            this.global.finalY *
            this.global.screenHeight;
        else if (currentTime < note.endTiming) {
          keyLength =
            ((note.endTiming - currentTime) / this.global.remainingTime) *
            this.global.finalY *
            this.global.screenHeight;
          y = this.global.finalY * this.global.screenHeight;
        } else keyLength = 0;
        if (canMirror) {
          tempY = 2 * this.Y - y;
          //黑色部分
          painter.beginPath();
          painter.moveTo(this.middle + this.blackLength, tempY);
          painter.lineTo(this.middle + this.blackLength, tempY + keyLength);
          painter.lineTo(this.middle, tempY + keyLength + this.blackLength);
          painter.lineTo(this.middle - this.blackLength, tempY + keyLength);
          painter.lineTo(this.middle - this.blackLength, tempY);
          painter.lineTo(this.middle, tempY - this.blackLength);
          painter.closePath();
          painter.fillStyle = "rgba(22, 22, 14,0.1)";
          painter.fill();
          //上粉块
          painter.beginPath();
          painter.moveTo(this.middle + this.pinkLength, tempY);
          painter.lineTo(this.middle, tempY + this.pinkLength);
          painter.lineTo(this.middle - this.pinkLength, tempY);
          painter.lineTo(this.middle, tempY - this.pinkLength);
          painter.closePath();
          painter.globalCompositeOperation = "destination-out";
          painter.fillStyle = "rgba(203, 105, 121,1)";
          painter.fill();
          painter.globalCompositeOperation = "source-over";
          painter.fillStyle = "rgba(203, 105, 121,0.1)";
          painter.fill();
          //下粉块
          painter.beginPath();
          painter.moveTo(this.middle + this.pinkLength, tempY + keyLength);
          painter.lineTo(this.middle, tempY + keyLength + this.pinkLength);
          painter.lineTo(this.middle - this.pinkLength, tempY + keyLength);
          painter.lineTo(this.middle, tempY + keyLength - this.pinkLength);
          painter.closePath();
          painter.globalCompositeOperation = "destination-out";
          painter.fillStyle = "rgba(203, 105, 121,1)";
          painter.fill();
          painter.globalCompositeOperation = "source-over";
          painter.fillStyle = "rgba(203, 105, 121,0.1)";
          painter.fill();
        }
        painter.beginPath();
        painter.moveTo(this.middle + this.blackLength, y);
        painter.lineTo(this.middle + this.blackLength, y - keyLength);
        painter.lineTo(this.middle, y - keyLength - this.blackLength);
        painter.lineTo(this.middle - this.blackLength, y - keyLength);
        painter.lineTo(this.middle - this.blackLength, y);
        painter.lineTo(this.middle, y + this.blackLength);
        painter.closePath();
        painter.fillStyle = "rgb(22, 22, 14)";
        painter.fill();
        //下粉块
        painter.beginPath();
        painter.moveTo(this.middle + this.pinkLength, y);
        painter.lineTo(this.middle, y + this.pinkLength);
        painter.lineTo(this.middle - this.pinkLength, y);
        painter.lineTo(this.middle, y - this.pinkLength);
        painter.closePath();
        painter.fillStyle = "rgb(203, 105, 121)";
        painter.fill();
        //上粉块
        painter.beginPath();
        painter.moveTo(this.middle + this.pinkLength, y - keyLength);
        painter.lineTo(this.middle, y - keyLength + this.pinkLength);
        painter.lineTo(this.middle - this.pinkLength, y - keyLength);
        painter.lineTo(this.middle, y - keyLength - this.pinkLength);
        painter.closePath();
        painter.fillStyle = "rgb(203, 105, 121)";
        painter.fill();
      } else if (note.noteType == 2) {
        if (canMirror) {
          tempY = 2 * this.Y - y;
          painter.beginPath();
          painter.moveTo(this.middle, tempY - this.whiteLength);
          painter.lineTo(this.middle + this.whiteLength, tempY);
          painter.lineTo(this.middle, tempY + this.whiteLength);
          painter.lineTo(this.middle - this.whiteLength, tempY);
          painter.lineTo(this.middle, tempY - this.whiteLength);
          painter.closePath();
          painter.fillStyle = "rgba(255,255,255,0.3)";
          painter.fill();
          painter.strokeStyle = "rgba(0,0,0,0.3)";
          painter.lineWidth = 1;
          painter.stroke();
        }
        painter.beginPath();
        painter.moveTo(this.middle, y - this.whiteLength);
        painter.lineTo(this.middle + this.whiteLength, y);
        painter.lineTo(this.middle, y + this.whiteLength);
        painter.lineTo(this.middle - this.whiteLength, y);
        painter.lineTo(this.middle, y - this.whiteLength);
        painter.closePath();
        painter.fillStyle = "rgb(255,255,255)";
        painter.fill();
        painter.strokeStyle = "rgb(0,0,0)";
        painter.lineWidth = 1;
        painter.stroke();
      }
    },

    //初始化
    initiate() {
      this.setHeightAndTop();
      this.setIndex();
      this.generateWidthPath();
      this.generatePositionXPath();
      this.generateRGBPath();
      this.myTrack.notes.sort(function(a, b) {
        return a.timing - b.timing;
      });
      var track = this.myTrack;
      var index = 0;
      var last = track.notes.length;
      for (var j = track.notes.length - 1; j >= 0; j--) {
        track.notes[j].judged = false;
        if (track.notes[j].timing > this.global.currentTime) {
          index = j;
        }
        if (
          this.global.currentTime <
          track.notes[j].timing - this.global.remainingTime
        ) {
          last = j;
        }
      }
      track.currentNote = index;
      track.lastNote = last - 1;
      this.myTrack.judges = [];
      this.myTrack.tempPositionX = this.getPositionX();
      this.myTrack.tempWidth = this.getWidth();
      this.myTrack.tempR = this.getRGB()[0];
      this.myTrack.tempG = this.getRGB()[1];
      this.myTrack.tempB = this.getRGB()[2];
      this.paintTrack();
    },

    //判定
    judge() {
      if (this.Track.notes.length > 0 && !this.myTrack.judgeFinished) {
        let currentKey = "";
        if (this.Track.type == 1) {
          currentKey = this.Track.key.toUpperCase();
        } else
          currentKey = this.Track.notes[
            this.myTrack.currentNote
          ].key.toUpperCase();
        let currentJudge = this.global.keyPressTime[currentKey];
        let currentTime = this.global.currentTime;
        let timing = this.Track.notes[this.myTrack.currentNote].timing;
        let pureTime = this.global.pureTime;
        let farTime = this.global.farTime;
        let lostTime = this.global.lostTime;
        let isUsed = this.global.keyUsed[currentKey];
        let isHold = this.global.keyIsHold[currentKey];
        let note = this.Track.notes[this.myTrack.currentNote];
        if (this.isJudgingHold) {
          if (currentTime > note.endTiming) {
            this.isJudgingHold = false;
            this.addCount(this.tempJudge);
            this.addNoteCount();
          } else if (!isHold) {
            this.isJudgingHold = false;
            this.addNoteCount();
            if (currentTime > note.endTiming - farTime) {
              this.addCount(this.tempJudge);
            } else {
              this.addCount({
                type: "lost",
                key: "lostCount",
                message: "因提前释放长键判定为Lost",
                judgeTime: currentTime,
                timing: note.endTiming,
              });
            }
          }
        } else {
          if (note.noteType == 2) {
            if (currentTime > timing - lostTime) {
              if (currentTime > timing + farTime) {
                this.addCount({
                  type: "lost",
                  key: "lostCount",
                  message: "因超时没有按到而判定为Lost",
                  judgeTime: currentTime,
                  timing: timing,
                });
                this.addNoteCount();
              } else if (this.global.keyIsHold[currentKey]) {
                this.addCount({
                  type: "pure",
                  key: "pureCount",
                  message: "pure",
                  judgeTime: currentTime,
                  timing: timing,
                });
                this.$forceUpdate();
                this.addNoteCount();
              }
            }
          } else if (note.noteType == 0) {
            if (currentTime > timing - lostTime) {
              if (currentTime > timing + lostTime) {
                this.addCount({
                  type: "lost",
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
                    type: "pure",
                    key: "pureCount",
                    message: "pure",
                    judgeTime: currentJudge,
                    timing: timing,
                  });
                  this.$forceUpdate();
                  this.myglobal.keyUsed[currentKey] = true;
                  this.addNoteCount();
                } else if (
                  currentJudge > timing - farTime &&
                  currentJudge < timing + farTime
                ) {
                  this.addCount({
                    type: "far",
                    key: "farCount",
                    message:
                      currentJudge < timing
                        ? "因为过早按下而判定为far(early)"
                        : "因为过晚按下而判定为far(late)",
                    judgeTime: currentJudge,
                    timing: timing,
                  });
                  this.$forceUpdate();
                  this.myglobal.keyUsed[currentKey] = true;
                  this.addNoteCount();
                } else if (
                  currentJudge > timing - lostTime &&
                  currentJudge < timing + lostTime
                ) {
                  this.addCount({
                    type: "lost",
                    key: "lostCount",
                    message:
                      currentJudge < timing
                        ? "因为过早按下而判定为lost(early)"
                        : "因为过晚按下而判定为lost(late)",
                    judgeTime: currentJudge,
                    timing: timing,
                  });
                  this.$forceUpdate();
                  this.myglobal.keyUsed[currentKey] = true;
                  this.addNoteCount();
                }
              }
            }
          } else {
            if (currentTime > timing - lostTime) {
              if (currentTime > timing + lostTime) {
                this.addCount({
                  type: "lost",
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
                  this.tempJudge = {
                    type: "pure",
                    key: "pureCount",
                    message: "pure",
                    judgeTime: currentJudge,
                    timing: note.endTiming,
                  };
                  this.myTrack.judges.push({
                    type: "pure",
                    key: "pureCount",
                    message: "pure",
                    judgeTime: currentJudge,
                    timing: timing,
                  });
                  this.myglobal.keyUsed[currentKey] = true;
                  this.isJudgingHold = true;
                } else if (
                  currentJudge > timing - farTime &&
                  currentJudge < timing + farTime
                ) {
                  this.tempJudge = {
                    type: "far",
                    key: "farCount",
                    message:
                      currentJudge < timing
                        ? "因为过早按下而判定为far(early)"
                        : "因为过晚按下而判定为far(late)",
                    judgeTime: currentJudge,
                    timing: note.endTiming,
                  };
                  this.myTrack.judges.push({
                    type: "far",
                    key: "farCount",
                    message:
                      currentJudge < timing
                        ? "因为过早按下而判定为far(early)"
                        : "因为过晚按下而判定为far(late)",
                    judgeTime: currentJudge,
                    timing: timing,
                  });
                  this.myglobal.keyUsed[currentKey] = true;
                  this.isJudgingHold = true;
                } else if (
                  currentJudge > timing - lostTime &&
                  currentJudge < timing + lostTime
                ) {
                  this.addCount({
                    type: "lost",
                    key: "lostCount",
                    message:
                      currentJudge < timing
                        ? "因为过早按下而判定为lost(early)"
                        : "因为过晚按下而判定为lost(late)",
                    judgeTime: currentJudge,
                    timing: timing,
                  });
                  this.$forceUpdate();
                  this.myglobal.keyUsed[currentKey] = true;
                  this.addNoteCount();
                }
              }
            }
          }
        }
      }
    },

    //主进程+1
    addCount(param) {
      if (param.type != "lost") {
        this.myTrack.judges.push(param);
      }
      this.$emit("addCount", param);
    },

    //判定note+1
    addNoteCount() {
      if (this.myTrack.currentNote < this.Track.notes.length - 1) {
        this.myTrack.currentNote++;
      } else {
        this.myTrack.judgeFinished = true;
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

      this.widthPath.push({
        type: 0,
        width: this.myTrack.width,
        startTime: start,
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
            endTime: this.Track.endTiming,
          });
        }
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
      var currentTime = this.global.currentTime;
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
      var currentTime = this.global.currentTime;
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
      var currentTime = this.global.currentTime;
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
      let k = this.global.finalY / this.animationTime;
      if (
        this.global.currentTime <
        this.myTrack.startTiming + this.animationTime
      ) {
        this.top =
          this.finalHeight -
          (k * this.global.currentTime - k * this.myTrack.startTiming) *
            this.global.screenHeight;
      } else if (
        this.global.currentTime >
        this.myTrack.endTiming - this.animationTime
      ) {
        this.top =
          this.finalHeight -
          (-k * this.global.currentTime + k * this.myTrack.endTiming) *
            this.global.screenHeight;
      } else {
        this.top = 0;
      }
      this.height = this.finalHeight - this.top;
    },
    setIndex() {
      for (var i = 0; i < this.myTrack.moveOperations.length; i++) {
        this.myTrack.moveOperations[i].index = i;
      }
      for (i = 0; i < this.myTrack.changeWidthOperations.length; i++) {
        this.myTrack.changeWidthOperations[i].index = i;
      }
      for (i = 0; i < this.myTrack.changeColorOperations.length; i++) {
        this.myTrack.changeColorOperations[i].index = i;
      }
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
