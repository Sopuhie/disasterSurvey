<template>
	<div class="wb-panel">
		<section class="wb-block">
			<div class="wb-title"><span class="txt">速报数据标准化</span></div>
			<p class="wb-hint">接入海洋灾害灾情速报及地方上报，统一承灾体受损与基础设施字段后入库。</p>
			<el-button type="primary" class="max-w wb-gap" @click="store.showIntegrateForm = !store.showIntegrateForm">
				{{ store.showIntegrateForm ? '收起录入' : '录入上报信息' }}
			</el-button>
			<div v-if="store.showIntegrateForm" class="wb-entry">
				<el-form label-width="76px" size="small">
					<el-form-item label="数据来源">
						<el-select v-model="store.integrateForm.sourceId" class="max-w">
							<el-option v-for="item in sources" :key="item.id" :label="item.name" :value="item.id" />
						</el-select>
					</el-form-item>
					<el-form-item label="上报时间">
						<el-date-picker
							v-model="store.integrateForm.reportTime"
							type="datetime"
							style="width: 100%"
							value-format="YYYY-MM-DD HH:mm" />
					</el-form-item>
					<el-form-item label="区县">
						<el-input v-model="store.integrateForm.county" />
					</el-form-item>
					<el-form-item label="地市">
						<el-input v-model="store.integrateForm.city" />
					</el-form-item>
					<el-form-item label="承灾体">
						<el-select v-model="store.integrateForm.hazardType" class="max-w">
							<el-option label="海堤" value="海堤" />
							<el-option label="渔港" value="渔港" />
							<el-option label="海水养殖区" value="海水养殖区" />
							<el-option label="滨海旅游区" value="滨海旅游区" />
						</el-select>
					</el-form-item>
					<el-form-item label="名称">
						<el-input v-model="store.integrateForm.hazardName" />
					</el-form-item>
					<el-form-item label="受损情况">
						<el-input v-model="store.integrateForm.damage" type="textarea" :rows="2" />
					</el-form-item>
					<el-form-item label="设施影响">
						<el-input v-model="store.integrateForm.facility" type="textarea" :rows="2" />
					</el-form-item>
					<el-form-item label="预警色">
						<el-select v-model="store.integrateForm.level" class="max-w">
							<el-option label="蓝色" value="蓝色" />
							<el-option label="黄色" value="黄色" />
							<el-option label="橙色" value="橙色" />
							<el-option label="红色" value="红色" />
						</el-select>
					</el-form-item>
					<el-button type="primary" class="max-w" style="margin-bottom: 8px" @click="saveIntegrate">标准化入库</el-button>
				</el-form>
			</div>
		</section>
		<section class="wb-block">
			<div class="wb-title">
				<span class="txt">整合概况</span>
				<span class="sub">共 {{ store.integrateList.length }} 条</span>
			</div>
			<div class="wb-kpis">
				<div class="wb-kpi green">
					<span>已标准化</span>
					<b>{{ standardized }} <small>条</small></b>
				</div>
				<div class="wb-kpi orange">
					<span>待校核</span>
					<b>{{ pending }} <small>条</small></b>
				</div>
				<div class="wb-kpi">
					<span>数据来源</span>
					<b>{{ sourceCount }} <small>类</small></b>
				</div>
			</div>
		</section>
	</div>
</template>

<script setup>
	import { computed, onMounted, ref } from 'vue'
	import { assessmentApi } from '@/api/assessment/index.js'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useAssessmentActions } from '@/views/assessment/composables/useAssessmentActions.js'

	const store = useAssessmentStore()
	const { saveIntegrate } = useAssessmentActions()
	const api = assessmentApi()
	const sources = ref([])
	const standardized = computed(() => store.integrateList.filter(item => item.status === '已标准化').length)
	const pending = computed(() => store.integrateList.filter(item => item.status !== '已标准化').length)
	const sourceCount = computed(() => new Set(store.integrateList.map(item => item.sourceId)).size)

	onMounted(async () => {
		sources.value = await api.getReportSources()
	})
</script>
