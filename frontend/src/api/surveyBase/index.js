import { surveyBaseMockStore } from './mockStore.js'
import { SURVEY_BASE_TYPES, ASSESS_BASE_TYPES } from './schemas.js'
import { withMockFallback, isNonEmptyList } from '@/api/withMockFallback.js'
import {
	getExposureTypeByHazard,
	remoteBizBatchSave,
	remoteBizDelete,
	remoteBizGetById,
	remoteBizList,
	remoteBizQueryPage,
	remoteBizSave,
	remoteBizUpdate,
	remoteHazardOrBizList
} from '@/api/bizRemote.js'
import { remoteExposureTree } from '@/api/evaluateRemote.js'
import { clearGeojsonEnrichCache, enrichRowsWithLocalGeojson } from './geojsonEnrich.js'

const wait = (ms = 120) => new Promise(resolve => setTimeout(resolve, ms))

const ALL_TYPES = { ...SURVEY_BASE_TYPES, ...ASSESS_BASE_TYPES }

/** 列表页不需要把完整边界塞进表格行（编辑时再按 id 取全量） */
const LIST_GEO_TYPES = new Set(['county', 'city', 'town', 'seawall', 'fishing', 'mariculture', 'tourist'])
/** 市/县/乡镇远程若慢，短超时后回落本地，避免空等 12s */
const ADMIN_FAST_FALLBACK_TYPES = new Set(['county', 'city', 'town'])
/** 四类承灾体一键入库 */
export const HAZARD_SYNC_TYPES = [
	{ type: 'seawall', label: '海堤数据' },
	{ type: 'fishing', label: '渔港数据' },
	{ type: 'mariculture', label: '海水养殖区' },
	{ type: 'tourist', label: '滨海旅游区' }
]
const GEO_LIST_MARKER = '__HAS_GEO__'
const HAZARD_SYNC_STATUS_KEY = 'survey_hazard_sync_status_v1'

const hasGeoValue = value => {
	if (value == null || value === '') return false
	if (value === GEO_LIST_MARKER) return true
	if (typeof value === 'object') return Object.keys(value).length > 0
	return String(value).trim().length > 0
}

/** 列表轻量化：保留「是否有空间」标记，去掉巨型 geojson / boundaryLines */
const lightenListRecords = (type, records = []) => {
	if (!LIST_GEO_TYPES.has(type) || !Array.isArray(records)) return records
	return records.map(row => {
		if (!row || typeof row !== 'object') return row
		const next = { ...row }
		next.geojson = hasGeoValue(next.geojson) ? GEO_LIST_MARKER : ''
		if ('boundaryLines' in next) delete next.boundaryLines
		return next
	})
}

const listLocalType = (type, { keyword = '', extraType = '' } = {}) => {
	let list = surveyBaseMockStore.list(type)
	if (extraType) {
		list = list.filter(item => item.type === extraType || item.kind === extraType || item.hazardType === extraType)
	}
	if (keyword) {
		const text = String(keyword)
		list = list.filter(item => JSON.stringify(item).includes(text))
	}
	return list
}

const paginateLocal = (list, params = {}) => {
	const current = Number(params.current) || 1
	const size = Number(params.size) || 20
	const start = (current - 1) * size
	return {
		records: list.slice(start, start + size),
		total: list.length,
		current,
		size
	}
}

const applyClientFilters = (list, type, params = {}) => {
	let rows = Array.isArray(list) ? [...list] : []
	const text = String(params.keyword || '').trim()
	const sceneType = params.type || ''
	const kind = params.kind || ''
	const hazardType = params.hazardType || ''
	const warnLevel = params.warnLevel || ''
	const status = params.status || ''

	if (text) {
		rows = rows.filter(item => JSON.stringify(item || {}).includes(text))
	}
	if ((type === 'assessSar' || type === 'assessStructure') && sceneType) {
		rows = rows.filter(item => item.type === sceneType)
	}
	if ((type === 'assessDamage' || type === 'assessImpactZone') && (kind || sceneType)) {
		rows = rows.filter(item => item.kind === (kind || sceneType))
	}
	if (type === 'reported' && hazardType) {
		rows = rows.filter(item => item.hazardType === hazardType)
	}
	if (type === 'assessOvertop' && (warnLevel || status)) {
		rows = rows.filter(item => item.warnLevel === (warnLevel || status))
	}
	if (type === 'assessIntegrate' && status) {
		rows = rows.filter(item => item.status === status)
	}
	return rows
}

/**
 * 灾情调查/评估基础数据 API。
 * 优先真实后端；接口失败或无数据时回落 localStorage MOCK。
 */
