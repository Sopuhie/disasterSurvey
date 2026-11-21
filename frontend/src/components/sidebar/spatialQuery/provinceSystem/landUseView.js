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
		let cols = params[NAME]
		if (!cols.includes('year')) {
			cols.unshift('year')
		}
		let datas = params[YEAR].map(n => new Array(cols.length).fill(0))
		let groupFeatures = _.groupBy(res, n => n.properties[YEAR])
		let index = 0
		for (let [key, val] of Object.entries(groupFeatures)) {
			datas[index][0] = key
			cols.forEach((m, subIndex) => {
				if (subIndex > 0) {
					let feats = val.filter(feat => feat.properties[NAME] == m)
					let area = feats
						.map(feat => feat.properties.Shape_Area)
						.reduce((accumulator, currentValue) => accumulator + currentValue, 0)
					datas[index][subIndex] = area.toFixed(2)
				}
			})
			index++
		}
		tableHeaders.value = cols.map(m => {
			return { prop: m, label: m }
		})

		tableDatas.value = datas.map(m => {
			return cols
				.map((n, index) => {
					return {
						[n]: m[index]
					}
				})
				.reduce((accumulator, currentValue) => {
					return { ...accumulator, ...currentValue }
				}, {})
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
			series: [{ type: 'bar' }, { type: 'bar' }]
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
			datas.push({ name: key, value: val.length })
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

/**
 * 查询数据
 */
function queryLandUseData(configInfo, item, headers, datas, option) {
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

export { queryLandUseData }
