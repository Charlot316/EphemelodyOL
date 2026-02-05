<template>
  <div class="profile-page">
    <BackgroundDisplay />
    <div class="content-overlay">
      <Header />
      <div class="main-content">
        <div class="profile-card glass">
          <div class="profile-header">
            <div class="avatar-section">
              <div class="avatar-uploader" @click="triggerUpload">
                <img v-if="userIcon" :src="userIcon" class="avatar" />
                <div v-else class="avatar-placeholder">
                  <i class="el-icon-plus"></i>
                </div>
                <div class="avatar-overlay">
                  <i class="el-icon-camera"></i>
                </div>
              </div>
              <!-- Hidden file input for cropper -->
              <input type="file" ref="fileInput" style="display: none" accept="image/*" @change="onFileChange" />
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
              <el-slider v-model="noteSpeed" :min="100" :max="2000" :step="50" :format-tooltip="formatTooltip"
                @change="saveSettings" />
            </div>

            <h3 class="section-title">{{ $t('profile.accountSecurity') }}</h3>
            <div class="setting-item">
              <el-button type="primary" @click="passwordVisible = true" class="security-btn">
                <i class="el-icon-key"></i> {{ $t('profile.changePassword') }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Change Password Dialog -->
    <el-dialog :title="$t('profile.changePassword')" v-model="passwordVisible" width="400px"
      custom-class="glass-dialog">
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

    <!-- Avatar Crop Dialog -->
    <el-dialog title="裁剪头像" v-model="cropperVisible" width="600px" custom-class="glass-dialog"
      :close-on-click-modal="false" destroy-on-close>
      <div class="cropper-wrapper">
        <vue-cropper ref="cropper" :src="imgSrc" :aspect-ratio="1" :view-mode="1" drag-mode="move" :auto-crop-area="1"
          style="width: 100%; height: 400px;" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cropperVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="cropAndUpload" :loading="uploading">确认上传</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import Header from "../components/Header";
import BackgroundDisplay from "@/components/BackgroundDisplay";
import VueCropper from 'vue-cropperjs';
import 'cropperjs/dist/cropper.css';

export default {
  components: { Header, BackgroundDisplay, VueCropper },
  data() {
    return {
      noteSpeed: 1000,
      passwordVisible: false,
      passwordForm: {
        oldPassword: "",
        newPassword: ""
      },
      cropperVisible: false,
      imgSrc: "",
      uploading: false
    };
  },
  computed: {
    username() {
      return this.$store.state.user.username || this.$t('profile.guest');
    },
    userIcon() {
      // 兼容后端返回的 icon 字段和前端可能存的 iconUrl
      return this.$store.state.user.icon || this.$store.state.user.iconUrl;
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
    triggerUpload() {
      this.$refs.fileInput.click();
    },
    onFileChange(e) {
      const file = e.target.files[0];
      if (!file) return;
      if (!file.type.startsWith('image/')) {
        return this.$message.error(this.$t('profile.formatError'));
      }
      // 放宽到 10MB
      if (file.size > 10 * 1024 * 1024) {
        return this.$message.error(this.$t('profile.sizeError') + " (Max 10MB)");
      }

      const reader = new FileReader();
      reader.onload = (event) => {
        this.imgSrc = event.target.result;
        this.cropperVisible = true;
        // 重置 input 以便下次选择同一文件
        e.target.value = '';
      };
      reader.readAsDataURL(file);
    },
    async cropAndUpload() {
      const canvas = this.$refs.cropper.getCroppedCanvas({
        width: 400,
        height: 400
      });

      this.uploading = true;
      canvas.toBlob(async (blob) => {
        const formData = new FormData();
        formData.append('file', blob, 'avatar.png');

        try {
          const { data: res } = await this.$http.post('/user/uploadIcon', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });
          if (res.code === 0) {
            this.$message.success(this.$t('profile.avatarSuccess'));
            this.$store.commit("changeParam", { key: "icon", value: res.data.url });
            this.cropperVisible = false;
          } else {
            this.$message.error(res.msg || this.$t('profile.uploadFailed'));
          }
        } catch (err) {
          this.$message.error(this.$t('common.error'));
        } finally {
          this.uploading = false;
        }
      }, 'image/png');
    },
    beforeAvatarUpload(file) {
      const isValid = file.type === "image/jpeg" || file.type === "image/png";
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isValid) this.$message.error(this.$t('profile.formatError'));
      if (!isLt10M) this.$message.error(this.$t('profile.sizeError') + " (Max 10MB)");
      return isValid && isLt10M;
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
  position: absolute;
  inset: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
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
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 40px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 30px;
  padding-bottom: 30px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.avatar-section {
  position: relative;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  border: 3px solid rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.avatar-uploader:hover {
  border-color: var(--accent-cyan);
  transform: scale(1.05);
  box-shadow: var(--glow-cyan);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: center;
  align-items: center;
  color: #aaa;
  font-size: 32px;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
  opacity: 0;
  transition: 0.3s;
}

.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.user-info h1 {
  font-size: 32px;
  font-weight: 800;
  margin: 0 0 12px;
  color: #fff;
  letter-spacing: -0.5px;
}

.section-title {
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 800;
  letter-spacing: 2px;
  margin-bottom: 24px;
  text-transform: uppercase;
}

.setting-item {
  margin-bottom: 32px;
}

.setting-label {
  display: flex;
  justify-content: space-between;
  color: #f8fafc;
  font-weight: 600;
  margin-bottom: 10px;
  font-size: 15px;
}

.setting-value {
  color: var(--accent-cyan);
  font-family: 'Outfit', sans-serif;
}

.setting-desc {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 20px;
  line-height: 1.5;
}

.security-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  border-radius: 12px;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.security-btn:hover {
  transform: translateY(-2px);
  filter: brightness(1.1);
}
</style>
