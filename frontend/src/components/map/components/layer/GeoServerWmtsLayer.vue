<template />

<script setup>
import { ref, toRaw, unref, inject, defineProps, watch } from "vue";
import buildLayer, { LayerType } from "../../primitive/layer/Layer";
import emitter from "../../utils/eventbus";

const geosvrWmtsLyr = ref(null);

const props = defineProps({
  lyrOption: {
    // name: string 图层名称
    // layer: string 加载图层名称
    // url: string 服务地址
    // projection?: string 投影
    // resolutions?: number[] 分辨率
    // extent?：number[] 四至
    // origin?：number[] 原点
    // style?: string 样式
    // zIndex?: number	图层层级
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

emitter.on("mapMounted", (map) => {
  if (map) {
    const lyrOpt = toRaw(props.lyrOption);
    lyrOpt.projection = lyrOpt?.projection || map.projection;
    geosvrWmtsLyr.value = buildLayer(LayerType.GeoServerWmts, lyrOpt);
    if (!geosvrWmtsLyr) {
      throw new Error("initialize GeoServerWmts layer failed");
    }
    if (map.layerManager.getLayer(lyrOpt.name)) {
      map.layerManager.removeLayer(lyrOpt.name);
    }
    map.layerManager.addLayer(lyrOpt.name, unref(geosvrWmtsLyr));
  }
});
</script>
