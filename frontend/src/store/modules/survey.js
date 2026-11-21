import { defineStore } from 'pinia'
import { SURVEY_STEPS } from '@/views/survey/constants.js'

export const useSurveyStore = defineStore('survey', {
	state: () => ({
		/** 左侧工作台是否收起（工具栏需据此左移） */
		leftPanelCollapsed: false,
		currentStep: SURVEY_STEPS[0].id,
		districtQueryMode: 'name',
		districtKeyword: '连江县',
		townKeyword: '',
		queriedDistrict: null,
		queriedTown: null,
		selectedHazardType: 'seawall',
		hazardQueryMode: 'name',
		hazardKeyword: '',
		hazardCoordLon: '',
		hazardCoordLat: '',
		hazardList: [],
		selectedHazard: null,
		scenes: [],
		selectedSceneId: '',
		identifying: false,
		identifyProgress: 0,
		affectedFeature: null,
		affectedMeta: null,
		overlayTarget: 'population',
		overlaying: false,
		overlayResult: null,
		overlayResults: {},
		compareType: 'seawall',
		compareView: 'chart',
		compareRows: [],
		loadingHazards: false,
		/** 后端调查任务 ID（liteMode 落库；失败则为空走本地） */
		surveyTaskId: '',
		overlayTaskId: ''
	}),
	getters: {
		hasAffectedArea: state => !!state.affectedFeature,
		currentOverlayCache: state => state.overlayResults[state.overlayTarget] || null
	},
	actions: {
		setStep(step) {
			this.currentStep = step
		},
		setLeftPanelCollapsed(collapsed) {
			this.leftPanelCollapsed = !!collapsed
		},
		reset() {
			this.leftPanelCollapsed = false
			this.currentStep = SURVEY_STEPS[0].id
			this.districtQueryMode = 'name'
			this.districtKeyword = '连江县'
			this.townKeyword = ''
			this.queriedDistrict = null
			this.queriedTown = null
			this.selectedHazardType = 'seawall'
			this.hazardQueryMode = 'name'
			this.hazardKeyword = ''
			this.hazardCoordLon = ''
			this.hazardCoordLat = ''
			this.hazardList = []
			this.selectedHazard = null
			this.selectedSceneId = this.scenes[0]?.id || ''
			this.identifying = false
			this.identifyProgress = 0
			this.affectedFeature = null
			this.affectedMeta = null
			this.overlayTarget = 'population'
			this.overlaying = false
			this.overlayResult = null
			this.overlayResults = {}
			this.compareType = 'seawall'
			this.compareView = 'chart'
			this.compareRows = []
			this.surveyTaskId = ''
			this.overlayTaskId = ''
		}
	}
})
