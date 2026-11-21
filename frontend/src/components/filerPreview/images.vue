<template>
	<el-image :src="fileUrl" style="width: 100%; height: 100%"></el-image>
</template>

<script setup>
	const props = defineProps({
		fileUrl: {
			type: String,
			default: ''
		}
	})
</script>

<style lang="scss" scoped></style>
