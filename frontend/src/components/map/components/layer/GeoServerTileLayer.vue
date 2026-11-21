<template />

<script setup>
import { ref, toRaw, unref, inject, defineProps, watch } from "vue";
import buildLayer, { LayerType } from "../../primitive/layer/Layer";
import emitter from "../../utils/eventbus";

const geosvrTileLyr = ref(null);

const props = defineProps({
  lyrOption: {
    // name: String,
    // url: String,
    // origin: Number[], minX, minY
    // layers: String[],
    // styles?: String[],
    // zIndex?: Number,
    // filter? String, // "habitat in('水浇地', '河流')"
    // projection? Object || String || system, 
    // tileLoadFunction?: (imageTile, src) => {}
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
    geosvrTileLyr.value = buildLayer(LayerType.GeoServerTile, lyrOpt);
    if (!geosvrTileLyr) {
      throw new Error("initialize GeoServerTile layer failed");
    }
    if (map.layerManager.getLayer(lyrOpt.name)) {
      map.layerManager.removeLayer(lyrOpt.name);
    }
    map.layerManager.addLayer(lyrOpt.name, unref(geosvrTileLyr));
  }
});
</script>
