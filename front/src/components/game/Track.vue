<template>
  <div>
    <div v-for="(judge, index) in myTrack.judges" :key="index">
      <judge-painter :middle="middle" :Y="Y" :global="global" :judge="judge" />
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, ref, reactive, computed, watch, onMounted } from 'vue';
import JudgePainter from "./JudgePainter.vue";

const props = defineProps({
  Track: Object,
  global: Object
});

const emit = defineEmits(["addCount"]);

const myTrack = props.Track;
const myglobal = props.global;

const lengthForBlackPoint = 10;
const widthPath = ref([]);
const positionXPath = ref([]);
const RGBPath = ref([]);
const widthIndex = ref(0);
const positionXIndex = ref(0);
const RGBIndex = ref(0);
const opacity = 0.3;
const animationTime = 100;
const height = ref(0);
const top = ref(0);
const blackLength = 28;
const pinkLength = 20;
const whiteLength = 15;
const mirrorOpacity = 0.1;
const isJudgingHold = ref(false);
const tempJudge = ref({});
const judgeAnimationTime = 175; // Added missing constant from logic

const isActive = computed(() => {
  if (myTrack.type == 1) {
    const currentTime = myglobal.currentTime;
    const keyPressTime = myglobal.keyPressTime[myTrack.key.toUpperCase()];
    const isHolding = myglobal.keyIsHold[myTrack.key.toUpperCase()];
    return (
      isHolding ||
      (currentTime - keyPressTime > 0 && currentTime - keyPressTime < 175)
    );
  } else return false;
});

const lengthForKey = computed(() => {
  if (myglobal.screenHeight * 0.1 > 30) {
    return 30;
  } else {
    return myglobal.screenHeight * 0.1;
  }
});

const finalHeight = computed(() => {
  return myglobal.screenHeight * myglobal.finalY;
});

const width = computed(() => {
  return 2 * myTrack.tempWidth * myglobal.screenWidth;
});

const halfWidth = computed(() => {
  return myTrack.tempWidth * myglobal.screenWidth;
});

const left = computed(() => {
  return (
    (myTrack.tempPositionX - myTrack.tempWidth) *
    myglobal.screenWidth
  );
});

const middle = computed(() => {
  return myTrack.tempPositionX * myglobal.screenWidth;
});

const Y = computed(() => {
  return myglobal.finalY * myglobal.screenHeight;
});

// Binary Search
const binaryGetCurrentIndex = (currentTime, path) => {
  let right = path.length - 1;
  if (right <= 0) return 0;
  let left = 0;
  let mid = Math.floor((right + left) / 2);
  while (right > left) {
    if (
      path[mid].startTime <= currentTime &&
      path[mid].endTime >= currentTime
    ) {
      return mid;
    }
    if (path[mid].startTime > currentTime) {
      right = mid - 1;
    } else {
      left = mid + 1;
    }
    mid = Math.floor((right + left) / 2);
  }
  return mid;
};

const getPositionX = () => {
  const currentTime = myglobal.currentTime;
  if (positionXPath.value.length === 0) return myTrack.positionX || 0;
  
  let currentX = positionXPath.value[positionXIndex.value];
  if (!currentX || !(currentTime <= currentX.endTime && currentTime >= currentX.startTime)) {
    positionXIndex.value = binaryGetCurrentIndex(currentTime, positionXPath.value);
    currentX = positionXPath.value[positionXIndex.value];
  }
  if (!currentX) return 0;
  if (currentX.type == 0) return currentX.positionX;
  if (currentX.type == 1) return currentX.k * currentTime + currentX.b;
  return 0;
};

const getWidth = () => {
  const currentTime = myglobal.currentTime;
  if (widthPath.value.length === 0) return myTrack.width || 0;

  let currentWidth = widthPath.value[widthIndex.value];
  if (!currentWidth || !(currentTime <= currentWidth.endTime && currentTime >= currentWidth.startTime)) {
    widthIndex.value = binaryGetCurrentIndex(currentTime, widthPath.value);
    currentWidth = widthPath.value[widthIndex.value];
  }
  if (!currentWidth) return 0;
  if (currentWidth.type == 0) return currentWidth.width;
  if (currentWidth.type == 1) return currentWidth.k * currentTime + currentWidth.b;
  return 0;
};

