<template>
	<div style="height: 100%" id="docx"></div>
</template>

<script setup>
	import jsPreviewDocx from '@js-preview/docx'
	import '@js-preview/docx/lib/index.css'

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
		myPreviewer = jsPreviewDocx.init(document.getElementById('docx'))
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