export function surveyBaseApi() {
	return {
		getSchema: type => ALL_TYPES[type] || SURVEY_BASE_TYPES[type] || null,

		queryPage: async (type, params = {}) => {
			await surveyBaseMockStore.ensureReady()
			if (!ALL_TYPES[type] && !SURVEY_BASE_TYPES[type]) throw new Error('未知数据类型')

			// 运维列表统一走 bizdata 分页；承灾体不再合并拉 exposure 全量（调查运行时 listType 仍会合并）
			// 市/县远程短超时回落，避免列表被 12s 拖死
			const { data, source } = await withMockFallback(
				async () => {
					const page = await remoteBizQueryPage(type, params)
					const filtered = applyClientFilters(page.records || [], type, params)
					if (filtered.length !== (page.records || []).length) {
						return paginateLocal(filtered, params)
					}
					return page
				},
				() => surveyBaseMockStore.query(type, params),
				data => isNonEmptyList(data?.records),
				ADMIN_FAST_FALLBACK_TYPES.has(type) ? { timeoutMs: 2500 } : undefined
			)

			// 列表页不做全量边界 enrich（会再解析县界/市界）；仅轻量化标记是否有空间
			let records = data?.records || []
			if (LIST_GEO_TYPES.has(type) && source === 'remote') {
				// 远端缺几何时，用本地台账判断「已录入」，避免误显示未录入
				records = records.map(row => {
					if (!row || hasGeoValue(row.geojson)) return row
					if (surveyBaseMockStore.hasLocalGeo(type, row.id)) {
						return { ...row, geojson: GEO_LIST_MARKER }
					}
					return row
				})
			}
			records = lightenListRecords(type, records)
			return {
				code: 200,
				data: { ...data, records },
				resultCode: 'S',
				source
			}
		},

		create: async (type, payload) => {
			await surveyBaseMockStore.ensureReady()
			try {
				const remote = await remoteBizSave(type, payload)
				if (remote) {
					try {
						const exists = surveyBaseMockStore.getById(type, remote.id || payload.id)
						if (exists) surveyBaseMockStore.update(type, { ...payload, ...remote })
						else surveyBaseMockStore.create(type, { ...payload, ...remote })
					} catch (e) {
						/* local sync optional */
					}
					return { code: 200, data: remote, resultMsg: '新增成功', resultCode: 'S', source: 'remote' }
				}
			} catch (e) {
				/* fallthrough mock */
			}
			await wait()
			const row = surveyBaseMockStore.create(type, payload)
			return { code: 200, data: row, resultMsg: '新增成功（本地 MOCK）', resultCode: 'S', source: 'mock' }
		},

		update: async (type, payload) => {
			await surveyBaseMockStore.ensureReady()
			try {
				const remote = await remoteBizUpdate(type, payload)
				if (remote) {
					try {
						const exists = surveyBaseMockStore.getById(type, remote.id || payload.id)
						if (exists) surveyBaseMockStore.update(type, { ...payload, ...remote })
						else surveyBaseMockStore.create(type, { ...payload, ...remote })
					} catch (e) {
						/* ignore */
					}
					return { code: 200, data: remote, resultMsg: '更新成功', resultCode: 'S', source: 'remote' }
				}
			} catch (e) {
				/* fallthrough */
			}
			await wait()
			const exists = surveyBaseMockStore.getById(type, payload.id)
			const row = exists
				? surveyBaseMockStore.update(type, payload)
				: surveyBaseMockStore.create(type, payload)
			return { code: 200, data: row, resultMsg: '更新成功（本地 MOCK）', resultCode: 'S', source: 'mock' }
		},

		remove: async (type, id) => {
			await surveyBaseMockStore.ensureReady()
			try {
				await remoteBizDelete(id)
			} catch (e) {
				/* still remove local */
			}
			await wait()
			try {
				surveyBaseMockStore.remove(type, id)
			} catch (e) {
				/* ignore */
			}
			return { code: 200, data: true, resultMsg: '删除成功', resultCode: 'S' }
		},

		reset: async () => {
			await wait()
			clearGeojsonEnrichCache()
			await surveyBaseMockStore.reset()
			return {
				code: 200,
				data: true,
				resultMsg: '已恢复默认数据（市/县按边界文件、乡镇按乡镇.json含几何重新导入）',
				resultCode: 'S'
			}
		},

		/**
		 * 把当前类型本地台账（含空间 geojson）批量写入数据库。
		 * 海堤/渔港/养殖/旅游会先用本地系统 JSON 补齐缺失几何再上传。
		 */
		syncMockToRemote: async type => {
			await surveyBaseMockStore.ensureReady()
			let list = surveyBaseMockStore.list(type)
			if (!list.length) throw new Error('本地无数据可同步')

			// 四类承灾体：确保每条带上完整 geojson（来自 datas JSON / IndexedDB）
			if (LIST_GEO_TYPES.has(type)) {
				list = await enrichRowsWithLocalGeojson(type, list)
				const withGeo = list.filter(item => hasGeoValue(item?.geojson)).length
				if (!withGeo) {
					throw new Error('未找到可入库的空间数据，请确认 public/datas 中对应 JSON 可访问')
				}
				// 统一成字符串，便于后端 JSON_DATA 落库
				list = list.map(item => {
					if (!item || !hasGeoValue(item.geojson) || typeof item.geojson === 'string') return item
					return { ...item, geojson: JSON.stringify(item.geojson) }
				})
			}

			const summary = await remoteBizBatchSave(type, list)
			return {
				code: 200,
				data: { ...summary, withGeo: list.filter(item => hasGeoValue(item?.geojson)).length },
				resultCode: 'S',
				resultMsg: '同步完成'
			}
		},

		/**
		 * 一键同步四类承灾体（含空间）到数据库。
		 * @param {(info:{index:number,total:number,type:string,label:string,phase:string,result?:object})=>void} [onProgress]
		 */
		syncAllHazardsToRemote: async (onProgress = null) => {
			const api = surveyBaseApi()
			const results = []
			const total = HAZARD_SYNC_TYPES.length
			for (let i = 0; i < HAZARD_SYNC_TYPES.length; i++) {
				const item = HAZARD_SYNC_TYPES[i]
				onProgress?.({
					index: i + 1,
					total,
					type: item.type,
					label: item.label,
					phase: 'start'
				})
				try {
					const res = await api.syncMockToRemote(item.type)
					const data = res?.data || {}
					const row = {
						type: item.type,
						label: item.label,
						ok: true,
						success: Number(data.success) || 0,
						fail: Number(data.fail) || 0,
						total: Number(data.total) || 0,
						withGeo: Number(data.withGeo) || 0,
						message: ''
					}
					results.push(row)
					onProgress?.({
						index: i + 1,
						total,
						type: item.type,
						label: item.label,
						phase: 'done',
						result: row
					})
				} catch (err) {
					const row = {
						type: item.type,
						label: item.label,
						ok: false,
						success: 0,
						fail: 0,
						total: 0,
						withGeo: 0,
						message: err?.message || '同步失败'
					}
					results.push(row)
					onProgress?.({
						index: i + 1,
						total,
						type: item.type,
						label: item.label,
						phase: 'error',
						result: row
					})
				}
			}
			const status = {
				at: new Date().toISOString(),
				results
			}
			try {
				localStorage.setItem(HAZARD_SYNC_STATUS_KEY, JSON.stringify(status))
			} catch (e) {
				/* ignore */
			}
			return { code: 200, data: status, resultCode: 'S', resultMsg: '四类承灾体同步结束' }
		},

		getHazardSyncStatus: () => {
			try {
				const raw = localStorage.getItem(HAZARD_SYNC_STATUS_KEY)
				return raw ? JSON.parse(raw) : null
			} catch (e) {
				return null
			}
		},

		getAll: async () => {
			await surveyBaseMockStore.ensureReady()
			await wait(40)
			return { code: 200, data: surveyBaseMockStore.getAll(), resultCode: 'S' }
		},

		getById: async (type, id) => {
			await surveyBaseMockStore.ensureReady()
			const { data } = await withMockFallback(
				() => remoteBizGetById(id),
				() => surveyBaseMockStore.getById(type, id),
				d => !!d
			)
			if (!data) return data
			const [enriched] = await enrichRowsWithLocalGeojson(type, [data])
			return enriched || data
		},

		/** 承灾体树（后端 exposure/tree），空则回落列表拼简易树 */
		getExposureTree: async hazardType => {
			const exposureType =
				{ seawall: 'Dike', fishing: 'Fishery', mariculture: 'CultureArea', tourist: 'TouristArea' }[hazardType] ||
				hazardType
			const { data } = await withMockFallback(
				() => remoteExposureTree(exposureType),
				async () => {
					let list = []
					try {
						list = getExposureTypeByHazard(hazardType)
							? await remoteHazardOrBizList(hazardType)
							: surveyBaseMockStore.list(hazardType)
					} catch (e) {
						list = surveyBaseMockStore.list(hazardType)
					}
					if (!isNonEmptyList(list)) list = surveyBaseMockStore.list(hazardType)
					return [
						{
							nodeId: 'root',
							nodeName: '全部',
							nodeType: 'ROOT',
							children: (list || []).map(item => ({
								nodeId: item.id,
								nodeName: item.name || item.dangerName,
								nodeType: exposureType,
								nodeCode: item.hazardCode || item.code || item.id
							}))
						}
					]
				},
				isNonEmptyList
			)
			return data
		},

		/** 供调查/评估运行时拉取整表（真库优先，空则回落本地） */
		listType: async (type, { keyword = '', extraType = '' } = {}) => {
			await surveyBaseMockStore.ensureReady()
			const { data } = await withMockFallback(
				async () => {
					if (getExposureTypeByHazard(type)) {
						return remoteHazardOrBizList(type, keyword)
					}
					return remoteBizList(type, { keyword, extraType })
				},
				() => listLocalType(type, { keyword, extraType }),
				isNonEmptyList
			)
			return enrichRowsWithLocalGeojson(type, data || [])
		}
	}
}
