<template>
	<div class="wind-result">
		<template v-if="store.currentStep === 'damage'">
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">海风破坏性分析结果</span></div>
			</div>
			<template v-if="filteredDamage.length">
				<div class="wind-scene" v-if="currentScene">
					<span>{{ currentScene.name }}</span>
					<em>{{ currentScene.date }} · {{ currentScene.sensor }} · {{ currentScene.maxWind }} m/s · {{ currentScene.direction }}</em>
				</div>
				<div class="wb-kpis">
					<div class="wb-kpi">
						<span>评估对象</span>
						<b>{{ filteredDamage.length }} <small>处</small></b>
					</div>
					<div class="wb-kpi red">
						<span>最大风速</span>
						<b>{{ windStats.maxWind }} <small>m/s</small></b>
					</div>
					<div class="wb-kpi orange">
						<span>主导风向</span>
						<b>{{ windStats.direction }}</b>
					</div>
					<div class="wb-kpi purple">
						<span>高风险对象</span>
						<b>{{ windStats.highRisk }} <small>处</small></b>
					</div>
				</div>
				<div class="wind-split">
					<div class="wind-chart">
						<ECharts :key="`wind-level-${store.damageTarget}`" :option="levelPieOption" :resize="false" />
					</div>
					<div class="wind-chart">
						<ECharts :key="`wind-target-${store.damageTarget}`" :option="targetBarOption" :resize="false" />
					</div>
				</div>
				<div class="wb-title"><span class="txt">对象明细</span></div>
				<div v-for="row in filteredDamage" :key="row.name" class="wind-obj" :class="`lv-${row.level}`">
					<div class="wind-obj__top">
						<strong>{{ row.name }}</strong>
						<span class="wb-tag" :class="`level-${row.level}`">{{ row.levelLabel }}</span>
					</div>
					<div class="wind-obj__meta">
						<span>{{ row.target }}</span>
						<span>{{ row.county || '-' }}</span>
						<span>风速 {{ row.wind }} m/s</span>
						<span>风向 {{ row.dir || '-' }}</span>
						<span>{{ row.levelText }}</span>
					</div>
					<p>{{ row.analysis }}</p>
				</div>
			</template>
			<div v-else class="wb-empty">
				<el-empty description="请选择影像场景并开始分析" :image-size="84" />
			</div>
		</template>

		<template v-else-if="store.currentStep === 'behavior'">
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">海风行为分析结果</span></div>
			</div>
			<template v-if="store.behaviorResult">
				<p class="wind-brief">{{ store.behaviorResult.desc }}</p>
				<div class="wb-kpis">
					<div class="wb-kpi orange">
						<span>最大风速</span>
						<b>{{ store.behaviorResult.maxWind }} <small>m/s</small></b>
					</div>
					<div class="wb-kpi">
						<span>主导风向</span>
						<b>{{ store.behaviorResult.avgDir }}</b>
					</div>
					<div class="wb-kpi red">
						<span>淹没范围</span>
						<b>{{ store.behaviorResult.inundationKm2 }} <small>km²</small></b>
					</div>
					<div class="wb-kpi purple">
						<span>受灾人口</span>
						<b>{{ store.behaviorResult.affectedPopulation }} <small>人</small></b>
					</div>
				</div>
				<div class="wind-params">
					<div>
						<span>过程名称</span>
						<b>{{ store.behaviorResult.event?.name }}</b>
					</div>
					<div>
						<span>登陆地点</span>
						<b>{{ store.behaviorResult.event?.landfall || '-' }}</b>
					</div>
					<div>
						<span>强度等级</span>
						<b>{{ store.behaviorResult.event?.level }}</b>
					</div>
					<div>
						<span>路径点数</span>
						<b>{{ store.behaviorResult.event?.points?.length || 0 }} 个</b>
					</div>
				</div>
				<div class="wind-chart wind-chart--wide">
					<ECharts key="wind-inundation" :option="inundationOption" :resize="false" />
				</div>
				<div class="wb-title"><span class="txt">路径监测点</span></div>
				<div v-for="point in store.behaviorResult.event?.points || []" :key="point.time" class="wind-obj lv-3">
					<div class="wind-obj__top">
						<strong>{{ point.time }}</strong>
						<span>{{ point.wind }} m/s · {{ point.dir }}</span>
					</div>
					<div class="wind-obj__meta">
						<span>经度 {{ point.lng }}</span>
						<span>纬度 {{ point.lat }}</span>
					</div>
				</div>
				<div class="wb-title"><span class="txt">承灾体结构损害</span></div>
				<div v-for="row in store.behaviorResult.structures" :key="row.name" class="wind-obj" :class="`lv-${row.level}`">
					<div class="wind-obj__top">
						<strong>{{ row.name }}</strong>
						<span class="wb-tag" :class="`level-${row.level}`">{{ levelFull(row.level) }}</span>
					</div>
					<div class="wind-obj__meta">
						<span>{{ row.type }}</span>
						<span>{{ row.county || '-' }}</span>
					</div>
					<p>{{ row.damage }}</p>
				</div>
			</template>
			<div v-else class="wb-empty">
				<el-empty description="请选择监测过程并开始分析" :image-size="84" />
			</div>
		</template>

		<template v-else>
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">海风综合评估</span></div>
				<el-button v-if="store.reportResult" size="small" type="primary" @click="store.reportVisible = true">
					查看报告
				</el-button>
			</div>
			<template v-if="store.reportResult">
				<div class="wb-kpis" style="margin-bottom: 16px">
					<div class="wb-kpi">
						<span>综合指数</span>
						<b>{{ store.reportResult.total }}</b>
					</div>
					<div class="wb-kpi" :class="gradeAccent">
						<span>评估等级</span>
						<b>{{ store.reportResult.grade }}</b>
					</div>
					<div class="wb-kpi orange">
						<span>最大风速</span>
						<b>{{ store.behaviorResult?.maxWind || store.reportResult.processParams?.[0]?.value || '-' }}</b>
					</div>
					<div class="wb-kpi purple">
						<span>评估对象</span>
						<b>{{ store.reportResult.damageRows?.length || 0 }} <small>处</small></b>
					</div>
				</div>
				<div class="wind-chart wind-chart--wide">
					<ECharts key="wind-score" :option="scoreOption" :resize="false" />
				</div>
				<p class="wind-brief" v-for="(text, index) in summaryList" :key="index">{{ text }}</p>
				<template v-if="store.archivedReports.length">
					<div class="wb-title"><span class="txt">已归档报告</span></div>
					<div
						v-for="item in store.archivedReports.slice(0, 5)"
						:key="item.id"
						class="archive-row"
						@click="openArchivedReport(item.id)">
						<strong>{{ item.reportNo }}</strong>
						<span>{{ item.grade }} · {{ item.archivedAt }}</span>
					</div>
				</template>
			</template>
			<div v-else class="wb-empty">
				<el-empty description="请设置评估因子并生成报告" :image-size="84" />
			</div>
		</template>
	</div>
