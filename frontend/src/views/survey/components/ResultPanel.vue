<template>
	<div class="wb-result">
		<template v-if="surveyStore.currentStep === 'query'">
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">查询结果</span></div>
				<el-button
					v-if="surveyStore.selectedHazard"
					size="small"
					type="primary"
					@click="openHazardDetail">
					查看详情
				</el-button>
			</div>
			<template v-if="surveyStore.queriedDistrict || surveyStore.queriedTown || surveyStore.selectedHazard">
				<div class="wb-kpis" v-if="surveyStore.queriedTown">
					<div class="wb-kpi">
						<span>乡镇</span>
						<b>{{ surveyStore.queriedTown.name }}</b>
					</div>
					<div class="wb-kpi">
						<span>所属区县</span>
						<b>{{ surveyStore.queriedTown.county }}</b>
					</div>
					<div class="wb-kpi">
						<span>所属地市</span>
						<b>{{ surveyStore.queriedTown.city }}</b>
					</div>
					<div class="wb-kpi green">
						<span>乡镇面积</span>
						<b>{{ formatNum(surveyStore.queriedTown.statArea) }} <small>km²</small></b>
					</div>
					<div class="wb-kpi orange">
						<span>常住人口</span>
						<b>{{ formatNum(surveyStore.queriedTown.population) }} <small>万人</small></b>
					</div>
					<div class="wb-kpi purple">
						<span>人口密度</span>
						<b>{{ formatNum(surveyStore.queriedTown.density) }} <small>人/km²</small></b>
					</div>
				</div>
				<div class="wb-kpis" v-else-if="surveyStore.queriedDistrict">
					<div class="wb-kpi">
						<span>行政区划</span>
						<b>{{ surveyStore.queriedDistrict.name }}</b>
					</div>
					<div class="wb-kpi">
						<span>所属地市</span>
						<b>{{ surveyStore.queriedDistrict.city || '-' }}</b>
					</div>
					<div class="wb-kpi green">
						<span>面积</span>
						<b>{{ formatNum(surveyStore.queriedDistrict.statArea) }} <small>km²</small></b>
					</div>
					<div class="wb-kpi orange">
						<span>常住人口</span>
						<b>{{ formatNum(surveyStore.queriedDistrict.population) }} <small>万人</small></b>
					</div>
					<div class="wb-kpi purple">
						<span>人口密度</span>
						<b>{{ formatNum(surveyStore.queriedDistrict.density) }} <small>人/km²</small></b>
					</div>
				</div>
				<div v-if="surveyStore.selectedHazard" style="margin-top: 16px">
					<div class="wb-title"><span class="txt">承灾体基础信息</span></div>
					<div class="wb-kv">
						<p><span>名称</span>{{ surveyStore.selectedHazard.name }}</p>
						<p><span>类型</span>{{ surveyStore.selectedHazard.type || '-' }}</p>
						<p><span>乡镇</span>{{ surveyStore.selectedHazard.town || '-' }}</p>
						<p><span>行政区划</span>{{ surveyStore.selectedHazard.division || '-' }}</p>
						<p v-if="surveyStore.selectedHazard.lengthKm || surveyStore.selectedHazard.length">
							<span>长度</span>{{ surveyStore.selectedHazard.lengthKm || surveyStore.selectedHazard.length }} km
						</p>
						<p v-if="surveyStore.selectedHazard.areaKm2 || surveyStore.selectedHazard.area">
							<span>面积</span>{{ surveyStore.selectedHazard.areaKm2 || surveyStore.selectedHazard.area }} km²
						</p>
						<p v-if="surveyStore.selectedHazard.grade"><span>工程等级</span>{{ surveyStore.selectedHazard.grade }}</p>
						<p v-if="surveyStore.selectedHazard.standard || surveyStore.selectedHazard.Return_per">
							<span>设防标准</span>{{ surveyStore.selectedHazard.standard || surveyStore.selectedHazard.Return_per }}
						</p>
						<p v-if="surveyStore.selectedHazard.level"><span>景区等级</span>{{ surveyStore.selectedHazard.level }}</p>
						<p v-if="surveyStore.selectedHazard.berths"><span>泊位数</span>{{ surveyStore.selectedHazard.berths }}</p>
						<p v-if="surveyStore.selectedHazard.capacity"><span>容量</span>{{ surveyStore.selectedHazard.capacity }}</p>
						<p v-if="surveyStore.selectedHazard.species"><span>养殖品种</span>{{ surveyStore.selectedHazard.species }}</p>
						<p v-if="surveyStore.selectedHazard.method"><span>养殖方式</span>{{ surveyStore.selectedHazard.method }}</p>
						<p v-if="surveyStore.selectedHazard.builtYear || surveyStore.selectedHazard.time">
							<span>建成年份</span>{{ surveyStore.selectedHazard.builtYear || surveyStore.selectedHazard.time }}
						</p>
						<p>
							<span>管护单位</span>
							{{ surveyStore.selectedHazard.manager || surveyStore.selectedHazard.host || surveyStore.selectedHazard.Management || '-' }}
						</p>
						<p><span>状态</span>{{ surveyStore.selectedHazard.status || '-' }}</p>
					</div>
					<p class="detail-tip">可进入详情查阅调查明细、现场资料与附件，完成核对并归档留底。</p>
				</div>
			</template>
			<div v-else class="wb-empty">
				<el-empty description="请在左侧查询区划、乡镇或承灾体" :image-size="84" />
			</div>
		</template>

		<template v-else-if="surveyStore.currentStep === 'identify'">
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">受灾范围识别结果</span></div>
				<el-button
					v-if="surveyStore.selectedHazard"
					size="small"
					type="primary"
					@click="openHazardDetail">
					查看详情
				</el-button>
			</div>
			<section v-if="surveyStore.selectedHazard" class="verify-area">
				<div class="wb-title"><span class="txt">灾后数据面积</span></div>
				<p class="verify-name">{{ surveyStore.selectedHazard.name }}</p>
				<div class="wb-kpis">
					<div class="wb-kpi">
						<span>{{ monitorPlan.monitor }}</span>
						<b>{{ formatNum(surveyStore.selectedHazard.monitorValue) }} <small>{{ monitorPlan.unit }}</small></b>
					</div>
					<div class="wb-kpi green">
						<span>{{ monitorPlan.plan }}</span>
						<b>{{ formatNum(surveyStore.selectedHazard.planValue) }} <small>{{ monitorPlan.unit }}</small></b>
					</div>
				</div>
			</section>
			<template v-if="surveyStore.affectedMeta">
				<div class="wb-kpis" style="margin-bottom: 16px">
					<div class="wb-kpi red">
						<span>{{ SURVEY_KPI_NAMES.areaKm2 }}</span>
						<b>{{ surveyStore.affectedMeta.areaKm2 }} <small>km²</small></b>
					</div>
					<div class="wb-kpi orange">
						<span>{{ SURVEY_KPI_NAMES.areaWanMu }}</span>
						<b>{{ surveyStore.affectedMeta.areaWanMu }} <small>万亩</small></b>
					</div>
					<div class="wb-kpi">
						<span>覆盖区县</span>
						<b>{{ surveyStore.affectedMeta.countyCount }} <small>个</small></b>
					</div>
				</div>
				<ZTableCom
					:tableHeaders="identifyHeaders"
					:tableData="surveyStore.affectedMeta.counties"
					:tableHeight="360"
					stripe
					:headerCellStyle="tableHeadStyle" />
			</template>
			<div v-else class="wb-empty">
				<el-empty description="请先选择遥感场景并开始解译" :image-size="84" />
			</div>
		</template>

		<template v-else-if="surveyStore.currentStep === 'overlay'">
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">{{ overlayTitle }}</span></div>
				<div class="wb-result-actions">
					<el-button
						v-if="surveyStore.selectedHazard"
						size="small"
						type="primary"
						plain
						@click="openHazardDetail">
						查看详情
					</el-button>
					<el-button v-if="canExportOverlay" size="small" type="primary" @click="exportOverlayImage">导出影像</el-button>
				</div>
			</div>
			<template v-if="surveyStore.overlayResult">
				<section v-if="surveyStore.selectedHazard" class="verify-area">
					<div class="wb-title"><span class="txt">灾后数据面积</span></div>
					<p class="verify-name">{{ surveyStore.selectedHazard.name }}</p>
					<div class="wb-kpis">
						<div class="wb-kpi">
							<span>{{ monitorPlan.monitor }}</span>
							<b>{{ formatNum(surveyStore.selectedHazard.monitorValue) }} <small>{{ monitorPlan.unit }}</small></b>
						</div>
						<div class="wb-kpi green">
							<span>{{ monitorPlan.plan }}</span>
							<b>{{ formatNum(surveyStore.selectedHazard.planValue) }} <small>{{ monitorPlan.unit }}</small></b>
						</div>
					</div>
				</section>
				<section v-if="levelComposition.length" class="verify-area">
					<div class="wb-title"><span class="txt">受灾范围与{{ overlayHazardLabel }}叠置 · 一至四级构成</span></div>
					<div class="level-list">
						<div v-for="item in levelComposition" :key="item.level" class="level-row">
							<span class="level-name">{{ item.label }}</span>
							<span class="level-bar"><i :style="{ width: item.percent + '%' }"></i></span>
							<span class="level-meta">{{ item.count }} 处 · {{ item.area }} {{ item.unit }}</span>
						</div>
					</div>
				</section>
				<div class="wb-table-scroll">
					<ZTableCom
						:tableHeaders="overlayHeaders"
						:tableData="surveyStore.overlayResult.rows"
						:tableHeight="280"
						stripe
						:headerCellStyle="tableHeadStyle" />
				</div>
			</template>
			<div v-else class="wb-empty">
				<el-empty description="请选择叠置目标并开始分析" :image-size="84" />
			</div>
		</template>

		<template v-else>
			<div class="wb-result-head">
				<div class="wb-title"><span class="txt">灾后分析结果与上报数据对比分析</span></div>
				<el-button
					v-if="surveyStore.selectedHazard"
					size="small"
					type="primary"
					@click="openHazardDetail">
					查看详情
				</el-button>
			</div>
			<template v-if="surveyStore.compareRows.length">
				<section v-if="surveyStore.selectedHazard" class="verify-area">
					<div class="wb-title"><span class="txt">灾后数据面积</span></div>
					<p class="verify-name">{{ surveyStore.selectedHazard.name }}</p>
					<div class="wb-kpis">
						<div class="wb-kpi">
							<span>{{ monitorPlan.monitor }}</span>
							<b>{{ formatNum(surveyStore.selectedHazard.monitorValue) }} <small>{{ monitorPlan.unit }}</small></b>
						</div>
						<div class="wb-kpi green">
							<span>{{ monitorPlan.plan }}</span>
							<b>{{ formatNum(surveyStore.selectedHazard.planValue) }} <small>{{ monitorPlan.unit }}</small></b>
						</div>
					</div>
				</section>
				<div v-if="surveyStore.compareView === 'chart'" class="wb-chart">
					<ECharts :option="chartOption" :resize="false" />
				</div>
				<ZTableCom
					v-else
					:tableHeaders="compareHeaders"
					:tableData="surveyStore.compareRows"
					:tableHeight="420"
					stripe
					:headerCellStyle="tableHeadStyle" />
			</template>
			<div v-else class="wb-empty">
				<el-empty description="请选择承灾体类型并生成对比" :image-size="84" />
			</div>
		</template>

		<section v-if="archivedList.length" class="archive-ledger">
			<div class="wb-title"><span class="txt">已归档底账</span></div>
			<div
				v-for="item in archivedList"
				:key="item.id"
				class="archive-ledger__item"
				@click="openArchived(item)">
				<p class="name">{{ item.hazardName }}</p>
				<p class="meta">{{ item.archiveNo }} · {{ item.archivedAt }} · {{ item.verifyStatus }}</p>
			</div>
		</section>

		<HazardDetailDrawer
			v-model="detailVisible"
			:type-id="detailTypeId"
			:hazard="detailHazard"
			:overlay-note="overlayNote"
			:compare-note="compareNote"
			@archived="refreshArchived" />
	</div>
