<template>
	<el-button v-btn type="primary" plain @click="onDraw">{{ btnTxt }}</el-button>
</template>

<script setup>
	const props = defineProps({
		btnTxt: {
			type: String,
			default: '按钮'
		}
	})
	const emits = defineEmits(['onDraw'])
	const onDraw = () => {
		emits('onDraw')
	}
</script>

<style lang="scss" scoped></style>
