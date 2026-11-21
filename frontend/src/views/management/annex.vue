<!-- 附件管理 -->
<template>
	<div class="serviceServe max-wh flex">
		<div class="aside p-12">
			<MenuTree :treeData="treeData" @nodeClick="nodeClick">
				<template #default="{ data: { node, data } }">
					<span class="custom-tree-node max-w flex fa jsb">
						<span
							>{{ node.label }}
							<span v-if="data?.statisticResult">({{ data.statisticResult }})</span>
						</span>
					</span>
				</template>
			</MenuTree>
		</div>
		<div class="wrap pl-12">
			<FilterContainer>
				<template #left>
					<el-input
						class="mr-12"
						v-model="mode.like_name"
						placeholder="请输入资源名称"
						clearable
						style="width: 200px"></el-input>
					<el-button type="primary" plain @click="searchHandle">
						<el-icon class="el-icon--left"><Search /></el-icon>搜索
					</el-button>
					<el-button type="primary" plain @click="addFileHandle">
						<el-icon class="el-icon--left"><Plus /></el-icon>添加文件资源
					</el-button>
				</template>
			</FilterContainer>
			<PageContainer class="mt-10 annex-page" :loading="loading" height="0">
				<div class="tableBox" ref="tableBox">
					<ZTable ref="refTable" :tableData="tableData" :tableHeaders="annexHeaders" :tableHeight="tableHeight">
						<template #idx="{ scope }">
							{{ paginationIdxVal(scope.$index) }}
						</template>
						<template #status="{ scope }">
							{{ scope.row.status === '0' ? '开放' : '不开放' }}
						</template>
						<template #level="{ scope }">
							{{ getLevel(scope.row) }}
						</template>
						<template #action="{ scope }">
							<el-button type="primary" link @click="getDetail(scope.row)">详情</el-button>
							<el-button type="primary" link @click="editHandle(scope.row)">编辑</el-button>
							<el-button type="danger" link @click="delHandle(scope.row)">删除</el-button>
						</template>
					</ZTable>
				</div>
				<ZPagination
					ref="PaginRef"
					:disabled="loading"
					:total="total"
					:pageSize="mode.size"
					:currentPage="mode.current"
					@sizeChange="sizeChange"
					@currentChange="currentChange"></ZPagination>
			</PageContainer>
		</div>
	</div>
	<!-- 添加文件资源 -->
	<ZDialog
		ref="addFileDialogRef"
		width="70%"
		height="1130"
		alignCenter
		:title="title"
		cancelText="返回"
		submitText="确认添加"
		@save="addFileSave">
		<div class="maxwh">
			<AddFileCom ref="addFileRef" :type="type" :itemData="rowData"></AddFileCom>
		</div>
	</ZDialog>
	<!-- 详情 -->
	<ZDialog ref="fPreviewRef" title="详情" width="80%" height="666" alignCenter :showOkBtn="false">
		<FilerPreview :fileType="fileType" :fileList="fileList" :fileBaseUrl="fileBaseUrl"></FilerPreview>
	</ZDialog>
</template>

