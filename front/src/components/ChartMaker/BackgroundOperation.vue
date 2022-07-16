<template>
  <div :class="currentClass">
    <div
      style="width:100%;display: flex;justify-content: space-between;border: none;"
    >
      <div>
        <el-image
          style="width: 70px;height:70px;border-radius: 5px;"
          :src="operation.background"
          fit="fit"
          class="image"
          :preview-src-list="[operation.background]"
        />
      </div>
      <div style="width:calc(100% - 80px);">
        <div
          style="width:100%;height:20px; display: flex;justify-content: space-between; align-items: center;line-height: 20px;"
        >
          <div style="font-weight:800">操作{{ operation.index + 1 }}</div>
          <div>
            <el-button
              v-if="!myOperation.edit"
              type="text"
              class="edit-button"
              icon="el-icon-s-tools"
              @click="startEdit"
            />
            <el-button
              v-if="myOperation.edit && !myOperation.isNew"
              type="text"
              class="cancel-button"
              icon="el-icon-error"
              @click="myOperation.edit = false"
            />
            <el-button
              v-if="myOperation.edit"
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
        </div>
        <div style="width:100%;margin-top:10px;">
          <h4>时机 {{ myOperation.startTime }}</h4>
        </div>
      </div>
    </div>
    <transition
      name="flip-list"
      enter-active-class="animate__animated animate__fadeInDown"
      leave-active-class="animate__animated animate__fadeOutUp"
    >
      <div v-show="myOperation.edit">
        <el-form
          :model="tempOperation"
          :rules="rules"
          ref="form"
          @submit.prevent="saveOperation"
        >
          <el-form-item label="时机" label-width="80px" prop="startTime">
            <el-input
              @keydown.enter="saveOperation"
              v-model="tempOperation.startTime"
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
          <el-form-item label="背景" label-width="80px" prop="background">
            <el-input
              @keydown.enter="saveOperation"
              v-model="tempOperation.background"
              style="width:100px"
            />
            <el-tooltip
              class="item"
              effect="dark"
              content="输入背景图片的url，您也可以点击下方按钮上传自己的图片至服务器"
              placement="top-start"
              style="margin-left:10px;"
            >
              <i class="el-icon-question" />
            </el-tooltip>
          </el-form-item>
          <el-form-item label="手动上传" label-width="80px">
            <el-upload
              class="upload-demo"
              action="http://47.113.89.104:8090/chart/uploadBackground"
              :with-credentials="true"
              name="background"
              :data="{
                songId: $route.query.songId,
                startTime: tempOperation.startTime,
              }" 
              :on-success="handleUploadSuccess"
              :disabled="tempOperation.startTime == ''"
            >
              <el-button
                size="small"
                :disabled="tempOperation.startTime == ''"
                >{{
                  tempOperation.startTime == "" ? "请先填写时机" : "点击上传"
                }}</el-button
              >
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  props: ["operation", "global", "chart"],
  data() {
    var checkStartTime = (rule, value, callback) => {
      if (!value) {
        rule;
        return callback(new Error("时机不能为空"));
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
    return {
      myOperation: this.operation,
      myGlobal: this.global,
      myChart: this.chart,
      tempOperation: {},
      form: {},
      rules: {
        startTime: [
          { required: true, validator: checkStartTime, trigger: "blur" },
        ],
        background: [{ required: true, trigger: "blur" }],
      },
    };
  },
  created() {
    this.myOperation.edit = false;
  },
  methods: {
    updateOperation() {
      this.myGlobal.reCalculateChartMaker = !this.myGlobal
        .reCalculateChartMaker;
    },
    startEdit() {
      this.myOperation.edit = true;
      this.tempOperation = JSON.parse(JSON.stringify(this.myOperation));
    },
    handleUploadSuccess(response) {
      this.tempOperation.background = response.data.background;
      this.$notify({
        title: "上传成功",
        message: "图片已上传至服务器",
        type: "success",
      });
      this.saveOperation();
    },
    saveOperation() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          setTimeout(() => {
            this.updateOperation();
          }, 500);
          this.$emit("editStatus", true);
          for (var key in this.tempOperation) {
            this.myOperation[key] = this.tempOperation[key];
          }
          this.myOperation.edit = false;
          this.myOperation.isNew = false;
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
        if (this.myOperation.isNew) {
          this.$emit("editStatus", true);
        }
        this.myChart.changeBackgroundOperations.splice(
          this.myOperation.index,
          1
        );
        this.updateOperation();
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
      if (this.myOperation.edit) {
        currentClass = "edit ";
      } else {
        currentClass = "not-edit ";
      }
      if (
        this.global.currentTime > this.operation.startTime &&
        this.global.currentTime < this.operation.endTime
      ) {
        currentClass += "current-operation";
      } else if (this.global.currentTime > this.operation.endTime) {
        currentClass += "passed-operation";
      } else {
        currentClass += "to-come-operation ";
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
  height: 270px;
  width: calc(100% - 30px);
  margin: 10px;
  padding: 5px;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.current-operation {
  background: rgb(47, 47, 47);
  color: rgb(171, 171, 171);
  box-shadow: 0 0 5px 2px rgba(255, 255, 255, 0.5);
  transition: 0.5s;
}

.current-operation .el-form-item__label {
  color: rgb(171, 171, 171);
  transition: 0.5s;
}

.passed-operation {
  background: rgb(30, 30, 30);
  color: rgb(100, 100, 100);
  box-shadow: 0 0 0px 0px rgba(127, 127, 127, 0.5);
  transition: 0.5s;
}

.passed-operation .el-form-item__label {
  color: rgb(171, 171, 171);
  transition: 0.5s;
}

.to-come-operation {
  background: #2f2f2f;
  color: rgb(171, 171, 171);
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
  transition: 0.5s;
}

.to-come-operation .el-form-item__label {
  color: rgb(171, 171, 171);
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
