<!-- 三维监测系统 -->
<template>
	<div class="pFixed noPage"></div>
</template>

<script setup>
	import * as turf from '@turf/turf'
	import { useRouteHistory } from '@/utils/routeHistory.js'
	const { getPreviousRoute } = useRouteHistory()
	const previousRoute = getPreviousRoute()

	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	import { useSystemStore } from '@/store/modules/system.js'
	const store = useSystemStore()

	onMounted(() => {
		if (mapStore.activeMap && !store.excludePaths.includes(previousRoute.path)) {
			appStore.setSwitchObj('is2D', '3d')
		}
	})
</script>

<style lang="scss" scoped></style>
