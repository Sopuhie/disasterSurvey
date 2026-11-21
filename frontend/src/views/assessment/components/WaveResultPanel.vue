<template>
	<div class="wave-result">
		<template v-if="store.currentStep === 'damage'">
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">海浪破坏性分析结果</span></div>
			</div>
			<template v-if="filteredDamage.length">
				<div class="wave-scene" v-if="currentScene">
					<span>{{ currentScene.name }}</span>
					<em>{{ currentScene.date }} · {{ currentScene.sensor }}</em>
				</div>
				<div class="wb-kpis">
					<div class="wb-kpi">
						<span>评估对象</span>
						<b>{{ filteredDamage.length }} <small>处</small></b>
					</div>
					<div class="wb-kpi red">
						<span>最大波高</span>
						<b>{{ waveStats.maxHeight }} <small>m</small></b>
					</div>
					<div class="wb-kpi orange">
						<span>最大波速</span>
						<b>{{ waveStats.maxSpeed }} <small>m/s</small></b>
					</div>
					<div class="wb-kpi purple">
						<span>高风险对象</span>
						<b>{{ waveStats.highRisk }} <small>处</small></b>
					</div>
				</div>
				<div class="wave-split">
					<div class="wave-chart-box">
						<ECharts :key="`wave-level-${store.damageTarget}`" :option="levelPieOption" :resize="false" />
					</div>
					<div class="wave-chart-box">
						<ECharts :key="`wave-target-${store.damageTarget}`" :option="targetBarOption" :resize="false" />
					</div>
				</div>
				<div class="wb-title"><span class="txt">对象明细</span></div>
				<div
					v-for="row in filteredDamage"
					:key="row.name"
					class="wave-obj"
					:class="`lv-${row.level}`">
					<div class="wave-obj__top">
						<strong>{{ row.name }}</strong>
						<span class="wb-tag" :class="`level-${row.level}`">{{ row.levelLabel }}</span>
					</div>
					<div class="wave-obj__meta">
						<span>{{ row.target }}</span>
						<span>{{ row.county || '-' }}</span>
						<span>波高 {{ row.height }} m</span>
						<span>波速 {{ row.speed }} m/s</span>
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
				<div class="wb-title"><span class="txt">海浪行为分析结果</span></div>
			</div>
			<template v-if="store.behaviorResult">
				<p class="wave-brief">{{ store.behaviorResult.desc }}</p>
				<div class="wb-kpis">
					<div class="wb-kpi orange">
						<span>最大波高</span>
						<b>{{ store.behaviorResult.maxHeight }} <small>m</small></b>
					</div>
					<div class="wb-kpi">
						<span>波周期</span>
						<b>{{ store.behaviorResult.period }} <small>s</small></b>
					</div>
					<div class="wb-kpi red">
						<span>侵蚀范围</span>
						<b>{{ store.behaviorResult.inundationKm2 }} <small>km²</small></b>
					</div>
					<div class="wb-kpi purple">
						<span>侵蚀岸线</span>
						<b>{{ store.behaviorResult.lengthKm }} <small>km</small></b>
					</div>
				</div>
				<div class="wave-params">
					<div><span>来波方向</span><b>{{ store.behaviorResult.direction }}</b></div>
					<div><span>最大波速</span><b>{{ store.behaviorResult.scene?.maxSpeed || store.behaviorResult.avgSpeed }} m/s</b></div>
					<div><span>平均波速</span><b>{{ store.behaviorResult.avgSpeed }} m/s</b></div>
					<div><span>波能指数</span><b>{{ store.behaviorResult.energyIndex }}</b></div>
				</div>
				<div class="wave-chart-box wave-chart-box--wide">
					<ECharts key="wave-erosion" :option="erosionOption" :resize="false" />
				</div>
				<div class="wb-title"><span class="txt">海上及海岸结构损害</span></div>
				<div
					v-for="row in store.behaviorResult.structures"
					:key="row.name"
					class="wave-obj"
					:class="`lv-${row.level}`">
					<div class="wave-obj__top">
						<strong>{{ row.name }}</strong>
						<span class="wb-tag" :class="`level-${row.level}`">{{ levelFull(row.level) }}</span>
					</div>
					<div class="wave-obj__meta">
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
				<div class="wb-title"><span class="txt">海浪综合评估</span></div>
				<el-button v-if="store.reportResult" size="small" type="primary" @click="store.reportVisible = true">
					查看报告
				</el-button>
			</div>
			<template v-if="store.reportResult">
				<div class="wave-hero" :class="`grade-${store.reportResult.grade}`">
					<div>
						<span>综合指数</span>
						<b>{{ store.reportResult.total }}</b>
					</div>
					<div class="wave-hero__grade">
						<em>评估等级</em>
						<strong>{{ store.reportResult.grade }}</strong>
						<small>{{ store.reportResult.district }} {{ store.reportResult.city }}</small>
					</div>
				</div>
				<div class="wave-split">
					<div class="wave-chart-box">
						<ECharts key="wave-gauge" :option="gaugeOption" :resize="false" />
					</div>
					<div class="wave-chart-box">
						<ECharts key="wave-radar" :option="radarOption" :resize="false" />
					</div>
				</div>
				<div class="wb-title"><span class="txt">分项得分</span></div>
				<div v-for="item in store.reportResult.scores" :key="item.name" class="wave-score">
					<span>{{ item.name }}</span>
					<div class="bar"><i :style="{ width: `${item.value}%` }"></i></div>
					<b>{{ item.value }}</b>
				</div>
				<div class="wave-summary">
					<p v-for="(text, index) in summaryList" :key="index">{{ text }}</p>
				</div>
				<div class="wb-title"><span class="txt">处置建议</span></div>
				<ul class="wave-advice">
					<li v-for="item in adviceList" :key="item">{{ item }}</li>
				</ul>
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
	import { LEVEL_MAP } from '@/views/assessment/constants.js'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useAssessmentActions } from '@/views/assessment/composables/useAssessmentActions.js'
	import { formatImpactLevel, formatImpactLevelFull } from '@/shared/metricUnits.js'

	const store = useAssessmentStore()
	const { openArchivedReport } = useAssessmentActions()
	const summaryList = computed(() => {
		const summary = store.reportResult?.summary
		return Array.isArray(summary) ? summary : summary ? [summary] : []
	})
	const filteredDamage = computed(() => {
		if (store.damageTarget === 'all') return store.damageRows
		return store.damageRows.filter(
			item => item.target === store.damageTarget || (store.damageTarget === '海堤' && item.target === '海塘')
		)
	})
	const currentScene = computed(() => store.scenes.find(item => item.id === store.selectedSceneId) || store.scenes[0])
	const waveStats = computed(() => {
		const rows = filteredDamage.value
		return {
			maxHeight: rows.reduce((max, item) => Math.max(max, item.height || 0), 0).toFixed(1),
			maxSpeed: rows.reduce((max, item) => Math.max(max, item.speed || 0), 0).toFixed(1),
			highRisk: rows.filter(item => item.level <= 2).length
		}
	})
	const levelText = level => formatImpactLevel(level)
	const levelFull = level => formatImpactLevelFull(level)
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
			series: [
				{
					type: 'pie',
					radius: ['48%', '70%'],
					center: ['50%', '42%'],
					label: { show: false },
					data
				}
			]
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
			series: [
				{
					type: 'bar',
					barWidth: 18,
					data: Object.values(groups),
					itemStyle: { color: '#1677FF', borderRadius: [4, 4, 0, 0] }
				}
			]
		}
	})
	const erosionOption = computed(() => ({
		tooltip: { trigger: 'axis' },
		legend: { top: 4, itemWidth: 8, itemHeight: 8, textStyle: chartText },
		grid: { top: 32, left: 8, right: 12, bottom: 4, containLabel: true },
		xAxis: {
			type: 'category',
			data: (store.behaviorResult?.erosion || []).map(item => item.name),
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
				name: '侵蚀面积(km²)',
				type: 'bar',
				barWidth: 14,
				data: (store.behaviorResult?.erosion || []).map(item => item.areaKm2),
				itemStyle: { color: '#1677FF', borderRadius: [4, 4, 0, 0] }
			},
			{
				name: '岸线长度(km)',
				type: 'line',
				smooth: true,
				data: (store.behaviorResult?.erosion || []).map(item => item.lengthKm),
				itemStyle: { color: '#FA8C16' }
			}
		]
	}))
	const gaugeOption = computed(() => {
		const total = store.reportResult?.total || 0
		return {
			series: [
				{
					type: 'gauge',
					startAngle: 210,
					endAngle: -30,
					min: 0,
					max: 100,
					radius: '92%',
					center: ['50%', '58%'],
					progress: { show: true, width: 12 },
					pointer: { length: '58%', width: 5 },
					axisLine: {
						lineStyle: {
							width: 12,
							color: [
								[0.5, '#F5222D'],
								[0.65, '#FA8C16'],
								[0.8, '#1677FF'],
								[1, '#52C41A']
							]
						}
					},
					axisTick: { show: false },
					splitLine: { length: 8, lineStyle: { color: '#d9e0ea' } },
					axisLabel: { fontSize: 10, color: '#8a97a8', distance: 12 },
					detail: { valueAnimation: true, fontSize: 22, offsetCenter: [0, '24%'], formatter: '{value}' },
					title: { offsetCenter: [0, '48%'], fontSize: 11, color: '#8a97a8' },
					data: [{ value: total, name: '综合指数' }]
				}
			]
		}
	})
	const radarOption = computed(() => ({
		radar: {
			indicator: (store.reportResult?.scores || []).map(item => ({ name: item.name, max: 100 })),
			radius: '58%',
			center: ['50%', '55%'],
			axisName: { color: '#5c6b7a', fontSize: 11 },
			splitArea: { areaStyle: { color: ['#f8fbff', '#eef6ff'] } }
		},
		series: [
			{
				type: 'radar',
				data: [
					{
						value: (store.reportResult?.scores || []).map(item => item.value),
						areaStyle: { color: 'rgba(22, 119, 255, 0.22)' },
						lineStyle: { color: '#1677FF' },
						itemStyle: { color: '#1677FF' }
					}
				]
			}
		]
	}))
	const adviceList = computed(() => {
		const actions = store.reportResult?.actions
		if (actions) return [...(actions.immediate || []), ...(actions.shortTerm || []).slice(0, 2)]
		return ['请先生成综合评估报告。']
	})
