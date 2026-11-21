/**
 * 渔港
 */

export const fishPortHeaders = [
	// { prop: 'COASTNAME', label: '岸段名称', minWidth: 120 },
	{ label: '序号', prop: 'number', width: 80, fixed: 'left' },
	{ label: '站点编号', prop: 'code', width: 120 },
	{ label: '地点名称', prop: 'name', width: 180 },
	{ label: '渔港名称', prop: 'coastal', width: 180 },
	{ label: '计算点经度', prop: 'startLng', width: 175 },
	{ label: '计算点纬度', prop: 'startLat', width: 175 },
	{ label: '隐患等级', prop: 'level', width: 125 },
	{ label: '服务地址', prop: 'serveUrl', slot: 'serveUrl', minWidth: 380 },
	{ label: '隐患类型', prop: 'yhType', slot: 'yhType', width: 110 },
	{ label: '整治前图片', prop: 'img1', slot: 'img1', width: 100 },
	{ label: '整治后图片', prop: 'img2', slot: 'img2', width: 100 },
	// { label: '雨量(ml)', prop: 'rainfall', width: 80 },
	{ label: '操作', prop: '', slot: 'action', width: 120 }
]

export const fishPortDatas = shoreName => {
	let coords = [
		[119.137843, 25.1654263],
		[119.3079608, 25.29763755],
		[119.518927, 25.6901633],
		[117.3123387, 23.75298673],
		[117.2377701, 23.6397234],
		[117.2380037, 23.63412764]
	]
	return [
		{
			number: '01',
			code: '5111',
			name: shoreName,
			coastal: shoreName,
			district: '平潭',
			association: '2',
			startLng: coords[0][0],
			startLat: coords[0][1],
			level: '三级隐患',
			rainfall: '20.1',
			yhType: '1',
			serveUrl: 'http://frp.dd0519.cn:10201/geoserver/tiger/wms',
			createTime: '2014-09-05'
		},
		{
			number: '02',
			code: '5112',
			name: shoreName,
			coastal: shoreName,
			district: '福州',
			association: '1',
			startLng: coords[1][0],
			startLat: coords[1][1],
			level: '二级隐患',

			rainfall: '20.1',
			yhType: '2',
			serveUrl: 'http://frp.dd0519.cn:10201/geoserver/tiger/wms',
			createTime: '2013-01-25'
		},
		{
			number: '03',
			code: '6101',
			name: shoreName,
			coastal: shoreName,
			district: '漳州',
			association: '1',
			startLng: coords[2][0],
			startLat: coords[2][1],
			level: '一级隐患',

			rainfall: '20.1',
			yhType: '3',
			serveUrl: 'http://frp.dd0519.cn:10201/geoserver/tiger/wms',
			createTime: '2014-09-05'
		},
		{
			number: '04',
			code: '5111',
			name: shoreName,
			coastal: shoreName,
			district: '福州',
			association: '2',
			startLng: coords[3][0],
			startLat: coords[3][1],
			level: '三级隐患',
			rainfall: '20.1',
			yhType: '4',
			serveUrl: 'http://frp.dd0519.cn:10201/geoserver/tiger/wms',
			createTime: '2019-08-15'
		},
		{
			number: '05',
			code: '5111',
			name: shoreName,
			coastal: shoreName,
			district: '厦门',
			association: '1',
			startLng: coords[4][0],
			startLat: coords[4][1],
			level: '二级隐患',

			rainfall: '20.1',
			yhType: '4',
			serveUrl: 'http://frp.dd0519.cn:10201/geoserver/tiger/wms',
			createTime: '2014-09-05'
		},
		{
			number: '06',
			code: '6101',
			name: shoreName,
			coastal: shoreName,
			district: '宁德',
			association: '1',
			startLng: coords[5][0],
			startLat: coords[5][1],
			level: '一级隐患',
			rainfall: '20.1',
			yhType: '5',
			serveUrl: 'http://frp.dd0519.cn:10201/geoserver/tiger/wms',
			createTime: '2020-02-28'
		}
	]
}
