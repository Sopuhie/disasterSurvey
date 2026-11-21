<!-- 目录树管理 -->
<template>
	<div class="serviceCatalog max-wh flex">
		<Wrap title="服务资源" v-model="mode.like_serviceName">
			<template #tree v-if="serviceTree.length">
				<Tree
					ref="serviceTreeRef"
					itemName="service"
					:filterText="mode.like_serviceName"
					showDetail
					:data="serviceTree"
					:checkStrictly="true"
					@handleCheck="handleCheck"
					@handleDetails="handleDetails"></Tree>
			</template>
		</Wrap>
		<Btns
			text1="绑定资源到专题"
			text2="从专题中解绑资源"
			:btnObj="state.left"
			@handleBind="handleBind('theme')"
			@handleUnbind="handleUnbind('theme')"></Btns>
		<Wrap title="专题资源" v-model="mode.like_themename" :btnObjs="state.theme">
			<template #title>
				<el-tooltip content="添加专题">
					<el-icon
						class="cp el-iconColor"
						:class="{ ' el-icon--disabled': state.theme.add }"
						@click="handleTheme('add')">
						<FolderAdd />
					</el-icon>
				</el-tooltip>
				<el-tooltip content="编辑专题">
					<el-icon
						class="cp ml-10 el-iconColor"
						:class="{ ' el-icon--disabled': state.theme.edit }"
						@click="handleTheme('edit')">
						<EditPen />
					</el-icon>
				</el-tooltip>
				<el-tooltip content="删除专题">
					<el-icon
						class="cp ml-10 el-iconColor"
						:class="{ ' el-icon--disabled': state.theme.del }"
						@click="handleTheme('del')">
						<Delete />
					</el-icon>
				</el-tooltip>
			</template>
			<template #tree v-if="themeTree.length">
				<Tree
					ref="themeTreeRef"
					itemName="theme"
					:accordion="false"
					:filterText="mode.like_themename"
					:data="themeTree"
					:checkStrictly="true"
					@handleCheck="handleCheck"></Tree>
			</template>
		</Wrap>
		<Btns
			text1="绑定专题到目录"
			text2="从目录中解绑专题"
			:btnObj="state.right"
			@handleBind="handleBind('catalog')"
			@handleUnbind="handleUnbind('catalog')"></Btns>
		<Wrap title="目录资源" v-model="mode.like_name">
			<template #title>
				<el-tooltip content="添加目录">
					<el-icon
						class="cp el-iconColor"
						:class="{ ' el-icon--disabled': state.catalog.add }"
						@click="handleCatalogAdd">
						<FolderAdd />
					</el-icon>
				</el-tooltip>
				<el-tooltip content="编辑目录">
					<el-icon
						class="cp ml-10 el-iconColor"
						:class="{ ' el-icon--disabled': state.catalog.edit }"
						@click="handleCatalogEdit">
						<EditPen />
					</el-icon>
				</el-tooltip>
				<el-tooltip content="删除目录">
					<el-icon
						class="cp ml-10 el-iconColor"
						:class="{ ' el-icon--disabled': state.catalog.del }"
						@click="handleCatalogDel">
						<Delete />
					</el-icon>
				</el-tooltip>
			</template>
			<template #tree v-if="catalogTree.length">
				<Tree
					ref="catalogTreeRef"
					itemName="catalog"
					:filterText="mode.like_name"
					:data="catalogTree"
					:checkStrictly="true"
					@handleCheck="handleCheck"></Tree>
			</template>
		</Wrap>
		<ZDialog
			v-if="showAddThemeDialog"
			:wrapStyle="addThemeWrapStyle"
			:title="addThemeDialogTitle"
			toPath="cancel"
			@cancel="showAddThemeDialog = false"
			@handleOk="addThemeSave">
			<div class="max-wh p-16">
				<el-form :model="addThemeForm" label-width="110px">
					<el-form-item label="节点类型" prop="type">
						<el-select v-model="addThemeForm.type" placeholder="请选择">
							<el-option v-for="item in themeType" :value="item.value" :label="item.label">{{
								item.label
							}}</el-option>
						</el-select>
					</el-form-item>
					<el-form-item
						label="专题目录名称"
						prop="themeName"
						:rules="[
							{
								required: true,
								message: '请输入专题目录名称',
								trigger: 'blur'
							}
						]">
						<el-input v-model="addThemeForm.themeName" clearable></el-input>
					</el-form-item>
					<el-form-item label="排序索引" prop="sortIndex">
						<el-input v-model.number="addThemeForm.sortIndex" clearable></el-input>
					</el-form-item>
				</el-form>
			</div>
		</ZDialog>
		<ZDialog
			v-if="showAddCatalogDialog"
			:wrapStyle="addCatalogWrapStyle"
			:title="catalogDialogTitle"
			toPath="cancel"
			@cancel="handleAddCatalogCancel"
			@handleOk="handleCatalogSave">
			<div class="max-wh p-16">
				<el-form :model="addCatalogForm" label-width="110px">
					<el-form-item
						label="目录名称"
						prop="name"
						:rules="[
							{
								required: true,
								message: '请输入目录名称',
								trigger: 'blur'
							}
						]">
						<el-input v-model="addCatalogForm.name" clearable></el-input>
					</el-form-item>
					<el-form-item label="排序索引" prop="sortIndex">
						<el-input v-model.number="addCatalogForm.sortIndex" clearable></el-input>
					</el-form-item>
				</el-form>
			</div>
		</ZDialog>
		<ZDialog
			v-if="showDetailDialog"
			:wrapStyle="detailWrapStyle"
			title="服务信息"
			:footer="false"
			toPath="cancel"
			@cancel="showDetailDialog = false">
			<div class="max-wh p-16" v-if="previewData">
				<el-form :model="previewData" label-width="110px">
					<el-form-item label="资源名称">
						<el-input v-model="previewData.serviceName"></el-input>
					</el-form-item>
					<el-form-item label="地址">
						<el-input v-model="previewData.serviceUrl"></el-input>
					</el-form-item>
					<el-form-item label="资源截图">
						<el-image
							:src="previewData.screenshot !== '' ? previewData.screenshot : '@/assets/map.png'"
							fit="fill"
							style="width: 300px; height: 120px" />
					</el-form-item>
				</el-form>
			</div>
		</ZDialog>
	</div>
