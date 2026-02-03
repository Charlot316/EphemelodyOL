<template>
  <div class="login-page">
    <background-display />
    <div class="login-wrapper">
      <div class="login-card glass">
        <div class="login-header">
          <h1 class="logo-text">EPHEMELODY</h1>
          <p class="subtitle">{{ $t('register.title') }}</p>
        </div>

        <div class="form-container">
          <el-form ref="register" :model="form" :rules="rules">
            <el-form-item prop="username">
              <div class="input-label">{{ $t('common.username') }}</div>
              <el-input v-model="form.username" :placeholder="$t('register.placeholderUser')"
                prefix-icon="User"></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <div class="input-label">{{ $t('common.password') }}</div>
              <el-input v-model="form.password" :placeholder="$t('register.placeholderPass')" show-password
                prefix-icon="Lock"></el-input>
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <div class="input-label">{{ $t('common.confirmPassword') }}</div>
              <el-input v-model="form.confirmPassword" :placeholder="$t('register.placeholderConfirm')" show-password
                prefix-icon="Check" @keyup.enter="register()"></el-input>
            </el-form-item>
          </el-form>
        </div>

        <div class="login-actions">
          <el-button type="primary" class="login-btn" @click="register()">{{ $t('register.submit') }}</el-button>
          <div class="register-hint">
            {{ $t('register.alreadyHave') }} <router-link to="/login" class="link-text">{{ $t('register.loginNow')
              }}</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import BackgroundDisplay from "../components/BackgroundDisplay";
export default {
  components: { BackgroundDisplay },
  name: "register",
  data() {
    return {
      form: {
        password: "",
        username: "",
        confirmPassword: "",
      },
      rules: {
        username: [
          { required: true, message: "Please enter username", trigger: "blur" },
          { max: 20, message: "Username too long", trigger: "blur" },
        ],
        password: [
          { required: true, message: "Please enter password", trigger: "blur" },
          { min: 6, message: "Min 6 characters", trigger: "blur" },
        ],
        confirmPassword: [
          { required: true, message: "Please confirm password", trigger: "blur" },
        ],
      },
    };
  },
  methods: {
    register() {
      this.$refs.register.validate(async (valid) => {
        if (!valid) return;
        if (this.form.password !== this.form.confirmPassword) {
          return this.$notify({
            title: "Error",
            message: "Passwords do not match",
            type: "error",
          });
        }
        try {
          const { hashPassword } = await import("../utils/crypto");
          const hashedPassword = await hashPassword(this.form.password);
          const registerForm = {
            username: this.form.username,
            password: hashedPassword,
          };
          const { data: res } = await this.$http.post(
            "/user/register",
            registerForm
          );
          if (res.code !== 0) {
            return this.$notify({
              title: "Failed",
              message: res.data || "Registration failed",
              type: "error",
            });
          }
          this.$notify({
            title: "Success",
            message: "Account created successfully!",
            type: "success",
          });
          this.$router.push({ path: "/login" });
        } catch (err) {
          console.error(err);
          return this.$notify({
            title: "Error",
            message: "Network Error",
            type: "error",
          });
        }
      });
    },
  },
};
</script>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.login-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 480px;
  padding: 50px 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  animation: slideIn 0.8s ease-out;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-text {
  font-size: 42px;
  font-weight: 800;
  letter-spacing: 4px;
  background: linear-gradient(to right, #00f3ff, #ff007f);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 10px;
}

.subtitle {
  color: var(--text-muted);
  font-size: 14px;
  letter-spacing: 1px;
}

.input-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 8px;
  text-transform: uppercase;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.login-btn {
  width: 100%;
  height: 54px;
  border-radius: 12px !important;
  font-size: 16px !important;
  letter-spacing: 2px !important;
  margin-top: 20px;
}

.register-hint {
  text-align: center;
  margin-top: 25px;
  font-size: 14px;
  color: var(--text-muted);
}

.link-text {
  color: var(--accent-cyan);
  font-weight: 600;
  text-decoration: none;
  margin-left: 5px;
}

.link-text:hover {
  color: var(--accent-pink);
  text-shadow: 0 0 10px rgba(255, 0, 127, 0.3);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.el-input__inner) {
  height: 50px !important;
  font-size: 15px !important;
  padding-left: 15px !important;
}
</style>
