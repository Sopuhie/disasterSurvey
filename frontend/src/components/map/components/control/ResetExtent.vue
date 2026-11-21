<template>
</template>

<script setup>
import { ref, unref, defineExpose } from "vue";
import { ResetExtent }  from "../../primitive/control/ResetExtent.js"
import emitter from "../../utils/eventbus";

let setCtrl = ref(null);

emitter.on("mapMounted", map => {
  setCtrl = new ResetExtent({
    map: map
  })
})

const reset = () => {
  setCtrl.reset();
}


defineExpose({
  reset,
})

</script>