<template>
	<el-dialog
		v-model="visible"
		:width="width"
		:height="height"
		:top="top"
		:align-center="alignCenter"
		:show-close="false"
		:modal="true"
		:fullscreen="fullscreen"
		:close-on-click-modal="closeOnClickModal"
		:draggable="draggable"
		:overflow="overflow"
		:destroy-on-close="destroyOnClose"
		@closed="close">
		<div class="dialogBox" :style="{ height: dialogHeight }">
			<slot></slot>
		</div>
		<template #header>
			<div class="dialogHeader flex fa jsb">
				<el-tooltip effect="dark" :content="title" placement="bottom-start" :disabled="isShow">
					<el-text ref="dialogTileRef" class="dialog_title_l" @mouseover="isShowTooltip">{{ title }}</el-text>
				</el-tooltip>
				<slot name="headerC"></slot>
				<span class="dialogHeaderClose el-iconColor flex faj cp" @click="close">
					<el-icon v-if="showClose"><Close /></el-icon>
				</span>
			</div>
		</template>
		<template #footer v-if="footer">
			<div class="dialog-footer">
				<el-button v-btn v-show="showCancelBtn" @click="visible = false"> {{ cancelText }} </el-button>
				<el-button v-btn v-show="showOkBtn" type="primary" @click="saveSubmit">
					{{ submitText }}
				</el-button>
			</div>
		</template>
	</el-dialog>
</template>

<script setup>
	const dialogTileRef = ref(null)
	const visible = ref(false)
	const isShow = ref(true)

	const props = defineProps({
		// 是否显示
		show: {
			type: Boolean,
			default: false
		},
		// 是否需要遮罩层
		modal: {
			type: Boolean,
			default: true
		},
		// 是否显示取消按钮
		showCancelBtn: {
			type: Boolean,
			default: true
		},
		// 是否显示确定按钮
		showOkBtn: {
			type: Boolean,
			default: true
		},
		// 是否可以拖拽
		draggable: {
			type: Boolean,
			default: true
		},
		// 是否可以溢出
		overflow: {
			type: Boolean,
			default: false
		},
		// 是否显示关闭按钮
		showClose: {
			type: Boolean,
			default: true
		},
		// 是否显示footer
		footer: {
			type: Boolean,
			default: true
		},
		// margin-top 值，alignCenter=true时无效
		top: {
			type: String,
			default: '80px'
		},
		// 是否水平垂直对齐对话框
		alignCenter: {
			type: Boolean,
			default: true
		},
		// 是否可以通过点击 modal 关闭 Dialog
		closeOnClickModal: {
			type: Boolean,
			default: true
		},
		// 当关闭 Dialog 时，销毁其中的元素
		destroyOnClose: {
			type: Boolean,
			default: true
		},
		fullscreen: {
			type: Boolean,
			default: false
		},
		// 确定按钮文字
		submitText: {
			type: String,
			default: '确定'
		},
		// 取消按钮文字
		cancelText: {
			type: String,
			default: '取消'
		},
		// 标题
		title: {
			type: String,
			default: '提示'
		},
		// 宽度
		width: {
			type: String,
			default: '500px'
		},
		// 高度
		height: {
			type: String,
			default: ''
		}
	})

	const dialogHeight = computed(() => {
		let height = parseInt(props.height, 10)
		console.log('height:', height)

		const winHeight = window.innerHeight
		// 内容区域最大高度
		// 80+56+65+80
		let boxHeight = props.footer ? winHeight - 281 : winHeight - 216
		if (isNaN(height)) {
			return ''
		} else {
			// 有设置高度，props.height !==''
			if (height > boxHeight) {
				return boxHeight + 'px'
			} else {
				return height + 'px'
			}
		}
	})

	const emit = defineEmits(['save'])

	// title的文字超出才显示tooltips
	const isShowTooltip = () => {
		const span = dialogTileRef.value
		const overflow = span.scrollWidth > span.clientWidth
		isShow.value = !overflow
	}
	// 打开
	const show = (data = true) => {
		visible.value = data
	}
	// 提交
	const saveSubmit = () => {
		emit('save', false)
	}
	// 关闭
	const close = () => {
		visible.value = false
	}

	defineExpose({
		show,
		close
	})
</script>

<style lang="scss" scoped>
	.dialogHeader {
		position: relative;
		height: 100%;
		& .dialog_title_l {
			max-width: 128px;
			font-weight: bold;
			overflow: hidden;
			white-space: nowrap;
			text-overflow: ellipsis;
		}
		& .dialogHeaderClose {
			height: 100%;
			width: 48px;
		}
	}
	.dialogBox {
		position: relative;
		overflow: hidden;
		overflow-y: auto;
	}
</style>
