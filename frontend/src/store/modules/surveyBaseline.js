import { defineStore } from 'pinia'
import { withMockFallback, isNonEmptyObject } from '@/api/withMockFallback.js'
import {
	remoteBaselineGet,
	remoteBaselineLatest,
	remoteBaselineList,
	remoteBaselineSave
} from '@/api/bizRemote.js'

const LS_KEY = 'survey_baseline_local_v1'

const readLocal = () => {
	try {
		return JSON.parse(localStorage.getItem(LS_KEY) || 'null')
	} catch (e) {
		return null
	}
}

const writeLocal = pkg => {
	try {
		localStorage.setItem(LS_KEY, JSON.stringify(pkg))
	} catch (e) {
		/* quota */
	}
}

/** 精简 GeoJSON，避免基线包过大 */
const slimFeature = feature => {
	if (!feature?.geometry) return null
	return {
		type: 'Feature',
		geometry: feature.geometry,
		properties: {
			name: feature.properties?.name || feature.properties?.dangerName || '',
			areaKm2: feature.properties?.areaKm2,
			lengthKm: feature.properties?.lengthKm
		}
	}
}

const slimOverlayPack = pack => {
	if (!pack) return null
	return {
		target: pack.target,
		totals: pack.totals || {},
		rows: (pack.rows || []).slice(0, 80),
		features: (pack.features || []).slice(0, 40).map(slimFeature).filter(Boolean)
	}
}

export const buildBaselinePackage = surveyState => {
	const overlayResults = {}
	Object.keys(surveyState.overlayResults || {}).forEach(key => {
		overlayResults[key] = slimOverlayPack(surveyState.overlayResults[key])
	})
	const hazard = surveyState.selectedHazard
	return {
		sceneId: surveyState.selectedSceneId || '',
		district: surveyState.queriedDistrict
			? {
					name: surveyState.queriedDistrict.name,
					city: surveyState.queriedDistrict.city,
					statArea: surveyState.queriedDistrict.statArea,
					density: surveyState.queriedDistrict.density
				}
			: null,
		surveyTaskId: surveyState.surveyTaskId || '',
		overlayTaskId: surveyState.overlayTaskId || '',
		affectedMeta: surveyState.affectedMeta || null,
		affectedFeature: slimFeature(surveyState.affectedFeature),
		overlayTarget: surveyState.overlayTarget || '',
		overlayResults,
		compareType: surveyState.compareType || '',
		compareRows: (surveyState.compareRows || []).slice(0, 40),
		selectedHazard: hazard
			? {
					id: hazard.id,
					name: hazard.name,
					type: hazard.type,
					hazardType: surveyState.selectedHazardType
				}
			: null,
		updatedAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
	}
}

export const useSurveyBaselineStore = defineStore('surveyBaseline', {
	state: () => ({
		package: readLocal(),
		source: readLocal() ? 'local' : '',
		loading: false
	}),
	getters: {
		hasBaseline: state => !!state.package?.sceneId && !!state.package?.affectedMeta,
		affectedAreaKm2: state => Number(state.package?.affectedMeta?.areaKm2) || 0,
		overlayTotals: state => {
			const pack = state.package?.overlayResults || {}
			const out = {}
			Object.keys(pack).forEach(k => {
				out[k] = pack[k]?.totals || null
			})
			return out
		}
	},
	actions: {
		async publishFromSurvey(surveyState) {
			const pkg = buildBaselinePackage(surveyState)
			if (!pkg.sceneId) return null
			this.package = pkg
			writeLocal(pkg)
			this.source = 'local'
			try {
				const { data, source } = await withMockFallback(
					() => remoteBaselineSave(pkg),
					() => pkg,
					d => isNonEmptyObject(d) && !!d.sceneId
				)
				if (data) {
					this.package = data
					writeLocal(data)
					this.source = source
				}
			} catch (e) {
				/* 本地已写 */
			}
			return this.package
		},

		async loadByScene(sceneId = '') {
			this.loading = true
			try {
				const { data, source } = await withMockFallback(
					async () => {
						if (sceneId) {
							const hit = await remoteBaselineGet(sceneId)
							if (hit) return hit
						}
						return remoteBaselineLatest()
					},
					() => {
						const local = readLocal()
						if (sceneId && local?.sceneId && local.sceneId !== sceneId) return null
						return local
					},
					d => isNonEmptyObject(d) && !!d.sceneId
				)
				if (data) {
					this.package = data
					writeLocal(data)
					this.source = source
				}
				return data
			} finally {
				this.loading = false
			}
		},

		async listBaselines(keyword = '') {
			const { data } = await withMockFallback(
				() => remoteBaselineList(keyword),
				() => {
					const local = readLocal()
					return local ? [local] : []
				},
				d => Array.isArray(d) && d.length > 0
			)
			return data || []
		},

		clear() {
			this.package = null
			this.source = ''
			try {
				localStorage.removeItem(LS_KEY)
			} catch (e) {
				/* ignore */
			}
		}
	}
})
