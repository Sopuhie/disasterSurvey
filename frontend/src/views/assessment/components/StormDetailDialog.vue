<!-- 风暴潮受灾信息整合 · 查看详情 -->
<template>
	<el-dialog
		v-model="visible"
		title="风暴潮受灾信息整合详情"
		width="960px"
		append-to-body
		align-center
		destroy-on-close
		class="storm-detail-dialog">
		<template v-if="row">
			<div class="sd-kpis">
				<div><span>承灾体</span><b>{{ row.hazardName || row.name }}</b></div>
				<div><span>类型</span><b>{{ row.hazardType || '-' }}</b></div>
				<div><span>区县</span><b>{{ row.county || '-' }}</b></div>
				<div><span>预警色</span><b>{{ row.level || row.warnLevel || '-' }}</b></div>
				<div>
					<span>影响等级</span>
					<b>{{ impactLevelLabel }}</b>
				</div>
			</div>
			<table class="sd-table">
				<tr>
					<th>事件名称</th>
					<td>{{ row.eventName || '-' }}</td>
					<th>灾害类型</th>
					<td>{{ row.disasterType || '风暴潮' }}</td>
				</tr>
				<tr>
					<th>上报时间</th>
					<td>{{ row.reportTime || '-' }}</td>
					<th>发生时间</th>
					<td>{{ row.occurTime || '-' }}</td>
				</tr>
				<tr>
					<th>地市 / 乡镇</th>
					<td>{{ row.city || '-' }} / {{ row.town || row.location || '-' }}</td>
					<th>数据来源</th>
					<td>{{ row.sourceName || '-' }}</td>
				</tr>
				<tr>
					<th>受损情况</th>
					<td colspan="3">{{ row.damage || '-' }}</td>
				</tr>
				<tr>
					<th>设施影响</th>
					<td colspan="3">{{ row.facility || '-' }}</td>
				</tr>
				<tr>
					<th>损失长度(km)</th>
					<td>{{ formatNum(row.damageLength) }}</td>
					<th>损失面积(km²)</th>
					<td>{{ formatNum(row.damageArea) }}</td>
				</tr>
				<tr>
					<th>受灾人口(人)</th>
					<td>{{ formatNum(row.affectedPeople) }}</td>
					<th>经济损失(万元)</th>
					<td>{{ formatNum(row.directLoss) }}</td>
				</tr>
				<tr>
					<th>核验状态</th>
					<td>{{ row.verifyStatus || '-' }}</td>
					<th>标准化状态</th>
					<td>{{ row.status || '-' }}</td>
				</tr>
				<tr>
					<th>上报单位 / 填报人</th>
					<td>{{ row.reportOrg || '-' }} / {{ row.reportor || '-' }}</td>
					<th>审核人</th>
					<td>{{ row.auditor || '-' }}</td>
				</tr>
				<tr>
					<th>备注</th>
					<td colspan="3">{{ row.remark || '-' }}</td>
				</tr>
			</table>

			<template v-if="tideRows.length">
				<div class="sd-sub">关联潮位警戒信息</div>
				<el-table :data="tideRows" border size="small" max-height="220">
					<el-table-column prop="name" label="潮位站/海堤" min-width="140" />
					<el-table-column prop="city" label="所在市" width="100" />
					<el-table-column prop="warnTide" :label="SURVEY_LABELS.warnTide" width="110" />
					<el-table-column prop="tideHeight" :label="SURVEY_LABELS.tideHeight" width="90" />
					<el-table-column prop="protectPop" :label="SURVEY_LABELS.protectPop" width="110" />
					<el-table-column prop="crestElev" :label="SURVEY_LABELS.crestElev" width="110" />
					<el-table-column prop="hasWall" label="防浪墙" width="90" />
					<el-table-column prop="warnLevel" :label="SURVEY_LABELS.warnLevel" width="90" />
				</el-table>
			</template>
		</template>
		<template #footer>
			<el-button @click="visible = false">关闭</el-button>
		</template>
	</el-dialog>
</template>

<script setup>
	import { computed } from 'vue'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import {
		formatImpactLevelFull,
		SURVEY_LABELS,
		warnColorToImpactLevel
	} from '@/shared/metricUnits.js'

	const store = useAssessmentStore()
	const visible = computed({
		get: () => store.stormDetailVisible,
		set: val => {
			store.stormDetailVisible = val
		}
	})
	const row = computed(() => store.stormDetailRow)
	const impactLevelLabel = computed(() => {
		const color = row.value?.level || row.value?.warnLevel
		const level = warnColorToImpactLevel(color)
		return level ? formatImpactLevelFull(level) : '-'
	})
	const formatNum = value => (value === null || value === undefined || value === '' ? '-' : value)

	const tideRows = computed(() => {
		const name = row.value?.hazardName || row.value?.name || ''
		const county = row.value?.county || ''
		const city = row.value?.city || ''
		return (store.overtoppingRows || [])
			.filter(item => {
				if (name && item.name?.includes(name.replace(/海堤|景区|渔港|养殖.*/, ''))) return true
				if (city && item.city === city) return true
				if (county && item.name?.includes(county.replace(/[市县区]$/, ''))) return true
				return false
			})
			.slice(0, 8)
	})
</script>

<style lang="scss" scoped>
	.sd-kpis {
		display: grid;
		grid-template-columns: repeat(5, 1fr);
		gap: 8px;
		margin-bottom: 12px;
		div {
			background: #f6f9fd;
			border: 1px solid #edf2f8;
			border-radius: 8px;
			padding: 8px 10px;
		}
		span {
			display: block;
			font-size: 12px;
			color: #8a97a8;
		}
		b {
			font-size: 14px;
			color: #1a3a5c;
		}
	}
	.sd-table {
		width: 100%;
		border-collapse: collapse;
		margin-bottom: 12px;
		th,
		td {
			border: 1px solid #e6edf5;
			padding: 8px 10px;
			font-size: 13px;
			text-align: left;
		}
		th {
			width: 18%;
			background: #f4f7fb;
			color: #3d4a5c;
		}
	}
	.sd-sub {
		margin: 8px 0;
		font-size: 14px;
		font-weight: 600;
		color: #1a3a5c;
	}
</style>
