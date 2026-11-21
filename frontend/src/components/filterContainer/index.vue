<template>
	<div class="filterContainer flex jsb p-10">
		<div class="flex fa fw">
			<slot name="left"></slot>
		</div>
		<div class="flex fa fw">
			<slot name="right"></slot>
		</div>
	</div>
</template>

<script setup></script>

<style lang="scss" scoped>
	.filterContainer {
		align-items: flex-start;
		height: 64px;
		border-radius: 8px;
		overflow: hidden;
		background-color: var(--el-bg-color);
	}
</style>
