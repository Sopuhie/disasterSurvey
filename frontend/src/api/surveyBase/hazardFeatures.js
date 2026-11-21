/**
 * 承灾体空间要素：优先使用运维管理录入的 GeoJSON，否则按经纬度生成示意几何。
 */
import {
	makeCircleFeature,
	makePointFeature
} from '@/views/survey/mock/surveyDemoData.js'

const GEOMETRY_TYPES = new Set([
	'Point',
	'MultiPoint',
	'LineString',
	'MultiLineString',
	'Polygon',
	'MultiPolygon',
	'GeometryCollection'
])

export function parseStoredGeojson(raw) {
	if (raw == null || raw === '') return null
	if (typeof raw === 'object') return raw
	const text = String(raw).trim()
	if (!text) return null
	try {
		return JSON.parse(text)
	} catch (e) {
		return null
	}
}

export function featureFromStoredGeojson(raw, properties = {}) {
	const obj = parseStoredGeojson(raw)
	if (!obj?.type) return null

	const mergeProps = base => {
		const next = { ...(base || {}), ...properties }
		delete next.geojson
		return next
	}

	if (obj.type === 'FeatureCollection') {
		const first = obj.features?.find(item => item?.geometry)
		if (!first) return null
		return {
			type: 'Feature',
			geometry: first.geometry,
			properties: mergeProps(first.properties)
		}
	}
	if (obj.type === 'Feature') {
		if (!obj.geometry) return null
		return {
			type: 'Feature',
			geometry: obj.geometry,
			properties: mergeProps(obj.properties)
		}
	}
	if (GEOMETRY_TYPES.has(obj.type)) {
		return {
			type: 'Feature',
			geometry: obj,
			properties: mergeProps({})
		}
	}
	return null
}

function syntheticHazardFeature(item, typeId) {
	const props = { dangerName: item.name, ...item }
	delete props.geojson
	if (typeId === 'tourist') {
		return makePointFeature(item.lon, item.lat, props)
	}
	if (typeId === 'seawall') {
		const dx = 0.04
		return {
			type: 'Feature',
			geometry: {
				type: 'LineString',
				coordinates: [
					[item.lon - dx, item.lat],
					[item.lon, item.lat + 0.015],
					[item.lon + dx, item.lat]
				]
			},
			properties: props
		}
	}
	return makeCircleFeature(item.lon, item.lat, Math.max(Number(item.areaKm2) || 1.2, 1.2), props)
}

/** 单条系统承灾体记录 → GeoJSON Feature */
export function makeSystemHazardFeature(item, typeId) {
	if (!item) return null
	const props = { dangerName: item.name, name: item.name, ...item }
	delete props.geojson
	const fromGeo = featureFromStoredGeojson(item.geojson, props)
	if (fromGeo) return fromGeo
	if (!Number.isFinite(Number(item.lon)) || !Number.isFinite(Number(item.lat))) return null
	return syntheticHazardFeature(item, typeId)
}

export const HAZARD_TARGET_LABEL = {
	seawall: '海堤',
	fishing: '渔港',
	mariculture: '海水养殖区',
	tourist: '滨海旅游区'
}

/** 评估破坏分析承灾体类别（与调查四类及海岸线统一） */
export const ASSESS_TARGET_LABEL = {
	seawall: '海堤',
	fishing: '渔港',
	mariculture: '海水养殖区',
	tourist: '滨海旅游区'
}

export function countyFromDivision(division = '') {
	const parts = String(division).split('-').filter(Boolean)
	return parts[parts.length - 1] || ''
}
