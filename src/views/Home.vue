<template>
  <div>
    <BackgroundDisplay />
    <div class="home">
      <Header />
      <div class="home-container">
        <span class="home-button" style="top:20%;" @click="$router.push({path:'/public'})">开始游戏</span>
        <span class="home-button" style="top:40%;" @click="$router.push({path:'/society'})">社区谱面</span>
        <span class="home-button" style="top:60%;" @click="$router.push({path:'/admin'})">管理谱面</span>
      </div>
    </div>
  </div>
</template>

<script>
import Header from "../components/Header";
import BackgroundDisplay from "@/components/BackgroundDisplay";
export default {
  data() {
    return {
      param: {
        newPassword: "",
        oldPassword: "",
      },
      editVisible_changepassword: false,
      editVisible_uploadIcon: false,
      user: {
        iconUrl: "",
        password: "",
        username: "",
        userId: "",
      },
      imgUrl: "",
    };
  },
  created() {

  },
  components: {
    Header,
    BackgroundDisplay,
  },
  computed: {},
};
</script>

<style scoped>
.home {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
}
.home-container {
  position: absolute;
  top: 100px;
  left: 0;
  width: 100vw;
  height: calc(100% - 100px);
}
.home-button {
  cursor: pointer;
  position: absolute;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 60px;
  text-align: center;
  line-height: 60px;
  color: #fff;
  font-size: 24px;
  text-transform: uppercase;
  text-decoration: none;
  font-family: sans-serif;
  box-sizing: border-box;
  background: linear-gradient(90deg, #03a9f4, #f441a5, #ffeb3b, #03a9f4);
  background-size: 400%;
  border-radius: 5px;
  z-index: 1;
}
.home-button:hover {
  animation: animateButton 8s linear infinite;
}
.home-button:before {
  content: "";
  position: absolute;
  top: -5px;
  left: -5px;
  right: -5px;
  bottom: -5px;
  z-index: -1;
  background: linear-gradient(90deg, #03a9f4, #f441a5, #ffeb3b, #03a9f4);
  background-size: 400%;
    border-radius: 5px;
  filter: blur(20px);
  opacity: 0;
  transition: 0.5s;
}
.home-button:hover:before {
  filter: blur(20px);
  opacity: 1;
  animation: animateButton 8s linear infinite;
}
@keyframes animateButton {
  0% {
    background-position: 0%;
  }
  100% {
    background-position: 400%;
  }
}
</style>