</template>

<script setup>
	import axios from 'axios'
	import ZDialog from '@/components/dialog/index.vue'
	import Wrap from '@/views/service/components/wrap.vue'
	import Tree from '@/views/service/components/tree.vue'
	import Btns from '@/views/service/components/btns.vue'
	import {
		setTreeProps,
		getTimeFormatted
	} from '@/utils/index.js'
	import { _debounce } from '@/utils/index.js'
	import { useAppStore } from '@/store/modules/app.js'
	const store = useAppStore()
	import { serviceApi } from '@/api/service/index.js'
	const sApi = serviceApi()

	const mode = reactive({
		like_serviceName: '',
		like_themename: '',
		like_name: ''
	})
	const state = reactive({
		left: {
			top: true,
			bottom: true
		},
		right: {
			top: true,
			bottom: true
		},
		theme: {
			add: false,
			edit: true,
			del: true
		},
		catalog: {
			add: false,
			edit: true,
			del: true
		}
	})

	const themeType = reactive([
		{ label: '目录', value: 'folder' },
		{ label: '专题', value: 'theme' }
	])

	const serviceTreeRef = ref(null)
	const themeTreeRef = ref(null)
	const catalogTreeRef = ref(null)
	const showDetailDialog = ref(false)
	const showAddThemeDialog = ref(false)
	const showAddCatalogDialog = ref(false)
	const addThemeDialogTitle = ref('新增目录/专题')
	const catalogDialogTitle = ref('添加目录')
	const serviceTree = ref([])
	const themeTree = ref([])
	const catalogTree = ref([])
	const previewData = ref(null)
	const addThemeWrapStyle = ref({
		width: '448px',
	height: '236px',
		top: '50%',
		left: '50%',
		transform: 'translate(-50%, -50%)'
	})
	const addCatalogWrapStyle = ref({
		width: '448px',
	height: '200px',
		top: '50%',
		left: '50%',
		transform: 'translate(-50%, -50%)'
	})
	const detailWrapStyle = ref({
		width: '448px',
		height: '252px',
		top: '50%',
		left: '50%',
		transform: 'translate(-50%, -50%)'
	})

	const addThemeForm = reactive({
		cjr: store.userInfo.username,
		cjsj: getTimeFormatted('YYYY-MM-DD HH:mm:ss'),
		sortIndex: 0,
		type: 'folder',
		parentId: [],
		themeName: ''
	})
	const addCatalogForm = reactive({
		cjr: store.userInfo.username,
		cjsj: getTimeFormatted('YYYY-MM-DD HH:mm:ss'),
		sortIndex: 0,
		type: 'folder',
		parentId: [],
		name: '',
		themeId: ''
	})

	watch(
		() => mode.service,
		_debounce(val => {
			console.log('service:', val)
		}, 500)
	)

	onMounted(() => {
		axios.all([getServiceTree(), getThemeTree(), getCatalogTree()])
	})

	const handleCheck = () => {
		const serviceCheckNode = toRaw(serviceTreeRef.value?.checkNode)
		const themeCheckNode = toRaw(themeTreeRef.value?.checkNode)
		const catalogCheckNode = toRaw(catalogTreeRef.value?.checkNode)
		if (serviceCheckNode && themeCheckNode?.nodeType === 'theme') {
			state.left.top = false
		} else {
			state.left.top = true
		}
		if (themeCheckNode) {
			state.theme.add = false
			state.theme.edit = false
			state.theme.del = false
			if (themeCheckNode?.nodeType === 'folder') {
				state.left.bottom = true
				state.right.top = true
			}
			if (themeCheckNode?.nodeType === 'theme') {
				state.left.top = !serviceCheckNode
				state.right.top = catalogCheckNode?.nodeType !== 'folder'
				state.left.bottom = true
				state.theme.add = true
				state.theme.edit = false
				state.theme.del = false
			}
			if (themeCheckNode?.nodeType === 'service') {
				state.theme.add = true
				state.theme.edit = true
				state.theme.del = true
				state.left.bottom = false
			}
		} else {
			state.theme.add = false
			state.theme.edit = true
			state.theme.del = true
			state.left.bottom = true
			state.right.top = true
		}
		if (catalogCheckNode) {
			state.catalog.edit = false
			state.catalog.del = false
			state.right.bottom = catalogCheckNode?.nodeType === 'theme' ? false : true
			if (catalogCheckNode?.nodeType === 'folder' || catalogCheckNode?.nodeType === undefined) {
				state.catalog.add = false
			} else {
				state.catalog.add = true
			}
		} else {
			state.catalog.add = false
			state.catalog.edit = true
			state.catalog.del = true
			state.right.top = true
			state.right.bottom = true
		}
	}

	async function getServiceTree() {
		const res = await sApi.serviceTree({ like_serviceName: '' })
		if (res?.data) {
			handleCheck()
			serviceTree.value = setTreeDisabled(
				setTreeProps([res.data], 'nodeName', 'nodeId'),
				'nodeType',
				'service'
			)
		}
		return res
	}
	async function getThemeTree() {
		const res = await sApi.themeTree({ like_themename: '' })
		if (res?.data) {
			handleCheck()
			themeTree.value = setTreeProps(res.data, 'nodeName', 'nodeId', 'nodeId')
		}
		return res
	}
	async function getCatalogTree() {
		const res = await sApi.catalogTree({ like_name: '' })
		if (res?.data) {
			handleCheck()
			catalogTree.value = setTreeProps(res.data, 'nodeName', 'nodeId')
		}
		return res
	}

	const handleDetails = data => {
		showDetailDialog.value = true
		const serviceId = data?.nodeId
		sApi
			.serviceGetById({ serviceId })
			.then(res => {
				previewData.value = res.data
			})
			.catch(err => {
				ElMessage.error(err)
			})
	}

	const handleBind = type => {
		switch (type) {
			case 'theme':
				bindTheme()
				break
			case 'catalog':
				bindCatalog()
				break
			default:
				break
		}
	}
	const handleUnbind = type => {
		switch (type) {
			case 'theme':
				unbindTheme()
				break
			case 'catalog':
				unbindCatalog()
				break
			default:
				break
		}
	}
	function bindTheme() {
		const checkNode = serviceTreeRef.value?.checkNode
		const themeCheckNode = themeTreeRef.value?.checkNode
		const themeId = themeCheckNode?.id
		const form = {
			loginUser: store.userInfo.username,
			services: [
				{
					serviceId: checkNode.nodeId,
					serviceName: checkNode.nodeName
				}
			],
			themeId: themeId
		}
		sApi
			.themeBindService(form)
			.then(res => {
				if (res.resultCode === 'S') {
					ElMessage({
						message: res.resultMsg,
						type: 'success'
					})
					themeTreeRef.value?.reset()
					getThemeTree()
				} else {
					ElMessage.error(res.resultMsg)
				}
			})
			.catch(error => {
				ElMessage.error(error)
			})
	}
	function unbindTheme() {
		const checkNode = themeTreeRef.value?.checkNode
		const themeId = checkNode?.id
		ElMessageBox.confirm('你确定解绑该资源吗？', '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				let parentData = getParentDataAll(themeTree.value, themeId)
				if (parentData.length) {
					const flattenedArray = simpleArr(parentData).filter(item => item.nodeType === 'theme')
					const form = {
						loginUser: store.userInfo.username,
						services: [
							{
								serviceId: checkNode.nodeId,
								serviceName: checkNode.nodeName
							}
						],
						themeId: flattenedArray[0]['id']
					}
					sApi.themeUnbindService([form]).then(res => {
						if (res.resultCode === 'S') {
							ElMessage({
								message: res.resultMsg,
								type: 'success'
							})
							themeTreeRef.value?.reset()
							getThemeTree()
						} else {
							ElMessage.error(res.resultMsg)
						}
					})
				}
			})
			.catch(() => {})
	}

	function bindCatalog() {
		catalogDialogTitle.value = '添加目录'
		const checkNode = themeTreeRef.value.checkNode
		const id = catalogTreeRef.value?.checkNode?.id
		addCatalogForm.name = checkNode.nodeName
		addCatalogForm.parentId = id
		addCatalogForm.themeId = checkNode.nodeId
		addCatalogForm.type = checkNode.nodeType
		handleCatalogSave()
	}
	function unbindCatalog() {
		handleCatalogDel('解绑')
	}
	function simpleArr(arr) {
		const res = []
		for (const item of arr) {
			const { children, ...rest } = item
			res.push(rest)
			if (children && children.length) {
				res.push(...simpleArr(children))
			}
		}
		return res
	}
	function setTreeDisabled(data, typeKey, enableType) {
		if (!Array.isArray(data)) return []
		const loop = arr =>
			arr.map(item => {
				const children = Array.isArray(item.children) ? loop(item.children) : []
				return {
					...item,
					disabled: item[typeKey] !== enableType,
					children
				}
			})
		return loop(data)
	}
	function getParentDataAll(tree, targetId) {
		const path = []
		const dfs = nodes => {
			for (const node of nodes || []) {
				path.push(node)
				if (node.id === targetId || node.nodeId === targetId) return true
				if (node.children?.length && dfs(node.children)) return true
				path.pop()
			}
			return false
		}
		return dfs(tree) ? path : []
	}
	const handleTheme = type => {
		switch (type) {
			case 'add':
				if (state.theme.add) return
				addThemeForm.themeName = ''
				addThemeForm.type = 'folder'
				addThemeForm.sortIndex = 0
				addThemeDialogTitle.value = '新增目录/专题'
				showAddThemeDialog.value = true
				break
			case 'edit':
				handleThemeEdit()
				break
			case 'del':
				handleThemeDel()
				break
			default:
				break
		}
	}
	const addThemeSave = () => {
		let form = { ...addThemeForm }
		const checkNode = themeTreeRef.value?.checkNode
		const id = checkNode?.id
		if (addThemeDialogTitle.value === '新增目录/专题') {
			form.parentId = id ? id : ''
			sApi
				.themeAdd(form)
				.then(res => {
					if (res.resultCode === 'S') {
						ElMessage({
							message: res.resultMsg,
							type: 'success'
						})
						getThemeTree()
						addThemeForm.themeName = ''
						addThemeForm.type = 'folder'
						showAddThemeDialog.value = false
					}
				})
				.catch(err => {
					ElMessage.error(err)
				})
		} else {
			form.id = checkNode.nodeId
			form.parentId = ''
			sApi
				.themeUpdate(form)
				.then(res => {
					if (res.resultCode === 'S') {
						ElMessage({
							message: res.resultMsg,
							type: 'success'
						})
						getThemeTree()
						addThemeForm.themeName = ''
						addThemeForm.type = 'folder'
						showAddThemeDialog.value = false
					}
				})
				.catch(err => {
					ElMessage.error(err)
				})
		}
	}
	const handleThemeEdit = () => {
		if (state.theme.edit) return
		addThemeDialogTitle.value = '修改目录/专题'
		showAddThemeDialog.value = true
		const checkNode = themeTreeRef.value.checkNode
		addThemeForm.themeName = checkNode.nodeName
		addThemeForm.type = checkNode.nodeType
		addThemeForm.sortIndex = checkNode.sortIndex
	}
	const handleThemeDel = () => {
		if (state.theme.del) return
		const themeId = themeTreeRef.value.checkNode.id
		ElMessageBox.confirm('确定删除该数据吗?', '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				sApi
					.themeDelete({ themeId })
					.then(() => {
						getThemeTree()
						ElMessage({
							type: 'success',
							message: '删除成功'
						})
					})
					.catch(() => {})
			})
			.catch(() => {})
	}
	const handleCatalogAdd = () => {
		if (state.catalog.add) return
		catalogDialogTitle.value = '添加目录'
		showAddCatalogDialog.value = true
	}
	const handleCatalogEdit = () => {
		if (state.catalog.edit) return
		catalogDialogTitle.value = '修改目录或专题'
		const checkNode = catalogTreeRef.value.checkNode
		addCatalogForm.id = checkNode.nodeId
		addCatalogForm.type = checkNode.nodeType
		addCatalogForm.name = checkNode.nodeName
		addCatalogForm.sortIndex = checkNode.sortIndex
		showAddCatalogDialog.value = true
	}
	const handleCatalogDel = txt => {
		if (state.catalog.del) return
		const checkNode = catalogTreeRef.value.checkNode
		const catalogId = checkNode?.id
		const name = checkNode?.nodeName
		if (txt === '解绑') {
			getHandle()
		} else {
			const msgTxt = `确定删除 "${name}" 吗？其下子目录都会被删除，专题会被解绑`
			ElMessageBox.confirm(msgTxt, '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					getHandle()
				})
				.catch(() => {})
		}

		function getHandle() {
			sApi
				.catalogDelete({ catalogId })
				.then(() => {
					getCatalogTree()
					ElMessage({
						type: 'success',
						message: '操作成功'
					})
				})
				.catch(() => {})
		}
	}
	const handleCatalogSave = () => {
		let form = { ...addCatalogForm }
		const id = catalogTreeRef.value?.checkNode?.id
		if (form.type === 'theme' && !id) {
			ElMessage.error('请先勾选！')
			return
		}
		if (catalogDialogTitle.value === '添加目录') {
			form.parentId = id ? id : ''
			sApi
				.catalogAdd(form)
				.then(res => {
					if (res.resultCode === 'S') {
						ElMessage({
							message: res.resultMsg,
							type: 'success'
						})
						getCatalogTree()
						addCatalogForm.themeName = ''
						addCatalogForm.themeId = ''
						addCatalogForm.name = ''
						addCatalogForm.type = 'folder'
						showAddCatalogDialog.value = false
					}
				})
				.catch(err => {
					ElMessage.error(err)
				})
		} else {
			form.parentId = ''
			sApi
				.catalogUpdate(form)
				.then(res => {
					if (res.resultCode === 'S') {
						ElMessage({
							message: res.resultMsg,
							type: 'success'
						})
						getCatalogTree()
						addCatalogForm.themeId = ''
						addCatalogForm.id = ''
						addCatalogForm.name = ''
						addCatalogForm.type = 'folder'
						showAddCatalogDialog.value = false
					}
				})
				.catch(err => {
					ElMessage.error(err)
				})
		}
	}
	const dialogClose = () => {
		addThemeForm.parentId = []
		addThemeForm.themeName = ''
		addCatalogForm.parentId = []
		addCatalogForm.themeId = ''
		addCatalogForm.name = ''
		addCatalogForm.type = 'folder'
		addCatalogForm.sortIndex = 0
	}
	const handleAddCatalogCancel = () => {
		showAddCatalogDialog.value = false
		dialogClose()
	}
</script>

<style lang="scss" scoped>
	.serviceCatalog {
		gap: 0;
		padding: 8px 10px 10px;
		overflow: hidden;
	}
	:deep(.el-iconColor) {
		color: #909399;
		font-size: 16px;
	}
	:deep(.el-icon--disabled) {
		opacity: 0.45;
		pointer-events: none;
	}
	:deep(.serviceWrap) {
		height: 100%;
	}
</style>
