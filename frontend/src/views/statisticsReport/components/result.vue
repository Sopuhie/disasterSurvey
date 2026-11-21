<!-- 统计报表--结果页面 -->
<template>
	<ZDialogCom
		:id="'zDialog'"
		ref="zDialogRef"
		:isDrag="false"
		:showTitle="false"
		:footer="false"
		:wrapStyle="wrapStyle"
		:bodyStyle="{ padding: '0 24px' }"
		v-show="true">
		<div class="box max-wh">
			<div class="resize-handle-x" v-resize="{ target: '#zDialog', direction: 'x', onMouseUp: onResizeComplete }"></div>
			<div class="resize-handle-y" v-resize="{ target: '#zDialog', direction: 'y', onMouseUp: onResizeComplete }"></div>
			<div class="title flex fa jsb">
				<ul class="tabs">
					<li
						:class="{ cur: tabIdx === idx }"
						v-for="(item, idx) in tabs"
						:key="'tabs_' + idx"
						@click="tabChange(item, idx)">
						{{ item }}
					</li>
				</ul>
				<div class="flex fa">
					<el-button
						type="primary"
						plain
						v-btn
						v-if="tabs[0] === '查询结果'"
						icon="Download"
						style="margin: 0 20px 0 12px"
						:disabled="show"
						@click="downloadExcel">
						<!-- <span>下载shp</span> -->
						<span>导出表格</span>
					</el-button>
					<el-icon class="icon_rotate" :class="show ? 'rotate270' : 'rotate90'" color="#ffffff" @click="setShow">
						<DArrowLeft v-if="show" />
						<DArrowRight v-else />
					</el-icon>
				</div>
			</div>
			<div class="con" :class="{ cur: show }" v-if="tabs[0] === '查询结果'">
				<div class="tableBox" ref="tableBox" style="height: calc(100% - 60px)">
					<ZTable
						ref="refTable"
						style="width: 100%; height: 100%; color: #333"
						:tableData="nowData"
						:tableHeaders="tableHeaders"
						:tableHeight="tableHeight"
						:imageSize="tableHeight - 160"
						@rowClick="rowClick"></ZTable>
				</div>
				<div class="footer flex faj" :class="{ cur: show }">
					<ZPagination
						ref="PaginRef"
						layout="total, prev, pager, next"
						:total="tData.length"
						:pageSize="mode.size"
						:currentPage="mode.current"
						@sizeChange="sizeChange"
						@currentChange="currentChange"></ZPagination>
				</div>
			</div>
			<div class="tj_result_box" v-if="tabs[0] === '统计详情' && showChart">
				<ECharts :option="eChartOption" :resize="false"></ECharts>
			</div>
		</div>
	</ZDialogCom>
</template>

