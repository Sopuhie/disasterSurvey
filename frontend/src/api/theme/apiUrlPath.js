const prefix = `/web-disaster-resource/web/disaster/resource/theme`

export default {
	/**
	 * 根据专题id获取专题下的服务和图层接口
	 */
	getLayersById: `${prefix}/getLayersById`,
	/**
	 * 根据专题名称获取专题
	 */
  getThemeByName: `${prefix}/getByName`,
	/**
	 * 根据专题名称获取专题
	 */
	getServiceByName: `/web-disaster-resource/web/disaster/resource/service/proxy/forwarding`
}
