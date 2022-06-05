<template>
  <div
    @click="selfClicked"
    :style="{
      position: 'absolute',
      top: '20px',
      left: left - 20 + 'px',
      zIndex: zIndex,
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
        <el-form-item label="音符类别" label-width="80px" prop="operationType">
          <el-radio-group
            v-model="myOperation.tempOperation.operationType"
            size="small"
            style="width:130px;line-height: 20px;"
          >
            <el-radio :label="0">短键</el-radio>
            <el-radio :label="1">长键</el-radio>
            <el-radio :label="2">滑键</el-radio>
          </el-radio-group>
          <el-tooltip
            class="item"
            effect="dark"
            content="设置音符的类别"
            placement="top-start"
            style="margin-left:10px;"
          >
            <i class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="按键" label-width="80px" prop="key">
          <el-input
            :disabled="track.type == 1"
            @keydown.enter="saveOperation"
            v-model="myOperation.tempOperation.key"
            style="width:130px"
          />
          <el-tooltip
            class="item"
            effect="dark"
            content="设置音符的按键，当轨道是实轨时，音符按键必须等于实轨按键，虚轨的音符需要每一个设置自己的按键。请输入单个字母，不区分大小写"
            placement="top-start"
            style="margin-left:10px;"
          >
            <i class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
        <el-form-item label="时机" label-width="80px" prop="timing">
          <el-input
            @keydown.enter="saveOperation"
            v-model="myOperation.tempOperation.timing"
            style="width:130px"
          />
          <el-tooltip
            class="item"
            effect="dark"
            content="设置音符的时机"
            placement="top-start"
            style="margin-left:10px;"
          >
            <i class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
        <el-form-item
          label="结束时机"
          label-width="80px"
          prop="endTiming"
          v-if="myOperation.tempOperation.operationType == 1"
        >
          <el-input
            @keydown.enter="saveOperation"
            v-model="myOperation.tempOperation.endTiming"
            style="width:130px"
          />
          <el-tooltip
            class="item"
            effect="dark"
            content="设置长键的结束时机"
            placement="top-start"
            style="margin-left:10px;"
          >
            <i class="el-icon-question" />
          </el-tooltip>
        </el-form-item>
      </el-form>
      <template #reference>
        <div>
          <div v-if="operation.operationType == 0">
            <el-image
              @dragstart.prevent
              @mousedown="
                canMove = true;
                zIndex = 10;
              "
              style="width:40px;height:40px;user-select:none;cursor: move;"
              src="http://pic.mcatk.com/charlot-pictures/EpheHitOperation.png"
            />
          </div>
          <div v-if="operation.operationType == 1">
            <div
              @mousedown="longOperationCanMove"
              :style="{
                userSelect: 'none',
                height: '38px',
                position: 'absolute',
                background: 'rgb(22, 22, 14)',
                cursor: 'move',
                width:
                  ((myOperation.endTiming - myOperation.timing) / this.displayAreaTime) *
                    (this.global.documentWidth - 300) +
                  'px',
                left: '20px',
                top: '1px',
              }"
            ></div>
            <el-image
              @dragstart.prevent
              @mousedown="
                leftMove = true;
                zIndex = 10;
              "
              style="width:40px;height:40px;position:absolute;left:0;top:0;user-select: none;cursor:w-resize;"
              src="http://pic.mcatk.com/charlot-pictures/EpheHitOperation.png"
            />
            <el-image
              @dragstart.prevent
              @mousedown="
                rightMove = true;
                zIndex = 10;
              "
              :style="{
                userSelect: 'none',
                height: '40px',
                width: '40px',
                position: 'absolute',
                cursor: 'e-resize',
                left:
                  ((myOperation.endTiming - myOperation.timing) / this.displayAreaTime) *
                    (this.global.documentWidth - 300) +
                  'px',
                top: '0px',
              }"
              src="http://pic.mcatk.com/charlot-pictures/EpheHitOperation.png"
            />
          </div>
          <div v-if="operation.operationType == 2">
            <el-image
              @mousedown="
                canMove = true;
                zIndex = 10;
              "
              @dragstart.prevent
              style="width:40px;height:40px;cursor: move;"
              src="http://pic.mcatk.com/charlot-pictures/EpheSlideOperation.png"
            />
          </div>
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
    "currentOperationType",
    "enableEdit",
    "chart",
  ],
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
          callback(new Error("不能小于0"));
        } else if (value < this.myTrack.startTiming) {
          callback(new Error("不能小于轨道开始时机"));
        } else if (value > this.myTrack.endTiming) {
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
        } else if (value > this.myTrack.endTiming) {
          callback(new Error("不能大于轨道结束时机"));
        } else if (
          parseInt(value) <
          parseInt(this.myOperation.tempOperation.timing) + 100
        ) {
          callback(new Error("长键长度不得小于100"));
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
      zIndex: 0,
      edit: false,
      rules: {
        type: [{ required: true, message: "请选择音符类别", trigger: "blur" }],
        key: [{ required: true, validator: checkKey, trigger: "blur" }],
        timing: [
          { required: true, validator: checkStartTime, trigger: "blur" },
        ],
        endTiming: [
          { required: true, validator: checkEndTime, trigger: "blur" },
        ],
      },
    };
  },
  created() {
    if (this.myOperation.operationType != 1) {
      this.myOperation.endTiming = parseInt(this.myOperation.timing) + 150;
    }

    this.myOperation.tempOperation = JSON.parse(JSON.stringify(this.myOperation));
  },
  watch: {
    "global.mouseUp"() {
      this.canMove = false;
      this.leftMove = false;
      this.rightMove = false;
      this.zIndex = 0;
    },
    "global.mouseMove"() {
      if (this.canMove) {
        if (
          this.global.currentTime > this.track.startTiming &&
          this.global.currentTime < this.track.endTiming
        ) {
          if (this.myOperation.operationType == 1) {
            this.duration = this.operation.endTiming - this.operation.timing;

            this.myOperation.timing = this.roundTime(
              this.global.currentTime - this.passedTime
            );
          } else {
            this.myOperation.timing = this.roundTime(this.global.currentTime);
          }

          if (this.myOperation.operationType != 1) {
            this.myOperation.endTiming = parseInt(this.myOperation.timing) + 150;
          } else {
            this.myOperation.endTiming = this.myOperation.timing + this.duration;
          }
          this.updateTemp();
        }
      } else if (this.leftMove) {
        if (
          this.global.currentTime > this.track.startTiming &&
          this.global.currentTime < this.myOperation.endTiming - 150
        ) {
          this.myOperation.timing = this.roundTime(this.global.currentTime);

          this.updateTemp();
        }
      } else if (this.rightMove) {
        if (
          this.global.currentTime > this.myOperation.timing + 150 &&
          this.global.currentTime < this.track.endTiming
        ) {
          this.myOperation.endTiming = this.roundTime(this.global.currentTime);
          this.updateTemp();
        }
      }
    },
  },
  computed: {
    left() {
      return (
        (this.myOperation.timing / this.displayAreaTime) *
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
      this.myOperation.tempOperation = JSON.parse(JSON.stringify(this.myOperation));
      this.myOperation.tempOperation.key = this.myOperation.tempOperation.key.toUpperCase();
    },
    selfClicked() {
      if (this.currentOperationType == 3) this.deleteSelf();
      else if (this.enableEdit) this.startEdit();
    },
    updateTrack() {
      this.myGlobal.reCalculateTrack = !this.myGlobal.reCalculateTrack;
      this.myGlobal.reCalculateChartMaker = !this.myGlobal
        .reCalculateChartMaker;
    },
    longOperationCanMove() {
      setTimeout(() => {
        this.passedTime = Math.ceil(this.global.currentTime - this.operation.timing);
      }, 10);
      this.canMove = true;
      this.zIndex = 10;
    },
    startEdit() {
      this.edit = true;
      this.myOperation.tempOperation = JSON.parse(JSON.stringify(this.myOperation));
      this.myOperation.tempOperation.key = this.myOperation.tempOperation.key.toUpperCase();
      if (this.myOperation.operationType != 1) {
        this.myOperation.endTiming = parseInt(this.myOperation.timing) + 150;
      }
    },
    saveOperation() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          for (var key in this.myOperation.tempOperation) {
            if (key != "tempOperation") this.myOperation[key] = this.myOperation.tempOperation[key];
          }
          this.myOperation.key = this.myOperation.key.toUpperCase();
          this.edit = false;
          this.myOperation.tempOperation = {};
        } else {
          return false;
        }
      });
    },
    deleteOperation() {
      this.$confirm("您确定删除该音符?", "提示", {
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
      this.myTrack.operations.splice(this.myOperation.index, 1);
      this.updateTrack();
    },
  },
};
</script>

<style scope></style>
