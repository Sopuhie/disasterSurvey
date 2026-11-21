import { ElMessage } from 'element-plus'
import { assessmentApi } from '@/api/assessment/index.js'
import {
	generateAssessmentReport,
	listAssessmentRecords,
	listAssessmentReports,
	resolveDisasterType,
	runComprehensive,
	stormEconomicLoss,
	stormIntegrate,
	waveBehavior,
	waveDamage,
	windBehavior,
	windDamage
} from '@/api/assessment/pipeline.js'
import { surveyApi } from '@/api/survey/index.js'
import { findLatestEvaluateResult } from '@/api/taskResultHydrate.js'
import { isMockEnabled } from '@/constants/disasterEnums.js'
import { useAssessmentStore } from '@/store/modules/assessment.js'
import { useDisasterWorkflowStore } from '@/store/modules/disasterWorkflow.js'
import { useSurveyBaselineStore } from '@/store/modules/surveyBaseline.js'
import { useMapStore } from '@/store/modules/map.js'
import { getAssessType } from '@/views/assessment/constants.js'
import { emptyIntegrateForm } from '@/views/assessment/mock/integrateData.js'
import {
	buildWindBehavior,
	calcEconomicLoss,
	damageRowsToFeatures,
	decorateDamageRows,
	inundationRowsToFeatures,
	scaleDamageByScene,
	scaleMetricRows
} from '@/views/assessment/utils/assessmentEngine.js'
import { buildComprehensiveReport } from '@/views/assessment/utils/reportBuilder.js'
import {
	clearAssessmentLayers,
	clearThemeLayers,
	drawAffected,
	drawDistrict,
	drawImpact,
	drawOvertop,
	drawPath,
	drawSceneImage,
	drawSurveyBaseline,
	setThemeLayer
} from '@/views/assessment/utils/assessmentLayers.js'
import { enrichScenesWithLocalImages } from '@/utils/localRsImages.js'
import { makeAffectedFeature } from '@/views/survey/mock/surveyDemoData.js'

const api = assessmentApi()
const survey = surveyApi()
const DEFAULT_DISTRICT = '连江县'

const waitForMap = async (timeout = 8000) => {
	const start = Date.now()
	while (Date.now() - start < timeout) {
		const map = useMapStore().activeMap
		if (map?.layerManager || map?.lyrManager) return true
		await new Promise(resolve => setTimeout(resolve, 120))
	}
	return !!(useMapStore().activeMap?.layerManager || useMapStore().activeMap?.lyrManager)
}

const notice = (type, message, silent) => {
	if (!silent) ElMessage[type](message)
}

