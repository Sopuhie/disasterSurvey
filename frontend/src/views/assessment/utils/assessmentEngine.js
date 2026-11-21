import { LEVEL_MAP } from '@/views/assessment/constants.js'
import { landUsePrices } from '@/views/assessment/mock/economyData.js'
import { makeCircleFeature, makePointFeature, mockCounties } from '@/views/survey/mock/surveyDemoData.js'
import { formatDisasterLevel, toMeters, toProtectPeople } from '@/shared/metricUnits.js'

const levelByValue = (value, breaks) => {
	if (value >= breaks[0]) return 1
	if (value >= breaks[1]) return 2
	if (value >= breaks[2]) return 3
	return 4
}

export const calcEconomicLoss = (economy, affectedKm2) => {
	const rows = Object.keys(economy.land || {}).map(type => {
		const ratio = economy.land[type]
		const area = Number((affectedKm2 * ratio).toFixed(2))
		const unit = landUsePrices[type] || 50
		const loss = Number((area * unit).toFixed(2))
		return { type, ratio, ratioPct: Math.round(ratio * 100), area, unit, loss }
	})
	const totalLoss = Number(rows.reduce((sum, item) => sum + item.loss, 0).toFixed(2))
	const gdpShock = Number(((totalLoss / 10000 / (economy.gdp || 1)) * 100).toFixed(2))
	return {
		rows,
		totalLoss,
		gdp: economy.gdp,
		gdpShock,
		affectedKm2
	}
}

export const buildWindBehavior = event => {
	const points = event.points || []
	const maxWind = Math.max(...points.map(item => item.wind), event.maxWind || 0)
	const avgDir = points[points.length - 1]?.dir || points[0]?.dir || '-'
	const peak = points.reduce((best, item) => (item.wind > (best?.wind || 0) ? item : best), points[0])
	const inundation = peak
		? makeCircleFeature(peak.lng, peak.lat, Math.max(maxWind * 0.42, 12), {
				name: `${event.name}淹没范围`
		  })
		: null
	return {
		event,
		maxWind,
		avgDir,
		inundation,
		pathFeature: {
			type: 'Feature',
			geometry: { type: 'LineString', coordinates: points.map(item => [item.lng, item.lat]) },
			properties: { name: event.name }
		},
		pointFeatures: points.map(item => ({
			type: 'Feature',
			geometry: { type: 'Point', coordinates: [item.lng, item.lat] },
			properties: item
		}))
	}
}

/** 按所选 SAR 场景强度缩放破坏性样本，使结果随场景变化 */
export const scaleDamageByScene = (rows, scene, kind) => {
	const factor =
		kind === 'wind'
			? Number(scene?.maxWind || 30) / 30
			: Number(scene?.maxHeight || 4) / 4
	const clamped = Math.max(0.55, Math.min(factor, 1.45))
	return (rows || []).map(item => {
		const next = { ...item }
		if (kind === 'wind' && item.wind != null) {
			next.wind = Number((Number(item.wind) * clamped).toFixed(1))
			delete next.level
		}
		if (kind === 'wave') {
			if (item.height != null) next.height = Number((Number(item.height) * clamped).toFixed(1))
			if (item.speed != null) next.speed = Number((Number(item.speed) * clamped * 0.95).toFixed(1))
			delete next.level
		}
		if (item.analysis) {
			const tag = scene?.name ? `（依据 ${scene.name}）` : ''
			next.analysis = String(item.analysis).includes('依据') ? item.analysis : `${item.analysis}${tag}`
		}
		return next
	})
}

/** 按台风/场景强度缩放面积类结果 */
export const scaleMetricRows = (rows, scale, keys = ['areaKm2', 'population', 'lengthKm']) => {
	const factor = Math.max(0.5, Math.min(Number(scale) || 1, 1.6))
	return (rows || []).map(item => {
		const next = { ...item }
		keys.forEach(key => {
			if (item[key] != null && item[key] !== '') {
				next[key] = Number((Number(item[key]) * factor).toFixed(2))
			}
		})
		return next
	})
}

export const decorateDamageRows = (rows, kind) => {
	return rows.map(item => {
		const value = kind === 'wind' ? item.wind : item.height
		const level = item.level || levelByValue(value, kind === 'wind' ? [32, 28, 24] : [4.5, 3.5, 2.8])
		return {
			...item,
			level,
			levelLabel: LEVEL_MAP[level].label,
			levelText: LEVEL_MAP[level].text
		}
	})
}

