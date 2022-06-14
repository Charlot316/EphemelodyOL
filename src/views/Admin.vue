<template>
        <div class="div1">
            <div class="divTop">
                <button class="btn btn1">新增</button>
                <!-- <input type="text"> -->
                <div style="float:right;width:50%;">
                    <el-row>
                        <el-col :span="6">
                            <el-select
                                v-model="params.searchType" 
                                placeholder="搜索类别"
                                size="medium"
                                class="handle-select mr10"
                                id="搜索类别"
                            >
                                <el-option
                                key="0"
                                label="歌曲名称"
                                value="0"
                                ></el-option>
                                <el-option
                                key="1"
                                label="歌手名称"
                                value="1"
                                ></el-option>
                                <el-option
                                key="2"
                                label="上传者名称"
                                value="2"
                                ></el-option>
                                <el-option
                                key="3"
                                label="难度"
                                value="3"
                                ></el-option>
                                <el-option
                                key="4"
                                label="热度"
                                value="4"
                                ></el-option>
                            </el-select>
                        </el-col>
                        <el-col :span="2"></el-col>
                        <el-col :span="12">
                            <el-input
                                placeholder="请输入关键词进行搜索"
                                v-model="params.searchContent"
                                size="medium"
                                @keyup.enter="getCharts()"
                            >
                                <template #append>
                                <el-button
                                    icon="el-icon-search"
                                    @click="getCharts()"
                                    type="primary"
                                >
                                </el-button>
                                </template>
                            </el-input>
                        </el-col>
                        <el-col :span="1"></el-col>
                        <el-col :span="3">
                            <el-select
                                v-model="params.sortWay" 
                                placeholder="升"
                                size="medium"
                                class="handle-select mr10"
                                id="升/降"
                            >
                            <el-option
                                key="0"
                                label="升"
                                value="0"
                            ></el-option>
                            <el-option
                                key="0"
                                label="降"
                                value="1"
                            ></el-option>
                            </el-select>
                        </el-col>
                    </el-row>
                </div>

            </div>
            <!-- <div class="div2"> -->
                <div class="div3">
                    <div class="div4" v-for="item in songs" :key="item">
                        <div class="div5">
                            <el-image :src="item.songCover" class="img1"></el-image>
                        </div>
                        <div class="div6">
                            <div class="div7">{{item.songName}}</div>
                            <div class="div8">
                            <span class="icon-active" icon="el-icon-link" style="margin-left: 10px;"><i class="el-icon-setting"></i></span>
                            <span class="icon-active" icon="el-icon-link" style="margin-left: 10px;"><i class="el-icon-delete"></i></span>
                            </div>
                            <div class="div9">
                                创作者：{{item.songWriter}}
                            </div>
                        </div>
                    </div>
                </div>
            <!-- </div> -->
        </div>

  
 
</template>
 
<script>
export default {
    data() {
        return {
            type: "",
            params: {
                status:"0",
                searchType:"",
                searchContent:"",
                sortType:"",
                sortWay:""
            },
            songs:[],
        };
    },
    mounted() {
    },
    created() {
        this.getMyAllCharts();
    },
    methods: {
        async getMyAllCharts(){
            const{data: res} = await this.$http.post(
                '/user/getAllMyCharts'
            );
            if(res.code == 0){
                this.songs = res.data.charts
            }
            else{
                this.$message.error("获取异常")
            }
        },
        async getCharts(){
            this.params.sortType = this.params.searchType
            const{data: res} = await this.$http.post(
                '/user/getMyCharts',this.params
            );
            if(res.code == 0){
                this.songs = res.data.charts
            }
            else{
                this.$message.error("获取异常")
            }
        }
        
    }
};
</script>
 
<style>
    .divTop{
        /* border:1px solid black; */
        width: 70%;
        height: 5%;
        margin-left: 15%;
        margin-bottom: 0.5%;
    }
    .btn {
        /* width: 30%;
        height: 20%; */
        color: #fff;
        border-radius: 5px;
        padding: 10px 25px;
        font-family: "Lato", sans-serif;
        font-weight: 50;
        background: transparent;
        cursor: pointer;
        transition: all 0.3s ease;
        position: relative;
        display: inline-block;
        box-shadow: inset 2px 2px 2px 0px rgba(255, 255, 255, 0.5),
            7px 7px 20px 0px rgba(0, 0, 0, 0.1), 4px 4px 5px 0px rgba(0, 0, 0, 0.1);
    }

    /* 1 */
    .btn1 {
        background: rgb(44, 202, 75);
        background: linear-gradient(
            0deg,
            rgb(58, 207, 73) 0%,
            rgb(31, 162, 75) 100%
        );
        border: none;
    } 
    .btn1:active {
        transform: scale(0.98);
        box-shadow: 3px 2px 22px 1px rgba(0, 0, 0, 0.24);
    } 
  .img1{
      height: 100%;
      width: 100%;
      object-fit: cover;
      cursor: pointer;
  }
  .div1{
      width: 100%;
      height: 100%;
      background-color: #eee;
      font-size: 14px;
      padding: 20px 0;
      overflow:auto;
      background-image: url(../assets/img/login.jpg);
  }
  .div2{
      display: flex;
      width: 70%;
      margin: 0 auto;
      background-color: #FFFFFF;
      border-radius: 5px;
      height: 100%;
      overflow:auto;
  }
  .div3{
      flex: 1;
      /* border-right: 1px solid #cccccc; */
      width: 70%;
      margin: 0 auto;
      background-color: #FFFFFF;
      border-radius: 5px;
      /* height: 100%; */
      /* overflow:auto; */
  }
  .div4{
      height: 170px;
      display: flex;
      margin: 10px 20px;
      border-bottom: 1px solid #cccccc;
  }
  /* .div4:active{
      transform: scale(0.98);
  } */
  .div5{
      margin: 20px 0;
      width: 200px;
  }
  .div6{
      margin: 30px 20px;
  }
  .div7{
      font-size: 18px;
      color: #000000;
      cursor: pointer;
  }
  .div8{
      display: flex;
      color: #cccccc;
      line-height: 40px;
  }
  .div9{
      overflow: hidden;
      text-overflow: ellipsis;
      line-height: 20px;
      color: gray;
  }
  /* .icon-active{
      size: 20px;
  }
  .icon-active:hover{
    color: #585858;
  } */
  .item img {
    width: 100%;
    height: auto;
    transform: scale(1);
    transition: transform 1s ease 0s;
  }
 
  .item:hover img{
    transform: scale(1.1);
  }
  .face-pic:hover img{
    transform:rotate(360deg);
    -ms-transform:rotate(360deg); /* Internet Explorer */
    -moz-transform:rotate(360deg); /* Firefox */
    -webkit-transform:rotate(360deg); /* Safari 和 Chrome */
    -o-transform:rotate(360deg); /* Opera */
    transition: transform 0.6s ease 0s;
  }
</style>