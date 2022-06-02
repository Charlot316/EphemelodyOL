<template>
  <div :style="{ position: 'absolute', top: '20px', left: left - 20 + 'px' }">
    <div v-if="note.noteType == 0">
      <el-image
        @dragstart.prevent
        style="width:40px;height:40px;user-select:none;"
        src="http://pic.mcatk.com/charlot-pictures/EpheHitNote.png"
      />
    </div>
    <div v-if="note.noteType == 1">
      <div
        :style="{
          userSelect: 'none',
          height: '38px',
          position: 'absolute',
          background: 'rgb(22, 22, 14)',
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
        @dragstart.prevent
        style="width:40px;height:40px;"
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
    };
  },
  watch: {
    "global.currentTime"() {},
  },
  created() {},
  computed: {
    left() {
      return (
        (this.myNote.timing / this.displayAreaTime) *
        (this.global.documentWidth - 300)
      );
    },
  },
};
</script>

<style scope></style>
