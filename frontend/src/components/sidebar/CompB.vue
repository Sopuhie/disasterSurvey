<template>
	<ZDialogCom
		v-if="showDialog"
		:title="title"
		:wrapStyle="wrapStyle"
		toPath="cancel"
		:footer="false"
		showSwitch
		class="abcefdsa"
		@cancel="cancelHandler"
		:bodyStyle="{ padding: '16px' }">
		<div class="max-wh oy flex fc">
			<div class="formBox" v-if="formData">
				<FormScheme ref="formRef" :formData="formData" :rules="rules" :formMode="addFormMode">
					<template #Draw>
						<el-button v-btn type="primary" plain @click="onDraw">绘制</el-button>
						<el-button v-btn type="primary" plain @click="importRangeHandler()">导入</el-button>
					</template>
					<template #YdjcBtn>
						<div class="max-w flex fa jend">
							<el-button v-btn type="primary" plain @click="statisHandler">统计</el-button>
						</div>
					</template>
				</FormScheme>
			</div>
			<div class="chartBox" v-if="showChart && chartOption.series && chartOption.series.length > 0">
				<ECharts class="max-wh" :option="chartOption" :resize="false"></ECharts>
			</div>
			<div class="tableBox" v-if="!showChart && tableData.length > 0">
				<ZTable class="max-wh" :tableData="tableData" :tableHeaders="tableHeaders"></ZTable>
			</div>
		</div>
	</ZDialogCom>
</template>

