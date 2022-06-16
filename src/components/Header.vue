<template>
  <div class="header">
    <div class="left-logo" @click="$router.push({ path: '/home' })">
      EphemelodyOL
    </div>
    <div class="right">
      <el-dropdown class="user-name" trigger="hover" @command="handleCommand">
        <span class="el-dropdown-link">
          <Icon v-if="$store.state.islogin" />
          <span
            v-else
            style="font-size: 18px;font-weight: 700;color: rgb(4, 15, 47);"
            >未登录</span
          >
        </span>
        <template #dropdown>
          <el-dropdown-menu v-if="$store.state.islogin">
            <router-link to="/home">
              <el-dropdown-item>主页</el-dropdown-item>
            </router-link>
            <router-link to="/manage" v-if="$store.state.user.isAdmin">
              <el-dropdown-item>管理员界面</el-dropdown-item>
            </router-link>
            <el-dropdown-item command="changepassword"
              >修改密码</el-dropdown-item
            >
            <el-dropdown-item command="uploadicon">修改头像</el-dropdown-item>
            <el-dropdown-item command="loginout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
          <el-dropdown-menu v-else>
            <router-link to="/login">
              <el-dropdown-item>前往登录</el-dropdown-item>
            </router-link>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <!--编辑用户修改密码的弹出框-->
    <el-dialog
      title="修改密码"
      v-model="editVisible_changepassword"
      width="30%"
    >
      <el-form label-width="100px" :model="param">
        <el-form-item label="原密码" prop="oldpassword">
          <el-input type="password" v-model="param.oldPassword"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newpassword">
          <el-input
            type="password"
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
        action="http://47.113.89.104:8090/user/uploadIcon"
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
</template>
<script>
import Icon from "./Icon";
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
  components: { Icon },
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
    handleCommand(command) {
      if (command === "loginout") {
        this.$store.commit("loginout");
        this.$router.push({ path: "/login" });
      } else if (command == "changepassword") {
        this.startChangePassword();
      } else if (command == "uploadicon") {
        this.startUploadIcon();
      }
    },
  },
  mounted() {},
};
</script>
<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80px;
  padding: 5px 20px;
}
.left-logo {
  font-size: 50px;
  font-weight: 300;
  color: #ffffff;
  text-shadow: 0 0 2px rgb(64, 64, 64);
  letter-spacing: 3px;
  cursor: pointer;
}
</style>
