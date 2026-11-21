/**
 * 灾情调查任务 / 灾害评估任务 / 报告导出 远程接口
 * Mock 回落由 withMockFallback + VITE_USE_MOCK 统一控制
 */
import request from '@/utils/request.js'
import { unwrapExecuteResult } from '@/api/withMockFallback.js'
import { DisasterTypeEnum, ExposureEntityType, toDisasterType, toExposureType } from '@/constants/disasterEnums.js'

const SURVEY_TASK = '/web-disaster-evaluate/web/disaster/evaluate/survey/task'
const EVAL_TASK = '/web-disaster-evaluate/web/disaster/evaluate/task'
const REPORT = '/web-disaster-evaluate/web/disaster/evaluate/report'
const EXPOSURE = '/web-disaster-evaluate/web/disaster/evaluate/exposure'

const quiet = { hideErrorMsg: true, timeout: 20000 }

const loginName = () => {
	try {
		const user = JSON.parse(localStorage.getItem('userInfo') || 'null')
		return user?.userName || user?.username || user?.name || 'system'
	} catch (e) {
		return 'system'
	}
}

/** 前端叠置目标 → 后端 targetEntity（兼容旧 StepCompute） */
export const TARGET_ENTITY_MAP = {
	population: 'Pop',
	district: 'Xzq',
	seawall: ExposureEntityType.SEAWALL,
	fishing: ExposureEntityType.FISHING_PORT,
	mariculture: ExposureEntityType.MARICULTURE,
	tourist: ExposureEntityType.TOURIST_AREA,
	SEAWALL: ExposureEntityType.SEAWALL,
	FISHING_PORT: ExposureEntityType.FISHING_PORT,
	MARICULTURE: ExposureEntityType.MARICULTURE,
	TOURIST_AREA: ExposureEntityType.TOURIST_AREA
}

export const DISASTER_TYPE_MAP = {
	storm: '1',
	wind: '2',
	wave: '3',
	[DisasterTypeEnum.STORM_SURGE]: '1',
	[DisasterTypeEnum.SEA_WIND]: '2',
	[DisasterTypeEnum.SEA_WAVE]: '3'
}

export function resolveTargetEntity(targetId) {
	if (!targetId) return 'Pop'
	if (TARGET_ENTITY_MAP[targetId]) return TARGET_ENTITY_MAP[targetId]
	const code = toExposureType(targetId)
	return code ? ExposureEntityType[code] : targetId
}

export function resolveDisasterTypeCode(typeId) {
	return toDisasterType(typeId) || typeId
}

