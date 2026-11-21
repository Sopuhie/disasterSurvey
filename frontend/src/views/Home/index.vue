<template>
	<div class="home max-wh" :class="{ isCollapse: appStore.switchObj.sidebarFlag }">
		<div class="mapBox" :style="mapStyle">
			<OLMap
				ref="map"
				style="width: 100%; height: 100%"
				:mapOptions="mapOptions"
				:getMap="initMapHandler"
				v-show="appStore.switchObj.is2D === '2d'">
				<coastalProtectionPopup
					ref="coastalProtectionPopupRef"
					v-if="curSubTabIdx == 'coastalProtectionPopupRef'"
					:selectedFeature="selectedFeature"
					@close="closeConPop" />
				<fishPortPopup
					ref="fishPortPopupRef"
					v-if="curSubTabIdx == 'fishPortPopupRef'"
					:selectedFeature="selectedFeature"
					@close="closeConPop" />
				<mariculturePopup
					ref="mariculturePopupRef"
					v-if="curSubTabIdx == 'mariculturePopupRef'"
					:selectedFeature="selectedFeature"
					@close="closeConPop" />
				<touristAreaPopup
					ref="touristAreaPopupRef"
					v-if="curSubTabIdx == 'touristAreaPopupRef'"
					:selectedFeature="selectedFeature"
					@close="closeConPop" />
			</OLMap>
		</div>
		<div
			ref="cesiumContainer"
			id="cesiumContainer"
			class="3d max-wh flex faj"
			v-if="appStore.switchObj.is2D === '3d'"></div>
		<div class="max-wh">
			<!-- <Theme2></Theme2> -->
			<!-- 属性查询 -->
			<ZDialogCom
				v-if="appStore.switchObj.identify"
				title="属性查询"
				toPath="cancel"
				:footer="false"
				:wrapStyle="wrapStyle"
				:bodyStyle="{ padding: '16px' }"
				@cancel="cancel">
				<SpatialQuery />
			</ZDialogCom>
			<!-- 导入数据管理 -->
			<ImportData v-if="appStore.switchObj.importData"></ImportData>
			<ToolbarCom></ToolbarCom>
			<router-view></router-view>
		</div>
	</div>
</template>

