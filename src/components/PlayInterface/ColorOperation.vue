<template>
  <div class="color-operation"></div>
</template>

<script>
export default {
  props: ["Track", "Global", "Operation"],
  data() {
    return {
      myTrack: this.Track,
      myOperation: this.Operation,
      Rk: 0.0,
      Gk: 0.0,
      Bk: 0.0,
    };
  },
  watch: {
    "Global.currentTime"() {
      if (this.Operation.startTime != this.Operation.endTime) {
        if (
          this.Global.currentTime >= this.Operation.startTime &&
          this.Global.currentTime <= this.Operation.endTime
        ) {
          this.myTrack.tempR =
            this.Rk * this.Global.currentTime +
            this.Operation.endR -
            this.Rk * this.Operation.endTime;
          this.myTrack.tempG =
            this.Gk * this.Global.currentTime +
            this.Operation.endG -
            this.Gk * this.Operation.endTime;
          this.myTrack.tempB =
            this.Bk * this.Global.currentTime +
            this.Operation.endB -
            this.Bk * this.Operation.endTime;
        }
      }
    },
  },
  created() {
    if (this.Operation.startTime != this.Operation.endTime) {
      this.Rk =
        (this.Operation.endR - this.Operation.startR) /
        (this.Operation.endTime - this.Operation.startTime);
      this.Gk =
        (this.Operation.endG - this.Operation.startG) /
        (this.Operation.endTime - this.Operation.startTime);
      this.Bk =
        (this.Operation.endB - this.Operation.startB) /
        (this.Operation.endTime - this.Operation.startTime);
    }
  },
  unmounted() {
    this.myTrack.tempR = this.Operation.endR;
    this.myTrack.tempG = this.Operation.endG;
    this.myTrack.tempB = this.Operation.endB;
  },
};
</script>

<style scope></style>
