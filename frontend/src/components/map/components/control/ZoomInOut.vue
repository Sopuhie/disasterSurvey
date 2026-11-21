<template>
</template>

<script setup>
import { ref, unref, defineExpose } from "vue";
import { ZoomInOut }  from "../../primitive/control/ZoomInOut.js"
import emitter from "../../utils/eventbus";

const zoomTool = ref(null);

emitter.on("mapMounted", map => {
  zoomTool.value = new ZoomInOut({
    map: map
  })
})

const zoomIn = () => {
  zoomTool.value.zoomIn();
}

const zoomOut = () => {
  zoomTool.value.zoomOut();
}

defineExpose({
  zoomIn,
  zoomOut
})

</script>