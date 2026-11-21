<!-- 运维配置 -->
<template>
	<div class="systemLog max-wh">
		<div class="systemLog__header flex fa pl-10 pr-10">
			<el-input
				class="mr-10"
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
		</div>
		<div class="systemLog__content mt-10 p-10">
			<div class="tableBox" ref="tableBox" style="height: calc(100% - 52px)">
				<ZTable ref="refTable" :tableData="routerData" :tableHeaders="routerHeaders" :tableHeight="tableHeight">
					<template #idx="{ scope }">
						{{ paginationIdxVal(scope.$index) }}
					</template>
					<template #noCache="{ scope }">
						<el-button :type="scope.row.noCache === 1 ? 'success' : 'danger'" link>
							{{ scope.row.noCache === 1 ? '是' : '否' }}
						</el-button>
					</template>
					<template #hidden="{ scope }">
						<el-button :type="scope.row.hidden === 1 ? 'success' : 'danger'" link>
							{{ scope.row.hidden === 1 ? '是' : '否' }}
						</el-button>
					</template>
					<template #alwaysShow="{ scope }">
						<el-button :type="scope.row.alwaysShow === 1 ? 'success' : 'danger'" link>
							{{ scope.row.alwaysShow === 1 ? '是' : '否' }}
						</el-button>
					</template>
					<template #action="{ scope }">
						<el-button type="primary" link @click="editHandle(scope.row)">编辑</el-button>
						<el-button type="danger" :disabled="scope.row.path === '/home'" link @click="delHandle(scope.row)">
							<span>删除</span>
						</el-button>
					</template>
				</ZTable>
			</div>
			<div class="footer flex faj">
				<ZPagination
					ref="PaginRef"
					:disabled="loading"
					:total="total"
					:pageSize="mode.size"
					:currentPage="mode.current"
					@sizeChange="sizeChange"
					@currentChange="currentChange"></ZPagination>
			</div>
		</div>
	</div>
	<!-- 添加/编辑 -->
	<ZDialog
		v-if="showDialog"
		:wrapStyle="wrapStyle"
		:bodyStyle="{ padding: '16px' }"
		toPath="cancel"
		alignCenter
		:title="title"
		@cancel="cancelHandler"
		@handleOk="aeSave">
		<AddOrEdit ref="addOrEditRef" :itemData="rowData" :routerData="routerData" :type="typeHandle"></AddOrEdit>
	</ZDialog>
</template>

<script setup>
	import ZTable from '@/components/table/index.vue'
	import ZPagination from '@/components/pagination/index.vue'
	import ZDialog from '@/components/dialog/index.vue'
	import AddOrEdit from '@/views/management/ocCom/addOrEdit.vue'
	import { routerHeaders } from '@/views/management/headers.js'
	import { useAppStore } from '@/store/modules/app.js'
	const store = useAppStore()

	import { systemApi } from '@/api/system/index.js'
	const api = systemApi()

	const wrapStyle = ref({
		width: '768px',
		height: '650px',
		top: '50%',
		left: '50%',
		transform: 'translate(-50%, -50%)'
	})

	const tableBox = ref(null)
	const refTable = ref(null)
	const PaginRef = ref(null)
	const loading = ref(false)
	const showDialog = ref(false)
	const tableHeight = ref(300)
	const rowData = ref(null)
	const total = ref(0)
	const addOrEditRef = ref(null)
	const title = ref('添加配置')
	const typeHandle = ref(true)

	const routerData = ref([])
	const mode = ref({
		adminFlag: '',
		loginUserId: '',
		current: 1,
		size: 20,
		like_name: '',
		like_title: ''
	})

	onMounted(() => {
		getPageData()
		tableHeight.value = tableBox.value.clientHeight
	})

	const cancelHandler = () => {
		showDialog.value = false
	}

	// 添加
	const addHandle = () => {
		title.value = '添加配置'
		rowData.value = null
		typeHandle.value = true
		showDialog.value = true
	}
	// 编辑
	const editHandle = row => {
		title.value = '修改配置'
		rowData.value = row
		typeHandle.value = false
		showDialog.value = true
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
					.routerDelete({ routerId: row.routerId })
					.then(res => {
						getPageData()
						getRouterTree()
						ElMessage({
							type: 'success',
							message: '删除成功'
						})
					})
					.catch(err => {})
			})
			.catch(() => {})
	}
	// 添加/编辑保存
	const aeSave = () => {
		if (typeHandle.value) {
			api
				.routerAdd(addOrEditRef.value.form)
				.then(res => {
					showDialog.value = false
					getPageData()
					getRouterTree()
					ElMessage({ message: '添加成功', type: 'success' })
				})
				.catch(err => {})
		} else {
			api
				.routerUpdate(addOrEditRef.value.form)
				.then(res => {
					showDialog.value = false
					getPageData()
					getRouterTree()
					ElMessage({ message: '更新成功', type: 'success' })
				})
				.catch(err => {})
		}
	}
	// 获取路由数据
	const getPageData = () => {
		loading.value = true
		api
			.routerQueryWithPage(mode.value)
			.then(res => {
				setTimeout(() => {
					loading.value = false
					const data = res.data || []
					routerData.value = data
					const size = mode.value.size
					const current = mode.value.current
					// 当前页不满一页时，说明是最后一页，用实际条数计算 total，避免分页多出空页
					if (data.length < size) {
						total.value = (current - 1) * size + data.length
					} else {
						total.value = res.pageInfo?.recordCount ?? 0
					}
					// 若当前页超出实际最大页，重置到第 1 页并重新拉取
					const maxPage = Math.max(1, Math.ceil(total.value / size))
					if (current > maxPage) {
						mode.value.current = 1
						getPageData()
					}
				}, 300)
			})
			.catch(err => {
				loading.value = false
			})
	}
	// 搜索
	const searchHandle = () => {
		getPageData()
	}
	// 路由更新时调用（更新菜单）
	const getRouterTree = () => {
		api
			.routerTree()
			.then(res => {
				if (res?.data?.length > 0) {
					store.setRouterData(res.data)
				} else {
					localStorage.removeItem('routerData')
				}
			})
			.catch(err => {})
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
</script>

<style lang="scss" scoped>
	.systemLog {
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
	.footer {
		height: 60px;
		box-shadow: inset 0 1px 0 0 #e8e8e8;
		opacity: 1;
		transition: opacity 0.5 ease-out;
		&.cur {
			opacity: 0;
		}
	}
</style>
