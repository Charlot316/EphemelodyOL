<template>
  <div :style="{ position: 'absolute', top: '20px', left: left - 20 + 'px' }">
    <div v-if="note.noteType == 0">
      <el-image
        @dragstart.prevent
        @mousedown="canMove = true"
        style="width:40px;height:40px;user-select:none;cursor:nesw-resize"
        src="http://pic.mcatk.com/charlot-pictures/EpheHitNote.png"
      />
    </div>
    <div v-if="note.noteType == 1">
      <div
        @mousedown="longNoteCanMove"
        :style="{
          userSelect: 'none',
          height: '38px',
          position: 'absolute',
          background: 'rgb(22, 22, 14)',
          cursor: 'nesw-resize',
          width:
            ((myNote.endTiming - myNote.timing) / this.displayAreaTime) *
              (this.global.documentWidth - 300) +
            'px',
          left: '20px',
          top: '1px',
        }"
      ></div>
      <el-image
        @dragstart.prevent
        style="width:40px;height:40px;position:absolute;left:0;top:0;user-select: none;"
        src="http://pic.mcatk.com/charlot-pictures/EpheHitNote.png"
      />
      <el-image
        @dragstart.prevent
        :style="{
          userSelect: 'none',
          height: '40px',
          width: '40px',
          position: 'absolute',
          left:
            ((myNote.endTiming - myNote.timing) / this.displayAreaTime) *
              (this.global.documentWidth - 300) +
            'px',
          top: '0px',
        }"
        src="http://pic.mcatk.com/charlot-pictures/EpheHitNote.png"
      />
    </div>
    <div v-if="note.noteType == 2">
      <el-image
        @mousedown="canMove = true"
        @dragstart.prevent
        style="width:40px;height:40px;cursor:nesw-resize"
        src="http://pic.mcatk.com/charlot-pictures/EpheSlideNote.png"
      />
    </div>
  </div>
</template>

<script>
export default {
  props: ["note", "global", "track", "displayAreaTime"],
  data() {
    return {
      myNote: this.note,
      myTrack: this.track,
      myGlobal: this.global,
      canMove: false,
      passedTime: 0,
    };
  },
  watch: {
    "global.mouseUp"() {
      this.canMove = false;
    },
    "global.mouseMove"() {
      if (this.canMove) {
        if (
          this.global.currentTime > this.track.startTiming &&
          this.global.currentTime < this.track.endTiming
        ) {
          if (this.myNote.noteType == 1) {
            this.duration = this.note.endTiming - this.note.timing;
            this.myNote.timing = Math.ceil(
              this.global.currentTime - this.passedTime
            );
            this.myNote.endTiming = this.myNote.timing + this.duration;
            console.log(this.myNote.endTiming);
            this.$forceUpdate();
          } else {
            this.myNote.timing = this.global.currentTime;
          }
          this.myTrack.notes.sort(function(a, b) {
            return a.timing - b.timing;
          });
        }
      }
    },
  },
  computed: {
    left() {
      return (
        (this.myNote.timing / this.displayAreaTime) *
        (this.global.documentWidth - 300)
      );
    },
  },
  methods: {
    longNoteCanMove() {
      setTimeout(() => {
        this.passedTime = Math.ceil(this.global.currentTime - this.note.timing);
      }, 10);
      this.canMove = true;
    },
  },
};
</script>

<style scope></style>
