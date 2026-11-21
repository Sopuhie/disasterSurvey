<!-- 省域监测系统 -->
<template>
	<SidebarCom v-if="mapStore.showMenuTree"></SidebarCom>
</template>

<script setup>
	import SidebarCom from '@/components/sidebar/index.vue'
	import { useRouteHistory } from '@/utils/routeHistory.js'
	const { getPreviousRoute } = useRouteHistory()
	const previousRoute = getPreviousRoute()

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	import { useSystemStore } from '@/store/modules/system.js'
	const store = useSystemStore()

	onMounted(() => {
		if (mapStore.activeMap && !store.excludePaths.includes(previousRoute.path)) {
		}
	})
</script>

<style lang="scss" scoped></style>
