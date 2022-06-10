<template>
  <div
    v-if="display"
    @click="selfClicked"
    :style="{
      position: 'absolute',
      left: 0,
      width:
        (chart.songLength / displayAreaTime) * (global.documentWidth - 300) +
        'px',
      height: '30px',
      zIndex: 10,
      overflow: 'hidden',
    }"
  >
    <div v-for="count in LineCount" :key="count">
      <div
        v-if="(count - 1) % 16 == 0 && (count - 1) * singleWidth + left > 0"
        :style="{
          position: 'absolute',
          top: 0,
          left: (count - 1) * singleWidth + left + 'px',
          width: '1px',
          height: '30px',
          background: 'rgb(255,255,255)',
        }"
      ></div>
      <div
        v-else-if="
          (count - 1) % 8 == 0 &&
            display4 &&
            (count - 1) * singleWidth + left > 0
        "
        :style="{
          position: 'absolute',
          top: 0,
          left: (count - 1) * singleWidth + left + 'px',
          width: '1px',
          height: '20px',
          background: 'rgb(255,255,255)',
        }"
      ></div>
      <div
        v-else-if="
          (count - 1) % 4 == 0 &&
            display8 &&
            (count - 1) * singleWidth + left > 0
        "
        :style="{
          position: 'absolute',
          top: 0,
          left: (count - 1) * singleWidth + left + 'px',
          width: '1px',
          height: '12px',
          background: 'rgb(255,255,255)',
        }"
      ></div>
      <div
        v-else-if="display16 && (count - 1) * singleWidth + left > 0"
        :style="{
          position: 'absolute',
          top: 0,
          left: (count - 1) * singleWidth + left + 'px',
          width: '1px',
          height: '8px',
          background: 'rgb(255,255,255)',
        }"
      ></div>
    </div>
  </div>
</template>

<script>
export default {
  props: ["global", "chart", "displayAreaTime"],
  data() {
    return {
      display: false,
      display4: false,
      display8: false,
      display16: false,
    };
  },
  created() {
    var bpm16 = this.chart.bpm / 16;
    this.LineCount = Math.ceil(this.chart.songLength / bpm16) + 16;
    this.setDisplay();
  },
  computed() {},
  watch: {
    displayAreaTime() {
      this.setDisplay();
      if (this.rightScrollElement == null) {
        this.rightScrollElement = document.getElementById(
          "footer-right-scroll"
        );
      }
      var scrollLeft =
        (this.global.currentTime / this.displayAreaTime) *
          (this.global.documentWidth - 300) -
        (this.global.documentWidth - 300) / 2;
      if (scrollLeft < 0) scrollLeft = 0;
      this.rightScrollElement.scrollLeft = scrollLeft;
      this.scrollLeft = this.rightScrollElement.scrollLeft;
    },
    "chart.bpm"() {
      this.setDisplay();
    },
    "chart.firstBeatDelay"() {
      this.setDisplay();
    },
  },
  methods: {
    setDisplay() {
      var bpm16 = this.chart.bpm / 16;
      var bpm8 = this.chart.bpm / 8;
      var bpm4 = this.chart.bpm / 4;
      var bpm = this.chart.bpm / 1;
      var wholeLength = this.global.documentWidth - 300;
      if ((wholeLength * bpm) / this.displayAreaTime > 100) {
        this.display = true;
        var delay = this.chart.firstBeatDelay;
        delay %= bpm;
        this.left = (delay / this.displayAreaTime) * wholeLength;
        this.left -= (bpm / this.displayAreaTime) * wholeLength;
        this.singleWidth = (wholeLength * bpm16) / this.displayAreaTime;

        if ((wholeLength * bpm4) / this.displayAreaTime > 25) {
          this.display4 = true;
          if ((wholeLength * bpm8) / this.displayAreaTime > 20) {
            this.display8 = true;
            if ((wholeLength * bpm16) / this.displayAreaTime > 15) {
              this.display16 = true;
            } else {
              this.display16 = false;
            }
          } else {
            this.display8 = false;
            this.display16 = false;
          }
        } else {
          this.display4 = false;
          this.display8 = false;
          this.display16 = false;
        }
      } else {
        this.display = false;
        this.display4 = false;
        this.display8 = false;
        this.display16 = false;
      }
    },
  },
};
</script>

<style scope></style>
