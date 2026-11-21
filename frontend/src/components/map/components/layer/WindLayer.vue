<template />

<script setup>
import { ref, toRaw, unref, inject, defineProps, watch } from "vue";
import buildLayer, { LayerType } from "../../primitive/layer/Layer";
import emitter from "../../utils/eventbus";

const windLyr = ref(null);
const map = inject("map");

const props = defineProps({
  lyrOption: {
    // name: String,
    // zIndex: Number
    // data: json 数据
    // forceRender: boolean	在鼠标交互时始终显示粒子
    // windOptions: object	风场参数，具体配置如下
    // {
    //   globalAlpha	全局透明度，主要影响粒子路径拖尾效果	number	0.9
    //   lineWidth	粒子路径宽度	number|function	1, 当为回调函数时，参数function(m:对应点风速值) => number
    //   colorScale	粒子颜色配置	string|function|string[]	#fff，当为回调函数时，参数function(m:对应点风速值) => string
    //   velocityScale	对于粒子路径步长的乘积基数	number	1 / 25
    //   maxAge | particleAge(不推荐使用)	粒子路径能够生成的最大帧数	number	90
    //   paths	生成的粒子路径数量	number|function	800, 当为回调函数时，参数function(m:对应点风速值) => number
    //   particleMultiplier	粒子路径数量的系数，不推荐使用（视野宽度 * 高度 * 系数）	number	1 / 300
    //   frameRate	帧率（ms）	number	20
    // }
    // fieldOptions: object	数据配置项，具体配置如下
    // {
    //   /* 一般格点数据是按照矩形范围来切割，所以定义其经纬度范围 */
    //   xmin: number; // 经度最小值
    //   ymin: number; // 纬度最小值
    //   xmax: number; // 经度最大值
    //   ymax: number; // 纬度最大值
    //   deltaX: number; // x（经度）增量
    //   deltaY: number; // y（纬度）增量 (默认我们采用的数据和格点原始数据方向保持一致，数据从左上到右下) 但是需要注意的是此时 deltaY为 -(ymax-ymin) / rows
    //   cols: number; // 列（可由 `(xmax - xmin) / deltaX` 得到）
    //   rows: number; // 行
    //   us: number[]; // U分量
    //   vs: number[]; // V分量
    //   flipY?: boolean; // 因为grib2json的问题，我们需要翻转 Y 轴数据
    //   wrapX?: boolean; // 是否实现跨世界渲染
    //   wrappedX?: boolean; // 当数据范围时按照 [0, 360] 时需要对x方向进行切割转换为 [-180, 180]，即将废弃
    //   translateX?: boolean; // 当数据范围时按照 [0, 360] 时需要对x方向进行切割转换为 [-180, 180]
    // }
    type: Object,
    default: () => {
      return null;
    },
  },
  isVisble: {
    type: Boolean,
    default: () => {
      return true;
    },
  },
});

emitter.on("mapMounted", (map) => {});

watch(
  () => props.lyrOption,
  () => {
    if (map.value) {
      const lyrOpt = toRaw(props.lyrOption);
      windLyr.value = buildLayer(LayerType.Wind, lyrOpt);
      if (!windLyr) {
        throw new Error("initialize Wind layer failed");
      }
      if (map.value.layerManager.getLayer(lyrOpt.name)) {
        map.value.layerManager.removeLayer(lyrOpt.name);
      }
      map.value.layerManager.addLayer(lyrOpt.name, unref(windLyr));
    }
  },
  {
    deep: true,
    immediate: true,
  }
);
</script>