</template>

<script setup>
	import { computed } from 'vue'
	import ECharts from '@/components/eCharts/index.vue'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useAssessmentActions } from '@/views/assessment/composables/useAssessmentActions.js'
	import { formatImpactLevel, formatImpactLevelFull } from '@/shared/metricUnits.js'
	import { LEVEL_MAP } from '@/views/assessment/constants.js'

	const store = useAssessmentStore()
	const { openArchivedReport } = useAssessmentActions()
	const summaryList = computed(() => {
		const summary = store.reportResult?.summary
		return Array.isArray(summary) ? summary : summary ? [summary] : []
	})
	const gradeAccent = computed(() => {
		const map = { 轻度: 'green', 中度: '', 较重: 'orange', 严重: 'red' }
		return map[store.reportResult?.grade] || ''
	})
	const filteredDamage = computed(() => {
		if (store.damageTarget === 'all') return store.damageRows
		return store.damageRows.filter(item => item.target === store.damageTarget || (store.damageTarget === '海堤' && item.target === '海塘'))
	})
	const currentScene = computed(() => store.scenes.find(item => item.id === store.selectedSceneId) || store.scenes[0])
	const windStats = computed(() => {
		const rows = filteredDamage.value
		const dirs = rows.map(item => item.dir).filter(Boolean)
		return {
			maxWind: rows.reduce((max, item) => Math.max(max, item.wind || 0), 0).toFixed(1),
			direction: dirs[0] || currentScene.value?.direction || '-',
			highRisk: rows.filter(item => item.level <= 2).length
		}
	})
	const levelText = level => formatImpactLevel(level)
	const levelFull = level => formatImpactLevelFull(level)
	const scoreOption = computed(() => ({
		tooltip: { trigger: 'axis' },
		grid: { top: 24, left: 12, right: 12, bottom: 8, containLabel: true },
		xAxis: {
			type: 'category',
			data: (store.reportResult?.scores || []).map(item => item.name),
			axisTick: { show: false },
			axisLine: { lineStyle: { color: '#d9e0ea' } },
			axisLabel: { color: '#5c6b7a', interval: 0, fontSize: 11 }
		},
		yAxis: {
			type: 'value',
			max: 100,
			splitLine: { lineStyle: { color: '#eef2f7' } },
			axisLabel: { color: '#8a97a8' }
		},
		series: [
			{
				type: 'bar',
				barWidth: 18,
				data: (store.reportResult?.scores || []).map(item => item.value),
				itemStyle: { color: '#1677FF', borderRadius: [4, 4, 0, 0] }
			}
		]
	}))
	const chartText = { color: '#5c6b7a', fontSize: 11 }
	const levelPieOption = computed(() => {
		const colors = { 1: '#F5222D', 2: '#FA8C16', 3: '#1677FF', 4: '#52C41A' }
		const data = [1, 2, 3, 4]
			.map(level => ({
				name: LEVEL_MAP[level].label,
				value: filteredDamage.value.filter(item => item.level === level).length,
				itemStyle: { color: colors[level] }
			}))
			.filter(item => item.value)
		return {
			tooltip: { trigger: 'item' },
			legend: { bottom: 0, itemWidth: 8, itemHeight: 8, textStyle: chartText },
			series: [{ type: 'pie', radius: ['48%', '70%'], center: ['50%', '42%'], label: { show: false }, data }]
		}
	})
	const targetBarOption = computed(() => {
		const groups = {}
		filteredDamage.value.forEach(item => {
			groups[item.target] = (groups[item.target] || 0) + 1
		})
		return {
			tooltip: { trigger: 'axis' },
			grid: { top: 28, left: 8, right: 8, bottom: 8, containLabel: true },
			xAxis: {
				type: 'category',
				data: Object.keys(groups),
				axisTick: { show: false },
				axisLine: { lineStyle: { color: '#d9e0ea' } },
				axisLabel: chartText
			},
			yAxis: {
				type: 'value',
				minInterval: 1,
				splitLine: { lineStyle: { color: '#eef2f7' } },
				axisLabel: chartText
			},
			series: [{ type: 'bar', barWidth: 18, data: Object.values(groups), itemStyle: { color: '#1677FF', borderRadius: [4, 4, 0, 0] } }]
		}
	})
	const inundationOption = computed(() => ({
		tooltip: { trigger: 'axis' },
		legend: { top: 4, itemWidth: 8, itemHeight: 8, textStyle: chartText },
		grid: { top: 32, left: 8, right: 12, bottom: 4, containLabel: true },
		xAxis: {
			type: 'category',
			data: (store.behaviorResult?.inundationRows || []).map(item => item.name),
			axisTick: { show: false },
			axisLine: { lineStyle: { color: '#d9e0ea' } },
			axisLabel: chartText
		},
		yAxis: {
			type: 'value',
			splitLine: { lineStyle: { color: '#eef2f7' } },
			axisLabel: chartText
		},
		series: [
			{
				name: '淹没面积(km²)',
				type: 'bar',
				barWidth: 14,
				data: (store.behaviorResult?.inundationRows || []).map(item => item.areaKm2),
				itemStyle: { color: '#1677FF', borderRadius: [4, 4, 0, 0] }
			},
			{
				name: '受灾人口(人)',
				type: 'line',
				smooth: true,
				data: (store.behaviorResult?.inundationRows || []).map(item => item.population),
				itemStyle: { color: '#FA8C16' }
			}
		]
	}))
