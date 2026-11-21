/**
 * 从已完成的调查/评估任务 RESULTDATA / REPORTDATA 回填步骤结果。
 * 远程不可用或无匹配时返回 null，由上层台账推算 / MOCK。
 */
import {
	DISASTER_TYPE_MAP,
	TARGET_ENTITY_MAP,
	remoteEvaluateTaskDetail,
	remoteEvaluateTaskQueryPage,
	remoteSurveyTaskDetail,
	remoteSurveyTaskQueryPage
} from '@/api/evaluateRemote.js'

export const parseJsonField = raw => {
	if (raw == null || raw === '') return null
	if (typeof raw === 'object') return raw
	try {
		return JSON.parse(raw)
	} catch (e) {
		return null
	}
}

const matchScene = (payload, sceneId) => {
	if (!sceneId) return true
	const sid = String(payload?.sceneId || '')
	return !sid || sid === String(sceneId)
}

/** 查最近完成的调查任务并解析 resultData */
export async function findLatestSurveyResult({ kind, sceneId = '', targetId = '' } = {}) {
	try {
		const { list } = await remoteSurveyTaskQueryPage({
			current: 1,
			size: 30,
			eq_status: '3',
			eq_sceneId: sceneId || undefined,
			eq_targetEntity: targetId ? TARGET_ENTITY_MAP[targetId] || targetId : undefined,
			adminFlag: '1'
		})
		const targetEntity = targetId ? TARGET_ENTITY_MAP[targetId] || targetId : ''
		for (const row of list || []) {
			if (targetEntity && row.targetEntity && row.targetEntity !== targetEntity) continue
			if (sceneId && row.sceneId && String(row.sceneId) !== String(sceneId)) continue
			let payload = parseJsonField(row.resultData)
			if (!payload && row.id) {
				const detail = await remoteSurveyTaskDetail(row.id)
				payload = parseJsonField(detail?.resultData)
			}
			if (!payload || payload.kind !== kind) continue
			if (!matchScene(payload, sceneId) && !(row.sceneId && String(row.sceneId) === String(sceneId))) continue
			if (targetId && payload.targetId && payload.targetId !== targetId) continue
			return { taskId: row.id, payload, row }
		}
	} catch (e) {
		/* 静默 */
	}
	return null
}

/** 查最近完成的评估任务（可按 disasterType / kind） */
export async function findLatestEvaluateResult({ typeId = '', kind = '', sceneId = '' } = {}) {
	try {
		const disasterType = DISASTER_TYPE_MAP[typeId] || ''
		const { list } = await remoteEvaluateTaskQueryPage({
			current: 1,
			size: 30,
			eq_status: '3',
			eq_disasterType: disasterType || undefined,
			eq_sceneId: sceneId || undefined,
			adminFlag: '1'
		})
		for (const row of list || []) {
			if (disasterType && row.disasterType && String(row.disasterType) !== String(disasterType)) continue
			if (sceneId && row.sceneId && String(row.sceneId) !== String(sceneId)) continue
			let resultData = parseJsonField(row.resultData)
			let reportData = parseJsonField(row.reportData)
			if ((!resultData || !reportData) && row.id) {
				const detail = await remoteEvaluateTaskDetail(row.id)
				resultData = resultData || parseJsonField(detail?.resultData)
				reportData = reportData || parseJsonField(detail?.reportData)
			}
			if (kind && resultData?.kind && resultData.kind !== kind) continue
			if (sceneId && resultData?.sceneId && String(resultData.sceneId) !== String(sceneId) && row.sceneId && String(row.sceneId) !== String(sceneId)) continue
			if (!resultData && !reportData) continue
			return { taskId: row.id, resultData, reportData, row }
		}
	} catch (e) {
		/* 静默 */
	}
	return null
}