const getRGB = () => {
  const currentTime = myglobal.currentTime;
  if (RGBPath.value.length === 0) return [myTrack.R || 0, myTrack.G || 0, myTrack.B || 0];

  let currentRGB = RGBPath.value[RGBIndex.value];
  if (!currentRGB || !(currentTime <= currentRGB.endTime && currentTime >= currentRGB.startTime)) {
    RGBIndex.value = binaryGetCurrentIndex(currentTime, RGBPath.value);
    currentRGB = RGBPath.value[RGBIndex.value];
  }
  if (!currentRGB) return [0, 0, 0];
  if (currentRGB.type == 0) return [currentRGB.R, currentRGB.G, currentRGB.B];
  if (currentRGB.type == 1) {
    return [
      currentRGB.Rk * currentTime + currentRGB.Rb,
      currentRGB.Gk * currentTime + currentRGB.Gb,
      currentRGB.Bk * currentTime + currentRGB.Bb,
    ];
  }
  return [0, 0, 0];
};

const setHeightAndTop = () => {
  let k = myglobal.finalY / animationTime;
  if (myglobal.currentTime < myTrack.startTiming + animationTime) {
    top.value = finalHeight.value - (k * myglobal.currentTime - k * myTrack.startTiming) * myglobal.screenHeight;
  } else if (myglobal.currentTime > myTrack.endTiming - animationTime) {
    top.value = finalHeight.value - (-k * myglobal.currentTime + k * myTrack.endTiming) * myglobal.screenHeight;
  } else {
    top.value = 0;
  }
  height.value = finalHeight.value - top.value;
};

