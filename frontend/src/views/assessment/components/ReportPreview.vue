<template>
	<el-dialog
		v-model="store.reportVisible"
		width="920px"
		append-to-body
		align-center
		class="wb-report-dialog"
		:show-close="false">
		<div v-if="r" class="wb-report" id="assessment-report">
			<div class="rpt-cover">
				<button class="report-close" type="button" data-print="ignore" @click="store.reportVisible = false">×</button>
				<p class="rpt-org">{{ r.org }}</p>
				<p class="rpt-en">POST-DISASTER COMPREHENSIVE ASSESSMENT REPORT</p>
				<h1>{{ r.title }}</h1>
				<table class="rpt-meta">
					<tr>
						<th>报告编号</th>
						<td>{{ r.reportNo }}</td>
						<th>灾害类型</th>
						<td>{{ r.typeLabel }}</td>
					</tr>
					<tr>
						<th>评估区域</th>
						<td>{{ r.district }} {{ r.city }}</td>
						<th>过程时相</th>
						<td>{{ r.eventDate }}</td>
					</tr>
					<tr>
						<th>评估方法</th>
						<td colspan="3">{{ r.method }}</td>
					</tr>
					<tr>
						<th>生成时间</th>
						<td>{{ r.createdAt }}</td>
						<th>监测场景</th>
						<td>{{ r.sceneName }}</td>
					</tr>
				</table>
			</div>

			<div class="rpt-body">
				<section>
					<h3>1 评估结论</h3>
					<div class="rpt-hero">
						<div>
							<span>综合指数</span>
							<b>{{ r.total }}</b>
						</div>
						<div>
							<span>灾情等级</span>
							<strong :class="`grade-${r.grade}`">{{ r.grade }}</strong>
							<em>{{ r.gradeMeta.range }}</em>
						</div>
					</div>
					<p v-for="(text, index) in r.summary" :key="index">{{ text }}</p>
					<p class="rpt-callout">等级含义：{{ r.gradeMeta.meaning }}</p>
				</section>

				<section>
					<h3>2 评估范围与主要指标</h3>
					<table class="rpt-table">
						<tr v-for="item in r.overview" :key="item.label">
							<th>{{ item.label }}</th>
							<td>{{ item.value }}</td>
						</tr>
					</table>
				</section>

				<section v-if="r.surveyBaselineSection">
					<h3>2.1 调查基线（摸底成果）</h3>
					<p>{{ r.surveyBaselineSection.paragraph }}</p>
					<ul v-if="r.surveyBaselineSection.overlayLines?.length">
						<li v-for="(line, idx) in r.surveyBaselineSection.overlayLines" :key="idx">{{ line }}</li>
					</ul>
					<p v-if="r.surveyBaselineSection.ledgerHint">{{ r.surveyBaselineSection.ledgerHint }}</p>
				</section>

				<section>
					<h3>3 致灾过程概况</h3>
					<p>{{ r.processText }}</p>
					<div class="rpt-kpis">
						<div v-for="item in r.processParams" :key="item.label">
							<span>{{ item.label }}</span>
							<b>{{ item.value }}</b>
						</div>
					</div>
				</section>

				<section>
					<h3>4 承灾体破坏性分析</h3>
					<table class="rpt-table">
						<thead>
							<tr>
								<th>名称</th>
								<th>对象</th>
								<th>区县</th>
								<th>动力要素</th>
								<th>影响等级</th>
								<th>破坏程度</th>
								<th>分析结论</th>
							</tr>
						</thead>
						<tbody>
							<tr v-for="row in r.damageRows" :key="row.name">
								<td>{{ row.name }}</td>
								<td>{{ row.target }}</td>
								<td>{{ row.county || '-' }}</td>
								<td>{{ damageFactor(row) }}</td>
								<td>{{ row.levelLabel }}</td>
								<td>{{ row.levelText }}</td>
								<td>{{ row.analysis }}</td>
							</tr>
						</tbody>
					</table>
					<p>{{ r.damageConclusion }}</p>
				</section>

				<section>
					<h3>5 经济损失评估</h3>
					<p>
						受灾面积 {{ r.economy.affectedKm2 }} km²，测算经济损失 {{ r.economy.totalLoss }} 万元，约占对照经济体量的
						{{ r.economy.gdpShock }}%。{{ r.economyConclusion }}
					</p>
					<table class="rpt-table" v-if="r.economy.rows?.length">
						<thead>
							<tr>
								<th>用地类型</th>
								<th>{{ SURVEY_LABELS.areaKm2 }}</th>
								<th>单价(万元/km²)</th>
								<th>损失(万元)</th>
							</tr>
						</thead>
						<tbody>
							<tr v-for="row in r.economy.rows" :key="row.type">
								<td>{{ row.type }}</td>
								<td>{{ row.area }}</td>
								<td>{{ row.unit }}</td>
								<td>{{ row.loss }}</td>
							</tr>
							<tr>
								<th>合计</th>
								<td>{{ r.economy.affectedKm2 }}</td>
								<td>—</td>
								<th>{{ r.economy.totalLoss }}</th>
							</tr>
						</tbody>
					</table>
				</section>

				<section>
					<h3>6 动力过程与影响范围</h3>
					<p>{{ r.behaviorText }}</p>
					<table class="rpt-table" v-if="r.erosion?.length">
						<thead>
							<tr>
								<th>区县</th>
								<th>地市</th>
								<th>侵蚀面积（km²）</th>
								<th>岸线长度（km）</th>
							</tr>
						</thead>
						<tbody>
							<tr v-for="row in r.erosion" :key="row.name">
								<td>{{ row.name }}</td>
								<td>{{ row.city || '-' }}</td>
								<td>{{ row.areaKm2 }}</td>
								<td>{{ row.lengthKm }}</td>
							</tr>
						</tbody>
					</table>
					<table class="rpt-table" v-if="r.structures?.length" style="margin-top: 12px">
						<thead>
							<tr>
								<th>结构名称</th>
								<th>类型</th>
								<th>区县</th>
								<th>损害情况</th>
								<th>影响等级 / 破坏程度</th>
							</tr>
						</thead>
						<tbody>
							<tr v-for="row in r.structures" :key="row.name">
								<td>{{ row.name }}</td>
								<td>{{ row.type }}</td>
								<td>{{ row.county || '-' }}</td>
								<td>{{ row.damage }}</td>
								<td>{{ formatImpactLevelFull(row.level) }}</td>
							</tr>
						</tbody>
					</table>
				</section>

				<section v-if="r.integrateRecords?.length">
					<h3>{{ sectionNo('integrate') }} 灾情信息整合</h3>
					<p>已标准化汇集速报及地方上报信息 {{ r.integrateRecords.length }} 条，主要记录如下。</p>
					<table class="rpt-table">
						<thead>
							<tr>
								<th>来源</th>
								<th>时间</th>
								<th>区县</th>
								<th>承灾体</th>
								<th>名称</th>
								<th>受损情况</th>
								<th>预警色</th>
								<th>影响等级</th>
							</tr>
						</thead>
						<tbody>
							<tr v-for="row in r.integrateRecords" :key="row.id">
								<td>{{ row.sourceName }}</td>
								<td>{{ row.reportTime }}</td>
								<td>{{ row.county }}</td>
								<td>{{ row.hazardType }}</td>
								<td>{{ row.hazardName }}</td>
								<td>{{ row.damage }}</td>
								<td>{{ row.level }}</td>
								<td>{{ integrateImpactLabel(row.level) }}</td>
							</tr>
						</tbody>
					</table>
				</section>

				<section v-if="r.overtoppingRows?.length">
					<h3>{{ sectionNo('overtop') }} 漫堤分析</h3>
					<p>{{ r.overtoppingSummary }}</p>
					<table class="rpt-table">
						<thead>
							<tr>
								<th>所在市</th>
								<th>海堤名称</th>
								<th>{{ SURVEY_LABELS.warnTide }}</th>
								<th>{{ SURVEY_LABELS.tideHeight }}</th>
								<th>{{ SURVEY_LABELS.protectPop }}</th>
								<th>{{ SURVEY_LABELS.crestElev }}</th>
								<th>防浪墙</th>
								<th>{{ SURVEY_LABELS.warnLevel }}</th>
							</tr>
						</thead>
						<tbody>
							<tr v-for="row in r.overtoppingRows.slice(0, 20)" :key="row.id">
								<td>{{ row.city }}</td>
								<td>{{ row.name }}</td>
								<td>{{ row.warnTide }}</td>
								<td>{{ row.tideHeight ?? '-' }}</td>
								<td>{{ row.protectPop }}</td>
								<td>{{ row.crestElev }}</td>
								<td>{{ row.hasWall }}</td>
								<td>{{ row.warnLevel }}</td>
							</tr>
						</tbody>
					</table>
				</section>

				<section>
					<h3>{{ sectionNo('score') }} 分项评价与等级判定</h3>
					<p>{{ r.scoringExplain }}</p>
					<table class="rpt-table">
						<thead>
							<tr>
								<th>指标</th>
								<th>权重</th>
								<th>得分</th>
								<th>说明</th>
							</tr>
						</thead>
						<tbody>
							<tr v-for="item in r.scores" :key="item.name">
								<td>{{ item.name }}</td>
								<td>{{ item.weight }}</td>
								<td>{{ item.value }}</td>
								<td>{{ item.desc }}</td>
							</tr>
							<tr>
								<th>综合指数</th>
								<td>100%</td>
								<th>{{ r.total }}</th>
								<th>{{ r.grade }}（{{ r.gradeMeta.range }}）</th>
							</tr>
						</tbody>
					</table>
				</section>

				<section>
					<h3>{{ sectionNo('actions') }} 对策建议</h3>
					<h4>应急处置</h4>
					<ol>
						<li v-for="item in r.actions.immediate" :key="item">{{ item }}</li>
					</ol>
					<h4>近期工作</h4>
					<ol>
						<li v-for="item in r.actions.shortTerm" :key="item">{{ item }}</li>
					</ol>
					<h4>恢复与备灾</h4>
					<ol>
						<li v-for="item in r.actions.midTerm" :key="item">{{ item }}</li>
					</ol>
				</section>

				<section>
					<h3>{{ sectionNo('sources') }} 数据来源与不确定性</h3>
					<h4>数据来源</h4>
					<ol>
						<li v-for="item in r.sources" :key="item">{{ item }}</li>
					</ol>
					<h4>不确定性说明</h4>
					<ol>
						<li v-for="item in r.limitations" :key="item">{{ item }}</li>
					</ol>
					<h4>补充说明</h4>
					<p>{{ r.note }}</p>
				</section>

				<div class="rpt-sign">
					<p>编制单位：{{ r.sign.org }}</p>
					<p>评估：{{ r.sign.role }}　　审核：{{ r.sign.review }}　　日期：{{ r.sign.date }}</p>
					<p class="rpt-foot">本报告仅用于灾后快速评估与决策参考。</p>
				</div>
			</div>
		</div>
		<template #footer>
			<el-button @click="store.reportVisible = false">关闭</el-button>
			<el-button :loading="archiving" @click="onArchive">归档报告</el-button>
			<el-button :loading="printing" @click="onExport">打印 / 导出</el-button>
		</template>
	</el-dialog>
