import request from '@/utils/request.js'
import { unwrapExecuteResult } from '@/api/withMockFallback.js'

const BASE = '/api/v1/disaster-events'

/**
 * 创建灾害事件
 */
export async function createDisasterEvent(payload = {}) {
	const res = await request({
		url: BASE,
		method: 'post',
		data: payload
	})
	return unwrapExecuteResult(res)
}

/**
 * 分页/条件查询事件列表
 */
export async function listDisasterEvents({ keyword, status, current, size } = {}) {
	const res = await request({
		url: BASE,
		method: 'get',
		params: {
			keyword: keyword || undefined,
			status: status || undefined,
			current: current || undefined,
			size: size || undefined
		}
	})
	return {
		list: unwrapExecuteResult(res) || [],
		pageInfo: res.pageInfo || null
	}
}

/**
 * 事件详情（含基线与评估进度）
 */
export async function getDisasterEvent(eventId) {
	const res = await request({
		url: `${BASE}/${eventId}`,
		method: 'get'
	})
	return unwrapExecuteResult(res)
}
