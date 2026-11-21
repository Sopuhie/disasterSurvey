<!-- 纵向表格 -->
<template>
	<table class="table" style="width: 100%" border="0" cellspacing="1">
		<tr v-for="(row, index) in processedTableData" :key="index">
			<template v-for="(cell, cellIndex) in row" :key="cellIndex">
				<th v-if="cell.type === 'header'">{{ cell.label }}</th>
				<td v-else>{{ cell.value }}</td>
			</template>
		</tr>
	</table>
</template>

<script setup>
	import { computed } from 'vue'

	const props = defineProps({
		rows: {
			type: Number,
			default: 1
		},
		tableData: {
			type: Array,
			default: () => [
				[
					{ type: 'header', label: '站点名称' },
					{ type: 'data', value: 'xxxx分局' },
					{ type: 'header', label: '站点编码' },
					{ type: 'data', value: '站点编码' },
					{ type: 'header', label: '经度' },
					{ type: 'data', value: '39.9042° N' },
					{ type: 'header', label: '纬度' },
					{ type: 'data', value: '2024-06-07' },
					{ type: 'header', label: '类型' },
					{ type: 'data', value: 'xxx海域' },
					{ type: 'header', label: '建立年份' },
					{ type: 'data', value: '规则数' }
				]
			]
		}
	})

	const processedTableData = computed(() => {
		// 如果 rows 是 1，直接返回原始数据
		if (props.rows === 1) {
			return props.tableData
		}
		// 获取原始数据
		const items = props.tableData[0]
		const result = []

		// 根据 rows 的数量分割数据
		for (let i = 0; i <= items.length / 2; i++) {
			const currentRow = items.slice(i * (2 * props.rows), 2 * props.rows * (i + 1))
			if (currentRow.length > 0) {
				result.push(currentRow)
			}
		}
		return result
	})
</script>

<style scoped></style>
