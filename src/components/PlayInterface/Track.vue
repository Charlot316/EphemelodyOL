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
      class="black-line"
      :style="{
        width: '2px',
        position: 'absolute',
        left: 0.5 * width + 'px',
        top: '0px',
        height: height + 'px',
        background: 'rgba(0,0,0,0.3)',
      }"
    ></div>
    <div
      class="black-point"
      :style="{
        width: lengthForBlackPoint + 'px',
        position: 'absolute',
        left: 0.5 * width - offsetDiagonal - 1.2 + 'px',
        top: height - offsetDiagonal + 'px',
        margin: 'auto 0',
        height: lengthForBlackPoint + 'px',
        background: 'rgb(0,0,0)',
        transform: 'rotateZ(45deg)',
      }"
    ></div>
    <div
      class="track-body"
      @click="isActive = !isActive"
      :style="{
        height: height + 'px',
        width: width - 4 + 'px',
        margin: 'auto 0',
        borderTop: '0px',
        borderBottom: '0px',
        borderRight: '2px solid rgba(255,255,255,1)',
        borderLeft: '2px solid rgba(255,255,255,1)',
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
    >
      {{ Track.left }}
      <div v-for="Note in Track.notes" :key="Note">
        <Note
          :Note="Note"
          v-if="
            Global.currentTime > Note.timing - Global.remainingTime &&
              Global.currentTime < Note.timing + Global.lostTime
          "
          :left="0.5 * width"
          :Global="Global"
        />
      </div>
      <div v-for="MoveOperation in Track.moveOperations" :key="MoveOperation">
        <MoveOperation
          :Operation="MoveOperation"
          v-if="
            Global.currentTime > MoveOperation.startTime &&
              Global.currentTime < MoveOperation.endTime + 2*refreshTime
          "
          :Global="Global"
          :Track="Track"
        />
      </div>
      <div
        v-for="WidthOperation in Track.changeWidthOperations"
        :key="WidthOperation"
      >
        <WidthOperation
          :Operation="WidthOperation"
          v-if="
            Global.currentTime > WidthOperation.startTime &&
              Global.currentTime < WidthOperation.endTime + 2*refreshTime
          "
          :Global="Global"
          :Track="Track"
        />
      </div>
      <div
        v-for="ColorOperation in Track.changeColorOperations"
        :key="ColorOperation"
      >
        <ColorOperation
          :Operation="ColorOperation"
          v-if="
            Global.currentTime > ColorOperation.startTime &&
              Global.currentTime < ColorOperation.endTime + 2*refreshTime
          "
          :Global="Global"
          :Track="Track"
        />
      </div>
    </div>
  </div>
</template>

<script>
import Note from "./Note";
import MoveOperation from "./MoveOperation";
import WidthOperation from "./WidthOperation";
import ColorOperation from "./ColorOperation";
export default {
  props: ["Track", "Global"],
  components: {
    Note,
    MoveOperation,
    WidthOperation,
    ColorOperation,
  },
  data() {
    return {
      myTrack: this.Track,
      isActive: false,
      lengthForBlackPoint: 15,
      refreshTime: 1000 / this.$store.state.refreshRate,
    };
  },
  watch: {
    "Global.screenWidth"() {
      this.myTrack.width = this.Track.width;
    },
  },
  created() {
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
      return (
        Math.sqrt(this.lengthForBlackPoint * this.lengthForBlackPoint * 2) -
        this.lengthForBlackPoint
      );
    },
  },
};
</script>

<style scope></style>