</template>

<script setup>
	import { computed, ref } from 'vue'
	import { ElMessage } from 'element-plus'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useAssessmentActions } from '@/views/assessment/composables/useAssessmentActions.js'
	import {
		formatImpactLevelFull,
		SURVEY_LABELS,
		warnColorToImpactLevel
	} from '@/shared/metricUnits.js'

	const store = useAssessmentStore()
	const { archiveReport, exportRemoteReport } = useAssessmentActions()
	const printing = ref(false)
	const archiving = ref(false)
	const r = computed(() => store.reportResult)
	const integrateImpactLabel = color => {
		const level = warnColorToImpactLevel(color)
		return level ? formatImpactLevelFull(level) : '-'
	}

	const downloadLocalJson = () => {
		if (!r.value) return
		const blob = new Blob([JSON.stringify(r.value, null, 2)], { type: 'application/json;charset=utf-8' })
		const url = URL.createObjectURL(blob)
		const a = document.createElement('a')
		a.href = url
		a.download = `${r.value.reportNo || r.value.title || '评估报告'}.json`
		a.click()
		URL.revokeObjectURL(url)
	}

	const sectionNo = key => {
		let n = 7
		const hasIntegrate = !!r.value?.integrateRecords?.length
		const hasOvertop = !!r.value?.overtoppingRows?.length
		const map = {
			integrate: n,
			overtop: hasIntegrate ? n + 1 : n,
			score: n + (hasIntegrate ? 1 : 0) + (hasOvertop ? 1 : 0),
			actions: n + 1 + (hasIntegrate ? 1 : 0) + (hasOvertop ? 1 : 0),
			sources: n + 2 + (hasIntegrate ? 1 : 0) + (hasOvertop ? 1 : 0)
		}
		return map[key] || n
	}

	const damageFactor = row => {
		if (row.height) return `波高 ${row.height} m / 波速 ${row.speed || '-'} m/s`
		if (row.wind) return `风速 ${row.wind} m/s`
		return '-'
	}

	const onArchive = async () => {
		archiving.value = true
		try {
			await archiveReport()
		} finally {
			archiving.value = false
		}
	}

	const PRINT_CSS = `
		@page { size: A4; margin: 18mm 16mm; }
		* { box-sizing: border-box; }
		body {
			margin: 0;
			color: #000;
			font: 13px/1.8 "SimSun", "Songti SC", "Microsoft YaHei", serif;
			background: #fff;
		}
		h1 {
			margin: 12px 0 16px;
			text-align: center;
			font-size: 20px;
			font-weight: 700;
			letter-spacing: 0.08em;
			color: #000;
		}
		h3 {
			margin: 18px 0 8px;
			padding: 0 0 6px;
			border-bottom: 1px solid #000;
			font-size: 15px;
			font-weight: 700;
			color: #000;
		}
		h4 { margin: 12px 0 6px; font-size: 13px; font-weight: 700; color: #000; }
		p, li { font-size: 13px; line-height: 1.8; color: #222; text-align: justify; }
		ol { margin: 0 0 8px; padding-left: 1.6em; }
		.rpt-org {
			margin: 0;
			text-align: center;
			color: #000;
			font-size: 14px;
			font-weight: 700;
			letter-spacing: 0.2em;
		}
		.rpt-en {
			margin: 6px 0 0;
			text-align: center;
			color: #555;
			font-size: 11px;
			letter-spacing: 0.08em;
		}
		.rpt-cover {
			padding-bottom: 10px;
			border-bottom: 3px double #000;
			margin-bottom: 16px;
			background: #fff;
		}
		.rpt-meta, .rpt-table { width: 100%; border-collapse: collapse; margin: 8px 0 12px; }
		.rpt-meta th, .rpt-meta td, .rpt-table th, .rpt-table td {
			border: 1px solid #000;
			padding: 6px 8px;
			text-align: left;
			font-size: 12px;
			color: #000;
		}
		.rpt-meta th, .rpt-table th { background: #eee; width: 18%; font-weight: 700; }
		.rpt-hero { display: flex; margin: 8px 0 12px; border: 1px solid #000; }
		.rpt-hero > div {
			flex: 1;
			background: #fff;
			border: 0;
			border-radius: 0;
			padding: 12px 14px;
		}
		.rpt-hero > div:first-child { border-right: 1px solid #000; }
		.rpt-hero span { display: block; color: #444; font-size: 12px; }
		.rpt-hero b { font-size: 28px; line-height: 1.3; color: #000; font-weight: 700; }
		.rpt-hero strong { display: block; font-size: 22px; margin: 4px 0; color: #000; font-weight: 700; }
		.rpt-hero em { color: #555; font-style: normal; font-size: 12px; }
		.rpt-callout {
			background: #f5f5f5;
			border: 1px solid #000;
			border-left-width: 3px;
			padding: 8px 12px;
			border-radius: 0;
			color: #222;
		}
		.rpt-kpis { display: flex; flex-wrap: wrap; margin: 8px 0 12px; border: 1px solid #000; }
		.rpt-kpis > div {
			flex: 1;
			min-width: 140px;
			border: 0;
			border-right: 1px solid #000;
			border-radius: 0;
			padding: 8px 10px;
			background: #fff;
		}
		.rpt-kpis > div:last-child { border-right: 0; }
		.rpt-kpis span { display: block; color: #444; font-size: 12px; }
		.rpt-kpis b { color: #000; font-weight: 700; }
		.rpt-sign {
			margin-top: 28px;
			padding-top: 12px;
			border-top: 1px solid #000;
			text-align: right;
			color: #222;
		}
		.rpt-foot { color: #555; font-size: 12px; }
		[data-print="ignore"] { display: none !important; }
		.grade-严重, .grade-较重, .grade-中度, .grade-轻度 { color: #000 !important; }
	`

	const onExport = async () => {
		printing.value = true
		let remoteOk = false
		try {
			const path = await exportRemoteReport()
			if (path) {
				downloadLocalJson()
				ElMessage.success(`服务端已导出：${path}（同时已下载本地 JSON）`)
				remoteOk = true
			}
		} catch (e) {
			remoteOk = false
		}
		if (remoteOk) {
			printing.value = false
			return
		}
		printReport()
	}

	const printReport = () => {
		const source = document.getElementById('assessment-report')
		if (!source) {
			ElMessage.warning('未找到可打印的报告内容')
			printing.value = false
			return
		}
		printing.value = true
		const clone = source.cloneNode(true)
		clone.querySelectorAll('[data-print="ignore"]').forEach(node => node.remove())
		const iframe = document.createElement('iframe')
		iframe.setAttribute('title', 'assessment-report-print')
		iframe.style.cssText = 'position:fixed;right:0;bottom:0;width:0;height:0;border:0;opacity:0;'
		document.body.appendChild(iframe)
		const doc = iframe.contentDocument
		doc.open()
		doc.write(`<!DOCTYPE html><html><head><meta charset="utf-8"><title>${r.value?.title || '综合评估报告'}</title><style>${PRINT_CSS}</style></head><body>${clone.innerHTML}</body></html>`)
		doc.close()
		const win = iframe.contentWindow
		const cleanup = () => {
			printing.value = false
			iframe.remove()
		}
		const trigger = () => {
			try {
				win.focus()
				win.print()
			} finally {
				setTimeout(cleanup, 400)
			}
		}
		if (iframe.contentDocument.readyState === 'complete') {
			setTimeout(trigger, 80)
		} else {
			iframe.onload = () => setTimeout(trigger, 80)
		}
	}
</script>
