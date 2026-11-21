<template>
	<div class="logo flex fa ml-24">{{ appStore.appTitle }}</div>
</template>

<script setup>
	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()
</script>

<style lang="scss" scoped>
	.logo {
		position: fixed;
		top: 0;
		left: 0;
		z-index: 2024;
		// width: 278px;
		height: 56px;
		font-size: 18px;
		font-weight: 600;
		padding-left: 44px;
		font-family: PingFangSC-Semibold, PingFang SC;
		color: #1a1b1f;
		background-image: url(@/assets/images/logo-background.png);
		background-size: 278px 40px;
		background-repeat: no-repeat;
		background-position: left center;
	}
</style>
