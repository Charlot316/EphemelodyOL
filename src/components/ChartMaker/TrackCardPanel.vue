<template>
  <div :class="track.edit?'panel-edit':'panel-no-edit'">
    <div style="background:rgb(255,255,255);height:80px;"></div>
  </div>
</template>

<script>
export default {
props: ["track", "global", "chart"],

};
</script>

<style scope>
.panel-no-edit {
  height: 80px;
  padding-top: 5px;
  padding-bottom: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
.panel-edit {
  height: 515px;
  padding-top: 5px;
  padding-bottom: 5px;
  border-radius: 5px;
  transition: 0.5s;
}
</style>
