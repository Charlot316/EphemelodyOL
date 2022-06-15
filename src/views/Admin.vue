<template>
  <div style="width:100%;height:100%;">
    <background-display />
    <div class="div1">
      <Header />
      <div class="divTop">
        <button class="btn btn1" @click="getAdd()">新增</button>
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
          <div class="div5" @click="next(item.songId)">
            <el-image :src="item.songCover" class="img1"></el-image>
          </div>
          <div class="div6">
            <div>
              <div class="div7">{{ item.songName }}</div>
              <div class="div8">
                <span
                  class="icon-active"
                  icon="el-icon-link"
                  style="margin-left: 10px;"
                  ><i class="el-icon-setting" @click="getEdit(item.songId)"></i
                ></span>
                <span
                  class="icon-active"
                  icon="el-icon-link"
                  style="margin-left: 10px;"
                  ><i
                    class="el-icon-delete"
                    @click="deleteSong(item.songId)"
                  ></i
                ></span>
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

      <!--编辑基本信息的弹出框-->
      <el-dialog
        title="修改谱面信息"
        v-model="editVisible"
        width="40%"
        height="50%"
      >
        <el-form :model="form">
          <el-form-item label="受否公开谱面" :label-width="formLabelWidth">
            <el-switch
              v-model="this.value"
              active-color="#13ce66"
              active-value="true"
            >
            </el-switch>
          </el-form-item>
          <el-form-item label="歌曲名称" :label-width="formLabelWidth">
            <el-input v-model="form.songName" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="歌手名称" :label-width="formLabelWidth">
            <el-input v-model="form.songWriter" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item :label-width="formLabelWidth">
            <el-button type="primary" size="small" @click="startUploadSong()"
              >上传音频</el-button
            >
          </el-form-item>
          <el-form-item :label-width="formLabelWidth">
            <el-button type="primary" size="small" @click="startUploadBack()"
              >上传歌曲默认背景</el-button
            >
          </el-form-item>
          <el-form-item :label-width="formLabelWidth">
            <el-button type="primary" size="small" @click="startUploadCover()"
              >上传歌曲封面</el-button
            >
          </el-form-item>
          <el-form-item label="设置加载文字" :label-width="formLabelWidth">
            <el-input v-model="form.loadingText" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="设置谱面定数" :label-width="formLabelWidth">
            <el-input
              v-model="form.chartConstant"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-form-item label="设置加载完毕文字" :label-width="formLabelWidth">
            <el-input v-model="form.loadedText" autocomplete="off"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="editVisible = false">取 消</el-button>
            <el-button type="primary" @click="editSongInfo()">确 定</el-button>
          </div>
        </template>
      </el-dialog>

      <!--编辑新增谱面的弹出框-->
      <el-dialog title="新增谱面" v-model="addVisible" width="40%" height="50%">
        <el-form :model="newSong">
          <el-form-item label="BPM" :label-width="formLabelWidth">
            <el-input v-model="newSong.BPM" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="FirstBeatDelay" :label-width="formLabelWidth">
            <el-input
              v-model="newSong.firstBeatDelay"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-form-item label="歌曲ID" :label-width="formLabelWidth">
            <el-input v-model="newSong.songId" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="歌曲长度" :label-width="formLabelWidth">
            <el-input
              v-model="newSong.songLength"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-form-item label="默认背景" :label-width="formLabelWidth">
            <el-input
              v-model="newSong.defaultBackground"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-form-item label="歌曲音频" :label-width="formLabelWidth">
            <el-input v-model="newSong.songUrl" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="上传者" :label-width="formLabelWidth">
            <el-input v-model="newSong.uploader" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="作者" :label-width="formLabelWidth">
            <el-input
              v-model="newSong.songWriter"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-form-item label="封面" :label-width="formLabelWidth">
            <el-input v-model="newSong.songCover" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="加载文字" :label-width="formLabelWidth">
            <el-input
              v-model="newSong.loadingText"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-form-item label="加载完成文字" :label-width="formLabelWidth">
            <el-input
              v-model="newSong.loadedText"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-form-item label="歌曲名称" :label-width="formLabelWidth">
            <el-input v-model="newSong.songName" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="NotesCount" :label-width="formLabelWidth">
            <el-input
              v-model="newSong.notesCount"
              autocomplete="off"
            ></el-input>
          </el-form-item>
          <el-form-item label="上传者ID" :label-width="formLabelWidth">
            <el-input
              v-model="newSong.uploaderId"
              autocomplete="off"
            ></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="addVisible = false">取 消</el-button>
            <el-button type="primary" @click="addSong()">确 定</el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog title="上传音频" v-model="uploadSongVisible" width="20%">
        <el-upload
          class="avatar-uploader"
          action="http://localhost:8090/chart/uploadSong"
          with-credentials="true"
          name="file"
          accept=".wav,.mp4"
          auto-upload="false"
          :data="{ songId: this.selectedSongId }"
          :show-file-list="false"
          :on-success="handleAvatarSuccess1"
          :before-upload="beforeAvatarUpload"
        >
          <img v-if="songUrl" :src="songUrl" class="avatar" />
          <i v-else class="el-icon-plus avatar-uploader-icon"></i>
        </el-upload>
      </el-dialog>

      <el-dialog title="上传默认背景" v-model="uploadBackVisible" width="20%">
        <el-upload
          class="avatar-uploader"
          action="http://localhost:8090/chart/uploadDefaultBackground"
          with-credentials="true"
          name="file"
          accept=".jpg,.png"
          auto-upload="false"
          :data="{ songId: this.selectedSongId }"
          :show-file-list="false"
          :on-success="handleAvatarSuccess2"
          :before-upload="beforeAvatarUpload"
        >
          <i class="el-icon-plus avatar-uploader-icon"></i>
        </el-upload>
      </el-dialog>

      <el-dialog title="上传歌曲封面" v-model="uploadCoverVisible" width="20%">
        <el-upload
          class="avatar-uploader"
          action="http://localhost:8090/chart/uploadSongCover"
          with-credentials="true"
          name="file"
          accept=".jpg,.png"
          auto-upload="false"
          :data="{ songId: this.selectedSongId }"
          :show-file-list="false"
          :on-success="handleAvatarSuccess3"
          :before-upload="beforeAvatarUpload"
        >
          <i class="el-icon-plus avatar-uploader-icon"></i>
        </el-upload>
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
        status: "0",
        searchType: "",
        searchContent: "",
        sortType: "",
        sortWay: "",
      },
      form: {
        songId: "",
        songName: "",
        songWriter: "",
        songUrl: "",
        defaultBackground: "",
        songCover: "",
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
      addVisible: false,
      value: false,
      song: {
        songId: "",
      },
      uploadSongVisible: false,
      uploadBackVisible: false,
      uploadCoverVisible: false,
      songUrl: "",
    };
  },
  mounted() {},
  created() {
    this.getMyAllCharts();
  },
  methods: {
    async getMyAllCharts() {
      const { data: res } = await this.$http.post("/user/getAllMyCharts");
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
        "/user/getMyCharts",
        this.params
      );
      console.log(res);
      if (res.code == 0) {
        this.songs = res.data.charts;
      } else {
        this.$message.error("获取异常");
      }
    },
    getEdit(songId) {
      this.editVisible = true;
      this.selectedSongId = songId;
    },
    getAdd() {
      this.addVisible = true;
    },
    startUploadSong() {
      this.uploadSongVisible = true;
    },
    startUploadBack() {
      this.uploadBackVisible = true;
    },
    startUploadCover() {
      this.uploadCoverVisible = true;
    },
    async addSong() {
      const { data: res } = await this.$http.post(
        "/chart/newChart",
        this.newSong
      );
      if (res.code === 0) {
        this.$message.success("新增成功");
        this.getMyAllCharts();
        this.addVisible = false;
      } else {
        this.$message.error("编辑失败");
      }
    },
    async editSongInfo() {
      console.log(this.form);
      this.song.songId = this.selectedSongId;
      this.form.songId = this.selectedSongId;
      const { data: res1 } = await this.$http.post(
        "/user/publiciseChart",
        this.song
      );
      if (res1.code != 0) {
        this.$message.error("公开化失败");
      }
      let formData = new FormData();
      formData.append("songId", this.form.songId);
      formData.append("songName", this.form.songName);
      formData.append("songWriter", this.form.songWrite);
      formData.append("songUrl", this.form.songUrl);
      formData.append("defaultBackground", this.form.defaultBackground);
      formData.append("songCover", this.form.songCover);
      formData.append("loadingText", this.form.loadingText);
      formData.append("chartConstant", this.form.chartConstant);
      formData.append("loadedText", this.form.loadedText);
      const { data: res } = await this.$http.post(
        "/chart/editChartInfo",
        this.formData
      );
      if (res.code == 0) {
        this.$message.success("修改成功");
        this.editVisible = false;
      } else {
        this.$message.error("编辑失败");
      }
    },
    async delete(songId) {
      this.deleteForm.songId = songId;
      const { data: res } = await this.$http.post(
        "/user/deleteChart",
        this.deleteForm
      );
      if (res.code === 0) {
        this.getMyAllCharts();
        this.$message({
          type: "success",
          message: "删除成功!",
        });
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
    next(songId) {
      this.$router.push({
        path: "/chart/maker",
        query: {
          songId: songId,
        },
      });
      setTimeout(() => {
        location.reload() 
      }, 100);
    },
    handleAvatarSuccess1(res, file) {
      this.songUrl = URL.createObjectURL(file.raw);
      this.$message.success("上传成功");
      console.log(res);
      this.form.songUrl = res.data;
      this.uploadSongVisible = false;
    },
    handleAvatarSuccess2(res, file) {
      this.songUrl = URL.createObjectURL(file.raw);
      this.$message.success("上传成功");
      this.form.defaultBackground = res.data.url;
      this.uploadBackVisible = false;
    },
    handleAvatarSuccess3(res, file) {
      this.songUrl = URL.createObjectURL(file.raw);
      this.$message.success("上传成功");
      this.form.songCover = res.data.url;
      this.uploadCoverVisible = false;
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
  cursor: pointer;
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
}
.div7 {
  font-size: 18px;
  color: #000000;
  cursor: pointer;
  /* float: left; */
}
.div8 {
  /* display: flex; */
  color: #cccccc;
  line-height: 40px;
  /* width: 100%; */
  margin-left: auto;
}
.div9 {
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 15px;
  color: gray;
  margin-top: 2px;
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
