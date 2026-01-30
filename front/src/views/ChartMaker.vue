<template>
  <div class="play-interface select">
    <div class="header">
      <div class="header-buttons">
        <div>
          <el-button
            size="small"
            type="text"
            class="header-button"
            @click="router.go(-1)"
            >返回</el-button
          >
        </div>
        <div>
          <el-button
            size="small"
            type="text"
            class="header-button"
            @click="saveChart(false)"
            >保存</el-button
          >
          <el-button
            size="small"
            type="text"
            class="header-button"
            @click="saveChart(true)"
            >保存并返回</el-button
          >
        </div>
      </div>
    </div>
    <!-- 侧边栏 -->
    <transition
      name="fade"
      enter-active-class="animate__animated animate__fadeInLeft"
      leave-active-class="animate__animated animate__fadeOutLeft"
    >
      <div
        v-if="menuOpened"
        :class="menuOpened ? 'sider-opened' : 'sider-closed'"
        :style="siderStyle"
      >
        <MenuPanel
          key="menupanel"
          :Height="global.documentHeight - footerHeight - 50"
          :footerHeight="footerHeight"
          :global="global"
          :chart="chart"
        /></div>
    </transition>
    <transition
      name="fade"
      enter-active-class="animate__animated animate__fadeInLeft"
      leave-active-class="animate__animated animate__fadeOutLeft"
    >
      <div
        v-if="menuOpened"
        :class="menuOpened ? 'sider-opened-track' : 'sider-closed-track'"
        :style="siderStyle"
      >
        <TrackPanel
          key="trackpanel"
          :Height="global.documentHeight - footerHeight - 50"
          :Track="currentSelectTrack"
          :global="global"
          :chart="chart"
          @currentTrack="handleCurrentTrack"
        /></div>
    </transition>

    <!-- 谱面展示 -->
    <div class="select">
      <div
        :class="menuOpened ? 'container-small' : 'container-big'"
        id="play-interface-container"
        :style="containerStyle"
      >
        <!-- 音频 -->
        <audio
          id="audioSong"
          preload="auto"
          controls
          :src="chart.songUrl"
          style="display:none"
        />
        <!-- 背景 -->
        <div v-for="image in imagePath" :key="image.url + image.startTime">
          <img
            :src="image.url"
            v-show="
              global.currentTime >= image.startTime &&
                global.currentTime <= image.endTime
            "
            style="position:absolute;left:0;top:0;width:100%;height:100%;object-fit:fill;user-drag:none;"
          />
        </div>
        <!-- 判定线 -->
        <div
          :style="{
            height: '2px',
            position: 'absolute',
            left: '0px',
            top: global.screenHeight * global.finalY - 1 + 'px',
            width: global.screenWidth + 'px',
          }"
        >
          <div
            class="white-line"
            :style="{
              height: '100%',
              position: 'absolute',
              left: (global.screenWidth - whiteLineLength) / 2 + 'px',
              width: whiteLineLength + 'px',
              background: 'rgba(255,255,255,1)',
            }"
          ></div>
        </div>
        <canvas id="track-canvas" style="position:absolute;top:0;left:0;" />
        <canvas id="note-canvas" style="position:absolute;top:0;left:0;" />
        <canvas id="judge-canvas" style="position:absolute;top:0;left:0;" />
        <!-- 轨道 -->
        <div
          class="play-interface-track-container"
          v-for="trackItem in chart.tracks"
          :key="trackItem.trackId || trackItem.startTiming"
        >
          <div>
            <Track
              :Track="trackItem"
              :global="global"
              v-if="
                global.currentTime > trackItem.startTiming &&
                  global.currentTime < trackItem.endTiming
              "
            />
          </div>
        </div>
        <div
          class="selected-track"
          v-if="
            currentSelectTrack != null &&
              global.currentTime > currentSelectTrack.startTiming &&
              global.currentTime < currentSelectTrack.endTiming &&
              currentSelectTrack.tempPositionX >= 0 &&
              currentSelectTrack.tempPositionX <= 1
          "
          :style="{
            position: 'absolute',
            top: '0px',
            left:
              (currentSelectTrack.tempPositionX -
                currentSelectTrack.tempWidth) *
                global.screenWidth +
              'px',
            width:
              2 * currentSelectTrack.tempWidth * global.screenWidth - 4 + 'px',
            height: global.finalY * global.screenHeight - 2 + 'px',
            border: '2px solid rgba(255,255,255,1)',
            background: 'rgba(255,255,255,0.2)',
          }"
        ></div>
      </div>
    </div>

    <!-- 进度条 -->
    <div
      v-if="chartGot"
      :class="menuOpened ? 'time-controller-small' : 'time-controller-big'"
      :style="containerStyle"
    >
      <div class="time-control-buttons">
        <el-button
          size="small"
          class="header-button"
          @click="changeMenuDisplay"
          type="text"
        >
          <el-icon><FullScreen /></el-icon>
        </el-button>
        <el-button
          size="small"
          @click="reStart"
          class="header-button"
          type="text"
        >
          <el-icon><RefreshLeft /></el-icon>
        </el-button>
        <el-button
          v-if="isRunning"
          size="small"
          class="header-button"
          @click="pause"
          type="text"
        >
          <el-icon><VideoPause /></el-icon>
        </el-button>
        <el-button
          v-else
          size="small"
          class="header-button"
          @click="play"
          type="text"
        >
          <el-icon><VideoPlay /></el-icon>
        </el-button>
        <el-button
          size="small"
          type="text"
          class="header-button"
          @click="globalSetting = true"
        >
          <el-icon><Setting /></el-icon>
        </el-button>
      </div>
      <div class="header-slide">
        <div class="header-slide-item">
          <el-slider
            v-model="global.currentTime"
            :min="displayStart"
            :max="displayEnd"
            @change="changeTime"
            @mousedown="SlideMouseDown"
            @mouseup="SlideMouseUp"
            @touchstart="SlideMouseDown"
            @touchend="SlideMouseUp"
          ></el-slider>
        </div>
      </div>
      <el-dialog
        v-model="globalSetting"
        @close="checkbpm"
        width="650px"
        title="全局设置"
      >
        <el-form :model="form" label-width="200px" style="padding: 20px;">
          <el-form-item label="音量">
            <el-input-number
              v-model="volume"
              :min="0"
              :max="100"
              @change="changeVolume"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="按键盘上下键同样可以调节音量"
              placement="top-start"
              style="margin-left:10px;"
            >
              <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item label="首拍偏移(单位:ms)">
            <el-input-number
              v-model="chart.firstBeatDelay"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="第一拍的偏移，在一拍间隔部分可以顺带测量，你也可以选择使用音频软件观察声波自行填写"
              placement="top-start"
            >
              <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item label="末拍偏移(单位:ms)">
            <el-input-number
              v-model="chart.lastBeatDelay"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="最后一拍的偏移，你可以填写好精确的第一拍和最后一拍的偏移和节拍数，然后直接计算出一拍间隔"
              placement="top-start"
            >
              <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </el-form-item>
          <el-form-item label="节拍数">
            <el-input-number
              v-model="chart.beatsCount"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="用以计算一拍间隔"
              placement="top-start"
            >
              <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
            <div style="margin-top:15px; width: 100%;">
              <el-button @click="ManualCalculatebpm">
                精确计算一拍间隔
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="一拍间隔(单位:ms)">
            <el-input-number
              v-model="chart.bpm"
              :min="0"
              :max="chart.songLength"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="一个节拍的长度"
              placement="top-start"
            >
              <el-icon style="margin-left: 10px;"><QuestionFilled /></el-icon>
            </el-tooltip>
            <div style="margin-top:15px; width: 100%;">
              <el-button @mousedown="calculatebpm">{{
                !bpmStart ? "粗略估算一拍间隔" : "请在节奏点处按下"
              }}</el-button>
              <el-button @click="endbpm" v-if="bpmStart">
                结束或重新测量
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="显示时间区域">
            <div style="display: flex; gap: 10px;">
              <el-input-number v-model="displayStart" :min="0" :max="chart.songLength" placeholder="开始" />
              <el-input-number v-model="displayEnd" :min="0" :max="chart.songLength" placeholder="结束" />
            </div>
          </el-form-item>
          <el-form-item label="">
            <el-slider
              v-model="displayRange"
              range
              :min="0"
              :max="chart.songLength"
              @change="changeDisplayArea"
            ></el-slider>
          </el-form-item>
        </el-form>
      </el-dialog>
    </div>

    <!-- 时间轴 -->
    <transition
      name="fade"
      enter-active-class="animate__animated animate__fadeInUp"
      leave-active-class="animate__animated animate__fadeOutDown"
    >
      <div
        v-if="menuOpened && chartGot"
        :class="menuOpened ? 'footer-opened' : 'footer-closed'"
        :style="footerStyle"
      >
        <div
          style="height:15px;width:100%;cursor:ns-resize;text-align: center;padding-top:5px;font-size: 15px;"
          id="footer-resizer"
          @mousedown="canDrag = true"
        >
          <span style="color:rgb(200,200,200)">{{ global.currentTime }}</span>
          <span style="color:rgb(150,150,150)">/{{ chart.songLength }}</span>
        </div>
        <div style="height:calc(100% - 20px);width:100%;">
          <Footer
            :chart="chart"
            :global="global"
            @currentTrack="handleCurrentTrack"
          />
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { Axios } from '@/plugins/axios';
import { ElNotification } from 'element-plus';
import { FullScreen, RefreshLeft, VideoPause, VideoPlay, Setting, QuestionFilled } from '@element-plus/icons-vue';
import Track from "@/components/game/Track.vue";
import MenuPanel from "@/components/editor/MenuPanel.vue";
import TrackPanel from "@/components/editor/TrackPanel.vue";
import Footer from "@/components/editor/Footer.vue";
import "animate.css";

