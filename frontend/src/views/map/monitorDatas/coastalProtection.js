
/**
 * 海岸防护工程
 */

export const coastalProtectionHeaders = [
    { label: '站点编号', prop: 'code', width: 120, fixed: 'left' },
    { label: '站点名称', prop: 'name', width: 120 },
    { label: '海堤名称', prop: 'coastal', width: 180 },
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

// export const coastalProtectionDatas = (shoreName) => {
//     return [
// 		{
// 			id: '01',
// 			code: 'SJ894512D32',
// 			name: '琯头',
//             coastal: shoreName,
// 			createTime: '2023-12-08 08:43:19',
// 			tidalevel: 'Ⅲ',
// 			watertemp: '16.3',
// 			salinity: '35',
// 			wind: '3.6',
// 			temp: '18.3',
// 			pres: '1.013',
// 			rainfall: '24.9',
// 			visibility: '20km'
// 		},
// 		{
// 			id: '02',
// 			code: 'SJ8G345802',
// 			name: '石码',
//             coastal: shoreName,
// 			createTime: '2023-12-08 08:44:11',
// 			tidalevel: 'Ⅱ',
// 			watertemp: '15.2',
// 			salinity: '34.7',
// 			wind: '4.2',
// 			temp: '19.7',
// 			pres: '1.012',
// 			rainfall: '0',
// 			visibility: '13'
// 		},
// 		{
// 			id: '03',
// 			code: 'SJ84357S82',
// 			name: '梅花',
//             coastal: shoreName,
// 			createTime: '2023-11-12 12:26:12',
// 			tidalevel: 'Ⅳ',
// 			watertemp: '18.8',
// 			salinity: '36',
// 			wind: '4.4',
// 			temp: '21.1',
// 			pres: '1.014',
// 			rainfall: '4.3',
// 			visibility: '13'
// 		},
// 		{
// 			id: '04',
// 			code: 'SJ875425Y4',
// 			name: '长门',
//             coastal: shoreName,
// 			createTime: '2023-11-11 15:27:44',
// 			tidalevel: 'Ⅱ',
// 			watertemp: '17.3',
// 			salinity: '31',
// 			wind: '5.1',
// 			temp: '17.6',
// 			pres: '1.010',
// 			rainfall: '7.5',
// 			visibility: '17.9m'
// 		},
// 		{
// 			id: '05',
// 			code: 'SJG347234T1',
// 			name: '崇武',
//             coastal: shoreName,
// 			createTime: '2023-11-27 12:25:32',
// 			tidalevel: 'Ⅲ',
// 			watertemp: '14.5',
// 			salinity: '32.6',
// 			wind: '6.2',
// 			temp: '16.7',
// 			pres: '1.021',
// 			rainfall: '6.3',
// 			visibility: '16'
// 		},
// 		{
// 			id: '06',
// 			code: 'SJ8T34569',
// 			name: '碧里',
//             coastal: shoreName,
// 			createTime: '2023-11-13 08:38:14',
// 			tidalevel: 'Ⅲ',
// 			watertemp: '15.8',
// 			salinity: '32',
// 			wind: '4.9',
// 			temp: '15.8',
// 			pres: '1.012',
// 			rainfall: '21.4',
// 			visibility: '16'
// 		},
// 		{
// 			id: '07',
// 			code: 'SJ88672347',
// 			name: '漳湾',
//             coastal: shoreName,
// 			createTime: '2023-12-12 07:12:24',
// 			tidalevel: 'Ⅳ',
// 			watertemp: '13.6',
// 			salinity: '29',
// 			wind: '5.8',
// 			temp: '15.4',
// 			pres: '1.011',
// 			rainfall: '21.4',
// 			visibility: '14'
// 		},
// 		{
// 			id: '08',
// 			code: 'SJ8A34521',
// 			name: '宫前',
//             coastal: shoreName,
// 			createTime: '2023-12-02 09:12:41',
// 			tidalevel: 'Ⅱ',
// 			watertemp: '17.2',
// 			salinity: '32',
// 			wind: '4.7',
// 			temp: '16.7',
// 			pres: '1.011',
// 			rainfall: '20.1',
// 			visibility: '18'
// 		}
// 	]
// }

export const coastalProtectionDatas = (shoreName) => {
  const stationNames = [
    { name: '琯头', code: 'SJ894512D32' },
    { name: '石码', code: 'SJ8G345802' },
    { name: '梅花', code: 'SJ84357S82' },
    { name: '长门', code: 'SJ875425Y4' },
    { name: '崇武', code: 'SJG347234T1' },
    { name: '碧里', code: 'SJ8T34569' },
    { name: '漳湾', code: 'SJ88672347' },
    { name: '宫前', code: 'SJ8A34521' }
  ];

  const tideLevels = ['Ⅰ', 'Ⅱ', 'Ⅲ', 'Ⅳ', 'Ⅴ'];

  // 生成随机数函数
  const randomInRange = (min, max, decimals = 1) => {
    return (Math.random() * (max - min) + min).toFixed(decimals);
  };

  // 获取当前时间
  const now = new Date();

  const data = [];

  for (let i = 0; i < 1000; i++) {
    // 时间逐时递减
    const time = new Date(now.getTime() - i * 60 * 60 * 1000);
    const createTime = time.toISOString().replace('T', ' ').substring(0, 19);

    // 随机选择站点
    const station = stationNames[Math.floor(Math.random() * stationNames.length)];

    // 生成随机数据
    const tideLevel = tideLevels[Math.floor(Math.random() * tideLevels.length)];
    const waterTemp = randomInRange(10, 25); // 水温 10-25°C
    const salinity = randomInRange(28, 36); // 盐度 28-36
    const wind = randomInRange(1, 8); // 风速 1-8 m/s
    const temp = randomInRange(15, 30); // 气温 15-30°C
    const pres = randomInRange(1.000, 1.030, 3); // 气压 1.000-1.030
    const rainfall = randomInRange(0, 30); // 降雨量 0-30mm
    const visibility = Math.random() > 0.5 ?
      `${randomInRange(5, 20, 0)}km` :
      `${randomInRange(50, 20000, 0)}m`; // 能见度 5-20km 或 50-20000m

    data.push({
      id: (i + 1).toString().padStart(3, '0'), // 三位数ID
      code: station.code,
      name: station.name,
      coastal: shoreName,
      createTime: createTime,
      tidalevel: tideLevel,
      watertemp: waterTemp,
      salinity: salinity,
      wind: wind,
      temp: temp,
      pres: pres,
      rainfall: rainfall,
      visibility: visibility
    });
  }

  return data;
};