/**
 * 按 sourceFid / 名称，用本地系统 JSON 回填台账行缺失的 geojson。
 */
import { loadAllHazardRowsFromJson } from './hazardJsonLoader.js'
import { loadAllAdminRowsFromJson } from './adminJsonLoader.js'

const ENRICH_TYPES = new Set(['seawall', 'fishing', 'mariculture', 'tourist', 'county', 'city'])

let cachePromise = null

const loadSeedPack = () => {
	if (!cachePromise) {
		cachePromise = Promise.all([
			loadAllHazardRowsFromJson(),
			loadAllAdminRowsFromJson().catch(() => ({ county: [], city: [] }))
		])
			.then(([hazards, admin]) => ({ ...hazards, ...admin }))
			.catch(err => {
				cachePromise = null
				console.warn('[geojsonEnrich] seed load failed', err)
				return {}
			})
	}
	return cachePromise
}

const hasGeo = value => {
	if (value == null || value === '') return false
	if (typeof value === 'object') return Object.keys(value).length > 0
	return String(value).trim().length > 0
}

const buildIndex = rows => {
	const byFid = new Map()
	const byName = new Map()
	;(rows || []).forEach(row => {
		if (!hasGeo(row?.geojson)) return
		if (row.sourceFid != null) byFid.set(String(row.sourceFid), row.geojson)
		if (row.name) byName.set(String(row.name).trim(), row.geojson)
	})
	return { byFid, byName }
}

export async function enrichRowsWithLocalGeojson(type, list) {
	if (!ENRICH_TYPES.has(type) || !Array.isArray(list) || !list.length) return list
	const need = list.some(item => !hasGeo(item?.geojson))
	if (!need) return list

	const pack = await loadSeedPack()
	const { byFid, byName } = buildIndex(pack[type] || [])
	if (!byFid.size && !byName.size) return list

	return list.map(item => {
		if (!item || hasGeo(item.geojson)) return item
		const fromFid = item.sourceFid != null ? byFid.get(String(item.sourceFid)) : null
		const fromName = item.name ? byName.get(String(item.name).trim()) : null
		const geojson = fromFid || fromName || ''
		if (!geojson) return item
		return { ...item, geojson }
	})
}

export function clearGeojsonEnrichCache() {
	cachePromise = null
}
