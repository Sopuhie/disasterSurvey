<template>
	<div class="tableLists" :class="{ isCollapse: appStore.switchObj.sidebarFlag }">
		<div class="tableLists__header flex fa pl-10 pr-10">
			<el-input class="mr-10" v-model="mode.input1" style="width: 132px" placeholder="请输入名称" />
			<el-input class="mr-10" v-model="mode.input5" style="width: 132px" placeholder="请输入状态信息" />
			<div>
				<el-date-picker
					style="width: 240px"
					class="mr-10"
					v-model="mode.input6"
					type="datetimerange"
					start-placeholder="开始时间"
					end-placeholder="结束时间"
					format="YYYY-MM-DD HH:mm:ss"
					date-format="YYYY/MM/DD ddd"
					time-format="A hh:mm:ss" />
			</div>
			<el-button v-btn @click="resetHandle">
				<el-icon><Refresh /></el-icon>
				<span>重置</span>
			</el-button>
			<el-button v-btn type="primary" @click="searchHandle">
				<el-icon><Search /></el-icon>
				<span>查询</span>
			</el-button>
			<el-button v-btn @click="close">
				<el-icon><Close /></el-icon>
				<span>关闭</span>
			</el-button>
		</div>
		<div class="tableLists__content mt-10 p-10">
			<div class="tableBox" ref="tableBox" style="height: calc(100% - 52px)">
				<ZTable class="max-wh" :tableData="tableData" :tableHeight="tableHeight" :tableHeaders="tableHeaders">
					<template #idx="{ scope }">
						{{ paginationIdxVal(scope.$index) }}
					</template>
				</ZTable>
			</div>
			<ZPagination
				ref="PaginRef"
				:total="20"
				:pageSize="mode.size"
				:currentPage="mode.current"
				@sizeChange="sizeChange"
				@currentChange="currentChange"></ZPagination>
		</div>
	</div>
</template>

<script setup>
	import ZTable from '@/components/table/index.vue'
	import ZPagination from '@/components/pagination/index.vue'
	import { onMounted } from 'vue'

	import { getHeaders, loadDatas } from '@/views/map/loadBaseDatas/index.js'
	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()

	const props = defineProps({
		selectedName: {
			type: String,
			default: ''
		},
		pathName: {
			type: String,
			default: ''
		},
	})

	const tableData = ref([]);
	const tableHeaders = ref([])

	onMounted(() => {
		tableHeaders.value = getHeaders(props.pathName)
		tableData.value = loadDatas(props.pathName, props.selectedName)
	})

	const tableBox = ref(null)
	const PaginRef = ref(null)
	const tableHeight = ref(300)

	const options = [
		{
			value: 'Option1',
			label: 'Option1'
		},
		{
			value: 'Option2',
			label: 'Option2'
		}
	]
	const mode = ref({
		input1: '',
		input2: '',
		input3: '',
		input4: '',
		input5: '',
		input6: '',
		current: 1,
		size: 20
	})

	onMounted(() => {
		tableHeight.value = tableBox.value?.clientHeight
	})
	const emits = defineEmits(['close'])
	// 重置
	const resetHandle = () => {
		mode.value = {
			input1: '',
			input2: '',
			input3: '',
			input4: '',
			input5: '',
			input6: '',
			current: 1,
			size: 20
		}
	}
	// 查询
	const searchHandle = () => {}
	// 关闭
	const close = () => {
		emits('close')
	}
	// 分页事件
	const sizeChange = val => {
		mode.value.size = val
		// getPageData()
	}
	const currentChange = val => {
		mode.value.current = val
		// getPageData()
	}
	// 分页器
	const paginationIdxVal = index => {
		return mode.value.current > 1 ? index + 1 + mode.value.size * (mode.value.current - 1) : index + 1
	}
</script>

<style lang="scss" scoped>
	.tableLists {
		position: absolute;
		top: 140px;
		left: 570px;
		z-index: 3001;
		width: calc(100% - 634px);
		height: calc(100% - 212px);
		background-color: #fff;
		&.isCollapse {
			left: 64px;
			width: calc(100% - 64px);
		}
		&__header {
			height: 64px;
			background-color: #fff;
		}
		&__content {
			border-radius: 8px;
			height: calc(100% - 80px);
			background-color: #fff;
		}
	}
</style>
