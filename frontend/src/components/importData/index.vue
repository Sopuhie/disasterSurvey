<!-- 导入数据管理 -->
<template>
	<ZDialogCom
		title="数据管理"
		cancelBtnTxt="关闭"
		toPath="cancel"
		:okBtn="false"
		:wrapStyle="importDataStyle"
		:bodyStyle="{ padding: '16px' }"
		@cancel="closeHandler">
		<ul class="importDataTh flex">
			<li></li>
			<li>名称</li>
			<li>图层顺序</li>
			<li>类型</li>
			<li>显隐</li>
			<li>颜色</li>
			<li>操作</li>
		</ul>
		<ul class="importDataTd">
			<li
				class="flex"
				v-for="(item, index) in importDatas"
				:key="'importData_' + index"
				@click.stop="selectedHandler(item)">
				<p @click.stop>
					<el-checkbox v-model="item.check" size="large" />
				</p>
				<p class="pl-8 pr-8">
					<span class="max-wh textEs" style="display: inline-block">{{ item.source }}</span>
				</p>
				<p>
					<span>{{ item.name }}</span>
				</p>
				<p>
					<span>{{ item.geometryType }}</span>
				</p>
				<p @click.stop>
					<el-switch v-model="item.isVisible" active-color="#13ce66" inactive-color="#ff4949"></el-switch>
				</p>
				<p @click.stop>
					<el-color-picker v-model="item.color"></el-color-picker>
				</p>
				<p @click.stop>
					<el-icon @click.stop="removeImportData(item, index)"><Delete /></el-icon>
				</p>
			</li>
		</ul>
		<template #okBtn>
			<el-button type="primary" plain v-btn @click="dataDataHandler">导出</el-button>
			<el-button type="primary" plain v-btn @click="clearDataHandler">清空数据</el-button>
		</template>
	</ZDialogCom>
</template>