<script setup>
	import { systemName, prefix, tiandituVecTileUrl, tiandituCiaTileUrl } from '@/config'
	// import Theme2 from '@/components/navs/theme2.vue'
	import ToolbarCom from '@/components/toolbar/index.vue'
	import SpatialQuery from '@/components/spatialQuery/index.vue'
	import ImportData from '@/components/importData/index.vue'
	import ZDialogCom from '@/components/dialog/index.vue'
	import OLMap from '@/components/map/components/Map.vue'
	import { buildProjection } from '@/components/map/primitive/util/ProjectionUtil.js'
	import { BASE_LAYER_NAME } from '@/components/map/primitive/config/LayerConstantConfig.js'
	import coastalProtectionPopup from './popup/coastalProtectionPopup.vue'
	import fishPortPopup from './popup/fishPortPopup.vue'
	import mariculturePopup from './popup/mariculturePopup.vue'
	import touristAreaPopup from './popup/touristAreaPopup.vue'
	import { getWindowSize } from '@/utils/index.js'
	import buildLayer, { LayerType } from '@/components/map/primitive/layer/Layer'
	import emitter from '@/components/map/utils/eventbus'
	import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
	import EsriJSONUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()

	import { useSystemStore } from '@/store/modules/system.js'
	const store = useSystemStore()

	import { systemApi } from '@/api/system/index.js'
	const api = systemApi()

	import { themeApi } from '@/api/theme/index.js'
	const themeApiInst = themeApi()

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	const wrapStyle = reactive({
		width: '490px',
		height: `${getWindowSize().h.value - 193}px`,
		top: '95px',
		left: `${getWindowSize().w.value - 510}px`
	})

	const showPage = ref(false)
	const cesiumContainer = ref(null)
	const curSubTabIdx = ref('')

	const selectedFeature = ref(null)
	let mapOptions = ref(null)

	const mapStyle = computed(() => {
		return { width: '100%', height: '100%', borderRadius: '8px', overflow: 'hidden' }
	})

	onMounted(() => {
		// 页面刷新时，关闭属性查询弹窗
		appStore.setSwitchObj('identify', false)
		appStore.setSwitchObj('importData', false)
		appStore.setToolBarSelect('delAll')

		localStorage.setItem('switchObj', JSON.stringify(appStore.switchObj))
		getDictionaryData()
		getExtentConfig()
		getCatalogTreeData()
		window.onresize = () => {
			return (() => {
				wrapStyle.height = `${(getWindowSize().h.value / 5) * 4}px`
				wrapStyle.left = `${getWindowSize().w.value - 440}px`
			})()
		}
	})

	onUnmounted(() => {
		window.onresize = null
	})

	watch(
		() => mapStore.getMapConfig,
		val => {
			if (val) {
				let proj = mapStore.getProjections[mapStore.getMapConfig.code]
				let extent = []
				if (mapStore.getMapConfig?.extent) {
					extent = mapStore.getMapConfig.extent?.split(',').map(Number)
				}
				if (extent?.length) {
					proj?.setExtent(extent)
				}
				mapOptions.value = {
					viewOptions: {
						projection: proj,
						extent: extent.length ? extent : undefined,
						center: mapStore.getMapConfig.center?.split(',').map(Number),
						zoom: mapStore.getMapConfig.zoom,
						maxZoom: mapStore.getMapConfig.maxZoom,
						minZoom: mapStore.getMapConfig.minZoom
					}
				}
			}
		},
		{
			deep: true,
			immediate: true
		}
	)

	emitter.on('mapClick', args => {
		if (args?.length) {
			let feature = GeoJSONUtil.writeFeatureObject(EsriJSONUtil.readFeature(args[0].feature))
			mapStore.activeMap.featureManager.removeFeatures()
			mapStore.activeMap.featureManager.addFeature('Temp_' + Math.floor(Math.random() * 10000), feature, true)
			mapStore.activeMap.flyToManager.flyToRegion(feature)
			let t = setTimeout(() => {
				mapStore.activeMap.featureManager.removeFeatures()
			}, 3 * 1000)
			selectedFeature.value = feature
			switch (args[0].lyrName) {
				case 'coastProtectionLyr':
					curSubTabIdx.value = 'coastalProtectionPopupRef'
					return
				case 'fishPortLyr':
					curSubTabIdx.value = 'fishPortPopupRef'
					return
				case 'maricultureLyr':
					curSubTabIdx.value = 'mariculturePopupRef'
					return
				case 'touristAreaLyr':
					curSubTabIdx.value = 'touristAreaPopupRef'
					return
			}
		}
	})

	/**
	 * 初始化获取地图对象
	 */
	const initMapHandler = map => {
		mapStore.setActiveMap('ONE_MAP', map)
	}
	/**
	 * 默认天地图底图（系统底图未配置或加载失败时回退）
	 */
	const loadFallbackTiandituBasemap = () => {
		const baseLyr = buildLayer(LayerType.Group, {
			name: '天地图底图',
			zIndex: -200,
			layers: [
				{
					type: LayerType.XYZ,
					name: '天地图底图1',
					lyrOption: {
						url: tiandituVecTileUrl,
						zIndex: -200
					}
				},
				{
					type: LayerType.XYZ,
					name: '天地图底图2',
					lyrOption: {
						url: tiandituCiaTileUrl,
						zIndex: -200
					}
				}
			]
		})
		mapStore.activeMap?.lyrManager?.removeLayer(BASE_LAYER_NAME)
		mapStore.activeMap?.lyrManager?.addLayer(BASE_LAYER_NAME, baseLyr)
	}

	/**
	 * 按系统底图专题 id 加载底图
	 */
	const loadBasemapByThemeId = themeId => {
		if (!mapStore.activeMap?.lyrManager) return Promise.reject(new Error('地图未初始化'))
		if (!themeId) {
			loadFallbackTiandituBasemap()
			return Promise.resolve(false)
		}
		return themeApiInst
			.getLayersById({ themeId })
			.then(res => {
				if (res?.data?.length) {
					mapStore.loadBaseMapTheme({ name: BASE_LAYER_NAME, themeId }, res.data)
					return true
				}
				ElMessage.warning('系统底图专题未绑定服务，已使用默认天地图')
				loadFallbackTiandituBasemap()
				return false
			})
			.catch(err => {
				ElMessage.warning(err?.message || '加载系统底图失败，已使用默认天地图')
				loadFallbackTiandituBasemap()
				return false
			})
	}

	/**
	 * 加载行政区划蒙版等矢量图层（与底图无关，只加载一次）
	 */
	const loadAdminBoundaryLayers = () => {
		if (!mapStore.activeMap?.lyrManager) return
		if (mapStore.activeMap.lyrManager.getLayer('行政区划')) return
		const baseLyr1 = buildLayer(LayerType.Group, {
			name: 'GroupLayer',
			zIndex: 99,
			layers: [
				{
					type: LayerType.Vector,
					lyrOption: {
						name: '蒙版',
						url: (prefix ? '/' + prefix : '') + '/datas/蒙版.json',
						lyrStyle: {
							fill: {
								color: 'rgba(0,0,0,0.4)'
							}
						}
					}
				},
				{
					type: LayerType.Vector,
					lyrOption: {
						name: '市界',
						url: (prefix ? '/' + prefix : '') + '/datas/市界.json',
						lyrStyle: {
							stroke: {
								color: 'grey',
								width: 1
							}
						}
					}
				},
				{
					type: LayerType.Vector,
					lyrOption: {
						name: '行政区内陆轮廓',
						url: (prefix ? '/' + prefix : '') + '/datas/行政区内陆轮廓.json',
						lyrStyle: {
							stroke: {
								color: 'grey',
								width: 2
							}
						}
					}
				},
				{
					type: LayerType.Vector,
					lyrOption: {
						name: '行政区划中心点',
						url: (prefix ? '/' + prefix : '') + '/datas/行政区划中心点.json',
						lyrStyle: feature => {
							return {
								image: {
									rotateWithView: true,
									src:
										'data:image/svg+xml;utf8,' +
										encodeURIComponent(
											`<svg t="1735280097920" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="12121" width="12" height="12"><path d="M513.133 65.327c-247.538 0-448.208 200.67-448.208 448.207 0 247.539 200.67 448.208 448.208 448.208 247.54 0 448.208-200.67 448.208-448.208 0-247.539-200.668-448.207-448.208-448.207z m0 837.322c-214.902 0-389.114-174.213-389.114-389.114 0-214.903 174.212-389.115 389.113-389.115 214.904 0 389.116 174.212 389.116 389.115 0 214.901-174.212 389.114-389.116 389.114zM168.53 513.535a336.754 336.754 0 1 0 689.205 0 336.754 336.754 0 1 0-689.205 0z" fill="#e0620d" p-id="12122"></path></svg>`
										),
									imgSize: [30, 30],
									offset: [0, 0]
								},
								text: {
									font: `${'normal'} 15px blue`,
									fill: {
										color: 'white'
									},
									offsetY: 20,
									text: feature.properties.label
								}
							}
						}
					}
				}
			]
		})
		mapStore.activeMap.lyrManager.addLayer('行政区划', baseLyr1)
	}

	/**
	 * 初始化底图信息（优先系统底图配置 mapTheme/imageTheme）
	 */
	const getBaseMapInfo = (themeId, basemapType = 'img') => {
		const tryLoad = (retry = 0) => {
			if (!mapStore.activeMap?.lyrManager) {
				if (retry < 20) {
					setTimeout(() => tryLoad(retry + 1), 200)
				}
				return
			}
			mapStore.setBasemapType(basemapType)
			loadBasemapByThemeId(themeId).finally(() => {
				loadAdminBoundaryLayers()
			})
		}
		setTimeout(() => tryLoad(0), 300)
	}

	/**
	 * 初始化获取资源目录树
	 */
	const getCatalogTreeData = () => {
		const roles = appStore.userInfo?.roles?.map(item => item.roleId).join(',')
		const form = {
			adminFlag: appStore?.userInfo?.adminFlag,
			roles
		}
		api
			.permissionClientQuery(form)
			.then(res => {
				if (res?.data) {
					showPage.value = true
					const data = res.data
					const catalogList = setDisabled(data.catalogList)

					function setDisabled(data) {
						data.forEach(item => {
							item.disabled = item.type === 'folder' ? true : false
							if (item?.childrens?.length > 0) {
								setDisabled(item.childrens)
							}
						})
						return data
					}

					store.setCatalogTreeData(catalogList)
					store.setMenuTreeData(data.navList)
					nextTick(() => {
						getMapInitConfig()
					})
				} else {
					store.setCatalogTreeData([])
					store.setMenuTreeData([])
					ElMessage.error('获取目录树失败')
				}
			})
			.catch(err => {
				store.setCatalogTreeData([])
				store.setMenuTreeData([])
				nextTick(() => {
					getMapInitConfig()
				})
			})
	}

	/**
	 * 初始化获取投影信息数据字典
	 */
	const getDictionaryData = () => {
		api
			.queryDictionary({
				zdlxdm: 'projectionConfig'
			})
			.then(res => {
				if (res?.data?.length) {
					res.data.forEach(projInfo => {
						let proj = buildProjection({ code: projInfo.zdbm, projection: projInfo.zdz })
						mapStore.setProjection(projInfo.zdbm, proj)
					})
				}
			})
			.catch(err => ElMessage.error(err))
	}

	/**
	 * 初始化获取四至配置信息
	 */
	const getExtentConfig = () => {
		api
			.queryDictionary({
				zdlxdm: 'extentConfig'
			})
			.then(res => {
				if (res?.data?.length) {
					res.data.forEach(extInfo => {
						mapStore.setSystemExtent(extInfo.zdbm, JSON.parse(extInfo.zdz))
					})
				}
			})
			.catch(err => ElMessage.error(err))
	}

	/**
	 * 初始化获取地图配置信息
	 */
	const getMapInitConfig = () => {
		api
			.queryMapConfig({
				configName: systemName
			})
			.then(res => {
				if (res?.data) {
					appStore.setAppTitle(res?.data.title)
					mapStore.setMapConfig(res?.data)
					// 默认加载影像底图：影像 ← mapTheme，电子 ← imageTheme
					const themeId = res?.data.mapTheme || res?.data.imageTheme
					const basemapType = res?.data.mapTheme ? 'img' : 'vec'
					getBaseMapInfo(themeId, basemapType)
				} else {
					getBaseMapInfo(null, 'img')
				}
			})
			.catch(err => {
				ElMessage.error(err)
				getBaseMapInfo(null, 'img')
			})
	}

	/**
	 * 属性查询-关闭弹窗
	 */
	const cancel = () => {
		appStore.setSwitchObj('identify', false)
		appStore.setToolBarSelect('delAll')
	}

	const closeConPop = () => {
		curSubTabIdx.value = ''
	}
</script>

<style lang="scss" scoped>
	.home {
		// background: linear-gradient(180deg, #bce4ff 0%, #bce4ff 100%);
		padding: 10px 10px 10px 290px;
		overflow: hidden;
		&.isCollapse {
			padding-left: 74px;
		}
		.ol-perspective-map {
			position: absolute;
			left: 50%;
			top: 50%;
			width: 200%;
			height: 200%;
			-webkit-transform: translate(-50%, -50%);
			transform: translate(-50%, -50%);
		}
		& #cesiumContainer {
			width: 100%;
			height: calc(100% - 56px);
			position: absolute;
			top: 56px;
			left: 0;
		}
		& .pageWrap {
			position: absolute;
			top: 0;
			left: 0;
		}
	}
</style>
