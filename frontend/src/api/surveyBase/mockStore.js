import { remoteSensingScenes } from '@/views/survey/mock/remoteSensingScenes.js'
import { loadTownRowsFromJson } from './townJsonLoader.js'
import { reportedLossData } from '@/views/survey/mock/reportedData.js'
import { sarScenes, typhoonEvents } from '@/views/assessment/mock/sarScenes.js'
import { countyEconomy, landUsePrices } from '@/views/assessment/mock/economyData.js'
import {
	structureRows,
	windDamageRows,
	waveDamageRows,
	waveErosionRows,
	windInundationRows
} from '@/views/assessment/mock/impactData.js'
import { reportSources, integrateRecords } from '@/views/assessment/mock/integrateData.js'
import { loadAllHazardRowsFromJson } from './hazardJsonLoader.js'
import { loadAllAdminRowsFromJson } from './adminJsonLoader.js'
import { loadOvertopSeedRows } from './overtopSeed.js'
import { km2ToWanMu, DISASTER_LEVEL_CN } from '@/shared/metricUnits.js'
import {
	applyGeojsonMap,
	extractGeojsonMap,
	fillMissingGeojson,
	loadGeojsonMap,
	saveGeojsonMap
} from './geojsonPersist.js'

/** v17：乡镇.json（含 geojson 边界）正式入库 */
const STORAGE_KEY = 'survey_base_data_v17'
const ADMIN_BOUNDARY_IMPORT = 'adminBoundaryImport'
const ADMIN_BOUNDARY_IMPORT_VERSION = 'v2'
const TOWN_IMPORT = 'townImport'
const TOWN_IMPORT_VERSION = 'v3'
const HAZARD_TYPES = ['seawall', 'fishing', 'mariculture', 'tourist']
const GEOJSON_SEED_TYPES = ['county', 'city', ...HAZARD_TYPES]
const HAZARD_TYPE_LABEL = {
	seawall: '海堤',
	fishing: '渔港',
	mariculture: '海水养殖区',
	tourist: '滨海旅游区'
}
const INTEGRATE_SOURCE_NAME = {
	speed: '海洋灾害灾情速报子系统',
	local: '地方应急上报',
	marine: '海洋观测站报文'
}

const uid = () => `sb_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`

const clone = data => JSON.parse(JSON.stringify(data))

const enrichHazard = (item, index, typeId = '') => {
	const level = Number(item.disasterLevel) || (index % 4) + 1
	const isSeawall = typeId === 'seawall'
	const lengthKm = Number(item.lengthKm || item.length || 0) || 1.2
	const areaKm2 = Number(item.areaKm2 || item.area || 0) || 1.2
	/** 海堤：监测/计划按长度 km；其余承灾体按万亩 */
	const base = isSeawall ? lengthKm : km2ToWanMu(areaKm2)
	return {
		...item,
		monitorValue: item.monitorValue ?? Number(base.toFixed(2)),
		planValue: item.planValue ?? Number((base * 1.12).toFixed(2)),
		monitorUnit: isSeawall ? 'km' : '万亩',
		planUnit: isSeawall ? 'km' : '万亩',
		disasterLevel: level,
		disasterLevelLabel: item.disasterLevelLabel || DISASTER_LEVEL_CN[level],
		affectedPopulation: item.affectedPopulation ?? 600 + index * 280,
		economicLoss: item.economicLoss ?? 80 + index * 36
	}
}

const seedReported = hazardPack => {
	const rows = []
	Object.keys(reportedLossData).forEach(hazardType => {
		const firstName = hazardPack[hazardType]?.[0]?.name || ''
		;(reportedLossData[hazardType] || []).forEach((item, index) => {
			const level = Number(item.disasterLevel) || (index % 4) + 1
			rows.push({
				id: uid(),
				hazardType,
				hazardTypeLabel: HAZARD_TYPE_LABEL[hazardType] || hazardType,
				targetName: item.targetName || (index === 0 ? firstName : ''),
				disasterLevel: level,
				disasterLevelLabel: item.disasterLevelLabel || DISASTER_LEVEL_CN[level],
				...item
			})
		})
	})
	return rows
}

