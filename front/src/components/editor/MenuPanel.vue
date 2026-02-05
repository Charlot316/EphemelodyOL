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

    <div class="assets-list">
      <div v-if="!chart.assets || chart.assets.length === 0" class="empty-state">
        <el-icon>
          <Files />
        </el-icon>
        <p>暂无资源</p>
        <p class="sub">点击上方按钮上传</p>
      </div>
      <div v-for="asset in filteredAssets" :key="asset.id" class="asset-item" draggable="true"
        @dragstart="onDragStart($event, asset)">
        <div class="asset-preview">
          <img :src="asset.url" alt="asset" />
          <div class="asset-overlay">
            <el-button type="primary" size="small" circle @click="copyUrl(asset)">
              <el-icon>
                <Link />
              </el-icon>
            </el-button>
            <el-button type="danger" size="small" circle @click="deleteAsset(asset)">
              <el-icon>
                <Delete />
              </el-icon>
            </el-button>
          </div>
        </div>
        <div class="asset-info">
          <div v-if="editingAssetId === asset.id" class="rename-container">
            <el-input v-model="tempName" size="small" @blur="handleRename(asset)" @keyup.enter="handleRename(asset)"
              auto-focus ref="renameInput" />
          </div>
          <span v-else class="asset-name" @dblclick="startRename(asset)">
            <el-tag v-if="asset.name === 'Cover'" size="small" type="info" class="cover-tag">封面</el-tag>
            {{ asset.name }}
            <el-icon class="rename-icon" @click.stop="startRename(asset)">
              <Edit />
            </el-icon>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, computed, ref, inject } from 'vue';
import { Picture, Plus, Files, Delete, Link, Edit } from '@element-plus/icons-vue';
import { ElMessageBox, ElNotification } from 'element-plus';
import { Axios } from '@/plugins/axios';

const props = defineProps({
  chart: Object,
  global: Object,
  Height: Number
});

const syncAction = inject('syncAction');
const history = inject('commandHistory');

const editingAssetId = ref(null);
const tempName = ref('');

const startRename = (asset) => {
  editingAssetId.value = asset.id;
  tempName.value = asset.name;
};

const handleRename = async (asset) => {
  if (!tempName.value || tempName.value === asset.name) {
    editingAssetId.value = null;
    return;
  }

  const oldName = asset.name;
  const newName = tempName.value;

  const executeRename = (name) => {
    asset.name = name;
    if (syncAction) syncAction("UPDATE_ASSET", { id: asset.id, name: name });
  };

  history.pushCommand({
    undo: () => executeRename(oldName),
    redo: () => executeRename(newName),
    description: `重命名资产: ${oldName} -> ${newName}`
  });

  executeRename(newName);
  editingAssetId.value = null;
};

const filteredAssets = computed(() => {
  if (!props.chart.assets) return [];
  // 过滤掉封面和默认背景，避免重复存储和显示（用户要求 cover/bg 不纳入 asset 管理）
  return props.chart.assets.filter(asset => {
    const isCover = (asset.name === 'Cover' || asset.url === props.chart.songCover);
    const isDefaultBg = (asset.name === 'DefaultBackground' || asset.url === props.chart.defaultBackground);
    return !isCover && !isDefaultBg;
  });
});

const handleUploadSuccess = (response) => {
  if (response.code === 0) {
    props.chart.assets.push(response.data);
    ElNotification({ title: '成功', message: '图片已上传至资源库', type: 'success' });
  } else {
    ElNotification({ title: '失败', message: response.message, type: 'error' });
  }
};

const copyUrl = (asset) => {
  navigator.clipboard.writeText(asset.url).then(() => {
    ElNotification({ title: '成功', message: '链接已复制到剪贴板', type: 'success' });
  }).catch(() => {
    ElNotification({ title: '错误', message: '复制失败', type: 'error' });
  });
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
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

.assets-list {
  flex: 1;
  min-height: 0;
  /* 关键：允许 flex 子元素在内容超出时收缩，从而触发滚动 */
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: 20px;
  padding-right: 4px;
}

.assets-list::-webkit-scrollbar {
  width: 4px;
}

.assets-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.asset-item {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
  cursor: grab;
  flex-shrink: 0;
  /* 绝对不允许在纵向被挤压 */
  width: 100%;
}

.asset-item:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.06);
}

.asset-preview {
  position: relative;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  aspect-ratio: 16 / 9;
  /* 给出一个默认比例，防止初始加载时高度坍塌 */
  min-height: 120px;
}

.asset-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
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
  cursor: pointer;
}

.rename-icon {
  font-size: 12px;
  color: #666;
  margin-left: auto;
  opacity: 0;
  transition: opacity 0.2s;
}

.asset-name:hover .rename-icon {
  opacity: 1;
}

.rename-container {
  width: 100%;
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
