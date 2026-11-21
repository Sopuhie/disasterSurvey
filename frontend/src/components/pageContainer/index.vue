<template>
	<div
		class="pageContainer"
		:style="{ height: `calc(100% - ${height}px)` }"
		v-loading="loading"
		:element-loading-text="elLoadingText"
		:element-loading-background="elLoadingBg">
		<slot></slot>
	</div>
</template>

<script setup>
	const props = defineProps({
		height: {
			type: String,
			default: '80'
		},
		loading: {
			type: Boolean,
			default: false
		},
		elLoadingText: {
			type: String,
			default: '加载中....'
		},
		elLoadingBg: {
			type: String,
			default: 'rgba(122, 122, 122, 0.4)'
		}
	})
</script>

<style lang="scss" scoped>
	.pageContainer {
		overflow: hidden;
		padding: 16px 16px 0;
		border-radius: 8px;
		background-color: var(--el-bg-color);
	}
</style>
