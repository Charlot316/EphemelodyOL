<template>
  <div class="move-operation"></div>
</template>

<script>
export default {
  props: ["Track", "Global", "Operation"],
  data() {
    return {
      myTrack: this.Track,
      myOperation: this.Operation,
      k: 0.0,
    };
  },
  watch: {
    "Global.currentTime"() {
      if (this.Operation.startTime != this.Operation.endTime) {
        if (
          this.Global.currentTime >= this.Operation.startTime &&
          this.Global.currentTime <= this.Operation.endTime
        ) {
          this.myTrack.tempPositionX =
            this.k * this.Global.currentTime +
            this.Operation.endX -
            this.k * this.Operation.endTime;
        }
      }
    },
  },
  created() {
    if (this.Operation.startTime != this.Operation.endTime) {
      this.k =
        (this.Operation.endX - this.Operation.startX) /
        (this.Operation.endTime - this.Operation.startTime);
    }
  },
  unmounted() {
    this.myTrack.tempPositionX = this.Operation.endX;
  },
};
</script>

<style scope></style>
