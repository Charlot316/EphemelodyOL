<template>
  <div v-if="loadingStatus.finished" class="show-record">
    <div class="loading-background-end">
      <img :src="chart.defaultBackground" class="loading-background" />
    </div>
    <div
      class="loading-background-end-shader"
      :style="{
        position: 'absolute',
        top: 0,
        left: 0,
        height: global.screenHeight + 'px',
        width: global.screenWidth + 'px',
      }"
    ></div>

    <div class="record-container">
      <div
        class="record-container-body"
        :style="{
          position: 'absolute',
          top: 0.2 * global.screenHeight + 'px',
          left: 0,
          height: 0.6 * global.screenHeight + 'px',
          width: '100%',
        }"
      >
        <div class="song-cover">
          <img class="songcover-img" :src="chart.songCover" />
        </div>
        <div class="record-content">
          <div>
            <div
              class="song-name"
              style="min-width:400px;text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:50px;color:rgb(255,255,255);background-color:rgba(105, 245, 202, 0.5);padding:0px 10px;"
            >
              {{ chart.songName }}
            </div>
            <div
              style="min-width:400px;text-align:left;text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:70px;color:rgb(255,255,255);padding:0px 10px;"
            >
              {{ global.score }}
            </div>
            <div
              style="min-width:400px;text-align:left;text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:20px;color:rgb(255,255,255);padding:0px 10px;"
            >
              最大连击:{{ global.maxCombo + "\u3000" }}历史最佳成绩:{{ chart.formerBestScore }}
            </div>
            <div
              style="min-width:400px;text-align:left;text-shadow: 1px 1px 0 rgba(0,0,0,0.25);font-size:20px;color:rgb(255,255,255);padding:0px 10px;"
            >
              pure:{{ global.pureCount + "\u3000" }}far:{{
                global.farCount + "\u3000"
              }}lost:{{ global.lostCount }}
            </div>
          </div>
        </div>
      </div>

      <div
        :class="'play-button'"
        @click="$router.go(-1)"
        style="width:150px;height:150px;line-height:150px;position:absolute;bottom:50px;right:50px;border-radius:50%;text-align: center;"
      >
        继续
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: ["loadingStatus", "chart", "global"],
};
</script>

<style scoped>
.record-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.record-container-body {
  display: flex;
  align-items: center;
}

@keyframes song-cover-enter {
  0% {
    right: 100%;
  }
  100% {
    right: 55%;
  }
}
.song-cover {
  width: 300px;
  height: 300px;
  margin: 0;
  position: absolute;
  right: 55%;
  animation: song-cover-enter 0.5s ease-out;
}
.songcover-img {
  width: 100%;
  height: 100%;
  object-fit: fill;
  -webkit-user-drag: none;
  border: 2px solid rgb(255, 255, 255);
}

@keyframes record-content-enter {
  0% {
    left: 100%;
  }
  100% {
    left: 55%;
  }
}
.record-content {
  left: 55%;
  position: absolute;
  animation: record-content-enter 0.5s ease-out;
  display: flex;
}

@keyframes loading-background-end {
  0% {
    filter: blur(0px);
  }
  100% {
    filter: blur(10px);
  }
}

.loading-background-end {
  animation-name: loading-background-end;
  animation-duration: 1s;
  animation-iteration-count: 1;
  filter: blur(10px);
}

@keyframes loading-background-end-shader {
  0% {
    background-color: rgba(100, 100, 100, 0);
  }
  100% {
    background-color: rgba(100, 100, 100, 0.2);
  }
}
.loading-background-end-shader {
  animation-name: loading-background-end-shader;
  animation-duration: 1s;
  animation-iteration-count: 1;
  background: rgba(100, 100, 100, 0.2);
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
.play-button:hover {
  transform: scale(0.9);
  transition: 0.5s;
}

.play-button:active {
  transform: scale(0.85);
  transition: 0.1s;
}
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
</style>
