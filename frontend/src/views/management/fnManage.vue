<template>
	<div class="systemLog max-wh">
		<div class="systemLog__header flex fa pl-10 pr-10">
			<el-input
				v-model="mode.like_name"
				placeholder="请输入名称"
				clearable
				style="width: 200px"
				@keyup.enter.native="searchHandle"></el-input>
			<el-select class="ml-10" v-model="mode.eq_nodetype" placeholder="请选择类型" clearable style="width: 200px">
				<el-option v-for="item in lxOptions" :key="item.value" :label="item.label" :value="item.value" />
			</el-select>
			<!-- <el-select
				class="ml-10 mr-10"
				v-model="mode.eq_system"
				placeholder="请选择所属子系统"
				clearable
				style="width: 200px">
				<el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
			</el-select> -->
			<span style="padding: 20px;"></span>
			<el-button type="primary" plain @click="searchHandle">
				<el-icon class="el-icon--left"><Search /></el-icon>
				<span>搜索</span>
			</el-button>
			<el-button type="primary" plain @click="addHandle">
				<el-icon class="el-icon--left"><Plus /></el-icon>
				<span>添加</span>
			</el-button>
		</div>
		<div class="systemLog__content mt-10 p-10">
			<div class="tableBox" ref="tableBox" style="height: 100%">
				<el-table
					:data="tableData"
					style="width: 100%"
					:max-height="tableHeight"
					row-key="id"
					:tree-props="{ children: 'children' }"
					default-expand-all
					:border="true">
					<el-table-column
						:align="head.align"
						:prop="head.prop"
						:label="head.label"
						:minWidth="head.minWidth"
						:width="head.width"
						:maxWidth="head.maxWidth"
						:fixed="head.fixed"
						:showOverflowTooltip="head.showTooltip"
						v-for="head in actionHeaders"
						:key="head.id">
						<template #default="scope">
							<span v-if="head.prop">{{ scope.row[head.prop] }}</span>
							<span v-else-if="head.slot === 'idx'">
								{{ paginationIdxVal(scope.$index) }}
							</span>
							<span v-else-if="head.slot === 'parentId'">
								{{ getParentName(scope.row) }}
							</span>
							<span v-else-if="head.slot === 'state'">
								{{ getRowState(scope.row) }}
							</span>
							<span v-else-if="head.slot === 'action'">
								<el-button
									:type="scope.row.visible === '0' ? 'success' : 'danger'"
									link
									@click="actionHandle(scope.row, '停用')">
									<span>{{ scope.row.visible === '0' ? '启用' : '停用' }}</span>
								</el-button>
								<el-button type="primary" link @click="actionHandle(scope.row, '编辑')">
									<span>编辑</span>
								</el-button>
								<el-button type="danger" link @click="actionHandle(scope.row, '删除')">
									<span>删除</span>
								</el-button>
								<el-button type="primary" link @click="actionHandle(scope.row, '配置')">
									<span>配置</span>
								</el-button>
							</span>
						</template>
					</el-table-column>
					<template #empty>
						<div class="empty" :style="{ height: tableHeight - 44 + 'px' }">
							<el-empty :image-size="200" description="暂无数据" />
						</div>
					</template>
				</el-table>
			</div>
		</div>
	</div>
	<ZDialogCom
		v-if="showDialog"
		:wrapStyle="wrapStyle"
		:bodyStyle="{ padding: '16px' }"
		toPath="cancel"
		alignCenter
		:title="title"
		@cancel="cancelHandler"
		@handleOk="addSave">
		<ConfigCom v-if="title === '功能配置'" ref="configRef" :itemData="rowData"></ConfigCom>
		<AddCom v-else ref="addRef" :options="options" :itemData="rowData"></AddCom>
	</ZDialogCom>
</template>

