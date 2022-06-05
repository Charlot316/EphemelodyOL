<template>
  <div
    @click="selfClicked"
    :style="{
      position: 'absolute',
      top: '20px',
      left: left + 'px',
    }"
  >
    <el-popover
      v-model:visible="edit"
      placement="top"
      width="300"
      trigger="manual"
    >
      <div style="text-align:right;">
        <el-button
          type="text"
          class="cancel-button"
          icon="el-icon-error"
          @click="edit = false"
        />
        <el-button
          type="text"
          class="ok-button"
          icon="el-icon-success"
          @click="saveOperation"
        />
        <el-button
          type="text"
          class="delete-button"
          icon="el-icon-remove"
          @click="deleteOperation"
        />
      </div>
      <el-form
        :model="myOperation.tempOperation"
        :rules="rules"
        ref="form"
        @submit.prevent="saveOperation"
      >
        <el-form-item label="开始时机" label-width="80px" prop="startTime">
          <el-input
            @keydown.enter="saveOperation"
            v-model="myOperation.tempOperation.startTime"
            style="width:130px"
          />
          <el-tooltip
            class="item"
            effect="dark"
            content="设置操作的起始时机"
            placement="top-start"
            style="margin-left:10px;"
          >
            <i class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="结束时机" label-width="80px" prop="endTime">
          <el-input
            @keydown.enter="saveOperation"
            v-model="myOperation.tempOperation.endTime"
            style="width:130px"
          />
          <el-tooltip
            class="item"
            effect="dark"
            content="设置操作的结束时机"
            placement="top-start"
            style="margin-left:10px;"
          >
            <i class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="开始颜色" label-width="80px" prop="startColor">
          <el-color-picker
            v-model="myOperation.tempOperation.startColor"
            color-format="rgb"
          />
          <el-tooltip
            class="item"
            effect="dark"
            content="设置操作的开始颜色"
            placement="top-start"
            style="margin-left:10px;"
          >
            <i class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="结束颜色" label-width="80px" prop="endColor">
          <el-color-picker
            v-model="myOperation.tempOperation.endColor"
            color-format="rgb"
          />
          <el-tooltip
            class="item"
            effect="dark"
            content="设置操作的结束颜色"
            placement="top-start"
            style="margin-left:10px;"
          >
            <i class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
      </el-form>
      <template #reference>
        <div>
          <div
            @mousedown="longOperationCanMove"
            :style="{
              userSelect: 'none',
              height: '40px',
              position: 'absolute',
              background: 'rgb(70, 70, 70)',
              cursor: 'move',
              width:
                ((myOperation.endTime - myOperation.startTime) /
                  displayAreaTime) *
                  (global.documentWidth - 300) +
                'px',
              left: '-1px',
              top: '1px',
              overflow: 'hidden',
              lineHeight: '40px',
              fontSize: '20px',
              border: '0px solid #fff',
              borderLeftWidth: '1px',
              borderRightWidth: '1px',
            }"
          >
            <div style="text-align:center;color:rgb(255,255,255)">
              从<span
                :style="{
                  color:
                    'rgb(' +
                    myOperation.startR +
                    ',' +
                    myOperation.startG +
                    ',' +
                    myOperation.startB +
                    ')',
                }"
                >█</span
              >到<span
                :style="{
                  color:
                    'rgb(' +
                    myOperation.endR +
                    ',' +
                    myOperation.endG +
                    ',' +
                    myOperation.endB +
                    ')',
                }"
                >█</span
              >
            </div>
          </div>
          <i
            @mousedown="leftMove = true"
            style="width:1px;height:40px;position:absolute;left:0px;top:0;cursor:w-resize;"
            src="http://pic.mcatk.com/charlot-pictures/EpheHitOperation.png"
          />
          <i
            @mousedown="rightMove = true"
            :style="{
              userSelect: 'none',
              height: '40px',
              width: '1px',
              position: 'absolute',
              cursor: 'e-resize',
              left:
                ((myOperation.endTime - myOperation.startTime) /
                  displayAreaTime) *
                  (global.documentWidth - 300) +
                1 +
                'px',
              top: '0px',
            }"
            src="http://pic.mcatk.com/charlot-pictures/EpheHitOperation.png"
          />
        </div>
      </template>
    </el-popover>
  </div>
</template>

