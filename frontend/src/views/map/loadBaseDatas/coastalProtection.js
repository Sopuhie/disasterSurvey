
/**
 * 海岸防护工程
 */

export const coastalProtectionHeaders = [
    { label: '序号', prop: 'number', width: 80, fixed: 'left' },
    { label: '堤防代码', prop: 'code', width: 120 },
    { label: '岸段名称', prop: 'name', width: 160 },
    { label: '起点经度', prop: 'startLng', width: 120 },
    { label: '起点纬度', prop: 'startLat', width: 120 },
    { label: '终点经度', prop: 'endLng', width: 120 },
    { label: '终点纬度', prop: 'endLat', width: 120 },
    { label: '堤顶宽度(m)', prop: 'width', width: 120 },
    { label: '堤顶高程(m)', prop: 'height', width: 120 },
    { label: '堤顶沉降(m)', prop: 'descend', width: 120 },
    { label: '堤防长度(km)', prop: 'length', width: 120 },
    { label: '创建时间', prop: 'createTime', width: 180 },
    { label: '建设标准', prop: '', slot: 'status', width: 120 },
    { label: '效果评估', prop: 'effection', width: 160 },
    { label: '操作', prop: '', slot: 'action', width: 120, fixed: 'right' }
]

export const coastalProtectionDatas = (shoreName) => {
    return [
		{
			number: '01',
			code: 'HD93555612',
			name: '北江海堤',
			startLng: '119.137843',
			startLat: '25.1654263',
			endLng: '119.135062',
			endLat: '25.1588848',
			width: '55.03',
            height: '7.18',
            descend: '0.77',
            length: '24.43',
			createTime: '2018-07-02',
			status: '1',
            effection: '合格'
		},
		{
			number: '02',
			code: 'HD93555613',
			name: '亭后海堤',
			startLng: '119.3079608',
			startLat: '25.29763755',
			endLng: '119.332726',
			endLat: '25.2852972',
			width: '92.98',
            height: '5.72',
            descend: '1.23',
            length: '5.92',
			createTime: '2021-11-19',
			status: '2',
            effection: '良好'
		},
		{
			number: '03',
			code: 'HD93555614',
			name: '城头海堤',
			startLng: '119.518927',
			startLat: '25.6901633',
			endLng: '119.554405',
			endLat: '25.6944985',
			width: '14.73',
            height: '3.72',
            descend: '0.45',
            length: '14.65',
			createTime: '2015-08-06',
			status: '3',
            effection: '合格'
		},
		{
			number: '04',
			code: 'HD93555615',
			name: '东梧海堤',
			startLng: '117.3123387',
			startLat: '23.75298673',
			endLng: '117.3051723',
			endLat: '23.7431167',
			width: '12.89',
            height: '2.06',
            descend: '0.69',
            length: '3.49',
			createTime: '2012-06-18',
			status: '4',
            effection: '良好'
		},
		{
			number: '05',
			code: 'HD93555616',
			name: '前江海堤悬钟段',
			startLng: '117.2377701',
			startLat: '23.6397234',
			endLng: '117.238003',
			endLat: '23.634127',
			width: '25.94',
            height: '4.41',
            descend: '0.35',
            length: '51.78',
			createTime: '2018-02-13',
			status: '1',
            effection: '良好'
		},
        {
			number: '06',
			code: 'HD93555617',
			name: '甲线头海堤',
			startLng: '117.2380037',
			startLat: '23.63412764',
			endLng: '117.2293277',
			endLat: '23.63003101',
			width: '50.36',
            height: '4.73',
            descend: '0.57',
            length: '31.17',
			createTime: '2019-12-23',
			status: '1',
            effection: '合格'
		},
        {
			number: '07',
			code: 'HD93555618',
			name: '三赤海堤',
			startLng: '119.9729535',
			startLat: '26.6574041',
			endLng: '119.9665966',
			endLat: '26.6508597',
			width: '65.86',
            height: '5.94',
            descend: '0.61',
            length: '34.32',
			createTime: '2022-09-20',
			status: '1',
            effection: '良好'
		}
	]
}