</script>

<style lang="scss" scoped>
	.wind-result {
		height: 100%;
		overflow: auto;
	}
	.wind-scene {
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: 8px;
		margin-bottom: 12px;
		padding: 8px 12px;
		border-radius: 8px;
		background: #f0f7ff;
		color: #1677ff;
		font-size: 12px;
		em {
			color: #8a97a8;
			font-style: normal;
		}
	}
	.wind-brief {
		margin: 0 0 12px;
		padding: 10px 12px;
		background: #f6f9fd;
		border-radius: 8px;
		color: #3d4a5c;
		font-size: 13px;
		line-height: 22px;
	}
	.wind-split {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 10px;
		margin: 14px 0;
	}
	.wind-chart {
		height: 176px;
		background: #f8fbff;
		border: 1px solid #edf2f8;
		border-radius: 8px;
		padding: 4px;
		&--wide {
			width: 100%;
			height: 210px;
			margin-bottom: 16px;
		}
	}
	.wind-params {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 8px;
		margin: 12px 0 14px;
		div {
			background: #fff;
			border: 1px solid #edf2f8;
			border-radius: 8px;
			padding: 10px 12px;
			span {
				display: block;
				color: #8a97a8;
				font-size: 12px;
				margin-bottom: 4px;
			}
			b {
				color: #1f2d3d;
				font-size: 14px;
			}
		}
	}
	.wind-obj {
		background: #fff;
		border: 1px solid #edf2f8;
		border-left: 3px solid #1677ff;
		border-radius: 8px;
		padding: 10px 12px;
		margin-bottom: 8px;
		&.lv-1 {
			border-left-color: #f5222d;
		}
		&.lv-2 {
			border-left-color: #fa8c16;
		}
		&.lv-3 {
			border-left-color: #1677ff;
		}
		&.lv-4 {
			border-left-color: #52c41a;
		}
		&__top {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 8px;
			margin-bottom: 6px;
			strong {
				color: #1f2d3d;
				font-size: 14px;
			}
		}
		&__meta {
			display: flex;
			flex-wrap: wrap;
			gap: 8px 12px;
			color: #8a97a8;
			font-size: 12px;
			margin-bottom: 6px;
		}
		p {
			margin: 0;
			color: #3d4a5c;
			font-size: 13px;
			line-height: 20px;
		}
	}
	.wb-kpis {
		margin-bottom: 2px;
	}
	.archive-row {
		display: flex;
		justify-content: space-between;
		gap: 8px;
		padding: 8px 10px;
		margin-bottom: 6px;
		border: 1px solid #edf2f8;
		border-radius: 8px;
		cursor: pointer;
		font-size: 12px;
		strong {
			color: #1a3a5c;
		}
		span {
			color: #8a97a8;
		}
		&:hover {
			border-color: #91caff;
			background: #f0f7ff;
		}
	}
</style>
