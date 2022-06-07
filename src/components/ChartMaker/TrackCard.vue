<template>
  <div :class="currentClass" @dblclick="currentTrack">
    <div
      style="width:100%;display: flex;justify-content: space-between;border: none;"
    >
      <div
        style="width: 70px;height:70px;border-radius: 5px;position:relative;pointer-events: none;"
      >
        <el-image
          style="position:absolute;top:0;left:0;width: 70px;height:70px;border-radius: 5px;"
          :src="require('@/assets/img/EpheTrack.jpg')"
          fit="fit"
          class="image"
        />
        <div
          style="position:absolute;top:0;left:0;width: 70px;height:70px;border-radius: 5px;
        text-align:center; line-height: 70px; color:white; text-shadow:2px 2px 5px black; font-size: 50px;"
        >
          {{ myTrack.type == 1 ? myTrack.key.toUpperCase() : "虚" }}
        </div>
        <div
          v-if="
            global.currentTime > track.startTiming &&
              global.currentTime < track.endTiming
          "
          :style="{
            position: 'absolute',
            height: '70px',
            top: 0,
            left: (myTrack.tempPositionX - myTrack.tempWidth) * 160 + 75 + 'px',
            width: 2 * myTrack.tempWidth * 160 + 'px',
            background:
              'rgba(' +
              myTrack.tempR +
              ',' +
              myTrack.tempG +
              ',' +
              myTrack.tempB +
              ',0.5)',
          }"
        ></div>
      </div>
      <div style="width:calc(100% - 80px);">
        <div
          style="width:100%;height:20px; display: flex;justify-content: space-between; align-items: center;line-height: 20px;"
        >
          <div style="font-weight:800">轨道{{ track.index + 1 }}</div>
          <div>
            <el-tooltip
              class="item"
              effect="dark"
              :content="
                myTrack.showInTimeline ? '在时间轴中隐去' : '在时间轴中显示'
              "
              placement="top"
            >
              <el-button
                type="text"
                class="hide-button"
                :icon="
                  myTrack.showInTimeline ? 'el-icon-minus' : 'el-icon-view'
                "
                @click="myTrack.showInTimeline = !myTrack.showInTimeline"
              />
            </el-tooltip>
            <el-button
              v-if="!myTrack.edit"
              type="text"
              class="edit-button"
              icon="el-icon-s-tools"
              @click="startEdit"
            />
            <el-button
              v-if="myTrack.edit && !myTrack.isNew"
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
    <transition
      name="flip-list"
      enter-active-class="animate__animated animate__fadeInDown"
      leave-active-class="animate__animated animate__fadeOutUp"
    >
      <div v-show="myTrack.edit" style="margin-top:20px;">
        <el-form
          :model="myTrack.tempTrack"
          :rules="rules"
          ref="form"
          @submit.prevent="saveTrack"
        >
          <el-form-item label="轨道类别" label-width="80px" prop="type">
            <el-radio-group v-model="myTrack.tempTrack.type"  style="width:130px;line-height: 20px;">
              <el-radio :label="0">虚轨</el-radio>
              <el-radio :label="1">实轨</el-radio>
            </el-radio-group>
            
            <el-tooltip
              class="item"
              effect="dark"
              content="设置轨道的类别，如果是虚轨，则轨道下方不会出现提示字母，如果虚轨上有音符，请您确认在音符对应的时机该轨道移动到音符字母对应的实轨上"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
          </el-form-item>
          <el-form-item label="按键" label-width="80px" prop="key">
            <el-input
              @keydown.enter="saveTrack"
              v-model="myTrack.tempTrack.key"
              style="width:130px"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="设置轨道的按键，请输入单个字母，不区分大小写"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
          </el-form-item>
          <el-form-item label="开始时机" label-width="80px" prop="startTiming">
            <el-input
              @keydown.enter="saveTrack"
              v-model="myTrack.tempTrack.startTiming"
              style="width:130px"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="设置轨道的开始时机"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
          </el-form-item>
          <el-form-item label="结束时机" label-width="80px" prop="endTiming">
            <el-input
              @keydown.enter="saveTrack"
              v-model="myTrack.tempTrack.endTiming"
              style="width:130px"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="设置轨道的结束时机"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
          </el-form-item>
          <el-form-item label="横坐标" label-width="80px" prop="positionX">
            <el-input
              @keydown.enter="saveTrack"
              v-model="myTrack.tempTrack.positionX"
              style="width:130px"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="设置轨道的横坐标，请输入一个小数，代表轨道横坐标占画面全宽的比例"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
          </el-form-item>
          <el-form-item label="宽度" label-width="80px" prop="width">
            <el-input
              @keydown.enter="saveTrack"
              v-model="myTrack.tempTrack.width"
              style="width:130px"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="设置轨道的宽度，您输入的数值是宽度的一半，它是轨道一半的宽度占整个画面的比例"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
          </el-form-item>
          <el-form-item label="默认颜色" label-width="80px" prop="color">
            <el-color-picker
              v-model="myTrack.tempTrack.color"
              color-format="rgb"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="设置轨道的默认颜色"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
          </el-form-item>
        </el-form>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  props: ["track", "global", "chart"],
  data() {
    var checkKey = (rule, value, callback) => {
      if (!value) {
        rule;
        return callback(new Error("按键不能为空"));
      }
      var reg = /^[A-Za-z]$/;
      if (reg.test(value)) {
        callback();
      } else {
        callback(new Error("按键必须是单个字母"));
      }
    };
    var checkStartTime = (rule, value, callback) => {
      if (value != 0 && !value) {
        rule;
        return callback(new Error("开始时机不能为空"));
      }
      if (parseFloat(value).toString() == "NaN") {
        callback(new Error("请输入数字值"));
      } else {
        if (value < 0) {
          callback(new Error("开始时机不能小于0"));
        } else if (value > this.chart.songLength) {
          callback(new Error("开始时机不能超过歌曲长度"));
        } else if (value > this.myTrack.tempTrack.endTiming) {
          callback(new Error("开始时机不能超过结束时机"));
        } else {
          callback();
        }
      }
    };
    var checkEndTime = (rule, value, callback) => {
      if (value != 0 && !value) {
        rule;
        return callback(new Error("结束时机不能为空"));
      }
      if (parseFloat(value).toString() == "NaN") {
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
    var checkWidth = (rule, value, callback) => {
      if (!value) {
        rule;
        return callback(new Error("宽度不能为空"));
      }
      if (parseFloat(value).toString() == "NaN") {
        callback(new Error("请输入数字值"));
      } else {
        callback();
      }
    };
    var checkPositionX = (rule, value, callback) => {
      if (!value) {
        rule;
        return callback(new Error("横坐标不能为空"));
      }
      if (parseFloat(value).toString() == "NaN") {
        callback(new Error("请输入数字值"));
      } else {
        callback();
      }
    };
    return {
      myTrack: this.track,
      myGlobal: this.global,
      myChart: this.chart,
      form: {},
      rules: {
        type: [{ required: true, message: "请选择轨道类别", trigger: "blur" }],
        key: [{ required: true, validator: checkKey, trigger: "blur" }],
        startTiming: [
          { required: true, validator: checkStartTime, trigger: "blur" },
        ],
        endTiming: [
          { required: true, validator: checkEndTime, trigger: "blur" },
        ],
        width: [{ required: true, validator: checkWidth, trigger: "blur" }],
        positionX: [
          { required: true, validator: checkPositionX, trigger: "blur" },
        ],
        color: [{ required: true, message: "请选择颜色", trigger: "blur" }],
      },
    };
  },
  created() {
    this.myTrack.edit = false;
    this.myTrack.showInTimeline = true;
    this.myTrack.tempTrack = JSON.parse(JSON.stringify(this.myTrack));
    this.myTrack.tempTrack.key = this.myTrack.tempTrack.key.toUpperCase();
    this.myTrack.tempTrack.color =
      "rgb(" +
      this.myTrack.R +
      "," +
      this.myTrack.G +
      "," +
      this.myTrack.B +
      ")";
  },
  methods: {
    updateTrack() {
      this.myGlobal.reCalculateTrack = !this.myGlobal.reCalculateTrack;
      this.myGlobal.reCalculateChartMaker = !this.myGlobal
        .reCalculateChartMaker;
    },
    currentTrack() {
      this.$emit("currentTrack", this.myTrack);
      this.startEdit();
    },
    startEdit() {
      this.myTrack.edit = true;
      document
        .querySelector("#trackCard" + this.myTrack.index)
        .scrollIntoView(true);
      this.myTrack.tempTrack = JSON.parse(JSON.stringify(this.myTrack));
      this.myTrack.tempTrack.key = this.myTrack.tempTrack.key.toUpperCase();
      this.myTrack.tempTrack.color =
        "rgb(" +
        this.myTrack.R +
        "," +
        this.myTrack.G +
        "," +
        this.myTrack.B +
        ")";
    },
    saveTrack() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          setTimeout(() => {
            this.updateTrack();
          }, 500);
          for (var key in this.myTrack.tempTrack) {
            if (key != "tempTrack")
              this.myTrack[key] = this.myTrack.tempTrack[key];
          }
          this.myTrack.key = this.myTrack.key.toUpperCase();
          var rgb = this.myTrack.tempTrack.color
            .substring(4, this.myTrack.tempTrack.color.length - 1)
            .split(",");
          this.myTrack.R = rgb[0];
          this.myTrack.G = rgb[1];
          this.myTrack.B = rgb[2];
          this.myTrack.edit = false;
          if (this.myTrack.isNew) this.$emit("editStatus", true);
          this.myTrack.isNew = false;
        } else {
          return false;
        }
      });
    },
    deleteTrack() {
      this.$confirm("您确定删除该轨道?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        if (this.myTrack.isNew) this.$emit("editStatus", true);
        this.myChart.tracks.splice(this.myTrack.index, 1);
        this.updateTrack();
        this.$notify({
          title: "成功",
          message: "删除成功",
          type: "success",
        });
      });
    },
  },
  computed: {
    currentClass() {
      var currentClass = "";
      if (this.myTrack.edit) {
        currentClass = "edit-track ";
      } else {
        currentClass = "not-edit-track ";
      }
      if (
        this.global.currentTime > this.track.startTiming &&
        this.global.currentTime < this.track.endTiming
      ) {
        currentClass += "current-track";
      } else if (this.global.currentTime > this.track.endTiming) {
        currentClass += "passed-track";
      } else {
        currentClass += "to-come-track";
      }
      return currentClass;
    },
  },
};
</script>