<script setup>
	import ZDialogCom from '@/components/dialog/index.vue'
	import FormScheme from '@/components/form/scheme.vue'
	import ZTable from '@/components/table/index.vue'
	import { getWindowSize } from '@/utils/index.js'
	import ECharts from '@/components/eCharts/index.vue'

	import { useSystemStore } from '@/store/modules/system.js'
	const store = useSystemStore()

	import { loadSpatialData, unloadSpatialData, importRangeHandler, drawPolygon } from './spatialQuery/index.js'
	import {
		loadSpatialData as loadCitySpatialData,
		unloadSpatialData as unloadCitySpatialData,
		queryStatisticsData
	} from './spatialQuery/citySystem/cityStatisticsView.js'
	// 省域查询
	import { queryIndustryData } from './spatialQuery/provinceSystem/industryView.js'
	import { queryHouseData } from './spatialQuery/provinceSystem/houseView.js'
	import { queryLandUseData } from './spatialQuery/provinceSystem/landUseView.js'
	// 市域查询
	import { queryCityGreenEcologyData } from './spatialQuery/citySystem/cityGreenEcologyView.js'
	import { queryCityInfoTechnologyData } from './spatialQuery/citySystem/cityInfoTechnologyView.js'
	import { queryCityLandUseData } from './spatialQuery/citySystem/cityLandUseView.js'
	import { queryCityPlanRuleData } from './spatialQuery/citySystem/cityPlanRuleView.js'
	import { queryCityEducationalResourData } from './spatialQuery/citySystem/cityEducationalResourView.js'
	// 特色域查询
	import { queryFeatureFacilityWarningData } from './spatialQuery/featureSystem/featureFacilityWarningView.js'
	import { queryFeatureHistoryCityData } from './spatialQuery/featureSystem/featureHistoryCityView.js'
	import { queryFeatureSmartsLandData } from './spatialQuery/featureSystem/featureSmartsLandView.js'

	const list = ref([])
	const wrapStyle = reactive({
		width: '440px',
		height: `${(getWindowSize().h.value / 5) * 4}px`,
		top: '80px',
		left: `${getWindowSize().w.value - 440}px`
	})
	const formRef = ref(null)
	const showDialog = ref(false)
	const title = ref('')
	const showChart = ref(true)
	const formData = ref(null)
	const addFormMode = ref({
		date: '',
		type: '',
		result: '',
		industry: '',
		enterprise: '',
		group: '',
		DLMC: '',
		tjzd: null,
		tjData: null,
		fzzd: null,
		tjType: '',
		iLabel: ''
	})

	const tableData = ref([])
	const tableHeaders = ref([])
	const rules = ref({})
	const chartOption = ref({})
	const selectedItemConfig = ref(null)
	const hadLoadLyrName = ref('')

	watch(
		addFormMode,
		val => {
			showChart.value = !addFormMode.value.result.toLowerCase().includes('table')

			nextTick(() => {
				if (val.tjData) {
					const configure = JSON.parse(store.menuItemData.configure)
					const iLabel = val.tjData
					addFormMode.value.iLabel = iLabel
					const fields = configure[title.value]['fields']
					let configItem = fields[0]['children'][0]['options'].filter(item => item.label === iLabel)[0]

					if (hadLoadLyrName.value != iLabel && configItem) {
						// 加载展示数据
						loadCitySpatialData({ configItem })
						hadLoadLyrName.value = iLabel
					}

					const strTypes = ['FIRST', 'LAST', 'MIN', 'MAX', 'COUNT']

					const options2 = fields[2]['children'][0]['options'].filter(item => item.label === iLabel)[0]['fields']
					const options4 = fields[4]['children'][0]['options'].filter(item => item.label === iLabel)[0]['fields']

					formData.value.fields[2]['children'][0]['options'] = options2
					formData.value.fields[4]['children'][0]['options'] = options4
				}
			})
		},
		{ deep: true, immediate: true }
	)
	watch(
		() => store.menuItemData,
		() => {
			nextTick(() => {
				title.value !== '' && getFormData()
			})
		}
	)
	watch(
		() => store.monitorItemData,
		val => {
			if (!val) {
				return false
			}
			nextTick(() => {
				handleNodeClick(val)
			})
		},
		{ deep: true, immediate: true }
	)
	const route = useRoute()
	watch(
		() => route.path,
		() => {
			resetForm()
			tableHeaders.value = []
			tableData.value = []
		}
	)

	onMounted(() => {
		handleNodeClick(store.monitorItemData)
	})

	/**
	 * 功能模块--列表点击事件
	 * @param {Object} item 点击的节点数据
	 */
	const handleNodeClick = data => {
		if (!data) return
		if (data.name) {
			showDialog.value = false
			selectedItemConfig.value = data
			if (title.value === data.name) {
				return
			}
			title.value = data.name
			nextTick(() => {
				getFormData()
				getFormRules()
				showDialog.value = true
				resetForm()
			})

			loadSpatialData({
				configItem: data,
				tableHeaders,
				tableData,
				chartOption
			})
		}
	}

	/**
	 * 重置表单数据
	 */
	const resetForm = () => {
		addFormMode.value = {
			date: '',
			type: '',
			result: '',
			industry: '',
			enterprise: '',
			group: '',
			DLMC: [],
			tjzd: []
		}
		chartOption.value = {}
		loadSpatialData({
			configItem: selectedItemConfig.value,
			tableHeaders,
			tableData,
			chartOption
		})
	}

	/**
	 * 统计
	 */
	const statisHandler = async () => {
		if (addFormMode.value.iLabel && route.path === '/citySystem') {
			let itemObjs = {
				tjDataItem: null, // 统计数据项
				tjzdItem: null, // 统计字段
				tjTypeItem: null, // 统计类型
				fzzdItem: null // 分组字段
			}
			const configure = JSON.parse(store.menuItemData.configure)
			const iLabel = addFormMode.value.iLabel
			const fields = configure[title.value]['fields']

			itemObjs.tjDataItem = fields[0]['children'][0]['options'].filter(item => item.label === iLabel)[0]
			const options2 = fields[2]['children'][0]['options'].filter(item => item.label === iLabel)[0]['fields']
			itemObjs.tjzdItem = options2.filter(item => addFormMode.value.tjzd.includes(item.value))

			itemObjs.tjTypeItem = fields[3]['children'][0]['options'].filter(
				item => item.value === addFormMode.value.tjType
			)[0]

			const options4 = fields[4]['children'][0]['options'].filter(item => item.label === iLabel)[0]['fields']
			itemObjs.fzzdItem = options4.filter(item => item.value === addFormMode.value.fzzd)
			showChart.value = false
			queryStatisticsData(configure, itemObjs, tableHeaders, tableData, {})
			return false
		}

		// 表单校验
		let formRes = await formRef.value.submitForm()
		if (!formRes) return
		// 清空原有数据
		tableHeaders.value = []
		tableData.value = []
		// 省
		if (selectedItemConfig.value.name == '产业监测模块') {
			queryIndustryData(selectedItemConfig.value, addFormMode.value, tableHeaders, tableData, chartOption)
		}
		if (selectedItemConfig.value.name == '房屋监测模块') {
			queryHouseData(selectedItemConfig.value, addFormMode.value, tableHeaders, tableData, chartOption)
		}
		if (selectedItemConfig.value.name == '用地监测模块') {
			queryLandUseData(selectedItemConfig.value, addFormMode.value, tableHeaders, tableData, chartOption)
		}
		// 市
		if (selectedItemConfig.value.name == '用地监测') {
			const configure = JSON.parse(store.menuItemData.configure)
			const itemFields = configure[selectedItemConfig.value.name]['fields']
			queryCityLandUseData(
				selectedItemConfig.value,
				addFormMode.value,
				tableHeaders,
				tableData,
				chartOption,
				itemFields
			)
		}
		if (selectedItemConfig.value.name == '规划导则') {
			queryCityPlanRuleData(selectedItemConfig.value, addFormMode.value, tableHeaders, tableData, chartOption)
		}
		if (selectedItemConfig.value.name == '服务设施监测') {
			queryCityEducationalResourData(selectedItemConfig.value, addFormMode.value, tableHeaders, tableData, chartOption)
		}
		if (selectedItemConfig.value.name == '工信产业监测') {
			queryCityInfoTechnologyData(selectedItemConfig.value, addFormMode.value, tableHeaders, tableData, chartOption)
		}
		if (selectedItemConfig.value.name == '绿色生态监测') {
			queryCityGreenEcologyData(selectedItemConfig.value, addFormMode.value, tableHeaders, tableData, chartOption)
		}
		// 特色
		if (selectedItemConfig.value.name == '历史名城场景') {
			queryFeatureHistoryCityData(selectedItemConfig.value, addFormMode.value, tableHeaders, tableData, chartOption)
		}
		if (selectedItemConfig.value.name == '智慧土储场景') {
			queryFeatureSmartsLandData(selectedItemConfig.value, addFormMode.value, tableHeaders, tableData, chartOption)
		}
		if (selectedItemConfig.value.name == '设施预警场景') {
			queryFeatureFacilityWarningData(selectedItemConfig.value, addFormMode.value, tableHeaders, tableData, chartOption)
		}
	}
	/**
	 * 绘制
	 */
	const onDraw = () => {
		drawPolygon()
	}
	/**
	 * 关闭弹窗
	 */
	const cancelHandler = () => {
		showDialog.value = false
		title.value = ''
		store.setMonitorItemData(null)
		localStorage.removeItem('monitorItemData')

		unloadSpatialData()
		unloadCitySpatialData()
		hadLoadLyrName.value = ''
	}

	const getFormData = () => {
		if (store.menuItemData?.configure) {
			const configure = JSON.parse(store.menuItemData.configure)
			list.value = configure?.lists ? configure.lists : []
			formData.value = configure[title.value] ? configure[title.value] : null

			if (route.path === '/citySystem') {
				formData.value.fields[2]['children'][0]['options'] = []
				formData.value.fields[4]['children'][0]['options'] = []
			}
		}
	}
	const getFormRules = () => {
		if (store.menuItemData?.configure) {
			const configure = JSON.parse(store.menuItemData.configure)
			rules.value = configure[selectedItemConfig.value.name]?.rules || {}
		}
	}
</script>

<style lang="scss" scoped>
	.lists {
		height: calc(100% - 0px);
		background: var(--sidebar-bg);
		border-radius: 12px;
		padding: 24px 16px 8px;
		& li {
			padding-left: 8px;
			height: 32px;
			color: var(--el-text-color-regular);
			&:hover {
				background-color: var(--sq-color-4);
			}
		}
	}
	.formBox {
		flex-shrink: 0;
	}
	.tableBox,
	.chartBox {
		min-height: 300px;
		flex-grow: 1;
		background-color: var(--sidebar-bg-color);
	}
</style>