const paintNote = (note) => {
  const painter = myglobal.notePainter;
  const currentTime = myglobal.currentTime;
  const yValue =
    ((myglobal.finalY / myglobal.remainingTime) * currentTime -
      (myglobal.finalY / myglobal.remainingTime) *
        (note.timing - myglobal.remainingTime)) *
    myglobal.screenHeight;
  
  const canMirror =
    (note.noteType != 1 &&
      yValue / myglobal.screenHeight >= 0.6 &&
      yValue / myglobal.screenHeight < 0.8) ||
    (note.noteType == 1 && yValue / myglobal.screenHeight > 0.6);

  if (note.noteType == 0) {
    if (canMirror) {
      const tempY = 2 * Y.value - yValue;
      painter.beginPath();
      painter.moveTo(middle.value, tempY - blackLength);
      painter.lineTo(middle.value + blackLength, tempY);
      painter.lineTo(middle.value, tempY + blackLength);
      painter.lineTo(middle.value - blackLength, tempY);
      painter.lineTo(middle.value, tempY - blackLength);
      painter.closePath();
      painter.fillStyle = "rgba(22, 22, 14,0.1)";
      painter.fill();
      painter.beginPath();
      painter.moveTo(middle.value, tempY - pinkLength);
      painter.lineTo(middle.value + pinkLength, tempY);
      painter.lineTo(middle.value, tempY + pinkLength);
      painter.lineTo(middle.value - pinkLength, tempY);
      painter.lineTo(middle.value, tempY - pinkLength);
      painter.closePath();
      painter.globalCompositeOperation = "destination-out";
      painter.fillStyle = "rgba(203, 105, 121,1)";
      painter.fill();
      painter.globalCompositeOperation = "source-over";
      painter.fillStyle = "rgba(203, 105, 121,0.1)";
      painter.fill();
    }
    painter.beginPath();
    painter.moveTo(middle.value, yValue - blackLength);
    painter.lineTo(middle.value + blackLength, yValue);
    painter.lineTo(middle.value, yValue + blackLength);
    painter.lineTo(middle.value - blackLength, yValue);
    painter.lineTo(middle.value, yValue - blackLength);
    painter.closePath();
    painter.fillStyle = "rgb(22, 22, 14)";
    painter.fill();
    painter.beginPath();
    painter.moveTo(middle.value, yValue - pinkLength);
    painter.lineTo(middle.value + pinkLength, yValue);
    painter.lineTo(middle.value, yValue + pinkLength);
    painter.lineTo(middle.value - pinkLength, yValue);
    painter.lineTo(middle.value, yValue - pinkLength);
    painter.closePath();
    painter.fillStyle = "rgb(203, 105, 121)";
    painter.fill();
  } else if (note.noteType == 1) {
    let kl = 0;
    let currentY = yValue;
    if (currentTime < note.timing) {
      kl = ((note.endTiming - note.timing) / myglobal.remainingTime) * myglobal.finalY * myglobal.screenHeight;
    } else if (currentTime < note.endTiming) {
      kl = ((note.endTiming - currentTime) / myglobal.remainingTime) * myglobal.finalY * myglobal.screenHeight;
      currentY = myglobal.finalY * myglobal.screenHeight;
    }
    
    if (canMirror) {
      const tempY = 2 * Y.value - currentY;
      painter.beginPath();
      painter.moveTo(middle.value + blackLength, tempY);
      painter.lineTo(middle.value + blackLength, tempY + kl);
      painter.lineTo(middle.value, tempY + kl + blackLength);
      painter.lineTo(middle.value - blackLength, tempY + kl);
      painter.lineTo(middle.value - blackLength, tempY);
      painter.lineTo(middle.value, tempY - blackLength);
      painter.closePath();
      painter.fillStyle = "rgba(22, 22, 14,0.1)";
      painter.fill();
      // Up pink
      painter.beginPath();
      painter.moveTo(middle.value + pinkLength, tempY);
      painter.lineTo(middle.value, tempY + pinkLength);
      painter.lineTo(middle.value - pinkLength, tempY);
      painter.lineTo(middle.value, tempY - pinkLength);
      painter.closePath();
      painter.globalCompositeOperation = "destination-out";
      painter.fillStyle = "rgba(203, 105, 121,1)";
      painter.fill();
      painter.globalCompositeOperation = "source-over";
      painter.fillStyle = "rgba(203, 105, 121,0.1)";
      painter.fill();
      // Down pink
      painter.beginPath();
      painter.moveTo(middle.value + pinkLength, tempY + kl);
      painter.lineTo(middle.value, tempY + kl + pinkLength);
      painter.lineTo(middle.value - pinkLength, tempY + kl);
      painter.lineTo(middle.value, tempY + kl - pinkLength);
      painter.closePath();
      painter.globalCompositeOperation = "destination-out";
      painter.fillStyle = "rgba(203, 105, 121,1)";
      painter.fill();
      painter.globalCompositeOperation = "source-over";
      painter.fillStyle = "rgba(203, 105, 121,0.1)";
      painter.fill();
    }
    painter.beginPath();
    painter.moveTo(middle.value + blackLength, currentY);
    painter.lineTo(middle.value + blackLength, currentY - kl);
    painter.lineTo(middle.value, currentY - kl - blackLength);
    painter.lineTo(middle.value - blackLength, currentY - kl);
    painter.lineTo(middle.value - blackLength, currentY);
    painter.lineTo(middle.value, currentY + blackLength);
    painter.closePath();
    painter.fillStyle = "rgb(22, 22, 14)";
    painter.fill();
    // Down
    painter.beginPath();
    painter.moveTo(middle.value + pinkLength, currentY);
    painter.lineTo(middle.value, currentY + pinkLength);
    painter.lineTo(middle.value - pinkLength, currentY);
    painter.lineTo(middle.value, currentY - pinkLength);
    painter.closePath();
    painter.fillStyle = "rgb(203, 105, 121)";
    painter.fill();
    // Up
    painter.beginPath();
    painter.moveTo(middle.value + pinkLength, currentY - kl);
    painter.lineTo(middle.value, currentY - kl + pinkLength);
    painter.lineTo(middle.value - pinkLength, currentY - kl);
    painter.lineTo(middle.value, currentY - kl - pinkLength);
    painter.closePath();
    painter.fillStyle = "rgb(203, 105, 121)";
    painter.fill();
  } else if (note.noteType == 2) {
    if (canMirror) {
      const tempY = 2 * Y.value - yValue;
      painter.beginPath();
      painter.moveTo(middle.value, tempY - whiteLength);
      painter.lineTo(middle.value + whiteLength, tempY);
      painter.lineTo(middle.value, tempY + whiteLength);
      painter.lineTo(middle.value - whiteLength, tempY);
      painter.lineTo(middle.value, tempY - whiteLength);
      painter.closePath();
      painter.fillStyle = "rgba(255,255,255,0.3)";
      painter.fill();
      painter.strokeStyle = "rgba(0,0,0,0.3)";
      painter.lineWidth = 1;
      painter.stroke();
    }
    painter.beginPath();
    painter.moveTo(middle.value, yValue - whiteLength);
    painter.lineTo(middle.value + whiteLength, yValue);
    painter.lineTo(middle.value, yValue + whiteLength);
    painter.lineTo(middle.value - whiteLength, yValue);
    painter.lineTo(middle.value, yValue - whiteLength);
    painter.closePath();
    painter.fillStyle = "rgb(255,255,255)";
    painter.fill();
    painter.strokeStyle = "rgb(0,0,0)";
    painter.lineWidth = 1;
    painter.stroke();
  }
};

