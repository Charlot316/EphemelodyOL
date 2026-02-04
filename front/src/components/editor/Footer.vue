<template>
  <div class="footer-container">
    <div class="footer-header">
    <div class="footer-toolbar">
      <!-- 分组 0: 轨道管理与视图对齐 (对齐 siderWidth) -->
      <div 
        class="toolbar-side-aligned" 
        :style="{ 
          width: siderWidth + 'px',
          width: siderWidth + 'px'
        }"
      >
          <div class="toolbar-group-minimal">
            <el-tooltip content="新建轨道" placement="top">
              <el-button link class="tool-btn-minimal" @click="newTrack">
                <el-icon><CirclePlus /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="显示所有" placement="top">
              <el-button link class="tool-btn-minimal" @click="showAllTracks">
                <el-icon><View /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip :content="global.timeSort ? '当前模式: 时间排序' : '当前模式: 坐标排序'" placement="top">
              <el-button link class="tool-btn-minimal" @click="global.timeSort = !global.timeSort; updateTrack();">
                <el-icon><Sort v-if="!global.timeSort"/><Timer v-else/></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip :content="showNoRemain ? '过滤空轨' : '显示空轨'" placement="top">
              <el-button link :class="['tool-btn-minimal', { 'is-active': !showNoRemain }]" @click="showNoRemain = !showNoRemain">
                <el-icon><Filter /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip :content="showCurrent ? '显示全局' : '锁定当前'" placement="top">
              <el-button link :class="['tool-btn-minimal', { 'is-active': showCurrent }]" @click="showCurrent = !showCurrent">
                <el-icon><Aim /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip :content="autoScroll ? '关闭跟随' : '自动跟随'" placement="top">
              <el-button link :class="['tool-btn-minimal', { 'is-active': autoScroll }]" @click="autoScroll = !autoScroll">
                <el-icon><VideoPlay /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>


        <!-- 分组 2: 轨道类型过滤 -->
        <div class="toolbar-group">
          <el-button
            size="small"
            class="toggle-btn"
            :class="{ 'is-active': showReal }"
            @click="showReal = !showReal"
          >
            <el-icon><CircleCheck /></el-icon>实轨
          </el-button>
          <el-button
            size="small"
            class="toggle-btn"
            :class="{ 'is-active': showFake }"
            @click="showFake = !showFake"
          >
            <el-icon><MagicStick /></el-icon>虚轨
          </el-button>
        </div>


        <!-- 分组 3: 点击行为设置 -->
        <div class="toolbar-group mode-selector">
          <el-button
            v-for="(label, idx) in ['短键', '长键', '滑键']"
            :key="idx"
            size="small"
            :class="['mode-btn', { 'is-selected': currentNoteType == idx }]"
            @click="currentNoteType = (currentNoteType === idx ? -1 : idx)"
          >
            <span class="mode-dot" :class="'mode-' + idx"></span>
            {{ label }}
          </el-button>
          <el-button
            size="small"
            :class="['mode-btn', { 'is-selected': currentNoteType == 3 }]"
            @click="switchToDeleteMode"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>


        <!-- 分组 4: 协作状态 -->
        <div class="toolbar-group">
          <el-tooltip placement="top">
            <template #content>
              <div class="user-list-tooltip">
                <div class="tooltip-title">当前在线编辑器 ({{ onlineCount }})</div>
                <div v-for="(user, idx) in onlineUsers" :key="idx" class="user-item">
                  {{ user }}
                </div>
              </div>
            </template>
            <div class="online-indicator" :class="{ 'multi-user': onlineCount > 1 }">
              <span class="pulse-dot"></span>
              <span class="count-label">{{ onlineCount }} 人在线</span>
            </div>
          </el-tooltip>
        </div>


        <!-- 分组 5: 杂项 -->
        <div class="toolbar-group">
          <el-tooltip content="重置到上次保存" placement="top">
            <el-button
              size="small"
              class="tool-btn-rect is-warning"
              @click="resetChart"
            >
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-tooltip>
        </div>


        <!-- 分组 6: 系统与视图 -->
        <div class="toolbar-group">
          <el-tooltip content="返回列表" placement="top">
            <el-button
              size="small"
              class="tool-btn-rect"
              @click="router.go(-1)"
            >
              <el-icon><Back /></el-icon>
              返回
            </el-button>
          </el-tooltip>
          <el-tooltip content="仅发布更改" placement="top">
            <el-button
              size="small"
              class="tool-btn-rect"
              @click="saveChart(false)"
            >
              <el-icon><Upload /></el-icon>
              发布
            </el-button>
          </el-tooltip>
          <el-tooltip content="通过表决后生成游玩文件并返回" placement="top">
            <el-button
              size="small"
              class="tool-btn-rect is-primary"
              @click="saveChart(true)"
            >
              <el-icon><UploadFilled /></el-icon>
              发布并返回
            </el-button>
          </el-tooltip>
        </div>


        <!-- 分组 7: 全局控制 (移除冗余播放按钮) -->
        <div class="toolbar-group">
          <el-tooltip content="全局设置" placement="top">
            <el-button
              circle
              size="small"
              class="tool-btn-glass"
              @click="$emit('open-settings')"
            >
              <el-icon><Setting /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>

      <div class="footer-header-right">
        <div class="time-display-wrapper">
          <span class="current-time">{{ Math.floor(global.currentTime) }}</span>
          <span class="time-separator">/</span>
          <span class="total-time">{{ Math.floor(chart.songLength) }}</span>
          <span class="unit">ms</span>
        </div>
        <div class="custom-slider-container">
          <el-icon><ZoomIn /></el-icon>
          <input 
            type="range" 
            v-model.number="displayAreaTime" 
            :min="1000" 
            :max="chart.songLength > 1000 ? chart.songLength : 1001"
            class="custom-range"
          />
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div v-if="chart.tracks" class="footer-main-content" @wheel="handleWheel">
      <div 
        class="footer-track-area" 
        :style="{ height: bgCollapsed ? 'calc(100% - 32px)' : 'calc(100% - 100px)' }"
      >
        <div class="footer-left">
          <div
            class="footer-track-container"
            id="footer-left-scroll"
            @scroll="leftScroll"
          >
            <transition-group
              name="list"
              enter-active-class="animate__animated animate__fadeInUp"
              leave-active-class="animate__animated animate__fadeOutUp"
            >
              <div v-for="track in displayTracks.filter(isVisible)" :key="track.index">
                <TrackCard
                  :chart="chart"
                  :track="track"
                  :global="global"
                  @currentTrack="handleCurrentTrack"
                  @editStatus="handleEditStatus"
                />
              </div>
            </transition-group>
          </div>
        </div>
        <div
          class="footer-right"
          id="footer-right-scroll"
          @scroll="rightScroll"
          @mousemove="rightMouseMove($event)"
          @mousedown="rightClick($event)"
          @contextmenu.prevent
        >
          <div
            class="beat-line-wrapper-absolute"
            :style="{ top: scrollTop + 'px' }"
          >
            <BeatLine
              :chart="chart"
              :global="global"
              :displayAreaTime="displayAreaTime"
              :siderWidth="siderWidth"
            />
          </div>
          <div style="position:absolute;left:0;top:0;">
            <transition-group
              name="list"
              enter-active-class="animate__animated animate__fadeInUp"
              leave-active-class="animate__animated animate__fadeOutUp"
            >
              <div v-for="track in displayTracks.filter(isVisible)" :key="track.index">
                <TrackCardPanel
                  :siderWidth="siderWidth"
                  :currentNoteType="currentNoteType"
                  :id="'trackCardPanel' + track.index"
                  :chart="chart"
                  :track="track"
                  :global="global"
                  :scrollLeft="scrollLeft"
                  :displayAreaTime="displayAreaTime"
                  :enableEdit="enableEdit"
                  @currentTrack="handleCurrentTrack"
                />
              </div>
            </transition-group>
          </div>

          <div
            class="time-indicater"
            id="time-indicater"
            :style="{
              width: '1px',
              background: 'rgb(255,255,0)',
              height: '100%',
              position: 'absolute',
              pointerEvents: 'none',
              top: scrollTop + 'px',
              left: (global.currentTime / displayAreaTime) * (global.documentWidth - siderWidth) + 'px',
            }"
          ></div>
          <div
            class="time-indicater-false"
            id="time-indicater-false"
            :style="{
              width: '1px',
              background: 'rgb(255,255,255)',
              height: '100%',
              position: 'absolute',
              pointerEvents: 'none',
              top: scrollTop + 'px',
              left: indicatorLeft + 'px',
            }"
          ></div>
        </div>
      </div>
      
      <!-- 背景时间轴 -->
      <BackgroundTimeline 
        :chart="chart"
        :global="global"
        :displayAreaTime="displayAreaTime"
        v-model:scrollLeft="scrollLeft"
        :indicatorLeft="indicatorLeft"
        @toggle-collapse="bgCollapsed = $event"
        @mousemove="rightMouseMove($event)"
        @mousedown="rightClick($event)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, defineProps, defineEmits, onMounted, inject } from 'vue';
