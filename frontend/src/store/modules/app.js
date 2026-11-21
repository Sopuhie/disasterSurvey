import { defineStore } from 'pinia'

const appTitle = localStorage.getItem('appTitle')
const curNavObj = localStorage.getItem('curNavObj')
const userInfo = JSON.parse(localStorage.getItem('userInfo'))
const switchObj = JSON.parse(localStorage.getItem('switchObj'))
const toolBarSelect = JSON.parse(localStorage.getItem('toolBarSelect'))
const token = localStorage.getItem('token')
const breadcrumb = JSON.parse(localStorage.getItem('breadcrumb'))

export const useAppStore = defineStore('app', {
	state: () => ({
		appTitle: appTitle || '灾后调查与评估',
		// 主题，目前有theme2
		appTheme: 'theme2',
		appThemeVal: 'blue',
		userInfo: userInfo || null,
		token: token || '',
		// 是否清空工具栏选中状态
		toolBarSelect: toolBarSelect || [],
		currentSystem: '',
		// 页面上所有显隐的组件
		switchObj: switchObj || {
			// 是否显示数据上传
			showUploadData: false,
			// 2维 / 3维
			is2D: '2d',
			// 属性查询
			identify: false,
			// 导入数据管理
			importData: true,
			// 图例
			legend: false,
			// 底图切换
			baseMap: false,
			// 左侧边栏显示/隐藏
			sidebarShow: true,
			// 左侧边栏切换
			sidebarSwitch: true,
			// 左侧边栏展开/收起
			sidebarFlag: false,
			// 在线制图
			thematicMap: false,
			// 工具栏位置410/72/84
			toolBarLeft: '410px',
			// 目录树已选中数量
			selectNum: 0
		},
		// 打开修改密码页面
		openForgotPassword: false,
		curNavObj: curNavObj || '基础数据服务',
		breadcrumb: breadcrumb || ['系统日志']
	}),
	actions: {
		setCurNavObj(v) {
			this.curNavObj = v
			localStorage.setItem('curNavObj', v)
		},
		setCurrentSystem(v) {
			this.currentSystem = v
			localStorage.setItem('currentSystem', v)
		},
		setAppTitle(v) {
			this.appTitle = v
			localStorage.setItem('appTitle', v)
		},
		setAppTheme(v) {
			this.appTheme = v
			localStorage.setItem('appTheme', v)
		},
		setAppThemeVal(v) {
			this.appThemeVal = v
			localStorage.setItem('appThemeVal', v)
		},
		setUserInfo(v) {
			this.userInfo = v
			localStorage.setItem('userInfo', JSON.stringify(v))
		},
		setToken(v) {
			this.token = v
			localStorage.setItem('token', v)
		},
		setSwitchObj(key, value) {
			this.switchObj[key] = value
			localStorage.setItem('switchObj', JSON.stringify(this.switchObj))
		},
		setToolBarSelect(type, value) {
			if (type === 'delAll') {
				this.toolBarSelect = []
			}
			if (type === 'add') {
				this.toolBarSelect = value
			} else {
				this.toolBarSelect.splice(value, 1)
			}
			localStorage.setItem('toolBarSelect', JSON.stringify(this.toolBarSelect))
		},
		setOpenForgotPassword(v) {
			this.openForgotPassword = v
		},
		setBreadcrumb(v) {
			this.breadcrumb = v
			localStorage.setItem('breadcrumb', JSON.stringify(v))
		}
	},
	getters: {}
})
