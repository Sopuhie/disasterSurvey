<template>
	<div class="wb-panel">
		<section class="wb-block">
			<div class="wb-title"><span class="txt">事件与基线</span></div>
			<p class="wb-hint">
				当前事件：
				<strong>{{ workflowStore.currentEvent?.eventName || workflowStore.currentEventId || '未绑定' }}</strong>
				<span class="wb-status" :class="statusClass">{{ statusText }}</span>
			</p>
		</section>
		<section class="wb-block">
			<div class="wb-title"><span class="txt">上报对比分析</span></div>
			<p class="wb-hint">对照遥感解译与灾情速报，图表/表格数据直接消费后端对比核查结果；确认后锁定调查基线。</p>
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
			<el-button
				class="max-w wb-gap"
				type="success"
				:disabled="workflowStore.isBaselineConfirmed"
				:loading="confirming"
				@click="onConfirm">
				{{ workflowStore.isBaselineConfirmed ? '基线已锁定' : '确认并锁定基线' }}
			</el-button>
		</section>
	</div>
</template>

<script setup>
	import { computed, ref } from 'vue'
	import { HAZARD_TYPES } from '@/views/survey/constants.js'
	import { BaselineStatusLabel } from '@/constants/disasterEnums.js'
	import { useSurveyStore } from '@/store/modules/survey.js'
	import { useSurveyActions } from '@/views/survey/composables/useSurveyActions.js'

	const surveyStore = useSurveyStore()
	const { loadCompare, confirmBaseline, workflowStore } = useSurveyActions()
	const hazardTypes = HAZARD_TYPES
	const confirming = ref(false)

	const statusText = computed(() => {
		const st = workflowStore.currentBaseline?.status
		return st ? BaselineStatusLabel[st] || st : '未生成'
	})
	const statusClass = computed(() => {
		const st = String(workflowStore.currentBaseline?.status || '').toUpperCase()
		if (st === 'CONFIRMED') return 'ok'
		if (st === 'DRAFT') return 'draft'
		return 'empty'
	})

	const onTypeChange = typeId => {
		surveyStore.compareType = typeId
		loadCompare(typeId)
	}

	const onConfirm = async () => {
		confirming.value = true
		try {
			await confirmBaseline()
		} finally {
			confirming.value = false
		}
	}
</script>

<style scoped>
	.wb-status {
		margin-left: 8px;
		padding: 1px 8px;
		border-radius: 4px;
		font-size: 12px;
	}
	.wb-status.ok {
		color: #389e0d;
		background: #f6ffed;
	}
	.wb-status.draft {
		color: #d48806;
		background: #fffbe6;
	}
	.wb-status.empty {
		color: #8c8c8c;
		background: #f5f5f5;
	}
	.wb-gap {
		margin-top: 10px;
	}
</style>
