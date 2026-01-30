<template>
  <div class="home-page">
    <BackgroundDisplay />
    <div class="content-overlay">
      <Header />
      <main class="main-content">
        <div class="welcome-section">
          <h1 class="glitch-text">SELECT YOUR JOURNEY</h1>
          <p class="subtitle">Where every beat tells an eternal story</p>
        </div>

        <div class="nav-grid">
          <div class="nav-card glass" @click="$router.push({path:'/public'})">
            <div class="icon-box cyan-glow">
              <i class="el-icon-video-play"></i>
            </div>
            <h3>Official</h3>
            <p>Master the original rhythm collections</p>
            <div class="card-footer">PLAY NOW</div>
          </div>

          <div class="nav-card glass" @click="$router.push({path:'/society'})">
            <div class="icon-box pink-glow">
              <i class="el-icon-ship"></i>
            </div>
            <h3>Community</h3>
            <p>Explore thousands of player-created charts</p>
            <div class="card-footer">EXPLORE</div>
          </div>

          <div class="nav-card glass" @click="$router.push({path:'/admin'})">
            <div class="icon-box purple-glow">
              <i class="el-icon-setting"></i>
            </div>
            <h3>Workshop</h3>
            <p>Create and manage your own melodies</p>
            <div class="card-footer">MANAGE</div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script>
import Header from "../components/Header";
import BackgroundDisplay from "@/components/BackgroundDisplay";
export default {
  components: {
    Header,
    BackgroundDisplay,
  },
};
</script>

<style scoped>
.home-page {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.content-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0 40px;
}

.welcome-section {
  text-align: center;
  margin-bottom: 60px;
}

.welcome-section h1 {
  font-size: 56px;
  font-weight: 800;
  letter-spacing: 6px;
  margin-bottom: 10px;
  background: linear-gradient(to right, #fff, var(--accent-cyan));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  max-width: 1200px;
  width: 100%;
}

.nav-card {
  padding: 40px;
  cursor: pointer;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.nav-card:hover {
  transform: translateY(-15px) scale(1.02);
  border-color: var(--accent-cyan);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
}

.icon-box {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 32px;
  margin-bottom: 25px;
}

.cyan-glow { background: rgba(0, 243, 255, 0.1); color: var(--accent-cyan); box-shadow: 0 0 20px rgba(0, 243, 255, 0.2); }
.pink-glow { background: rgba(255, 0, 127, 0.1); color: var(--accent-pink); box-shadow: 0 0 20px rgba(255, 0, 127, 0.2); }
.purple-glow { background: rgba(126, 34, 206, 0.1); color: #a855f7; box-shadow: 0 0 20px rgba(126, 34, 206, 0.2); }

.nav-card h3 {
  font-size: 24px;
  margin-bottom: 15px;
  font-weight: 600;
}

.nav-card p {
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 30px;
}

.card-footer {
  font-weight: 800;
  font-size: 12px;
  letter-spacing: 2px;
  color: var(--accent-cyan);
  border-top: 1px solid var(--glass-border);
  padding-top: 20px;
  width: 100%;
}
</style>

