<template>
  <div class="manage-view">
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
            <el-select v-model="sort" :placeholder="$t('select.sortBy')" class="sort-select" @change="getCharts()">
              <el-option :label="$t('select.name') + ' (A-Z)'" value="00"></el-option>
              <el-option :label="$t('select.name') + ' (Z-A)'" value="01"></el-option>
              <el-option :label="$t('select.artist') + ' (A-Z)'" value="10"></el-option>
              <el-option :label="$t('select.artist') + ' (Z-A)'" value="11"></el-option>
              <el-option :label="$t('select.level') + ' (Low-High)'" value="30"></el-option>
              <el-option :label="$t('select.level') + ' (High-Low)'" value="31"></el-option>
            </el-select>
          </div>
        </div>
      </div>

      <div class="management-container">
        <div class="management-list">
          <div v-for="item in songs" :key="item.songId" class="manage-card glass">
            <div class="card-left">
              <el-image :src="item.songCover" class="manage-cover" fit="cover"></el-image>
              <div class="meta-info">
                <h4 class="song-title">{{ item.songName }}</h4>
                <p class="song-artist">{{ item.songWriter }}</p>
                <div class="status-tags">
                  <el-tag size="mini" :type="item.status === 2 ? 'success' : 'info'" effect="dark">
                    {{ item.status === 2 ? 'CERTIFIED' : 'PENDING' }}
                  </el-tag>
                  <span class="constant">CONSTANT: {{ item.chartConstant }}</span>
                </div>
              </div>
            </div>

            <div class="card-details">
              <div class="text-info">
                <span><i class="el-icon-chat-dot-round"></i> {{ item.loadingText || 'NO TEXT' }}</span>
                <span><i class="el-icon-check"></i> {{ item.loadedText || 'NO TEXT' }}</span>
              </div>
            </div>

            <div class="card-actions">
              <el-button-group>
                <el-button type="primary" icon="el-icon-edit" size="medium" @click="getEdit(item)"></el-button>
                <el-button type="danger" icon="el-icon-delete" size="medium" @click="deleteSong(item.songId)"></el-button>
              </el-button-group>
            </div>
          </div>
        </div>
      </div>

      <el-dialog title="MANAGE STATUS" v-model="editVisible" width="400px">
        <div class="dialog-content">
          <p class="dialog-desc">Certify this chart to push it to official collection.</p>
          <div class="switch-row">
            <span>Certification Status</span>
            <el-switch v-model="value" active-color="#00f3ff"></el-switch>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="editVisible = false" size="medium">CANCEL</el-button>
            <el-button type="primary" @click="editStatus()" size="medium">UPDATE</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import BackgroundDisplay from "../components/BackgroundDisplay";
import Header from "../components/Header";
export default {
  components: { BackgroundDisplay, Header },
  data() {
    return {
      sort: "00",
      params: {
        status: "1",
        searchType: "0",
        searchContent: "",
        sortType: "0",
        sortWay: "0",
      },
      songs: [],
      editVisible: false,
      selectedSongId: "",
      selectedSong: null,
      value: false,
    };
  },
  created() {
    if (!this.$store.state.user.isAdmin) {
      this.$router.push("/home");
    }
    this.getAllCharts();
  },
  methods: {
    async getAllCharts() {
      const { data: res } = await this.$http.post("/user/getAllCharts");
      if (res.code == 0) {
        this.songs = res.data.charts;
      } else {
        this.$message.error("Fetch failed");
      }
    },
    async getCharts() {
      this.params.sortType = this.sort.substr(0, 1);
      this.params.sortWay = this.sort.substr(1, 1);
      const { data: res } = await this.$http.post("/user/getPublicCharts", this.params);
      if (res.code == 0) {
        this.songs = res.data.songs.map(s => s.songInfo);
      } else {
        this.$message.error("Search failed");
      }
    },
    getEdit(item) {
      this.selectedSongId = item.songId;
      this.selectedSong = item;
      this.value = item.status === 2;
      this.editVisible = true;
    },
    async delete(songId) {
      const { data: res } = await this.$http.post("/user/deleteChart", { songId });
      if (res.code === 0) {
        this.$message.success("Deleted successfully");
        this.getAllCharts();
      } else {
        this.$message.error("Delete failed");
      }
    },
    deleteSong(songId) {
      this.$confirm("Permanently delete this chart?", "WARNING", {
        confirmButtonText: "DELETE",
        cancelButtonText: "CANCEL",
        type: "warning",
      }).then(() => {
        this.delete(songId);
      });
    },
    async editStatus() {
      const endpoint = (this.selectedSong.status == 1 && this.value) 
          ? "/admin/accreditChart" 
          : (this.selectedSong.status == 2 && !this.value)
          ? "/admin/disaccreditChart" : null;

      if (endpoint) {
        const { data: res } = await this.$http.post(endpoint, { songId: this.selectedSongId });
        if (res.code == 0) this.$message.success("Updated successfully");
      }
      this.getAllCharts();
      this.editVisible = false;
    },
  },
};
</script>

<style scoped>
.manage-view {
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
  gap: 20px;
}

.search-section { flex: 1; display: flex; gap: 10px; }
.type-select { width: 120px; }
.search-input { flex: 1; }
.sort-select { width: 180px; }

.management-container {
  flex: 1;
  overflow-y: auto;
  padding: 0 40px 60px;
}

.management-list {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.manage-card {
  padding: 20px 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
}

.manage-card:hover {
  border-color: var(--accent-cyan);
  background: rgba(255, 255, 255, 0.08);
}

.card-left {
  display: flex;
  gap: 20px;
  align-items: center;
  flex: 1;
}

.manage-cover {
  width: 100px;
  height: 100px;
  border-radius: 12px;
}

.song-title { font-size: 20px; margin: 0 0 5px; font-weight: 700; }
.song-artist { color: var(--text-muted); font-size: 14px; margin-bottom: 12px; }

.status-tags { display: flex; gap: 10px; align-items: center; }
.constant { font-size: 11px; font-weight: 800; color: var(--accent-cyan); }

.card-details { flex: 1; color: var(--text-muted); font-size: 13px; }
.text-info { display: flex; flex-direction: column; gap: 8px; }
.text-info i { margin-right: 8px; color: var(--accent-cyan); }

.card-actions { width: 120px; text-align: right; }

.dialog-content { padding: 10px 0; }
.dialog-desc { color: var(--text-muted); font-size: 14px; margin-bottom: 25px; line-height: 1.6; }
.switch-row { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }

.dialog-footer { padding-top: 20px; }
</style>
