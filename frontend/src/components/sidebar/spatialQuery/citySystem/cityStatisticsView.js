
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

const serviceInfos = []
const themeLyrName = 'themeTempLyr'
let info = null

/**
 * 查询 loading
 */
let loading = null
/**
 * 查询结果
 */
let queryRes = []
/**
 * 初始化配置信息
 */
let config = null
/**
 * 查询参数
 */
let params = null
/**
 * 显示表头
 */
let tableHeaders = null
/**
 * 表格数据
 */
let tableDatas = null
/**
 * 图表信息
 */
let chartOption = null

let chartTextColor = '#ffffff'

/**
 * 加载空间数据
 */
const loadSpatialData = async ({ configItem, tableHeaders, tableData, chartOption }) => {
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
 * 设置查询条件
 */
const getFilters = () => {
	let filters = { where: `1=1`, geometry: '' }
	let statisFields = params.tjzdItem.map(n => {
		return {
			statisticType: params.tjTypeItem.value,  // eg: sum  / count
			onStatisticField: n.value,
			outStatisticFieldName:n.label
		}
	})
	filters.outStatistics = encodeURI(JSON.stringify(statisFields));
	filters.groupByFieldsForStatistics = params.fzzdItem[0].value;  // eg: XZQMC
	filters.geometry = getFilterGeometry();
	return filters
}

/**
 * 获取 geometry
 */
const getFilterGeometry = () => {
	let geometry = ''
	if (mapStore.getImportDatas?.length) {
		let coords = mapStore.getImportDatas
			.filter(feat => feat.check)
			.map(m => {
				if (m.geometry.geometry.type == 'MultiPolygon') {
					let coors = []
					m.geometry?.geometry?.coordinates.map(k => {
						coors.push(k[0])
					})
					return _.flatten(coors)
				} else if (m.geometry.geometry.type == 'Polygon') {
					return m.geometry.geometry.coordinates[0]
				} else {
					return []
				}
			})
		geometry = encodeURI(JSON.stringify({ hasZ: false, hasM: false, rings: coords }))
	}
	return geometry
}

/**
 * 查询数据
 */
const checkSpatialDatas = async filters => {
	chartTextColor = appStore.appThemeVal === 'dark' ? '#ffffff' : '#606266'
	let param = {
		where: filters?.where || `1=1`,
		outFields: '*',
		returnGeometry: false,
		returnDistinctValues: false,
		returnIdsOnly: false,
		inSR: mapStore.activeMap.projection.getCode().split(':')[1],
		outSR: mapStore.activeMap.projection.getCode().split(':')[1],
		geometryType: 'esriGeometryPolygon',
		geometry: filters?.geometry || '', //encodeURI(JSON.stringify({ hasZ: false, hasM: false, rings: coords })),
		spatialRel: 'esriSpatialRelIntersects',
		outStatistics: filters.outStatistics,
		groupByFieldsForStatistics: filters.groupByFieldsForStatistics, 
		f: 'pjson'
	}
	let res = await api.query(params.tjDataItem.url + '/' + params.tjDataItem.layerIndex, param)
	loading.close()
	if (res?.features?.length) {
		let geojson = GeoJSONUtil.writeFeaturesObject(EsrijsonUtil.readFeatures(res))
		queryRes = geojson.features
		return queryRes
	} else {
		return []
	}
}

/**
 * 设置表格
 */
async function singleTable() {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		tableHeaders.value = []
		for (let [key, val] of Object.entries(res[0].properties)) {
			if (key.toLocaleLowerCase() == 'geometry') 
				continue;

			let propLabel = params.fzzdItem.find(n => n.value == key)?.label || key
			tableHeaders.value.push({
				prop: key,
				label: propLabel
			})
		}
		tableDatas.value = res.map(m => m.properties)
	} else {
		loading.close()
		ElMessage({
			showClose: true,
			message: `未查询到数据，请检查查询条件`,
			type: 'warning'
		})
	}
}

/**
 * 查询数据
 */
function queryStatisticsData(configInfo, item, headers, datas, option) {
	config = configInfo
	params = item
	tableHeaders = headers
	tableDatas = datas
	chartOption = option
	loading = ElLoading.service({
		lock: true,
		text: 'Loading',
		background: 'rgba(0, 0, 0, 0.7)'
	})

	try {
		singleTable();
	} catch (e) {
		console.log(e)
		loading.close()
	}
}


export { loadSpatialData, unloadSpatialData, queryStatisticsData }
