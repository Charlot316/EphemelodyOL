<template>
  <div class="icon-container" style="cursor:pointer;">
    <div>
      <img
        :src="user.playerIcon"
        class="img1"
        onerror="onerror=null;src='https://img0.baidu.com/it/u=3730772664,138405132&fm=26&fmt=auto'"
      />
    </div>
    <div class="icon-info">
      <div style="font-weight:600;">{{ user.ranking }} {{ user.player }}</div>

      <div style="font-weight:300;">
        <span>{{ Status }} {{ user.bestScore }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: ["user"],
  data() {
    return {
      myUser: this.user,
    };
  },

  created() {},
  computed: {
    Status() {
      if (this.user.recordStatus == 0) {
        return "D";
      } else if (this.user.recordStatus == 1) {
        return "C";
      } else if (this.user.recordStatus == 2) {
        return "F";
      } else if (this.user.recordStatus == 3) {
        return "P";
      }
      return "";
    },
    style() {
      if (this.user.recordStatus == 0) {
        return "color:red;";
      } else if (this.user.recordStatus == 1) {
        return "color:#FFA488;";
      } else if (this.user.recordStatus == 2) {
        return "color:blue;";
      } else if (this.user.recordStatus == 3) {
        return "color:gold;";
      }

      return "";
    },
  },
  methods: {},
  watch: {
    user() {
      this.myUser = this.user;
    },
  },
};
</script>

<style scoped>
.icon-container {
  display: flex;
  align-items: left;
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

.img1 {
  width: 35px;
  height: 35px;
  margin-left: 1%;
  background-size: cover;
  clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%);
  /* clip-path: polygon(50 0, 100% 50%, 50% 100%,0 50%); */
  transition: 1s clip-path;
}
.img1:hover {
  -webkit-clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
  clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
}
</style>
