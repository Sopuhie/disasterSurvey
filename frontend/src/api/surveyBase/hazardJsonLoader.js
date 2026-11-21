/**
 * 从 public/datas 系统 JSON（EsriJSON）加载四类承灾体，映射为运维/调查台账行。
 * 写入 geojson 字段（Feature JSON 字符串）；持久化时由 geojsonPersist 旁路到 IndexedDB。
 */
import { DataNames } from '/public/map.js'
import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
import EsriJSONUtil from '@/components/map/primitive/util/EsrijsonUtil.js'

export const HAZARD_JSON_KEYS = {
	seawall: '海岸防护工程',
	fishing: '渔港',
	mariculture: '海水养殖区',
	tourist: '滨海旅游区'
}

const strip = value => String(value ?? '').trim()

const toNumber = (value, fallback = 0) => {
	const n = Number(value)
	return Number.isFinite(n) ? n : fallback
}

const avgCoords = coords => {
	if (!coords?.length) return null
	let sx = 0
	let sy = 0
	let n = 0
	coords.forEach(pt => {
		if (!Array.isArray(pt) || pt.length < 2) return
		const x = Number(pt[0])
		const y = Number(pt[1])
		if (!Number.isFinite(x) || !Number.isFinite(y)) return
		sx += x
		sy += y
		n += 1
	})
	return n ? { lon: sx / n, lat: sy / n } : null
}

/** GeoJSON geometry → 代表点 */
export function centroidFromGeojsonGeometry(geometry) {
	if (!geometry?.type) return null
	const { type, coordinates } = geometry
	if (type === 'Point') {
		const [lon, lat] = coordinates || []
		return Number.isFinite(lon) && Number.isFinite(lat) ? { lon, lat } : null
	}
	if (type === 'MultiPoint' || type === 'LineString') {
		return avgCoords(coordinates)
	}
	if (type === 'MultiLineString') {
		return avgCoords((coordinates || []).flat())
	}
	if (type === 'Polygon') {
		return avgCoords(coordinates?.[0] || [])
	}
	if (type === 'MultiPolygon') {
		return avgCoords(coordinates?.[0]?.[0] || [])
	}
	return null
}

const esriFeatureToGeojson = feat => {
	try {
		return GeoJSONUtil.writeFeatureObject(EsriJSONUtil.readFeature(feat))
	} catch (e) {
		return null
	}
}

const pickLonLat = (attrs, geometry, lonKeys, latKeys) => {
	for (let i = 0; i < lonKeys.length; i += 1) {
		const lon = toNumber(attrs[lonKeys[i]], NaN)
		const lat = toNumber(attrs[latKeys[i]], NaN)
		if (Number.isFinite(lon) && Number.isFinite(lat) && !(lon === 0 && lat === 0)) {
			return { lon, lat }
		}
	}
	return centroidFromGeojsonGeometry(geometry) || { lon: 0, lat: 0 }
}

const formatReturnPeriod = value => {
	const text = strip(value)
	if (!text || text === '-') return ''
	if (/年/.test(text)) return text
	const n = Number(text)
	return Number.isFinite(n) && n > 0 ? `${n}年一遇` : text
}

const parseBuiltYear = value => {
	const text = strip(value)
	if (!text || text === '-') return undefined
	const m = text.match(/(\d{4})/)
	return m ? Number(m[1]) : undefined
}

const inferFishingType = (name, type) => {
	const t = strip(type)
	if (t) return t
	const m = String(name || '').match(/(中心渔港|一级渔港|二级渔港|三级渔港|渔港)/)
	return m ? m[1] : '渔港'
}

const mapSeawall = (attrs, geometry) => {
	const name = strip(attrs.dangerName)
	const { lon, lat } = pickLonLat(attrs, geometry, [], [])
	const lengthKm = toNumber(attrs.length)
	const hdxl = strip(attrs.HDXL)
	return {
		name,
		dangerName: name,
		division: strip(attrs.division),
		HDXL: hdxl,
		length: lengthKm,
		lengthKm,
		Return_per: strip(attrs.Return_per),
		standard: formatReturnPeriod(attrs.Return_per),
		Dam_elevat: strip(attrs.Dam_elevat),
		time: strip(attrs.time),
		builtYear: parseBuiltYear(attrs.time),
		type: hdxl || '海堤',
		town: '',
		lon,
		lat,
		areaKm2: 0,
		No: attrs.No,
		sourceFid: attrs.FID,
		fromSystem: true
	}
}

