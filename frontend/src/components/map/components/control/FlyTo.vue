<template>
</template>

<script setup>
import { ref, unref, defineExpose, inject } from "vue";
import emitter from "../../utils/eventbus";

const map = inject("map");

emitter.on("mapMounted", map => {
  console.log(map)
})

const flyTo = (coords, zoomTo) => {
  map.value.flyToManager.flyTo(coords, zoomTo);
}

const flyToRegion = (region, flash = false) => {
  map.value.flyToManager.flyToRegion(region, flash);
}

defineExpose({
  flyTo,
  flyToRegion
})

</script>