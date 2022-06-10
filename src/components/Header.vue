<template>
  <div class="header">
    <div class="left-logo">
      EphemelodyOL
    </div>
    <div class="right">
      <Icon :user="$store.state.user"/>

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
  background-color: #fff;
  border-bottom: 1px solid #e5e5e5;
}
</style>
