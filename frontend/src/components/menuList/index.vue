<template>
	<div class="menuList max-wh pt-8">
		<p class="title flex pl-8 pr-8 mb-8" :class="appStore.switchObj.sidebarFlag ? 'faj' : 'fa jsb'">
			<span v-show="!appStore.switchObj.sidebarFlag">{{ appTitleEn }}</span>
			<el-icon
				class="collapse-btn cp"
				size="18"
				:title="appStore.switchObj.sidebarFlag ? '展开菜单' : '收起菜单'"
				@click="changeCollapse">
				<Expand v-if="appStore.switchObj.sidebarFlag" />
				<Fold v-else />
			</el-icon>
		</p>
		<el-menu
			router
			:collapse-transition="false"
			:default-active="routePath"
			class="el-menu-vertical-demo oy"
			style="height: calc(100vh - 48px)"
			:collapse="appStore.switchObj.sidebarFlag"
			:popper-offset="-2"
			@select="handleSelect">
			<menuItem :data="menuList" :routePath="routePath"></menuItem>
		</el-menu>
		<div
			class="menuTree flex fa h-80 cp noSelect"
			v-show="!appStore.switchObj.sidebarFlag"
			@click="mapStore.showMenuTree = true">
			<div class="p-8 flex fa">
				<span style="color: #1890ff; font-size: 14px; font-family: Microsoft YaHei; font-weight: bold"
					>资源目录设置</span
				>
				<span
					class="flex faj pl-12 pr-12 ml-8"
					style="color: white; height: 18px; border-radius: 88px; background: #f52530"
					>{{ mapStore.checkedKeys.length }}</span
				>
			</div>
		</div>
	</div>
</template>

<script setup>
	import menuItem from './menuItem.vue'
	import { appTitleEn } from '@/config'
	import { Fold, Expand } from '@element-plus/icons-vue'

	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()
	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	import { useRouter, useRoute } from 'vue-router'
	const router = useRouter()

	const route = useRoute()
	const routePath = computed(() => route.path)

	const props = defineProps({
		menuList: {
			type: Array,
			default: () => []
		}
	})

	const handleSelect = path => {
		router.push(path)
	}

	const changeCollapse = () => {
		appStore.setSwitchObj('sidebarFlag', !appStore.switchObj.sidebarFlag)
	}
</script>

<style lang="scss" scoped>
	.menuList {
		background-image: url('@/assets/imgs/menuListBg.png');
		background-size: cover;
		background-position: center;
		background-repeat: no-repeat;
		& .title {
			height: 40px;
			color: #303133;
		}
		& .collapse-btn {
			flex-shrink: 0;
			color: #606266;
			&:hover {
				color: #1890ff;
			}
		}
		& .menuTree {
			margin: 16px 10px;
			background-image: url('@/assets/imgs/menuTree.png');
			background-repeat: no-repeat;
			background-size: 100% 100%;
			background-position: center;
		}
	}
	.el-menu-vertical-demo {
		height: calc(100% - 202px) !important;
	}
</style>
