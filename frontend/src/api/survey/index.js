import {
	makeCircleFeature,
	makeDistrictFeature,
	makeOverlayFeatures,
	mockIdentifyMeta,
	mockOverlayResults
} from '@/views/survey/mock/surveyDemoData.js'
import { surveyBaseMockStore } from '@/api/surveyBase/mockStore.js'
import { surveyBaseApi } from '@/api/surveyBase/index.js'
import { makeSystemHazardFeature } from '@/api/surveyBase/hazardFeatures.js'
import {
	loadHazardFeatures,
	resolveAdminFeatureByCoord,
	resolveAdminFeatureByName
} from '@/views/survey/utils/dataLoader.js'
import {
	overlayHazards,
	resolveAffectedFeature,
	summarizeAffectedArea
} from '@/views/survey/utils/overlayEngine.js'
import { buildProfile, hazardDossierStore } from '@/views/survey/mock/hazardDossier.js'
import { withMockFallback, isNonEmptyList } from '@/api/withMockFallback.js'
import { isMockEnabled, toExposureLegacyId, toExposureType } from '@/constants/disasterEnums.js'
import {
	remoteDossierDownload,
	remoteDossierGet,
	remoteDossierListArchived,
	remoteDossierRemoveItem,
	remoteDossierSave,
	remoteDossierUpload,
	remoteExposureList
} from '@/api/bizRemote.js'
import {
	TARGET_ENTITY_MAP,
	remoteComputeIdentify,
	remoteComputeOverlay,
	remoteSurveyTaskBuild
} from '@/api/evaluateRemote.js'
import {
	listSurveyDossiers,
	queryBaseExposures,
	queryRegionPopulation,
	saveSurveyDossier
} from '@/api/survey/pipeline.js'
import { findLatestSurveyResult } from '@/api/taskResultHydrate.js'
import { km2ToWanMu, wanMuToKm2 } from '@/shared/metricUnits.js'
import { buildAreaTree } from '@/utils/areaData.js'
import { loadLocalRsManifest } from '@/utils/localRsImages.js'

const isTaskId = d => typeof d === 'string' && d.length > 0

/** 步骤 UI 结果落库到调查任务（liteMode）；失败不影响主流程 */
const persistSurveyLiteTask = async ({ name, targetId, sceneId, resultData }) => {
	try {
		const { data } = await withMockFallback(
			() =>
				remoteSurveyTaskBuild({
					name,
					targetEntity: TARGET_ENTITY_MAP[targetId] || targetId || 'Pop',
					sceneId: sceneId || '',
					liteMode: true,
					resultData
				}),
			() => null,
			isTaskId,
			{ silent: true }
		)
		return data || ''
	} catch (e) {
		return ''
	}
}

/** 用县域台账推算识别 KPI；mock=false 时禁止回落演示数据 */
const buildIdentifyMetaFromCounties = async (scene, district) => {
	const ratio = scene?.inundationRatio || 0.45
	let counties = await getCounties()
	if (!isNonEmptyList(counties)) {
		if (!isMockEnabled()) {
			throw new Error('无法获取行政区划底数，请检查 /api/v1/survey/base/region-population')
		}
		const areaKm2 = Number((mockIdentifyMeta.areaKm2 * (0.85 + ratio)).toFixed(2))
		const rows = (mockIdentifyMeta.counties || []).map(item => {
			const a = Number((item.areaKm2 * (0.85 + ratio)).toFixed(2))
			return {
				...item,
				areaKm2: a,
				areaWanMu: km2ToWanMu(a),
				affectedPopulation: Math.round(item.affectedPopulation * (0.85 + ratio))
			}
		})
		return {
			areaKm2,
			areaWanMu: km2ToWanMu(areaKm2),
			countyCount: rows.length,
			counties: rows,
			sceneName: scene?.name,
			sceneDate: scene?.date,
			regionHint: scene?.regionHint,
			sourceHint: 'mock'
		}
	}
	const city = district?.city || ''
	const focusName = district?.name || ''
	let pool = city ? counties.filter(c => (c.city || '').includes(city) || city.includes(c.city || '')) : counties
	if (!isNonEmptyList(pool)) pool = counties
	if (focusName) {
		pool = [...pool].sort((a, b) => {
			const aHit = a.name === focusName ? 0 : 1
			const bHit = b.name === focusName ? 0 : 1
			return aHit - bHit
		})
	}
	const hit = pool.slice(0, 6)
	const rows = hit.map((item, index) => {
		const base = Number(item.statArea || item.areaKm2 || 400)
		const areaKm2 = Number((base * 0.035 * (0.85 + ratio) * (1 - index * 0.06)).toFixed(2))
		const density = Number(item.density) || 500
		return {
			name: item.name,
			city: item.city || '',
			areaKm2,
			areaWanMu: km2ToWanMu(areaKm2),
			density,
			affectedPopulation: Math.round(density * areaKm2)
		}
	})
	const areaKm2 = Number(rows.reduce((s, r) => s + r.areaKm2, 0).toFixed(2))
	return {
		areaKm2,
		areaWanMu: km2ToWanMu(areaKm2),
		countyCount: rows.length,
		counties: rows,
		sceneName: scene?.name,
		sceneDate: scene?.date,
		regionHint: scene?.regionHint,
		sourceHint: 'ledger'
	}
}

