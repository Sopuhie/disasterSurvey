<template>
	<el-dialog title="缓冲区分析参数设置" v-model="showBuffer" width="40%" append-to-body>
		<FormCom v-if="formData" :formData="formData" :formMode="formMode"></FormCom>
		<template #footer>
			<div class="dialog-footer">
				<el-button v-btn type="primary" plain @click="showBuffer = false"> 取消 </el-button>
				<el-button v-btn type="primary" plain @click="showBuffer = false"> 确定 </el-button>
			</div>
		</template>
	</el-dialog>
	<ZDialogCom
		title="空间分析"
		toPath="cancel"
		showSwitch
		:wrapStyle="wrapStyle"
		:bodyStyle="{ padding: '24px' }"
		@cancel="cancel">
		<div class="p-16 max-wh">
			<p style="color: var(--el-text-color-regular)">选择空间分析工具:</p>
			<el-radio-group v-model="tool" @change="handleToolChange">
				<el-row style="margin-right: 16px; width: 116px" v-for="{ label, value } in toolList" :key="value">
					<el-col :span="24">
						<el-radio :label="value">
							{{ label }}
							<el-popover trigger="hover" placement="right" :width="420">
								<template #reference>
									<el-icon><QuestionFilled /></el-icon>
								</template>
								<template #default>
									<div>
										<img :src="helpImage(value)" class="helpImage" />
									</div>
								</template>
							</el-popover>
						</el-radio>
					</el-col>
				</el-row>
			</el-radio-group>
			<el-button v-btn type="primary" plain @click="submit" style="float: right; width: 60px"> 计算 </el-button>
		</div>
	</ZDialogCom>
</template>

