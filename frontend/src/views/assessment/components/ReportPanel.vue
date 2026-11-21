<template>
	<div class="wb-panel">
		<section class="wb-block">
			<div class="wb-title"><span class="txt">综合评估因子</span></div>
			<p class="wb-hint">整合承灾体与经济影响、结构损伤、应急响应有效性及未来备灾准备，生成评估报告。</p>
			<div class="wb-factors">
				<div class="wb-factor">
					<div class="wb-factor__head">
						<span>结构损伤</span>
						<b>{{ store.reportFactors.structure }}</b>
					</div>
					<el-slider v-model="store.reportFactors.structure" :min="0" :max="100" />
				</div>
				<div class="wb-factor">
					<div class="wb-factor__head">
						<span>应急响应</span>
						<b>{{ store.reportFactors.response }}</b>
					</div>
					<el-slider v-model="store.reportFactors.response" :min="0" :max="100" />
				</div>
				<div class="wb-factor">
					<div class="wb-factor__head">
						<span>备灾准备</span>
						<b>{{ store.reportFactors.prepare }}</b>
					</div>
					<el-slider v-model="store.reportFactors.prepare" :min="0" :max="100" />
				</div>
			</div>
			<el-input
				v-model="store.reportFactors.note"
				class="wb-gap"
				type="textarea"
				:rows="3"
				placeholder="可录入现场核查或恢复建议" />
			<div v-if="baselineHint" class="wb-hint wb-gap">{{ baselineHint }}</div>
			<div class="report-actions">
				<el-button
					type="primary"
					class="max-w"
					:loading="store.reportGenerating"
					:disabled="store.reportGenerating"
					@click="runReport">
					生成评估报告
				</el-button>
				<el-button class="max-w" :loading="store.reportLoading" @click="onReplay">按当前场景复盘</el-button>
				<el-button class="max-w" :loading="archiving" @click="onArchive">归档当前报告</el-button>
			</div>
		</section>
		<section class="wb-block" v-if="store.archivedReports.length">
			<div class="wb-title">
				<span class="txt">归档台账</span>
				<span class="sub">{{ store.archivedReports.length }} 份</span>
			</div>
			<div
				v-for="item in store.archivedReports.slice(0, 6)"
				:key="item.id"
				class="archive-item"
				@click="openArchivedReport(item.id)">
				<strong>{{ item.reportNo }}</strong>
				<span>{{ item.typeLabel }} · {{ item.grade }} · {{ item.archivedAt }}</span>
			</div>
		</section>
	</div>
</template>

<script setup>
	import { computed, ref } from 'vue'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useSurveyBaselineStore } from '@/store/modules/surveyBaseline.js'
	import { useAssessmentActions } from '@/views/assessment/composables/useAssessmentActions.js'

	const store = useAssessmentStore()
	const baselineStore = useSurveyBaselineStore()
	const { runReport, archiveReport, openArchivedReport, replayScenario } = useAssessmentActions()
	const archiving = ref(false)

	const baselineHint = computed(() => {
		const pkg = baselineStore.package
		if (!pkg?.affectedMeta) return '尚未加载调查基线：可先在灾情调查完成识别/叠置，或点击「按当前场景复盘」。'
		return `已衔接调查基线（场景 ${pkg.sceneId || '-'}，受灾 ${pkg.affectedMeta.areaKm2 ?? '-'} km²）`
	})

	const onReplay = async () => {
		if (store.reportLoading) return
		await replayScenario(store.selectedSceneId)
	}

	const onArchive = async () => {
		archiving.value = true
		try {
			if (!store.reportResult) await runReport({ openPreview: false })
			await archiveReport()
		} finally {
			archiving.value = false
		}
	}
</script>

<style lang="scss" scoped>
	.report-actions {
		display: flex;
		flex-direction: column;
		gap: 10px;
		width: 100%;
		:deep(.el-button) {
			margin-left: 0;
			width: 100%;
		}
	}
	.archive-item {
		display: flex;
		flex-direction: column;
		gap: 2px;
		padding: 8px 10px;
		margin-bottom: 6px;
		border: 1px solid #edf2f8;
		border-radius: 8px;
		cursor: pointer;
		background: #f8fbff;
		strong {
			font-size: 13px;
			color: #1a3a5c;
		}
		span {
			font-size: 12px;
			color: #8a97a8;
		}
		&:hover {
			border-color: #91caff;
		}
	}
</style>
