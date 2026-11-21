import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
import EsrijsonUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
import _ from 'lodash'

import { useAppStore } from '@/store/modules/app.js'
const appStore = useAppStore()

import { useMapStore } from '@/store/modules/map.js'
const mapStore = useMapStore()

import { arcGisApi } from '@/api/gis/arcgis.js'
const api = arcGisApi()

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
 * 分组字段
 */
let groupFields = ['企业类型', '所属行业']

/**
 * 设置查询条件
 */
const getFilters = () => {
	let filters = { where: `1=1`, geometry: '' }
	if (params.企业类型?.length) {
		let strs = params.企业类型.map(m => "'" + m + "'").join(',')
		filters.where = filters.where + ` AND ${'企业类型'} IN (${strs})`
	}
	if (params.所属行业?.length) {
		let strs = params.所属行业.map(m => "'" + m + "'").join(',')
		filters.where = filters.where + ` AND ${'所属行业'} IN (${strs})`
	}
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
 * 设置表格
 */
async function singleTable() {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let field = groupFields.find(n => n != params.group)
		let header = _.uniq(res.map(feat => feat.properties[field]))
		let headers = [{ prop: params.group, label: params.group }].concat(
			header.map(n => {
				return { prop: n, label: n }
			})
		)
		tableHeaders.value = headers

		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[params.group]
			let res = groupFeatures[val] || []
			res.push(feat)
			groupFeatures[val] = res
		})
		for (let [key, val] of Object.entries(groupFeatures)) {
			let mapVals = headers.map(hName => {
				return val.filter(feat => feat.properties[field] == hName.prop).length
			})
			mapVals[0] = key
			tableDatas.value.push(
				headers.reduce((accumulator, currentValue, index) => {
					accumulator[currentValue.prop] = mapVals[index]
					return accumulator
				}, {})
			)
		}
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
 * 单一行业类型不同企业类型占比
 */
async function singleIndustry() {
	if (params.所属行业?.length) {
		params.所属行业 = [params.所属行业[0]]
	}
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let field = groupFields.find(n => n != '所属行业')
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[field]
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
				text: '单一行业类型不同企业类型占比',
				subtext: '',
				left: 'center',
				top: '12px'
			},
			tooltip: { trigger: 'item' },
			legend: {
				orient: 'vertical',
				left: 'left',
				top: '16px'
			},
			series: [
				{
					name: '企业数量',
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
		loading.close()
		ElMessage({
			showClose: true,
			message: `未查询到数据，请检查查询条件`,
			type: 'warning'
		})
	}
}

/**
 * 单一企业类型不同行业类型占比
 */
async function singleEnterprise() {
	if (params.企业类型?.length) {
		params.企业类型 = [params.企业类型[0]]
	}
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let field = groupFields.find(n => n != '企业类型')
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[field]
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
				text: '单一企业类型不同行业类型占比',
				subtext: '',
				left: 'center',
				top: '12px'
			},
			tooltip: { trigger: 'item' },
			legend: {
				orient: 'vertical',
				left: 'left',
				top: '16px'
			},
			series: [
				{
					name: '企业数量',
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
		loading.close()
		ElMessage({
			showClose: true,
			message: `未查询到数据，请检查查询条件`,
			type: 'warning'
		})
	}
}

/**
 * 单一行业类型不同企业类型数量占比
 */
async function singleIndustryMultiEnterprise() {
	if (params.所属行业?.length) {
		params.所属行业 = [params.所属行业[0]]
	}
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let field = groupFields.find(n => n != '所属行业')
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[field]
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
				text: '单一行业类型不同企业类型数量占比',
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
			series: [{ data: datas.map(n => n.value), type: 'bar' }]
		}
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
 * 单一企业类型不同行业类型数量占比
 */
async function singleEnterpriseMultiIndustry() {
	if (params.企业类型?.length) {
		params.企业类型 = [params.企业类型[0]]
	}
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let field = groupFields.find(n => n != '企业类型')
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[field]
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
				text: '单一企业类型不同行业类型数量占比',
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
			series: [{ data: datas.map(n => n.value), type: 'bar' }]
		}
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
 * 不同行业数量占比
 */
