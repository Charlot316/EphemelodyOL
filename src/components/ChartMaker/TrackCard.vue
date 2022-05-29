<template>
  <div :class="currentClass">
    <div
      style="width:100%;display: flex;justify-content: space-between;border: none;"
    >
      <div>
      <h1>轨道</h1>
      </div>
      <div style="width:calc(100% - 80px);">
        <div
          style="width:100%;height:20px; display: flex;justify-content: space-between; align-items: center;line-height: 20px;"
        >
          <div style="font-weight:800">操作{{ track.index + 1 }}</div>
          <div>
            <el-button
              v-if="!mytrack.edit"
              type="text"
              class="edit-button"
              icon="el-icon-s-tools"
              @click="startEdit"
            />
            <el-button
              v-if="myTrack.edit"
              type="text"
              class="cancel-button"
              icon="el-icon-error"
              @click="myTrack.edit = false"
            />
            <el-button
              v-if="myTrack.edit"
              type="text"
              class="ok-button"
              icon="el-icon-success"
              @click="saveTrack"
            />
            <el-button
              type="text"
              class="delete-button"
              icon="el-icon-remove"
              @click="deleteTrack"
            />
          </div>
        </div>
        <div style="width:100%;margin-top:10px;">
          <h4>时机 {{ myTrack.startTiming }}</h4>
        </div>
      </div>
    </div>
    <div v-show="myTrack.edit">
      <el-form
        :model="tempTrack"
        :rules="rules"
        ref="form"
        @submit.prevent="saveTrack"
      >
        <el-form-item label="时机" label-width="80px" prop="startTiming">
          <el-input
            @keydown.enter="saveTrack"
            v-model="tempTrack.startTiming"
            style="width:100px"
          />
          <el-tooltip
            class="item"
            effect="dark"
            content="设置操作的时机"
            placement="top-start"
            style="margin-left:10px;"
          >
            <i class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  props: ["track", "global", "chart"],
  data() {
    var checkStartTime = (rule, value, callback) => {
      if (!value) {
        rule;
        return callback(new Error("时机不能为空"));
      }
      if (Number.isNaN(value)) {
        callback(new Error("请输入数字值"));
      } else {
        if (value < 0) {
          callback(new Error("时机不能小于0"));
        } else if (value > this.chart.songLength) {
          callback(new Error("时机不能超过歌曲长度"));
        } else {
          callback();
        }
      }
    };
    return {
      mytrack: this.track,
      myGlobal: this.global,
      myChart: this.chart,
      tempTrack: {},
      form: {},
      rules: {
        startTiming: [{ validator: checkStartTime, trigger: "blur" }],
        background: [{ required: true, trigger: "blur" }],
      },
    };
  },
  created() {
    this.myTrack.edit = false;
  },
  methods: {
    updateTrack() {
      this.myGlobal.reCalculateChartMaker = !this.myGlobal
        .reCalculateChartMaker;
    },
    startEdit() {
      this.myTrack.edit = true;
      this.tempTrack = JSON.parse(JSON.stringify(this.myTrack));
    },
    saveTrack() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          setTimeout(() => {
            this.updateTrack();
          }, 500);

          for (var key in this.tempTrack) {
            this.myTrack[key] = this.tempTrack[key];
          }
          this.myTrack.edit = false;
        } else {
          return false;
        }
      });
    },
    deleteTrack() {
      this.$confirm("您确定删除该操作?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.myChart.tracks.splice(
          this.myTrack.index,
          1
        );
        this.updateTrack();
        this.$notify({
          title: "成功",
          message: "删除成功",
          position: "top-left",
          type: "success",
        });
      });
    },
  },
  computed: {
    currentClass() {
      var currentClass = "";
      if (this.myTrack.edit) {
        currentClass = "edit ";
      } else {
        currentClass = "not-edit ";
      }
      if (
        this.global.currentTime > this.track.startTiming &&
        this.global.currentTime < this.track.endTiming
      ) {
        currentClass += "current-track";
      } else if (this.global.currentTime > this.track.endTiming) {
        currentClass += "passed-track";
      } else if (this.global.currentTime < this.track.startTiming) {
        currentClass += "to-come-track ";
      }
      return currentClass;
    },
  },
};
</script>

<style scope>
.not-edit {
  height: 70px;
  width: calc(100% - 30px);
  margin: 10px;
  padding: 5px;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.edit {
  height: 170px;
  width: calc(100% - 30px);
  margin: 10px;
  padding: 5px;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.current-track {
  background: white;
  color: #303133;
  -webkit-box-shadow: 0 0 9px 4px rgba(127, 127, 127, 0.5);
  box-shadow: 0 0 9px 4px rgba(127, 127, 127, 0.5);
  transition: 0.5s;
}
.passed-track {
  background: #f3f1f1;
  color: rgb(110, 110, 110);
  -webkit-box-shadow: 0 0 0px 0px rgba(127, 127, 127, 0.5);
  box-shadow: 0 0 0px 0px rgba(127, 127, 127, 0.5);
  transition: 0.5s;
}
.to-come-track {
  background: white;
  color: rgb(32, 32, 32);
  -webkit-box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
  transition: 0.5s;
}
.delete-button {
  color: #f56c6c;
}
.delete-button:hover {
  color: #f89898;
}
.delete-button:active {
  color: #c45656;
}
.ok-button {
  color: #67c23a;
}
.ok-button:hover {
  color: #95d475;
}
.ok-button:active {
  color: #529b2e;
}
.cancel-button {
  color: #909399;
}
.cancel-button:hover {
  color: #b1b3b8;
}
.cancel-button:active {
  color: #73767a;
}
</style>
