import * as turf from '@turf/turf'
import { km2ToWanMu } from '@/shared/metricUnits.js'
import { buildAffectedArea } from '@/views/survey/utils/overlayEngine.js'

export const mockCounties = [
	{ name: '连江县', city: '福州市', population: 64.6, density: 568.62, statArea: 1136.09, lon: 119.53, lat: 26.2 },
	{ name: '长乐区', city: '福州市', population: 81.4, density: 1202.83, statArea: 676.74, lon: 119.52, lat: 25.96 },
	{ name: '福清市', city: '福州市', population: 139.7, density: 910.4, statArea: 1534.6, lon: 119.39, lat: 25.72 },
	{ name: '平潭县', city: '福州市', population: 38.6, density: 1040.2, statArea: 371.1, lon: 119.79, lat: 25.51 },
	{ name: '霞浦县', city: '宁德市', population: 46.8, density: 312.5, statArea: 1496.0, lon: 120.0, lat: 26.89 },
	{ name: '福鼎市', city: '宁德市', population: 54.2, density: 360.1, statArea: 1506.3, lon: 120.22, lat: 27.32 },
	{ name: '蕉城区', city: '宁德市', population: 47.1, density: 328.4, statArea: 1433.2, lon: 119.53, lat: 26.66 },
	{ name: '惠安县', city: '泉州市', population: 104.6, density: 1448.36, statArea: 722.2, lon: 118.8, lat: 25.03 },
	{ name: '晋江市', city: '泉州市', population: 206.9, density: 3190.5, statArea: 649.0, lon: 118.55, lat: 24.78 },
	{ name: '石狮市', city: '泉州市', population: 69.8, density: 4362.5, statArea: 160.0, lon: 118.65, lat: 24.73 },
	{ name: '思明区', city: '厦门市', population: 106.6, density: 14180.0, statArea: 75.2, lon: 118.08, lat: 24.45 },
	{ name: '翔安区', city: '厦门市', population: 42.3, density: 1024.6, statArea: 412.8, lon: 118.25, lat: 24.62 },
	{ name: '东山县', city: '漳州市', population: 22.1, density: 1134.2, statArea: 194.9, lon: 117.4, lat: 23.7 },
	{ name: '漳浦县', city: '漳州市', population: 87.5, density: 418.6, statArea: 2091.0, lon: 117.61, lat: 24.12 },
	{ name: '秀屿区', city: '莆田市', population: 58.4, density: 1168.0, statArea: 500.0, lon: 119.1, lat: 25.32 }
]

export const mockTowns = [
	{ name: '敖江镇', county: '连江县', city: '福州市', population: 8.6, density: 620.1, statArea: 138.4, lon: 119.62, lat: 26.18, areaKm2: 9.4 },
	{ name: '黄岐镇', county: '连江县', city: '福州市', population: 4.2, density: 780.4, statArea: 53.8, lon: 119.82, lat: 26.32, areaKm2: 6.8 },
	{ name: '城头镇', county: '连江县', city: '福州市', population: 3.9, density: 410.2, statArea: 95.1, lon: 119.68, lat: 26.24, areaKm2: 5.2 },
	{ name: '江阴镇', county: '福清市', city: '福州市', population: 6.8, density: 540.6, statArea: 125.7, lon: 119.32, lat: 25.48, areaKm2: 11.6 },
	{ name: '沙埔镇', county: '福清市', city: '福州市', population: 5.1, density: 468.3, statArea: 108.9, lon: 119.45, lat: 25.55, areaKm2: 8.3 },
	{ name: '屿头乡', county: '平潭县', city: '福州市', population: 1.8, density: 390.5, statArea: 46.1, lon: 119.72, lat: 25.58, areaKm2: 4.7 },
	{ name: '北厝镇', county: '平潭县', city: '福州市', population: 3.4, density: 512.8, statArea: 66.3, lon: 119.78, lat: 25.46, areaKm2: 7.1 },
	{ name: '三沙镇', county: '霞浦县', city: '宁德市', population: 5.6, density: 318.4, statArea: 175.8, lon: 120.16, lat: 26.9, areaKm2: 10.2 },
	{ name: '前岐镇', county: '福鼎市', city: '宁德市', population: 4.8, density: 286.7, statArea: 167.4, lon: 120.28, lat: 27.28, areaKm2: 6.4 },
	{ name: '崇武镇', county: '惠安县', city: '泉州市', population: 7.2, density: 1486.2, statArea: 48.4, lon: 118.93, lat: 24.89, areaKm2: 5.9 },
	{ name: '西埔镇', county: '东山县', city: '漳州市', population: 6.1, density: 980.3, statArea: 62.2, lon: 117.43, lat: 23.7, areaKm2: 4.4 },
	{ name: '杏陈镇', county: '东山县', city: '漳州市', population: 3.3, density: 760.5, statArea: 43.4, lon: 117.48, lat: 23.78, areaKm2: 5.6 },
	{ name: '东峤镇', county: '秀屿区', city: '莆田市', population: 8.4, density: 1120.6, statArea: 75.0, lon: 119.05, lat: 25.18, areaKm2: 3.8 },
	{ name: '三都镇', county: '蕉城区', city: '宁德市', population: 2.7, density: 210.4, statArea: 128.3, lon: 119.68, lat: 26.64, areaKm2: 6.1 }
]