<script setup>
	import ZDialogCom from '@/components/dialog/index.vue'
	import AddCom from '@/views/management/fnCom/add.vue'
	import ConfigCom from '@/views/management/fnCom/config.vue'
	import { actionHeaders } from '@/views/management/headers.js'
	import { setTreeProps, getParentItem, getWindowSize } from '@/utils/index.js'

	import { systemApi } from '@/api/system/index.js'
	const api = systemApi()

	const wrapStyle = ref({
		width: '768px',
		height: '650px',
		top: '80px',
		left: `${getWindowSize().w.value - 768}px`
	})

	const lxOptions = ref([])
	const options = ref([])
	const tableBox = ref(null)
	const addRef = ref(null)
	const configRef = ref(null)
	const showDialog = ref(false)
	const loading = ref(false)
	const tableHeight = ref(300)
	const tableData = ref([])
	const actionTree = ref([])
	const typeHandle = ref('add')
	const title = ref('添加功能')
	const rowData = ref(null)
	const mode = ref({
		adminFlag: '',
		current: 1,
		size: 0,
		eq_nodetype: '',
		eq_system: '',
		eq_visible: '',
		like_alias: '',
		like_name: '',
		loginUserId: ''
	})

	onMounted(() => {
		getActionTree()
		getDict()
		getConfigPageData()
		tableHeight.value = tableBox.value.clientHeight
	})

	const cancelHandler = () => {
		showDialog.value = false
	}

	// 获取分页数据
	const getPageData = () => {
		loading.value = true
		api
			.actionQueryWithPage(mode.value)
			.then(res => {
				setTimeout(() => {
					loading.value = false
					tableData.value = initTreeData(res.data) || []
				}, 300)
			})
			.catch(err => {
				loading.value = false
			})
	}
	// 获取子系统数据
	const getConfigPageData = () => {
		const form = {
			adminFlag: '',
			current: 1,
			size: 0,
			like_name: '',
			like_title: '',
			loginUserId: ''
		}
		api
			.configQueryWithPage(form)
			.then(res => {
				options.value = setTreeProps(res.data, 'title', 'id', 'name')
			})
			.catch(err => {
				ElMessage.error(err)
			})
	}
	// 获取字典--功能菜单类型
	const getDict = () => {
		api
			.dictDetailQuery({
				zdlxdm: 'gngl_gncdlx'
			})
			.then(res => {
				if (res.data?.length > 0) {
					const dictData = []
					if (res?.data?.length > 0) {
						res.data.forEach(item => {
							dictData.push({ label: item.zdz, value: item.zdbm })
						})
						lxOptions.value = dictData
					}
				}
			})
			.catch(err => {
				ElMessage.error(err.message)
			})
	}
	// 上级功能树
	const getActionTree = () => {
		api
			.actionTree()
			.then(res => {
				actionTree.value = res.data
				getPageData()
			})
			.catch(err => {
				ElMessage.error(err.message)
			})
	}
	// 父级名称
	const getParentName = row => {
		let name = ''
		if (actionTree.value.length === 0) {
			return name
		} else {
			if (row.parentId === '-1') {
				name = '/'
			} else {
				const parentItem = getParentItem(toRaw(actionTree.value), row.parentId)
				name = parentItem?.name ? parentItem.name : '/'
			}
			return name
		}
	}
	// 是否有搜索条件
	const hasSearchCondition = () => {
		const m = mode.value
		return !!(m.like_name?.trim() || m.eq_nodetype || m.eq_system)
	}
	// 将树扁平化为列表（带 parentId，用于回溯祖先）
	const flattenTree = (nodes, result = [], parentId = '-1') => {
		if (!nodes?.length) return result
		nodes.forEach(node => {
			const item = { ...node, _parentId: node.parentId ?? parentId }
			result.push(item)
			if (node.children?.length) {
				flattenTree(node.children, result, node.id)
			}
		})
		return result
	}
	// 单节点是否匹配搜索条件
	const nodeMatches = (node, m) => {
		if (m.like_name?.trim()) {
			const name = (node.name || '').toLowerCase()
			const alias = (node.alias || '').toLowerCase()
			const kw = m.like_name.trim().toLowerCase()
			if (!name.includes(kw) && !alias.includes(kw)) return false
		}
		if (m.eq_nodetype && node.nodetype !== m.eq_nodetype) return false
		if (m.eq_system && (node.system ?? node.configId ?? node.subsystem) !== m.eq_system) return false
		return true
	}
	// 在完整树中按条件筛选，保留匹配节点及其祖先路径（支持子级搜索）
	const filterTreeBySearch = (tree, m) => {
		if (!tree?.length) return []
		const treeCopy = JSON.parse(JSON.stringify(tree))
		const flat = flattenTree(treeCopy)
		const idToNode = {}
		flat.forEach(n => {
			idToNode[n.id] = n
		})
		const matchedIds = flat.filter(n => nodeMatches(n, m)).map(n => n.id)
		const showIds = new Set(matchedIds)
		matchedIds.forEach(id => {
			let cur = idToNode[id]
			while (cur?._parentId && cur._parentId !== '-1') {
				showIds.add(cur._parentId)
				cur = idToNode[cur._parentId]
			}
		})
		const filterNodes = (nodes) => {
			if (!nodes?.length) return []
			return nodes
				.filter(n => showIds.has(n.id))
				.map(n => ({
					...n,
					children: n.children?.length ? filterNodes(n.children) : []
				}))
		}
		return filterNodes(treeCopy)
	}
	// 搜索
	const searchHandle = () => {
		if (hasSearchCondition()) {
			loading.value = true
			setTimeout(() => {
				tableData.value = filterTreeBySearch(actionTree.value, mode.value) || []
				loading.value = false
			}, 100)
		} else {
			getPageData()
		}
	}
	// 操作事件
	const actionHandle = (row, flag) => {
		rowData.value = row
		switch (flag) {
			case '停用':
				stopHandle(row)
				break
			case '编辑':
				editHandle(row)
				break
			case '删除':
				deleteHandle(row)
				break
			case '配置':
				configHandle(row)
				break
			default:
				break
		}
	}
	// 添加事件
	const addHandle = () => {
		showDialog.value = true
		rowData.value = null
		title.value = '添加配置'
		typeHandle.value = true
		wrapStyle.value.width = '768px'
		wrapStyle.value.height = '650px'
		wrapStyle.value.left = `${(getWindowSize().w.value - 768) / 2}px`
	}
	// 编辑
	const editHandle = row => {
		showDialog.value = true
		title.value = '修改功能'
		typeHandle.value = false
		wrapStyle.value.width = '768px'
		wrapStyle.value.height = '650px'
		wrapStyle.value.left = `${(getWindowSize().w.value - 768) / 2}px`
	}
	// 添加/编辑保存
	const addSave = () => {
		let form = null
		if (title.value === '添加配置') {
			form = addRef.value.form
			api
				.actionAdd(form)
				.then(res => {
					showDialog.value = false
					getPageData()
					ElMessage({ message: res.resultMsg, type: 'success' })
				})
				.catch(err => {
					ElMessage.error(err)
				})
		}
		if (title.value === '修改功能') {
			form = addRef.value.form
			form.id = rowData.value.id
			api
				.actionUpdate(form)
				.then(res => {
					showDialog.value = false
					getPageData()
					ElMessage({ message: res.resultMsg, type: 'success' })
				})
				.catch(err => {
					ElMessage.error(err)
				})
		}
		if (title.value === '功能配置') {
			form = rowData.value
			const jsonData = configRef.value.getJsonData()
			form['configure'] = JSON.stringify(jsonData)
			api
				.actionUpdate(form)
				.then(res => {
					showDialog.value = false
					getPageData()
					ElMessage({ message: res.resultMsg, type: 'success' })
				})
				.catch(err => {
					ElMessage.error(err)
				})
		}
	}
	// 停用
	const stopHandle = row => {
		const message = row.visible === '1' ? '你确定停用此功能吗？' : '你确定启用此功能吗？'

		ElMessageBox.confirm(message, '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				api
					.actionUpdate({ id: row.id, visible: row.visible === '1' ? '0' : '1', zhgxr: row.zhgxr })
					.then(res => {
						getPageData()
					})
					.catch(err => {})
			})
			.catch(err => {})
	}
	// 删除
	const deleteHandle = row => {
		ElMessageBox.confirm('确定删除该数据吗?', '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				api
					.actionDelete({ actionId: row.id })
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
			.catch(err => {})
	}
	// 配置
	const configHandle = row => {
		showDialog.value = true
		title.value = '功能配置'
		wrapStyle.value.width = '1200px'
		wrapStyle.value.height = `${getWindowSize().h.value - 120}px`
		wrapStyle.value.left = `${(getWindowSize().w.value - 1200) / 2}px`
	}

	const getRowState = row => {
		let txt = ''
		switch (row.nodetype) {
			case 'menu':
				txt = '菜单'
				break
			case 'action':
				txt = '按钮'
				break
			case 'folder':
				txt = '目录'
				break
			case 'leftmenu':
				txt = '侧边菜单'
				break
			case 'outmodule':
				txt = '外部模块'
				break
			default:
				txt = ''
				break
		}
		return txt
	}
	// 分页器
	const paginationIdxVal = index => {
		return mode.value.current > 1 ? index + 1 + mode.value.size * (mode.value.current - 1) : index + 1
	}
	// 树数据格式化
	const initTreeData = (arr, parentId = '-1') => {
		if (!arr || arr?.length === 0) return []
		function loop(parentId) {
			return arr.reduce((pre, cur) => {
				if (cur.parentId === parentId) {
					cur.children = loop(cur.id)
					pre.push(cur)
				}
				return pre
			}, [])
		}
		return loop(parentId)
	}
</script>

<style lang="scss" scoped>
	.systemLog {
		&__header {
			height: 52px;
			border-radius: 8px;
			background-color: #fff;
		}
		&__content {
			border-radius: 8px;
			height: calc(100% - 62px);
			background-color: #fff;
		}
	}
</style>
