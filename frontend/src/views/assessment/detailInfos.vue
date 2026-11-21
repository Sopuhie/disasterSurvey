<template>
	<el-dialog v-model="centerDialogVisible" title="风暴潮受灾信息整合" width="1100" height="800" center>
		<el-table :data="gridData">
			<el-table-column property="name" label="名称" width="150" />
			<el-table-column property="date" label="日期" width="120" />
			<el-table-column property="time" label="时间" width="100" />
			<el-table-column property="highPos" label="高潮位(m)" width="100" />
			<el-table-column property="bluePos" label="蓝色警戒(m)" width="120" />
			<el-table-column property="yellowPos" label="黄色警戒(m)" width="120" />
			<el-table-column property="orangePos" label="橙色警戒(m)" width="120" />
			<el-table-column property="redPos" label="红色警戒(m)" width="120" />
			<el-table-column property="level" label="预警级别" />
		</el-table>
	</el-dialog>
</template>

<script setup>
	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	const centerDialogVisible = ref(true)

	const gridData = [
		{
			name: '东山站',
			date: '2024-05-02',
			time: '12:25',
			highPos: '2.60',
			bluePos: '2.55',
			yellowPos: '2.65',
			orangePos: '2.85',
			redPos: '3.05',
			level: '蓝色'
		},
		{
			name: '东山站',
			date: '2024-05-02',
			time: '14:10',
			highPos: '2.72',
			bluePos: '2.55',
			yellowPos: '2.65',
			orangePos: '2.85',
			redPos: '3.05',
			level: '黄色'
		},
		{
			name: '东山站',
			date: '2024-05-02',
			time: '16:40',
			highPos: '2.91',
			bluePos: '2.55',
			yellowPos: '2.65',
			orangePos: '2.85',
			redPos: '3.05',
			level: '橙色'
		}
	]
</script>