const mapFishingOrMariculture = (attrs, geometry, kind) => {
	const name = strip(attrs.dangerName)
	const { lon, lat } = pickLonLat(attrs, geometry, ['longtitude', 'longitude'], ['latitude', 'latitude'])
	const areaKm2 = toNumber(attrs.area) || toNumber(attrs.Shape_Area)
	const method = strip(attrs.method)
	const typeRaw = strip(attrs.type)
	const base = {
		name,
		dangerName: name,
		division: strip(attrs.division),
		method,
		type: kind === 'fishing' ? inferFishingType(name, typeRaw) : typeRaw || method || (kind === 'mariculture' ? '海水养殖区' : ''),
		town: strip(attrs.town),
		lon,
		lat,
		longtitude: toNumber(attrs.longtitude),
		latitude: toNumber(attrs.latitude),
		host: strip(attrs.host),
		manager: strip(attrs.host),
		area: toNumber(attrs.area),
		areaKm2,
		Shape_Leng: toNumber(attrs.Shape_Leng),
		Shape_Area: toNumber(attrs.Shape_Area),
		sourceId: strip(attrs.id),
		sourceFid: attrs.FID,
		fromSystem: true
	}
	if (kind === 'fishing') {
		return { ...base, berths: undefined, capacity: '', builtYear: undefined, status: '在用' }
	}
	return { ...base, species: '', status: '在养' }
}

const mapTourist = (attrs, geometry) => {
	const name = strip(attrs.dangerName)
	const { lon, lat } = pickLonLat(attrs, geometry, ['longitude', 'longtitude'], ['latitude', 'latitude'])
	const areaText = strip(attrs['面积'])
	const areaKm2 = toNumber(areaText)
	return {
		name,
		dangerName: name,
		division: strip(attrs.division),
		type: '滨海旅游区',
		town: '',
		lon,
		lat,
		longitude: toNumber(attrs.longitude),
		latitude: toNumber(attrs.latitude),
		面积: areaText,
		areaKm2: Number.isFinite(areaKm2) && areaKm2 > 0 ? areaKm2 : 0,
		Management: strip(attrs.Management),
		manager: strip(attrs.Management),
		level: '',
		status: '开放',
		sourceFid: attrs.FID,
		fromSystem: true
	}
}

const mappers = {
	seawall: mapSeawall,
	fishing: (attrs, geometry) => mapFishingOrMariculture(attrs, geometry, 'fishing'),
	mariculture: (attrs, geometry) => mapFishingOrMariculture(attrs, geometry, 'mariculture'),
	tourist: mapTourist
}

const stringifyFeature = (geometry, properties = {}) => {
	if (!geometry) return ''
	return JSON.stringify({
		type: 'Feature',
		geometry,
		properties
	})
}

/**
 * 加载并映射某一类承灾体台账行（含 geojson 字段）
 * @param {'seawall'|'fishing'|'mariculture'|'tourist'} typeId
 */
export async function loadHazardRowsFromJson(typeId) {
	const dataKey = HAZARD_JSON_KEYS[typeId]
	const url = DataNames[dataKey]
	const mapper = mappers[typeId]
	if (!url || !mapper) return []

	const res = await fetch(url)
	if (!res.ok) throw new Error(`加载 ${dataKey} 失败：${res.status}`)
	const esri = await res.json()
	const features = esri?.features || []
	const rows = []
	features.forEach(feat => {
		const attrs = feat?.attributes || {}
		const geo = esriFeatureToGeojson(feat)
		const geometry = geo?.geometry || null
		const row = mapper(attrs, geometry)
		if (!row.name) return
		row.geojson = stringifyFeature(geometry, {
			name: row.name,
			dangerName: row.name,
			sourceFid: row.sourceFid
		})
		rows.push(row)
	})
	return rows
}

/** 并行加载四类 */
export async function loadAllHazardRowsFromJson() {
	const typeIds = Object.keys(HAZARD_JSON_KEYS)
	const entries = await Promise.all(
		typeIds.map(async typeId => {
			try {
				const rows = await loadHazardRowsFromJson(typeId)
				return [typeId, rows]
			} catch (e) {
				console.warn(`[hazardJsonLoader] ${typeId} 加载失败`, e)
				return [typeId, []]
			}
		})
	)
	return Object.fromEntries(entries)
}
