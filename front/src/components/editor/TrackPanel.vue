<template>
  <div class="track-panel-container">
    <div style="background:rgb(32, 32, 32);height:70px;">
      <h4
        style="padding:5px;padding-top:10px;padding-bottom:5x;color:rgb(225,225,225);padding-left:10px;"
      >
        轨道列表
      </h4>
      <hr style="border:0.5px solid rgba(100,100,100,0.1)" />
      <div
        style="width:100%;height:20px;padding:5px;display: flex;
          justify-content: space-between; align-items: center;"
      >
        <div>
          <el-button
            link
            style="margin-left:10px;"
            class="plus-button"
            @click="newTrack"
          >
            <el-icon><CirclePlus /></el-icon>
            新增
          </el-button>
        </div>
        <div>
          <el-button
            link
            class="show-button"
            style="margin-right:5px;"
            @click="autoScroll = !autoScroll"
          >{{ autoScroll ? "关闭滚动" : "开启滚动" }}</el-button>
          <el-button
            link
            class="show-button"
            style="margin-right:13px;"
            @click="trackShowAll = !trackShowAll"
          >{{ trackShowAll ? "显示当前" : "显示全部" }}</el-button>
        </div>
      </div>
    </div>

    <div
      class="track-container-container"
      :style="{ height: Height - 70 + 'px' }"
      v-if="chart.tracks"
    >
      <transition-group
        name="flip-list"
        enter-active-class="animate__animated animate__fadeInUp"
        leave-active-class="animate__animated animate__fadeOutUp"
      >
        <div v-for="(trackItem, idx) in chart.tracks" :key="trackItem.trackId || ('panel-track-' + idx)">
          <transition
            name="flip-list"
            enter-active-class="animate__animated animate__fadeInUp"
            leave-active-class="animate__animated animate__fadeOutUp"
          >
            <TrackCard
              v-show="
                trackShowAll ||
                  (global.currentTime > trackItem.startTiming &&
                    global.currentTime < trackItem.endTiming)
              "
              :chart="chart"
              :track="trackItem"
              :global="global"
              :id="'trackCard' + trackItem.index"
              @editStatus="handleEditStatus"
              @currentTrack="handleCurrentTrack"
            />
          </transition>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue';
import { CirclePlus } from '@element-plus/icons-vue';
import { ElNotification } from 'element-plus';
import TrackCard from "./TrackCard.vue";
import "animate.css";

const props = defineProps({
  chart: Object,
  global: Object,
  Height: Number
});

const emit = defineEmits(["currentTrack"]);

const editFinished = ref(true);
const trackShowAll = ref(true);
const autoScroll = ref(true);

const handleCurrentTrack = (param) => {
  emit("currentTrack", param);
};

const handleEditStatus = (param) => {
  editFinished.value = param;
};

const updateOperation = () => {
  props.global.reCalculateChartMaker = !props.global.reCalculateChartMaker;
};

const newTrack = () => {
  if (editFinished.value) {
    editFinished.value = false;
    const track = {
      startTiming: 0,
      endTiming: 150,
      isNew: true,
      type: 1,
      key: "D",
      R: "160",
      G: "160",
      B: "160",
      background: props.chart.defaultBackground,
      notes: [],
      moveOperations: [],
      changeWidthOperations: [],
      changeColorOperations: [],
      edit: true
    };
    props.chart.tracks.push(track);
    props.global.timeSort = true;
    updateOperation();
    
    document.querySelector("#trackCard0")?.scrollIntoView({ behavior: "auto" });
  } else {
    ElNotification({
      title: "提示",
      message: "请先完成正在编辑的轨道",
      type: "warning",
    });
    const editIndex = props.chart.tracks.findIndex(t => t.edit);
    if (editIndex !== -1) {
      document.querySelector("#trackCard" + editIndex)?.scrollIntoView({ behavior: "auto" });
    }
  }
};

watch(() => props.global.currentTime, (newVal) => {
  if (autoScroll.value) {
    const tracks = props.chart.tracks;
    for (let i = tracks.length - 1; i >= 0; i--) {
      if (newVal > tracks[i].startTiming && newVal < tracks[i].endTiming) {
        const scrollIndex = Math.max(0, i - 2);
        document.querySelector("#trackCard" + tracks[scrollIndex].index)?.scrollIntoView({ behavior: "auto" });
        break;
      }
    }
  }
});
</script>

<style scoped>
.track-panel-container {
  width: 90%;
  padding: 0% 5%;
  height: 100%;
}
.plus-button { color: #888; }
.plus-button:hover { color: #fff; }
.plus-button:active { color: #aaa; }

.flip-list-move { transition: transform 1s; }

.animate__animated.animate__fadeInUp { --animate-duration: 0.2s; }
.animate__animated.animate__fadeOutUp { --animate-duration: 0.2s; }

.track-container-container::-webkit-scrollbar { width: 0 !important; }

.track-container-container {
  overflow-y: scroll;
  padding-bottom: 20px;
}
</style>
