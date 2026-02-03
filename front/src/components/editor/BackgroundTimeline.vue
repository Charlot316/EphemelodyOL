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
              width: (( (op.endTime || (op.startTime + 2000)) - op.startTime) / displayAreaTime) * (global.documentWidth - 300) + 'px'
            }"
            @mousedown.stop="props.global.currentNoteType === 3 ? deleteOp(index) : startDragOp($event, op)"
          >
            <!-- Resize Handles -->
            <div 
              class="resize-handle left" 
              @mousedown.stop="startResizeLeft($event, op)"
            ></div>
            <div 
              class="resize-handle right" 
              @mousedown.stop="startResizeRight($event, op)"
            ></div>

            <img :src="op.background" alt="bg" @dragstart.prevent />
            <div class="segment-info">{{ op.startTime }}ms</div>
            <div class="segment-actions">
              <el-icon class="delete-icon" @click.stop="deleteOp(index)"><Delete /></el-icon>
            </div>
          </div>

          <!-- Time Indicators -->
          <div
            class="time-indicater"
            :style="{
              width: '1px',
              background: 'rgb(255,255,0)',
              height: '100%',
              position: 'absolute',
              pointerEvents: 'none',
              top: '0px',
              left: (global.currentTime / displayAreaTime) * (global.documentWidth - 300) + 'px',
              zIndex: 30
            }"
          ></div>
          <div
            class="time-indicater-false"
            :style="{
              width: '1px',
              background: 'rgb(255,255,255)',
              height: '100%',
              position: 'absolute',
              pointerEvents: 'none',
              top: '0px',
              left: indicatorLeft + 'px',
              zIndex: 30
            }"
          ></div>

        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue';
import { Picture, ArrowDown, ArrowUp, Delete } from '@element-plus/icons-vue';
import { ElNotification } from 'element-plus';

const props = defineProps({
  chart: Object,
  global: Object,
  displayAreaTime: Number,
  scrollLeft: Number,
  indicatorLeft: Number
});

const emit = defineEmits(['update:scrollLeft', 'toggle-collapse']);

const isCollapsed = ref(false);
const scrollRef = ref(null);

// Dragging state
const draggingOp = ref(null);
const dragType = ref(null); // 'move', 'left', 'right'
const dragStartX = ref(0);
const dragStartStartTime = ref(0);
const dragStartEndTime = ref(0);

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
  emit('toggle-collapse', isCollapsed.value);
};

// Synchronize prop scrollLeft to element
watch(() => props.scrollLeft, (newVal) => {
  if (scrollRef.value && scrollRef.value.scrollLeft !== newVal) {
    scrollRef.value.scrollLeft = newVal;
  }
});

const handleScroll = (e) => {
  emit('update:scrollLeft', e.target.scrollLeft);
};

const handleDrop = (e) => {
  try {
    const data = JSON.parse(e.dataTransfer.getData('application/json'));
    if (data.type === 'background-asset') {
      if (data.isCover) {
        ElNotification({
          title: '操作受限',
          message: '这是封面图片，不能作为背景图使用。请上传或选择专门的背景素材。',
          type: 'warning',
          duration: 3000
        });
        return;
      }
      const rect = e.currentTarget.getBoundingClientRect();
      const x = e.clientX - rect.left + scrollRef.value.scrollLeft;
      const startTime = Math.round((x / (props.global.documentWidth - 300)) * props.displayAreaTime);
      
      // Calculate End Time: try to give it 2000ms duration
      const proposedEndTime = startTime + 2000;
      let endTime = proposedEndTime;

      // Find next operation to avoid overlap if needed, or clamp endTime
      const nextOp = props.chart.changeBackgroundOperations
          .filter(op => op.startTime > startTime)
          .sort((a, b) => a.startTime - b.startTime)[0];
          
      if (nextOp && endTime > nextOp.startTime) {
          endTime = nextOp.startTime;
      }

      // Add new background operation
      props.chart.changeBackgroundOperations.push({
        startTime: startTime,
        endTime: endTime,
        background: data.url,
        assetId: data.id,
        edit: false
      });
      props.chart.changeBackgroundOperations.sort((a,b) => a.startTime - b.startTime);
      
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

// Dragging Handlers
const startDragOp = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'move';
  dragStartX.value = props.global.clientX;
  dragStartStartTime.value = op.startTime;
  dragStartEndTime.value = op.endTime || (op.startTime + 2000);
};

const startResizeLeft = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'left';
  dragStartX.value = props.global.clientX;
  dragStartStartTime.value = op.startTime;
  dragStartEndTime.value = op.endTime || (op.startTime + 2000);
};

const startResizeRight = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'right';
  dragStartX.value = props.global.clientX;
  dragStartStartTime.value = op.startTime;
  dragStartEndTime.value = op.endTime || (op.startTime + 2000);
};

watch(() => props.global.mouseMove, () => {
  if (draggingOp.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - 300)) * props.displayAreaTime);
    
    if (dragType.value === 'move') {
      const duration = dragStartEndTime.value - dragStartStartTime.value;
      let newStart = dragStartStartTime.value + deltaTime;
      if (newStart < 0) newStart = 0;
      
      draggingOp.value.startTime = newStart;
      draggingOp.value.endTime = newStart + duration;
    } else if (dragType.value === 'left') {
      let newStart = dragStartStartTime.value + deltaTime;
      if (newStart < 0) newStart = 0;
      if (newStart >= draggingOp.value.endTime - 100) newStart = draggingOp.value.endTime - 100;
      draggingOp.value.startTime = newStart;
    } else if (dragType.value === 'right') {
      let newEnd = dragStartEndTime.value + deltaTime;
      if (newEnd <= draggingOp.value.startTime + 100) newEnd = draggingOp.value.startTime + 100;
      draggingOp.value.endTime = newEnd;
    }
  }
});

watch(() => props.global.mouseUp, () => {
  if (draggingOp.value) {
    props.chart.changeBackgroundOperations.sort((a,b) => a.startTime - b.startTime);
    draggingOp.value = null;
    dragType.value = null;
    props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
  }
});
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
  cursor: grab;
  z-index: 10;
}

.bg-segment:active {
  cursor: grabbing;
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

.resize-handle {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 10px;
  background: transparent;
  z-index: 20;
  cursor: ew-resize;
}

.resize-handle.left {
  left: 0;
  cursor: w-resize;
}

.resize-handle.right {
  right: 0;
  cursor: e-resize;
}

.resize-handle:hover {
  background: rgba(255, 255, 255, 0.1);
}
</style>
