<template>
  <div :class="currentClass" @click="edit=!edit">
    <div>
      <el-image
        style="width: 70px;height:70px;"
        :src="operation.background"
        fit="fit"
        class="image"
        :preview-src-list="[operation.background]"
      />
    </div>
    <div style=" display: flex;justify-content: space-between;">
      <h3>操作{{ operation.index + 1 }}</h3>
      <div></div>
    </div>
  </div>
</template>

<script>
export default {
  props: ["operation", "global"],
  data() {
    return {
      myOperation: this.operation,
      edit: true,
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
  display: flex;
  justify-content: space-between;
  border: none;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.edit {
  height: 170px;
  width: calc(100% - 30px);
  margin: 10px;
  padding: 5px;
  display: flex;
  justify-content: space-between;
  border: none;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.current-operation {
  background: white;
  color: black;
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
</style>
