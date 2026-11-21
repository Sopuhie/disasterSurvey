<template>
	<div class="wb-panel">
		<section class="wb-block">
			<div class="wb-title"><span class="txt">漫堤分析</span></div>
			<p class="wb-hint">
				基于运维漫堤分析数据核查：警戒潮位/潮高/堤顶高程统一为 m，保护人口统一为 人，并与调查海堤台账关联上图。
			</p>
			<div class="wb-row wb-gap">
				<el-input v-model="store.overtoppingCity" clearable placeholder="所在市，如 宁德市" style="flex: 1" />
			</div>
			<div class="wb-row">
				<el-input
					v-model="store.overtoppingKeyword"
					clearable
					placeholder="海堤名称 / 乡镇 / 潮位站"
					style="flex: 1"
					@keyup.enter="loadOvertopping" />
				<el-button type="primary" :loading="store.overtoppingLoading" @click="loadOvertopping">查询</el-button>
			</div>
			<div class="wb-scene-card" v-if="store.queriedDistrict">
				<div><span>当前区县</span><b>{{ store.queriedDistrict.name }}</b></div>
				<div><span>所属地市</span><b>{{ store.queriedDistrict.city }}</b></div>
			</div>
			<el-button type="primary" class="max-w" :loading="store.overtoppingLoading" @click="loadOvertopping">
				加载漫堤分析结果
			</el-button>
		</section>
		<section class="wb-block">
			<div class="wb-title">
				<span class="txt">结果摘要</span>
				<span class="sub">{{ store.overtoppingRows.length }} 条</span>
			</div>
			<div class="wb-kpis">
				<div class="wb-kpi">
					<span>记录数</span>
					<b>{{ store.overtoppingRows.length }}</b>
				</div>
				<div class="wb-kpi orange">
					<span>有防浪墙</span>
					<b>{{ withWall }}</b>
				</div>
				<div class="wb-kpi red">
					<span>预警超限</span>
					<b>{{ overWarn }}</b>
				</div>
				<div class="wb-kpi purple">
					<span>保护人口合计</span>
					<b>{{ protectPopTotal }} <small>人</small></b>
				</div>
			</div>
		</section>
	</div>
</template>

<script setup>
	import { computed } from 'vue'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useAssessmentActions } from '@/views/assessment/composables/useAssessmentActions.js'

	const store = useAssessmentStore()
	const { loadOvertopping } = useAssessmentActions()

	const withWall = computed(
		() => store.overtoppingRows.filter(item => String(item.hasWall || '').includes('有')).length
	)
	const overWarn = computed(
		() => store.overtoppingRows.filter(item => item.warnLevel && item.warnLevel !== '不超').length
	)
	const protectPopTotal = computed(() =>
		store.overtoppingRows.reduce((sum, item) => sum + (Number(item.protectPop) || 0), 0)
	)
</script>
