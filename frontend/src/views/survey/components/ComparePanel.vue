<template>
	<div class="wb-panel">
		<section class="wb-block">
			<div class="wb-title"><span class="txt">上报对比分析</span></div>
			<p class="wb-hint">对照所选承灾体的现状监测与灾情速报，图表展示受灾人口、经济损失、受灾面积，可切换表格。</p>
			<div class="wb-tiles">
				<button
					v-for="item in hazardTypes"
					:key="item.id"
					class="wb-tile"
					:class="{ active: surveyStore.compareType === item.id }"
					@click="onTypeChange(item.id)">
					<i class="wb-dot" :class="item.id"></i>
					{{ item.label }}
				</button>
			</div>
			<el-radio-group v-model="surveyStore.compareView" size="small" class="wb-gap">
				<el-radio-button label="chart">柱状图</el-radio-button>
				<el-radio-button label="table">表格</el-radio-button>
			</el-radio-group>
			<el-button class="max-w" type="primary" @click="loadCompare(surveyStore.compareType)">生成对比</el-button>
		</section>
	</div>
</template>

<script setup>
	import { HAZARD_TYPES } from '@/views/survey/constants.js'
	import { useSurveyStore } from '@/store/modules/survey.js'
	import { useSurveyActions } from '@/views/survey/composables/useSurveyActions.js'

	const surveyStore = useSurveyStore()
	const { loadCompare } = useSurveyActions()
	const hazardTypes = HAZARD_TYPES

	const onTypeChange = typeId => {
		surveyStore.compareType = typeId
		loadCompare(typeId)
	}
</script>