const buildPopulationOverlayFromMeta = meta => {
	const rows = (meta?.counties || []).map(item => ({
		name: item.name,
		city: item.city,
		areaKm2: item.areaKm2,
		areaWanMu: item.areaWanMu ?? km2ToWanMu(item.areaKm2 || 0),
		density: item.density,
		affectedPopulation: item.affectedPopulation
	}))
	return {
		target: 'population',
		rows,
		totals: {
			count: rows.length,
			areaKm2: Number(rows.reduce((s, r) => s + (Number(r.areaKm2) || 0), 0).toFixed(2)),
			affectedPopulation: rows.reduce((s, r) => s + (Number(r.affectedPopulation) || 0), 0)
		}
	}
}

const wait = (ms = 500) => new Promise(resolve => setTimeout(resolve, ms))
const baseApi = surveyBaseApi()

const normalize = text => String(text || '').replace(/\s+/g, '')
const stripAdminSuffix = text =>
	normalize(text).replace(/(特别行政区|自治区|自治州|地区|盟|市|州|县|区|旗)$/g, '')

/** 承灾体是否属于所选行政区（按 division/town/county/city 模糊匹配） */
const hazardMatchesDistrict = (item, districtName) => {
	const district = normalize(districtName)
	if (!district) return true
	const districtCore = stripAdminSuffix(district)
	const fields = [item?.division, item?.town, item?.county, item?.city, item?.name]
	return fields.some(field => {
		const text = normalize(field)
		if (!text) return false
		const core = stripAdminSuffix(text)
		return (
			text.includes(district) ||
			district.includes(text) ||
			(!!districtCore && (text.includes(districtCore) || core === districtCore || core.includes(districtCore)))
		)
	})
}

const hazardMatchesTown = (item, townName) => {
	const town = normalize(townName)
	if (!town) return true
	const townCore = stripAdminSuffix(town)
	const fields = [item?.town, item?.division, item?.name]
	return fields.some(field => {
		const text = normalize(field)
		if (!text) return false
		const core = stripAdminSuffix(text)
		return text.includes(town) || town.includes(text) || (!!townCore && (text.includes(townCore) || core === townCore))
	})
}

const HAZARD_TYPE_IDS = ['seawall', 'fishing', 'mariculture', 'tourist']

const ready = () => surveyBaseMockStore.ensureReady()

/** 仅 Mock 模式可走运维万能表；业务主路径禁止 */
const listType = (type, opts) => baseApi.listType(type, opts)

/** 仅读本地种子（不请求 bizdata） */
const listLocalOnly = async type => {
	await surveyBaseMockStore.ensureReady()
	return surveyBaseMockStore.list(type) || []
}

const getCounties = async () => {
	try {
		const data = await queryRegionPopulation({})
		if (isNonEmptyList(data?.rows)) return data.rows
		if (data?.primary) return [data.primary]
	} catch (e) {
		if (!isMockEnabled()) throw e
	}
	if (!isMockEnabled()) throw new Error('行政区划底数查询失败')
	return listType('county')
}

const getCities = async () => {
	if (!isMockEnabled()) {
		const local = await listLocalOnly('city')
		return local
	}
	return listType('city')
}

const getTowns = async () => {
	if (!isMockEnabled()) {
		return listLocalOnly('town')
	}
	return listType('town')
}

const normalizeHazardRows = (list, exposureType) =>
	(list || []).map(item => ({
		...item,
		name: item.name || item.dangerName,
		exposureType
	}))

const getHazardsByType = async (typeId, { regionName, keyword } = {}) => {
	const exposureType = toExposureType(typeId) || typeId
	try {
		const data = await queryBaseExposures({
			exposureType,
			regionName: regionName || undefined,
			keyword: keyword || undefined
		})
		const grouped = data?.exposures || {}
		const list = grouped[exposureType] || grouped[String(exposureType)] || []
		return normalizeHazardRows(list, exposureType)
	} catch (e) {
		if (!isMockEnabled()) {
			try {
				return normalizeHazardRows(await remoteExposureList(exposureType, keyword || ''), exposureType)
			} catch (e2) {
				throw new Error(e?.message || `承灾体台账查询失败（${typeId}）`)
			}
		}
	}
	return listType(toExposureLegacyId(typeId) || typeId)
}

const getScenes = async () => {
	if (isMockEnabled()) {
		const fromBiz = await listType('rsScene').catch(() => [])
		if (isNonEmptyList(fromBiz)) return fromBiz
	}
	const manifest = await loadLocalRsManifest()
	const items = (manifest?.items || []).map(item => ({
		id: item.id,
		name: item.name || item.id,
		date: item.date || '',
		regionHint: item.regionHint || item.region || '',
		inundationRatio: Number(item.inundationRatio) || 0.45,
		imageId: item.id,
		sensor: item.sensor || '孔径雷达卫星'
	}))
	if (!items.length && !isMockEnabled()) {
		throw new Error('未找到遥感场景（请检查 public/datas/images/manifest.json）')
	}
	return items
}

