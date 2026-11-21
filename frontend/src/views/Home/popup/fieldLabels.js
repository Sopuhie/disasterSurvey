/**
 * 承灾体地图弹窗：英文属性名 → 中文标签
 * （灾情调查 mock / 运维 schemas / 旧图层字段一并覆盖）
 */
export const HAZARD_FIELD_LABELS = {
	// 通用 / 旧图层
	id: '编号',
	ID: '编号',
	FID: '编号',
	No: '编号',
	dangerName: '名称',
	name: '名称',
	type: '类型',
	division: '行政区',
	town: '乡镇',
	lon: '经度',
	lat: '纬度',
	longtitude: '经度',
	longitude: '经度',
	latitude: '纬度',
	Shape_Leng: '长度',
	Shape_Area: '面积',
	length: '长度',
	lengthKm: '长度(km)',
	area: '统计面积',
	areaKm2: '面积(km²)',
	method: '养殖方式',
	host: '拥有者',
	manager: '管护单位',
	Management: '管理单位',
	status: '状态',
	builtYear: '建成年代',
	time: '建成时间',
	// 渔港
	berths: '泊位数',
	capacity: '容量',
	// 养殖
	species: '养殖品种',
	// 海堤（对齐 海岸防护工程.json）
	grade: '等级',
	standard: '防潮标准',
	Dam_elevat: '堤顶高程(m)',
	Return_per: '重现期(年)',
	HDXL: '海堤序列',
	面积: '面积',
	// 旅游
	level: '景区等级',
	// 灾情调查扩展（面积类监测/计划为万亩；海堤为 km）—— 与 metricUnits.SURVEY_LABELS 对齐
	monitorValue: '现状监测值(万亩)',
	planValue: '近期计划值(万亩)',
	monitorUnit: '监测值单位',
	planUnit: '计划值单位',
	disasterLevel: '灾害等级',
	disasterLevelLabel: '灾害等级名称',
	affectedPopulation: '受灾人口(人)',
	economicLoss: '经济损失(万元)',
	areaKm2: '受灾面积(km²)',
	areaWanMu: '受灾面积(万亩)',
	protectPop: '保护人口(人)',
	warnTide: '警戒潮位(m)',
	tideHeight: '潮高(m)',
	crestElev: '堤顶高程(m)',
	wallElev: '防浪墙高程(m)',
	warnLevel: '预警色',
	impactLevel: '影响等级',
	city: '所属市',
	county: '所属县',
	remark: '备注',
	hazardCode: '承灾体编码',
	code: '编码',
	_dataType: '数据类型'
}

/** 弹窗中不展示的内部字段 */
const HIDDEN_KEYS = new Set([
	'features',
	'geometry',
	'coordinates',
	'geojson',
	'fromSystem',
	'sourceFid',
	'sourceId',
	'_source',
	'__v_isRef',
	'__v_isReactive'
])

/**
 * 将 feature.properties 转为弹窗列表数据（中文标题，去重同名标签）
 * @param {Record<string, any>} properties
 * @param {Record<string, string>} [extraLabels] 弹窗专属补充映射
 */
export function buildPopupListData(properties = {}, extraLabels = {}) {
	const labels = { ...HAZARD_FIELD_LABELS, ...extraLabels }
	const usedLabels = new Set()
	const list = []

	for (const [key, value] of Object.entries(properties || {})) {
		if (HIDDEN_KEYS.has(key)) continue
		if (value !== null && typeof value === 'object') continue

		const title = labels[key]
		// 无中文映射则不展示（避免 remark / hazardCode / _source 等英文原文）
		if (!title) continue
		// 同中文标签只保留首次（避免 name / dangerName 重复两行「名称」）
		if (usedLabels.has(title)) continue
		usedLabels.add(title)

		list.push({ title, value, key })
	}
	return list
}
