<template>
	<div class="wb-panel">
		<section class="wb-block">
			<div class="wb-title"><span class="txt">叠置目标</span></div>
			<p class="wb-hint">基于已识别的受灾范围，与人口密度、乡镇行政区划或承灾体数据进行相交分析。海堤、渔港、养殖区和旅游区分析结果支持导出遥感影像。</p>
			<div class="wb-tiles">
				<button
					v-for="item in overlayTargets"
					:key="item.id"
					class="wb-tile"
					:class="{ active: surveyStore.overlayTarget === item.id }"
					@click="onTargetChange(item.id)">
					<i class="wb-dot" :class="item.id"></i>
					{{ item.label }}
				</button>
			</div>
			<div class="wb-row">
				<el-button type="primary" class="max-w" :loading="surveyStore.overlaying" @click="runOverlay(surveyStore.overlayTarget)">
					开始分析
				</el-button>
				<el-button v-if="canExport" type="primary" plain @click="exportOverlayImage">导出影像</el-button>
			</div>
		</section>
		<section class="wb-block" v-if="surveyStore.overlayResult">
			<div class="wb-title"><span class="txt">分析摘要</span></div>
			<div class="wb-kpis">
				<div class="wb-kpi">
					<span>命中对象</span>
					<b>{{ surveyStore.overlayResult.totals.count }}</b>
				</div>
				<div class="wb-kpi orange" v-if="surveyStore.overlayResult.totals.areaKm2">
					<span>相交面积</span>
					<b>{{ surveyStore.overlayResult.totals.areaKm2 }} <small>km²</small></b>
				</div>
				<div class="wb-kpi green" v-if="surveyStore.overlayResult.totals.lengthKm">
					<span>相交长度</span>
					<b>{{ surveyStore.overlayResult.totals.lengthKm }} <small>km</small></b>
				</div>
				<div class="wb-kpi red" v-if="surveyStore.overlayResult.totals.affectedPopulation">
					<span>受灾人口</span>
					<b>{{ surveyStore.overlayResult.totals.affectedPopulation }} <small>人</small></b>
				</div>
			</div>
		</section>
	</div>
</template>

<script setup>
	import { computed } from 'vue'
	import { OVERLAY_TARGETS } from '@/views/survey/constants.js'
	import { useSurveyStore } from '@/store/modules/survey.js'
	import { useSurveyActions } from '@/views/survey/composables/useSurveyActions.js'

	const surveyStore = useSurveyStore()
	const { runOverlay, exportOverlayImage } = useSurveyActions()
	const overlayTargets = OVERLAY_TARGETS
	const onTargetChange = targetId => {
		surveyStore.overlayTarget = targetId
		runOverlay(targetId)
	}
	const canExport = computed(() => {
		const target = OVERLAY_TARGETS.find(item => item.id === surveyStore.overlayTarget)
		return target?.kind === 'hazard' && !!surveyStore.overlayResult
	})
</script>
