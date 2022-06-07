<template>
  <div
    :class="track.edit ? 'panel-edit' : 'panel-no-edit'"
    :style="{
      width:
        (chart.songLength / displayAreaTime) * (global.documentWidth - 300) +
        'px',
    }"
  >
    <div class="track-tracks">
      <div
        @dblclick="newNote"
        :class="track.edit ? 'note-track-edit' : 'note-track'"
        :style="{
          width:
            (chart.songLength / displayAreaTime) *
              (global.documentWidth - 300) +
            'px',
        }"
      >
        <div class="track-range">
          <div>
            <div
              :style="{
                position: 'absolute',
                left:
                  (myTrack.startTiming / displayAreaTime) *
                    (global.documentWidth - 300) +
                  'px',
                top: 0,
                height: '80px',
                width:
                  '1px',
                background: 'rgb(255,255,255)',
              }"
            >
              <div
                @mousedown="longOperationCanMove"
                :style="{
                  userSelect: 'none',
                  height: '80px',
                  position: 'absolute',
                  background: 'rgb(70, 70, 70)',
                  cursor: 'move',
                  width:
                    ((myTrack.endTiming - myTrack.startTiming) / displayAreaTime) *
                      (global.documentWidth - 300) +
                    'px',
                  left: '-1px',
                  top: '1px',
                  overflow: 'hidden',
                  lineHeight: '40px',
                  fontSize: '20px',
                  border: '0px solid #fff',
                  borderLeftWidth: '1px',
                  borderRightWidth: '1px',
                }"
              ></div>
              <div
                @mousedown="leftMove = true"
                style="width:1px;height:80px;position:absolute;left:0px;top:0;cursor:w-resize;"
                src="http://pic.mcatk.com/charlot-pictures/EpheHitOperation.png"
              />
              <div
                @mousedown="rightMove = true"
                :style="{
                  userSelect: 'none',
                  height: '80px',
                  width: '1px',
                  position: 'absolute',
                  cursor: 'e-resize',
                  left:
                    ((myTrack.endTiming - myTrack.startTiming) / displayAreaTime) *
                      (global.documentWidth - 300) +
                    1 +
                    'px',
                  top: '0px',
                }"
                src="http://pic.mcatk.com/charlot-pictures/EpheHitOperation.png"
              />
            </div>
          </div>
        </div>
        <div v-for="note in track.notes" :key="note">
          <note
            :currentNoteType="currentNoteType"
            :track="track"
            :global="global"
            :chart="chart"
            :note="note"
            :enableEdit="enableEdit"
            :displayAreaTime="displayAreaTime"
          />
        </div>
      </div>
      <transition
        name="fade"
        enter-active-class="animate__animated animate__fadeInDown"
        leave-active-class="animate__animated animate__fadeOutUp"
      >
        <div v-if="track.edit">
          <div
            @dblclick="newMoveOperations"
            class="positionX-track"
            :style="{
              width:
                (chart.songLength / displayAreaTime) *
                  (global.documentWidth - 300) +
                'px',
            }"
          >
            <div v-for="operation in track.moveOperations" :key="operation">
              <MoveOperation
                :currentNoteType="currentNoteType"
                :track="track"
                :global="global"
                :chart="chart"
                :operation="operation"
                :enableEdit="enableEdit"
                :displayAreaTime="displayAreaTime"
              />
            </div>
          </div>
          <div
            @dblclick="newWidthOperations"
            class="width-track"
            :style="{
              width:
                (chart.songLength / displayAreaTime) *
                  (global.documentWidth - 300) +
                'px',
            }"
          >
            <div
              v-for="operation in track.changeWidthOperations"
              :key="operation"
            >
              <WidthOperation
                :currentNoteType="currentNoteType"
                :track="track"
                :global="global"
                :chart="chart"
                :operation="operation"
                :enableEdit="enableEdit"
                :displayAreaTime="displayAreaTime"
              />
            </div>
          </div>
          <div
            @dblclick="newColorOperations"
            class="color-track"
            :style="{
              width:
                (chart.songLength / displayAreaTime) *
                  (global.documentWidth - 300) +
                'px',
            }"
          >
            <div
              v-for="operation in track.changeColorOperations"
              :key="operation"
            >
              <ColorOperation
                :currentNoteType="currentNoteType"
                :track="track"
                :global="global"
                :chart="chart"
                :operation="operation"
                :enableEdit="enableEdit"
                :displayAreaTime="displayAreaTime"
              />
            </div>
          </div>
        </div>
      </transition>
    </div>
    <div class="track-labels">
      <transition
        name="fade"
        enter-active-class="animate__animated animate__fadeInDown"
        leave-active-class="animate__animated animate__fadeOutUp"
        ><div v-if="track.edit">
          <div class="note-track-label" :style="{ left: scrollLeft + 'px' }">
            音符轨道
          </div>
          <div
            class="positionX-track-label"
            :style="{ left: scrollLeft + 'px' }"
          >
            坐标操作
          </div>
          <div class="width-track-label" :style="{ left: scrollLeft + 'px' }">
            宽度操作
          </div>
          <div class="color-track-label" :style="{ left: scrollLeft + 'px' }">
            色彩操作
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import "animate.css";
import Note from "./Note";
import MoveOperation from "./MoveOperation";
import WidthOperation from "./WidthOperation";
import ColorOperation from "./ColorOperation";
export default {
  props: [
    "track",
    "global",
    "chart",
    "scrollLeft",
    "displayAreaTime",
    "currentNoteType",
    "enableEdit",
  ],
  components: { Note, MoveOperation, WidthOperation, ColorOperation },
  watch: {
    "global.mouseUp"() {
      this.canMove = false;
      this.leftMove = false;
      this.rightMove = false;
    },
    "global.mouseMove"() {
      if (this.canMove) {
        if (
          this.global.currentTime > 0 &&
          this.global.currentTime < this.chart.songLength
        ) {
          this.duration = this.myTrack.endTiming - this.myTrack.startTiming;

          this.myTrack.startTiming = this.roundTime(
            this.global.currentTime - this.passedTime
          );

          this.myTrack.endTiming = this.myTrack.startTiming + this.duration;
        }
      } else if (this.leftMove) {
        var currentTime = this.global.currentTime;
        if (this.roundTime(currentTime) <= this.chart.songLength) {
          this.myTrack.startTiming = this.roundTime(currentTime);
        }
      } else if (this.rightMove) {
        currentTime = this.global.currentTime;
        if (this.roundTime(currentTime) >= this.myTrack.startTiming) {
          this.myTrack.endTiming = this.roundTime(currentTime);
        }
      }
    },
  },
  data() {
    return {
      myTrack: this.track,
      myGlobal: this.global,
    };
  },
  methods: {
    roundTime(timing) {
      if (this.global.beatLine) {
        var bpm = this.chart.BPM / 16;
        var mod = (timing - this.chart.firstBeatDelay) % bpm;
        if (mod > bpm / 2) {
          timing += bpm - mod;
        } else {
          timing -= mod;
        }
      }
      return Math.ceil(timing);
    },
    updateTrack() {
      this.myGlobal.reCalculateTrack = !this.myGlobal.reCalculateTrack;
      this.myGlobal.reCalculateChartMaker = !this.myGlobal
        .reCalculateChartMaker;
    },
    longOperationCanMove() {
      setTimeout(() => {
        this.passedTime = Math.ceil(
          this.global.currentTime - this.myTrack.startTiming
        );
      }, 10);
      this.canMove = true;
    },
    newMoveOperations() {
      if (this.currentNoteType != 3) {
        if (
          this.global.currentTime > this.track.startTiming &&
          this.global.currentTime < this.track.endTiming - 150
        ) {
          this.myTrack.moveOperations.push({
            startX: this.track.tempPositionX,
            endX: this.track.tempPositionX,
            startTime: this.global.currentTime,
            endTime: this.global.currentTime + 150,
          });
          this.updateTrack();
        } else {
          this.$notify({
            title: "错误",
            message: "请在轨道范围内添加操作",
            type: "error",
          });
        }
      }
    },
    newWidthOperations() {
      if (this.currentNoteType != 3) {
        if (
          this.global.currentTime > this.track.startTiming &&
          this.global.currentTime < this.track.endTiming - 150
        ) {
          this.myTrack.changeWidthOperations.push({
            startWidth: this.track.tempWidth,
            endWidth: this.track.tempWidth,
            startTime: this.global.currentTime,
            endTime: this.global.currentTime + 150,
          });
          this.updateTrack();
        } else {
          this.$notify({
            title: "错误",
            message: "请在轨道范围内添加操作",
            type: "error",
          });
        }
      }
    },
    newColorOperations() {
      if (this.currentNoteType != 3) {
        if (
          this.global.currentTime > this.track.startTiming &&
          this.global.currentTime < this.track.endTiming - 150
        ) {
          this.myTrack.changeColorOperations.push({
            startR: this.track.tempR,
            startG: this.track.tempG,
            startB: this.track.tempB,
            endR: this.track.tempR,
            endG: this.track.tempG,
            endB: this.track.tempB,
            startTime: this.global.currentTime,
            endTime: this.global.currentTime + 150,
          });
          this.updateTrack();
        } else {
          this.$notify({
            title: "错误",
            message: "请在轨道范围内添加操作",
            type: "error",
          });
        }
      }
    },
    newNote() {
      if (this.currentNoteType != 3) {
        if (
          this.global.currentTime > this.track.startTiming &&
          this.global.currentTime < this.track.endTiming - 150
        ) {
          this.myTrack.notes.push({
            noteType: this.currentNoteType,
            key: this.track.key,
            timing: this.global.currentTime,
            endTiming: this.global.currentTime + 150,
          });
          this.updateTrack();
        } else {
          this.$notify({
            title: "错误",
            message: "请在轨道范围内添加音符",
            type: "error",
          });
        }
      }
    },
  },
};
</script>

