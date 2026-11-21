<template>
	<div class="app max-wh">
		<el-config-provider :locale="zhCn">
			<Theme2></Theme2>
			<div class="pageContainer flex">
				<div class="navBox noSelect" :class="{ isCollapse: appStore.switchObj.sidebarFlag }">
					<MenuListCom :menuList="menuList"></MenuListCom>
				</div>
				<router-view></router-view>
			</div>
		</el-config-provider>
	</div>
</template>

<script setup>
	import zhCn from 'element-plus/es/locale/lang/zh-cn'
	import Theme2 from '@/components/navs/theme2.vue'
	import MenuListCom from '@/components/menuList/index.vue'
	import { getQueryString, jumpToThirdPartyLogin } from '@/utils/index.js'
	import resMsg from '@/utils/msg.js'
	import { isSingleLogin } from '@/config'
	import { useRouteHistory } from '@/utils/routeHistory.js'
	import { authApi } from '@/api/auth/index.js'
	const api = authApi()
	import { useAppStore } from '@/store/modules/app.js'
	import { tryInitFeedback, destroyFeedback } from '@/utils/feedback.js'
	import {
		syncSsoPTypeFromUrl,
		applySsoLoginData,
		clearSsoCallbackQuery,
		redirectYjssoRetry
	} from '@/utils/sso.js'
	const appStore = useAppStore()

	const route = useRoute()
	const router = useRouter()
	const { addRoute } = useRouteHistory()

	/** 登录后初始化反馈挂件（右侧滑出）；登出销毁 */
	watch(
		() => appStore.token,
		token => {
			if (token) {
				tryInitFeedback()
			} else {
				destroyFeedback()
			}
		},
		{ immediate: true }
	)

	watch(
		() => route.path,
		() => {
			if (!appStore.token) {
				destroyFeedback()
			} else {
				tryInitFeedback()
			}
		}
	)

	const menuList = [
		{
			name: '基础数据服务',
			icon: 'provinceSystem',
			path: '/provinceSystem',
			children: []
		},
		{
			name: '灾情调查',
			icon: 'disasterSurvey',
			path: '/disasterSurvey',
			children: []
		},
		{
			name: '灾害评估',
			icon: 'disasterAssessment',
			path: '/disasterAssessment',
			children: []
		},
		{
			name: '运维管理',
			icon: 'management',
			path: '/management',
			children: [
				{ name: '系统配置', icon: '', path: '/management/configManagement', children: [] },
				{ name: '功能管理', icon: '', path: '/management/functionManagement', children: [] },
				{ name: '运维配置', icon: '', path: '/management/operationConfiguration', children: [] },
				{ name: '服务管理', icon: '', path: '/management/serviceManage', children: [] },
				{ name: '目录树管理', icon: '', path: '/management/catalogManage', children: [] },
				{
					name: '灾情调查数据',
					icon: '',
					path: '/management/survey',
					children: [
						{ name: '县级行政区', icon: '', path: '/management/surveyCounty', children: [] },
						{ name: '市级行政区', icon: '', path: '/management/surveyCity', children: [] },
						{ name: '乡镇行政区', icon: '', path: '/management/surveyTown', children: [] },
						{ name: '海堤数据', icon: '', path: '/management/surveySeawall', children: [] },
						{ name: '渔港数据', icon: '', path: '/management/surveyFishing', children: [] },
						{ name: '海水养殖区', icon: '', path: '/management/surveyMariculture', children: [] },
						{ name: '滨海旅游区', icon: '', path: '/management/surveyTourist', children: [] },
						{ name: '遥感影像场景', icon: '', path: '/management/surveyRsScene', children: [] },
						{ name: '灾情速报数据', icon: '', path: '/management/surveyReported', children: [] }
					]
				},
				{
					name: '灾害评估数据',
					icon: '',
					path: '/management/assess',
					children: [
						{ name: 'SAR遥感场景', icon: '', path: '/management/assessSar', children: [] },
						{ name: '台风过程', icon: '', path: '/management/assessTyphoon', children: [] },
						{ name: '土地利用单价', icon: '', path: '/management/assessLandPrice', children: [] },
						{ name: '县域经济数据', icon: '', path: '/management/assessCountyEconomy', children: [] },
						{ name: '海上结构物', icon: '', path: '/management/assessStructure', children: [] },
						{ name: '承灾体破坏样本', icon: '', path: '/management/assessDamage', children: [] },
						{ name: '淹没侵蚀分区', icon: '', path: '/management/assessImpactZone', children: [] },
						{ name: '漫堤分析数据', icon: '', path: '/management/assessOvertop', children: [] },
						{ name: '受灾整合台账', icon: '', path: '/management/assessIntegrate', children: [] },
						{ name: '整合数据来源', icon: '', path: '/management/assessReportSource', children: [] }
					]
				},
				{ name: '系统日志', icon: '', path: '/management/systemLog', children: [] },
				{ name: '服务日志', icon: '', path: '/management/serviceLog', children: [] },
				// { name: '附件管理', icon: '', path: '/management/annex', children: [] }
			]
		}
	]

	onMounted(() => {
		window.addEventListener('beforeunload', e => beforeunloadHandler(e))
		window.addEventListener('unload', e => unloadHandler(e))
		addRoute(route)
		syncSsoPTypeFromUrl()
		// 一级回调页由 YjSsoLogin 自行处理，避免与 App 内跳转冲突
		if (isSingleLogin && route.name !== 'yjSsoLogin') {
			login()
		}
	})

	const login = () => {
		const code = getQueryString('code')
		const st = getQueryString('st')
		const yjsso = getQueryString('yjsso')
		const token = localStorage.getItem('token')
		const userInfo = localStorage.getItem('userInfo')

		// 登录页带 yjsso=true：重新走一级授权
		if (route.name === 'Login' && yjsso === 'true' && !st && !code) {
			localStorage.setItem('pType', 'st')
			jumpToThirdPartyLogin()
			return
		}

		if (token) {
			if (!userInfo) {
				if (code) {
					whileLogin('code', code)
				} else if (st) {
					whileLogin('st', st)
				}
			} else if (code || st) {
				clearSsoCallbackQuery()
			}
		} else {
			if (code) {
				whileLogin('code', code)
			} else if (st) {
				whileLogin('st', st)
			} else if (route.name !== 'Login') {
				jumpToThirdPartyLogin()
			}
		}
	}
	const whileLogin = (type, code) => {
		localStorage.setItem('pType', type)
		if (type === 'code') {
			getCheckToken(code)
		} else if (type === 'st') {
			getCheckTokenOne(code)
		}
	}

	// 二级单点：用授权码换本地登录态
	const getCheckToken = code => {
		api
			.verifyLogin({ code })
			.then(res => {
				if (applySsoLoginData(res, appStore)) {
					setTimeout(() => clearSsoCallbackQuery(), 2 * 1000)
				}
			})
			.catch(err => {
				resMsg({ message: err?.data?.resultMsg, type: 'error', duration: 3000 })
				setTimeout(() => {
					localStorage.setItem('pType', 'code')
					jumpToThirdPartyLogin()
				}, 2000)
			})
	}
	// 一级登录：用 ticket 换本地登录态
	const getCheckTokenOne = ticket => {
		api
			.yjssoSsoLogin({ ticket })
			.then(res => {
				if (applySsoLoginData(res, appStore)) {
					clearSsoCallbackQuery()
					if (route.name === 'yjSsoLogin' || route.name === 'Login') {
						router.replace('/provinceSystem')
					}
				} else {
					resMsg({ message: res?.resultMsg || '一级单点登录失败', type: 'error', duration: 3000 })
					redirectYjssoRetry()
				}
			})
			.catch(err => {
				resMsg({ message: err?.data?.resultMsg, type: 'error', duration: 3000 })
				redirectYjssoRetry()
			})
	}

	watch(
		() => route.fullPath,
		() => {
			addRoute(route)
		}
	)

	let _beforeUnload_time = ''
	let _gap_time = ''

	const beforeunloadHandler = e => {
		_beforeUnload_time = new Date().getTime()
	}
	const unloadHandler = async e => {
		_gap_time = new Date().getTime() - _beforeUnload_time
		//判断是窗口关闭还是刷新
		if (_gap_time <= 5) {
			const username = appStore.userInfo?.username || 'system'
			await api.loginOut({ username })
		} else {
			console.log('刷新')
		}
	}

	onUnmounted(() => {
		window.removeEventListener('beforeunload', e => beforeunloadHandler(e))
		window.removeEventListener('unload', e => unloadHandler(e))
	})
</script>

<style scoped>
	.pageContainer {
		height: calc(100vh - 75px);
	}
	.navBox {
		position: absolute;
		top: 85px;
		left: 0;
		z-index: 100;
		width: 280px;
		height: calc(100% - 85px);
		border-radius: 0 8px 0 0;
		overflow: auto;
		background-color: white;
		&.isCollapse {
			width: 64px;
		}
	}
</style>
