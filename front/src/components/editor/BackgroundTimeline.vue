<template>
  <div class="bg-timeline-container" :class="{ collapsed: isCollapsed }">
    <!-- 左侧固定部分 -->
    <div class="bg-timeline-left">
      <div class="header">
        <el-icon>
          <Picture />
        </el-icon>
        <span>背景轨道</span>
        <el-button type="text" size="small" class="collapse-btn" @click="toggleCollapse">
          <el-icon>
            <ArrowDown v-if="!isCollapsed" />
            <ArrowUp v-else />
          </el-icon>
        </el-button>
      </div>
    </div>

    <!-- 右侧滚动部分 -->
    <div class="bg-timeline-right" @scroll="handleScroll" ref="scrollRef">
      <div class="bg-track-content"
        :style="{ width: (chart.songLength / displayAreaTime) * (global.documentWidth - global.siderWidth) + 'px' }"
        @dragover.prevent @drop="handleDrop">
        <div v-if="!isCollapsed" class="bg-segments">
          <!-- 默认背景底色/片段 -->
          <div class="default-bg-segment" :style="{ width: '100%' }">
            <span class="label">默认背景</span>
          </div>

          <!-- 背景操作片段 -->
          <div v-for="(op, index) in chart.changeBackgroundOperations" :key="index" class="bg-segment" :style="{
            left: (op.startTiming / displayAreaTime) * (global.documentWidth - global.siderWidth) + 'px',
            width: (((op.endTiming || (op.startTiming + 2000)) - op.startTiming) / displayAreaTime) * (global.documentWidth - global.siderWidth) + 'px'
          }" @mousedown.stop="props.global.currentNoteType === 3 ? deleteOp(index) : startDragOp($event, op)">
            <!-- Resize Handles -->
            <div class="resize-handle left" @mousedown.stop="startResizeLeft($event, op)"></div>
            <div class="resize-handle right" @mousedown.stop="startResizeRight($event, op)"></div>

            <img :src="getBackgroundUrl(op)" alt="bg" @dragstart.prevent />
            <div class="segment-info">{{ op.startTiming }}ms</div>
            <div class="segment-actions">
              <el-icon class="delete-icon" @click.stop="deleteOp(index)">
                <Delete />
              </el-icon>
            </div>
          </div>

          <!-- Time Indicators -->
          <div class="time-indicater" :style="{
            width: '1px',
            background: 'rgb(255,255,0)',
            height: '100%',
            position: 'absolute',
            pointerEvents: 'none',
            top: '0px',
            left: (global.currentTime / displayAreaTime) * (global.documentWidth - global.siderWidth) + 'px',
            zIndex: 30
          }"></div>
          <div class="time-indicater-false" :style="{
            width: '1px',
            background: 'rgb(255,255,255)',
            height: '100%',
            position: 'absolute',
            pointerEvents: 'none',
            top: '0px',
            left: indicatorLeft + 'px',
            zIndex: 30
          }"></div>

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
const syncAction = inject('syncAction');

const isCollapsed = ref(false);
const scrollRef = ref(null);

// Dragging state
const draggingOp = ref(null);
const dragType = ref(null); // 'move', 'left', 'right'
const dragStartX = ref(0);
const dragStartStartTiming = ref(0);
const dragStartEndTiming = ref(0);
const snapPoints = ref([]);

const collectSnapPoints = (currentOp) => {
  const points = [];
  // Collect from Background Operations (excluding self)
  props.chart.changeBackgroundOperations.forEach(op => {
    if (op !== currentOp) {
      if (op.startTiming !== undefined) points.push(op.startTiming);
      if (op.endTiming !== undefined) points.push(op.endTiming);
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
          if (o.startTiming !== undefined) points.push(o.startTiming);
          if (o.endTiming !== undefined) points.push(o.endTiming);
        });
      }
      if (track.changeWidthOperations) {
        track.changeWidthOperations.forEach(o => {
          if (o.startTiming !== undefined) points.push(o.startTiming);
          if (o.endTiming !== undefined) points.push(o.endTiming);
        });
      }
      if (track.changeColorOperations) {
        track.changeColorOperations.forEach(o => {
          if (o.startTiming !== undefined) points.push(o.startTiming);
          if (o.endTiming !== undefined) points.push(o.endTiming);
        });
      }
    });
  }
  return points;
};

