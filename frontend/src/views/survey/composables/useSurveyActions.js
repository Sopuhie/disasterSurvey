import { ElMessage } from 'element-plus'
import { surveyApi } from '@/api/survey/index.js'
import { makeSystemHazardFeature } from '@/api/surveyBase/hazardFeatures.js'
import { useSurveyStore } from '@/store/modules/survey.js'
import { useSurveyBaselineStore } from '@/store/modules/surveyBaseline.js'
import { getOverlayTarget } from '@/views/survey/constants.js'
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
	const mapStore = useMapStore()

	const publishBaseline = async () => {
		try {
			await baselineStore.publishFromSurvey(surveyStore.$state)
		} catch (e) {
			/* 不影响主流程 */
		}
	}

	const loadSelectedSceneImage = async ({ fly = true } = {}) => {
		const scene = surveyStore.scenes.find(item => item.id === surveyStore.selectedSceneId) || surveyStore.scenes[0]
		await waitForMap()
		return drawSceneImage(scene, { fly })
	}

	const initSurvey = async () => {
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
			surveyStore.identifyProgress = 100
			await publishBaseline()
			notice('success', '受灾范围识别完成', silent)
		} catch (err) {
			notice('error', '受灾范围识别失败', silent)
		} finally {
			if (timer) clearInterval(timer)
			surveyStore.identifying = false
		}
	}

	const runOverlay = async (targetId = surveyStore.overlayTarget, { silent = false } = {}) => {
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
			await publishBaseline()
			notice('success', `${target?.label || ''}叠置分析完成，共 ${result.totals.count} 条`, silent)
			return result
		} catch (err) {
			notice('error', '叠置分析失败', silent)
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
		surveyStore.compareRows = buildCompareSeries(overlayResult, reports, targetName)
		await publishBaseline()
		if (!silent && overlayResult) ElMessage.success('对比结果已生成')
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
		ensureSurveyStepResult,
		exportOverlayImage,
		destroySurvey
	}
}
