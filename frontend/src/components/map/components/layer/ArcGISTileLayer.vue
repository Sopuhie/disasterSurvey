<template />

<script setup>
import { ref, toRaw, unref, inject, defineProps, watch } from "vue";
import buildLayer, { LayerType } from "../../primitive/layer/Layer";
import emitter from "../../utils/eventbus";

const ArcGISTileLyr = ref(null);

const props = defineProps({
  lyrOption: {
    // name: String,
    // url: String,
    // origin: Number[], 
    // zIndex?: Number,
    // tileSize?: Number || 256, 
    // resolutions?: Number[] || system, 
    // projection? Object || String || system, 
    // extent?: number[] || system
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
    lyrOpt.projection = lyrOpt.projection || map.projection;
    lyrOpt.resolutions = lyrOpt.resolutions || map.resolutions;
    ArcGISTileLyr.value = buildLayer(LayerType.ArcGISTile, lyrOpt);
    if (!ArcGISTileLyr) {
      throw new Error("initialize ArcGISTile layer failed");
    }
    if (map.layerManager.getLayer(lyrOpt.name)) {
      map.layerManager.removeLayer(lyrOpt.name);
    }
    map.layerManager.addLayer(lyrOpt.name, unref(ArcGISTileLyr));
  }
});
</script>