<script setup>
	import ZDialogCom from '@/components/dialog/index.vue'
	import ZTable from '@/components/table/index.vue'
	import ZPagination from '@/components/pagination/index.vue'
	import ECharts from '@/components/eCharts/index.vue'

	import { spatialApi } from '@/api/spatial/index.js'
	const api = spatialApi()

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	const tableBox = ref(null)
	const tableBox2 = ref(null)
	const refTable = ref(null)
	const tabIdx = ref(0)
	const current = ref(1)
	const show = ref(true)
	const panelFlag = ref(true)
	const layersIdx = ref('')
	const tableHeight = ref(300)
	const tableHeight2 = ref(300)
	const eChartOption = ref({})
	const showChart = ref(false)
	const nowData = ref([])
	const mode = ref({
		current: 1,
		size: 20
	})

	const wrapStyle = reactive({
		width: '896px',
		height: '56px',
		bottom: '40px',
		right: '1px'
		// transition: 'height 0.5s ease-out'
	})

	const props = defineProps({
		pageListDataTotal: {
			type: Array,
			default: () => []
		},
		isShow: {
			type: Boolean,
			default: true
		},
		tabs: {
			type: Array,
			default: () => ['查询结果']
		},
		// 图表数据
		eChartData: {
			type: Object,
			default: () => {}
		},
		// 查询的结果数据
		resultTableData: {
			type: Object,
			default: () => {}
		},
		// 统计的结果数据
		staTableData: {
			type: Array,
			default: () => []
		},
		staItemTableData: {
			type: Array,
			default: () => []
		},
		echartsType: {
			type: String,
			default: 'pie'
		},
		lyrUrl: {
			type: String,
			default: ''
		},
		selectLyrInfo: {
			type: Object,
			default: () => {}
		},
		tjZdName: {
			type: Object,
			default: function () {
				return { fz: '', tj: '' }
			}
		},
		tjVal: {
			type: Object,
			default: function () {
				return {
					fzzd: undefined,
					tjzd: undefined,
					tjType: 'sum',
					echartsType: 'pie'
				}
			}
		}
	})

	/**
	 * 表格数据
	 */
	const tableData = computed(() => {
		let data = []
		if (tabIdx.value === 0) {
			data = props.resultTableData
		} else if (tabIdx.value === 1) {
			data = panelFlag.value ? props.staTableData : props.staItemTableData
		}
		return data
	})
	const tData = computed(() => {
		const data = tableData.value
		if (data?.data?.length) {
			layersIdx.value = data.tabName
			return data.data
		} else {
			return []
		}
	})
	const tableHeaders = computed(() => {
		if (tableData.value.length === 0) return []
		const columns = tableData.value['columns']
		let headers = []
		for (const key in columns) {
			if (Object.hasOwnProperty.call(columns, key)) {
				headers.push({ label: columns[key], prop: key, minWidth: 120 })
			}
		}
		return headers
	})
	const getTableHeight = () => {
		nextTick(() => {
			tableHeight.value = tableBox?.value?.clientHeight
		})
	}
	// 深度监听isShow，控制弹窗显示隐藏
	watch(
		() => props.isShow,
		newVal => {
			show.value = newVal
			wrapStyle.height = newVal ? '56px' : '460px'
			showChart.value = false
			setTimeout(() => {
				if (props.tabs[0] === '统计详情') {
					showChart.value = true
					eChartOption.value = props.eChartData
				}
			}, 1000)
		},
		{ deep: true, immediate: true }
	)

	const emits = defineEmits(['resultPopChange'])

	onMounted(() => {
		nextTick(() => {
			getTableHeight()
			getPageData()
		})
	})

	const onResizeComplete = obj => {
		const { width, height, direction } = obj
		if (direction === 'y') {
			wrapStyle.width = width + 'px'
			wrapStyle.height = height + 'px'
		}
		getTableHeight()
	}

	const getPageData = () => {
		const data = tableData.value
		if (data?.data?.length) {
			layersIdx.value = data.tabName
			const { size, current } = mode.value
			const start = (current - 1) * size
			const end = start + size
			nowData.value = data.data.slice(start, end)
		} else {
			nowData.value = []
		}
	}

	/**
	 * 表格行点击事件
	 * @param {Object} row 行数据
	 */
	const rowClick = row => {
		for (const [key, val] of Object.entries(row.geometry)) {
			mapStore.activeMap.featureManager.removeFeatures()
			mapStore.activeMap.flyToManager.flyToRegion(
				{
					type: 'Feature',
					geometry: {
						type: 'Polygon',
						coordinates: row.geometry[key]
					}
				},
				true
			)
		}
	}
	const tabChange = (item, idx) => {
		if (show.value) return
		tabIdx.value = idx
		panelFlag.value = idx === 1 ? true : false
		current.value = 1
		mode.value.current = 1
		if (idx === 0) {
			// paginationCur(1, 'tabChange')
		}
		if (idx === 1) {
			tableHeight2.value = tableBox2.value.clientHeight
		}
	}
	/**
	 * 展开/收起--切换，互斥
	 */
	const setShow = () => {
		show.value = !show.value
		wrapStyle.height = show.value ? '56px' : '460px'
		emits('resultPopChange', show.value)
	}
	/**
	 * 下载shp
	 */
	const downloadShp = async () => {
		let loading = ElLoading.service({
			lock: true,
			text: 'Loading',
			background: 'rgba(0, 0, 0, 0.7)'
		})
		try {
			let res = await api.exportQuery({
				exportType: 'shp',
				fileUrl: '',
				geometryType: props.selectLyrInfo.geometryType,
				groupData: [{}],
				serviceUrl: decodeURIComponent(props.lyrUrl + '/query'),
				showType: '1',
				spatialReference: mapStore.activeMap.projection.getCode().split(':')[1],
				targetFiled: props.selectLyrInfo.fields.find(m => m.type == 'esriFieldTypeOID').name,
				targetFiledValues: props.pageListDataTotal.join(',')
			})
			if (res.resultCode == 'S') {
				const blob = new Blob([res.data], { type: 'application/zip;charset=utf-8' })
				const link = document.createElement('a')
				link.href = URL.createObjectURL(blob)
				link.download = 'queryResult.zip'
				link.click()
				URL.revokeObjectURL(link.href)
			}
		} catch (err) {
			// 如果在try块里有异常被抛出时执行的语句
			console.log(err)
		} finally {
			loading.close()
		}
	}
	/**
	 * 下载excel
	 */
	const downloadExcel = () => {
		refTable.value.downloadExcel()
	}
	/**
	 * 分页事件
	 * @param {Number} val
	 */
	const sizeChange = val => {
		mode.value.size = val
		getPageData()
	}
	const currentChange = val => {
		mode.value.current = val
		getPageData()
	}
