/**
 * 本地遥感影像目录（public/datas/images）加载与地图叠加。
 * 切换场景时先移除旧图层，再按 bbox 以 ImageStatic 叠加新影像。
 */
import ImageLayer from 'ol/layer/Image'
import Static from 'ol/source/ImageStatic'
import { transformExtent } from 'ol/proj'
import * as turf from '@turf/turf'
import { useMapStore } from '@/store/modules/map.js'

export const LOCAL_RS_LAYER_NAME = 'localRsImageLyr'
export const LOCAL_RS_MANIFEST_URL = '/datas/images/manifest.json'

/** 场景 id → manifest 影像 id（评估侧部分场景共用同一幅影像） */
export const SCENE_IMAGE_ID_MAP = {
	'typhoon-2024': 'typhoon-2024',
	'storm-2023': 'storm-2023',
	'surge-2025': 'surge-2025',
	'sar-wind-2024': 'sar-wind-2024',
	'sar-wind-2023': 'sar-wind-2024',
	'sar-wave-2024': 'sar-wave-2024',
	'sar-wave-2023': 'sar-wave-2024',
	'sar-storm-2024': 'typhoon-2024',
	'sar-storm-2023': 'storm-2023'
}

let manifestCache = null
let manifestPromise = null

const getMap = () => useMapStore().activeMap

export async function loadLocalRsManifest(force = false) {
	if (!force && manifestCache) return manifestCache
	if (!force && manifestPromise) return manifestPromise
	manifestPromise = fetch(LOCAL_RS_MANIFEST_URL)
		.then(res => {
			if (!res.ok) throw new Error(`manifest ${res.status}`)
			return res.json()
		})
		.then(data => {
			const basePath = (data?.basePath || '/datas/images/').replace(/\/?$/, '/')
			manifestCache = {
				basePath,
				items: (data?.items || []).map(item => ({
					...item,
					url: `${basePath}${item.file}`,
					/** 同目录 jpg 预览（体积更小，优先用于交互叠加） */
					previewUrl: item.file?.endsWith('.png')
						? `${basePath}${item.file.replace(/\.png$/i, '.jpg')}`
						: `${basePath}${item.file}`
				}))
			}
			return manifestCache
		})
		.catch(err => {
			manifestPromise = null
			console.warn('[localRsImages] 加载 manifest 失败', err)
			manifestCache = { basePath: '/datas/images/', items: [] }
			return manifestCache
		})
	return manifestPromise
}

export function getLocalRsItemById(imageId) {
	if (!imageId || !manifestCache?.items?.length) return null
	return manifestCache.items.find(item => item.id === imageId) || null
}

export function resolveSceneImageId(scene) {
	if (!scene) return ''
	if (scene.imageId) return scene.imageId
	if (SCENE_IMAGE_ID_MAP[scene.id]) return SCENE_IMAGE_ID_MAP[scene.id]
	if (getLocalRsItemById(scene.id)) return scene.id
	return ''
}

/** 将本地影像元数据合并进场景列表（下拉展示用名称/区域） */
export async function enrichScenesWithLocalImages(scenes = []) {
	await loadLocalRsManifest()
	return scenes.map(scene => {
		const imageId = resolveSceneImageId(scene)
		const item = getLocalRsItemById(imageId)
		if (!item) return { ...scene, imageId: imageId || '' }
		return {
			...scene,
			imageId,
			imageName: item.name,
			imageUrl: item.previewUrl || item.url,
			imageHdUrl: item.url,
			bbox: item.bbox,
			regionHint: scene.regionHint || item.region,
			/** 下拉优先展示本地影像名称，保留原场景业务名作副标题 */
			displayName: item.name || scene.name
		}
	})
}

export function removeLocalRsImageLayer() {
	const map = getMap()
	if (!map?.layerManager) return
	if (map.layerManager.getLayer(LOCAL_RS_LAYER_NAME)) {
		map.layerManager.removeLayer(LOCAL_RS_LAYER_NAME)
	}
}

/**
 * 按场景叠加本地影像；切换时先移除旧层。
 * @param {object|string} sceneOrId 场景对象或 imageId
 * @param {{ fly?: boolean, preferHd?: boolean }} options
 */
export async function showLocalRsImage(sceneOrId, { fly = true, preferHd = false } = {}) {
	await loadLocalRsManifest()
	const map = getMap()
	if (!map?.layerManager) return false

	let imageId = ''
	let scene = null
	if (typeof sceneOrId === 'string') {
		imageId = sceneOrId
	} else if (sceneOrId) {
		scene = sceneOrId
		imageId = resolveSceneImageId(scene)
	}
	const item = getLocalRsItemById(imageId)
	if (!item?.bbox?.length) {
		removeLocalRsImageLayer()
		return false
	}

	const url = preferHd ? item.url : item.previewUrl || item.url
	const projection = map.olView?.getProjection?.() || map.projection
	const projCode = projection?.getCode?.() || 'EPSG:4326'
	const imageExtent =
		projCode === 'EPSG:4326' ? item.bbox.slice() : transformExtent(item.bbox, 'EPSG:4326', projection)

	removeLocalRsImageLayer()

	const olLyr = new ImageLayer({
		opacity: 0.92,
		zIndex: 28,
		source: new Static({
			url,
			projection,
			imageExtent,
			interpolate: true
		})
	})
	olLyr.set('lyrName', LOCAL_RS_LAYER_NAME)
	map.layerManager.addLayer(LOCAL_RS_LAYER_NAME, { ol: olLyr })

	if (fly && map.flyToManager) {
		try {
			map.flyToManager.flyToRegion(turf.bboxPolygon(item.bbox))
		} catch (e) {
			map.olView?.fit?.(imageExtent, { padding: [80, 420, 80, 420], duration: 800, maxZoom: 12 })
		}
	}
	return true
}

export async function showLocalRsImageBySceneId(scenes, sceneId, options) {
	const scene = (scenes || []).find(item => item.id === sceneId) || null
	if (!scene) {
		removeLocalRsImageLayer()
		return false
	}
	return showLocalRsImage(scene, options)
}
