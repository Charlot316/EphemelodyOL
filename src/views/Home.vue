<template>
  <div class="home">
    <Header />
    <div style="border: 1px solid black ;height:100%">
      <div class="leftDiv">
        <div style="margin-left:25%;width:50%;height:40%">
          <img :src="user.iconUrl" class="img1" />
        </div>
        <div style="width:50%;margin-left:25%">
          <button
            class="btn_2 btn2"
            style="margin-top:5%"
            @click="startUploadIcon()"
          >
            上传头像
          </button>
          <button
            class="btn_2 btn2"
            style="margin-top:7%"
            @click="startChangePassword()"
          >
            修改密码
          </button>
        </div>
      </div>
      <div class="rightDiv">
        <button class="btn btn1" @click="$router.push({path:'/public'})">开始游戏</button>
        <div class="div1">
          <div class="div_btn21">
            <button class="btn_2 btn2"  @click="$router.push({path:'/society'})">社区</button>
          </div>
          <div class="div_btn22">
            <button class="btn_2 btn2"  @click="$router.push({path:'/admin'})">管理</button>
          </div>
        </div>
      </div>

      <!--编辑用户修改密码的弹出框-->
      <el-dialog
        title="修改密码"
        v-model="editVisible_changepassword"
        width="30%"
      >
        <el-form label-width="100px" :model="param">
          <el-form-item label="原密码" prop="oldpassword">
            <el-input
              type="password"
              autocomplete="off"
              v-model="param.oldPassword"
            ></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newpassword">
            <el-input
              type="password"
              autocomplete="off"
              v-model="param.newPassword"
              @keyup.enter="changePassword()"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="changePassword()">确定</el-button>
            <el-button @click="quitChangePassword">取消</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>

      <!--编辑用户上传头像的弹出框-->
      <el-dialog title="上传头像" v-model="editVisible_uploadIcon" width="20%">
        <el-upload
          class="avatar-uploader"
          action="http://localhost:8090/user/uploadIcon"
          :with-credentials="true"
          name="file"
          accept=".jpg,.png"
          auto-upload="false"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <img v-if="imgUrl" :src="imgUrl" class="avatar" />
          <i v-else class="el-icon-plus avatar-uploader-icon"></i>
        </el-upload>
      </el-dialog>
    </div>
  </div>
</template>
<script>
import Header from "../components/Header";
export default {
  data() {
    return {
      param: {
        newPassword: "",
        oldPassword: "",
      },
      editVisible_changepassword: false,
      editVisible_uploadIcon: false,
      user: {
        iconUrl: "",
        password: "",
        username: "",
        userId: "",
      },
      imgUrl: "",
    };
  },
  created() {
    this.getUserInformation();
  },
  components: {
    Header,
  },
  computed: {},
  methods: {
    startUploadIcon() {
      this.editVisible_uploadIcon = true;
    },
    quitUploadIcon() {
      this.editVisible_uploadIcon = false;
    },

    handleAvatarSuccess(res, file) {
      this.imageUrl = URL.createObjectURL(file.raw);
      this.$message.success("上传成功");
      this.editVisible_uploadIcon = false;
      this.imgUrl = "";
      this.getUserInformation();
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === "image/jpeg" || file.type === "image/png";
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isJPG) {
        this.$message.error("上传头像图片只能是 JPG或PNG 格式!");
      }
      if (!isLt2M) {
        this.$message.error("上传头像图片大小不能超过 2MB!");
      }
      return isJPG && isLt2M;
    },
    startChangePassword() {
      this.editVisible_changepassword = true;
    },
    quitChangePassword() {
      this.editVisible_changepassword = false;
    },
    async getUserInformation() {
      try {
        const { data: res } = await this.$http.post("/user/getUserInformation");
        if (res.code !== 0)
          return this.$notify({
            title: "错误",
            message: res.data,
            type: "error",
          });
        this.user.username = res.data.username;
        this.user.userId = res.data.userId;
        this.user.iconUrl = res.data.iconUrl;
        this.user.password = res.data.password;
        this.$store.commit("changeParam", {
          key: "icon",
          value: res.data.iconUrl,
        });
        console.log(res);
      } catch (err) {
        return this.$notify({
          title: "错误",
          message: "网络异常",
          type: "error",
        });
      }
    },
    async changePassword() {
      try {
        if (this.param.oldPassword !== this.user.password) {
          return this.$notify({
            title: "请重新输入",
            message: "旧密码输入错误",
            type: "error",
          });
        } else if (this.param.oldPassword === this.param.newPassword) {
          return this.$notify({
            title: "请重新输入",
            message: "新密码和旧密码不能一样",
            type: "error",
          });
        }
        const { data: res } = await this.$http.post(
          "/user/changePassword",
          this.param
        );
        if (res.code !== 0)
          return this.$notify({
            title: "错误",
            message: res.data,
            type: "error",
          });
        this.$notify({
          title: "成功",
          message: "修改密码成功！",
          type: "success",
        });
        this.editVisible_changepassword = false;
      } catch (err) {
        return this.$notify({
          title: "错误",
          message: "网络异常",
          type: "error",
        });
      }
    },
  },
};
</script>

