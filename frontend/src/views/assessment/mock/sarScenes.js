export const sarScenes = [
	{
		id: 'sar-wind-2024',
		imageId: 'sar-wind-2024',
		name: '闽江口—平潭高清（海面风场）',
		date: '2024-09-16',
		sensor: '孔径雷达卫星',
		type: 'wind',
		maxWind: 32.6,
		direction: 'ENE'
	},
	{
		id: 'sar-storm-2024',
		imageId: 'typhoon-2024',
		name: '闽东沿海高清（风暴潮灾后）',
		date: '2024-09-17',
		sensor: '孔径雷达卫星',
		type: 'storm',
		inundationRatio: 0.48
	},
	{
		id: 'sar-storm-2023',
		imageId: 'storm-2023',
		name: '闽南沿海高清（风暴潮灾后）',
		date: '2023-07-28',
		sensor: '孔径雷达卫星',
		type: 'storm',
		inundationRatio: 0.41
	},
	{
		id: 'sar-wave-2024',
		imageId: 'sar-wave-2024',
		name: '平潭—莆田高清（海面波浪场）',
		date: '2024-09-16',
		sensor: '孔径雷达卫星',
		type: 'wave',
		maxHeight: 4.8,
		period: 9.2,
		maxSpeed: 13.5
	},
	{
		id: 'sar-wind-2023',
		imageId: 'sar-wind-2024',
		name: '闽江口—平潭高清（2023风场）',
		date: '2023-07-22',
		sensor: '孔径雷达卫星',
		type: 'wind',
		maxWind: 28.1,
		direction: 'ESE'
	},
	{
		id: 'sar-wave-2023',
		imageId: 'sar-wave-2024',
		name: '平潭—莆田高清（2023波浪场）',
		date: '2023-07-22',
		sensor: '孔径雷达卫星',
		type: 'wave',
		maxHeight: 3.6,
		period: 8.1,
		maxSpeed: 10.8
	}
]

export const typhoonEvents = [
	{
		id: 'TY2414',
		name: '贝碧嘉',
		year: 2024,
		level: '强台风',
		landfall: '福清沿海',
		maxWind: 38,
		points: [
			{ lng: 123.2, lat: 25.8, time: '09-16 02:00', wind: 28, dir: 'ENE' },
			{ lng: 121.6, lat: 25.4, time: '09-16 08:00', wind: 35, dir: 'ENE' },
			{ lng: 119.8, lat: 25.6, time: '09-16 14:00', wind: 38, dir: 'E' },
			{ lng: 118.9, lat: 26.1, time: '09-16 20:00', wind: 32, dir: 'ESE' }
		]
	},
	{
		id: 'TY2305',
		name: '杜苏芮',
		year: 2023,
		level: '超强台风',
		landfall: '晋江沿海',
		maxWind: 52,
		points: [
			{ lng: 122.8, lat: 23.9, time: '07-27 08:00', wind: 42, dir: 'NW' },
			{ lng: 120.4, lat: 24.4, time: '07-27 14:00', wind: 50, dir: 'WNW' },
			{ lng: 118.6, lat: 24.9, time: '07-27 20:00', wind: 46, dir: 'WNW' }
		]
	}
]
