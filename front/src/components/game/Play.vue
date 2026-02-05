<template>
  <div v-show="loadingStatus.runStart" v-if="!loadingStatus.finished" class="play-container">
    <BeatPlayer ref="playerRef" :chart="chart" :global="global" mode="play" @audio-loaded="audioLoaded"
      @image-loaded="imageLoaded" @image-progress="$emit('imageProgress', $event)" @add-count="addCount"
      @time-update="$emit('timeUpdate', $event)" @finished="$emit('finished')" />

    <!-- 记分板 -->
    <div :class="loadingStatus.beforeFinished
      ? 'play-interface-scoreboard-container-upward'
      : 'play-interface-scoreboard-container-downward'
      " :style="{
        height: '200px',
        position: 'absolute',
        top: '0px',
        left: '0px',
        width: global.screenWidth + 'px',
        background: 'linear-gradient(0deg, rgba(0,0,0,0) 0, rgba(0,0,0,1) 100%)',
        zIndex: 100
      }">
      <div class="score-counter" style="text-align:center;
          position:absolute;
          left:0px;
          width: 50px;
          margin: 0 auto;
          text-shadow: 1px 1px 0 rgba(0,0,0,0.25);
          font-size:30px;
          color:rgb(255,255,255);
          cursor: pointer;" @click="pause">
        {{ "|" + "&#32;" + "|" }}
      </div>
      <div class="score-counter" style="text-align:center;
          position:absolute;
          right:20px;
          width: 300px;
          margin: 0 auto;
          text-shadow: 0 4px 12px rgba(0,0,0,0.5);
          font-size:48px;
          font-weight: 800;
          font-family: 'Outfit', sans-serif;
          color:rgb(255,255,255)">
        <RollingNumber :value="global.score || 0" :format="{ minimumIntegerDigits: 8, useGrouping: false }" />
      </div>
      <div class="combo-counter-wrapper" v-if="global.combo > 1">
        <div class="combo-number" :key="global.combo">
          {{ global.combo }}
        </div>
        <div class="combo-label">
          <span :style="{
            color: [
              global.combo == global.pureCount && global.lostCount == 0
                ? 'rgb(247, 199, 9)'
                : global.lostCount == 0
                  ? 'rgb(135, 206, 250)'
                  : 'rgb(255, 255, 255)',
            ],
          }">{{ global.lostCount == 0 ? "⬥" : "⬦" }}</span>
          COMBO
          <span :style="{
            color: [
              global.combo == global.pureCount && global.lostCount == 0
                ? 'rgb(247, 199, 9)'
                : global.lostCount == 0
                  ? 'rgb(135, 206, 250)'
                  : 'rgb(255, 255, 255)',
            ],
          }">{{ global.lostCount == 0 ? "⬥" : "⬦" }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, defineExpose } from 'vue';
import BeatPlayer from "@/components/game/BeatPlayer.vue";
import RollingNumber from './RollingNumber.vue';

const props = defineProps({
  loadingStatus: Object,
  chart: Object,
  global: Object,
  score: String,
});

const emit = defineEmits([
  "audioLoaded",
  "imageLoaded",
  "pause",
  "back",
  "reStart",
  "continuePlay",
  "reStart",
  "continuePlay",
  "addCount",
  "timeUpdate",
  "finished"
]);

const playerRef = ref(null);

const audioLoaded = (audio) => emit("audioLoaded", audio);
const imageLoaded = () => emit("imageLoaded");
const pause = () => emit("pause");
const addCount = (param) => emit("addCount", param);

// 暴露方法给 PlayInterface
defineExpose({
  play: () => playerRef.value?.play(),
  pause: () => playerRef.value?.pause(),
  reStart: () => playerRef.value?.reStart(),
  resize: () => playerRef.value?.resize(),
  repaint: () => playerRef.value?.repaint(),
  seek: (t) => playerRef.value?.seek(t),
});
</script>

<style scoped>
@keyframes scoreboard-container-downward {
  0% {
    top: -200px;
  }

  100% {
    top: 0px;
  }
}

@keyframes scoreboard-container-upward {
  0% {
    top: 0px;
  }

  100% {
    top: -200px;
  }
}

.play-interface-scoreboard-container-downward {
  top: 0;
  animation: scoreboard-container-downward 0.5s ease-out;
}

.play-interface-scoreboard-container-upward {
  top: -200px;
  animation: scoreboard-container-upward 0.5s ease-out;
}

.play-container {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  overflow: hidden;
}

.combo-counter-wrapper {
  position: absolute;
  left: 50%;
  top: 50px;
  transform: translateX(-50%);
  text-align: center;
  pointer-events: none;
}

.combo-number {
  font-size: 80px;
  font-weight: 900;
  font-family: 'Outfit', sans-serif;
  color: #fff;
  line-height: 1;
  text-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
  animation: combo-pop 0.15s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes combo-pop {
  0% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.15);
  }

  100% {
    transform: scale(1);
  }
}

.combo-label {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 4px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: -5px;
  text-transform: uppercase;
}
</style>
