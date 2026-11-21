import { center } from '@turf/turf'

/** 与 vite base（config.json 的 prefix）一致，部署后改 prefix 仍需重新打包 */
const assetBase = (import.meta.env.BASE_URL || '/').replace(/\/$/, '')
const assetUrl = path => `${assetBase}${path.startsWith('/') ? path : '/' + path}`

/**
 * 初始化底图信息
 */
export const MapDefaultOption = {
	viewOptions: {
		projection: 'EPSG:4326',
		center: [118.762572, 26.143884],
		// 局里现场
		// extent: [115.209594, 22.803628, 123.06524, 28.267319],
		zoom: 7.5,
		minZoom: 1,
		maxZoom: 13
	}
}

/**
 * 其他资源
 */
export const OtherResources = [
	{
		name: '土地利用',
		type: 'arcgisrest',
		lyrIndexs: [0],
		center: [119.306239, 26.079698],
		url: 'https://frp.dd0519.cn:6443/arcgis/rest/services/FJ/fuzhoudata/MapServer'
	},
	{
		name: '人口情况',
		type: 'arcgisrest',
		lyrIndexs: [1],
		center: [119.306239, 26.079698],
		url: 'https://frp.dd0519.cn:6443/arcgis/rest/services/FJ/fuzhoudata/MapServer'
	},
	{
		name: '经济情况',
		type: 'arcgisrest',
		lyrIndexs: [2],
		center: [119.306239, 26.079698],
		url: 'https://frp.dd0519.cn:6443/arcgis/rest/services/FJ/fuzhoudata/MapServer'
	},
	{
		name: '详细规划',
		type: 'arcgisrest',
		lyrIndexs: [3],
		center: [119.306239, 26.079698],
		url: 'https://frp.dd0519.cn:6443/arcgis/rest/services/FJ/fuzhoudata/MapServer'
	},
]

export const DistrictNames = [
	{
		value: '福州市',
		label: '福州市',
		children: [
			{
				value: '鼓楼区',
				label: '鼓楼区'
			},
			{
				value: '台江区',
				label: '台江区'
			},
			{
				value: '仓山区',
				label: '仓山区'
			},
			{
				value: '马尾区',
				label: '马尾区'
			},
			{
				value: '晋安区',
				label: '晋安区'
			},
			{
				value: '福清市',
				label: '福清市'
			},
			{
				value: '长乐市',
				label: '长乐市'
			},
			{
				value: '闽侯县',
				label: '闽侯县'
			},
			{
				value: '连江县',
				label: '连江县'
			},
			{
				value: '罗源县',
				label: '罗源县'
			},
			{
				value: '闽清县',
				label: '闽清县'
			},
			{
				value: '永泰县',
				label: '永泰县'
			},
			{
				value: '平潭县',
				label: '平潭县'
			}
		]
	},
	{
		value: '厦门市',
		label: '厦门市',
		children: [
			{
				value: '思明区',
				label: '思明区'
			},
			{
				value: '海沧区',
				label: '海沧区'
			},
			{
				value: '湖里区',
				label: '湖里区'
			},
			{
				value: '集美区',
				label: '集美区'
			},
			{
				value: '同安区',
				label: '同安区'
			},
			{
				value: '翔安区',
				label: '翔安区'
			}
		]
	},
	{
		value: '莆田市',
		label: '莆田市',
		children: [
			{
				value: '城厢区',
				label: '城厢区'
			},
			{
				value: '涵江区',
				label: '涵江区'
			},
			{
				value: '荔城区',
				label: '荔城区'
			},
			{
				value: '秀屿区',
				label: '秀屿区'
			},
			{
				value: '仙游县',
				label: '仙游县'
			}
		]
	},
	{
		value: '三明市',
		label: '三明市',
		children: [
			{
				value: '梅列区',
				label: '梅列区'
			},
			{
				value: '三元区',
				label: '三元区'
			},
			{
				value: '永安市',
				label: '永安市'
			},
			{
				value: '明溪县',
				label: '明溪县'
			},
			{
				value: '清流县',
				label: '清流县'
			},
			{
				value: '宁化县',
				label: '宁化县'
			},
			{
				value: '大田县',
				label: '大田县'
			},
			{
				value: '尤溪县',
				label: '尤溪县'
			},
			{
				value: '沙县',
				label: '沙县'
			},
			{
				value: '将乐县',
				label: '将乐县'
			},
			{
				value: '泰宁县',
				label: '泰宁县'
			},
			{
				value: '建宁县',
				label: '建宁县'
			}
		]
	},
	{
		value: '泉州市',
		label: '泉州市',
		children: [
			{
				value: '鲤城区',
				label: '鲤城区'
			},
			{
				value: '丰泽区',
				label: '丰泽区'
			},
			{
				value: '洛江区',
				label: '洛江区'
			},
			{
				value: '泉港区',
				label: '泉港区'
			},
			{
				value: '石狮市',
				label: '石狮市'
			},
			{
				value: '晋江市',
				label: '晋江市'
			},
			{
				value: '南安市',
				label: '南安市'
			},
			{
				value: '惠安县',
				label: '惠安县'
			},
			{
				value: '安溪县',
				label: '安溪县'
			},
			{
				value: '永春县',
				label: '永春县'
			},
			{
				value: '德化县',
				label: '德化县'
			},
			{
				value: '金门县',
				label: '金门县'
			}
		]
	},
	{
		value: '漳州市',
		label: '漳州市',
		children: [
			{
				value: '芗城区',
				label: '芗城区'
			},
			{
				value: '龙文区',
				label: '龙文区'
			},
			{
				value: '龙海市',
				label: '龙海市'
			},
			{
				value: '云霄县',
				label: '云霄县'
			},
			{
				value: '漳浦县',
				label: '漳浦县'
			},
			{
				value: '诏安县',
				label: '诏安县'
			},
			{
				value: '长泰县',
				label: '长泰县'
			},
			{
				value: '东山县',
				label: '东山县'
			},
			{
				value: '南靖县',
				label: '南靖县'
			},
			{
				value: '平和县',
				label: '平和县'
			},
			{
				value: '华安县',
				label: '华安县'
			}
		]
	},
	{
		value: '南平市',
		label: '南平市',
		children: [
			{
				value: '延平区',
				label: '延平区'
			},
			{
				value: '邵武市',
				label: '邵武市'
			},
			{
				value: '武夷山市',
				label: '武夷山市'
			},
			{
				value: '建瓯市',
				label: '建瓯市'
			},
			{
				value: '建阳市',
				label: '建阳市'
			},
			{
				value: '顺昌县',
				label: '顺昌县'
			},
			{
				value: '浦城县',
				label: '浦城县'
			},
			{
				value: '光泽县',
				label: '光泽县'
			},
			{
				value: '松溪县',
				label: '松溪县'
			},
			{
				value: '政和县',
				label: '政和县'
			}
		]
	},
	{
		value: '龙岩市',
		label: '龙岩市',
		children: [
			{
				value: '新罗区',
				label: '新罗区'
			},
			{
				value: '漳平市',
				label: '漳平市'
			},
			{
				value: '长汀县',
				label: '长汀县'
			},
			{
				value: '永定县',
				label: '永定县'
			},
			{
				value: '上杭县',
				label: '上杭县'
			},
			{
				value: '武平县',
				label: '武平县'
			},
			{
				value: '连城县',
				label: '连城县'
			}
		]
	},
	{
		value: '宁德市',
		label: '宁德市',
		children: [
			{
				value: '蕉城区',
				label: '蕉城区'
			},
			{
				value: '福安市',
				label: '福安市'
			},
			{
				value: '福鼎市',
				label: '福鼎市'
			},
			{
				value: '霞浦县',
				label: '霞浦县'
			},
			{
				value: '古田县',
				label: '古田县'
			},
			{
				value: '屏南县',
				label: '屏南县'
			},
			{
				value: '寿宁县',
				label: '寿宁县'
			},
			{
				value: '周宁县',
				label: '周宁县'
			},
			{
				value: '柘荣县',
				label: '柘荣县'
			}
		]
	}
]

