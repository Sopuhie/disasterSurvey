
/**
 * 海岸防护工程
 */

export const coastalProtectionHeaders = [
    { label: '序号', prop: 'number', width: 80, fixed: 'left' },
    { label: '站点编号', prop: 'code', width: 120 },
    { label: '地点名称', prop: 'name', width: 120 },
    { label: '海堤名称', prop: 'coastal', width: 180 },
    { label: '计算点经度', prop: 'startLng', width: 175 },
    { label: '计算点纬度', prop: 'startLat', width: 175 },
    { label: '隐患等级', prop: 'level', width: 125 },
    { label: '服务地址', prop: 'serveUrl', slot: 'serveUrl', minWidth: 120 },
    { label: '隐患类型', prop: 'yhType', slot: 'yhType', width: 110 },
    { label: '整治前图片', prop: 'img1', slot: 'img1', width: 100 },
    { label: '整治后图片', prop: 'img2', slot: 'img2', width: 100 },
    // { label: '雨量(ml)', prop: 'rainfall', width: 80 },
    { label: '操作', prop: '', slot: 'action', width: 120 }
]

export const coastalProtectionDatas = (shoreName) => {
    let coords = [
        [119.52102836371087, 25.69048580790483],
        [119.53178073910982, 25.691987254390394],
        [119.53393801851568, 25.69229306520729],
        [119.55141472188804, 25.693915749675647],
        [119.55312861079827, 25.694104832931266],
        [119.55377361073917, 25.694439833447678],
    ];
    switch(shoreName) {
		case '城头海堤':
            break;
		case '三赤海堤':
            coords = [
                [119.97295352748336, 26.657404131795317],
                [119.97071306175849, 26.655087249776784],
                [119.96877437973387, 26.65317586577362],
                [119.96659666690277, 26.65085973468905],
                [119.97295352748336, 26.657404131795317],
                [119.96877437973387, 26.65317586577362]
            ];
            break;
		case '北江海堤':
            coords = [
                [119.10551505155605, 25.187912547564533],
                [119.10614081091897, 25.186653263842942],
                [119.10716353721575, 25.1845956608411],
                [119.10797141857044, 25.18298612085522],
                [119.10853092737078, 25.18186562803918],
                [119.10880558557376, 25.181304644023676],
            ]
            break;
		case '亭后海堤':
            coords = [
                [119.31920947137546, 25.291526304842293],
                [119.31938102584142, 25.291296278823765],
                [119.31977997720779, 25.291039805971483],
                [119.32027479149586, 25.29082630224677],
                [119.32047461614583, 25.29074047159522],
                [119.32060604464856, 25.290678779962537]
            ];
            break;
		case '东梧海堤':
            coords = [
                [117.31431770277779, 23.747001347222252],
                [117.30986924933141, 23.744847190320694],
                [117.30959748304548, 23.744826672845512],
                [117.30934979751231, 23.744765067408306],
                [117.30903502216896, 23.744434934361486],
                [117.30789444100711, 23.743908988794402]
            ];
            break;
		case '前江海堤悬钟段':
            coords = [
                [117.23777019215476, 23.639723050885387],
                [117.23778455833332, 23.639479022222226],
                [117.23786200555557, 23.63758489444446],
                [117.23800376017145, 23.634127646987054],
                [117.23778455833332, 23.639479022222226],
                [117.23777019215476, 23.639723050885387],
            ]
            break;
		case '甲线头海堤':
            coords = [
                [117.2323660861111, 23.63475055277779],
                [117.23222592992794, 23.63471347349101],
                [117.23213369286646, 23.634842418067915],
                [117.23206932439024, 23.634946298865806],
                [117.231793725, 23.635076902777765],
                [117.23165955009905, 23.635038931784194]
            ]
            break;
    }
    return [
		{
			number: '01',
			code: '5111',
			name: '六鳌',
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
			name: '古雷',
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
			name: '龙海',
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
			name: '三沙',
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
			name: '翔安',
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
			name: '沙埕',
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