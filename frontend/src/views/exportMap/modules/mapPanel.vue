<template>
	<div class="mapPanel max-h">
		<div ref="paper" class="paper-container" :style="exportMapStore.paperProps?.style">
			<template v-if="mapModules.length && !exportMapStore.isPreview">
				<VueDragResize
					v-for="(item, i) in mapModules.filter(map => map.show !== false)"
					parentLimitation
					:parentScaleX="exportMapStore.paperProps?.scale || 1"
					:parentScaleY="exportMapStore.paperProps?.scale || 1"
					:key="item.prop || i"
					:x="item.x"
					:y="item.y"
					:w="item.w"
					:h="item.h"
					:z="item.z"
					:parent="true"
					:isActive="activeProp === item.prop"
					:isResizable="true"
					:preventActiveBehavior="false"
					@activated="activeProp = item.prop"
					@dragging="dragStop($event, item.prop)"
					@resizing="dragStop($event, item.prop)">
					<div
						class="drag-panel noSelect max-wh"
						:class="{ active: activeProp === item.prop }"
						@click.stop="activeProp = item.prop">
						<span class="noSelect cp drag-label" :style="`font-size:${Math.max(item.f || 12, 12)}px;color:${item.color || '#333'};`">
							{{ item.label }}
						</span>
					</div>
				</VueDragResize>
			</template>
			<template v-if="exportMapStore.isPreview">
				<div
					v-for="(item, i) in mapModules"
					:key="i"
					class="pre-panel"
					:style="`font-size:${item.f}px;width:${item.w}px;height:${item.h}px;top:${item.y}px;left:${item.x}px;`">
					<span v-if="item.prop.includes('text')" :style="`color:${item.color}`">{{ item.label }}</span>
					<img
						v-else-if="item.prop === 'mapFactor.compass'"
						style="width: 100%; height: auto"
						:src="item.value"
						alt="" />
					<OLMap
						v-if="item.prop === 'map'"
						ref="map"
						style="width: 100%; height: 100%"
						:getMap="getMapHandler"
						:mapOptions="MapDefaultOption"></OLMap>
				</div>
			</template>
		</div>
	</div>
