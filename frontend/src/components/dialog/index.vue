<template>
	<div class="zDialog__mask" :class="{ pFixed: !isNest }">
		<div
			:id="id"
			class="zDialog drag parentLimitation"
			:class="{ isSwitch, dockCollapse: dockOnCollapse && isSwitch, pFixed: !isNest }"
			:style="wrapStyle"
			v-drag="{ isDrag }">
			<div class="zDialog__header header noSelect flex fa jsb" :class="{ isDrag }" v-show="showTitle">
				<p class="header_title flex fa">
					<span
						class="switchDialog flex faj cp"
						:class="{ active: isSwitch && !dockOnCollapse }"
						v-if="showSwitch"
						@click.stop="switchHandle">
						<el-icon class="flex faj" v-if="isSwitch"><DArrowLeft /></el-icon>
						<el-icon class="flex faj" v-else><DArrowRight /></el-icon>
					</span>
					<span class="title" :class="{ 'ml-24': showSwitch }">{{ title }}</span>
				</p>
				<div class="headerCenter flex1">
					<slot name="headerC"></slot>
					<slot name="headerc"></slot>
				</div>
				<span class="flex fa cp">
					<slot name="close"></slot>
					<el-icon size="18" v-if="closeBtn" @click.stop="close"><Close /></el-icon>
				</span>
			</div>
			<div
				class="zDialog__body"
				:class="{
					max_body: !footer || !showTitle,
					maxwh_body: !footer && !showTitle
				}"
				:style="bodyStyle">
				<slot></slot>
			</div>
			<div class="zDialog__footer flex fa jsb" v-if="footer">
				<span>
					<slot name="flootLeft"></slot>
				</span>
				<span>
					<slot name="flootCenter"></slot>
				</span>
				<span>
					<el-button type="primary" plain v-btn v-if="cancelBtn" @click.stop="handleCancel">
						{{ cancelBtnTxt }}
					</el-button>
					<slot name="cancelBtn"></slot>
					<slot name="okBtn"></slot>
					<el-button v-btn type="primary" plain class="ml-12" v-if="okBtn" @click.stop="handleOk">
						{{ okBtnTxt }}
					</el-button>
				</span>
			</div>
		</div>
		<!-- 收起后右侧吸附入口（对齐意见反馈挂件） -->
		<button
			v-if="dockOnCollapse && isSwitch"
			type="button"
			class="zDialog__dock-tab"
			:style="dockTabStyle"
			@click.stop="switchHandle">
			{{ dockText || title }}
		</button>
	</div>
</template>

<script setup>
	const router = useRouter()

	// 是否收起
	const isSwitch = ref(false)

	const props = defineProps({
		id: {
			type: String,
			default: ''
		},
		// 是否嵌套
		isNest: {
			type: Boolean,
			default: false
		},
		// 是否可拖拽
		isDrag: {
			type: Boolean,
			default: true
		},
		// 是否收起
		showSwitch: {
			type: Boolean,
			default: false
		},
		/** 收起后吸附右侧竖条，点击再打开（同意见反馈） */
		dockOnCollapse: {
			type: Boolean,
			default: false
		},
		/** 吸附条文案，默认取 title */
		dockText: {
			type: String,
			default: ''
		},
		/** 吸附条 top，避免与意见反馈重叠 */
		dockTop: {
			type: String,
			default: '38%'
		},
		title: {
			type: String,
			default: '我是标题'
		},
		// 是否显示顶部
		showTitle: {
			type: Boolean,
			default: true
		},
		// 是否显示关闭按钮
		closeBtn: {
			type: Boolean,
			default: true
		},
		// 自定义宽高
		wrapStyle: {
			type: Object,
			default: function () {
				return { width: '440px', height: '500px' }
			}
		},
		titleStyle: {
			type: Object,
			default: function () {
				return { 'background-color': 'white' }
			}
		},
		// body样式
		bodyStyle: {
			type: Object,
			default: function () {
				return { 'background-color': 'white', padding: '16px 24px 0' }
			}
		},
		// 关闭后跳转的路由地址 cancel
		toPath: {
			type: String,
			default: '/'
		},
		// 是否显示取消按钮
		cancelBtn: {
			type: Boolean,
			default: true
		},
		// 取消按钮文字
		cancelBtnTxt: {
			type: String,
			default: '取消'
		},
		// 是否显示确定按钮
		okBtn: {
			type: Boolean,
			default: true
		},
		// 确定按钮文字
		okBtnTxt: {
			type: String,
			default: '确定'
		},
		// 是否显示底部按钮区
		footer: {
			type: Boolean,
			default: true
		}
	})

	const emits = defineEmits(['cancel', 'handleOk', 'switchHandle'])

	const dockTabStyle = computed(() => ({
		top: props.dockTop
	}))

	function switchHandle() {
		isSwitch.value = !isSwitch.value
		emits('switchHandle', isSwitch.value)
	}

	function setSwitch() {
		isSwitch.value = !isSwitch.value
	}

	function close() {
		if (props.toPath === 'cancel') {
			emits('cancel')
		} else {
			router.push('/')
		}
	}

	function handleCancel() {
		close()
	}
	function handleOk() {
		emits('handleOk')
	}

	defineExpose({
		isSwitch,
		setSwitch,
		close,
		switchHandle
	})
