<template>
	<div class="wb-panel">
		<section class="wb-block">
			<div class="wb-title"><span class="txt">行政区划查询</span></div>
			<el-radio-group v-model="surveyStore.districtQueryMode" size="small" class="wb-gap">
				<el-radio-button label="name">县市名称</el-radio-button>
				<el-radio-button label="town">乡镇名称</el-radio-button>
			</el-radio-group>
			<div v-if="surveyStore.districtQueryMode === 'name'" class="wb-area-select">
				<el-popover
					v-model:visible="areaPopoverVisible"
					placement="bottom-start"
					:width="340"
					trigger="click"
					:teleported="false"
					popper-class="wb-area-popper">
					<template #reference>
						<div class="wb-area-trigger">
							<span class="wb-area-trigger__label">{{ surveyStore.districtKeyword || '请选择行政区' }}</span>
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
										:class="{ active: surveyStore.districtKeyword === item.name }"
										@click.stop="selectArea(item.name)">
										{{ item.name }}
									</p>
									<ul class="wb-area-children">
										<li
											class="wb-area-child"
											:class="{ active: surveyStore.districtKeyword === child.name }"
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
			<div v-else class="wb-row">
				<el-select
					v-model="surveyStore.townKeyword"
					filterable
					clearable
					:disabled="!currentDistrictName"
					:placeholder="townSelectPlaceholder"
					style="flex: 1"
					@change="onTownSelect">
					<el-option
						v-for="item in townOptions"
						:key="item.value"
						:label="item.label || item.value"
						:value="item.value" />
				</el-select>
				<el-button type="primary" :disabled="!currentDistrictName" @click="queryTownByName">查询</el-button>
			</div>
		</section>

		<section class="wb-block wb-block--fill">
			<div class="wb-title">
				<span class="txt">承灾体查询</span>
				<span class="sub">{{ surveyStore.hazardList.length }} 条</span>
			</div>
			<div class="wb-tiles">
				<button
					v-for="item in hazardTypes"
					:key="item.id"
					class="wb-tile"
					:class="{ active: surveyStore.selectedHazardType === item.id }"
					@click="onHazardTypeChange(item.id)">
					<i class="wb-dot" :class="item.id"></i>
					{{ item.label }}
				</button>
			</div>
			<el-radio-group v-model="surveyStore.hazardQueryMode" size="small" class="wb-gap">
				<el-radio-button label="name">名称</el-radio-button>
				<el-radio-button label="coord">坐标</el-radio-button>
			</el-radio-group>
			<div v-if="surveyStore.hazardQueryMode === 'name'">
				<el-input
					v-model="surveyStore.hazardKeyword"
					class="wb-gap"
					clearable
					placeholder="按名称、乡镇或区划筛选"
					:prefix-icon="Search"
					@input="refreshHazardList" />
			</div>
			<div v-else class="wb-coord wb-gap">
				<el-input v-model="surveyStore.hazardCoordLon" placeholder="经度" />
				<el-input v-model="surveyStore.hazardCoordLat" placeholder="纬度" />
				<el-button type="primary" @click="queryHazardByCoord">查询</el-button>
			</div>
			<div class="wb-list" v-loading="surveyStore.loadingHazards">
				<div
					v-for="item in surveyStore.hazardList"
					:key="item.name"
					class="wb-list-item"
					:class="{ active: surveyStore.selectedHazard?.name === item.name }"
					@click="locateHazard(item)">
					<p class="name">{{ item.name }}</p>
					<p class="meta">{{ item.town || item.division }} · {{ hazardMeta(item) }}</p>
				</div>
				<el-empty v-if="!surveyStore.loadingHazards && !surveyStore.hazardList.length" :image-size="64" description="暂无承灾体" />
			</div>
		</section>
	</div>
</template>

<script setup>
	import { computed, onMounted, ref, watch } from 'vue'
	import { ArrowDown, Search } from '@element-plus/icons-vue'
	import { HAZARD_TYPES } from '@/views/survey/constants.js'
	import { useSurveyStore } from '@/store/modules/survey.js'
	import { useSurveyActions } from '@/views/survey/composables/useSurveyActions.js'

	const surveyStore = useSurveyStore()
	const {
		fetchAreaTree,
		fetchTownSuggestions,
		queryDistrictByName,
		queryTownByName,
		refreshHazardList,
		locateHazard,
		queryHazardByCoord
	} = useSurveyActions()
	const hazardTypes = HAZARD_TYPES
	const areaOptions = ref([])
	const areaPopoverVisible = ref(false)
	const areaSearchName = ref('')
	const townOptions = ref([])

	const currentDistrictName = computed(
		() => (surveyStore.districtKeyword || surveyStore.queriedDistrict?.name || '').trim()
	)
	const townSelectPlaceholder = computed(() =>
		currentDistrictName.value ? `请选择${currentDistrictName.value}下的乡镇` : '请先选择县市'
	)

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
		surveyStore.districtKeyword = name
		surveyStore.townKeyword = ''
		areaSearchName.value = name
		areaPopoverVisible.value = false
		await queryDistrictByName()
	}

	const onAreaSearchSelect = item => {
		selectArea(item.value)
	}

	const loadTownOptions = async () => {
		const district = currentDistrictName.value
		if (!district) {
			townOptions.value = []
			surveyStore.townKeyword = ''
			return
		}
		try {
			const list = await fetchTownSuggestions('', district)
			townOptions.value = list
			if (surveyStore.townKeyword && !list.some(item => item.value === surveyStore.townKeyword)) {
				surveyStore.townKeyword = ''
			}
		} catch (e) {
			townOptions.value = []
		}
	}

	const onTownSelect = async val => {
		if (!val) return
		await queryTownByName()
	}

	watch(
		() => [surveyStore.districtQueryMode, currentDistrictName.value],
		([mode]) => {
			if (mode === 'town') loadTownOptions()
		},
		{ immediate: true }
	)

	onMounted(() => {
		loadAreaOptions()
	})

	const onHazardTypeChange = async typeId => {
		surveyStore.selectedHazardType = typeId
		surveyStore.selectedHazard = null
		await refreshHazardList()
		if (surveyStore.hazardList[0]) await locateHazard(surveyStore.hazardList[0])
	}

	const hazardMeta = item => {
		const lengthKm = Number(item.lengthKm || item.length || 0)
		const areaKm2 = Number(item.areaKm2 || item.area || 0)
		if (lengthKm) return `${lengthKm} km`
		if (areaKm2) return `${areaKm2} km²`
		return item.type || item.method || item.division || ''
	}
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
</style>