const route = useRoute();
const router = useRouter();
const store = useStore();

const canDrag = ref(false);
const chart = reactive({
  songLength: 0,
  tracks: [],
  changeBackgroundOperations: [],
  defaultBackground: '',
  songUrl: '',
  songCover: '',
  uploader: '',
  songName: '',
  songWriter: '',
  loadingText: '',
  loadedText: '',
  bpm: 0,
  firstBeatDelay: 0,
  lastBeatDelay: 0,
  beatsCount: 0
});

const chartGot = ref(false);
const bpmStart = ref(false);
const bpmcount = ref(0);
const bpmtotal = ref(0);
const startTotal = ref(0);
const lastTime = ref(0);
const startTime = ref(0);

const global = reactive({
  currentTime: 0,
  beatLine: true,
  screenWidth: 0,
  screenHeight: 0,
  remainingTime: 700,
  finalY: 0.8,
  lostTime: 150,
  pureTime: 50,
  farTime: 100,
  isEdit: true,
  keyPressTime: {},
  keyIsHold: {},
  keyUsed: {},
  notePainter: null,
  trackPainter: null,
  judgePainter: null,
  noteCanvas: null,
  trackCanvas: null,
  judgeCanvas: null,
  repaint: false,
  reCalculateTrack: false,
  reCalculateChartMaker: false,
  mouseDown: false,
  mouseUp: true,
  mouseMove: false,
  clientX: 0,
  clientY: 0,
  timeSort: true,
  documentHeight: 0,
  documentWidth: 0
});