const getSnappedTime = (time, points) => {
  const pxThreshold = 10;
  const msThreshold = (pxThreshold / (props.global.documentWidth - props.global.siderWidth)) * props.displayAreaTime;

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

const normalizeUrl = (url) => {
  if (!url) return "";
  if (url.startsWith("http://localhost:8080")) {
    return url.replace("http://localhost:8080", "http://localhost:8090");
  }
  if (url.includes("pic.mcatk.com")) {
    const fileName = url.split('/').pop();
    return "http://localhost:8090/" + fileName;
  }
  return url;
};

const getBackgroundUrl = (op) => {
  if (op.assetId && props.chart.assets) {
    const asset = props.chart.assets.find(a => a.id === op.assetId);
    if (asset) return normalizeUrl(asset.url);
  }
  return normalizeUrl(op.background);
};

const checkOverlap = (start, end, excludeOp) => {
  for (const op of props.chart.changeBackgroundOperations) {
    if (op === excludeOp) continue;
    const opEnd = op.endTiming || (op.startTiming + 2000);
    // Strict overlap check
    if (start < opEnd && end > op.startTiming) {
      return true;
    }
  }
  return false;
};

const deleteOp = (index) => {
  const op = props.chart.changeBackgroundOperations[index];
  props.chart.changeBackgroundOperations.splice(index, 1);

  if (syncAction) syncAction("DELETE_BG_OP", op.id);

  if (commandHistory) {
    commandHistory.pushCommand({
      description: 'Delete BG Op',
      undo: () => {
        props.chart.changeBackgroundOperations.push(op);
        props.chart.changeBackgroundOperations.sort((a, b) => a.startTiming - b.startTiming);
        if (syncAction) syncAction("ADD_BG_OP", op);
      },
      redo: () => {
        const idx = props.chart.changeBackgroundOperations.indexOf(op);
        if (idx !== -1) {
          props.chart.changeBackgroundOperations.splice(idx, 1);
          if (syncAction) syncAction("DELETE_BG_OP", op.id);
        }
      }
    });
  }
};

// Dragging Handlers
const startDragOp = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'move';
  dragStartX.value = props.global.clientX;
  dragStartStartTiming.value = op.startTiming;
  dragStartEndTiming.value = op.endTiming || (op.startTiming + 2000);

  snapPoints.value = collectSnapPoints(op);
};

const startResizeLeft = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'left';
  dragStartX.value = props.global.clientX;
  dragStartStartTiming.value = op.startTiming;
  dragStartEndTiming.value = op.endTiming || (op.startTiming + 2000);

  snapPoints.value = collectSnapPoints(op);
};

const startResizeRight = (e, op) => {
  draggingOp.value = op;
  dragType.value = 'right';
  dragStartX.value = props.global.clientX;
  dragStartStartTiming.value = op.startTiming;
  dragStartEndTiming.value = op.endTiming || (op.startTiming + 2000);

  snapPoints.value = collectSnapPoints(op);
};

