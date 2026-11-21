<template>
	<iframe
		v-if="fileType !== 'txt'"
		:src="fileUrl"
		frameborder="0"
		style="width: 100%; height: 100%"></iframe>
	<div v-else class="txt-content p-16" v-html="fileContent"></div>
</template>

<script setup>
	import axios from 'axios'

	const fileContent = ref('')
	const props = defineProps({
		fileUrl: {
			type: String,
			default: ''
		},
		fileType: {
			type: String,
			default: ''
		}
	})

	watch(
		() => props.fileUrl,
		value => {
			fetchTextFile(value)
		}
	)
	onMounted(() => {
		fetchTextFile(props.fileUrl)
	})

	const fetchTextFile = url => {
		axios
			.get(url, { responseType: 'text' })
			.then(response => {
				fileContent.value = response.data.replace(/\n|\r\n/g, '<br/>')
			})
			.catch(error => {
				ElMessage.error('Error fetching text fileP:' + error)
			})
	}
</script>

<style lang="scss" scoped></style>
