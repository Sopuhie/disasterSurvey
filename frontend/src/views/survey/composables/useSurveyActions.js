import { ElMessage } from 'element-plus'
import { surveyApi } from '@/api/survey/index.js'
import {
	compareQuickReport,
	confirmSurveyBaseline,
	exportExposureImages,
	runOverlayAnalysis,
	saveAffectedArea
} from '@/api/survey/pipeline.js'
import { makeSystemHazardFeature } from '@/api/surveyBase/hazardFeatures.js'
import { BaselineStatusEnum, isMockEnabled } from '@/constants/disasterEnums.js'
import { useDisasterWorkflowStore } from '@/store/modules/disasterWorkflow.js'
import { useSurveyStore } from '@/store/modules/survey.js'
import { useSurveyBaselineStore } from '@/store/modules/surveyBaseline.js'
import { getHazardExposureType, getOverlayTarget } from '@/views/survey/constants.js'
import { buildCompareSeries } from '@/views/survey/utils/overlayEngine.js'
import { clearSurveyLayers, drawAffectedArea, drawDistrict, drawHazard, drawOverlay, drawSceneImage } from '@/views/survey/utils/surveyLayers.js'
import { ExportPicture, PictureType } from '@/components/map/primitive/control/ExportPicture.js'
import { useMapStore } from '@/store/modules/map.js'
import { enrichScenesWithLocalImages } from '@/utils/localRsImages.js'
import * as turf from '@turf/turf'
import { transformExtent } from 'ol/proj'

const api = surveyApi()
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

