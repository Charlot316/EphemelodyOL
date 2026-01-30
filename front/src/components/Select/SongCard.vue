<template>
  <router-link
    :to="{ path: '/play', query: { songId: song.songInfo.songId } }"
    class="card-link"
  >
    <div class="song-card glass" @mouseenter="mouseEnter = true" @mouseleave="mouseEnter = false">
      <div class="image-container">
        <img :src="song.songInfo.defaultBackground" class="card-bg" />
        <div class="difficulty-badge" :class="getDifficultyClass(song.songInfo.songDifficulty)">
          {{ song.songInfo.songDifficulty }}
        </div>
      </div>
      
      <div class="song-info">
        <div class="main-info">
          <img :src="song.songInfo.songCover" class="mini-cover" />
          <div class="title-area">
            <h4 class="song-title">{{ song.songInfo.songName }}</h4>
            <span class="artist">{{ song.songInfo.songWriter }}</span>
          </div>
        </div>
        
        <div class="stats-row">
          <span class="uploader"><i class="el-icon-user"></i> {{ song.songInfo.uploader }}</span>
        </div>
      </div>

      <div class="rank-overlay" :class="{ 'is-active': mouseEnter }">
        <div class="rank-content">
          <div class="rank-header">TOP RANKING</div>
          <div class="rank-list">
            <div v-for="(record, index) in song.tenBestRecords" :key="index" class="rank-item">
              <span class="rank-num">#{{ index + 1 }}</span>
              <span class="rank-name">{{ record.player }}</span>
              <span class="rank-score">{{ record.score }}</span>
            </div>
          </div>
          <div class="my-rank-section">
            <div class="rank-header">MY BEST</div>
            <div class="rank-item mine">
              <span class="rank-name">{{ $store.state.user.username }}</span>
              <span class="rank-score">{{ song.myRecord.score || 'NO DATA' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </router-link>
</template>

<script>
export default {
  props: ["song"],
  data() {
    return {
      mouseEnter: false,
    };
  },
  methods: {
    getDifficultyClass(diff) {
      if (diff < 5) return 'diff-easy';
      if (diff < 10) return 'diff-normal';
      if (diff < 14) return 'diff-hard';
      return 'diff-insane';
    }
  }
};
</script>

<style scoped>
.card-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.song-card {
  height: 280px;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.song-card:hover {
  transform: translateY(-8px);
  border-color: var(--accent-cyan);
}

.image-container {
  height: 160px;
  position: relative;
}

.card-bg {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.song-card:hover .card-bg {
  transform: scale(1.1);
}

.difficulty-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 4px 12px;
  border-radius: 8px;
  font-weight: 800;
  font-size: 14px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}

.diff-easy { background: #22c55e; color: white; }
.diff-normal { background: #3b82f6; color: white; }
.diff-hard { background: #eab308; color: white; }
.diff-insane { background: #ef4444; color: white; text-shadow: 0 0 5px rgba(255,255,255,0.5); }

.song-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.main-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.mini-cover {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
}

.title-area {
  flex: 1;
  min-width: 0;
}

.song-title {
  font-size: 16px;
  font-weight: 700;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.artist {
  font-size: 12px;
  color: var(--text-muted);
}

.stats-row {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  justify-content: flex-end;
}

.rank-overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(8px);
  transform: translateY(100%);
  transition: transform 0.4s cubic-bezier(0.19, 1, 0.22, 1);
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.rank-overlay.is-active {
  transform: translateY(0);
}

.rank-header {
  font-size: 12px;
  font-weight: 800;
  color: var(--accent-cyan);
  letter-spacing: 2px;
  margin-bottom: 12px;
}

.rank-list {
  flex: 1;
  overflow-y: auto;
  font-size: 13px;
}

.rank-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.rank-num { font-weight: 800; width: 30px; }
.rank-name { flex: 1; color: var(--text-muted); }
.rank-score { font-family: monospace; color: var(--accent-pink); }

.my-rank-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid var(--glass-border);
}

.rank-item.mine .rank-name {
  color: var(--text-main);
  font-weight: 600;
}
</style>

