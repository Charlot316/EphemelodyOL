<template
  ><router-link
    target="_blank"
    :to="{ path: '/play', query: { songId: song.songInfo.songId } }"
  >
    <div
      class="song-card"
      @mouseenter="
        mouseEnter = true;
        zIndex = 10;
      "
      @mouseleave="
        mouseEnter = false;
        zIndex = 0;
      "
      :style="{ zIndex: zIndex }"
    >
      <img :src="song.songInfo.defaultBackground" class="default-background" />
      <div class="song-info">
        <img :src="song.songInfo.songCover" class="song-cover" />
        <div class="content">
          <!-- <div style="font-size:15px;">{{ song.songInfo.uploader }}</div>  -->
          <div>
            {{ song.songInfo.songName }} LV.{{ song.songInfo.songDifficulty }}
          </div>
          <div style="font-size:15px;">{{ song.songInfo.songWriter }}</div>
        </div>
      </div>
      <div :class="mouseEnter ? 'rank' : 'rank-notopen'">
        <div style="height:120px;overflow: auto;padding:10px;">
          世界排名
          <div v-for="record in song.tenBestRecords" :key="record">
            <Icon :user="record" />
          </div>
        </div>
        <div style="height:50px;padding:10px;padding-top: 0px;">
          <hr />
          我的排名
          <Icon
            :user="
              Object.assign(song.myRecord, {
                player: $store.state.user.username,
                playerIcon: $store.state.user.icon,
              })
            "
          />
        </div>
      </div>
    </div>
  </router-link>
</template>

<script>
import Icon from "./Icon";
export default {
  props: ["song"],
  components: { Icon },
  data() {
    return {
      mouseEnter: false,
      zIndex: 0,
    };
  },
  created() {},
  methods: {},
};
</script>

<style scoped>
.song-card {
  position: relative;
  width: 300px;
  margin: 10px;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  transition: 0.5s;
  height: 200px;
  background-color: #fff;
  color: rgb(32, 32, 32);
  -webkit-box-shadow: 0 0 2px 0 rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 2px 0 rgba(255, 255, 255, 0.8);
  transition: 0.5s;
  transform: scale(1);
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}
.song-card:hover {
  background: white;
  color: #303133;
  -webkit-box-shadow: 0 0 9px 4px rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 9px 4px rgba(255, 255, 255, 0.8);
  transition: 0.5s;
  transform: scale(1.05);
  height: 400px;
  cursor: pointer;
}

.song-card:active {
  background: rgb(210, 210, 210);
  color: #303133;
  -webkit-box-shadow: 0 0 9px 4px rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 9px 4px rgba(255, 255, 255, 0.8);
  transition: 0.1s;
  transform: scale(1.04);
  cursor: pointer;
}

.default-background {
  position: absolute;
  left: 0;
  top: 0;
  height: 200px;
  width: 300px;
  border-radius: 5px;
}
.song-info {
  position: absolute;
  left: 0;
  top: 100px;
  height: 100px;
  width: 300px;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.5);
  overflow: hidden;
}
.song-cover {
  position: absolute;
  left: 10px;
  top: 10px;
  height: 80px;
  width: 80px;
  border-radius: 5px;
}
.song-info .content {
  position: absolute;
  left: 100px;
  bottom: 10px;
  max-width: 200px;
  font-size: 20px;
  color: rgb(71, 71, 71);
}
.rank {
  position: absolute;
  left: 0px;
  top: 200px;
  height: 200px;
  width: 300px;
  border-radius: 5px;
  transition: 0.5s;
  overflow: hidden;
}
.rank-notopen {
  position: absolute;
  left: 0px;
  top: 200px;
  height: 0px;
  width: 300px;
  border-radius: 5px;
  transition: 0.5s;
  overflow: hidden;
}
</style>
