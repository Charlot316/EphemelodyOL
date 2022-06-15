<template>
  <div class="select-container">
    <Header/>
    <div class="divTop">
      <!-- <input type="text"> -->
      <div style="float:right;width:50%;">
        <el-row :gutter="20">
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
            </el-select>
          </el-col>
          <el-col :span="9">
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
          <el-col :span="6">
            <el-select
              v-model="params.sortType"
              placeholder="排序类别"
              size="medium"
              class="handle-select mr10"
              id="排序类别"
            >
              <el-option key="0" label="歌曲名称" value="0"></el-option>
              <el-option key="1" label="歌手名称" value="1"></el-option>
              <el-option key="2" label="上传者名称" value="2"></el-option>
              <el-option key="3" label="难度" value="3"></el-option>
              <el-option key="4" label="热度" value="4"></el-option>
            </el-select>
          </el-col>
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
      <div v-for="song in songs" :key="song" class="card-context">
        <song-card :song="song" />
      </div>
      <div v-for="count in 10" :key="count" class="card-context"></div>
    </div>
    <!-- </div> -->
  </div>
</template>

<script>
import Header from "../components/Header";
import SongCard from "../components/Select/SongCard";
export default {
  components: { SongCard,Header },
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
    if(this.$route.path == "/public"){
      this.params.status = "2"
    }
    else if(this.$route.path == "/society"){
      this.params.status = "1"
    }
     this.getCharts();
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

<style scoped>
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
  overflow: auto;
  background-image: url(../assets/img/login.png);
}

.songs-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  width: 90%;
  height: 95%;
  margin: 0 auto;
  margin-bottom: 200px;
  border-radius: 5px;
  /* height: 100%; */
  /* overflow:auto; */
}
.card-context {
  flex: 1;
  width: 300px;
  min-width: 300px;
  height: 200px;
  margin: 10px;
}
</style>
