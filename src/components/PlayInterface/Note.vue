<template>
  <div
    class="note-slice"
    :style="{
      position: 'absolute',
      height: lengthForPinkPoint + 'px',
      width: lengthForPinkPoint + 'px',
      left: left - offsetDiagonal - 2 + 'px',
      top: top - offsetDiagonal - 2 + 'px',
      transform: 'rotateZ(45deg)',
    }"
  >
    <div
      class="pink-point"
      :style="{
        width: '100%',
        margin: 'auto 0',
        height: '100%',
        background: 'rgb(22, 22, 14)',
      }"
    >
      <div
        class="black-point"
        :style="{
          width: lengthForBlackPoint + 'px',
          position: 'absolute',
          left: 0.5 * (lengthForPinkPoint - lengthForBlackPoint) + 'px',
          top: 0.5 * (lengthForPinkPoint - lengthForBlackPoint) + 'px',
          margin: 'auto 0',
          height: lengthForBlackPoint + 'px',
          background: 'rgb(203, 105, 121)',
        }"
      ></div>
    </div>
  </div>
</template>

<script>
export default {
  props: ["Note", "Global", "left"],
  data() {
    return {
      myNote: this.Note,
      lengthForBlackPoint: 30,
      lengthForPinkPoint: 45,
    };
  },
  watch: {
    "Global.screenWidth"() {},
  },
  created() {
    console.log(this.left);
  },
  computed: {
    top() {
      return 50;
    },
    offsetDiagonal() {
      return (
        Math.sqrt(this.lengthForPinkPoint * this.lengthForPinkPoint * 2) -
        this.lengthForPinkPoint
      );
    },
  },
};
</script>

<style scope></style>
