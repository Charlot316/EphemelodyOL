<template>
  <router-link  :to="{ path: '/home' }">
    <div class="icon-container" style="cursor:pointer;">
      <div class="icon-info">
        <div class="user-name">{{ $store.state.user.username }}</div>
        <div class="potential">{{ potential }}</div>
      </div>
      <div>
        <img
        class="img1"
          :src="$store.state.user.icon ? $store.state.user.icon : require('@/assets/img/user.jpg')"
          onerror="onerror=null;src='https://img0.baidu.com/it/u=3730772664,138405132&fm=26&fmt=auto'"
        />
      </div>
    </div>
  </router-link>
</template>

<script>
export default {
  props: ["plus", "minus"],
  data() {
    return {
    };
  },
computed:{
  potential(){
    return parseFloat(this.$store.state.user.potential).toFixed(2);
  }
},
  created() {
  },
  methods: {},
  watch: {
  },
};
</script>

<style scoped>
.icon-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-width: 200px;
  max-width: 300px;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}
.icon-info {
  margin-left: 10px;
}
.user-name {
  text-align: left;
  font-size: 18px;
  max-width: 200px;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgb(4, 15, 47);
}
.info {
  font-size: 15px;
  color: #696969;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-name{
  font-size: 30px;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 0 2px rgb(64, 64, 64);
}
.potential{
  font-size: 20px;
  text-align: center;
  color: #ffffff;
  text-shadow: 0 0 2px rgb(64, 64, 64);
  background:rgba(180,238,180,0.5)
}
.img1{
  width:70px;
  height:70px;
  object-fit:fill;
  clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%);
  transition: 1s clip-path;
}

.img1:hover {
  -webkit-clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
  clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
}
</style>