const imagePath = ref([]);
const isRunning = ref(false);
const sliding = ref(false);
const menuOpened = ref(true);
const volume = ref(store.state.volume || 100);
const currentSelectTrack = ref(null);
const currentTracks = ref([]);
const globalSetting = ref(false);
const displayStart = ref(0);
const displayEnd = ref(10);
const footerHeight = ref(426);
const form = reactive({});

const displayRange = computed({
  get: () => [displayStart.value, displayEnd.value],
  set: (val) => {
    displayStart.value = val[0];
    displayEnd.value = val[1];
  }
});

const whiteLineLength = computed(() => {
  const time = 200;
  const waitLoad = 1000;
  if (global.currentTime > time + waitLoad && global.currentTime < chart.songLength - time) {
    return global.screenWidth;
  } else {
    if (global.currentTime <= time + waitLoad) {
      if (global.currentTime < waitLoad) return 0;
      return ((global.currentTime - waitLoad) * global.screenWidth) / time;
    } else {
      return ((chart.songLength - global.currentTime) * global.screenWidth) / time;
    }
  }
});

let audio = null;
let playInterfaceContainer = null;

const setIndex = () => {
  chart.tracks.forEach((t, i) => (t.index = i));
  chart.changeBackgroundOperations?.forEach((op, i) => (op.index = i));
};

