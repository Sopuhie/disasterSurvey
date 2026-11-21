<template>
	<div style="height: 100%" id="pdf"></div>
</template>

<script setup>
	import jsPreviewPdf from '@js-preview/pdf'

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
		myPreviewer = jsPreviewPdf.init(document.getElementById('pdf'), {
			onError: e => {
				console.log('发生错误', e)
			},
			onRendered: () => {
				console.log('渲染完成')
			}
		})
		myPreviewer.preview(props.fileUrl)
	}
</script>

<style lang="scss" scoped></style>
