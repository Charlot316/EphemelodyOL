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
const snapPoints = ref([]);
const dragBounds = ref({ min: 0, max: Infinity });

const collectSnapPoints = (currentOp) => {
  const points = [];
  // Collect from Background Operations (excluding self)
  props.chart.changeBackgroundOperations.forEach(op => {
    if (op !== currentOp) {
      if (op.startTime !== undefined) points.push(op.startTime);
      if (op.endTime !== undefined) points.push(op.endTime);
    }
  });
  
  // Collect from Tracks
  if (props.chart.tracks) {
    props.chart.tracks.forEach(track => {
      if (track.notes) {
        track.notes.forEach(n => {
          if (n.timing !== undefined) points.push(n.timing);
          if (n.endTiming !== undefined) points.push(n.endTiming);
        });
      }
      if (track.moveOperations) {
        track.moveOperations.forEach(o => {
          if (o.startTime !== undefined) points.push(o.startTime);
          if (o.endTime !== undefined) points.push(o.endTime);
        });
      }
      if (track.changeWidthOperations) {
        track.changeWidthOperations.forEach(o => {
          if (o.startTime !== undefined) points.push(o.startTime);
          if (o.endTime !== undefined) points.push(o.endTime);
        });
      }
       if (track.changeColorOperations) {
        track.changeColorOperations.forEach(o => {
          if (o.startTime !== undefined) points.push(o.startTime);
          if (o.endTime !== undefined) points.push(o.endTime);
        });
      }
    });
  }
  return points;
};

const getSnappedTime = (time, points, threshold = 100) => { // 100ms visual equivalent roughly or just 50ms
  // Calculate threshold in ms based on a pixel distance, e.g. 10px
  // 10px in ms = (10 / (docWidth - 300)) * displayTime
  const pxThreshold = 10;
  const msThreshold = (pxThreshold / (props.global.documentWidth - 300)) * props.displayAreaTime;
  
  let bestTime = time;
  let minDiff = msThreshold;
  
  for (const point of points) {
    const diff = Math.abs(time - point);
    if (diff < minDiff) {
      minDiff = diff;
      bestTime = point;
    }
  }
  return bestTime;
};

// Dragging Handlers
const startDragOp = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'move';
  dragStartX.value = props.global.clientX;
  dragStartStartTime.value = op.startTime;
  dragStartEndTime.value = op.endTime || (op.startTime + 2000);
  
  snapPoints.value = collectSnapPoints(op);
  
  // Calculate Bounds
  const sorted = [...props.chart.changeBackgroundOperations].sort((a,b) => a.startTime - b.startTime);
  const idx = sorted.indexOf(op);
  const prev = idx > 0 ? sorted[idx - 1] : null;
  const next = idx < sorted.length - 1 ? sorted[idx + 1] : null;
  
  dragBounds.value = {
    min: prev ? prev.endTime : 0,
    max: next ? next.startTime : props.chart.songLength
  };
};

const startResizeLeft = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'left';
  dragStartX.value = props.global.clientX;
  dragStartStartTime.value = op.startTime;
  dragStartEndTime.value = op.endTime || (op.startTime + 2000);
  
  snapPoints.value = collectSnapPoints(op);
  
  const sorted = [...props.chart.changeBackgroundOperations].sort((a,b) => a.startTime - b.startTime);
  const idx = sorted.indexOf(op);
  const prev = idx > 0 ? sorted[idx - 1] : null;

  dragBounds.value = {
    min: prev ? prev.endTime : 0,
    max: op.endTime // Cannot pass its own end time
  };
};

const startResizeRight = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'right';
  dragStartX.value = props.global.clientX;
  dragStartStartTime.value = op.startTime;
  dragStartEndTime.value = op.endTime || (op.startTime + 2000);
  
  snapPoints.value = collectSnapPoints(op);
  
  const sorted = [...props.chart.changeBackgroundOperations].sort((a,b) => a.startTime - b.startTime);
  const idx = sorted.indexOf(op);
  const next = idx < sorted.length - 1 ? sorted[idx + 1] : null;

  dragBounds.value = {
    min: op.startTime, // Cannot pass start
    max: next ? next.startTime : props.chart.songLength
  };
};

watch(() => props.global.mouseMove, () => {
  if (draggingOp.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - 300)) * props.displayAreaTime);
    
    if (dragType.value === 'move') {
      const duration = dragStartEndTime.value - dragStartStartTime.value;
      let newStart = dragStartStartTime.value + deltaTime;
      
      // Snap Start
      newStart = getSnappedTime(newStart, snapPoints.value);
      // Snap End? Usually snap the edge we are most "aware" of, or both.
      // Let's try snapping start first. If that doesn't snap, maybe snap end?
      // Priorities: Start Snap > End Snap.
      
      let newEnd = newStart + duration;
      
      // Bounds Check
      if (newStart < dragBounds.value.min) {
        newStart = dragBounds.value.min;
        newEnd = newStart + duration;
      }
      if (newEnd > dragBounds.value.max) {
        newEnd = dragBounds.value.max;
        newStart = newEnd - duration;
      }
      
      // Re-verify start min bound in case end bound pushed it back
      if (newStart < dragBounds.value.min) newStart = dragBounds.value.min;

      draggingOp.value.startTime = newStart;
      draggingOp.value.endTime = newStart + duration;
      
    } else if (dragType.value === 'left') {
      let newStart = dragStartStartTime.value + deltaTime;
      newStart = getSnappedTime(newStart, snapPoints.value);
      
      if (newStart < dragBounds.value.min) newStart = dragBounds.value.min;
      if (newStart > dragBounds.value.max - 100) newStart = dragBounds.value.max - 100; // Min duration 100ms?
      
      draggingOp.value.startTime = newStart;
      
    } else if (dragType.value === 'right') {
      let newEnd = dragStartEndTime.value + deltaTime;
      newEnd = getSnappedTime(newEnd, snapPoints.value);
      
      if (newEnd > dragBounds.value.max) newEnd = dragBounds.value.max;
      if (newEnd < dragBounds.value.min + 100) newEnd = dragBounds.value.min + 100;

      draggingOp.value.endTime = newEnd;
    }
  }
});

watch(() => props.global.mouseUp, () => {
  if (draggingOp.value) {
    props.chart.changeBackgroundOperations.sort((a,b) => a.startTime - b.startTime);
    draggingOp.value = null;
    dragType.value = null;
    snapPoints.value = [];
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
