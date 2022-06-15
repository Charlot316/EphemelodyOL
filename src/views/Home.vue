<template>
  <div>
    <BackgroundDisplay />
    <div class="home">
      <Header />
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
  created() {},
  components: {
    Header,
    BackgroundDisplay,
  },
  computed: {},
};
</script>

<style>
.leftDiv {
  /* border: 1px solid black; */
  width: 30%;
  height: 60%;
  float: left;
  margin-top: 5%;
  margin-left: 18%;
}
.rightDiv {
  /* border: 1px solid black; */
  width: 30%;
  height: 60%;
  float: right;
  margin-top: 5%;
  margin-right: 18%;
}
.img1 {
  width: 100%;
  height: 100%;
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
.divUser {
  border: 1px solid black;
  width: 30%;
}
.home {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
}

.div1 {
  width: 80%;
  height: 40%;
  /* margin-left: 50%; */
  margin-top: 5%;
  /* border: 2px solid black; */
}

.div_btn21 {
  width: 45%;
  height: 40%;
  float: left;
}

.div_btn22 {
  width: 45%;
  height: 40%;
  float: right;
}

.btn_2 {
  width: 100%;
  height: 100%;
  color: #fff;
  border-radius: 5px;
  padding: 10px 25px;
  font-family: "Lato", sans-serif;
  font-weight: 500;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: inline-block;
  box-shadow: inset 2px 2px 2px 0px rgba(255, 255, 255, 0.5),
    7px 7px 20px 0px rgba(0, 0, 0, 0.1), 4px 4px 5px 0px rgba(0, 0, 0, 0.1);
}

.btn {
  width: 80%;
  height: 20%;
  color: #fff;
  border-radius: 5px;
  padding: 10px 25px;
  font-family: "Lato", sans-serif;
  font-weight: 500;
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: inline-block;
  box-shadow: inset 2px 2px 2px 0px rgba(255, 255, 255, 0.5),
    7px 7px 20px 0px rgba(0, 0, 0, 0.1), 4px 4px 5px 0px rgba(0, 0, 0, 0.1);
}

.btn1 {
  /* margin-left: 50%;
  margin-top: 10%; */
  font-size: 40px;
  margin-top: 20%;
  background: rgb(247, 150, 192);
  background: radial-gradient(
    circle,
    rgba(247, 150, 192, 1) 0%,
    rgba(118, 174, 241, 1) 100%
  );
  border: none;
}

.btn1:hover {
  background: rgb(247, 150, 192);
  background: radial-gradient(
    circle,
    rgb(187, 81, 127) 0%,
    rgb(48, 81, 122) 100%
  );
  transform: scale(1.2);
}

.btn1:active {
  transform: scale(0.98);
  box-shadow: 3px 2px 22px 1px rgba(0, 0, 0, 0.24);
}

.btn2 {
  font-size: 20px;
  background: rgb(247, 150, 192);
  background: radial-gradient(
    circle,
    rgba(247, 150, 192, 1) 0%,
    rgba(118, 174, 241, 1) 100%
  );
  border: none;
}

.btn2:hover {
  background: rgb(247, 150, 192);
  background: radial-gradient(
    circle,
    rgb(187, 81, 127) 0%,
    rgb(48, 81, 122) 100%
  );
  transform: scale(1.2);
}

.btn2:active {
  transform: scale(0.98);
  box-shadow: 3px 2px 22px 1px rgba(0, 0, 0, 0.24);
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}
</style>
