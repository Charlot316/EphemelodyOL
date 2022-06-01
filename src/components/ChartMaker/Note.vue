<template>
  <div :style="{position:'absolute',top:'20px',left:left+'px'}">
    <div v-if="note.noteType == 0">
      <el-image
        style="width:40px;height:40px;"
        src="http://pic.mcatk.com/charlot-pictures/EpheHitNote.png"
      />
    </div>
    <div v-if="note.noteType == 1"></div>
    <div v-if="note.noteType == 2">
      <el-image
        style="width:40px;height:40px;"
        src="http://pic.mcatk.com/charlot-pictures/EpheSlideNote.png"
      />
    </div>
  </div>
</template>

<script>
export default {
  props: ["note", "global", "track","displayAreaTime"],
  data() {
    return {
      myNote: this.note,
      myTrack: this.track,
      myGlobal: this.global,
    };
  },
  watch: {
    "global.currentTime"() {
      this.myNote.positionY =
        (this.global.finalY / this.global.remainingTime) *
          this.global.currentTime -
        (this.global.finalY / this.global.remainingTime) *
          (this.Note.timing - this.global.remainingTime);
    },
  },
  created() {},
  computed: {
    left(){
      return (this.myNote.timing / this.displayAreaTime)*(this.global.documentWidth-300);
    },

  },
};
</script>

<style scope></style>
