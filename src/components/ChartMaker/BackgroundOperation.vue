<template>
  <div
    class="current-operation"
    style="height: 80px;width: calc(100% - 20px);margin:10px;"
  >
    <div>
      <el-image
        lazy
        style="width: 50px;height:50px;"
        :src="operation.background"
        fit="fit"
        class="image"
        :preview-src-list="[operation.background]"
      />
    </div>
    <div></div>
  </div>
</template>

<script>
export default {
  props: ["operation", "global"],
  data() {
    return {
      myOperation: this.operation,
    };
  },
};
</script>

<style scope>
.current-operation {
  border: none;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  background: white;
  -webkit-box-shadow: 0 0 5px 2px rgba(127, 127, 127, 0.5);
  box-shadow: 0 0 5px 2px rgba(127, 127, 127, 0.5);
}
.passed-operation {
  border: none;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  background: #bebebe;
  -webkit-box-shadow: 0 0 9px 4px rgba(127, 127, 127, 0.5);
  box-shadow: 0 0 9px 4px rgba(127, 127, 127, 0.5);
}
.to-come-operation {
  border: none;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  background: white;
  -webkit-box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
}
</style>
