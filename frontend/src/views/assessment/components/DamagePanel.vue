<template>
	<div class="wb-panel">
		<section class="wb-block">
			<div class="wb-title"><span class="txt">孔径雷达破坏性分析</span></div>
			<p class="wb-hint">
				{{
					store.currentType === 'wave'
						? '选择本地影像后叠加到地图，依据 SAR 波浪场评估渔港、海塘和海岸线破坏等级。切换影像会先移除上一幅。'
						: '选择本地影像后叠加到地图，依据 SAR 海面风场评估渔港、海塘和海岸线破坏等级。切换影像会先移除上一幅。'
				}}
			</p>
			<el-select v-model="store.selectedSceneId" class="max-w wb-gap" placeholder="请选择影像数据" @change="onSceneChange">
				<el-option
					v-for="item in store.scenes"
					:key="item.id"
					:label="sceneLabel(item)"
					:value="item.id" />
			</el-select>
			<div v-if="currentScene" class="wb-scene-card">
				<div>
					<span>传感器</span>
					<b>{{ currentScene.sensor }}</b>
				</div>
				<div v-if="currentScene.maxWind">
					<span>风场强度</span>
					<b>{{ currentScene.maxWind }} m/s · {{ currentScene.direction }}</b>
				</div>
				<div v-if="currentScene.maxHeight">
					<span>波浪场</span>
					<b>{{ currentScene.maxHeight }} m / {{ currentScene.maxSpeed || '-' }} m/s</b>
				</div>
			</div>
			<div class="wb-tiles">
				<button
					class="wb-tile"
					:class="{ active: store.damageTarget === 'all' }"
					@click="store.damageTarget = 'all'">
					<i class="wb-dot all"></i>
					全部对象
				</button>
				<button
					v-for="item in targets"
					:key="item.id"
					class="wb-tile"
					:class="{ active: store.damageTarget === item.label }"
					@click="store.damageTarget = item.label">
					<i class="wb-dot" :class="item.id"></i>
					{{ item.label }}
				</button>
			</div>
			<el-button type="primary" class="max-w" :loading="store.damageLoading" @click="runDamage">开始分析</el-button>
		</section>
	</div>
</template>

<script setup>
	import { computed, onMounted, watch } from 'vue'
	import { IMPACT_TARGETS } from '@/views/assessment/constants.js'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useAssessmentActions } from '@/views/assessment/composables/useAssessmentActions.js'

	const store = useAssessmentStore()
	const { runDamage, loadSelectedSceneImage } = useAssessmentActions()
	const targets = IMPACT_TARGETS
	const currentScene = computed(() => store.scenes.find(item => item.id === store.selectedSceneId) || store.scenes[0])
	const sceneLabel = item => {
		const name = item.displayName || item.imageName || item.name
		return item.date ? `${name}（${item.date}）` : name
	}

	const onSceneChange = () => {
		loadSelectedSceneImage({ fly: true })
	}

	watch(
		() => store.selectedSceneId,
		(id, prev) => {
			if (!id || id === prev || !store.damageRows.length) return
			runDamage({ silent: true })
		}
	)

	onMounted(() => {
		if (store.selectedSceneId) loadSelectedSceneImage({ fly: true })
	})
</script>
