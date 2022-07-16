<template><div></div> </template>

<script>
const colorOpacity = 0.04;
export default {
  data() {
    return {
      myglobal: this.global,
      judgeSize: 300,
      judgeAnimationTime: 300,
      colorList: [
        "rgba(0,0,0," + colorOpacity + ")",
        "rgba(255,255,255," + colorOpacity + ")",
        "rgba(255,215,0," + colorOpacity + ")",
        "rgba(218,165,32," + colorOpacity + ")",
        "rgba(173,255,47," + colorOpacity + ")",
        "rgba(100,149,237," + colorOpacity + ")",
        "rgba(0,191,255," + colorOpacity + ")",
        "rgba(255,0,255," + colorOpacity + ")",
        "rgba(72,61,139," + colorOpacity + ")",
        "rgba(0,0,0," + colorOpacity + ")",
        "rgba(0,0,0," + colorOpacity + ")",
      ],
      flakes:[],
    };
  },
  props: ["Y", "middle", "global", "judge"],
  watch: {
    "global.currentTime"() {
      this.judgePainter(this.judge);
    },
  },
  created(){

      for(var i=0;i<4;i++)
       this.flakes.push({})
  },
  methods: {
    judgePainter(judge) {
      this.paintJudge(judge);
      //this.paintFlakes(judge);
    },
    paintJudge(judge) {
      var painter = this.global.judgePainter;
      var currentTime = this.global.currentTime;
      var size = 0;
      var width = 0;
    if(currentTime < judge.timing ) currentTime=judge.timing 
      if (currentTime < judge.timing + this.judgeAnimationTime * 0.75) {
        size =
          ((0.9 * this.judgeSize) / (this.judgeAnimationTime * 0.75)) *
          (currentTime - judge.timing);
        width = 40;
      } else if (currentTime < judge.timing + this.judgeAnimationTime) {
        var k = (0.4 * this.judgeSize) / this.judgeAnimationTime;
        var b = this.judgeSize - k * (judge.timing + this.judgeAnimationTime);
        size = k * currentTime + b;
        width =
          (40 / (this.judgeAnimationTime * 0.25)) *
          (judge.timing + this.judgeAnimationTime - currentTime);
      } else {
        size = 0;
        width = 0;
      }
      if (judge.type == "far") {
        size *= 0.6;
      }

      painter.beginPath();
      painter.moveTo(this.middle, this.Y - size);
      painter.lineTo(this.middle + size, this.Y);
      painter.lineTo(this.middle, this.Y + size);
      painter.lineTo(this.middle - size, this.Y);
      painter.lineTo(this.middle, this.Y - size);
      painter.closePath();
      var temp = 0;
      if (judge.type == "pure") {
        var gradient = painter.createLinearGradient(
          this.middle - size,
          this.Y - size,
          this.middle + size,
          this.Y + size
        );
        for (temp = 0; temp <= 10; temp++) {
          gradient.addColorStop("" + temp / 10, this.colorList[temp]);
        }
        painter.strokeStyle = gradient;
      } else if (judge.type == "far") {
        painter.strokeStyle = "rgba(100,149,237," + colorOpacity + ")";
      }
      painter.globalCompositeOperation = "lighter";
      for (temp = 2; temp < width; temp++) {
        painter.lineWidth = temp / 2;
        temp++;
        painter.stroke();
      }
      painter.beginPath();
      var scale = 1.5;
      painter.moveTo(this.middle, this.Y - scale * size);
      painter.lineTo(this.middle + scale * size, this.Y);
      painter.lineTo(this.middle, this.Y + scale * size);
      painter.lineTo(this.middle - scale * size, this.Y);
      painter.lineTo(this.middle, this.Y - scale * size);
      painter.closePath();
      painter.strokeStyle = "rgba(255,255,255," + 0.2 * colorOpacity + ")";
      for (temp = 2; temp < 3 * width; temp++) {
        painter.lineWidth = temp;
        painter.stroke();
        temp += 5;
      }
      painter.globalCompositeOperation = "source-over";
    },
    // paintFlakes(judge) {
    //   var painter = this.global.judgePainter;
    //   var size = 0;
    //   var width = 0;
    //   if (currentTime < judge.timing + this.judgeAnimationTime * 0.75) {
    //     size =
    //       ((0.9 * this.judgeSize) / (this.judgeAnimationTime * 0.75)) *
    //       (currentTime - judge.timing);
    //     width = 40;
    //   } else if (currentTime < judge.timing + this.judgeAnimationTime) {
    //     var k = (0.4 * this.judgeSize) / this.judgeAnimationTime;
    //     var b = this.judgeSize - k * (judge.timing + this.judgeAnimationTime);
    //     size = k * currentTime + b;
    //     width =
    //       (40 / (this.judgeAnimationTime * 0.25)) *
    //       (judge.timing + this.judgeAnimationTime - currentTime);
    //   } else {
    //     size = 0;
    //     width = 0;
    //   }
    //   if (judge.type == "far") {
    //     size *= 0.6;
    //   }
    //   if (judge == "pure") {
    //     Math.ceil(Math.random());
    //   } else {
    //   }
    // },
  },
};
</script>

<style scoped></style>
