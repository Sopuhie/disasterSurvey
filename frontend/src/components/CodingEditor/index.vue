<template>
	<div ref="monacoEditorRef" class="monaco-editor" style="width: 100%; height: 100%"></div>
</template>

<script setup>
	import * as monaco from 'monaco-editor/esm/vs/editor/editor.api'
	import * as xml from 'monaco-editor/esm/vs/basic-languages/xml/xml'

	let editor
	const monacoEditorRef = ref(null)

	const props = defineProps({
		value: {
			type: String,
			default: '<xml>\n\t<node>Hello XML</node>\n</xml>'
		},
		language: {
			type: String,
			default: 'xml'
		}
	})

	const editorValue = ref(props.value)
	const emit = defineEmits(['update:value'])

	watch(
		() => props.value,
		newValue => {
			editorValue.value = newValue
			editor.setValue(newValue)
		}
	)

	onMounted(() => {
		monaco.languages.register({ id: 'xml' })
		monaco.languages.setLanguageConfiguration('xml', xml.conf)
		monaco.languages.setMonarchTokensProvider('xml', xml.language)
		// 注册 XML 格式化程序
		monaco.languages.registerDocumentFormattingEditProvider('xml', {
			provideDocumentFormattingEdits: function (model) {
				return formatters.format(model.getValue(), null, null)
			}
		})

		// 创建编辑器
		editor = monaco.editor.create(monacoEditorRef.value, {
			value: props.value,
			language: props.language,
			// language: 'xml',
			theme: 'vs-dark',
			automaticLayout: true,
			wordWrap: 'on', // 自动换行
			minimap: {
				enabled: false
			},
			tabSize: 2, // 设置 tabSize 为 2
			autoIndent: true, // 自动缩进
			folding: false, // 代码折叠
			fontSize: 14,
			readOnly: true, // 是否只读
			selectOnLineNumbers: true, // 显示行号
			formatOnPaste: true, // 粘贴时自动格式化
			formatOnType: true, // 输入时自动格式化
			suggestOnTriggerCharacters: true, // 触发字符时显示建议
			codeLens: true // 代码提示
		})
		editorValue.value = props.value
		editor.setValue(props.value)
		editor.onDidChangeModelContent(() => {
			const newValue = editor.getValue()
			editorValue.value = newValue
			emit('update:value', newValue)
		})
	})

	defineExpose({ editorValue })
</script>
