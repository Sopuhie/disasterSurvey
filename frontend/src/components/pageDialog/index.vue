<template>
	<div class="pageDialog pFixed">
		<el-dialog
			v-model="dialogVisible"
			:modal="modal"
			:title="title"
			:width="width"
			:show-close="false"
			:destroy-on-close="true"
			:close-on-click-modal="closeOnClickModal"
			:style="{
				width: width + 'px !important',
				right: right + 'px',
				top: top + 'px'
			}"
			draggable
			@close="close">
			<template #header>
				<div class="pageDialog_header flex fa jsb">
					<span>{{ title }}</span>
					<el-icon @click="close"><Close /></el-icon>
				</div>
			</template>
			<div :style="{ height: height + 'px' }">
				<slot></slot>
			</div>
			<template #footer v-if="footer">
				<div class="dialog-footer flex fa jsb">
					<p></p>
					<p>
						<el-button v-btn @click="dialogVisible = false"> {{ cancelText }} </el-button>
						<el-button v-btn type="primary" @click="ok"> {{ confirmText }} </el-button>
					</p>
				</div>
			</template>
		</el-dialog>
	</div>
</template>

<script setup>
	const router = useRouter()

	const props = defineProps({
		visible: {
			type: Boolean,
			default: false
		},
		title: {
			type: String,
			default: '标题'
		},
		width: {
			type: Number,
			default: 300
		},
		height: {
			type: Number,
			default: 300
		},
		left: {
			type: Number,
			default: 0
		},
		right: {
			type: Number,
			default: 0
		},
		top: {
			type: Number,
			default: 64
		},
		modal: {
			type: Boolean,
			default: false
		},
		footer: {
			type: Boolean,
			default: true
		},
		cancelText: {
			type: String,
			default: '取消'
		},
		confirmText: {
			type: String,
			default: '确认'
		},
		closeOnClickModal: {
			type: Boolean,
			default: true
		}
	})
	const emit = defineEmits(['submit'])

	const dialogVisible = ref(true)

	function close() {
		dialogVisible.value = false
		router.push('/')
	}
	function ok() {
		emit('submit')
	}
</script>

<style lang="scss" scoped>
	.pageDialog {
		& .pageDialog_header {
			padding: 0 16px;
			height: 56px;
			border-bottom: 1px solid #f0f0f0;
			& i {
				cursor: pointer;
			}
		}
		& .dialog-footer {
			height: 56px;
			padding: 0 16px;
			border-top: 1px solid #f0f0f0;
		}
	}
</style>
