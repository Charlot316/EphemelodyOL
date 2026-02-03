<template>
  <div class="admin-view">
    <background-display />
    <div class="view-content">
      <AppHeader />
      
      <div class="filter-header-wrapper">
        <div class="filter-header glass">
          <div class="left-actions">
            <el-button type="primary" icon="el-icon-plus" @click="getAdd()">NEW CHART</el-button>
          </div>
          
          <div class="search-section">
            <el-select v-model="params.searchType" placeholder="Type" class="type-select">
              <el-option label="Name" value="0"></el-option>
              <el-option label="Artist" value="1"></el-option>
              <el-option label="Level" value="3"></el-option>
            </el-select>
            <el-input
              placeholder="Search your rhythms..."
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
            <el-select v-model="sort" placeholder="Sort" class="sort-select" @change="getCharts()">
              <el-option label="Name Asc" value="00"></el-option>
              <el-option label="Name Desc" value="01"></el-option>
              <el-option label="Level Asc" value="30"></el-option>
              <el-option label="Level Desc" value="31"></el-option>
            </el-select>
          </div>
        </div>
      </div>

      <div class="management-container">
        <div class="management-list">
          <div v-for="item in songs" :key="item.songId" class="manage-card glass">
            <div class="card-left" @click="next(item.songId)">
              <div class="cover-wrapper">
                <el-image :src="item.songCover" class="manage-cover" fit="cover"></el-image>
                <div class="hover-overlay"><i class="el-icon-edit-outline"></i> EDIT CHART</div>
              </div>
              <div class="meta-info">
                <h4 class="song-title">{{ item.songName }}</h4>
                <p class="song-artist">{{ item.songWriter }}</p>
                <div class="status-tags">
                  <el-tag size="small" :type="getStatusType(item.status)" effect="dark">
                    {{ getStatusLabel(item.status) }}
                  </el-tag>
                  <span class="constant">CONSTANT: {{ item.chartConstant }}</span>
                </div>
              </div>
            </div>

            <div class="card-actions">
              <div class="action-group">
                <button class="action-btn edit-btn" @click="getEdit(item.songId, item)">EDIT</button>
                <button class="action-btn delete-btn" @click="deleteSong(item.songId)">DELETE</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Edit Info Dialog -->
      <el-dialog title="CHART SETTINGS" v-model="editVisible" width="500px">
        <el-form label-position="top" :model="form" class="settings-form">
          <div class="switch-row">
            <span>PUBLIC VISIBILITY</span>
            <el-switch v-model="value" active-color="#00f3ff" :disabled="selectedSong.status === 2"></el-switch>
          </div>
          
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="MELODY NAME">
                <el-input v-model="form.songName"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="ARTIST / WRITER">
                <el-input v-model="form.songWriter"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="upload-buttons">
            <el-button type="info" plain size="small" icon="el-icon-headset" @click="uploadSongVisible = true">Audio</el-button>
            <el-button type="info" plain size="small" icon="el-icon-picture" @click="uploadBackVisible = true">Background</el-button>
            <el-button type="info" plain size="small" icon="el-icon-picture-outline" @click="uploadCoverVisible = true">Cover</el-button>
          </div>

          <el-form-item label="CHART CONSTANT">
            <el-input v-model="form.chartConstant"></el-input>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="LOADING TEXT">
                <el-input v-model="form.loadingText"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="LOADED TEXT">
                <el-input v-model="form.loadedText"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="editVisible = false">CANCEL</el-button>
            <el-button type="primary" @click="editSongInfo()">SAVE CHANGES</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Add Dialog -->
      <el-dialog title="CREATE NEW MELODY" v-model="addVisible" width="500px">
        <el-form label-position="top" :model="newSong">
          <el-form-item label="MELODY NAME">
            <el-input v-model="newSong.songName" placeholder="Enter melody title"></el-input>
          </el-form-item>
          <el-form-item label="ARTIST">
            <el-input v-model="newSong.songWriter" placeholder="Artist name"></el-input>
          </el-form-item>
          <p class="hint">You can upload assets later in the settings.</p>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="addVisible = false">CANCEL</el-button>
            <el-button type="primary" @click="addSong()">CREATE</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Asset Upload Dialogs (Simpler) -->
      <el-dialog :title="'UPLOAD ASSET'" v-model="assetVisible" width="400px">
        <el-upload
          class="asset-uploader"
          :action="uploadUrl"
          with-credentials
          name="file"
          :data="{ songId: selectedSongId }"
          :on-success="handleUploadSuccess"
        >
          <div class="upload-box">
            <i class="el-icon-upload"></i>
            <p>Click or drag file here</p>
          </div>
        </el-upload>
      </el-dialog>
      
      <!-- Individual Upload Dialogs Rekept for Compatibility -->
      <el-dialog title="UPLOAD AUDIO" v-model="uploadSongVisible" width="350px">
        <el-upload :action="$http.defaults.baseURL + '/chart/uploadSong'" with-credentials name="file" :data="{ songId: selectedSongId }" :on-success="() => { uploadSongVisible = false; $message.success('Audio Uploaded'); }" drag>
          <i class="el-icon-upload"></i> <div class="el-upload__text">Drop .wav/.mp3 here</div>
        </el-upload>
      </el-dialog>
      <el-dialog title="UPLOAD BACKGROUND" v-model="uploadBackVisible" width="350px">
        <el-upload :action="$http.defaults.baseURL + '/chart/uploadDefaultBackground'" with-credentials name="file" :data="{ songId: selectedSongId }" :on-success="() => { uploadBackVisible = false; $message.success('Background Uploaded'); }" drag>
          <i class="el-icon-upload"></i> <div class="el-upload__text">Drop Image here</div>
        </el-upload>
      </el-dialog>
      <el-dialog title="UPLOAD COVER" v-model="uploadCoverVisible" width="350px">
        <el-upload :action="$http.defaults.baseURL + '/chart/uploadSongCover'" with-credentials name="file" :data="{ songId: selectedSongId }" :on-success="() => { uploadCoverVisible = false; $message.success('Cover Uploaded'); }" drag>
          <i class="el-icon-upload"></i> <div class="el-upload__text">Drop Image here</div>
        </el-upload>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import BackgroundDisplay from "../components/BackgroundDisplay";
