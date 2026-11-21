<!-- 专题图制作 -->
<template>
	<ZDialogCom
		ref="zDialogRef"
		title="在线制图"
		:isDrag="true"
		:okBtn="false"
		:wrapStyle="wrapStyle"
		:bodyStyle="{ padding: '16px' }"
		@cancel="handleCancel">
		<div class="max-wh oy noSelect">
			<Step1 v-if="step === 1" @typeChange="typeChange"></Step1>
			<Step2 v-if="step === 2"></Step2>
		</div>
		<template #okBtn>
			<el-button v-btn type="primary" @click.stop="handleStep">
				{{ step === 1 ? '下一步' : '上一步' }}
			</el-button>
			<el-button v-btn type="primary" v-if="step === 2" @click.stop="handleOk">打印下载</el-button>
		</template>
	</ZDialogCom>
	<div class="thematicMap_mask max-wh pageFixed">
		<div class="mapBoxWrap" :style="printSize">
			<div class="mapBox max-wh noSelect"></div>
		</div>
	</div>
</template>

<script setup>
	import { getPrintSize } from './index.js'
	import ZDialogCom from '@/components/dialog/index.vue'
	import Step1 from './components/step1.vue'
	import Step2 from './components/step2.vue'

	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()

	const wrapStyle = reactive({
		width: '392px',
		height: 'calc(100% - 106px)',
		top: '64px',
		left: '8px'
	})
	// 打印纸相关
	const printDirection = ref('h')
	const printSize = computed(() => {
		const baseWidth = 750
		let obj = { width: '0px', height: '0px', left: '0px', top: '0px' }
		const width = getPrintSize(baseWidth, printDirection.value).width
		const height = getPrintSize(baseWidth, printDirection.value).height
		obj.width = width + 'px'
		obj.height = height + 'px'
		obj.left = `calc(50% - ${width / 2}px)`
		obj.top = `calc(50% - ${height / 2}px)`
		return obj
	})

	const step = ref(2)

	onMounted(() => {
		appStore.setSwitchObj('sidebarShow', false)
		appStore.setSwitchObj('sidebarSwitch', true)
		appStore.setSwitchObj('sidebarFlag', true)
		// initMap()
		getPrintSize(600, 'h')
		getPrintSize(600, 'v')
	})

	/**
	 * 确定
	 */
	const handleOk = () => {}
	const typeChange = direction => {
		printDirection.value = direction
	}
	/**
	 * 切换步骤
	 */
	const handleStep = () => {
		step.value = step.value === 1 ? 2 : 1
	}
	/**
	 * 取消
	 */
	const handleCancel = () => {
		appStore.setSwitchObj('sidebarShow', true)
	}
	/**
	 * 组件销毁时恢复侧边栏显示
	 */
	onBeforeUnmount(() => {
		appStore.setSwitchObj('sidebarShow', true)
	})
</script>

<style lang="scss" scoped>
	.thematicMap_mask {
		z-index: 2021 !important;
		overflow: hidden;
		background-color: rgba(255, 255, 255, 0.6);
		& .mapBoxWrap {
			position: absolute;
			border-radius: 4px;
			border: 16px solid #fff;
			background-color: #fff;
			box-shadow: 0px 2px 10px 0px rgba(7, 18, 56, 0.2);
			& .mapBox {
				border: 1px solid #e8e8e8;
				background-color: transparent;
			}
		}
	}
</style>