const seedLandPrice = () =>
	Object.keys(landUsePrices).map(name => ({
		id: uid(),
		name,
		price: landUsePrices[name],
		unit: '万元/km²',
		remark: ''
	}))

const seedCountyEconomy = () =>
	countyEconomy.map(item => ({
		id: uid(),
		name: item.name,
		gdp: item.gdp,
		landFarm: item.land?.耕地 ?? 0,
		landBuild: item.land?.建设用地 ?? 0,
		landAqua: item.land?.养殖水面 ?? 0,
		landForest: item.land?.林地 ?? 0,
		landWater: item.land?.水域 ?? 0
	}))

const mapHazardPack = hazardPack => {
	const out = {}
	HAZARD_TYPES.forEach(typeId => {
		out[typeId] = (hazardPack[typeId] || []).map((item, index) => ({
			id: uid(),
			...enrichHazard(item, index, typeId)
		}))
	})
	return out
}

const seedDamage = () => [
	...windDamageRows.map(item => ({ id: uid(), kind: 'wind', hazardCode: '', ...item })),
	...waveDamageRows.map(item => ({ id: uid(), kind: 'wave', hazardCode: '', ...item }))
]

const seedImpactZone = () => [
	...windInundationRows.map(item => ({
		id: uid(),
		kind: 'wind_inundation',
		geojson: '',
		lon: '',
		lat: '',
		lengthKm: '',
		remark: '',
		...item
	})),
	...waveErosionRows.map(item => ({
		id: uid(),
		kind: 'wave_erosion',
		geojson: '',
		lon: '',
		lat: '',
		population: '',
		remark: '',
		...item
	}))
]

const wrapAdminRow = item => {
	const row = item || {}
	return {
		...row,
		id: row.id || uid(),
		geojson: row.geojson || ''
	}
}

/** 县级 ← 县界.json；市级 ← 县界按地级合并面 + 市界.json 弧段 */
const seedAdminTables = (adminPack = {}) => ({
	county: (adminPack.county || []).map(item =>
		wrapAdminRow({
			...item,
			source: item.source || '县界.json',
			fromSystem: item.fromSystem !== false
		})
	),
	city: (adminPack.city || []).map(item =>
		wrapAdminRow({
			...item,
			source: item.source || '市界.json',
			fromSystem: item.fromSystem !== false
		})
	)
})

const seedTownTable = (townRows = []) => {
	// 不再回落 mockTowns（那 14 条示意数据），只认 乡镇.json
	const list = Array.isArray(townRows) ? townRows : []
	return list.map(item => {
		const row = item || {}
		return {
			...row,
			id: row.id || uid(),
			geojson: row.geojson || ''
		}
	})
}

/** 清空市/县旧数据后，按县界/市界重新录入并落盘 */
const replaceAdminFromBoundaryFiles = (store, adminPack = {}) => {
	if (!store || typeof store !== 'object') return store
	const next = seedAdminTables(adminPack)
	store.county = next.county
	store.city = next.city
	store.meta = {
		...(store.meta || {}),
		[ADMIN_BOUNDARY_IMPORT]: ADMIN_BOUNDARY_IMPORT_VERSION,
		adminBoundaryImportedAt: Date.now(),
		adminBoundaryCountyCount: next.county.length,
		adminBoundaryCityCount: next.city.length
	}
	writeStore(store)
	if (import.meta.env?.DEV) {
		console.info(
			`[surveyBaseMockStore] 已导入边界文件：县级 ${next.county.length} 条（县界.json），市级 ${next.city.length} 条（市界/县界合并）`
		)
	}
	return store
}

/** 清空乡镇旧数据后，按 乡镇.json 重新录入并落盘 */
const replaceTownsFromJson = (store, townRows = []) => {
	if (!store || typeof store !== 'object') return store
	store.town = seedTownTable(townRows)
	store.meta = {
		...(store.meta || {}),
		[TOWN_IMPORT]: TOWN_IMPORT_VERSION,
		townImportedAt: Date.now(),
		townImportCount: store.town.length
	}
	writeStore(store)
	if (import.meta.env?.DEV) {
		console.info(`[surveyBaseMockStore] 已导入乡镇行政区：${store.town.length} 条（乡镇.json）`)
	}
	return store
}

