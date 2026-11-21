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
 * 分组字段：年
 */
let YEAR = 'Year'
/**
 * 分组字段：Name
 */
let NAME = 'Name'

/**
 * 设置查询条件
 */
const getFilters = () => {
	let filters = { where: `1=1`, geometry: '' }
	if (params.Year?.length) {
		let strs = params.Year.map(m => "'" + m + "'").join(',')
		filters.where = filters.where + ` AND ${'Year'} IN (${strs})`
	}
	if (params.Name?.length) {
		let strs = params.Name.map(m => "'" + m + "'").join(',')
		filters.where = filters.where + ` AND ${'Name'} IN (${strs})`
	}
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
		returnGeometry: false,
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
		let datas = mapStore.getImportDatas
			.filter(feat => feat.check)
			.map(range => {
				let filterFeats = res.filter(feat => !turf.booleanDisjoint(range.geometry, feat))
				return { range, features: filterFeats }
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
 * 单一年份不同用地类型比较
 */
const singleYearDiffLandUsePieHandler = async () => {
	if (params.Year?.length) {
		params.Year = [params.Year[0]]
	}
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[NAME]
			let res = groupFeatures[val] || []
			res.push(feat)
			groupFeatures[val] = res
		})

		let datas = []
		for (let [key, val] of Object.entries(groupFeatures)) {
			datas.push({ name: key, value: val.length })
		}
		chartOption.value = {
			title: {
				text: '单一年份不同用地类型比较',
				subtext: '',
				left: 'center',
				top: '12px'
			},
			tooltip: { trigger: 'item' },
			legend: {
				orient: 'vertical',
				left: 'left',
				top: '12px'
			},
			series: [
				{
					name: '用地类型数量',
					type: 'pie',
					radius: '50%',
					data: datas,
					label: { normal: { show: true, formatter: '{b}: {d}%' } },
					emphasis: {
						itemStyle: {
							shadowBlur: 10,
							shadowOffsetX: 0,
							shadowColor: 'rgba(0, 0, 0, 0.5)'
						}
					}
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
 * 单一年份不同用地类型比较
 */
const singleYearDiffLandUseBarHandler = async () => {
	if (params.Year?.length) {
		params.Year = [params.Year[0]]
	}
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[NAME]
			let res = groupFeatures[val] || []
			res.push(feat)
			groupFeatures[val] = res
		})

		let datas = []
		for (let [key, val] of Object.entries(groupFeatures)) {
			datas.push({ name: key, value: val.length })
		}

		chartOption.value = {
			title: {
				text: '单一年份不同用地类型比较',
				subtext: '',
				left: 'center',
				top: '12px'
			},
			tooltip: { trigger: 'item' },
			xAxis: {
				type: 'category',
				data: datas.map(n => n.name)
			},
			yAxis: {
				type: 'value'
			},
			series: [
				{
					data: datas.map(n => n.value),
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
 * 多个年份单一用地类型比较
 */
const multiYearSingleLandUseHandler = async () => {
	if (params.Name?.length) {
		params.Name = [params.Name[0]]
	}
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[YEAR]
			let res = groupFeatures[val] || []
			res.push(feat)
			groupFeatures[val] = res
		})

		let datas = []
		for (let [key, val] of Object.entries(groupFeatures)) {
			datas.push({ name: key, value: val.length })
		}

		chartOption.value = {
			title: {
				text: '多个年份单一用地类型比较',
				subtext: '',
				left: 'center',
				top: '12px'
			},
			tooltip: { trigger: 'item' },
			xAxis: {
				type: 'category',
				data: datas.map(n => n.name)
			},
			yAxis: {
				type: 'value'
			},
			series: [
				{
					data: datas.map(n => n.value),
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
 * 多个年份不同用地类型比较
 */
const multiYearMultiLandUseHandler = async () => {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[YEAR]
			let res = groupFeatures[val] || []
			res.push(feat)
			groupFeatures[val] = res
		})

		let years = ['year']
		if (params.Year?.length) {
			years = years.concat(params.Year)
		}

		let datas = []
		datas = params.Name.map(m => {
			let data = []
			data.push(m)
			params.Year.forEach(y => {
				let val = groupFeatures[y] || []
				let res = val.filter(n => n.properties[NAME] == m)
				data.push(res.length)
			})
			return data
		})
		datas.unshift(years)

		chartOption.value = {
			title: {
				text: '多个年份不同用地类型比较',
				subtext: '',
				left: 'center',
				top: '12px'
			},
			tooltip: {},
			dataset: { source: datas },
			xAxis: { type: 'category' },
			yAxis: {},
			series: [{ type: 'bar' }, { type: 'bar' }, { type: 'bar' }]
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
 * 多个年份单一用地类型比较
 */
const multiYearSingleLandUseBarHandler = async () => {
	if (params.Name?.length) {
		params.Name = [params.Name[0]]
	}
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[YEAR]
			let res = groupFeatures[val] || []
			res.push(feat)
			groupFeatures[val] = res
		})

		let datas = []
		for (let [key, val] of Object.entries(groupFeatures)) {
			datas.push({
				name: key,
				value: val.length
			})
		}

		chartOption.value = {
			title: {
				text: '多个年份单一用地类型比较',
				subtext: '',
				left: 'center',
				top: '12px'
			},
			xAxis: {
				type: 'category',
				data: datas.map(n => n.name)
			},
			yAxis: {
				type: 'value'
			},
			series: [
				{
					data: datas.map(n => n.value),
					type: 'line',
					smooth: true
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

// 创建where条件
function createWhereClause(objs) {
	let conditions = []
	for (let key in objs) {
		if (key !== 'result' && Array.isArray(objs[key]) && objs[key].length > 0) {
			let subConditions = objs[key].map(item => `${key} IN ('${item}')`).join(' OR ')
			conditions.push(subConditions)
		}
	}
	let whereClause = conditions.length > 0 ? '1=1 AND ' + conditions.join(' AND ') : '1=1'
	return whereClause
}

// 用地监测
const ydjcHandler = async () => {
	const filters = {
		where: createWhereClause(params),
		geometry: getFilterGeometry()
	}
	const res = await checkSpatialDatas(filters)
	console.log('res:', res)
	chartOption.value = {}
	const option = {
		title: {
			text: 'Referer of a Website',
			subtext: '',
			left: 'center',
			top: '12px'
		},
		tooltip: { trigger: 'axis' },
		grid: {
			left: '5%',
			right: '5%',
			top: '15%',
			bottom: '10%',
			containLabel: true
		},
		xAxis: {
			type: 'category',
			data: []
		},
		yAxis: {
			type: 'value'
		},
		series: []
	}

	if (params.result == 'table') {
		if (res?.length) {
			let datas = mapStore.getImportDatas
				.filter(feat => feat.check)
				.map(range => {
					let filterFeats = res.filter(feat => !turf.booleanDisjoint(range.geometry, feat))
					return {
						range,
						features: filterFeats
					}
				})
			console.log('datas:', datas)
		}
		tableHeaders.value = [
			{ prop: 'name', label: '范围分区' },
			{ prop: 'count', label: '栋数' },
			{ prop: 'area', label: '基底面积（平方米）' }
		]
		tableDatas.value = [
			{ name: 'source1', count: 10, area: 10000 },
			{ name: 'source2', count: 20, area: 20000 },
			{ name: 'source3', count: 30, area: 30000 }
		]
		// tableDatas.value = datas.map(m => {
		// 	return {
		// 		name: m.range.source,
		// 		count: m.features.length,
		// 		area: m.features
		// 			.map(n => n.properties.Area)
		// 			.reduce((accumulator, currentValue) => accumulator + currentValue)
		// 			.toFixed(2)
		// 	}
		// })
	} else if (params.result == 'pie') {
		chartOption.value = option
		chartOption.value.xAxis = null
		chartOption.value.yAxis = null
		chartOption.value.tooltip.trigger = 'item'
		chartOption.value.legend = {
			orient: 'vertical',
			left: 'left'
		}
		chartOption.value.title.text = '大饼图'
		chartOption.value.series = [
			{
				type: 'pie',
				radius: '50%',
				data: [
					{ value: 1048, name: 'Search' },
					{ value: 735, name: 'Direct' },
					{ value: 580, name: 'Email' },
					{ value: 484, name: 'Union' },
					{ value: 300, name: 'Video' }
				],
				emphasis: {
					itemStyle: {
						shadowBlur: 10,
						shadowOffsetX: 0,
						shadowColor: 'rgba(0, 0, 0, 0.5)'
					}
				}
			}
		]
	} else if (params.result == 'bar') {
		chartOption.value = option
		chartOption.value.dataZoom = [{ type: 'inside', start: 0, zoomLock: false }]
		chartOption.value.title.text = '柱状图'
		chartOption.value.xAxis.data = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
		chartOption.value.series = { type: 'bar', data: [120, 200, 150, 80, 70, 110, 130] }
	} else if (params.result == 'line') {
		chartOption.value = option
		chartOption.value.dataZoom = [{ type: 'inside', start: 0, zoomLock: false }]
		chartOption.value.title.text = '折线图'
		chartOption.value.xAxis.data = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
		chartOption.value.series = { type: 'line', data: [150, 230, 224, 218, 135, 147, 260] }
	}
}

/**
 * 查询数据
 */
function queryFeatureFacilityWarningData(configInfo, item, headers, datas, option) {
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
	switch (config.name) {
		case '用地监测':
			return ydjcHandler()
		default:
			break
	}

	switch (item.result) {
		// 表格
		case 'table':
			return showTable()
		// 单一年份不同用地类型比较
		case 'singleYearDiffLandUsePie':
			return singleYearDiffLandUsePieHandler()
		// 单一年份不同用地类型比较
		case 'singleYearDiffLandUseBar':
			return singleYearDiffLandUseBarHandler()
		// 多个年份单一用地类型比较
		case 'multiYearSingleLandUse':
			return multiYearSingleLandUseHandler()
		// 多个年份不同用地类型比较
		case 'multiYearMultiLandUse':
			return multiYearMultiLandUseHandler()
		// 多个年份单一用地类型比较
		case 'multiYearSingleLandUseBar':
			return multiYearSingleLandUseBarHandler()
	}
}

export { queryFeatureFacilityWarningData }