export function useSurveyActions() {
	const surveyStore = useSurveyStore()
	const baselineStore = useSurveyBaselineStore()
	const workflowStore = useDisasterWorkflowStore()
	const mapStore = useMapStore()

	const publishBaseline = async () => {
		try {
			await baselineStore.publishFromSurvey(surveyStore.$state)
		} catch (e) {
			/* 不影响主流程 */
		}
	}

	const requireEventId = () => {
		if (!workflowStore.currentEventId) {
			throw new Error('请先绑定或创建灾害事件')
		}
		return workflowStore.currentEventId
	}

	const syncPipelineBaseline = remote => {
		if (remote && (remote.baselineId || remote.id || remote.status)) {
			workflowStore.applyBaselineFromRemote(remote)
		}
	}

	const loadSelectedSceneImage = async ({ fly = true } = {}) => {
		const scene = surveyStore.scenes.find(item => item.id === surveyStore.selectedSceneId) || surveyStore.scenes[0]
		await waitForMap()
		return drawSceneImage(scene, { fly })
	}

	const initSurvey = async () => {
		try {
			await workflowStore.ensureEvent({
				regionName: surveyStore.districtKeyword || DEFAULT_DISTRICT,
				districtName: surveyStore.districtKeyword || DEFAULT_DISTRICT
			})
		} catch (e) {
			if (!isMockEnabled()) {
				ElMessage.error(e?.message || '创建/绑定灾害事件失败，请检查后端服务')
				return
			}
		}
		const scenes = await api.getRemoteSensingScenes()
		surveyStore.scenes = await enrichScenesWithLocalImages(scenes)
		if (!surveyStore.selectedSceneId) {
			surveyStore.selectedSceneId = surveyStore.scenes[0]?.id || ''
		}
		if (!surveyStore.districtKeyword) surveyStore.districtKeyword = DEFAULT_DISTRICT
		// 初始进入强制按列表首条重新选中并定位
		surveyStore.selectedHazard = null
		await refreshHazardList()
		await waitForMap()
		await ensureSurveyStepResult(surveyStore.currentStep, { selectFirstHazard: true })
	}

	const fetchDistrictSuggestions = queryString => {
		return api.getDistrictSuggestions(queryString)
	}

	const fetchAreaTree = () => api.getAreaTree()

	const fetchTownSuggestions = (queryString, district) => {
		return api.getTownSuggestions(queryString, {
			district: district || surveyStore.districtKeyword || surveyStore.queriedDistrict?.name || ''
		})
	}

	const applyDistrict = (item, { fly = true } = {}) => {
		surveyStore.queriedDistrict = item
		if (item?.feature) drawDistrict(item.feature, { fly })
	}

	const applyTown = town => {
		surveyStore.queriedTown = town
		const countyObj = town?.county && typeof town.county === 'object' ? town.county : null
		if (countyObj) applyDistrict(countyObj)
		if (town?.feature) drawDistrict(town.feature)
		const countyName = countyObj?.name || (typeof town?.county === 'string' ? town.county : '')
		ElMessage.success(`已定位到${town.name}${countyName ? `（${countyName}）` : ''}`)
	}

	const refreshHazardList = async () => {
		surveyStore.loadingHazards = true
		try {
			const district = (surveyStore.districtKeyword || surveyStore.queriedDistrict?.name || '').trim()
			const town =
				surveyStore.districtQueryMode === 'town'
					? (surveyStore.townKeyword || surveyStore.queriedTown?.name || '').trim()
					: ''
			surveyStore.hazardList = await api.getHazards({
				typeId: surveyStore.selectedHazardType,
				keyword: surveyStore.hazardKeyword,
				district,
				town
			})
		} catch (err) {
			surveyStore.hazardList = []
			ElMessage.error('承灾体数据加载失败')
		} finally {
			surveyStore.loadingHazards = false
		}
	}

	const queryDistrictByName = async () => {
		const name = surveyStore.districtKeyword.trim()
		if (!name) {
			ElMessage.warning('请选择行政区')
			return
		}
		const hit = await api.queryDistrict({ name })
		if (!hit) {
			ElMessage.warning('未查询到对应行政区划')
			return
		}
		applyDistrict(hit)
		surveyStore.queriedTown = null
		surveyStore.townKeyword = ''
		surveyStore.selectedHazard = null
		await refreshHazardList()
		ElMessage.success(`已定位到${hit.name}，承灾体 ${surveyStore.hazardList.length} 条`)
	}

	const queryTownByName = async () => {
		const name = (surveyStore.townKeyword || '').trim()
		if (!name) {
			ElMessage.warning('请选择乡镇')
			return
		}
		const district = (surveyStore.districtKeyword || surveyStore.queriedDistrict?.name || '').trim()
		if (!district) {
			ElMessage.warning('请先选择县市')
			return
		}
		const options = await api.getTownSuggestions('', { district })
		if (!options.some(item => item.value === name)) {
			ElMessage.warning(`「${name}」不属于当前所选县市`)
			return
		}
		const hit = await api.queryTown({ name })
		if (!hit) {
			ElMessage.warning('未查询到对应乡镇')
			return
		}
		surveyStore.townKeyword = hit.name
		applyTown(hit)
		surveyStore.selectedHazard = null
		await refreshHazardList()
		ElMessage.success(`已定位到${hit.name}，承灾体 ${surveyStore.hazardList.length} 条`)
	}

	const locateHazard = async item => {
		if (!item) return
		const typeId = surveyStore.selectedHazardType
		// 先选中，保证列表高亮即时反馈
		surveyStore.selectedHazard = item
		surveyStore.overlayTarget = typeId
		surveyStore.compareType = typeId
		surveyStore.overlayResults = {}
		surveyStore.overlayResult = null
		surveyStore.compareRows = []
		surveyStore.compareMetricRows = []
		surveyStore.compareChart = null

		let feature = item.feature
		if (!feature?.geometry) {
			try {
				feature = await api.resolveHazardFeature({ typeId, item })
			} catch (e) {
				feature = null
			}
		}
		if (!feature?.geometry) {
			feature = makeSystemHazardFeature(item, typeId)
		}
		if (!feature?.geometry) return

		const next = { ...item, feature }
		// 异步解析期间若用户已点选其它对象，则不再覆盖
		if (surveyStore.selectedHazard?.name !== item.name) return
		surveyStore.selectedHazard = next
		drawHazard(typeId, [feature], { fly: true })
	}

	const queryHazardByCoord = async () => {
		const lon = Number(surveyStore.hazardCoordLon)
		const lat = Number(surveyStore.hazardCoordLat)
		if (!Number.isFinite(lon) || !Number.isFinite(lat)) {
			ElMessage.warning('请输入承灾体经纬度')
			return
		}
		const hit = await api.queryHazardByCoord({
			typeId: surveyStore.selectedHazardType,
			lon,
			lat
		})
		if (!hit) {
			ElMessage.warning('附近未查询到对应承灾体')
			return
		}
		surveyStore.hazardKeyword = hit.name
		await refreshHazardList()
		locateHazard(hit)
		ElMessage.success(`已定位到${hit.name}`)
	}

	const identifyAffectedArea = async ({ silent = false } = {}) => {
		if (surveyStore.identifying) return
		if (workflowStore.isBaselineConfirmed) {
			notice('warning', '调查基线已锁定，不可重新识别', silent)
			return
		}
		surveyStore.identifying = true
		surveyStore.identifyProgress = silent ? 60 : 8
		const timer = silent
			? null
			: setInterval(() => {
					surveyStore.identifyProgress = Math.min(surveyStore.identifyProgress + 12, 90)
			  }, 160)
		try {
			await loadSelectedSceneImage({ fly: !silent })
			const { feature, meta, taskId } = await api.getAffectedAreaMeta({
				sceneId: surveyStore.selectedSceneId,
				district: surveyStore.queriedDistrict,
				forceRefresh: !silent
			})
			surveyStore.affectedFeature = feature
			surveyStore.affectedMeta = meta
			surveyStore.surveyTaskId = taskId || ''
			surveyStore.overlayResults = {}
			surveyStore.overlayResult = null
			surveyStore.overlayTaskId = ''
			if (feature) drawAffectedArea(feature)

			// 对接 /api/v1/survey 主线：保存受灾范围到事件基线
			const eventId = requireEventId()
			const remote = await saveAffectedArea(eventId, {
				affectedAreaGeojson: feature ? JSON.stringify(feature) : '',
				geojson: feature ? JSON.stringify(feature) : '',
				rsImageId: surveyStore.selectedSceneId,
				sceneId: surveyStore.selectedSceneId,
				sceneName: meta?.sceneName,
				sceneDate: meta?.sceneDate,
				districtName: surveyStore.queriedDistrict?.name || '',
				city: surveyStore.queriedDistrict?.city || '',
				inundationRatio: meta?.inundationRatio
			})
			syncPipelineBaseline(remote)

			surveyStore.identifyProgress = 100
			await publishBaseline()
			notice('success', '受灾范围识别完成', silent)
		} catch (err) {
			notice('error', err?.message || '受灾范围识别失败', silent)
		} finally {
			if (timer) clearInterval(timer)
			surveyStore.identifying = false
		}
	}

	const runOverlay = async (targetId = surveyStore.overlayTarget, { silent = false } = {}) => {
		if (workflowStore.isBaselineConfirmed) {
			notice('warning', '调查基线已锁定，不可重新叠置', silent)
			return surveyStore.overlayResult
		}
		if (!surveyStore.affectedFeature) {
			if (silent) {
				await identifyAffectedArea({ silent: true })
			} else {
				ElMessage.warning('请先完成受灾范围识别')
				surveyStore.setStep('identify')
				return null
			}
		}
		if (!surveyStore.affectedFeature) return null
		surveyStore.overlayTarget = targetId
		surveyStore.overlaying = true
		try {
			const target = getOverlayTarget(targetId)
			const hazardName =
				surveyStore.selectedHazard &&
				(targetId === surveyStore.selectedHazardType || target?.kind === 'hazard')
					? surveyStore.selectedHazard.name
					: ''
			const result = await api.runOverlay({
				targetId,
				hazardName: target?.kind === 'hazard' ? hazardName : '',
				sceneId: surveyStore.selectedSceneId,
				affectedFeature: surveyStore.affectedFeature,
				forceRefresh: !silent
			})
			surveyStore.overlayResult = result
			surveyStore.overlayTaskId = result?.taskId || ''
			surveyStore.overlayResults = { ...surveyStore.overlayResults, [targetId]: result }
			if (result.features?.length) drawOverlay(result.features)
			if (target?.kind === 'hazard') {
				surveyStore.selectedHazardType = targetId
				surveyStore.compareType = targetId
			}

			// 对接主线：全量叠置 + 影像导出清单
			const eventId = requireEventId()
			const remoteOverlay = await runOverlayAnalysis(eventId, {
				sceneId: surveyStore.selectedSceneId,
				districtName: surveyStore.queriedDistrict?.name || '',
				city: surveyStore.queriedDistrict?.city || '',
				affectedAreaGeojson: JSON.stringify(surveyStore.affectedFeature)
			})
			syncPipelineBaseline(remoteOverlay)
			const exported = await exportExposureImages(eventId, {
				rsImageId: surveyStore.selectedSceneId
			})
			syncPipelineBaseline(exported)
			surveyStore.imageExports = exported?.exports || null

			await publishBaseline()
			notice('success', `${target?.label || ''}叠置分析完成，共 ${result.totals.count} 条`, silent)
			return result
		} catch (err) {
			notice('error', err?.message || '叠置分析失败', silent)
			return null
		} finally {
			surveyStore.overlaying = false
		}
	}

	const loadCompare = async (typeId = surveyStore.compareType, { silent = false } = {}) => {
		surveyStore.compareType = typeId
		let overlayResult = surveyStore.overlayResults[typeId]
		if (!overlayResult) {
			if (!surveyStore.affectedFeature) {
				await identifyAffectedArea({ silent: true })
			}
			overlayResult = await runOverlay(typeId, { silent: true })
		}
		const targetName =
			surveyStore.selectedHazard && surveyStore.selectedHazardType === typeId ? surveyStore.selectedHazard.name : ''
		const reports = await api.getReportedLoss({ hazardType: typeId, targetName })
		const series = buildCompareSeries(overlayResult, reports, targetName)
		surveyStore.compareRows = series

		// 对接主线：速报对比核查；右侧图表保留 series 结构，同时消费后端 chart
		try {
			const eventId = requireEventId()
			const monitor = series[0] || {}
			const reportAgg = series.slice(1).reduce(
				(acc, row) => ({
					population: acc.population + (Number(row.population) || 0),
					area: acc.area + (Number(row.area) || 0),
					loss: acc.loss + (Number(row.loss) || 0)
				}),
				{ population: 0, area: 0, loss: 0 }
			)
			const reportCount = Math.max(series.length - 1, 1)
			const quickItems = [
				{
					metric: '受灾人口',
					unit: '人',
					rsValue: Number(monitor.population) || 0,
					quickValue: reportAgg.population / reportCount
				},
				{
					metric: '受灾面积',
					unit: 'km²',
					rsValue: Number(monitor.area) || 0,
					quickValue: reportAgg.area / reportCount
				},
				{
					metric: '经济损失',
					unit: '万元',
					rsValue: Number(monitor.loss) || 0,
					quickValue: reportAgg.loss / reportCount
				}
			]
			const remote = await compareQuickReport(eventId, {
				quickReport: quickItems,
				items: quickItems
			})
			syncPipelineBaseline(remote)
			const compare = remote?.compare
			if (compare?.chart) {
				surveyStore.compareChart = compare.chart
			}
			if (compare?.rows?.length) {
				surveyStore.compareMetricRows = compare.rows
			}
		} catch (e) {
			if (!isMockEnabled()) throw e
		}

		await publishBaseline()
		if (!silent && overlayResult) ElMessage.success('对比结果已生成')
	}

	const confirmBaseline = async () => {
		if (workflowStore.isBaselineConfirmed) {
			ElMessage.info('调查基线已确认锁定')
			return workflowStore.currentBaseline
		}
		try {
			const eventId = requireEventId()
			const baseline = await confirmSurveyBaseline(eventId, {
				remark: '前端 Step4 确认锁定'
			})
			workflowStore.applyBaselineFromRemote(baseline)
			ElMessage.success('调查基线已确认锁定，可作为灾害评估输入')
			return baseline
		} catch (e) {
			ElMessage.error(e?.message || '基线确认失败')
			throw e
		}
	}

	const ensureSurveyStepResult = async (step = surveyStore.currentStep, { selectFirstHazard = false } = {}) => {
		if (step === 'query') {
			const firstHazard = surveyStore.hazardList[0]
			const willLocateFirst = !!(selectFirstHazard || (!surveyStore.selectedHazard && firstHazard))
			if (!surveyStore.queriedDistrict) {
				const hit = await api.queryDistrict({ name: surveyStore.districtKeyword || DEFAULT_DISTRICT })
				if (hit) {
					surveyStore.districtKeyword = hit.name
					// 即将定位承灾体时，区划仅上图不抢飞行，避免闪到县界
					applyDistrict(hit, { fly: !willLocateFirst })
					surveyStore.queriedTown = null
				}
			} else if (surveyStore.queriedDistrict.feature) {
				drawDistrict(surveyStore.queriedDistrict.feature, { fly: !willLocateFirst })
			}
			if (willLocateFirst && firstHazard) {
				await locateHazard(firstHazard)
			} else if (surveyStore.selectedHazard?.feature) {
				drawHazard(surveyStore.selectedHazardType, [surveyStore.selectedHazard.feature], { fly: true })
			}
			return
		}
		if (step === 'identify') {
			await loadSelectedSceneImage({ fly: !surveyStore.affectedMeta })
			if (!surveyStore.affectedMeta) await identifyAffectedArea({ silent: true })
			else if (surveyStore.affectedFeature) drawAffectedArea(surveyStore.affectedFeature)
			return
		}
		if (step === 'overlay') {
			if (!surveyStore.overlayResult) await runOverlay(surveyStore.overlayTarget, { silent: true })
			else if (surveyStore.overlayResult.features?.length) drawOverlay(surveyStore.overlayResult.features)
			return
		}
		if (!surveyStore.compareRows.length) await loadCompare(surveyStore.compareType, { silent: true })
	}

	const fitExportExtent = map => {
		const feature = surveyStore.selectedHazard?.feature || surveyStore.affectedFeature
		if (!feature?.geometry) return
		const [minX, minY, maxX, maxY] = turf.bbox(feature)
		const view = map.olMap.getView()
		const extent = transformExtent([minX, minY, maxX, maxY], 'EPSG:4326', view.getProjection())
		view.fit(extent, { padding: [80, 480, 80, 420], duration: 0, maxZoom: 14 })
	}

	const exportOverlayImage = () => {
		const map = mapStore.activeMap
		if (!map?.olMap) {
			ElMessage.warning('地图尚未就绪')
			return
		}
		if (!surveyStore.overlayResult && !surveyStore.selectedHazard) {
			ElMessage.warning('请先选择调查对象并完成叠置分析')
			return
		}
		fitExportExtent(map)
		const objectName = surveyStore.selectedHazard?.name
		const label = getOverlayTarget(surveyStore.overlayTarget)?.label || '叠置'
		const exporter = new ExportPicture({
			name: objectName ? `灾后${objectName}影像` : `灾后${label}叠置影像`,
			type: PictureType.PNG
		})
		exporter.exportPic(map)
		ElMessage.success(objectName ? `已按${objectName}范围导出影像` : `已导出${label}遥感影像图片`)
	}

	const destroySurvey = () => {
		clearSurveyLayers()
		surveyStore.reset()
	}

	return {
		initSurvey,
		loadSelectedSceneImage,
		fetchDistrictSuggestions,
		fetchAreaTree,
		fetchTownSuggestions,
		queryDistrictByName,
		queryTownByName,
		refreshHazardList,
		locateHazard,
		queryHazardByCoord,
		identifyAffectedArea,
		runOverlay,
		loadCompare,
		confirmBaseline,
		ensureSurveyStepResult,
		exportOverlayImage,
		destroySurvey,
		workflowStore,
		BaselineStatusEnum,
		getHazardExposureType
	}
}
