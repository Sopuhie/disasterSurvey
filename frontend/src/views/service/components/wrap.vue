<template>
	<div class="serviceWrap">
		<div class="title pl-10 pr-10 flex fa jsb">
			<el-text>{{ title }}</el-text>
			<p>
				<slot name="title"></slot>
			</p>
		</div>
		<div class="p-10">
			<el-input v-model="inputValue" placeholder="输入关键字进行过滤" clearable>
				<template #suffix>
					<el-icon class="el-input__icon"><Search /></el-icon>
				</template>
			</el-input>
		</div>
		<slot name="tree"></slot>
	</div>
</template>

<script setup>
	const inputValue = ref('')
	defineProps({
		title: {
			type: String,
			default: '标题'
		},
		modelValue: {
			type: String,
			default: ''
		}
	})

	const emit = defineEmits(['update:modelValue'])

	watch(inputValue, newVal => {
		emit('update:modelValue', newVal)
	})
</script>

<style lang="scss">
	.serviceWrap {
		width: calc((100% - 160px) / 3);
		height: 100%;
		background-color: var(--el-bg-color);
		border: 1px solid var(--el-border-color);
		border-radius: 2px;
		overflow: hidden;
		& .title {
			height: 52px;
			background-color: var(--el-bg-color-page);
		}
		& .el-input__wrapper {
			border-radius: 2px;
		}
	}
</style>
