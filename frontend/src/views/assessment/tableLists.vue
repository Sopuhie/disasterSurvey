<template>
	<div class="tableLists" :class="{ isCollapse: appStore.switchObj.sidebarFlag }">
		<div class="tableLists__header flex fa jsb pl-10 pr-10">
			<el-button v-btn type="primary" @click="searchHandle">
				<el-icon><CirclePlus /></el-icon>
				<span>新建</span>
			</el-button>
			<div>
				<el-button v-btn @click="close">
					<el-icon><Close /></el-icon>
					<span>关闭</span>
				</el-button>
			</div>
		</div>
		<div class="tableLists__content mt-10 p-10">
			<div class="tableBox" ref="tableBox" style="height: calc(100% - 52px)">
				<ZTable class="max-wh" :tableData="tableData" :tableHeight="tableHeight" :tableHeaders="tableHeaders">
					<template #idx="{ scope }">
						{{ paginationIdxVal(scope.$index) }}
					</template>
					<template #action="{ scope }">
						<el-button v-btn type="danger" link @click="delHandle(scope.row)">删除</el-button>
						<el-button v-btn type="primary" link @click="getDetail(scope.row)">详情</el-button>
					</template>
				</ZTable>
			</div>
			<ZPagination
				ref="PaginRef"
				:total="20"
				:pageSize="mode.size"
				:currentPage="mode.current"
				@sizeChange="sizeChange"
				@currentChange="currentChange"></ZPagination>
		</div>
	</div>
	<!-- 详情展示 -->
	<ZDialogCom
		ref="addFileDialogRef"
		title="信息展示"
		appendToBody
		:footer="false"
		width="960px"
		height="550px"
		alignCenter
		closeOnClickModal>
		<VerticalTable :rows="2" :tableData="transformToTableData(listData)"></VerticalTable>
		<ul class="fileList mt-10">
			<li class="flex fa jsb" v-for="(item, index) in fileList" :key="'file_' + item.name">
				<div class="flex fa">
					<img :src="getAssetsFile(item.type)" width="40" alt="" />
					<div class="ml-8">
						<p class="name">{{ item.name }}</p>
						<p class="info">
							<span :class="['tip', item.tip]">{{ item.tip }}</span>
							<span class="ml-2 mr-2">· {{ item.size }} ·</span>
							<span class="ml-2 mr-2">上传于{{ item.createTime }} ·</span>
							<span class="ml-2 mr-2">上传者：{{ item.auth }}</span>
						</p>
					</div>
				</div>
				<div class="flex fa">
					<el-button v-btn class="ml-12" size="small" type="primary" @click="fileListHandle('Dowload', item, index)">
						<span>下载</span>
					</el-button>
					<el-button v-btn class="ml-12" size="small" @click="fileListHandle('Delete', item, index)">
						<span>删除</span>
					</el-button>
				</div>
			</li>
		</ul>
	</ZDialogCom>
</template>

