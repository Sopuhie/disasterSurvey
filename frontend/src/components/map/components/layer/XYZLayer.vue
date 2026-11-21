<template />

<script setup>
import { ref, toRaw, unref, inject, defineProps, watch } from "vue";
import buildLayer, { LayerType } from "../../primitive/layer/Layer";
import emitter from "../../utils/eventbus";

const tileLyr = ref(null);

const props = defineProps({
  lyrOption: {
    // name: String,
    // url: String,
    // zIndex: Number
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
    tileLyr.value = buildLayer(LayerType.XYZ, lyrOpt);
    if (!tileLyr) {
      throw new Error("initialize XYZ layer failed");
    }
    if (map.layerManager.getLayer(lyrOpt.name)) {
      map.layerManager.removeLayer(lyrOpt.name);
    }
    map.layerManager.addLayer(lyrOpt.name, unref(tileLyr));
  }
});
</script>
