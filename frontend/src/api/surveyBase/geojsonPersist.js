/**
 * 台账 GeoJSON 旁路持久化：属性仍走 localStorage，几何走 IndexedDB，避免配额溢出。
 */
const DB_NAME = 'survey_base_geojson_v1'
const STORE_NAME = 'byKey'
const GEOJSON_TYPES = new Set([
	'county',
	'city',
	'town',
	'seawall',
	'fishing',
	'mariculture',
	'tourist',
	'rsScene',
	'assessSar',
	'assessStructure',
	'assessImpactZone'
])

const openDb = () =>
	new Promise((resolve, reject) => {
		if (typeof indexedDB === 'undefined') {
			reject(new Error('indexedDB unavailable'))
			return
		}
		const req = indexedDB.open(DB_NAME, 1)
		req.onupgradeneeded = () => {
			const db = req.result
			if (!db.objectStoreNames.contains(STORE_NAME)) {
				db.createObjectStore(STORE_NAME)
			}
		}
		req.onsuccess = () => resolve(req.result)
		req.onerror = () => reject(req.error || new Error('open indexedDB failed'))
	})

const geoKey = (type, id) => `${type}:${id}`

export function extractGeojsonMap(store) {
	const map = {}
	const lean = {}
	Object.keys(store || {}).forEach(type => {
		const list = store[type]
		if (!Array.isArray(list)) {
			lean[type] = list
			return
		}
		if (!GEOJSON_TYPES.has(type)) {
			lean[type] = list
			return
		}
		lean[type] = list.map(item => {
			if (!item || typeof item !== 'object') return item
			const next = { ...item }
			const raw = next.geojson
			if (raw != null && String(raw).trim() !== '') {
				map[geoKey(type, next.id)] = typeof raw === 'string' ? raw : JSON.stringify(raw)
			}
			delete next.geojson
			return next
		})
	})
	return { lean, map }
}

export function applyGeojsonMap(store, map = {}) {
	if (!store || !map) return store
	Object.keys(store).forEach(type => {
		if (!GEOJSON_TYPES.has(type) || !Array.isArray(store[type])) return
		store[type].forEach(item => {
			if (!item?.id) return
			const hit = map[geoKey(type, item.id)]
			if (hit != null && hit !== '' && (item.geojson == null || item.geojson === '')) {
				item.geojson = hit
			}
		})
	})
	return store
}

/** 用种子行的 geojson 按 id/sourceFid/name 回填缺失字段 */
export function fillMissingGeojson(targetStore, seedStore) {
	GEOJSON_TYPES.forEach(type => {
		const target = targetStore?.[type]
		const seed = seedStore?.[type]
		if (!Array.isArray(target) || !Array.isArray(seed) || !seed.length) return

		const byFid = new Map()
		const byName = new Map()
		seed.forEach(row => {
			if (!row?.geojson) return
			if (row.sourceFid != null) byFid.set(String(row.sourceFid), row.geojson)
			if (row.name) byName.set(String(row.name).trim(), row.geojson)
		})

		target.forEach(row => {
			if (!row || (row.geojson != null && String(row.geojson).trim() !== '')) return
			const fromFid = row.sourceFid != null ? byFid.get(String(row.sourceFid)) : null
			const fromName = row.name ? byName.get(String(row.name).trim()) : null
			row.geojson = fromFid || fromName || ''
		})
	})
	return targetStore
}

export async function saveGeojsonMap(map) {
	try {
		const db = await openDb()
		await new Promise((resolve, reject) => {
			const tx = db.transaction(STORE_NAME, 'readwrite')
			const store = tx.objectStore(STORE_NAME)
			store.clear()
			Object.keys(map || {}).forEach(key => {
				store.put(map[key], key)
			})
			tx.oncomplete = () => resolve()
			tx.onerror = () => reject(tx.error || new Error('save geojson failed'))
		})
		db.close()
	} catch (e) {
		console.warn('[geojsonPersist] save failed', e)
	}
}

export async function loadGeojsonMap() {
	try {
		const db = await openDb()
		const map = await new Promise((resolve, reject) => {
			const tx = db.transaction(STORE_NAME, 'readonly')
			const store = tx.objectStore(STORE_NAME)
			const req = store.getAllKeys()
			req.onsuccess = () => {
				const keys = req.result || []
				const valuesReq = store.getAll()
				valuesReq.onsuccess = () => {
					const values = valuesReq.result || []
					const out = {}
					keys.forEach((key, idx) => {
						out[key] = values[idx]
					})
					resolve(out)
				}
				valuesReq.onerror = () => reject(valuesReq.error)
			}
			req.onerror = () => reject(req.error)
		})
		db.close()
		return map
	} catch (e) {
		console.warn('[geojsonPersist] load failed', e)
		return {}
	}
}
