/**
 * 渔港
 */

export const fishPortHeaders = [
	{ label: '序号', prop: 'number', width: 80, fixed: 'left' },
	{ label: '站点编号', prop: 'code', width: 120 },
	{ label: '地点名称', prop: 'name', width: 180 },
	{ label: '渔港名称', prop: 'coastal', width: 180 },
	{ label: '计算点经度', prop: 'startLng', width: 120 },
	{ label: '计算点纬度', prop: 'startLat', width: 120 },
	{ label: '评价结果', prop: 'res', minWidth: 120 },
	{ label: '渔港状态', prop: '', slot: 'status', width: 100 },
	{ label: '操作', prop: '', slot: 'action', width: 160 }
]

export const fishPortDatas = shoreName => {
	return [
		{
			number: '01',
			code: '6101',
			name: shoreName,
			coastal: shoreName,
			startLng: '120.416670',
			startLat: '27.166670',
			res: '文字描述',
			status: '1'
		},
		{
			number: '02',
			code: '6101',
			name: shoreName,
			coastal: shoreName,
			startLng: '120.416670',
			startLat: '27.166670',
			res: '文字描述',
			status: '1'
		},
		{
			number: '03',
			code: '6101',
			name: shoreName,
			coastal: shoreName,
			startLng: '120.416670',
			startLat: '27.166670',
			res: '文字描述',
			status: '2'
		},
		{
			number: '04',
			code: '6101',
			name: shoreName,
			coastal: shoreName,
			startLng: '120.416670',
			startLat: '27.166670',
			res: '文字描述',
			status: '3'
		},
		{
			number: '05',
			code: '6101',
			name: shoreName,
			coastal: shoreName,
			startLng: '120.416670',
			startLat: '27.166670',
			res: '文字描述',
			status: '4'
		},
		{
			number: '06',
			code: '6101',
			name: shoreName,
			coastal: shoreName,
			startLng: '120.416670',
			startLat: '27.166670',
			res: '文字描述',
			status: '3'
		}
	]
}