const paintNotes = () => {
  if (!myTrack.judgeFinished) {
    for (let i = myTrack.lastNote; i >= myTrack.currentNote; i--) {
      paintNote(myTrack.notes[i]);
    }
  }
};

const paintTrack = async () => {
  await paintNotes();
  const painter = myglobal.trackPainter;
  if (!painter) return;
  if (width.value > 4 && height.value > 0) {
    const longerThanScreen = height.value > myglobal.screenHeight - Y.value;
    // Fill main rect
    painter.beginPath();
    painter.rect(left.value + 2, top.value, width.value - 4, height.value);
    painter.fillStyle = `rgba(${myTrack.tempR},${myTrack.tempG},${myTrack.tempB},${opacity})`;
    painter.fill();
    if (isActive.value) {
      painter.fillStyle = "rgba(255,255,255,0.4)";
      painter.fill();
    }

    painter.beginPath();
    painter.rect(
      left.value + 2,
      Y.value,
      width.value - 4,
      longerThanScreen ? myglobal.screenHeight - Y.value : height.value
    );
    painter.fillStyle = `rgba(${myTrack.tempR},${myTrack.tempG},${myTrack.tempB},0.1)`;
    painter.fill();
    if (isActive.value) {
      painter.fillStyle = "rgba(255,255,255,0.2)";
      painter.fill();
    }
    // Left line
    painter.beginPath();
    painter.moveTo(left.value, top.value);
    painter.lineTo(left.value, Y.value);
    painter.strokeStyle = "rgba(255,255,255,0.8)";
    painter.lineWidth = 2;
    painter.stroke();

    painter.beginPath();
    painter.moveTo(left.value, Y.value);
    painter.lineTo(left.value, longerThanScreen ? myglobal.screenHeight : Y.value + height.value);
    painter.strokeStyle = "rgba(255,255,255,0.1)";
    painter.lineWidth = 2;
    painter.stroke();

    // Right line
    painter.beginPath();
    painter.moveTo(left.value + width.value, top.value);
    painter.lineTo(left.value + width.value, Y.value);
    painter.strokeStyle = "rgba(255,255,255,0.8)";
    painter.lineWidth = 2;
    painter.stroke();

    painter.beginPath();
    painter.moveTo(left.value + width.value, Y.value);
    painter.lineTo(left.value + width.value, longerThanScreen ? myglobal.screenHeight : Y.value + height.value);
    painter.strokeStyle = "rgba(255,255,255,0.1)";
    painter.lineWidth = 2;
    painter.stroke();

    // Middle line
    painter.beginPath();
    painter.moveTo(middle.value, top.value);
    painter.lineTo(middle.value, Y.value);
    painter.strokeStyle = "rgba(0,0,0,0.3)";
    painter.lineWidth = 1;
    painter.stroke();

    painter.beginPath();
    painter.moveTo(middle.value, Y.value);
    painter.lineTo(middle.value, longerThanScreen ? myglobal.screenHeight : Y.value + height.value);
    painter.strokeStyle = "rgba(0,0,0,0.05)";
    painter.lineWidth = 1;
    painter.stroke();

    // Diamond
    painter.beginPath();
    painter.moveTo(middle.value, Y.value - lengthForBlackPoint);
    painter.lineTo(middle.value + lengthForBlackPoint, Y.value);
    painter.lineTo(middle.value, Y.value + lengthForBlackPoint);
    painter.lineTo(middle.value - lengthForBlackPoint, Y.value);
    painter.lineTo(middle.value, Y.value - lengthForBlackPoint);
    painter.closePath();
    painter.fillStyle = "rgb(22, 22, 14)";
    painter.fill();

    if (myTrack.type == 1) {
      const keyY = (Y.value * 9) / 8;
      painter.beginPath();
      painter.moveTo(middle.value, keyY - lengthForKey.value);
      painter.lineTo(middle.value + lengthForKey.value, keyY);
      painter.lineTo(middle.value, keyY + lengthForKey.value);
      painter.lineTo(middle.value - lengthForKey.value, keyY);
      painter.lineTo(middle.value, keyY - lengthForKey.value);
      painter.closePath();
      painter.strokeStyle = "rgb(255,255,255)";
      painter.lineWidth = 1;
      painter.stroke();
      if (isActive.value) {
        painter.fillStyle = "rgba(255,255,255,0.4)";
        painter.fill();
      }
      painter.fillStyle = "rgba(255, 255, 255,0.2)";
      painter.fill();
      painter.beginPath();
      painter.font = `${lengthForKey.value}px Arial`;
      painter.shadowColor = "rgba(0, 0, 0, 1)";
      painter.shadowBlur = 2;
      painter.fillStyle = "rgba(255, 255, 255,1)";
      painter.textAlign = "center";
      painter.textBaseline = "middle";
      painter.fillText(myTrack.key.toUpperCase(), middle.value, keyY);
      painter.shadowBlur = 0;
    }
  }
};