<script setup>
	import ZDialogCom from '@/components/dialog2/index.vue'
	import ZTable from '@/components/table/index.vue'
	import ZPagination from '@/components/pagination/index.vue'
	import { getAssetsFile, transformToTableData } from '@/utils/index.js'
	import { DataNames } from '/public/map.js'

	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()

	const props = defineProps({
		selectedData: {
			type: Object,
			default: () => []
		}
	})

	const tableBox = ref(null)
	const PaginRef = ref(null)
	const addFileDialogRef = ref(null)

	const tableHeight = ref(300)
	const tableData = ref([])
	const tableHeaders = [
		// { prop: 'date', label: '日期', minWidth: 120 },
		{ prop: 'seawallName', label: '名称', minWidth: 120 },
		{ prop: 'forecastStation', label: '监测站名称', minWidth: 130 },
		{ prop: 'lng', label: '经度', minWidth: 120 },
		{ prop: 'lat', label: '纬度', minWidth: 120 },
		{ prop: 'level', label: '等级', minWidth: 120 },
		{ prop: 'probability', label: '概率', minWidth: 120 },
		{ prop: 'seawallArea', label: '类型', minWidth: 120 },
		{ prop: 'seawallCode', label: '编码', minWidth: 120 },
		{ prop: 'seawallCommonName', label: '通用名称', minWidth: 120 },
		{ prop: 'seawallHeight', label: '潮高', minWidth: 120 },
		{ prop: 'seawallType', label: '浪类型', minWidth: 200 },
		{ prop: 'totalWaterHeight', label: '潮高', minWidth: 120 },
		{ prop: 'waterPoint', label: '水位点编号', minWidth: 120 },
		{ prop: 'waveHeight', label: '浪高', minWidth: 120 },
		// { prop: '', slot: 'action', fixed: 'right', label: '操作', width: 120, align: 'center' }
	]

	const mode = ref({
		input1: '',
		input2: '',
		input3: '',
		input4: '',
		input5: '',
		input6: '',
		current: 1,
		size: 20
	})
	const listData = ref([
		{
			title: '监测站编码',
			value: '5105',
			key: 'STATIONCODE'
		},
		{
			title: '监测站名称',
			value: '长乐潭头潮位站',
			key: 'STATIONNAME'
		},
		{
			title: '观测年',
			value: '2023',
			key: 'OBSERVEYEAR'
		},
		{
			title: '观测月',
			value: '08',
			key: 'OBSERVEMONTH'
		},
		{
			title: '观测日',
			value: '12',
			key: 'OBSERVEDAY'
		},
		{
			title: '观测时',
			value: '14',
			key: 'OBSERVEHOUR'
		},
		{
			title: '观测分',
			value: '41',
			key: 'OBSERVEMINUTE'
		},
		{
			title: '观测秒',
			value: '28',
			key: 'OBSERVESECOND'
		},
		{
			title: '潮高(m)',
			value: '1.53',
			key: 'TIDALHEIGHT'
		},
		{
			title: '说明',
			value: '',
			key: 'DESCRIPTION'
		},
		{
			title: '观测时间',
			value: '2023-08-12 14:41:28',
			key: 'OBSERVETIME'
		}
	])
	const fileList = ref([
		{
			type: 'pdf',
			tip: 'PDF',
			name: '建设标准文档.pdf',
			size: '2.34MB',
			createTime: '2020-01-01',
			auth: '张三'
		},
		{
			type: 'jpg',
			tip: 'IMAGE',
			name: '现场照片.jpg',
			size: '2.34MB',
			createTime: '2020-01-01',
			auth: '张三'
		},
		{
			type: 'doc',
			tip: 'DOC',
			name: '竣工验收文档.docx',
			size: '2.34MB',
			createTime: '2020-01-01',
			auth: '张三'
		}
	])
	// 删除
	const fileListHandle = (type, item, index) => {
		if (type === 'Dowload') {
		}
		if (type === 'Delete') {
			fileList.value.splice(index, 1)
		}
	}

	watch(
		() => props.selectedData,
		v => {
			if (v) {
				nextTick(async () => {
					await getDataByName(props.selectedData)
				})
			}
		},
		{
			immediate: true
		}
	)

	onMounted(() => {
		tableHeight.value = tableBox.value?.clientHeight
	})
	const emits = defineEmits(['close'])

	// 初始化数据
	const getDataByName = val => {
		// 获取受灾数据信息
		let data = fetch(DataNames['风暴潮岸段分析'])
			.then(res => res.json())
			.then(res => {
				let item = res.find(n => n.name == val)
				if (item?.data?.dangerousData) {
					for (let [key, val] of Object.entries(item.data?.dangerousData)) {
						let vals = val.map(m => {
							return Object.assign(m, { level: {
									LevelThree: 'Ⅲ级',
									LevelTwo: 'Ⅱ级',
									LevelFour: 'Ⅳ级',
									LevelOne: 'Ⅰ级'
								}[key] })
						})
						tableData.value = tableData.value.concat(vals)
					}
				}
			})
			.catch(error => {
				console.log(error)
			})
	}

	// 重置
	const resetHandle = () => {
		mode.value = {
			input1: '',
			input2: '',
			input3: '',
			input4: '',
			input5: '',
			input6: '',
			current: 1,
			size: 20
		}
	}
	// 查询
	const searchHandle = () => {}
	// 关闭
	const close = () => {
		emits('close')
	}
	// 详情
	const getDetail = row => {
		addFileDialogRef.value.show()
	}
	// 删除
	const delHandle = row => {
		ElMessageBox.confirm('确定删除该数据吗?', '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {})
			.catch(() => {})
	}
	// 分页事件
	const sizeChange = val => {
		mode.value.size = val
		// getPageData()
	}
	const currentChange = val => {
		mode.value.current = val
		// getPageData()
	}
	// 分页器
	const paginationIdxVal = index => {
		return mode.value.current > 1 ? index + 1 + mode.value.size * (mode.value.current - 1) : index + 1
	}
</script>

<style lang="scss" scoped>
	.tableLists {
		position: absolute;
		top: 140px;
		left: 266px;
		z-index: 3001;
		width: calc(100% - 322px);
		height: calc(100% - 196px);
		background-color: #fff;
		&.isCollapse {
			left: 80px;
			width: calc(100% - 144px);
		}
		&__header {
			height: 64px;
			background-color: #fff;
		}
		&__content {
			border-radius: 8px;
			height: calc(100% - 80px);
			background-color: #fff;
		}
	}
	.fileList {
		& li {
			padding: 16px 0;
			&:not(:last-child) {
				border-bottom: 1px solid #dbdbdb;
			}
			& .name {
				font-family: 'Microsoft YaHei';
				font-size: 14px;
				font-weight: bold;
				color: #303133;
			}
			& .tip {
				display: inline-block;
				font-family: 'Microsoft YaHei';
				font-size: 12px;
				padding: 2px 4px;
				min-width: 44px;
				text-align: center;
				background-color: #ebeef5;
				&.PDF {
					color: #f05542;
				}
				&.IMAGE {
					color: #1890ff;
				}
				&.DOC {
					color: #1890ff;
				}
			}
			& .info {
				font-family: 'Microsoft YaHei';
				font-size: 12px;
				color: #606266;
			}
		}
	}
</style>
