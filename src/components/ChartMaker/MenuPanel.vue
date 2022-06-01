<template>
  <div class="menu-panel-container">
    <div style="background:rgb(32, 32, 32);height:70px;">
      <h4
        style="padding:5px;padding-top:10px;padding-bottom:5x;color:rgb(225,225,225);padding-left:10px;"
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
            style="margin-left:10px;"
            class="plus-button"
            icon="el-icon-circle-plus"
            @click="newOperation"
            >新增</el-button
          >
        </div>
        <div>
          <el-button
            type="text"
            class="show-button"
            style="margin-right:5px;"
            @click="autoScroll = !autoScroll"
            >{{ autoScroll ? "关闭滚动" : "开启滚动" }}</el-button
          >
          <el-button
            type="text"
            class="show-button"
            style="margin-right:13px;"
            @click="operationShowAll = !operationShowAll"
            >{{ operationShowAll ? "显示当前" : "显示全部" }}</el-button
          >
        </div>
      </div>
    </div>
    <div
      class="menu-container-container"
      :style="{ height: Height - 70 + 'px' }"
    >
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
              @editStatus="editStatus"
              :editFinished="editFinished"
            />
          </transition>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script>
import BackgroundOperation from "./BackgroundOperation";
import "animate.css";
export default {
  props: ["chart", "global", "Height"],

  data() {
    return {
      operationShowAll: true,
      autoScroll: true,
      myChart: this.chart,
      myGlobal: this.global,
      editFinished: true,
    };
  },
  components: { BackgroundOperation },
  watch: {
    chart() {
      this.myChart = this.chart;
    },
    "global.currentTime"() {
      if (this.autoScroll) {
        for (var i = 0; i < this.chart.changeBackgroundOperations.length; i++) {
          if (
            this.global.currentTime >
              this.chart.changeBackgroundOperations[i].startTime &&
            this.global.currentTime <
              this.chart.changeBackgroundOperations[i].endTime
          ) {
            if (i > 0)
              setTimeout(() => {
                document
                  .querySelector(
                    "#backgroundOperation" +
                      this.chart.changeBackgroundOperations[i - 1].index
                  )
                  .scrollIntoView(true);
              }, 200);
            else
              setTimeout(() => {
                document
                  .querySelector(
                    "#backgroundOperation" +
                      this.chart.changeBackgroundOperations[0].index
                  )
                  .scrollIntoView(true);
              }, 200);
            break;
          }
        }
      }
    },
  },
  methods: {
    updateOperation() {
      this.myGlobal.reCalculateChartMaker = !this.myGlobal
        .reCalculateChartMaker;
    },
    editStatus(param) {
      this.editFinished = param;
    },
    newOperation() {
      if (this.editFinished) {
        this.editFinished=false;
        var operation = {
          startTime: 0,
          isNew: true,
          background: this.myChart.defaultBackground,
        };
        this.myChart.changeBackgroundOperations.push(operation);
        this.updateOperation();
        setTimeout(() => {
          this.myChart.changeBackgroundOperations[0].edit = true;
          document.querySelector("#backgroundOperation0").scrollIntoView(true);
        }, 10);
      } else {
        this.$notify({
          title: "提示",
          message: "请先完成正在编辑的操作",
          type: "warning",
        });
        for (
          var i = 0;
          i < this.myChart.changeBackgroundOperations.length;
          i++
        ) {
          if (this.myChart.changeBackgroundOperations[i].edit) {
            document
              .querySelector("#backgroundOperation" + i)
              .scrollIntoView(true);
            break;
          }
        }
      }
    },
  },
};
</script>
<style scope>
.menu-panel-container {
  padding: 0% 5%;
  width: calc(90% - 1px);
  border: 0px solid rgba(255, 255, 255, 0.2);
  background: rgb(32, 32, 32);
  border-right-width: 1px;
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
.show-button {
  color: #c1c1c1;
}
.show-button:hover {
  color: #d0d0d0;
}
.show-button:active {
  color: #656565;
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

.menu-container-container::-webkit-scrollbar {
  width: 0 !important;
}

.menu-container-container {
  overflow-y: scroll;
}
</style>
