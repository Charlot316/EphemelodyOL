<template>
  <div class="workspace-root select" :class="{ 'is-fullscreen': isEditorFullscreen }">
    <!-- 顶部状态栏 (可选/隐藏) -->
    
    <div class="main-layout" :style="layoutStyle">
      <!-- 1. 侧边栏: 资产与菜单 -->
      <transition name="panel-slide">
          <aside 
            v-if="menuOpened" 
            class="panel-sider glass-panel" 
            :style="{ width: global.siderWidth + 'px', gridRow: 1 }"
          >
          
          <MenuPanel
            :Height="global.documentHeight - footerHeight"
            :footerHeight="footerHeight"
            :global="global"
            :chart="chart"
          />
          <div class="resizer-v right" @mousedown="startResizingSider"></div>
        </aside>
      </transition>

      <!-- 2. 预览区 -->
      <main 
        class="panel-preview glass-panel" 
        :style="{ gridRow: 1, width: '100%' }"
      >
        
        <div class="player-wrapper">
          <BeatPlayer
            ref="playerRef"
            :chart="chart"
            :global="global"
            mode="edit"
            :selectedTrackId="currentSelectTrack ? (currentSelectTrack.trackId || currentSelectTrack.index) : null"
            :displayRange="[displayStart, displayEnd]"
            :volume="volume"
            @track-click="handleTrackClick"
            @audio-loaded="onAudioLoaded"
            @toggle-fullscreen="handleToggleFullscreen"
            :isFullscreen="isEditorFullscreen"
            playerId="editor-player"
          />
        </div>
      </main>

      <footer 
        class="panel-footer glass-panel" 
        :style="{ 
          height: footerHeight + 'px', 
          gridColumn: '1 / 3',
          gridRow: 2
        }"
      >
        <div class="resizer-h top" @mousedown="startResizingFooter"></div>
        <Footer
          :chart="chart"
          :global="global"
          :siderWidth="global.siderWidth"
          @currentTrack="handleCurrentTrack"
          @open-settings="globalSetting = true"
          @toggle-fullscreen="handleToggleFullscreen"
          @restart="playerRef.value?.reStart()"
          @seek-delta="handleSeekDelta"
        />
      </footer>
    </div>


    <!-- 全局设置弹窗 -->
    <el-dialog v-model="globalSetting" @close="checkbpm" width="650px" title="全局设置" custom-class="glass-dialog">
      <el-form :model="form" label-width="200px" style="padding: 20px;">
        <el-form-item label="BPM / Interval (ms)">
            <el-input-number v-model="chart.bpm" :min="0.1" :step="0.01" controls-position="right" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="First Beat (ms)">
            <el-input-number v-model="chart.firstBeatDelay" :step="1" controls-position="right" style="width: 100%;" />
        </el-form-item>
        <el-divider content-position="left">Auxiliary / Manual</el-divider>
        <el-form-item label="Total Beats">
            <el-input-number v-model="chart.beatsCount" :min="0" :step="1" controls-position="right" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="Last Beat (ms)">
            <el-input-number v-model="chart.lastBeatDelay" :step="1" controls-position="right" style="width: 100%;" />
        </el-form-item>
        <el-form-item>
            <el-button @click="ManualCalculatebpm">Manual Calc BPM</el-button>
        </el-form-item>
        <el-form-item label="音量">
          <el-input-number v-model="volume" :min="0" :max="100" @change="changeVolume" />
        </el-form-item>
        <el-form-item label="Note Speed (ms)">
           <el-slider v-model="global.remainingTime" :min="100" :max="2000" :step="50" style="width: 200px; display: inline-block; margin-right: 15px; vertical-align: middle;" />
           <el-input-number v-model="global.remainingTime" :min="100" :max="2000" :step="50" controls-position="right" />
        </el-form-item>
        <el-form-item label="微调步长 (ms)">
          <el-input-number v-model="timeStep" :min="1" :max="100" />
        </el-form-item>
        <!-- 保留其他表单项 -->
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount, provide } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { Axios } from '@/plugins/axios';
import { ElNotification } from 'element-plus';
import { VideoPause, VideoPlay, Setting } from '@element-plus/icons-vue';
import BeatPlayer from "@/components/game/BeatPlayer.vue";
import MenuPanel from "@/components/editor/MenuPanel.vue";
import Footer from "@/components/editor/Footer.vue";
import { useChartEditor } from '@/composables/useChartEditor';
import { useBpmTool } from '@/composables/useBpmTool';
import { useCommandHistory } from '@/composables/useCommandHistory';
import { v4 as uuidv4 } from 'uuid';

