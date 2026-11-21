import { LEVEL_MAP } from '@/views/assessment/constants.js'
import { defaultEconomy } from '@/views/assessment/mock/economyData.js'
import { waveDamageRows, windDamageRows, structureRows, waveErosionRows } from '@/views/assessment/mock/impactData.js'
import { calcEconomicLoss, decorateDamageRows } from '@/views/assessment/utils/assessmentEngine.js'
import { formatImpactLevelFull, warnColorToImpactLevel } from '@/shared/metricUnits.js'

const GRADE_META = {
	严重: {
		range: '综合指数 < 50',
		meaning: '致灾强度高、承灾体大面积受损，区域经济活动受到明显冲击，需立即启动高等级应急响应。'
	},
	较重: {
		range: '50 ≤ 综合指数 < 65',
		meaning: '重点岸段和关键承灾体受损较明显，局部生产生活受到影响，应加密监测并开展针对性加固。'
	},
	中度: {
		range: '65 ≤ 综合指数 < 80',
		meaning: '整体可控，部分对象出现中低等级破坏，应以核查、修复和预案完善为主。'
	},
	轻度: {
		range: '综合指数 ≥ 80',
		meaning: '致灾影响有限，结构损伤较轻，宜做好常规巡查、资料归档和后续维护安排。'
	}
}

const pad = n => String(n).padStart(2, '0')

const buildReportNo = typeId => {
	const now = new Date()
	const typeCode = { storm: 'ST', wave: 'WV', wind: 'WD' }[typeId] || 'HZ'
	return `HZPG-${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}-${typeCode}-${pad(now.getHours())}${pad(now.getMinutes())}`
}

const avg = (list, key) => {
	if (!list.length) return 0
	return Number((list.reduce((sum, item) => sum + (Number(item[key]) || 0), 0) / list.length).toFixed(1))
}

const levelStats = rows =>
	[1, 2, 3, 4].map(level => ({
		level,
		label: LEVEL_MAP[level].label,
		text: LEVEL_MAP[level].text,
		count: rows.filter(item => item.level === level).length
	}))

const fallbackDamage = typeId => {
	if (typeId === 'wave') return decorateDamageRows(waveDamageRows, 'wave')
	if (typeId === 'wind') return decorateDamageRows(windDamageRows, 'wind')
	return decorateDamageRows(
		[
			{ name: '城头海堤', target: '海堤', county: '福清市', height: 3.6, speed: 10.4, wind: 28.6, level: 2, analysis: '局部漫堤，迎潮面护坡松动。' },
			{ name: '百户澳三级渔港', target: '渔港', county: '平潭', height: 3.2, speed: 9.6, wind: 26.4, level: 3, analysis: '码头面层冲刷，系缆设施偏位。' },
			{ name: '连江岸段', target: '海岸线', county: '连江县', height: 3.9, speed: 11.1, wind: 30.2, level: 2, analysis: '沙质岸段蚀退，潮汐通道拓宽。' }
		],
		'wind'
	)
}

const sumBy = (list, key) => Number(list.reduce((sum, item) => sum + (Number(item[key]) || 0), 0).toFixed(2))

const buildActions = (typeLabel, grade, highRisk) => {
	const names = highRisk.map(item => item.name).join('、') || '重点渔港、海塘及侵蚀岸段'
	if (grade === '严重') {
		return {
			immediate: [
				`立即启动${typeLabel}高等级应急响应，对 ${names} 实施封控、警戒和人员劝离。`,
				'加密潮位、波浪/风场及现场巡查频次，滚动报送险情。',
				'调配块石、土工织物、抢险机械和应急供电设备，优先处置漫堤、淘空和走锚风险。'
			],
			shortTerm: [
				'48 小时内完成Ⅰ、Ⅱ级对象的险情复核与应急加固。',
				'组织养殖区、渔港和滨海旅游区专项核查，核定直接经济损失。',
				'会同交通、海事部门评估航道、助航设施和跨海结构安全。'
			],
			midTerm: [
				'结合本次过程修订海塘、渔港设防标准和应急预案。',
				'对反复受损岸段开展护岸结构优化和生态减灾措施论证。',
				'完善多源数据融合评估流程，形成可复用的灾后评估工作底稿。'
			]
		}
	}
	if (grade === '较重') {
		return {
			immediate: [
				`对 ${names} 加密巡查，必要时实施局部交通和亲水平台管控。`,
				`持续跟踪${typeLabel}动力过程，及时更新警戒岸段。`,
				'核验排水闸、系泊设施和供电箱等关键节点，防止次生险情。'
			],
			shortTerm: [
				'一周内完成高风险对象的专项核查和修复清单。',
				'复核遥感解译范围与地方上报差异，形成对照说明。',
				'补充现场照片、断面和损失台账，支撑后续理赔与重建。'
			],
			midTerm: [
				'将本次评估指标纳入年度备灾准备和工程维护计划。',
				'针对越浪、淘空和岸线侵蚀薄弱点安排岁修。',
				'开展应急队伍与物资配置评估，提升同类过程应对能力。'
			]
		}
	}
	return {
		immediate: [`保持对 ${names} 的常规巡查和预警关注。`, '做好现场资料归档，避免关键证据缺失。'],
		shortTerm: ['安排中低等级对象的维护检修。', '结合评估结果更新地方应急联络与物资台账。'],
		midTerm: ['完善监测站点布局和评估模型参数。', '形成本次过程的技术总结，用于培训和预案修订。']
	}
}

