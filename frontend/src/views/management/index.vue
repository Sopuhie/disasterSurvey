<!-- 运维管理 -->
<template>
	<div class="management" :class="{ isCollapse: appStore.switchObj.sidebarFlag }">
		<div class="management__nav flex fa">
			<span class="nav-parent">资源管理</span>
			<span class="nav-sep">/</span>
			<span class="nav-current">{{ route.meta?.title || '运维管理' }}</span>
		</div>
		<div class="management__content">
			<router-view></router-view>
		</div>
	</div>
</template>

<script setup>
	import { useRoute } from 'vue-router'
	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()
	const route = useRoute()
</script>

<style lang="scss" scoped>
	.management {
		position: absolute;
		top: 85px;
		left: 290px;
		z-index: 3001;
		width: calc(100% - 300px);
		height: calc(100vh - 95px);
		overflow: hidden;
		border-radius: 7px;
		background: linear-gradient(180deg, #bce4ff 0%, #bce4ff 100%);
		&.isCollapse {
			left: 74px;
			width: calc(100% - 84px);
		}
	}
	.management__nav {
		height: 34px;
		padding: 0 10px;
		font-size: 14px;
		color: #909399;
		& .nav-parent {
			color: #909399;
		}
		& .nav-sep {
			padding: 0 8px;
		}
		& .nav-current {
			color: #303133;
		}
	}
	.management__content {
		height: calc(100% - 34px);
	}
</style>
