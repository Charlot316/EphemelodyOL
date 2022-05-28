<template>
  <div class="menu-panel-container">
    <el-collapse>
      <el-collapse-item title=" 背景操作" name="1"> </el-collapse-item>
      <el-collapse-item title=" 当前轨道" name="2"> </el-collapse-item>
      <el-collapse-item title=" 全局插入音符" name="3"> </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
export default {};
</script>

<style scope>
.menu-panel-container {
  height: calc(100vh - 80px);
  width:90%;
  padding:0% 5%;
}
</style>
