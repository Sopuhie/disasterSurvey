<!-- 功能配置 -->
<template>
	<div class="config max-wh">
		<div id="editor" class="max-wh" ref="editorRef"></div>
	</div>
</template>
<script setup>
	let editor = null

	const editorRef = ref(null)
	const editorJson = ref(null)
	const jsonData = ref({})

	const props = defineProps({
		itemData: {
			type: Object,
			default: () => {
				return null
			}
		}
	})

	onMounted(() => {
		initEditor()
		// Api地址：https://github.com/josdejong/jsoneditor/blob/master/docs/api.md
		// editor.set();
		// editor.get();
		// editor.setMode(); // tree, view, form, code, text, view
		// editor.setSchema({});
		// editor.setText();
		// editor.update();
		// editor.validate(); // 验证错误
		// editor.validate();
		// editor.destroy();
	})
	onUnmounted(() => {
		editor = null
	})

	const initEditor = () => {
		if (props.itemData) {
			const configure = props.itemData['configure']
			jsonData.value = JSON.parse(configure)
		}
		editor = new JSONEditor(editorRef.value, {
			modes: ['tree', 'view', 'form', 'code', 'text'],
			mode: 'code',
			mainMenuBar: true,
			language: 'zh-CN',
			schema: {
				type: 'object',
				properties: {
					name: { type: 'string' }
				}
			},
			indentation: 2,
			onChange: function () {
				editorJson.value = editor.get()
			},
			onError: function (err) {}
		})
		editor.set(jsonData.value)
	}
	const getJsonData = () => {
		editorJson.value = editor.get()
		return editorJson.value
	}
	defineExpose({ editorJson, getJsonData })
</script>

<style lang="scss" scoped></style>