</template>

<script setup>
	import { computed, onMounted, ref } from 'vue'
	import { ElMessage } from 'element-plus'
	import ECharts from '@/components/eCharts/index.vue'
	import ZTableCom from '@/components/table/index.vue'
	import HazardDetailDrawer from './HazardDetailDrawer.vue'
	import { getOverlayTarget } from '@/views/survey/constants.js'
	import { useSurveyStore } from '@/store/modules/survey.js'
	import { useSurveyActions } from '@/views/survey/composables/useSurveyActions.js'
	import { surveyApi } from '@/api/survey/index.js'
	import {
		formatDisasterLevel,
		monitorPlanLabels,
		SURVEY_KPI_NAMES,
		SURVEY_LABELS
	} from '@/shared/metricUnits.js'

	const surveyStore = useSurveyStore()
	const { exportOverlayImage } = useSurveyActions()
	const api = surveyApi()
	const tableHeadStyle = {
		background: '#f4f7fb',
		color: '#3d4a5c',
		fontWeight: '600',
		fontSize: '13px'
	}

	const detailVisible = ref(false)
	const detailTypeId = ref('')
	const detailHazard = ref(null)
	const archivedList = ref([])

	const formatNum = value => (value === null || value === undefined || value === '' ? '-' : value)
	const monitorPlan = computed(() => monitorPlanLabels(surveyStore.selectedHazardType))

	const overlayNote = computed(() => {
		const result = surveyStore.overlayResult
		if (!result || !surveyStore.selectedHazard) return ''
		const hit = (result.rows || []).find(row => row.name === surveyStore.selectedHazard.name)
		if (!hit) return `当前叠置目标：${getOverlayTarget(surveyStore.overlayTarget)?.label || '-'}，本对象未出现在结果行中。`
		const levelText = hit.disasterLevelLabel || formatDisasterLevel(hit.disasterLevel)
		return `叠置命中：${SURVEY_LABELS.disasterLevel} ${levelText}，${SURVEY_LABELS.areaKm2} ${hit.areaKm2 ?? '-'}（${hit.areaWanMu ?? '-'} 万亩），${SURVEY_LABELS.affectedPopulation} ${hit.affectedPopulation ?? '-'}，${SURVEY_LABELS.economicLoss} ${hit.economicLoss ?? '-'}。`
	})

	const compareNote = computed(() => {
		if (!surveyStore.compareRows?.length) return ''
		return `对比样本 ${surveyStore.compareRows.length} 条，含解译与上报口径对照。`
	})

	const openHazardDetail = () => {
		if (!surveyStore.selectedHazard) {
			ElMessage.warning('请先选择海堤、渔港、养殖区或旅游区对象')
			return
		}
		detailTypeId.value = surveyStore.selectedHazardType
		detailHazard.value = surveyStore.selectedHazard
		detailVisible.value = true
	}

	const refreshArchived = async () => {
		try {
			archivedList.value = (await api.listArchivedDossiers()).slice(0, 8)
		} catch (e) {
			archivedList.value = []
		}
	}

	const openArchived = item => {
		detailTypeId.value = item.hazardType
		detailHazard.value = {
			name: item.hazardName,
			...(item.profile || {})
		}
		detailVisible.value = true
	}

	onMounted(() => {
		refreshArchived()
	})

	const identifyHeaders = [
		{ label: '区县', prop: 'name' },
		{ label: '地市', prop: 'city' },
		{ label: SURVEY_LABELS.areaKm2, prop: 'areaKm2' },
		{ label: SURVEY_LABELS.areaWanMu, prop: 'areaWanMu' },
		{ label: SURVEY_LABELS.density, prop: 'density' },
		{ label: SURVEY_LABELS.affectedPopulation, prop: 'affectedPopulation' }
	]

	const canExportOverlay = computed(() => {
		const target = getOverlayTarget(surveyStore.overlayTarget)
		return target?.kind === 'hazard' && !!surveyStore.overlayResult
	})

	const overlayTitle = computed(() => {
		const target = getOverlayTarget(surveyStore.overlayTarget)
		if (target?.id === 'population') return '受灾范围与人口密度叠置结果'
		if (target?.id === 'district') return '受灾范围与乡镇行政区划叠置结果'
		return `受灾范围与${target?.label || ''}叠置结果`
	})

	const LEVELS = [1, 2, 3, 4].map(level => ({
		level,
		label: formatDisasterLevel(level)
	}))

	const overlayHazardLabel = computed(() => getOverlayTarget(surveyStore.overlayTarget)?.label || '承灾体')

	const levelComposition = computed(() => {
		const target = getOverlayTarget(surveyStore.overlayTarget)
		const rows = surveyStore.overlayResult?.rows || []
		if (target?.kind !== 'hazard' || !rows.length) return []
		const total = rows.length || 1
		const useLength = surveyStore.selectedHazardType === 'seawall'
		return LEVELS.map(item => {
			const group = rows.filter(row => Number(row.disasterLevel) === item.level)
			const area = group.reduce(
				(sum, row) => sum + (useLength ? Number(row.lengthKm) || 0 : Number(row.areaKm2) || 0),
				0
			)
			return {
				...item,
				count: group.length,
				area: area ? area.toFixed(2) : '0',
				unit: useLength ? 'km' : 'km²',
				percent: Math.round((group.length / total) * 100)
			}
		})
	})

	const overlayHeaders = computed(() => {
		const target = getOverlayTarget(surveyStore.overlayTarget)
		if (target?.id === 'population') {
			return [
				{ label: '区县', prop: 'name' },
				{ label: '地市', prop: 'city' },
				{ label: SURVEY_LABELS.areaKm2, prop: 'areaKm2' },
				{ label: SURVEY_LABELS.areaWanMu, prop: 'areaWanMu' },
				{ label: SURVEY_LABELS.density, prop: 'density' },
				{ label: SURVEY_LABELS.affectedPopulation, prop: 'affectedPopulation' }
			]
		}
		if (target?.id === 'district') {
			return [
				{ label: '乡镇', prop: 'name' },
				{ label: '所属区县', prop: 'county' },
				{ label: '地市', prop: 'city' },
				{ label: SURVEY_LABELS.areaKm2, prop: 'areaKm2' },
				{ label: SURVEY_LABELS.areaWanMu, prop: 'areaWanMu' }
			]
		}
		return [
			{ label: '名称', prop: 'name' },
			{ label: SURVEY_LABELS.disasterLevel, prop: 'disasterLevelLabel' },
			{ label: '乡镇', prop: 'town' },
			{ label: SURVEY_LABELS.areaKm2, prop: 'areaKm2' },
			{ label: SURVEY_LABELS.areaWanMu, prop: 'areaWanMu' },
			{ label: SURVEY_LABELS.affectedPopulation, prop: 'affectedPopulation' },
			{ label: SURVEY_LABELS.economicLoss, prop: 'economicLoss' }
		]
	})

	const compareHeaders = [
		{ label: '数据来源', prop: 'source' },
		{ label: SURVEY_LABELS.affectedPopulation, prop: 'population' },
		{ label: SURVEY_LABELS.areaKm2, prop: 'area' },
		{ label: SURVEY_LABELS.economicLoss, prop: 'loss' }
	]

	const chartOption = computed(() => {
		const rows = surveyStore.compareRows || []
		const categories = rows.map(item => item.source)
		const needRotate = categories.some(item => String(item || '').length > 6)
		return {
			tooltip: {
				trigger: 'axis',
				axisPointer: { type: 'shadow' },
				confine: true
			},
			legend: {
				bottom: 4,
				left: 'center',
				icon: 'roundRect',
				itemWidth: 10,
				itemHeight: 8,
				itemGap: 18,
				textStyle: { fontSize: 11, color: '#5c6b7a' }
			},
			grid: {
				top: 28,
				left: 8,
				right: 8,
				bottom: 36,
				containLabel: true
			},
			xAxis: {
				type: 'category',
				data: categories,
				axisTick: { alignWithLabel: true, show: false },
				axisLine: { lineStyle: { color: '#d9e0ea' } },
				axisLabel: {
					color: '#5c6b7a',
					fontSize: 11,
					interval: 0,
					hideOverlap: true,
					rotate: needRotate ? 25 : 0,
					formatter: value => {
						const text = String(value || '')
						return text.length > 10 ? `${text.slice(0, 10)}…` : text
					}
				}
			},
			yAxis: [
				{
					type: 'value',
					name: '人口/损失',
					nameGap: 10,
					splitNumber: 4,
					splitLine: { lineStyle: { color: '#eef2f7' } },
					axisLabel: { color: '#8a97a8', fontSize: 11 },
					nameTextStyle: { color: '#8a97a8', fontSize: 11, align: 'left' }
				},
				{
					type: 'value',
					name: '面积(km²)',
					nameGap: 10,
					splitNumber: 4,
					splitLine: { show: false },
					axisLabel: { color: '#8a97a8', fontSize: 11 },
					nameTextStyle: { color: '#8a97a8', fontSize: 11, align: 'right' }
				}
			],
			series: [
				{
					name: SURVEY_LABELS.affectedPopulation,
					type: 'bar',
					barMaxWidth: 16,
					barGap: '35%',
					barCategoryGap: '48%',
					data: rows.map(item => item.population || 0),
					itemStyle: { color: '#1677FF', borderRadius: [3, 3, 0, 0] }
				},
				{
					name: SURVEY_LABELS.economicLoss,
					type: 'bar',
					barMaxWidth: 16,
					data: rows.map(item => item.loss || 0),
					itemStyle: { color: '#FA8C16', borderRadius: [3, 3, 0, 0] }
				},
				{
					name: SURVEY_LABELS.areaKm2,
					type: 'line',
					yAxisIndex: 1,
					smooth: true,
					symbol: 'circle',
					symbolSize: 8,
					lineStyle: { width: 2, color: '#36CFC9' },
					itemStyle: { color: '#36CFC9' },
					areaStyle: { color: 'rgba(54, 207, 201, 0.12)' },
					data: rows.map(item => item.area || 0)
				}
			]
		}
	})
