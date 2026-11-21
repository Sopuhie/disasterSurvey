<template>
	<ZDialogCom
		ref="zDialogRef"
		title="统计报表"
		toPath="cancel"
		:isDrag="false"
		showSwitch
		:wrapStyle="wrapStyle"
		:bodyStyle="{ padding: '16px' }"
		:okBtnTxt="okBtnTxt"
		@switchHandle="switchHandle"
		@cancel="cancel"
		@handleOk="handleOk">
		<div class="max-wh oy">
			<div class="items mb-32">
				<div class="itemTitle">
					<span class="txt" title="查询前，请确认已打开/已勾选该图层">查询条件</span>
					<el-button v-btn type="primary" plain :disabled="resetBtn" @click="reset">
						<el-icon class="el-icon--left"><Refresh /></el-icon>
						<span>重置</span>
					</el-button>
				</div>
				<p class="item_ti">
					<span class="key">查询图层</span>
					<el-tree-select
						class="statisticsReportTree"
						v-model="statiLayerUrl"
						:data="statiLayerDatas"
						render-after-expand
						fit-input-width
						clearable
						filterable
						placeholder="请选择"
						style="width: 100%; background-color: var(--el-bg-color-opacity)"
						@change="onChange"></el-tree-select>
				</p>
				<QueryCom ref="queryComRef" :fields="statiLayerFields" :lyrUrl="statiLayerUrl"></QueryCom>
				<p class="mt-24" style="margin-left: 42px">
					<el-button type="primary" plain v-btn icon="Plus" @click="addQueryItem">新增查询条件</el-button>
				</p>
			</div>
			<div class="items mb-32">
				<div class="itemTitle mb-10">
					<span class="txt">
						查询范围
						<small>请选择以下其中一种方式确定</small>
					</span>
					<span></span>
				</div>
				<ScopeCom ref="scopeComRef" :radioArr="radioArr"></ScopeCom>
				<p class="mt-24">
					<el-button type="primary" plain v-btn @click="addTJHandle">
						<el-icon class="el-icon--left">
							<Plus v-if="!showTj" />
							<Minus v-else />
						</el-icon>
						<span>{{ showTj ? '删除统计条件' : '新增统计条件' }}</span>
					</el-button>
				</p>
			</div>
			<div class="items" v-if="showTj">
				<div class="itemTitle">
					<span class="txt">统计条件</span>
					<span></span>
				</div>
				<StatisticsCom ref="StatisticsComRef" :fields="statiLayerFields"></StatisticsCom>
			</div>
		</div>
	</ZDialogCom>
	<ResultCom
		ref="resultComRef"
		v-if="isHide"
		:isShow="isShow"
		:tabs="tabs"
		:resultTableData="statiLayerFeatures"
		:pageListDataTotal="statiLayerIds"
		:lyrUrl="statiLayerUrl"
		:selectLyrInfo="selectLyrInfo"
		:eChartData="eChartData"
		@resultPopChange="resultPopChange"></ResultCom>
</template>

