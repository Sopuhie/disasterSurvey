<!-- 模板添加与删除 -->
<template>
	<ZDialogCom
		:title="exportMapStore.saveType === 'add' ? '模板制作' : '模板编辑'"
		toPath="cancel"
		:isDrag="false"
		:footer="false"
		:wrapStyle="wrapStyle"
		:bodyStyle="{ padding: '0px' }"
		@cancel="closeHandler">
		<template #close>
			<el-button v-btn type="primary" size="small" @click="saveData">保存</el-button>
			<el-button v-btn type="primary" size="small" class="mr-12" @click="showPreviewPanel">预览</el-button>
		</template>
		<div class="max-wh flex">
			<EditPanelCom></EditPanelCom>
			<MapPanelCom></MapPanelCom>
		</div>
	</ZDialogCom>
</template>

<script setup>
	import ZDialogCom from '@/components/dialog/index.vue'
	import EditPanelCom from '@/views/exportMap/modules/editPanel.vue'
	import MapPanelCom from '@/views/exportMap/modules/mapPanel.vue'
	import { mappingApi } from '@/views/exportMap/api.js'
	const api = mappingApi()
	import { useExportMapStore } from '@/store/modules/exportMap.js'
	const exportMapStore = useExportMapStore()

	const wrapStyle = ref({
		width: '100%',
		height: '100%',
		left: 0,
		top: 0
	})
	const emits = defineEmits(['cancel'])
	// 保存模板
	const saveData = () => {
		if (!exportMapStore.formData.name) {
			ElMessage({
				message: '请添加模板命名!',
				type: 'error'
			})
			return false
		}
		const userInfo = JSON.parse(localStorage.getItem('userInfo'))
		let obj = {
			isShare: '0',
			content: JSON.stringify(exportMapStore.formData),
			name: exportMapStore.formData.name
		}
		if (exportMapStore.saveType === 'add') {
			obj.cjr = userInfo?.username || 'system'

			api
				.add(obj)
				.then(res => {
					if (res.resultCode === 'S') {
						ElMessage({
							message: '模板保存成功!',
							type: 'success'
						})
						emits('cancel')
					}
				})
				.catch(err => {
					ElMessage({
						message: '模板保存失败!' + err,
						type: 'error'
					})
				})
		}
		if (exportMapStore.saveType === 'edit') {
			obj.id = exportMapStore.formData.id
			obj.zhgxr = userInfo?.username || 'system'
			api
				.update(obj)
				.then(res => {
					if (res.resultCode === 'S') {
						ElMessage({
							message: '模板保存成功!',
							type: 'success'
						})
						emits('cancel')
					}
				})
				.catch(err => {
					ElMessage({
						message: '模板保存失败!' + err,
						type: 'error'
					})
				})
		}
	}
	const showPreviewPanel = val => {
		exportMapStore.setPreview(true)
	}
	/**
	 * 关闭弹窗
	 */
	const closeHandler = () => {
		exportMapStore.setPreview(false)
		emits('cancel')
	}
</script>
<style lang="scss" scoped></style>
