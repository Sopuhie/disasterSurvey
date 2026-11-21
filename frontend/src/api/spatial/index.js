import request from '@/utils/request.js'

const prefix1 = '/web-disaster-map/web/disaster/map'

export function spatialApi() {
	return {
		/**
		 * 查询统计结果导出
		 */
		exportQuery: data => {
			return request({
				url: prefix1 + '/queryStatistics/export',
				method: 'post',
				data,
				responseType: 'blob'
			})
		}
	}
}
