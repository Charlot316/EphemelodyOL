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
              v-if="!edit"
              type="text"
              class="edit-button"
              icon="el-icon-s-tools"
              @click="edit = true"
            />
            <el-button
              v-if="edit"
              type="text"
              class="cancel-button"
              icon="el-icon-error"
              @click="edit = false"
            />
            <el-button
              v-if="edit"
              type="text"
              class="ok-button"
              icon="el-icon-success"
              @click="edit = false"
            />
            <el-button
              type="text"
              class="delete-button"
              icon="el-icon-remove"
            />
          </div>
        </div>
        <div style="width:100%;margin-top:10px;">
          <h4>时机 {{ myOperation.startTime }}</h4>
        </div>
      </div>
    </div>
    <div v-show="edit">
      ceshi
    </div>
  </div>
</template>

<script>
export default {
  props: ["operation", "global"],
  data() {
    return {
      myOperation: this.operation,
      edit: false,
    };
  },
  created() {
    console.log(this.operation);
  },
  computed: {
    currentClass() {
      var currentClass = "";
      if (this.edit) {
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
      } else if (this.global.currentTime < this.operation.startTime) {
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
  height: 170px;
  width: calc(100% - 30px);
  margin: 10px;
  padding: 5px;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.current-operation {
  background: white;
  color: #303133;
  -webkit-box-shadow: 0 0 9px 4px rgba(127, 127, 127, 0.5);
  box-shadow: 0 0 9px 4px rgba(127, 127, 127, 0.5);
  transition: 0.5s;
}
.passed-operation {
  background: #f3f1f1;
  color: rgb(110, 110, 110);
  -webkit-box-shadow: 0 0 0px 0px rgba(127, 127, 127, 0.5);
  box-shadow: 0 0 0px 0px rgba(127, 127, 127, 0.5);
  transition: 0.5s;
}
.to-come-operation {
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
