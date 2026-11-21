/**
 * 灾情调查 4 步闭环 API（/api/v1/survey/**）
 */
import request from '@/utils/request.js'
import { unwrapExecuteResult } from '@/api/withMockFallback.js'
import { toExposureType } from '@/constants/disasterEnums.js'

const BASE = '/api/v1/survey'

export async function queryRegionPopulation(params = {}) {
	const res = await request({
		url: `${BASE}/base/region-population`,
		method: 'get',
		params: {
			regionCode: params.regionCode || undefined,
			regionName: params.regionName || params.name || undefined,
			lon: params.lon,
			lat: params.lat
		}
	})
	return unwrapExecuteResult(res)
}

export async function queryBaseExposures(params = {}) {
	const exposureType = params.exposureType ? toExposureType(params.exposureType) || params.exposureType : undefined
	const res = await request({
		url: `${BASE}/base/exposures`,
		method: 'get',
		params: {
			regionCode: params.regionCode || undefined,
			regionName: params.regionName || undefined,
			exposureType,
			keyword: params.keyword || undefined
		}
	})
	return unwrapExecuteResult(res)
}

export async function saveAffectedArea(eventId, body = {}) {
	const res = await request({
		url: `${BASE}/events/${eventId}/affected-area`,
		method: 'post',
		data: body
	})
	return unwrapExecuteResult(res)
}

export async function runOverlayAnalysis(eventId, body = {}) {
	const res = await request({
		url: `${BASE}/events/${eventId}/overlay-analysis`,
		method: 'post',
		data: body || {}
	})
	return unwrapExecuteResult(res)
}

export async function exportExposureImages(eventId, body = {}) {
	const res = await request({
		url: `${BASE}/events/${eventId}/exposure-images/export`,
		method: 'post',
		data: body || {}
	})
	return unwrapExecuteResult(res)
}

export async function compareQuickReport(eventId, body = {}) {
	const res = await request({
		url: `${BASE}/events/${eventId}/compare-quick-report`,
		method: 'post',
		data: body || {}
	})
	return unwrapExecuteResult(res)
}

export async function confirmSurveyBaseline(eventId, body = {}) {
	const res = await request({
		url: `${BASE}/events/${eventId}/baseline/confirm`,
		method: 'post',
		data: body || {}
	})
	return unwrapExecuteResult(res)
}

export async function getSurveyBaseline(eventId) {
	const res = await request({
		url: `${BASE}/events/${eventId}/baseline`,
		method: 'get'
	})
	return unwrapExecuteResult(res)
}

export async function listSurveyDossiers(eventId, exposureType) {
	const res = await request({
		url: `${BASE}/events/${eventId}/dossiers`,
		method: 'get',
		params: {
			exposureType: exposureType ? toExposureType(exposureType) || exposureType : undefined
		}
	})
	return unwrapExecuteResult(res) || []
}

export async function saveSurveyDossier(eventId, dossier = {}) {
	const res = await request({
		url: `${BASE}/events/${eventId}/dossiers`,
		method: 'post',
		data: {
			...dossier,
			exposureType: toExposureType(dossier.exposureType || dossier.hazardType) || dossier.exposureType
		}
	})
	return unwrapExecuteResult(res)
}
