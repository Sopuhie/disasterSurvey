<!-- 灾害评估 -->
<template>
	<div class="assessment">
		<ZDialogCom2 v-show="!store.leftPanelCollapsed" :wrapStyle="leftStyle" :isDrag="false">
			<div class="wb-shell">
				<div class="wb-head assess-left-head">
					<div>
						<h3>{{ store.typeConfig.label }}评估</h3>
						<p>{{ currentStepMeta?.label }} · {{ currentStepMeta?.desc }}</p>
					</div>
					<span class="assess-left-collapse flex faj cp" title="收起面板" @click="setLeftCollapsed(true)">
						<el-icon :size="16"><DArrowLeft /></el-icon>
					</span>
				</div>
				<div class="wb-types">
					<button
						v-for="item in types"
						:key="item.id"
						class="wb-type"
						:class="{ active: store.currentType === item.id }"
						@click="switchType(item)">
						{{ item.label }}
					</button>
				</div>
				<div class="wb-steps">
					<button
						v-for="item in store.steps"
						:key="item.id"
						class="wb-step"
						:class="{
							active: store.currentStep === item.id,
							done: item.index < currentStepMeta.index
						}"
						@click="goStep(item.id)">
						<span class="wb-step__idx">{{ item.index }}</span>
						<span class="wb-step__label">{{ item.label }}</span>
					</button>
				</div>
				<div class="wb-body">
					<IntegratePanel v-if="store.currentType === 'storm' && store.currentStep === 'integrate'" />
					<LossPanel v-else-if="store.currentType === 'storm' && store.currentStep === 'loss'" />
					<DamagePanel v-else-if="store.currentStep === 'damage'" />
					<BehaviorPanel v-else-if="store.currentStep === 'behavior'" />
					<ReportPanel v-else />
				</div>
				<div class="wb-foot" v-if="nextStep">
					<el-button class="max-w" type="primary" plain @click="goStep(nextStep.id)">
						下一步：{{ nextStep.label }}
					</el-button>
				</div>
			</div>
		</ZDialogCom2>
		<button
			v-show="store.leftPanelCollapsed"
			type="button"
			class="assess-left-dock"
			:style="leftDockStyle"
			title="展开灾害评估"
			@click="setLeftCollapsed(false)">
			灾害评估
		</button>
		<ZDialogCom
			:title="`${store.typeConfig.label}评估结果`"
			:isDrag="false"
			:footer="false"
			:closeBtn="false"
			toPath="cancel"
			showSwitch
			dockOnCollapse
			dockText="评估结果"
			:wrapStyle="rightStyle"
			:bodyStyle="{ padding: '16px 18px 20px' }">
			<ResultPanel />
		</ZDialogCom>
		<ReportPreview />
		<StormDetailDialog />
	</div>
</template>

<script setup>
	import ZDialogCom from '@/components/dialog/index.vue'
	import ZDialogCom2 from '@/components/dialog/index2.vue'
	import IntegratePanel from './components/IntegratePanel.vue'
	import LossPanel from './components/LossPanel.vue'
	import DamagePanel from './components/DamagePanel.vue'
	import BehaviorPanel from './components/BehaviorPanel.vue'
	import ReportPanel from './components/ReportPanel.vue'
	import ResultPanel from './components/ResultPanel.vue'
	import ReportPreview from './components/ReportPreview.vue'
	import StormDetailDialog from './components/StormDetailDialog.vue'
	import { DArrowLeft } from '@element-plus/icons-vue'
	import { ASSESS_TYPES } from './constants.js'
	import { useAppStore } from '@/store/modules/app.js'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useAssessmentActions } from './composables/useAssessmentActions.js'
	import { clearAssessmentLayers } from './utils/assessmentLayers.js'

	const appStore = useAppStore()
	const store = useAssessmentStore()
	const { initAssessment, destroyAssessment, ensureAssessmentStepResult } = useAssessmentActions()
	const types = ASSESS_TYPES

	const currentStepMeta = computed(() => store.steps.find(item => item.id === store.currentStep) || store.steps[0] || {})
	const nextStep = computed(() => store.steps.find(item => item.index === (currentStepMeta.value.index || 0) + 1))

	const setLeftCollapsed = collapsed => {
		store.setLeftPanelCollapsed(collapsed)
	}

	const panelShadow = '0 10px 28px rgba(15, 35, 68, 0.14)'
	const navWidth = computed(() => (appStore.switchObj.sidebarFlag ? 64 : 280))
	const leftStyle = computed(() => ({
		padding: '0px !important',
		width: '340px',
		height: 'calc(100% - 151px)',
		top: '95px',
		left: `${navWidth.value + 20}px`,
		overflow: 'hidden',
		borderRadius: '8px',
		border: 'none',
		boxShadow: panelShadow
	}))
	const leftDockStyle = computed(() => ({
		left: `${navWidth.value}px`
	}))
	const rightStyle = computed(() => ({
		width: '400px',
		height: 'calc(100% - 164px)',
		top: '140px',
		left: 'calc(100% - 464px)',
		borderRadius: '8px',
		overflow: 'hidden',
		boxShadow: panelShadow
	}))

	const goStep = async stepId => {
		store.setStep(stepId)
		await ensureAssessmentStepResult(stepId)
	}

	const switchType = async item => {
		if (store.currentType === item.id) return
		clearAssessmentLayers()
		store.setType(item.id)
		store.reset()
		await initAssessment(item.id)
	}

	onMounted(() => {
		appStore.setCurNavObj('灾害评估')
		store.setLeftPanelCollapsed(false)
		initAssessment(store.currentType)
	})

	onUnmounted(() => {
		store.setLeftPanelCollapsed(false)
		destroyAssessment()
	})
</script>

<style lang="scss" scoped>
	.assess-left-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 8px;
	}
	.assess-left-collapse {
		flex-shrink: 0;
		width: 28px;
		height: 28px;
		margin-top: 2px;
		border-radius: 6px;
		color: #5c6b7a;
		&:hover {
			color: #1677ff;
			background: #eef6ff;
		}
	}
	.assess-left-dock {
		position: fixed;
		top: 38%;
		z-index: 3001;
		transform: translateY(-50%);
		writing-mode: vertical-rl;
		letter-spacing: 4px;
		padding: 16px 10px;
		border: none;
		border-radius: 0 8px 8px 0;
		cursor: pointer;
		color: #fff;
		background: #1677ff;
		box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
		font-size: 14px;
		line-height: 1.4;
		&:hover {
			background: #4096ff;
		}
	}
</style>