/**
 * 行政区划配置图层
 */
export const DistrictData = {
	界线: assetUrl('/datas/界线.json'),
	近海: assetUrl('/datas/近海.json'),
	蒙版: assetUrl('/datas/蒙版.json'),
	市界: assetUrl('/datas/市界.json'),
	行政区内陆轮廓: assetUrl('/datas/行政区内陆轮廓.json'),
	行政区划中心点: assetUrl('/datas/行政区划中心点.json')
}

/***
 * 测试数据
 */
export const DataNames = {
	海岸防护工程: assetUrl('/datas/海岸防护工程.json'),
	渔港: assetUrl('/datas/海渔港.json'),
	海水养殖区: assetUrl('/datas/海水养殖区.json'),
	滨海旅游区: assetUrl('/datas/滨海旅游区.json'),
	人口经济: assetUrl('/datas/人口经济.json'),

	漫堤分析: assetUrl('/datas/漫堤分析.json'),
	风暴潮岸段分析: assetUrl('/datas/风暴潮-岸段分析.json')
}

/**
 * 图例展示
 */
export const LegendList = [
	{
		name: '综合质量评价等值面',
		items: [
			{
				name: 'Ⅰ类',
				color: 'rgba(115, 178, 255, 1)'
			},
			{
				name: 'Ⅱ类',
				color: 'rgba(178, 221, 247, 1)'
			},
			{
				name: 'Ⅲ类',
				color: 'rgba(196, 196, 196, 1)'
			},
			{
				name: 'Ⅳ类',
				color: 'rgba(166, 150, 135, 1)'
			},
			{
				name: '劣Ⅳ类',
				color: 'rgba(111, 88, 67, 1)'
			}
		]
	},
	{
		name: '富营养化评价等值面',
		items: [
			{
				name: '轻度富营养化',
				color: 'rgba(255, 255, 0, 1)'
			},
			{
				name: '中度富营养化',
				color: 'rgba(255, 153, 0, 1)'
			},
			{
				name: '重度富营养化',
				color: 'rgba(255, 0, 0, 1)'
			}
		]
	}
]

/**
 * 图表展示点
 */
export const ChartPoints = [
	{
		name: '宁德市',
		point: [120.18996, 26.9411681]
	},
	{
		name: '福州市',
		point: [119.70364, 25.9355661]
	},
	{
		name: '莆田市',
		point: [119.23382, 25.3050041]
	},
	{
		name: '泉州市',
		point: [118.75987, 24.8351741]
	},
	{
		name: '厦门市',
		point: [118.42379, 24.433438]
	},
	{
		name: '漳州市',
		point: [117.754382, 23.932981]
	},
	{
		name: '平潭市',
		point: [119.862386, 25.472535]
	}
]

/**
 * 站位信息
 */
export const StationPoints = './data/testData.json'

/**
 * 站位图层
 */
export const StationLayer = 'StationLayer'
