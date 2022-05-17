<template>
  <div class="about">
    <div class="content-box" :class="{ 'content-collapse': collapse }">
      <div class="content">
        <router-view v-slot="{ Component }">
          <transition name="move" mode="out-in">
            <keep-alive :include="tagsList">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
        <!-- <el-backtop target=".content"></el-backtop> -->
      </div>
    </div>
  </div>
</template>
<script>
export default {
  components: {
  },
  computed: {
    tagsList() {
      return this.$store.state.tagsList.map((item) => item.name);
    },
    collapse() {
      return this.$store.state.collapse;
    },
  },
};
</script>
