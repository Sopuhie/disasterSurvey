/**
 * 灾情调查基础数据 - 各类型表头 / 表单 / 搜索字段定义
 */
import { monitorPlanLabels, SURVEY_LABELS } from '@/shared/metricUnits.js'

const GEOJSON_COLUMN = { prop: '', slot: 'geojson', label: '空间数据', width: 90, align: 'center' }
const GEOJSON_FIELD = {
	prop: 'geojson',
	label: 'GeoJSON',
	type: 'textarea',
	defaultValue: '',
	placeholder:
		'粘贴 Feature / FeatureCollection / Geometry JSON，例如：{"type":"Point","coordinates":[119.53,26.2]}',
	rows: 8
}

const DISASTER_LEVEL_OPTIONS = [
	{ label: '一级', value: 1 },
	{ label: '二级', value: 2 },
	{ label: '三级', value: 3 },
	{ label: '四级', value: 4 }
]

/** 灾损核验列/表单：与 metricUnits.SURVEY_LABELS 对齐（海堤监测/计划为 km，其余为万亩） */
const buildVerifyColumns = typeId => {
	const labels = monitorPlanLabels(typeId)
	return [
		{ prop: 'monitorValue', label: labels.monitorColumn, width: 140, align: 'center' },
		{ prop: 'planValue', label: labels.planColumn, width: 140, align: 'center' },
		{ prop: 'disasterLevel', label: SURVEY_LABELS.disasterLevel, width: 90, align: 'center' },
		{ prop: 'affectedPopulation', label: SURVEY_LABELS.affectedPopulation, width: 110, align: 'center' },
		{ prop: 'economicLoss', label: SURVEY_LABELS.economicLoss, width: 130, align: 'center' }
	]
}

const buildVerifyFields = typeId => {
	const labels = monitorPlanLabels(typeId)
	return [
		{
			prop: 'monitorValue',
			label: labels.monitor,
			type: 'number',
			placeholder: `填 ${labels.unit}`
		},
		{
			prop: 'planValue',
			label: labels.plan,
			type: 'number',
			placeholder: `填 ${labels.unit}`
		},
		{
			prop: 'disasterLevel',
			label: SURVEY_LABELS.disasterLevel,
			type: 'select',
			options: DISASTER_LEVEL_OPTIONS
		},
		{ prop: 'affectedPopulation', label: SURVEY_LABELS.affectedPopulation, type: 'number' },
		{ prop: 'economicLoss', label: SURVEY_LABELS.economicLoss, type: 'number' }
	]
}

const withVerify = schema => {
	const columns = [...(schema.columns || [])]
	const verifyColumns = buildVerifyColumns(schema.id)
	const actionIdx = columns.findIndex(col => col.slot === 'action')
	if (actionIdx >= 0) columns.splice(actionIdx, 0, ...verifyColumns)
	else columns.push(...verifyColumns)
	return {
		...schema,
		columns,
		fields: [...(schema.fields || []), ...buildVerifyFields(schema.id)]
	}
}

const withGeojson = schema => {
	const columns = [...(schema.columns || [])]
	const actionIdx = columns.findIndex(col => col.slot === 'action')
	if (actionIdx >= 0) columns.splice(actionIdx, 0, GEOJSON_COLUMN)
	else columns.push(GEOJSON_COLUMN)
	return {
		...schema,
		columns,
		fields: [...(schema.fields || []), { ...GEOJSON_FIELD }]
	}
}

