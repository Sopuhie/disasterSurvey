<template>
</template>

<script setup>
import { ref, unref, inject, defineExpose, defineProps } from "vue";
import { ExportPicture, PictureType }  from "../../primitive/control/ExportPicture.js"
import emitter from "../../utils/eventbus";

const map = inject("map");
const exportTool = ref(null);

const props = defineProps({
  name: {
    type: String,
    default: () => {
      return "exportmap";
    },
  },
  type: {
    type: String,
    default: () => {
      return PictureType.PNG;
    },
  },
});

emitter.on("mapMounted", map => {
  exportTool.value = new ExportPicture({
    name: props.name,
    type: props.type
  })
})

const exportPicture = () => {
  exportTool.value.exportPic(unref(map));
};

defineExpose({
  exportPicture
})

</script>