export const mockHazards = {
	seawall: [
		{ name: '敖江海堤', type: '海堤', division: '福建省-福州市-连江县', town: '敖江镇', lon: 119.62, lat: 26.18, lengthKm: 4.6, areaKm2: 0, grade: '2级', standard: '50年一遇', builtYear: 1998, manager: '连江县水利局', status: '在役' },
		{ name: '城头海堤', type: '海堤', division: '福建省-福州市-连江县', town: '城头镇', lon: 119.68, lat: 26.24, lengthKm: 3.2, areaKm2: 0, grade: '3级', standard: '20年一遇', builtYear: 2004, manager: '连江县水利局', status: '在役' },
		{ name: '江阴海堤', type: '海堤', division: '福建省-福州市-福清市', town: '江阴镇', lon: 119.32, lat: 25.48, lengthKm: 5.8, areaKm2: 0, grade: '1级', standard: '100年一遇', builtYear: 2008, manager: '福清市水利局', status: '在役' },
		{ name: '沙埔海堤', type: '海堤', division: '福建省-福州市-福清市', town: '沙埔镇', lon: 119.45, lat: 25.55, lengthKm: 2.9, areaKm2: 0, grade: '3级', standard: '20年一遇', builtYear: 1995, manager: '福清市水利局', status: '在役' },
		{ name: '三沙海堤', type: '海堤', division: '福建省-宁德市-霞浦县', town: '三沙镇', lon: 120.12, lat: 26.92, lengthKm: 3.7, areaKm2: 0, grade: '2级', standard: '50年一遇', builtYear: 2001, manager: '霞浦县水利局', status: '在役' },
		{ name: '前岐海堤', type: '海堤', division: '福建省-宁德市-福鼎市', town: '前岐镇', lon: 120.28, lat: 27.28, lengthKm: 4.1, areaKm2: 0, grade: '2级', standard: '50年一遇', builtYear: 2006, manager: '福鼎市水利局', status: '在役' }
	],
	fishing: [
		{ name: '江阴渔港', type: '一级渔港', division: '福建省-福州市-福清市', town: '江阴镇', lon: 119.3, lat: 25.47, areaKm2: 1.86, berths: 86, capacity: '渔船 420 艘', builtYear: 2012, manager: '福清市海洋与渔业局', status: '在用' },
		{ name: '黄岐渔港', type: '一级渔港', division: '福建省-福州市-连江县', town: '黄岐镇', lon: 119.82, lat: 26.32, areaKm2: 1.24, berths: 64, capacity: '渔船 280 艘', builtYear: 2009, manager: '连江县海洋与渔业局', status: '在用' },
		{ name: '三沙渔港', type: '中心渔港', division: '福建省-宁德市-霞浦县', town: '三沙镇', lon: 120.21, lat: 26.91, areaKm2: 2.05, berths: 102, capacity: '渔船 510 艘', builtYear: 2015, manager: '霞浦县海洋与渔业局', status: '在用' },
		{ name: '东山渔港', type: '一级渔港', division: '福建省-漳州市-东山县', town: '西埔镇', lon: 117.43, lat: 23.7, areaKm2: 1.58, berths: 78, capacity: '渔船 360 艘', builtYear: 2011, manager: '东山县海洋与渔业局', status: '在用' },
		{ name: '崇武渔港', type: '二级渔港', division: '福建省-泉州市-惠安县', town: '崇武镇', lon: 118.93, lat: 24.89, areaKm2: 0.92, berths: 46, capacity: '渔船 180 艘', builtYear: 2007, manager: '惠安县海洋与渔业局', status: '在用' }
	],
	mariculture: [
		{ name: '沙埔镇围海养殖', type: '围海养殖', division: '福建省-福州市-福清市', town: '沙埔镇', lon: 119.47, lat: 25.56, areaKm2: 6.4, species: '牡蛎 / 对虾', manager: '福清市海洋与渔业局', status: '在养' },
		{ name: '屿头乡养殖单元', type: '筏式养殖', division: '福建省-福州市-平潭县', town: '屿头乡', lon: 119.72, lat: 25.58, areaKm2: 4.8, species: '紫菜 / 海带', manager: '平潭综合实验区海洋与渔业局', status: '在养' },
		{ name: '三沙围海养殖', type: '围海养殖', division: '福建省-宁德市-霞浦县', town: '三沙镇', lon: 120.16, lat: 26.88, areaKm2: 8.2, species: '大黄鱼 / 鲍鱼', manager: '霞浦县海洋与渔业局', status: '在养' },
		{ name: '东山湾养殖区', type: '浅海养殖', division: '福建省-漳州市-东山县', town: '杏陈镇', lon: 117.48, lat: 23.78, areaKm2: 5.1, species: '牡蛎 / 花蛤', manager: '东山县海洋与渔业局', status: '在养' },
		{ name: '湄洲湾养殖区', type: '浅海养殖', division: '福建省-莆田市-秀屿区', town: '东峤镇', lon: 119.05, lat: 25.18, areaKm2: 3.6, species: '牡蛎', manager: '秀屿区海洋与渔业局', status: '在养' }
	],
	tourist: [
		{ name: '坛南湾景区', type: '滨海旅游区', division: '福建省-福州市-平潭县', town: '北厝镇', lon: 119.78, lat: 25.46, areaKm2: 2.4, level: '4A', manager: '平潭综合实验区文旅集团', status: '开放' },
		{ name: '黄岐半岛景区', type: '滨海旅游区', division: '福建省-福州市-连江县', town: '黄岐镇', lon: 119.84, lat: 26.31, areaKm2: 1.7, level: '3A', manager: '连江县文旅局', status: '开放' },
		{ name: '三都澳景区', type: '滨海旅游区', division: '福建省-宁德市-蕉城区', town: '三都镇', lon: 119.68, lat: 26.64, areaKm2: 3.1, level: '4A', manager: '蕉城区文旅局', status: '开放' },
		{ name: '马銮湾景区', type: '滨海旅游区', division: '福建省-漳州市-东山县', town: '杏陈镇', lon: 117.42, lat: 23.66, areaKm2: 2.0, level: '4A', manager: '东山县文旅局', status: '开放' },
		{ name: '崇武古城景区', type: '滨海旅游区', division: '福建省-泉州市-惠安县', town: '崇武镇', lon: 118.92, lat: 24.88, areaKm2: 1.3, level: '4A', manager: '惠安县文旅局', status: '开放' }
	]
}

