<template>
  <div class="header">
    <div class="left-logo">
      EphemelodyOL
    </div>
    <div class="right">
      <Icon/>
    </div>
  </div>
</template>
<script>
import Icon from "./Icon"
export default {
  data() {
    return {};
  },
  components:{Icon},
  computed: {},
  methods: {},
  mounted() {},
};
</script>
<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80px;
  padding: 5px 20px;
}
.left-logo{
  font-size: 50px;
  font-weight: 300;
  color: #ffffff;
  text-shadow: 0 0 2px rgb(64, 64, 64);
}
</style>
