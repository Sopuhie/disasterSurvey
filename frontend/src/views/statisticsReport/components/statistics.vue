<!-- 统计条件 -->
<template>
	<ul class="tj_select">
		<li class="item">
			<span class="txt">统计类型</span>
			<el-select v-model="tj.tjType" style="width: 110px" placements="top" @change="tjTypeChange">
				<el-option :key="item.type" :label="item.title" :value="item.type" v-for="item in tjTypeDatas"> </el-option>
			</el-select>
		</li>
		<li class="item">
			<span class="txt">图表类型</span>
			<el-select v-model="tj.echartsType" style="width: 110px" placements="top">
				<el-option :key="item.type" :label="item.title" :value="item.type" v-for="item in echartsData"> </el-option>
			</el-select>
		</li>
		<li class="item">
			<span class="txt">分组字段</span>
			<el-tree-select
				v-model="tj.fzzd"
				:data="groupFields"
				render-after-expand
				fit-input-width
				clearable
				filterable
				:fallback-placements="['top']"
				placeholder="请选择"
				style="width: 108px"
				popper-class="key-tree-select-popper"
				@change="fzzdSelect"></el-tree-select>
		</li>
		<li class="item" style="position: relative">
			<span class="txt">统计字段</span>
			<el-tree-select
				v-model="tj.tjzd"
				:data="statisFields"
				render-after-expand
				fit-input-width
				clearable
				:fallback-placements="['top']"
				placeholder="请选择"
				style="width: 108px"
				popper-class="key-tree-select-popper"
				@focus="tjzdFocus"
				@change="tjzdSelect"></el-tree-select>
		</li>
	</ul>
</template>

<script setup>
	const tjzdDatas = ref([])
	const echartsData = ref([
		{ type: 'pie', title: '饼状图' },
		{ type: 'bar', title: '柱状图' },
		{ type: 'line', title: '折线图' }
	])
	const tjTypeDatas = ref([
		{ type: 'sum', title: '汇总' },
		{ type: 'count', title: '计数' }
	])
	const tj = ref({
		fzzd: undefined,
		tjzd: undefined,
		tjType: 'sum',
		echartsType: 'pie'
	})

	const props = defineProps({
		fields: {
			type: Array,
			default: () => []
		}
	})

	/**
	 * 分组字段
	 */
	const groupFields = computed(() => {
		let filterFields = []
		let filterStrs = ['string']
		if (props.fields?.length) {
			filterFields = props.fields.filter(n => filterStrs.some(m => n.type.toLowerCase().includes(m)))
		}
		return filterFields
	})

	/**
	 * 统计字段
	 */
	const statisFields = computed(() => {
		let filterFields = []
		let filterStrs = ['double', 'integer']
		if (props.fields?.length) {
			filterFields = props.fields.filter(n => filterStrs.some(m => n.type.toLowerCase().includes(m)))
		}
		return filterFields
	})

	/**
	 * 统计类型选择
	 * @param {string} value
	 */
	const tjTypeChange = value => {
		console.log('统计类型选择:', value, tj.value)
	}
	/**
	 * 分组字段选择
	 * @param {string} value
	 */
	const fzzdSelect = value => {
		console.log(value)
	}
	const tjzdFocus = () => {}
	const tjzdSelect = () => {}
	defineExpose({ tj })
</script>

<style lang="scss" scoped>
	.tj_select {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		& li.item {
			display: flex;
			align-items: center;
			width: 50%;
			margin-top: 24px;
			& .txt {
				display: inline-block;
				font-size: 14px;
				margin-right: 12px;
			}
		}
	}
</style>
