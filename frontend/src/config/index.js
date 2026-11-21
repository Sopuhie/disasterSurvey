/**
 * 运行时配置
 * - public/config.json        环境开关：{ "env": "test" | "prod" }
 * - public/config.test.json   测试环境完整配置
 * - public/config.prod.json   部署环境完整配置
 * 部署后改 JSON 即可，无需重新打包；修改 prefix 后仍需重新打包（vite base）
 * - enableMockTools           运维台账是否显示 MOCK 工具（同步/恢复/来源角标）
 */

export let appEnv = 'test'
export let baseURL = ''
export let authURL = ''
export let tiandituVecTileUrl = ''
export let tiandituCiaTileUrl = ''
export let systemName = ''
export let localCoordSysName = ''
export let appTitle = ''
export let appTitleEn = ''
export let base = ''
export let app_id = ''
export let client_id = ''
export let client_secret = ''
export let one_base = ''
export let one_app_id = ''
export let one_client_id = ''
export let one_client_secret = ''
/** 中转系统识别用 systemCode，写入 state=systemCode_timestamp */
export let one_system_code = ''
/** 一级授权回调地址（中转模式应指向中转系统 /yjsso/ssoCode） */
export let one_redirect_uri = ''
export let isSingleLogin = false
export let isZwzt = false
export let prefix = ''
/** @type {Record<string, any>} */
export let feedbackOpen = {}
/**
 * 运维台账 MOCK 工具开关：同步 MOCK、恢复默认、数据来源角标。
 * 未配置时默认 false（生产更安全）；测试环境可在 config.test.json 设为 true。
 */
export let enableMockTools = false

let loaded = false

function applyConfig(data) {
	baseURL = data.baseURL
	authURL = data.authURL
	tiandituVecTileUrl = data.tiandituVecTileUrl
	tiandituCiaTileUrl = data.tiandituCiaTileUrl
	systemName = data.systemName
	localCoordSysName = data.localCoordSysName
	appTitle = data.appTitle
	appTitleEn = data.appTitleEn
	base = data.base
	app_id = data.app_id
	client_id = data.client_id
	client_secret = data.client_secret
	one_base = data.one_base
	one_app_id = data.one_app_id
	one_client_id = data.one_client_id
	one_client_secret = data.one_client_secret
	one_system_code = data.one_system_code || data.one_client_secret || ''
	one_redirect_uri = data.one_redirect_uri || ''
	isSingleLogin = data.isSingleLogin
	isZwzt = data.isZwzt
	prefix = data.prefix ?? ''
	feedbackOpen = data.feedbackOpen || {}
	enableMockTools = data.enableMockTools === true
}

async function fetchJson(url) {
	const res = await fetch(url, { cache: 'no-cache' })
	if (!res.ok) {
		throw new Error(`加载配置失败: ${url} (${res.status})`)
	}
	return res.json()
}

export async function loadAppConfig() {
	if (loaded) return
	// 保证以 / 结尾，避免 BASE_URL=/disaster 时拼成 /disasterconfig.json
	const basePath = (import.meta.env.BASE_URL || '/').replace(/\/?$/, '/')
	const switcher = await fetchJson(`${basePath}config.json`)

	// 兼容：若仍是旧版「整份配置」写在 config.json，直接使用
	if (switcher.baseURL) {
		appEnv = 'legacy'
		applyConfig(switcher)
		loaded = true
		return
	}

	const env = switcher.env === 'prod' ? 'prod' : 'test'
	appEnv = env
	const data = await fetchJson(`${basePath}config.${env}.json`)
	applyConfig(data)
	loaded = true
}
