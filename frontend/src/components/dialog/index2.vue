<template>
	<div
		:id="id"
		class="zDialog drag parentLimitation popBg pb-10"
		:style="{ zIndex: 3000, ...wrapStyle }"
		v-drag="{ isDrag }">
		<div class="zDialog__header noSelect">
			<slot name="header"></slot>
		</div>
		<slot></slot>
	</div>
</template>

<script setup>
	const props = defineProps({
		id: {
			type: String,
			default: ''
		},
		// 是否可拖拽
		isDrag: {
			type: Boolean,
			default: true
		},
		// 自定义宽高
		wrapStyle: {
			type: Object,
			default: function () {
				return { width: '440px', height: '500px' }
			}
		}
	})
</script>

<style lang="scss" scoped>
	.zDialog {
		will-change: transition;
		position: fixed;
		border: 1px solid #dcdfe6;
		background-color: white;
		border-radius: 8px;
	}
</style>
