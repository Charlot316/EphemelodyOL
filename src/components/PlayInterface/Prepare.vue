<template>
  <div v-if="!loadingStatus.runStart" class="show-info">
  
    <img
      :src="chart.defaultBackground"
      class="loading-background"
      style="user-drag:none;"
    />
    <div style="position:absolute;width:100vw;height:100vh;"></div>
    <Header/>
    <div
      :class="loadingStatus.runReady ? 'info-container-out' : 'info-container'"
      style="overflow: auto;"
    >
      <div
        style="display: flex;justify-content: space-between;align-items: center;"
      >
        <div class="songcover-container">
          <img class="songcover-img" :src="chart.songCover" />
        </div>
        <div class="info" style="padding:20px;">
          <div
            class="song-uploader"
            style="text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:30px;color:rgb(255,250,235);"
          >
            {{ chart.uploader }}
          </div>
          <div
            class="song-name"
            style="text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:70px;color:rgb(255,255,255);background-color:rgba(105, 245, 202, 0.5);"
          >
            {{ chart.songName }}
          </div>
          <div
            class="song-writer"
            style="text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:50px;color:rgb(255,255,255);background-color:rgba(255, 255, 255, 0.5);"
          >
            {{ chart.songWriter }}
          </div>
        </div>
      </div>
      <div
        class="loading-container"
        style="text-align: center;margin-right:50px;"
      >
        <div
          :class="loadingStatus.canRun ? 'play-button' : 'play-button-disabled'"
          style="width:150px;height:150px;line-height:150px;margin:20px auto;border-radius:50%;"
          @click="startMusic"
        >
          {{ loadingStatus.canRun ? "开始" : "加载中" }}
        </div>
        <div
          class="loading-text"
          v-if="!loadingStatus.canRun"
          style="padding:10px;min-width:400px;text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:20px;color:rgb(255,255,255);"
        >
          {{ chart.loadingText }}
        </div>
        <div
          v-if="loadingStatus.canRun"
          style="padding:10px;min-width:400px;text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:20px;color:rgb(255,255,255);"
        >
          {{ chart.loadedText }}
        </div>
        <div
          class="loaded-text"
          v-if="loadingStatus.canRun"
          style="height:2px;min-width:400px;text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:20px;color:rgb(255,255,255);"
        ></div>
      </div>
    </div>
  </div>
</template>

<script>
import Header from "../Header";
export default {
  props: ["loadingStatus", "chart"],
  components: {
    Header,
  },
  watch: {
    chart(){
      this.$forceUpdate();
      console.log(this.chart.defaultBackground)
    }
  },
  created() {},
  methods: {
    startMusic() {
      this.$emit("startMusic");
    },
  },
};
</script>

<style scoped>
@keyframes backgroung-image {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes linearGradientMove {
  100% {
    background-position: 4px 0, -4px 100%, 0 -4px, 100% 4px;
  }
}

.loading-text {
  background: linear-gradient(90deg, rgb(255, 255, 255) 50%, transparent 0)
      repeat-x,
    linear-gradient(90deg, rgb(255, 255, 255) 50%, transparent 0) repeat-x,
    linear-gradient(0deg, rgb(255, 255, 255) 50%, transparent 0) repeat-y,
    linear-gradient(0deg, rgb(255, 255, 255) 50%, transparent 0) repeat-y;
  background-size: 0px 0px, 4px 1px, 0px 0px, 0px 0px;
  background-position: 0 0, 0 100%, 0 0, 100% 0;
  animation: linearGradientMove 0.3s infinite linear;
}

@keyframes loadedText {
  0% {
    background-size: 0% 2px;
  }
  100% {
    background-size: 100% 2px;
  }
}
.loaded-text {
  background-image: linear-gradient(rgb(255, 255, 255), rgb(255, 255, 255));
  background-position: center bottom;
  background-repeat: no-repeat;
  animation-duration: 1s;
  animation-name: loadedText;
}

@keyframes playbutton {
  0% {
    box-shadow: 0px 0px 0px 0px rgba(255, 120, 160, 0.8);
  }
  100% {
    box-shadow: 0px 0px 0px 40px rgba(255, 144, 164, 0.2);
  }
}
.play-button {
  animation-name: playbutton;
  animation-duration: 2s;
  animation-iteration-count: infinite;
  transform: scale(1);
  cursor: pointer;
  font-size: 30px;
  color: rgb(255, 255, 255);
  background-image: -webkit-linear-gradient(
    -240.9453959009229deg,
    rgb(252, 169, 234) 0,
    rgb(227, 133, 192) 6%,
    rgb(255, 124, 198) 53%,
    rgb(243, 83, 113) 100%
  );
  background-image: -moz-linear-gradient(
    330.9453959009229deg,
    rgb(252, 169, 234) 0,
    rgb(227, 133, 192) 6%,
    rgb(255, 124, 198) 53%,
    rgb(243, 83, 113) 100%
  );
  background-image: linear-gradient(
    330.9453959009229deg,
    rgb(252, 169, 234) 0,
    rgb(227, 133, 192) 6%,
    rgb(255, 124, 198) 53%,
    rgb(243, 83, 113) 100%
  );
  transform: scale(1);
  transition: 0.5s;
}

.play-button-disabled {
  transform: scale(1);
  cursor: wait;
  font-size: 30px;
  color: rgb(255, 255, 255);
  background-image: -webkit-linear-gradient(
    -240.9453959009229deg,
    rgb(82, 105, 125) 0,
    rgb(65, 82, 97) 6%,
    rgb(27, 55, 80) 53%,
    rgb(17, 48, 81) 100%
  );
  background-image: -moz-linear-gradient(
    330.9453959009229deg,
    rgb(82, 105, 125) 0,
    rgb(65, 82, 97) 6%,
    rgb(27, 55, 80) 53%,
    rgb(17, 48, 81) 100%
  );
  background-image: linear-gradient(
    330.9453959009229deg,
    rgb(82, 105, 125) 0,
    rgb(65, 82, 97) 6%,
    rgb(27, 55, 80) 53%,
    rgb(17, 48, 81) 100%
  );
  transform: scale(1);
  transition: 0.5s;
}
.play-button:hover {
  transform: scale(0.9);
  transition: 0.5s;
}

.play-button:active {
  transform: scale(0.85);
  transition: 0.1s;
}

.loading-background {
  position: absolute;
  left: 0;
  top: 0;
  width: 100vw;
  height: 100vh;
  object-fit: fill;
  -webkit-user-drag: none;
  animation-name: backgroung-image;
  animation-duration: 10s;
  animation-iteration-count: infinite;
}

@keyframes info-container-upward {
  0% {
    bottom: -400px;
  }
  100% {
    bottom: 0px;
  }
}

.info-container {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 300px;
  padding: 40px;
  object-fit: fill;
  -webkit-user-drag: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
  animation: info-container-upward 0.5s ease-out;
}

@keyframes info-container-downward {
  0% {
    bottom: 0px;
  }
  100% {
    bottom: -400px;
  }
}
.info-container-out {
  animation: info-container-downward 0.5s ease-out;
  position: absolute;
  left: 0;
  bottom: -400px;
  width: 100%;
  height: 300px;
  padding: 40px;
  object-fit: fill;
  -webkit-user-drag: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.songcover-container {
  width: 300px;
  height: 300px;
  margin: 0;
}

.songcover-img {
  width: 100%;
  height: 100%;
  object-fit: fill;
  -webkit-user-drag: none;
  border: 2px solid rgb(255, 255, 255);
}
</style>
