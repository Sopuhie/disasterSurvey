/**
 * 渔港
 */

export const fishPortHeaders = [
	{ label: '序号', prop: 'number', width: 80, fixed: 'left' },
	{ label: '海鱼港代码', prop: 'code', width: 120 },
	{ label: '名称', prop: 'name', width: 160 },
	{ label: '经度', prop: 'startLng', width: 120 },
	{ label: '纬度', prop: 'startLat', width: 120 },
	{ label: '行政区', prop: 'division', width: 200 },
	{ label: '养殖方式', prop: 'method', width: 120 },
	{ label: '创建时间', prop: 'createTime', width: 180 },
	{ label: '建设标准', prop: 'level', slot: 'status', width: 120 },
	{ label: '效果评估', prop: 'effection', width: 160 },
	{ label: '操作', prop: '', slot: 'action', width: 120, fixed: 'right' }
]

export const fishPortDatas = shoreName => {
	return [
		{
			number: '1',
			code: 'YG093213',
			name: '屿头乡养殖单元',
			startLng: '119.851',
			startLat: '25.562',
			division: '福建省-福州市-平潭',
			method: '围海养殖',
			createTime: '2018-04-28',
			level: '二级',
			effection: '合格'
		},
		{
			number: '2',
			code: 'YG093214',
			name: '君山镇养殖单元',
			startLng: '119.632',
			startLat: '25.887',
			division: '福建省-福州市-马尾区',
			method: '围海养殖',
			createTime: '2015-08-01',
			level: '一级',
			effection: '良好'
		},
		{
			number: '3',
			code: 'YG093215',
			name: '海坛街道养殖单元',
			startLng: '119.787',
			startLat: '25.619',
			division: '福建省-福州市-罗源县',
			method: '开放式养殖',
			createTime: '2017-09-01',
			level: '三级',
			effection: '合格'
		},
		{
			number: '4',
			code: 'YG093216',
			name: '苏平镇养殖单元',
			startLng: '118.466',
			startLat: '24.625',
			division: '福建省-福州市-福清市',
			method: '开放式养殖',
			createTime: '2021-02-25',
			level: '二级',
			effection: '合格'
		},
		{
			number: '5',
			code: 'YG093217',
			name: '金井镇养殖单元',
			startLng: '117.318',
			startLat: '23.758',
			division: '福建省-漳州市-诏安县',
			method: '围海养殖',
			createTime: '2017-09-12',
			level: '三级',
			effection: '合格'
		},
		{
			number: '6',
			code: 'YG093218',
			name: '南海乡养殖单元',
			startLng: '119.004',
			startLat: '25.263',
			division: '福建省-漳州市-漳浦县',
			method: '围海养殖',
			createTime: '2019-04-12',
			level: '三级',
			effection: '良好'
		},
		{
			number: '7',
			code: 'YG093219',
			name: '鉴江镇南部养殖区',
			startLng: '119.626',
			startLat: '26.618',
			division: '福建省-漳州市-云霄县',
			method: '围海养殖',
			createTime: '2020-05-22',
			level: '二级',
			effection: '良好'
		}
	]
}
