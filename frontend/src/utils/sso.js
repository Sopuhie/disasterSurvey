import {
	isSingleLogin,
	isZwzt,
	base,
	client_id,
	one_base,
	one_client_id,
	one_system_code,
	one_redirect_uri,
	one_client_secret
} from '@/config'

/**
 * 从 search 或 hash 读取查询参数（兼容 /?x=1 与 /#/path?x=1）
 */
export function getQueryParam(name) {
	const url = new URL(document.location.toString())
	let val = url.searchParams.get(name)
	if (!val && window.location.hash.includes('?')) {
		const hashQuery = window.location.hash.split('?')[1] || ''
		val = new URLSearchParams(hashQuery).get(name)
	}
	return val
}

/** 根据 URL 同步登录类型：yjsso=true → 一级(st)，否则默认二级(code) */
export function syncSsoPTypeFromUrl() {
	const yjsso = getQueryParam('yjsso')
	if (yjsso === 'true') {
		localStorage.setItem('pType', 'st')
		return 'st'
	}
	if (!localStorage.getItem('pType')) {
		localStorage.setItem('pType', 'code')
	}
	return localStorage.getItem('pType')
}

/** 写入登录态（兼容政务中台 / 普通 tokenInfo 结构） */
export function applySsoLoginData(res, appStore) {
	if (!res || res.resultCode !== 'S') return false
	const data = res.data || {}
	if (data.ssoToken) {
		const { ssoToken, ssoUser } = data
		localStorage.setItem('tokenInfo', JSON.stringify(ssoToken))
		localStorage.setItem('token', ssoToken?.access_token || '')
		localStorage.setItem('userInfo', JSON.stringify(ssoUser || {}))
		appStore.setToken(ssoToken?.access_token)
		appStore.setUserInfo(ssoUser)
		return true
	}
	if (data.tokenInfo) {
		const { tokenInfo, userInfo } = data
		localStorage.setItem('tokenInfo', JSON.stringify(tokenInfo))
		localStorage.setItem('token', tokenInfo?.access_token || '')
		localStorage.setItem('userInfo', JSON.stringify(userInfo || {}))
		appStore.setToken(tokenInfo?.access_token)
		appStore.setUserInfo(userInfo)
		return true
	}
	return false
}

/** 清除地址栏中的授权回调参数 */
export function clearSsoCallbackQuery() {
	window.history.pushState({}, '', location.origin + location.pathname + location.hash.split('?')[0])
}

/**
 * 构建一级单点授权地址（中转模式：redirect_uri 指向中转/业务 ssoCode，state=systemCode_timestamp）
 */
export function buildYjssoAuthorizeUrl() {
	const systemCode = one_system_code || one_client_secret || 'disaster'
	const redirectUri = one_redirect_uri
	const state = `${systemCode}_${Date.now()}`
	return (
		`${one_base}/idp/authCenter/authenticate` +
		`?client_id=${encodeURIComponent(one_client_id)}` +
		`&response_type=code` +
		`&redirect_uri=${encodeURIComponent(redirectUri)}` +
		`&state=${encodeURIComponent(state)}`
	)
}

/** 跳转一级单点授权页 */
export function jumpToYjssoLogin() {
	if (!isSingleLogin) return false
	localStorage.setItem('pType', 'st')
	const href = buildYjssoAuthorizeUrl()
	setTimeout(() => {
		localStorage.clear()
		localStorage.setItem('pType', 'st')
		window.location.replace(href)
	}, 300)
	return false
}

/** 一级登录失败：回跳本系统并带 yjsso=true，再走一级授权 */
export function redirectYjssoRetry() {
	localStorage.setItem('pType', 'st')
	const baseUrl = import.meta.env.BASE_URL || '/'
	const loginPath = `${baseUrl}login?yjsso=true`.replace(/\/{2,}/g, '/').replace(':/', '://')
	setTimeout(() => {
		window.location.replace(loginPath)
	}, 500)
}

/** 统一跳转第三方登录（按 pType 区分一/二级） */
export function jumpToThirdPartyLogin() {
	if (!isSingleLogin) return false
	const pType = localStorage.getItem('pType') || 'code'

	if (pType === 'st') {
		return jumpToYjssoLogin()
	}

	let redirect_uri = `${location.origin}:443${location.pathname}&response_type=code`
	let locationHref = `${base}/oauth2/authorize?client_id=${client_id}&redirect_uri=${redirect_uri}`
	if (isZwzt && pType === 'code') {
		redirect_uri = `${location.origin}:443${location.pathname}&response_type=code&scope=all`
		locationHref = `${base}/oauth/authorize?client_id=${client_id}&redirect_uri=${redirect_uri}`
	}

	setTimeout(() => {
		localStorage.clear()
		localStorage.setItem('pType', 'code')
		window.location.replace(locationHref)
	}, 1000)
	return false
}

/** 判断请求是否属于一级单点接口 */
export function isYjssoRequestUrl(url = '') {
	return String(url).includes('/yjsso/')
}
