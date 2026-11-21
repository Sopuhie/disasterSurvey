<!-- 灾情调查 -->
<template>
	<div class="survey">
		<ZDialogCom2 v-show="!surveyStore.leftPanelCollapsed" :wrapStyle="leftStyle" :isDrag="false">
			<div class="wb-shell">
				<div class="wb-head survey-left-head">
					<div>
						<h3>灾情调查</h3>
						<p>{{ currentStepMeta?.label }} · {{ currentStepMeta?.desc }}</p>
					</div>
					<span class="survey-left-collapse flex faj cp" title="收起面板" @click="setLeftCollapsed(true)">
						<el-icon :size="16"><DArrowLeft /></el-icon>
					</span>
				</div>
				<div class="wb-steps">
					<button
						v-for="item in steps"
						:key="item.id"
						class="wb-step"
						:class="{
							active: surveyStore.currentStep === item.id,
							done: item.index < currentStepMeta.index
						}"
						@click="goStep(item.id)">
						<span class="wb-step__idx">{{ item.index }}</span>
						<span class="wb-step__label">{{ item.label }}</span>
					</button>
				</div>
				<div class="wb-body">
					<QueryPanel v-if="surveyStore.currentStep === 'query'" />
					<IdentifyPanel v-else-if="surveyStore.currentStep === 'identify'" />
					<OverlayPanel v-else-if="surveyStore.currentStep === 'overlay'" />
					<ComparePanel v-else />
				</div>
				<div class="wb-foot" v-if="nextStep">
					<el-button class="max-w" type="primary" plain @click="goStep(nextStep.id)">
						下一步：{{ nextStep.label }}
					</el-button>
				</div>
			</div>
		</ZDialogCom2>
		<button
			v-show="surveyStore.leftPanelCollapsed"
			type="button"
			class="survey-left-dock"
			:style="leftDockStyle"
			title="展开灾情调查"
			@click="setLeftCollapsed(false)">
			灾情调查
		</button>
		<ZDialogCom
			title="调查结果"
			:isDrag="false"
			:footer="false"
			:closeBtn="false"
			toPath="cancel"
			showSwitch
			dockOnCollapse
			dockText="调查结果"
			:wrapStyle="rightStyle"
			:bodyStyle="{ padding: '16px 18px 20px' }">
			<ResultPanel />
		</ZDialogCom>
	</div>
</template>

<script setup>
	import ZDialogCom from '@/components/dialog/index.vue'
	import ZDialogCom2 from '@/components/dialog/index2.vue'
	import QueryPanel from './components/QueryPanel.vue'
	import IdentifyPanel from './components/IdentifyPanel.vue'
	import OverlayPanel from './components/OverlayPanel.vue'
	import ComparePanel from './components/ComparePanel.vue'
	import ResultPanel from './components/ResultPanel.vue'
	import { DArrowLeft } from '@element-plus/icons-vue'
	import { SURVEY_STEPS } from './constants.js'
	import { useAppStore } from '@/store/modules/app.js'
	import { useSurveyStore } from '@/store/modules/survey.js'
	import { useSurveyActions } from './composables/useSurveyActions.js'

	const appStore = useAppStore()
	const surveyStore = useSurveyStore()
	const { initSurvey, destroySurvey, ensureSurveyStepResult } = useSurveyActions()
	const steps = SURVEY_STEPS

	const currentStepMeta = computed(() => steps.find(item => item.id === surveyStore.currentStep) || steps[0])
	const nextStep = computed(() => steps.find(item => item.index === currentStepMeta.value.index + 1))

	const setLeftCollapsed = collapsed => {
		surveyStore.setLeftPanelCollapsed(collapsed)
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
	/** 吸附在左侧菜单栏右缘 */
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
		surveyStore.setStep(stepId)
		await ensureSurveyStepResult(stepId)
	}

	onMounted(() => {
		appStore.setCurNavObj('灾情调查')
		surveyStore.setLeftPanelCollapsed(false)
		initSurvey()
	})

	onUnmounted(() => {
		surveyStore.setLeftPanelCollapsed(false)
		destroySurvey()
	})
</script>

<style lang="scss" scoped>
	.survey-left-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 8px;
	}
	.survey-left-collapse {
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
	.survey-left-dock {
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