import { 
  Sort, Timer, Filter, Aim, Compass, CircleCheck, 
  MagicStick, Delete, EditPen, ZoomIn, CirclePlus, View, Refresh,
  Back, Upload, UploadFilled, Setting, FullScreen, RefreshLeft,
  ArrowLeft, ArrowRight
} from '@element-plus/icons-vue';
import { ElMessageBox, ElNotification } from 'element-plus';
import TrackCard from "./TrackCard.vue";
import TrackCardPanel from "./TrackCardPanel.vue";
import BackgroundTimeline from "./BackgroundTimeline.vue";
import BeatLine from "./BeatLine.vue";
import "animate.css";

const onlineUsers = inject('onlineUsers');
const onlineCount = inject('onlineCount');
const resetChart = inject('resetChart');
const saveChart = inject('saveChart');
const router = inject('router');

const props = defineProps({
  chart: Object,
  global: Object,
  siderWidth: Number,
  siderPos: String
});

const emit = defineEmits(["currentTrack", "open-settings", "toggle-fullscreen", "restart", "seek-delta"]);

const editFinished = ref(true);

const handleEditStatus = (val) => {
  editFinished.value = val;
};

const newTrack = () => {
  if (editFinished.value) {
    editFinished.value = false;
    const track = {
      startTiming: 0,
      endTiming: 1500,
      isNew: true,
      type: 1,
      key: "K",
      R: "160",
      G: "160",
      B: "160",
      width: 1,
      positionX: 0,
      background: props.chart.defaultBackground,
      notes: [],
      moveOperations: [],
      changeWidthOperations: [],
      changeColorOperations: [],
      edit: true,
      index: props.chart.tracks.length,
      showInTimeline: true
    };
    props.chart.tracks.push(track);
    // Auto scroll to the new track which is at the bottom
    setTimeout(() => {
       const newTrackIndex = props.chart.tracks.length - 1;
       const leftScroll = document.getElementById("footer-left-scroll");
       if (leftScroll) leftScroll.scrollTop = leftScroll.scrollHeight;
    }, 100);
    
    updateTrack();
  } else {
    ElNotification({
      title: "提示",
      message: "请先完成正在编辑的轨道",
      type: "warning",
    });
    // Find editing track and scroll to it
    const editIndex = props.chart.tracks.findIndex(t => t.edit);
    if (editIndex !== -1) {
       // logic to scroll to editIndex track
    }
  }
};

