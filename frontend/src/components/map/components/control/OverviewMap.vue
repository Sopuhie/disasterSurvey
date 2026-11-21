<template>
</template>

<script setup>
import { ref, unref, defineExpose, inject } from "vue";
import emitter from "../../utils/eventbus";
import OverviewMap from "../../primitive/control/OverviewMap" 

const map = inject("map");

emitter.on("mapMounted", map => {
  let overmap = new OverviewMap({
    projection: map.projection
  });
  overmap.setMap(map);
})

defineExpose({
})

</script>