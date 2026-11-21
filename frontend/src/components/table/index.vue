<template>
	<el-table
		id="resultTable"
		ref="refTable"
		:class="{ asExpand }"
		:data="tableData"
		:border="border"
		:stripe="stripe"
		style="width: 100%"
		:height="tableHeight || undefined"
		:max-height="tableHeight || undefined"
		:header-cell-style="headerCellStyle"
		:table-layout="tableLayout"
		:row-class-name="tableRowClassName"
		:row-key="rowKey"
		:expand-row-keys="expandRowKeys"
		@row-click="handleRowClick"
		@selection-change="handleSelectionChange"
		@select="handleSelectChange"
		@select-all="handleSelectAllChange">
		<el-table-column type="expand" width="1" v-if="asExpand">
			<template #default="scope">
				<slot :name="'expand'" :scope="scope"></slot>
			</template>
		</el-table-column>
		<el-table-column fixed v-if="selectShow" type="selection" width="55" />
		<el-table-column
			:type="columnType"
			:align="head.align"
			:prop="head.prop"
			:label="head.label"
			:minWidth="head.minWidth"
			:width="head.width"
			:maxWidth="head.maxWidth"
			:fixed="head.fixed"
			:showOverflowTooltip="head.showTooltip"
			v-for="head in tableHeaders"
			:key="head.id">
			<template #default="scope">
				<span v-if="head.prop">{{ scope.row[head.prop] }}</span>
				<slot v-else :scope="scope" :name="head.slot"></slot>
			</template>
		</el-table-column>
		<template #empty>
			<div class="empty" v-if="empty" :style="{ height: tableHeight - 44 + 'px' }">
				<el-empty :image-size="imageSize" :description="emptyText" />
			</div>
		</template>
	</el-table>
</template>

<script setup>
	import { saveAs } from 'file-saver'
	import * as XLSX from 'xlsx'

	const props = defineProps({
		tableData: {
			type: Array,
			default: () => []
		},
		tableHeaders: {
			type: Array,
			default: () => []
		},
		// 表格高度
		tableHeight: {
			type: Number,
			default: 300
		},
		imageSize: {
			type: Number,
			default: 200
		},
		// 表头样式
		headerCellStyle: {
			type: Object,
			default: () => {
				return {}
			}
		},
		border: {
			type: Boolean,
			default: true
		},
		empty: {
			type: Boolean,
			default: true
		},
		stripe: {
			type: Boolean,
			default: false
		},
		// 对应列的类型 selection/expand/index
		columnType: {
			type: String,
			default: ''
		},
		// 是否显示选择栏
		selectShow: {
			type: Boolean,
			default: false
		},
		// 列的宽度是否自撑开
		fit: {
			type: Boolean,
			default: true
		},
		// 是否要高亮当前行
		highlightCurrentRow: {
			type: Boolean,
			default: false
		},
		// 设置表格单元、行和列的布局方式
		tableLayout: {
			type: String,
			default: 'fixed'
		},
		// large / default /small
		size: {
			type: String,
			default: 'default'
		},
		emptyText: {
			type: String,
			default: '暂无数据'
		},
		// 行的key
		rowKey: {
			type: String,
			default: 'id'
		},
		// 是否显示展开行按钮列
		asExpand: {
			type: Boolean,
			default: false
		},
		// 展开行的keys
		expandRowKeys: {
			type: Array,
			default: () => []
		}
	})

	const emit = defineEmits(['selectionChange', 'rowClick', 'delete', 'selectChange', 'selectAllChange'])
	// 选中项发生改变
	const handleSelectionChange = selection => {
		emit('selectionChange', selection)
	}
	const handleRowClick = (row, column) => {
		emit('rowClick', row, column)
	}
	// 手动触发勾选
	const handleSelectChange = (selection, row) => {
		emit('selectChange', selection, row)
	}
	// 手动触发全选
	const handleSelectAllChange = selection => {
		emit('selectAllChange', selection)
	}

	const refTable = ref(null)
	// 回显选中
	const handleEcho = echoData => {
		clearSelection()
		if (echoData && echoData.length) {
			for (const row of echoData) {
				refTable.value.toggleRowSelection(row, true)
			}
		}
	}
	// 清空选择
	const clearSelection = () => {
		refTable.value.clearSelection()
	}
	/**
	 * 下载excel
	 */
	const downloadExcel = () => {
		const table = refTable.value
		if (!table) return

		const workbook = XLSX.utils.table_to_book(document.querySelector('#resultTable'), { row: true })
		const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })

		const excelData = new Blob([excelBuffer], { type: 'application/octet-stream' })
		saveAs(excelData, 'data.xlsx')
	}
	defineExpose({ handleEcho, clearSelection, downloadExcel })

	const tableRowClassName = scope => {
		return scope.rowIndex % 2 === 0 && props.stripe ? 'bg-row' : ''
	}
</script>
