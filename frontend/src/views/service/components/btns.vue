<template>
	<div class="serviceBtns flex faj fc fw">
		<el-tooltip :content="text1" placement="top">
			<el-button type="primary" :disabled="btnObj.top" circle plain @click="handleBind">
				<el-icon class="el-icon--center"><ArrowRightBold /></el-icon>
			</el-button>
		</el-tooltip>
		<el-tooltip :content="text2" placement="bottom">
			<el-button type="primary" :disabled="btnObj.bottom" circle plain @click="handleUnbind">
				<el-icon class="el-icon--center"><ArrowLeftBold /></el-icon>
			</el-button>
		</el-tooltip>
	</div>
</template>

<script setup>
	defineProps({
		text1: {
			type: String,
			default: '向右'
		},
		text2: {
			type: String,
			default: '向左'
		},
		btnObj: {
			type: Object,
			default: () => {
				return { top: false, bottom: false }
			}
		}
	})
	const emit = defineEmits(['handleBind', 'handleUnbind'])

	function handleBind() {
		emit('handleBind')
	}
	function handleUnbind() {
		emit('handleUnbind')
	}
</script>

<style lang="scss" scoped>
	.serviceBtns {
		width: 72px;
	}
	:deep(.el-button.is-circle) {
		width: 26px;
		height: 26px;
	}
	:deep(.el-button + .el-button) {
		margin-left: 0;
		margin-top: 16px;
	}
</style>
