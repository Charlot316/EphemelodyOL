<template>
  <div :style="{ width: width, backgroundColor: backgroundColor,left:left }"></div>
</template>

<script>
export default {
  props:['Track','Global'],
  data() {
    return {
      left: 0,
      center:0,
    };
  },
  computed:{
    width(){
      return this.Track.positionX*this.Global.screenWidth
    },
    backgroundColor(){
      return "#"+this.Track.R.toString(16)+this.Track.G.toString(16)+this.Track.B.toString(16)
    },

  },

};
</script>

<style scope></style>
