import { defineStore } from 'pinia'
import { BaselineStatusEnum } from '@/constants/disasterEnums.js'
import {
	createDisasterEvent,
	getDisasterEvent,
	listDisasterEvents
} from '@/api/disasterEvent/index.js'
import { confirmSurveyBaseline, getSurveyBaseline } from '@/api/survey/pipeline.js'

const LS_EVENT = 'disaster_workflow_event_id_v1'

const readEventId = () => {
	try {
		return localStorage.getItem(LS_EVENT) || ''
	} catch (e) {
		return ''
	}
}

const writeEventId = id => {
	try {
		if (id) localStorage.setItem(LS_EVENT, id)
		else localStorage.removeItem(LS_EVENT)
	} catch (e) {
		/* ignore */
	}
}

/**
 * 灾情调查 ↔ 灾害评估 上下游工作流状态
 */
export const useDisasterWorkflowStore = defineStore('disasterWorkflow', {
	state: () => ({
		currentEventId: readEventId(),
		currentEvent: null,
		currentBaseline: null,
		loading: false
	}),
	getters: {
		hasEvent: state => !!state.currentEventId,
		isBaselineConfirmed: state =>
			String(state.currentBaseline?.status || '').toUpperCase() === BaselineStatusEnum.CONFIRMED,
		baselineStatus: state => state.currentBaseline?.status || ''
	},
	actions: {
		setEventId(eventId) {
			this.currentEventId = eventId || ''
			writeEventId(this.currentEventId)
		},
		clearWorkflow() {
			this.currentEventId = ''
			this.currentEvent = null
			this.currentBaseline = null
			writeEventId('')
		},
		async refreshEvent() {
			if (!this.currentEventId) {
				this.currentEvent = null
				return null
			}
			const detail = await getDisasterEvent(this.currentEventId)
			this.currentEvent = detail
			if (detail?.confirmedBaseline) {
				this.currentBaseline = detail.confirmedBaseline
			} else if (detail?.baseline) {
				this.currentBaseline = detail.baseline
			}
			return detail
		},
		async refreshBaseline() {
			if (!this.currentEventId) {
				this.currentBaseline = null
				return null
			}
			const baseline = await getSurveyBaseline(this.currentEventId)
			this.currentBaseline = baseline
			return baseline
		},
		/**
		 * 进入灾情调查时绑定或创建事件
		 */
		async ensureEvent(defaults = {}) {
			this.loading = true
			try {
				if (this.currentEventId) {
					try {
						await this.refreshEvent()
						if (this.currentEvent) {
							await this.refreshBaseline().catch(() => null)
							return this.currentEvent
						}
					} catch (e) {
						this.clearWorkflow()
					}
				}
				const created = await createDisasterEvent({
					eventName:
						defaults.eventName ||
						`${defaults.regionName || '沿海区县'}灾后调查-${new Date().toISOString().slice(0, 10)}`,
					disasterTypes: defaults.disasterTypes || 'STORM_SURGE,SEA_WIND,SEA_WAVE',
					regionCode: defaults.regionCode || '',
					regionName: defaults.regionName || defaults.districtName || '',
					status: 'ACTIVE'
				})
				this.setEventId(created?.id || created?.eventId)
				this.currentEvent = created
				this.currentBaseline = null
				return created
			} finally {
				this.loading = false
			}
		},
		async bindExistingEvent(eventId) {
			this.setEventId(eventId)
			await this.refreshEvent()
			await this.refreshBaseline().catch(() => null)
			return this.currentEvent
		},
		async listEvents(params = {}) {
			return listDisasterEvents(params)
		},
		async confirmBaseline(payload = {}) {
			if (!this.currentEventId) throw new Error('请先绑定灾害事件')
			const baseline = await confirmSurveyBaseline(this.currentEventId, payload)
			this.currentBaseline = baseline
			return baseline
		},
		applyBaselineFromRemote(baseline) {
			if (!baseline) return
			this.currentBaseline = baseline
		}
	}
})
