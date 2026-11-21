import { ZoomInOut } from '@/components/map/primitive/control/ZoomInOut.js'
import { useMapStore } from '@/store/modules/map.js'
const mapStore = useMapStore()

/**
 * 获取A3(297mm×420mm)/A4(210mm×297mm)打印纸的宽高
 * @param {Number} w 打印宽度
 * @param {String} direction 纸张方向
 * @param {String} layout 纸张排版
 * @returns {Object} 打印尺寸对象
 */
const getPrintSize = (w = 750, direction = 'h') => {
	// 宽高比例
	const ratio = 1.414
	let obj = { width: 750, height: 750 / ratio }

	if (w <= 0 || w === null || w === undefined) return obj

	//  横向打印
	if (direction === 'h') {
		obj.width = w
		obj.height = w / ratio
	}
	//  纵向打印
	if (direction === 'v') {
		obj.width = w / ratio
		obj.height = w
	}
	return obj
}

/**
 * 放大功能
 */
function zoomInHandler() {
	new ZoomInOut({
		map: mapStore.activeMap
	}).zoomIn()
}

/**
 * 缩小功能
 */
function zoomOutHandler() {
	new ZoomInOut({
		map: mapStore.activeMap
	}).zoomOut()
}

export { getPrintSize, zoomInHandler, zoomOutHandler }
