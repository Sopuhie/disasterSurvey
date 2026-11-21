<!-- 附件管理 - drawer 壳（与专题制图一致） -->
<template>
	<el-drawer
		v-model="drawerVisible"
		class="annex-drawer"
		direction="rtl"
		size="calc(100% - 160px)"
		:with-header="false"
		:modal="true"
		:append-to-body="true"
		:close-on-click-modal="true"
		:close-on-press-escape="true"
		:destroy-on-close="false"
		:z-index="2800"
		@opened="onDrawerOpened"
		@closed="onDrawerClosed">
		<div class="annex-drawer-panel">
			<div class="annex-drawer-head zDialog__header noSelect flex fa jsb">
				<span class="title">附件管理</span>
				<el-icon class="cp" size="20" @click="closeDrawer"><Close /></el-icon>
			</div>
			<div class="annex-drawer-body">
				<AnnexCom ref="annexRef" />
			</div>
		</div>
	</el-drawer>
</template>

<script setup>
	import { Close } from '@element-plus/icons-vue'
	import AnnexCom from '@/views/management/annex.vue'

	const emits = defineEmits(['cancel'])
	const drawerVisible = ref(true)
	const annexRef = ref(null)

	const closeDrawer = () => {
		drawerVisible.value = false
	}

	const onDrawerOpened = () => {
		nextTick(() => annexRef.value?.syncTableHeight?.())
	}

	const onDrawerClosed = () => {
		document.body.classList.remove('annex-drawer-open')
		emits('cancel')
	}

	onMounted(() => {
		document.body.classList.add('annex-drawer-open')
	})

	onUnmounted(() => {
		document.body.classList.remove('annex-drawer-open')
	})
</script>

<style lang="scss" scoped>
	.annex-drawer-panel {
		display: flex;
		flex-direction: column;
		width: 100%;
		height: 100%;
		overflow: hidden;
		background: #f0f2f5;
	}
	.annex-drawer-head {
		flex-shrink: 0;
		height: 48px;
		padding: 0 16px;
		color: #fff;
		background-color: #3c7dd7;
		& .title {
			font-weight: bold;
			font-size: 16px;
		}
	}
	.annex-drawer-body {
		flex: 1;
		min-height: 0;
		height: 0; /* 配合 flex:1，确保子级百分比高度可计算 */
		padding: 12px;
		overflow: hidden;
		:deep(.serviceServe) {
			width: 100%;
			height: 100%;
		}
	}
</style>

<style lang="scss">
	.annex-drawer.el-drawer {
		z-index: 2800 !important;
		height: 100% !important;
		top: 0 !important;
		bottom: 0 !important;
		box-shadow: -4px 0 24px rgba(0, 0, 0, 0.18);
	}
	.el-overlay:has(.annex-drawer),
	.el-overlay.annex-drawer-overlay {
		z-index: 2800 !important;
	}
	.annex-drawer .el-drawer__body {
		padding: 0;
		height: 100%;
		overflow: hidden;
	}
	/* 附件管理内嵌弹窗需高于 drawer 2800 */
	body.annex-drawer-open .el-overlay:has(.el-dialog),
	body.annex-drawer-open .el-dialog {
		z-index: 3000 !important;
	}
</style>