const showAllTracks = () => {
  props.chart.tracks.forEach(track => {
    track.showInTimeline = true;
  });
  ElNotification({
    title: "成功",
    message: "已显示所有隐藏轨道",
    type: "success"
  });
};

const switchToDeleteMode = () => {
  if (currentNoteType.value === 3) {
      // 已经在删除模式，再次点击退出
      currentNoteType.value = -1;
      ElNotification({
        title: '快捷删除模式关闭',
        message: '已退出删除模式',
        type: 'info',
        duration: 2000
      });
      return; 
  }
  ElMessageBox.confirm(
    '进入快捷删除模式后，点击音符或操作将直接删除（无确认）。确定要继续吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      currentNoteType.value = 3;
      ElNotification({
        title: '快捷删除模式开启',
        message: '点击实体即可删除',
        type: 'warning',
        duration: 2000
      });
    })
    .catch(() => {
      // cancel
    });
};

const scrollLeft = ref(0);
const scrollTop = ref(0);
const displayAreaTime = ref(10000);
const indicatorLeft = ref(0);
const rightClicked = ref(false);
const autoScroll = ref(false);
const showReal = ref(true);
const showFake = ref(true);
const showNoRemain = ref(true);
const currentNoteType = ref(-1);
const enableEdit = ref(true);
const showCurrent = ref(false);
const bgCollapsed = ref(false);

let audio = null;

const updateTrack = () => {
  props.global.reCalculateTrack = !props.global.reCalculateTrack;
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};

const handleCurrentTrack = (param) => {
  emit("currentTrack", param);
};

const leftScroll = () => {
  const leftElem = document.getElementById("footer-left-scroll");
  const rightElem = document.getElementById("footer-right-scroll");
  if (leftElem && rightElem) {
    rightElem.scrollTop = leftElem.scrollTop;
  }
};

