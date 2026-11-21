/**
 * 调查 / 评估统一指标口径与计量单位（唯一文案源）
 * 1 km² = 1500 亩 = 0.15 万亩
 *
 * 约定：
 * - 表头/列名：用 SURVEY_LABELS（含单位）
 * - KPI 标题：用短名（不含单位），单位放在数值旁 <small>
 * - 调查灾害等级：一级—四级；评估影响等级：Ⅰ—Ⅳ级 + 破坏程度
 */
import { LEVEL_MAP } from '@/views/assessment/constants.js'

export const KM2_TO_WANMU = 0.15

export const km2ToWanMu = value => {
	const n = Number(value)
	if (!Number.isFinite(n)) return 0
	return Number((n * KM2_TO_WANMU).toFixed(2))
}

export const wanMuToKm2 = value => {
	const n = Number(value)
	if (!Number.isFinite(n)) return 0
	return Number((n / KM2_TO_WANMU).toFixed(2))
}

/** 潮位/高程：源数据常为 cm（>50 视为 cm），统一为 m */
export const toMeters = value => {
	const n = Number(value)
	if (!Number.isFinite(n)) return null
	if (Math.abs(n) > 50) return Number((n / 100).toFixed(2))
	return Number(n.toFixed(2))
}

/** 保护人口：源数据常为万人（小数或较小整数），统一为 人 */
export const toProtectPeople = value => {
	const n = Number(value)
	if (!Number.isFinite(n) || n <= 0) return 0
	if (n < 500) return Math.round(n * 10000)
	return Math.round(n)
}

export const DISASTER_LEVEL_CN = {
	1: '一级',
	2: '二级',
	3: '三级',
	4: '四级'
}

export const formatDisasterLevel = level => DISASTER_LEVEL_CN[Number(level)] || `${level || '-'}`

/** 评估影响等级：Ⅰ—Ⅳ级 */
export const formatImpactLevel = level => {
	const meta = LEVEL_MAP[Number(level)]
	if (!meta) return `${level ?? '-'}级`
	return `${meta.label}`
}

/** 评估影响等级 + 破坏程度 */
export const formatImpactLevelFull = level => {
	const meta = LEVEL_MAP[Number(level)]
	if (!meta) return `${level ?? '-'}级`
	return `${meta.label}（${meta.text}）`
}

/** 预警色（整合台账）→ 影响等级 1—4，便于与Ⅰ—Ⅳ对齐 */
export const warnColorToImpactLevel = color => {
	const map = { 红色: 1, 橙色: 2, 黄色: 3, 蓝色: 4 }
	return map[color] || null
}

export const formatWarnColorWithImpact = color => {
	const level = warnColorToImpactLevel(color)
	if (!level) return color || '-'
	return `${color}（对应${formatImpactLevel(level)}）`
}

export const SURVEY_LABELS = {
	monitorValue: '现状监测值(万亩)',
	planValue: '近期计划值(万亩)',
	monitorValueLength: '现状监测值(km)',
	planValueLength: '近期计划值(km)',
	affectedPopulation: '受灾人口(人)',
	economicLoss: '经济损失(万元)',
	areaKm2: '受灾面积(km²)',
	areaWanMu: '受灾面积(万亩)',
	disasterLevel: '灾害等级',
	density: '人口密度(人/km²)',
	protectPop: '保护人口(人)',
	warnTide: '警戒潮位(m)',
	tideHeight: '潮高(m)',
	crestElev: '堤顶高程(m)',
	wallElev: '防浪墙高程(m)',
	warnLevel: '预警色',
	impactLevel: '影响等级'
}

/** KPI 短名（单位单独展示，避免「(万亩) … 万亩」） */
export const SURVEY_KPI_NAMES = {
	monitorValue: '现状监测值',
	planValue: '近期计划值',
	affectedPopulation: '受灾人口',
	economicLoss: '经济损失',
	areaKm2: '受灾面积',
	areaWanMu: '受灾面积',
	protectPop: '保护人口',
	warnTide: '警戒潮位',
	tideHeight: '潮高',
	crestElev: '堤顶高程'
}

/** 按承灾体类型返回监测/计划值：表头带单位，KPI 短名 + unit */
export const monitorPlanLabels = (typeId = '') => {
	if (typeId === 'seawall') {
		return {
			monitor: SURVEY_KPI_NAMES.monitorValue,
			plan: SURVEY_KPI_NAMES.planValue,
			monitorColumn: SURVEY_LABELS.monitorValueLength,
			planColumn: SURVEY_LABELS.planValueLength,
			unit: 'km'
		}
	}
	return {
		monitor: SURVEY_KPI_NAMES.monitorValue,
		plan: SURVEY_KPI_NAMES.planValue,
		monitorColumn: SURVEY_LABELS.monitorValue,
		planColumn: SURVEY_LABELS.planValue,
		unit: '万亩'
	}
}