const sortTrack = () => {
  if (global.timeSort) {
    chart.tracks.sort((a, b) => a.startTiming - b.startTiming);
  } else {
    chart.tracks.sort((a, b) => a.positionX - b.positionX);
  }
  setIndex();
};

const generateImagePath = () => {
  imagePath.value = [];
  const ops = chart.changeBackgroundOperations || [];
  let end = ops.length === 0 ? chart.songLength : (ops.sort((a,b) => a.startTime - b.startTime), ops[0].startTime);
  
  imagePath.value.push({ url: chart.defaultBackground, startTime: 0, endTime: end });
  
  ops.forEach((op, i) => {
    const start = op.startTime;
    if (i !== ops.length - 1) {
      const nextStart = ops[i+1].startTime;
      imagePath.value.push({ url: op.background, startTime: start, endTime: nextStart });
      op.endTime = nextStart;
    } else {
      imagePath.value.push({ url: op.background, startTime: start, endTime: chart.songLength + 1000 });
      op.endTime = chart.songLength + 1000;
    }
  });
};

const resetTrack = () => {
  global.keyPressTime = {};
  global.keyIsHold = {};
  global.keyUsed = {};
  chart.tracks.forEach((track) => {
    let index = 0;
    let last = track.notes.length;
    track.notes.forEach((note, j) => {
      note.judged = false;
      if (note.timing + global.lostTime > global.currentTime) index = Math.min(index, j) === 0 ? j : index; // Wrong logic in original? index = j means last one that satisfies?
      // Re-implementing logic correctly based on what it seems to do
    });
    // Original loop:
    track.notes.forEach((note, j) => {
       if (note.timing + global.lostTime > global.currentTime) {
         // This sets index to the LAST note that satisfies? No, looking at original j goes track.notes.length - 1 down to 0
       }
    });
    
    // Better:
    let foundIndex = 0;
    for (let j = track.notes.length - 1; j >= 0; j--) {
      if (track.notes[j].timing + global.lostTime > global.currentTime) foundIndex = j;
    }
    track.currentNote = foundIndex;
    
    let foundLast = track.notes.length;
    for (let j = track.notes.length - 1; j >= 0; j--) {
      if (global.currentTime < track.notes[j].timing - global.remainingTime) foundLast = j;
    }
    track.lastNote = foundLast - 1;
    
    track.judges = [];
    track.judgeFinished = track.currentNote === track.notes.length;
  });
};

const repaint = () => {
  global.notePainter?.clearRect(0, 0, global.noteCanvas.width, global.noteCanvas.height);
  global.trackPainter?.clearRect(0, 0, global.trackCanvas.width, global.trackCanvas.height);
  global.judgePainter?.clearRect(0, 0, global.judgeCanvas.width, global.judgeCanvas.height);
  global.repaint = !global.repaint;
};

const resize = () => {
  playInterfaceContainer = document.getElementById("play-interface-container");
  if (!playInterfaceContainer) return;
  global.screenWidth = playInterfaceContainer.offsetWidth;
  global.screenHeight = playInterfaceContainer.offsetHeight;
  if (global.noteCanvas) {
    global.noteCanvas.width = global.screenWidth;
    global.noteCanvas.height = global.screenHeight;
  }
  if (global.trackCanvas) {
    global.trackCanvas.width = global.screenWidth;
    global.trackCanvas.height = global.screenHeight;
  }
  if (global.judgeCanvas) {
    global.judgeCanvas.width = global.screenWidth;
    global.judgeCanvas.height = global.screenHeight;
  }
};

const run = () => {
  if (!sliding.value) {
    global.currentTime = Math.floor(audio.currentTime * 1000);
  } else {
    resetTrack();
  }
  if (global.currentTime >= chart.songLength) isRunning.value = false;
  if (global.currentTime >= displayEnd.value) {
    audio.currentTime = displayEnd.value / 1000;
    pause();
  }
  requestAnimationFrame(run);
};