const buildSurveyBaselineSection = (surveyBaseline, surveySummary) => {
	if (!surveyBaseline?.affectedMeta && !surveySummary) return null
	const meta = surveyBaseline?.affectedMeta || {}
	const totals = surveyBaseline?.overlayResults || {}
	const line = typeId => {
		const t = totals[typeId]?.totals
		if (!t) return null
		return `${typeId === 'seawall' ? '海堤' : typeId === 'fishing' ? '渔港' : typeId === 'mariculture' ? '养殖区' : typeId === 'tourist' ? '旅游区' : typeId} ${t.count || 0} 处` +
			(t.areaKm2 != null ? `，叠置面积 ${t.areaKm2} km²` : '') +
			(t.lengthKm != null ? `，长度 ${t.lengthKm} km` : '')
	}
	const overlayLines = ['seawall', 'fishing', 'mariculture', 'tourist'].map(line).filter(Boolean)
	const compareHint =
		Array.isArray(surveyBaseline?.compareRows) && surveyBaseline.compareRows.length
			? `监测与上报对比 ${surveyBaseline.compareRows.length} 组指标已纳入研判。`
			: ''
	return {
		title: '调查基线',
		areaKm2: meta.areaKm2 ?? null,
		areaWanMu: meta.areaWanMu ?? null,
		countyCount: meta.countyCount ?? meta.counties?.length ?? null,
		district: surveyBaseline?.district?.name || '',
		overlayLines,
		compareHint,
		ledgerHint: surveySummary
			? `运维台账承灾体底数：海堤 ${surveySummary.seawall}、渔港 ${surveySummary.fishing}、养殖区 ${surveySummary.mariculture}、旅游区 ${surveySummary.tourist}`
			: '',
		paragraph: [
			meta.areaKm2 != null
				? `灾情调查识别受灾面积约 ${meta.areaKm2} km²（${meta.areaWanMu ?? '-'} 万亩），覆盖区县 ${meta.countyCount ?? meta.counties?.length ?? '-'} 个。`
				: '本次评估引用运维承灾体台账作为调查底数。',
			overlayLines.length ? `叠置分析命中：${overlayLines.join('；')}。` : '',
			compareHint
		]
			.filter(Boolean)
			.join('')
	}
}