const addCount = (param) => {
  if (param.type != "lost") {
    myTrack.judges.push(param);
  }
  emit("addCount", param);
};

const addNoteCount = () => {
  if (myTrack.currentNote < myTrack.notes.length - 1) {
    myTrack.currentNote++;
  } else {
    myTrack.judgeFinished = true;
  }
};

const judge = () => {
  if (myTrack.notes.length > 0 && !myTrack.judgeFinished) {
    let currentKey = "";
    if (myTrack.type == 1) {
      currentKey = myTrack.key.toUpperCase();
    } else {
      currentKey = myTrack.notes[myTrack.currentNote].key.toUpperCase();
    }
    const currentJudge = myglobal.keyPressTime[currentKey];
    const currentTime = myglobal.currentTime;
    const note = myTrack.notes[myTrack.currentNote];
    const timing = note.timing;
    const { pureTime, farTime, lostTime } = myglobal;
    const isUsed = myglobal.keyUsed[currentKey];
    const isHold = myglobal.keyIsHold[currentKey];

    if (isJudgingHold.value) {
      if (currentTime > note.endTiming) {
        isJudgingHold.value = false;
        addCount(tempJudge.value);
        addNoteCount();
      } else if (!isHold) {
        isJudgingHold.value = false;
        addNoteCount();
        if (currentTime > note.endTiming - farTime) {
          addCount(tempJudge.value);
        } else {
          addCount({
            type: "lost",
            key: "lostCount",
            message: "因提前释放长键判定为Lost",
            judgeTime: currentTime,
            timing: note.endTiming,
          });
        }
      }
    } else {
      if (note.noteType == 2) {
        if (currentTime > timing - farTime) {
          if (currentTime > timing + farTime) {
            addCount({
              type: "lost",
              key: "lostCount",
              message: "因超时没有按到而判定为Lost",
              judgeTime: currentTime,
              timing: timing,
            });
            addNoteCount();
          } else if (myglobal.keyIsHold[currentKey]) {
            addCount({
              type: "pure",
              key: "pureCount",
              message: "pure",
              judgeTime: currentTime,
              timing: timing,
            });
            addNoteCount();
          }
        }
      } else if (note.noteType == 0) {
        if (currentTime > timing - farTime) {
          if (currentTime > timing + lostTime) {
            addCount({
              type: "lost",
              key: "lostCount",
              message: "因超时没有按到而判定为Lost",
              judgeTime: currentTime,
              timing: timing,
            });
            addNoteCount();
          } else if (!isUsed) {
            if (currentJudge > timing - pureTime && currentJudge < timing + pureTime) {
              addCount({ type: "pure", key: "pureCount", message: "pure", judgeTime: currentJudge, timing });
              myglobal.keyUsed[currentKey] = true;
              addNoteCount();
            } else if (currentJudge > timing - farTime && currentJudge < timing + farTime) {
              addCount({
                type: "far",
                key: "farCount",
                message: currentJudge < timing ? "因为过早按下而判定为far(early)" : "因为过晚按下而判定为far(late)",
                judgeTime: currentJudge,
                timing
              });
              myglobal.keyUsed[currentKey] = true;
              addNoteCount();
            } else if (currentJudge > timing - lostTime && currentJudge < timing + lostTime) {
              addCount({
                type: "lost",
                key: "lostCount",
                message: currentJudge < timing ? "因为过早按下而判定为lost(early)" : "因为过晚按下而判定为lost(late)",
                judgeTime: currentJudge,
                timing
              });
              myglobal.keyUsed[currentKey] = true;
              addNoteCount();
            }
          }
        }
      } else {
        // Long Note
        if (currentTime > timing - farTime) {
          if (currentTime > timing + farTime) {
            addCount({
              type: "lost",
              key: "lostCount",
              message: "因超时没有按到而判定为Lost",
              judgeTime: currentTime,
              timing: timing,
            });
            addNoteCount();
          } else if (!isUsed) {
            if (currentJudge > timing - pureTime && currentJudge < timing + pureTime) {
              tempJudge.value = { type: "pure", key: "pureCount", message: "pure", judgeTime: currentJudge, timing: note.endTiming };
              myTrack.judges.push({ type: "pure", key: "pureCount", message: "pure", judgeTime: currentJudge, timing });
              myglobal.keyUsed[currentKey] = true;
              isJudgingHold.value = true;
            } else if (currentJudge > timing - farTime && currentJudge < timing + farTime) {
              tempJudge.value = { type: "far", key: "farCount", message: currentJudge < timing ? "early" : "late", judgeTime: currentJudge, timing: note.endTiming };
              myTrack.judges.push({ type: "far", key: "farCount", message: "far", judgeTime: currentJudge, timing });
              myglobal.keyUsed[currentKey] = true;
              isJudgingHold.value = true;
            } else if (currentJudge > timing - lostTime && currentJudge < timing + lostTime) {
              addCount({ type: "lost", key: "lostCount", message: "lost", judgeTime: currentJudge, timing });
              myglobal.keyUsed[currentKey] = true;
              addNoteCount();
            }
          }
        }
      }
    }
  }
};