const getReportedByType = async (hazardType, targetName) => {
	if (!isMockEnabled()) {
		/* 速报对比以 pipeline compare-quick-report 为准；此处不再读万能表 */
		return []
	}
	const list = await listType('reported')
	return list
		.filter(item => item.hazardType === hazardType)
		.filter(item => !targetName || !item.targetName || item.targetName === targetName)
		.map(item => ({
			year: item.year,
			count: item.count,
			area: item.area,
			length: item.length,
			loss: item.loss,
			targetName: item.targetName || '',
			disasterLevel: item.disasterLevel,
			population: item.affectedPopulation,
			reportNo: item.reportNo,
			disasterName: item.disasterName,
			disasterType: item.disasterType,
			status: item.status,
			region: item.region,
			countyCount: item.countyCount,
			damagedCount: item.damagedCount,
			affectedPopulation: item.affectedPopulation,
			evacuated: item.evacuated,
			infrastructureLoss: item.infrastructureLoss,
			industryLoss: item.industryLoss,
			reportOrg: item.reportOrg,
			reportor: item.reportor,
			remark: item.remark
		}))
}

const nearestByCoord = (list, lon, lat) => {
	if (!list?.length) return null
	let best = list[0]
	let bestDist = Number.POSITIVE_INFINITY
	list.forEach(item => {
		const d = Math.hypot(Number(item.lon) - Number(lon), Number(item.lat) - Number(lat))
		if (d < bestDist) {
			bestDist = d
			best = item
		}
	})
	return best
}

const findCountyByName = async name => {
	const keyword = normalize(name)
	const list = await getCounties()
	return (
		list.find(item => item.name === name) ||
		list.find(item => item.name.includes(keyword) || keyword.includes(item.name)) ||
		list.find(item => item.city === name || normalize(item.name) === keyword)
	)
}

const findTownByName = async name => {
	const keyword = normalize(name)
	const list = await getTowns()
	return list.find(item => item.name === name) || list.find(item => item.name.includes(keyword) || keyword.includes(item.name))
}

/** 系统 JSON 要素索引：sourceFid / dangerName */
const indexSystemFeatures = features => {
	const byFid = new Map()
	const byName = new Map()
	;(features || []).forEach(feature => {
		const props = feature?.properties || {}
		if (props.FID != null && props.FID !== '') byFid.set(String(props.FID), feature)
		const name = props.dangerName || props.name
		if (name) byName.set(String(name), feature)
	})
	return { byFid, byName }
}

const attachHazardFeature = (item, typeId, index) => {
	if (!item) return null
	const props = { dangerName: item.name, name: item.name, ...item }
	delete props.geojson
	delete props.feature

	if (index) {
		const fromFid = item.sourceFid != null ? index.byFid.get(String(item.sourceFid)) : null
		const fromName = index.byName.get(String(item.name)) || index.byName.get(String(item.dangerName || ''))
		const hit = fromFid || fromName
		if (hit?.geometry) {
			return {
				type: 'Feature',
				geometry: hit.geometry,
				properties: { ...hit.properties, ...props }
			}
		}
	}
	return makeSystemHazardFeature(item, typeId)
}

const buildHazardOverlay = async (typeId, hazardName) => {
	let list = await getHazardsByType(typeId)
	if (hazardName) {
		const hit = list.filter(item => item.name === hazardName || item.dangerName === hazardName)
		if (hit.length) list = hit
	}
	const rows = list.map(item => {
		const lengthKm = Number(item.lengthKm || item.length || 0)
		const rawArea = Number(item.areaKm2 || item.area || 0)
		const monitorValue = Number(item.monitorValue)
		const isSeawall = typeId === 'seawall'
		/** 面积：优先空间面积；非海堤且仅有监测万亩时反算 km² */
		const areaKm2 = rawArea
			? rawArea
			: !isSeawall && Number.isFinite(monitorValue)
				? wanMuToKm2(monitorValue)
				: 0
		const areaWanMu = rawArea
			? km2ToWanMu(rawArea)
			: !isSeawall && Number.isFinite(monitorValue)
				? monitorValue
				: km2ToWanMu(areaKm2)
		return {
			name: item.name,
			division: item.division,
			town: item.town,
			areaKm2: Number(areaKm2.toFixed?.(2) ?? areaKm2) || 0,
			areaWanMu: Number(areaWanMu.toFixed?.(2) ?? areaWanMu) || 0,
			lengthKm,
			disasterLevel: Number(item.disasterLevel) || 1,
			disasterLevelLabel: item.disasterLevelLabel || '',
			affectedPopulation: Number(item.affectedPopulation) || 0,
			economicLoss: Number(item.economicLoss) || 0,
			monitorValue: Number.isFinite(monitorValue) ? monitorValue : isSeawall ? lengthKm : areaWanMu,
			planValue: Number(item.planValue) || 0,
			monitorUnit: item.monitorUnit || (isSeawall ? 'km' : '万亩'),
			planUnit: item.planUnit || (isSeawall ? 'km' : '万亩')
		}
	})
	return {
		rows,
		totals: {
			count: rows.length,
			areaKm2: Number(rows.reduce((sum, item) => sum + (Number(item.areaKm2) || 0), 0).toFixed(2)),
			lengthKm: Number(rows.reduce((sum, item) => sum + (Number(item.lengthKm) || 0), 0).toFixed(2)),
			affectedPopulation: rows.reduce((sum, item) => sum + (Number(item.affectedPopulation) || 0), 0),
			economicLoss: Number(rows.reduce((sum, item) => sum + (Number(item.economicLoss) || 0), 0).toFixed(2))
		}
	}
}

