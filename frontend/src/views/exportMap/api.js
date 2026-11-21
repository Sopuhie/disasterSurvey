// 专题制图

import request from '@/utils/request.js'

const prefix = '/web-disaster-map/web/disaster/map/mapping'

export function mappingApi() {
	return {
		// 专题出图模板资源保存或新增接口
		add: data => {
			return request({ url: prefix + '/template/add', method: 'post', data })
		},
		// 专题出图模板资源列表分页查询接口
		queryWithPage: data => {
			return request({ url: prefix + '/template/queryWithPage', method: 'post', data })
		},
		// 专题出图模板资源更新接口
		update: data => {
			return request({ url: prefix + '/template/update', method: 'post', data })
		},
		// 日志查询导出
		logExport: data => {
			return request({ url: prefix + '/log/export', method: 'post', data, responseType: 'blob' })
		},
		// 日志批量查询接口（可分页）
		logQuery: data => {
			return request({ url: prefix + '/log/query', method: 'post', data })
		},
		// 日志保存或新增接口
		logSave: data => {
			return request({ url: prefix + '/log/save', method: 'post', data })
		},
		// 标注图片或文件上传接口
		symbolUpload: data => {
			return request({ url: prefix + '/symbol/upload', method: 'post', data })
		},
		// 专题出图模板资源删除接口
		delete: params => {
			return request({ url: prefix + '/template/delete', method: 'get', params })
		},
		// 根据文件夹获取其里面的文件列表
		symbolQuery: params => {
			return request({ url: prefix + '/symbol/query', method: 'get', params })
		},
		// 根据符号文件相对路径删除目标文件
		symbolDelete: params => {
			return request({ url: prefix + '/symbol/delete', method: 'get', params })
		}
	}
}
