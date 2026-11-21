<template />

<script setup>
import { ref, toRaw, unref, inject, defineProps, watch } from "vue";
import buildLayer, { LayerType } from "../../primitive/layer/Layer"
import emitter from "../../utils/eventbus";

const vctLyr = ref(null);

const props = defineProps({
  lyrOption: {
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

emitter.on("mapMounted", map => {
  if (map) {
    const lyrOpt = toRaw(props.lyrOption);
    vctLyr.value = buildLayer(LayerType.Vector, lyrOpt);
    if (!vctLyr) {
        throw new Error("initialize vector layer failed");
    }
    if (map.layerManager.getLayer(lyrOpt.name)) {
        map.layerManager.removeLayer(lyrOpt.name);
    }
    map.layerManager.addLayer(lyrOpt.name, unref(vctLyr));
  }
})

</script>