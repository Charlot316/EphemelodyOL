<template>
  <div class="footer-container">
    <div class="footer-header"></div>
    <div class="footer-left">
      <div class="footer-track-container"></div>
    </div>
    <div class="footer-right"></div>
  </div>
</template>

<script>
export default {};
</script>

<style scope>
.footer-container {
  height: 100%;
  width: 100vw;
  position:relative;
}
.footer-header {
  height: 50px;
  width: 100vw;
  position: absolute;
  top: 0px;
  left: 0px;
}
.footer-left {
  height: calc(100% - 50px);
  width: 300px;
  position: absolute;
  top: 50px;
  left: 0px;
}
.footer-track-container {
  width: calc(90% - 1px);
  height: 100%;
  padding: 0% 5%;
  border: 0px solid rgba(255, 255, 255, 0.2);
  background: rgb(32, 32, 32);
  border-right-width: 1px;
}
.footer-right {
  height: calc(100% - 50px);
  width: calc(100vw - 300px);
  background: rgb(32, 32, 32);
  position: absolute;
  top: 50px;
  left: 300px;
}
</style>