<script setup>
	import FilterContainer from '@/components/filterContainer/index.vue'
	import FilerPreview from '@/components/filerPreview/index.vue'
	import PageContainer from '@/components/pageContainer/index.vue'
	import ZTable from '@/components/table/index.vue'
	import ZPagination from '@/components/pagination/index.vue'
	import ZDialog from '@/components/dialog2/index.vue'
	import MenuTree from '@/views/management/annex/menuTree.vue'
	import AddFileCom from '@/views/management/annex/addFile.vue'
	import { annexHeaders } from '@/views/management/headers.js'
	import { setTreeProps } from '@/utils/index.js'
	import { baseURL } from '@/config'

	import { serviceApi } from '@/api/service/index.js'
	const api = serviceApi()

	import { systemApi } from '@/api/system/index.js'
	const sApi = systemApi()

	const levelOptions = ref([])
	const tableBox = ref(null)
	const loading = ref(false)
	const tableHeight = ref(300)
	const tableData = ref([])
	const treeData = ref([])
	const addFileDialogRef = ref(null)
	const addFileRef = ref(null)
	const rowData = ref(null)
	const title = ref('文件集资源注册')
	const fPreviewRef = ref(null)
	const fileType = ref('excel')
	const fileBaseUrl = ref(`${baseURL}web-disaster-common/web/disaster/common/openfile/`)
	const fileList = ref([])
	const fileDetails = ref([])
	const total = ref(0)
	const type = ref('add')

	const mode = ref({
		adminFlag: '',
		current: 1,
		size: 20,
		like_name: '',
		eq_type: ''
	})

	const syncTableHeight = () => {
		const h = tableBox.value?.clientHeight
		if (h && h > 0) {
			tableHeight.value = h
		}
	}

	let tableResizeObs = null
	onMounted(() => {
		syncTableHeight()
		nextTick(syncTableHeight)
		// drawer 动画结束后再量一次，避免高度为 0 导致表格空白
		setTimeout(syncTableHeight, 320)
		if (typeof ResizeObserver !== 'undefined' && tableBox.value) {
			tableResizeObs = new ResizeObserver(() => syncTableHeight())
			tableResizeObs.observe(tableBox.value)
		}
		getDict()
		getPageData()
		getLeftTab()
	})
	onUnmounted(() => {
		tableResizeObs?.disconnect()
		tableResizeObs = null
	})

	// 获取分页数据
	const getPageData = () => {
		loading.value = true

		api
			.annexFileQueryWithPage(mode.value)
			.then(res => {
				setTimeout(() => {
					loading.value = false
					const data = res.data || []
					tableData.value = data
					const size = mode.value.size
					const current = mode.value.current
					const recordCount = res.pageInfo?.recordCount
					// 接口返回的 recordCount 可能为 0 或缺失，用当前页数据校正 total
					if (data.length < size) {
						total.value = (current - 1) * size + data.length
					} else {
						total.value = recordCount != null ? recordCount : (current - 1) * size + data.length
					}
				}, 300)
			})
			.catch(err => {
				loading.value = false
			})
	}
	const getLeftTab = () => {
		const form = {
			actionName: '',
			dictType: 'zygl_wjzylx',
			groupColumn: 'TYPE',
			resourceType: 'file'
		}
		api
			.lefttabQuery(form)
			.then(res => {
				const data = res.data || []
				treeData.value = setTreeProps(data, 'nodeName', 'nodeId')
			})
			.catch(err => {
				ElMessage.error(err.msg)
			})
	}
	// 树节点点击事件
	const nodeClick = data => {
		mode.value.eq_type = data.nodeCode
		getPageData()
	}
	// 获取字典--文件等级
	const getDict = () => {
		sApi
			.dictTypeQuery({ like_zdlxbm: 'zygl_filelevel' })
			.then(res => {
				if (res.data?.length > 0) {
					const data = res.data[0]
					sApi
						.dictDetailQuery({ flid: data.id })
						.then(res => {
							if (res.data?.length > 0) {
								levelOptions.value = res.data
							}
						})
						.catch(err => {})
				}
			})
			.catch(err => {})
	}
	const getDetail = row => {
		api
			.annexFileGetById({ fileResourceId: row.id })
			.then(res => {
				if (res.data) {
					const details = res.data.details
					fileList.value = details
					fPreviewRef.value.show()
				}
			})
			.catch(err => {
				ElMessage.error(err.msg)
			})
	}
	// 搜索
	const searchHandle = () => {
		getPageData()
	}
	// 添加文件资源
	const addFileHandle = () => {
		rowData.value = null
		title.value = '文件集资源注册'
		type.value = 'add'
		addFileDialogRef.value.show()
	}
	// 添加文件资源保存
	const addFileSave = () => {
		const form = { ...addFileRef.value.form }
		if (title.value === '文件集资源编辑') {
			form.id = rowData.value.id
			api
				.annexFileUpdate(form)
				.then(res => {
					getPageData()
					ElMessage({ message: '编辑成功', type: 'success' })
					addFileDialogRef.value.close()
				})
				.catch(err => {})
		} else if (form.details?.length > 0) {
			fileDetails.value = form.details
			api
				.annexFileAdd(form)
				.then(res => {
					getPageData()
					ElMessage({ message: '添加文件资源成功', type: 'success' })
					addFileDialogRef.value.close()
				})
				.catch(err => {})
		} else {
			ElMessage.error('请上传文件')
			return
		}
	}
	// 编辑
	const editHandle = row => {
		title.value = '文件集资源编辑'
		rowData.value = row
		type.value = 'edit'
		addFileDialogRef.value.show()
	}
	// 删除
	const delHandle = row => {
		ElMessageBox.confirm('确定删除该数据吗?', '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				api
					.annexFileDelete({ fileResourceId: row.id })
					.then(res => {
						getPageData()
						ElMessage({
							type: 'success',
							message: res.resultMsg
						})
					})
					.catch(err => {})
			})
			.catch(() => {})
	}
	// 获取文件等级
	const getLevel = row => {
		const level = levelOptions.value.find(item => item.zdbm == row.level)
		return level?.zdz || ''
	}
	// 分页事件
	const sizeChange = val => {
		mode.value.size = val
		getPageData()
	}
	const currentChange = val => {
		mode.value.current = val
		getPageData()
	}
	// 分页器
	const paginationIdxVal = index => {
		return mode.value.current > 1 ? index + 1 + mode.value.size * (mode.value.current - 1) : index + 1
	}

	defineExpose({ syncTableHeight })
</script>

<style lang="scss" scoped>
	.serviceServe {
		width: 100%;
		height: 100%;
		min-height: 0;
		& .aside {
			width: 260px;
			height: 100%;
			overflow: auto;
			flex-shrink: 0;
		}
		& .wrap {
			display: flex;
			flex-direction: column;
			width: calc(100% - 260px);
			height: 100%;
			min-width: 0;
			min-height: 0;
			overflow: hidden;
		}
		& .annex-page {
			flex: 1;
			min-height: 0;
			display: flex;
			flex-direction: column;
			height: 100% !important;
		}
		& .tableBox {
			flex: 1;
			min-height: 0;
			height: calc(100% - 52px);
			overflow: hidden;
		}
	}
	:deep() {
		.aside {
			background-color: var(--el-bg-color);
		}
	}
</style>
