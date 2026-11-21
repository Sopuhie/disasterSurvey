<template />

<script setup>
import {
  ref,
  toRaw,
  unref,
  watch,
  inject,
  defineProps,
  defineExpose,
} from "vue";
import { SwipeTool2, SwipeType } from "../../primitive/interaction/SwipeTool2";
import emitter from "../../utils/eventbus";

const map = inject("map");
let swipeTool = null;

const props = defineProps({
  swipeMode: {
    type: String,
    default: () => {
      return SwipeType.Horizontal;
    },
  },
});

emitter.on("mapMounted", (map) => {
  swipeTool = new SwipeTool2({
    // map: unref(map),
    // orientation: props.swipeMode
  });
});

watch(
  () => props.swipeMode,
  (val) => {
    if (val) {
      swipeTool.setSwipeType(val);
    }
  }
);

function start(leftLyrs, rightLyrs) {
  swipeTool.start({
    map: unref(map),
    leftLyrs, 
    rightLyrs
  });
}

function finish() {
  swipeTool.finish(unref(map));
}

defineExpose({
  start,
  finish,
});
</script>
