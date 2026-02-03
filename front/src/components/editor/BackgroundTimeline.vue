<template>
  <div class="bg-timeline-container" :class="{ collapsed: isCollapsed }">
    <!-- 左侧固定部分 -->
    <div class="bg-timeline-left">
      <div class="header">
        <el-icon><Picture /></el-icon>
        <span>背景轨道</span>
        <el-button 
          type="text" 
          size="small" 
          class="collapse-btn" 
          @click="toggleCollapse"
        >
          <el-icon><ArrowDown v-if="!isCollapsed"/><ArrowUp v-else/></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 右侧滚动部分 -->
    <div class="bg-timeline-right" @scroll="handleScroll" ref="scrollRef">
      <div 
        class="bg-track-content"
        :style="{ width: (chart.songLength / displayAreaTime) * (global.documentWidth - 300) + 'px' }"
        @dragover.prevent
        @drop="handleDrop"
      >
        <div v-if="!isCollapsed" class="bg-segments">
          <!-- 默认背景底色/片段 -->
          <div class="default-bg-segment" :style="{ width: '100%' }">
            <span class="label">默认背景</span>
          </div>

          <!-- 背景操作片段 -->
          <div 
            v-for="(op, index) in chart.changeBackgroundOperations" 
            :key="index"
            class="bg-segment"
            :style="{
              left: (op.startTime / displayAreaTime) * (global.documentWidth - 300) + 'px',
              width: ((op.endTime - op.startTime) / displayAreaTime) * (global.documentWidth - 300) + 'px'
            }"
            @mousedown="startDragOp($event, op)"
          >
            <img :src="op.background" alt="bg" />
            <div class="segment-info">{{ op.startTime }}ms</div>
            <div class="segment-actions">
              <el-icon class="delete-icon" @click.stop="deleteOp(index)"><Delete /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { Picture, ArrowDown, ArrowUp, Delete } from '@element-plus/icons-vue';
import { ElNotification } from 'element-plus';

const props = defineProps({
  chart: Object,
  global: Object,
  displayAreaTime: Number,
  scrollLeft: Number
});

const emit = defineEmits(['update:scrollLeft', 'toggle-collapse']);

const isCollapsed = ref(false);
const scrollRef = ref(null);

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
  emit('toggle-collapse', isCollapsed.value);
};

const handleScroll = (e) => {
  emit('update:scrollLeft', e.target.scrollLeft);
};

const handleDrop = (e) => {
  try {
    const data = JSON.parse(e.dataTransfer.getData('application/json'));
    if (data.type === 'background-asset') {
      const rect = e.currentTarget.getBoundingClientRect();
      const x = e.clientX - rect.left + scrollRef.value.scrollLeft;
      const startTime = Math.round((x / (props.global.documentWidth - 300)) * props.displayAreaTime);
      
      // Add new background operation
      props.chart.changeBackgroundOperations.push({
        startTime: startTime,
        background: data.url,
        edit: false
      });
      
      // Trigger migration to update endTimes
      // Note: we might need to emit an event or access a global method
      props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
      ElNotification({ title: '已添加背景', type: 'success', size: 'small' });
    }
  } catch (err) {
    console.error('Drop error:', err);
  }
};

const deleteOp = (index) => {
  props.chart.changeBackgroundOperations.splice(index, 1);
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};
</script>

<style scoped>
.bg-timeline-container {
  display: flex;
  height: 100px;
  background: #1a1a1a;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.bg-timeline-container.collapsed {
  height: 32px;
}

.bg-timeline-left {
  width: 300px;
  flex-shrink: 0;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  background: #222;
  display: flex;
  align-items: center;
  padding: 0 16px;
}

.bg-timeline-left .header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #888;
  font-size: 13px;
  width: 100%;
}

.collapse-btn {
  margin-left: auto;
  color: #666;
}

.bg-timeline-right {
  flex-grow: 1;
  overflow-x: auto;
  overflow-y: hidden;
  background: #111;
  position: relative;
}

.bg-timeline-right::-webkit-scrollbar {
  height: 4px;
}

.bg-timeline-right::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
}

.bg-track-content {
  height: 100%;
  position: relative;
}

.bg-segments {
  height: 100%;
  position: relative;
}

.default-bg-segment {
  position: absolute;
  top: 10px;
  bottom: 10px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px dashed rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  font-size: 12px;
}

.bg-segment {
  position: absolute;
  top: 5px;
  bottom: 5px;
  background: #2c3e50;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  z-index: 10;
}

.bg-segment img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.8;
}

.segment-info {
  position: absolute;
  bottom: 2px;
  left: 4px;
  font-size: 10px;
  background: rgba(0,0,0,0.5);
  padding: 1px 3px;
  border-radius: 2px;
  color: #eee;
}

.segment-actions {
  position: absolute;
  top: 2px;
  right: 2px;
  opacity: 0;
  transition: opacity 0.2s;
}

.bg-segment:hover .segment-actions {
  opacity: 1;
}

.delete-icon {
  background: rgba(245, 108, 108, 0.8);
  color: white;
  padding: 3px;
  border-radius: 50%;
  font-size: 12px;
}
</style>
