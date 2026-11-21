<!-- 灾害评估 -->
<template>
	<div class="assessment">
		<ZDialogCom2 v-show="!store.leftPanelCollapsed" :wrapStyle="leftStyle" :isDrag="false">
			<div class="wb-shell">
				<div class="wb-head assess-left-head">
					<div>
						<h3>{{ store.typeConfig.label }}评估</h3>
						<p>{{ currentStepMeta?.label }} · {{ currentStepMeta?.desc }}</p>
						<p v-if="eventHint" class="assess-event-hint">{{ eventHint }}</p>
					</div>
					<span class="assess-left-collapse flex faj cp" title="收起面板" @click="setLeftCollapsed(true)">
						<el-icon :size="16"><DArrowLeft /></el-icon>
					</span>
				</div>
				<div v-if="showBaselineGate" class="assess-baseline-gate">
					<p>风暴潮经济损失 / 综合评估需要已确认的调查基线。</p>
					<div class="assess-baseline-gate__actions">
						<el-button type="primary" size="small" @click="goSurvey">前往完成调查</el-button>
						<el-button size="small" @click="pickConfirmedEvent" :loading="pickingEvent">选择历史已确认事件</el-button>
					</div>
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
	import { ElMessage, ElMessageBox } from 'element-plus'
	import { useRoute, useRouter } from 'vue-router'
	import { BaselineStatusLabel } from '@/constants/disasterEnums.js'
	import { ASSESS_TYPES, resolveTypeFromRoute } from './constants.js'
	import { useAppStore } from '@/store/modules/app.js'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useAssessmentActions } from './composables/useAssessmentActions.js'
	import { clearAssessmentLayers } from './utils/assessmentLayers.js'

	const route = useRoute()
	const router = useRouter()
	const appStore = useAppStore()
	const store = useAssessmentStore()
	const { initAssessment, destroyAssessment, ensureAssessmentStepResult, workflowStore } = useAssessmentActions()
	const types = ASSESS_TYPES
	const pickingEvent = ref(false)

	const currentStepMeta = computed(() => store.steps.find(item => item.id === store.currentStep) || store.steps[0] || {})
	const nextStep = computed(() => store.steps.find(item => item.index === (currentStepMeta.value.index || 0) + 1))
	const eventHint = computed(() => {
		const name = workflowStore.currentEvent?.eventName || workflowStore.currentEventId
		if (!name) return '未绑定灾害事件'
		const st = workflowStore.currentBaseline?.status
		const stLabel = st ? BaselineStatusLabel[st] || st : '未确认'
		return `事件：${name} · 基线：${stLabel}`
	})
	const showBaselineGate = computed(() => {
		const needBaseline =
			store.currentType === 'storm' && (store.currentStep === 'loss' || store.currentStep === 'report')
		return needBaseline && !workflowStore.isBaselineConfirmed
	})

	const goSurvey = () => router.push('/survey')

	const pickConfirmedEvent = async () => {
		pickingEvent.value = true
		try {
			const { list } = await workflowStore.listEvents({ status: 'ACTIVE', size: 50 })
			const options = (list || []).filter(Boolean)
			if (!options.length) {
				ElMessage.warning('暂无可用灾害事件，请先新建并完成调查')
				return
			}
			const labels = options.map(
				(item, idx) => `${idx + 1}. ${item.eventName || item.id}${item.regionName ? `（${item.regionName}）` : ''}`
			)
			const { value } = await ElMessageBox.prompt(
				`请输入序号选择事件：\n${labels.slice(0, 12).join('\n')}`,
				'选择历史事件',
				{
					confirmButtonText: '绑定',
					cancelButtonText: '取消',
					inputPattern: /^[1-9]\d*$/,
					inputErrorMessage: '请输入有效序号'
				}
			)
			const picked = options[Number(value) - 1]
			if (!picked?.id && !picked?.eventId) {
				ElMessage.warning('无效选择')
				return
			}
			await workflowStore.bindExistingEvent(picked.id || picked.eventId)
			if (!workflowStore.isBaselineConfirmed) {
				ElMessage.warning('该事件调查基线尚未确认，可前往灾情调查完成锁定')
			} else {
				ElMessage.success('已绑定已确认基线的事件')
			}
			await initAssessment(store.currentType)
		} catch (e) {
			if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
		} finally {
			pickingEvent.value = false
		}
	}

	const applyRouteMeta = async () => {
		const typeId =
			route.meta?.assessType ||
			resolveTypeFromRoute(String(route.path).split('/').pop()) ||
			store.currentType
		if (typeId && typeId !== store.currentType) {
			clearAssessmentLayers()
			store.setType(typeId)
			store.reset()
		}
		if (route.meta?.assessStep) {
			store.setStep(route.meta.assessStep)
		}
		await initAssessment(store.currentType)
		if (route.meta?.reportsOnly || route.meta?.assessStep === 'report') {
			await ensureAssessmentStepResult('report')
		}
	}

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
		const target = item.path || ASSESS_TYPES.find(t => t.id === item.id)?.path
		if (target && route.path !== target) {
			await router.push(target)
			return
		}
		clearAssessmentLayers()
		store.setType(item.id)
		store.reset()
		await initAssessment(item.id)
	}

	watch(
		() => route.fullPath,
		() => {
			applyRouteMeta()
		},
		{ immediate: true }
	)

	onMounted(() => {
		appStore.setCurNavObj('灾害评估')
		store.setLeftPanelCollapsed(false)
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
	.assess-event-hint {
		margin-top: 4px;
		font-size: 12px;
		color: #5c6b7a;
	}
	.assess-baseline-gate {
		margin: 0 12px 10px;
		padding: 10px 12px;
		border-radius: 8px;
		background: #fff7e6;
		border: 1px solid #ffd591;
		color: #ad6800;
		font-size: 13px;
		line-height: 1.5;
		&__actions {
			display: flex;
			flex-wrap: wrap;
			gap: 8px;
			margin-top: 8px;
		}
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
