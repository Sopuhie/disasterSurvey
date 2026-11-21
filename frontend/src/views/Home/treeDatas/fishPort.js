/**
 * 渔港
 */

export const fishPortDatas = shoreName => {
	return [
		{
			id: '0',
			label: '福建省',
			children: [
				{
					id: '1',
					label: '福州市',
					children: [
						{
							id: '屿头乡养殖单元',
							label: '屿头乡养殖单元'
						},
						{
							id: '君山镇养殖单元',
							label: '君山镇养殖单元'
						},
						{
							id: '海坛街道养殖单元',
							label: '海坛街道养殖单元'
						},
						{
							id: '苏平镇养殖单元',
							label: '苏平镇养殖单元'
						}
					]
				},
				{
					id: '2',
					label: '漳州市',
					children: [
						{
							id: '金井镇养殖单元',
							label: '金井镇养殖单元'
						},
						{
							id: '南海乡养殖单元',
							label: '南海乡养殖单元'
						},
						{
							id: '鉴江镇南部养殖区',
							label: '鉴江镇南部养殖区'
						}
					]
				}
			]
		}
	]
}
