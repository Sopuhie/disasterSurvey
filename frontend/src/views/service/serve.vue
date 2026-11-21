<!-- 服务管理 -->
<template>
	<div class="serviceServe max-wh">
		<FilterContainer>
			<template #left>
				<el-input
					class="mr-12"
					v-model="mode.like_serviceName"
					placeholder="请输入资源名称"
					clearable
					style="width: 200px"></el-input>
				<el-button type="primary" plain @click="searchHandle">
					<el-icon class="el-icon--left"><Search /></el-icon>搜索
				</el-button>
				<el-button type="primary" plain @click="addHandle('add')">
					<el-icon class="el-icon--left"><Plus /></el-icon>添加资源
				</el-button>
				<!-- <el-button type="primary" plain @click="addHandle('service')">
					<el-icon class="el-icon--left"><Plus /></el-icon>发布服务
				</el-button> -->
			</template>
		</FilterContainer>
		<div class="listsBox pl-10 pr-10 pt-10 mt-10" v-loading="loading">
			<ul class="lists flex fw" v-if="listData?.length">
				<li class="item" v-for="(item, index) in listData" :key="'lists_' + index">
					<div class="itemTitle flex fa jsb">
						<span class="itemTitleName">{{ item.serviceName }}</span>
						<p class="flex fnw">
							<!-- <el-button type="primary" link @click="previewHandle(item)">预览</el-button> -->
							<el-button type="primary" link @click="metadataHandle(item)">查看元数据</el-button>
						</p>
					</div>
					<div class="itemCon">
						<div class="flex">
							<img src="@/assets/imgs/map.png" alt="" />
							<div class="itemConList">
								<p class="flex fa fnw">
									<span class="itemConLabel">发布机构：</span>
									<span>{{ item.unit }}</span>
								</p>
								<p class="flex fa fnw">
									<span class="itemConLabel">发布人：</span>
									<span>{{ item.cjr }}</span>
								</p>
							</div>
						</div>
						<div class="flex mt6">
							<el-input
								v-model="item.description"
								type="textarea"
								:rows="2"
								resize="none"
								readonly
								placeholder="描述说明"></el-input>
						</div>
						<div class="btns flex fa jsb">
							<span></span>
							<p>
								<el-button type="primary" link @click="handleEdit(item)">编辑</el-button>
								<el-button type="danger" link @click="handleDel(item)">删除</el-button>
							</p>
						</div>
					</div>
				</li>
			</ul>
			<div class="lists" v-else>
				<ZEmpty empty-text="暂无数据"></ZEmpty>
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
	<!-- 预览 -->
	<ZDialog
		v-if="showPreviewDialog"
		:wrapStyle="previewWrapStyle"
		:title="state.itemData?.serviceName"
		:cancelBtn="false"
		:okBtn="false"
		toPath="cancel"
		@cancel="showPreviewDialog = false">
		<template #headerC>
			<p class="dialog_title_c flex fa">
				<span style="color: white">转发地址：</span>
				<el-input v-model="state.itemData.serviceUrl" disabled style="width: 500px" />
			</p>
		</template>
		<div class="max-wh p-16">
			<ZEmpty empty-text="暂不支持预览"></ZEmpty>
		</div>
	</ZDialog>
	<!-- 资源元数据 -->
	<ZDialog
		v-if="showMetadataDialog"
		:wrapStyle="metadataWrapStyle"
		title="资源元数据"
		:footer="false"
		toPath="cancel"
		@cancel="showMetadataDialog = false">
		<div class="max-wh p-16">
			<DetailCom :data="state.itemData"></DetailCom>
		</div>
	</ZDialog>
	<!-- 添加资源 -->
	<ZDialog
		v-if="showAddDialog"
		:wrapStyle="addWrapStyle"
		:title="title"
		toPath="cancel"
		@cancel="showAddDialog = false"
		@handleOk="addSave">
		<div class="max-wh p-16">
			<AddCom v-if="addType !== 'service'" ref="addRef" :title="title" :itemData="rowData"></AddCom>
			<AddServiceCom
				v-if="addType === 'service'"
				ref="addRef"
				:itemData="rowData"
				@close="closeDialog"></AddServiceCom>
		</div>
	</ZDialog>
