import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
import EsrijsonUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
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
	let filters = {
		where: `1=1`,
		geometry: ''
	}
	let types = params[params.whereField]
	let vals = []
	types.forEach(t => {
		vals = vals.concat(params[t])
	})
	let strs = vals.map(m => "'" + m + "'").join(',')
	filters.where = filters.where + ` AND ${params.whereField} IN (${strs})`
	filters.geometry = getFilterGeometry()
	return filters
}

/**
 * 获取geometry
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
		let types = params[params.whereField]
		let datas = types.map(t => {
			let vals = params[t]
			let filterFeats = res.filter(feat => vals.includes(feat.properties[params.whereField]))
			return { type: t, features: filterFeats }
		})

		if (datas?.length) {
			let headers = [{ prop: 'name', label: '一级类名称' }]
			let areaTotal = new Array(datas?.length).fill(0)
			let lengthTotal = new Array(datas?.length).fill(0)

			if (params.tjzd.includes('getArea')) {
				headers.push({ prop: 'Area', label: '面积（平方米）' })
				areaTotal = datas.map(d => {
					return d.features.map(f => {
						let feature = GeoJSONUtil.readFeature(f, {
							dataProjection: mapStore.activeMap.projection,
							featureProjection: 'EPSG:4326'
						})
						let convertFeat = GeoJSONUtil.writeFeatureObject(feature)
						let area = turf.area(convertFeat)
						return parseFloat(area.toFixed(2))
					})
				})
			}
			if (params.tjzd.includes('getLength')) {
				headers.push({ prop: 'Length', label: '周长（米）' })
				lengthTotal = datas.map(d => {
					return d.features.map(f => {
						let feature = GeoJSONUtil.readFeature(f, {
							dataProjection: mapStore.activeMap.projection,
							featureProjection: 'EPSG:4326'
						})
						let convertFeat = GeoJSONUtil.writeFeatureObject(feature)
						let len = turf.length(convertFeat, { units: 'meters' })
						return parseFloat(len.toFixed(2))
					})
				})
			}
			tableHeaders.value = headers
			tableDatas.value = datas.map((m, index) => {
				let row = { name: m.type }
				if (params.tjzd.includes('getArea')) {
					row = Object.assign(row, {
						Area: areaTotal[index].reduce((accumulator, currentValue) => accumulator + currentValue, 0)
					})
				}
				if (params.tjzd.includes('getLength')) {
					row = Object.assign(row, {
						Length: lengthTotal[index].reduce((accumulator, currentValue) => accumulator + currentValue, 0)
					})
				}
				return row
			})
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
 * 柱状图
 */
