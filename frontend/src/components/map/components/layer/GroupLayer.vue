<template />

<script setup>
import {
  ref,
  toRaw,
  unref,
  inject,
  defineProps,
  defineExpose,
  watch,
} from "vue";
import buildLayer, { LayerType } from "../../primitive/layer/Layer";
import emitter from "../../utils/eventbus";

const map = inject("map");
const groupLyr = ref(null);

const props = defineProps({
  lyrOption: {
    // name: String,
    // layers: [],
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
    groupLyr.value = buildLayer(LayerType.Group, lyrOpt);
    if (!groupLyr) {
      throw new Error("initialize group layer failed");
    }
    if (map.layerManager.getLayer(lyrOpt.name)) {
      map.layerManager.removeLayer(lyrOpt.name);
    }
    map.layerManager.addLayer(lyrOpt.name, unref(groupLyr));
  }
});

const addLayer = (type, lyrOption) => {
  let lyr = buildLayer(type, lyrOption);
  groupLyr.value.addLayer({ lyrName: lyrOption.name, layer: lyr });
};

const setOpacity = (opacity) => {
  groupLyr.value.setOpacity(opacity);
};

defineExpose({
  addLayer,
  setOpacity,
});
</script>
