<template>
    <span class="rolling-number">
        {{ displayValue }}
    </span>
</template>

<script setup>
import { ref, watch, onMounted, defineProps } from 'vue';
import TWEEN from '@tweenjs/tween.js';

const props = defineProps({
    value: {
        type: [Number, String],
        required: true
    },
    format: {
        type: Object,
        default: () => ({})
    }
});

const displayValue = ref('');
const internalValue = ref({ val: 0 });

const updateDisplay = () => {
    let val = Math.floor(internalValue.value.val);
    let str = val.toString();

    if (props.format.minimumIntegerDigits) {
        str = str.padStart(props.format.minimumIntegerDigits, '0');
    }

    displayValue.value = str;
};

const animateValue = (newValue) => {
    const target = Number(newValue);
    new TWEEN.Tween(internalValue.value)
        .to({ val: target }, 500)
        .easing(TWEEN.Easing.Quadratic.Out)
        .onUpdate(updateDisplay)
        .start();
};

watch(() => props.value, (newVal) => {
    animateValue(newVal);
});

onMounted(() => {
    internalValue.value.val = Number(props.value);
    updateDisplay();

    // Ensure tween works
    const animate = (time) => {
        requestAnimationFrame(animate);
        TWEEN.update(time);
    };
    requestAnimationFrame(animate);
});
</script>

<style scoped>
.rolling-number {
    display: inline-block;
    font-variant-numeric: tabular-nums;
}
</style>