</script>

<style lang="scss" scoped>
	.wb-result-actions {
		display: flex;
		align-items: center;
		gap: 8px;
	}
	.wb-table-scroll {
		width: 100%;
		min-height: 0;
		margin-top: 4px;
		:deep(.el-table) {
			width: 100%;
		}
		:deep(.el-table__body-wrapper) {
			overflow-y: auto !important;
		}
		:deep(.el-scrollbar__wrap) {
			overflow-x: auto;
		}
	}
	.detail-tip {
		margin: 10px 0 0;
		font-size: 12px;
		color: #8a97a8;
		line-height: 1.5;
	}
	.verify-area {
		margin-bottom: 16px;
	}
	.verify-name {
		margin: 0 0 8px;
		font-size: 13px;
		color: #5c6b7a;
	}
	.level-list {
		display: flex;
		flex-direction: column;
		gap: 8px;
	}
	.level-row {
		display: grid;
		grid-template-columns: 72px 1fr auto;
		gap: 8px;
		align-items: center;
		font-size: 12px;
		color: #3d4a5c;
	}
	.level-bar {
		height: 8px;
		border-radius: 4px;
		background: #eef2f7;
		overflow: hidden;
		i {
			display: block;
			height: 100%;
			background: #1677ff;
		}
	}
	.level-meta {
		color: #8a97a8;
		white-space: nowrap;
	}
	.archive-ledger {
		margin-top: 18px;
		padding-top: 12px;
		border-top: 1px solid #eef2f7;
		&__item {
			padding: 8px 10px;
			border-radius: 6px;
			cursor: pointer;
			transition: background 0.15s ease;
			&:hover {
				background: #f4f7fb;
			}
			.name {
				margin: 0;
				font-size: 13px;
				font-weight: 600;
				color: #1a3a5c;
			}
			.meta {
				margin: 2px 0 0;
				font-size: 12px;
				color: #8a97a8;
			}
		}
	}
</style>
