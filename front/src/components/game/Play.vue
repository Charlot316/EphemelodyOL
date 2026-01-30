<template>
  <div
    v-show="loadingStatus.runStart"
    v-if="!loadingStatus.finished"
    class="play-interface"
  >
    <!-- 音频 -->
    <audio
      id="audioSong"
      preload="auto"
      controls
      :src="chart.songUrl"
      style="display:none"
      @canplaythrough="audioLoaded"
    />
    <!-- 背景 -->
    <div v-for="image in imagePath" :key="image.url + image.startTime">
      <img
        :src="image.url"
        v-show="
          global.currentTime >= image.startTime &&
            global.currentTime <= image.endTime
        "
        @load="imageLoaded"
        style="position:absolute;left:0;top:0;width:100%;height:100%;object-fit:fill;user-drag:none;"
      />
    </div>

    <!-- 判定线 -->
    <div
      :style="{
        height: '2px',
        position: 'absolute',
        left: '0px',
        top: global.screenHeight * global.finalY - 1 + 'px',
        width: global.screenWidth + 'px',
      }"
    >
      <div
        class="white-line"
        :style="{
          height: '100%',
          position: 'absolute',
          left: (global.screenWidth - whiteLineLength) / 2 + 'px',
          width: whiteLineLength + 'px',
          background: 'rgba(255,255,255,1)',
        }"
      ></div>
    </div>

    <canvas id="track-canvas" style="position:absolute;top:0;left:0;" />
    <canvas id="note-canvas" style="position:absolute;top:0;left:0;" />
    <canvas id="judge-canvas" style="position:absolute;top:0;left:0;" />
    <!-- 轨道 -->
    <div
      class="play-interface-track-container"
      v-for="trackItem in chart.tracks"
      :key="trackItem.trackId || trackItem.startTiming"
    >
      <Track
        :Track="trackItem"
        :global="global"
        @addCount="addCount"
        v-if="
          global.currentTime > trackItem.startTiming &&
            global.currentTime < trackItem.endTiming
        "
      />
    </div>

    <!-- 记分板 -->
    <div
      :class="
        loadingStatus.beforeFinished
          ? 'play-interface-scoreboard-container-upward'
          : 'play-interface-scoreboard-container-downward'
      "
      :style="{
        height: '200px',
        position: 'absolute',
        left: '0px',
        width: global.screenWidth + 'px',
        background: [
          '-webkit-linear-gradient(90deg, rgba(0,0,0,0) 0, rgba(0,0,0,1) 100%)',
          '-moz-linear-gradient(0deg, rgba(0,0,0,0) 0, rgba(0,0,0,1) 100%)',
          'linear-gradient(0deg, rgba(0,0,0,0) 0, rgba(0,0,0,1) 100%)',
        ],
      }"
    >
      <div
        class="score-counter"
        style="text-align:center;
          position:absolute;
          left:0px;
          width: 50px;
          margin: 0 auto;
          text-shadow: 1px 1px 0 rgba(0,0,0,0.25);
          font-size:30px;
          color:rgb(255,255,255);
          cursor: pointer;"
        @click="pause"
      >
        {{ "|" + "&#32;" + "|" }}
      </div>
      <div
        class="score-counter"
        style="text-align:center;
          position:absolute;
          right:0px;
          width: 200px;
          margin: 0 auto;
          text-shadow: 1px 1px 0 rgba(0,0,0,0.25);
          font-size:40px;
          color:rgb(255,255,255)"
      >
        {{ score }}
      </div>
      <div class="combo-counter" v-if="global.combo > 1">
        <div
          style="text-align:center;
          width: 200px;
          margin: 0 auto;
          text-shadow: 1px 1px 0 rgba(0,0,0,0.25);
          font-size:70px;
          color:rgb(255,255,255)"
        >
          {{ global.combo }}
        </div>
        <div
          style="text-align:center;
          width: 200px;
          margin: 0 auto;
          text-shadow: 1px 1px 0 rgba(0,0,0,0.25);
          font-size:20px;
          color:rgb(255,255,255)"
        >
          <span
            :style="{
              color: [
                global.combo == global.pureCount && global.lostCount == 0
                  ? 'rgb(247, 199, 9)'
                  : global.lostCount == 0
                  ? 'rgb(135, 206, 250)'
                  : 'rgb(255, 255, 255)',
              ],
            }"
            >{{ global.lostCount == 0 ? "⬥" : "⬦" }}</span
          >
          COMBO
          <span
            :style="{
              color: [
                global.combo == global.pureCount && global.lostCount == 0
                  ? 'rgb(247, 199, 9)'
                  : global.lostCount == 0
                  ? 'rgb(135, 206, 250)'
                  : 'rgb(255, 255, 255)',
              ],
            }"
            >{{ global.lostCount == 0 ? "⬥" : "⬦" }}</span
          >
        </div>
      </div>
    </div>
  
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import Track from "@/components/game/Track.vue";

const props = defineProps({
  loadingStatus: Object,
  chart: Object,
  global: Object,
  imagePath: Array,
  score: String,
  whiteLineLength: Number
});

const emit = defineEmits([
  "audioLoaded",
  "imageLoaded",
  "pause",
  "back",
  "reStart",
  "continuePlay",
  "addCount"
]);

const audioLoaded = () => emit("audioLoaded");
const imageLoaded = () => emit("imageLoaded");
const pause = () => emit("pause");
const addCount = (param) => emit("addCount", param);
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
</style>