<script>
export default {
  props: [
    "operation",
    "global",
    "track",
    "displayAreaTime",
    "currentNoteType",
    "enableEdit",
    "chart",
  ],
  data() {
    var checkStartTime = (rule, value, callback) => {
      if (value != 0 && !value) {
        rule;
        return callback(new Error("开始时机不能为空"));
      }
      if (parseFloat(value).toString() == "NaN") {
        callback(new Error("请输入数字值"));
      } else {
        if (value < 0) {
          callback(new Error("不能小于0"));
        } else if (value < this.myTrack.startTiming) {
          callback(new Error("不能小于轨道开始时机"));
        } else if (value > this.myTrack.endTime) {
          callback(new Error("不能大于轨道结束时机"));
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
          callback(new Error("不能小于0"));
        } else if (value < this.myTrack.startTiming) {
          callback(new Error("不能小于轨道开始时机"));
        } else if (value > this.myTrack.endTime) {
          callback(new Error("不能大于轨道结束时机"));
        } else {
          callback();
        }
      }
    };
    return {
      myOperation: this.operation,
      myTrack: this.track,
      myGlobal: this.global,
      canMove: false,
      leftMove: false,
      rightMove: false,
      passedTime: 0,
      edit: false,
      rules: {
        startTime: [
          { required: true, validator: checkStartTime, trigger: "blur" },
        ],
        endTime: [{ required: true, validator: checkEndTime, trigger: "blur" }],
        startColor: [{ required: true, message: "请选择颜色", rigger: "blur" }],
        endColor: [{ required: true, message: "请选择颜色", trigger: "blur" }],
      },
    };
  },
  created() {
    this.myOperation.tempOperation = JSON.parse(
      JSON.stringify(this.myOperation)
    );
    this.myOperation.tempOperation.startColor =
      "rgb(" +
      this.myOperation.tempOperation.startR +
      "," +
      this.myOperation.tempOperation.startG +
      "," +
      this.myOperation.tempOperation.startB +
      ")";
    this.myOperation.tempOperation.endColor =
      "rgb(" +
      this.myOperation.tempOperation.endR +
      "," +
      this.myOperation.tempOperation.endG +
      "," +
      this.myOperation.tempOperation.endB +
      ")";
  },
  watch: {
    "global.mouseUp"() {
      this.canMove = false;
      this.leftMove = false;
      this.rightMove = false;
    },
    "global.mouseMove"() {
      if (this.canMove) {
        if (
          this.global.currentTime > this.track.startTiming &&
          this.global.currentTime < this.track.endTiming
        ) {
          this.duration = this.operation.endTime - this.operation.startTime;

          this.myOperation.startTime = this.roundTime(
            this.global.currentTime - this.passedTime
          );

          this.myOperation.endTime = this.myOperation.startTime + this.duration;

          this.updateTemp();
        }
      } else if (this.leftMove) {
        var currentTime = this.global.currentTime;
        if (this.roundTime(currentTime) <= this.myOperation.endTime) {
          this.myOperation.startTime = this.roundTime(currentTime);
          this.updateTemp();
        }
      } else if (this.rightMove) {
        currentTime = this.global.currentTime;
        if (this.roundTime(currentTime) >= this.myOperation.startTime) {
          this.myOperation.endTime = this.roundTime(currentTime);
          this.updateTemp();
        }
      }
    },
  },
  computed: {
    left() {
      return (
        (this.myOperation.startTime / this.displayAreaTime) *
        (this.global.documentWidth - 300)
      );
    },
  },
  methods: {
    roundTime(timing) {
      if (this.global.beatLine) {
        var bpm = this.chart.BPM / 16;
        var mod = (timing - this.chart.firstBeatDelay) % bpm;
        if (mod > bpm / 2) {
          timing += bpm - mod;
        } else {
          timing -= mod;
        }
      }
      return Math.ceil(timing);
    },
    updateTemp() {
      this.myOperation.tempOperation = JSON.parse(
        JSON.stringify(this.myOperation)
      );
    },
    selfClicked() {
      if (this.currentNoteType == 3) this.deleteSelf();
      else if (this.enableEdit) this.startEdit();
    },
    updateTrack() {
      this.myGlobal.reCalculateTrack = !this.myGlobal.reCalculateTrack;
      this.myGlobal.reCalculateChartMaker = !this.myGlobal
        .reCalculateChartMaker;
    },
    longOperationCanMove() {
      setTimeout(() => {
        this.passedTime = Math.ceil(
          this.global.currentTime - this.operation.startTime
        );
      }, 10);
      this.canMove = true;
    },
    startEdit() {
      this.edit = true;
      this.myOperation.tempOperation = JSON.parse(
        JSON.stringify(this.myOperation)
      );
      this.myOperation.tempOperation.startColor =
        "rgb(" +
        this.myOperation.tempOperation.startR +
        "," +
        this.myOperation.tempOperation.startG +
        "," +
        this.myOperation.tempOperation.startB +
        ")";
      this.myOperation.tempOperation.endColor =
        "rgb(" +
        this.myOperation.tempOperation.endR +
        "," +
        this.myOperation.tempOperation.endG +
        "," +
        this.myOperation.tempOperation.endB +
        ")";
    },
    saveOperation() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          var rgb = this.myOperation.tempOperation.startColor
            .substring(4, this.myOperation.tempOperation.startColor.length - 1)
            .split(",");
          this.myOperation.tempOperation.startR = rgb[0];
          this.myOperation.tempOperation.startG = rgb[1];
          this.myOperation.tempOperation.startB = rgb[2];
          rgb = this.myOperation.tempOperation.endColor
            .substring(4, this.myOperation.tempOperation.endColor.length - 1)
            .split(",");
          this.myOperation.tempOperation.endR = rgb[0];
          this.myOperation.tempOperation.endG = rgb[1];
          this.myOperation.tempOperation.endB = rgb[2];
          for (var key in this.myOperation.tempOperation) {
            if (key != "tempOperation")
              this.myOperation[key] = this.myOperation.tempOperation[key];
          }
          this.edit = false;
          this.myOperation.tempOperation = {};
        } else {
          return false;
        }
      });
    },
    deleteOperation() {
      this.$confirm("您确定删除该操作?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.deleteSelf();
        this.$notify({
          title: "成功",
          message: "删除成功",
          type: "success",
        });
      });
    },
    deleteSelf() {
      this.myTrack.changeColorOperations.splice(this.myOperation.index, 1);
      this.updateTrack();
    },
  },
};
</script>

<style scope></style>
