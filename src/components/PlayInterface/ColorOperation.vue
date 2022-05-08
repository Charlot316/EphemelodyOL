<template>
  <div class="move-operation">
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
      this.myTrack.positionX =this.k*this.Global.currentTime+this.Operation.endX-this.k*this.Operation.endTime
    },
  },
  created(){
    this.myOperation.startX=this.Track.positionX
    this.k=(this.Operation.endX-this.Operation.startX)/(this.Operation.endTime-this.Operation.startTime)
  },
  unmounted(){
   this.myTrack.positionX=this.Operation.endX 
  }
};
</script>

<style scope></style>
