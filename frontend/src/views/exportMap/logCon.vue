<template>
	<div class="max-wh">
		<el-form ref="ruleFormRef" :model="formData" label-width="auto" :size="formSize" status-icon>
			<el-form-item label="状态" prop="region">
				<el-select v-model="formData.like_status" placeholder="请选择">
					<el-option label="成功" value="S" />
					<el-option label="失败" value="F" />
				</el-select>
			</el-form-item>
			<el-form-item label="操作时间">
				<el-col :span="11">
					<el-form-item prop="date1">
						<el-date-picker v-model="formData.startTime" type="date" placeholder="开始日期" style="width: 100%" />
					</el-form-item>
				</el-col>
				<el-col class="flex faj" :span="2">
					<span class="text-gray-500">-</span>
				</el-col>
				<el-col :span="11">
					<el-form-item prop="date2">
						<el-time-picker v-model="formData.endTime" placeholder="结束日期" style="width: 100%" />
					</el-form-item>
				</el-col>
			</el-form-item>
			<el-form-item>
				<el-button v-btn type="primary" @click="exportHandler">导出</el-button>
				<el-button v-btn type="primary" @click="initData">搜索</el-button>
				<el-button v-btn type="primary" @click="resetForm(ruleFormRef)">重置</el-button>
			</el-form-item>
		</el-form>
		<div class="tableBox">
			<el-table :data="tableData" :border="true" style="width: 100%">
				<el-table-column type="expand">
					<template #default="{ row }">
						<div class="pl-10 pr-10 max-w">
							<p>用户ID：{{ row.family.userId }}</p>
							<p>用户名称：{{ row.family.name }}</p>
							<p>用户部门：{{ row.family.department }}</p>
							<p>操作类型：{{ row.family.type }}</p>
							<p class="normalText">主要内容：{{ row.family.content }}</p>
							<p>状态：{{ row.family.state }}</p>
							<p>状态信息：{{ row.family.msg }}</p>
							<p>操作时间：{{ row.family.date }}</p>
						</div>
					</template>
				</el-table-column>
				<el-table-column label="出图内容" prop="topic" showOverflowTooltip />
				<el-table-column label="状态" prop="state" width="60" />
				<el-table-column label="时间" prop="date" showOverflowTooltip />
			</el-table>
		</div>
	</div>
</template>

<script setup>
	import { mappingApi } from '@/views/exportMap/api.js'
	const api = mappingApi()

	import { useAppStore } from '@/store/modules/app.js'
	const store = useAppStore()

	const ruleFormRef = ref(null)
	const formData = ref({
		current: 0,
		size: 0,
		startTime: '',
		endTime: '',
		like_status: '',
		like_content: '',
		like_userId: '',
		like_username: ''
	})
	const tableData = ref([])

	onMounted(() => {
		initData()
	})

	const initData = () => {
		formData.value.like_userId = store.userInfo?.userId
		formData.value.like_username = store.userInfo?.username || 'system'
		api
			.logQuery(formData.value)
			.then(res => {
				if (res.resultCode === 'S') {
					if (res?.data?.length > 0) {
						res?.data?.forEach(item => {
							const content = JSON.parse(item.content)
							const obj = {
								state: '成功',
								date: item.createTime,
								topic: item.topic,
								family: {
									userId: item.userId,
									department: content.unit,
									date: content.createTime,
									name: content.title,
									state: '成功',
									type: '打印',
									msg: '请求成功',
									content: item.content
								}
							}

							tableData.value.push(obj)
						})
					}
				}
			})
			.catch(err => {
				// ElMessage({ message: err, type: 'error' })
			})
	}

	// 导出
	const exportHandler = () => {
		api
			.logExport(formData.value)
			.then(res => {
				ElMessage({ message: '导出成功', type: 'success' })
				const blob = new Blob([res.data], {
					type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
				})
				const downloadUrl = window.URL.createObjectURL(blob)
				const link = document.createElement('a')
				link.href = downloadUrl
				link.download = '出图日志.xlsx'
				document.body.appendChild(link)
				link.click()
				document.body.removeChild(link)
				window.URL.revokeObjectURL(downloadUrl)
			})
			.catch(err => {
				// ElMessage({ message: err, type: 'error' })
			})
	}
	// 重置
	const resetForm = formEl => {
		if (!formEl) return
		formEl.resetFields()
	}

	defineExpose({
		formData
	})
</script>

<style lang="scss" scoped>
	.normalText {
		word-break: break-all;
		word-wrap: break-word;
		overflow-wrap: break-word;
		white-space: normal;
	}
</style>