const generateWidthPath = () => {
  widthPath.value = [];
  widthIndex.value = 0;
  let length = myTrack.changeWidthOperations.length;
  let start = myTrack.startTiming;
  let end = start;
  if (length == 0) {
    end = myTrack.endTiming - animationTime;
  } else {
    myTrack.changeWidthOperations.sort((a, b) => a.startTime - b.startTime);
    end = myTrack.changeWidthOperations[0].startTime;
  }
  widthPath.value.push({ type: 0, width: 0, startTime: 0, endTime: start });
  widthPath.value.push({ type: 0, width: myTrack.width, startTime: start, endTime: end });

  for (let i = 0; i < length; i++) {
    let operation = myTrack.changeWidthOperations[i];
    start = operation.startTime;
    end = operation.endTime;
    if (operation.startTime != operation.endTime) {
      let k = (operation.endWidth - operation.startWidth) / (operation.endTime - operation.startTime);
      let b = operation.endWidth - k * operation.endTime;
      widthPath.value.push({ type: 1, k, b, startTime: start, endTime: end });
    }
    if (i != length - 1) {
      let nextOperation = myTrack.changeWidthOperations[i + 1];
      if (end < nextOperation.startTime) {
        widthPath.value.push({ type: 0, width: operation.endWidth, startTime: end, endTime: nextOperation.startTime });
      }
    } else {
      widthPath.value.push({ type: 0, width: operation.endWidth, startTime: end, endTime: myTrack.endTiming });
    }
  }
};

