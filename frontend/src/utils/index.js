/**
 * 目录树，设置label和id，为了兼容treeSelect，可以添加valueKey
 *
 * @param {Array} data
 * @param {String} key
 * @param {String} id
 * @param {String} valueKey value的key
 * @return {Array}
 */
const setTreeProps = (data, key, id, valueKey = '') => {
	if (data?.length) {
		filterChildren(data, key, id, valueKey)
		return data
	} else {
		return []
	}
	function filterChildren(data, key, id, valueKey) {
		data.forEach(item => {
			item['id'] = item.id ? item.id : item[id]
			if (valueKey) {
				item['value'] = item[valueKey]
			}
			if (key) {
				item['label'] = item[key]
			} else {
				item['label'] = item.label ? item.label : item.name
				if (!item.label) {
					item.label = ''
				}
				if (item.name) {
					delete item.name
				}
			}
			if (!item.children) {
				item.children = []
			}
			if (item.children?.length) {
				filterChildren(item.children, key, id, valueKey)
			}
		})
	}
}

// 深拷贝--使用递归，处理更复杂的对象，包括函数和循环引用
const deepCopy = (obj, hash = new WeakMap()) => {
	if (typeof obj !== 'object' || obj === null) {
		return obj
	}
	if (hash.has(obj)) {
		return hash.get(obj)
	}
	let newObj = Array.isArray(obj) ? [] : {}
	hash.set(obj, newObj)
	for (let key in obj) {
		if (obj.hasOwnProperty(key)) {
			newObj[key] = deepCopy(obj[key], hash)
		}
	}
	return newObj
}

// 获取当前时间，格式化
const getTimeFormatted = (type = 'YYYY-MM-DD HH:mm:ss') => {
	const date = new Date()
	const year = date.getFullYear()
	const month = ('0' + (date.getMonth() + 1)).slice(-2) // 月份是从0开始的，所以需要加1，并且可能需要补零
	const day = ('0' + date.getDate()).slice(-2) // 可能需要补零
	const hours = ('0' + date.getHours()).slice(-2) // 可能需要补零
	const minutes = ('0' + date.getMinutes()).slice(-2) // 可能需要补零
	const seconds = ('0' + date.getSeconds()).slice(-2) // 可能需要补零
	const week = date.getDay() // 星期
	const weekArr = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
	if (type === 'YYYY-MM-DD HH:mm:ss') {
		return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
	}
	if (type === 'YYYY-MM-DD HH:mm:ss w') {
		return `${year}-${month}-${day} ${hours}:${minutes}:${seconds} ${weekArr[week]}`
	}
	if (type === 'YYYY-MM-DD') {
		return `${year}-${month}-${day}`
	}
}

function transformToTableData(list) {
	const tableData = [
		list.flatMap(item => [
			{ type: 'header', label: item.title },
			{ type: 'data', value: item.value, key: item.key }
		])
	]

	return tableData
}

// 属性查询 - 转换数据
function transformData(data) {
	const transformedData = []

	function transformObject(obj) {
		const newObj = { choose: false, children: [] }

		if (obj.themeName) {
			newObj.label = obj.themeName
			newObj.children = obj.services.map(transformObject)
		} else if (obj.serviceName) {
			newObj.label = obj.serviceName
			newObj.children = obj.features.map(transformObject)
		} else if (obj.layerName) {
			newObj.label = obj.layerName
			newObj.layerId = obj.layerId
			newObj.displayFieldName = obj.displayFieldName
			newObj.value = obj.value
			newObj.attributes = obj.attributes
			newObj.geometryType = obj.geometryType
			newObj.geometry = obj.geometry
		}

		return newObj
	}

	for (const item of data) {
		const transformedItem = transformObject(item)
		transformedData.push(transformedItem)
	}

	return transformedData
}

const setIdentifyData = arr => {
	const keys = ['services', 'features']
	const labels = ['themeName', 'serviceName', 'layerName']
	if (arr.length > 0) {
		return transformData(arr, keys, labels)
	} else {
		return []
	}
}

