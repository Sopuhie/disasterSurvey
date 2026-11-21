import { defineStore } from 'pinia'
import buildLayer, { LayerType } from '../../components/map/primitive/layer/Layer'
import { ViewMode } from '@/components/map/primitive/control/SplitScreen'
import { BASE_LAYER_NAME } from '@/components/map/primitive/config/LayerConstantConfig.js'
import { transfromGeojson, transfrom } from '@/components/map/primitive/util/ProjectionUtil.js'
import * as turf from '@turf/turf'

export const useMapStore = defineStore('map', {
	state: () => ({
		showMenuTree: false,
		mapname: 'ONE_MAP', // 无论如何都会存在
		activemap: null,
		loadedthemes: {}, // { "onemap", [{ themeInfo: object, serviceInfo: object }] },
		checkedKeys: [],
		basemaptheme: {},
		basemapType: 'img', // 当前底图类型：vec 电子 / img 影像（初始化默认影像）
		projections: {}, // 读取配置的投影信息
		scales: [], // 读取配置的比例尺信息
		mapConfig: null, // 地图初始化配置信息,
		importDatas: [], // 导入数据集合 [{ source: string, geometry: geojson }]
		parseconfig: null, // 导入数据配置信息,
		systemextents: {}, // 配置三个子系统的四至
		loadlyrnames: ['coastProtectionLyr', 'fishPortLyr', 'maricultureLyr', 'touristAreaLyr']
	}),
	actions: {
		setMenuTreeStatus(status) {
			this.showMenuTree = status
		},
		setParseConfig(config) {
			this.parseconfig = config
		},
		/**
		 * 存储解析数据信息
		 */
		setImportDatas(datas) {
			if (Array.isArray(datas)) {
				this.importDatas = datas
			} else {
				this.importDatas = this.importDatas.concat(datas)
			}
		},
		/**
		 * 存储地图初始化配置信息
		 */
		setMapConfig(config) {
			this.mapConfig = config
		},
		/**
		 * 存储投影信息
		 */
		setProjection(name, proj) {
			this.projections[name] = proj
		},
		/**
		 * 存储三个系统的四至信息
		 */
		setSystemExtent(name, extent) {
			this.systemextents[name] = extent
		},
		/**
		 * 存储比例尺信息
		 */
		setScale(label, value) {
			this.scales.push({ label, value })
		},
		/**
		 * 资源目录勾选的值
		 */
		setCheckedKeys(checkedKeys) {
			this.checkedKeys = checkedKeys
		},
		/**
		 * 初始化设置地图对象
		 */
		setActiveMap(name, map) {
			this.activemap = map
			this.mapname = name
		},
		/**
		 * 当前底图类型 vec/img
		 */
		setBasemapType(type) {
			if (type === 'vec' || type === 'img') {
				this.basemapType = type
			}
		},
		/**
		 * 加载底图
		 */
		loadBaseMapTheme(themeInfo, serviceInfo) {
			if (!this.activemap?.lyrManager) {
				throw new Error('地图未初始化')
			}
			this.activemap.lyrManager.removeLayer(BASE_LAYER_NAME)
			this.addThemeLyrsToMap(BASE_LAYER_NAME, serviceInfo)
			this.basemaptheme = {
				themeInfo,
				serviceInfo
			}
		},
		/**
		 * 加载专题
		 */
		loadTheme(themeInfo, serviceInfo) {
			this.addThemeLyrsToMap(themeInfo.name, serviceInfo)
			this.saveLoadThemeInfo(themeInfo, serviceInfo)
		},
		/**
		 * 移除专题
		 */
		unLoadTheme(themeInfo) {
			if (this.loadedthemes[this.mapname]?.find(m => m.themeInfo.themeId == themeInfo.themeId)) {
				this.activemap.lyrManager.removeLayer(themeInfo.name)
				this.loadedthemes[this.mapname] = this.loadedthemes[this.mapname]?.filter(
					m => m.themeInfo.themeId != themeInfo.themeId
				)
			} else {
				throw new Error('专题不存在')
			}
		},
		/**
		 * 加载专题图层
		 */
		addThemeLyrsToMap(themeName, lyrInfos) {
			const isBasemap = themeName == BASE_LAYER_NAME
			let lyrOption = {
				name: themeName,
				layers: lyrInfos.map(m => {
					const serviceType = String(m.serviceType || '').toLowerCase()
					return {
						type: serviceType,
						lyrOption: {
							url: m.serviceUrl,
							name: m.serviceName,
							...(this.metaDataDeserialized(m.metaData) || null)
						}
					}
				})
			}
			// 判断若是底图，则设置最底部
			if (isBasemap) {
				lyrOption.zIndex = -200
				lyrOption.layers.forEach(lyr => {
					lyr.lyrOption.zIndex = -200
				})
			}
			this.activemap.lyrManager.removeLayer(themeName)
			this.activemap.lyrManager.addLayer(themeName, buildLayer(LayerType.Group, lyrOption))
			// 切换底图不改变当前视野
			if (!isBasemap) {
				this.flyToExtentOrCenter(lyrOption)
			}
		},
		/**
		 * 飞跃到四至或中心点
		 */
		flyToExtentOrCenter(lyrOption) {
			if (lyrOption?.layers?.length) {
				let exts = lyrOption.layers
					.map(lyr => {
						if (lyr.lyrOption?.extent) {
							return {
								projection: lyr.lyrOption.projection || this.activemap.projection,
								extent: lyr.lyrOption?.extent
							}
						} else {
							return null
						}
					})
					.filter(Boolean)
				if (exts?.length) {
					let geojson = turf.bboxPolygon(exts[0].extent)
					let cvtGeo = transfromGeojson(geojson, exts[0].projection, this.activemap.projection)
					this.activemap.flyToManager.flyToRegion(cvtGeo)
				} else {
					let points = lyrOption.layers
						.map(lyr => {
							if (lyr.lyrOption?.center) {
								return {
									projection: lyr.lyrOption.projection || this.activemap.projection,
									center: lyr.lyrOption?.center
								}
							} else {
								return null
							}
						})
						.filter(Boolean)
					if (points?.length) {
						let cent = transfrom(points[0].center, points[0].projection, this.activemap.projection)
						this.activemap.flyToManager.flyTo(cent, 13)
					}
				}
			}
		},
		/**
		 * 存储专题
		 */
		saveLoadThemeInfo(themeInfo, serviceInfo) {
			if (serviceInfo?.length) {
				serviceInfo.forEach(item => (item.opacity = 100))
			}
			if (this.loadedthemes[this.mapname]) {
				this.loadedthemes[this.mapname].push({
					themeInfo,
					serviceInfo
				})
			} else {
				this.loadedthemes[this.mapname] = [
					{
						themeInfo,
						serviceInfo
					}
				]
			}
		},
		/**
		 * 反序列化配置参数
		 */
		metaDataDeserialized(metas) {
			let metadata = {
				projection: this.activemap.projection,
				zIndex: -100
			}
			if (metas) {
				metadata = Object.assign(metadata, JSON.parse(metas).metadata)
				// 反序列化数组
				if (metadata?.layers?.length) {
					try {
						metadata.layers = JSON.parse(metadata.layers)
					} catch (error) {
						metadata.layers = metadata?.layers
					}
				}
				if (metadata?.zIndex) {
					metadata.zIndex = parseInt(metadata.zIndex)
				}
				if (metadata?.extent?.length) {
					metadata.extent = JSON.parse(metadata.extent)
				}
				if (metadata?.zooms?.length) {
					metadata.zooms = JSON.parse(metadata.zooms)
				}
				if (metadata?.center?.length) {
					metadata.center = JSON.parse(metadata.center)
				}
				if (metadata?.origin?.length) {
					metadata.origin = JSON.parse(metadata.origin)
				}
				if (metadata?.resolutions?.length) {
					metadata.resolutions = JSON.parse(metadata.resolutions)
				}
			}
			return metadata
		},
		/**
		 * 根据地图模式复制专题信息
		 */
		setMapMode(mapMode) {
			let originData = this.loadedthemes['ONE_MAP'] || []
			for (let [key, val] of Object.entries(this.loadedthemes)) {
				if (key != 'ONE_MAP') {
					delete this.loadedthemes[key] // 移除其它分屏数据
				}
			}
			switch (mapMode) {
				case ViewMode.ONE_MAP:
					let themeNames = this.loadedthemes['ONE_MAP'].map(info => info.themeInfo.name)
					let themeLoadNames = this.activemap.lyrManager.innerLayers.map(lyr => lyr.lyrName)
					themeLoadNames.forEach(lyrName => {
						if (lyrName != BASE_LAYER_NAME && !themeNames.includes(lyrName)) {
							this.activemap.lyrManager.removeLayer(lyrName)
						}
					})
					themeNames.forEach(lyrName => {
						if (lyrName != BASE_LAYER_NAME && !themeLoadNames.includes(lyrName)) {
							let info = this.loadedthemes['ONE_MAP'].find(m => m.themeInfo.name == lyrName)
							this.addThemeLyrsToMap(info.themeInfo.name, info.serviceInfo)
						}
					})
					break
				case ViewMode.DOUBLE_MAP:
					this.setSplitScreenLoadThemes(['TWO_MAP'], originData)
					break
				case ViewMode.THREE_MAP:
					this.setSplitScreenLoadThemes(['TWO_MAP', 'THREE_MAP'], originData)
					break
				case ViewMode.FOUR_MAP:
					this.setSplitScreenLoadThemes(['TWO_MAP', 'THREE_MAP', 'FOUR_MAP'], originData)
					break
				case ViewMode.SIX_MAP:
					this.setSplitScreenLoadThemes(['TWO_MAP', 'THREE_MAP', 'FOUR_MAP', 'FIVE_MAP', 'SIX_MAP'], originData)
					break
			}
		},
		/**
		 * 设置分屏专题目录树
		 */
		setSplitScreenLoadThemes(screens, originData) {
			screens.forEach(m => {
				this.loadedthemes[m] = JSON.parse(JSON.stringify(originData))
			})
		}
	},
	getters: {
		mapName() {
			return this.mapname
		},
		activeMap() {
			return this.activemap
		},
		baseMapTheme() {
			return this.basemaptheme
		},
		getBasemapType() {
			return this.basemapType
		},
		loadedThemes() {
			return this.loadedthemes[this.mapname] || []
		},
		getProjections() {
			return this.projections || null
		},
		getScales() {
			return this.scales
		},
		getMapConfig() {
			return this.mapConfig
		},
		getImportDatas() {
			return this.importDatas
		},
		getSystemExtents() {
			return this.systemextents
		},
		getParseConfig() {
			return this.parseconfig
		},
		loadLyrNames() {
			return this.loadlyrnames
		}
	}
})