<script setup>
	import { saveAs } from 'file-saver'
	import * as XLSX from 'xlsx'
	import ZDialogCom from '@/components/dialog/index.vue'
	import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
	import { IMPORT_LAYER_NAME } from '@/components/map/primitive/config/LayerConstantConfig.js'
	import buildLayer, { LayerType } from '@/components/map/primitive/layer/Layer.js'
	import { buildStyle } from '@/components/map/primitive/style/Style.js'

	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	let vctLyr = null
	const importDatas = ref([])
	const screenHeight = ref(window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight)

	onMounted(() => {
		if (mapStore.activeMap) {
			vctLyr = buildLayer(LayerType.Vector, { name: IMPORT_LAYER_NAME, zIndex: 100 })
			vctLyr.setZIndex(100)
			mapStore.activeMap.layerManager.addLayer(IMPORT_LAYER_NAME, vctLyr)
			renderImportDatas()
		}
	})

	onUnmounted(() => {
		if (mapStore.activeMap) {
			mapStore.activeMap.layerManager.removeLayer(IMPORT_LAYER_NAME)
			vctLyr = null
		}
	})

	const importDataStyle = computed(() => {
		if (appStore.switchObj.importData) {
			return {
				width: '604px',
				height: '400px',
				top: `${screenHeight.value - 442}px`,
				left: '466px'
			}
		} else {
			return {
				width: '604px',
				height: '400px',
				top: `${screenHeight.value - 442}px`,
				left: '466px'
			}
		}
	})

	/**
	 * 监听导入数据
	 */
	watch(
		() => mapStore.getImportDatas,
		val => {
			importDatas.value = val
			// let geometries = mapStore.getImportDatas.filter(feat => feat.check).map(m => m.geometry);
			// if (geometries.length > 0) {
			// 	mapStore.activeMap.featureManager.removeFeatures();
			// 	geometries.forEach(geo => {
			// 		mapStore.activeMap.featureManager.addFeature("FlyTo" + Math.floor(Math.random()*10000), geo, true);
			// 	})
			// }
		},
		{ deep: true, immediate: true }
	)

	/**
	 * 监听数据变化
	 */
	watch(
		() => importDatas,
		() => {
			renderImportDatas()
		},
		{ deep: true, immediate: true }
	)

	/**
	 * 渲染数据
	 */
	function renderImportDatas() {
		if (vctLyr) {
			vctLyr.clear()
		}
		let datas = unref(importDatas)
		if (datas?.length && vctLyr) {
			datas
				.filter(n => n.isVisible)
				.forEach(item => {
					let sourceData = mapStore.getImportDatas.find(n => n.source === item.source)
					if (sourceData) {
						sourceData.color = item.color
					}
					let olFeature = GeoJSONUtil.readFeature(item.geometry)
					olFeature.setStyle(
						buildStyle({
							fill: {
								color: hexToRgb(item.color, 0.4)
							},
							stroke: {
								color: item.color,
								width: 2
							},
							image: {
								rotateWithView: true,
								src:
									'data:image/svg+xml;utf8,' +
									encodeURIComponent(
										`<svg t="1611279325836" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2581" width="48" height="48"><path d="M510.3 71.1c-167 0-302.4 134.7-302.4 300.9s302.4 526.5 302.4 526.5S812.7 538 812.7 371.9 677.3 71.1 510.3 71.1z m0 432.4c-73.1 0-132.3-58.9-132.3-131.6 0-72.7 59.2-131.6 132.3-131.6 73.1 0 132.2 58.9 132.2 131.6 0 72.7-59.1 131.6-132.2 131.6z m0 0" p-id="2582" fill="${item.color}"></path><path d="M644 777.9c-8.2 11.3-16.3 22.2-24.1 32.5C700.6 819.7 756 838.8 756 861c0 31.1-110 56.4-245.6 56.4-135.7 0-245.7-25.3-245.7-56.4 0-22.2 55.4-41.3 136.1-50.6-8.1-10.8-16.2-21.6-24.2-32.5-89.2 15.8-149.8 47-149.8 83 0 51.8 126.9 94 283.5 94s283.4-42.2 283.4-94c0-36-60.6-67.1-149.7-83z m0 0" p-id="2583" fill="${item.color}"></path></svg>`
									),
								imgSize: [48, 48], // 及图标大小
								offset: [0, 0]
							}
						})
					)
					vctLyr.ol.getSource().addFeature(olFeature)
				})
		}
	}

	/**
	 * 定位选择的图斑
	 */
	function selectedHandler(data) {
		if (data.isVisible) {
			if (data.geometry.geometry.type === 'Point') {
				mapStore.activeMap.flyToManager.flyTo(data.geometry.geometry.coordinates, 13)
			} else {
				mapStore.activeMap.flyToManager.flyToRegion(data.geometry)
			}
		}
	}

	/**
	 * 删除指定的图斑
	 */
	function removeImportData(item, index) {
		mapStore.getImportDatas.splice(index, 1)
	}

	/**
	 * 转换十六进制颜色值
	 */
	function hexToRgb(hex, opacity = 1) {
		// 去除#号
		var color = hex.replace('#', '')
		// 分割成红、绿、蓝三部分的16进制字符串
		var red = parseInt(color.substring(0, 2), 16)
		var green = parseInt(color.substring(2, 4), 16)
		var blue = parseInt(color.substring(4, 6), 16)
		return `rgba(${red}, ${green}, ${blue},${opacity})`
	}

	/**
	 * 关闭弹窗
	 */
	const closeHandler = () => {
		appStore.setSwitchObj('importData', false)
	}

	/**
	 * 清空数据
	 */
	function clearDataHandler() {
		mapStore.getImportDatas.length = 0
	}

	/**
	 * 导出数据
	 */
	function dataDataHandler() {
		function exportToExcel(data, fileName) {
			// 将数据转换为工作表
			const worksheet = XLSX.utils.json_to_sheet(data)

			// 创建工作簿并添加工作表
			const workbook = XLSX.utils.book_new()
			XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1')

			// 生成Excel的配置项
			const wbout = XLSX.write(workbook, { bookType: 'xlsx', type: 'binary' })

			// 创建二进制对象并创建url
			const blob = new Blob([s2ab(wbout)], { type: 'application/octet-stream' })
			const url = URL.createObjectURL(blob)

			// 创建a标签模拟点击进行下载
			const a = document.createElement('a')
			if (typeof a.download === 'undefined') {
				window.location = url
			} else {
				a.href = url
				a.download = fileName + '.xlsx'
				document.body.appendChild(a)
				a.click()
				document.body.removeChild(a)
			}

			// 清除对象URL
			setTimeout(function () {
				URL.revokeObjectURL(url)
			}, 100)
		}

		// 转换s2ab_
		function s2ab(s) {
			const buf = new ArrayBuffer(s.length)
			const view = new Uint8Array(buf)
			for (let i = 0; i !== s.length; ++i) view[i] = s.charCodeAt(i) & 0xff
			return buf
		}

		let clone = mapStore.getImportDatas.map(n => {
			let obj = Object.assign({}, n)
			obj.geometry = JSON.stringify(obj.geometry)
			return obj
		})
		exportToExcel(clone, 'users')
	}
</script>

<style lang="scss" scoped>
	.importDataTh {
		border-top: 1px solid var(--el-border-color);
		border-left: 1px solid var(--el-border-color);
		& li {
			width: 12%;
			height: 44px;
			line-height: 44px;
			text-align: center;
			background-color: var(--el-bg-color);
			border-bottom: 1px solid var(--el-border-color);
			border-right: 1px solid var(--el-border-color);
		}
		& li:nth-child(1) {
			width: 55px;
		}
		& li:nth-child(2) {
			width: calc(40% - 55px);
		}
	}
	.importDataTd {
		border-left: 1px solid var(--el-border-color);
		li {
			height: 44px;
			line-height: 44px;
			border-bottom: 1px solid var(--el-border-color);
			& p {
				width: 12%;
				height: 44px;
				line-height: 44px;
				text-align: center;
				color: var(--el-text-color-regular);
				border-right: 1px solid var(--el-border-color);
				&:nth-child(1) {
					width: 55px;
				}
				&:nth-child(2) {
					width: calc(40% - 55px);
					text-align: left;
				}
			}
		}
	}
</style>
