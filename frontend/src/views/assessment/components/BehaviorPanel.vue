<template>
	<div class="wb-panel">
		<section class="wb-block">
			<div class="wb-title"><span class="txt">影像监测行为分析</span></div>
			<p class="wb-hint" v-if="store.currentType === 'wind'">
				查询卫星影像监测数据，通过风速和风向分析台风、热带气旋行为，估测淹没范围与承灾体损害。
			</p>
			<p class="wb-hint" v-else>查询卫星影像监测数据，通过波高和波速分析海浪行为，估测岸线侵蚀与海上结构损害。</p>
			<template v-if="store.currentType === 'wind'">
				<el-select v-model="store.selectedTyphoonId" class="max-w wb-gap">
					<el-option
						v-for="item in store.typhoonEvents"
						:key="item.id"
						:label="`${item.name}（${item.year} / ${item.level}）`"
						:value="item.id" />
				</el-select>
				<div v-if="currentEvent" class="wb-scene-card">
					<div>
						<span>登陆地点</span>
						<b>{{ currentEvent.landfall }}</b>
					</div>
					<div>
						<span>最大风速</span>
						<b>{{ currentEvent.maxWind }} m/s</b>
					</div>
				</div>
			</template>
			<template v-else>
				<el-select
					v-model="store.selectedSceneId"
					class="max-w wb-gap"
					placeholder="请选择影像数据"
					@change="onSceneChange">
					<el-option
						v-for="item in store.scenes"
						:key="item.id"
						:label="sceneLabel(item)"
						:value="item.id" />
				</el-select>
				<div v-if="currentScene" class="wb-scene-card">
					<div>
						<span>最大波高</span>
						<b>{{ currentScene.maxHeight }} m</b>
					</div>
					<div>
						<span>最大波速</span>
						<b>{{ currentScene.maxSpeed || '-' }} m/s</b>
					</div>
				</div>
			</template>
			<el-button type="primary" class="max-w" :loading="store.behaviorLoading" @click="runBehavior">开始分析</el-button>
		</section>
	</div>
</template>

<script setup>
	import { computed, onMounted, watch } from 'vue'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useAssessmentActions } from '@/views/assessment/composables/useAssessmentActions.js'

	const store = useAssessmentStore()
	const { runBehavior, loadSelectedSceneImage } = useAssessmentActions()
	const currentEvent = computed(() => store.typhoonEvents.find(item => item.id === store.selectedTyphoonId) || store.typhoonEvents[0])
	const currentScene = computed(() => store.scenes.find(item => item.id === store.selectedSceneId) || store.scenes[0])
	const sceneLabel = item => {
		const name = item.displayName || item.imageName || item.name
		return item.date ? `${name}（${item.date}）` : name
	}
	const onSceneChange = () => {
		loadSelectedSceneImage({ fly: true })
	}

	watch(
		() => [store.selectedTyphoonId, store.selectedSceneId],
		([typhoonId, sceneId], prev = []) => {
			if (!store.behaviorResult) return
			const [prevTyphoon, prevScene] = prev
			if (store.currentType === 'wind' && typhoonId && typhoonId !== prevTyphoon) {
				runBehavior({ silent: true })
			}
			if (store.currentType === 'wave' && sceneId && sceneId !== prevScene) {
				runBehavior({ silent: true })
			}
		}
	)

	onMounted(() => {
		if (store.selectedSceneId) loadSelectedSceneImage({ fly: true })
	})
</script>
