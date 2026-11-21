import request from '@/utils/request.js'
import { unwrapExecuteResult } from '@/api/withMockFallback.js'

const BIZ = '/web-disaster-evaluate/web/disaster/evaluate/bizdata'
const EXPOSURE = '/web-disaster-evaluate/web/disaster/evaluate/exposure'
const DOSSIER = '/web-disaster-evaluate/web/disaster/evaluate/dossier'
const BASELINE = '/web-disaster-evaluate/web/disaster/evaluate/baseline'

const quiet = { hideErrorMsg: true, timeout: 12000 }

const HAZARD_EXPOSURE_TYPE = {
	seawall: 'Dike',
	fishing: 'Fishery',
	mariculture: 'CultureArea',
	tourist: 'TouristArea'
}

export function getExposureTypeByHazard(type) {
	return HAZARD_EXPOSURE_TYPE[type] || ''
}

export async function remoteBizList(dataType, { keyword = '', extraType = '' } = {}) {
	const res = await request({
		url: `${BIZ}/list`,
		method: 'get',
		params: { dataType, keyword: keyword || undefined, extraType: extraType || undefined },
		...quiet
	})
	return unwrapExecuteResult(res) || []
}

export async function remoteBizQueryPage(dataType, params = {}) {
	const res = await request({
		url: `${BIZ}/queryWithPage`,
		method: 'post',
		data: {
			dataType,
			keyword: params.keyword || '',
			extraType: params.type || params.kind || params.hazardType || params.extraType || '',
			pageInfo: {
				pageIndex: Number(params.current) || 1,
				pageSize: Number(params.size) || 20
			}
		},
		...quiet
	})
	const records = unwrapExecuteResult(res) || []
	const pageInfo = res.pageInfo || {}
	return {
		records,
		total: pageInfo.recordCount ?? pageInfo.totalCount ?? pageInfo.total ?? records.length,
		current: Number(params.current) || 1,
		size: Number(params.size) || 20
	}
}

