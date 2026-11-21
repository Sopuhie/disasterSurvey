import * as turf from '@turf/turf'
import { getDistrictName, loadCountyFeatures, loadHazardFeatures } from './dataLoader.js'
import { getHazardType } from '@/views/survey/constants.js'
import { km2ToWanMu as toWanMu } from '@/shared/metricUnits.js'
import {
	getLocalRsItemById,
	loadLocalRsManifest,
	resolveSceneImageId
} from '@/utils/localRsImages.js'

export const squareMetersToKm2 = m2 => Number((m2 / 1e6).toFixed(2))
export const km2ToWanMu = toWanMu

const flattenPolygons = feature => {
	if (!feature?.geometry) return []
	const type = feature.geometry.type
	if (type === 'Polygon') return [feature]
	if (type === 'MultiPolygon') return turf.flatten(feature).features
	return []
}

export const intersectFeatures = (a, b) => {
	const aPolys = flattenPolygons(a)
	const bPolys = flattenPolygons(b)
	const parts = []
	aPolys.forEach(p => {
		bPolys.forEach(q => {
			try {
				const hit = turf.intersect(p, q)
				if (hit) parts.push(hit)
			} catch (err) {
				// skip invalid pairs
			}
		})
	})
	if (!parts.length) return null
	if (parts.length === 1) return parts[0]
	try {
		return parts.reduce((acc, cur) => turf.union(acc, cur))
	} catch (err) {
		return turf.featureCollection(parts)
	}
}

const unionFeatures = features => {
	const polys = features.flatMap(flattenPolygons)
	if (!polys.length) return null
	try {
		return polys.reduce((acc, cur) => {
			try {
				return turf.union(acc, cur)
			} catch (err) {
				return acc
			}
		})
	} catch (err) {
		return polys[0]
	}
}

export const findDistrictByName = (features, name) => {
	if (!name) return null
	const keyword = name.trim()
	const normalize = text => (text || '').replace(/[市县区]$/g, '')
	return (
		features.find(item => getDistrictName(item) === keyword) ||
		features.find(item => {
			const label = getDistrictName(item)
			const city = item.properties?.city || item.properties?.地级 || ''
			return (
				label.includes(keyword) ||
				keyword.includes(label) ||
				city === keyword ||
				normalize(label) === normalize(keyword)
			)
		})
	)
}

export const findDistrictByCoord = (features, lon, lat) => {
	const point = turf.point([Number(lon), Number(lat)])
	return features.find(item => {
		try {
			return turf.booleanPointInPolygon(point, item)
		} catch (err) {
			return false
		}
	})
}

export const listHazards = (features, keyword = '') => {
	const text = keyword.trim()
	return features
		.map(item => ({
			name: item.properties?.dangerName || item.properties?.name || '',
			division: item.properties?.division || '',
			town: item.properties?.town || '',
			feature: item
		}))
		.filter(item => item.name)
		.filter(item => !text || item.name.includes(text) || item.division.includes(text))
}

/**
 * 在基准面内生成偏沿岸的解译面（避免整幅影像矩形 / 示意圆）。
 */
const buildCoastalStrip = (baseFeature, scene, ratio) => {
	const [minX, minY, maxX, maxY] = turf.bbox(baseFeature)
	const width = Math.max(maxX - minX, 1e-6)
	const height = Math.max(maxY - minY, 1e-6)
	const seed = String(scene?.id || scene?.name || 'scene')
		.split('')
		.reduce((sum, ch) => sum + ch.charCodeAt(0), 0)
	const inlandX = minX + width * (1 - ratio)
	const steps = 16
	const ring = []
	for (let i = 0; i <= steps; i++) {
		const t = i / steps
		const y = minY + height * t
		const wave = Math.sin(t * Math.PI * 3 + seed * 0.07) * width * 0.035
		ring.push([inlandX + wave, y])
	}
	for (let i = steps; i >= 0; i--) {
		const t = i / steps
		const y = minY + height * t
		const wave = Math.cos(t * Math.PI * 2.4 + seed * 0.05) * width * 0.02
		ring.push([maxX + wave * 0.4, y])
	}
	ring.push(ring[0])
	try {
		return turf.polygon([ring])
	} catch (err) {
		return turf.bboxPolygon([inlandX, minY, maxX, maxY])
	}
}

