<template>
  <div class="profile-page">
    <BackgroundDisplay />
    <div class="content-overlay">
      <Header />
      <div class="main-content">
        <div class="profile-card glass">
          <div class="profile-header">
            <div class="avatar-section">
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
                <img v-if="userIcon" :src="userIcon" class="avatar" />
                <div v-else class="avatar-placeholder">
                  <i class="el-icon-plus"></i>
                </div>
                <div class="avatar-overlay">
                  <i class="el-icon-camera"></i>
                </div>
              </el-upload>
            </div>
            <div class="user-info">
              <h1 class="username">{{ username }}</h1>
              <el-tag size="small" type="success" v-if="isAdmin">{{ $t('profile.admin') }}</el-tag>
              <el-tag size="small" type="info" v-else>{{ $t('profile.player') }}</el-tag>
            </div>
          </div>

          <div class="settings-section">
            <h3 class="section-title">{{ $t('profile.gameSettings') }}</h3>
            
            <div class="setting-item">
              <div class="setting-label">
                <span>{{ $t('profile.noteSpeed') }}</span>
                <span class="setting-value">{{ noteSpeed }}ms</span>
              </div>
              <p class="setting-desc">{{ $t('profile.noteSpeedDesc') }}</p>
              <el-slider 
                v-model="noteSpeed" 
                :min="100" 
                :max="2000" 
                :step="50" 
                :format-tooltip="formatTooltip"
                @change="saveSettings"
              />
            </div>

            <h3 class="section-title">{{ $t('profile.accountSecurity') }}</h3>
            <div class="setting-item">
              <el-button type="primary" plain @click="passwordVisible = true" class="security-btn">
                <i class="el-icon-key"></i> {{ $t('profile.changePassword') }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Change Password Dialog -->
    <el-dialog :title="$t('profile.changePassword')" v-model="passwordVisible" width="400px" custom-class="glass-dialog">
      <el-form label-position="top" :model="passwordForm">
        <el-form-item :label="$t('profile.currentPassword')">
          <el-input type="password" v-model="passwordForm.oldPassword" show-password></el-input>
        </el-form-item>
        <el-form-item :label="$t('profile.newPassword')">
          <el-input type="password" v-model="passwordForm.newPassword" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="changePassword">{{ $t('common.update') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import Header from "../components/Header";
import BackgroundDisplay from "@/components/BackgroundDisplay";

export default {
  components: { Header, BackgroundDisplay },
  data() {
    return {
      noteSpeed: 1000,
      passwordVisible: false,
      passwordForm: {
        oldPassword: "",
        newPassword: ""
      }
    };
  },
  computed: {
    username() {
      return this.$store.state.user.username || this.$t('profile.guest');
    },
    userIcon() {
      return this.$store.state.user.iconUrl;
    },
    isAdmin() {
      return this.$store.state.user.isAdmin === 1;
    }
  },
  mounted() {
    const savedSpeed = localStorage.getItem('noteSpeed');
    if (savedSpeed) {
      this.noteSpeed = parseInt(savedSpeed);
    }
  },
  methods: {
    saveSettings() {
      localStorage.setItem('noteSpeed', this.noteSpeed);
      this.$message.success(this.$t('profile.saveSuccess'));
    },
    formatTooltip(val) {
      return `${val}ms`;
    },
    handleAvatarSuccess(res) {
      if(res.code === 0) {
        this.$message.success(this.$t('profile.avatarSuccess'));
        this.$store.commit("changeParam", { key: "icon", value: res.data.iconUrl });
      } else {
        this.$message.error(res.msg || this.$t('profile.uploadFailed'));
      }
    },
    beforeAvatarUpload(file) {
      const isValid = file.type === "image/jpeg" || file.type === "image/png";
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isValid) this.$message.error(this.$t('profile.formatError'));
      if (!isLt2M) this.$message.error(this.$t('profile.sizeError'));
      return isValid && isLt2M;
    },
    async changePassword() {
      if (!this.passwordForm.oldPassword || !this.passwordForm.newPassword) {
        return this.$message.warning(this.$t('profile.fillAll'));
      }
      try {
        const { hashPassword } = await import("../utils/crypto");
        const hashedOld = await hashPassword(this.passwordForm.oldPassword);
        const hashedNew = await hashPassword(this.passwordForm.newPassword);
        
        const { data: res } = await this.$http.post("/user/changePassword", {
          oldPassword: hashedOld,
          newPassword: hashedNew
        });
        
        if (res.code === 0) {
          this.$message.success(this.$t('profile.passSuccess'));
          this.passwordVisible = false;
          this.passwordForm = { oldPassword: "", newPassword: "" };
        } else {
          this.$message.error(res.data || this.$t('common.failed'));
        }
      } catch (err) {
        this.$message.error(this.$t('common.error'));
      }
    }
  }
};
</script>

<style scoped>
.profile-page {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
}
.content-overlay {
  position: absolute; inset: 0; z-index: 10; display: flex; flex-direction: column;
}
.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}
.profile-card {
  width: 100%;
  max-width: 600px;
  background: rgba(30, 30, 30, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 40px;
  max-height: 80vh;
  overflow-y: auto;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 30px;
  padding-bottom: 30px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.avatar-section {
  position: relative;
}
.avatar-uploader {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  border: 2px solid rgba(255,255,255,0.1);
  transition: border-color 0.3s;
}
.avatar-uploader:hover {
  border-color: var(--accent-cyan);
}
.avatar {
  width: 100%; height: 100%; object-fit: cover;
}
.avatar-placeholder {
  width: 100%; height: 100%; background: rgba(255,255,255,0.05); display: flex; justify-content: center; align-items: center; color: #aaa; font-size: 24px;
}
.avatar-overlay {
  position: absolute; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; color: #fff; opacity: 0; transition: 0.3s;
}
.avatar-uploader:hover .avatar-overlay { opacity: 1; }

.user-info h1 {
  font-size: 28px; font-weight: 700; margin: 0 0 10px; color: #fff;
}

.section-title {
  font-size: 14px; color: var(--text-muted); font-weight: 800; letter-spacing: 1px; margin-bottom: 20px;
}

.setting-item {
  margin-bottom: 30px;
}
.setting-label {
  display: flex; justify-content: space-between; color: #eee; font-weight: 600; margin-bottom: 8px;
}
.setting-value {
  color: var(--accent-cyan);
}
.setting-desc {
  font-size: 12px; color: #888; margin-bottom: 15px;
}
.security-btn {
  width: 100%; justify-content: center;
}
</style>