</template>

<script setup>
	import { getTimeFormatted } from '@/utils/index.js'
	import { setFormData } from '@/utils/formValidation.js'
	import FilterContainer from '@/components/filterContainer/index.vue'
	import ZPagination from '@/components/pagination/index.vue'
	import ZDialog from '@/components/dialog/index.vue'
	import ZEmpty from '@/components/empty/index.vue'
	import AddCom from '@/views/service/components/serve/add.vue'
	import AddServiceCom from '@/views/service/components/serve/addService.vue'
	import DetailCom from '@/views/service/components/serve/details.vue'

	import { serviceApi } from '@/api/service/index.js'
	const api = serviceApi()

	const showPreviewDialog = ref(false)
	const showMetadataDialog = ref(false)
	const showAddDialog = ref(false)
	const PaginRef = ref(null)
	const addRef = ref(null)
	const loading = ref(false)
	const total = ref(0)
	const listData = ref([])
	const rowData = ref(null)
	const title = ref('添加资源')
	const addType = ref('add')
	const previewWrapStyle = ref({
		width: '960px',
		height: '500px',
		top: '50%',
		left: '50%',
		transform: 'translate(-50%, -50%)'
	})
	const metadataWrapStyle = ref({
		width: '960px',
		height: '500px',
		top: '50%',
		left: '50%',
		transform: 'translate(-50%, -50%)'
	})
	const addWrapStyle = computed(() => {
		return {
			width: addType.value === 'service' ? '70%' : '680px',
			height: addType.value === 'service' ? '760px' : '500px',
			top: addType.value === 'service' ? '56%' : '50%',
			left: '50%',
			transform: 'translate(-50%, -50%)'
		}
	})

	const mode = ref({
		current: 1,
		size: 20,
		eq_project: '',
		eq_serviceType: '',
		like_serviceName: '',
		like_unit: ''
	})
	const state = reactive({
		pageSize: 20,
		currentPage: 1,
		itemData: null
	})

	onMounted(() => {
		getPageData()
	})

	// 获取分页数据
	const getPageData = () => {
		loading.value = true
		api
			.serviceQueryWithPage(mode.value)
			.then(res => {
				setTimeout(() => {
					loading.value = false
					listData.value = res.data || []
					total.value = res.pageInfo.recordCount
				}, 300)
			})
			.catch(() => {
				loading.value = false
			})
	}
	// 搜索
	const searchHandle = () => {
		getPageData()
	}
	// 添加
	const addHandle = type => {
		addType.value = type
		rowData.value = null
		title.value = type === 'add' ? '添加资源' : '发布服务'
		showAddDialog.value = true
	}

	// 添加资源保存
	const addSave = () => {
		let form = { ...addRef.value.addFormMode }
		form.createtime = getTimeFormatted()

		if (title.value === '添加资源') {
			form.serviceType = form.serviceType[1]
			form = setFormData(form)
			api
				.serviceAdd(form)
				.then(res => {
					showAddDialog.value = false
					getPageData()
					ElMessage({
						type: 'success',
						message: res.resultMsg
					})
				})
				.catch(err => {
					ElMessage.error(err)
				})
		} else if (title.value === '发布服务') {
			addRef.value.addShp()
		} else {
			form = setFormData(form)
			form.id = rowData.value.id
			api
				.serviceUpdate(form)
				.then(res => {
					showAddDialog.value = false
					getPageData()
					ElMessage({
						type: 'success',
						message: res.resultMsg
					})
				})
				.catch(err => {
					ElMessage.error(err)
				})
		}
	}
	const closeDialog = () => {
		showAddDialog.value = false
		getPageData()
	}
	// 预览
	const previewHandle = item => {
		state.itemData = item
		showPreviewDialog.value = true
	}
	// 查看元数据
	const metadataHandle = item => {
		state.itemData = item
		showMetadataDialog.value = true
	}
	// 编辑
	const handleEdit = item => {
		addType.value = 'edit'
		title.value = '修改资源'
		rowData.value = item
		showAddDialog.value = true
	}
	// 删除
	const handleDel = item => {
		ElMessageBox.confirm('确定删除该数据吗？', '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				api
					.serviceDelete({ serviceId: item.id })
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

	// Dialog提交事件
	const save = flag => {
		console.log('submitDialog', flag)
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
</script>

<style lang="scss" scoped>
	.serviceServe {
		overflow: hidden;
		& .listsBox {
			height: calc(100% - 84px);
			border-radius: 8px;
			background-color: #fff;
			margin-top: 16px;
			padding: 16px 16px 0;
		}
		& .lists {
			min-width: 680px;
			height: calc(100% - 60px);
			overflow: hidden;
			overflow-y: auto;
			padding-right: 4px;
		}
		& .lists::-webkit-scrollbar {
			width: 8px;
			height: 8px;
		}
		& .lists::-webkit-scrollbar-thumb {
			background: #c7ccd6;
			border-radius: 4px;
		}
		& .lists::-webkit-scrollbar-track {
			background: transparent;
		}

		& li.item {
			min-width: 216px;
			width: calc((100% - 24px) / 3);
			height: 234px;
			margin-right: 12px;
			margin-bottom: 12px;
			border-radius: 4px;
			&:nth-child(3n) {
				margin-right: 0;
			}
			& .itemTitle {
				height: 40px;
				padding: 0 6px;
				font-size: 14px;
				& .itemTitleName {
					min-width: 80px;
					overflow: hidden;
					white-space: nowrap;
					text-overflow: ellipsis;
				}
			}
			& .itemCon {
				padding: 6px 6px 0;
				& img {
					width: 170px;
					margin-right: 10px;
					margin-bottom: 6px;
					object-fit: fill;
					vertical-align: top;
				}
				& .itemConList {
					font-size: 14px;
					margin-bottom: 6px;
					min-width: 130px;
					width: calc(100% - 180px);
					& span {
						display: inline-block;
						white-space: nowrap;
					}
					& p {
						height: 22px;
						line-height: 22px;
					}
					& .itemConLabel {
						text-align-last: justify;
						width: 70px;
						overflow: hidden;
					}
				}
				& .btns {
					height: 36px;
				}
			}
		}
	}
	.footer {
		height: 60px;
		box-shadow: inset 0 1px 0 0 #e8e8e8;
		opacity: 1;
		transition: opacity 0.5 ease-out;
	}
	.dialog_title_c {
		white-space: nowrap;
		width: 100%;
		overflow: hidden;
	}
	:deep() {
		.el-collapse,
		.el-collapse-item__wrap {
			border-bottom: 0px solid var(--el-collapse-border-color) !important;
		}
		.el-collapse .el-collapse-item__header {
			background-color: #2260ff;
			color: white;
			padding-left: 16px;
			border-radius: 4px;
			height: 44px !important;
		}
		.el-collapse.collapseWrap .el-collapse-item__header {
			background-color: #ffffff;
			color: #606266;
			border: 1px solid var(--el-border-color);
		}
		.el-collapse.collapseWrap .el-collapse-item__header {
			height: 32px !important;
		}
		.el-collapse .el-collapse-item__content {
			padding: 12px 12px 24px;
		}
		.el-collapse.collapseWrap .el-collapse-item__content {
			padding: 12px 12px 24px 0;
		}
		.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content {
			background: #409eff;
			color: #ffffff;
		}
		.el-tree--highlight-current
			.el-tree-node.is-current
			> .el-tree-node__content
			.el-tree-node__expand-icon {
			color: #ffffff !important;
		}
		.lists .item {
			color: var(--el-text-color-primary);
			border: 1px solid var(--el-border-color);
		}
		.lists .itemTitle {
			border-bottom: 1px solid var(--el-border-color);
		}
	}
</style>