</script>

<style lang="scss" scoped>
	.wave-result {
		height: 100%;
		overflow: auto;
	}
	.wave-scene {
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
	.wave-brief {
		margin: 0 0 12px;
		padding: 10px 12px;
		background: #f6f9fd;
		border-radius: 8px;
		color: #3d4a5c;
		font-size: 13px;
		line-height: 22px;
	}
	.wave-split {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 10px;
		margin: 14px 0;
	}
	.wave-chart-box {
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
	.wave-params {
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
	.wave-obj {
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
	.wave-hero {
		display: grid;
		grid-template-columns: 140px 1fr;
		gap: 12px;
		align-items: center;
		padding: 16px 18px;
		border-radius: 12px;
		color: #fff;
		background: linear-gradient(135deg, #0f5fbf 0%, #1677ff 58%, #36cfc9 100%);
		margin-bottom: 4px;
		span,
		em {
			display: block;
			font-size: 12px;
			opacity: 0.86;
			font-style: normal;
		}
		b {
			font-size: 42px;
			line-height: 46px;
			font-weight: 700;
		}
		strong {
			display: block;
			font-size: 24px;
			line-height: 32px;
			margin: 2px 0 4px;
		}
		small {
			opacity: 0.86;
			font-size: 12px;
		}
		&.grade-严重 {
			background: linear-gradient(135deg, #a8071a 0%, #f5222d 60%, #ff7a45 100%);
		}
		&.grade-较重 {
			background: linear-gradient(135deg, #ad4e00 0%, #fa8c16 60%, #ffc53d 100%);
		}
		&.grade-轻度 {
			background: linear-gradient(135deg, #135200 0%, #52c41a 60%, #95de64 100%);
		}
	}
	.wave-score {
		display: grid;
		grid-template-columns: 76px 1fr 32px;
		align-items: center;
		gap: 8px;
		margin-bottom: 8px;
		font-size: 13px;
		color: #3d4a5c;
		.bar {
			height: 8px;
			border-radius: 4px;
			background: #eef2f7;
			overflow: hidden;
			i {
				display: block;
				height: 100%;
				border-radius: 4px;
				background: linear-gradient(90deg, #1677ff, #36cfc9);
			}
		}
		b {
			text-align: right;
			color: #1f2d3d;
		}
	}
	.wave-summary {
		margin: 12px 0 16px;
		padding: 12px 14px;
		background: #f6f9fd;
		border-radius: 8px;
		color: #3d4a5c;
		font-size: 13px;
		line-height: 22px;
		p {
			margin: 0 0 8px;
			&:last-child {
				margin-bottom: 0;
			}
		}
	}
	.wave-advice {
		margin: 0;
		padding: 0;
		list-style: none;
		li {
			position: relative;
			padding: 8px 0 8px 16px;
			color: #3d4a5c;
			font-size: 13px;
			line-height: 20px;
			border-bottom: 1px dashed #eef2f6;
			&::before {
				content: '';
				position: absolute;
				left: 0;
				top: 15px;
				width: 6px;
				height: 6px;
				border-radius: 50%;
				background: #1677ff;
			}
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
