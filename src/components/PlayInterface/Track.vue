<template>
  <div
    class="track-slice"
    :style="{
      position: 'absolute',
      height: '100%',
      width: width + 'px',
      left: left + 'px',
    }"
  >
    <div
      class="track-body"
      @click="isActive = !isActive"
      :style="{
        height: height + 'px',
        width: '100%',
        margin: 'auto 0',
        borderRight: '2px solid rgba(255,255,255,0.5)',
        borderLeft: '2px solid rgba(255,255,255,0.5)',
        top: 0,
        background: [
          isActive
            ? [
                'linear-gradient(0deg, rgba(255,255,255,0.8) 0, rgba(' +
                  Track.R +
                  ',' +
                  Track.G +
                  ',' +
                  Track.B +
                  ',0.8) 100%)',
                '-moz-linear-gradient(0deg, rgba(255,255,255,0.8) 0, rgba(' +
                  Track.R +
                  ',' +
                  Track.G +
                  ',' +
                  Track.B +
                  ',0.8) 100%)',
                '-webkit-linear-gradient(90deg, rgba(255,255,255,0.8) 0, rgba(' +
                  Track.R +
                  ',' +
                  Track.G +
                  ',' +
                  Track.B +
                  ',0.8) 100%)',
              ]
            : 'rgba(' + Track.R + ',' + Track.G + ',' + Track.B + ',0.8)',
        ],
      }"
    ></div>
    <div
      class="black-point"
      :style="{
        width: lengthForBlackPoint+'px',
        position: 'absolute',
        left: 0.5 * width - offsetDiagonal + 'px',
        top: height - offsetDiagonal - 2 + 'px',
        margin: 'auto 0',
        height: lengthForBlackPoint+'px',
        background: 'rgb(0,0,0)',
        transform: 'rotateZ(45deg)',
      }"
    ></div>
    <div
      class="black-point"
      :style="{
        width: '2px',
        position: 'absolute',
        left: 0.5 * width+1  + 'px',
        top: '0px',
        height: height+'px',
        background: 'rgba(0,0,0,0.3)',
      }"
    ></div>
  </div>
</template>

<script>
export default {
  props: ["Track", "Global"],
  data() {
    return {
      myTrack: this.Track,
      isActive: false,
      lengthForBlackPoint: 15,
    };
  },
  watch: {
    "Global.screenWidth"() {
      this.myTrack.width = this.Track.width;
    },
  },
  computed: {
    height() {
      return this.Global.screenHeight * this.Global.finalY;
    },
    width() {
      return this.Track.width * this.Global.screenWidth;
    },
    left() {
      return (
        (this.Track.positionX - 0.5 * this.Track.width) *
        this.Global.screenWidth
      );
    },
    offsetDiagonal() {
      return Math.sqrt(this.lengthForBlackPoint * this.lengthForBlackPoint * 2)-this.lengthForBlackPoint;
    },
  },
};
</script>

<style scope></style>
