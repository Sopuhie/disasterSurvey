/**
 * 评估报告归档台账（localStorage）
 */
const STORAGE_KEY = 'assessment_report_archive_v1'

const clone = data => JSON.parse(JSON.stringify(data))

const readAll = () => {
	try {
		const raw = localStorage.getItem(STORAGE_KEY)
		if (raw) {
			const parsed = JSON.parse(raw)
			if (Array.isArray(parsed)) return parsed
		}
	} catch (e) {
		/* ignore */
	}
	return []
}

const writeAll = list => {
	localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

export const reportArchiveStore = {
	list() {
		return clone(readAll()).sort((a, b) => String(b.archivedAt || '').localeCompare(String(a.archivedAt || '')))
	},
	save(report, meta = {}) {
		const list = readAll()
		const row = {
			id: `AR_${Date.now()}`,
			reportNo: report?.reportNo || `RPT-${Date.now().toString().slice(-8)}`,
			title: report?.title || '综合评估报告',
			typeId: meta.typeId || report?.typeId || '',
			typeLabel: report?.typeLabel || '',
			district: report?.district || '',
			sceneName: report?.sceneName || '',
			grade: report?.grade || '',
			total: report?.total ?? '',
			archivedAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
			archivedBy: meta.archivedBy || '评估归档员',
			remark: meta.remark || '',
			snapshot: report
		}
		list.unshift(row)
		writeAll(list.slice(0, 50))
		return clone(row)
	},
	getById(id) {
		return clone(readAll().find(item => item.id === id) || null)
	}
}
