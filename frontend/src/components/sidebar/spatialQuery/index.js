import { transfrom } from '@/components/map/primitive/util/ProjectionUtil.js'
import buildLayer, { LayerType } from '@/components/map/primitive/layer/Layer.js'
import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
import EsrijsonUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
import { DrawType } from '@/components/map/primitive/interaction/DrawTool'
import emitter from '@/components/map/utils/eventbus.js'
import * as turf from '@turf/turf'

import { useMapStore } from '@/store/modules/map.js'
const mapStore = useMapStore()

import { arcGisApi } from '@/api/gis/arcgis.js'
const api = arcGisApi()

import { useAppStore } from '@/store/modules/app.js'
const appStore = useAppStore()

import { useExportStore } from '@/store/modules/export.js'

const serviceInfos = []
const themeLyrName = 'themeTempLyr'
let info = null

/**
 * 加载空间数据
 */
const loadSpatialData = async ({ configItem, tableHeaders, tableData, chartOption }) => {
	if (!configItem?.url)
		return;
	
	info = { configItem, tableHeaders, tableData, chartOption }
	let serviceInfo = await getServiceInfo(configItem)
	if (serviceInfo) {
		flyToRegion(serviceInfo)
		// PS: 打开功能时不加载数据，若看数据从数据目录中加载，这里打开功能模块只做查询
		loadLayer(configItem)
	}
}

/**
 * 加载展示数据
 */
const loadLayer = configItem => {
	mapStore.activeMap.lyrManager.removeLayer(themeLyrName)
	mapStore.activeMap.lyrManager.addLayer(
		themeLyrName,
		buildLayer(LayerType.ArcGISRest, {
			name: themeLyrName,
			url: configItem.url,
			layers: [configItem?.layerIndex]
		})
	)
}

/**
 * 移除展示数据
 */
const unloadSpatialData = () => {
	mapStore.activeMap.lyrManager.removeLayer(themeLyrName)
}

/**
 * 飞跃到该区域
 */
const flyToRegion = serviceInfo => {
	if (serviceInfo?.extent) {
		let proj = mapStore.getProjections['EPSG:' + serviceInfo.extent.spatialReference.wkid]
		let leftbottom = transfrom([serviceInfo.extent.xmin, serviceInfo.extent.ymin], proj, mapStore.activeMap.projection)
		let rightTop = transfrom([serviceInfo.extent.xmax, serviceInfo.extent.ymax], proj, mapStore.activeMap.projection)

		mapStore.activeMap.flyToManager.flyToRegion({
			type: 'Feature',
			geometry: {
				type: 'Polygon',
				coordinates: [[leftbottom, [rightTop[0], leftbottom[1]], rightTop, [leftbottom[0], rightTop[1]], leftbottom]]
			},
			properties: null
		})
	}
}

/**
 * 获取服务信息
 */
const getServiceInfo = async configItem => {
	let info = serviceInfos.find(n => n.configItem.name == configItem.name)
	if (info) {
		return info.serviceInfo
	} else {
		const serviceInfo = await api.getMapServerInfo(configItem.url + '/' + configItem.layerIndex)
		if (!serviceInfo?.error) {
			serviceInfos.push({
				configItem,
				serviceInfo
			})
			return serviceInfo
		} else {
			ElMessage({
				showClose: true,
				message: `获取'${configItem.name}'服务信息失败，请检查是否正确配置！`,
				type: 'warning'
			})
			return null
		}
	}
}

/**
 * 监听是否绘制完成
 * 专题制图为独立操作：打开出图时不写入数据管理、不展示到主地图
 */
emitter.on('drawend', feature => {
	if (!feature) return
	const exportStore = useExportStore()
	if (exportStore.isOpenExport) return

	let t = setTimeout(() => {
		clearTimeout(t)
		mapStore.activeMap.featureManager.removeFeatures()
		let drawTool = mapStore.activeMap.drawTool
		drawTool.clear()
		drawTool.finish()
	}, 100)

	mapStore.setImportDatas({
		source: '查询图斑' + (mapStore.getImportDatas.length + 1),
		name: '图斑' + (mapStore.getImportDatas.length + 1),
		color: '#ff0000',
		isVisible: true,
		check: true,
		geometryType: {
			Point: '点',
			LineString: '线',
			Polygon: '面'
		}[feature.geometry.type],
		geometry: feature
	})
	appStore.setSwitchObj('importData', true)
})

/**
 * 绘制面
 */
const drawPolygon = () => {
	let drawTool = mapStore.activeMap.drawTool
	drawTool.clear()
	drawTool.finish()
	mapStore.activeMap.featureManager.removeFeatures()

	drawTool.start({
		drawType: DrawType.Polygon,
		drawingStyle: feature => {
			return {
				stroke: {
					color: 'rgb(61,151,248)',
					width: 3
				},
				fill: {
					color: 'rgba(255, 255, 255, 0.3)'
				}
			}
		}
	})
}

/**
 * 导入范围文件
 */
const importRangeHandler = () => {
	appStore.setSwitchObj('showUploadData', true)
}

export { loadSpatialData, unloadSpatialData, importRangeHandler, drawPolygon }