<style scope>
.not-edit-track {
  height: 70px;
  width: calc(100% - 30px);
  margin: 10px;
  padding: 5px;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.edit-track {
  height: 505px;
  width: calc(100% - 30px);
  margin: 10px;
  padding: 5px;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.hide-button {
  color: rgb(218, 218, 218);
}
.hide-button:hover {
  color: rgb(239, 239, 239);
}
.hide-button:active {
  color: rgb(183, 183, 183);
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
.current-track {
  background: rgb(47, 47, 47);
  color: rgb(171, 171, 171);
  box-shadow: 0 0 5px 2px rgba(255, 255, 255, 0.5);
  transition: 0.5s;
}

.current-track .el-form-item__label {
  color: rgb(171, 171, 171);
  transition: 0.5s;
}

.passed-track {
  background: rgb(30, 30, 30);
  color: rgb(100, 100, 100);
  box-shadow: 0 0 0px 0px rgba(127, 127, 127, 0.5);
  transition: 0.5s;
}

.passed-track .el-form-item__label {
  color: rgb(171, 171, 171);
  transition: 0.5s;
}

.to-come-track {
  background: #2f2f2f;
  color: rgb(171, 171, 171);
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
  transition: 0.5s;
}

.to-come-track .el-form-item__label {
  color: rgb(171, 171, 171);
  transition: 0.5s;
}
</style>
