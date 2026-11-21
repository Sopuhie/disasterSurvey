/**
 * 前后端统一领域枚举（与后端 com.santech.core.framework.entity.enums.biz 保持一致）
 */

export const DisasterTypeEnum = Object.freeze({
	STORM_SURGE: 'STORM_SURGE',
	SEA_WIND: 'SEA_WIND',
	SEA_WAVE: 'SEA_WAVE'
})

export const DisasterTypeLabel = Object.freeze({
	[DisasterTypeEnum.STORM_SURGE]: '风暴潮',
	[DisasterTypeEnum.SEA_WIND]: '海风',
	[DisasterTypeEnum.SEA_WAVE]: '海浪'
})

export const ExposureTypeEnum = Object.freeze({
	SEAWALL: 'SEAWALL',
	FISHING_PORT: 'FISHING_PORT',
	MARICULTURE: 'MARICULTURE',
	TOURIST_AREA: 'TOURIST_AREA'
})

export const ExposureTypeLabel = Object.freeze({
	[ExposureTypeEnum.SEAWALL]: '海堤/海岸防护工程',
	[ExposureTypeEnum.FISHING_PORT]: '渔港',
	[ExposureTypeEnum.MARICULTURE]: '海水养殖区',
	[ExposureTypeEnum.TOURIST_AREA]: '滨海旅游区'
})

/** 标准编码 → 历史前端短 id（图层/路由兼容） */
export const ExposureLegacyAlias = Object.freeze({
	[ExposureTypeEnum.SEAWALL]: 'seawall',
	[ExposureTypeEnum.FISHING_PORT]: 'fishing',
	[ExposureTypeEnum.MARICULTURE]: 'mariculture',
	[ExposureTypeEnum.TOURIST_AREA]: 'tourist'
})

/** 标准编码 → 后端实体类型 */
export const ExposureEntityType = Object.freeze({
	[ExposureTypeEnum.SEAWALL]: 'Dike',
	[ExposureTypeEnum.FISHING_PORT]: 'Fishery',
	[ExposureTypeEnum.MARICULTURE]: 'CultureArea',
	[ExposureTypeEnum.TOURIST_AREA]: 'TouristArea'
})

export const BaselineStatusEnum = Object.freeze({
	DRAFT: 'DRAFT',
	CONFIRMED: 'CONFIRMED',
	ARCHIVED: 'ARCHIVED'
})

export const BaselineStatusLabel = Object.freeze({
	[BaselineStatusEnum.DRAFT]: '草稿',
	[BaselineStatusEnum.CONFIRMED]: '已确认',
	[BaselineStatusEnum.ARCHIVED]: '已归档'
})

export const ComputeTaskStatusEnum = Object.freeze({
	CREATED: 'CREATED',
	RUNNING: 'RUNNING',
	SUCCEEDED: 'SUCCEEDED',
	FAILED: 'FAILED'
})

export const AssessmentSubTypeEnum = Object.freeze({
	STORM_INFO_INTEGRATION: 'STORM_INFO_INTEGRATION',
	STORM_ECONOMIC_LOSS: 'STORM_ECONOMIC_LOSS',
	STORM_COMPREHENSIVE: 'STORM_COMPREHENSIVE',
	WIND_DAMAGE_ANALYSIS: 'WIND_DAMAGE_ANALYSIS',
	WIND_BEHAVIOR_ANALYSIS: 'WIND_BEHAVIOR_ANALYSIS',
	WIND_COMPREHENSIVE: 'WIND_COMPREHENSIVE',
	WAVE_DAMAGE_ANALYSIS: 'WAVE_DAMAGE_ANALYSIS',
	WAVE_BEHAVIOR_ANALYSIS: 'WAVE_BEHAVIOR_ANALYSIS',
	WAVE_COMPREHENSIVE: 'WAVE_COMPREHENSIVE'
})

const EXPOSURE_ALIAS_TO_CODE = {
	seawall: ExposureTypeEnum.SEAWALL,
	dike: ExposureTypeEnum.SEAWALL,
	SEAWALL: ExposureTypeEnum.SEAWALL,
	Dike: ExposureTypeEnum.SEAWALL,
	fishing: ExposureTypeEnum.FISHING_PORT,
	fishery: ExposureTypeEnum.FISHING_PORT,
	FISHING_PORT: ExposureTypeEnum.FISHING_PORT,
	Fishery: ExposureTypeEnum.FISHING_PORT,
	mariculture: ExposureTypeEnum.MARICULTURE,
	CultureArea: ExposureTypeEnum.MARICULTURE,
	MARICULTURE: ExposureTypeEnum.MARICULTURE,
	tourist: ExposureTypeEnum.TOURIST_AREA,
	TouristArea: ExposureTypeEnum.TOURIST_AREA,
	TOURIST_AREA: ExposureTypeEnum.TOURIST_AREA
}

const DISASTER_ALIAS_TO_CODE = {
	storm: DisasterTypeEnum.STORM_SURGE,
	economic: DisasterTypeEnum.STORM_SURGE,
	'storm-surge': DisasterTypeEnum.STORM_SURGE,
	STORM_SURGE: DisasterTypeEnum.STORM_SURGE,
	'1': DisasterTypeEnum.STORM_SURGE,
	wind: DisasterTypeEnum.SEA_WIND,
	seabreeze: DisasterTypeEnum.SEA_WIND,
	'sea-wind': DisasterTypeEnum.SEA_WIND,
	SEA_WIND: DisasterTypeEnum.SEA_WIND,
	'2': DisasterTypeEnum.SEA_WIND,
	wave: DisasterTypeEnum.SEA_WAVE,
	seawave: DisasterTypeEnum.SEA_WAVE,
	'sea-wave': DisasterTypeEnum.SEA_WAVE,
	SEA_WAVE: DisasterTypeEnum.SEA_WAVE,
	'3': DisasterTypeEnum.SEA_WAVE
}

/** 任意别名 → ExposureTypeEnum 标准编码 */
export function toExposureType(raw) {
	if (!raw) return null
	return EXPOSURE_ALIAS_TO_CODE[raw] || EXPOSURE_ALIAS_TO_CODE[String(raw)] || null
}

/** 标准编码 → 历史前端短 id */
export function toExposureLegacyId(codeOrAlias) {
	const code = toExposureType(codeOrAlias) || codeOrAlias
	return ExposureLegacyAlias[code] || codeOrAlias
}

/** 任意别名 → DisasterTypeEnum 标准编码 */
export function toDisasterType(raw) {
	if (!raw) return null
	return DISASTER_ALIAS_TO_CODE[raw] || DISASTER_ALIAS_TO_CODE[String(raw)] || null
}

/** 是否启用 Mock（仅 VITE_USE_MOCK=true 时） */
export function isMockEnabled() {
	return String(import.meta.env?.VITE_USE_MOCK || '').toLowerCase() === 'true'
}
