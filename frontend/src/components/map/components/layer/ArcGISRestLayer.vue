<template />

<script setup>
import { ref, toRaw, unref, inject, defineProps, watch } from "vue";
import buildLayer, { LayerType } from "../../primitive/layer/Layer";
import emitter from "../../utils/eventbus";

const ArcGISRestLyr = ref(null);

const props = defineProps({
  lyrOption: {
    // name: String,
    // url: String,
    // zIndex: Number,
    // layers: []
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
    ArcGISRestLyr.value = buildLayer(LayerType.ArcGISRest, lyrOpt);
    if (!ArcGISRestLyr) {
      throw new Error("initialize ArcGISRest layer failed");
    }
    if (map.layerManager.getLayer(lyrOpt.name)) {
      map.layerManager.removeLayer(lyrOpt.name);
    }
    map.layerManager.addLayer(lyrOpt.name, unref(ArcGISRestLyr));
  }
});
</script>
