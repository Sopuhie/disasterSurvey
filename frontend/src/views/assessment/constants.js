export const ASSESS_TYPES = [
	{
		id: 'storm',
		label: '风暴潮',
		path: '/disasterAssessment',
		steps: [
			{ id: 'integrate', label: '信息整合', index: 1, desc: '速报数据标准化入库' },
			{ id: 'loss', label: '经济损失', index: 2, desc: '区划定位、专题叠加与损失测算' },
			{ id: 'report', label: '综合评估', index: 3, desc: '综合因子与评估报告' }
		]
	},
	{
		id: 'wind',
		label: '海风',
		path: '/disasterAssessment',
		steps: [
			{ id: 'damage', label: '破坏性分析', index: 1, desc: 'SAR 海面风场破坏等级分析' },
			{ id: 'behavior', label: '行为分析', index: 2, desc: '台风过程监测与淹没估测' },
			{ id: 'report', label: '综合评估', index: 3, desc: '综合因子与评估报告' }
		]
	},
	{
		id: 'wave',
		label: '海浪',
		path: '/disasterAssessment',
		steps: [
			{ id: 'damage', label: '破坏性分析', index: 1, desc: 'SAR 波浪场破坏等级分析' },
			{ id: 'behavior', label: '行为分析', index: 2, desc: '波高波速与岸线侵蚀估测' },
			{ id: 'report', label: '综合评估', index: 3, desc: '综合因子与评估报告' }
		]
	}
]

export const ROUTE_TYPE_MAP = {
	storm: 'storm',
	economic: 'storm',
	wind: 'wind',
	seabreeze: 'wind',
	wave: 'wave',
	seawave: 'wave'
}

export const LAYER_NAMES = {
	district: 'assessDistrictLyr',
	affected: 'assessAffectedLyr',
	impact: 'assessImpactLyr',
	path: 'assessPathLyr',
	overtop: 'assessOvertopLyr',
	surveyAffected: 'assessSurveyAffectedLyr',
	surveyOverlay: 'assessSurveyOverlayLyr',
	rsImage: 'localRsImageLyr',
	themePrefix: 'assessTheme_'
}

/** 风暴潮经济损失步骤可叠加的专题图层（对应 OtherResources） */
export const THEME_LAYER_OPTIONS = [
	{ id: 'landuse', label: '土地利用', resourceName: '土地利用' },
	{ id: 'population', label: '人口情况', resourceName: '人口情况' },
	{ id: 'economy', label: '经济情况', resourceName: '经济情况' },
	{ id: 'planning', label: '详细规划', resourceName: '详细规划' }
]

export const IMPACT_TARGETS = [
	{ id: 'fishing', label: '渔港' },
	{ id: 'seawall', label: '海堤' },
	{ id: 'coastline', label: '海岸线' },
	{ id: 'mariculture', label: '海水养殖区' },
	{ id: 'tourist', label: '滨海旅游区' }
]

export const OVERTOP_STYLE = {
	fill: { color: 'rgba(245, 34, 45, 0.22)' },
	stroke: { color: '#F5222D', width: 3 },
	image: {
		radius: 7,
		fill: { color: '#F5222D' },
		stroke: { color: '#fff', width: 1 }
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

/** 调查基线受灾范围（评估同屏，略偏橙以区分评估淹没面） */
export const SURVEY_AFFECTED_STYLE = {
	fill: { color: 'rgba(250, 140, 22, 0.2)' },
	stroke: { color: '#FA8C16', width: 2, lineDash: [8, 6] }
}

export const SURVEY_OVERLAY_STYLE = {
	fill: { color: 'rgba(19, 194, 194, 0.25)' },
	stroke: { color: '#13C2C2', width: 2 },
	image: {
		radius: 6,
		fill: { color: '#13C2C2' },
		stroke: { color: '#fff', width: 1 }
	}
}

export const IMPACT_STYLE = {
	fill: { color: 'rgba(255, 159, 67, 0.35)' },
	stroke: { color: '#FF9F43', width: 3 },
	image: {
		radius: 8,
		fill: { color: '#FF9F43' },
		stroke: { color: '#fff', width: 1 }
	}
}

export const PATH_STYLE = {
	stroke: { color: '#722ED1', width: 3 },
	image: {
		radius: 6,
		fill: { color: '#722ED1' },
		stroke: { color: '#fff', width: 1 }
	}
}

export const LEVEL_MAP = {
	1: { label: 'Ⅰ级', text: '破坏性非常严重', color: '#F52530' },
	2: { label: 'Ⅱ级', text: '破坏性严重', color: '#FF9F43' },
	3: { label: 'Ⅲ级', text: '破坏性一般', color: '#F8AF20' },
	4: { label: 'Ⅳ级', text: '破坏性轻微', color: '#1890FF' }
}

export function getAssessType(id) {
	return ASSESS_TYPES.find(item => item.id === id) || ASSESS_TYPES[0]
}

export function resolveTypeFromRoute(id) {
	return ROUTE_TYPE_MAP[id] || 'storm'
}