</script>

<style lang="scss" scoped>
	.resize-handle-x {
		position: absolute;
		top: 20%;
		left: -22px;
		width: 2px;
		height: 60%;
		background-color: #ddd;
		cursor: ew-resize;
	}
	.resize-handle-y {
		position: absolute;
		top: 0px;
		left: 20%;
		width: 60%;
		height: 2px;
		background-color: #ddd;
		cursor: ns-resize;
	}
	.zDialog__body {
		overflow: unset !important;
	}
	.box {
		position: relative;
		// overflow: hidden;
	}
	.title {
		display: flex;
		align-items: center;
		justify-content: space-between;
		height: 56px;
		& .tabs {
			display: flex;
			align-items: center;
			& li {
				position: relative;
				height: 56px;
				line-height: 56px;
				font-size: 16px;
				color: var(--el-text-color-regular);
				margin-right: 20px;
				cursor: pointer;
				&.cur {
					color: var(--el-text-color-regular);
				}
			}
		}
		& .icon_rotate {
			cursor: pointer;
			-webkit-transition: all 0.5s;
			-moz-transition: all 0.5s;
			-ms-transition: all 0.5s;
			-o-transition: all 0.5s;
			transition: all 0.5s;
			&:hover {
				color: var(--sidebar-text);
			}
			&.rotate90 {
				transform: rotate(90deg);
			}
			&.rotate270 {
				transform: rotate(270deg);
			}
		}
	}

	.con {
		height: calc(100% - 56px);
		opacity: 1;
		transition: opacity 0.5 ease-out;
		&.cur {
			opacity: 0;
		}
		& .filter_content {
			display: flex;
			align-items: center;
			height: 86px;
			font-size: 14px;
			font-family: PingFangSC-Regular, PingFang SC;
			font-weight: 400;
			color: var(--el-text-color-regular);
			& .tips_txt {
				padding-right: 24px;
			}
			& .key {
				margin: 0 12px 0 24px;
			}
		}
	}
	.tj_result_box {
		width: 100%;
		height: calc(100% - 72px);
	}

	.footer {
		height: 60px;
		box-shadow: inset 0 1px 0 0 #e8e8e8;
		opacity: 1;
		transition: opacity 0.5 ease-out;
		&.cur {
			opacity: 0;
		}
	}
	:deep() {
		.el-scrollbar__view {
			display: block !important;
		}
	}
</style>
