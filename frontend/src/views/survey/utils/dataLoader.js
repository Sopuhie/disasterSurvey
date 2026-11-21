import * as turf from '@turf/turf'
import { prefix } from '@/config'
import { DataNames } from '/public/map.js'
import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
import EsriJSONUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
import { CITY_NAMES, HAZARD_TYPES } from '@/views/survey/constants.js'

const withPrefix = path => (prefix ? '/' + prefix : '') + path

const cache = {
	counties: null,
	cities: null,
	population: null,
	cityCenters: null,
	hazards: {}
}

/** Esri rings → GeoJSON Polygon / MultiPolygon（OL 解析失败时的兜底） */
const ringsToGeometry = rings => {
	if (!Array.isArray(rings) || !rings.length) return null
	if (rings.length === 1) {
		return { type: 'Polygon', coordinates: rings }
	}
	// 多环按多个外环处理（县界数据以独立外环为主）
	return {
		type: 'MultiPolygon',
		coordinates: rings.map(ring => [ring])
	}
}

const toFeature = feat => {
	if (!feat) return null
	// 已是 GeoJSON Feature
	if (feat.type === 'Feature' && feat.geometry) return feat
	try {
		const olFeat = EsriJSONUtil.readFeature(feat)
		if (olFeat) return GeoJSONUtil.writeFeatureObject(olFeat)
	} catch (err) {
		/* fallback below */
	}
	const geometry = ringsToGeometry(feat.geometry?.rings)
	if (!geometry) return null
	return {
		type: 'Feature',
		geometry,
		properties: { ...(feat.attributes || feat.properties || {}) }
	}
}

const toFeatureCollection = esri => {
	if (!esri?.features?.length) return []
	return esri.features.map(toFeature).filter(Boolean)
}

export async function loadPopulation() {
	if (cache.population) return cache.population
	const res = await fetch(DataNames['人口经济'])
	cache.population = await res.json()
	return cache.population
}

async function loadCityCenters() {
	if (cache.cityCenters) return cache.cityCenters
	try {
		const esri = await fetch(withPrefix('/datas/行政区划中心点.json')).then(res => res.json())
		cache.cityCenters = toFeatureCollection(esri)
		return cache.cityCenters
	} catch (err) {
		cache.cityCenters = []
		return []
	}
}

const findCityCenter = (centers, cityName) => {
	const keyword = (cityName || '').replace(/市$/g, '')
	return centers.find(item => {
		const label = item.properties?.label || item.properties?.地名 || ''
		return label === keyword || label.includes(keyword) || keyword.includes(label)
	})
}

async function buildFallbackCounties(population) {
	const centers = await loadCityCenters()
	return (population || []).map(item => {
		const center = findCityCenter(centers, item.shi) || findCityCenter(centers, item.name)
		const coords = center?.geometry?.coordinates || [119.3, 26.08]
		const radius = Math.max(Math.sqrt((Number(item.面积) || 400) / Math.PI), 6)
		const circle = turf.circle(coords, radius, { steps: 48, units: 'kilometers' })
		circle.properties = {
			地名: item.name,
			name: item.name,
			city: item.shi,
			population: Number(item.常住人口),
			density: Number(item.人口密),
			statArea: Number(item.面积)
		}
		return circle
	})
}

export async function loadCountyFeatures() {
	if (cache.counties) return cache.counties
	const population = await loadPopulation()
	try {
		const res = await fetch(withPrefix('/datas/县界.json'))
		if (res.ok) {
			const esri = await res.json()
			const features = toFeatureCollection(esri).map(feature => mergePopulation(feature, population))
			if (features.length) {
				cache.counties = features
				return cache.counties
			}
		}
	} catch (err) {
		// fallback below
	}
	cache.counties = await buildFallbackCounties(population)
	return cache.counties
}

export async function loadCityFeatures() {
	if (cache.cities) return cache.cities
	try {
		const res = await fetch(withPrefix('/datas/行政区划.json'))
		if (res.ok) {
			const esri = await res.json()
			const features = toFeatureCollection(esri).map(feature => {
				feature.properties = {
					...feature.properties,
					name: feature.properties?.地级 || feature.properties?.地名 || '',
					city: feature.properties?.地级 || ''
				}
				return feature
			})
			if (features.length) {
				cache.cities = features
				return cache.cities
			}
		}
	} catch (err) {
		// fallback below
	}
	const counties = await loadCountyFeatures()
	cache.cities = CITY_NAMES.map(name => {
		return counties.find(item => (item.properties?.city || '').includes(name.replace('市', '')) || name.includes(item.properties?.city || ''))
	}).filter(Boolean)
	return cache.cities
}

export async function loadHazardFeatures(typeId) {
	if (cache.hazards[typeId]) return cache.hazards[typeId]
	const config = HAZARD_TYPES.find(item => item.id === typeId)
	if (!config) return []
	try {
		const res = await fetch(DataNames[config.dataKey])
		if (!res.ok) {
			cache.hazards[typeId] = []
			return []
		}
		const esri = await res.json()
		cache.hazards[typeId] = toFeatureCollection(esri)
		return cache.hazards[typeId]
	} catch (err) {
		cache.hazards[typeId] = []
		return []
	}
}

