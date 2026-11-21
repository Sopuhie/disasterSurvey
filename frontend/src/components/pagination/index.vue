<template>
	<div class="pagination flex faj">
		<el-pagination
			v-model:current-page="state.currentPage"
			v-model:page-size="state.pageSize"
			:page-sizes="pageSizes"
			v-bind="typeof pageCount === 'number' ? { pageCount } : {}"
			:pager-count="pagerCount"
			:small="small"
			:disabled="disabled"
			:background="background"
			:layout="layout"
			:total="total"
			@size-change="handleSizeChange"
			@current-change="handleCurrentChange" />
	</div>
</template>

<script setup>
	import { reactive } from 'vue'

	const props = defineProps({
		total: { type: Number, default: 0 },
		layout: { type: String, default: 'total, sizes, prev, pager, next, jumper' },
		currentPage: { type: Number, default: 1 },
		pageSize: { type: Number, default: 20 },
		// 不传 pageCount，由 total + pageSize 计算页数；若传则按固定页数显示（与 total 二选一）
		pageCount: { type: Number, default: undefined },
		pagerCount: { type: Number, default: 5 },
		pageSizes: { type: Array, default: () => [20, 30, 50, 100] },
		small: { type: Boolean, default: true },
		background: { type: Boolean, default: true },
		disabled: { type: Boolean, default: false }
	})

	const state = reactive({
		currentPage: props.currentPage,
		pageSize: props.pageSize
	})

	const emit = defineEmits(['sizeChange', 'currentChange'])
	const handleSizeChange = val => {
		emit('sizeChange', val)
	}

	const handleCurrentChange = val => {
		emit('currentChange', val)
	}

	// 暴露变量
	defineExpose({
		// 分页还原成默认
		pageReset() {
			state.currentPage = 1
		}
	})
</script>

<style lang="scss" scoped>
	.pagination {
		width: 100%;
		height: 52px;
	}
</style>
