const eChartOptions = {
	pie: {
		tooltip: { formatter: '{b} : {c}' },
		series: [
			{
				type: 'pie',
				radius: [20, '50%'],
				itemStyle: {
					borderRadius: 8
				},
				data: [
					// { value: 1048, name: 'Search Engine' }
				]
			}
		]
	},
	bar: {
		xAxis: {
			type: 'category',
			data: [
				// '德城区', '陵城区'
			]
		},
		yAxis: { type: 'value' },
		grid: {
			top: '12%',
			left: '1%',
			right: '10%',
			containLabel: true
		},
		series: [
			{
				data: [
					// 53238.16863712144, 46182.27680749743
				],
				type: 'bar'
			}
		],
		dataZoom: [{ show: false }, { type: 'inside' }, { show: false }]
	},
	line: {
		tooltip: { formatter: '{b} : {c}' },
		xAxis: {
			type: 'category',
			data: [
				// 'Mon', 'Tue'
			]
		},
		yAxis: { type: 'value' },
		series: [
			{
				data: [
					// 150, 230
				],
				type: 'line'
			}
		]
	}
}

export { eChartOptions }
