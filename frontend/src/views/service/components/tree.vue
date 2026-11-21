<template>
	<div class="serviceTree" ref="serviceTree">
		<el-tree
			ref="treeRef"
			:data="data"
			:props="{ ...defaultProps, class: customNodeClass }"
			node-key="id"
			:accordion="accordion"
			:show-checkbox="showCheckbox"
			highlight-current
			:check-strictly="checkStrictly"
			:check-on-click-node="checkOnClickNode"
			:expand-on-click-node="expandOnClickNode"
			:height="treeHeight"
			:default-expanded-keys="defaultExpandedKeys"
			:default-checked-keys="defaultCheckedKeys"
			:filter-node-method="filterNode"
			@check="handleCheck"
			@check-change="handleCheckChange"
			@node-click="handleNodeClick">
			<template #default="{ node, data }">
				<span class="custom-tree-node flex fa jsb">
					<span class="flex fa">
						<template v-if="data.nodeType === 'folder'"><el-icon class="mr-8"><Folder /></el-icon></template>
						<template v-if="data.nodeType === 'theme'"><el-icon class="mr-8"><Files /></el-icon></template>
						<template v-if="data.nodeType === 'service'"><el-icon class="mr-8"><Collection /></el-icon></template>
						{{ node.label }}</span
					>
					<span>
						<a class="detail" v-if="data.nodeType === 'service' && showDetail" @click="handleDetails(data)">
							{{ detailText }}
						</a>
					</span>
				</span>
			</template>
		</el-tree>
	</div>
</template>

<script setup>
	import { _debounce } from '@/utils/index.js'
	const emit = defineEmits(['handleDetails', 'handleCheck'])
	import { useAppStore } from '@/store/modules/app.js'
	const store = useAppStore()

	const defaultProps = { value: 'id', label: 'label', children: 'children' }
	const serviceTree = ref(null)
	const treeRef = ref(null)
	const treeHeight = ref(200)
	const treeNode = ref(null)
	const checkNode = ref(null)
	const checkNodeKey = ref([])
	const defaultExpandedKeys = ref([])
	const defaultCheckedKeys = ref([])

	const props = defineProps({
		data: { type: Array, default: () => [] },
		itemName: { type: String, default: 'service' },
		filterText: { type: String, default: '' },
		checkOnClickNode: { type: Boolean, default: false },
		expandOnClickNode: { type: Boolean, default: true },
		accordion: { type: Boolean, default: true },
		checkStrictly: { type: Boolean, default: false },
		showCheckbox: { type: Boolean, default: true },
		showDetail: { type: Boolean, default: false },
		detailText: { type: String, default: '查看' }
	})

	watch(
		() => props.filterText,
		_debounce(val => treeRef.value?.filter(val), 200)
	)
	watch(
		() => store.darkTheme,
		() => {}
	)
	onMounted(() => {
		treeHeight.value = serviceTree.value.clientHeight
	})
	const customNodeClass = data => data.nodeType
	const handleDetails = data => emit('handleDetails', data)
	const handleNodeClick = data => {
		treeNode.value = data
	}
	const handleCheckChange = (data, checked) => {
		if (checked) treeRef.value.setCheckedNodes([data])
	}
	const handleCheck = (checkedNodes, checkedKeys) => {
		checkNode.value = checkedKeys.checkedKeys?.length ? checkedNodes : null
		checkNodeKey.value = checkedKeys.checkedKeys
		emit('handleCheck', { itemName: props.itemName, data: checkedNodes, checkedKeys: checkedKeys.checkedKeys })
	}
	const filterNode = (value, data) => {
		if (!value) return true
		return !!data.label && data.label.indexOf(value) !== -1
	}
	const reset = () => {
		treeNode.value = null
		checkNode.value = null
		checkNodeKey.value = []
	}
	const clickExpand = () => Object.values(treeRef.value?.store.nodesMap).forEach(v => v.expand())
	const clickCollapse = () => Object.values(treeRef.value?.store.nodesMap).forEach(v => v.collapse())

	defineExpose({ treeNode, checkNode, checkNodeKey, reset, clickExpand, clickCollapse })
</script>

<style lang="scss" scoped>
	.serviceTree {
		width: 100%;
		height: calc(100% - 132px);
		overflow-y: auto;
		padding: 0 8px 8px;
		box-sizing: border-box;
	}
	.serviceTree::-webkit-scrollbar {
		width: 6px;
	}
	.serviceTree::-webkit-scrollbar-thumb {
		background: #d1d5de;
		border-radius: 4px;
	}
	.serviceTree::-webkit-scrollbar-track {
		background: transparent;
	}
	.custom-tree-node {
		flex: 1;
		padding-right: 8px;
		& .detail {
			color: #1890ff;
			font-size: 12px;
		}
	}
	:deep() {
		.el-tree,
		.el-vl__wrapper {
			height: 100%;
		}
	}
</style>