const countySchema = {
	id: 'county',
	title: '县级行政区',
	searchKey: 'name',
	searchPlaceholder: '请输入县（区、市）名称',
	columns: [
		{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
		{ prop: 'name', label: '名称', minWidth: 120, align: 'center' },
		{ prop: 'city', label: '所属市', minWidth: 100, align: 'center' },
		{ prop: 'population', label: '常住人口(万)', minWidth: 110, align: 'center' },
		{ prop: 'density', label: '人口密度', minWidth: 100, align: 'center' },
		{ prop: 'statArea', label: '面积(km²)', minWidth: 100, align: 'center' },
		{ prop: 'lon', label: '经度', width: 100, align: 'center' },
		{ prop: 'lat', label: '纬度', width: 100, align: 'center' },
		{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
	],
	fields: [
		{ prop: 'name', label: '名称', required: true },
		{ prop: 'city', label: '所属市', required: true },
		{ prop: 'population', label: '常住人口(万)', type: 'number', required: true },
		{ prop: 'density', label: '人口密度', type: 'number', required: true },
		{ prop: 'statArea', label: '面积(km²)', type: 'number', required: true },
		{ prop: 'lon', label: '经度', type: 'number', required: true },
		{ prop: 'lat', label: '纬度', type: 'number', required: true }
	]
}

/** 市级行政区：面状范围由县界按地级合并；市界.json 弧段见 boundaryLines */
const citySchema = {
	id: 'city',
	title: '市级行政区',
	searchKey: 'name',
	searchPlaceholder: '请输入地级市名称',
	columns: [
		{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
		{ prop: 'name', label: '名称', minWidth: 120, align: 'center' },
		{ prop: 'countyCount', label: '下辖县区数', minWidth: 110, align: 'center' },
		{ prop: 'population', label: '常住人口(万)', minWidth: 110, align: 'center' },
		{ prop: 'density', label: '人口密度', minWidth: 100, align: 'center' },
		{ prop: 'statArea', label: '面积(km²)', minWidth: 100, align: 'center' },
		{ prop: 'lon', label: '经度', width: 100, align: 'center' },
		{ prop: 'lat', label: '纬度', width: 100, align: 'center' },
		{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
	],
	fields: [
		{ prop: 'name', label: '名称', required: true },
		{ prop: 'countyCount', label: '下辖县区数', type: 'number' },
		{ prop: 'population', label: '常住人口(万)', type: 'number' },
		{ prop: 'density', label: '人口密度', type: 'number' },
		{ prop: 'statArea', label: '面积(km²)', type: 'number' },
		{ prop: 'lon', label: '经度', type: 'number', required: true },
		{ prop: 'lat', label: '纬度', type: 'number', required: true },
		{
			prop: 'boundaryLines',
			label: '市界弧段 GeoJSON',
			type: 'textarea',
			rows: 6,
			placeholder: '来自 public/datas/市界.json 的弧段 FeatureCollection（可选）'
		}
	]
}

const townSchema = {
	id: 'town',
	title: '乡镇行政区',
	searchKey: 'name',
	searchPlaceholder: '请输入乡镇名称',
	columns: [
		{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
		{ prop: 'name', label: '名称', minWidth: 120, align: 'center' },
		{ prop: 'county', label: '所属县', minWidth: 100, align: 'center' },
		{ prop: 'city', label: '所属市', minWidth: 100, align: 'center' },
		{ prop: 'population', label: '常住人口(万)', minWidth: 110, align: 'center' },
		{ prop: 'density', label: '人口密度', minWidth: 100, align: 'center' },
		{ prop: 'statArea', label: '面积(km²)', minWidth: 100, align: 'center' },
		{ prop: 'areaKm2', label: '示意面积(km²)', minWidth: 120, align: 'center' },
		{ prop: 'lon', label: '经度', width: 100, align: 'center' },
		{ prop: 'lat', label: '纬度', width: 100, align: 'center' },
		{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
	],
	fields: [
		{ prop: 'name', label: '名称', required: true },
		{ prop: 'county', label: '所属县', required: true },
		{ prop: 'city', label: '所属市', required: true },
		{ prop: 'population', label: '常住人口(万)', type: 'number' },
		{ prop: 'density', label: '人口密度', type: 'number' },
		{ prop: 'statArea', label: '面积(km²)', type: 'number' },
		{ prop: 'areaKm2', label: '示意面积(km²)', type: 'number' },
		{ prop: 'lon', label: '经度', type: 'number', required: true },
		{ prop: 'lat', label: '纬度', type: 'number', required: true }
	]
}

/** 字段对齐 public/datas 海岸防护工程.json */
const seawallSchema = {
	id: 'seawall',
	title: '海堤',
	searchKey: 'name',
	searchPlaceholder: '请输入海堤名称',
	columns: [
		{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
		{ prop: 'name', label: '名称', minWidth: 140, align: 'center' },
		{ prop: 'division', label: '行政区划', minWidth: 200, align: 'center' },
		{ prop: 'HDXL', label: '海堤序列', minWidth: 100, align: 'center' },
		{ prop: 'length', label: '长度(km)', width: 100, align: 'center' },
		{ prop: 'Return_per', label: '重现期(年)', width: 110, align: 'center' },
		{ prop: 'Dam_elevat', label: '堤顶高程(m)', width: 120, align: 'center' },
		{ prop: 'time', label: '建成时间', width: 110, align: 'center' },
		{ prop: 'lon', label: '经度', width: 100, align: 'center' },
		{ prop: 'lat', label: '纬度', width: 100, align: 'center' },
		{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
	],
	fields: [
		{ prop: 'name', label: '名称', required: true },
		{ prop: 'division', label: '行政区划', required: true },
		{ prop: 'HDXL', label: '海堤序列' },
		{ prop: 'length', label: '长度(km)', type: 'number' },
		{ prop: 'Return_per', label: '重现期(年)', placeholder: '如 20' },
		{ prop: 'Dam_elevat', label: '堤顶高程(m)' },
		{ prop: 'time', label: '建成时间', placeholder: '如 1978.05' },
		{ prop: 'lon', label: '经度', type: 'number', required: true },
		{ prop: 'lat', label: '纬度', type: 'number', required: true },
		{ prop: 'type', label: '类型', defaultValue: '海堤' },
		{ prop: 'town', label: '乡镇' },
		{ prop: 'standard', label: '防潮标准' },
		{ prop: 'builtYear', label: '建成年代', type: 'number' },
		{ prop: 'status', label: '状态', defaultValue: '在役' }
	]
}

/** 字段对齐 public/datas 海渔港.json */
const fishingSchema = {
	id: 'fishing',
	title: '渔港',
	searchKey: 'name',
	searchPlaceholder: '请输入渔港名称',
	columns: [
		{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
		{ prop: 'name', label: '名称', minWidth: 150, align: 'center' },
		{ prop: 'division', label: '行政区划', minWidth: 180, align: 'center' },
		{ prop: 'type', label: '类型', minWidth: 100, align: 'center' },
		{ prop: 'method', label: '方式', minWidth: 100, align: 'center' },
		{ prop: 'town', label: '乡镇', minWidth: 100, align: 'center' },
		{ prop: 'area', label: '面积', width: 90, align: 'center' },
		{ prop: 'host', label: '拥有者', minWidth: 160, align: 'center' },
		{ prop: 'lon', label: '经度', width: 100, align: 'center' },
		{ prop: 'lat', label: '纬度', width: 100, align: 'center' },
		{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
	],
	fields: [
		{ prop: 'name', label: '名称', required: true },
		{ prop: 'division', label: '行政区划', required: true },
		{ prop: 'type', label: '类型', defaultValue: '渔港' },
		{ prop: 'method', label: '方式' },
		{ prop: 'town', label: '乡镇' },
		{ prop: 'lon', label: '经度', type: 'number', required: true },
		{ prop: 'lat', label: '纬度', type: 'number', required: true },
		{ prop: 'area', label: '面积', type: 'number' },
		{ prop: 'areaKm2', label: '面积(km²)', type: 'number' },
		{ prop: 'host', label: '拥有者' },
		{ prop: 'manager', label: '管护单位' },
		{ prop: 'berths', label: '泊位数', type: 'number' },
		{ prop: 'capacity', label: '容量' },
		{ prop: 'status', label: '状态', defaultValue: '在用' }
	]
}

/** 字段对齐 public/datas 海水养殖区.json */
const maricultureSchema = {
	id: 'mariculture',
	title: '海水养殖区',
	searchKey: 'name',
	searchPlaceholder: '请输入养殖区名称',
	columns: [
		{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
		{ prop: 'name', label: '名称', minWidth: 160, align: 'center' },
		{ prop: 'division', label: '行政区划', minWidth: 180, align: 'center' },
		{ prop: 'method', label: '养殖方式', minWidth: 110, align: 'center' },
		{ prop: 'type', label: '类型', minWidth: 100, align: 'center' },
		{ prop: 'town', label: '乡镇', minWidth: 100, align: 'center' },
		{ prop: 'area', label: '面积', width: 90, align: 'center' },
		{ prop: 'host', label: '拥有者', minWidth: 180, align: 'center' },
		{ prop: 'lon', label: '经度', width: 110, align: 'center' },
		{ prop: 'lat', label: '纬度', width: 110, align: 'center' },
		{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
	],
	fields: [
		{ prop: 'name', label: '名称', required: true },
		{ prop: 'division', label: '行政区划', required: true },
		{ prop: 'method', label: '养殖方式' },
		{ prop: 'type', label: '类型', defaultValue: '海水养殖区' },
		{ prop: 'town', label: '乡镇' },
		{ prop: 'lon', label: '经度', type: 'number', required: true },
		{ prop: 'lat', label: '纬度', type: 'number', required: true },
		{ prop: 'area', label: '面积', type: 'number' },
		{ prop: 'areaKm2', label: '面积(km²)', type: 'number' },
		{ prop: 'host', label: '拥有者' },
		{ prop: 'manager', label: '管护单位' },
		{ prop: 'species', label: '养殖品种' },
		{ prop: 'status', label: '状态', defaultValue: '在养' }
	]
}

/** 字段对齐 public/datas 滨海旅游区.json */
const touristSchema = {
	id: 'tourist',
	title: '滨海旅游区',
	searchKey: 'name',
	searchPlaceholder: '请输入旅游区名称',
	columns: [
		{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
		{ prop: 'name', label: '名称', minWidth: 160, align: 'center' },
		{ prop: 'division', label: '行政区划', minWidth: 200, align: 'center' },
		{ prop: '面积', label: '面积', width: 100, align: 'center' },
		{ prop: 'Management', label: '管理单位', minWidth: 160, align: 'center' },
		{ prop: 'lon', label: '经度', width: 110, align: 'center' },
		{ prop: 'lat', label: '纬度', width: 110, align: 'center' },
		{ prop: 'status', label: '状态', width: 80, align: 'center' },
		{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
	],
	fields: [
		{ prop: 'name', label: '名称', required: true },
		{ prop: 'division', label: '行政区划', required: true },
		{ prop: 'type', label: '类型', defaultValue: '滨海旅游区' },
		{ prop: 'town', label: '乡镇' },
		{ prop: 'lon', label: '经度', type: 'number', required: true },
		{ prop: 'lat', label: '纬度', type: 'number', required: true },
		{ prop: '面积', label: '面积' },
		{ prop: 'areaKm2', label: '面积(km²)', type: 'number' },
		{ prop: 'Management', label: '管理单位' },
		{ prop: 'manager', label: '管护单位' },
		{ prop: 'level', label: '景区等级' },
		{ prop: 'status', label: '状态', defaultValue: '开放' }
	]
}

const rsSceneSchema = {
	id: 'rsScene',
	title: '遥感影像场景',
	searchKey: 'name',
	searchPlaceholder: '请输入场景名称',
	columns: [
		{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
		{ prop: 'id', label: '场景ID', minWidth: 140, align: 'center' },
		{ prop: 'name', label: '名称', minWidth: 180, align: 'center' },
		{ prop: 'regionHint', label: '区域', minWidth: 100, align: 'center' },
		{ prop: 'date', label: '影像日期', width: 120, align: 'center' },
		{ prop: 'inundationRatio', label: '淹没比例', width: 100, align: 'center' },
		{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
	],
	fields: [
		{ prop: 'id', label: '场景ID', required: true },
		{ prop: 'name', label: '名称', required: true },
		{ prop: 'imageId', label: '本地影像ID', placeholder: '对应 datas/images/manifest.json' },
		{ prop: 'regionHint', label: '区域提示', required: true },
		{ prop: 'date', label: '影像日期', required: true },
		{ prop: 'inundationRatio', label: '淹没比例(0-1)', type: 'number', required: true }
	]
}

export const SURVEY_BASE_TYPES = {
	county: withGeojson(countySchema),
	city: withGeojson(citySchema),
	town: withGeojson(townSchema),
	seawall: withGeojson(withVerify(seawallSchema)),
	fishing: withGeojson(withVerify(fishingSchema)),
	mariculture: withGeojson(withVerify(maricultureSchema)),
	tourist: withGeojson(withVerify(touristSchema)),
	rsScene: withGeojson(rsSceneSchema),
	reported: {
		id: 'reported',
		title: '灾情速报数据',
		searchKey: 'year',
		searchPlaceholder: '请输入速报期次/事件名称/编号',
		extraSearch: {
			prop: 'hazardType',
			placeholder: '承灾体类型',
			options: [
				{ label: '全部', value: '' },
				{ label: '海堤', value: 'seawall' },
				{ label: '渔港', value: 'fishing' },
				{ label: '海水养殖区', value: 'mariculture' },
				{ label: '滨海旅游区', value: 'tourist' }
			]
		},
		columns: [
			{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
			{ prop: 'reportNo', label: '速报编号', minWidth: 180, align: 'center' },
			{ prop: 'hazardTypeLabel', label: '承灾体类型', minWidth: 110, align: 'center' },
			{ prop: 'disasterName', label: '灾害事件', minWidth: 250, align: 'center' },
			{ prop: 'disasterType', label: '灾害类型', width: 100, align: 'center' },
			{ prop: 'year', label: '速报期次', minWidth: 110, align: 'center' },
			{ prop: 'status', label: '速报状态', width: 90, align: 'center' },
			{ prop: 'targetName', label: '承灾体对象', minWidth: 140, align: 'center' },
			{ prop: 'disasterLevel', label: SURVEY_LABELS.disasterLevel, width: 90, align: 'center' },
			{ prop: 'region', label: '影响区域', minWidth: 180, align: 'center' },
			{ prop: 'countyCount', label: '涉及县区', width: 90, align: 'center' },
			{ prop: 'startTime', label: '开始时间', minWidth: 150, align: 'center' },
			{ prop: 'endTime', label: '结束时间', minWidth: 150, align: 'center' },
			{ prop: 'reportTime', label: '上报时间', minWidth: 150, align: 'center' },
			{ prop: 'count', label: '受灾数量(处)', width: 110, align: 'center' },
			{ prop: 'damagedCount', label: '损毁数量(处)', width: 110, align: 'center' },
			{ prop: 'area', label: SURVEY_LABELS.areaKm2, width: 120, align: 'center' },
			{ prop: 'length', label: '受灾长度(km)', width: 120, align: 'center' },
			{ prop: 'affectedPopulation', label: SURVEY_LABELS.affectedPopulation, width: 120, align: 'center' },
			{ prop: 'evacuated', label: '转移安置(人)', width: 120, align: 'center' },
			{ prop: 'loss', label: SURVEY_LABELS.economicLoss, width: 150, align: 'center' },
			{ prop: 'infrastructureLoss', label: '设施损失(万元)', width: 130, align: 'center' },
			{ prop: 'industryLoss', label: '产业损失(万元)', width: 130, align: 'center' },
			{ prop: 'reportOrg', label: '上报单位', minWidth: 160, align: 'center' },
			{ prop: 'reportor', label: '填报人', width: 90, align: 'center' },
			{ prop: 'remark', label: '备注', minWidth: 160, align: 'center', showTooltip: true },
			{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
		],
		fields: [
			{ prop: 'reportNo', label: '速报编号', required: true },
			{
				prop: 'hazardType',
				label: '承灾体类型',
				required: true,
				type: 'select',
				options: [
					{ label: '海堤', value: 'seawall' },
					{ label: '渔港', value: 'fishing' },
					{ label: '海水养殖区', value: 'mariculture' },
					{ label: '滨海旅游区', value: 'tourist' }
				]
			},
			{ prop: 'disasterName', label: '灾害事件名称', required: true },
			{
				prop: 'disasterType',
				label: '灾害类型',
				required: true,
				type: 'select',
				options: [
					{ label: '台风', value: '台风' },
					{ label: '风暴潮', value: '风暴潮' },
					{ label: '灾害性海浪', value: '灾害性海浪' },
					{ label: '赤潮', value: '赤潮' },
					{ label: '其他', value: '其他' }
				]
			},
			{ prop: 'year', label: '速报期次', required: true },
			{ prop: 'targetName', label: '承灾体对象', placeholder: '与海堤/渔港等名称一致，用于对象级核对' },
			{
				prop: 'disasterLevel',
				label: SURVEY_LABELS.disasterLevel,
				type: 'select',
				options: DISASTER_LEVEL_OPTIONS
			},
			{
				prop: 'status',
				label: '速报状态',
				required: true,
				type: 'select',
				defaultValue: '初报',
				options: [
					{ label: '初报', value: '初报' },
					{ label: '续报', value: '续报' },
					{ label: '终报', value: '终报' }
				]
			},
			{ prop: 'region', label: '影响区域', required: true },
			{ prop: 'countyCount', label: '涉及县区数', type: 'number', required: true },
			{ prop: 'startTime', label: '开始时间', required: true, placeholder: '如 2024-09-15 08:00' },
			{ prop: 'endTime', label: '结束时间', placeholder: '如 2024-09-18 18:00' },
			{ prop: 'reportTime', label: '上报时间', required: true, placeholder: '如 2024-09-19 10:30' },
			{ prop: 'count', label: '受灾数量(处)', type: 'number', required: true },
			{ prop: 'damagedCount', label: '损毁数量(处)', type: 'number', defaultValue: 0 },
			{ prop: 'area', label: SURVEY_LABELS.areaKm2, type: 'number', required: true },
			{ prop: 'length', label: '受灾长度(km)', type: 'number', defaultValue: 0 },
			{ prop: 'affectedPopulation', label: SURVEY_LABELS.affectedPopulation, type: 'number', defaultValue: 0 },
			{ prop: 'evacuated', label: '转移安置(人)', type: 'number', defaultValue: 0 },
			{ prop: 'loss', label: SURVEY_LABELS.economicLoss, type: 'number', required: true },
			{ prop: 'infrastructureLoss', label: '设施损失(万元)', type: 'number', defaultValue: 0 },
			{ prop: 'industryLoss', label: '产业损失(万元)', type: 'number', defaultValue: 0 },
			{ prop: 'reportOrg', label: '上报单位', required: true },
			{ prop: 'reportor', label: '填报人' },
			{ prop: 'remark', label: '备注', type: 'textarea', rows: 3, placeholder: '补充说明、核查情况等' }
		]
	}
}

export const SURVEY_BASE_MENU = {
	name: '灾情调查数据',
	path: '/management/survey',
	children: [
		{ type: 'county', name: '县级行政区', path: '/management/surveyCounty' },
		{ type: 'city', name: '市级行政区', path: '/management/surveyCity' },
		{ type: 'town', name: '乡镇行政区', path: '/management/surveyTown' },
		{ type: 'seawall', name: '海堤数据', path: '/management/surveySeawall' },
		{ type: 'fishing', name: '渔港数据', path: '/management/surveyFishing' },
		{ type: 'mariculture', name: '海水养殖区', path: '/management/surveyMariculture' },
		{ type: 'tourist', name: '滨海旅游区', path: '/management/surveyTourist' },
		{ type: 'rsScene', name: '遥感影像场景', path: '/management/surveyRsScene' },
		{ type: 'reported', name: '灾情速报数据', path: '/management/surveyReported' }
	]
}

/** 灾害评估专属基础数据（与调查重复项不再单独建菜单） */
export const ASSESS_BASE_TYPES = {
	assessSar: {
		id: 'assessSar',
		title: 'SAR遥感场景',
		searchPlaceholder: '请输入场景名称/ID',
		extraSearch: {
			prop: 'type',
			placeholder: '场景类型',
			options: [
				{ label: '全部', value: '' },
				{ label: '海风风场', value: 'wind' },
				{ label: '海浪场', value: 'wave' },
				{ label: '风暴潮影像', value: 'storm' }
			]
		},
		columns: [
			{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
			{ prop: 'id', label: '场景ID', minWidth: 140, align: 'center' },
			{ prop: 'name', label: '名称', minWidth: 180, align: 'center' },
			{ prop: 'type', label: '类型', width: 90, align: 'center' },
			{ prop: 'date', label: '影像日期', width: 120, align: 'center' },
			{ prop: 'sensor', label: '传感器', minWidth: 120, align: 'center' },
			{ prop: 'maxWind', label: '最大风速(m/s)', width: 120, align: 'center' },
			{ prop: 'direction', label: '主导风向', width: 90, align: 'center' },
			{ prop: 'maxHeight', label: '最大浪高(m)', width: 120, align: 'center' },
			{ prop: 'maxSpeed', label: '最大波速', width: 100, align: 'center' },
			{ prop: 'period', label: '周期(s)', width: 90, align: 'center' },
			{ prop: 'inundationRatio', label: '淹没比例', width: 100, align: 'center' },
			{ prop: '', slot: 'geojson', label: '空间数据', width: 90, align: 'center' },
			{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
		],
		fields: [
			{ prop: 'id', label: '场景ID', required: true },
			{ prop: 'name', label: '名称', required: true },
			{ prop: 'imageId', label: '本地影像ID', placeholder: '对应 datas/images/manifest.json' },
			{
				prop: 'type',
				label: '类型',
				required: true,
				type: 'select',
				options: [
					{ label: '海风风场', value: 'wind' },
					{ label: '海浪场', value: 'wave' },
					{ label: '风暴潮影像', value: 'storm' }
				]
			},
			{ prop: 'date', label: '影像日期', required: true },
			{ prop: 'sensor', label: '传感器', defaultValue: '孔径雷达卫星' },
			{ prop: 'maxWind', label: '最大风速(m/s)', type: 'number' },
			{ prop: 'direction', label: '主导风向' },
			{ prop: 'maxHeight', label: '最大浪高(m)', type: 'number' },
			{ prop: 'maxSpeed', label: '最大波速', type: 'number' },
			{ prop: 'period', label: '周期(s)', type: 'number' },
			{ prop: 'inundationRatio', label: '淹没比例(0-1)', type: 'number' },
			{
				prop: 'geojson',
				label: 'GeoJSON',
				type: 'textarea',
				rows: 6,
				placeholder: '可选，粘贴覆盖范围 Geometry / Feature / FeatureCollection'
			}
		]
	},
	assessTyphoon: {
		id: 'assessTyphoon',
		title: '台风过程',
		searchPlaceholder: '请输入台风名称/编号',
		columns: [
			{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
			{ prop: 'id', label: '台风编号', minWidth: 110, align: 'center' },
			{ prop: 'name', label: '名称', width: 100, align: 'center' },
			{ prop: 'year', label: '年份', width: 80, align: 'center' },
			{ prop: 'level', label: '强度等级', minWidth: 100, align: 'center' },
			{ prop: 'landfall', label: '登陆点', minWidth: 120, align: 'center' },
			{ prop: 'maxWind', label: '最大风速(m/s)', width: 120, align: 'center' },
			{ prop: '', slot: 'points', label: '路径点', width: 90, align: 'center' },
			{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
		],
		fields: [
			{ prop: 'id', label: '台风编号', required: true },
			{ prop: 'name', label: '名称', required: true },
			{ prop: 'year', label: '年份', type: 'number', required: true },
			{ prop: 'level', label: '强度等级', required: true },
			{ prop: 'landfall', label: '登陆点', required: true },
			{ prop: 'maxWind', label: '最大风速(m/s)', type: 'number', required: true },
			{
				prop: 'points',
				label: '路径点 JSON',
				type: 'json',
				required: true,
				rows: 8,
				placeholder: '[{"lng":119.8,"lat":25.6,"time":"09-16 14:00","wind":38,"dir":"E"}]'
			}
		]
	},
	assessLandPrice: {
		id: 'assessLandPrice',
		title: '土地利用单价',
		searchPlaceholder: '请输入用地类型',
		columns: [
			{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
			{ prop: 'name', label: '用地类型', minWidth: 140, align: 'center' },
			{ prop: 'price', label: '单价(万元/km²)', minWidth: 140, align: 'center' },
			{ prop: 'unit', label: '单位', width: 120, align: 'center' },
			{ prop: 'remark', label: '备注', minWidth: 160, align: 'center' },
			{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
		],
		fields: [
			{ prop: 'name', label: '用地类型', required: true },
			{ prop: 'price', label: '单价(万元/km²)', type: 'number', required: true },
			{ prop: 'unit', label: '单位', defaultValue: '万元/km²' },
			{ prop: 'remark', label: '备注', type: 'textarea', rows: 2 }
		]
	},
	assessCountyEconomy: {
		id: 'assessCountyEconomy',
		title: '县域经济数据',
		searchPlaceholder: '请输入县（市、区）名称',
		columns: [
			{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
			{ prop: 'name', label: '县市区', minWidth: 120, align: 'center' },
			{ prop: 'gdp', label: 'GDP(亿元)', width: 110, align: 'center' },
			{ prop: 'landFarm', label: '耕地占比', width: 100, align: 'center' },
			{ prop: 'landBuild', label: '建设用地占比', width: 120, align: 'center' },
			{ prop: 'landAqua', label: '养殖水面占比', width: 120, align: 'center' },
			{ prop: 'landForest', label: '林地占比', width: 100, align: 'center' },
			{ prop: 'landWater', label: '水域占比', width: 100, align: 'center' },
			{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
		],
		fields: [
			{ prop: 'name', label: '县市区', required: true },
			{ prop: 'gdp', label: 'GDP(亿元)', type: 'number', required: true },
			{ prop: 'landFarm', label: '耕地占比(0-1)', type: 'number', required: true },
			{ prop: 'landBuild', label: '建设用地占比(0-1)', type: 'number', required: true },
			{ prop: 'landAqua', label: '养殖水面占比(0-1)', type: 'number', required: true },
			{ prop: 'landForest', label: '林地占比(0-1)', type: 'number', required: true },
			{ prop: 'landWater', label: '水域占比(0-1)', type: 'number', required: true }
		]
	},
	assessStructure: {
		id: 'assessStructure',
		title: '海上结构物',
		searchPlaceholder: '请输入结构物名称/编码/所属县',
		extraSearch: {
			prop: 'type',
			placeholder: '结构类型',
			options: [
				{ label: '全部', value: '' },
				{ label: '海上结构', value: '海上结构' },
				{ label: '海岸结构', value: '海岸结构' },
				{ label: '助航设施', value: '助航设施' },
				{ label: '港口设施', value: '港口设施' },
				{ label: '防护工程', value: '防护工程' }
			]
		},
		columns: [
			{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
			{ prop: 'code', label: '结构编码', minWidth: 120, align: 'center' },
			{ prop: 'name', label: '名称', minWidth: 140, align: 'center' },
			{ prop: 'type', label: '结构类型', minWidth: 100, align: 'center' },
			{ prop: 'subtype', label: '细分类型', minWidth: 110, align: 'center' },
			{ prop: 'city', label: '所属市', width: 90, align: 'center' },
			{ prop: 'county', label: '所属县', minWidth: 100, align: 'center' },
			{ prop: 'town', label: '乡镇', minWidth: 100, align: 'center' },
			{ prop: 'locationDesc', label: '位置描述', minWidth: 140, align: 'center', showTooltip: true },
			{ prop: 'lon', label: '经度', width: 100, align: 'center' },
			{ prop: 'lat', label: '纬度', width: 100, align: 'center' },
			{ prop: 'builtYear', label: '建成年代', width: 90, align: 'center' },
			{ prop: 'structureScale', label: '规模/尺度', minWidth: 140, align: 'center' },
			{ prop: 'material', label: '主体材料', minWidth: 120, align: 'center' },
			{ prop: 'designStandard', label: '设计标准', minWidth: 110, align: 'center' },
			{ prop: 'importance', label: '重要等级', width: 90, align: 'center' },
			{ prop: 'status', label: '运行状态', width: 90, align: 'center' },
			{ prop: 'manager', label: '管护单位', minWidth: 180, align: 'center' },
			{ prop: 'contact', label: '联系人', width: 90, align: 'center' },
			{ prop: 'level', label: '风险等级', width: 90, align: 'center' },
			{ prop: 'damage', label: '典型损毁描述', minWidth: 180, align: 'center', showTooltip: true },
			{ prop: 'damageMode', label: '主要损毁模式', minWidth: 120, align: 'center' },
			{ prop: 'repairSuggest', label: '修复建议', minWidth: 140, align: 'center', showTooltip: true },
			{ prop: 'lastInspectTime', label: '最近巡查时间', minWidth: 140, align: 'center' },
			{ prop: 'remark', label: '备注', minWidth: 140, align: 'center', showTooltip: true },
			{ prop: '', slot: 'geojson', label: '空间数据', width: 90, align: 'center' },
			{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
		],
		fields: [
			{ prop: 'code', label: '结构编码', required: true },
			{ prop: 'name', label: '名称', required: true },
			{
				prop: 'type',
				label: '结构类型',
				required: true,
				type: 'select',
				defaultValue: '海上结构',
				options: [
					{ label: '海上结构', value: '海上结构' },
					{ label: '海岸结构', value: '海岸结构' },
					{ label: '助航设施', value: '助航设施' },
					{ label: '港口设施', value: '港口设施' },
					{ label: '防护工程', value: '防护工程' }
				]
			},
			{ prop: 'subtype', label: '细分类型', placeholder: '如跨海栈桥/灯塔/观景平台' },
			{ prop: 'city', label: '所属市', required: true },
			{ prop: 'county', label: '所属县', required: true },
			{ prop: 'town', label: '乡镇' },
			{ prop: 'locationDesc', label: '位置描述' },
			{ prop: 'lon', label: '经度', type: 'number', required: true },
			{ prop: 'lat', label: '纬度', type: 'number', required: true },
			{ prop: 'builtYear', label: '建成年代', type: 'number' },
			{ prop: 'structureScale', label: '规模/尺度', placeholder: '如长度、高度、泊位数等' },
			{ prop: 'material', label: '主体材料' },
			{ prop: 'designStandard', label: '设计标准', placeholder: '如50年一遇' },
			{
				prop: 'importance',
				label: '重要等级',
				type: 'select',
				defaultValue: '一般',
				options: [
					{ label: '特别重要', value: '特别重要' },
					{ label: '重要', value: '重要' },
					{ label: '一般', value: '一般' }
				]
			},
			{
				prop: 'status',
				label: '运行状态',
				type: 'select',
				defaultValue: '在用',
				options: [
					{ label: '在用', value: '在用' },
					{ label: '维修中', value: '维修中' },
					{ label: '停用', value: '停用' },
					{ label: '报废', value: '报废' }
				]
			},
			{ prop: 'manager', label: '管护单位' },
			{ prop: 'contact', label: '联系人' },
			{ prop: 'level', label: '风险等级(1-4)', type: 'number', defaultValue: 3 },
			{ prop: 'damage', label: '典型损毁描述', type: 'textarea', rows: 2 },
			{ prop: 'damageMode', label: '主要损毁模式', placeholder: '冲刷/淘空/越浪/倾覆等' },
			{ prop: 'repairSuggest', label: '修复建议', type: 'textarea', rows: 2 },
			{ prop: 'lastInspectTime', label: '最近巡查时间', placeholder: '如 2024-09-20 10:00' },
			{ prop: 'remark', label: '备注', type: 'textarea', rows: 2 },
			{
				prop: 'geojson',
				label: 'GeoJSON',
				type: 'textarea',
				rows: 5,
				placeholder: '可选，粘贴结构物位置 Geometry / Feature'
			}
		]
	},
	assessIntegrate: {
		id: 'assessIntegrate',
		title: '受灾整合台账',
		searchPlaceholder: '请输入县名/承灾体/事件名称/编号',
		extraSearch: {
			prop: 'status',
			placeholder: '标准化状态',
			options: [
				{ label: '全部', value: '' },
				{ label: '已标准化', value: '已标准化' },
				{ label: '待校核', value: '待校核' },
				{ label: '已复核', value: '已复核' }
			]
		},
		columns: [
			{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
			{ prop: 'id', label: '记录ID', minWidth: 100, align: 'center' },
			{ prop: 'eventName', label: '关联灾害事件', minWidth: 200, align: 'center' },
			{ prop: 'disasterType', label: '灾害类型', width: 100, align: 'center' },
			{ prop: 'sourceName', label: '数据来源', minWidth: 200, align: 'center' },
			{ prop: 'reportNo', label: '来源编号', minWidth: 160, align: 'center' },
			{ prop: 'reportTime', label: '上报时间', minWidth: 150, align: 'center' },
			{ prop: 'occurTime', label: '灾情发生时间', minWidth: 150, align: 'center' },
			{ prop: 'city', label: '市', width: 90, align: 'center' },
			{ prop: 'county', label: '县', width: 90, align: 'center' },
			{ prop: 'town', label: '乡镇', minWidth: 100, align: 'center' },
			{ prop: 'hazardType', label: '承灾体类型', width: 110, align: 'center' },
			{ prop: 'hazardName', label: '承灾体名称', minWidth: 120, align: 'center' },
			{ prop: 'hazardCode', label: '承灾体编码', minWidth: 150, align: 'center' },
			{ prop: 'damage', label: '主体损毁情况', minWidth: 160, align: 'center', showTooltip: true },
			{ prop: 'facility', label: '附属设施受损', minWidth: 140, align: 'center', showTooltip: true },
			{ prop: 'damageLength', label: '损毁长度(km)', width: 120, align: 'center' },
			{ prop: 'damageArea', label: '损毁面积(km²)', width: 120, align: 'center' },
			{ prop: 'affectedPeople', label: '影响人数', width: 100, align: 'center' },
			{ prop: 'directLoss', label: '直接损失(万元)', width: 130, align: 'center' },
			{ prop: 'level', label: '预警级别', width: 90, align: 'center' },
			{ prop: 'urgency', label: '紧急程度', width: 90, align: 'center' },
			{ prop: 'verifyStatus', label: '核验状态', width: 100, align: 'center' },
			{ prop: 'status', label: '标准化状态', width: 110, align: 'center' },
			{ prop: 'reportOrg', label: '上报单位', minWidth: 180, align: 'center' },
			{ prop: 'reportor', label: '填报人', width: 90, align: 'center' },
			{ prop: 'auditor', label: '审核人', width: 90, align: 'center' },
			{ prop: 'standardTime', label: '标准化时间', minWidth: 150, align: 'center' },
			{ prop: 'lon', label: '经度', width: 100, align: 'center' },
			{ prop: 'lat', label: '纬度', width: 100, align: 'center' },
			{ prop: 'remark', label: '备注', minWidth: 140, align: 'center', showTooltip: true },
			{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
		],
		fields: [
			{ prop: 'id', label: '记录ID', placeholder: '留空则自动生成' },
			{
				prop: 'sourceId',
				label: '数据来源',
				required: true,
				type: 'select',
				options: [
					{ label: '海洋灾害灾情速报子系统', value: 'speed' },
					{ label: '地方应急上报', value: 'local' },
					{ label: '海洋观测站报文', value: 'marine' }
				]
			},
			{ prop: 'reportNo', label: '来源编号' },
			{ prop: 'eventName', label: '关联灾害事件', required: true },
			{
				prop: 'disasterType',
				label: '灾害类型',
				required: true,
				type: 'select',
				options: [
					{ label: '台风', value: '台风' },
					{ label: '风暴潮', value: '风暴潮' },
					{ label: '灾害性海浪', value: '灾害性海浪' },
					{ label: '其他', value: '其他' }
				]
			},
			{ prop: 'reportTime', label: '上报时间', required: true, placeholder: '如 2024-09-16 08:20' },
			{ prop: 'occurTime', label: '灾情发生时间', placeholder: '如 2024-09-15 20:00' },
			{ prop: 'city', label: '市', required: true },
			{ prop: 'county', label: '县', required: true },
			{ prop: 'town', label: '乡镇' },
			{
				prop: 'hazardType',
				label: '承灾体类型',
				required: true,
				type: 'select',
				options: [
					{ label: '海堤', value: '海堤' },
					{ label: '渔港', value: '渔港' },
					{ label: '海水养殖区', value: '海水养殖区' },
					{ label: '滨海旅游区', value: '滨海旅游区' },
					{ label: '海岸线', value: '海岸线' },
					{ label: '海上结构', value: '海上结构' }
				]
			},
			{ prop: 'hazardName', label: '承灾体名称', required: true },
			{ prop: 'hazardCode', label: '承灾体编码' },
			{ prop: 'damage', label: '主体损毁情况', type: 'textarea', rows: 2, required: true },
			{ prop: 'facility', label: '附属设施受损', type: 'textarea', rows: 2 },
			{ prop: 'damageLength', label: '损毁长度(km)', type: 'number', defaultValue: 0 },
			{ prop: 'damageArea', label: '损毁面积(km²)', type: 'number', defaultValue: 0 },
			{ prop: 'affectedPeople', label: '影响人数', type: 'number', defaultValue: 0 },
			{ prop: 'directLoss', label: '直接损失(万元)', type: 'number', defaultValue: 0 },
			{
				prop: 'level',
				label: '预警级别',
				type: 'select',
				defaultValue: '蓝色',
				options: [
					{ label: '蓝色', value: '蓝色' },
					{ label: '黄色', value: '黄色' },
					{ label: '橙色', value: '橙色' },
					{ label: '红色', value: '红色' }
				]
			},
			{
				prop: 'urgency',
				label: '紧急程度',
				type: 'select',
				defaultValue: '一般',
				options: [
					{ label: '一般', value: '一般' },
					{ label: '紧急', value: '紧急' },
					{ label: '特急', value: '特急' }
				]
			},
			{
				prop: 'verifyStatus',
				label: '核验状态',
				type: 'select',
				defaultValue: '未核验',
				options: [
					{ label: '未核验', value: '未核验' },
					{ label: '核验中', value: '核验中' },
					{ label: '已核验', value: '已核验' }
				]
			},
			{
				prop: 'status',
				label: '标准化状态',
				type: 'select',
				defaultValue: '已标准化',
				options: [
					{ label: '已标准化', value: '已标准化' },
					{ label: '待校核', value: '待校核' },
					{ label: '已复核', value: '已复核' }
				]
			},
			{ prop: 'reportOrg', label: '上报单位' },
			{ prop: 'reportor', label: '填报人' },
			{ prop: 'auditor', label: '审核人' },
			{ prop: 'standardTime', label: '标准化时间', placeholder: '如 2024-09-16 14:00' },
			{ prop: 'lon', label: '经度', type: 'number' },
			{ prop: 'lat', label: '纬度', type: 'number' },
			{ prop: 'remark', label: '备注', type: 'textarea', rows: 2 }
		]
	},
	assessReportSource: {
		id: 'assessReportSource',
		title: '整合数据来源',
		searchPlaceholder: '请输入来源名称',
		columns: [
			{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
			{ prop: 'id', label: '来源编码', minWidth: 120, align: 'center' },
			{ prop: 'name', label: '来源名称', minWidth: 200, align: 'center' },
			{ prop: 'remark', label: '说明', minWidth: 180, align: 'center' },
			{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
		],
		fields: [
			{ prop: 'id', label: '来源编码', required: true },
			{ prop: 'name', label: '来源名称', required: true },
			{ prop: 'remark', label: '说明', type: 'textarea', rows: 2 }
		]
	},
	/** 漫堤分析：支撑风暴潮漫堤核查与上图，可与海堤台账按名称关联 */
	assessOvertop: {
		id: 'assessOvertop',
		title: '漫堤分析数据',
		searchPlaceholder: '请输入海堤名称/所在市/潮位站',
		extraSearch: {
			prop: 'warnLevel',
			placeholder: '预警状态',
			options: [
				{ label: '全部', value: '' },
				{ label: '不超', value: '不超' },
				{ label: '蓝色', value: '蓝色' },
				{ label: '黄色', value: '黄色' },
				{ label: '橙色', value: '橙色' },
				{ label: '红色', value: '红色' }
			]
		},
		columns: [
			{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
			{ prop: 'city', label: '所在市', width: 100, align: 'center' },
			{ prop: 'name', label: '海堤名称', minWidth: 160, align: 'center' },
			{ prop: 'hazardCode', label: '关联海堤编码', minWidth: 130, align: 'center' },
			{ prop: 'location', label: '乡镇', minWidth: 120, align: 'center' },
			{ prop: 'tideStation', label: '潮位站', minWidth: 110, align: 'center' },
			{ prop: 'warnTide', label: SURVEY_LABELS.warnTide, width: 110, align: 'center' },
			{ prop: 'tideHeight', label: SURVEY_LABELS.tideHeight, width: 90, align: 'center' },
			{ prop: 'protectPop', label: SURVEY_LABELS.protectPop, width: 110, align: 'center' },
			{ prop: 'crestElev', label: SURVEY_LABELS.crestElev, width: 110, align: 'center' },
			{ prop: 'hasWall', label: '防浪墙', width: 90, align: 'center' },
			{ prop: 'wallElev', label: SURVEY_LABELS.wallElev, width: 120, align: 'center' },
			{ prop: 'warnLevel', label: SURVEY_LABELS.warnLevel, width: 80, align: 'center' },
			{ prop: 'lon', label: '经度', width: 100, align: 'center' },
			{ prop: 'lat', label: '纬度', width: 100, align: 'center' },
			{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
		],
		fields: [
			{ prop: 'city', label: '所在市', required: true },
			{ prop: 'name', label: '海堤名称', required: true },
			{ prop: 'hazardCode', label: '关联海堤编码', placeholder: '对应调查侧海堤 id/编码' },
			{ prop: 'location', label: '乡镇' },
			{ prop: 'tideStation', label: '潮位站' },
			{ prop: 'warnTide', label: SURVEY_LABELS.warnTide, type: 'number', required: true },
			{ prop: 'tideHeight', label: SURVEY_LABELS.tideHeight, type: 'number', placeholder: '统一米制，勿填 cm' },
			{ prop: 'protectPop', label: SURVEY_LABELS.protectPop, type: 'number' },
			{ prop: 'crestElev', label: SURVEY_LABELS.crestElev, type: 'number' },
			{
				prop: 'hasWall',
				label: '防浪墙',
				type: 'select',
				defaultValue: '有',
				options: [
					{ label: '有', value: '有' },
					{ label: '无', value: '无' }
				]
			},
			{ prop: 'wallElev', label: SURVEY_LABELS.wallElev, type: 'number' },
			{
				prop: 'warnLevel',
				label: SURVEY_LABELS.warnLevel,
				type: 'select',
				defaultValue: '不超',
				options: [
					{ label: '不超', value: '不超' },
					{ label: '蓝色', value: '蓝色' },
					{ label: '黄色', value: '黄色' },
					{ label: '橙色', value: '橙色' },
					{ label: '红色', value: '红色' }
				]
			},
			{ prop: 'lon', label: '经度', type: 'number' },
			{ prop: 'lat', label: '纬度', type: 'number' },
			{ prop: 'remark', label: '备注', type: 'textarea', rows: 2 }
		]
	},
	/** 承灾体破坏样本：海浪/海风破坏性分析底数，可关联四类承灾体 */
	assessDamage: {
		id: 'assessDamage',
		title: '承灾体破坏样本',
		searchPlaceholder: '请输入名称/区县/承灾体编码',
		extraSearch: {
			prop: 'kind',
			placeholder: '灾害类型',
			options: [
				{ label: '全部', value: '' },
				{ label: '海风', value: 'wind' },
				{ label: '海浪', value: 'wave' }
			]
		},
		columns: [
			{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
			{ prop: 'kind', label: '类型', width: 80, align: 'center' },
			{ prop: 'name', label: '对象名称', minWidth: 140, align: 'center' },
			{ prop: 'target', label: '承灾体类别', width: 110, align: 'center' },
			{ prop: 'hazardCode', label: '关联承灾体编码', minWidth: 140, align: 'center' },
			{ prop: 'county', label: '区县', width: 100, align: 'center' },
			{ prop: 'lon', label: '经度', width: 100, align: 'center' },
			{ prop: 'lat', label: '纬度', width: 100, align: 'center' },
			{ prop: 'wind', label: '风速(m/s)', width: 100, align: 'center' },
			{ prop: 'dir', label: '风向', width: 80, align: 'center' },
			{ prop: 'height', label: '波高(m)', width: 90, align: 'center' },
			{ prop: 'speed', label: '波速(m/s)', width: 100, align: 'center' },
			{ prop: 'period', label: '周期(s)', width: 90, align: 'center' },
			{ prop: 'level', label: '破坏等级', width: 90, align: 'center' },
			{ prop: 'analysis', label: '分析结论', minWidth: 180, align: 'center', showTooltip: true },
			{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
		],
		fields: [
			{
				prop: 'kind',
				label: '灾害类型',
				required: true,
				type: 'select',
				options: [
					{ label: '海风', value: 'wind' },
					{ label: '海浪', value: 'wave' }
				]
			},
			{ prop: 'name', label: '对象名称', required: true },
			{
				prop: 'target',
				label: '承灾体类别',
				required: true,
				type: 'select',
				options: [
					{ label: '海堤', value: '海堤' },
					{ label: '渔港', value: '渔港' },
					{ label: '海岸线', value: '海岸线' },
					{ label: '海水养殖区', value: '海水养殖区' },
					{ label: '滨海旅游区', value: '滨海旅游区' }
				]
			},
			{ prop: 'hazardCode', label: '关联承灾体编码', placeholder: '对应调查侧四类台账 id' },
			{ prop: 'county', label: '区县', required: true },
			{ prop: 'lon', label: '经度', type: 'number', required: true },
			{ prop: 'lat', label: '纬度', type: 'number', required: true },
			{ prop: 'wind', label: '风速(m/s)', type: 'number' },
			{ prop: 'dir', label: '风向' },
			{ prop: 'height', label: '波高(m)', type: 'number' },
			{ prop: 'speed', label: '波速(m/s)', type: 'number' },
			{ prop: 'period', label: '周期(s)', type: 'number' },
			{ prop: 'lengthKm', label: '影响岸线(km)', type: 'number' },
			{ prop: 'level', label: '破坏等级(1-4)', type: 'number', defaultValue: 3 },
			{ prop: 'analysis', label: '分析结论', type: 'textarea', rows: 3, required: true }
		]
	},
	/** 淹没/侵蚀分区：行为分析上图与人口面积测算底数 */
	assessImpactZone: {
		id: 'assessImpactZone',
		title: '淹没侵蚀分区',
		searchPlaceholder: '请输入区县/地市名称',
		extraSearch: {
			prop: 'kind',
			placeholder: '分区类型',
			options: [
				{ label: '全部', value: '' },
				{ label: '风场淹没', value: 'wind_inundation' },
				{ label: '浪场侵蚀', value: 'wave_erosion' }
			]
		},
		columns: [
			{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 70, align: 'center' },
			{ prop: 'kind', label: '类型', width: 120, align: 'center' },
			{ prop: 'name', label: '区县', minWidth: 120, align: 'center' },
			{ prop: 'city', label: '地市', width: 100, align: 'center' },
			{ prop: 'areaKm2', label: '面积(km²)', width: 110, align: 'center' },
			{ prop: 'lengthKm', label: '岸线(km)', width: 100, align: 'center' },
			{ prop: 'population', label: SURVEY_LABELS.affectedPopulation, width: 110, align: 'center' },
			{ prop: 'lon', label: '经度', width: 100, align: 'center' },
			{ prop: 'lat', label: '纬度', width: 100, align: 'center' },
			{ prop: '', slot: 'geojson', label: '空间数据', width: 90, align: 'center' },
			{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 140, align: 'center' }
		],
		fields: [
			{
				prop: 'kind',
				label: '分区类型',
				required: true,
				type: 'select',
				options: [
					{ label: '风场淹没', value: 'wind_inundation' },
					{ label: '浪场侵蚀', value: 'wave_erosion' }
				]
			},
			{ prop: 'name', label: '区县', required: true },
			{ prop: 'city', label: '地市', required: true },
			{ prop: 'areaKm2', label: '面积(km²)', type: 'number', required: true },
			{ prop: 'lengthKm', label: '岸线长度(km)', type: 'number' },
			{ prop: 'population', label: SURVEY_LABELS.affectedPopulation, type: 'number' },
			{ prop: 'lon', label: '经度', type: 'number' },
			{ prop: 'lat', label: '纬度', type: 'number' },
			{
				prop: 'geojson',
				label: 'GeoJSON',
				type: 'textarea',
				rows: 6,
				placeholder: '可选，粘贴分区范围 Geometry / Feature'
			},
			{ prop: 'remark', label: '备注', type: 'textarea', rows: 2 }
		]
	}
}

// 合并到统一类型表，供 BaseDataManage 使用
Object.assign(SURVEY_BASE_TYPES, ASSESS_BASE_TYPES)

export const ASSESS_BASE_MENU = {
	name: '灾害评估数据',
	path: '/management/assess',
	children: [
		{ type: 'assessSar', name: 'SAR遥感场景', path: '/management/assessSar' },
		{ type: 'assessTyphoon', name: '台风过程', path: '/management/assessTyphoon' },
		{ type: 'assessLandPrice', name: '土地利用单价', path: '/management/assessLandPrice' },
		{ type: 'assessCountyEconomy', name: '县域经济数据', path: '/management/assessCountyEconomy' },
		{ type: 'assessStructure', name: '海上结构物', path: '/management/assessStructure' },
		{ type: 'assessDamage', name: '承灾体破坏样本', path: '/management/assessDamage' },
		{ type: 'assessImpactZone', name: '淹没侵蚀分区', path: '/management/assessImpactZone' },
		{ type: 'assessOvertop', name: '漫堤分析数据', path: '/management/assessOvertop' },
		{ type: 'assessIntegrate', name: '受灾整合台账', path: '/management/assessIntegrate' },
		{ type: 'assessReportSource', name: '整合数据来源', path: '/management/assessReportSource' }
	]
}
