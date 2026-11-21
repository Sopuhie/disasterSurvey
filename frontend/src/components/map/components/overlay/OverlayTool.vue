<template>
  <div class="refOverlay" ref="refOverlay">
    <slot :close="close" :selectedFeatures="selFeats" :coords="coordinates" />
  </div>
</template>

<script setup>
import {
  ref,
  toRaw,
  unref,
  inject,
  defineProps,
  watch,
  onMounted,
  defineExpose,
} from "vue";
import OverlayTool from "../../primitive/overlay/OverlayTool.js";
import GeoJSONUtil from "../../primitive/util/GeojsonUtil.js";
import emitter from "../../utils/eventbus";
import { getCenter } from "../../primitive/util/CalculationUtil.js";

const map = inject("map");
let overlayTool = null;

const refOverlay = ref(null);
let coordinates = ref([]);
let selFeats = ref(null);

const props = defineProps({
  options: {
    // {
    //   id?: number | string;
    //   element?: HTMLElement;
    //   offset?: number[];
    //   position?: number[];
    //   positioning?: any | string;
    //   stopEvent?: boolean;
    //   insertFirst?: boolean;
    //   autoPan?: boolean;
    //   autoPanAnimation?: {
    //       duration?: number;
    //       easing?: (p0: number) => number;
    //   };
    //   autoPanMargin?: number;
    //   className?: string;
    //   toCenter?: boolean;
    // }
    type: Object,
    default: () => {
      return null;
    },
  },
  layers: {
    type: Array,
    default: () => {
      return [];
    },
  },
});

watch(
  () => coordinates,
  (val) => {
    if (val?.length) {
      overlay.positionTo(val);
    } else {
      overlay.positionTo(null);
    }
  }
);

emitter.on("mapMounted", (map) => {
  if (map) {
    overlayTool = new OverlayTool(toRaw(props.options));
    map.overlayManager.addOverlay("Overlay" + Math.floor(Math.random()*10000), overlayTool);
    overlayTool.bindElement(unref(refOverlay));

    if (props.layers?.length) {
      let lyrs = props.layers.map((lyr) => {
        return map.layerManager.getLayer(lyr).ol;
      });
      map.olMap.on("click", (evt) => {
        selFeats.value = null;
        coordinates.value = evt.coordinate;
        let pixel = map.olMap.getEventPixel(evt.originalEvent);
        let feats = map.olMap.getFeaturesAtPixel(pixel, {
          layerFilter: (layer) => {
            if (lyrs?.length) {
              return lyrs.includes(layer);
            } else {
              return true;
            }
          },
        });
        if (feats && feats.length) {
          selFeats.value = GeoJSONUtil.writeFeatures(feats);
          if (props.options.toCenter) {
            let center = getCenter(unref(selFeats));
            let { resolution, zoom } = map.mapOptions.viewOptions;
            if (resolution) {
              zoom = map.olView.getZoomForResolution(resolution);
            }
            map.flyToManager.flyTo(center, zoom);
          }
        }
      });
    }
  }
});

function close() {
  coordinates.value = [];
}

defineExpose({
  close,
});
</script>
