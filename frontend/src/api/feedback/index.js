import request from '@/utils/request.js'
import { feedbackOpen } from '@/config'
import { buildFeedbackOpenSignPayload } from '@/utils/openSign.js'
import { useAppStore } from '@/store/modules/app.js'

/**
 * 获取反馈窗口初始化信息与身份签名
 * useLocalSign=true 时本地签发；否则请求宿主后端 sign 接口
 */
export function getFeedbackOpenSign() {
	if (feedbackOpen.useLocalSign) {
		const appStore = useAppStore()
		let userInfo = appStore.userInfo
		if (!userInfo) {
			try {
				userInfo = JSON.parse(localStorage.getItem('userInfo'))
			} catch (e) {
				userInfo = null
			}
		}
		return buildFeedbackOpenSignPayload(feedbackOpen, userInfo)
	}
	return request({
		url: feedbackOpen.signApi,
		method: 'get'
	}).then(res => {
		// 兼容：直接返回 map，或包在 data / result 里
		if (res && res.frontUrl) return res
		if (res && res.data && res.data.frontUrl) return res.data
		if (res && res.result && res.result.frontUrl) return res.result
		return res
	})
}
