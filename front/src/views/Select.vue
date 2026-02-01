<template>
  <div class="select-view">
    <background-display />
    <div class="view-content">
      <Header />
      
      <div class="filter-header-wrapper">
        <div class="filter-header glass">
          <div class="search-section">
            <el-select v-model="params.searchType" :placeholder="$t('select.type')" class="type-select">
              <el-option :label="$t('select.name')" value="0"></el-option>
              <el-option :label="$t('select.artist')" value="1"></el-option>
              <el-option :label="$t('select.uploader')" value="2"></el-option>
              <el-option :label="$t('select.level')" value="3"></el-option>
            </el-select>
            <el-input
              :placeholder="$t('select.searchMelodies')"
              v-model="params.searchContent"
              class="search-input"
              @keyup.enter="getCharts()"
            >
              <template #append>
                <el-button icon="el-icon-search" @click="getCharts()"></el-button>
              </template>
            </el-input>
          </div>

          <div class="sort-section">
            <el-select v-model="params.sortType" :placeholder="$t('select.sortBy')" class="sort-select">
              <el-option :label="$t('select.name')" value="0"></el-option>
              <el-option :label="$t('select.artist')" value="1"></el-option>
              <el-option :label="$t('select.popularity')" value="4"></el-option>
            </el-select>
            <el-select v-model="params.sortWay" :placeholder="$t('select.order')" class="order-select">
              <el-option :label="$t('select.asc')" value="0"></el-option>
              <el-option :label="$t('select.desc')" value="1"></el-option>
            </el-select>
          </div>
        </div>
      </div>

      <div class="songs-grid-container">
        <transition-group name="list" tag="div" class="songs-grid">
          <div v-for="song in songs" :key="song.songInfo.songId" class="card-wrapper">
            <song-card :song="song" />
          </div>
        </transition-group>
        <div v-if="songs.length === 0" class="no-data">
          <i class="el-icon-info"></i>
          <p>{{ $t('select.noData') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Header from "../components/Header";
import SongCard from "../components/Select/SongCard";
import BackgroundDisplay from "../components/BackgroundDisplay";

export default {
  components: { SongCard, Header, BackgroundDisplay },
  data() {
    return {
      params: {
        status: "0",
        searchType: "0",
        searchContent: "",
        sortType: "4",
        sortWay: "1",
      },
      songs: [],
    };
  },
  created() {
    if (this.$route.path == "/public") {
      this.params.status = "2";
    } else if (this.$route.path == "/society") {
      this.params.status = "1";
    }
    this.getCharts();
  },
  methods: {
    async getCharts() {
      const { data: res } = await this.$http.post(
        "/user/getPublicCharts",
        this.params
      );
      if (res.code == 0) {
        this.songs = res.data.songs;
      } else {
        this.$message.error("Fetch failed");
      }
    },
  },
};
</script>

<style scoped>
.select-view {
  width: 100vw;
  height: 100vh;
  position: relative;
}

.view-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.filter-header-wrapper {
  padding: 20px 40px;
  display: flex;
  justify-content: center;
}

.filter-header {
  width: 100%;
  max-width: 1200px;
  padding: 15px 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.search-section {
  display: flex;
  flex: 1;
  gap: 10px;
}

.sort-section {
  display: flex;
  gap: 10px;
}

.type-select { width: 120px; }
.search-input { flex: 1; }
.sort-select { width: 140px; }
.order-select { width: 100px; }

.songs-grid-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px 40px 60px;
}

.songs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 30px;
  max-width: 1400px;
  margin: 0 auto;
}

.no-data {
  text-align: center;
  padding: 100px;
  color: var(--text-muted);
}

.no-data i {
  font-size: 48px;
  margin-bottom: 20px;
}

/* Animations */
.list-enter-active, .list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from, .list-leave-to {
  opacity: 0;
  transform: translateY(30px);
}
</style>