const generatePositionXPath = () => {
  positionXPath.value = [];
  positionXIndex.value = 0;
  let length = myTrack.moveOperations.length;
  let start = myTrack.startTiming;
  let end = start;
  if (length == 0) {
    end = myTrack.endTiming;
  } else {
    myTrack.moveOperations.sort((a, b) => a.startTime - b.startTime);
    end = myTrack.moveOperations[0].startTime;
  }
  positionXPath.value.push({ type: 0, positionX: myTrack.positionX, startTime: 0, endTime: end });
  for (let i = 0; i < length; i++) {
    let operation = myTrack.moveOperations[i];
    start = operation.startTime;
    end = operation.endTime;
    if (operation.startTime != operation.endTime) {
      let k = (operation.endX - operation.startX) / (operation.endTime - operation.startTime);
      let b = operation.endX - k * operation.endTime;
      positionXPath.value.push({ type: 1, k, b, startTime: start, endTime: end });
    }
    if (i != length - 1) {
      let nextOperation = myTrack.moveOperations[i + 1];
      if (end < nextOperation.startTime) {
        positionXPath.value.push({ type: 0, positionX: operation.endX, startTime: end, endTime: nextOperation.startTime });
      }
    } else {
      if (end < myTrack.endTiming) {
        positionXPath.value.push({ type: 0, positionX: operation.endX, startTime: end, endTime: myTrack.endTiming });
      }
    }
  }
};

const generateRGBPath = () => {
  RGBPath.value = [];
  RGBIndex.value = 0;
  let length = myTrack.changeColorOperations.length;
  let start = myTrack.startTiming;
  let end = start;
  if (length == 0) {
    end = myTrack.endTiming;
  } else {
    myTrack.changeColorOperations.sort((a, b) => a.startTime - b.startTime);
    end = myTrack.changeColorOperations[0].startTime;
  }
  RGBPath.value.push({ type: 0, R: myTrack.R, G: myTrack.G, B: myTrack.B, startTime: 0, endTime: end });
  for (let i = 0; i < length; i++) {
    let operation = myTrack.changeColorOperations[i];
    start = operation.startTime;
    end = operation.endTime;
    if (operation.startTime != operation.endTime) {
      let Rk = (operation.endR - operation.startR) / (operation.endTime - operation.startTime);
      let Rb = operation.endR - Rk * operation.endTime;
      let Gk = (operation.endG - operation.startG) / (operation.endTime - operation.startTime);
      let Gb = operation.endG - Gk * operation.endTime;
      let Bk = (operation.endB - operation.startB) / (operation.endTime - operation.startTime);
      let Bb = operation.endB - Bk * operation.endTime;
      RGBPath.value.push({ type: 1, Rk, Rb, Gk, Gb, Bk, Bb, startTime: start, endTime: end });
    }
    if (i != length - 1) {
      let nextOperation = myTrack.changeColorOperations[i + 1];
      if (end < nextOperation.startTime) {
        RGBPath.value.push({ type: 0, R: operation.endR, G: operation.endG, B: operation.endB, startTime: end, endTime: nextOperation.startTime });
      }
    } else {
      if (end < myTrack.endTiming) {
        RGBPath.value.push({ type: 0, R: operation.endR, G: operation.endG, B: operation.endB, startTime: end, endTime: myTrack.endTiming });
      }
    }
  }
};