export function useAssessmentActions() {
	const store = useAssessmentStore()
	const baselineStore = useSurveyBaselineStore()
	const workflowStore = useDisasterWorkflowStore()

	const currentScene = () => store.scenes.find(item => item.id === store.selectedSceneId) || store.scenes[0]

	const requireEventId = () => {
		if (!workflowStore.currentEventId) {
			throw new Error('请先在灾情调查中绑定或创建灾害事件')
		}
		return workflowStore.currentEventId
	}

	const requireConfirmedBaseline = () => {
		if (!workflowStore.isBaselineConfirmed) {
			throw new Error('调查基线尚未确认锁定，请先完成灾情调查 Step4')
		}
		return workflowStore.currentBaseline
	}

	const syncWorkflow = async () => {
		if (!workflowStore.currentEventId) return null
		try {
			await workflowStore.refreshEvent()
			await workflowStore.refreshBaseline().catch(() => null)
		} catch (e) {
			/* 事件可能已失效 */
		}
		return workflowStore.currentEvent
	}

	/** 调查基线仍参与损失等计算，但不再上图（橙虚线受灾面 / 青绿叠置易误解） */
	const applySurveyBaselineOnMap = () => {
		drawSurveyBaseline(null)
	}

	const syncDistrictFromBaseline = async () => {
		const name = baselineStore.package?.district?.name
		if (!name) return
		if (!store.queriedDistrict || store.queriedDistrict.name !== name) {
			store.districtKeyword = name
			const hit = await survey.queryDistrict({ name })
			if (hit) {
				store.queriedDistrict = hit
				drawDistrict(hit.feature)
			}
		}
	}

	const loadSelectedSceneImage = async ({ fly = true } = {}) => {
		await waitForMap()
		return drawSceneImage(currentScene(), { fly })
	}

	const ensureDistrict = async () => {
		if (store.queriedDistrict) {
			if (store.queriedDistrict.feature) drawDistrict(store.queriedDistrict.feature)
			return store.queriedDistrict
		}
		const name = store.districtKeyword?.trim() || DEFAULT_DISTRICT
		store.districtKeyword = name
		const hit = await survey.queryDistrict({ name })
		if (!hit) return null
		store.queriedDistrict = hit
		drawDistrict(hit.feature)
		return hit
	}

	const initAssessment = async typeId => {
		if (typeId && typeId !== store.currentType) {
			store.setType(typeId)
		}
		// 步骤配置变更后（如隐藏漫堤分析），纠正无效 currentStep
		if (!store.steps.some(item => item.id === store.currentStep)) {
			store.setStep(store.steps[0]?.id || 'integrate')
		}
		if (!store.districtKeyword) store.districtKeyword = DEFAULT_DISTRICT
		await syncWorkflow()
		if (workflowStore.currentEvent?.regionName && !store.districtKeyword) {
			store.districtKeyword = workflowStore.currentEvent.regionName
		}
		try {
			if (workflowStore.currentEventId) {
				const records = await listAssessmentRecords(
					workflowStore.currentEventId,
					resolveDisasterType(store.currentType)
				)
				const integrateRec = (records || []).find(
					r => String(r.subType || r.assessmentSubType || '').includes('INTEGRATION')
				)
				if (integrateRec?.result?.items) {
					store.integrateList = integrateRec.result.items.map((item, idx) => ({
						id: `INT-${idx}`,
						status: '已标准化',
						county: integrateRec.result.district || '',
						hazardName: item.metric,
						damage: `${item.value ?? ''}${item.unit || ''}`,
						...item
					}))
				}
			}
		} catch (e) {
			/* 无记录记录时再走台账 */
		}
		if (!store.integrateList.length) {
			try {
				store.integrateList = await api.getIntegrateRecords()
			} catch (e) {
				store.integrateList = []
			}
		}
		const scenes = await api.getSarScenes(store.currentType)
		store.scenes = await enrichScenesWithLocalImages(scenes)
		store.selectedSceneId = store.scenes[0]?.id || ''
		if (!store.typhoonEvents.length) {
			store.typhoonEvents = await api.getTyphoonEvents()
			store.selectedTyphoonId = store.typhoonEvents[0]?.id || ''
		} else if (!store.selectedTyphoonId) {
			store.selectedTyphoonId = store.typhoonEvents[0]?.id || ''
		}
		try {
			store.archivedReports = await listAssessmentReports({
				disasterType: resolveDisasterType(store.currentType)
			})
		} catch (e) {
			try {
				store.archivedReports = await api.listArchivedReports()
			} catch (err) {
				store.archivedReports = []
			}
		}
		await waitForMap()
		await baselineStore.loadByScene(store.selectedSceneId)
		if (baselineStore.package?.sceneId && store.scenes.some(s => s.id === baselineStore.package.sceneId)) {
			store.selectedSceneId = baselineStore.package.sceneId
		}
		await syncDistrictFromBaseline()
		applySurveyBaselineOnMap({ fly: true })
		await ensureAssessmentStepResult(store.currentStep)
	}

	const fetchDistrictSuggestions = queryString => survey.getDistrictSuggestions(queryString)
	const fetchAreaTree = () => survey.getAreaTree()

	const queryDistrictByName = async () => {
		const name = store.districtKeyword.trim()
		if (!name) {
			ElMessage.warning('请输入沿海县市名称')
			return
		}
		const hit = await survey.queryDistrict({ name })
		if (!hit) {
			ElMessage.warning('未查询到对应行政区划')
			return
		}
		store.queriedDistrict = hit
		drawDistrict(hit.feature)
		ElMessage.success(`已定位到${hit.name}`)
	}

	const queryDistrictByCoord = async () => {
		const lon = Number(store.coordLon)
		const lat = Number(store.coordLat)
		if (!Number.isFinite(lon) || !Number.isFinite(lat)) {
			ElMessage.warning('请输入有效经纬度')
			return
		}
		const hit = await survey.queryDistrict({ lon, lat })
		if (!hit) {
			ElMessage.warning('该坐标未落入沿海区县范围')
			return
		}
		store.queriedDistrict = hit
		store.districtKeyword = hit.name
		drawDistrict(hit.feature)
		ElMessage.success(`已定位到${hit.name}`)
	}

	const toggleThemeLayer = (themeId, enabled) => {
		store.themeLayers[themeId] = !!enabled
		setThemeLayer(themeId, !!enabled)
	}

	const saveIntegrate = async () => {
		const form = store.integrateForm
		if (!form.county || !form.hazardName || !form.damage) {
			ElMessage.warning('请完整填写区县、承灾体名称和受损情况')
			return
		}
		try {
			const eventId = requireEventId()
			const items = [
				{
					metric: form.hazardName,
					unit: '',
					value: form.damage,
					source: form.sourceId || '速报',
					facility: form.facility,
					level: form.level,
					county: form.county,
					city: form.city
				},
				...store.integrateList.map(row => ({
					metric: row.hazardName || row.metric,
					unit: row.unit || '',
					value: row.value ?? row.damage,
					source: row.source || '速报'
				}))
			]
			const remote = await stormIntegrate(eventId, {
				districtName: form.county,
				quickReport: items,
				items
			})
			const normalized = remote?.result?.items || []
			if (normalized.length) {
				store.integrateList = normalized.map((item, idx) => ({
					id: `INT-${idx}`,
					status: '已标准化',
					county: form.county,
					city: form.city,
					hazardName: item.metric,
					damage: `${item.value ?? ''}${item.unit || ''}`,
					...item
				}))
			} else {
				const saved = { ...form, id: `ST-${Date.now().toString().slice(-4)}`, status: '已标准化' }
				store.integrateList = [saved, ...store.integrateList]
			}
			store.integrateForm = emptyIntegrateForm()
			store.showIntegrateForm = false
			ElMessage.success('已标准化入库并写入评估记录')
			return
		} catch (e) {
			if (!isMockEnabled()) {
				ElMessage.error(e?.message || '信息整合失败')
				return
			}
		}
		const saved = await api.saveIntegrateRecord(form)
		store.integrateList = [saved, ...store.integrateList]
		store.integrateForm = emptyIntegrateForm()
		store.showIntegrateForm = false
		ElMessage.success('已标准化入库（本地）')
	}

	const runLoss = async ({ silent = false, forceRefresh } = {}) => {
		if (!store.queriedDistrict) {
			if (silent) await ensureDistrict()
			else {
				ElMessage.warning('请先查询沿海县市')
				return
			}
		}
		if (!store.queriedDistrict) return
		store.lossLoading = true
		try {
			await syncWorkflow()
			requireConfirmedBaseline()
			await loadSelectedSceneImage({ fly: !silent })
			const district = store.queriedDistrict
			const scene = currentScene()
			store.lossResult = null

			const ratio = scene?.inundationRatio || 0.46
			const baselineArea =
				Number(workflowStore.currentBaseline?.affectedAreaKm2) ||
				Number(baselineStore.package?.affectedMeta?.areaKm2) ||
				0
			const eventId = requireEventId()
			const remote = await stormEconomicLoss(eventId, {
				districtName: district.name || '',
				city: district.city || '',
				sceneId: store.selectedSceneId || scene?.id || '',
				sceneName: scene?.name || '',
				sceneDate: scene?.date || '',
				statArea: district.statArea,
				density: district.density,
				inundationRatio: ratio,
				affectedKm2: baselineArea > 0 ? baselineArea : undefined,
				forceRefresh: forceRefresh ?? !silent
			})
			const lossPayload = remote?.lossResult || remote?.result || remote
			if (lossPayload) {
				store.lossResult = {
					sceneName: lossPayload.sceneName || scene?.name || '灾后遥感影像',
					sceneDate: scene?.date || '',
					sensor: scene?.sensor || '孔径雷达卫星',
					economy: lossPayload.economy,
					summary: lossPayload.summary || {
						areaKm2: lossPayload.summary?.areaKm2,
						countyCount: 1,
						counties: [{ name: district.name, city: district.city }]
					},
					landHint: '受灾面积取自已确认调查基线，由 /api/v1/assessment 风暴潮经济损失接口测算。',
					fromSurveyBaseline: true,
					totalLoss: lossPayload.totalLoss,
					gdpShock: lossPayload.gdpShock,
					landLoss: lossPayload.landLoss,
					...lossPayload
				}
				store.evaluateTaskId = remote?.id || remote?.assessmentId || store.evaluateTaskId
			}

			drawDistrict(district.feature)
			const baseFeat = baselineStore.package?.affectedFeature
			drawAffected(baseFeat || makeAffectedFeature(district))
			applySurveyBaselineOnMap()
			Object.keys(store.themeLayers || {}).forEach(id => {
				if (store.themeLayers[id]) setThemeLayer(id, true)
			})
			notice('success', '经济损失预估完成（已引用确认基线）', silent)
		} catch (err) {
			if (isMockEnabled() && !String(err?.message || '').includes('基线')) {
				try {
					const district = store.queriedDistrict
					const scene = currentScene()
					const ratio = scene?.inundationRatio || 0.46
					const baselineArea = Number(baselineStore.package?.affectedMeta?.areaKm2) || 0
					const affectedKm2 =
						baselineArea > 0
							? baselineArea
							: Number(((district.statArea || 680) * 0.078 * (0.72 + ratio)).toFixed(2))
					const economy = await api.getCountyEconomy(district.name)
					const loss = calcEconomicLoss(economy, affectedKm2)
					store.lossResult = {
						sceneName: scene?.name || '灾后遥感影像',
						economy,
						summary: { areaKm2: affectedKm2, countyCount: 1, counties: [{ name: district.name }] },
						fromSurveyBaseline: baselineArea > 0,
						...loss
					}
					notice('success', '经济损失预估完成（Mock）', silent)
					return
				} catch (e2) {
					/* fallthrough */
				}
			}
			notice('error', err?.message || '经济损失评估失败', silent)
		} finally {
			store.lossLoading = false
		}
	}

	const runDamage = async ({ silent = false } = {}) => {
		store.damageLoading = true
		try {
			await syncWorkflow()
			await loadSelectedSceneImage({ fly: !silent })
			const kind = store.currentType === 'wave' ? 'wave' : 'wind'
			const scene = currentScene()
			if (store.selectedSceneId) {
				await baselineStore.loadByScene(store.selectedSceneId)
			}
			const eventId = requireEventId()
			const body = {
				sceneName: scene?.name || '',
				sarImageId: store.selectedSceneId || scene?.id || '',
				maxWind: scene?.maxWind,
				maxHeight: scene?.maxHeight
			}
			const remote = kind === 'wave' ? await waveDamage(eventId, body) : await windDamage(eventId, body)
			const rows = remote?.result?.rows || remote?.rows
			if (Array.isArray(rows) && rows.length) {
				store.damageRows = decorateDamageRows(rows, kind)
			} else if (isMockEnabled()) {
				const raw = kind === 'wave' ? await api.getWaveDamage() : await api.getWindDamage()
				const scaled = scaleDamageByScene(raw, scene, kind)
				store.damageRows = decorateDamageRows(scaled, kind)
			} else if (!Array.isArray(rows) || !rows.length) {
				throw new Error('破坏性分析无结果，请检查评估接口返回')
			}
			drawImpact(damageRowsToFeatures(store.damageRows))
			applySurveyBaselineOnMap()
			store.evaluateTaskId = remote?.id || store.evaluateTaskId
			notice('success', `破坏性分析完成（${scene?.name || '当前场景'}）`, silent)
		} catch (err) {
			if (isMockEnabled()) {
				try {
					const kind = store.currentType === 'wave' ? 'wave' : 'wind'
					const scene = currentScene()
					const raw = kind === 'wave' ? await api.getWaveDamage() : await api.getWindDamage()
					store.damageRows = decorateDamageRows(scaleDamageByScene(raw, scene, kind), kind)
					drawImpact(damageRowsToFeatures(store.damageRows))
					notice('success', '破坏性分析完成（Mock）', silent)
					return
				} catch (e2) {
					/* fallthrough */
				}
			}
			notice('error', err?.message || '破坏性分析失败', silent)
		} finally {
			store.damageLoading = false
		}
	}

	const runBehavior = async ({ silent = false } = {}) => {
		store.behaviorLoading = true
		try {
			await syncWorkflow()
			await loadSelectedSceneImage({ fly: !silent })
			const eventId = requireEventId()
			const scene = currentScene()
			if (store.currentType === 'wind') {
				const event = store.typhoonEvents.find(item => item.id === store.selectedTyphoonId) || store.typhoonEvents[0]
				const remote = await windBehavior(eventId, {
					eventName: event?.name,
					maxWind: event?.maxWind || scene?.maxWind,
					points: event?.points || event?.track || undefined,
					sceneName: scene?.name
				})
				const remoteResult = remote?.result || remote
				const local = buildWindBehavior(event)
				const windScale = Number(remoteResult?.maxWind || event?.maxWind || scene?.maxWind || 30) / 36
				const structures = isMockEnabled()
					? await api.getStructureDamage().catch(() => [])
					: await api.getStructureDamage()
				const inundationRows = scaleMetricRows(
					isMockEnabled()
						? await api.getWindInundation().catch(() => [])
						: await api.getWindInundation(),
					windScale
				)
				const inundationKm2 =
					Number(remoteResult?.inundationRadiusKm) ||
					Number(inundationRows.reduce((sum, item) => sum + item.areaKm2, 0).toFixed(2))
				const affectedPopulation = Math.round(
					inundationRows.reduce((sum, item) => sum + (Number(item.population) || 0), 0)
				)
				store.behaviorResult = {
					kind: 'wind',
					...local,
					maxWind: remoteResult?.maxWind || local.maxWind,
					avgDir: remoteResult?.avgDir || local.avgDir,
					scene,
					structures,
					inundationRows,
					inundationKm2,
					affectedPopulation,
					desc: `依据台风「${event?.name || remoteResult?.eventName || '-'}」与后端海风行为分析，最大风速 ${remoteResult?.maxWind || local.maxWind} m/s，估测影响半径约 ${inundationKm2} km。`
				}
				drawPath([local.pathFeature, ...local.pointFeatures])
				drawAffected(inundationRowsToFeatures(inundationRows))
			} else {
				const remote = await waveBehavior(eventId, {
					sceneName: scene?.name,
					maxHeight: scene?.maxHeight,
					maxSpeed: scene?.maxSpeed,
					period: scene?.period
				})
				const remoteResult = remote?.result || remote
				const structures = isMockEnabled()
					? await api.getStructureDamage().catch(() => [])
					: await api.getStructureDamage()
				const waveScale = Number(scene?.maxHeight || remoteResult?.maxHeight || 4) / 4
				const erosion = scaleMetricRows(
					isMockEnabled() ? await api.getWaveErosion().catch(() => []) : await api.getWaveErosion(),
					waveScale
				)
				const inundationKm2 =
					Number(remoteResult?.inundationKm2) ||
					Number(erosion.reduce((sum, item) => sum + item.areaKm2, 0).toFixed(2))
				const lengthKm =
					Number(remoteResult?.lengthKm) ||
					Number(erosion.reduce((sum, item) => sum + item.lengthKm, 0).toFixed(1))
				const avgSpeed = scene?.maxSpeed ? Number((scene.maxSpeed * 0.82).toFixed(1)) : 11.1
				store.behaviorResult = {
					kind: 'wave',
					scene,
					structures,
					erosion,
					inundation: makeAffectedFeature(store.queriedDistrict),
					inundationKm2,
					lengthKm,
					maxHeight: remoteResult?.maxHeight || scene?.maxHeight || 4.8,
					period: remoteResult?.period || scene?.period || 9.2,
					avgSpeed: remoteResult?.avgSpeed || avgSpeed,
					direction: remoteResult?.direction || 'ESE',
					energyIndex: remoteResult?.energyIndex || Math.round(((scene?.maxHeight || 4.8) ** 2) * 8),
					desc:
						remoteResult?.desc ||
						`依据 ${scene?.name || 'SAR 波浪场'} 海浪行为分析：侵蚀约 ${lengthKm} km、范围约 ${inundationKm2} km²。`
				}
				drawAffected(store.behaviorResult.inundation)
			}
			notice('success', '行为分析完成', silent)
		} catch (err) {
			notice('error', err?.message || '行为分析失败', silent)
		} finally {
			store.behaviorLoading = false
		}
	}

	const loadOvertopping = async ({ silent = false } = {}) => {
		store.overtoppingLoading = true
		try {
			const cityHint = store.queriedDistrict?.city || store.overtoppingCity || ''
			store.overtoppingRows = await api.getOvertoppingRows({
				city: store.overtoppingCity || cityHint,
				keyword: store.overtoppingKeyword
			})
			const features = await api.getOvertoppingFeatures(store.overtoppingRows)
			drawOvertop(features)
			applySurveyBaselineOnMap()
			notice('success', `漫堤分析加载完成，共 ${store.overtoppingRows.length} 条（已上图）`, silent)
		} catch (err) {
			store.overtoppingRows = []
			drawOvertop([])
			notice('error', err?.message || '漫堤分析加载失败', silent)
		} finally {
			store.overtoppingLoading = false
		}
	}

	const openStormDetail = row => {
		store.stormDetailRow = row
		store.stormDetailVisible = true
	}

	const runReport = async ({ openPreview = true, forceRefresh } = {}) => {
		const typeLabel = getAssessType(store.currentType).label
		const scene = currentScene()
		store.reportExportPath = ''
		if (openPreview) store.reportGenerating = true
		try {
			await syncWorkflow()
			if (store.currentType === 'storm') {
				requireConfirmedBaseline()
			}
			const eventId = requireEventId()
			if (store.selectedSceneId) {
				await baselineStore.loadByScene(store.selectedSceneId)
			}

			const body = {
				typeId: store.currentType,
				disasterType: resolveDisasterType(store.currentType),
				sceneName: scene?.name || '',
				structure: store.reportFactors.structure,
				response: store.reportFactors.response,
				prepare: store.reportFactors.prepare,
				note: store.reportFactors.note,
				forceRefresh: forceRefresh ?? openPreview
			}
			const comprehensive = await runComprehensive(eventId, store.currentType, body)
			const summary = comprehensive?.result || comprehensive
			store.evaluateTaskId = comprehensive?.id || comprehensive?.assessmentId || store.evaluateTaskId

			const localReport = buildComprehensiveReport({
				typeId: store.currentType,
				typeLabel,
				district: store.queriedDistrict,
				loss: store.lossResult,
				damageRows: store.damageRows,
				behavior: store.behaviorResult,
				factors: store.reportFactors,
				integrateList: store.integrateList,
				overtoppingRows: store.overtoppingRows,
				scene,
				typhoon: store.typhoonEvents.find(item => item.id === store.selectedTyphoonId),
				surveySummary: null,
				surveyBaseline: baselineStore.package
			})
			store.reportResult = {
				...localReport,
				title: localReport.title || `${typeLabel}综合评估报告`,
				grade: summary?.grade || localReport.grade,
				total: summary?.totalScore ?? localReport.total,
				totalScore: summary?.totalScore ?? localReport.total,
				factors: summary?.factors || localReport.factors,
				recommend: summary?.recommend || localReport.recommend,
				assessmentId: comprehensive?.id,
				eventId,
				fromPipeline: true
			}
			applySurveyBaselineOnMap()
			if (openPreview) store.reportVisible = true
		} catch (err) {
			if (isMockEnabled()) {
				store.reportResult = buildComprehensiveReport({
					typeId: store.currentType,
					typeLabel,
					district: store.queriedDistrict,
					loss: store.lossResult,
					damageRows: store.damageRows,
					behavior: store.behaviorResult,
					factors: store.reportFactors,
					integrateList: store.integrateList,
					overtoppingRows: store.overtoppingRows,
					scene,
					typhoon: store.typhoonEvents.find(item => item.id === store.selectedTyphoonId),
					surveyBaseline: baselineStore.package
				})
				if (openPreview) store.reportVisible = true
				return
			}
			ElMessage.error(err?.message || '综合评估失败')
		} finally {
			if (openPreview) store.reportGenerating = false
		}
	}

	/** 服务端报告导出；无任务或失败时返回空，由预览页回落本地打印/JSON */
	const exportRemoteReport = async () => {
		if (!store.evaluateTaskId) {
			if (!store.reportResult) await runReport({ openPreview: false })
		}
		if (!store.evaluateTaskId) return ''
		const path = await api.exportEvaluateReport(store.evaluateTaskId)
		store.reportExportPath = path || ''
		return path || ''
	}

	const archiveReport = async ({ remark = '', archivedBy = '评估归档员' } = {}) => {
		try {
			await syncWorkflow()
			const eventId = requireEventId()
			if (!store.reportResult) {
				await runReport({ openPreview: false })
			}
			if (!store.reportResult) {
				ElMessage.warning('请先生成评估报告')
				return null
			}
			const scene = currentScene()
			const saved = await generateAssessmentReport(eventId, {
				typeId: store.currentType,
				disasterType: resolveDisasterType(store.currentType),
				reportTitle: store.reportResult.title,
				sceneName: scene?.name || store.reportResult.sceneName,
				remark,
				archivedBy
			})
			store.archivedReports = await listAssessmentReports({
				disasterType: resolveDisasterType(store.currentType)
			})
			if (saved?.snapshot) {
				store.reportResult = {
					...store.reportResult,
					...saved.snapshot,
					reportNo: saved.reportNo,
					downloadPath: saved.downloadPath || saved.reportFilePath
				}
			}
			ElMessage.success(`报告已归档：${saved.reportNo || saved.id}`)
			return saved
		} catch (e) {
			ElMessage.error(e?.message || '报告归档失败')
			throw e
		}
	}

	const openArchivedReport = async id => {
		try {
			const row = await api.getArchivedReport(id)
			const snap = row?.snapshot
			if (!snap) {
				ElMessage.warning('未找到归档报告')
				return
			}
			store.reportResult = {
				...snap,
				title: snap.title || row.title || snap.eventName,
				grade: snap.grade || row.grade,
				total: snap.totalScore ?? row.totalScore,
				reportNo: row.reportNo
			}
			store.reportVisible = true
		} catch (e) {
			ElMessage.error(e?.message || '打开归档报告失败')
		}
	}

	const ensureAssessmentStepResult = async (step = store.currentStep) => {
		if (step === 'integrate') {
			if (!store.integrateList.length) {
				store.integrateList = await api.getIntegrateRecords()
			}
			return
		}
		if (step === 'loss') {
			await ensureDistrict()
			if (!workflowStore.isBaselineConfirmed) {
				await loadSelectedSceneImage({ fly: false })
				return
			}
			if (!store.lossResult) await runLoss({ silent: true })
			else await loadSelectedSceneImage({ fly: false })
			return
		}
		if (step === 'overtop') {
			await ensureDistrict()
			if (!store.overtoppingCity && store.queriedDistrict?.city) {
				store.overtoppingCity = store.queriedDistrict.city
			}
			if (!store.overtoppingRows.length) await loadOvertopping({ silent: true })
			return
		}
		if (step === 'damage') {
			if (!store.selectedSceneId && store.scenes[0]) store.selectedSceneId = store.scenes[0].id
			if (!store.damageRows.length) await runDamage({ silent: true })
			else {
				await loadSelectedSceneImage({ fly: false })
				drawImpact(damageRowsToFeatures(store.damageRows))
			}
			return
		}
		if (step === 'behavior') {
			if (!store.selectedTyphoonId && store.typhoonEvents[0]) {
				store.selectedTyphoonId = store.typhoonEvents[0].id
			}
			if (!store.behaviorResult) await runBehavior({ silent: true })
			else await loadSelectedSceneImage({ fly: false })
			return
		}
		if (step === 'report') {
			// 进入综合评估：自动按当前场景复盘；风暴潮需已确认基线
			if (!store.selectedSceneId && store.scenes[0]) {
				store.selectedSceneId = store.scenes[0].id
			}
			if (store.currentType === 'storm' && !workflowStore.isBaselineConfirmed) {
				await loadSelectedSceneImage({ fly: false })
				return
			}
			store.reportLoading = true
			try {
				await replayScenario(store.selectedSceneId, { silent: true, manageLoading: false })
				if (!store.reportResult) await runReport({ openPreview: false })
			} finally {
				store.reportLoading = false
			}
		}
	}

	/** 按场景复盘：加载调查基线 + 最近评估任务结果并上图 */
	const replayScenario = async (
		sceneId = store.selectedSceneId,
		{ silent = false, manageLoading = true } = {}
	) => {
		if (!sceneId) {
			if (!silent) ElMessage.warning('请先选择遥感场景')
			return null
		}
		store.selectedSceneId = sceneId
		if (manageLoading) store.reportLoading = true
		try {
			await waitForMap()
			await loadSelectedSceneImage({ fly: true })
			const pkg = await baselineStore.loadByScene(sceneId)
			await syncDistrictFromBaseline()
			applySurveyBaselineOnMap({ fly: true })

			const evalHit = await findLatestEvaluateResult({
				typeId: store.currentType,
				sceneId
			})
			if (evalHit?.resultData?.kind === 'loss' && evalHit.resultData.lossResult) {
				store.lossResult = evalHit.resultData.lossResult
				store.evaluateTaskId = evalHit.taskId || ''
			} else if (evalHit?.resultData?.kind === 'report') {
				store.evaluateTaskId = evalHit.taskId || ''
			}
			const reportSnap = evalHit?.reportData?.snapshot || evalHit?.reportData
			if (reportSnap?.title || reportSnap?.grade) {
				store.reportResult = reportSnap
			} else if (pkg?.affectedMeta) {
				await runReport({ openPreview: false, forceRefresh: true })
			}

			if (store.lossResult?.summary && store.queriedDistrict) {
				drawDistrict(store.queriedDistrict.feature)
				drawAffected(pkg?.affectedFeature || makeAffectedFeature(store.queriedDistrict))
			}
			notice(
				'success',
				pkg ? '已按场景复盘调查基线与评估结果' : '未找到调查基线，已尝试回填评估任务',
				silent
			)
			return { baseline: pkg, evaluate: evalHit }
		} finally {
			if (manageLoading) store.reportLoading = false
		}
	}

	const destroyAssessment = () => {
		clearAssessmentLayers()
		clearThemeLayers()
		store.reset()
	}

	return {
		initAssessment,
		fetchDistrictSuggestions,
		fetchAreaTree,
		queryDistrictByName,
		queryDistrictByCoord,
		toggleThemeLayer,
		saveIntegrate,
		loadSelectedSceneImage,
		runLoss,
		runDamage,
		runBehavior,
		loadOvertopping,
		openStormDetail,
		runReport,
		exportRemoteReport,
		archiveReport,
		openArchivedReport,
		replayScenario,
		ensureAssessmentStepResult,
		destroyAssessment,
		workflowStore,
		syncWorkflow,
		requireConfirmedBaseline
	}
}
