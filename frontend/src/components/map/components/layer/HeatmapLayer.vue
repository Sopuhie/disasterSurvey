<template />

<script setup>
import { ref, toRaw, unref, inject, defineProps, watch } from "vue";
import buildLayer, { LayerType } from "../../primitive/layer/Layer";
import emitter from "../../utils/eventbus";

const map = inject("map");
let heatmapLyr = null;

const props = defineProps({
  lyrOption: {
    // name: String,
    // zIndex: Number
    // url?: string,
    // geoJSON?: GeoJSON | null,
    // style?: StyleLike,
    // blur: number,
    // radius: number,
    // weight: string
    type: Object,
    default: () => {
      return null;
    },
  },
  points: {
    // [{
    //   lng: number,
    //   lat: number,
    //   weight: number
    // }, ...]
    type: Array,
    default: () => {
      return [];
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
    heatmapLyr = buildLayer(LayerType.Heatmap, lyrOpt);
    if (!heatmapLyr) {
      throw new Error("initialize Heatmap layer failed");
    }
    if (map.layerManager.getLayer(lyrOpt.name)) {
      map.layerManager.removeLayer(lyrOpt.name);
    }
    map.layerManager.addLayer(lyrOpt.name, unref(heatmapLyr));
  }
});

watch(
  () => props.points,
  () => {
    if (props.points?.length) {
      heatmapLyr.draw({
        type: "FeatureCollection",
        features: props.points.map((point, index) => {
          return {
            id: index,
            type: "Feature",
            geometry: {
              type: "Point",
              coordinates: [point.lng, point.lat],
            },
            properties: {
              weight: point.weight,
            },
          };
        }),
      });
    }
  },
  {
    deep: true,
    immediate: true,
  }
);

function setBlur(blur) {
  if (heatmapLyr) {
    heatmapLyr.setBlur(blur);
  }
}

function setRadius(radius) {
  if (heatmapLyr) {
    heatmapLyr.setRadius(radius);
  }
}

function clear() {
  if (heatmapLyr) {
    heatmapLyr.clear();
  }
}

function isAuto(flag) {
  if (heatmapLyr) {
    heatmapLyr.isAuto(flag, unref(map));
  }
}

defineExpose({
  setBlur,
  setRadius,
  clear,
  isAuto,
});
</script>
