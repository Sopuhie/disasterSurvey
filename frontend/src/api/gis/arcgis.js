import axios from 'axios'
import request from '@/utils/request.js'
import { transfrom } from '@/components/map/primitive/util/ProjectionUtil.js'

export function arcGisApi() {
	/**
	 * ArcGIS Identify 功能
	 * @param {*} url  请求的服务地址
	 * @param {*} queryParams  请求参数，要求包括（geometry，geometryType，sr，layers，tolerance，mapExtent，imageDisplay，returnGeometry，f）
	 */
	async function exeArcServiceIdentify(url, queryParams) {
		if (!url || !url.length) {
			throw new Error('invalid url')
		}
		if (!queryParams || !Object.keys(queryParams).length) {
			throw new Error('invalid queryParams')
		}

		let params = []
		for (let [key, value] of Object.entries(queryParams)) {
			params.push(key + '=' + value)
		}

		return axios.get(url + '/Identify?' + params.join('&')).then(response => {
			return response.data
		})
	}

	/**
	 * 属性查询
	 */
	async function identifyQuery(url, { coords, size, extent, layers, dataProject, targetProject }) {
		// 数据服务可能存在多个不同坐标系，为彻底解决这问题修改步骤如下：
		// 1、将系统当前的坐标转换为服务坐标
		// 2、将获取的结果数据坐标转换为系统坐标

		// 判断系统当前的坐标系统是否与服务一致，若不同则进行转换
		let queryCoords = coords
		let queryMapExtent = extent
		if (dataProject != targetProject) {
			// 转当前坐标
			queryCoords = transfrom(coords, dataProject, targetProject)
			// 转换四至
			let minCoords = transfrom([extent[0], extent[1]], dataProject, targetProject)
			let maxCoords = transfrom([extent[2], extent[3]], dataProject, targetProject)
			queryMapExtent = [minCoords[0], minCoords[1], maxCoords[0], maxCoords[1]]
		}
		
		// 查询数据
		let result = await this.exeArcServiceIdentify(url, {
			geometry: queryCoords,
			geometryType: GeometryType.esriGeometryPoint,
			sr: targetProject.replace('EPSG:', ''),
			layers: layers ? layers : FilterLyrLevel.ALL,
			tolerance: 10,
			mapExtent: queryMapExtent,
			imageDisplay: [size[0], size[1], 300],
			returnGeometry: true,
			f: FormatType.JSON
		})

		// 数据转换，将获取的结果数据坐标转换为系统坐标
		if (dataProject != targetProject) {
			// 后续 用到再写转换
		}

		return result
	}

	/**
	 * 唯一值查询
	 */
	async function uniqueValueQuery(url, fields) {
		if (!url || !url.length) {
			throw new Error('invalid url')
		}
		if (!fields?.length) {
			throw new Error('invalid fields')
		}

		let queryParams = {
			where: '1=1',
			outFields: fields.toString(),
			returnGeometry: false,
			returnDistinctValues: true,
			f: 'json'
		}

		let params = []
		for (let [key, value] of Object.entries(queryParams)) {
			params.push(key + '=' + value)
		}

		return axios.get(url + '/query?' + params.join('&')).then(response => {
			return response.data
		})
	}
	
	/**
	 * 获取服务信息
	 */
	async function getMapServerInfo(url) {
		return axios.get(url + '?f=pjson').then(response => {
			return response.data
		})
	}

	/**
	 * 执行 query 查询
	 */
	async function query(url, queryParams) {
		if (!url || !url.length) {
			throw new Error('invalid url')
		}
		
		let params = []
		for (let [key, value] of Object.entries(queryParams)) {
			params.push(key + '=' + value)
		}

		return axios.get(url + '/query?' + params.join('&')).then(response => {
			return response.data
		})
	}

	/**
	 * 获取图例信息
	 */
	async function getLegendInfo(url) {
		return axios.get(url + '/legend?f=json').then(response => {
			return response.data
		})
	}


	return {
		query,
		exeArcServiceIdentify,
		identifyQuery,
		getMapServerInfo,
		uniqueValueQuery,
		getLegendInfo
	}
}

export const GeometryType = {
	esriGeometryEnvelope: 'esriGeometryEnvelope',
	esriGeometryPoint: 'esriGeometryPoint',
	esriGeometryPolyline: 'esriGeometryPolyline',
	esriGeometryPolygon: 'esriGeometryPolygon',
	esriGeometryMultipoint: 'esriGeometryMultipoint'
}

export const SpatialRel = {
	esriSpatialRelIntersects: 'esriSpatialRelIntersects',
	esriSpatialRelEnvelopeIntersects: 'esriSpatialRelEnvelopeIntersects',
	esriSpatialRelIndexIntersects: 'esriSpatialRelIndexIntersects',
	esriSpatialRelTouches: 'esriSpatialRelTouches',
	esriSpatialRelOverlaps: 'esriSpatialRelOverlaps',
	esriSpatialRelCrosses: 'esriSpatialRelCrosses',
	esriSpatialRelWithin: 'esriSpatialRelWithin',
	esriSpatialRelContains: 'esriSpatialRelContains',
	esriSpatialRelRelation: 'esriSpatialRelRelation'
}

export const FormatType = {
	HTML: 'html',
	JSON: 'json',
	AMF: 'amf',
	KMZ: 'kmz'
}

export const FilterLyrLevel = {
	TOP: 'top',
	VISIBLE: 'visible',
	ALL: 'all'
}