export const buildComprehensiveReport = ({
	typeId = 'wave',
	typeLabel = '海浪',
	district,
	loss,
	damageRows,
	behavior,
	factors = {},
	integrateList = [],
	overtoppingRows = [],
	scene,
	typhoon,
	surveySummary = null,
	surveyBaseline = null
}) => {
	const records = integrateList || []
	const overtopRows = overtoppingRows || []
	const overWarnCount = overtopRows.filter(item => item.warnLevel && item.warnLevel !== '不超').length
	const stormProxyRows = records.map((item, index) => {
		const level = warnColorToImpactLevel(item.level) || 4
		return {
			name: item.hazardName || `上报对象${index + 1}`,
			target: item.hazardType === '海塘' ? '海堤' : item.hazardType || '承灾体',
			county: item.county || '',
			level,
			levelLabel: LEVEL_MAP[level].label,
			levelText: LEVEL_MAP[level].text,
			warnLevel: item.level || '',
			analysis: `${item.damage || item.facility || '依据灾情上报信息评估'}（上报预警色：${item.level || '-'}；影响等级：${formatImpactLevelFull(level)}）`
		}
	})
	const rows = damageRows?.length
		? damageRows
		: typeId === 'storm' && stormProxyRows.length
			? stormProxyRows
			: fallbackDamage(typeId)
	const stats = levelStats(rows)
	const highRisk = rows.filter(item => item.level <= 2)
	const damageScore = rows.length
		? Math.round(100 - (rows.reduce((sum, item) => sum + item.level, 0) / rows.length) * 18)
		: 70
	const erosion = behavior?.erosion || waveErosionRows
	const structures = behavior?.structures || structureRows
	const affectedKm2 = Number(loss?.affectedKm2 || behavior?.inundationKm2 || sumBy(erosion, 'areaKm2'))
	const economy = loss?.totalLoss
		? { ...loss, estimated: false }
		: { ...calcEconomicLoss(defaultEconomy, affectedKm2), estimated: true, affectedKm2 }
	const lossScore = Math.max(40, 92 - Math.round((economy.totalLoss || 0) / 80))
	const overtopPenalty = Math.min(12, overWarnCount * 1.5)
	const total = Math.round(
		damageScore * 0.28 +
			lossScore * 0.22 +
			(factors.structure || 70) * 0.18 +
			(factors.response || 70) * 0.18 +
			(factors.prepare || 60) * 0.14 -
			(typeId === 'storm' ? overtopPenalty * 0.35 : 0)
	)
	const grade = total >= 80 ? '轻度' : total >= 65 ? '中度' : total >= 50 ? '较重' : '严重'
	const gradeMeta = GRADE_META[grade]
	const areaName = district?.name || '福建沿海'
	const cityName = district?.city || '沿海设区市'
	const eventDate = scene?.date || records[0]?.reportTime?.slice(0, 10) || '2024-09-16'
	const maxHeight = behavior?.maxHeight || Math.max(...rows.map(item => item.height || 0), 0)
	const maxWind = behavior?.maxWind || typhoon?.maxWind || Math.max(...rows.map(item => item.wind || 0), 0)
	const period = behavior?.period || avg(rows, 'period') || 8.6
	const isWave = typeId === 'wave'
	const isStorm = typeId === 'storm'
	const isWind = typeId === 'wind'
	const erosionArea = behavior?.inundationKm2 || sumBy(erosion, 'areaKm2')
	const erosionLen = behavior?.lengthKm || sumBy(erosion, 'lengthKm')

	const processText = isWave
		? `评估所依据的主要过程为 ${eventDate} 前后影响福建沿海的灾害性海浪。SAR 海面波浪场显示，过程最大波高约 ${maxHeight || 4.8} m，特征周期约 ${period} s，来波方向以 ESE 为主。高能浪作用于开敞岸段、渔港口门和海塘迎潮面，造成越浪、淘空、岸滩流失和海上结构冲击。`
		: isStorm
			? `评估所依据的主要过程为 ${eventDate} 前后的风暴潮增水与沿岸漫堤过程。潮位抬升叠加风浪作用，导致部分海堤局部漫溢、渔港设施进水和养殖区筏架走锚。本次综合评估结合灾情速报、经济损失测算与漫堤分析（核查 ${overtopRows.length} 条海堤/潮位记录，其中预警超限 ${overWarnCount} 处）形成。`
			: `评估所依据的主要过程为 ${eventDate} 前后的灾害性海风过程${typhoon?.name ? `（台风「${typhoon.name}」）` : ''}。SAR 海面风场显示过程最大风速约 ${maxWind || 32} m/s，强风作用于渔港、海塘和沙质岸段，造成护坡松动、港池涌浪和岸线蚀退。`

	const summary = [
		`综合${typeLabel}对${areaName}（${cityName}）承灾体、岸线过程和区域经济活动的影响，本次评估综合指数为 ${total}，灾情等级判定为「${grade}」。${gradeMeta.meaning}`,
		isStorm
			? `信息整合 ${records.length} 条；经济损失测算受灾面积约 ${economy.affectedKm2} km²、损失 ${economy.totalLoss} 万元（约占 GDP ${economy.gdpShock}%）；漫堤分析核查 ${overtopRows.length} 处，预警超限 ${overWarnCount} 处。`
			: `承灾体方面，共评估 ${rows.length} 处对象，其中Ⅰ、Ⅱ级高风险 ${highRisk.length} 处，主要集中于 ${highRisk.map(item => item.name).join('、') || '重点岸段'}。${
					isWind
						? `行为分析估测淹没约 ${behavior?.inundationKm2 || economy.affectedKm2} km²，受灾人口约 ${behavior?.affectedPopulation || '-'} 人。`
						: `经济方面，受灾面积约 ${economy.affectedKm2} km²，测算经济损失 ${economy.totalLoss} 万元${economy.estimated ? '（按区域土地利用结构估算）' : ''}。`
			  }`,
		isWave
			? `动力过程方面，侵蚀范围约 ${erosionArea} km²，累计岸线 ${erosionLen} km，海上及海岸结构受损 ${structures.length} 处。建议优先处置高风险渔港、海塘和开敞岸段，防止后续浪况造成二次致灾。`
			: isStorm
				? `建议优先处置漫堤预警超限海堤与高风险上报承灾体，完善潮位警戒联动与应急加固安排。`
				: `建议结合台风路径与风场破坏等级，优先处置高风险渔港、海塘和开敞岸段，防止后续风浪叠加致灾。`
	]

	return {
		title: `${typeLabel}灾后综合评估报告`,
		reportNo: buildReportNo(typeId),
		org: '福建省海洋灾害调查与评估系统',
		typeId,
		typeLabel,
		district: areaName,
		city: cityName,
		createdAt: new Date().toLocaleString(),
		eventDate,
		sceneName: scene?.name || `${typeLabel}遥感监测场景`,
		method: isWave
			? 'SAR 波浪场解译 + 承灾体破坏等级分析 + 岸线侵蚀估测 + 综合加权评价'
			: isStorm
				? '灾情速报标准化 + 遥感受灾范围识别 + 土地利用经济损失测算 + 漫堤分析 + 综合加权评价'
				: 'SAR 海面风场解译 + 承灾体破坏等级分析 + 台风路径淹没估测 + 综合加权评价',
		total,
		grade,
		gradeMeta,
		scores: [
			{ name: '承灾体损毁', value: damageScore, weight: '28%', desc: '依据破坏等级均值折算，等级越高得分越低。' },
			{ name: '经济影响', value: lossScore, weight: '22%', desc: '依据损失规模占对照经济体量的冲击程度折算。' },
			{ name: '结构损伤', value: factors.structure || 70, weight: '18%', desc: '反映海塘、码头、跨海结构等现场核验判断。' },
			{ name: '应急响应', value: factors.response || 70, weight: '18%', desc: '反映预警发布、巡查封控和抢险组织有效性。' },
			{ name: '备灾准备', value: factors.prepare || 60, weight: '14%', desc: '反映预案、物资、队伍和工程设防准备水平。' }
		],
		summary,
		overview: [
			{ label: '评估区域', value: `${areaName} ${cityName}` },
			{ label: '灾害类型', value: typeLabel },
			{ label: '过程时相', value: eventDate },
			{ label: '评估对象', value: `${rows.length} 处` },
			{ label: '高风险对象', value: `${highRisk.length} 处` },
			{ label: '受灾面积', value: `${economy.affectedKm2} km²` },
			{ label: '经济损失', value: `${economy.totalLoss} 万元` },
			{ label: '综合指数', value: `${total}（${grade}）` }
		],
		processText,
		processParams: isWave
			? [
					{ label: '最大波高', value: `${maxHeight || 4.8} m` },
					{ label: '波周期', value: `${period} s` },
					{ label: '来波方向', value: behavior?.direction || 'ESE' },
					{ label: '波能指数', value: `${behavior?.energyIndex || Math.round((maxHeight || 4.8) ** 2 * 8)}` }
			  ]
			: isStorm
				? [
						{ label: '上报记录', value: `${records.length} 条` },
						{ label: '受灾面积', value: `${economy.affectedKm2} km²` },
						{ label: 'GDP 冲击', value: `${economy.gdpShock}%` },
						{ label: '漫堤核查', value: `${overtopRows.length} 条（超限 ${overWarnCount}）` }
				  ]
				: [
						{ label: '最大风速', value: `${maxWind || 32} m/s` },
						{ label: '主导风向', value: behavior?.avgDir || typhoon?.direction || 'ENE' },
						{ label: '淹没范围', value: `${behavior?.inundationKm2 || economy.affectedKm2} km²` },
						{ label: '评估对象', value: `${rows.length} 处` }
				  ],
		damageRows: rows,
		overtoppingRows: overtopRows,
		overtoppingSummary: isStorm
			? `漫堤分析共核查 ${overtopRows.length} 条海堤/潮位记录，其中预警超限 ${overWarnCount} 处，有防浪墙 ${overtopRows.filter(item => String(item.hasWall || '').includes('有')).length} 处。重点关注堤顶高程偏低与保护人口较大的岸段。`
			: '',
		damageStats: stats,
		damageConclusion: `破坏性分析结果中，Ⅰ级 ${stats[0].count} 处、Ⅱ级 ${stats[1].count} 处、Ⅲ级 ${stats[2].count} 处、Ⅳ级 ${stats[3].count} 处。高风险对象占比 ${Math.round((highRisk.length / rows.length) * 100)}%，表明${typeLabel}能量主要集中在开敞岸段和口门水域，防护工程迎潮面和渔港系泊系统是本次过程的薄弱环节。`,
		economy,
		economyConclusion: economy.estimated
			? `本次未单独完成区县经济损失测算，已按沿海县土地利用结构对 ${economy.affectedKm2} km² 受灾面积进行估算，结果仅供等级判定参考，正式损失应以现场核定为准。`
			: `经济损失测算叠加了受灾范围与县域土地利用结构：建设用地和养殖水面单价较高，是损失构成的主要来源；耕地、林地和水域次之。建议在重建阶段按用地类型分解修复资金。`,
		erosion,
		structures,
		behaviorText: isWave
			? `根据波浪场和行为分析结果，岸线侵蚀主要分布在平潭、福清、连江和晋江等开敞岸段，累计侵蚀面积 ${erosionArea} km²、岸线 ${erosionLen} km。跨海栈桥、防波堤灯塔、观景平台和助航标志等结构出现冲刷、露筋或偏位，需纳入专项检测。`
			: isWind
				? `依据台风路径与 SAR 风场行为分析，估测沿海淹没约 ${behavior?.inundationKm2 || erosionArea} km²，受灾人口约 ${behavior?.affectedPopulation || '-'} 人；结构损害样本 ${structures.length} 处。后续应结合潮位站与现场水位痕迹对模型结果进行率定。`
				: `行为分析用于刻画致灾过程路径及其对淹没或侵蚀范围的贡献。后续应结合潮位站、波浪浮标和现场水位痕迹对模型结果进行率定。`,
		integrateRecords: records,
		scoringExplain:
			'综合指数采用加权求和：承灾体损毁 28%、经济影响 22%、结构损伤 18%、应急响应 18%、备灾准备 14%。等级划分为：≥80 轻度、65–79 中度、50–64 较重、<50 严重。结构损伤、应急响应和备灾准备来自评估人员综合判断，可在报告生成前调整。',
		actions: buildActions(typeLabel, grade, highRisk),
		surveySummary,
		surveyBaseline,
		surveyBaselineSection: buildSurveyBaselineSection(surveyBaseline, surveySummary),
		sources: [
			'海洋灾害灾情速报及地方应急上报信息',
			scene?.name ? `${scene.name}（${scene.sensor || '孔径雷达卫星'}）` : '孔径雷达卫星遥感监测数据',
			'沿海县市人口经济与土地利用结构数据',
			surveyBaseline?.affectedMeta
				? `灾情调查基线（场景 ${surveyBaseline.sceneId || '-'}，受灾 ${surveyBaseline.affectedMeta.areaKm2 ?? '-'} km²）`
				: surveySummary
					? `运维/调查四类承灾体底数（海堤 ${surveySummary.seawall}、渔港 ${surveySummary.fishing}、养殖区 ${surveySummary.mariculture}、旅游区 ${surveySummary.tourist}）`
					: '海堤、渔港、海水养殖区和滨海旅游区等承灾体空间数据（运维管理台账）',
			...(isStorm ? ['运维漫堤分析数据（警戒潮位 / 堤顶高程 / 防浪墙，关联海堤台账上图）'] : []),
			...(isWind && typhoon?.name ? [`台风过程监测数据（${typhoon.name}）`] : []),
			'运维承灾体破坏样本与淹没/侵蚀分区',
			'现场核验与综合评估因子赋值'
		],
		limitations: [
			'遥感解译和叠置分析受云量、时相和空间分辨率约束，局部小尺度损毁可能漏检。',
			'经济损失采用土地利用均价估算，未含间接停产、生态修复和更长周期的恢复成本。',
			'破坏等级主要依据动力要素阈值与典型对象样本，后续应以现场检测和设计复核校正。',
			'本报告用于灾后快速评估与决策参考，不替代正式的工程鉴定或保险定损文件。'
		],
		note: factors.note || '无补充说明。建议在现场核查完成后，对本报告中的高风险对象清单和损失测算进行一次修订。',
		sign: {
			org: '福建省海洋灾害调查与评估系统',
			role: '评估岗位',
			review: '审核岗位',
			date: new Date().toLocaleDateString()
		}
	}
}