const getChart = async () => {
  try {
    const { data: res } = await Axios.get(`/user/getChart?songId=${route.query.songId}`);
    if (res.code !== 0) {
      ElNotification({ title: "失败", message: "谱面获取失败！", type: "error" });
      return;
    }
    Object.assign(chart, res.data);
    chartGot.value = true;
    displayStart.value = 0;
    sortTrack();
    audio = document.getElementById("audioSong");
    audio.oncanplay = () => {
      chart.songLength = Math.round(1000 * audio.duration);
      generateImagePath();
      displayEnd.value = chart.songLength;
    };
    audio.volume = volume.value / 100;
    requestAnimationFrame(run);
    
    if (!chart.bpm || chart.bpm === 0) {
      globalSetting.value = true;
      ElNotification({ type: "warning", title: "提示", message: "请设置节拍" });
    }
    
    setTimeout(resize, 1000);
  } catch (err) {
    ElNotification({ title: "错误", message: "网络异常", type: "error" });
  }
};

const saveChart = async (back) => {
  try {
    const { data: res } = await Axios.post("/chart/editChartContent", chart);
    if (res.code !== 0) {
      ElNotification({ title: "失败", message: "谱面保存失败！", type: "error" });
      return;
    }
    ElNotification({ title: "成功", message: "谱面保存成功！", type: "success" });
    if (back) router.push("/admin");
  } catch (err) {
    ElNotification({ title: "错误", message: "网络异常", type: "error" });
  }
};

const changeMenuDisplay = () => {
  menuOpened.value = !menuOpened.value;
  for (let i = 0; i < 1000; i += 16) {
    setTimeout(() => {
      resize();
      repaint();
    }, i);
  }
};

const siderStyle = computed(() => ({
  '--footerHeight': footerHeight.value + 'px',
  '--documentHeight': global.documentHeight + 'px',
}));

const containerStyle = computed(() => ({
  '--footerHeight': footerHeight.value + 'px',
  '--documentHeight': global.documentHeight + 'px',
}));

const footerStyle = computed(() => ({
  '--footerHeight': footerHeight.value + 'px',
  '--documentHeight': global.documentHeight + 'px',
}));

const handleCurrentTrack = (param) => {
  if (currentSelectTrack.value) currentSelectTrack.value.edit = false;
  currentSelectTrack.value = param;
};

const changeTime = () => {
  resetTrack();
  audio.currentTime = global.currentTime / 1000;
  document.querySelector("#time-indicater")?.scrollIntoView({ behavior: "smooth" });
};

const SlideMouseDown = () => {
  audio.pause();
  sliding.value = true;
  resetTrack();
};

const SlideMouseUp = () => {
  sliding.value = false;
  audio.currentTime = global.currentTime / 1000;
  resetTrack();
  if (isRunning.value) {
    setTimeout(() => {
      resetTrack();
      audio.play();
    }, 50);
  }
};

const pause = () => {
  audio.pause();
  isRunning.value = false;
};

const play = () => {
  resetTrack();
  sliding.value = false;
  isRunning.value = true;
  setTimeout(() => {
    resetTrack();
    audio.play();
  }, 50);
};

const reStart = () => {
  resetTrack();
  audio.currentTime = displayStart.value / 1000;
  global.currentTime = displayStart.value;
  if (isRunning.value) {
    setTimeout(() => {
      resetTrack();
      audio.play();
    }, 50);
  }
};

const changeDisplayArea = (val) => {
  displayStart.value = val[0];
  displayEnd.value = val[1];
  global.currentTime = displayStart.value;
  audio.currentTime = global.currentTime / 1000;
  resetTrack();
};

const changeVolume = () => {
  audio.volume = volume.value / 100;
};

const ManualCalculatebpm = () => {
  if (chart.beatsCount && chart.lastBeatDelay && chart.firstBeatDelay) {
    chart.bpm = (chart.lastBeatDelay - chart.firstBeatDelay) / chart.beatsCount;
  } else {
    ElNotification({ title: "错误", message: "请先输入首拍、末拍偏移和节拍数", type: "error" });
  }
};