<style scope>
.panel-no-edit {
  height: 80px;
  padding-top: 5px;
  padding-bottom: 5px;
  border-radius: 5px;
  transition: 0.5s;
  position: relative;
}
.panel-edit {
  height: 515px;
  padding-top: 5px;
  padding-bottom: 5px;
  border-radius: 5px;
  transition: 0.5s;
  position: relative;
}
.track-tracks {
  position: absolute;
  top: 0;
  left: 0;
}
.track-labels {
  position: absolute;
  top: 0;
  left: 0;
}
.note-track {
  position: absolute;
  top: 5px;
  left: 0px;
  height: 80px;
  transition: 0.5s;
  width: 100%;
}
.note-track-edit {
  position: absolute;
  left: 0px;
  top: 40px;
  height: 80px;
  transition: 0.5s;
  width: 100%;
}
.positionX-track {
  position: absolute;
  left: 0px;
  top: 160px;
  height: 80px;
}
.width-track {
  position: absolute;
  left: 0px;
  top: 280px;
  height: 80px;
}
.color-track {
  position: absolute;
  left: 0px;
  top: 400px;
  height: 80px;
}

.note-track-label {
  color: rgb(210, 210, 210);
  position: absolute;
  left: 0px;
  top: 15px;
  width: 300px;
}
.positionX-track-label {
  color: rgb(210, 210, 210);
  position: absolute;
  left: 0px;
  top: 135px;
  width: 300px;
}
.width-track-label {
  color: rgb(210, 210, 210);
  position: absolute;
  left: 0px;
  top: 255px;
  width: 300px;
}
.color-track-label {
  color: rgb(210, 210, 210);
  position: absolute;
  left: 0px;
  top: 375px;
  width: 300px;
}
</style>
