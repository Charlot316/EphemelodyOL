<template>
  <div class="header-container">
    <div class="header-content glass">
      <div class="left-section" @click="$router.push({ path: '/' })">
        <h1 class="header-logo">EPHEMELODY</h1>
      </div>
      
      <div class="right-section">
        <el-dropdown trigger="hover" @command="handleLangCommand" class="lang-dropdown">
          <div class="lang-selector">
            <i class="el-icon-discover"></i>
            <span>{{ $i18n.locale.toUpperCase() }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="user-dropdown glass">
              <el-dropdown-item command="zh">中文 (ZH)</el-dropdown-item>
              <el-dropdown-item command="en">English (EN)</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-dropdown trigger="hover" @command="handleCommand">
          <div class="user-profile">
            <template v-if="$store.state.islogin">
              <Icon />
              <span class="username-display">{{ $store.state.user.username }}</span>
            </template>
            <span v-else class="login-prompt">{{ $t('header.notLoggedIn') }}</span>
          </div>
          
          <template #dropdown>
            <el-dropdown-menu class="user-dropdown glass">
              <template v-if="$store.state.islogin">
                <el-dropdown-item command="home" icon="el-icon-house">{{ $t('header.dashboard') }}</el-dropdown-item>
                <el-dropdown-item v-if="$store.state.user.isAdmin" command="admin" icon="el-icon-monitor">{{ $t('header.adminPanel') }}</el-dropdown-item>
                <el-dropdown-item divided command="changepassword" icon="el-icon-key">{{ $t('header.resetPassword') }}</el-dropdown-item>
                <el-dropdown-item command="uploadicon" icon="el-icon-picture-outline">{{ $t('header.changeAvatar') }}</el-dropdown-item>
                <el-dropdown-item divided command="loginout" icon="el-icon-switch-button" class="logout-item">{{ $t('common.logout') }}</el-dropdown-item>
              </template>
              <template v-else>
                <el-dropdown-item command="login" icon="el-icon-user">{{ $t('header.goToLogin') }}</el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- Password Dialog -->
    <el-dialog :title="$t('header.resetPassword')" v-model="editVisible_changepassword" width="450px" class="glass-dialog">
      <div class="dialog-body">
        <el-form label-position="top" :model="param">
          <el-form-item :label="$t('header.currentPass')">
            <el-input type="password" v-model="param.oldPassword" show-password :placeholder="$t('header.passPlaceholder')"></el-input>
          </el-form-item>
          <el-form-item :label="$t('header.newPass')">
            <el-input type="password" v-model="param.newPassword" show-password :placeholder="$t('header.newPassPlaceholder')" @keyup.enter="changePassword()"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="quitChangePassword" size="medium">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="changePassword()" size="medium">{{ $t('common.update') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Avatar Dialog -->
    <el-dialog :title="$t('header.avatarTitle')" v-model="editVisible_uploadIcon" width="400px" class="glass-dialog">
      <div class="upload-area">
        <el-upload
          class="avatar-uploader"
          :action="$http.defaults.baseURL + '/user/uploadIcon'"
          with-credentials
          name="file"
          accept=".jpg,.png"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <div class="upload-placeholder" v-if="!imgUrl">
            <i class="el-icon-plus"></i>
            <p>{{ $t('header.avatarDesc') }}</p>
          </div>
          <img v-else :src="imgUrl" class="preview-avatar" />
        </el-upload>
      </div>
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
      imgUrl: "",
    };
  },
  components: { Icon },
  methods: {
    handleLangCommand(lang) {
      this.$i18n.locale = lang;
      localStorage.setItem('lang', lang);
    },
    handleCommand(command) {
      switch(command) {
        case 'loginout': 
          this.$store.commit("loginout");
          this.$router.push("/login");
          break;
        case 'home': this.$router.push("/"); break;
        case 'admin': this.$router.push("/manage"); break;
        case 'changepassword': this.editVisible_changepassword = true; break;
        case 'uploadicon': this.editVisible_uploadIcon = true; break;
        case 'login': this.$router.push("/login"); break;
      }
    },
    async changePassword() {
      if (!this.param.oldPassword || !this.param.newPassword) {
        return this.$message.warning("Please fill all fields");
      }
      try {
        const { hashPassword } = await import("../utils/crypto");
        const hashedOld = await hashPassword(this.param.oldPassword);
        const hashedNew = await hashPassword(this.param.newPassword);
        
        const { data: res } = await this.$http.post("/user/changePassword", {
          oldPassword: hashedOld,
          newPassword: hashedNew
        });
        
        if (res.code === 0) {
          this.$notify({ title: "Success", message: "Password updated", type: "success" });
          this.editVisible_changepassword = false;
        } else {
          this.$notify({ title: "Failed", message: res.data, type: "error" });
        }
      } catch (err) {
        this.$notify({ title: "Error", message: "Action failed", type: "error" });
      }
    },
    handleAvatarSuccess(res) {
      this.$message.success("Avatar updated");
      this.editVisible_uploadIcon = false;
      this.getUserInformation();
    },
    beforeAvatarUpload(file) {
      const isValid = file.type === "image/jpeg" || file.type === "image/png";
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isValid) this.$message.error("JPG or PNG only!");
      if (!isLt2M) this.$message.error("Max 2MB!");
      return isValid && isLt2M;
    },
    async getUserInformation() {
      const { data: res } = await this.$http.post("/user/getUserInformation");
      if (res.code === 0) {
        this.$store.commit("changeParam", { key: "icon", value: res.data.iconUrl });
      }
    }
  }
};
</script>

<style scoped>
.header-container {
  padding: 15px 40px;
  width: 100%;
}

.header-content {
  height: 70px;
  padding: 0 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 20px;
}

.header-logo {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 4px;
  cursor: pointer;
  background: linear-gradient(to right, #fff, var(--accent-cyan));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  transition: opacity 0.3s;
}

.header-logo:hover {
  opacity: 0.8;
}

.right-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.lang-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 10px;
  background: rgba(255,255,255,0.05);
  font-size: 13px;
  font-weight: 700;
  transition: all 0.3s;
}

.lang-selector:hover {
  background: rgba(255,255,255,0.1);
  color: var(--accent-cyan);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  padding: 8px 15px;
  border-radius: 12px;
  transition: background 0.3s;
}

.user-profile:hover {
  background: rgba(255,255,255,0.05);
}

.username-display {
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.login-prompt {
  font-weight: 800;
  font-size: 13px;
  color: var(--accent-cyan);
}

.logout-item {
  color: #ff4d4f !important;
}

/* Dialog & Upload Styles */
.dialog-body { padding: 10px 0; }
.upload-area {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.avatar-uploader {
  width: 160px;
  height: 160px;
  border: 2px dashed var(--glass-border);
  border-radius: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: var(--accent-cyan);
}

.upload-placeholder {
  text-align: center;
  color: var(--text-muted);
}

.upload-placeholder i { font-size: 32px; margin-bottom: 10px; }
.upload-placeholder p { font-size: 12px; }

.preview-avatar {
  width: 100%;
  height: 100%;
  border-radius: 18px;
  object-fit: cover;
}
</style>

