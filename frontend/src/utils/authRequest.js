import axios from 'axios'
import qs from 'qs'
import resMsg from '@/utils/msg.js'
import { authURL, isSingleLogin, isZwzt, base, app_id } from '@/config'
import { getToken } from '@/utils/auth'
import { jumpToThirdPartyLogin } from '@/utils/index.js'
import { isYjssoRequestUrl, jumpToYjssoLogin, redirectYjssoRetry } from '@/utils/sso.js'
import { appApi } from '@/api/app/index.js'
const api = appApi()

const whitelist = ['/web-disaster-common/web/disaster/common/checkcode/generate']

const service = axios.create({
	baseURL: authURL
})
service.defaults.retry = 3
service.defaults.retryDelay = 1000

service.interceptors.request.use(
	config => {
		if (Object.prototype.toString.call(config.data) === '[object FormData]') {
			if (config.method === 'get') {
				config.paramsSerializer = function (params) {
					return qs.stringify(params, { arrayFormat: 'repeat' })
				}
			}
			config.headers['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8'
		} else {
			config.headers['Content-Type'] = 'application/json;charset=UTF-8'
		}
		const userInfo = JSON.parse(localStorage.getItem('userInfo')) || null
		if (userInfo) {
			config.headers['peopleId'] = userInfo?.id || ''
			config.headers['username'] = userInfo?.userName || 'system'
		}
		// config.headers['Authentication-Token'] = getToken()
		config.headers['access_token'] = getToken()
		config.headers['roles'] = 'rl0'
		return config
	},
	error => {
		return Promise.reject(error)
	}
)

// 白名单
const whiteList = [
	'/web-disaster-common/web/disaster/common/log/export',
	'/web-disaster-resource/web/disaster/resource/style/download',
	'/web-disaster-map/web/disaster/map/queryStatistics/export',
	'/web-disaster-map/web/disaster/map/mapping/log/export'
]

service.interceptors.response.use(
	response => {
		const res = response.data
		const resConfig = response.config
		const resUrl = response.config.url
		// console.log('resUrl:', resUrl)

		if (whiteList.includes(resUrl)) {
			return {
				code: 200,
				data: res,
				resultCode: 'S',
				resultMsg: '操作成功'
			}
		} else if (response.status === 200 || response.status === 201) {
			if (res.resultCode === 'S' || whitelist.includes(resConfig.url.split('?')[0])) {
				return Promise.resolve(res)
			} else {
				resMsg({
					message: res.resultMsg,
					type: 'error',
					duration: 3000
				})
				return Promise.reject(response)
			}
		} else {
			return Promise.reject(response)
		}
	},
	async error => {
		if (0 === error.request.status) {
			resMsg({
				message: '请检查网络，或联系管理员',
				type: 'error',
				duration: 3000
			})
		}
		if (401 === error.response.status) {
			// token失效
			if (error.response.data.resultCode === 'R') {
				let refToken
				if (getToken()) {
					// 刷新token
					refToken = await api.verifyRefreshToken({
						refreshToken: getToken()
					})

					// 刷新token成功，保存token并更新
					if (refToken?.resultCode === 'S') {
						useApp.setToken(refToken.data.access_token)
					}
				}
			}
			if (isSingleLogin) {
				resMsg({ message: '身份过期，即将自动返回登录', type: 'error', duration: 3000 })
				const pType = localStorage.getItem('pType')
				const reqUrl = error.config?.url || ''
				// 一级单点失败：回跳 /login?yjsso=true，避免误入二级授权
				if (pType === 'st' || isYjssoRequestUrl(reqUrl)) {
					if (isYjssoRequestUrl(reqUrl)) {
						redirectYjssoRetry()
					} else {
						jumpToYjssoLogin()
					}
				} else if (isZwzt) {
					const redirect_uri = `${location.origin + ':443'}${location.pathname}&response_type=code&scope=all`
					const locationHref =
						base + `/notice/logout?access_token=${getToken()}&app_id=${app_id}&redirect_uri=${redirect_uri}`
					setTimeout(() => {
						localStorage.clear()
						localStorage.setItem('pType', 'code')
						window.location.replace(locationHref)
					}, 1000)
				} else {
					api.verifyLogout({ accessToken: getToken() })
					setTimeout(() => {
						jumpToThirdPartyLogin()
					}, 2000)
				}
			}
		} else if (500 === error.response.status) {
			resMsg({ message: '服务器错误', type: 'error', duration: 3000 })
		} else if (504 === error.response.status) {
			resMsg({ message: '请求超时', type: 'error', duration: 3000 })
		} else {
			return Promise.reject(error)
		}
	}
)
export default service
