import { toRaw, ref } from 'vue'
import emitter from '../map/utils/eventbus'
import { ResetExtent } from '../map/primitive/control/ResetExtent.js'
import { DrawType, State as DrawState } from '../map/primitive/interaction/DrawTool'
import { SwipeTool2, SwipeType } from '../map/primitive/interaction/SwipeTool2.js'
import { MeasureType } from '../map/primitive/interaction/MeasureTool'
import { SplitScreen, ViewMode } from '../map/primitive/control/SplitScreen.js'
import { PrintDialog } from '../map/primitive/control/PrintDialog.js'
import { useMapStore } from '@/store/modules/map.js'
import { useAppStore } from '@/store/modules/app.js'

const appStore = useAppStore()
const mapStore = useMapStore()
let isMapMounted = false
let measureTool = null
let drawTool = null
let splitScnTool = null
let swipeTool = null

const flyToCoordsVisible = ref(false)

// 工具栏点击事件
const toolBarHandle = (item, childItem) => {
	let data = null
	appStore.setSwitchObj('toolBarSelect', false)
	if (childItem) {
		data = childItem
		console.log('工具栏下拉菜单事件:', toRaw(childItem), '----', data.name)
		switch (data.executetool) {
			case 'distance':
				measureLengthHandle()
				break
			case 'area':
				measureAreaHandle()
				break
			case 'areaMu':
				measureMuHandle()
				break
			case 'closeMeasure':
				closeAllMapTool()
				break
			case 'drawByCoodinates':
				drawByCoodinates()
				break
			case 'drawPolygon':
				drawPolygon()
				break
			case 'drawLineString':
				drawLineString()
				break
			case 'drawPoint':
				drawPoint()
				break
			case 'splitMap_1':
				setSplitScreen(ViewMode.ONE_MAP)
				break
			case 'splitMap_2':
				setSplitScreen(ViewMode.DOUBLE_MAP)
				break
			case 'splitMap_3':
				setSplitScreen(ViewMode.THREE_MAP)
				break
			case 'splitMap_4':
				setSplitScreen(ViewMode.FOUR_MAP)
				break
			case 'splitMap_6':
				setSplitScreen(ViewMode.SIX_MAP)
				break
			case 'splitMap_close':
				setSplitScreen(ViewMode.ONE_MAP)
				break
			case 'js:topB':
				setSwipeScreen(SwipeType.Horizontal)
				break
			case 'js:leftR':
				setSwipeScreen(SwipeType.Vertical)
				break
			case 'js:close':
				closeSwipeScreen()
				break
			default:
				break
		}
	} else {
		data = item
		console.log('工具栏菜单事件:', toRaw(item), '----', data.name)
		switch (data.executetool) {
			case 'closeAction':
				closeAllMapTool()
				break
			case 'fullExtent':
				resetExtent()
				break
			case 'clear':
				clearMapElements()
				break
			case 'printMap':
				exportPicture()
				break
			case 'importData':
				parseData(data.configure)
				break
			case 'importLayer':
				openImportLayer()
				break
			case 'identify':
				spatialQuery()
			default:
				break
		}
	}
}

/**
 * 初始化工具
 */
emitter.on('mapMounted', map => {
	// mapStore.setActiveMap('ONE_MAP', map)

	// 初始化测量工具
	measureTool = mapStore.activeMap.measureTool
	// 初始化绘制工具
	drawTool = mapStore.activeMap.drawTool

	// 初始化分屏工具
	let mapEl = mapStore.activeMap.olMap.getTargetElement()
	splitScnTool = new SplitScreen({
		map: mapStore.activeMap,
		mapOption: mapStore.activeMap.mapOptions,
		mainContainer: mapEl,
		width: mapEl.offsetWidth,
		height: mapEl.offsetHeight
	})
	isMapMounted = true
})

emitter.on('activemap', ({ mapName, map }) => {
	mapStore.setActiveMap(mapName, map)
	measureTool = mapStore.activeMap.measureTool
	drawTool = mapStore.activeMap.drawTool
})

/**
 * 测量长度
 */
function measureLengthHandle() {
	if (isMapMounted) {
		closeAllMapTool()
		measureTool.start(MeasureType.Length)
	}
}

/**
 * 测量面积
 */
function measureAreaHandle() {
	if (isMapMounted) {
		closeAllMapTool()
		measureTool.start(MeasureType.Area)
	}
}

/**
 * 测量亩
 */
function measureMuHandle() {
	if (isMapMounted) {
		closeAllMapTool()
		measureTool.start(MeasureType.Mu)
	}
}

/**
 * 地图复位
 */
function resetExtent() {
	if (isMapMounted) {
		// 初始化复位工具
		new ResetExtent({
			map: mapStore.activeMap
		}).reset()
	}
}

/**
 * 坐标输入
 */
function drawByCoodinates() {
	flyToCoordsVisible.value = true
}
function closeByCoodinates() {
	flyToCoordsVisible.value = false
}