export async function remoteBizSave(dataType, data) {
	const res = await request({
		url: `${BIZ}/save`,
		method: 'post',
		data: { dataType, data },
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteBizUpdate(dataType, data) {
	const res = await request({
		url: `${BIZ}/update`,
		method: 'post',
		data: { dataType, data },
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteBizDelete(id) {
	const res = await request({
		url: `${BIZ}/delete`,
		method: 'get',
		params: { id },
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteBizGetById(id) {
	const res = await request({
		url: `${BIZ}/getById`,
		method: 'get',
		params: { id },
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteExposureList(hazardOrExposureType, keyword = '') {
	const exposureType = HAZARD_EXPOSURE_TYPE[hazardOrExposureType] || hazardOrExposureType
	const res = await request({
		url: `${EXPOSURE}/list`,
		method: 'get',
		params: { exposureType, keyword: keyword || undefined },
		...quiet
	})
	return unwrapExecuteResult(res) || []
}

export async function remoteArchiveReport(payload) {
	const res = await request({
		url: `${BIZ}/reportArchive/save`,
		method: 'post',
		data: payload,
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteListArchives() {
	const res = await request({
		url: `${BIZ}/reportArchive/list`,
		method: 'get',
		...quiet
	})
	return unwrapExecuteResult(res) || []
}

export async function remoteGetArchive(id) {
	const res = await request({
		url: `${BIZ}/reportArchive/getById`,
		method: 'get',
		params: { id },
		...quiet
	})
	return unwrapExecuteResult(res)
}

/**
 * 承灾体：合并 DB_DANGER 暴露库 + 运维业务台账（台账同 id 覆盖暴露库）。
 * 二者皆空时返回 []，由上层回落 MOCK。
 */
export async function remoteHazardOrBizList(type, keyword = '') {
	let exposure = []
	let biz = []
	if (HAZARD_EXPOSURE_TYPE[type]) {
		try {
			exposure = await remoteExposureList(type, keyword)
		} catch (e) {
			exposure = []
		}
	}
	try {
		biz = await remoteBizList(type, { keyword })
	} catch (e) {
		biz = []
	}
	if (!exposure.length && !biz.length) return []
	const map = new Map()
	;(exposure || []).forEach(row => {
		if (row?.id != null) map.set(String(row.id), { ...row, _source: 'exposure' })
	})
	;(biz || []).forEach(row => {
		if (row?.id == null) return
		const key = String(row.id)
		map.set(key, { ...(map.get(key) || {}), ...row, _source: 'biz' })
	})
	return Array.from(map.values())
}

/** ---------- 调查对象档案（现场资料/附件/核对归档） ---------- */

export async function remoteDossierGet(hazardType, hazardName) {
	const res = await request({
		url: `${DOSSIER}/get`,
		method: 'get',
		params: { hazardType, hazardName },
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteDossierSave(dossier) {
	const res = await request({
		url: `${DOSSIER}/save`,
		method: 'post',
		data: dossier,
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteDossierListArchived(keyword = '') {
	const res = await request({
		url: `${DOSSIER}/listArchived`,
		method: 'get',
		params: { keyword: keyword || undefined },
		...quiet
	})
	return unwrapExecuteResult(res) || []
}

/**
 * 上传现场资料 / 附件
 * @param {FormData} formData 需含 file、kind、hazardType、hazardName，以及可选元数据字段
 */
export async function remoteDossierUpload(formData) {
	const res = await request({
		url: `${DOSSIER}/upload`,
		method: 'post',
		data: formData,
		headers: { 'Content-Type': 'multipart/form-data' },
		timeout: 120000,
		...quiet
	})
	return unwrapExecuteResult(res)
}

/** 下载档案文件为 Blob */
export async function remoteDossierDownload(filePath) {
	const res = await request({
		url: `${DOSSIER}/download`,
		method: 'get',
		params: { filePath },
		responseType: 'blob',
		...quiet
	})
	// blob 接口经白名单外时返回 ExecuteResult；download 直接返回二进制
	if (res instanceof Blob) return res
	if (res?.data instanceof Blob) return res.data
	return res
}

/** 删除资料/附件条目 */
export async function remoteDossierRemoveItem({ hazardType, hazardName, kind, itemId }) {
	const res = await request({
		url: `${DOSSIER}/removeItem`,
		method: 'post',
		data: { hazardType, hazardName, kind, itemId },
		...quiet
	})
	return unwrapExecuteResult(res)
}

/** 批量写入业务台账（用于把 MOCK 推到服务器；大数据分片，避免超时） */
export async function remoteBizBatchSave(dataType, list = [], { chunkSize = 15 } = {}) {
	const rows = Array.isArray(list) ? list : []
	if (!rows.length) {
		return { dataType, success: 0, fail: 0, total: 0 }
	}
	const size = Math.max(1, Number(chunkSize) || 15)
	let success = 0
	let fail = 0
	for (let i = 0; i < rows.length; i += size) {
		const chunk = rows.slice(i, i + size)
		const res = await request({
			url: `${BIZ}/batchSave`,
			method: 'post',
			data: { dataType, list: chunk },
			...quiet,
			timeout: 120000
		})
		const summary = unwrapExecuteResult(res) || {}
		success += Number(summary.success) || 0
		fail += Number(summary.fail) || 0
	}
	return { dataType, success, fail, total: rows.length }
}

/** ---------- 调查基线包（供评估消费 / 情景复盘） ---------- */

export async function remoteBaselineGet(sceneId) {
	const res = await request({
		url: `${BASELINE}/get`,
		method: 'get',
		params: { sceneId },
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteBaselineLatest() {
	const res = await request({
		url: `${BASELINE}/latest`,
		method: 'get',
		...quiet
	})
	return unwrapExecuteResult(res)
}

export async function remoteBaselineSave(payload) {
	const res = await request({
		url: `${BASELINE}/save`,
		method: 'post',
		data: payload,
		...quiet,
		timeout: 30000
	})
	return unwrapExecuteResult(res)
}

export async function remoteBaselineList(keyword = '') {
	const res = await request({
		url: `${BASELINE}/list`,
		method: 'get',
		params: { keyword: keyword || undefined },
		...quiet
	})
	return unwrapExecuteResult(res) || []
}
