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
import { DrawTool, DrawType } from "../../primitive/interaction/DrawTool";
import emitter from "../../utils/eventbus";

const map = inject("map");
let drawTool = null;
let isDrawed = false;

const props = defineProps({
  drawType: {
    type: String,
    default: () => {
      return DrawType.Line;
    },
  },
  drawingStyle: {
    type: Object,
    default: () => {
      return null;
    },
  },
  drawedStyle: {
    type: Object,
    default: () => {
      return null;
    },
  },
});

emitter.on("mapMounted", (map) => {
  // drawTool = new DrawTool({
  //   map: map.value,
  // });
  drawTool = map.drawTool;
});

watch(
  () => props.drawType,
  () => {
    if (drawTool && isDrawed) {
      drawTool.toggle({ ...props });
    }
  }
);

// 结束绘制
function finishDraw() {
  drawTool.finish();
  isDrawed = false;
}

// 开始绘制
function startDraw() {
  drawTool.start({ ...props });
  isDrawed = true;
}

// 清除已经绘制
function clear() {
  drawTool.clear();
}

function getFeatures() {
  return drawTool.getFeatures();
}

defineExpose({
  startDraw,
  clear,
  finishDraw,
  getFeatures
});
</script>
