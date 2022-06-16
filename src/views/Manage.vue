<template>
  <div style="width:100%;height:100%;">
    <background-display />
    <div class="div1">
        <Header/>
      <div class="divTop">
        <!-- <input type="text"> -->
        <div style="float:right;width:70%;">
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
            <el-col :span="1"></el-col>
            <el-col :span="11">
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
            <el-col :span="5">
              <el-select
                v-model="sort"
                placeholder="按歌手名升序"
                size="medium"
                class="handle-select mr10"
                id="升/降"
                @change="getCharts()"
              >
                <el-option key="0" label="按歌曲名升序" value="00"></el-option>
                <el-option key="1" label="按歌曲名降序" value="01"></el-option>
                <el-option key="2" label="按歌手名升序" value="10"></el-option>
                <el-option key="3" label="按歌手名降序" value="11"></el-option>
                <el-option
                  key="4"
                  label="按上传者名升序"
                  value="20"
                ></el-option>
                <el-option key="5" label="按歌手名降序" value="21"></el-option>
                <el-option key="6" label="按难度升序" value="30"></el-option>
                <el-option key="7" label="按难度降序" value="31"></el-option>
                <el-option key="8" label="按热度升序" value="40"></el-option>
                <el-option key="9" label="按热度降序" value="41"></el-option>
              </el-select>
            </el-col>
          </el-row>
        </div>
      </div>
      <!-- <div class="div2"> -->
      <div class="div3">
        <div class="div4" v-for="item in songs" :key="item">
          <div class="div5">
            <el-image :src="item.songCover" class="img1"></el-image>
          </div>
          <div class="div6">
            <div  style="display: flex;justify-content: space-between;align-items: center;">
              <div class="div7">{{ item.songName }}</div>
              <div class="div8">
                <div style="float:right">
                  <span
                    class="icon-active"
                    icon="el-icon-link"
                    style="margin-left: 10px;cursor:pointer"
                    ><i class="el-icon-setting" @click="getEdit(item)"></i
                  ></span>
                  <span
                    class="icon-active"
                    icon="el-icon-link"
                    style="margin-left: 10px;cursor:pointer"
                    ><i
                      class="el-icon-delete"
                      @click="deleteSong(item.songId)"
                    ></i
                  ></span>
                </div>
              </div>
            </div>
            <div class="div9">
              <span>作者：{{ item.songWriter }}</span>
            </div>
            <div class="div9">谱面定数：{{ item.chartConstant }}</div>
            <div class="div9">加载中文字：{{ item.loadingText }}</div>
            <div class="div9">加载完成文字：{{ item.loadedText }}</div>
          </div>
        </div>
      </div>
      <!-- </div> -->
      <el-dialog title="调整" v-model="editVisible" width="40%" height="50%">
        <el-form>
          <el-form-item label="是否认定谱面" :label-width="formLabelWidth">
            <el-switch
              v-model="value"
              active-color="#13ce66"
              :active-value="true"
            >
            </el-switch>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="editVisible = false">取 消</el-button>
            <el-button type="primary" @click="editStatus()">确 定</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import BackgroundDisplay from "../components/BackgroundDisplay";
