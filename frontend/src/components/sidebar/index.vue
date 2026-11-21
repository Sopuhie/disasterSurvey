<template>
	<transition name="fade" mode="out-in">
		<div
			class="sidebar noSelect"
			v-show="appStore.switchObj.sidebarSwitch && appStore.switchObj.sidebarShow"
			:style="panelStyle">
			<div class="sidebarPanel">
				<div class="closeBtn flex fa jsb">
					<span>资源目录</span>
					<el-icon size="16" class="cp" @click="mapStore.showMenuTree = false"><CloseBold /></el-icon>
				</div>
				<div class="sidebarBody">
					<CompA></CompA>
				</div>
			</div>
		</div>
	</transition>
</template>

<script setup>
	import CompA from './CompA.vue'
	import CompB from './CompB.vue'

	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()
	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()
	const navWidth = computed(() => (appStore.switchObj.sidebarFlag ? 64 : 280))
	const panelStyle = computed(() => ({
		left: `${navWidth.value + 12}px`,
		top: '95px',
		width: '340px',
		height: 'calc(100% - 151px)'
	}))

	const lists = shallowRef([
		{
			name: '数据资源',
			icon: 'icon-gsd-icon-line-universal-treeselect',
			component: CompA,
			children: []
		},
		{
			name: '功能模块',
			icon: 'icon-map',
			component: CompB,
			children: []
		},
		{
			name: '基础服务',
			icon: 'icon-chazhao',
			component: null,
			children: [
				{ name: '空间分析', path: '/build-enquiry' },
				{ name: '统计报表', path: '/statistics-report' }
			]
		}
	])
	const visible = ref(false)

	const curIndex = ref(0)
	const itemData = ref(null)

	onMounted(() => {
		itemData.value = lists[0]
		document.addEventListener('click', closeMenu)
	})
	onUnmounted(() => {
		document.removeEventListener('click', closeMenu)
	})

	const closeMenu = el => {
		const className = el.target?.className
		if (className && typeof className === 'string' && className.indexOf('childItem') !== -1) {
			visible.value = false
		}
		if (curIndex.value === 2) {
			if (
				className.indexOf('sidebarItem child') !== -1 ||
				className.indexOf('childItem_wrap') !== -1 ||
				className.indexOf('sidebarName') !== -1 ||
				className.indexOf('iconfont icon-chazhao') !== -1
			) {
				visible.value = true
			} else {
				visible.value = false
			}
		} else {
			visible.value = false
		}
	}
</script>

<style lang="scss" scoped>
	.sidebar {
		position: fixed;
		z-index: 2025;
		overflow: hidden;
		border-radius: 8px;
		background: #fff;
		box-shadow: 0 10px 28px rgba(15, 35, 68, 0.14);
	}

	.sidebarPanel {
		display: flex;
		flex-direction: column;
		height: 100%;
		overflow: hidden;

		&::before {
			content: '';
			flex-shrink: 0;
			height: 3px;
			background: linear-gradient(90deg, #1677ff 0%, #36cfc9 100%);
		}

		& .closeBtn {
			flex-shrink: 0;
			padding: 14px 16px 12px;
			border-bottom: 1px solid #eef2f6;
			span {
				font-size: 16px;
				font-weight: 600;
				color: #1f2d3d;
			}
		}

		& .sidebarBody {
			flex: 1;
			min-height: 0;
			overflow: hidden;
			:deep(.comResourceCatalog) {
				height: 100%;
			}
		}
	}
	.fadeIn-enter-active,
	.fadeIn-leave-active {
		transition: opacity 0.5s ease-in-out;
	}

	.fadeIn-enter,
	.fadeIn-leave-to {
		opacity: 0;
	}
</style>
