<template>
  <div class="header-container">
    <div class="header-content">
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
          <div class="user-profile" @click="$router.push('/profile')">
            <template v-if="$store.state.islogin && $store.state.user">
              <Icon />
              <span class="username-display">{{ $store.state.user.username }}</span>
            </template>
            <span v-else class="login-prompt">{{ $t('header.notLoggedIn') }}</span>
          </div>
          
          <template #dropdown>
            <el-dropdown-menu class="user-dropdown glass">
              <template v-if="$store.state.islogin">
                <el-dropdown-item command="home" icon="el-icon-house">{{ $t('header.dashboard') }}</el-dropdown-item>
                <el-dropdown-item command="profile" icon="el-icon-user">My Profile</el-dropdown-item>
                <el-dropdown-item v-if="$store.state.user.isAdmin" command="admin" icon="el-icon-monitor">{{ $t('header.adminPanel') }}</el-dropdown-item>
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



  </div>
</template>

<script>
import Icon from "./Icon";
export default {
  data() {
    return {
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
        case 'profile': this.$router.push("/profile"); break;
        case 'admin': this.$router.push("/manage"); break;
        case 'login': this.$router.push("/login"); break;
      }
    }
  }
};
</script>

<style scoped>
.header-container {
  padding: 15px 40px;
  width: 100%;
  box-sizing: border-box;
  flex-shrink: 0;
  z-index: 1000;
  position: relative;
}

.header-content {
  height: 70px;
  min-height: 70px;
  padding: 0 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 20px;
  background: rgba(30, 30, 30, 0.6);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.1);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  width: 100%; /* Ensure content takes full width */
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

