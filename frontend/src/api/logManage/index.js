import request from '@/utils/request.js'

// --------------------- 日志管理 ---------------------
const prefix2 = '/web-disaster-common/web/disaster/common'
export function logApi() {
	return {
		// 日志批量查询接口（可分页）
		logQuery: data => {
			return request({ url: prefix2 + '/log/query', method: 'post', data })
		},
		// 日志保存或新增接口
		logSave: data => {
			return request({ url: prefix2 + '/log/save', method: 'post', data })
		},
		// 日志查询导出
		logExport: data => {
			return request({ url: prefix2 + '/log/export', method: 'post', data, responseType: 'blob' })
		}
	}
}