<style>
.leftDiv {
  /* border: 1px solid black; */
  width: 30%;
  height: 60%;
  float: left;
  margin-top: 5%;
  margin-left: 18%;
}
.rightDiv {
  /* border: 1px solid black; */
  width: 30%;
  height: 60%;
  float: right;
  margin-top: 5%;
  margin-right: 18%;
}
/* .img1{
  margin-left: 20%;
  height: 50%;
  width: 60%;
  object-fit: cover;
  cursor: pointer;
} */
.img1 {
  width: 100%;
  height: 100%;
  margin-left: 1%;
  background-size: cover;
  clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%);
  /* clip-path: polygon(50 0, 100% 50%, 50% 100%,0 50%); */
  transition: 1s clip-path;
}
.img1:hover {
  -webkit-clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
  clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
}
.divUser {
  border: 1px solid black;
  width: 30%;
}
.home {
  width: 100%;
  height: 100%;
  background-image: url(../assets/img/login.png);
}

.div1 {
  width: 80%;
  height: 40%;
  /* margin-left: 50%; */
  margin-top: 5%;
  /* border: 2px solid black; */
}

.div_btn21 {
  width: 45%;
  height: 40%;
  float: left;
}

.div_btn22 {
  width: 45%;
  height: 40%;
  float: right;
}

.btn_2 {
  width: 100%;
  height: 100%;
  color: #fff;
  border-radius: 5px;
  padding: 10px 25px;
  font-family: "Lato", sans-serif;
  font-weight: 500;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: inline-block;
  box-shadow: inset 2px 2px 2px 0px rgba(255, 255, 255, 0.5),
    7px 7px 20px 0px rgba(0, 0, 0, 0.1), 4px 4px 5px 0px rgba(0, 0, 0, 0.1);
}

.btn {
  width: 80%;
  height: 20%;
  color: #fff;
  border-radius: 5px;
  padding: 10px 25px;
  font-family: "Lato", sans-serif;
  font-weight: 500;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: inline-block;
  box-shadow: inset 2px 2px 2px 0px rgba(255, 255, 255, 0.5),
    7px 7px 20px 0px rgba(0, 0, 0, 0.1), 4px 4px 5px 0px rgba(0, 0, 0, 0.1);
}

.btn1 {
  /* margin-left: 50%;
  margin-top: 10%; */
  font-size: 40px;
  margin-top: 20%;
  background: rgb(247, 150, 192);
  background: radial-gradient(
    circle,
    rgba(247, 150, 192, 1) 0%,
    rgba(118, 174, 241, 1) 100%
  );
  border: none;
}

.btn1:hover {
  background: rgb(247, 150, 192);
  background: radial-gradient(
    circle,
    rgb(187, 81, 127) 0%,
    rgb(48, 81, 122) 100%
  );
  transform: scale(1.2);
}

.btn1:active {
  transform: scale(0.98);
  box-shadow: 3px 2px 22px 1px rgba(0, 0, 0, 0.24);
}

.btn2 {
  font-size: 20px;
  background: rgb(247, 150, 192);
  background: radial-gradient(
    circle,
    rgba(247, 150, 192, 1) 0%,
    rgba(118, 174, 241, 1) 100%
  );
  border: none;
}

.btn2:hover {
  background: rgb(247, 150, 192);
  background: radial-gradient(
    circle,
    rgb(187, 81, 127) 0%,
    rgb(48, 81, 122) 100%
  );
  transform: scale(1.2);
}

.btn2:active {
  transform: scale(0.98);
  box-shadow: 3px 2px 22px 1px rgba(0, 0, 0, 0.24);
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}
</style>
