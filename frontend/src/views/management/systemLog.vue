<!-- 系统日志 -->
<template>
	<div class="systemLog max-wh">
		<div class="systemLog__header flex fa pl-10 pr-10">
			<el-input
				class="mr-10"
				v-model="mode.like_username"
				style="width: 140px"
				placeholder="请输入用户名"
				@keyup.enter.native="searchHandle" />
			<el-input
				class="mr-10"
				v-model="mode.like_target"
				style="width: 140px"
				placeholder="请输入操作内容"
				@keyup.enter.native="searchHandle" />
			<el-input
				class="mr-10"
				v-model="mode.like_type"
				style="width: 140px"
				placeholder="请输入操作类型"
				@keyup.enter.native="searchHandle" />
			<el-select class="mr-10" v-model="mode.like_status" placeholder="请选择状态" style="width: 140px">
				<el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
			</el-select>
			<el-input
				class="mr-10"
				v-model="mode.name"
				style="width: 140px"
				placeholder="请输入状态信息"
				@keyup.enter.native="searchHandle" />
			<div>
				<el-date-picker
					class="mr-10"
					v-model="searchTime"
					type="daterange"
					start-placeholder="开始时间"
					end-placeholder="结果时间"
					format="YYYY-MM-DD HH:mm:ss"
					value-format="YYYY-MM-DD HH:mm:ss"
					style="width: 354px" />
			</div>
			<el-button class="mb8" type="primary" plain @click="searchHandle">
				<el-icon class="el-icon--left"><Search /></el-icon>搜索
			</el-button>
			<el-button class="mb8" type="primary" plain @click="exportLog">导出</el-button>
		</div>
		<div class="systemLog__content mt-10 p-10">
			<div class="tableBox" ref="tableBox" style="height: calc(100% - 52px)">
				<ZTable class="max-wh" :tableData="tableData" :tableHeaders="loginHeaders" :tableHeight="tableHeight">
					<template #idx="{ scope }">
						{{ paginationIdxVal(scope.$index) }}
					</template>
				</ZTable>
			</div>
			<ZPagination
				ref="PaginRef"
				:total="total"
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
	import { loginHeaders } from '@/views/management/headers.js'

	import { logApi } from '@/api/logManage/index.js'
	const api = logApi()

	import { useAppStore } from '@/store/modules/app.js'
	const store = useAppStore()

	const tableBox = ref(null)
	const PaginRef = ref(null)
	const loading = ref(false)
	const tableHeight = ref(300)
	const tableData = ref([])
	const total = ref(0)
	const searchTime = ref([])

	const options = [
		{
			value: '成功',
			label: '成功'
		},
		{
			value: '异常',
			label: '异常'
		}
	]

	const mode = ref({
		current: 1,
		size: 20,
		startTime: '',
		endTime: '',
		like_status: '',
		like_target: '',
		like_type: '',
		like_userId: '',
		like_username: ''
	})

	onMounted(() => {
		getPageData()
		tableHeight.value = tableBox.value.clientHeight
	})

	// 获取分页数据
	const getPageData = () => {
		loading.value = true
		if (searchTime.value?.length) {
			mode.value.startTime = searchTime.value[0]
			mode.value.endTime = searchTime.value[1]
		} else {
			mode.value.startTime = ''
			mode.value.endTime = ''
		}

		api
			.logQuery(mode.value)
			.then(res => {
				setTimeout(() => {
					loading.value = false
					tableData.value = res.data || []
					total.value = res.pageInfo.recordCount
				}, 300)
			})
			.catch(err => {
				loading.value = false
			})
	}
	// 搜索
	const searchHandle = () => {
		getPageData()
	}

	// 导出
	const exportLog = () => {
		const loading = ElLoading.service({
			lock: true,
			text: '文件加载中...',
			background: 'rgba(0, 0, 0, 0.7)'
		})
		api
			.logExport(mode.value)
			.then(res => {
				const url = URL.createObjectURL(res.data)
				const link = document.createElement('a')
				link.href = url
				link.setAttribute('download', '系统日志.xlsx')
				document.body.appendChild(link)
				link.click()
				loading.close()
			})
			.catch(err => {
				loading.close()
			})
	}

	// 分页事件
	const sizeChange = val => {
		mode.value.size = val
		getPageData()
	}
	const currentChange = val => {
		mode.value.current = val
		getPageData()
	}
	// 分页器
	const paginationIdxVal = index => {
		return mode.value.current > 1 ? index + 1 + mode.value.size * (mode.value.current - 1) : index + 1
	}
</script>

<style lang="scss" scoped>
	.systemLog {
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