export async function loadAllHazardFeatures() {
	const entries = await Promise.all(HAZARD_TYPES.map(async item => [item.id, await loadHazardFeatures(item.id)]))
	return Object.fromEntries(entries)
}

export function mergePopulation(feature, population) {
	const name = feature.properties?.地名 || feature.properties?.name || ''
	const city = feature.properties?.地级 || feature.properties?.shi || ''
	const pop = (population || []).find(
		item => item.name === name || name.includes(item.name) || item.name.includes(name)
	)
	feature.properties = {
		...feature.properties,
		name,
		city: pop?.shi || city,
		population: pop ? Number(pop.常住人口) : null,
		density: pop ? Number(pop.人口密) : null,
		statArea: pop ? Number(pop.面积) : null
	}
	return feature
}

export function getDistrictName(feature) {
	return (
		feature?.properties?.地名 ||
		feature?.properties?.县级 ||
		feature?.properties?.name ||
		feature?.properties?.地级 ||
		''
	)
}

export function isCityName(name) {
	const text = String(name || '').trim()
	if (!text) return false
	return CITY_NAMES.some(item => item === text || item.includes(text) || text.includes(item.replace(/市$/, '')))
}

const nameMatch = (a, b) => {
	const x = String(a || '').trim()
	const y = String(b || '').trim()
	if (!x || !y) return false
	return x === y || x.includes(y) || y.includes(x) || x.replace(/[市区县]$/g, '') === y.replace(/[市区县]$/g, '')
}

const unionCountyFeatures = (features, props = {}) => {
	if (!features?.length) return null
	if (features.length === 1) {
		return {
			...features[0],
			properties: { ...features[0].properties, ...props }
		}
	}
	try {
		const merged = features.reduce((acc, cur) => {
			try {
				return turf.union(acc, cur) || acc
			} catch (err) {
				return acc
			}
		})
		if (!merged) return features[0]
		merged.properties = { ...(merged.properties || {}), ...props }
		return merged
	} catch (err) {
		return {
			...features[0],
			properties: { ...features[0].properties, ...props }
		}
	}
}

const featureToDistrictItem = (feature, fallbackName = '') => {
	if (!feature) return null
	const name = getDistrictName(feature) || fallbackName
	const city = feature.properties?.city || feature.properties?.地级 || ''
	let lon = feature.properties?.lon
	let lat = feature.properties?.lat
	try {
		const c = turf.centroid(feature)
		lon = c.geometry.coordinates[0]
		lat = c.geometry.coordinates[1]
	} catch (err) {
		/* keep props */
	}
	return {
		name,
		city,
		population: feature.properties?.population ?? null,
		density: feature.properties?.density ?? null,
		statArea: feature.properties?.statArea ?? null,
		lon,
		lat,
		feature
	}
}

/**
 * 按名称解析真实行政区范围：县/区用县界；市名则合并该市下全部县界面。
 * 优先本地 public/datas/县界.json，避免示意圆面。
 */
export async function resolveAdminFeatureByName(name) {
	const keyword = String(name || '').trim()
	if (!keyword) return null
	const counties = await loadCountyFeatures()
	if (isCityName(keyword)) {
		const cityKey = CITY_NAMES.find(item => item === keyword || item.includes(keyword) || keyword.includes(item.replace(/市$/, ''))) || keyword
		const parts = counties.filter(item => {
			const city = item.properties?.city || item.properties?.地级 || ''
			return nameMatch(city, cityKey) || nameMatch(city, keyword)
		})
		const feature = unionCountyFeatures(parts, {
			name: cityKey,
			地名: cityKey,
			city: cityKey,
			地级: cityKey,
			level: 'city'
		})
		return featureToDistrictItem(feature, cityKey)
	}
	const hit =
		counties.find(item => nameMatch(getDistrictName(item), keyword)) ||
		counties.find(item => nameMatch(item.properties?.县级, keyword))
	return featureToDistrictItem(hit, keyword)
}

/** 按坐标落点解析真实县界范围 */
export async function resolveAdminFeatureByCoord(lon, lat) {
	const x = Number(lon)
	const y = Number(lat)
	if (!Number.isFinite(x) || !Number.isFinite(y)) return null
	const counties = await loadCountyFeatures()
	const point = turf.point([x, y])
	const hit = counties.find(item => {
		try {
			return turf.booleanPointInPolygon(point, item)
		} catch (err) {
			return false
		}
	})
	return featureToDistrictItem(hit)
}

export async function getDistrictSuggestions() {
	const [counties, population] = await Promise.all([loadCountyFeatures(), loadPopulation()])
	const names = new Set([
		...CITY_NAMES,
		...counties.map(getDistrictName).filter(Boolean),
		...population.map(item => item.name).filter(Boolean)
	])
	return [...names].map(value => ({ value }))
}
