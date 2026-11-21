<template>
	<div style="height: 100%" id="excel"></div>
</template>

<script setup>
	import jsPreviewExcel from '@js-preview/excel'
	import '@js-preview/excel/lib/index.css'

	let myPreviewer = null

	const props = defineProps({
		fileUrl: {
			type: String,
			default: ''
		}
	})
	watch(
		() => props.fileUrl,
		() => {
			if (myPreviewer) {
				myPreviewer.preview(props.fileUrl)
			}
		}
	)

	onMounted(() => {
		init()
	})

	const init = () => {
		myPreviewer = jsPreviewExcel.init(document.getElementById('excel'))
		myPreviewer
			.preview(props.fileUrl)
			.then(res => {
				console.log('预览完成')
			})
			.catch(e => {
				console.log('预览失败', e)
			})
	}
</script>

<style lang="scss" scoped></style>
