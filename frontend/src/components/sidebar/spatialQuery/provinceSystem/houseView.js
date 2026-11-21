import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
import EsrijsonUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
import emitter from '@/components/map/utils/eventbus.js'
import _ from 'lodash'

import { useAppStore } from '@/store/modules/app.js'
const appStore = useAppStore()

import { useMapStore } from '@/store/modules/map.js'
const mapStore = useMapStore()

import { arcGisApi } from '@/api/gis/arcgis.js'
const api = arcGisApi()

import * as turf from '@turf/turf'
import { ElLoading } from 'element-plus'
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

/**
 * 设置查询条件
 */
const getFilters = () => {
	let filters = { geometry: '' }
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
		filters.geometry = encodeURI(JSON.stringify({ hasZ: false, hasM: false, rings: coords }))
	}
	return filters
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
 * 图表展示
 */
const showChartInfo = async () => {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let datas = mapStore.getImportDatas
			.filter(feat => feat.check)
			.map(range => {
				let filterFeats = res.filter(feat => !turf.booleanDisjoint(range.geometry, feat))
				return { range, features: filterFeats }
			})
		chartOption.value = {
			tooltip: {
				trigger: 'axis',
				axisPointer: { type: 'shadow' }
			},
			xAxis: {
				type: 'category',
				data: datas.map(m => m.range.source)
			},
			yAxis: {
				type: 'value'
			},
			series: [
				{
					data: datas.map(m => m.features.length),
					type: 'bar'
				}
			]
		}
	} else {
		ElMessage({
			showClose: true,
			message: `未查询到数据，请检查查询条件`,
			type: 'warning'
		})
	}
}

/**
 * 图表展示
 */
const showChartInfo2 = async () => {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let datas = mapStore.getImportDatas
			.filter(feat => feat.check)
			.map(range => {
				let filterFeats = res.filter(feat => !turf.booleanDisjoint(range.geometry, feat))
				return { range, features: filterFeats }
			})
		let res33 = datas
			.map(m => {
				let areas = m.features.map(feat => feat.properties.Shape_Area)
				let sum = areas.reduce((accumulator, currentValue) => accumulator + currentValue, 0)
				return sum.toFixed(2)
			})
			.map(Number)

		chartOption.value = {
			tooltip: {
				trigger: 'axis',
				axisPointer: { type: 'shadow' }
			},
			xAxis: {
				type: 'category',
				data: datas.map(m => m.range.source)
			},
			yAxis: {
				type: 'value'
			},
			series: [{ data: res33, type: 'bar' }]
		}
	} else {
		ElMessage({
			showClose: true,
			message: `未查询到数据，请检查查询条件`,
			type: 'warning'
		})
	}
}

/**
 * 表格展示
 */
const showTable = async () => {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let datas = mapStore.getImportDatas
			.filter(feat => feat.check)
			.map(range => {
				let filterFeats = res.filter(feat => !turf.booleanDisjoint(range.geometry, feat))
				return { range, features: filterFeats }
			})

		tableHeaders.value = [
			{ prop: 'name', label: '范围分区' },
			{ prop: 'count', label: '栋数' },
			{ prop: 'area', label: '基底面积（平方米）' }
		]
		tableDatas.value = datas.map(m => {
			return {
				name: m.range.source,
				count: m.features.length,
				area: m.features
					.map(n => n.properties.Shape_Area)
					.reduce((accumulator, currentValue) => accumulator + currentValue)
					.toFixed(2)
			}
		})
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
function queryHouseData(configInfo, item, headers, datas, option) {
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
		case 'table':
			return showTable()
		case 'eCharts':
			return showChartInfo()
		case 'eCharts2':
			return showChartInfo2()
	}
}

export { queryHouseData }