</script>

<style lang="scss" scoped>
	.zDialog__mask {
		width: 100%;
		height: 100%;
		top: 0;
		left: 0;
		z-index: 3000;
		pointer-events: none;
		background-color: transparent;
	}
	.zDialog {
		border-radius: 8px;
		box-shadow: 0px 6px 10px 6px rgba(0, 0, 0, 0.16);
		pointer-events: all;
		background-color: var(--el-bg-color);
		will-change: transition;
		&.isSwitch {
			left: 100% !important;
		}
		&.dockCollapse {
			opacity: 0;
			pointer-events: none;
			.switchDialog {
				display: none;
			}
		}

		& .zDialog__header {
			height: 44px;
			padding: 0 16px 0 24px;
			border-radius: 8px 8px 0 0;
			background: linear-gradient( 90deg, #D2EAFF 0%, #EFF7FF 100%);
			& .header_title {
				position: relative;
				height: 56px;
			}
			&.isDrag {
				cursor: move !important;
			}
			& .title {
				font-family: PingFangSC-Medium;
				font-size: 16px;
				color: var(--el-text-color-primary);
				font-weight: 600;
				display: flex;
				align-items: center;
			}
			& .headerCenter {
				flex: 1;
				min-width: 0;
				padding: 0 12px;
				overflow: hidden;
			}
			& i {
				color: var(--el-text-color-primary);
				width: 24px;
				height: 24px;
				text-align: center;
				cursor: pointer;
			}
		}
		& .zDialog__body {
			height: calc(100% - 100px);
			overflow: hidden;
			overflow-y: auto;
			cursor: default;
			&.max_body {
				height: calc(100% - 44px);
			}
			&.maxwh_body {
				height: 100%;
			}
		}
		& .zDialog__body::-webkit-scrollbar {
			width: 8px;
			height: 8px;
		}
		& .zDialog__body::-webkit-scrollbar-thumb {
			background: #c5c9d3;
			border-radius: 4px;
		}
		& .zDialog__body::-webkit-scrollbar-track {
			background: transparent;
		}
		& .zDialog__footer {
			height: 56px;
			padding: 0 16px;
			border-top: 0.01rem solid var(--el-border-color);
		}
	}

	.switchDialog {
		position: absolute;
		top: 8px;
		left: -16px;
		width: 40px;
		height: 38px;
		cursor: pointer;
		&.active {
			left: -75px;
			color: black;
			background-color: white;
			border-radius: 8px;
		}
	}

	.zDialog__dock-tab {
		position: fixed;
		right: 0;
		z-index: 3001;
		transform: translateY(-50%);
		writing-mode: vertical-rl;
		letter-spacing: 4px;
		padding: 16px 10px;
		border: none;
		border-radius: 8px 0 0 8px;
		cursor: pointer;
		color: #fff;
		background: #1677ff;
		box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
		font-size: 14px;
		line-height: 1.4;
		pointer-events: all;
		&:hover {
			background: #4096ff;
		}
	}
</style>
