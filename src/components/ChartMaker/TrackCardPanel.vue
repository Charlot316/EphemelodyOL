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
        <div v-for="note in track.notes" :key="note">
          <note
            :currentNoteType="currentNoteType"
            :track="track"
            :global="global"
            :note="note"
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
            class="positionX-track"
            :style="{
              width:
                (chart.songLength / displayAreaTime) *
                  (global.documentWidth - 300) +
                'px',
            }"
          ></div>
          <div
            class="width-track"
            :style="{
              width:
                (chart.songLength / displayAreaTime) *
                  (global.documentWidth - 300) +
                'px',
            }"
          ></div>
          <div
            class="color-track"
            :style="{
              width:
                (chart.songLength / displayAreaTime) *
                  (global.documentWidth - 300) +
                'px',
            }"
          ></div>
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
export default {
  props: [
    "track",
    "global",
    "chart",
    "scrollLeft",
    "displayAreaTime",
    "currentNoteType",
  ],
  components: { Note },
  data() {
    return {
      myTrack: this.track,
      myGlobal: this.global,
    };
  },
  methods: {
    updateTrack() {
      this.myGlobal.reCalculateTrack = !this.myGlobal.reCalculateTrack;
      this.myGlobal.reCalculateChartMaker = !this.myGlobal
        .reCalculateChartMaker;
    },
    newNote() {
      if (this.currentNoteType != 3) {
        if (
          this.global.currentTime > this.track.startTiming &&
          this.global.currentTime < this.track.endTiming
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
  background: rgb(70, 70, 70);
  position: absolute;
  top: 5px;
  left: 0px;
  height: 80px;
  transition: 0.5s;
  width: 100%;
}
.note-track-edit {
  background: rgb(70, 70, 70);
  position: absolute;
  left: 0px;
  top: 40px;
  height: 80px;
  transition: 0.5s;
  width: 100%;
}
.positionX-track {
  background: rgb(255, 165, 165);
  position: absolute;
  left: 0px;
  top: 160px;
  height: 80px;
}
.width-track {
  background: rgb(184, 223, 107);
  position: absolute;
  left: 0px;
  top: 280px;
  height: 80px;
}
.color-track {
  background: rgb(83, 195, 208);
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
