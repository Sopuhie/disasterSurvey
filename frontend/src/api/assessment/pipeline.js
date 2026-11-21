/**
 * 灾害评估三轨 API（/api/v1/assessment/**）
 */
import request from '@/utils/request.js'
import { unwrapExecuteResult } from '@/api/withMockFallback.js'
import { DisasterTypeEnum, toDisasterType } from '@/constants/disasterEnums.js'

const BASE = '/api/v1/assessment'

/** 前端评估类型短码 → DisasterTypeEnum */
export const ASSESS_TYPE_TO_DISASTER = Object.freeze({
	storm: DisasterTypeEnum.STORM_SURGE,
	wind: DisasterTypeEnum.SEA_WIND,
	wave: DisasterTypeEnum.SEA_WAVE
})

export function resolveDisasterType(typeId) {
	return ASSESS_TYPE_TO_DISASTER[typeId] || toDisasterType(typeId) || DisasterTypeEnum.STORM_SURGE
}

const postEvent = async (eventId, path, body = {}) => {
	const res = await request({
		url: `${BASE}/events/${eventId}${path}`,
		method: 'post',
		data: body || {}
	})
	return unwrapExecuteResult(res)
}

const get = async (url, params) => {
	const res = await request({ url, method: 'get', params })
	return unwrapExecuteResult(res)
}

// ---------- 风暴潮 ----------
export const stormIntegrate = (eventId, body) => postEvent(eventId, '/storm-surge/integrate', body)
export const stormEconomicLoss = (eventId, body) => postEvent(eventId, '/storm-surge/economic-loss', body)
export const stormComprehensive = (eventId, body) => postEvent(eventId, '/storm-surge/comprehensive', body)

// ---------- 海风 ----------
export const windDamage = (eventId, body) => postEvent(eventId, '/sea-wind/damage', body)
export const windBehavior = (eventId, body) => postEvent(eventId, '/sea-wind/behavior', body)
export const windComprehensive = (eventId, body) => postEvent(eventId, '/sea-wind/comprehensive', body)

// ---------- 海浪 ----------
export const waveDamage = (eventId, body) => postEvent(eventId, '/sea-wave/damage', body)
export const waveBehavior = (eventId, body) => postEvent(eventId, '/sea-wave/behavior', body)
export const waveComprehensive = (eventId, body) => postEvent(eventId, '/sea-wave/comprehensive', body)

// ---------- 记录与报告 ----------
export const listAssessmentRecords = (eventId, disasterType) =>
	get(`${BASE}/events/${eventId}/records`, {
		disasterType: disasterType ? resolveDisasterType(disasterType) : undefined
	})

export const generateAssessmentReport = (eventId, body = {}) =>
	postEvent(eventId, '/reports/generate', {
		...body,
		disasterType: resolveDisasterType(body.disasterType || body.typeId)
	})

export const listAssessmentReports = ({ disasterType, keyword } = {}) =>
	get(`${BASE}/reports`, {
		disasterType: disasterType ? resolveDisasterType(disasterType) : undefined,
		keyword: keyword || undefined
	})

export const getAssessmentReport = reportId => get(`${BASE}/reports/${reportId}`)

/** 按灾种调用综合评估 */
export async function runComprehensive(eventId, typeId, body = {}) {
	const code = resolveDisasterType(typeId)
	if (code === DisasterTypeEnum.SEA_WIND) return windComprehensive(eventId, body)
	if (code === DisasterTypeEnum.SEA_WAVE) return waveComprehensive(eventId, body)
	return stormComprehensive(eventId, body)
}
