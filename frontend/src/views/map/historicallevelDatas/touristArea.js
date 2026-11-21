
/**
 * 滨海旅游区
 */

export const touristAreaHeaders = [
  { label: '序号', prop: 'number', width: 80, fixed: 'left' },
  { label: '站点编号', prop: 'code', width: 120 },
  { label: '地点名称', prop: 'name', width: 250 },
  { label: '滨海旅游区名称', prop: 'coastal', width: 250 },
  { label: '计算点经度', prop: 'startLng', width: 120 },
  { label: '计算点纬度', prop: 'startLat', width: 120 },
  { label: '评价结果', prop: 'res', minWidth: 120 },
  { label: '状态', prop: '', slot: 'status', width: 100 },
  { label: '操作', prop: '', slot: 'action', width: 160 }
]

export const touristAreaDatas = (shoreName) => {
  return [
		{
			number: '01',
			code: '6101',
			name: shoreName,
            coastal: shoreName,
			startLng: '118.66529',
			startLat: '24.665874',
			res: '文字描述',
			status: '1'
		},
		{
			number: '02',
			code: '6101',
			name: shoreName,
            coastal: shoreName,
			startLng: '118.667744',
			startLat: '24.79824',
			res: '文字描述',
			status: '1'
		},
		{
			number: '03',
			code: '6101',
			name: shoreName,
            coastal: shoreName,
			startLng: '119.007702',
			startLat: '24.662969',
			res: '文字描述',
			status: '2'
		},
		{
			number: '04',
			code: '6101',
			name: shoreName,
            coastal: shoreName,
			startLng: '118.959751',
			startLat: '24.876567',
			res: '文字描述',
			status: '3'
		},
		{
			number: '05',
			code: '6101',
			name: shoreName,
            coastal: shoreName,
			startLng: '119.77792',
			startLat: '24.936821',
			res: '文字描述',
			status: '4'
		},
		{
			number: '06',
			code: '6101',
			name: shoreName,
            coastal: shoreName,
			startLng: '119.783432',
			startLat: '25.179526',
			res: '文字描述',
			status: '3'
		}
	]
}