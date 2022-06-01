<template>
  <div :class="track.edit ? 'panel-edit' : 'panel-no-edit'">
    <div class="track-tracks">
      <div :class="track.edit ? 'note-track-edit' : 'note-track'"></div>
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
          <div class="note-track-label">音符轨道</div>
          <div class="positionX-track-label">坐标操作</div>
          <div class="width-track-label">宽度操作</div>
          <div class="color-track-label">色彩操作</div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import "animate.css";
export default {
  props: ["track", "global", "chart"],
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
  background: rgb(255, 255, 255);
  position: absolute;
  top: 5px;
  left: 0px;
  height: 80px;
  transition: 0.5s;
  width: 10000px;
}
.note-track-edit {
  background: rgb(255, 255, 255);
  position: absolute;
  left: 0px;
  top: 40px;
  height: 80px;
  width: 10000px;
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
  width: 10000px;
}
.color-track {
  background: rgb(83, 195, 208);
  position: absolute;
  left: 0px;
  top: 400px;
  height: 80px;
  width: 10000px;
}

.note-track-label {
  color: white;
  position: absolute;
  left: 0px;
  top: 20px;
  width:100px;
}
.positionX-track-label {
  color: white;
  position: absolute;
  left: 0px;
  top: 140px;
  width:100px;
}
.width-track-label {
  color: white;
  position: absolute;
  left: 0px;
  top: 260px;
  width:100px;
}
.color-track-label {
  color: white;
  position: absolute;
  left: 0px;
  top: 380px;
  width:100px;
}
</style>
