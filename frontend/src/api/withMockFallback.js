/**
 * 真实接口优先；失败或空数据时回落 MOCK。
 */

export const isNonEmptyList = data => Array.isArray(data) && data.length > 0

export const isNonEmptyObject = data =>
	!!data && typeof data === 'object' && !Array.isArray(data) && Object.keys(data).length > 0

/**
 * @param {() => Promise<any>} remoteFn 返回业务 data（已解包）
 * @param {() => Promise<any>|any} mockFn
 * @param {(data:any)=>boolean} [hasData] 默认：数组非空或对象非空
 * @param {{ timeoutMs?: number }} [options] timeoutMs>0 时远程超时则回落 MOCK（避免市/县等空等 12s）
 */
export async function withMockFallback(remoteFn, mockFn, hasData, options = {}) {
	const check =
		hasData ||
		(data => {
			if (Array.isArray(data)) return data.length > 0
			if (data == null) return false
			if (typeof data === 'object') return Object.keys(data).length > 0
			return true
		})
	const timeoutMs = Number(options?.timeoutMs) || 0
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
	} catch (e) {
		// 静默回落
		if (import.meta.env?.DEV) {
			console.warn('[withMockFallback] remote failed, use mock', e?.message || e)
		}
	}
	const mock = typeof mockFn === 'function' ? await mockFn() : mockFn
	return { data: mock, source: 'mock' }
}

/** 从 ExecuteResult 解包 data；失败抛错（由 withMockFallback 捕获） */
export function unwrapExecuteResult(res) {
	if (!res) throw new Error('empty response')
	if (res.resultCode && res.resultCode !== 'S') {
		throw new Error(res.resultMsg || 'request failed')
	}
	return res.data
}
