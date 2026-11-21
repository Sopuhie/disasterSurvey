import request from '@/utils/request.js'
import { getToken } from '@/utils/auth'

// --------------------- 资源管理 ---------------------
const prefix1 = '/web-disaster-authentication/web/disaster/authentication'
const prefix2 = '/web-disaster-common/web/disaster/common'
const prefix3 = '/web-disaster-map/web/disaster'
const prefix4 = '/web-disaster-resource/web/disaster/resource'

export function serviceApi() {
	return {
		// --------------------- 文件上传 ---------------------
		// 单文件上传
		fileSingleUpload: data => {
			return request({
				url: prefix2 + '/file/singleUpload',
				method: 'post',
				data,
				headers: { 'Content-Type': 'multipart/form-data', 'Authentication-Token': getToken() }
			})
		},
		// 多文件上传
		fileMultipleUpload: data => {
			return request({
				url: prefix2 + '/file/multipleUpload',
				method: 'post',
				data
			})
		},
		// 先分片上传，再调检查，检查传完了，再调合并
		// 上传文件块
		fileChunkUpload: data => {
			return request({
				url: prefix2 + '/file/chunk/upload',
				method: 'post',
				data,
				headers: {
					'Authentication-Token': getToken()
				}
			})
		},
		// 检查文件块
		fileChunkCheck: params => {
			return request({
				url: prefix2 + '/file/chunk/check',
				method: 'get',
				params
				// headers: {
				// 	'Authentication-Token': getToken()
				// }
			})
		},
		// 合并文件
		fileChunkMerge: data => {
			return request({
				url: prefix2 + '/file/chunk/merge',
				method: 'post',
				data,
				headers: {
					'Authentication-Token': getToken()
				}
			})
		},

		// --------------------- 附件管理 ---------------------
		// 文件资源列表分页查询接口
		annexFileQueryWithPage: data => {
			return request({ url: prefix4 + '/file/queryWithPage', method: 'post', data })
		},
		// 文件资源保存或新增接口
		annexFileAdd: data => {
			return request({ url: prefix4 + '/file/add', method: 'post', data })
		},
		// 文件资源删除接口
		annexFileDelete: data => {
			return request({ url: prefix4 + '/file/delete', method: 'post', data })
		},
		// 文件资源删除接口
		annexFileDetailDelete: data => {
			return request({ url: prefix4 + '/file/detail/delete', method: 'post', data })
		},
		// 文件资源更新接口
		annexFileUpdate: data => {
			return request({ url: prefix4 + '/file/update', method: 'post', data })
		},
		// 根据ID获取文件资源详情信息接口
		annexFileGetById: params => {
			return request({ url: prefix4 + '/file/getById', method: 'get', params })
		},

		// 资源列表左侧全局统计菜单查询接口
		lefttabQuery: data => {
			return request({ url: prefix4 + '/statistics/lefttab/query', method: 'post', data })
		},
		// shp数据服务发布接口
		shpPublish: data => {
			return request({ url: prefix3 + '/map/server/shp/publish', method: 'post', data })
		},

		// --------------------- 服务管理 ---------------------
		serviceAdd: data => {
			return request({ url: prefix4 + '/service/add', method: 'post', data })
		},
		serviceQueryWithPage: data => {
			return request({ url: prefix4 + '/service/queryWithPage', method: 'post', data })
		},
		serviceTree: data => {
			return request({ url: prefix4 + '/service/tree', method: 'post', data })
		},
		serviceUpdate: data => {
			return request({ url: prefix4 + '/service/update', method: 'post', data })
		},
		serviceDelete: params => {
			return request({ url: prefix4 + '/service/delete', method: 'get', params })
		},
		serviceGetById: params => {
			return request({ url: prefix4 + '/service/getById', method: 'get', params })
		},

		// --------------------- 数据专题 ---------------------
		themeBindService: data => {
			return request({ url: prefix4 + '/theme/bind/service', method: 'post', data })
		},
		themeGetLayersById: params => {
			return request({ url: prefix4 + '/theme/getLayersById', method: 'get', params })
		},
		themeTree: data => {
			return request({ url: prefix4 + '/theme/tree', method: 'post', data })
		},
		themeUnbindService: data => {
			return request({ url: prefix4 + '/theme/unbind/service', method: 'post', data })
		},
		themeAdd: data => {
			return request({ url: prefix4 + '/theme/add', method: 'post', data })
		},
		themeDelete: params => {
			return request({ url: prefix4 + '/theme/delete', method: 'get', params })
		},
		themeGetById: params => {
			return request({ url: prefix4 + '/theme/getById', method: 'get', params })
		},
		themeGetByName: params => {
			return request({ url: prefix4 + '/theme/getByName', method: 'get', params })
		},
		themeUpdate: data => {
			return request({ url: prefix4 + '/theme/update', method: 'post', data })
		},

		// --------------------- 数据目录 ---------------------
		catalogAdd: data => {
			return request({ url: prefix4 + '/catalog/add', method: 'post', data })
		},
		catalogDelete: params => {
			return request({ url: prefix4 + '/catalog/delete', method: 'get', params })
		},
		catalogGetById: params => {
			return request({ url: prefix4 + '/catalog/getById', method: 'get', params })
		},
		catalogTree: data => {
			return request({ url: prefix4 + '/catalog/tree', method: 'post', data })
		},
		catalogUpdate: data => {
			return request({ url: prefix4 + '/catalog/update', method: 'post', data })
		}
	}
}
