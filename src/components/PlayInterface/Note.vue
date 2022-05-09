<template>
  <div
    class="note-slice"
    :style="{
      position: 'absolute',
      height: [
        Note.noteType == 0
          ? lengthForBlackPoint + 'px'
          : lengthForWhitePoint + 'px',
      ],
      width: [
        Note.noteType == 0
          ? lengthForBlackPoint + 'px'
          : lengthForWhitePoint + 'px',
      ],
      left: [
        Note.noteType == 0
          ? left - offsetDiagonal - 2 + 'px'
          : left - offsetDiagonal + 'px',
      ],
      top: [
        Note.noteType == 0
          ? top - offsetDiagonal - 2 + 'px'
          : top - offsetDiagonal + 'px',
      ],
      transform: 'rotateZ(45deg)',
    }"
  >
    <div
      class="point"
      :style="{
        width: [     
          Note.noteType == 0
            ? lengthForPinkPoint + 'px'
            : lengthForWhitePoint - 2 + 'px',
        ],
        margin: 'auto 0',
        height: [
          Note.noteType == 0
            ? lengthForPinkPoint + 'px'
            : lengthForWhitePoint - 2 + 'px',
        ],
        background: [
          Note.noteType == 0 ? 'rgb(203, 105, 121)' : 'rgb(250, 250, 250)',
        ],
        border: [
          Note.noteType == 0
            ? 0.5 * (lengthForBlackPoint - lengthForPinkPoint) +
              'px solid rgb(22, 22, 14)'
            : '1px solid rgb(22, 22, 14)',
        ],
      }"
    ></div>
  </div>
</template>

<script>
export default {
  props: ["Note", "Global", "left"],
  data() {
    return {
      myNote: this.Note,
      lengthForPinkPoint: 30,
      lengthForBlackPoint: 45,
      lengthForWhitePoint: 20,
    };
  },
  watch: {
    "Global.currentTime"() {
      this.myNote.positionY =
        (this.Global.finalY / this.Global.remainingTime) *
          this.Global.currentTime -
        (this.Global.finalY / this.Global.remainingTime) *
          (this.Note.timing - this.Global.remainingTime);
    },
  },
  created() {
    this.myNote.positionY=0;
  },
  computed: {
    top() {
      return this.Note.positionY * this.Global.screenHeight;
    },
    offsetDiagonal() {
      if (this.Note.noteType == 0) {
        return (
          Math.sqrt(this.lengthForBlackPoint * this.lengthForBlackPoint * 2) -
          this.lengthForBlackPoint
        );
      } else {
        return (
          Math.sqrt(this.lengthForWhitePoint * this.lengthForWhitePoint * 2) -
          this.lengthForWhitePoint
        );
      }
    },
  },
};
</script>

<style scope></style>
