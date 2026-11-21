import emitter from '@/components/map/utils/eventbus.js'
import { State } from '@/components/map/primitive/interaction/DrawTool.js'

import { useExportStore } from '@/store/modules/export.js'
const exportStore = useExportStore()

/**
 * 地图对象
 */
let drawTool = null

/**
 * 绘制类型
 */
let drawType = 'Text'

/**
 * 绘制样式
 */
let drawStyle = {
	text: {
		font: 'normal 16px black',
		fill: {
			color: 'blue'
		},
		text: '',
		stroke: {
			color: 'rgba(255,255,255,1)',
			width: 2
		}
	}
}

const drawText = style => {
	if (!exportStore.outMap?.drawTool) return
	const text = String(style?.text || '').trim()
	if (!text) return
	drawTool = exportStore.outMap.drawTool
	drawStyle = Object.assign(drawStyle, {
		id: style.id,
		text: {
			font: `${style.fontStyle || 'normal'} ${style.fontSize2 || 16}px black`,
			fill: {
				color: style.color || 'blue'
			},
			text,
			stroke: {
				color: style.borderColor || 'white',
				width: style.fontWeight || 2
			}
		}
	})
	exportStore.setDrawType('Text')
	startDrawTool()
}

emitter.on('drawend', feature => {
	if (exportStore.isOpenExport && exportStore.drawType == 'Text') {
		const styleId = drawStyle.id
		exportStore.setDrawedText(feature, drawStyle)
		closeDrawTool()
		// 落点后立即标记，避免下一次输入又进入绘制并生成新文字
		emitter.emit('exportTextPlaced', styleId)
	}
})

const startDrawTool = () => {
	let drawOption = {
		drawType: drawType,
		skipOverlay: true,
		// 绘制过程用透明占位，真正样式在 OutputLayer 上设置，避免双份视觉文字
		drawedStyle: {
			fill: {
				color: 'rgba(255, 255, 255, 0.0001)'
			}
		}
	}
	if (drawTool.state() == State.Drawding) {
		drawTool.toggle(drawOption)
	} else {
		drawTool.start(drawOption)
	}
}

const closeDrawTool = () => {
	if (drawTool.state() == State.Drawding) {
		drawTool.finish()
		drawTool.clear()
	}
}

export default drawText
