<template>
	<div
		class="wb-result"
		v-loading="store.reportLoading && store.currentStep === 'report'"
		element-loading-text="正在复盘当前场景评估结果…">
		<template v-if="store.currentType === 'wave'">
			<WaveResultPanel />
		</template>

		<template v-else-if="store.currentType === 'wind'">
			<WindResultPanel />
		</template>

		<template v-else-if="store.currentStep === 'integrate'">
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">风暴潮受灾信息整合结果</span></div>
			</div>
			<div class="wb-kpis" style="margin-bottom: 16px">
				<div class="wb-kpi">
					<span>整合记录</span>
					<b>{{ store.integrateList.length }} <small>条</small></b>
				</div>
				<div class="wb-kpi green">
					<span>已标准化</span>
					<b>{{ integrateStats.standardized }} <small>条</small></b>
				</div>
				<div class="wb-kpi orange">
					<span>数据来源</span>
					<b>{{ integrateStats.sourceCount }} <small>类</small></b>
				</div>
				<div class="wb-kpi purple">
					<span>承灾体类型</span>
					<b>{{ integrateStats.hazardCount }} <small>类</small></b>
				</div>
			</div>
			<div class="storm-split">
				<div class="storm-chart">
					<ECharts key="storm-source" :option="sourcePieOption" :resize="false" />
				</div>
				<div class="storm-chart">
					<ECharts key="storm-hazard" :option="hazardBarOption" :resize="false" />
				</div>
			</div>
			<ZTableCom
				:tableHeaders="integrateHeaders"
				:tableData="integrateDisplayList"
				:tableHeight="320"
				stripe
				:headerCellStyle="tableHeadStyle">
				<template #detail="{ scope }">
					<el-button link type="primary" size="small" @click="openStormDetail(scope.row)">详情</el-button>
				</template>
			</ZTableCom>
		</template>

		<template v-else-if="store.currentStep === 'loss'">
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">灾后经济损失预估</span></div>
			</div>
			<template v-if="store.lossResult">
				<div class="storm-scene">
					<span>{{ store.lossResult.sceneName }}</span>
					<em>{{ store.lossResult.sceneDate }} · {{ store.lossResult.sensor }}</em>
				</div>
				<div class="wb-kpis" style="margin-bottom: 16px">
					<div class="wb-kpi">
						<span>评估区县</span>
						<b>{{ store.queriedDistrict?.name || '-' }}</b>
					</div>
					<div class="wb-kpi red">
						<span>受灾面积</span>
						<b>{{ store.lossResult.affectedKm2 }} <small>km²</small></b>
					</div>
					<div class="wb-kpi orange">
						<span>经济损失</span>
						<b>{{ store.lossResult.totalLoss }} <small>万元</small></b>
					</div>
					<div class="wb-kpi purple">
						<span>占 GDP</span>
						<b>{{ store.lossResult.gdpShock }} <small>%</small></b>
					</div>
				</div>
				<div class="storm-params">
					<div>
						<span>县域 GDP</span>
						<b>{{ store.lossResult.gdp }} 亿元</b>
					</div>
					<div>
						<span>受灾人口</span>
						<b>{{ store.lossResult.summary?.counties?.[0]?.affectedPopulation || '-' }} 人</b>
					</div>
					<div>
						<span>行政区面积</span>
						<b>{{ store.queriedDistrict?.statArea || '-' }} km²</b>
					</div>
					<div>
						<span>用地类型</span>
						<b>{{ store.lossResult.rows?.length || 0 }} 类</b>
					</div>
				</div>
				<p class="storm-hint">{{ store.lossResult.landHint }}</p>
				<div class="storm-chart storm-chart--wide">
					<ECharts key="storm-land" :option="landPieOption" :resize="false" />
				</div>
				<ZTableCom
					:tableHeaders="lossHeaders"
					:tableData="store.lossResult.rows"
					:tableHeight="240"
					stripe
					:headerCellStyle="tableHeadStyle" />
			</template>
			<div v-else class="wb-empty">
				<el-empty description="请查询区县并开始评估" :image-size="84" />
			</div>
		</template>

		<template v-else-if="store.currentStep === 'overtop'">
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">漫堤分析结果</span></div>
			</div>
			<template v-if="store.overtoppingRows.length">
				<div class="wb-kpis" style="margin-bottom: 16px">
					<div class="wb-kpi">
						<span>核查记录</span>
						<b>{{ store.overtoppingRows.length }} <small>条</small></b>
					</div>
					<div class="wb-kpi green">
						<span>有防浪墙</span>
						<b>{{ overtopStats.withWall }} <small>处</small></b>
					</div>
					<div class="wb-kpi orange">
						<span>预警超限</span>
						<b>{{ overtopStats.overWarn }} <small>处</small></b>
					</div>
				<div class="wb-kpi purple">
					<span>保护人口</span>
					<b>{{ overtopStats.protectPop }} <small>人</small></b>
				</div>
				</div>
				<ZTableCom
					:tableHeaders="overtopHeaders"
					:tableData="store.overtoppingRows"
					:tableHeight="360"
					stripe
					:headerCellStyle="tableHeadStyle" />
			</template>
			<div v-else class="wb-empty">
				<el-empty description="请加载漫堤分析数据" :image-size="84" />
			</div>
		</template>

		<template v-else>
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">风暴潮综合评估</span></div>
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
						<span>经济损失</span>
						<b>{{ store.reportResult.economy?.totalLoss || '-' }} <small>万元</small></b>
					</div>
					<div class="wb-kpi purple">
						<span>上报记录</span>
						<b>{{ store.reportResult.integrateRecords?.length || 0 }} <small>条</small></b>
					</div>
				</div>
				<div class="wb-chart" style="height: 240px">
					<ECharts :option="scoreOption" :resize="false" />
				</div>
				<p class="wb-desc" style="margin-top: 14px" v-for="(text, index) in summaryList" :key="index">{{ text }}</p>
				<template v-if="store.archivedReports.length">
					<div class="wb-title" style="margin-top: 16px"><span class="txt">已归档报告</span></div>
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
	import ZTableCom from '@/components/table/index.vue'
	import WaveResultPanel from './WaveResultPanel.vue'
	import WindResultPanel from './WindResultPanel.vue'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useAssessmentActions } from '@/views/assessment/composables/useAssessmentActions.js'
	import {
		formatImpactLevelFull,
		SURVEY_LABELS,
		warnColorToImpactLevel
	} from '@/shared/metricUnits.js'

	const store = useAssessmentStore()
	const { openStormDetail, openArchivedReport } = useAssessmentActions()
	const integrateDisplayList = computed(() =>
		store.integrateList.map(item => {
			const impact = warnColorToImpactLevel(item.level)
			return {
				...item,
				impactLevelLabel: impact ? formatImpactLevelFull(impact) : '-'
			}
		})
	)
	const overtopStats = computed(() => {
		const rows = store.overtoppingRows || []
		return {
			withWall: rows.filter(item => String(item.hasWall || '').includes('有')).length,
			overWarn: rows.filter(item => item.warnLevel && item.warnLevel !== '不超').length,
			protectPop: rows.reduce((sum, item) => sum + (Number(item.protectPop) || 0), 0)
		}
	})
	const summaryList = computed(() => {
		const summary = store.reportResult?.summary
		return Array.isArray(summary) ? summary : summary ? [summary] : []
	})
	const tableHeadStyle = {
		background: '#f4f7fb',
		color: '#3d4a5c',
		fontWeight: '600',
		fontSize: '13px'
	}
	const gradeAccent = computed(() => {
		const map = { 轻度: 'green', 中度: '', 较重: 'orange', 严重: 'red' }
		return map[store.reportResult?.grade] || ''
	})
	const integrateStats = computed(() => ({
		standardized: store.integrateList.filter(item => item.status === '已标准化').length,
		sourceCount: new Set(store.integrateList.map(item => item.sourceId)).size,
		hazardCount: new Set(store.integrateList.map(item => item.hazardType)).size
	}))
	const sourcePieOption = computed(() => {
		const groups = {}
		store.integrateList.forEach(item => {
			groups[item.sourceName] = (groups[item.sourceName] || 0) + 1
		})
		return {
			tooltip: { trigger: 'item' },
			legend: { bottom: 0, itemWidth: 8, itemHeight: 8, textStyle: { color: '#5c6b7a', fontSize: 11 } },
			series: [
				{
					type: 'pie',
					radius: ['46%', '68%'],
					center: ['50%', '42%'],
					label: { show: false },
					data: Object.keys(groups).map(name => ({ name, value: groups[name] }))
				}
			]
		}
	})
	const hazardBarOption = computed(() => {
		const groups = {}
		store.integrateList.forEach(item => {
			groups[item.hazardType] = (groups[item.hazardType] || 0) + 1
		})
		return {
			tooltip: { trigger: 'axis' },
			grid: { top: 24, left: 8, right: 8, bottom: 8, containLabel: true },
			xAxis: {
				type: 'category',
				data: Object.keys(groups),
				axisTick: { show: false },
				axisLine: { lineStyle: { color: '#d9e0ea' } },
				axisLabel: { color: '#5c6b7a', fontSize: 11 }
			},
			yAxis: {
				type: 'value',
				minInterval: 1,
				splitLine: { lineStyle: { color: '#eef2f7' } },
				axisLabel: { color: '#8a97a8', fontSize: 11 }
			},
			series: [
				{
					type: 'bar',
					barWidth: 16,
					data: Object.values(groups),
					itemStyle: { color: '#1677FF', borderRadius: [4, 4, 0, 0] }
				}
			]
		}
	})
	const landPieOption = computed(() => ({
		tooltip: { trigger: 'item' },
		legend: { bottom: 0, itemWidth: 8, itemHeight: 8, textStyle: { color: '#5c6b7a', fontSize: 11 } },
		series: [
			{
				type: 'pie',
				radius: ['42%', '68%'],
				center: ['50%', '46%'],
				label: { formatter: '{b} {d}%', fontSize: 11 },
				data: (store.lossResult?.rows || []).map(item => ({ name: item.type, value: item.loss }))
			}
		]
	}))
	const integrateHeaders = [
		{ label: '来源', prop: 'sourceName', width: 140 },
		{ label: '时间', prop: 'reportTime', width: 130 },
		{ label: '区县', prop: 'county', width: 80 },
		{ label: '承灾体', prop: 'hazardType', width: 90 },
		{ label: '名称', prop: 'hazardName', width: 110 },
		{ label: '受损情况', prop: 'damage' },
		{ label: '设施', prop: 'facility' },
		{ label: '预警色', prop: 'level', width: 70 },
		{ label: '影响等级', prop: 'impactLevelLabel', width: 100 },
		{ label: '状态', prop: 'status', width: 80 },
		{ label: '操作', slot: 'detail', width: 72, fixed: 'right' }
	]
	const overtopHeaders = [
		{ label: '所在市', prop: 'city', width: 90 },
		{ label: '海堤名称', prop: 'name', width: 140 },
		{ label: '乡镇', prop: 'location', width: 100 },
		{ label: SURVEY_LABELS.warnTide, prop: 'warnTide', width: 110 },
		{ label: SURVEY_LABELS.tideHeight, prop: 'tideHeight', width: 90 },
		{ label: SURVEY_LABELS.protectPop, prop: 'protectPop', width: 110 },
		{ label: SURVEY_LABELS.crestElev, prop: 'crestElev', width: 110 },
		{ label: '防浪墙', prop: 'hasWall', width: 80 },
		{ label: SURVEY_LABELS.wallElev, prop: 'wallElev', width: 120 },
		{ label: '预警', prop: 'warnLevel', width: 80 },
		{ label: '潮位站', prop: 'tideStation', width: 110 }
	]
	const lossHeaders = [
		{ label: '用地类型', prop: 'type' },
		{ label: '用地占比(%)', prop: 'ratioPct', width: 110 },
		{ label: SURVEY_LABELS.areaKm2, prop: 'area' },
		{ label: '单价(万元/km²)', prop: 'unit' },
		{ label: '损失(万元)', prop: 'loss' }
	]
	const scoreOption = computed(() => ({
		tooltip: { trigger: 'axis' },
		grid: { top: 24, left: 12, right: 12, bottom: 8, containLabel: true },
		xAxis: {
			type: 'category',
			data: (store.reportResult?.scores || []).map(item => item.name),
			axisTick: { show: false },
			axisLine: { lineStyle: { color: '#d9e0ea' } },
			axisLabel: { color: '#5c6b7a', interval: 0 }
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
				barWidth: 22,
				data: (store.reportResult?.scores || []).map(item => item.value),
				itemStyle: {
					borderRadius: [4, 4, 0, 0],
					color: {
						type: 'linear',
						x: 0,
						y: 0,
						x2: 0,
						y2: 1,
						colorStops: [
							{ offset: 0, color: '#36CFC9' },
							{ offset: 1, color: '#1677FF' }
						]
					}
				}
			}
		]
	}))
</script>

<style lang="scss" scoped>
	.storm-split {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 10px;
		margin-bottom: 14px;
	}
	.storm-chart {
		height: 176px;
		background: #f8fbff;
		border: 1px solid #edf2f8;
		border-radius: 8px;
		padding: 4px;
		&--wide {
			width: 100%;
			height: 210px;
			margin-bottom: 14px;
		}
	}
	.storm-scene {
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
	.storm-params {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 8px;
		margin: 0 0 12px;
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
				font-size: 13px;
			}
		}
	}
	.storm-hint {
		margin: 0 0 12px;
		padding: 8px 12px;
		background: #f6f9fd;
		border-radius: 8px;
		color: #5c6b7a;
		font-size: 12px;
		line-height: 20px;
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
