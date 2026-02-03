<template>
  <div class="menu-panel-container">
    <div class="panel-header">
      <div class="title-section">
        <el-icon>
          <Picture />
        </el-icon>
        <h4>图片资源管理</h4>
      </div>
      <div class="header-actions">
        <el-upload action="/api/chart/uploadAsset" :data="{ songId: chart.songId, type: 'image' }"
          :show-file-list="false" :on-success="handleUploadSuccess" :with-credentials="true">
          <el-button size="small" circle class="upload-btn-header">
            <el-icon>
              <Plus />
            </el-icon>
          </el-button>
        </el-upload>
      </div>
    </div>

    <div class="assets-grid" :style="{ height: Height - 60 + 'px' }">
      <div v-if="!chart.assets || chart.assets.length === 0" class="empty-state">
        <el-icon>
          <Files />
        </el-icon>
        <p>暂无资源</p>
        <p class="sub">点击上方按钮上传</p>
      </div>
      <div v-for="asset in chart.assets" :key="asset.id" class="asset-item" draggable="true"
        @dragstart="onDragStart($event, asset)">
        <div class="asset-preview">
          <img :src="asset.url" alt="asset" />
          <div class="asset-overlay">
            <el-button type="danger" size="small" circle @click="deleteAsset(asset)">
              <el-icon>
                <Delete />
              </el-icon>
            </el-button>
          </div>
        </div>
        <div class="asset-info">
          <span class="asset-name">
            <el-tag v-if="asset.name === 'Cover'" size="small" type="info" class="cover-tag">封面</el-tag>
            {{ asset.name }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import { Picture, Plus, Files, Delete } from '@element-plus/icons-vue';
import { ElMessageBox, ElNotification } from 'element-plus';
import { Axios } from '@/plugins/axios';

const props = defineProps({
  chart: Object,
  global: Object,
  Height: Number
});

const handleUploadSuccess = (response) => {
  if (response.code === 0) {
    props.chart.assets.push(response.data);
    ElNotification({ title: '成功', message: '图片已上传至资源库', type: 'success' });
  } else {
    ElNotification({ title: '失败', message: response.message, type: 'error' });
  }
};

const deleteAsset = (asset) => {
  ElMessageBox.confirm('确定要从资源库中删除这张图片吗？', '确认删除', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const { data: res } = await Axios.post('/chart/deleteAsset', { id: asset.id });
      if (res.code === 0) {
        const index = props.chart.assets.findIndex(a => a.id === asset.id);
        if (index !== -1) props.chart.assets.splice(index, 1);
        ElNotification({ title: '成功', message: '资源已删除', type: 'success' });
      }
    } catch (err) {
      ElNotification({ title: '错误', message: '网络异常', type: 'error' });
    }
  }).catch(() => { });
};

const onDragStart = (event, asset) => {
  event.dataTransfer.setData('assetUrl', asset.url);
  event.dataTransfer.setData('assetId', String(asset.id));
  event.dataTransfer.setData('application/json', JSON.stringify({
    type: 'background-asset',
    url: asset.url,
    id: asset.id,
    name: asset.name,
    isCover: asset.name === 'Cover'
  }));
};
</script>

<style scoped>
.menu-panel-container {
  padding: 0 16px;
  width: 100%;
  height: 100%;
  background: #1e1e1e;
  box-sizing: border-box;
}

.panel-header {
  height: 48px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.title-section {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #eee;
}

.title-section h4 {
  margin: 0;
  font-size: 14px;
}

.upload-btn-header {
  background: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: #aaa !important;
}

.upload-btn-header:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
}

.assets-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  overflow-y: auto;
  padding-bottom: 20px;
}

.assets-grid::-webkit-scrollbar {
  width: 4px;
}

.assets-grid::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.asset-item {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
  cursor: grab;
}

.asset-item:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.06);
}

.asset-preview {
  aspect-ratio: 16/9;
  position: relative;
  overflow: hidden;
}

.asset-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.asset-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.asset-item:hover .asset-overlay {
  opacity: 1;
}

.asset-info {
  padding: 6px 8px;
}

.asset-name {
  font-size: 11px;
  color: #888;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cover-tag {
  height: 16px;
  line-height: 14px;
  padding: 0 4px;
  font-size: 10px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: #ccc;
}

.empty-state {
  grid-column: span 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #555;
  gap: 12px;
}

.empty-state .el-icon {
  font-size: 40px;
}

.empty-state p {
  margin: 0;
  font-size: 13px;
}

.empty-state .sub {
  font-size: 12px;
  opacity: 0.6;
}
</style>
