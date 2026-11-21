<template />

<script setup>
import { ref, toRaw, unref, inject, defineProps, watch } from "vue";
import buildLayer, { LayerType } from "../../primitive/layer/Layer";
import emitter from "../../utils/eventbus";

const geosvrImageLyr = ref(null);

const props = defineProps({
  lyrOption: {
    // name: String,
    // url: String,
    // layers: String[],
    // styles?: String[],
    // zIndex?: Number,
    // filter? String, // "habitat in('水浇地', '河流')"
    // projection? Object || String || system, 
    // imageLoadFunction?: (image, src) => {}
    // params?: Object
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
    geosvrImageLyr.value = buildLayer(LayerType.GeoServerImage, lyrOpt);
    if (!geosvrImageLyr) {
      throw new Error("initialize GeoServerImage layer failed");
    }
    if (map.layerManager.getLayer(lyrOpt.name)) {
      map.layerManager.removeLayer(lyrOpt.name);
    }
    map.layerManager.addLayer(lyrOpt.name, unref(geosvrImageLyr));
  }
});
</script>