</template>
<script setup>
	import VueDragResize from 'vue-drag-resize/src'
	import _ from 'lodash'
	import OLMap from '@/components/map/components/Map.vue'
	import { mmToPx, ptToPx } from '@/views/exportMap/tool.js'
	import { paperTypeSize } from '@/views/exportMap/modules/constant.js'
	import { MapDefaultOption } from '/public/map.js'
	import { useExportMapStore } from '@/store/modules/exportMap.js'
	const exportMapStore = useExportMapStore()
	import { useExportStore } from '@/store/modules/export.js'
	const exportStore = useExportStore()
	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	const paper = ref(null)
	const mapModules = ref([])
	const moduleMap = ref({}) // 地图模块
	const activeProp = ref('map')

	const props = defineProps({
		templateProps: {
			type: Object,
			default: () => {}
		}
	})
	watch(
		() => exportMapStore.formData,
		() => {
			changePaperLayout()
		},
		{ deep: true }
	)

	onMounted(() => {
		let paperStyle = {}
		if (props.templateProps) {
			paperStyle = {
				width: props.templateProps.paramJson.paperStyle.width.slice(0, -2),
				height: props.templateProps.paramJson.paperStyle.height.slice(0, -2)
			}
			exportMapStore.setFormData({ type: 'all', data: props.templateProps.paramJson.formData })
		} else {
			const [width, height] = forPaperSize('A4_a')
			paperStyle = {
				width,
				height
			}
			exportMapStore.setFormData({ type: 'other', key: 'thumbnial.show', value: true })
		}
		changePaperStyle(paperStyle)
		changePaperLayout()
	})

	const getMapHandler = map => {
		nextTick(() => {
			exportStore.setOutMap(map)
			let lyrs = mapStore.activeMap?.layers
			if (!lyrs) return
			lyrs.forEach(l => {
				if (l.layer.clone && l.layer.clone instanceof Function) {
					map.lyrManager.addLayer(l.lyrName, l.layer.clone())
				}
			})
		})
	}

	// 设置模块位置
	const changePaperLayout = () => {
		mapModules.value = []
		nextTick(() => {
			mapModules.value = forPaperLayout(exportMapStore.formData)
			let newModuleMap = mapModules.value.find(item => item.prop === 'map')
			if (!newModuleMap) return
			newModuleMap = JSON.parse(JSON.stringify(newModuleMap))
			newModuleMap.w = newModuleMap.w / 3.125
			newModuleMap.h = newModuleMap.h / 3.125
			moduleMap.value = newModuleMap
		})
	}

	function toNum(val, fallback = 0) {
		const n = Number(val)
		return Number.isFinite(n) ? n : fallback
	}

	function forPaperLayout(fData) {
		const { map, text, mapFactor, thumbnial } = fData
		const initFontSize = ptToPx(9)
		let list = []
		const textList = (text?.modules || []).map(item => {
			const { prop, label } = item
			return {
				...text[prop],
				label,
				prop: 'text.' + prop
			}
		})
		const mapFactorList = (mapFactor?.modules || []).map(item => {
			const { prop, label } = item
			return {
				...mapFactor[prop],
				label,
				prop: 'mapFactor.' + prop
			}
		})
		list.push({
			...map,
			prop: 'map',
			label: '地图'
		})
		list.push({
			...thumbnial,
			prop: 'thumbnial',
			label: '缩略图'
		})
		list = list.concat(textList, mapFactorList)

		return list.map(item => {
			const { width, height, x: left, y: top, fontSize, ...rest } = item
			const [w, h, x, y] = mmToPx(toNum(width), toNum(height), toNum(left), toNum(top))
			const f = fontSize ? ptToPx(fontSize) : initFontSize
			return {
				...rest,
				w,
				h,
				x,
				y,
				f
			}
		})
	}
	// 改变纸的样式
	let paperStyleRetry = 0
	const changePaperStyle = paperStyle => {
		nextTick(() => {
			const panel = document.getElementsByClassName('mapPanel')[0]
			const clientHeight = panel?.clientHeight || 0
			const clientWidth = panel?.clientWidth || 0
			const { width, height } = paperStyle
			if (!clientWidth || !clientHeight || !width || !height) {
				if (paperStyleRetry < 20) {
					paperStyleRetry += 1
					setTimeout(() => changePaperStyle(paperStyle), 80)
				}
				return
			}
			paperStyleRetry = 0
			const pad = 24
			const scaleVal = Math.min((clientWidth - pad) / width, (clientHeight - pad) / height, 1)
			exportMapStore.setPaperProps({
				style: {
					width: width + 'px',
					height: height + 'px',
					transform: `translate(-50%, -50%) scale(${scaleVal})`,
					transformOrigin: 'center center'
				},
				scale: scaleVal
			})
		})
	}
	const forPaperSize = val => {
		const { w, h } = paperTypeSize[val]
		return mmToPx(w, h)
	}

	const dragStop = _.debounce(({ left, top, width, height }, keys) => {
		const value = mapModules.value.find(item => item['prop'] === keys)
		exportMapStore.setFormData({ type: 'other', key: keys, value: { ...value, ...getXywh(left, top, width, height) } })
		function getXywh(left, top, w, h) {
			const dpi = 300
			const ratio = 25.4
			const x = Math.round((left * ratio) / dpi)
			const y = Math.round((top * ratio) / dpi)
			const width = Math.round((w * ratio) / dpi)
			const height = Math.round((h * ratio) / dpi)
			return { x, y, width, w: width, height, h: height }
		}
	}, 300)
</script>
<style lang="scss" scoped>
	.mapPanel {
		position: relative;
		width: 75%;
		overflow: hidden;
		background-color: #d8dde3;
	}
	.paper-container {
		background-color: #fff;
		position: absolute;
		top: 50%;
		left: 50%;
		box-shadow: 0 8px 24px rgba(0, 0, 0, 0.16);
	}
	.pre-panel {
		position: absolute;
	}
	.drag-panel {
		box-sizing: border-box;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 100%;
		padding: 4px 8px;
		overflow: hidden;
		background-color: rgba(242, 245, 247, 0.92);
		border: 1px dashed #8fa4b8;
		&.active {
			background-color: rgba(24, 144, 255, 0.12);
			border: 1px solid #1890ff;
		}
	}
	.drag-label {
		max-width: 100%;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		line-height: 1.2;
	}
</style>
