<template>
	<div class="wb-panel">
		<section class="wb-block">
			<div class="wb-title"><span class="txt">区县查询</span></div>
			<el-radio-group v-model="store.districtQueryMode" size="small" class="wb-gap">
				<el-radio-button label="name">县市名称</el-radio-button>
				<el-radio-button label="coord">经纬度</el-radio-button>
			</el-radio-group>
			<div v-if="store.districtQueryMode === 'name'" class="wb-area-select">
				<el-popover
					v-model:visible="areaPopoverVisible"
					placement="bottom-start"
					:width="340"
					trigger="click"
					:teleported="false"
					popper-class="wb-area-popper">
					<template #reference>
						<div class="wb-area-trigger">
							<span class="wb-area-trigger__label">{{ store.districtKeyword || '请选择行政区' }}</span>
							<el-icon><ArrowDown /></el-icon>
						</div>
					</template>
					<div class="wb-area-box" @click.stop>
						<div class="wb-area-search">
							<el-autocomplete
								v-model="areaSearchName"
								:fetch-suggestions="areaQuerySearch"
								clearable
								placeholder="请输入区域名称"
								style="flex: 1"
								@select="onAreaSearchSelect" />
						</div>
						<div class="wb-area-list">
							<ul>
								<li class="wb-area-item" v-for="item in areaOptions" :key="item.name">
									<p
										class="wb-area-city"
										:class="{ active: store.districtKeyword === item.name }"
										@click.stop="selectArea(item.name)">
										{{ item.name }}
									</p>
									<ul class="wb-area-children">
										<li
											class="wb-area-child"
											:class="{ active: store.districtKeyword === child.name }"
											v-for="child in item.children"
											:key="child.name"
											@click.stop="selectArea(child.name)">
											{{ child.name }}
										</li>
									</ul>
								</li>
							</ul>
						</div>
					</div>
				</el-popover>
				<el-button type="primary" @click="queryDistrictByName">查询</el-button>
			</div>
			<div v-else class="wb-coord">
				<el-input v-model="store.coordLon" placeholder="经度" />
				<el-input v-model="store.coordLat" placeholder="纬度" />
				<el-button type="primary" @click="queryDistrictByCoord">查询</el-button>
			</div>
			<div v-if="store.queriedDistrict" class="wb-scene-card">
				<div><span>区县</span><b>{{ store.queriedDistrict.name }}</b></div>
				<div><span>地市</span><b>{{ store.queriedDistrict.city }}</b></div>
				<div><span>面积</span><b>{{ store.queriedDistrict.statArea }} km²</b></div>
				<div><span>人口</span><b>{{ store.queriedDistrict.population }} 万</b></div>
			</div>
		</section>
		<section class="wb-block">
			<div class="wb-title"><span class="txt">专题图层叠加</span></div>
			<p class="wb-hint">在区县定位基础上叠加土地利用、人口、经济与详细规划专题，辅助损失研判。</p>
			<div class="wb-theme-list">
				<label v-for="item in themeOptions" :key="item.id" class="wb-theme-item">
					<el-checkbox
						:model-value="!!store.themeLayers[item.id]"
						@change="val => toggleThemeLayer(item.id, val)">
						{{ item.label }}
					</el-checkbox>
				</label>
			</div>
		</section>
		<section class="wb-block">
			<div class="wb-title"><span class="txt">遥感识别与损失测算</span></div>
			<p class="wb-hint">选择本地遥感影像并叠加到地图后，结合县域经济和土地利用结构生成经济损失预估。切换影像会先移除上一幅。</p>
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
			<p v-if="!workflowStore.isBaselineConfirmed" class="wb-hint wb-gap" style="color: #ad6800">
				需先确认调查基线后才能测算经济损失。
			</p>
			<el-button
				type="primary"
				class="max-w"
				:loading="store.lossLoading"
				:disabled="!workflowStore.isBaselineConfirmed"
				@click="runLoss">
				开始评估
			</el-button>
		</section>
	</div>
</template>

