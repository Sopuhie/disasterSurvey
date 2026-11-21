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
 * 统计面积字段
 */
let SHAPE_AREA = 'YJLMC'
/**
 * 表头信息
 */
let headers = {
	YJLMC: [
		{ prop: 'name', label: '一级类名称' },
		{ prop: 'area', label: 'Shape_Area（㎡）' }
	],
	JZFG: [
		{ prop: 'name', label: '建筑风格' },
		{ prop: 'count', label: '数量' }
	],
	BHFL: [
		{ prop: 'name', label: '保护分类' },
		{ prop: 'count', label: '数量' }
	],
	LB: [
		{ prop: 'name', label: '类别' },
		{ prop: 'count', label: '数量' }
	]
}

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
	let queryParams = {
		where: filters?.where || `1=1`,
		outFields: '*',
		returnGeometry: params.field == SHAPE_AREA,
		returnDistinctValues: false,
		returnIdsOnly: false,
		inSR: mapStore.activeMap.projection.getCode().split(':')[1],
		outSR: mapStore.activeMap.projection.getCode().split(':')[1],
		geometryType: 'esriGeometryPolygon',
		geometry: filters?.geometry || '', //encodeURI(JSON.stringify({ hasZ: false, hasM: false, rings: coords })),
		spatialRel: 'esriSpatialRelIntersects',
		f: 'pjson'
	}
	let res = await api.query(config.url + '/' + config.layerIndex, queryParams)
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
			let calRes =
				params.field != SHAPE_AREA
					? [0]
					: val.map(f => {
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
				area: calRes.reduce((accumulator, currentValue) => accumulator + currentValue, 0),
				count: val.length || 0
			})
		}
		tableHeaders.value = headers[params.field]
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
 * 柱状图
 */
const showBar = async () => {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let resTableDatas = []
		let datas = _.groupBy(res, n => n.properties[params.field])
		for (let [key, val] of Object.entries(datas)) {
			let calRes =
				params.field != SHAPE_AREA
					? [0]
					: val.map(f => {
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
				area: calRes.reduce((accumulator, currentValue) => accumulator + currentValue, 0),
				count: val.length || 0
			})
		}

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

		chartOption.value = option
		chartOption.value.dataZoom = [{ type: 'inside', start: 0, zoomLock: false }]
		chartOption.value.title.text = '历史名称场景-柱状图'
		chartOption.value.xAxis.data = resTableDatas.map(k => k.name)
		chartOption.value.series = {
			type: 'bar',
			data: params.field == SHAPE_AREA ? resTableDatas.map(k => k.area) : resTableDatas.map(k => k.count)
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
 * 饼状图
 */
const showPie = async () => {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let resTableDatas = []
		let datas = _.groupBy(res, n => n.properties[params.field])
		for (let [key, val] of Object.entries(datas)) {
			let calRes =
				params.field != SHAPE_AREA
					? [0]
					: val.map(f => {
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
				area: calRes.reduce((accumulator, currentValue) => accumulator + currentValue, 0),
				count: val.length || 0
			})
		}

		const option = {
			title: {
				text: 'Referer of a Website',
				subtext: '',
				left: 'center',
				top: '12px'
			},
			tooltip: {
				trigger: 'axis'
			},
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

		chartOption.value = option
		chartOption.value.xAxis = null
		chartOption.value.yAxis = null
		chartOption.value.tooltip.trigger = 'item'
		chartOption.value.legend = {
			orient: 'vertical',
			left: 'left'
		}
		chartOption.value.title.text = '历史名称场景-饼状图'
		chartOption.value.series = [
			{
				type: 'pie',
				radius: '50%',
				data:
					params.field == SHAPE_AREA
						? resTableDatas.map(k => {
								return {
									name: k.name,
									value: k.area
								}
						  })
						: resTableDatas.map(k => {
								return {
									name: k.name,
									value: k.count
								}
						  }),
				emphasis: {
					itemStyle: {
						shadowBlur: 10,
						shadowOffsetX: 0,
						shadowColor: 'rgba(0, 0, 0, 0.5)'
					}
				}
			}
		]
	} else {
		ElMessage({
			showClose: true,
			message: `未查询到数据，请检查查询条件`,
			type: 'warning'
		})
	}
}

/**
 * 折线图
 */
const showLine = async () => {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let resTableDatas = []
		let datas = _.groupBy(res, n => n.properties[params.field])
		for (let [key, val] of Object.entries(datas)) {
			let calRes =
				params.field != SHAPE_AREA
					? [0]
					: val.map(f => {
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
				area: calRes.reduce((accumulator, currentValue) => accumulator + currentValue, 0),
				count: val.length || 0
			})
		}

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

		chartOption.value = option
		chartOption.value.dataZoom = [{ type: 'inside', start: 0, zoomLock: false }]
		chartOption.value.title.text = '历史名称场景-折线图'
		chartOption.value.xAxis.data = resTableDatas.map(k => k.name)
		chartOption.value.series = {
			type: 'line',
			data: params.field == SHAPE_AREA ? resTableDatas.map(k => k.area) : resTableDatas.map(k => k.count)
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
function queryFeatureHistoryCityData(configInfo, item, headers, datas, option) {
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
		// 柱状图
		case 'bar':
			return showBar()
		// 饼状图
		case 'pie':
			return showPie()
		// 折线图
		case 'line':
			return showLine()
	}
}

export { queryFeatureHistoryCityData }