const hitCounties = [
	{ name: '连江县', city: '福州市', areaKm2: 42.6, density: 568.62, affectedPopulation: 24223 },
	{ name: '长乐区', city: '福州市', areaKm2: 28.4, density: 1202.83, affectedPopulation: 34160 },
	{ name: '福清市', city: '福州市', areaKm2: 36.8, density: 910.4, affectedPopulation: 33503 },
	{ name: '平潭县', city: '福州市', areaKm2: 18.2, density: 1040.2, affectedPopulation: 18932 },
	{ name: '霞浦县', city: '宁德市', areaKm2: 31.5, density: 312.5, affectedPopulation: 9844 },
	{ name: '惠安县', city: '泉州市', areaKm2: 22.7, density: 1448.36, affectedPopulation: 32878 }
].map(item => ({ ...item, areaWanMu: km2ToWanMu(item.areaKm2) }))

export const mockIdentifyMeta = {
	areaKm2: 180.2,
	areaWanMu: km2ToWanMu(180.2),
	countyCount: hitCounties.length,
	counties: hitCounties.map(item => ({ ...item, areaWanMu: km2ToWanMu(item.areaKm2) }))
}

const toHazardOverlay = (list, scale = 0.55) => {
	const rows = list.slice(0, Math.max(3, list.length - 1)).map((item, index) => {
		const areaKm2 = Number(((item.areaKm2 || 0.8) * (scale - index * 0.06)).toFixed(2))
		return {
			name: item.name,
			division: item.division,
			town: item.town,
			areaKm2,
			areaWanMu: km2ToWanMu(areaKm2),
			lengthKm: Number(((item.lengthKm || 0) * Math.max(0.35, 0.72 - index * 0.08)).toFixed(2))
		}
	})
	return {
		rows,
		totals: {
			count: rows.length,
			areaKm2: Number(rows.reduce((sum, item) => sum + (Number(item.areaKm2) || 0), 0).toFixed(2)),
			lengthKm: Number(rows.reduce((sum, item) => sum + (Number(item.lengthKm) || 0), 0).toFixed(2))
		}
	}
}

