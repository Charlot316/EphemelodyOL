<template>
  <div style="width:100%;height:100%;">
    <background-display />
    <div class="loginbody">
      <div class="logindata">
        <div class="logintext">
          <h2>Welcome</h2>
        </div>
        <div class="formdata">
          <el-form ref="login" :model="form" :rules="rules">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                clearable
                placeholder="请输入账号"
              ></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                clearable
                placeholder="请输入密码"
                show-password
                @keyup.enter="login()"
              ></el-input>
            </el-form-item>
          </el-form>
        </div>
        <div class="tool"></div>
        <div class="butt">
          <el-button type="primary" @click="login()">登录</el-button>
          <el-button class="shou">
            <router-link to="/register">注册</router-link>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import BackgroundDisplay from "../components/BackgroundDisplay";
export default {
  components: { BackgroundDisplay },
  name: "login",
  data() {
    return {
      form: {
        password: "",
        username: "",
      },
      checked: false,
      rules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          { max: 10, message: "不能大于10个字符", trigger: "blur" },
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { max: 10, message: "不能大于10个字符", trigger: "blur" },
        ],
      },
    };
  },
  methods: {
    login() {
      this.$refs.login.validate(async (valid) => {
        if (!valid) {
          return;
        }
        try {
          const { data: res } = await this.$http.post("/user/login", this.form);
          if (res.code !== 0) {
            this.$notify({
              title: "失败",
              message: "登录失败！",
              type: "error",
            });
          }
          this.$notify({
            title: "成功",
            message: "登录成功！",
            type: "success",
          });
          this.$store.commit(
            "login",
            Object.assign(res.data, { username: this.form.username })
          );
          this.$router.push({ path: "/" });
        } catch (err) {
          return this.$notify({
            title: "错误",
            message: "网络异常",
            type: "error",
          });
        }
      });
    },
  },
  mounted() {
    if (localStorage.getItem("news")) {
      this.form = JSON.parse(localStorage.getItem("news"));
      this.checked = true;
    }
  },
};
</script>

<style scoped>
.loginbody {
  width: 100%;
  height: 100%;
  min-width: 1000px;
  background-size: 100% 100%;
  background-position: center center;
  overflow: auto;
  background-repeat: no-repeat;
  position: absolute;
  top: 0;
  left: 0;
  line-height: 100%;
  padding-top: 150px;
}

.logintext {
  margin-bottom: 20px;
  line-height: 50px;
  text-align: center;
  font-size: 30px;
  font-weight: bolder;
  color: white;
  text-shadow: 2px 2px 4px #000000;
}

.logindata {
  width: 400px;
  height: 300px;
  transform: translate(-50%);
  margin-left: 50%;
}

.tool {
  display: flex;
  justify-content: space-between;
  color: #606266;
}

.butt {
  margin-top: 10px;
  text-align: center;
}

.shou {
  cursor: pointer;
  color: #606266;
}
</style>