const rightScroll = () => {
  const leftElem = document.getElementById("footer-left-scroll");
  const rightElem = document.getElementById("footer-right-scroll");
  if (rightElem) {
    if (leftElem) leftElem.scrollTop = rightElem.scrollTop;
    scrollLeft.value = rightElem.scrollLeft;
    scrollTop.value = rightElem.scrollTop;
    // Synchronize horizontal scroll to BackgroundTimeline as well
    const bgTimelineScroll = document.querySelector(".bg-timeline-right");
    if (bgTimelineScroll) bgTimelineScroll.scrollLeft = rightElem.scrollLeft;
  }
};

const rightClick = (e) => {
  if (e.button !== 0) return;
  const rightElem = document.getElementById("footer-right-scroll");
  if (!rightElem) return;
  const rect = rightElem.getBoundingClientRect();
  if (e.clientX < rect.left) return;
  
  rightClicked.value = true;
  const x = e.clientX - rect.left + scrollLeft.value;
  const currentTime = (x / (rect.width)) * displayAreaTime.value;
  if (audio) audio.currentTime = currentTime / 1000;
  props.global.currentTime = currentTime;
  updateTrack();
};

const rightMouseMove = (e) => {
  const rightElem = document.getElementById("footer-right-scroll");
  if (!rightElem) return;
  const rect = rightElem.getBoundingClientRect();
  if (e.clientX < rect.left) return;
  
  const x = e.clientX - rect.left + scrollLeft.value;
  indicatorLeft.value = x;
  if (rightClicked.value) {
    const currentTime = (x / (rect.width)) * displayAreaTime.value;
    if (audio) audio.currentTime = currentTime / 1000;
    props.global.currentTime = currentTime;
    updateTrack();
  }
};

const handleWheel = (e) => {
  if (e.ctrlKey || e.metaKey) {
    e.preventDefault();
    // Zoom logic
    // deltaY positive = zoom out (increase time range)?
    // Chrome: pinch out -> deltaY negative (zoom in).
    // Let's assume negative deltaY means ZOOM IN (smaller displayAreaTime).
    
    // Sensitivity increased from 0.001 to 0.005
    const zoomFactor = 1 + (e.deltaY * 0.005);
    let newTime = displayAreaTime.value * zoomFactor;
    
    // Constraints
    if (newTime < 1000) newTime = 1000;
    if (newTime > props.chart.songLength) newTime = props.chart.songLength;
    if (newTime > 300000) newTime = 300000; // Hard max if songLength is weird
    
    displayAreaTime.value = newTime;
  }
};

window.addEventListener("mouseup", () => {
  rightClicked.value = false;
});

const displayTracks = computed(() => {
  const tracks = props.chart.tracks;
  if (!tracks) return [];
  
  const currentTime = props.global.currentTime;

  return [...tracks].sort((a, b) => {
    if (props.global.timeSort) {
      if (a.startTiming !== b.startTiming) return a.startTiming - b.startTiming;
    } else {
      // 模式2: 坐标排序 (状态权重四级排序)
      
      // 获取轨道当前相对于播放进度的状态权重
      // 0: 进行中 (正在屏幕上) - 最高优先级
      // 1: 待出场 (还没到时间) - 次高优先级
      // 2: 已过期 (已经结束) - 最低优先级
      const getStatusWeight = (t) => {
        if (currentTime >= t.startTiming && currentTime <= t.endTiming) return 0;
        if (currentTime < t.startTiming) return 1;
        return 2;
      };

      const weightA = getStatusWeight(a);
      const weightB = getStatusWeight(b);

      if (weightA !== weightB) return weightA - weightB;

      // 权重相同时的逻辑
      if (weightA === 0) {
        // 正在进行的：按坐标排序
        const posXA = a.positionX || 0;
        const posXB = b.positionX || 0;
        if (posXA !== posXB) return posXA - posXB;
      } else {
        // 待出场或已过期的：按时间排序
        if (a.startTiming !== b.startTiming) return a.startTiming - b.startTiming;
        if (a.endTiming !== b.endTiming) return a.endTiming - b.endTiming;
        const posXA = a.positionX || 0;
        const posXB = b.positionX || 0;
        if (posXA !== posXB) return posXA - posXB;
      }
    }
    return (a.id || 0) - (b.id || 0) || (a.index || 0) - (b.index || 0);
  });
});