import AppHeader from "../components/Header";
export default {
  components: { BackgroundDisplay, AppHeader },
  data() {
    return {
      sort: "00",
      params: {
        status: "0",
        searchType: "0",
        searchContent: "",
        sortType: "0",
        sortWay: "0",
      },
      form: {
        songId: "", songName: "", songWriter: "", chartConstant: "", loadingText: "", loadedText: ""
      },
      newSong: {
        songName: "", songWriter: "", defaultBackground: "", songUrl: "", songCover: "", loadingText: "", loadedText: ""
      },
      songs: [],
      editVisible: false,
      addVisible: false,
      uploadSongVisible: false,
      uploadBackVisible: false,
      uploadCoverVisible: false,
      selectedSongId: "",
      selectedSong: {},
      value: false,
      assetVisible: false,
    };
  },
  created() {
    this.getMyAllCharts();
  },
  methods: {
    async getMyAllCharts() {
      const { data: res } = await this.$http.post("/user/getAllMyCharts");
      if (res.code == 0) this.songs = res.data.charts;
    },
    async getCharts() {
      this.params.sortType = this.sort.substr(0, 1);
      this.params.sortWay = this.sort.substr(1, 1);
      const { data: res } = await this.$http.post("/user/getMyCharts", this.params);
      if (res.code == 0) this.songs = res.data.charts;
    },
    getEdit(songId, item) {
      this.selectedSongId = songId;
      this.selectedSong = item;
      this.value = item.status >= 1;
      this.form = JSON.parse(JSON.stringify(item));
      this.editVisible = true;
    },
    async editSongInfo() {
      const visibilityEndpoint = (this.value && this.selectedSong.status === 0) 
          ? "/user/publiciseChart" 
          : (!this.value && this.selectedSong.status === 1)
          ? "/user/privatizeChart" : null;

      if (visibilityEndpoint) {
        await this.$http.post(visibilityEndpoint, { songId: this.selectedSongId });
      }

      const { data: res } = await this.$http.post("/chart/editChartInfo", this.form);
      if (res.code == 0) {
        this.$message.success("Settings saved");
        this.getMyAllCharts();
        this.editVisible = false;
      }
    },
    async addSong() {
      const { data: res } = await this.$http.post("/chart/newChart", this.newSong);
      if (res.code === 0) {
        this.$message.success("Melody created");
        this.getMyAllCharts();
        this.addVisible = false;
      }
    },
    async delete(songId) {
      const { data: res } = await this.$http.post("/user/deleteChart", { songId });
      if (res.code === 0) {
        this.$message.success("Deleted");
        this.getMyAllCharts();
      }
    },
    deleteSong(songId) {
      this.$confirm("Delete this rhythm permanently?", "WARNING", {
        confirmButtonText: "DELETE", cancelButtonText: "CANCEL", type: "warning"
      }).then(() => this.delete(songId));
    },
    next(songId) {
      this.$router.push({ path: "/chart/maker", query: { songId } });
    },
    getStatusType(status) {
      if (status === 2) return 'success';
      if (status === 1) return 'warning';
      return 'info';
    },
    getStatusLabel(status) {
      if (status === 2) return 'CERTIFIED';
      if (status === 1) return 'PUBLIC';
      return 'PRIVATE';
    },
    getAdd() { this.addVisible = true; }
  },
};
</script>