watch(() => props.global.mouseMove, () => {
  if (draggingOp.value) {
    const deltaX = props.global.clientX - dragStartX.value;
    const deltaTime = Math.round((deltaX / (props.global.documentWidth - props.global.siderWidth)) * props.displayAreaTime);

    if (dragType.value === 'move') {
      const duration = dragStartEndTiming.value - dragStartStartTiming.value;
      let newStart = dragStartStartTiming.value + deltaTime;

      if (newStart < 0) newStart = 0;
      newStart = getSnappedTime(newStart, snapPoints.value);
      let newEnd = newStart + duration;

      if (!checkOverlap(newStart, newEnd, draggingOp.value)) {
        draggingOp.value.startTiming = newStart;
        draggingOp.value.endTiming = newEnd;
      }

    } else if (dragType.value === 'left') {
      let newStart = dragStartStartTiming.value + deltaTime;
      newStart = getSnappedTime(newStart, snapPoints.value);
      if (newStart < 0) newStart = 0;
      if (newStart >= draggingOp.value.endTiming - 100) newStart = draggingOp.value.endTiming - 100;

      if (!checkOverlap(newStart, draggingOp.value.endTiming, draggingOp.value)) {
        draggingOp.value.startTiming = newStart;
      }

    } else if (dragType.value === 'right') {
      let newEnd = dragStartEndTiming.value + deltaTime;
      newEnd = getSnappedTime(newEnd, snapPoints.value);
      if (newEnd <= draggingOp.value.startTiming + 100) newEnd = draggingOp.value.startTiming + 100;
      if (newEnd > props.chart.songLength) newEnd = props.chart.songLength;

      if (!checkOverlap(draggingOp.value.startTiming, newEnd, draggingOp.value)) {
        draggingOp.value.endTiming = newEnd;
      }
    }
  }
});

watch(() => props.global.mouseUp, () => {
  if (draggingOp.value) {
    props.chart.changeBackgroundOperations.sort((a, b) => a.startTiming - b.startTiming);

    const currentOp = draggingOp.value;
    const finalStart = currentOp.startTiming;
    const finalEnd = currentOp.endTiming;

    if (finalStart !== dragStartStartTiming.value || finalEnd !== dragStartEndTiming.value) {
      const oldS = dragStartStartTiming.value;
      const oldE = dragStartEndTiming.value;

      if (syncAction) syncAction("UPDATE_BG_OP", currentOp);

      if (commandHistory) {
        commandHistory.pushCommand({
          description: 'Move BG Op',
          undo: () => {
            currentOp.startTiming = oldS;
            currentOp.endTiming = oldE;
            props.chart.changeBackgroundOperations.sort((a, b) => a.startTiming - b.startTiming);
            if (syncAction) syncAction("UPDATE_BG_OP", currentOp);
            props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
          },
          redo: () => {
            currentOp.startTiming = finalStart;
            currentOp.endTiming = finalEnd;
            props.chart.changeBackgroundOperations.sort((a, b) => a.startTiming - b.startTiming);
            if (syncAction) syncAction("UPDATE_BG_OP", currentOp);
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

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
  emit('toggle-collapse', isCollapsed.value);
};

const handleScroll = (e) => {
  emit('update:scrollLeft', e.target.scrollLeft);
};

// Handle dropping an asset from MenuPanel to create a new BG operation
const handleDrop = (e) => {
  const assetUrl = e.dataTransfer.getData('assetUrl');
  const assetId = e.dataTransfer.getData('assetId');
  if (!assetUrl) return;

  const rect = e.currentTarget.getBoundingClientRect();
  const x = e.clientX - rect.left;
  const timeOffset = Math.round((x / (props.global.documentWidth - props.global.siderWidth)) * props.displayAreaTime);

  const newOp = {
    startTiming: timeOffset,
    endTiming: timeOffset + 5000,
    background: assetUrl,
    assetId: assetId ? parseInt(assetId) : null,
    isPending: true,
    clientId: Math.random().toString(36).substr(2, 9)
  };

  props.chart.changeBackgroundOperations.push(newOp);
  props.chart.changeBackgroundOperations.sort((a, b) => a.startTiming - b.startTiming);

  if (syncAction) syncAction("ADD_BG_OP", newOp, newOp.clientId);
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
  background: rgba(0, 0, 0, 0.5);
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