export const mockOverlayResults = {
	population: {
		target: 'population',
		rows: hitCounties,
		totals: {
			count: hitCounties.length,
			areaKm2: 180.2,
			affectedPopulation: hitCounties.reduce((sum, item) => sum + item.affectedPopulation, 0)
		}
	},
	district: {
		target: 'district',
		rows: mockTowns.map(item => ({
			name: item.name,
			county: item.county,
			city: item.city,
			areaKm2: item.areaKm2,
			areaWanMu: km2ToWanMu(item.areaKm2),
			density: item.density,
			affectedPopulation: Math.round(item.density * item.areaKm2)
		})),
		totals: {
			count: mockTowns.length,
			areaKm2: Number(mockTowns.reduce((sum, item) => sum + item.areaKm2, 0).toFixed(2))
		}
	},
	seawall: toHazardOverlay(mockHazards.seawall),
	fishing: toHazardOverlay(mockHazards.fishing),
	mariculture: toHazardOverlay(mockHazards.mariculture),
	tourist: toHazardOverlay(mockHazards.tourist)
}

export function makeCircleFeature(lon, lat, km, properties = {}) {
	const feature = turf.circle([lon, lat], km, { steps: 48, units: 'kilometers' })
	feature.properties = { ...properties }
	return feature
}

export function makePointFeature(lon, lat, properties = {}) {
	return {
		type: 'Feature',
		geometry: { type: 'Point', coordinates: [lon, lat] },
		properties
	}
}

export function makeDistrictFeature(item) {
	const radius = Math.max(Math.sqrt((item.statArea || 400) / Math.PI) * 0.35, 6)
	return makeCircleFeature(item.lon, item.lat, radius, {
		地名: item.name,
		name: item.name,
		city: item.city,
		population: item.population,
		density: item.density,
		statArea: item.statArea
	})
}

export function makeHazardFeature(item, typeId) {
	if (typeId === 'tourist') {
		return makePointFeature(item.lon, item.lat, { dangerName: item.name, ...item })
	}
	if (typeId === 'seawall') {
		const dx = 0.04
		return {
			type: 'Feature',
			geometry: {
				type: 'LineString',
				coordinates: [
					[item.lon - dx, item.lat],
					[item.lon, item.lat + 0.015],
					[item.lon + dx, item.lat]
				]
			},
			properties: { dangerName: item.name, ...item }
		}
	}
	return makeCircleFeature(item.lon, item.lat, Math.max(item.areaKm2 || 1.2, 1.2), {
		dangerName: item.name,
		...item
	})
}

/**
 * 评估等同步场景的受灾面兜底。
 * 优先：区县真实几何 / 场景 bbox；否则才退回示意圆（调查解译请用 resolveAffectedFeature）。
 */
export function makeAffectedFeature(district, scene) {
	if (district?.feature || scene?.bbox) {
		return buildAffectedArea(district?.feature || null, scene, scene?.bbox)
	}
	const center = district ? [district.lon, district.lat] : [119.45, 25.85]
	return makeCircleFeature(center[0] + 0.12, center[1], district ? 14 : 28, {
		name: '受灾范围',
		sceneName: scene?.name || '灾后遥感解译'
	})
}

export function makeOverlayFeatures(targetId) {
	if (targetId === 'population') {
		return hitCounties.map(item => {
			const county = mockCounties.find(row => row.name === item.name)
			return makeCircleFeature(county?.lon || 119.4, county?.lat || 26, 8, item)
		})
	}
	if (targetId === 'district') {
		return mockTowns.map(item => makeCircleFeature(item.lon, item.lat, 3.2, item))
	}
	const list = mockHazards[targetId] || []
	const names = new Set((mockOverlayResults[targetId]?.rows || []).map(item => item.name))
	return list.filter(item => names.has(item.name)).map(item => makeHazardFeature(item, targetId))
}

export function findMockCountyByName(name) {
	const keyword = (name || '').trim()
	const normalize = text => text.replace(/[市县区]$/g, '')
	return (
		mockCounties.find(item => item.name === keyword) ||
		mockCounties.find(item => item.name.includes(keyword) || keyword.includes(item.name)) ||
		mockCounties.find(item => item.city === keyword || normalize(item.name) === normalize(keyword))
	)
}

export function findMockCountyByCoord(lon, lat) {
	return nearestByCoord(mockCounties, lon, lat)
}

export function findMockTownByName(name) {
	const keyword = (name || '').trim()
	return (
		mockTowns.find(item => item.name === keyword) ||
		mockTowns.find(item => item.name.includes(keyword) || keyword.includes(item.name))
	)
}

export function findMockTownByCoord(lon, lat) {
	return nearestByCoord(mockTowns, lon, lat)
}

export function findMockHazardByCoord(typeId, lon, lat) {
	return nearestByCoord(mockHazards[typeId] || [], lon, lat)
}

function nearestByCoord(list, lon, lat) {
	if (!list.length) return null
	let best = list[0]
	let min = Infinity
	list.forEach(item => {
		const dist = (item.lon - lon) ** 2 + (item.lat - lat) ** 2
		if (dist < min) {
			min = dist
			best = item
		}
	})
	return best
}