<style scoped>
.admin-view { width: 100vw; height: 100vh; position: relative; }
.view-content { position: absolute; inset: 0; z-index: 10; display: flex; flex-direction: column; overflow: hidden; }

.filter-header-wrapper { padding: 20px 40px; display: flex; justify-content: center; }
.filter-header { width: 100%; max-width: 1200px; padding: 15px 25px; display: flex; gap: 20px; align-items: center; }

.search-section { flex: 1; display: flex; gap: 10px; }
.type-select { width: 120px; }
.search-input { flex: 1; }
.sort-select { width: 160px; }

.management-container { flex: 1; overflow-y: auto; padding: 0 40px 60px; }
.management-list { max-width: 1200px; margin: 0 auto; display: flex; flex-direction: column; gap: 15px; }

.manage-card { padding: 20px 30px; display: flex; align-items: center; justify-content: space-between; gap: 40px; }
.manage-card:hover { border-color: var(--accent-cyan); background: rgba(255, 255, 255, 0.08); }

.card-left { display: flex; gap: 20px; align-items: center; flex: 1; cursor: pointer; }
.cover-wrapper { position: relative; width: 120px; height: 120px; border-radius: 12px; overflow: hidden; }
.manage-cover { width: 100%; height: 100%; }
.hover-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.6); display: flex; flex-direction: column; justify-content: center; align-items: center; opacity: 0; transition: 0.3s; font-size: 10px; font-weight: 800; color: var(--accent-cyan); }
.cover-wrapper:hover .hover-overlay { opacity: 1; }

.song-title { font-size: 20px; margin: 0 0 5px; font-weight: 700; color: white; }
.song-artist { color: var(--text-muted); font-size: 14px; margin-bottom: 12px; }
.status-tags { display: flex; gap: 10px; align-items: center; }
.constant { font-size: 11px; font-weight: 800; color: var(--accent-cyan); }

.card-actions { width: 120px; text-align: right; }

/* Settings Form */
.switch-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; padding: 15px; background: rgba(255,255,255,0.05); border-radius: 12px; font-weight: 700; }
.upload-buttons { display: flex; gap: 10px; margin-bottom: 25px; }
.hint { font-size: 12px; color: var(--text-muted); margin-top: 10px; }

:deep(.el-upload-dragger) { background: rgba(255,255,255,0.02) !important; border: 1px dashed var(--glass-border) !important; }

.action-group { display: flex; gap: 10px; justify-content: flex-end; }
.action-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #ccc;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}
.edit-btn:hover {
  background: rgba(64, 158, 255, 0.2);
  border-color: #409eff;
  color: #409eff;
}
.delete-btn:hover {
  background: rgba(245, 108, 108, 0.2);
  border-color: #f56c6c;
  color: #f56c6c;
}
</style>
