<template>
	<div class="wb-panel">
		<section class="wb-block">
			<div class="wb-title"><span class="txt">遥感影像解译</span></div>
			<p class="wb-hint">选择本地灾后遥感影像后，系统将叠加到地图并自动进行图像处理生成受灾范围。切换影像会先移除上一幅再加载新图。</p>
			<el-select
				v-model="surveyStore.selectedSceneId"
				class="max-w wb-gap"
				placeholder="请选择遥感影像"
				@change="onSceneChange">
				<el-option
					v-for="item in surveyStore.scenes"
					:key="item.id"
					:label="sceneLabel(item)"
					:value="item.id" />
			</el-select>
			<el-button type="primary" class="max-w" :loading="surveyStore.identifying" @click="identifyAffectedArea">
				开始解译
			</el-button>
			<el-progress
				v-if="surveyStore.identifying || surveyStore.identifyProgress"
				class="wb-progress"
				:percentage="surveyStore.identifyProgress"
				:status="surveyStore.identifyProgress >= 100 ? 'success' : undefined" />
		</section>
		<section class="wb-block" v-if="surveyStore.affectedMeta">
			<div class="wb-title"><span class="txt">解译摘要</span></div>
			<div class="wb-kpis">
				<div class="wb-kpi">
					<span>覆盖区县</span>
					<b>{{ surveyStore.affectedMeta.countyCount }} <small>个</small></b>
				</div>
				<div class="wb-kpi red">
					<span>受灾面积</span>
					<b>{{ surveyStore.affectedMeta.areaKm2 }} <small>km²</small></b>
				</div>
			</div>
			<div class="wb-kv" style="margin-top: 8px">
				<p><span>场景</span>{{ surveyStore.affectedMeta.sceneName }}</p>
				<p><span>时相</span>{{ surveyStore.affectedMeta.sceneDate }}</p>
			</div>
		</section>
	</div>
</template>

<script setup>
	import { onMounted } from 'vue'
	import { useSurveyStore } from '@/store/modules/survey.js'
	import { useSurveyActions } from '@/views/survey/composables/useSurveyActions.js'

	const surveyStore = useSurveyStore()
	const { identifyAffectedArea, loadSelectedSceneImage } = useSurveyActions()

	const sceneLabel = item => {
		const name = item.displayName || item.imageName || item.name
		return item.date ? `${name}（${item.date}）` : name
	}

	const onSceneChange = () => {
		loadSelectedSceneImage({ fly: true })
	}

	onMounted(() => {
		if (surveyStore.selectedSceneId) loadSelectedSceneImage({ fly: true })
	})
</script>