const seedStaticData = (adminPack = {}, townRows = []) => ({
	...seedAdminTables(adminPack),
	meta: {
		[ADMIN_BOUNDARY_IMPORT]: ADMIN_BOUNDARY_IMPORT_VERSION,
		[TOWN_IMPORT]: TOWN_IMPORT_VERSION,
		adminBoundaryImportedAt: Date.now(),
		townImportedAt: Date.now()
	},
	town: seedTownTable(townRows),
	rsScene: remoteSensingScenes.map(item => ({ ...item })),
	assessSar: sarScenes.map(item => ({ ...item, geojson: item.geojson || '' })),
	assessTyphoon: typhoonEvents.map(item => ({ ...item })),
	assessLandPrice: seedLandPrice(),
	assessCountyEconomy: seedCountyEconomy(),
	assessStructure: structureRows.map(item => ({ id: uid(), geojson: '', ...item })),
	assessDamage: seedDamage(),
	assessImpactZone: seedImpactZone(),
	assessOvertop: [],
	assessIntegrate: integrateRecords.map(item => ({ ...item })),
	assessReportSource: reportSources.map(item => ({ ...item, remark: '' }))
})

const buildSeededStore = async (hazardPack, adminPack, townRows) => {
	const hazards = mapHazardPack(hazardPack)
	const overtop = await loadOvertopSeedRows()
	return {
		...seedStaticData(adminPack, townRows),
		...hazards,
		assessOvertop: overtop,
		reported: seedReported(hazards)
	}
}

const writeStore = store => {
	const { lean, map } = extractGeojsonMap(store)
	try {
		localStorage.setItem(STORAGE_KEY, JSON.stringify(lean))
	} catch (e) {
		console.warn('[surveyBaseMockStore] localStorage 写入失败，已跳过属性持久化', e)
	}
	void saveGeojsonMap(map)
}

let memoryStore = null
let readyPromise = null

const mergeMissingKeys = (parsed, seeded) => {
	Object.keys(seeded).forEach(key => {
		if (!Array.isArray(parsed[key])) parsed[key] = seeded[key]
	})
	return parsed
}

const needsGeojsonReseed = store =>
	GEOJSON_SEED_TYPES.some(typeId => {
		const list = store?.[typeId]
		if (!Array.isArray(list) || !list.length) return typeId === 'city' || HAZARD_TYPES.includes(typeId)
		const sample = list.slice(0, 20)
		return sample.every(item => item?.geojson == null || String(item.geojson).trim() === '')
	})

const loadSeedPacks = async () => {
	const [hazardPack, adminPack, townRows] = await Promise.all([
		loadAllHazardRowsFromJson(),
		loadAllAdminRowsFromJson().catch(err => {
			console.warn('[surveyBaseMockStore] 县界/市界加载失败', err)
			return { county: [], city: [] }
		}),
		loadTownRowsFromJson().catch(err => {
			console.warn('[surveyBaseMockStore] 乡镇.json 加载失败', err)
			return []
		})
	])
	return { hazardPack, adminPack, townRows }
}

