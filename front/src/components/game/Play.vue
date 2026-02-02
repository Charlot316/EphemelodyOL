  <div
    v-show="loadingStatus.runStart"
    v-if="!loadingStatus.finished"
    class="play-container"
  >
    <BeatPlayer
      ref="playerRef"
      :chart="chart"
      :global="global"
      mode="play"
      @audio-loaded="audioLoaded"
      @image-loaded="imageLoaded"
      @add-count="addCount"
    />

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
        zIndex: 100
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
import { ref, defineProps, defineEmits, defineExpose } from 'vue';
import BeatPlayer from "@/components/game/BeatPlayer.vue";

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
  "addCount"
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
</style>