const asSinglePolygon = feature => {
	if (!feature) return null
	if (feature.type === 'Feature') return feature
	if (feature.type === 'FeatureCollection') return feature.features?.[0] || null
	return null
}

/**
 * 按影像范围解译受灾面：
 * 1) 优先使用本地遥感影像 bbox
 * 2) 若已查询区县，则与真实县界相交
 * 3) 再按 inundationRatio 裁出沿岸解译带，避免整幅矩形或圆心示意面
 */
export const buildAffectedArea = (sourceFeature, scene, imageBbox) => {
	const ratio = Math.min(Math.max(Number(scene?.inundationRatio) || 0.45, 0.28), 0.92)
	const bbox = Array.isArray(imageBbox) && imageBbox.length === 4 ? imageBbox : scene?.bbox
	let imagePoly = null
	if (Array.isArray(bbox) && bbox.length === 4 && bbox.every(n => Number.isFinite(Number(n)))) {
		imagePoly = turf.bboxPolygon(bbox.map(Number))
	}

	let base = imagePoly
	if (imagePoly && sourceFeature?.geometry) {
		const clipped = asSinglePolygon(intersectFeatures(imagePoly, sourceFeature))
		base = clipped || sourceFeature
	} else if (!imagePoly && sourceFeature?.geometry) {
		base = sourceFeature
	}
	if (!base?.geometry) {
		base = turf.bboxPolygon([118.02, 23.55, 120.72, 27.52])
	}

	const strip = buildCoastalStrip(base, scene, ratio)
	let feature = asSinglePolygon(intersectFeatures(base, strip)) || asSinglePolygon(strip) || base
	if (!feature?.geometry) feature = base

	feature.properties = {
		...(feature.properties || {}),
		name: '受灾范围',
		sceneId: scene?.id || '',
		sceneName: scene?.name || scene?.displayName || '',
		imageId: scene?.imageId || '',
		imageBbox: bbox || null,
		source: imagePoly ? 'rs-image-extent' : 'district-extent'
	}
	return feature
}

/** 异步解析：读取本地影像 bbox + 区县真实几何后生成受灾面 */
export const resolveAffectedFeature = async ({ scene, district } = {}) => {
	await loadLocalRsManifest()
	const imageId = resolveSceneImageId(scene)
	const item = getLocalRsItemById(imageId)
	const imageBbox = item?.bbox || scene?.bbox || null
	let districtFeat = district?.feature || null
	if (!districtFeat && district?.name) {
		const counties = await loadCountyFeatures()
		districtFeat = findDistrictByName(counties, district.name)
	}
	return buildAffectedArea(districtFeat, { ...scene, imageId: imageId || scene?.imageId || '' }, imageBbox)
}

export const summarizeAffectedArea = async affectedFeature => {
	const counties = await loadCountyFeatures()
	const hits = []
	counties.forEach(county => {
		try {
			if (turf.booleanDisjoint(affectedFeature, county)) return
		} catch (err) {
			return
		}
		const inter = intersectFeatures(affectedFeature, county)
		if (!inter || inter.type === 'FeatureCollection') return
		const areaKm2 = squareMetersToKm2(turf.area(inter))
		if (!areaKm2) return
		hits.push({
			name: getDistrictName(county),
			city: county.properties?.city || county.properties?.地级 || '',
			areaKm2,
			density: county.properties?.density,
			population: county.properties?.population,
			feature: inter
		})
	})
	const areaKm2 = squareMetersToKm2(turf.area(affectedFeature))
	return {
		areaKm2,
		areaWanMu: km2ToWanMu(areaKm2),
		countyCount: hits.length,
		counties: hits
	}
}

