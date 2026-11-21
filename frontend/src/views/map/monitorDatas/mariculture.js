
/**
 * 海水养殖区
 */

export const maricultureHeaders = [
    { label: '站点编号', prop: 'code', width: 120, fixed: 'left' },
    { label: '站点名称', prop: 'name', width: 220 },
    { label: '养殖区名称', prop: 'coastal', width: 220 },
    { label: '监测时间', prop: 'createTime', width: 180 },
    { label: '等级', prop: 'tidalevel', width: 80 },
    { label: '水温(℃)', prop: 'watertemp', width: 80 },
    { label: '盐度(‰)', prop: 'salinity', width: 80 },
    { label: '风(km/h)', prop: 'wind', width: 120 },
    { label: '温度(°C)', prop: 'temp', width: 120 },
    { label: '气压(10^5Pa)', prop: 'pres', width: 120 },
    { label: '雨量(mm)', prop: 'rainfall', width: 120 },
    { label: '能见度(km)', prop: 'visibility', width: 120 }
]

export const maricultureDatas = (shoreName) => {
    return [
		{
			id: '01',
			code: 'SJ894512D32',
            name: shoreName,
            coastal: shoreName,
			createTime: '2023-12-08 08:43:19',
			tidalevel: 'Ⅲ',
			watertemp: '16.3',
			salinity: '35',
			wind: '3.6',
			temp: '18.3',
			pres: '1.013',
			rainfall: '24.9',
			visibility: '20km'
		},
		{
			id: '02',
			code: 'SJ8G345802',
            name: shoreName,
            coastal: shoreName,
			createTime: '2023-12-08 08:44:11',
			tidalevel: 'Ⅱ',
			watertemp: '15.2',
			salinity: '34.7',
			wind: '4.2',
			temp: '19.7',
			pres: '1.012',
			rainfall: '0',
			visibility: '13'
		},
		{
			id: '03',
			code: 'SJ84357S82',
            name: shoreName,
            coastal: shoreName,
			createTime: '2023-11-12 12:26:12',
			tidalevel: 'Ⅳ',
			watertemp: '18.8',
			salinity: '36',
			wind: '4.4',
			temp: '21.1',
			pres: '1.014',
			rainfall: '4.3',
			visibility: '13'
		},
		{
			id: '04',
			code: 'SJ875425Y4',
            name: shoreName,
            coastal: shoreName,
			createTime: '2023-11-11 15:27:44',
			tidalevel: 'Ⅱ',
			watertemp: '17.3',
			salinity: '31',
			wind: '5.1',
			temp: '17.6',
			pres: '1.010',
			rainfall: '7.5',
			visibility: '17.9m'
		},
		{
			id: '05',
			code: 'SJG347234T1',
            name: shoreName,
            coastal: shoreName,
			createTime: '2023-11-27 12:25:32',
			tidalevel: 'Ⅲ',
			watertemp: '14.5',
			salinity: '32.6',
			wind: '6.2',
			temp: '16.7',
			pres: '1.021',
			rainfall: '6.3',
			visibility: '16'
		},
		{
			id: '06',
			code: 'SJ8T34569',
            name: shoreName,
            coastal: shoreName,
			createTime: '2023-11-13 08:38:14',
			tidalevel: 'Ⅲ',
			watertemp: '15.8',
			salinity: '32',
			wind: '4.9',
			temp: '15.8',
			pres: '1.012',
			rainfall: '21.4',
			visibility: '16'
		},
		{
			id: '07',
			code: 'SJ88672347',
            name: shoreName,
            coastal: shoreName,
			createTime: '2023-12-12 07:12:24',
			tidalevel: 'Ⅳ',
			watertemp: '13.6',
			salinity: '29',
			wind: '5.8',
			temp: '15.4',
			pres: '1.011',
			rainfall: '21.4',
			visibility: '14'
		},
		{
			id: '08',
			code: 'SJ8A34521',
            name: shoreName,
            coastal: shoreName,
			createTime: '2023-12-02 09:12:41',
			tidalevel: 'Ⅱ',
			watertemp: '17.2',
			salinity: '32',
			wind: '4.7',
			temp: '16.7',
			pres: '1.011',
			rainfall: '20.1',
			visibility: '18'
		}
	]
}