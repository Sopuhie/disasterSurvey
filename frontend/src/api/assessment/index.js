import { surveyBaseMockStore } from '@/api/surveyBase/mockStore.js'
import { surveyBaseApi } from '@/api/surveyBase/index.js'
import { makeSystemHazardFeature } from '@/api/surveyBase/hazardFeatures.js'
import { defaultEconomy } from '@/views/assessment/mock/economyData.js'
import {
	enrichDamageWithHazards,
	linkOvertopToSeawalls,
	mapOvertoppingRows,
	overtoppingRowsToFeatures
} from '@/views/assessment/utils/assessmentEngine.js'
import { reportArchiveStore } from '@/views/assessment/mock/reportArchive.js'
import { withMockFallback, isNonEmptyList } from '@/api/withMockFallback.js'
import { remoteArchiveReport, remoteGetArchive, remoteListArchives } from '@/api/bizRemote.js'
import {
	DISASTER_TYPE_MAP,
	remoteComputeLoss,
	remoteEvaluateReportExport,
	remoteEvaluateTaskBuild,
	remoteEvaluateTaskDetail
} from '@/api/evaluateRemote.js'
import { findLatestEvaluateResult, parseJsonField } from '@/api/taskResultHydrate.js'
import { DataNames } from '/public/map.js'

const isTaskId = d => typeof d === 'string' && d.length > 0

const wait = (ms = 400) => new Promise(resolve => setTimeout(resolve, ms))
const baseApi = surveyBaseApi()
const listType = (type, opts) => baseApi.listType(type, opts)

const toLandMap = row => ({
	耕地: Number(row.landFarm) || 0,
	建设用地: Number(row.landBuild) || 0,
	养殖水面: Number(row.landAqua) || 0,
	林地: Number(row.landForest) || 0,
	水域: Number(row.landWater) || 0
})

const ready = () => surveyBaseMockStore.ensureReady()

const hazardPack = async () => ({
	seawall: await listType('seawall'),
	fishing: await listType('fishing'),
	mariculture: await listType('mariculture'),
	tourist: await listType('tourist')
})

const normalizeDamageRow = item => {
	const { id, kind, ...rest } = item
	return {
		...rest,
		id,
		kind,
		lon: Number(item.lon),
		lat: Number(item.lat),
		level: Number(item.level) || undefined
	}
}

/**
 * 灾害评估接口适配器。
 * 真接口优先；无数据/失败时回落 MOCK（含运维 localStorage 与系统 JSON）。
 */
