<!-- 专题图输出 -->
<template>
	<div class="add-site">
		<div class="head zDialog__header flex fa jsb pl-10 pr-10">
			<span class="title">专题图输出</span>
			<p class="flex fa cp">
				<el-icon @click="close"><Close /></el-icon>
			</p>
		</div>
		<div class="p-10">
			<el-form ref="outputFormRef" :model="formData" label-width="auto">
				<el-form-item label="格式：">
					<el-select v-model="formData.suffix">
						<el-option v-for="item in suffixOptions" :key="item.value" :label="item.label" :value="item.value" />
					</el-select>
				</el-form-item>
				<el-form-item label="分辨率：">
					<el-select v-model="formData.resolution">
						<el-option v-for="item in resolutionOptions" :key="item.value" :label="item.label" :value="item.value" />
					</el-select>
				</el-form-item>
				<el-form-item label="文件名：">
					<el-input v-model="formData.fileName" clearable placeholder="请输入名称" />
				</el-form-item>
				<el-form-item label=" ">
					<el-button type="primary" @click="save(outputFormRef)">确认</el-button>
					<el-button @click="close">取消</el-button>
				</el-form-item>
			</el-form>
		</div>
	</div>
</template>

<script setup>
	import { useExportStore } from '@/store/modules/export.js'
	const exportStore = useExportStore()

	const outputFormRef = ref(null)
	const formData = ref({
		suffix: 'jpg',
		resolution: '960x679',
		fileName: '水质差值分析专题图'
	})
	const suffixOptions = [
		{ label: 'jpg', value: 'jpg' },
		{ label: 'png', value: 'png' },
		{ label: 'pdf', value: 'pdf' }
	]
	const resolutionOptions = [
		{ label: '横图', value: '960x679' },
		{ label: '竖图', value: '476x671' }
	]

	const emits = defineEmits(['close', 'save'])
	const close = () => {
		emits('close')
	}
	const save = formRef => {
		formRef.validate(valid => {
			if (valid) {
				exportStore.setIsExportInfo(unref(formData))
				emits('save', formData)
			}
		})
	}
</script>

<style lang="scss" scoped>
	.add-site {
		position: fixed;
		top: calc(50% - 150px);
		left: calc(50% - 180px);
		z-index: 4;
		width: 360px;
		height: 300px;
		border-radius: 4px;
		background-color: #fff;
		& .head {
			height: 52px;
			font-size: 18px;
			padding: 0 10px;
			color: white;
			background-color: #3c7dd7;
		}
	}
</style>
