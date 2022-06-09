<template>
  <div class="header">
    <div class="left-logo">
      EphemelodyOL
    </div>
    <div class="right-buttons">
      <div>{{ $store.state.username }}</div>
      <img
        style="border-radius: 50%;"
        :src="
          $store.state.islogin
            ? $store.state.icon
            : require('@/assets/img/user.jpg')
        "
      />
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {};
  },
  computed: {},
  methods: {},
  mounted() {},
};
</script>
<style scoped>
.header {
  z-index:100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px;
  background-color: #fff;
  border-bottom: 1px solid #e5e5e5;
}
</style>