/** 漫堤分析 JSON → 表格行（潮位/高程统一 m，保护人口统一 人） */
export const mapOvertoppingRows = list => {
	return (list || []).map((item, index) => {
		const warnTide =
			item.warnTide != null
				? toMeters(item.warnTide)
				: item.warn != null
					? toMeters(item.warn)
					: toMeters(item.ysjj)
		const tideHeight = toMeters(item.tideHeight ?? item.tideheight ?? item.astTide)
		const crestElev = toMeters(item.crestElev ?? item.heightActual ?? item.seawallheight)
		const wallElev = toMeters(item.wallElev ?? item.dwwmnph)
		return {
			id: item.id || `ot_${index}_${item.name}`,
			city: item.city || '',
			name: item.name || '',
			location: item.location || '',
			warnTide: warnTide ?? '-',
			tideHeight: tideHeight ?? '-',
			blueWarn: toMeters(item.lsjj) ?? item.lsjj,
			yellowWarn: toMeters(item.ysjj) ?? item.ysjj,
			orangeWarn: toMeters(item.csjj) ?? item.csjj,
			redWarn: toMeters(item.hsjj) ?? item.hsjj,
			protectPop: toProtectPeople(item.protectPop ?? item.ptpp),
			crestElev: crestElev ?? '-',
			hasWall: item.hasWall || item.isdwwmnph || '',
			wallElev: wallElev ?? '-',
			warnLevel: item.warnLevel || '',
			tideStation: item.tideStation || item.trd || '',
			waterPoint: item.waterPoint,
			hazardCode: item.hazardCode || '',
			lon: item.lon,
			lat: item.lat,
			raw: item
		}
	})
}

export const damageRowsToFeatures = rows => {
	return (rows || [])
		.filter(item => Number.isFinite(Number(item.lon)) && Number.isFinite(Number(item.lat)))
		.map(item => {
			const lon = Number(item.lon)
			const lat = Number(item.lat)
			const props = { ...item, lon, lat }
			if (item.target === '海岸线' || item.target === '海堤' || item.target === '海塘') {
				const dx = 0.035
				return {
					type: 'Feature',
					geometry: {
						type: 'LineString',
						coordinates: [
							[lon - dx, lat],
							[lon, lat + 0.012],
							[lon + dx, lat]
						]
					},
					properties: props
				}
			}
			if (item.target === '渔港' || item.target === '海水养殖区') {
				return makeCircleFeature(lon, lat, 2.2, props)
			}
			return makePointFeature(lon, lat, props)
		})
}

/** 运维破坏样本 + 调查四类承灾体核验字段 → 统一破坏行 */
export const enrichDamageWithHazards = (damageRows, hazardPack = {}) => {
	const byId = {}
	const byName = {}
	Object.keys(hazardPack).forEach(typeId => {
		;(hazardPack[typeId] || []).forEach(row => {
			if (row.id) byId[String(row.id)] = { ...row, typeId }
			if (row.name) byName[row.name] = { ...row, typeId }
		})
	})
	return (damageRows || []).map(item => {
		const linked = (item.hazardCode && byId[String(item.hazardCode)]) || byName[item.name]
		if (!linked) return item
		const lon = Number(item.lon) || Number(linked.lon)
		const lat = Number(item.lat) || Number(linked.lat)
		const level = Number(item.level) || Number(linked.disasterLevel) || 3
		return {
			...item,
			lon,
			lat,
			county: item.county || linked.county || String(linked.division || '').split('-').pop() || '',
			level,
			hazardCode: item.hazardCode || linked.id,
			monitorValue: linked.monitorValue,
			economicLoss: linked.economicLoss,
			analysis:
				item.analysis ||
				`依据调查核验：监测值 ${linked.monitorValue ?? '-'} ${linked.monitorUnit || '万亩'}，灾害等级 ${linked.disasterLevelLabel || formatDisasterLevel(level)}。`
		}
	})
}

/** 漫堤行关联海堤台账坐标/几何，供上图 */
export const linkOvertopToSeawalls = (overtopRows, seawalls = []) => {
	const byId = Object.fromEntries((seawalls || []).filter(item => item.id).map(item => [String(item.id), item]))
	const byName = Object.fromEntries((seawalls || []).filter(item => item.name).map(item => [item.name, item]))
	return (overtopRows || []).map(item => {
		const hit = (item.hazardCode && byId[String(item.hazardCode)]) || byName[item.name]
		if (!hit) return item
		return {
			...item,
			hazardCode: item.hazardCode || hit.id,
			lon: Number(item.lon) || Number(hit.lon),
			lat: Number(item.lat) || Number(hit.lat),
			geojson: item.geojson || hit.geojson || '',
			division: hit.division,
			length: hit.length || hit.lengthKm
		}
	})
}

export const overtoppingRowsToFeatures = (rows, makeHazardFeature) => {
	return (rows || [])
		.map(item => {
			if (typeof makeHazardFeature === 'function' && (item.geojson || (item.lon && item.lat))) {
				const feat = makeHazardFeature(
					{
						name: item.name,
						lon: item.lon,
						lat: item.lat,
						geojson: item.geojson,
						warnLevel: item.warnLevel,
						crestElev: item.crestElev,
						protectPop: item.protectPop
					},
					'seawall'
				)
				if (feat) {
					feat.properties = { ...feat.properties, ...item, dangerName: item.name }
					return feat
				}
			}
			if (!Number.isFinite(Number(item.lon)) || !Number.isFinite(Number(item.lat))) return null
			return makePointFeature(Number(item.lon), Number(item.lat), { ...item, dangerName: item.name })
		})
		.filter(Boolean)
}

export const inundationRowsToFeatures = rows => {
	return (rows || []).map(item => {
		const county = mockCounties.find(row => row.name === item.name)
		const lon = Number(item.lon) || county?.lon || 119.5
		const lat = Number(item.lat) || county?.lat || 26
		return makeCircleFeature(lon, lat, Math.max(Math.sqrt(item.areaKm2 || 10) * 1.4, 7), {
			name: item.name,
			...item
		})
	})
}