const isVisible = (track) => {
  if (track.showInTimeline === false) return false;
  const showByNote = !showNoRemain.value ? (track.notes.length > 0) : true;
  const showByType = track.type === 1 ? showReal.value : showFake.value;
  const showByTime = showCurrent.value 
    ? (props.global.currentTime >= track.startTiming && props.global.currentTime <= track.endTiming) 
    : true;
  return showByType && showByNote && showByTime;
};

watch(scrollLeft, (newVal) => {
  const rightElem = document.getElementById("footer-right-scroll");
  if (rightElem && Math.abs(rightElem.scrollLeft - newVal) > 1) {
    rightElem.scrollLeft = newVal;
  }
});

watch(() => props.global.currentTime, (newVal) => {
  if (autoScroll.value) {
    const rightElem = document.getElementById("footer-right-scroll");
    if (rightElem) {
      const rect = rightElem.getBoundingClientRect();
      const currentX = (newVal / displayAreaTime.value) * (props.global.documentWidth - props.siderWidth);
      // Center the view on the playhead
      const targetScroll = currentX - (rect.width / 2);
      
      rightElem.scrollLeft = targetScroll;
      
      // Also sync background timeline
      const bgTimelineScroll = document.querySelector(".bg-timeline-right");
      if (bgTimelineScroll) bgTimelineScroll.scrollLeft = targetScroll;
    }
  }
});

onMounted(() => {
  audio = document.getElementById("audioSong");
  setTimeout(() => {
    displayAreaTime.value = 10000;
  }, 100);
});
</script>

<style scoped>
.footer-container { 
  height: 100%; 
  width: 100vw; 
  position: relative; 
  overflow: hidden; 
  --sider-width: v-bind(siderWidth + 'px');
  user-select: none;
}
.footer-header {
  height: 48px;
  width: 100vw;
  position: absolute;
  top: 0px;
  left: 0px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
  background: rgba(15, 15, 15, 0.9);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  z-index: 100;
  box-sizing: border-box;
}

.footer-main-content {
  position: absolute;
  top: 48px;
  bottom: 0;
  width: 100%;
}

.footer-track-area {
  display: flex;
  width: 100%;
  position: relative;
  transition: height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.footer-left { 
  width: var(--sider-width, 300px); 
  height: 100%; 
  flex-shrink: 0; 
  position: relative; 
}
.footer-track-container { 
  width: 100%; 
  height: 100%; 
  border-right: 1px solid rgba(255, 255, 255, 0.05); 
  background: rgb(32, 32, 32); 
  overflow-y: auto; 
  overflow-x: hidden;
}
.footer-track-container::-webkit-scrollbar { width: 0 !important; }

.footer-right { 
  flex-grow: 1;
  background: rgb(25, 25, 25); 
  height: 100%;
  overflow: auto; 
  padding-top: 0px;
  position: relative;
}
.footer-right::-webkit-scrollbar { height: 6px; background: rgba(0,0,0,0.2); }
.footer-right::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 3px; }

.footer-toolbar { 
  display: flex; 
  align-items: center; 
  height: 44px; /* 增加高度以提升大气感 */
  overflow-x: auto; 
  white-space: nowrap;
  background: rgba(10, 10, 10, 0.4);
}
.toolbar-side-aligned {
  display: flex;
  align-items: center;
  /* 这里的 22px 是轨道卡片内边距(10px) + 卡片内部边距(12px) 的物理起点，确保对齐预览图 */
  padding-left: 22px; 
  flex-shrink: 0;
  box-sizing: border-box;
}
.toolbar-group-minimal {
  display: flex;
  align-items: center;
  gap: 16px;
}
.toolbar-group {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  height: 100%;
}

/* 移除所有按钮边框和背景，消除臃肿感 */
:deep(.el-button) {
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
  padding: 6px !important;
  margin: 0 !important;
  height: auto !important;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  transform: translateY(-1px);
}

:deep(.el-button.is-active), :deep(.el-button.is-selected) {
  color: #409eff !important;
  background: rgba(64, 158, 255, 0.08) !important;
}