// ---------- 调查任务 ----------
export async function remoteSurveyTaskBuild(payload = {}) {
	const res = await request({
		url: `${SURVEY_TASK}/build`,
		method: 'post',
		data: {
			name: payload.name,
			dataPath: payload.dataPath || '',
			targetEntity: payload.targetEntity || 'Pop',
			cjr: payload.cjr || loginName(),
			liteMode: payload.liteMode !== false,
			sceneId: payload.sceneId || '',
			resultData: typeof payload.resultData === 'string' ? payload.resultData : JSON.stringify(payload.resultData || {})
		},
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteSurveyTaskProgress(taskId) {
	const res = await request({
		url: `${SURVEY_TASK}/progress/query`,
		method: 'get',
		params: { taskId },
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteSurveyTaskDetail(id) {
	const res = await request({
		url: `${SURVEY_TASK}/queryDetail`,
		method: 'get',
		params: { id },
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteSurveyTaskQueryPage(query = {}) {
	const res = await request({
		url: `${SURVEY_TASK}/queryWithPage`,
		method: 'post',
		data: {
			current: query.current || 1,
			size: query.size || 20,
			adminFlag: query.adminFlag || '1',
			loginUser: query.loginUser || loginName(),
			...query
		},
		...quiet
	})
	return { list: unwrapExecuteResult(res) || [], pageInfo: res.pageInfo }
}

export async function remoteSurveyTaskDelete(id) {
	const res = await request({
		url: `${SURVEY_TASK}/delete`,
		method: 'get',
		params: { id },
		...quiet
	})
	return unwrapExecuteResult(res)
}

// ---------- 评估任务 ----------
export async function remoteEvaluateTaskBuild(payload = {}) {
	const res = await request({
		url: `${EVAL_TASK}/build`,
		method: 'post',
		data: {
			name: payload.name,
			disasterType: payload.disasterType || '1',
			reportData:
				typeof payload.reportData === 'string'
					? payload.reportData
					: JSON.stringify(payload.reportData || {}),
			targetEntity: payload.targetEntity || '',
			factorJgss: payload.factorJgss ?? payload.structure,
			factorYjsy: payload.factorYjsy ?? payload.response,
			factorBzzb: payload.factorBzzb ?? payload.prepare,
			recommend: payload.recommend || payload.note || '',
			cjr: payload.cjr || loginName(),
			liteMode: payload.liteMode !== false,
			sceneId: payload.sceneId || '',
			resultData: typeof payload.resultData === 'string' ? payload.resultData : JSON.stringify(payload.resultData || {})
		},
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteEvaluateTaskProgress(taskId) {
	const res = await request({
		url: `${EVAL_TASK}/progress/query`,
		method: 'get',
		params: { taskId },
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteEvaluateTaskDetail(id) {
	const res = await request({
		url: `${EVAL_TASK}/queryDetail`,
		method: 'get',
		params: { id },
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteEvaluateTaskQueryPage(query = {}) {
	const res = await request({
		url: `${EVAL_TASK}/queryWithPage`,
		method: 'post',
		data: {
			current: query.current || 1,
			size: query.size || 20,
			adminFlag: query.adminFlag || '1',
			loginUser: query.loginUser || loginName(),
			...query
		},
		...quiet
	})
	return { list: unwrapExecuteResult(res) || [], pageInfo: res.pageInfo }
}

export async function remoteEvaluateTaskDelete(id) {
	const res = await request({
		url: `${EVAL_TASK}/delete`,
		method: 'get',
		params: { id },
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteEvaluateReportExport(evaluateId, exposureType = '') {
	const res = await request({
		url: `${REPORT}/export`,
		method: 'get',
		params: { evaluateId, exposureType: exposureType || undefined },
		...quiet,
		timeout: 60000
	})
	return unwrapExecuteResult(res)
}

export async function remoteExposureTree(exposureType) {
	const res = await request({
		url: `${EXPOSURE}/tree`,
		method: 'get',
		params: { exposureType },
		...quiet
	})
	return unwrapExecuteResult(res) || []
}

const COMPUTE = '/web-disaster-evaluate/web/disaster/evaluate'

/** 服务端受灾识别计算 */
export async function remoteComputeIdentify(payload = {}) {
	const res = await request({
		url: `${COMPUTE}/survey/compute/identify`,
		method: 'post',
		data: {
			persist: payload.persist !== false,
			...payload
		},
		...quiet,
		timeout: 30000
	})
	return unwrapExecuteResult(res)
}

/** 服务端叠置计算 */
export async function remoteComputeOverlay(payload = {}) {
	const res = await request({
		url: `${COMPUTE}/survey/compute/overlay`,
		method: 'post',
		data: {
			persist: payload.persist !== false,
			targetEntity: payload.targetEntity || TARGET_ENTITY_MAP[payload.targetId] || payload.targetId,
			...payload
		},
		...quiet,
		timeout: 30000
	})
	return unwrapExecuteResult(res)
}

/** 服务端经济损失计算 */
export async function remoteComputeLoss(payload = {}) {
	const res = await request({
		url: `${COMPUTE}/task/compute/loss`,
		method: 'post',
		data: {
			persist: payload.persist !== false,
			disasterType: payload.disasterType || DISASTER_TYPE_MAP[payload.typeId] || '1',
			...payload
		},
		...quiet,
		timeout: 30000
	})
	return unwrapExecuteResult(res)
}

export async function remoteSurveyTaskUpdate(payload = {}) {
	const res = await request({
		url: `${SURVEY_TASK}/update`,
		method: 'post',
		data: payload,
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteEvaluateTaskUpdate(payload = {}) {
	const res = await request({
		url: `${EVAL_TASK}/update`,
		method: 'post',
		data: payload,
		...quiet
	})
	return unwrapExecuteResult(res)
}

/** 轮询进度，lite 任务通常立刻 100 */
export async function pollTaskProgress(fetchProgress, taskId, { timeout = 8000, interval = 400 } = {}) {
	const start = Date.now()
	while (Date.now() - start < timeout) {
		const progress = await fetchProgress(taskId)
		const pct = Number(progress?.overallProgress)
		if (pct >= 100) return progress
		await new Promise(r => setTimeout(r, interval))
	}
	return fetchProgress(taskId)
}