const setIndex = () => {
  myTrack.moveOperations.forEach((op, i) => (op.index = i));
  myTrack.changeWidthOperations.forEach((op, i) => (op.index = i));
  myTrack.changeColorOperations.forEach((op, i) => (op.index = i));
};

const initiate = () => {
  setHeightAndTop();
  setIndex();
  generateWidthPath();
  generatePositionXPath();
  generateRGBPath();
  myTrack.notes.sort((a, b) => a.timing - b.timing);
  myTrack.notes.forEach((note, i) => (note.index = i));
  
  let index = 0;
  let last = myTrack.notes.length;
  for (let j = myTrack.notes.length - 1; j >= 0; j--) {
    myTrack.notes[j].judged = false;
    if (myTrack.notes[j].timing > myglobal.currentTime) index = j;
    if (myglobal.currentTime < myTrack.notes[j].timing - myglobal.remainingTime) last = j;
  }
  myTrack.currentNote = index;
  myTrack.lastNote = last - 1;
  myTrack.judges = [];
  myTrack.tempPositionX = getPositionX();
  myTrack.tempWidth = getWidth();
  const rgb = getRGB();
  myTrack.tempR = rgb[0];
  myTrack.tempG = rgb[1];
  myTrack.tempB = rgb[2];
  paintTrack();
};

watch(() => myglobal.currentTime, () => {
  if (myglobal.currentTime > 0) {
    myTrack.tempPositionX = getPositionX();
    myTrack.tempWidth = getWidth();
    const rgb = getRGB();
    myTrack.tempR = rgb[0];
    myTrack.tempG = rgb[1];
    myTrack.tempB = rgb[2];
    setHeightAndTop();
    while (
      myTrack.judges.length > 0 &&
      myTrack.judges[0].timing + judgeAnimationTime < myglobal.currentTime
    ) {
      myTrack.judges.shift();
    }
    // paintTrack() is handled by BeatPlayer triggering global.repaint
    // which ensures all tracks draw after the canvas is cleared.
  }
  while (
    myTrack.lastNote < myTrack.notes.length - 1 &&
    myglobal.currentTime > myTrack.notes[myTrack.lastNote + 1].timing - myglobal.remainingTime
  ) {
    myTrack.lastNote++;
  }
  judge();
});

watch(() => myglobal.screenHeight, () => {
  setHeightAndTop();
  paintTrack();
});

watch(() => myglobal.screenWidth, () => {
  paintTrack();
});

watch(() => myglobal.repaint, () => {
  paintTrack();
});

watch(() => myglobal.reCalculateTrack, () => {
  setIndex();
  generatePositionXPath();
  myTrack.tempPositionX = getPositionX();
  generateWidthPath();
  myTrack.tempWidth = getWidth();
  generateRGBPath();
  const rgb = getRGB();
  myTrack.tempR = rgb[0];
  myTrack.tempG = rgb[1];
  myTrack.tempB = rgb[2];
  myTrack.notes.sort((a, b) => a.timing - b.timing);
  myTrack.notes.forEach((n, i) => (n.index = i));
  paintTrack();
});

onMounted(() => {
  initiate();
});
</script>

<style scoped>
.select {
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}
</style>
