import { defineStore } from 'pinia'
import { ASSESS_TYPES, THEME_LAYER_OPTIONS } from '@/views/assessment/constants.js'

const emptyThemeLayers = () =>
	Object.fromEntries(THEME_LAYER_OPTIONS.map(item => [item.id, false]))

export const useAssessmentStore = defineStore('assessment', {
	state: () => ({
		/** 左侧评估面板是否收起（工具栏需据此左移） */
		leftPanelCollapsed: false,
		currentType: 'storm',
		currentStep: 'integrate',
		integrateList: [],
		integrateForm: {
			sourceId: 'speed',
			reportTime: '',
			county: '',
			city: '',
			hazardType: '海堤',
			hazardName: '',
			damage: '',
			facility: '',
			level: '蓝色'
		},
		showIntegrateForm: false,
		districtQueryMode: 'name',
		districtKeyword: '连江县',
		coordLon: '',
		coordLat: '',
		queriedDistrict: null,
		selectedSceneId: '',
		scenes: [],
		themeLayers: emptyThemeLayers(),
		lossResult: null,
		lossLoading: false,
		damageTarget: 'all',
		damageRows: [],
		damageLoading: false,
		typhoonEvents: [],
		selectedTyphoonId: '',
		behaviorResult: null,
		behaviorLoading: false,
		overtoppingRows: [],
		overtoppingLoading: false,
		overtoppingKeyword: '',
		overtoppingCity: '',
		stormDetailVisible: false,
		stormDetailRow: null,
		reportFactors: {
			structure: 72,
			response: 68,
			prepare: 61,
			note: ''
		},
		reportResult: null,
		reportLoading: false,
		reportGenerating: false,
		reportVisible: false,
		archivedReports: [],
		/** 后端评估任务 ID / 服务端报告相对路径（liteMode） */
		evaluateTaskId: '',
		reportExportPath: ''
	}),
	getters: {
		typeConfig: state => ASSESS_TYPES.find(item => item.id === state.currentType) || ASSESS_TYPES[0],
		steps() {
			return this.typeConfig.steps
		}
	},
	actions: {
		setType(typeId) {
			this.currentType = typeId
			this.currentStep = this.steps[0].id
		},
		setStep(step) {
			this.currentStep = step
		},
		setLeftPanelCollapsed(collapsed) {
			this.leftPanelCollapsed = !!collapsed
		},
		reset() {
			this.currentStep = this.steps[0]?.id || 'integrate'
			this.districtKeyword = this.districtKeyword || '连江县'
			this.queriedDistrict = null
			this.themeLayers = emptyThemeLayers()
			this.lossResult = null
			this.damageRows = []
			this.behaviorResult = null
			this.overtoppingRows = []
			this.overtoppingKeyword = ''
			this.overtoppingCity = ''
			this.stormDetailVisible = false
			this.stormDetailRow = null
			this.reportResult = null
			this.reportLoading = false
			this.reportGenerating = false
			this.reportVisible = false
			this.showIntegrateForm = false
			this.evaluateTaskId = ''
			this.reportExportPath = ''
		}
	}
})
