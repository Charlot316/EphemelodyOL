<template>
  <div class="select-container">
    <div class="divTop">
      <!-- <input type="text"> -->
      <div style="float:right;width:50%;">
        <el-row>
          <el-col :span="6">
            <el-select
              v-model="params.searchType"
              placeholder="搜索类别"
              size="medium"
              class="handle-select mr10"
              id="搜索类别"
            >
              <el-option key="0" label="歌曲名称" value="0"></el-option>
              <el-option key="1" label="歌手名称" value="1"></el-option>
              <el-option key="2" label="上传者名称" value="2"></el-option>
              <el-option key="3" label="难度" value="3"></el-option>
              <el-option key="4" label="热度" value="4"></el-option>
            </el-select>
          </el-col>
          <el-col :span="2"></el-col>
          <el-col :span="12">
            <el-input
              placeholder="请输入关键词进行搜索"
              v-model="params.searchContent"
              size="medium"
              @keyup.enter="getCharts()"
            >
              <template #append>
                <el-button
                  icon="el-icon-search"
                  @click="getCharts()"
                  type="primary"
                >
                </el-button>
              </template>
            </el-input>
          </el-col>
          <el-col :span="1"></el-col>
          <el-col :span="3">
            <el-select
              v-model="params.sortWay"
              placeholder="升"
              size="medium"
              class="handle-select mr10"
              id="升/降"
            >
              <el-option key="0" label="升" value="0"></el-option>
              <el-option key="0" label="降" value="1"></el-option>
            </el-select>
          </el-col>
        </el-row>
      </div>
    </div>
    <!-- <div class="div2"> -->
    <div class="songs-container">
      <div
        v-for="song in songs"
        :key="song"
        style="margin:0px;float:left;margin-bottom:10px;"
      >
        <song-card :song="song" />
      </div>
    </div>
    <!-- </div> -->
  </div>
</template>

