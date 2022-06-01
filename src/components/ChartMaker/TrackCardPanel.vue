<template>
  <div :class="track.edit ? 'panel-edit' : 'panel-no-edit'">
    <div class="track-tracks">
      <div :class="track.edit ? 'note-track-edit' : 'note-track'">
      <div v-for="note in track.notes" :key="note">
      <note :track="track" :global="global" :note="note" :displayAreaTime="displayAreaTime"/>
      </div>
      </div>
      <transition
        name="fade"
        enter-active-class="animate__animated animate__fadeInDown"
        leave-active-class="animate__animated animate__fadeOutUp"
      >
        <div v-if="track.edit">
          <div class="positionX-track"></div>
          <div class="width-track"></div>
          <div class="color-track"></div>
        </div>
      </transition>
    </div>
    <div class="track-labels">
      <transition
        name="fade"
        enter-active-class="animate__animated animate__fadeInDown"
        leave-active-class="animate__animated animate__fadeOutUp"
        ><div v-if="track.edit">
          <div class="note-track-label" :style="{ left: scrollLeft + 'px' }">
            音符轨道
          </div>
          <div
            class="positionX-track-label"
            :style="{ left: scrollLeft + 'px' }"
          >
            坐标操作
          </div>
          <div class="width-track-label" :style="{ left: scrollLeft + 'px' }">
            宽度操作
          </div>
          <div class="color-track-label" :style="{ left: scrollLeft + 'px' }">
            色彩操作
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import "animate.css";
import Note from "./Note";
export default {
  props: ["track", "global", "chart", "scrollLeft","displayAreaTime"],
  components:{Note},
};
</script>

<style scope>
.panel-no-edit {
  height: 80px;
  padding-top: 5px;
  padding-bottom: 5px;
  border-radius: 5px;
  transition: 0.5s;
  position: relative;
}
.panel-edit {
  height: 515px;
  padding-top: 5px;
  padding-bottom: 5px;
  border-radius: 5px;
  transition: 0.5s;
  position: relative;
}
.track-tracks {
  position: absolute;
  top: 0;
  left: 0;
}
.track-labels {
  position: absolute;
  top: 0;
  left: 0;
}
.note-track {
  background: rgb(56, 56, 56);
  position: absolute;
  top: 5px;
  left: 0px;
  height: 80px;
  transition: 0.5s;
}
.note-track-edit {
  background: rgb(255, 255, 255);
  position: absolute;
  left: 0px;
  top: 40px;
  height: 80px;
  transition: 0.5s;
}
.positionX-track {
  background: rgb(255, 165, 165);
  position: absolute;
  left: 0px;
  top: 160px;
  height: 80px;
  width: 10000px;
}
.width-track {
  background: rgb(184, 223, 107);
  position: absolute;
  left: 0px;
  top: 280px;
  height: 80px;
}
.color-track {
  background: rgb(83, 195, 208);
  position: absolute;
  left: 0px;
  top: 400px;
  height: 80px;
}

.note-track-label {
  color: rgb(210, 210, 210);
  position: absolute;
  left: 0px;
  top: 15px;
  width: 300px;
}
.positionX-track-label {
  color: rgb(210, 210, 210);
  position: absolute;
  left: 0px;
  top: 135px;
  width: 300px;
}
.width-track-label {
  color: rgb(210, 210, 210);
  position: absolute;
  left: 0px;
  top: 255px;
  width: 300px;
}
.color-track-label {
  color: rgb(210, 210, 210);
  position: absolute;
  left: 0px;
  top: 375px;
  width: 300px;
}
</style>
