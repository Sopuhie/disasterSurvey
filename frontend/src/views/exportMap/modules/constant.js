export const initPaper = {
	top: 8,
	bottom: 8,
	left: 8,
	right: 8
}
export const paperTypeSize = {
	A4_a: { w: 297, h: 210 },
	A4_c: { w: 210, h: 297 },
	A3_a: { w: 420, h: 297 },
	A3_c: { w: 297, h: 420 }
}
export const initMapData = {
	A4_a: {
		map: { height: 158, width: 244, z: 1 },
		thumbnial: { height: 32, width: 32, z: 4, show: true },
		text: {
			title: { height: 12, width: 281, fontSize: 30, z: 10, color: 'rgba(0, 0, 0, 1)' },
			producer: { height: 8, width: 70, fontSize: 9, z: 10, color: 'rgba(0, 0, 0, 1)' },
			time: { height: 8, width: 70, fontSize: 9, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain: { height: 8, width: 281, fontSize: 10, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain2: { height: 8, width: 48, fontSize: 9, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain3: { height: 8, width: 80, fontSize: 9, z: 10, color: 'rgba(0, 0, 0, 1)' }
		},
		legend: { height: 158, width: 34, z: 5 },
		mapFactor: {
			compass: { height: 16, width: 16, z: 6 },
			plottingScale: { height: 10, width: 40, z: 6, fontSize: 16 }
		}
	},
	A4_c: {
		map: { height: 210, width: 194, z: 1 },
		thumbnial: { height: 32, width: 32, z: 4, show: true },
		text: {
			title: { height: 14, width: 194, fontSize: 30, z: 10, color: 'rgba(0, 0, 0, 1)' },
			producer: { height: 8, width: 70, fontSize: 9, z: 10, color: 'rgba(0, 0, 0, 1)' },
			time: { height: 8, width: 64, fontSize: 9, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain: { height: 8, width: 194, fontSize: 10, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain2: { height: 8, width: 48, fontSize: 9, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain3: { height: 8, width: 80, fontSize: 9, z: 10, color: 'rgba(0, 0, 0, 1)' }
		},
		legend: { height: 28, width: 194, z: 5 },
		mapFactor: {
			compass: { height: 16, width: 16, z: 6 },
			plottingScale: { height: 12, width: 46, z: 6, fontSize: 16 }
		}
	},
	A3_a: {
		map: { height: 236, width: 318, z: 1 },
		thumbnial: { height: 40, width: 40, z: 4, show: true },
		text: {
			title: { height: 16, width: 400, fontSize: 30, z: 10, color: 'rgba(0, 0, 0, 1)' },
			producer: { height: 10, width: 90, fontSize: 10, z: 10, color: 'rgba(0, 0, 0, 1)' },
			time: { height: 10, width: 90, fontSize: 10, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain: { height: 10, width: 400, fontSize: 11, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain2: { height: 10, width: 70, fontSize: 10, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain3: { height: 10, width: 100, fontSize: 10, z: 10, color: 'rgba(0, 0, 0, 1)' }
		},
		legend: { height: 236, width: 76, z: 5 },
		mapFactor: {
			compass: { height: 18, width: 18, z: 6 },
			plottingScale: { height: 12, width: 50, z: 6, fontSize: 16 }
		}
	},
	A3_c: {
		map: { height: 318, width: 277, z: 1 },
		thumbnial: { height: 40, width: 40, z: 4, show: true },
		text: {
			title: { height: 16, width: 277, fontSize: 30, z: 10, color: 'rgba(0, 0, 0, 1)' },
			producer: { height: 10, width: 90, fontSize: 10, z: 10, color: 'rgba(0, 0, 0, 1)' },
			time: { height: 10, width: 90, fontSize: 10, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain: { height: 10, width: 277, fontSize: 11, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain2: { height: 10, width: 70, fontSize: 10, z: 10, color: 'rgba(0, 0, 0, 1)' },
			explain3: { height: 10, width: 100, fontSize: 10, z: 10, color: 'rgba(0, 0, 0, 1)' }
		},
		legend: { height: 42, width: 277, z: 5 },
		mapFactor: {
			compass: { height: 18, width: 18, z: 6 },
			plottingScale: { height: 14, width: 54, z: 6, fontSize: 16 }
		}
	}
}

function layoutSheet(type, mode) {
	const paper = paperTypeSize[type]
	const data = initMapData[type]
	const text = data.text
	const factor = data.mapFactor
	const m = initPaper
	const innerW = paper.w - m.left - m.right
	const footerY = paper.h - m.bottom - text.producer.height
	const gap = 3

	text.title.x = m.left
	text.title.y = m.top
	text.title.width = innerW
	text.explain.x = m.left
	text.explain.y = text.title.y + text.title.height + 1
	text.explain.width = innerW

	if (mode === 'landscape') {
		data.map.x = m.left
		data.map.y = text.explain.y + text.explain.height + gap
		data.map.height = footerY - data.map.y - gap
		data.map.width = innerW - data.legend.width - gap
		data.legend.x = data.map.x + data.map.width + gap
		data.legend.y = data.map.y
		data.legend.height = data.map.height
	} else {
		data.map.x = m.left
		data.map.y = text.explain.y + text.explain.height + gap
		data.map.width = innerW
		data.legend.x = m.left
		data.legend.y = footerY - data.legend.height - gap
		data.legend.width = innerW
		data.map.height = data.legend.y - data.map.y - gap
	}

	data.thumbnial.x = data.map.x + 2
	data.thumbnial.y = data.map.y + 2
	factor.compass.x = data.map.x + data.map.width - factor.compass.width - 3
	factor.compass.y = data.map.y + 3
	factor.plottingScale.x = data.map.x + 3
	factor.plottingScale.y = data.map.y + data.map.height - factor.plottingScale.height - 3

	text.producer.x = m.left
	text.producer.y = footerY
	text.explain3.x = text.producer.x + text.producer.width + 4
	text.explain3.y = footerY
	text.time.x = paper.w - m.right - text.time.width
	text.time.y = footerY
	text.explain2.x = Math.min(text.explain3.x + text.explain3.width + 4, text.time.x - text.explain2.width - 4)
	text.explain2.y = footerY
}

layoutSheet('A4_a', 'landscape')
layoutSheet('A4_c', 'portrait')
layoutSheet('A3_a', 'landscape')
layoutSheet('A3_c', 'portrait')