<script>
import SongCard from "../components/Select/SongCard";
export default {
  components: { SongCard },
  data() {
    return {
      type: "",
      params: {
        status: "0",
        searchType: "",
        searchContent: "",
        sortType: "",
        sortWay: "",
      },
      songs: [],
    };
  },
  mounted() {},
  created() {
    // this.getCharts();
    this.songs = [
      {
        songInfo: {
          songName: "熱愛発覚中",
          uploader: "charlot",
          songCover: "http://pic.mcatk.com/charlot-pictures/netsuai-cover.jpg",
          songWriter: "椎名林檎",
          songDifficulty: "9",
          defaultBackground:
            "http://pic.mcatk.com/charlot-pictures/netsuai-0.jpg",
          songId: "4",
        },
        myRecord: {
          bestScore: "0",
          recordStatus: "0",
          ranking: "0",
        },
        tenBestRecords: [
          {
            player: "",
            playerIcon: "",
            bestScore: "",
            recordStatus: "",
            ranking: "",
          },
        ],
      },
      {
        songInfo: {
          songName: "熱愛発覚中",
          uploader: "charlot",
          songCover: "http://pic.mcatk.com/charlot-pictures/netsuai-cover.jpg",
          songWriter: "椎名林檎",
          songDifficulty: "9",
          defaultBackground:
            "http://pic.mcatk.com/charlot-pictures/netsuai-0.jpg",
          songId: "4",
        },
        myRecord: {
          bestScore: "0",
          recordStatus: "0",
          ranking: "0",
        },
        tenBestRecords: [
          {
            player: "",
            playerIcon: "",
            bestScore: "",
            recordStatus: "",
            ranking: "",
          },
        ],
      },
      {
        songInfo: {
          songName: "熱愛発覚中",
          uploader: "charlot",
          songCover: "http://pic.mcatk.com/charlot-pictures/netsuai-cover.jpg",
          songWriter: "椎名林檎",
          songDifficulty: "9",
          defaultBackground:
            "http://pic.mcatk.com/charlot-pictures/netsuai-0.jpg",
          songId: "4",
        },
        myRecord: {
          bestScore: "0",
          recordStatus: "0",
          ranking: "0",
        },
        tenBestRecords: [
          {
            player: "",
            playerIcon: "",
            bestScore: "",
            recordStatus: "",
            ranking: "",
          },
        ],
      },
      {
        songInfo: {
          songName: "熱愛発覚中",
          uploader: "charlot",
          songCover: "http://pic.mcatk.com/charlot-pictures/netsuai-cover.jpg",
          songWriter: "椎名林檎",
          songDifficulty: "9",
          defaultBackground:
            "http://pic.mcatk.com/charlot-pictures/netsuai-0.jpg",
          songId: "4",
        },
        myRecord: {
          bestScore: "0",
          recordStatus: "0",
          ranking: "0",
        },
        tenBestRecords: [
          {
            player: "",
            playerIcon: "",
            bestScore: "",
            recordStatus: "",
            ranking: "",
          },
        ],
      },
      {
        songInfo: {
          songName: "熱愛発覚中",
          uploader: "charlot",
          songCover: "http://pic.mcatk.com/charlot-pictures/netsuai-cover.jpg",
          songWriter: "椎名林檎",
          songDifficulty: "9",
          defaultBackground:
            "http://pic.mcatk.com/charlot-pictures/netsuai-0.jpg",
          songId: "4",
        },
        myRecord: {
          bestScore: "0",
          recordStatus: "0",
          ranking: "0",
        },
        tenBestRecords: [
          {
            player: "",
            playerIcon: "",
            bestScore: "",
            recordStatus: "",
            ranking: "",
          },
        ],
      },
      {
        songInfo: {
          songName: "熱愛発覚中",
          uploader: "charlot",
          songCover: "http://pic.mcatk.com/charlot-pictures/netsuai-cover.jpg",
          songWriter: "椎名林檎",
          songDifficulty: "9",
          defaultBackground:
            "http://pic.mcatk.com/charlot-pictures/netsuai-0.jpg",
          songId: "4",
        },
        myRecord: {
          bestScore: "0",
          recordStatus: "0",
          ranking: "0",
        },
        tenBestRecords: [
          {
            player: "",
            playerIcon: "",
            bestScore: "",
            recordStatus: "",
            ranking: "",
          },
        ],
      },
      {
        songInfo: {
          songName: "熱愛発覚中",
          uploader: "charlot",
          songCover: "http://pic.mcatk.com/charlot-pictures/netsuai-cover.jpg",
          songWriter: "椎名林檎",
          songDifficulty: "9",
          defaultBackground:
            "http://pic.mcatk.com/charlot-pictures/netsuai-0.jpg",
          songId: "4",
        },
        myRecord: {
          bestScore: "0",
          recordStatus: "0",
          ranking: "0",
        },
        tenBestRecords: [
          {
            player: "",
            playerIcon: "",
            bestScore: "",
            recordStatus: "",
            ranking: "",
          },
        ],
      },
      {
        songInfo: {
          songName: "熱愛発覚中",
          uploader: "charlot",
          songCover: "http://pic.mcatk.com/charlot-pictures/netsuai-cover.jpg",
          songWriter: "椎名林檎",
          songDifficulty: "9",
          defaultBackground:
            "http://pic.mcatk.com/charlot-pictures/netsuai-0.jpg",
          songId: "4",
        },
        myRecord: {
          bestScore: "0",
          recordStatus: "0",
          ranking: "0",
        },
        tenBestRecords: [
          {
            player: "",
            playerIcon: "",
            bestScore: "",
            recordStatus: "",
            ranking: "",
          },
        ],
      },
      {
        songInfo: {
          songName: "熱愛発覚中",
          uploader: "charlot",
          songCover: "http://pic.mcatk.com/charlot-pictures/netsuai-cover.jpg",
          songWriter: "椎名林檎",
          songDifficulty: "9",
          defaultBackground:
            "http://pic.mcatk.com/charlot-pictures/netsuai-0.jpg",
          songId: "4",
        },
        myRecord: {
          bestScore: "0",
          recordStatus: "0",
          ranking: "0",
        },
        tenBestRecords: [
          {
            player: "",
            playerIcon: "",
            bestScore: "",
            recordStatus: "",
            ranking: "",
          },
        ],
      },
      {
        songInfo: {
          songName: "熱愛発覚中",
          uploader: "charlot",
          songCover: "http://pic.mcatk.com/charlot-pictures/netsuai-cover.jpg",
          songWriter: "椎名林檎",
          songDifficulty: "9",
          defaultBackground:
            "http://pic.mcatk.com/charlot-pictures/netsuai-0.jpg",
          songId: "4",
        },
        myRecord: {
          bestScore: "0",
          recordStatus: "0",
          ranking: "0",
        },
        tenBestRecords: [
          {
            player: "",
            playerIcon: "",
            bestScore: "",
            recordStatus: "",
            ranking: "",
          },
        ],
      },
    ];
  },
  methods: {
    async getCharts() {
      this.params.sortType = this.params.searchType;
      const { data: res } = await this.$http.post(
        "/user/getPublicCharts",
        this.params
      );
      if (res.code == 0) {
        this.songs = res.data.songs;
      } else {
        this.$message.error("获取异常");
      }
    },
  },
};
</script>

<style>
.divTop {
  /* border:1px solid black; */
  width: 70%;
  height: 5%;
  margin-left: 15%;
  margin-bottom: 0.5%;
}
.select-container {
  width: 100%;
  height: 100%;
  background-color: #eee;
  font-size: 14px;
  padding: 20px 0;
  overflow: auto;
  background-image: url(../assets/img/login.jpg);
}

.songs-container {
  flex: 1;
  /* border-right: 1px solid #cccccc; */
  width: 70%;
  height: 95%;
  margin: 0 auto;
  margin-bottom:200px;
  border-radius: 5px;
  /* height: 100%; */
  /* overflow:auto; */
}
</style>
