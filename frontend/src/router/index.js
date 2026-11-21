import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import { isSingleLogin } from '@/config'
import { getQueryParam, buildYjssoAuthorizeUrl, syncSsoPTypeFromUrl } from '@/utils/sso.js'

const routes = [
	{
		path: '/',
		name: 'Home',
		redirect: '/provinceSystem',
		component: () => import('../views/Home/index.vue'),
		children: [
			{
				path: '/monitorAll',
				name: 'monitor-all',
				component: () => import('../components/sidebar/CompB.vue'),
				meta: {
					title: '全景监测'
				}
			},
			{
				path: '/build-enquiry',
				name: 'buildEnquiry',
				component: () => import('../views/buildEnquiry/index.vue'),
				meta: {
					title: '空间分析'
				}
			},
			{
				path: '/statistics-report',
				name: 'statisticsReport',
				component: () => import('../views/statisticsReport/index.vue'),
				meta: {
					title: '统计报表'
				}
			},
			{
				path: '/export-map',
				name: 'exportMap',
				component: () => import('../views/exportMap/index.vue'),
				meta: {
					title: '专题制图'
				}
			},
			{
				path: '/projection-transform',
				name: 'projectionTransform',
				component: () => import('../views/projectionTransform/index.vue'),
				meta: {
					title: '空间分析'
				}
			},
			{
				path: '/thematicMap',
				name: 'ThematicMap',
				component: () => import('../views/thematicMap/index.vue'),
				meta: {
					title: '专题图制作'
				}
			},
			{
				path: '/provinceSystem',
				name: 'ProvinceSystem',
				component: () => import('../views/provinceSystem/index.vue'),
				meta: {
					title: '基础数据服务'
				}
			},
			{
				path: '/survey',
				name: 'disasterSurvey',
				component: () => import('../views/survey/index.vue'),
				meta: {
					title: '灾情调查'
				}
			},
			{
				path: '/survey/:id',
				redirect: to => ({ path: '/survey', query: to.query })
			},
			{
				path: '/disasterSurvey',
				redirect: '/survey'
			},
			{
				path: '/disasterSurvey/:id',
				redirect: '/survey'
			},
			{
				path: '/assessment/storm-surge',
				name: 'disasterAssessment',
				component: () => import('../views/assessment/index.vue'),
				meta: {
					title: '灾害评估',
					assessType: 'storm'
				}
			},
			{
				path: '/assessment/sea-wind',
				name: 'assessmentSeaWind',
				component: () => import('../views/assessment/index.vue'),
				meta: {
					title: '海风评估',
					assessType: 'wind'
				}
			},
			{
				path: '/assessment/sea-wave',
				name: 'assessmentSeaWave',
				component: () => import('../views/assessment/index.vue'),
				meta: {
					title: '海浪评估',
					assessType: 'wave'
				}
			},
			{
				path: '/assessment/reports',
				name: 'assessmentReports',
				component: () => import('../views/assessment/index.vue'),
				meta: {
					title: '评估报告归档',
					assessType: 'storm',
					assessStep: 'report',
					reportsOnly: true
				}
			},
			{
				path: '/assessment',
				redirect: '/assessment/storm-surge'
			},
			{
				path: '/disasterAssessment',
				redirect: '/assessment/storm-surge'
			},
			{
				path: '/disasterAssessment/economic',
				redirect: '/assessment/storm-surge'
			},
			{
				path: '/disasterAssessment/seabreeze',
				redirect: '/assessment/sea-wind'
			},
			{
				path: '/disasterAssessment/seawave',
				redirect: '/assessment/sea-wave'
			},
			{
				path: '/disasterAssessment/:id',
				redirect: to => {
					const id = String(to.params.id || '')
					if (id === 'economic' || id === 'storm') return '/assessment/storm-surge'
					if (id === 'seabreeze' || id === 'wind') return '/assessment/sea-wind'
					if (id === 'seawave' || id === 'wave') return '/assessment/sea-wave'
					return '/assessment/storm-surge'
				}
			},
			{
				path: '/management',
				name: 'management',
				redirect: '/management/systemLog',
				component: () => import('../views/management/index.vue'),
				meta: {
					title: '运维管理'
				},
				children: [
					{
						path: 'systemLog',
						name: 'systemLog',
						component: () => import('../views/management/systemLog.vue'),
						meta: {
							title: '系统日志'
						}
					},
					{
						path: 'serviceLog',
						name: 'serviceLog',
						component: () => import('../views/management/serviceLog.vue'),
						meta: {
							title: '服务日志'
						}
					},
					{
						path: 'serviceManage',
						name: 'serviceManage',
						component: () => import('../views/service/serve.vue'),
						meta: {
							title: '服务管理'
						}
					},
					{
						path: 'catalogManage',
						name: 'catalogManage',
						component: () => import('../views/service/catalog.vue'),
						meta: {
							title: '目录树管理'
						}
					},
					{
						path: 'configManagement',
						name: 'configManagement',
						component: () => import('../views/management/configManage.vue'),
						meta: {
							title: '系统配置'
						}
					},
					{
						path: 'functionManagement',
						name: 'functionManagement',
						component: () => import('../views/management/fnManage.vue'),
						meta: {
							title: '功能管理'
						}
					},
					{
						path: 'operationConfiguration',
						name: 'operationConfiguration',
						component: () => import('../views/management/operationConfig.vue'),
						meta: {
							title: '运维配置'
						}
					},
					{
						path: 'annex',
						name: 'annex',
						component: () => import('../views/management/annex.vue'),
						meta: {
							title: '运维配置'
						}
					},
					{
						path: 'surveyCounty',
						name: 'surveyCounty',
						component: () => import('../views/management/surveyBase/CountyManage.vue'),
						meta: { title: '县级行政区', dataType: 'county' }
					},
					{
						path: 'surveyCity',
						name: 'surveyCity',
						component: () => import('../views/management/surveyBase/CityManage.vue'),
						meta: { title: '市级行政区', dataType: 'city' }
					},
					{
						path: 'surveyTown',
						name: 'surveyTown',
						component: () => import('../views/management/surveyBase/TownManage.vue'),
						meta: { title: '乡镇行政区', dataType: 'town' }
					},
					{
						path: 'surveySeawall',
						name: 'surveySeawall',
						component: () => import('../views/management/surveyBase/SeawallManage.vue'),
						meta: { title: '海堤数据', dataType: 'seawall' }
					},
					{
						path: 'surveyFishing',
						name: 'surveyFishing',
						component: () => import('../views/management/surveyBase/FishingManage.vue'),
						meta: { title: '渔港数据', dataType: 'fishing' }
					},
					{
						path: 'surveyMariculture',
						name: 'surveyMariculture',
						component: () => import('../views/management/surveyBase/MaricultureManage.vue'),
						meta: { title: '海水养殖区', dataType: 'mariculture' }
					},
					{
						path: 'surveyTourist',
						name: 'surveyTourist',
						component: () => import('../views/management/surveyBase/TouristManage.vue'),
						meta: { title: '滨海旅游区', dataType: 'tourist' }
					},
					{
						path: 'surveyRsScene',
						name: 'surveyRsScene',
						component: () => import('../views/management/surveyBase/RsSceneManage.vue'),
						meta: { title: '遥感影像场景', dataType: 'rsScene' }
					},
					{
						path: 'surveyReported',
						name: 'surveyReported',
						component: () => import('../views/management/surveyBase/ReportedManage.vue'),
						meta: { title: '灾情速报数据', dataType: 'reported' }
					},
					{
						path: 'assessSar',
						name: 'assessSar',
						component: () => import('../views/management/assessBase/SarManage.vue'),
						meta: { title: 'SAR遥感场景', dataType: 'assessSar' }
					},
					{
						path: 'assessTyphoon',
						name: 'assessTyphoon',
						component: () => import('../views/management/assessBase/TyphoonManage.vue'),
						meta: { title: '台风过程', dataType: 'assessTyphoon' }
					},
					{
						path: 'assessLandPrice',
						name: 'assessLandPrice',
						component: () => import('../views/management/assessBase/LandPriceManage.vue'),
						meta: { title: '土地利用单价', dataType: 'assessLandPrice' }
					},
					{
						path: 'assessCountyEconomy',
						name: 'assessCountyEconomy',
						component: () => import('../views/management/assessBase/CountyEconomyManage.vue'),
						meta: { title: '县域经济数据', dataType: 'assessCountyEconomy' }
					},
					{
						path: 'assessStructure',
						name: 'assessStructure',
						component: () => import('../views/management/assessBase/StructureManage.vue'),
						meta: { title: '海上结构物', dataType: 'assessStructure' }
					},
					{
						path: 'assessDamage',
						name: 'assessDamage',
						component: () => import('../views/management/assessBase/DamageManage.vue'),
						meta: { title: '承灾体破坏样本', dataType: 'assessDamage' }
					},
					{
						path: 'assessImpactZone',
						name: 'assessImpactZone',
						component: () => import('../views/management/assessBase/ImpactZoneManage.vue'),
						meta: { title: '淹没侵蚀分区', dataType: 'assessImpactZone' }
					},
					{
						path: 'assessOvertop',
						name: 'assessOvertop',
						component: () => import('../views/management/assessBase/OvertopManage.vue'),
						meta: { title: '漫堤分析数据', dataType: 'assessOvertop' }
					},
					{
						path: 'assessIntegrate',
						name: 'assessIntegrate',
						component: () => import('../views/management/assessBase/IntegrateManage.vue'),
						meta: { title: '受灾整合台账', dataType: 'assessIntegrate' }
					},
					{
						path: 'assessReportSource',
						name: 'assessReportSource',
						component: () => import('../views/management/assessBase/ReportSourceManage.vue'),
						meta: { title: '整合数据来源', dataType: 'assessReportSource' }
					}
				]
			}
		]
	},
	{
		path: '/login',
		name: 'Login',
		component: () => import('../views/login/index.vue'),
		meta: {
			title: '登录页'
		}
	},
	{
		path: '/yjSsoLogin',
		name: 'yjSsoLogin',
		component: () => import('../views/login/YjSsoLogin.vue'),
		meta: {
			title: '一级单点登录'
		}
	},
	{
		path: '/404',
		name: '/404',
		component: () => import('../views/404/index.vue'),
		meta: {
			title: '错误页'
		}
	},
	{
		path: '/:pathMatch(.*)',
		redirect: '/404'
	}
]

const router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
	routes
})

router.beforeEach((to, from, next) => {
	const baseTitle = '灾后调查与评估'
	if (to.meta.title) {
		document.title = `${baseTitle} - ${to.meta.title}`
	} else {
		document.title = baseTitle
	}

	// 一级单点：未登录且 URL 带 yjsso=true 时，优先跳转一级授权（须在二级逻辑之前）
	if (isSingleLogin) {
		syncSsoPTypeFromUrl()
		const yjssoParam = getQueryParam('yjsso') || to.query?.yjsso
		const token = getToken()
		if (!token && yjssoParam === 'true') {
			if (to.name === 'yjSsoLogin') {
				next()
				return
			}
			localStorage.setItem('pType', 'st')
			window.location.href = buildYjssoAuthorizeUrl()
			return
		}
	}

	next()
})

export default router
