import { initMapData, initPaper } from '@/views/exportMap/modules/constant.js'
import { useExportMapStore } from '@/store/modules/exportMap.js'
const exportMapStore = useExportMapStore()

const symbolsOptions = [
	{ label: '大', value: '大' },
	{ label: '中', value: '中' },
	{ label: '小', value: '小' }
]

/** 填充常用色（面板直接罗列） */
const commonFillColors = [
	'#FFFFFF',
	'#000000',
	'#F5222D',
	'#FA8C16',
	'#FADB14',
	'#52C41A',
	'#13C2C2',
	'#1890FF',
	'#2F54EB',
	'#722ED1',
	'#EB2F96',
	'#8C8C8C',
	'#FFF1F0',
	'#FFF7E6',
	'#FEFFE6',
	'#F6FFED',
	'#E6FFFB',
	'#E6F7FF',
	'#F0F5FF',
	'#F9F0FF'
]

/** 符号标注：固定矢量图标（定位点、五角星等） */
const fixedSymbolList = [
	{
		id: 'pin',
		label: '定位点',
		color: '#F64336',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><path d="M448 981.333a64 42.667 0 1 0 128 0 64 42.667 0 1 0-128 0z" fill="#A93529"/><path d="M554.667 593.941v376.726A10.667 10.667 0 0 1 544 981.333h-64a10.667 10.667 0 0 1-10.667-10.666V593.94c-144.682-20.757-256-144.853-256-295.274C213.333 133.717 347.051 0 512 0s298.667 133.717 298.667 298.667c0 150.421-111.318 274.517-256 295.274z" fill="#F64336"/><path d="M426.667 298.667a85.333 85.333 0 1 0 170.666 0 85.333 85.333 0 1 0-170.666 0z" fill="#FFFFFF"/></svg>`
	},
	{
		id: 'star',
		label: '五角星',
		color: '#FA8C16',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><path d="M512 64l126.4 256.8L928 358.4l-208 202.4L768 896 512 761.6 256 896l48-335.2L96 358.4l289.6-37.6L512 64z" fill="#FA8C16"/></svg>`
	},
	{
		id: 'circle',
		label: '圆形',
		color: '#096DD9',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><circle cx="512" cy="512" r="360" fill="#096DD9"/><circle cx="512" cy="512" r="200" fill="#FFFFFF"/></svg>`
	},
	{
		id: 'triangle',
		label: '三角形',
		color: '#389E0D',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><path d="M512 128L896 864H128L512 128z" fill="#389E0D"/></svg>`
	},
	{
		id: 'square',
		label: '方形',
		color: '#722ED1',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><rect x="192" y="192" width="640" height="640" rx="48" fill="#722ED1"/></svg>`
	},
	{
		id: 'diamond',
		label: '菱形',
		color: '#13C2C2',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><path d="M512 96L928 512 512 928 96 512 512 96z" fill="#13C2C2"/></svg>`
	},
	{
		id: 'flag',
		label: '旗帜',
		color: '#EB2F96',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><path d="M256 96v832h64V608h448L640 384l128-224H320V96h-64z" fill="#EB2F96"/></svg>`
	}
]

/** 图形标注：固定绘制类型 */
const fixedGraphicsList = [
	{
		id: 'point',
		label: '绘制点',
		value: 'Point',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><circle cx="512" cy="512" r="220" fill="#276FD3"/><circle cx="512" cy="512" r="80" fill="#FFFFFF"/></svg>`
	},
	{
		id: 'line',
		label: '绘制线',
		value: 'LineString',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><path d="M160 768L384 384l192 192L864 192" stroke="#276FD3" stroke-width="72" fill="none" stroke-linecap="round" stroke-linejoin="round"/><circle cx="160" cy="768" r="64" fill="#276FD3"/><circle cx="864" cy="192" r="64" fill="#276FD3"/></svg>`
	},
	{
		id: 'polygon',
		label: '绘制面',
		value: 'Polygon',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><path d="M192 704L352 256h320l160 448H192z" fill="rgba(39,111,211,0.35)" stroke="#276FD3" stroke-width="48"/></svg>`
	},
	{
		id: 'circle',
		label: '绘制圆',
		value: 'Circle',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><circle cx="512" cy="512" r="320" fill="rgba(39,111,211,0.25)" stroke="#276FD3" stroke-width="48"/></svg>`
	},
	{
		id: 'box',
		label: '绘制矩形',
		value: 'Box',
		svg: `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="32" height="32"><rect x="192" y="256" width="640" height="512" rx="24" fill="rgba(39,111,211,0.25)" stroke="#276FD3" stroke-width="48"/></svg>`
	}
]

const svgToDataUrl = svg => 'data:image/svg+xml;utf8,' + encodeURIComponent(svg)

const legendIconList = [
	{ id: 1, label: 'legend_1', value: 'legend_1' },
	{ id: 2, label: 'legend_2', value: 'legend_2' },
	{ id: 3, label: 'legend_3', value: 'legend_3' },
	{ id: 4, label: 'legend_4', value: 'legend_4' },
	{ id: 5, label: 'legend_5', value: 'legend_5' }
]

// 文本模块
const textModules = [
	{
		id: 1,
		label: '标题',
		prop: 'title'
	},
	{
		id: 2,
		label: '制作人',
		prop: 'producer'
	},
	{
		id: 6,
		label: '制图单位',
		prop: 'explain3'
	},
	{
		id: 3,
		label: '打印时间',
		prop: 'time'
	},
	{
		id: 4,
		label: '说明1',
		prop: 'explain'
	},
	{
		id: 5,
		label: '说明2',
		prop: 'explain2'
	}
]
// 图素模块
const mapFactorModules = [
	{
		id: 1,
		label: '指北针',
		prop: 'compass'
	},
	{
		id: 2,
		label: '比例尺',
		prop: 'plottingScale',
		fontSize: 16
	},
	{
		id: 3,
		label: '图例',
		prop: 'legend'
	}
	// {
	// 	id: 4,
	// 	label: '轮廓',
	// 	prop: 'outline'
	// }
]
// 纸张尺寸
const paperTypes = [
	{
		label: 'A4横版',
		value: 'A4_a'
	},
	{
		label: 'A4竖版',
		value: 'A4_c'
	},
	{
		label: 'A3横版',
		value: 'A3_a'
	},
	{
		label: 'A3竖版',
		value: 'A3_c'
	}
]
// 刻度尺单位
const scaleUnits = [
	{
		label: '米',
		value: 1
	},
	{
		label: '千米',
		value: 2
	}
]
// 图例规模
const lengthList = [
	{
		label: '方案1(4列,6行)',
		value: '4,6'
	},
	{
		label: '方案2(4列,4行)',
		value: '4,4'
	},
	{
		label: '方案3(3列,6行)',
		value: '3,6'
	},
	{
		label: '方案4(2列,4行)',
		value: '2,4'
	}
]
// 图例
const legendTypes = [
	{
		id: 1,
		label: '横排图例'
	},
	{
		id: 2,
		label: '竖排图例'
	}
]

const mmToPx = (...args) => {
	const dpi = 300
	return args.map(num => Math.round(((Number(num) || 0) / 25.4) * dpi))
}
// fontSize换算
const ptToPx = num => {
	// 屏幕DPI分辨率（像素/英寸）
	const dpi = 300
	return Math.round((num / 70) * dpi)
}
const pxToMm = (...args) => {
	const dpi = 300
	return args.map(num => Math.round((num * 25.4) / dpi))
}

const resetFormData = () => {
	const initFormDataItem = initMapData['A4_a']
	const newFormData = {
		name: '',
		paper: {
			...initPaper,
			type: 'A4_a'
		},
		map: {
			...initFormDataItem.map
		},
		thumbnial: {
			...initFormDataItem.thumbnial
		},
		text: {
			modules: textModules,
			title: {
				...initFormDataItem.text.title,
				opacity: 100
			},
			producer: {
				...initFormDataItem.text.producer,
				opacity: 100
			},
			time: {
				...initFormDataItem.text.time,
				opacity: 100
			},
			explain: {
				...initFormDataItem.text.explain,
				opacity: 100
			},
			explain2: {
				...initFormDataItem.text.explain2,
				opacity: 100
			},
			explain3: {
				...initFormDataItem.text.explain3,
				opacity: 100
			}
		},
		mapFactor: {
			modules: mapFactorModules,
			compass: {
				...initFormDataItem.mapFactor.compass,
				// value: compassImage,
				value: '',
				opacity: 100
			},
			plottingScale: {
				...initFormDataItem.mapFactor.plottingScale
			},
			legend: {
				...initFormDataItem.legend,
				type: 1,
				opacity: 100
			}
		}
	}
	exportMapStore.setFormData({ type: 'all', data: newFormData })
}

export {
	symbolsOptions,
	commonFillColors,
	fixedSymbolList,
	fixedGraphicsList,
	svgToDataUrl,
	legendIconList,
	textModules,
	mapFactorModules,
	paperTypes,
	scaleUnits,
	lengthList,
	legendTypes,
	mmToPx,
	ptToPx,
	pxToMm,
	resetFormData
}
