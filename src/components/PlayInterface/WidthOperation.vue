<template>
  <div class="width-operation">
  </div>
</template>

<script>

export default {
  props: ["Track","Global","Operation"],
  data() {
    return {
      myTrack: this.Track,
      myOperation: this.Operation,
      k:0.0,
    };
  },
  watch: {
    "Global.currentTime"() {
      this.myTrack.width =this.k*this.Global.currentTime+this.Operation.endWidth-this.k*this.Operation.endTime
    },
  },
  created(){
    this.myOperation.startWidth=this.Track.width
    this.k=(this.Operation.endWidth-this.Operation.startWidth)/(this.Operation.endTime-this.Operation.startTime)
  },
  unmounted(){
   this.myTrack.width=this.Operation.endWidth
  }
};
</script>

<style scope></style>
