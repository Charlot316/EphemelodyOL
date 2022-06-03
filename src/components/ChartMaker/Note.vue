<template>
  <div
    @click="
      edit = true;
    "
    :style="{
      position: 'absolute',
      top: '20px',
      left: left - 20 + 'px',
      zIndex: zIndex,
    }"
  >
    <el-popover v-model:visible="edit" placement="top" width="400" trigger="manual">
      <el-button size="small" text @click="edit = false;">取消</el-button>
      <el-button size="small" type="primary" @click="edit = false"
        >确认</el-button
      >
      <template #reference>
        <div>
          <div v-if="note.noteType == 0">
            <el-image
              @dragstart.prevent
              @mousedown="
                canMove = true;
                zIndex = 10;
              "
              style="width:40px;height:40px;user-select:none;cursor: move;"
              src="http://pic.mcatk.com/charlot-pictures/EpheHitNote.png"
            />
          </div>
          <div v-if="note.noteType == 1">
            <div
              @mousedown="longNoteCanMove"
              :style="{
                userSelect: 'none',
                height: '38px',
                position: 'absolute',
                background: 'rgb(22, 22, 14)',
                cursor: 'move',
                width:
                  ((myNote.endTiming - myNote.timing) / this.displayAreaTime) *
                    (this.global.documentWidth - 300) +
                  'px',
                left: '20px',
                top: '1px',
              }"
            ></div>
            <el-image
              @dragstart.prevent
              @mousedown="
                leftMove = true;
                zIndex = 10;
              "
              style="width:40px;height:40px;position:absolute;left:0;top:0;user-select: none;cursor:w-resize;"
              src="http://pic.mcatk.com/charlot-pictures/EpheHitNote.png"
            />
            <el-image
              @dragstart.prevent
              @mousedown="
                rightMove = true;
                zIndex = 10;
              "
              :style="{
                userSelect: 'none',
                height: '40px',
                width: '40px',
                position: 'absolute',
                cursor: 'e-resize',
                left:
                  ((myNote.endTiming - myNote.timing) / this.displayAreaTime) *
                    (this.global.documentWidth - 300) +
                  'px',
                top: '0px',
              }"
              src="http://pic.mcatk.com/charlot-pictures/EpheHitNote.png"
            />
          </div>
          <div v-if="note.noteType == 2">
            <el-image
              @mousedown="
                canMove = true;
                zIndex = 10;
              "
              @dragstart.prevent
              style="width:40px;height:40px;cursor: move;"
              src="http://pic.mcatk.com/charlot-pictures/EpheSlideNote.png"
            />
          </div>
        </div>
      </template>
    </el-popover>
  </div>
</template>

<script>
export default {
  props: ["note", "global", "track", "displayAreaTime"],
  data() {
    return {
      myNote: this.note,
      myTrack: this.track,
      myGlobal: this.global,
      canMove: false,
      leftMove: false,
      rightMove: false,
      passedTime: 0,
      zIndex: 0,
      edit: false,
    };
  },
  watch: {
    "global.mouseUp"() {
      this.canMove = false;
      this.leftMove = false;
      this.rightMove = false;
      this.zIndex = 0;
    },
    "global.mouseMove"() {
      if (this.canMove) {
        if (
          this.global.currentTime > this.track.startTiming &&
          this.global.currentTime < this.track.endTiming
        ) {
          if (this.myNote.noteType == 1) {
            this.duration = this.note.endTiming - this.note.timing;
            this.myNote.timing = Math.ceil(
              this.global.currentTime - this.passedTime
            );
            this.myNote.endTiming = this.myNote.timing + this.duration;
            this.$forceUpdate();
          } else {
            this.myNote.timing = this.global.currentTime;
          }
          this.myTrack.notes.sort(function(a, b) {
            return a.timing - b.timing;
          });
        }
      } else if (this.leftMove) {
        if (
          this.global.currentTime > this.track.startTiming &&
          this.global.currentTime < this.myNote.endTiming - 150
        )
          this.myNote.timing = this.global.currentTime;
      } else if (this.rightMove) {
        if (
          this.global.currentTime > this.myNote.timing + 150 &&
          this.global.currentTime < this.track.endTiming
        )
          this.myNote.endTiming = this.global.currentTime;
      }
    },
  },
  computed: {
    left() {
      return (
        (this.myNote.timing / this.displayAreaTime) *
        (this.global.documentWidth - 300)
      );
    },
  },
  methods: {
    longNoteCanMove() {
      setTimeout(() => {
        this.passedTime = Math.ceil(this.global.currentTime - this.note.timing);
      }, 10);
      this.canMove = true;
      this.zIndex = 10;
    },
    breforeLeave() {
      console.log(this.edit);
    },
  },
};
</script>

<style scope></style>