import Header from "../components/Header";
export default {
  components: { BackgroundDisplay, Header },
  data() {
    return {
      type: "",
      sort: "",
      params: {
        status: "1",
        searchType: "",
        searchContent: "",
        sortType: "",
        sortWay: "",
      },
      form: {
        songId: "",
        songName: "",
        songWriter: "",
        songUrl: "http://192.168.2.169:8090/1.wav",
        defaultBackground:
          "http://pic.mcatk.com/charlot-pictures/netsuai-0.jpg",
        songCover: "http://pic.mcatk.com/charlot-pictures/netsuai-cover.jpg",
        loadingText: "",
        chartConstant: "",
        loadedText: "",
      },
      newSong: {
        BPM: "",
        firstBeatDelay: "",
        songId: "",
        songLength: "",
        defaultBackground: "",
        songUrl: "",
        uploader: "",
        songWriter: "",
        songCover: "",
        loadingText: "",
        loadedText: "",
        songName: "",
        notesCount: "",
        uploaderId: "",
      },
      deleteForm: {
        songId: "",
      },
      songs: [],
      editVisible: false,
      selectedSongId: "",
      selectedSong: "",
      addVisible: false,
      value: false,
      song: {
        songId: "",
      },
      formLabelWidth:'100',
    };
  },
  mounted() {},
  created() {
    if(!this.$store.state.user.isAdmin){
      this.$router.push("/home");
    }
    this.getAllCharts();
  },
  methods: {
    async getAllCharts() {
      const { data: res } = await this.$http.post("/user/getAllCharts");
      if (res.code == 0) {
        this.songs = res.data.charts;
      } else {
        this.$message.error("获取异常");
      }
    },
    async getCharts() {
      this.params.sortType = this.sort.substr(0, 1);
      this.params.sortWay = this.sort.substr(1, 1);
      console.log(this.params);
      const { data: res } = await this.$http.post(
        "/user/getPublicCharts",
        this.params
      );
      if (res.code == 0) {
        console.log(res);
        this.songs = [];
        for (var i = 0; i < res.data.songs.length; i++) {
          this.songs[i] = res.data.songs[i].songInfo;
          console.log(res.data.songs[i].songInfo);
        }
        console.log(this.songs);
      } else {
        this.$message.error("获取异常");
      }
    },
    getValue(){
      if(this.selectedSong.status == 1){
        this.value = false;
      }
      else{
        this.value = true;
      }
    },
    getEdit(item) {
      this.selectedSongId = item.songId;
      this.selectedSong = item;
      this.getValue();
      this.editVisible = true;
    },
    getAdd() {
      this.addVisible = true;
    },
    async delete(songId) {
      this.deleteForm.songId = songId;

      const { data: res } = await this.$http.post(
        "/user/deleteChart",
        this.deleteForm
      );
      if (res.code === 0) {
        this.getCharts();
        this.$message({
          type: "success",
          message: "删除成功!",
        });
        this.getCharts();
      } else {
        this.$message({
          type: "error",
          message: "删除失败!",
        });
      }
    },
    deleteSong(songId) {
      this.$confirm("此操作删除该谱面, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.delete(songId);
      });
    },
    async editStatus() {
      this.song.songId = this.selectedSongId;
      if (this.selectedSong.status == 1 && this.value) {
        const { data: res } = await this.$http.post(
          "/admin/accreditChart",
          this.song
        );
        if (res.code == 0) {
          this.$message.success("调整成功");
        }
      } 
      if(this.selectedSong.status == 2 && !this.value){
        const { data: res } = await this.$http.post(
          "/admin/disaccreditChart",
          this.song
        );
        if (res.code == 0) {
          this.$message.success("调整成功");
        }
      }
      this.getAllCharts();
      this.editVisible = false;
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
.btn {
  /* width: 30%;
        height: 20%; */
  color: #fff;
  border-radius: 5px;
  padding: 10px 25px;
  font-family: "Lato", sans-serif;
  font-weight: 50;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: inline-block;
  box-shadow: inset 2px 2px 2px 0px rgba(255, 255, 255, 0.5),
    7px 7px 20px 0px rgba(0, 0, 0, 0.1), 4px 4px 5px 0px rgba(0, 0, 0, 0.1);
}

/* 1 */
.btn1 {
  background: rgb(44, 202, 75);
  background: linear-gradient(0deg, rgb(58, 207, 73) 0%, rgb(31, 162, 75) 100%);
  border: none;
}
.btn1:active {
  transform: scale(0.98);
  box-shadow: 3px 2px 22px 1px rgba(0, 0, 0, 0.24);
}
.img1 {
  height: 100%;
  width: 100%;
  object-fit: cover;
}
.div1 {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  font-size: 14px;
  overflow: auto;
}
.div2 {
  display: flex;
  width: 70%;
  margin: 0 auto;
  background-color: #ffffff;
  border-radius: 5px;
  height: 100%;
  overflow: auto;
}
.div3 {
  flex: 1;
  /* border-right: 1px solid #cccccc; */
  width: 70%;
  margin: 0 auto;
  background-color: #ffffff;
  border-radius: 5px;
  /* height: 100%; */
  /* overflow:auto; */
}
.div4 {
  height: 170px;
  display: flex;
  margin: 10px 20px;
  border-bottom: 1px solid #cccccc;
}
.div5:active {
  transform: scale(0.98);
}
.div5 {
  margin: 20px 0;
  width: 200px;
}
.div6 {
  margin: 30px 20px;
  width:calc(100% - 200px);
}
.div7 {
  font-size: 18px;
  color: #000000;
  cursor: pointer;
  /* float: left; */
}
.div8 {
  display: flex;
  color: #cccccc;
  line-height: 40px;
  /* width: 100%; */
  /* float: right; */
}
.div9 {
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 15px;
  color: gray;
  margin-top: 4px;
}
/* .icon-active{
      size: 20px;
  }
  .icon-active:hover{
    color: #585858;
  } */
.item img {
  width: 100%;
  height: auto;
  transform: scale(1);
  transition: transform 1s ease 0s;
}

.item:hover img {
  transform: scale(1.1);
}
.face-pic:hover img {
  transform: rotate(360deg);
  -ms-transform: rotate(360deg); /* Internet Explorer */
  -moz-transform: rotate(360deg); /* Firefox */
  -webkit-transform: rotate(360deg); /* Safari 和 Chrome */
  -o-transform: rotate(360deg); /* Opera */
  transition: transform 0.6s ease 0s;
}
</style>
