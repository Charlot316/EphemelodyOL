<template>
  <div
    @click="selfClicked"
    :style="{
      position: 'absolute',
      top: '20px',
      left: left + 'px',
      zIndex: operation.zIndex,
    }"
    @mousedown="setZIndex"
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
        <el-form-item label="开始宽度" label-width="80px" prop="startWidth">
          <el-input
            @keydown.enter="saveOperation"
            v-model="myOperation.tempOperation.startWidth"
            style="width:130px"
          />
          <el-tooltip
            class="item"
            effect="dark"
            content="设置操作开始的宽度，请输入一个小数，代表轨道宽度占画面全宽的比例"
            placement="top-start"
            style="margin-left:10px;"
          >
            <i class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="结束宽度" label-width="80px" prop="endWidth">
          <el-input
            @keydown.enter="saveOperation"
            v-model="myOperation.tempOperation.endWidth"
            style="width:130px"
          />
          <el-tooltip
            class="item"
            effect="dark"
            content="设置操作结束的宽度，请输入一个小数，代表轨道宽度占画面全宽的比例"
            placement="top-start"
            style="margin-left:10px;"
          >
            <i class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
      </el-form>
      <template #reference>
        <div>
          <el-tooltip class="item" effect="dark" placement="top-start">
            <template #content>
              <div style="text-align:center">
                {{ myOperation.startTime + "→" + myOperation.endTime }}
                <br />
                {{ myOperation.startWidth + "→" + myOperation.endWidth }}
              </div>
            </template>
            <div>
              <div
                @mousedown="longOperationCanMove"
                :style="{
                  userSelect: 'none',
                  height: '40px',
                  position: 'absolute',
                  background: 'rgb(184, 223, 107)',
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
                  {{ myOperation.startWidth }}→{{ myOperation.endWidth }}
                </div>
              </div>
              <div
                @mousedown="leftMove = true"
                style="width:1px;height:40px;position:absolute;left:0px;top:0;cursor:w-resize;"
                src="http://pic.mcatk.com/charlot-pictures/EpheHitOperation.png"
              />
              <div
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
          </el-tooltip>
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
    var checkStartWidth = (rule, value, callback) => {
      if (!value) {
        rule;
        return callback(new Error("起始坐标不能为空"));
      }
      if (parseFloat(value).toString() == "NaN") {
        callback(new Error("请输入数字值"));
      } else {
        callback();
      }
    };
    var checkEndWidth = (rule, value, callback) => {
      if (!value) {
        rule;
        return callback(new Error("终止坐标不能为空"));
      }
      if (parseFloat(value).toString() == "NaN") {
        callback(new Error("请输入数字值"));
      } else {
        callback();
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
        startWidth: [
          { required: true, validator: checkStartWidth, trigger: "blur" },
        ],
        endWidth: [
          { required: true, validator: checkEndWidth, trigger: "blur" },
        ],
      },
    };
  },
  created() {
    this.myOperation.zIndex = 0;
    this.myOperation.tempOperation = JSON.parse(
      JSON.stringify(this.myOperation)
    );
    delete this.myOperation.tempOperation.tempOperation;
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
        var bpm = this.chart.bpm / 16;
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
      delete this.myOperation.tempOperation.tempOperation;
    },
    selfClicked() {
      if (this.currentNoteType == 3) this.deleteSelf();
      else if (this.enableEdit) this.startEdit();
    },
    setZIndex() {
      if (this.global.currentOperation) {
        this.myGlobal.currentOperation.zIndex = 0;
      }
      this.myGlobal.currentOperation = this.operation;
      this.myOperation.zIndex = 10;
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
      delete this.myOperation.tempOperation.tempOperation;
    },
    saveOperation() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          for (var key in this.myOperation.tempOperation) {
            if (key != "tempOperation")
              this.myOperation[key] = this.myOperation.tempOperation[key];
          }
          this.edit = false;
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
      this.myTrack.changeWidthOperations.splice(this.myOperation.index, 1);
      this.updateTrack();
    },
  },
};
</script>

<style scope></style>
