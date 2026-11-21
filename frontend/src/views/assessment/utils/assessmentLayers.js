import * as turf from '@turf/turf'
import buildLayer, { LayerType } from '@/components/map/primitive/layer/Layer'
import { useMapStore } from '@/store/modules/map.js'
import { OtherResources } from '/public/map.js'
import { removeLocalRsImageLayer, showLocalRsImage } from '@/utils/localRsImages.js'
import {
	AFFECTED_STYLE,
	DISTRICT_STYLE,
	IMPACT_STYLE,
	LAYER_NAMES,
	OVERTOP_STYLE,
	PATH_STYLE,
	SURVEY_AFFECTED_STYLE,
	SURVEY_OVERLAY_STYLE,
	THEME_LAYER_OPTIONS
} from '@/views/assessment/constants.js'

const ALL_LAYERS = [
	LAYER_NAMES.district,
	LAYER_NAMES.affected,
	LAYER_NAMES.impact,
	LAYER_NAMES.path,
	LAYER_NAMES.overtop,
	LAYER_NAMES.surveyAffected,
	LAYER_NAMES.surveyOverlay
]
const themeLayerName = id => `${LAYER_NAMES.themePrefix}${id}`

const getMap = () => useMapStore().activeMap

export function removeLayer(name) {
	const map = getMap()
	if (map?.layerManager.getLayer(name)) {
		map.layerManager.removeLayer(name)
	}
}

export function addOrReplaceLayer(name, features, style, zIndex = 60) {
	const map = getMap()
	if (!map || !features?.length) {
		removeLayer(name)
		return
	}
	removeLayer(name)
	const lyr = buildLayer(LayerType.Vector, {
		name,
		geojson: JSON.stringify({ type: 'FeatureCollection', features }),
		lyrStyle: style
	})
	lyr.ol.set('lyrName', name)
	lyr.setZIndex(zIndex)
	map.layerManager.addLayer(name, lyr)
}

export function flyToFeatures(features) {
	const map = getMap()
	if (!map || !features?.length) return
	if (features.length === 1 && features[0].geometry?.type === 'Point') {
		map.flyToManager.flyTo(features[0].geometry.coordinates, 12)
		return
	}
	if (features.length === 1) {
		map.flyToManager.flyToRegion(features[0])
		return
	}
	try {
		map.flyToManager.flyToRegion(turf.bboxPolygon(turf.bbox({ type: 'FeatureCollection', features })))
	} catch (err) {
		map.flyToManager.flyToRegion(features[0])
	}
}

export function drawDistrict(feature) {
	if (!feature) return removeLayer(LAYER_NAMES.district)
	addOrReplaceLayer(LAYER_NAMES.district, [feature], DISTRICT_STYLE, 40)
	flyToFeatures([feature])
}

export function drawAffected(feature) {
	const features = Array.isArray(feature) ? feature : feature ? [feature] : []
	if (!features.length) return removeLayer(LAYER_NAMES.affected)
	addOrReplaceLayer(LAYER_NAMES.affected, features, AFFECTED_STYLE, 50)
	flyToFeatures(features)
}

export function drawImpact(features) {
	if (!features?.length) return removeLayer(LAYER_NAMES.impact)
	addOrReplaceLayer(LAYER_NAMES.impact, features, IMPACT_STYLE, 70)
	flyToFeatures(features)
}

export function drawPath(features) {
	if (!features?.length) return removeLayer(LAYER_NAMES.path)
	addOrReplaceLayer(LAYER_NAMES.path, features, PATH_STYLE, 80)
	flyToFeatures(features)
}

export function drawOvertop(features) {
	if (!features?.length) return removeLayer(LAYER_NAMES.overtop)
	addOrReplaceLayer(LAYER_NAMES.overtop, features, OVERTOP_STYLE, 75)
	flyToFeatures(features)
}

/** 评估地图同屏叠加调查基线（受灾面 + 叠置命中） */
export function drawSurveyBaseline(pkg, { fly = false } = {}) {
	if (!pkg) {
		removeLayer(LAYER_NAMES.surveyAffected)
		removeLayer(LAYER_NAMES.surveyOverlay)
		return
	}
	const affected = pkg.affectedFeature
	if (affected?.geometry) {
		addOrReplaceLayer(LAYER_NAMES.surveyAffected, [affected], SURVEY_AFFECTED_STYLE, 48)
	} else {
		removeLayer(LAYER_NAMES.surveyAffected)
	}
	const overlayFeatures = []
	Object.values(pkg.overlayResults || {}).forEach(pack => {
		;(pack?.features || []).forEach(f => {
			if (f?.geometry) overlayFeatures.push(f)
		})
	})
	if (overlayFeatures.length) {
		addOrReplaceLayer(LAYER_NAMES.surveyOverlay, overlayFeatures.slice(0, 80), SURVEY_OVERLAY_STYLE, 55)
	} else {
		removeLayer(LAYER_NAMES.surveyOverlay)
	}
	if (fly) {
		const flyFeatures = [...(affected?.geometry ? [affected] : []), ...overlayFeatures]
		if (flyFeatures.length) flyToFeatures(flyFeatures)
	}
}

export function setThemeLayer(themeId, enabled) {
	const option = THEME_LAYER_OPTIONS.find(item => item.id === themeId)
	const name = themeLayerName(themeId)
	if (!option) return
	if (!enabled) {
		removeLayer(name)
		return
	}
	const resource = OtherResources.find(item => item.name === option.resourceName)
	const map = getMap()
	if (!map || !resource?.url) return
	removeLayer(name)
	const lyr = buildLayer(LayerType.ArcGISRest, {
		name,
		url: resource.url,
		layers: resource.lyrIndexs || [0],
		zIndex: 35
	})
	lyr.ol.set('lyrName', name)
	lyr.setZIndex(35)
	map.layerManager.addLayer(name, lyr)
}

export function clearThemeLayers() {
	THEME_LAYER_OPTIONS.forEach(item => removeLayer(themeLayerName(item.id)))
}

export async function drawSceneImage(scene, { fly = true } = {}) {
	if (!scene) {
		removeLocalRsImageLayer()
		return false
	}
	return showLocalRsImage(scene, { fly })
}

export function clearAssessmentLayers() {
	ALL_LAYERS.forEach(removeLayer)
	clearThemeLayers()
	removeLocalRsImageLayer()
}