// 统计报表--扁平数据转json树
const getTheme = (arr, themeid) => {
	if (!arr || !Array.isArray(arr) || arr.length === 0) return []
	let map = {}
	let newArr = []
	for (let i = 0; i < arr.length; i++) {
		arr[i]['children'] = []
		const key = arr[i][themeid]
		const { id, children, themeId, themeName, layerId } = arr[i]
		map[key] = { id, children, themeId, themeName, layerId }
	}
	arr.forEach(item => {
		if (item[themeid] === map[item[themeid]][themeid]) {
			delete item.children
			map[item[themeid]].children.push({
				key: item.id,
				value: item.id,
				title: item.subName,
				label: item.subName,
				themeId: item.themeId,
				themeName: item.themeName
			})
		}
	})
	let n = 0
	for (const key in map) {
		if (Object.hasOwnProperty.call(map, key)) {
			let keys = '10-'
			keys = keys + n
			newArr.push({
				key: keys,
				value: keys,
				title: map[key]['themeName'],
				label: map[key]['themeName'],
				...map[key]
			})
			n++
		}
	}
	return newArr
}

/**
 * 目录树，根据当前key，返回所有父级的name
 * @param {Array} data 原数组
 * @param {String} key 要查询的字段
 * @param {String} key2 要比较的字段
 * @param {String} name 要返回的字段
 *
 */
function searchParent(data, key, key2, name) {
	let t = []
	for (let i = 0; i < data.length; i++) {
		const e = data[i]
		if (e[key] === key2) {
			//若查询到对应的节点，则直接返回
			t.push(e[name])
			break
		} else if (e.children && e.children.length > 0) {
			//判断是否还有子节点，若有对子节点进行循环调用
			let p = searchParent(e.children, key, key2, name)
			//若p的长度不为0，则说明子节点在该节点的children内，记录此节点，并终止循环
			if (p.length !== 0) {
				p.unshift(e[name])
				t = p
				break
			}
		}
	}
	return t
}

// 根据parentIds，返回父级
const getParentItem = (data, parentId) => {
	let objs = null
	if (data?.length) {
		filterChildren(data, parentId)
		return objs
	} else {
		return null
	}
	function filterChildren(data, parentId) {
		for (let i = 0; i < data.length; i++) {
			if (data[i]['id'] === parentId) {
				objs = data[i]
				break
			}
			if (data[i].children?.length) {
				filterChildren(data[i].children, parentId)
			}
		}
	}
}

// 获取窗口大小
const getWindowSize = () => {
	const screenWidth = ref(window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth)
	const screenHeight = ref(window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight)
	return { w: screenWidth, h: screenHeight }
}

// 防抖
function _debounce(func, wait = 200) {
	let timeout
	return function () {
		clearTimeout(timeout)
		timeout = setTimeout(() => {
			func.apply(this, arguments)
		}, wait)
	}
}
// 节流
function _throttle(fn, interval = 200) {
	let last = 0
	let timer = null
	const that = this
	const args = arguments
	return function () {
		const now = +new Date()
		if (last && now - last < interval) {
			clearTimeout(timer)
			timer = setTimeout(function () {
				last = now
				fn.apply(that, args)
			}, interval)
		} else {
			last = now
			fn.apply(that, args)
		}
	}
}
// 获取文件名
function getFileName(fullFileName) {
	const lastDotIndex = fullFileName.lastIndexOf('.')
	return lastDotIndex === -1 ? fullFileName : fullFileName.slice(0, lastDotIndex)
}

const getAssetsFile = name => {
	return new URL(`../assets/imgs/${name}.png`, import.meta.url).href
}

import { getQueryParam, jumpToThirdPartyLogin } from '@/utils/sso.js'

// 获取url中的参数（兼容 hash 查询串）
function getQueryString(name) {
	return getQueryParam(name)
}

export {
	setTreeProps,
	deepCopy,
	getTimeFormatted,
	setIdentifyData,
	getTheme,
	getWindowSize,
	getParentItem,
	_debounce,
	_throttle,
	searchParent,
	getFileName,
	getAssetsFile,
	transformToTableData,
	getQueryString,
	jumpToThirdPartyLogin
}
