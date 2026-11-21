import { feedbackOpen, prefix } from '@/config'
import { getToken } from '@/utils/auth.js'
import { getFeedbackOpenSign } from '@/api/feedback/index.js'

const log = (...args) => {
	if (typeof console !== 'undefined') console.warn('[feedback]', ...args)
}

/**
 * 销毁反馈挂件（登出 / token 失效时调用，避免残留入口按钮与过期签名）
 */
export function destroyFeedback() {
	if (window.Feedback && typeof window.Feedback.destroy === 'function') {
		try {
			window.Feedback.destroy()
		} catch (e) {
			// 静默
		}
	}
	window.__feedbackInited = false
}

/**
 * 解析挂件 script / baseUrl（本地挂件优先）
 */
function resolveFrontUrls(data) {
	const withSlash = u => (u.endsWith('/') ? u : `${u}/`)
	if (feedbackOpen.useLocalWidget) {
		const base = withSlash(feedbackOpen.localFrontUrl || '/static/feedback/')
		// vite base 可能带 prefix
		const root = prefix ? `/${String(prefix).replace(/^\/|\/$/g, '')}` : ''
		const absBase = base.startsWith('http') ? base : `${root}${base}`.replace(/\/{2,}/g, '/')
		return {
			scriptUrl: `${absBase}feedback-widget.js`,
			baseUrl: absBase.endsWith('/') ? absBase : `${absBase}/`
		}
	}
	return {
		scriptUrl: data.frontUrl,
		baseUrl: data.baseUrl
	}
}

/**
 * 动态插入反馈挂件 script（平台不可达时 onerror 回退本地挂件）
 */
export function loadFeedbackScript(src, cb, onFail) {
	if (window.Feedback) {
		cb()
		return
	}
	const existing = document.querySelector(`script[data-feedback-widget="1"][src="${src}"]`)
	if (existing) {
		existing.addEventListener('load', cb, { once: true })
		existing.addEventListener('error', () => onFail && onFail(), { once: true })
		return
	}
	const script = document.createElement('script')
	script.async = true
	script.defer = true
	script.src = src
	script.dataset.feedbackWidget = '1'
	script.onload = cb
	script.onerror = () => {
		log('挂件脚本加载失败:', src)
		window.__feedbackInited = false
		script.remove()
		if (onFail) onFail()
	}
	document.body.appendChild(script)
}

function doInit(data, baseUrl) {
	if (!window.Feedback) {
		window.__feedbackInited = false
		log('window.Feedback 不存在，初始化中止')
		return
	}
	window.Feedback.init({
		sysCode: data.sysCode,
		sysName: data.sysName,
		user: data.user,
		sign: data.sign,
		timestamp: data.timestamp,
		baseUrl: baseUrl || data.baseUrl,
		apiBase: data.apiBase,
		refresh: done => {
			getFeedbackOpenSign()
				.then(d => {
					done({ sign: d.sign, timestamp: d.timestamp })
				})
				.catch(() => done(null))
		}
	})
}

/**
 * 登录后初始化反馈窗口（右侧滑出，幂等；失败尽量回退本地挂件）
 */
export function tryInitFeedback() {
	if (!feedbackOpen?.enabled) return
	if (window.__feedbackInited) return
	if (!getToken()) {
		log('无 token，跳过初始化')
		return
	}

	getFeedbackOpenSign()
		.then(data => {
			if (!data?.frontUrl && !feedbackOpen.useLocalWidget) {
				log('sign 结果缺少 frontUrl')
				return
			}
			window.__feedbackInited = true
			const { scriptUrl, baseUrl } = resolveFrontUrls(data)

			const tryLocalFallback = () => {
				if (feedbackOpen.useLocalWidget) {
					window.__feedbackInited = false
					return
				}
				const localBase = (feedbackOpen.localFrontUrl || '/static/feedback/').replace(/\/?$/, '/')
				const localSrc = `${localBase}feedback-widget.js`
				log('回退加载本地挂件', localSrc)
				window.__feedbackInited = true
				loadFeedbackScript(
					localSrc,
					() => doInit(data, localBase),
					() => {
						window.__feedbackInited = false
						log('本地挂件也加载失败')
					}
				)
			}

			loadFeedbackScript(scriptUrl, () => doInit(data, baseUrl), tryLocalFallback)
		})
		.catch(err => {
			log('获取签名失败', err)
		})
}
