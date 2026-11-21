import request from '@/utils/request.js'

const prefix1 = '/web-disaster-authentication/web/disaster/authentication'
const prefix2 = '/web-disaster-common/web/disaster/common'
const prefix3 = '/web-disaster-basic/web/disaster/basic'

export function systemApi() {
	return {
		// --------------------- 运维配置 ---------------------
		// 运维路由菜单保存或新增接口
		routerAdd: data => {
			return request({ url: prefix3 + '/router/add', method: 'post', data })
		},
		// 运维路由菜单项删除接口
		routerDelete: params => {
			return request({ url: prefix3 + '/router/delete', method: 'get', params })
		},
		// 运维路由菜单列表分页查询接口
		routerQueryWithPage: data => {
			return request({ url: prefix3 + '/router/queryWithPage', method: 'post', data })
		},
		// 根据权限查询运维路由菜单信息并组装成树结构返回的接口
		routerTree: params => {
			return request({ url: prefix3 + '/router/tree', method: 'get', params })
		},
		// 运维路由菜单更新接口
		routerUpdate: data => {
			return request({ url: prefix3 + '/router/update', method: 'post', data })
		},

		// --------------------- 系统配置 ---------------------
		// 系统基础配置列表分页查询接口
		configQueryWithPage: data => {
			return request({ url: prefix3 + '/config/queryWithPage', method: 'post', data })
		},
		// 系统基础配置保存或新增接口
		configAdd: data => {
			return request({ url: prefix3 + '/config/add', method: 'post', data })
		},
		// 系统基础配置项删除接口
		configDelete: params => {
			return request({ url: prefix3 + '/config/delete', method: 'get', params })
		},
		// 系统基础配置更新接口
		configUpdate: data => {
			return request({ url: prefix3 + '/config/update', method: 'post', data })
		},

		// --------------------- 功能管理 ---------------------
		// 功能列表分页查询接口
		actionQueryWithPage: data => {
			return request({ url: prefix3 + '/action/queryWithPage', method: 'post', data })
		},
		// 功能保存或新增接口
		actionAdd: data => {
			return request({ url: prefix3 + '/action/add', method: 'post', data })
		},
		// 功能项删除接口
		actionDelete: params => {
			return request({ url: prefix3 + '/action/delete', method: 'get', params })
		},
		// 功能项更新接口
		actionUpdate: data => {
			return request({ url: prefix3 + '/action/update', method: 'post', data })
		},
		// 根据权限查询系统功能信息并组装成树结构返回的接口
		actionTree: params => {
			return request({ url: prefix3 + '/action/tree', method: 'get', params })
		},

		/**
		 * 根据指定条件获取用户功能权限信息的接口-用于客户端的权限
		 */
		permissionClientQuery: data => {
			return request({ url: prefix1 + '/permission/client/query', method: 'post', data })
		},

		/**
		 * 获取数据字典信息
		 */
		queryDictionary: params => {
			return request({
				url: prefix2 + '/dict/detail/query',
				method: 'get',
				params
			})
		},

		/**
		 * 系统基础配置列表分页查询接口
		 */
		queryMapConfig: params => {
			return request({
				url: prefix3 + '/config/getByName',
				method: 'get',
				params
			})
		},

		// --------------------- 字典管理 ---------------------
		// 字典项删除接口
		dictDetailDelete: params => {
			return request({ url: prefix2 + '/dict/detail/delete', method: 'get', params })
		},
		// 字典项查询接口
		dictDetailQuery: params => {
			return request({ url: prefix2 + '/dict/detail/query', method: 'get', params })
		},
		// 字典项保存或新增接口
		dictDetailSave: data => {
			return request({ url: prefix2 + '/dict/detail/save', method: 'post', data })
		},
		// 字典项更新接口
		dictDetailUpdate: data => {
			return request({ url: prefix2 + '/dict/detail/update', method: 'post', data })
		},
		// 字典类型删除接口
		dictTypeDelete: params => {
			return request({ url: prefix2 + '/dict/type/delete', method: 'get', params })
		},
		// 字典类型记录批量查询接口（可分页）
		dictTypeQuery: data => {
			return request({ url: prefix2 + '/dict/type/query', method: 'post', data })
		},
		// 字典类型保存或新增接口
		dictTypeSave: data => {
			return request({ url: prefix2 + '/dict/type/save', method: 'post', data })
		},
		// 字典类型更新接口
		dictTypeUpdate: data => {
			return request({ url: prefix2 + '/dict/type/update', method: 'post', data })
		}
	}
}