const calculatebpm = () => {
  if (!bpmStart.value) {
    global.currentTime = 0;
    audio.currentTime = 0;
    bpmcount.value = 0;
    lastTime.value = 0;
    bpmtotal.value = 0;
    startTotal.value = 0;
    resetTrack();
    lastTime.value = global.currentTime;
    bpmStart.value = true;
    audio.play();
  } else {
    if (bpmcount.value <= 3) {
      lastTime.value = global.currentTime;
    } else if (bpmcount.value < 10) {
      const now = global.currentTime;
      bpmtotal.value += now - lastTime.value;
      startTotal.value += now - (now - lastTime.value) * bpmcount.value;
      lastTime.value = now;
    } else {
      const now = global.currentTime;
      bpmtotal.value += now - lastTime.value;
      startTotal.value += now - (now - lastTime.value) * bpmcount.value;
      lastTime.value = now;
      chart.bpm = bpmtotal.value / (bpmcount.value - 3);
      chart.firstBeatDelay = Math.round(startTotal.value / (bpmcount.value - 3));
    }
    bpmcount.value++;
  }
};

const endbpm = () => {
  audio.pause();
  global.currentTime = 0;
  audio.currentTime = 0;
  startTotal.value = 0;
  bpmcount.value = 0;
  bpmStart.value = false;
  lastTime.value = 0;
  bpmtotal.value = 0;
  resetTrack();
};

const checkbpm = () => {
  endbpm();
  if (!chart.bpm || chart.bpm === 0) {
    setTimeout(() => {
      ElNotification({ title: "提示", message: "请设置节拍", type: "warning" });
      globalSetting.value = true;
    }, 1000);
  }
};

watch(() => global.currentTime, () => {
  repaint();
  if (sliding.value) {
    document.querySelector("#time-indicater")?.scrollIntoView({ behavior: "smooth" });
  }
});

watch(() => global.reCalculateChartMaker, () => {
  if (chart.changeBackgroundOperations) {
    generateImagePath();
    sortTrack();
    resetTrack();
  }
});

onMounted(() => {
  global.noteCanvas = document.getElementById("note-canvas");
  global.trackCanvas = document.getElementById("track-canvas");
  global.judgeCanvas = document.getElementById("judge-canvas");
  global.notePainter = global.noteCanvas.getContext("2d");
  global.trackPainter = global.trackCanvas.getContext("2d");
  global.judgePainter = global.judgeCanvas.getContext("2d");
  
  global.documentHeight = document.documentElement.clientHeight;
  global.documentWidth = document.documentElement.clientWidth;
  
  window.addEventListener('resize', resize);
  
  document.onkeydown = (e) => {
    if (!e.repeat) {
      global.keyPressTime[e.key.toUpperCase()] = global.currentTime;
      global.keyIsHold[e.key.toUpperCase()] = true;
      global.keyUsed[e.key.toUpperCase()] = false;
      if (e.key === " ") {
        isRunning.value ? pause() : play();
      }
    }
    if (e.key === "ArrowUp") {
      volume.value = Math.min(100, volume.value + 10);
      changeVolume();
    } else if (e.key === "ArrowDown") {
      volume.value = Math.max(0, volume.value - 10);
      changeVolume();
    }
  };

  document.onkeyup = (e) => {
    global.keyIsHold[e.key.toUpperCase()] = false;
  };

  const container = document.getElementById("play-interface-container");
  container.onmousedown = (e) => {
    if (currentSelectTrack.value) currentSelectTrack.value.edit = false;
    currentSelectTrack.value = null;
    
    const visibleTracks = chart.tracks.filter(t => global.currentTime > t.startTiming && global.currentTime < t.endTiming);
    for (const track of visibleTracks) {
      const left = (track.tempPositionX - track.tempWidth) * global.screenWidth;
      const right = (track.tempPositionX + track.tempWidth) * global.screenWidth;
      if (e.offsetX > left && e.offsetX < right) {
        currentSelectTrack.value = track;
        document.querySelector("#trackCard" + track.index)?.scrollIntoView({ behavior: "smooth" });
        document.querySelector("#trackCardPanel" + track.index)?.scrollIntoView({ behavior: "smooth" });
        setTimeout(() => { track.edit = true; }, 10);
        break;
      }
    }
  };

  document.onmousemove = (e) => {
    global.clientX = e.clientX;
    global.clientY = e.clientY;
    global.mouseMove = !global.mouseMove;
    if (canDrag.value) {
      if (e.clientY > 130 && e.clientY < global.documentHeight - 100) {
        footerHeight.value = global.documentHeight - e.clientY;
        resize();
        setTimeout(repaint, 10);
      }
    }
  };

  document.onmouseup = () => {
    if (sliding.value) SlideMouseUp();
    canDrag.value = false;
    global.mouseUp = !global.mouseUp;
  };

  document.onmousedown = (e) => {
    global.clientX = e.clientX;
    global.clientY = e.clientY;
    global.mouseDown = !global.mouseDown;
  };

  getChart();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize);
});
</script>