export const overlayDistricts = async (affectedFeature, mode = 'district') => {
	const summary = await summarizeAffectedArea(affectedFeature)
	const rows = summary.counties
		.map(item => {
			const affectedPop = item.density ? Math.round(item.density * item.areaKm2) : 0
			return {
				name: item.name,
				city: item.city,
				areaKm2: item.areaKm2,
				areaWanMu: km2ToWanMu(item.areaKm2),
				density: item.density ? Number(item.density.toFixed(2)) : '-',
				affectedPopulation: affectedPop,
				feature: item.feature
			}
		})
		.sort((a, b) => b.areaKm2 - a.areaKm2)

	return {
		target: mode,
		rows,
		features: rows.map(item => {
			item.feature.properties = {
				...(item.feature.properties || {}),
				name: item.name,
				areaKm2: item.areaKm2,
				affectedPopulation: item.affectedPopulation
			}
			return item.feature
		}),
		totals: {
			count: rows.length,
			areaKm2: Number(rows.reduce((sum, item) => sum + item.areaKm2, 0).toFixed(2)),
			affectedPopulation: rows.reduce((sum, item) => sum + (item.affectedPopulation || 0), 0)
		}
	}
}

const featureIntersects = (affectedFeature, feature) => {
	const type = feature.geometry?.type
	try {
		if (type === 'Point' || type === 'MultiPoint') {
			return turf.booleanPointInPolygon(feature, affectedFeature)
		}
		return !turf.booleanDisjoint(affectedFeature, feature)
	} catch (err) {
		return false
	}
}

export const overlayHazards = async (affectedFeature, typeId) => {
	const config = getHazardType(typeId)
	const features = await loadHazardFeatures(typeId)
	const rows = []
	const hitFeatures = []

	features.forEach(feature => {
		if (!featureIntersects(affectedFeature, feature)) return
		const type = feature.geometry?.type
		let areaKm2 = 0
		let lengthKm = 0
		if (type === 'Polygon' || type === 'MultiPolygon') {
			const inter = intersectFeatures(affectedFeature, feature)
			if (inter && inter.type !== 'FeatureCollection') {
				areaKm2 = squareMetersToKm2(turf.area(inter))
			} else {
				areaKm2 = squareMetersToKm2(turf.area(feature))
			}
		} else if (type === 'LineString' || type === 'MultiLineString') {
			lengthKm = Number(turf.length(feature, { units: 'kilometers' }).toFixed(2))
		} else if (type === 'Point' || type === 'MultiPoint') {
			areaKm2 = Number(feature.properties?.面积 || feature.properties?.area || 0)
		}

		const row = {
			name: feature.properties?.dangerName || feature.properties?.name || '',
			division: feature.properties?.division || '',
			town: feature.properties?.town || '',
			areaKm2,
			areaWanMu: km2ToWanMu(areaKm2),
			lengthKm
		}
		rows.push(row)
		hitFeatures.push({
			...feature,
			properties: { ...feature.properties, ...row }
		})
	})

	return {
		target: typeId,
		label: config?.label || typeId,
		rows,
		features: hitFeatures,
		totals: {
			count: rows.length,
			areaKm2: Number(rows.reduce((sum, item) => sum + (item.areaKm2 || 0), 0).toFixed(2)),
			lengthKm: Number(rows.reduce((sum, item) => sum + (item.lengthKm || 0), 0).toFixed(2))
		}
	}
}

export const buildCompareSeries = (overlayResult, reports = [], hazardName = '') => {
	const row = hazardName ? overlayResult?.rows?.find(item => item.name === hazardName) : null
	/** area 仅取 km²，禁止用 monitorValue（万亩/km）顶替，避免图表口径混用 */
	const interpret = row
		? {
				source: '现状监测',
				count: 1,
				area: Number(row.areaKm2) || 0,
				length: Number(row.lengthKm) || 0,
				loss: Number(row.economicLoss) || 0,
				population: Number(row.affectedPopulation) || 0
		  }
		: {
				source: '现状监测',
				count: overlayResult?.totals?.count || 0,
				area: Number(overlayResult?.totals?.areaKm2) || 0,
				length: Number(overlayResult?.totals?.lengthKm) || 0,
				loss: Number(overlayResult?.totals?.economicLoss) || 0,
				population: Number(overlayResult?.totals?.affectedPopulation) || 0
		  }
	return [
		interpret,
		...reports.map(item => ({
			source: item.targetName ? `${item.year}·${item.targetName}` : String(item.year || '上报'),
			count: Number(item.count) || 0,
			area: Number(item.area) || 0,
			length: Number(item.length) || 0,
			loss: Number(item.loss) || 0,
			population: Number(item.population || item.affectedPopulation) || 0
		}))
	]
}

export { unionFeatures }
