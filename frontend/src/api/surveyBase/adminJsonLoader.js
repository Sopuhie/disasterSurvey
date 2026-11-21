/**
 * 从 public/datas 县界.json / 市界.json 导入运维行政区台账。
 * - 县级行政区：直接按 县界.json 要素录入
 * - 市级行政区：市界.json 为无名拓扑弧段；市级面由县界按「地级」合并，
 *   市界弧段挂到市级行的 boundaryLines 备查
 */
import { prefix } from '@/config'
import { DistrictData } from '/public/map.js'
import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
import EsriJSONUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
import { centroidFromGeojsonGeometry } from './hazardJsonLoader.js'

const withPrefix = path => (prefix ? '/' + prefix : '') + path

const strip = value => String(value ?? '').trim()

const toNumber = (value, fallback = 0) => {
	const n = Number(value)
	return Number.isFinite(n) ? n : fallback
}

const esriFeatureToGeojson = feat => {
	try {
		return GeoJSONUtil.writeFeatureObject(EsriJSONUtil.readFeature(feat))
	} catch (e) {
		return null
	}
}

const stringifyFeature = (geometry, properties = {}) => {
	if (!geometry) return ''
	return JSON.stringify({
		type: 'Feature',
		geometry,
		properties
	})
}

const nameMatch = (a, b) => {
	const x = strip(a)
	const y = strip(b)
	if (!x || !y) return false
	return (
		x === y ||
		x.includes(y) ||
		y.includes(x) ||
		x.replace(/[市区县]$/g, '') === y.replace(/[市区县]$/g, '')
	)
}

async function loadPopulationIndex() {
	try {
		const res = await fetch(withPrefix('/datas/人口经济.json'))
		if (!res.ok) return []
		const list = await res.json()
		return Array.isArray(list) ? list : []
	} catch (e) {
		return []
	}
}

const findPopulation = (population, name, city) => {
	return (
		(population || []).find(item => nameMatch(item.name, name)) ||
		(population || []).find(item => nameMatch(item.name, name) || nameMatch(item.shi, city)) ||
		null
	)
}

/** 合并同市县界面为 MultiPolygon（不依赖 turf，避免种子阶段重依赖） */
const mergeGeometries = features => {
	const polygons = []
	features.forEach(f => {
		const g = f?.geometry
		if (!g) return
		if (g.type === 'Polygon') polygons.push(g.coordinates)
		else if (g.type === 'MultiPolygon') polygons.push(...(g.coordinates || []))
	})
	if (!polygons.length) return null
	if (polygons.length === 1) return { type: 'Polygon', coordinates: polygons[0] }
	return { type: 'MultiPolygon', coordinates: polygons }
}

export async function loadCountyRowsFromJson() {
	const population = await loadPopulationIndex()
	const res = await fetch(withPrefix('/datas/县界.json'))
	if (!res.ok) throw new Error(`加载县界失败：${res.status}`)
	const esri = await res.json()
	const rows = []
	;(esri?.features || []).forEach(feat => {
		const attrs = feat?.attributes || {}
		const geo = esriFeatureToGeojson(feat)
		const geometry = geo?.geometry || null
		const name = strip(attrs.地名 || attrs.县级)
		if (!name) return
		const city = strip(attrs.地级)
		const pop = findPopulation(population, name, city)
		const center = centroidFromGeojsonGeometry(geometry) || { lon: 0, lat: 0 }
		rows.push({
			name,
			city,
			population: pop ? toNumber(pop.常住人口) : 0,
			density: pop ? toNumber(pop.人口密) : 0,
			statArea: pop ? toNumber(pop.面积) : toNumber(attrs.Shape_Area),
			lon: center.lon,
			lat: center.lat,
			sourceFid: attrs.FID,
			divisionCode: attrs.区划码 || attrs.县级码,
			fromSystem: true,
			geojson: stringifyFeature(geometry, { name, city, 地名: name, 地级: city })
		})
	})
	return rows
}

export async function loadCityBoundaryLines() {
	const url = DistrictData?.市界 || withPrefix('/datas/市界.json')
	const res = await fetch(url)
	if (!res.ok) throw new Error(`加载市界失败：${res.status}`)
	const esri = await res.json()
	const features = []
	;(esri?.features || []).forEach(feat => {
		const geo = esriFeatureToGeojson(feat)
		if (!geo?.geometry) return
		features.push({
			type: 'Feature',
			geometry: geo.geometry,
			properties: { ...(feat.attributes || {}), name: `市界弧段-${feat.attributes?.FID ?? features.length}` }
		})
	})
	return {
		type: 'FeatureCollection',
		features
	}
}

/**
 * 市级台账：面状范围来自县界按地级合并；boundaryLines 保留市界.json 弧段全集备查。
 */
export async function loadCityRowsFromJson(countyRows) {
	const counties = Array.isArray(countyRows) && countyRows.length ? countyRows : await loadCountyRowsFromJson()
	let boundaryLines = null
	try {
		boundaryLines = await loadCityBoundaryLines()
	} catch (e) {
		console.warn('[adminJsonLoader] 市界弧段加载失败', e)
	}
	const boundaryLinesText = boundaryLines ? JSON.stringify(boundaryLines) : ''

	const groups = new Map()
	counties.forEach(row => {
		const city = strip(row.city)
		if (!city) return
		if (!groups.has(city)) groups.set(city, [])
		groups.get(city).push(row)
	})

	const rows = []
	let boundaryAttached = false
	groups.forEach((list, city) => {
		const geometries = list
			.map(item => {
				try {
					const obj = typeof item.geojson === 'string' ? JSON.parse(item.geojson) : item.geojson
					return obj?.geometry || (obj?.type && obj?.coordinates ? obj : null)
				} catch (e) {
					return null
				}
			})
			.filter(Boolean)
			.map(geometry => ({ geometry }))
		const merged = mergeGeometries(geometries)
		const center = centroidFromGeojsonGeometry(merged) || {
			lon: list[0]?.lon || 0,
			lat: list[0]?.lat || 0
		}
		const population = list.reduce((sum, item) => sum + (Number(item.population) || 0), 0)
		const statArea = list.reduce((sum, item) => sum + (Number(item.statArea) || 0), 0)
		const row = {
			name: city,
			population: Number(population.toFixed(2)),
			density: statArea > 0 ? Number(((population * 10000) / statArea).toFixed(2)) : 0,
			statArea: Number(statArea.toFixed(2)),
			lon: center.lon,
			lat: center.lat,
			countyCount: list.length,
			fromSystem: true,
			boundaryLines: '',
			geojson: stringifyFeature(merged, { name: city, 地级: city, level: 'city' })
		}
		// 市界弧段全集只挂到第一条市级记录，避免重复占空间
		if (!boundaryAttached && boundaryLinesText) {
			row.boundaryLines = boundaryLinesText
			boundaryAttached = true
		}
		rows.push(row)
	})
	return rows
}

export async function loadAllAdminRowsFromJson() {
	const county = await loadCountyRowsFromJson()
	const city = await loadCityRowsFromJson(county)
	return { county, city }
}
