import { ExposureTypeEnum, ExposureTypeLabel, toExposureLegacyId, toExposureType } from '@/constants/disasterEnums.js'

export const SURVEY_STEPS = [
	{ id: 'query', label: '基础底数查询', index: 1, desc: '行政区划、人口密度与承灾体台账' },
	{ id: 'identify', label: '遥感范围识别', index: 2, desc: '灾后遥感影像受灾范围解译' },
	{ id: 'overlay', label: '叠置与影像导出', index: 3, desc: '人口/乡镇/四类承灾体叠置及影像导出' },
	{ id: 'compare', label: '对比核查与基线确认', index: 4, desc: '遥感 vs 速报对比并锁定调查基线' }
]

export const LAYER_NAMES = {
	district: 'surveyDistrictLyr',
	affected: 'surveyAffectedLyr',
	overlay: 'surveyOverlayLyr',
	rsImage: 'localRsImageLyr',
	seawall: 'coastProtectionLyr',
	fishing: 'fishPortLyr',
	mariculture: 'maricultureLyr',
	tourist: 'touristAreaLyr'
}

/** id 保留历史短码供图层兼容；exposureType 为前后端统一编码 */
export const HAZARD_TYPES = [
	{
		id: 'seawall',
		exposureType: ExposureTypeEnum.SEAWALL,
		label: ExposureTypeLabel[ExposureTypeEnum.SEAWALL],
		dataKey: '海岸防护工程',
		layerName: LAYER_NAMES.seawall,
		geomKind: 'line'
	},
	{
		id: 'fishing',
		exposureType: ExposureTypeEnum.FISHING_PORT,
		label: ExposureTypeLabel[ExposureTypeEnum.FISHING_PORT],
		dataKey: '渔港',
		layerName: LAYER_NAMES.fishing,
		geomKind: 'polygon'
	},
	{
		id: 'mariculture',
		exposureType: ExposureTypeEnum.MARICULTURE,
		label: ExposureTypeLabel[ExposureTypeEnum.MARICULTURE],
		dataKey: '海水养殖区',
		layerName: LAYER_NAMES.mariculture,
		geomKind: 'polygon'
	},
	{
		id: 'tourist',
		exposureType: ExposureTypeEnum.TOURIST_AREA,
		label: ExposureTypeLabel[ExposureTypeEnum.TOURIST_AREA],
		dataKey: '滨海旅游区',
		layerName: LAYER_NAMES.tourist,
		geomKind: 'polygon'
	}
]

export const OVERLAY_TARGETS = [
	{ id: 'population', label: '人口密度', kind: 'district' },
	{ id: 'district', label: '乡镇区划', kind: 'district' },
	...HAZARD_TYPES.map(item => ({ id: item.id, label: item.label, kind: 'hazard' }))
]

export const CITY_NAMES = ['福州市', '厦门市', '莆田市', '三明市', '泉州市', '漳州市', '南平市', '龙岩市', '宁德市']

export const HAZARD_STYLES = {
	seawall: {
		stroke: { color: 'rgba(24, 144, 255, 1)', width: 3 }
	},
	fishing: {
		fill: { color: 'rgba(204, 136, 0, 0.25)' },
		stroke: { color: 'rgba(204, 136, 0, 1)', width: 2 }
	},
	mariculture: {
		fill: { color: 'rgba(82, 196, 26, 0.25)' },
		stroke: { color: 'rgba(82, 196, 26, 1)', width: 2 }
	},
	tourist: {
		fill: { color: 'rgba(114, 46, 209, 0.25)' },
		stroke: { color: 'rgba(114, 46, 209, 1)', width: 2 },
		image: {
			radius: 7,
			fill: { color: 'rgba(114, 46, 209, 0.85)' },
			stroke: { color: '#fff', width: 1 }
		}
	}
}

export const DISTRICT_STYLE = {
	fill: { color: 'rgba(24, 144, 255, 0.16)' },
	stroke: { color: '#1890FF', width: 2 }
}

export const AFFECTED_STYLE = {
	fill: { color: 'rgba(245, 37, 48, 0.28)' },
	stroke: { color: '#F52530', width: 2 }
}

export const OVERLAY_STYLE = {
	fill: { color: 'rgba(255, 159, 67, 0.35)' },
	stroke: { color: '#FF9F43', width: 3 },
	image: {
		radius: 8,
		fill: { color: '#FF9F43' },
		stroke: { color: '#fff', width: 1 }
	}
}

export function getHazardType(id) {
	const legacy = toExposureLegacyId(id)
	return (
		HAZARD_TYPES.find(item => item.id === id || item.id === legacy || item.exposureType === id) ||
		HAZARD_TYPES.find(item => item.exposureType === toExposureType(id))
	)
}

export function getOverlayTarget(id) {
	const legacy = toExposureLegacyId(id)
	return OVERLAY_TARGETS.find(item => item.id === id || item.id === legacy)
}

export function getHazardExposureType(id) {
	return getHazardType(id)?.exposureType || toExposureType(id)
}
