/**
 * 演示矢量目录（public/datas）。
 * 资源目录面板会将其与「目录树管理」下发的正式专题目录合并展示。
 */
import { prefix } from '@/config'

const dataUrl = name => (prefix ? '/' + prefix : '') + `/datas/${name}.json`

const centerPointStyle = feature => ({
	image: {
		rotateWithView: true,
		src:
			'data:image/svg+xml;utf8,' +
			encodeURIComponent(
				`<svg t="1735280097920" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="12121" width="12" height="12"><path d="M513.133 65.327c-247.538 0-448.208 200.67-448.208 448.207 0 247.539 200.67 448.208 448.208 448.208 247.54 0 448.208-200.67 448.208-448.208 0-247.539-200.668-448.207-448.208-448.207z m0 837.322c-214.902 0-389.114-174.213-389.114-389.114 0-214.903 174.212-389.115 389.113-389.115 214.904 0 389.116 174.212 389.116 389.115 0 214.901-174.212 389.114-389.116 389.114zM168.53 513.535a336.754 336.754 0 1 0 689.205 0 336.754 336.754 0 1 0-689.205 0z" fill="#e0620d" p-id="12122"></path></svg>`
			),
		imgSize: [30, 30],
		offset: [0, 0]
	},
	text: {
		font: 'normal 15px blue',
		fill: { color: 'white' },
		offsetY: 20,
		text: feature.properties?.label || ''
	}
})

const theme = (id, name, file, zIndex, lyrStyle) => ({
	id,
	themeId: id,
	name,
	layerName: `demo-${id}`,
	type: 'theme',
	disabled: false,
	demo: true,
	url: dataUrl(file),
	zIndex,
	lyrStyle
})

export const demoCatalogTree = [
	{
		id: 'demo-root',
		name: '演示数据',
		type: 'folder',
		disabled: false,
		childrens: [
			{
				id: 'demo-folder-base',
				name: '基础地理',
				type: 'folder',
				disabled: false,
				childrens: [
					theme('mask', '蒙版', '蒙版', 15, {
						fill: { color: 'rgba(0,0,0,0.35)' },
						stroke: { color: 'rgba(0,0,0,0.15)', width: 1 }
					}),
					theme('admin', '行政区划', '行政区划', 22, {
						fill: { color: 'rgba(24,144,255,0.08)' },
						stroke: { color: '#1890FF', width: 1.5 }
					}),
					theme('county', '县界', '县界', 24, {
						fill: { color: 'rgba(82,196,26,0.06)' },
						stroke: { color: '#52C41A', width: 1.2 }
					}),
					theme('city-line', '市界', '市界', 28, {
						stroke: { color: 'grey', width: 1.5 }
					}),
					theme('inland', '行政区内陆轮廓', '行政区内陆轮廓', 30, {
						stroke: { color: 'grey', width: 2 }
					}),
					theme('boundary', '界线', '界线', 32, {
						stroke: { color: 'rgba(0,0,200,0.8)', width: 1.5 }
					}),
					theme('nearshore', '近海', '近海', 34, {
						stroke: { color: 'rgba(0,0,200,0.8)', width: 1.5 }
					}),
					theme('center', '行政区划中心点', '行政区划中心点', 60, centerPointStyle)
				]
			},
			{
				id: 'demo-folder-hazard',
				name: '承灾体',
				type: 'folder',
				disabled: false,
				childrens: [
					theme('seawall', '海岸防护工程', '海岸防护工程', 50, {
						stroke: { color: 'rgba(24,144,255,1)', width: 3 }
					}),
					theme('fishport', '海渔港', '海渔港', 52, {
						fill: { color: 'rgba(204,136,0,0.25)' },
						stroke: { color: 'rgba(204,136,0,1)', width: 2 }
					}),
					theme('mariculture', '海水养殖区', '海水养殖区', 54, {
						fill: { color: 'rgba(82,196,26,0.25)' },
						stroke: { color: 'rgba(82,196,26,1)', width: 2 }
					}),
					theme('tourist', '滨海旅游区', '滨海旅游区', 56, {
						fill: { color: 'rgba(114,46,209,0.25)' },
						stroke: { color: 'rgba(114,46,209,1)', width: 2 },
						image: {
							radius: 7,
							fill: { color: 'rgba(114,46,209,0.85)' },
							stroke: { color: '#fff', width: 1 }
						}
					})
				]
			}
		]
	}
]

export const demoExpandedKeys = ['demo-root', 'demo-folder-base', 'demo-folder-hazard']

export function flattenDemoThemes(nodes = demoCatalogTree, acc = []) {
	nodes.forEach(item => {
		if (item.type === 'theme') acc.push(item)
		if (item.childrens?.length) flattenDemoThemes(item.childrens, acc)
	})
	return acc
}
