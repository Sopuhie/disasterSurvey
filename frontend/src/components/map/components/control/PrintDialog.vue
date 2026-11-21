<template>
  <div ref="printDiv" />
</template>

<script setup>
import { ref, unref, inject, defineExpose, defineProps } from "vue";
import { PrintDialog as OLPrintDialog } from "../../primitive/control/PrintDialog.js"
import emitter from "../../utils/eventbus";

const map = inject("map");
const printDiv = ref(null)

const print = () => {
  let printDialog = new OLPrintDialog({ 
    map: unref(map),
    lang: 'zh',
    target: unref(printDiv)  // 传一个空 div，为了避免显示控件
  });
  printDialog.print();
};

defineExpose({
  print
})

</script>