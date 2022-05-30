<template>
  <div class="menu-panel-container">
    <h4
      style="padding:5px;margin-top:10px;margin-bottom:5x;color:rgb(225,225,225)"
    >
      背景操作
    </h4>
    <hr style="border:0.5px solid rgba(100,100,100,0.1)" />
    <div
      style="width:100%;height:20px;padding:5px;display: flex;
          justify-content: space-between; align-items: center;"
    >
      <div>
        <el-button
          type="text"
          class="plus-button"
          icon="el-icon-circle-plus"
          @click="newOperation"
          >新增</el-button
        >
      </div>
      <div>
        <el-switch
          v-model="operationShowAll"
          inline-prompt
          active-text="显示全部"
        />
      </div>
    </div>

    <transition-group
      name="flip-list"
      enter-active-class="animate__animated animate__fadeInUp"
      leave-active-class="animate__animated animate__fadeOutUp"
    >
      <div
        v-for="operation in chart.changeBackgroundOperations"
        :key="operation"
      >
        <transition
          name="flip-list"
          enter-active-class="animate__animated animate__fadeInUp"
          leave-active-class="animate__animated animate__fadeOutUp"
        >
          <BackgroundOperation
            v-show="
              operationShowAll ||
                (global.currentTime > operation.startTime &&
                  global.currentTime < operation.endTime)
            "
            :id="'backgroundOperation' + operation.index"
            :chart="chart"
            :operation="operation"
            :global="global"
          />
        </transition>
      </div>
    </transition-group>
  </div>
</template>

<script>
import BackgroundOperation from "./BackgroundOperation";
import "animate.css";
export default {
  props: ["chart", "global"],

  data() {
    return {
      operationShowAll: true,
      trackShowAll: true,
      myChart: this.chart,
      myGlobal: this.global,
    };
  },
  components: { BackgroundOperation },
  watch: {
    chart() {
      this.myChart = this.chart;
    },
    "global.currentTime"() {
      for (var i = 0; i < this.chart.changeBackgroundOperations.length; i++) {
        if (
          this.global.currentTime >
            this.chart.changeBackgroundOperations[i].startTime &&
          this.global.currentTime <
            this.chart.changeBackgroundOperations[i].endTime
        ) {
          if(i>0)
          document
            .querySelector(
              "#backgroundOperation" +
                this.chart.changeBackgroundOperations[i-1].index
            )
            .scrollIntoView(true);
          break;
        }
      }
    },
  },
  methods: {
    updateOperation() {
      this.myGlobal.reCalculateChartMaker = !this.myGlobal
        .reCalculateChartMaker;
    },
    newOperation() {
      var operation = {
        startTime: 0,
        background: this.myChart.defaultBackground,
      };
      this.myChart.changeBackgroundOperations.push(operation);
      this.updateOperation();
      setTimeout(() => {
        this.myChart.changeBackgroundOperations[0].edit = true;
      }, 10);
    },
  },
};
</script>
<style scope>
.menu-panel-container {
  height: calc(100vh - 350px);
  width: 90%;
  padding: 0% 5%;
  overflow: auto;
  -ms-overflow-style: none;
}
.plus-button {
  color: #67c23a;
}
.plus-button:hover {
  color: #95d475;
}
.plus-button:active {
  color: #529b2e;
}
.flip-list-move {
  transition: transform 1s;
}

.animate__animated.animate__fadeInLeft {
  --animate-duration: 0.2s;
}
.animate__animated.animate__fadeOutLeft {
  --animate-duration: 0.2s;
}
.animate__animated.animate__fadeInUp {
  --animate-duration: 0.2s;
}
.animate__animated.animate__fadeOutUp {
  --animate-duration: 0.2s;
}

.menu-panel-container::-webkit-scrollbar {
  width: 0 !important;
}
</style>