<script setup>
	import ZDialogCom from '@/components/dialog/index.vue'
	import QueryCom from '@/views/statisticsReport/components/query.vue'
	import ScopeCom from '@/views/statisticsReport/components/scope.vue'
	import StatisticsCom from '@/views/statisticsReport/components/statistics.vue'
	import ResultCom from '@/views/statisticsReport/components/result.vue'
	import { arcGisApi } from '@/api/gis/arcgis.js'
	import { eChartOptions } from './components/resEchartsData.js'
	import { getWindowSize } from '@/utils/index.js'
	import _ from 'lodash'

	const api = arcGisApi()

	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	const queryComRef = ref(null)
	const scopeComRef = ref(null)
	const zDialogRef = ref(null)
	const StatisticsComRef = ref(null)
	const resetBtn = ref(false)
	const showTj = ref(false)
	const isHide = ref(false)
	const isShow = ref(false)
	const statiLayerFields = ref([])
	let selectLyrInfo = ref(null)
	const okBtnTxt = ref('查询')
	const tabs = ref(['查询结果'])
	const statiLayerUrl = ref('')
	const radioArr = reactive([
		{ label: '导入', value: 3 },
		{ label: '绘制', value: 4 }
	])

	const wrapStyle = reactive({
		width: '490px',
		height: `${getWindowSize().h.value - 193}px`,
		top: '95px',
		left: `${getWindowSize().w.value - 510}px`
	})

	const legendData = computed(() => [])
	const statiLayerDatas = ref([])
	const statiLayerIds = ref([])
	const statiLayerFeatures = ref({})
	const eChartData = ref({})
	const eChartTimer = ref(null)

	/**
	 * 深度监听legendData变化
	 */
	watch(
		legendData,
		newVal => {
			if (newVal?.length === 0) {
				statiLayerUrl.value = ''
			}
		},
		{ deep: true, immediate: true }
	)
	/**
	 * 监听values变化
	 */
	watch(statiLayerUrl, newVal => {
		if (newVal?.length === 0) {
			queryComRef.value.reset()
		}
	})

	const emits = defineEmits(['cancel'])
	function cancel() {
		emits('cancel')
	}

	/**
	 * 监听当前加载的图层变化
	 */
	const fetchStatiLayerDatas = async () => {
		statiLayerDatas.value = []

		await Promise.all(
			mapStore.loadedThemes.map(async theme => {
				if (theme?.serviceInfo?.length) {
					const lyrData = {
						label: theme.themeInfo.name,
						value: theme.themeInfo.themeId,
						children: []
					}

					await Promise.all(
						theme.serviceInfo.map(async service => {
							const serviceData = {
								label: service.serviceName,
								value: service.serviceId,
								children: []
							}
							const serviceInfo = await api.getMapServerInfo(service.serviceUrl)
							if (!serviceInfo?.error) {
								const meta = mapStore.metaDataDeserialized(service.metaData)
								const layers = meta?.layers?.length
									? serviceInfo.layers.filter(lyr => meta.layers.includes(lyr.id))
									: serviceInfo.layers

								serviceData.children = layers.map(lyr => ({
									label: lyr.name,
									value: `${service.serviceUrl}/${lyr.id}`
								}))
							}
							lyrData.children.push(serviceData)
						})
					)

					statiLayerDatas.value.push(lyrData)
				}
			})
		)
	}

	watch(() => mapStore.loadedThemes, fetchStatiLayerDatas, {
		immediate: true
	})

	onBeforeUnmount(() => {
		eChartTimer.value && clearTimeout(eChartTimer.value)
	})
	/**
	 * 查询图层选择
	 * @param value {string} 图层url
	 */
	const onChange = async value => {
		statiLayerUrl.value = value
		statiLayerFields.value = []
		if (value.includes('MapServer')) {
			selectLyrInfo.value = await api.getMapServerInfo(value)
			if (selectLyrInfo.value?.type == 'Feature Layer' && selectLyrInfo.value?.fields?.length) {
				statiLayerFields.value = selectLyrInfo.value.fields
					.filter(m => !m.name.toLowerCase().includes('shape') && !m.name.toLowerCase().includes('objectid'))
					.map(feld => {
						return {
							type: feld.type,
							key: feld.name,
							value: feld.name,
							title: feld.alias || feld.name,
							label: feld.alias || feld.name
						}
					})
			} else {
				ElMessage({
					showClose: true,
					message: `请确认该图层为矢量图层，且有属性字段！`,
					type: 'warning'
				})
			}
		}
	}

	const switchHandle = type => {
		isShow.value = !type
	}
	/**
	 * 结果弹窗切换
	 * @param type {boolean} 显示/隐藏
	 */
	const resultPopChange = type => {
		isShow.value = type
		zDialogRef.value.switchHandle()
		setBoxStyle(type)
	}
	/**
	 * 设置弹窗位置
	 */
	const setBoxStyle = type => {
		if (type) {
			wrapStyle.left = `${getWindowSize().w.value - 440}px`
		} else {
			wrapStyle.left = `${getWindowSize().w.value}px`
		}
	}
	/**
	 * 重置
	 */
	const reset = () => {
		resetBtn.value = true
		queryComRef.value.reset()
		values.value = ''
		setTimeout(() => {
			resetBtn.value = false
		}, 1200)
	}
	/**
	 * 取消
	 */
	// const cancel = () => {
	// 	localStorage.setItem('oldRoutePath', route.path)
	// 	router.push('/')
	// }
	/**
	 * 查询/查询并统计
	 */
	const handleOk = async () => {
		// 查询条件数据
		const lists = queryComRef.value.lists
		let where = ''
		statiLayerIds.value = []
		lists.forEach(item => {
			if (item.key && item.relation && item.val) {
				const val = item.relationType.includes('String') ? `'${item.val}'` : parseFloat(item.val)
				where += ` ${item.type} ${item.name} ${item.relation} ${val}`
			}
		})
		let res = null
		let loading = null
		if (showTj.value) {
			tabs.value = ['统计详情']
			const statistics = StatisticsComRef.value.tj
			if (!statistics.fzzd) {
				ElMessage({
					showClose: true,
					message: `请选择分组字段！`,
					type: 'error',
					offset: 100
				})
				return
			} else if (!statistics.tjzd) {
				ElMessage({
					showClose: true,
					message: `请选择统计字段！`,
					type: 'error',
					offset: 100
				})
				return
			}
			clearTimeout(eChartTimer.value)
			loading = ElLoading.service({
				lock: true,
				text: 'Loading',
				background: 'rgba(0, 0, 0, 0.7)'
			})
			// 获取统计数据
			let res = await getStatistic(statistics, where)
			if (res) {
				const eChartsType = statistics.echartsType
				let options = JSON.parse(JSON.stringify(eChartOptions[eChartsType]))
				let seriesData = []
				if (eChartsType === 'pie') {
					res.features.forEach(m => {
						seriesData.push({
							name: m.attributes[statistics.fzzd],
							value: m.attributes[statistics.tjType]
						})
					})
					options['series'][0]['data'] = seriesData
				}
				if (eChartsType === 'bar' || eChartsType === 'line') {
					let xAxisData = []
					res.features.forEach(m => {
						xAxisData.push(m.attributes[statistics.fzzd])
						seriesData.push(m.attributes[statistics.tjType])
					})
					options['xAxis']['data'] = xAxisData
					options['series'][0]['data'] = seriesData
				}
				eChartTimer.value = setTimeout(() => {
					eChartData.value = res.features?.length ? options : {}
					isHide.value = true
					zDialogRef.value.switchHandle()
					loading.close()
				}, 500)
			}
		} else {
			if (!statiLayerUrl.value) {
				ElMessage({
					showClose: true,
					message: `请选择查询图层！`,
					type: 'error',
					offset: 100
				})
				return
			}
			loading = ElLoading.service({
				lock: true,
				text: 'Loading',
				background: 'rgba(0, 0, 0, 0.7)'
			})
			tabs.value = ['查询结果']
			res = await getFeatureIds(where.trim())
			if (res?.objectIds?.length) {
				statiLayerIds.value = res.objectIds
				let featRes = await getFeaturesByIds(res.objectIds)
				if (featRes?.features?.length) {
					let filterFields = ['fid', 'object', 'shape']
					featRes?.features.forEach(m => {
						m.attributes['geometry'] = m.geometry
					})
					for (const [key, val] of Object.entries(featRes.fieldAliases)) {
						if (filterFields.some(n => key.toLowerCase().includes(n))) {
							delete featRes.fieldAliases[key]
						}
					}
					statiLayerFeatures.value = {
						data: featRes.features.map(feat => feat.attributes),
						tabName: '查询结果',
						columns: featRes.fieldAliases
					}
				}
				eChartTimer.value = setTimeout(() => {
					isHide.value = true
					zDialogRef.value.switchHandle()
					loading.close()
				}, 500)
			}
		}
	}

	/**
	 * 统计数据
	 */
	const getStatistic = async (statisDatas, where) => {
		let range = getFilters()
		let params = {
			where: where || '1=1',
			outFields: selectLyrInfo.value.fields.find(m => m.type == 'esriFieldTypeOID').name,
			returnGeometry: false,
			returnDistinctValues: false,
			returnIdsOnly: true,
			outStatistics: encodeURI(
				JSON.stringify([
					{
						statisticType: statisDatas.tjType,
						onStatisticField: statisDatas.tjzd,
						outStatisticFieldName: statisDatas.tjType
					}
				])
			),
			groupByFieldsForStatistics: statisDatas.fzzd,
			returnCountOnly: false,
			f: 'pjson'
		}
		return await api.query(statiLayerUrl.value, Object.assign(params, range))
	}
	/**
	 * 根据 ID 集合查询获取要素
	 */
	const getFeaturesByIds = async ids => {
		if (ids?.length) {
			let objectidName = selectLyrInfo.value.fields.find(m => m.type == 'esriFieldTypeOID').name
			let params = {
				where: `${objectidName} in(${ids.toString()})`,
				outFields: '*',
				returnGeometry: true,
				returnDistinctValues: false,
				returnIdsOnly: false,
				inSR: mapStore.activeMap.projection.getCode().split(':')[1],
				outSR: mapStore.activeMap.projection.getCode().split(':')[1],
				f: 'pjson'
			}
			return await api.query(statiLayerUrl.value, params)
		}
	}
	/**
	 * 根据查询条件获取数据的 ID 集合
	 */
	const getFeatureIds = async where => {
		let range = getFilters()
		let params = {
			where: where || '1=1',
			outFields: selectLyrInfo.value.fields.find(m => m.type == 'esriFieldTypeOID').name,
			returnGeometry: false,
			returnDistinctValues: false,
			returnIdsOnly: true,
			f: 'pjson'
		}
		return await api.query(statiLayerUrl.value, Object.assign(params, range))
	}

	/**
	 * 获取查询范围条件
	 */
	const getFilters = () => {
		let filters = {}
		if (appStore.switchObj.importData && mapStore.getImportDatas?.length) {
			let coords = mapStore.getImportDatas
				.filter(feat => feat.check)
				.map(m => {
					if (m.geometry.geometry.type == 'MultiPolygon') {
						let coors = []
						m.geometry?.geometry?.coordinates.map(k => {
							coors.push(k[0])
						})
						return _.flatten(coors)
					} else if (m.geometry.geometry.type == 'Polygon') {
						return m.geometry.geometry.coordinates[0]
					} else {
						return []
					}
				})
			filters = {
				geometry: encodeURI(JSON.stringify({ hasZ: false, hasM: false, rings: coords })),
				spatialRel: 'esriSpatialRelIntersects',
				geometryType: 'esriGeometryPolygon',
				inSR: mapStore.activeMap.projection.getCode().split(':')[1],
				outSR: mapStore.activeMap.projection.getCode().split(':')[1]
			}
		}
		return filters
	}

	/**
	 * 新增查询条件
	 */
	const addQueryItem = () => {
		queryComRef.value.add()
	}
	/**
	 * 新增统计条件
	 */
	const addTJHandle = () => {
		showTj.value = !showTj.value
		okBtnTxt.value = showTj.value ? '查询并统计' : '查询'
	}
</script>

<style lang="scss"></style>