<script setup>
	import ZDialogCom from '@/components/dialog/index.vue'
	import FormCom from '@/components/form/index.vue'
	import { intersect, union, difference, buffer, nearestPointOnLine } from '@turf/turf'
	import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
	import { useMapStore } from '@/store/modules/map.js'
	import { getWindowSize } from '@/utils/index.js'
	const mapStore = useMapStore()

	const screenWidth = ref(window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth)
	const screenHeight = ref(window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight)
	const wrapStyle = reactive({
		width: '360px',
		height: `${getWindowSize().h.value - 212}px`,
		top: '116px',
		left: `${getWindowSize().w.value - 420}px`
	})

	const bufferData = {
		key: 'formScheme-test',
		showFormBtn: false,
		formBtn: [],
		attrs: {
			class: 'mt12',
			size: 'default',
			labelPosition: 'right',
			labelWidth: 'auto',
			disabled: false
		},
		fields: [
			{
				span: 24,
				children: [
					{
						type: 'input',
						label: '距离',
						prop: 'value',
						childAttrs: {
							type: 'number',
							placeholder: '请输入资源名称',
							clearable: true,
							readonly: false,
							disabled: false
						}
					}
				]
			},
			{
				span: 24,
				children: [
					{
						type: 'select',
						label: '单位',
						prop: 'unit',
						childAttrs: {
							type: 'text',
							placeholder: '请输入资源名称',
							clearable: true,
							readonly: false,
							disabled: false
						},
						options: [
							{ label: '米', value: 'meters' },
							{ label: '千米', value: 'kilometers' },
							{ label: '度', value: 'degrees' }
						]
					}
				]
			}
		]
	}

	const tool = ref('intersect')
	const showBuffer = ref(false)
	const formData = ref(null)
	const formMode = ref({
		value: 1000,
		unit: 'meters'
	})

	const toolList = reactive([
		{ label: '交集(相交)', value: 'intersect' },
		{ label: '并集(合并)', value: 'union' },
		{ label: '补集(擦除)', value: 'erase' },
		{ label: '相交取反', value: 'symDiff' },
		{ label: '缓冲区', value: 'buffer' },
		{ label: '连通性分析', value: 'connectivity' }
	])

	watch(
		() => screenWidth,
		val => {
			screenWidth.value = val
		}
	)

	onMounted(() => {
		window.onresize = () => {
			return (() => {
				screenWidth.value = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth
				screenHeight.value = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight
				wrapStyle.height = `${screenHeight.value - 80}px`
				wrapStyle.left = `${screenWidth.value - 360}px`
			})()
		}
		formData.value = bufferData
	})

	onUnmounted(() => {
		window.onresize = null
	})

	const emits = defineEmits(['cancel'])
	function cancel() {
		emits('cancel')
	}

	function handleToolChange(value) {
		tool.value = value
		showBuffer.value = value === 'buffer'
	}

	// 分析工具提示图片
	function helpImage(image) {
		return new URL(`./imgs/${image}.png`, import.meta.url).href
	}

	// 计算
	function submit() {
		if (mapStore.activeMap && tool.value) {
			switch (tool.value) {
				case 'intersect':
					intersectHandler()
					return
				case 'union':
					unionHandler()
					return
				case 'erase':
					eraseHandler()
					return
				case 'symDiff':
					differenceHandler()
					return
				case 'buffer':
					bufferHandler()
					return
				case 'connectivity':
					connectivityHandler()
					return
			}
		}
	}

	const connectivityHandler = () => {
		// let line = {
		// 	type: 'Feature',
		// 	geometry: {
		// 		type: 'LineString',
		// 		coordinates: [
		// 			[119.39310009024655, 26.08876066813326],
		// 			[119.38930253515787, 26.041884109508388],
		// 			[119.34016709483495, 25.99906519033921],
		// 			[119.29298780512357, 26.025473364186453],
		// 			[119.2674222945609, 26.03716267394023],
		// 			[119.25730798316278, 26.051878333184305]
		// 		]
		// 	}
		// }
		// let pt = {
		// 	type: 'Feature',
		// 	geometry: {
		// 		type: 'Point',
		// 		coordinates: [119.32908965450622, 26.067888962404297]
		// 	}
		// }
		// var snapped = nearestPointOnLine(line, pt, { units: 'meters' })

		if (mapStore.activeMap) {
			if (mapStore.getImportDatas?.length >= 2) {
				let points = mapStore.getImportDatas
					.filter(feat => feat.check && feat.geometry.geometry.type == 'Point')
					.map(m => m.geometry)


				fetch(`http://api.tianditu.gov.cn/drive?postStr={"orig":"${points[0].geometry.coordinates.toString()}","dest":"${points[1].geometry.coordinates.toString()}","style":"0"}&type=search&tk=851e3bacf2a59e3179a5cb71e8d6440e`).then((response) => {
					return response.text()
				})
				.then((data) => {
					const parser = new DOMParser();
					const xmlDoc = parser.parseFromString(data, 'text/xml');
					const items = xmlDoc.querySelectorAll('routelatlon');
					let coords = items[0].textContent.split(";").filter(Boolean);
					let feat = {
						type: 'Feature',
						geometry: {
							type: 'LineString',
							coordinates: coords.map(m => {
								return m.split(",").map(Number)
							})
						}
					}

					mapStore.activeMap.featureManager.removeFeatures()
					mapStore.activeMap.featureManager.addFeature('Temp_' + Math.floor(Math.random() * 10000), feat)
					// mapStore.activeMap.flyToManager.flyTo(snapped.geometry.coordinates, 13)
				});
			} else {
				ElMessage({
					showClose: true,
					message: `请使用绘制工具绘制 ${count} 个点`,
					type: 'warning'
				})
				return []
			}
		}
	}

	const intersectHandler = () => {
		let feats = getDrawFeatures()
		if (feats?.length == 2) {
			let intersection = intersect(feats[0], feats[1])
			flashPolygon(intersection)
		}
	}

	const unionHandler = () => {
		let feats = getDrawFeatures()
		if (feats?.length == 2) {
			let unionFeats = union(feats[0], feats[1])
			flashPolygon(unionFeats)
		}
	}

	const eraseHandler = () => {
		let feats = getDrawFeatures()
		if (feats?.length == 2) {
			let diffFeats = difference(feats[0], feats[1])
			flashPolygon(diffFeats)
		}
	}

	const bufferHandler = () => {
		let feats = getDrawFeatures(1)
		if (feats?.length == 1) {
			let feature = GeoJSONUtil.readFeature(feats[0], {
				dataProjection: mapStore.activeMap.projection,
				featureProjection: 'EPSG:4326'
			})
			let convertFeat = GeoJSONUtil.writeFeatureObject(feature)
			let bufferFeats = buffer(convertFeat, formMode.value.value, { units: formMode.value.unit, steps: 64 }) // "meters" | "millimeters" | "centimeters" | "kilometers" | "acres" | "miles" | "nauticalmiles" | "inches" | "yards" | "feet" | "radians" | "degrees" | "hectares";
			let feature2 = GeoJSONUtil.readFeature(bufferFeats, {
				dataProjection: 'EPSG:4326',
				featureProjection: mapStore.activeMap.projection
			})
			let convertFeat2 = GeoJSONUtil.writeFeatureObject(feature2)
			flashPolygon(convertFeat2)
		}
	}

	const differenceHandler = () => {
		let feats = getDrawFeatures()
		if (feats?.length == 2) {
			let unionFeats = union(feats[0], feats[1])
			let intersection = intersect(feats[0], feats[1])
			let diffFeats = difference(unionFeats, intersection)
			flashPolygon(diffFeats)
		}
	}

	function getDrawFeatures(count = 2) {
		if (mapStore.activeMap) {
			if (mapStore.getImportDatas?.length >= count) {
				let polygons = mapStore.getImportDatas
					.filter(feat => feat.check && feat.geometry.geometry.type == 'Polygon')
					.map(m => m.geometry)
				if (tool.value == 'buffer') {
					polygons = mapStore.getImportDatas.filter(feat => feat.check).map(m => m.geometry)
				}
				return polygons?.length >= count ? polygons.slice(0, count) : []
			} else {
				ElMessage({
					showClose: true,
					message: `请使用绘制工具绘制 ${count} 个面图形`,
					type: 'warning'
				})
				return []
			}
		}
	}

	function flashPolygon(polygon) {
		if (polygon) {
			mapStore.activeMap.featureManager.removeFeatures()
			mapStore.activeMap.flyToManager.flyToRegion(polygon, true)
		}
	}
</script>

<style lang="scss" scoped>
	.helpImage {
		display: block;
		max-width: 400px;
	}
</style>
