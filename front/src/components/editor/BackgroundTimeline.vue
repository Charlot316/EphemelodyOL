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
import { ref, defineProps, defineEmits, watch, inject } from 'vue';
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
const commandHistory = inject('commandHistory');

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

const checkOverlap = (start, end, excludeOp) => {
  for (const op of props.chart.changeBackgroundOperations) {
    if (op === excludeOp) continue;
    const opEnd = op.endTime || (op.startTime + 2000);
    // Strict overlap check
    if (start < opEnd && end > op.startTime) {
      return true;
    }
  }
  return false;
};

const deleteOp = (index) => {
  const op = props.chart.changeBackgroundOperations[index];
  props.chart.changeBackgroundOperations.splice(index, 1);
  
  if (commandHistory) {
    commandHistory.pushCommand({
      description: 'Delete BG Op',
      undo: () => {
         // Insert back. Since we might have sorted, index usage is risky if concurrent edits happened, 
         // but for single user session it's ok if we assume linear history.
         // Better to re-sort or insert at correct time position.
         // Ideally insert and sort.
         props.chart.changeBackgroundOperations.push(op);
         props.chart.changeBackgroundOperations.sort((a,b) => a.startTime - b.startTime);
      },
      redo: () => {
         const idx = props.chart.changeBackgroundOperations.indexOf(op);
         if (idx !== -1) props.chart.changeBackgroundOperations.splice(idx, 1);
      }
    });
  }
};

// Dragging Handlers
const startDragOp = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'move';
  dragStartX.value = props.global.clientX;
  dragStartStartTime.value = op.startTime;
  dragStartEndTime.value = op.endTime || (op.startTime + 2000);
  
  snapPoints.value = collectSnapPoints(op);
};

const startResizeLeft = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'left';
  dragStartX.value = props.global.clientX;
  dragStartStartTime.value = op.startTime;
  dragStartEndTime.value = op.endTime || (op.startTime + 2000);
  
  snapPoints.value = collectSnapPoints(op);
};

const startResizeRight = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'right';
  dragStartX.value = props.global.clientX;
  dragStartStartTime.value = op.startTime;
  dragStartEndTime.value = op.endTime || (op.startTime + 2000);
  
  snapPoints.value = collectSnapPoints(op);
};

watch(() => props.global.mouseMove, () => {
  if (draggingOp.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - 300)) * props.displayAreaTime);
    
    if (dragType.value === 'move') {
      const duration = dragStartEndTime.value - dragStartStartTime.value;
      let newStart = dragStartStartTime.value + deltaTime;
      
      if (newStart < 0) newStart = 0;
      // Snap Start
      newStart = getSnappedTime(newStart, snapPoints.value);
      let newEnd = newStart + duration;
      
      // Check overlap
      if (!checkOverlap(newStart, newEnd, draggingOp.value)) {
        draggingOp.value.startTime = newStart;
        draggingOp.value.endTime = newEnd;
      }
      
    } else if (dragType.value === 'left') {
      let newStart = dragStartStartTime.value + deltaTime;
      newStart = getSnappedTime(newStart, snapPoints.value);
      
      if (newStart < 0) newStart = 0;
      // Min duration check
      if (newStart >= draggingOp.value.endTime - 100) newStart = draggingOp.value.endTime - 100;
      
      if (!checkOverlap(newStart, draggingOp.value.endTime, draggingOp.value)) {
        draggingOp.value.startTime = newStart;
      }
      
    } else if (dragType.value === 'right') {
      let newEnd = dragStartEndTime.value + deltaTime;
      newEnd = getSnappedTime(newEnd, snapPoints.value);
      
      // Min duration check
      if (newEnd <= draggingOp.value.startTime + 100) newEnd = draggingOp.value.startTime + 100;
      if (newEnd > props.chart.songLength) newEnd = props.chart.songLength;

      if (!checkOverlap(draggingOp.value.startTime, newEnd, draggingOp.value)) {
        draggingOp.value.endTime = newEnd;
      }
    }
  }
});

watch(() => props.global.mouseUp, () => {
  if (draggingOp.value) {
    props.chart.changeBackgroundOperations.sort((a,b) => a.startTime - b.startTime);
    
    // Check changes
    const currentOp = draggingOp.value;
    const finalStart = currentOp.startTime;
    const finalEnd = currentOp.endTime;
    
    if (finalStart !== dragStartStartTime.value || finalEnd !== dragStartEndTime.value) {
        const oldS = dragStartStartTime.value;
        const oldE = dragStartEndTime.value;
        // opEnd was possibly undefined default? 
        // Logic: dragStartEndTime was computed.
        // We should just restore values.
        
        if (commandHistory) {
           commandHistory.pushCommand({
              description: 'Move BG Op',
              undo: () => {
                 currentOp.startTime = oldS;
                 // If oldE was the default (start+2000) and it was undefined in obj, 
                 // we might be setting a concrete value. That's fine.
                 currentOp.endTime = oldE;
                 props.chart.changeBackgroundOperations.sort((a,b) => a.startTime - b.startTime);
                 props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
              },
              redo: () => {
                 currentOp.startTime = finalStart;
                 currentOp.endTime = finalEnd;
                 props.chart.changeBackgroundOperations.sort((a,b) => a.startTime - b.startTime);
                 props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
              }
           });
        }
     }
    
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