async function initStore(forceReseed = false) {
	if (!forceReseed && memoryStore) return memoryStore

	const persistedGeo = await loadGeojsonMap()

	// 快路径：本地已有完整台账时，不要再拉 乡镇.json（约 16MB）/县界/承灾体
	if (!forceReseed) {
		try {
			const raw = localStorage.getItem(STORAGE_KEY)
			if (raw) {
				const parsed = JSON.parse(raw)
				if (parsed && typeof parsed === 'object') {
					const adminOk =
						parsed.meta?.[ADMIN_BOUNDARY_IMPORT] === ADMIN_BOUNDARY_IMPORT_VERSION &&
						Array.isArray(parsed.county) &&
						parsed.county.length > 0 &&
						Array.isArray(parsed.city) &&
						parsed.city.length > 0
					const townOk = parsed.meta?.[TOWN_IMPORT] === TOWN_IMPORT_VERSION
					applyGeojsonMap(parsed, persistedGeo)

					if (adminOk && townOk && !needsGeojsonReseed(parsed)) {
						const staticOnly = seedStaticData({}, [])
						mergeMissingKeys(parsed, staticOnly)
						HAZARD_TYPES.forEach(typeId => {
							if (!Array.isArray(parsed[typeId])) parsed[typeId] = []
						})
						memoryStore = parsed
						return memoryStore
					}

					const { hazardPack, adminPack, townRows } = await loadSeedPacks()
					const seeded = await buildSeededStore(hazardPack, adminPack, townRows)
					const merged = mergeMissingKeys(parsed, seeded)
					HAZARD_TYPES.forEach(typeId => {
						if (!Array.isArray(merged[typeId]) || !merged[typeId].length) {
							merged[typeId] = seeded[typeId]
						}
					})
					;['assessDamage', 'assessImpactZone', 'assessOvertop'].forEach(typeId => {
						if (!Array.isArray(merged[typeId]) || !merged[typeId].length) {
							merged[typeId] = seeded[typeId]
						}
					})
					applyGeojsonMap(merged, persistedGeo)
					if (needsGeojsonReseed(merged)) {
						fillMissingGeojson(merged, seeded)
					}
					let touched = false
					if (merged.meta?.[ADMIN_BOUNDARY_IMPORT] !== ADMIN_BOUNDARY_IMPORT_VERSION) {
						replaceAdminFromBoundaryFiles(merged, adminPack)
						touched = true
					}
					if (merged.meta?.[TOWN_IMPORT] !== TOWN_IMPORT_VERSION) {
						replaceTownsFromJson(merged, townRows)
						touched = true
					}
					if (!touched) writeStore(merged)
					memoryStore = merged
					return memoryStore
				}
			}
		} catch (e) {
			/* ignore */
		}
	}

	const { hazardPack, adminPack, townRows } = await loadSeedPacks()
	const seeded = await buildSeededStore(hazardPack, adminPack, townRows)
	applyGeojsonMap(seeded, persistedGeo)
	replaceAdminFromBoundaryFiles(seeded, adminPack)
	replaceTownsFromJson(seeded, townRows)
	memoryStore = seeded
	return memoryStore
}

const readStoreSync = () => {
	if (memoryStore) return memoryStore
	try {
		const raw = localStorage.getItem(STORAGE_KEY)
		if (raw) {
			const parsed = JSON.parse(raw)
			if (parsed && typeof parsed === 'object') {
				memoryStore = parsed
				return memoryStore
			}
		}
	} catch (e) {
		/* ignore */
	}
	return (
		memoryStore || {
			...seedStaticData(),
			city: [],
			seawall: [],
			fishing: [],
			mariculture: [],
			tourist: [],
			reported: []
		}
	)
}

