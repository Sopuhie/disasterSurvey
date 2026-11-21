import request from '@/utils/request.js'
import apiurl from "./apiUrlPath.js"

export function themeApi() {
	return {
		/**
     * 根据专题id获取专题下的服务和图层接口
     */
		getLayersById: params => {
			return request({
				url: apiurl.getLayersById,
				method: 'get',
				params
			})
		},
    /**
     * 根据专题名称获取专题
     */
		getThemeByName: params => {
			return request({
				url: apiurl.getThemeByName,
				method: 'get',
				params
			})
		},
	}
}