const showBar = async () => {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let types = params[params.whereField]
		let datas = types.map(t => {
			let vals = params[t]
			let filterFeats = res.filter(feat => vals.includes(feat.properties[params.whereField]))
			return { type: t, features: filterFeats }
		})

		if (datas?.length) {
			let areaTotal = new Array(datas?.length).fill(0)
			let lengthTotal = new Array(datas?.length).fill(0)

			if (params.tjzd.includes('getArea')) {
				areaTotal = datas.map(d => {
					return d.features.map(f => {
						let feature = GeoJSONUtil.readFeature(f, {
							dataProjection: mapStore.activeMap.projection,
							featureProjection: 'EPSG:4326'
						})
						let convertFeat = GeoJSONUtil.writeFeatureObject(feature)
						let area = turf.area(convertFeat)
						return parseFloat(area.toFixed(2))
					})
				})
			}
			if (params.tjzd.includes('getLength')) {
				lengthTotal = datas.map(d => {
					return d.features.map(f => {
						let feature = GeoJSONUtil.readFeature(f, {
							dataProjection: mapStore.activeMap.projection,
							featureProjection: 'EPSG:4326'
						})
						let convertFeat = GeoJSONUtil.writeFeatureObject(feature)
						let len = turf.length(convertFeat, { units: 'meters' })
						return parseFloat(len.toFixed(2))
					})
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
				legend: {
					orient: 'vertical',
					left: 'left'
				},
				series: []
			}
			chartOption.value = option
			chartOption.value.dataZoom = [{ type: 'inside', start: 0, zoomLock: false }]
			chartOption.value.title.text = '用地监测-柱状图'
			chartOption.value.xAxis.data = datas.map(n => n.type)
			let mapRes = datas.map((m, index) => {
				let row = {
					name: m.type
				}
				if (params.tjzd.includes('getArea')) {
					row = Object.assign(row, {
						Area: areaTotal[index].reduce((accumulator, currentValue) => accumulator + currentValue, 0)
					})
				}
				if (params.tjzd.includes('getLength')) {
					row = Object.assign(row, {
						Length: lengthTotal[index].reduce((accumulator, currentValue) => accumulator + currentValue, 0)
					})
				}
				return row
			})
			let res = []
			if (params.tjzd.includes('getArea')) {
				res.push({ name: '面积', type: 'bar', data: mapRes.map(m => m.Area) })
			}
			if (params.tjzd.includes('getLength')) {
				res.push({ name: '周长', type: 'bar', data: mapRes.map(m => m.Length) })
			}
			chartOption.value.series = res
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
		let types = params[params.whereField]
		let datas = types.map(t => {
			let vals = params[t]
			let filterFeats = res.filter(feat => vals.includes(feat.properties[params.whereField]))
			return { type: t, features: filterFeats }
		})

		if (datas?.length) {
			let areaTotal = new Array(datas?.length).fill(0)
			let lengthTotal = new Array(datas?.length).fill(0)

			if (params.tjzd.includes('getArea')) {
				areaTotal = datas.map(d => {
					return d.features.map(f => {
						let feature = GeoJSONUtil.readFeature(f, {
							dataProjection: mapStore.activeMap.projection,
							featureProjection: 'EPSG:4326'
						})
						let convertFeat = GeoJSONUtil.writeFeatureObject(feature)
						let area = turf.area(convertFeat)
						return parseFloat(area.toFixed(2))
					})
				})
			}
			if (params.tjzd.includes('getLength')) {
				lengthTotal = datas.map(d => {
					return d.features.map(f => {
						let feature = GeoJSONUtil.readFeature(f, {
							dataProjection: mapStore.activeMap.projection,
							featureProjection: 'EPSG:4326'
						})
						let convertFeat = GeoJSONUtil.writeFeatureObject(feature)
						let len = turf.length(convertFeat, { units: 'meters' })
						return parseFloat(len.toFixed(2))
					})
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

			let resDatas = []
			if (params.tjzd.includes('getArea')) {
				resDatas = datas.map((d, index) => {
					return {
						name: d.type,
						value: areaTotal[index].reduce((accumulator, currentValue) => accumulator + currentValue, 0)
					}
				})
			}
			if (params.tjzd.includes('getLength')) {
				resDatas = datas.map((d, index) => {
					return {
						name: d.type,
						value: lengthTotal[index].reduce((accumulator, currentValue) => accumulator + currentValue, 0)
					}
				})
			}

			chartOption.value = option
			chartOption.value.xAxis = null
			chartOption.value.yAxis = null
			chartOption.value.tooltip.trigger = 'item'
			chartOption.value.legend = {
				orient: 'vertical',
				left: 'left'
			}
			chartOption.value.title.text = '用地监测-折线图'
			chartOption.value.series = [
				{
					type: 'pie',
					radius: '50%',
					data: resDatas,
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
 * 折线图
 */
const showLine = async () => {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let types = params[params.whereField]
		let datas = types.map(t => {
			let vals = params[t]
			let filterFeats = res.filter(feat => vals.includes(feat.properties[params.whereField]))
			return { type: t, features: filterFeats }
		})

		if (datas?.length) {
			let areaTotal = new Array(datas?.length).fill(0)
			let lengthTotal = new Array(datas?.length).fill(0)

			if (params.tjzd.includes('getArea')) {
				areaTotal = datas.map(d => {
					return d.features.map(f => {
						let feature = GeoJSONUtil.readFeature(f, {
							dataProjection: mapStore.activeMap.projection,
							featureProjection: 'EPSG:4326'
						})
						let convertFeat = GeoJSONUtil.writeFeatureObject(feature)
						let area = turf.area(convertFeat)
						return parseFloat(area.toFixed(2))
					})
				})
			}
			if (params.tjzd.includes('getLength')) {
				lengthTotal = datas.map(d => {
					return d.features.map(f => {
						let feature = GeoJSONUtil.readFeature(f, {
							dataProjection: mapStore.activeMap.projection,
							featureProjection: 'EPSG:4326'
						})
						let convertFeat = GeoJSONUtil.writeFeatureObject(feature)
						let len = turf.length(convertFeat, { units: 'meters' })
						return parseFloat(len.toFixed(2))
					})
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
			chartOption.value.title.text = '用地监测-折线图'
			chartOption.value.xAxis.data = datas.map(n => n.type)
			let mapRes = datas.map((m, index) => {
				let row = { name: m.type }
				if (params.tjzd.includes('getArea')) {
					row = Object.assign(row, {
						Area: areaTotal[index].reduce((accumulator, currentValue) => accumulator + currentValue, 0)
					})
				}
				if (params.tjzd.includes('getLength')) {
					row = Object.assign(row, {
						Length: lengthTotal[index].reduce((accumulator, currentValue) => accumulator + currentValue, 0)
					})
				}
				return row
			})
			let res = []
			if (params.tjzd.includes('getArea')) {
				res.push({ name: '面积', type: 'line', data: mapRes.map(m => m.Area) })
			}
			if (params.tjzd.includes('getLength')) {
				res.push({ name: '周长', type: 'line', data: mapRes.map(m => m.Length) })
			}
			chartOption.value.series = res
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
 * 创建where条件
 */
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
/**
 *
 * @param {*} objs
 * @returns
 */
function getFormKey(objs) {
	let keys = []
	for (let key in objs) {
		if (key !== 'result' && Array.isArray(objs[key]) && objs[key].length > 0) {
			keys.push(key)
		}
	}
	return keys
}

/**
 *
 * @param {*} formKeys
 * @param {*} params
 * @param {*} fields
 * @returns
 */
function getSelectedOptionsWithChildren(formKeys, params, fields) {
	const result = {}

	formKeys.forEach(key => {
		const field = fields.find(f => f.children.some(c => c.prop === key))
		if (field) {
			const selectField = field.children.find(c => c.prop === key)
			if (selectField && selectField.options) {
				result[key] = []
				const selectedValues = params[key] || []
				selectedValues.forEach(value => {
					const option = selectField.options.find(o => o.value === value)
					if (option) {
						// 添加父选项
						if (!result[key].includes(option.value)) {
							result[key].push(option.value)
						}
						// 添加子选项
						if (option.children2 && option.children2.length > 0) {
							result[option.value] = option.children2.map(m => m.value)
						}
					}
				})
			}
		}
	})

	return result
}

/**
 * 查询数据
 */
function queryCityLandUseData(configInfo, item, headers, datas, option, fields) {
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
	const formKeys = getFormKey(params)
	// 查询字段
	params.whereField = formKeys?.length ? formKeys[0] : ''
	// 筛选值
	let whereKeys = getSelectedOptionsWithChildren(formKeys, params, fields)
	params = Object.assign(params, whereKeys)

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

export { queryCityLandUseData }