async function multiIndustry() {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let field = groupFields.find(n => n != '所属行业')
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[field]
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
				text: '不同行业数量占比',
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
			series: [{ data: datas.map(n => n.value), type: 'bar' }]
		}
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
 * 不同企业数量占比
 */
async function multiEnterprise() {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let field = groupFields.find(n => n != '企业类型')
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties[field]
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
				text: '不同行业数量占比',
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
			series: [{ data: datas.map(n => n.value), type: 'bar' }]
		}
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
 * N个行业不同企业类型占比
 */
async function multiIndustryMultiEnterprise() {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties['企业类型']
			let res = groupFeatures[val] || []
			res.push(feat)
			groupFeatures[val] = res
		})

		let datas = []
		for (let [key, val] of Object.entries(groupFeatures)) {
			datas.push({
				name: key,
				value: params.所属行业.map(n => {
					return val.filter(m => m.properties['所属行业'] == n).length
				})
			})
		}

		chartOption.value = {
			tooltip: {
				trigger: 'axis',
				axisPointer: {
					type: 'cross'
				}
			},
			legend: {
				data: params.企业类型,
				top: '5%',
				left: 'center'
			},
			xAxis: {
				type: 'category',
				data: params.所属行业,
				axisPointer: { type: 'shadow' }
			},
			yAxis: {
				type: 'value',
				name: '数量',
				interval: 10,
				axisLabel: { formatter: '{value} 个' }
			},
			series: datas.map(m => {
				return {
					name: m.name,
					type: 'bar',
					tooltip: {
						valueFormatter: function (value) {
							return value + ' 个'
						}
					},
					data: m.value
				}
			})
		}
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
 * N类企业不同行业类型占比
 */
async function multiEnterpriseMultiIndustry() {
	let filters = getFilters()
	let res = await checkSpatialDatas(filters)
	if (res?.length) {
		let groupFeatures = {}
		res.forEach(feat => {
			let val = feat.properties['所属行业']
			let res = groupFeatures[val] || []
			res.push(feat)
			groupFeatures[val] = res
		})

		let datas = []
		for (let [key, val] of Object.entries(groupFeatures)) {
			datas.push({
				name: key,
				value: params.企业类型.map(n => {
					return val.filter(m => m.properties['企业类型'] == n).length
				})
			})
		}

		chartOption.value = {
			tooltip: {
				trigger: 'axis',
				axisPointer: {
					type: 'cross'
				}
			},
			legend: {
				data: params.所属行业,
				top: '5%',
				left: 'center'
			},
			xAxis: {
				type: 'category',
				data: params.企业类型,
				axisPointer: { type: 'shadow' }
			},
			yAxis: {
				type: 'value',
				name: '数量',
				interval: 10,
				axisLabel: { formatter: '{value} 个' }
			},
			series: datas.map(m => {
				return {
					name: m.name,
					type: 'bar',
					tooltip: {
						valueFormatter: function (value) {
							return value + ' 个'
						}
					},
					data: m.value
				}
			})
		}
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
function queryIndustryData(configInfo, item, headers, datas, option) {
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
	try {
		switch (item.result) {
			case 'table':
				return singleTable()
			case 'singleIndustry':
				return singleIndustry()
			case 'singleEnterprise':
				return singleEnterprise()
			case 'singleIndustryMultiEnterprise':
				return singleIndustryMultiEnterprise()
			case 'singleEnterpriseMultiIndustry':
				return singleEnterpriseMultiIndustry()
			case 'multiIndustry':
				return multiIndustry()
			case 'multiEnterprise':
				return multiEnterprise()
			case 'multiIndustryMultiEnterprise':
				return multiIndustryMultiEnterprise()
			case 'multiEnterpriseMultiIndustry':
				return multiEnterpriseMultiIndustry()
		}
	} catch (e) {
		console.log(e)
		loading.close()
	}
}

export { queryIndustryData }