const buildHazardOverlayFeatures = async (typeId, rows) => {
	const names = new Set((rows || []).map(item => item.name).filter(Boolean))
	const list = (await getHazardsByType(typeId)).filter(item => !names.size || names.has(item.name))
	let index = null
	try {
		index = indexSystemFeatures(await loadHazardFeatures(typeId))
	} catch (e) {
		index = null
	}
	return list.map(item => attachHazardFeature(item, typeId, index)).filter(Boolean)
}

/**
 * 灾情调查数据适配器。
 * 基础数据统一读取运维仓库；四类承灾体种子来自 public/datas 系统 JSON。
 */
export function surveyApi() {
	return {
		getRemoteSensingScenes: async () => {
			await ready()
			return getScenes()
		},
		/** 运维市/县台账组装为市→县下拉树（只读运维配置） */
		getAreaTree: async () => {
			await ready()
			const [counties, cities] = await Promise.all([getCounties(), getCities()])
			return buildAreaTree(counties, cities)
		},
		getDistrictSuggestions: async keyword => {
			await ready()
			const counties = await getCounties()
			const list = counties
				.map(item => ({ value: item.name }))
				.concat([...new Set(counties.map(item => item.city))].map(value => ({ value })))
			const text = (keyword || '').trim()
			return text ? list.filter(item => item.value.includes(text)) : list
		},
		getTownSuggestions: async (keyword, { district } = {}) => {
			await ready()
			const districtName = normalize(district)
			const districtCore = stripAdminSuffix(districtName)
			const matchDistrict = item => {
				if (!districtName) return true
				const county = normalize(item.county)
				const city = normalize(item.city)
				const countyCore = stripAdminSuffix(county)
				const cityCore = stripAdminSuffix(city)
				return (
					county === districtName ||
					city === districtName ||
					county.includes(districtName) ||
					districtName.includes(county) ||
					city.includes(districtName) ||
					districtName.includes(city) ||
					(!!districtCore && (countyCore === districtCore || cityCore === districtCore))
				)
			}
			const list = (await getTowns())
				.filter(matchDistrict)
				.map(item => ({
					value: item.name,
					label: item.name,
					county: item.county,
					city: item.city
				}))
			const text = (keyword || '').trim()
			return text ? list.filter(item => item.value.includes(text)) : list
		},
		queryDistrict: async ({ name, lon, lat }) => {
			await ready()
			await wait(280)
			// Step1：优先 /api/v1/survey/base/region-population
			let remotePrimary = null
			try {
				const data = await queryRegionPopulation({
					regionName: name || undefined,
					lon: Number.isFinite(Number(lon)) ? Number(lon) : undefined,
					lat: Number.isFinite(Number(lat)) ? Number(lat) : undefined
				})
				remotePrimary = data?.primary || data?.rows?.[0] || null
			} catch (e) {
				if (!isMockEnabled() && !name && !(Number.isFinite(Number(lon)) && Number.isFinite(Number(lat)))) {
					throw e
				}
			}
			const admin = name
				? await resolveAdminFeatureByName(name)
				: await resolveAdminFeatureByCoord(Number(lon), Number(lat))
			const ledger =
				remotePrimary ||
				(name
					? await findCountyByName(name)
					: admin
						? await findCountyByName(admin.name)
						: nearestByCoord(await getCounties().catch(() => []), Number(lon), Number(lat)))
			if (!admin && !ledger) {
				if (!isMockEnabled()) throw new Error('未查询到行政区划')
				return null
			}
			const feature = admin?.feature || (ledger ? makeDistrictFeature(ledger) : null)
			if (!feature) {
				if (!isMockEnabled() && !ledger) throw new Error('行政区划无空间范围')
				if (!feature) return null
			}
			return {
				...(ledger || {}),
				...(admin || {}),
				name: admin?.name || ledger?.name || remotePrimary?.regionName,
				city: admin?.city || ledger?.city,
				population: ledger?.population ?? admin?.population ?? remotePrimary?.population,
				density: ledger?.density ?? admin?.density ?? remotePrimary?.density,
				statArea: ledger?.statArea ?? admin?.statArea ?? remotePrimary?.statArea,
				lon: admin?.lon ?? ledger?.lon,
				lat: admin?.lat ?? ledger?.lat,
				regionCode: ledger?.regionCode || remotePrimary?.regionCode || ledger?.code,
				feature
			}
		},
		queryTown: async ({ name, lon, lat }) => {
			await ready()
			await wait(260)
			const town = name ? await findTownByName(name) : nearestByCoord(await getTowns(), Number(lon), Number(lat))
			if (!town) return null
			const county = (await getCounties()).find(item => item.name === town.county)
			const countyAdmin = town.county ? await resolveAdminFeatureByName(town.county) : null
			return {
				...town,
				feature: makeCircleFeature(town.lon, town.lat, 3.6, town),
				county: county || countyAdmin
					? {
							...(county || {}),
							...(countyAdmin || {}),
							name: countyAdmin?.name || county?.name || town.county,
							city: countyAdmin?.city || county?.city,
							feature: countyAdmin?.feature || (county ? makeDistrictFeature(county) : null)
					  }
					: null
			}
		},
		getHazards: async ({ typeId, keyword, district, town } = {}) => {
			await ready()
			await wait(220)
			const text = (keyword || '').trim()
			const districtName = (district || '').trim()
			const townName = (town || '').trim()
			// Step1：优先 /api/v1/survey/base/exposures
			const list = await getHazardsByType(typeId, { regionName: districtName, keyword: text })
			return list.filter(item => {
				if (districtName && !hazardMatchesDistrict(item, districtName)) return false
				if (townName && !hazardMatchesTown(item, townName)) return false
				if (!text) return true
				return (
					item.name?.includes(text) ||
					item.dangerName?.includes(text) ||
					item.division?.includes(text) ||
					item.town?.includes(text)
				)
			})
		},
		/** 解析单条承灾体真实/示意几何 */
		resolveHazardFeature: async ({ typeId, item }) => {
			if (!item) return null
			if (item.feature?.geometry) return item.feature
			await ready()
			let index = null
			try {
				index = indexSystemFeatures(await loadHazardFeatures(typeId))
			} catch (e) {
				index = null
			}
			return attachHazardFeature(item, typeId, index)
		},
		queryHazardByCoord: async ({ typeId, lon, lat }) => {
			await ready()
			await wait(220)
			const item = nearestByCoord(await getHazardsByType(typeId), Number(lon), Number(lat))
			if (!item) return null
			let index = null
			try {
				index = indexSystemFeatures(await loadHazardFeatures(typeId))
			} catch (e) {
				index = null
			}
			return {
				...item,
				feature: attachHazardFeature(item, typeId, index)
			}
		},
		getAffectedAreaMeta: async ({ sceneId, district, forceRefresh = false }) => {
			await ready()
			await wait(forceRefresh ? 400 : 200)
			const scenes = await getScenes()
			const scene =
				scenes.find(item => item.id === sceneId) ||
				scenes[0] || { id: sceneId, name: '默认场景', date: '', regionHint: '', inundationRatio: 0.45 }
			const sid = scene.id || sceneId || ''
			// 受灾面始终按本地影像范围（可与区县相交）绘制，不再使用示意圆
			const feature = await resolveAffectedFeature({ scene, district })

			if (!forceRefresh) {
				const hit = await findLatestSurveyResult({ kind: 'identify', sceneId: sid })
				if (hit?.payload?.meta) {
					return {
						scene,
						feature,
						meta: {
							...hit.payload.meta,
							sceneName: hit.payload.meta.sceneName || scene.name,
							sceneDate: hit.payload.meta.sceneDate || scene.date,
							regionHint: hit.payload.meta.regionHint || scene.regionHint
						},
						taskId: hit.taskId,
						source: 'remote'
					}
				}
			}

			try {
				const { data: computed } = await withMockFallback(
					() =>
						remoteComputeIdentify({
							sceneId: sid,
							sceneName: scene.name,
							sceneDate: scene.date,
							regionHint: scene.regionHint,
							inundationRatio: scene.inundationRatio || 0.45,
							districtName: district?.name || '',
							city: district?.city || '',
							persist: true
						}),
					() => null,
					d => !!d?.meta,
					{ silent: true }
				)
				if (computed?.meta) {
					return {
						scene,
						feature,
						meta: computed.meta,
						taskId: computed.taskId || '',
						source: 'compute'
					}
				}
			} catch (e) {
				if (!isMockEnabled() && !feature) throw e
			}

			let meta = null
			try {
				const summary = await summarizeAffectedArea(feature)
				if (summary?.areaKm2) {
					meta = {
						areaKm2: summary.areaKm2,
						areaWanMu: summary.areaWanMu,
						countyCount: summary.countyCount,
						counties: summary.counties,
						sceneName: scene.name,
						sceneDate: scene.date,
						regionHint: scene.regionHint,
						sourceHint: 'rs-image'
					}
				}
			} catch (err) {
				meta = null
			}
			if (!meta) {
				meta = await buildIdentifyMetaFromCounties(scene, district)
			}
			if (!meta && !isMockEnabled()) {
				throw new Error('受灾范围识别失败：无有效遥感面或行政区底数')
			}
			const taskId = await persistSurveyLiteTask({
				name: `受灾范围识别-${scene.name || sid || 'default'}`,
				targetId: 'population',
				sceneId: sid,
				resultData: {
					kind: 'identify',
					sceneId: sid,
					district: district?.name || district?.county || '',
					meta
				}
			})
			return {
				scene,
				feature,
				meta,
				taskId,
				source: taskId ? 'rs-image+persisted' : meta.sourceHint || 'rs-image'
			}
		},
		runOverlay: async ({ targetId, hazardName, sceneId, affectedFeature = null, forceRefresh = false }) => {
			await ready()
			await wait(forceRefresh ? 400 : 200)

			if (!forceRefresh) {
				const hit = await findLatestSurveyResult({ kind: 'overlay', sceneId, targetId })
				if (hit?.payload?.rows && hit.payload.totals) {
					const rows = hit.payload.rows
					const features = HAZARD_TYPE_IDS.includes(targetId)
						? await buildHazardOverlayFeatures(targetId, rows)
						: makeOverlayFeatures(targetId)
					return {
						target: targetId,
						rows,
						totals: hit.payload.totals,
						features,
						taskId: hit.taskId,
						source: 'remote'
					}
				}
			}

			/** 四类承灾体：优先空间相交叠置 */
			if (HAZARD_TYPE_IDS.includes(targetId) && affectedFeature) {
				try {
					const spatial = await overlayHazards(affectedFeature, targetId)
					if (spatial?.rows?.length) {
						let pack = spatial
						if (hazardName) {
							const filtered = spatial.rows.filter(r => r.name === hazardName)
							if (filtered.length) {
								pack = {
									...spatial,
									rows: filtered,
									features: (spatial.features || []).filter(
										f => (f.properties?.name || f.properties?.dangerName) === hazardName
									),
									totals: {
										count: filtered.length,
										areaKm2: Number(
											filtered.reduce((s, r) => s + (Number(r.areaKm2) || 0), 0).toFixed(2)
										),
										lengthKm: Number(
											filtered.reduce((s, r) => s + (Number(r.lengthKm) || 0), 0).toFixed(2)
										)
									}
								}
							}
						}
						const taskId = await persistSurveyLiteTask({
							name: `叠置分析-${targetId}${hazardName ? '-' + hazardName : ''}`,
							targetId,
							sceneId,
							resultData: {
								kind: 'overlay',
								sceneId: sceneId || '',
								targetId,
								hazardName: hazardName || '',
								mode: 'spatial',
								totals: pack.totals,
								rows: (pack.rows || []).slice(0, 80)
							}
						})
						return {
							...pack,
							target: targetId,
							taskId,
							source: 'spatial'
						}
					}
				} catch (e) {
					/* 回落台账 / 服务端计算 */
				}
			}

			try {
				const { data: computed } = await withMockFallback(
					() =>
						remoteComputeOverlay({
							sceneId,
							targetId,
							targetEntity: TARGET_ENTITY_MAP[targetId] || targetId,
							hazardName: hazardName || '',
							persist: true
						}),
					() => null,
					d => Array.isArray(d?.rows) && d?.totals,
					{ silent: true }
				)
				if (computed?.rows && computed?.totals) {
					const features = HAZARD_TYPE_IDS.includes(targetId)
						? await buildHazardOverlayFeatures(targetId, computed.rows)
						: makeOverlayFeatures(targetId)
					return {
						target: targetId,
						rows: computed.rows,
						totals: computed.totals,
						features,
						taskId: computed.taskId || '',
						source: 'compute'
					}
				}
			} catch (e) {
				/* 继续空间/台账叠置 */
			}

			let pack = null
			if (HAZARD_TYPE_IDS.includes(targetId)) {
				pack = await buildHazardOverlay(targetId, hazardName)
			} else if (targetId === 'district') {
				const towns = await getTowns()
				pack = {
					target: 'district',
					rows: towns.map(item => ({
						name: item.name,
						county: item.county,
						city: item.city,
						areaKm2: item.areaKm2,
						areaWanMu: km2ToWanMu(item.areaKm2 || 0)
					})),
					totals: {
						count: towns.length,
						areaKm2: Number(towns.reduce((sum, item) => sum + (Number(item.areaKm2) || 0), 0).toFixed(2))
					}
				}
			} else if (targetId === 'population') {
				const identify = await findLatestSurveyResult({ kind: 'identify', sceneId })
				if (identify?.payload?.meta) {
					pack = buildPopulationOverlayFromMeta(identify.payload.meta)
				} else {
					const scenes = await getScenes()
					const scene = scenes.find(item => item.id === sceneId) || scenes[0]
					const meta = await buildIdentifyMetaFromCounties(scene, null)
					pack = buildPopulationOverlayFromMeta(meta)
				}
				if (!pack?.rows?.length && isMockEnabled() && mockOverlayResults.population) {
					pack = mockOverlayResults.population
				}
			}
			if ((!pack || !pack.rows?.length) && !isMockEnabled()) {
				throw new Error(`叠置分析无结果（${targetId}），请确认受灾范围与承灾体底数`)
			}
			pack = pack || { rows: [], totals: { count: 0, areaKm2: 0 } }
			const features = HAZARD_TYPE_IDS.includes(targetId)
				? await buildHazardOverlayFeatures(targetId, pack.rows)
				: makeOverlayFeatures(targetId)
			const taskId = await persistSurveyLiteTask({
				name: `叠置分析-${targetId}${hazardName ? '-' + hazardName : ''}`,
				targetId,
				sceneId,
				resultData: {
					kind: 'overlay',
					sceneId: sceneId || '',
					targetId,
					hazardName: hazardName || '',
					totals: pack.totals,
					rows: (pack.rows || []).slice(0, 80)
				}
			})
			return {
				...pack,
				target: targetId,
				features,
				taskId,
				source: taskId ? 'ledger+persisted' : pack.source || 'spatial'
			}
		},
		getReportedLoss: async ({ hazardType, targetName }) => {
			await ready()
			await wait(260)
			const matched = await getReportedByType(hazardType, targetName)
			if (targetName && !matched.some(item => item.targetName === targetName)) {
				return getReportedByType(hazardType)
			}
			return matched
		},
		/** 调查对象详情台账：优先 /api/v1/survey/.../dossiers */
		getHazardDossier: async ({ typeId, hazard, overlayNote = '', compareNote = '', eventId = '' } = {}) => {
			await ready()
			await wait(120)
			const name = hazard?.name || hazard?.dangerName
			if (!typeId || !name) throw new Error('请先选择调查对象')
			const exposureType = toExposureType(typeId) || typeId
			const row =
				(await getHazardsByType(typeId).catch(() => [])).find(
					item => item.name === name || item.dangerName === name
				) || hazard

			if (eventId) {
				try {
					const list = await listSurveyDossiers(eventId, exposureType)
					const hit = (list || []).find(
						item =>
							item.hazardName === name ||
							item.exposureName === name ||
							item.name === name ||
							item.exposureId === hazard?.id
					)
					if (hit) {
						const profile = { ...buildProfile(row), ...(hit.profile || {}) }
						return hazardDossierStore.save({
							...hit,
							hazardType: typeId,
							hazardName: name,
							exposureType,
							profile,
							surveySummary: {
								...(hit.surveySummary || {}),
								overlayNote: overlayNote || hit.surveySummary?.overlayNote || '',
								compareNote: compareNote || hit.surveySummary?.compareNote || ''
							},
							materials: Array.isArray(hit.materials) ? hit.materials : [],
							attachments: Array.isArray(hit.attachments) ? hit.attachments : []
						})
					}
					const created = await saveSurveyDossier(eventId, {
						exposureType,
						exposureId: hazard?.id || '',
						hazardName: name,
						exposureName: name,
						profile: buildProfile(row),
						surveySummary: { overlayNote, compareNote }
					})
					if (created) {
						return hazardDossierStore.save({
							...created,
							hazardType: typeId,
							hazardName: name,
							exposureType
						})
					}
				} catch (e) {
					if (!isMockEnabled()) throw e
				}
			}

			if (!isMockEnabled()) {
				throw new Error('详情台账不存在：请传入 eventId 并确认后端 dossiers 接口')
			}

			const { data: remote } = await withMockFallback(
				() => remoteDossierGet(typeId, name),
				() => null,
				d => !!d && typeof d === 'object'
			)
			if (remote) {
				const profile = { ...(remote.profile || {}), ...buildProfile(row) }
				return hazardDossierStore.save({
					...remote,
					hazardType: typeId,
					hazardName: name,
					profile,
					surveySummary: {
						...(remote.surveySummary || {}),
						overlayNote: overlayNote || remote.surveySummary?.overlayNote || '',
						compareNote: compareNote || remote.surveySummary?.compareNote || ''
					},
					materials: Array.isArray(remote.materials) ? remote.materials : [],
					attachments: Array.isArray(remote.attachments) ? remote.attachments : []
				})
			}
			return hazardDossierStore.ensure(typeId, row, { overlayNote, compareNote })
		},
		queryHazardDossierMaterials: async ({ typeId, hazardName, keyword = '', category = '' }) => {
			await ready()
			await wait(80)
			let dossier = null
			try {
				dossier = await remoteDossierGet(typeId, hazardName)
			} catch (e) {
				dossier = null
			}
			if (!dossier) dossier = hazardDossierStore.get(typeId, hazardName)
			if (!dossier) return []
			const text = String(keyword || '').trim()
			return (dossier.materials || []).filter(item => {
				if (category && item.category !== category) return false
				if (!text) return true
				return [item.title, item.summary, item.collector, item.location].join('|').includes(text)
			})
		},
		queryHazardDossierAttachments: async ({ typeId, hazardName, keyword = '' }) => {
			await ready()
			await wait(80)
			let dossier = null
			try {
				dossier = await remoteDossierGet(typeId, hazardName)
			} catch (e) {
				dossier = null
			}
			if (!dossier) dossier = hazardDossierStore.get(typeId, hazardName)
			if (!dossier) return []
			const text = String(keyword || '').trim()
			return (dossier.attachments || []).filter(item => {
				if (!text) return true
				return [item.name, item.type, item.uploader, item.remark].join('|').includes(text)
			})
		},
		toggleMaterialVerified: async ({ typeId, hazardName, materialId, verified }) => {
			await ready()
			await wait(80)
			const load = async () => {
				try {
					return (await remoteDossierGet(typeId, hazardName)) || hazardDossierStore.get(typeId, hazardName)
				} catch (e) {
					return hazardDossierStore.get(typeId, hazardName)
				}
			}
			const dossier = await load()
			if (!dossier) throw new Error('详情台账不存在')
			const next = {
				...dossier,
				materials: (dossier.materials || []).map(item =>
					item.id === materialId ? { ...item, verified: !!verified } : item
				)
			}
			hazardDossierStore.save(next)
			try {
				const saved = await remoteDossierSave(next)
				if (saved) {
					hazardDossierStore.save(saved)
					return saved
				}
			} catch (e) {
				if (!isMockEnabled()) throw e
			}
			if (!isMockEnabled()) throw new Error('现场资料核对状态保存失败')
			return next
		},
		toggleAttachmentVerified: async ({ typeId, hazardName, attachmentId, verified }) => {
			await ready()
			await wait(80)
			let dossier = null
			try {
				dossier = (await remoteDossierGet(typeId, hazardName)) || hazardDossierStore.get(typeId, hazardName)
			} catch (e) {
				dossier = hazardDossierStore.get(typeId, hazardName)
			}
			if (!dossier) throw new Error('详情台账不存在')
			const next = {
				...dossier,
				attachments: (dossier.attachments || []).map(item =>
					item.id === attachmentId ? { ...item, verified: !!verified } : item
				)
			}
			hazardDossierStore.save(next)
			try {
				const saved = await remoteDossierSave(next)
				if (saved) {
					hazardDossierStore.save(saved)
					return saved
				}
			} catch (e) {
				if (!isMockEnabled()) throw e
			}
			if (!isMockEnabled()) throw new Error('附件核对状态保存失败')
			return next
		},
		verifyHazardDossier: async ({ typeId, hazardName, verifyStatus, verifyRemark, verifiedBy }) => {
			await ready()
			await wait(120)
			let dossier = null
			try {
				dossier = (await remoteDossierGet(typeId, hazardName)) || hazardDossierStore.get(typeId, hazardName)
			} catch (e) {
				dossier = hazardDossierStore.get(typeId, hazardName)
			}
			if (!dossier) throw new Error('详情台账不存在')
			const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
			const next = {
				...dossier,
				verifyStatus: verifyStatus || '已核对',
				verifyRemark: verifyRemark || '',
				verifiedBy: verifiedBy || '调查核查员',
				verifiedAt: now
			}
			hazardDossierStore.save(next)
			try {
				const saved = await remoteDossierSave(next)
				if (saved) {
					hazardDossierStore.save(saved)
					return saved
				}
			} catch (e) {
				if (!isMockEnabled()) throw e
			}
			if (!isMockEnabled()) throw new Error('台账核对结果保存失败')
			return next
		},
		archiveHazardDossier: async ({ typeId, hazardName, archiveRemark, archivedBy }) => {
			await ready()
			await wait(140)
			let dossier = null
			try {
				dossier = (await remoteDossierGet(typeId, hazardName)) || hazardDossierStore.get(typeId, hazardName)
			} catch (e) {
				dossier = hazardDossierStore.get(typeId, hazardName)
			}
			if (!dossier) throw new Error('详情台账不存在')
			if (dossier.verifyStatus === '待核对') {
				throw new Error('请先完成资料核对后再归档')
			}
			const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
			const archiveNo =
				dossier.archiveNo ||
				`DA-${String(typeId).slice(0, 2).toUpperCase()}-${Date.now().toString().slice(-8)}`
			const next = {
				...dossier,
				archiveStatus: '已归档',
				archiveNo,
				archiveRemark: archiveRemark || dossier.archiveRemark || '',
				archivedBy: archivedBy || dossier.verifiedBy || '调查归档员',
				archivedAt: now
			}
			hazardDossierStore.save(next)
			try {
				const saved = await remoteDossierSave(next)
				if (saved) {
					hazardDossierStore.save(saved)
					return saved
				}
			} catch (e) {
				if (!isMockEnabled()) throw e
			}
			if (!isMockEnabled()) throw new Error('台账归档失败')
			return next
		},
		listArchivedDossiers: async ({ keyword = '' } = {}) => {
			await ready()
			await wait(100)
			const { data } = await withMockFallback(
				() => remoteDossierListArchived(keyword),
				() => hazardDossierStore.listArchived(keyword),
				isNonEmptyList
			)
			return data
		},
		/**
		 * 上传现场资料或附件
		 * @param {{ typeId, hazardName, kind: 'material'|'attachment', file: File, meta?: object }}
		 */
		uploadHazardDossierFile: async ({ typeId, hazardName, kind, file, meta = {} }) => {
			await ready()
			if (!typeId || !hazardName) throw new Error('请先选择调查对象')
			if (!file) throw new Error('请选择文件')
			const formData = new FormData()
			formData.append('file', file)
			formData.append('kind', kind === 'attachment' ? 'attachment' : 'material')
			formData.append('hazardType', typeId)
			formData.append('hazardName', hazardName)
			Object.entries(meta || {}).forEach(([key, value]) => {
				if (value != null && value !== '') formData.append(key, value)
			})
			try {
				const remote = await remoteDossierUpload(formData)
				if (remote?.dossier) {
					hazardDossierStore.save(remote.dossier)
					return remote.dossier
				}
			} catch (e) {
				if (!isMockEnabled()) throw e
			}
			if (!isMockEnabled()) throw new Error('文件上传失败')
			const local = await hazardDossierStore.uploadLocal({
				typeId,
				hazardName,
				kind: kind === 'attachment' ? 'attachment' : 'material',
				file,
				meta
			})
			if (!local) throw new Error('上传失败')
			try {
				const saved = await remoteDossierSave(local)
				if (saved) {
					hazardDossierStore.save(saved)
					return saved
				}
			} catch (e) {
				/* Mock 模式仅本地 */
			}
			return local
		},
		removeHazardDossierFile: async ({ typeId, hazardName, kind, itemId }) => {
			await ready()
			try {
				const remote = await remoteDossierRemoveItem({
					hazardType: typeId,
					hazardName,
					kind: kind === 'attachment' ? 'attachment' : 'material',
					itemId
				})
				if (remote?.dossier) {
					hazardDossierStore.save(remote.dossier)
					return remote.dossier
				}
			} catch (e) {
				/* mock */
			}
			const next = hazardDossierStore.removeItem(typeId, hazardName, kind, itemId)
			if (!next) throw new Error('删除失败')
			try {
				const saved = await remoteDossierSave(next)
				if (saved) {
					hazardDossierStore.save(saved)
					return saved
				}
			} catch (e) {
				/* local */
			}
			return next
		},
		/** 查看/下载：优先远端文件，其次本地 dataUrl */
		openHazardDossierFile: async item => {
			if (!item) throw new Error('文件不存在')
			if (item.fileUrl) {
				return { url: item.fileUrl, name: item.fileName || item.name || 'file', local: true }
			}
			if (!item.filePath) throw new Error('该条目无可查看文件')
			const blob = await remoteDossierDownload(item.filePath)
			if (!(blob instanceof Blob)) throw new Error('下载失败')
			const url = URL.createObjectURL(blob)
			return { url, name: item.fileName || item.name || 'file', local: false, revoke: true }
		}
	}
}