/**
 * 绘制面
 */
function drawPolygon() {
	if (isMapMounted) {
		closeAllMapTool()
		drawTool.start({
			drawType: DrawType.Polygon,
			drawingStyle: feature => {
				return {
					stroke: {
						color: 'green',
						width: 2
					},
					fill: {
						color: 'rgba(255, 0, 255, 0.3)'
					},
					image: {
						radius: 5,
						stroke: {
							color: 'rgba(0, 0, 0, 0.7)'
						},
						fill: {
							color: 'rgba(255, 255, 255, 0.2)'
						}
					}
				}
			}
		})
	}
}

/**
 * 绘制线
 */
function drawLineString() {
	if (isMapMounted) {
		closeAllMapTool()
		drawTool.start({
			drawType: DrawType.Line,
			drawingStyle: feature => {
				return {
					stroke: {
						color: 'blue',
						width: 3
					},
					fill: {
						color: 'rgba(0, 0, 255, 0.3)'
					},
					image: {
						radius: 5,
						stroke: {
							color: 'rgba(0, 0, 0, 0.7)'
						},
						fill: {
							color: 'rgba(255, 255, 255, 0.2)'
						}
					}
				}
			},
			drawedStyle: feature => {
				return {
					stroke: {
						color: 'blue',
						width: 3
					},
					fill: {
						color: 'rgba(0, 0, 255, 0.3)'
					}
				}
			}
		})
	}
}

/**
 * 绘制点
 */
function drawPoint() {
	if (isMapMounted) {
		closeAllMapTool()
		drawTool.start({
			drawType: DrawType.Point,
			drawingStyle: feature => {
				return {
					image: {
						radius: 5,
						stroke: {
							color: 'rgba(0, 0, 0, 0.7)'
						},
						fill: {
							color: 'rgba(255, 255, 255, 0.2)'
						}
					}
				}
			}
		})
	}
}

/**
 * 导出地图
 */
function exportPicture() {
	let printDialog = new PrintDialog({
		map: mapStore.activeMap,
		lang: 'zh',
		target: '', // 传一个空 div，为了避免显示控件
		close: () => {
			if (mapStore.activeMap.setPerspective) {
				mapStore.activeMap.setPerspective(8)
			}
		}
	})
	printDialog.print()
	if (mapStore.activeMap.setPerspective) {
		mapStore.activeMap.setPerspective(0)
	}
}

/**
 * 叠加数据
 */
function parseData(config) {
	// parseConfig.value = JSON.parse(config)
	mapStore.setParseConfig(JSON.parse(config))
	appStore.setSwitchObj('showUploadData', true)
}
/**
 * 导入图层
 */
function openImportLayer() {
	appStore.setSwitchObj('importData', true)
}

/**
 * 属性查询
 */

function spatialQuery() {
	if (isMapMounted) {
		closeAllMapTool()
		appStore.setSwitchObj('identify', true)
	}
}

/**
 * 清空地图
 */
function clearMapElements() {
	if (isMapMounted) {
		measureTool.clear()
		drawTool.clear()
		mapStore.activeMap.featureManager.removeFeatures()
		appStore.setToolBarSelect('delAll')
	}
}

/**
 * 地图分屏
 */
function setSplitScreen(newMapMode) {
	if (isMapMounted) {
		// 每次地图分屏清空已加载专题
		splitScnTool.setViewMode(mapStore.activeMap, newMapMode)
		mapStore.setMapMode(newMapMode)
	}
}

const themeLayerName = theme => theme?.themeInfo?.layerName || theme?.themeInfo?.name

/**
 * 开始卷帘
 */
function setSwipeScreen(swipeMode) {
	if (mapStore.loadedThemes?.length >= 2) {
		if (!swipeTool) {
			swipeTool = new SwipeTool2({})
		}
		const leftLyrs = [themeLayerName(mapStore.loadedThemes[0])]
		const rightLyrs = [themeLayerName(mapStore.loadedThemes[1])]
		swipeTool.start({
			map: mapStore.activeMap,
			leftLyrs,
			rightLyrs,
			swipeMode
		})
	} else {
		ElMessage({
			showClose: true,
			message: '请先加载两个专题！',
			type: 'warning'
		})
	}
}

/**
 * 关闭卷帘
 */
function closeSwipeScreen() {
	swipeTool?.finish(mapStore.activeMap)
}

/**
 * 关闭所有工具
 */
function closeAllMapTool() {
	// 关闭测量工具
	if (measureTool) {
		measureTool.finish()
	}
	// 关闭绘制工具
	if (drawTool) {
		drawTool.finish()
	}
	appStore.setSwitchObj('identify', false)
	appStore.setToolBarSelect('delAll')
}

/**
 * 关闭叠加面板
 */
function closeParsePanel() {
	appStore.setSwitchObj('showUploadData', false)
	appStore.setToolBarSelect('delAll')
}

export { toolBarHandle, flyToCoordsVisible, closeParsePanel, closeByCoodinates }