.tool-btn-minimal {
  font-size: 18px; /* 稍微加大图标 */
  color: #999;
}
.tool-btn-minimal.is-active {
  color: #409eff;
}
.toolbar-divider { width: 1px; height: 20px; background: rgba(255, 255, 255, 0.1); margin: 0 8px; }
.tool-btn { background: transparent !important; border: none !important; color: #888 !important; transition: all 0.2s ease; }
.tool-btn:hover { color: #fff !important; transform: scale(1.1); }
.tool-btn.is-active { color: var(--accent-cyan, #00f3ff) !important; text-shadow: 0 0 8px rgba(0, 243, 255, 0.5); }
.toggle-btn { background: transparent !important; border: 1px solid rgba(255, 255, 255, 0.1) !important; color: #999 !important; border-radius: 6px !important; padding: 4px 10px !important; }
.toggle-btn.is-active { background: rgba(64, 158, 255, 0.2) !important; border-color: #409eff !important; color: #409eff !important; }
.mode-btn { background: rgba(255, 255, 255, 0.05) !important; border: 1px solid transparent !important; color: #aaa !important; border-radius: 4px !important; padding: 4px 12px !important; display: flex; align-items: center; gap: 6px; }
.mode-btn.is-selected { background: rgba(255, 255, 255, 0.1) !important; border-color: rgba(255, 255, 255, 0.2) !important; color: #fff !important; }
.mode-dot { width: 6px; height: 6px; border-radius: 50%; }
.mode-0 { background: #409eff; }
.mode-1 { background: #e6a23c; }
.mode-2 { background: #67c23a; }
.delete-mode-btn { background: transparent !important; border: 1px solid transparent !important; color: #888 !important; }
.delete-mode-btn.is-selected { color: #f56c6c !important; }
.tool-btn-rect { background: rgba(255, 255, 255, 0.05) !important; border: 1px solid rgba(255, 255, 255, 0.1) !important; color: #aaa !important; border-radius: 4px !important; }
.tool-btn-rect.is-warning { color: #f56c6c !important; border-color: rgba(245, 108, 108, 0.3) !important; }
.tool-btn-rect.is-primary { background: linear-gradient(135deg, #409eff, #3a8ee6) !important; color: #fff !important; border: none !important; font-weight: 600 !important; }
.tool-btn-rect.is-primary:hover { filter: brightness(1.1); box-shadow: 0 0 10px rgba(64, 158, 255, 0.4); }
.footer-header-right { display: flex; align-items: center; }
.time-display-wrapper { background: rgba(0, 0, 0, 0.3); padding: 4px 12px; border-radius: 6px; border: 1px solid rgba(255, 255, 255, 0.05); font-family: 'Inter', monospace; }
.current-time { color: #fff; font-size: 14px; font-weight: 700; min-width: 50px; text-align: right; }
.time-separator { color: rgba(255, 255, 255, 0.3); margin: 0 4px; }
.total-time { color: rgba(255, 255, 255, 0.5); font-size: 13px; }
.unit { font-size: 10px; color: rgba(255, 255, 255, 0.2); margin-left: 4px; text-transform: uppercase; }
.custom-slider-container { color: #888; display: flex; align-items: center; gap: 12px; }
.custom-range { appearance: none; background: rgba(255,255,255,0.1); height: 4px; border-radius: 2px; outline: none; width: 120px; }
.custom-range::-webkit-slider-thumb { appearance: none; width: 12px; height: 12px; background: #67c23a; border-radius: 50%; cursor: pointer; }
.beat-line-wrapper-absolute { position: absolute; top: 0; left: 0; height: 100%; width: 100%; z-index: 5; pointer-events: none; }
.animate__animated { --animate-duration: 0.2s; }
.list-move { transition: transform 0.2s ease; }

.online-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 2px 4px;
  cursor: default;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #67c23a;
  box-shadow: 0 0 0 rgba(103, 194, 58, 0.4);
  animation: pulse 2s infinite;
}

.online-indicator.multi-user .pulse-dot {
  background: #e6a23c;
  box-shadow: 0 0 0 rgba(230, 162, 60, 0.4);
  animation: pulse-warn 2s infinite;
}

.count-label {
  font-size: 12px;
  color: #eee;
  font-weight: 600;
}

@keyframes pulse {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(103, 194, 58, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(103, 194, 58, 0); }
}

@keyframes pulse-warn {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(230, 162, 60, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(230, 162, 60, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(230, 162, 60, 0); }
}

.user-list-tooltip {
  padding: 4px;
}
.tooltip-title {
  font-size: 12px;
  color: #888;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  margin-bottom: 6px;
  padding-bottom: 4px;
}
.user-item {
  font-size: 13px;
  color: #fff;
  padding: 2px 0;
}
.tool-btn-glass {
  background: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: #aaa !important;
  backdrop-filter: blur(5px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.tool-btn-glass:hover {
  background: rgba(64, 158, 255, 0.2) !important;
  border-color: rgba(64, 158, 255, 0.5) !important;
  color: #409eff !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}
</style>