export const surveyBaseMockStore = {
	/** 首次访问前拉取系统 JSON；可强制重新种子化 */
	ensureReady(forceReseed = false) {
		if (!forceReseed && memoryStore && readyPromise) return readyPromise
		if (forceReseed) {
			readyPromise = null
			memoryStore = null
		}
		if (!readyPromise) {
			readyPromise = initStore(forceReseed).catch(err => {
				readyPromise = null
				throw err
			})
		}
		return readyPromise
	},
	list(type) {
		const store = readStoreSync()
		return clone(store[type] || [])
	},
	query(type, params = {}) {
		const {
			keyword = '',
			hazardType = '',
			status = '',
			kind = '',
			warnLevel = '',
			type: sceneType = '',
			current = 1,
			size = 20
		} = params
		// 先基于原数组筛选分页，避免整表 clone 大 geojson
		const store = readStoreSync()
		let list = store[type] || []
		const text = String(keyword || '').trim()
		if (text) {
			list = list.filter(item => {
				const blob = [
					item.name,
					item.dangerName,
					item.year,
					item.id,
					item.reportNo,
					item.disasterName,
					item.disasterType,
					item.region,
					item.reportOrg,
					item.reportor,
					item.remark,
					item.division,
					item.town,
					item.county,
					item.city,
					item.hazardName,
					item.hazardType,
					item.hazardCode,
					item.sourceName,
					item.landfall,
					item.level,
					item.sensor,
					item.host,
					item.Management,
					item.method,
					item.HDXL,
					item.location,
					item.tideStation,
					item.target,
					item.analysis,
					item.warnLevel
				]
					.filter(Boolean)
					.join('|')
				return blob.includes(text)
			})
		}
		if (type === 'reported' && hazardType) {
			list = list.filter(item => item.hazardType === hazardType)
		}
		if ((type === 'assessSar' || type === 'assessStructure') && sceneType) {
			list = list.filter(item => item.type === sceneType)
		}
		if ((type === 'assessDamage' || type === 'assessImpactZone') && (kind || sceneType)) {
			list = list.filter(item => item.kind === (kind || sceneType))
		}
		if (type === 'assessOvertop' && (warnLevel || status)) {
			list = list.filter(item => item.warnLevel === (warnLevel || status))
		}
		if (type === 'assessIntegrate' && status) {
			list = list.filter(item => item.status === status)
		}
		const total = list.length
		const start = Math.max(0, (Number(current) - 1) * Number(size))
		const records = list.slice(start, start + Number(size)).map(item => clone(item))
		return { records, total, current: Number(current), size: Number(size) }
	},
	getById(type, id) {
		const store = readStoreSync()
		const item = (store[type] || []).find(row => String(row.id) === String(id))
		return item ? clone(item) : null
	},
	/** 不 clone，仅判断本地是否已有空间字段（供列表角标） */
	hasLocalGeo(type, id) {
		const store = readStoreSync()
		const item = (store[type] || []).find(row => String(row.id) === String(id))
		const g = item?.geojson
		return !(g == null || g === '')
	},
	create(type, payload) {
		const store = readStoreSync()
		const list = store[type] || []
		const row = {
			...payload,
			id: payload.id || uid()
		}
		if (row.name && !row.dangerName) row.dangerName = row.name
		if (row.dangerName && !row.name) row.name = row.dangerName
		if (type === 'seawall' && row.length != null && row.lengthKm == null) {
			row.lengthKm = Number(row.length) || 0
		}
		if ((type === 'fishing' || type === 'mariculture') && row.host && !row.manager) {
			row.manager = row.host
		}
		if (type === 'tourist' && row.Management && !row.manager) {
			row.manager = row.Management
		}
		if (type === 'reported') {
			row.hazardTypeLabel = HAZARD_TYPE_LABEL[row.hazardType] || row.hazardType
		}
		if (type === 'assessIntegrate') {
			const sources = store.assessReportSource || []
			row.sourceName =
				INTEGRATE_SOURCE_NAME[row.sourceId] ||
				sources.find(s => s.id === row.sourceId)?.name ||
				row.sourceName ||
				''
			row.status = row.status || '已标准化'
		}
		list.unshift(row)
		store[type] = list
		memoryStore = store
		writeStore(store)
		return clone(row)
	},
	update(type, payload) {
		const store = readStoreSync()
		const list = store[type] || []
		const idx = list.findIndex(item => String(item.id) === String(payload.id))
		if (idx < 0) throw new Error('记录不存在')
		const next = { ...list[idx], ...payload }
		if (next.name && !payload.dangerName) next.dangerName = next.name
		if (next.dangerName && !payload.name) next.name = next.dangerName
		if (type === 'seawall' && payload.length != null && payload.lengthKm == null) {
			next.lengthKm = Number(payload.length) || 0
		}
		if ((type === 'fishing' || type === 'mariculture') && payload.host != null && payload.manager == null) {
			next.manager = payload.host
		}
		if (type === 'tourist' && payload.Management != null && payload.manager == null) {
			next.manager = payload.Management
		}
		if (type === 'reported') {
			next.hazardTypeLabel = HAZARD_TYPE_LABEL[next.hazardType] || next.hazardType
		}
		if (type === 'assessIntegrate') {
			const sources = store.assessReportSource || []
			next.sourceName =
				INTEGRATE_SOURCE_NAME[next.sourceId] ||
				sources.find(s => s.id === next.sourceId)?.name ||
				next.sourceName ||
				''
		}
		list[idx] = next
		store[type] = list
		memoryStore = store
		writeStore(store)
		return clone(next)
	},
	remove(type, id) {
		const store = readStoreSync()
		const list = store[type] || []
		store[type] = list.filter(item => String(item.id) !== String(id))
		memoryStore = store
		writeStore(store)
		return true
	},
	async reset() {
		await this.ensureReady(true)
		return clone(memoryStore)
	},
	getAll() {
		return clone(readStoreSync())
	}
}
