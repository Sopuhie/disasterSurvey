<template>
	<div class="empty" style="height: 100%">
		<el-empty :image-size="imageSize" :description="emptyText" />
	</div>
</template>

<script setup>
	const props = defineProps({
		emptyText: {
			type: String,
			default: '暂无数据'
		},
		imageSize: {
			type: Number,
			default: 200
		}
	})
</script>

<style lang="scss" scoped></style>
