import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
import EsrijsonUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
import emitter from '@/components/map/utils/eventbus.js'
import _ from 'lodash'

import { useMapStore } from '@/store/modules/map.js'
const mapStore = useMapStore()

import { arcGisApi } from '@/api/gis/arcgis.js'
const api = arcGisApi()

import * as turf from '@turf/turf'

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

/**
 * 设置查询条件
 */
const getFilters = () => {
	let filters = { where: `1=1`, geometry: '' }
	filters.geometry = getFilterGeometry()
	return filters
}
// 获取geometry
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
	let params = {
		where: filters?.where || `1=1`,
		outFields: '*',
		returnGeometry: true,
		returnDistinctValues: false,
		returnIdsOnly: false,
		inSR: mapStore.activeMap.projection.getCode().split(':')[1],
		outSR: mapStore.activeMap.projection.getCode().split(':')[1],
		geometryType: 'esriGeometryPolygon',
		geometry: filters?.geometry || '', //encodeURI(JSON.stringify({ hasZ: false, hasM: false, rings: coords })),
		spatialRel: 'esriSpatialRelIntersects',
		f: 'pjson'
	}
	let res = await api.query(config.url + '/' + config.layerIndex, params)
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
 * 表格展示
 */
const showTable = async () => {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let resTableDatas = []
		let datas = _.groupBy(res, n => n.properties[params.field])
		for (let [key, val] of Object.entries(datas)) {
			let calRes = val.map(f => {
				let feature = GeoJSONUtil.readFeature(f, {
					dataProjection: mapStore.activeMap.projection,
					featureProjection: 'EPSG:4326'
				})
				let convertFeat = GeoJSONUtil.writeFeatureObject(feature)
				let area = turf.area(convertFeat)
				return parseFloat(area.toFixed(2))
			})
			resTableDatas.push({
				name: key,
				val: calRes.reduce((accumulator, currentValue) => accumulator + currentValue, 0)
			})
		}

		tableHeaders.value = [
			{ prop: 'name', label: '控规情况' },
			{ prop: 'val', label: '地块面积（平方米）' }
		]
		tableDatas.value = resTableDatas
	} else {
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
function queryCityPlanRuleData(configInfo, item, headers, datas, option) {
	loading = ElLoading.service({
		lock: true,
		text: 'Loading',
		background: 'rgba(0, 0, 0, 0.7)'
	})
	config = configInfo
	params = item
	tableHeaders = headers
	tableDatas = datas
	chartOption = option

	switch (item.result) {
		// 表格
		case 'table':
			return showTable()
	}
}

export { queryCityPlanRuleData }
