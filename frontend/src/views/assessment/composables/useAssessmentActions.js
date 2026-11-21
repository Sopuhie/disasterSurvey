import { ElMessage } from 'element-plus'
import { assessmentApi } from '@/api/assessment/index.js'
import { surveyApi } from '@/api/survey/index.js'
import { findLatestEvaluateResult } from '@/api/taskResultHydrate.js'
import { useAssessmentStore } from '@/store/modules/assessment.js'
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

	const currentScene = () => store.scenes.find(item => item.id === store.selectedSceneId) || store.scenes[0]

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
		if (!store.integrateList.length) {
			store.integrateList = await api.getIntegrateRecords()
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
			store.archivedReports = await api.listArchivedReports()
		} catch (e) {
			store.archivedReports = []
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
		const saved = await api.saveIntegrateRecord(form)
		store.integrateList = [saved, ...store.integrateList]
		store.integrateForm = emptyIntegrateForm()
		store.showIntegrateForm = false
		ElMessage.success('已标准化入库')
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
		const refresh = forceRefresh ?? !silent
		store.lossLoading = true
		try {
			await loadSelectedSceneImage({ fly: !silent })
			const district = store.queriedDistrict
			const scene = currentScene()
			store.lossResult = null

			const hydrated = await api.hydrateLossResult({
				typeId: store.currentType,
				sceneId: store.selectedSceneId || scene?.id || '',
				forceRefresh: refresh
			})
			if (hydrated?.lossResult) {
				store.lossResult = hydrated.lossResult
				store.evaluateTaskId = hydrated.taskId || store.evaluateTaskId
				drawDistrict(district.feature)
				const baseFeat = baselineStore.package?.affectedFeature
				drawAffected(baseFeat || makeAffectedFeature(district))
				applySurveyBaselineOnMap()
				Object.keys(store.themeLayers || {}).forEach(id => {
					if (store.themeLayers[id]) setThemeLayer(id, true)
				})
				notice('success', '经济损失预估完成（已回填服务端结果）', silent)
				return
			}

			const ratio = scene?.inundationRatio || 0.46
			const baselineArea = Number(baselineStore.package?.affectedMeta?.areaKm2) || 0
			const fromSurvey = baselineArea > 0

			if (!fromSurvey) {
				const computed = await api.computeLossRemote({
					typeId: store.currentType,
					sceneId: store.selectedSceneId || scene?.id || '',
					sceneName: scene?.name || '',
					sceneDate: scene?.date || '',
					districtName: district.name || '',
					city: district.city || '',
					statArea: district.statArea,
					density: district.density,
					inundationRatio: ratio
				})
				if (computed?.lossResult) {
					store.lossResult = computed.lossResult
					if (computed.taskId) store.evaluateTaskId = computed.taskId
				}
			}

			if (!store.lossResult || fromSurvey) {
				const affectedKm2 = fromSurvey
					? baselineArea
					: Number(((district.statArea || 680) * 0.078 * (0.72 + ratio)).toFixed(2))
				const density = Number(baselineStore.package?.district?.density) || district.density || 500
				const affectedPop = Math.round(density * affectedKm2)
				const economy = await api.getCountyEconomy(district.name)
				const loss = calcEconomicLoss(economy, affectedKm2)
				const counties =
					baselineStore.package?.affectedMeta?.counties?.length > 0
						? baselineStore.package.affectedMeta.counties
						: [
								{
									name: district.name,
									city: district.city,
									areaKm2: affectedKm2,
									affectedPopulation: affectedPop
								}
							]
				store.lossResult = {
					sceneName: scene?.name || '灾后遥感影像',
					sceneDate: scene?.date || '',
					sensor: scene?.sensor || '孔径雷达卫星',
					economy,
					summary: {
						areaKm2: affectedKm2,
						countyCount: counties.length,
						counties
					},
					landHint: fromSurvey
						? '受灾面积取自灾情调查基线，叠加县域 GDP 与土地利用结构测算分项损失。'
						: '叠加县域 GDP 与土地利用结构（耕地、建设用地、养殖水面、林地、水域）测算分项损失；地图可叠加土地利用/人口/经济/详细规划专题。',
					fromSurveyBaseline: fromSurvey,
					...loss
				}
				try {
					const taskId = await api.persistLossLiteTask({
						typeId: store.currentType,
						lossResult: store.lossResult,
						sceneId: store.selectedSceneId || scene?.id || '',
						districtName: district.name || ''
					})
					if (taskId) store.evaluateTaskId = taskId
				} catch (e) {
					/* 旁路落库失败不影响展示 */
				}
			}
			drawDistrict(district.feature)
			const baseFeat = baselineStore.package?.affectedFeature
			drawAffected(baseFeat || makeAffectedFeature(district))
			applySurveyBaselineOnMap()
			Object.keys(store.themeLayers || {}).forEach(id => {
				if (store.themeLayers[id]) setThemeLayer(id, true)
			})
			notice(
				'success',
				fromSurvey ? '经济损失预估完成（已引用调查基线面积）' : '经济损失预估完成',
				silent
			)
		} catch (err) {
			notice('error', '经济损失评估失败', silent)
		} finally {
			store.lossLoading = false
		}
	}

	const runDamage = async ({ silent = false } = {}) => {
		store.damageLoading = true
		try {
			await loadSelectedSceneImage({ fly: !silent })
			const kind = store.currentType === 'wave' ? 'wave' : 'wind'
			const scene = currentScene()
			if (store.selectedSceneId) {
				await baselineStore.loadByScene(store.selectedSceneId)
			}
			const raw = kind === 'wave' ? await api.getWaveDamage() : await api.getWindDamage()
			const scaled = scaleDamageByScene(raw, scene, kind)
			store.damageRows = decorateDamageRows(scaled, kind)
			drawImpact(damageRowsToFeatures(store.damageRows))
			applySurveyBaselineOnMap()
			const seawallHit = baselineStore.package?.overlayResults?.seawall?.totals?.count
			notice(
				'success',
				seawallHit != null
					? `破坏性分析完成（已叠加调查海堤叠置 ${seawallHit} 处）`
					: `破坏性分析完成（${scene?.name || '当前场景'}）`,
				silent
			)
		} catch (err) {
			notice('error', '破坏性分析失败', silent)
		} finally {
			store.damageLoading = false
		}
	}

	const runBehavior = async ({ silent = false } = {}) => {
		store.behaviorLoading = true
		try {
			await loadSelectedSceneImage({ fly: !silent })
			if (store.currentType === 'wind') {
				const event = store.typhoonEvents.find(item => item.id === store.selectedTyphoonId) || store.typhoonEvents[0]
				const scene = currentScene()
				const result = buildWindBehavior(event)
				const windScale = Number(event?.maxWind || scene?.maxWind || 30) / 36
				const structures = await api.getStructureDamage()
				const inundationRows = scaleMetricRows(await api.getWindInundation(), windScale)
				const inundationKm2 = Number(inundationRows.reduce((sum, item) => sum + item.areaKm2, 0).toFixed(2))
				const affectedPopulation = Math.round(
					inundationRows.reduce((sum, item) => sum + (Number(item.population) || 0), 0)
				)
				store.behaviorResult = {
					kind: 'wind',
					...result,
					scene,
					structures: structures.map(item => ({
						...item,
						level: Math.min(4, Math.max(1, Number(item.level) || 3 - Math.round((windScale - 1) * 2)))
					})),
					inundationRows,
					inundationKm2,
					affectedPopulation,
					desc: `依据台风「${event?.name || '-'}」路径及节点强度（最大风速 ${result.maxWind} m/s，主导风向 ${result.avgDir}），结合 ${scene?.name || 'SAR 风场'} 估测沿海淹没约 ${inundationKm2} km²，受灾人口约 ${affectedPopulation} 人。`
				}
				drawPath([result.pathFeature, ...result.pointFeatures])
				drawAffected(inundationRowsToFeatures(inundationRows))
			} else {
				const scene = currentScene()
				const structures = await api.getStructureDamage()
				const waveScale = Number(scene?.maxHeight || 4) / 4
				const erosion = scaleMetricRows(await api.getWaveErosion(), waveScale)
				const inundationKm2 = Number(erosion.reduce((sum, item) => sum + item.areaKm2, 0).toFixed(2))
				const lengthKm = Number(erosion.reduce((sum, item) => sum + item.lengthKm, 0).toFixed(1))
				const avgSpeed = scene?.maxSpeed ? Number((scene.maxSpeed * 0.82).toFixed(1)) : 11.1
				store.behaviorResult = {
					kind: 'wave',
					scene,
					structures,
					erosion,
					inundation: makeAffectedFeature(store.queriedDistrict),
					inundationKm2,
					lengthKm,
					maxHeight: scene?.maxHeight || 4.8,
					period: scene?.period || 9.2,
					avgSpeed,
					direction: 'ESE',
					energyIndex: Math.round(((scene?.maxHeight || 4.8) ** 2) * 8),
					desc: `依据 ${scene?.name || 'SAR 波浪场'}：波高 ${scene?.maxHeight || '-'} m、波速 ${scene?.maxSpeed || avgSpeed} m/s、波周期 ${scene?.period || '-'} s，估测海岸线侵蚀 ${lengthKm} km、范围约 ${inundationKm2} km²，并对海上结构造成冲击。`
				}
				drawAffected(store.behaviorResult.inundation)
			}
			notice('success', '行为分析完成', silent)
		} catch (err) {
			notice('error', '行为分析失败', silent)
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
		const refresh = forceRefresh ?? openPreview
		if (openPreview) store.reportGenerating = true
		try {
			const hydrated = await api.hydrateReportResult({
				typeId: store.currentType,
				sceneId: store.selectedSceneId || scene?.id || '',
				forceRefresh: refresh
			})
			if (hydrated?.report) {
				store.reportResult = hydrated.report
				store.evaluateTaskId = hydrated.taskId || store.evaluateTaskId
				if (openPreview) store.reportVisible = true
				return
			}

			if (store.selectedSceneId) {
				await baselineStore.loadByScene(store.selectedSceneId)
			}
			let surveySummary = null
			try {
				surveySummary = await api.getSurveyHazardSummary()
			} catch (e) {
				surveySummary = null
			}
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
				surveySummary,
				surveyBaseline: baselineStore.package
			})
			applySurveyBaselineOnMap()
			try {
				store.evaluateTaskId = await api.persistEvaluateLiteTask({
					typeId: store.currentType,
					report: store.reportResult,
					factors: store.reportFactors,
					sceneId: store.selectedSceneId || scene?.id || '',
					districtName: store.queriedDistrict?.name || ''
				})
			} catch (e) {
				store.evaluateTaskId = ''
			}
			if (openPreview) store.reportVisible = true
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
		if (!store.reportResult) {
			await runReport({ openPreview: false })
		}
		if (!store.reportResult) {
			ElMessage.warning('请先生成评估报告')
			return null
		}
		const saved = await api.archiveAssessmentReport(store.reportResult, {
			typeId: store.currentType,
			remark,
			archivedBy
		})
		store.archivedReports = await api.listArchivedReports()
		ElMessage.success(`报告已归档：${saved.reportNo}`)
		return saved
	}

	const openArchivedReport = async id => {
		const row = await api.getArchivedReport(id)
		if (!row?.snapshot) {
			ElMessage.warning('未找到归档报告')
			return
		}
		store.reportResult = row.snapshot
		store.reportVisible = true
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
			// 进入综合评估：自动按当前场景复盘，右侧结果面板 loading 等待
			if (!store.selectedSceneId && store.scenes[0]) {
				store.selectedSceneId = store.scenes[0].id
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
		destroyAssessment
	}
}