export function assessmentApi() {
	return {
		getReportSources: async () => {
			await ready()
			await wait(120)
			return listType('assessReportSource')
		},
		getIntegrateRecords: async () => {
			await ready()
			await wait(200)
			return listType('assessIntegrate')
		},
		saveIntegrateRecord: async payload => {
			await ready()
			await wait(200)
			const row = {
				...payload,
				id: payload.id || `ST-${Date.now().toString().slice(-4)}`,
				status: payload.status || '已标准化'
			}
			try {
				const saved = await baseApi.create('assessIntegrate', row)
				if (saved?.data) return saved.data
			} catch (e) {
				/* mock below */
			}
			const exists = surveyBaseMockStore.getById('assessIntegrate', row.id)
			if (exists) {
				return surveyBaseMockStore.update('assessIntegrate', { ...exists, ...row })
			}
			return surveyBaseMockStore.create('assessIntegrate', row)
		},
		getSarScenes: async type => {
			await ready()
			await wait(160)
			const list = await listType('assessSar', { extraType: type || '' })
			return list.filter(item => !type || item.type === type)
		},
		getTyphoonEvents: async () => {
			await ready()
			await wait(160)
			return listType('assessTyphoon')
		},
		getCountyEconomy: async name => {
			await ready()
			await wait(160)
			const list = await listType('assessCountyEconomy')
			const hit =
				list.find(item => item.name === name || name?.includes(item.name) || item.name.includes(name || '')) || null
			if (!hit) {
				return {
					...defaultEconomy,
					name: name || defaultEconomy.name
				}
			}
			return {
				name: hit.name,
				gdp: hit.gdp,
				land: toLandMap(hit)
			}
		},
		getLandUsePrices: async () => {
			await ready()
			const list = await listType('assessLandPrice')
			const map = {}
			list.forEach(item => {
				if (item.name) map[item.name] = Number(item.price) || 0
			})
			return map
		},
		getWindDamage: async () => {
			await ready()
			await wait(280)
			const rows = (await listType('assessDamage', { extraType: 'wind' }))
				.filter(item => item.kind === 'wind')
				.map(normalizeDamageRow)
			return enrichDamageWithHazards(rows, await hazardPack())
		},
		getWaveDamage: async () => {
			await ready()
			await wait(280)
			const rows = (await listType('assessDamage', { extraType: 'wave' }))
				.filter(item => item.kind === 'wave')
				.map(normalizeDamageRow)
			return enrichDamageWithHazards(rows, await hazardPack())
		},
		getStructureDamage: async () => {
			await ready()
			await wait(200)
			return (await listType('assessStructure')).map(item => ({
				code: item.code,
				name: item.name,
				type: item.type,
				subtype: item.subtype,
				city: item.city,
				county: item.county,
				town: item.town,
				damage: item.damage,
				damageMode: item.damageMode,
				level: item.level,
				status: item.status,
				importance: item.importance,
				manager: item.manager,
				lon: item.lon,
				lat: item.lat
			}))
		},
		getWaveErosion: async () => {
			await ready()
			await wait(160)
			return (await listType('assessImpactZone', { extraType: 'wave_erosion' }))
				.filter(item => item.kind === 'wave_erosion')
				.map(item => ({
					name: item.name,
					city: item.city,
					areaKm2: Number(item.areaKm2) || 0,
					lengthKm: Number(item.lengthKm) || 0,
					lon: item.lon,
					lat: item.lat,
					geojson: item.geojson
				}))
		},
		getWindInundation: async () => {
			await ready()
			await wait(160)
			return (await listType('assessImpactZone', { extraType: 'wind_inundation' }))
				.filter(item => item.kind === 'wind_inundation')
				.map(item => ({
					name: item.name,
					city: item.city,
					areaKm2: Number(item.areaKm2) || 0,
					population: Number(item.population) || 0,
					lon: item.lon,
					lat: item.lat,
					geojson: item.geojson
				}))
		},
		getOvertoppingRows: async ({ city = '', keyword = '' } = {}) => {
			await ready()
			await wait(200)
			let rows = await listType('assessOvertop')
			if (!rows.length) {
				const url = DataNames['漫堤分析']
				const res = await fetch(url)
				if (!res.ok) throw new Error('漫堤分析数据加载失败')
				const json = await res.json()
				const raw = Array.isArray(json) ? json[0]?.data || json : json?.data || []
				rows = mapOvertoppingRows(raw)
			} else {
				rows = mapOvertoppingRows(rows)
			}
			rows = linkOvertopToSeawalls(rows, await listType('seawall'))
			const cityText = String(city || '').trim()
			const text = String(keyword || '').trim()
			if (cityText) rows = rows.filter(item => item.city?.includes(cityText) || cityText.includes(item.city || ''))
			if (text) {
				rows = rows.filter(item =>
					[item.name, item.location, item.city, item.tideStation, item.warnLevel, item.hazardCode]
						.join('|')
						.includes(text)
				)
			}
			return rows
		},
		getOvertoppingFeatures: async (rows = []) => {
			await ready()
			return overtoppingRowsToFeatures(rows, makeSystemHazardFeature)
		},
		getSurveyHazardSummary: async () => {
			await ready()
			const pack = await hazardPack()
			return {
				seawall: pack.seawall.length,
				fishing: pack.fishing.length,
				mariculture: pack.mariculture.length,
				tourist: pack.tourist.length,
				samples: {
					seawall: pack.seawall.slice(0, 5),
					fishing: pack.fishing.slice(0, 5),
					mariculture: pack.mariculture.slice(0, 5),
					tourist: pack.tourist.slice(0, 5)
				}
			}
		},
		archiveAssessmentReport: async (report, meta = {}) => {
			await wait(120)
			if (!report) throw new Error('无可归档的评估报告')
			const payload = {
				...meta,
				title: report.title || meta.title,
				typeId: report.typeId || meta.typeId,
				typeLabel: report.typeLabel || meta.typeLabel,
				district: report.district || meta.district,
				sceneName: report.sceneName || meta.sceneName,
				grade: report.grade,
				total: report.total,
				snapshot: report,
				reportNo: meta.reportNo
			}
			const { data } = await withMockFallback(
				() => remoteArchiveReport(payload),
				() => reportArchiveStore.save(report, meta),
				d => !!d
			)
			return data
		},
		listArchivedReports: async () => {
			await wait(80)
			const { data } = await withMockFallback(
				() => remoteListArchives(),
				() => reportArchiveStore.list(),
				isNonEmptyList
			)
			return data
		},
		getArchivedReport: async id => {
			await wait(60)
			const { data } = await withMockFallback(
				() => remoteGetArchive(id),
				() => reportArchiveStore.getById(id),
				d => !!d
			)
			return data
		},

		/** 回填最近一次损失测算（服务端 resultData.kind=loss） */
		hydrateLossResult: async ({ typeId, sceneId = '', forceRefresh = false } = {}) => {
			if (forceRefresh) return null
			const hit = await findLatestEvaluateResult({ typeId, kind: 'loss', sceneId })
			if (hit?.resultData?.lossResult) {
				return { taskId: hit.taskId, lossResult: hit.resultData.lossResult, source: 'remote' }
			}
			return null
		},

		/** 服务端损失计算；失败返回 null */
		computeLossRemote: async ({
			typeId,
			sceneId = '',
			sceneName = '',
			sceneDate = '',
			districtName = '',
			city = '',
			statArea,
			density,
			inundationRatio
		} = {}) => {
			const { data } = await withMockFallback(
				() =>
					remoteComputeLoss({
						typeId,
						disasterType: DISASTER_TYPE_MAP[typeId] || '1',
						sceneId,
						sceneName,
						sceneDate,
						districtName,
						city,
						statArea,
						density,
						inundationRatio,
						persist: true
					}),
				() => null,
				d => !!d?.lossResult
			)
			return data
		},

		/** 回填最近一次综合报告（reportData 快照） */
		hydrateReportResult: async ({ typeId, sceneId = '', forceRefresh = false } = {}) => {
			if (forceRefresh) return null
			const hit = await findLatestEvaluateResult({ typeId, kind: 'report', sceneId })
			const snapshot = hit?.reportData?.snapshot || hit?.reportData
			if (snapshot && (snapshot.title || snapshot.grade || snapshot.total != null)) {
				return { taskId: hit.taskId, report: snapshot, source: 'remote' }
			}
			if (hit?.taskId) {
				try {
					const detail = await remoteEvaluateTaskDetail(hit.taskId)
					const reportData = parseJsonField(detail?.reportData)
					const snap = reportData?.snapshot || reportData
					if (snap && (snap.title || snap.grade)) {
						return { taskId: hit.taskId, report: snap, source: 'remote-detail' }
					}
				} catch (e) {
					/* ignore */
				}
			}
			return null
		},

		/** 损失测算落库 */
		persistLossLiteTask: async ({ typeId, lossResult, sceneId = '', districtName = '' } = {}) => {
			const { data } = await withMockFallback(
				() =>
					remoteEvaluateTaskBuild({
						name: `经济损失预估-${districtName || typeId || ''}`,
						disasterType: DISASTER_TYPE_MAP[typeId] || '1',
						reportData: {
							kind: 'loss',
							district: districtName,
							sceneName: lossResult?.sceneName,
							summary: lossResult?.summary
						},
						sceneId,
						liteMode: true,
						resultData: {
							kind: 'loss',
							typeId,
							sceneId,
							district: districtName,
							lossResult
						}
					}),
				() => null,
				isTaskId
			)
			return data || ''
		},

		/** 步骤报告落库为评估任务（liteMode）；失败返回空串 */
		persistEvaluateLiteTask: async ({
			typeId,
			report,
			factors = {},
			sceneId = '',
			districtName = ''
		} = {}) => {
			const { data } = await withMockFallback(
				() =>
					remoteEvaluateTaskBuild({
						name: report?.title || `${typeId || '评估'}综合报告`,
						disasterType: DISASTER_TYPE_MAP[typeId] || '1',
						reportData: {
							snapshot: report,
							title: report?.title,
							reportNo: report?.reportNo,
							grade: report?.grade,
							total: report?.total,
							district: report?.district || districtName,
							sceneName: report?.sceneName,
							summary: report?.summary,
							kpis: report?.kpis
						},
						structure: factors.structure,
						response: factors.response,
						prepare: factors.prepare,
						recommend: factors.note || report?.recommend || '',
						sceneId,
						liteMode: true,
						resultData: {
							kind: 'report',
							typeId,
							sceneId,
							grade: report?.grade,
							total: report?.total,
							loss: report?.lossSummary || null,
							damageCount: report?.damageRows?.length || 0,
							overtopCount: report?.overtoppingRows?.length || 0
						}
					}),
				() => null,
				isTaskId
			)
			return data || ''
		},

		/** 服务端报告导出；失败返回空（上层回落本地打印/JSON） */
		exportEvaluateReport: async (evaluateId, exposureType = '') => {
			if (!evaluateId) return ''
			const { data } = await withMockFallback(
				() => remoteEvaluateReportExport(evaluateId, exposureType),
				() => null,
				d => typeof d === 'string' && d.length > 0
			)
			return data || ''
		}
	}
}