const route = useRoute();
const router = useRouter();
const store = useStore();
const playerRef = ref(null);
const isResizingFooter = ref(false);
const isResizingSider = ref(false);
const isEditorFullscreen = ref(false);
const timeStep = ref(1);
const siderWidth = ref(300);

const history = useCommandHistory();
provide('commandHistory', history);

const {
  chart,
  chartGot,
  isRunning,
  sliding,
  displayStart,
  displayEnd,
  displayRange,
  sortTrack,
  getChart,
  saveChart,
  onAudioLoaded,
  onlineUsers,
  onlineCount,
  resetChart,
  syncAction
} = useChartEditor(route, router);

provide('syncAction', syncAction);
provide('uuid', uuidv4);
provide('onlineUsers', onlineUsers);
provide('onlineCount', onlineCount);
provide('resetChart', resetChart);
provide('saveChart', saveChart);
provide('router', router);

const global = reactive({
  currentTime: 0,
  beatLine: true,
  documentHeight: 0,
  documentWidth: 0,
  clientX: 0,
  clientY: 0,
  mouseMove: false,
  mouseUp: false,
  isEdit: true,
  keyPressTime: {},
  keyIsHold: {},
  keyUsed: {},
  reCalculateChartMaker: false,
  siderWidth: 300, 
  remainingTime: 1000,
  finalY: 0.8,
  lostTime: 150,
  pureTime: 50,
  farTime: 100,
  timeSort: true,
});

const menuOpened = ref(true);
const volume = ref(store.state.volume || 100);
const currentSelectTrack = ref(null);
const globalSetting = ref(false);
const footerHeight = ref(400); 
const form = reactive({});

const playerActions = {
  seek: (t) => playerRef.value?.seek(t),
  play: () => playerRef.value?.play(),
  pause: () => playerRef.value?.pause()
};

const {
  ManualCalculatebpm,
  checkbpm: endbpm, // Map original endbpm to checkbpm if needed, or just use endbpm
  calculatebpm,     // If needed by template
  endbpm: checkbpm  // The template uses @close="checkbpm", so we map endbpm to checkbpm
} = useBpmTool(chart, global, playerActions);

// --- Logic ---

const layoutStyle = computed(() => {
  const sider = menuOpened.value ? `${global.siderWidth}px` : '0px';
  return {
    gridTemplateColumns: `${sider} 1fr`,
    gridTemplateRows: `1fr ${footerHeight.value}px`,
  };
});

const handleTimeChange = () => {
  playerRef.value?.seek(global.currentTime);
};

const handleSlideStart = () => {
  playerRef.value?.pause();
  sliding.value = true;
};

const handleSlideEnd = () => {
  sliding.value = false;
  playerRef.value?.seek(global.currentTime);
};

const togglePlay = () => {
  isRunning.value ? playerRef.value?.pause() : playerRef.value?.play();
  isRunning.value = !isRunning.value;
};

const handleToggleFullscreen = () => {
  const container = document.querySelector('.workspace-root');
  if (!document.fullscreenElement) {
    if (container.requestFullscreen) container.requestFullscreen();
    else if (container.webkitRequestFullscreen) container.webkitRequestFullscreen();
    isEditorFullscreen.value = true;
  } else {
    if (document.exitFullscreen) document.exitFullscreen();
    else if (document.webkitExitFullscreen) document.webkitExitFullscreen();
    isEditorFullscreen.value = false;
  }
};

const handleSeekDelta = (direction) => {
  const step = timeStep.value || 1;
  global.currentTime = Math.max(0, Math.min(chart.songLength, global.currentTime + direction * step));
  handleTimeChange();
};

const startResizingFooter = () => {
  isResizingFooter.value = true;
};

const startResizingSider = () => {
  isResizingSider.value = true;
};

const handleTrackClick = (track) => {
  currentSelectTrack.value = track;
};

const handleCurrentTrack = (track) => {
  currentSelectTrack.value = track;
};

