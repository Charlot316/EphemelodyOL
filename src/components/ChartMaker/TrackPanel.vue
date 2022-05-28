<template>
  <div class="track-panel-container">
    <el-button type="text" class="return-button" @click="$emit('returnMenu')">返回</el-button>
    <el-collapse>
      <el-collapse-item title="位置操作" name="1"> </el-collapse-item>
      <el-collapse-item title="宽度操作" name="2"> </el-collapse-item>
      <el-collapse-item title="颜色操作" name="3"> </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>

export default {
  props:['Track','global'],
  data(){
    return{
      myTrack:this.Track,
      myGlobal:this.global,
    }
  }
};
</script>

<style scope>
.track-panel-container {
  height: calc(100vh - 80px);
  width: 90%;
  padding: 0% 5%;
}
.return-button{color:#303133}
.return-button:hover{color:#949494}
.return-button:active{color:#000000}
</style>