<script setup>
	import { computed, onMounted, ref } from 'vue'
	import { ArrowDown } from '@element-plus/icons-vue'
	import { THEME_LAYER_OPTIONS } from '@/views/assessment/constants.js'
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	import { useAssessmentActions } from '@/views/assessment/composables/useAssessmentActions.js'

	const store = useAssessmentStore()
	const {
		fetchAreaTree,
		queryDistrictByName,
		queryDistrictByCoord,
		toggleThemeLayer,
		runLoss,
		loadSelectedSceneImage,
		workflowStore
	} = useAssessmentActions()
	const themeOptions = THEME_LAYER_OPTIONS
	const areaOptions = ref([])
	const areaPopoverVisible = ref(false)
	const areaSearchName = ref('')

	const sceneLabel = item => {
		const name = item.displayName || item.imageName || item.name
		return item.date ? `${name}（${item.date}）` : name
	}
	const onSceneChange = () => {
		loadSelectedSceneImage({ fly: true })
	}

	const loadAreaOptions = async () => {
		try {
			const tree = await fetchAreaTree()
			areaOptions.value = Array.isArray(tree) ? tree : []
		} catch (e) {
			areaOptions.value = []
		}
	}

	const flatAreaList = computed(() => {
		const result = []
		const flatten = list => {
			list.forEach(item => {
				result.push({ value: item.name })
				if (item.children?.length) flatten(item.children)
			})
		}
		flatten(areaOptions.value)
		return result
	})

	const areaQuerySearch = (queryString, cb) => {
		const results = queryString
			? flatAreaList.value.filter(item => item.value.toLowerCase().includes(queryString.toLowerCase()))
			: flatAreaList.value
		cb(results)
	}

	const selectArea = async name => {
		store.districtKeyword = name
		areaSearchName.value = name
		areaPopoverVisible.value = false
		await queryDistrictByName()
	}

	const onAreaSearchSelect = item => {
		selectArea(item.value)
	}

	onMounted(() => {
		loadAreaOptions()
		if (store.selectedSceneId) loadSelectedSceneImage({ fly: true })
	})
</script>

<style lang="scss" scoped>
	.wb-area-select {
		display: flex;
		align-items: center;
		gap: 8px;
		:deep(.el-popover.el-popper),
		:deep(.wb-area-popper) {
			padding: 12px;
		}
	}
	.wb-area-trigger {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: space-between;
		min-height: 32px;
		padding: 0 11px;
		border: 1px solid var(--el-border-color);
		border-radius: 4px;
		background: #fff;
		cursor: pointer;
		color: var(--el-text-color-regular);
		&:hover {
			border-color: var(--el-color-primary);
		}
		&__label {
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}
	}
	.wb-area-box {
		width: 100%;
	}
	.wb-area-search {
		display: flex;
		align-items: center;
		margin-bottom: 10px;
	}
	.wb-area-list {
		max-height: 280px;
		overflow-y: auto;
	}
	.wb-area-item {
		display: flex;
		align-items: flex-start;
		margin-bottom: 4px;
	}
	.wb-area-city {
		width: 70px;
		flex-shrink: 0;
		font-weight: 600;
		line-height: 20px;
		cursor: pointer;
		color: var(--el-text-color-regular);
		&.active {
			color: var(--el-color-primary);
		}
	}
	.wb-area-children {
		display: flex;
		flex-wrap: wrap;
		width: calc(100% - 70px);
		padding-bottom: 8px;
	}
	.wb-area-child {
		width: calc(100% / 3);
		height: 20px;
		line-height: 20px;
		margin-bottom: 8px;
		cursor: pointer;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		color: var(--el-text-color-regular);
		&.active {
			color: var(--el-color-primary);
		}
		&:hover {
			color: var(--el-color-primary);
		}
	}
	.wb-theme-list {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 6px 8px;
	}
	.wb-theme-item {
		display: block;
		padding: 6px 8px;
		border-radius: 6px;
		background: #f6f9fd;
		border: 1px solid #edf2f8;
	}
</style>
