import * as turf from '@turf/turf'
import buildLayer, { LayerType } from '@/components/map/primitive/layer/Layer'
import { useMapStore } from '@/store/modules/map.js'
import { removeLocalRsImageLayer, showLocalRsImage } from '@/utils/localRsImages.js'
import {
	AFFECTED_STYLE,
	DISTRICT_STYLE,
	HAZARD_STYLES,
	LAYER_NAMES,
	OVERLAY_STYLE,
	getHazardType
} from '@/views/survey/constants.js'

const SURVEY_LAYER_NAMES = [
	LAYER_NAMES.district,
	LAYER_NAMES.affected,
	LAYER_NAMES.overlay,
	LAYER_NAMES.seawall,
	LAYER_NAMES.fishing,
	LAYER_NAMES.mariculture,
	LAYER_NAMES.tourist
]

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
		geojson: JSON.stringify({
			type: 'FeatureCollection',
			features
		}),
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
		map.flyToManager.flyTo(features[0].geometry.coordinates, 13)
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

export function drawDistrict(feature, { fly = true } = {}) {
	if (!feature) {
		removeLayer(LAYER_NAMES.district)
		return
	}
	addOrReplaceLayer(LAYER_NAMES.district, [feature], DISTRICT_STYLE, 40)
	if (fly) flyToFeatures([feature])
}

export function drawAffectedArea(feature) {
	if (!feature) {
		removeLayer(LAYER_NAMES.affected)
		return
	}
	addOrReplaceLayer(LAYER_NAMES.affected, [feature], AFFECTED_STYLE, 50)
	flyToFeatures([feature])
}

export function drawHazard(typeId, features, { fly = true } = {}) {
	const config = getHazardType(typeId)
	if (!config) return
	HAZARD_TYPES_LAYER_CLEAN(typeId)
	if (!features?.length) return
	addOrReplaceLayer(config.layerName, features, HAZARD_STYLES[typeId], 55)
	if (fly) flyToFeatures(features)
}

function HAZARD_TYPES_LAYER_CLEAN(keepId) {
	;['seawall', 'fishing', 'mariculture', 'tourist'].forEach(id => {
		if (id !== keepId) removeLayer(getHazardType(id).layerName)
	})
}

export function drawOverlay(features) {
	if (!features?.length) {
		removeLayer(LAYER_NAMES.overlay)
		return
	}
	addOrReplaceLayer(LAYER_NAMES.overlay, features, OVERLAY_STYLE, 70)
	flyToFeatures(features)
}

export async function drawSceneImage(scene, { fly = true } = {}) {
	if (!scene) {
		removeLocalRsImageLayer()
		return false
	}
	return showLocalRsImage(scene, { fly })
}

export function clearSurveyLayers() {
	SURVEY_LAYER_NAMES.forEach(removeLayer)
	removeLocalRsImageLayer()
}