onMounted(() => {
  const savedSpeed = localStorage.getItem('noteSpeed');
  if (savedSpeed) {
    global.remainingTime = parseInt(savedSpeed);
  }
  watch(() => global.remainingTime, (newVal) => {
    localStorage.setItem('noteSpeed', newVal);
  });

  const updateDimensions = () => {
    global.documentHeight = document.documentElement.clientHeight;
    global.documentWidth = document.documentElement.clientWidth;
    playerRef.value?.resize();
  };
  
  updateDimensions();
  window.addEventListener('resize', updateDimensions);

  // 监听全屏变化 (处理 Esc 退出)
  document.addEventListener('fullscreenchange', () => {
    isEditorFullscreen.value = !!document.fullscreenElement;
  });

  window.onkeydown = (e) => {
    if (e.key === "ArrowLeft") {
      global.currentTime = Math.max(0, global.currentTime - timeStep.value);
      handleTimeChange();
    } else if (e.key === "ArrowRight") {
      global.currentTime = Math.min(chart.songLength, global.currentTime + timeStep.value);
      handleTimeChange();
      e.preventDefault();
    } else if ((e.ctrlKey || e.metaKey) && e.key === 'z') {
      if (e.shiftKey) {
        history.redo();
      } else {
        history.undo();
      }
      e.preventDefault();
    }
  };

  document.onmousemove = (e) => {
    global.clientX = e.clientX;
    global.clientY = e.clientY;
    global.mouseMove = !global.mouseMove;

    if (isResizingFooter.value) {
      footerHeight.value = Math.max(100, Math.min(600, global.documentHeight - e.clientY));
      playerRef.value?.resize();
    }
    if (isResizingSider.value) {
      global.siderWidth = Math.max(200, Math.min(600, e.clientX));
      playerRef.value?.resize();
    }
  };

  document.onmouseup = () => {
    isResizingFooter.value = false;
    isResizingSider.value = false;
    global.mouseUp = !global.mouseUp;
  };

  getChart(() => {
    // Loaded
  });
});

</script>

<style scoped>
.workspace-root {
  height: 100vh;
  width: 100vw;
  background: #111;
  color: #eee;
  overflow: hidden;
  position: relative;
}

.main-layout {
  display: grid;
  width: 100%;
  height: 100%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.glass-panel {
  background: rgba(30, 30, 30, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  position: relative;
  overflow: hidden;
}

.panel-drag-handle {
  height: 24px;
  background: rgba(255, 255, 255, 0.08);
  font-size: 11px;
  color: #aaa;
  display: flex;
  align-items: center;
  padding: 0 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  cursor: grab;
}
.handle-actions {
  margin-left: auto;
  display: flex;
  gap: 2px;
}

.panel-sider {
  grid-row: 1;
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.resizer-v {
  width: 4px;
  height: 100%;
  cursor: ew-resize;
  background: rgba(255, 255, 255, 0.05);
  position: absolute;
  top: 0;
  right: -2px;
  z-index: 100;
  transition: background 0.2s;
}
.resizer-v:hover { background: #409eff; }

.panel-preview {
  grid-column: 2;
  grid-row: 1;
  display: flex;
  flex-direction: column;
}

.player-wrapper {
  flex: 1;
  position: relative;
  width: 100%;
  height: 100%;
}

.panel-footer {
  grid-column: 1 / 3;
  grid-row: 2;
  z-index: 5;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.resizer-h {
  height: 4px;
  width: 100%;
  cursor: ns-resize;
  background: rgba(255, 255, 255, 0.1);
  position: absolute;
  z-index: 100;
  transition: background 0.2s;
}
.resizer-h.bottom { top: -2px; }
.resizer-h.top { bottom: -2px; }
.resizer-h:hover { background: #409eff; }

.global-time-slider {
  position: absolute;
  height: 40px;
  right: 0;
  padding: 0 20px;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(10px);
  z-index: 8;
  display: flex;
  align-items: center;
}

.player-floating-controls {
  position: absolute;
  bottom: 20px;
  right: 20px;
}

.glass-fab {
  background: rgba(64, 158, 255, 0.2) !important;
  border: 1px solid rgba(64, 158, 255, 0.4) !important;
  color: #fff !important;
  backdrop-filter: blur(10px);
  width: 50px;
  height: 50px;
  font-size: 24px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.3);
}

.is-fullscreen .panel-preview {
  position: fixed;
  top: 0; left: 0;
  width: 100vw; height: 100vh;
  z-index: 9999;
}

.panel-slide-enter-active, .panel-slide-leave-active { transition: transform 0.3s; }
.panel-slide-enter-from, .panel-slide-leave-to { transform: translateX(-100%); }

@media (max-width: 768px) {
  .main-layout {
    grid-template-columns: 1fr !important;
    grid-template-rows: auto 1fr auto !important;
  }
  .panel-sider { display: none; }
}
</style>
