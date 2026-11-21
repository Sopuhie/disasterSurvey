<!-- 系统配置 -->
<template>
	<FilterContainer>
		<template #left>
			<el-input
				class="mr-12"
				v-model="mode.like_title"
				placeholder="系统标题"
				clearable
				style="width: 200px"
				@keyup.enter.native="searchHandle"></el-input>
			<el-button type="primary" plain @click="searchHandle">
				<el-icon class="el-icon--left"><Search /></el-icon>搜索
			</el-button>
			<el-button type="primary" plain @click="addHandle">
				<el-icon class="el-icon--left"><Plus /></el-icon>添加
			</el-button>
		</template>
	</FilterContainer>
	<PageContainer class="mt-10" :loading="loading">
		<div class="tableBox" ref="tableBox" style="height: calc(100% - 52px)">
			<ZTable ref="refTable" :tableData="tableData" :tableHeaders="configHeaders" :tableHeight="tableHeight">
				<template #idx="{ scope }">
					{{ paginationIdxVal(scope.$index) }}
				</template>
				<template #action="{ scope }">
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

	<ZDialog
		v-if="showAeDialog"
		:wrapStyle="aeWrapStyle"
		:title="title"
		toPath="cancel"
		@cancel="showAeDialog = false"
		@handleOk="aeSave">
		<div class="max-wh p-16 pt-4">
			<AddOrEdit ref="addOrEditRef" :itemData="rowData" :type="typeHandle"></AddOrEdit>
		</div>
	</ZDialog>
	<ZDialog
		v-if="showCreateDialog"
		:wrapStyle="createWrapStyle"
		:title="title"
		toPath="cancel"
		@cancel="showCreateDialog = false"
		@handleOk="save">
		<div class="max-wh p-16 pt-4">
			<Create ref="createRef"></Create>
		</div>
	</ZDialog>
</template>

<script setup>
	import FilterContainer from '@/components/filterContainer/index.vue'
	import ZTable from '@/components/table/index.vue'
	import PageContainer from '@/components/pageContainer/index.vue'
	import ZPagination from '@/components/pagination/index.vue'
	import ZDialog from '@/components/dialog/index.vue'
	import AddOrEdit from '@/views/management/configCom/addOrEdit.vue'
	import Create from '@/views/management/configCom/create.vue'
	import { configHeaders } from '@/views/management/headers.js'
	import { systemApi } from '@/api/system/index.js'
	const api = systemApi()

	const tableBox = ref(null)
	const refTable = ref(null)
	const PaginRef = ref(null)
	const loading = ref(false)
	const tableHeight = ref(300)
	const tableData = ref([])
	const rowData = ref(null)
	const total = ref(0)
	const addOrEditRef = ref(null)
	const showAeDialog = ref(false)
	const showCreateDialog = ref(false)
	const createRef = ref(null)
	const title = ref('添加配置')
	const typeHandle = ref(true)
	const aeWrapStyle = ref({
		width: '768px',
		height: '515px',
		top: '50%',
		left: '50%',
		transform: 'translate(-50%, -50%)'
	})
	const createWrapStyle = ref({
		width: '768px',
		height: '515px',
		top: '50%',
		left: '50%',
		transform: 'translate(-50%, -50%)'
	})

	const mode = ref({
		adminFlag: '',
		current: 1,
		size: 20,
		like_name: '',
		like_title: '',
		loginUserId: ''
	})

	onMounted(() => {
		getPageData()
		tableHeight.value = tableBox.value.clientHeight
	})

	const getPageData = () => {
		loading.value = true
		api
			.configQueryWithPage(mode.value)
			.then(res => {
				setTimeout(() => {
					loading.value = false
					tableData.value = res.data || []
					total.value = res.pageInfo.recordCount
				}, 300)
			})
			.catch(() => {
				loading.value = false
			})
	}
	const searchHandle = () => {
		getPageData()
	}
	const addHandle = () => {
		title.value = '添加配置'
		rowData.value = null
		typeHandle.value = true
		showAeDialog.value = true
	}
	const editHandle = row => {
		rowData.value = row
		typeHandle.value = false
		title.value = '修改配置'
		showAeDialog.value = true
	}
	const delHandle = row => {
		ElMessageBox.confirm('确定删除该数据吗?', '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				api
					.configDelete({ systemConfigId: row.id })
					.then(() => {
						getPageData()
						ElMessage({
							type: 'success',
							message: '删除成功'
						})
					})
					.catch(() => {})
			})
			.catch(() => {})
	}
	const aeSave = () => {
		const form = addOrEditRef.value.form
		if (title.value === '添加配置') {
			api
				.configAdd(form)
				.then(() => {
					showAeDialog.value = false
					getPageData()
					ElMessage({ message: '添加成功', type: 'success' })
				})
				.catch(() => {})
		} else if (title.value === '修改配置') {
			form.id = rowData.value.id
			api
				.configUpdate(form)
				.then(() => {
					showAeDialog.value = false
					getPageData()
					ElMessage({ message: '添加成功', type: 'success' })
				})
				.catch(() => {})
		}
	}
	const save = () => {
		console.log('创建项目保存')
	}
	const sizeChange = val => {
		mode.value.size = val
		getPageData()
	}
	const currentChange = val => {
		mode.value.current = val
		getPageData()
	}
	const paginationIdxVal = index => {
		return mode.value.current > 1 ? index + 1 + mode.value.size * (mode.value.current - 1) : index + 1
	}
</script>

<style lang="scss" scoped></style>
