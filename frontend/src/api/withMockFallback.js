/**
 * Mock 适配层：仅当 VITE_USE_MOCK=true 时允许回落演示数据。
 * 关闭时远程失败/空数据必须抛错，禁止静默吞掉。
 */
import { isMockEnabled } from '@/constants/disasterEnums.js'
import { ElMessage } from 'element-plus'

export const isNonEmptyList = data => Array.isArray(data) && data.length > 0

export const isNonEmptyObject = data =>
	!!data && typeof data === 'object' && !Array.isArray(data) && Object.keys(data).length > 0

const defaultHasData = data => {
	if (Array.isArray(data)) return data.length > 0
	if (data == null) return false
	if (typeof data === 'object') return Object.keys(data).length > 0
	return true
}

/**
 * @param {() => Promise<any>} remoteFn 返回业务 data（已解包）
 * @param {() => Promise<any>|any} mockFn
 * @param {(data:any)=>boolean} [hasData]
 * @param {{ timeoutMs?: number, silent?: boolean, emptyMessage?: string }} [options]
 */
export async function withMockFallback(remoteFn, mockFn, hasData, options = {}) {
	const check = hasData || defaultHasData
	const timeoutMs = Number(options?.timeoutMs) || 0
	const allowMock = isMockEnabled()
	let remoteError = null

	try {
		const remote =
			timeoutMs > 0
				? await Promise.race([
						remoteFn(),
						new Promise((_, reject) => {
							setTimeout(() => reject(new Error(`remote timeout ${timeoutMs}ms`)), timeoutMs)
						})
					])
				: await remoteFn()
		if (check(remote)) return { data: remote, source: 'remote' }
		if (!allowMock) {
			const msg = options.emptyMessage || '接口返回空数据'
			if (!options.silent) ElMessage.error(msg)
			throw new Error(msg)
		}
	} catch (e) {
		remoteError = e
		if (!allowMock) {
			const msg = e?.message || '接口请求失败'
			if (!options.silent) ElMessage.error(msg)
			throw e instanceof Error ? e : new Error(msg)
		}
		if (import.meta.env?.DEV) {
			console.warn('[withMockFallback] remote failed, use mock (VITE_USE_MOCK=true)', e?.message || e)
		}
	}

	const mock = typeof mockFn === 'function' ? await mockFn() : mockFn
	if (!check(mock) && remoteError) {
		throw remoteError instanceof Error ? remoteError : new Error(String(remoteError))
	}
	return { data: mock, source: 'mock' }
}

/** 从 ExecuteResult 解包 data；失败抛错 */
export function unwrapExecuteResult(res) {
	if (!res) throw new Error('empty response')
	if (res.resultCode && res.resultCode !== 'S') {
		throw new Error(res.resultMsg || 'request failed')
	}
	return res.data
}