<style scoped>
.animate__animated.animate__fadeInLeft { --animate-duration: 0.5s; }
.animate__animated.animate__fadeOutLeft { --animate-duration: 0.5s; }
.animate__animated.animate__fadeInUp { --animate-duration: 0.5s; }
.animate__animated.animate__fadeOutDown { --animate-duration: 0.5s; }

.play-interface {
  height: 100vh;
  width: 100vw;
  background: rgb(55, 55, 55);
  overflow: auto;
}
.select {
  user-select: none;
}
.header-button { color: white; }
.header-button:hover { color: rgb(234, 234, 234); }
.header-button:active { color: rgb(212, 212, 212); }

.header {
  position: absolute;
  top: 0;
  height: 50px;
  width: 100%;
  background: rgb(39, 39, 39);
  z-index: 100;
}
.header-buttons {
  padding: 10px 10px 0px 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#play-interface-container {
  position: absolute;
  top: 50px;
  background: rgb(32, 32, 32);
}

.sider-closed {
  position: absolute;
  top: 50px;
  height: calc(var(--documentHeight) - 50px - var(--footerHeight));
  background: rgb(32, 32, 32);
  width: 0px;
  left: 0px;
}
.sider-opened {
  position: absolute;
  top: 50px;
  height: calc(var(--documentHeight) - 50px - var(--footerHeight));
  background: rgb(32, 32, 32);
  width: 300px;
  left: 0px;
  overflow: auto;
  padding-bottom: 20px;
  z-index: 50;
}

.sider-closed-track {
  position: absolute;
  top: 50px;
  height: calc(var(--documentHeight) - 50px - var(--footerHeight));
  background: rgb(32, 32, 32);
  width: 0px;
  left: 0px;
}
.sider-opened-track {
  position: absolute;
  top: 50px;
  height: calc(var(--documentHeight) - 50px - var(--footerHeight));
  background: rgb(32, 32, 32);
  width: 300px;
  left: 300px;
  overflow: auto;
  padding-bottom: 20px;
  z-index: 50;
}

.footer-closed {
  position: absolute;
  bottom: 0px;
  height: 0px;
  width: 100vw;
  left: 0px;
}
.footer-opened {
  position: absolute;
  bottom: 0px;
  height: var(--footerHeight);
  background: rgb(55, 55, 55);
  width: 100vw;
  left: 0px;
  z-index: 60;
}

.container-small {
  left: 600px;
  top: 50px;
  width: calc(100vw - 600px);
  height: calc(var(--documentHeight) - 120px - var(--footerHeight));
}
.container-big {
  left: 0px;
  top: 50px;
  width: 100vw;
  height: calc(var(--documentHeight) - 120px);
  transition: 0.5s;
}

.time-controller-small {
  position: absolute;
  left: 600px;
  bottom: var(--footerHeight);
  height: 80px;
  width: calc(100vw - 600px);
  background: rgb(32, 30, 32);
  z-index: 70;
}
.time-controller-big {
  position: absolute;
  left: 0px;
  bottom: 0px;
  height: 80px;
  width: 100vw;
  background: rgb(32, 30, 32);
  z-index: 70;
}
.time-control-buttons {
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}
.time-control-buttons .el-button {
  font-size: 24px;
}
.header-slide-item {
  width: 96%;
  padding: 0px 2%;
}

.selected-track {
  box-shadow: 0 0 20px 10px rgba(0, 0, 0, 0.5);
  pointer-events: none;
}

:deep(.el-slider__bar) { background-color: rgb(138, 138, 138); }
:deep(.el-slider__button) { border: 2px solid rgb(138, 138, 138); }
</style>
