<template>
	<div class="menuTree max-wh">
		<div class="mHeader">
			<p class="mt noSelect">{{ title }}</p>
		</div>
		<div class="treeBox noSelect max-w oy mt-10">
			<el-tree
				style="width: 100%; color: #606266 !important"
				:data="treeData"
				node-key="id"
				:accordion="accordion"
				highlight-current
				:props="defaultProps"
				:expand-on-click-node="expandOnClickNode"
				:default-expanded-keys="defaultExpandedKeys"
				@node-click="handleNodeClick">
				<template #default="{ node, data }">
					<slot name="default" :data="{ node, data }"></slot>
				</template>
			</el-tree>
		</div>
	</div>
</template>

<script setup>
	const defaultProps = {
		id: 'id',
		children: 'children',
		label: 'label'
	}
	const props = defineProps({
		title: {
			type: String,
			default: '资源目录'
		},
		expandOnClickNode: {
			type: Boolean,
			default: true
		},
		defaultExpandedKeys: {
			type: Array,
			default: () => []
		},
		accordion: {
			type: Boolean,
			default: true
		},
		treeData: {
			type: Array,
			default: () => []
		}
	})
	const emit = defineEmits(['node-click'])

	const handleNodeClick = (data, node) => {
		emit('node-click', data, node)
	}
</script>

<style lang="scss" scoped>
	.mHeader {
		color: #606266;
		& .mt {
			height: 32px;
			line-height: 32px;
			font-size: 12px;
			text-align: center;
			border-radius: 3px;
			border: 1px solid #dcdfe6;
			background-color: #e8f4ff;
		}
	}

	.treeBox {
		height: calc(100% - 48px);
	}
</style>
