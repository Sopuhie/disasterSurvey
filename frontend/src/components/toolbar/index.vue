<template>
	<div class="toolBtns flex noSelect" :style="sLeft">
		<div
			class="itemBtn flex faj"
			:class="{ cur: toolBtn.includes('全景监测') }"
			@click.stop="toolBtnHandle('全景监测')">
			<span class="itemBtnLabel mr-4">福建省</span>
			<el-icon><ArrowDown /></el-icon>
			<div class="area_box p-16" v-if="popoverVisible" @click.stop>
				<div class="area_search flex fa">
					<el-autocomplete
						v-model="areaName"
						:fetch-suggestions="querySearch"
						clearable
						placeholder="请输入区域名称"
						style="flex: 1"
						@select="handleSelect" />
					<el-icon class="ml-8 cp" @click="toolBtnHandle('全景监测')"><Close /></el-icon>
				</div>
				<el-divider />
				<div class="area_list max-w">
					<ul class="max-wh oy">
						<li class="item flex fas" v-for="item in areaList" :key="item.name">
							<p class="item_name" @click.stop="areaSelect(item)">{{ item.name }}</p>
							<ul class="flex fa fw">
								<li
									class="item_child flex faj"
									v-for="child in item.children"
									:key="child.name"
									@click.stop="areaSelect(child)">
									{{ child.name }}
								</li>
							</ul>
						</li>
					</ul>
				</div>
			</div>
		</div>

		<div class="itemBtn flex faj" :class="{ cur: toolBtn.includes('基础分析') }" @click="toolBtnHandle('基础分析')">
			<el-dropdown
				class="max-wh flex faj"
				popper-class="toolBtn_custom-popper"
				trigger="click"
				:teleported="false"
				@visible-change="visibleChange2"
				@command="handleCommand2">
				<span class="el-dropdown-link flex faj">
					<span class="itemBtnLabel mr-4">基础分析</span>
					<el-icon><ArrowDown /></el-icon>
				</span>
				<template #dropdown>
					<el-dropdown-menu>
						<el-dropdown-item command="/build-enquiry">
							<span>空间分析</span>
						</el-dropdown-item>
						<el-dropdown-item command="/statistics-report">
							<span>统计报表</span>
						</el-dropdown-item>
						<el-dropdown-item command="/projection-transform">
							<span>投影转换</span>
						</el-dropdown-item>
						<el-dropdown-item command="/export-map">
							<span>专题制图</span>
						</el-dropdown-item>
						<el-dropdown-item command="/annex">
							<span>附件管理</span>
						</el-dropdown-item>
					</el-dropdown-menu>
				</template>
			</el-dropdown>
		</div>
		<div
			class="toolbar noSelect flex fa ml-24"
			:style="xLeft"
			v-show="appStore.switchObj.is2D !== '3d' && lists.length > 0">
			<div class="flex fa cp" style="color: #1890ff" @click="openTB = !openTB">
				<span class="mr-8" style="font-size: 14px; font-family: 'Microsoft YaHei'; font-weight: bold">地图工具</span>
				<el-icon size="16" v-show="openTB"><ArrowLeft /></el-icon>
				<el-icon size="16" v-show="!openTB"><ArrowRight /></el-icon>
			</div>
			<ul class="toolbarLists flex fa fw" v-if="!openTB && lists && lists.length > 0">
				<li
					class="toolbarItem flex fa"
					:class="{ cur: toolBarSelect.includes(item.name) }"
					v-for="(item, index) in lists"
					:key="'toolbar_' + index">
					<el-dropdown
						ref="dropdownRef"
						trigger="click"
						teleported
						popper-class="map-toolbar-dropdown"
						:class="{ noChild: !(item.childrens && item.childrens.length) }">
						<span class="el-dropdown-link flex fa">
							<el-tooltip class="box-item" effect="light" :offset="24" :content="item.name" placement="bottom">
								<el-image
									style="width: 24px; height: 24px"
									:src="getAssetsFile(item.icon)"
									@click="itemClick(item, index)" />
							</el-tooltip>
							<el-icon class="el-icon--right" v-show="item.childrens && item.childrens.length">
								<arrow-down />
							</el-icon>
						</span>
						<template #dropdown>
							<el-dropdown-menu v-if="item.childrens && item.childrens.length > 0">
								<template v-for="(child, childIndex) in item.childrens" :key="'child_' + childIndex">
									<el-dropdown-item>
										<a
											class="flex fa"
											href="javascript:;"
											style="font-size: 14px"
											@click="dropdownHandle(item, index, child, childIndex)"
											:style="{
												color: dropdownAlias === child.name && childIdx === childIndex ? '#2875e0' : ''
											}">
											<i class="iconfont" :class="child.icon" style="display: inline-block"></i>
											<span style="margin-left: 8px">{{ child.name }}</span>
										</a>
									</el-dropdown-item>
								</template>
							</el-dropdown-menu>
						</template>
					</el-dropdown>
				</li>
			</ul>
			<ZDialogCom
				title="请输入坐标"
				toPath="cancel"
				v-if="flyToCoordsVisible"
				:headerIcon="false"
				:cancelBtn="false"
				:bodyStyle="{ padding: '12px' }"
				:wrapStyle="wrapStyle2"
				@cancel="flyClose"
				@handleOk="flyToCoords">
				<p class="flyTip noSelect">请输入 CGCS2000_3_Degree_GK_Zone_38 平面坐标（WKID：4526）</p>
				<div class="formItem flex item">
					<span class="formLabel asterisk" style="width: 48px">X坐标</span>
					<div class="formWidget flex fa" style="width: calc(100% - 60px)">
						<el-input v-model="xCoord" placeholder="X 坐标"></el-input>
						<span class="unit ml-4">m</span>
					</div>
				</div>
				<div class="formItem flex item">
					<span class="formLabel asterisk" style="width: 48px">Y坐标</span>
					<div class="formWidget flex fa" style="width: calc(100% - 60px)">
						<el-input v-model="yCoord" placeholder="Y 坐标"></el-input>
						<span class="unit ml-4">m</span>
					</div>
				</div>
			</ZDialogCom>
		</div>
	</div>

	<ParseData :isShow="appStore.switchObj.showUploadData" :close="closeParsePanel"></ParseData>
	<div class="toolbar_y" :style="{ left: yLeft }">
		<ul v-if="listsY && listsY.length > 0">
			<template v-for="(item, index) in listsY" :key="'toolbarY_' + index">
				<li class="toolbar_y-item flex faj" v-if="yToolList(item.name)">
					<el-tooltip class="box-item" effect="light" :offset="12" :content="item.name" placement="left">
						<div class="maxwh flex faj" @click="toolYhandle(item, this)">
							<img :src="getAssetsFile(item.icon)" width="20" height="20" alt="" />
						</div>
					</el-tooltip>
				</li>
			</template>
		</ul>
		<!-- 底图切换 -->
		<ZDialogCom
			title="底图切换"
			toPath="cancel"
			class="legend_y_dialog"
			v-if="appStore.switchObj.baseMap"
			:footer="false"
			:isDrag="false"
			:wrapStyle="wrapStyle"
			:bodyStyle="{ padding: '12px' }"
			@cancel="handleBaseMap"
			@click.stop>
			<div class="baseMap flex">
				<div
					class="baseMap_item cp flex faj fc"
					:class="{ active: mapStore.getBasemapType === 'vec' }"
					@click.stop="baseMapChange('vec')">
					<img src="@/assets/images/mapTheme.png" alt="" />
					<p>电子</p>
				</div>
				<div
					class="baseMap_item cp flex faj fc"
					:class="{ active: mapStore.getBasemapType === 'img' }"
					@click.stop="baseMapChange('img')">
					<img src="@/assets/images/imgTheme.png" alt="" />
					<p>影像</p>
				</div>
			</div>
		</ZDialogCom>
		<!-- 图例 -->
		<ZDialogCom
			title="图例"
			toPath="cancel"
			class="legend_y_dialog"
			v-if="appStore.switchObj.legend"
			:footer="false"
			:wrapStyle="wrapStyle3"
			:bodyStyle="{ padding: '12px' }"
			@cancel="handleLegend"
			@click.stop>
			<div class="legend_y max-wh oy">
				<el-tree
					v-if="legendDatas.length > 0"
					:data="legendDatas"
					:props="{ children: 'children', label: 'title' }"
					@node-click="legendNodeClick">
					<template #default="{ node, data }">
						<p class="tree_title pt-4 pb-4">
							<img
								style="max-width: 20px; margin-right: 6px"
								:src="`data:${data.contentType};base64,${data.imageData}`"
								alt="."
								v-if="data.imageData" />
							<span>
								{{ data.imageData ? data.label : `${data.title}(${data.children.length})` }}
							</span>
						</p>
					</template>
				</el-tree>
				<EmptyCom :image-size="110" v-else></EmptyCom>
			</div>
		</ZDialogCom>
	</div>
	<!-- 3d图例 -->
	<ZDialogCom
		title="图例"
		toPath="cancel"
		class="legend_y_dialog"
		v-if="appStore.switchObj.is2D === '3d' && show3dToolbar"
		:footer="false"
		:wrapStyle="wrapStyle4"
		:bodyStyle="{ padding: '12px' }"
		@cancel="show3dToolbar = false"
		@click.stop>
		<div class="legend_y max-wh oy">
			<el-tree
				v-if="legend3dDatas.length > 0"
				:data="legend3dDatas"
				:props="{ children: 'children', label: 'value' }"
				@node-click="legendNodeClick">
				<template #default="{ node, data }">
					<p class="tree_title pt-4 pb-4 flex fa">
						<span
							class="mr-8"
							:style="{ width: '32px', height: '16px', borderRadius: '4px', backgroundColor: data.color }"></span>
						<span>{{ data.value }}</span>
					</p>
				</template>
			</el-tree>
			<EmptyCom :image-size="110" v-else></EmptyCom>
		</div>
	</ZDialogCom>
	<div class="scaleOrCoord flex faj" v-if="appStore.switchObj.is2D !== '3d'" :style="statusBarPos">
		<span class="mr-8">比例尺</span>
		<el-select v-model="scale" size="small" style="width: 128px" @change="scaleChangeHandler">
			<el-option v-for="item in scaleOptions" :key="item.value" :label="item.label" :value="item.value" />
		</el-select>
		<span class="ml-8">坐标：{{ coordsText }}</span>
	</div>

	<div class="popupRef p-10" style="width: 466px" ref="popupRef" v-if="showListPop">
		<el-button-group class="ml-4" size="small">
			<el-button
				v-btn
				:type="curBtn === item ? 'primary' : 'default'"
				v-for="(item, index) in btn"
				:key="'btn_' + index"
				@click="changeBtn(item)">
				<span>{{ item }}</span>
			</el-button>
		</el-button-group>
		<table class="table mb-10 mt-10" style="width: 100%" border="0" cellspacing="1">
			<tr>
				<th style="width: calc(100% / 3)">人口总数：</th>
				<td style="width: calc(100% / 3)">{{ disasterData.preTotalPopulation }}(原)</td>
				<td>{{ disasterData.lastTotalPopulation }}(现)</td>
			</tr>
			<tr>
				<th>人口密度：</th>
				<td>{{ disasterData.preDensity }} 人/k㎡</td>
				<td>{{ disasterData.lastDensity }} 人/k㎡</td>
			</tr>
			<tr>
				<th>受灾人口：</th>
				<td>{{ disasterData.preDisasterPopulation }} 人</td>
				<td>{{ disasterData.lastDisasterPopulation }} 人</td>
			</tr>
		</table>
		<div class="chartBox max-w mt-10" style="height: 240px">
			<ECharts v-if="curBtn === '受灾人口'" class="max-wh" :option="chartOption1" :resize="false"></ECharts>
			<ECharts v-if="curBtn === '受灾面积'" class="max-wh" :option="chartOption2" :resize="false"></ECharts>
		</div>
	</div>
	<!-- 空间分析 -->
	<BuildEnquiry v-if="commandTxt === '/build-enquiry'" @cancel="commandTxt = ''"></BuildEnquiry>
	<!-- 统计报表 -->
	<StatisticsReport v-if="commandTxt === '/statistics-report'" @cancel="commandTxt = ''"></StatisticsReport>
	<!-- 投影转换 -->
	<ProjectionTransform v-if="commandTxt === '/projection-transform'" @cancel="commandTxt = ''"></ProjectionTransform>
	<!-- 专题制图 -->
	<ExportMap v-if="commandTxt === '/export-map'" @cancel="commandTxt = ''"></ExportMap>
	<!-- 附件管理 -->
	<AnnexDrawer v-if="commandTxt === '/annex'" @cancel="commandTxt = ''"></AnnexDrawer>
</template>

<script setup>
	import _ from 'lodash'
	import BuildEnquiry from '@/views/buildEnquiry/index.vue'
	import StatisticsReport from '@/views/statisticsReport/index.vue'
	import ProjectionTransform from '@/views/projectionTransform/index.vue'
	import ExportMap from '@/views/exportMap/index.vue'
	import AnnexDrawer from '@/views/management/annexDrawer.vue'
	import { ArrowDown } from '@element-plus/icons-vue'
	import { toolBarHandle, flyToCoordsVisible, closeParsePanel, closeByCoodinates } from './toolbar.js'
	import { ZoomInOut } from '@/components/map/primitive/control/ZoomInOut.js'
	import emitter from '@/components/map/utils/eventbus.js'
	import ZDialogCom from '@/components/dialog/index.vue'
	import ParseData from '@/components/parseData/index.vue'
	import EmptyCom from '@/components/empty/index.vue'
	import { BASE_LAYER_NAME } from '@/components/map/primitive/config/LayerConstantConfig.js'
	import OverviewMap from '@/components/map/primitive/control/OverviewMap'
	import EsrijsonUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
	import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
	import { getWindowSize } from '@/utils/index.js'
	import { prefix } from '@/config'
	import { DataNames } from "/public/map.js";

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	import { useSystemStore } from '@/store/modules/system.js'
	const systemStore = useSystemStore()

	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()

	import { useSurveyStore } from '@/store/modules/survey.js'
	const surveyStore = useSurveyStore()
	import { useAssessmentStore } from '@/store/modules/assessment.js'
	const assessmentStore = useAssessmentStore()

	import { systemApi } from '@/api/system/index.js'
	const api = systemApi()

	import { arcGisApi } from '@/api/gis/arcgis.js'
	const gisApi = arcGisApi()

	import { themeApi } from '@/api/theme/index.js'
	import { onMounted, watch } from 'vue'
	import { surveyApi } from '@/api/survey/index.js'
	const themeApi2 = themeApi()
	const surveyDataApi = surveyApi()

	const router = useRouter()
	const route = useRoute()

	const areaList = ref([])
	const loadAreaList = async () => {
		try {
			const tree = await surveyDataApi.getAreaTree()
			areaList.value = Array.isArray(tree) ? tree : []
		} catch (e) {
			areaList.value = []
		}
	}
	onMounted(() => {
		loadAreaList()
	})
	const commandTxt = ref('')
	const areaName = ref('')
	const popupRef = ref(null)
	const openTB = ref(false)
	const popoverVisible = ref(false)
	const showListPop = ref(false)
	const curBtn = ref('受灾人口')
	const btn = ['受灾人口', '受灾面积']
	const dropdownRef = ref(null)
	const itemIdx = ref(-1)
	const childIdx = ref(-1)
	const coords = ref([0, 0])
	const coordsText = computed(() => {
		const [x, y] = Array.isArray(coords.value) ? coords.value : [0, 0]
		const lon = Number(x)
		const lat = Number(y)
		if (!Number.isFinite(lon) || !Number.isFinite(lat)) return '-, -'
		return `${lon.toFixed(6)}, ${lat.toFixed(6)}`
	})
	const itemAlias = ref('')
	const dropdownAlias = ref('')
	const xCoord = ref(null)
	const yCoord = ref(null)
	const overviewCtrl = ref(null)
	const show3dToolbar = ref(true)
	const toolBtn = ref([])
	const monitorList = ref([])
	const isPerspective = ref(false)
	const disasterData = ref({
		preTotalPopulation: 0,
		lastTotalPopulation: 0,
		preDensity: 0,
		lastDensity: 0,
		preDisasterPopulation: 0,
		lastDisasterPopulation: 0,
	})

	let themeName = 'vec'
	let isOverview = false
	const vecBasemapVisible = ref(true)
	const imgBasemapVisible = ref(false)

	// 比例尺
	const scale = ref('1:1000000')
	const scaleOptions = mapStore.getScales

	const wrapStyle = reactive({
		width: '244px',
		height: '144px',
		top: 'calc(100% - 214px)',
		left: 'calc(100% - 300px)'
	})
	const wrapStyle2 = reactive({
		width: '398px',
		height: '324px',
		top: '148px',
		left: '482px'
	})
	const wrapStyle3 = reactive({
		width: '304px',
		height: '314px',
		top: 'calc(100% - 330px)',
		left: 'calc(100% - 360px)'
	})
	const wrapStyle4 = reactive({
		width: '304px',
		height: '435px',
		top: 'calc(100% - 452px)',
		left: 'calc(100% - 360px)'
	})
	const wrapStyle5 = ref({
		width: '640px',
		height: '626px',
		top: '130px',
		left: `${(getWindowSize().w.value - 640) / 2}px`
	})
	const legendDatas = ref([])
	const legend3dDatas = ref([])

	const chartOption1 = {
		title: {
			text: '受灾影响指数',
			textStyle: { color: '#FFFFFF', fontSize: '14px' }
		},
		tooltip: {
			position: 'auto'
		},
		radar: {
			shape: 'circle',
			radius: '60%',
			indicator: [
				{ name: '交通', max: 6500 },
				{ name: '养殖业', max: 16000 },
				{ name: '种植业', max: 30000 }
			]
		},
		series: [
			{
				name: 'Budget vs spending',
				type: 'radar',
				data: [
					{
						value: [4200, 3000, 20000],
						name: '受灾影响指数'
					},
					{
						value: [5000, 14000, 28000],
						name: '受灾影响指数'
					}
				]
			}
		]
	}
	const chartOption2 = {
		title: {
			text: '受灾面积统计',
			textStyle: { color: '#FFFFFF', fontSize: '14px' }
		},
		tooltip: {
			trigger: 'axis'
		},
		xAxis: [
			{
				type: 'category',
				axisLabel: { color: '#FFFFFF' },
				data: ['渔业养殖', '畜牧养殖', '交通运输', '电力设施']
			}
		],
		yAxis: [
			{
				type: 'value',
				axisLabel: { color: '#FFFFFF' }
			}
		],
		series: [
			{
				type: 'bar',
				data: [120, 10, 1, 2]
			},
			{
				type: 'bar',
				data: [0, 22, 0, 2]
			}
		]
	}

	const toolBarSelect = computed(() => {
		return appStore.toolBarSelect
	})

	const sLeft = computed(() => {
		const navWidth = appStore.switchObj.sidebarFlag ? 64 : 280
		const assessRouteNames = ['disasterAssessment', 'assessmentSeaWind', 'assessmentSeaWave', 'assessmentReports']
		const isWorkbench = route.name === 'disasterSurvey' || assessRouteNames.includes(route.name)
		const catalogOpen = mapStore.showMenuTree && route.name === 'ProvinceSystem'
		if (isWorkbench || catalogOpen) {
			// 左侧工作台收起后，菜单栏/工具栏贴菜单栏右缘左移
			const workbenchCollapsed =
				(route.name === 'disasterSurvey' && surveyStore.leftPanelCollapsed) ||
				(assessRouteNames.includes(route.name) && assessmentStore.leftPanelCollapsed)
			if (workbenchCollapsed) {
				return { left: `${navWidth + 48}px` }
			}
			return { left: `${navWidth + 20 + 340 + 10}px` }
		}
		if (appStore.switchObj.sidebarFlag) {
			return { left: route.path === '/provinceSystem' ? '84px' : '374px' }
		}
		return { left: route.path === '/provinceSystem' ? '300px' : '590px' }
	})

	const xLeft = computed(() => {
		if (appStore.appTheme === 'theme1') {
			return { top: 0, right: '258px' }
		}
		if (['灾情调查', '灾害评估'].includes(appStore.curNavObj) || mapStore.showMenuTree) {
			return {}
		}
		if (appStore.switchObj.sidebarFlag) {
			return { top: '128px', left: '382px' }
		}
		return { top: '128px', left: '134px' }
	})
	const yLeft = computed(() => {
		if (appStore.switchObj.sidebarSwitch) {
			return 'calc(100% - 48px)'
		}
	})
	const statusBarPos = computed(() => ({
		left: appStore.switchObj.sidebarFlag ? '85px' : '300px'
	}))

	const lists = computed(() => {
		let data = []
		const menuData = toRaw(systemStore?.menuTreeData)
		if (menuData?.length > 0) {
			const itemData = menuData.filter(item => item.name === '工具栏菜单')
			const configure = itemData[0]['childrens'].filter(item => item.name === 'x')[0]['childrens']
			data = configure
		}

		return data
	})
	const listsY = computed(() => {
		let data = []
		const menuData = toRaw(systemStore?.menuTreeData)
		if (menuData?.length > 0) {
			const itemData = menuData.filter(item => item.name === '工具栏菜单')
			const configure = itemData[0]['childrens'].filter(item => item.name === 'y')[0]['childrens']
			data = configure
		}
		return data
	})

	watch(vecBasemapVisible, val => {
		if (themeName == 'vec') {
			setBaseMapVisible(val)
		}
	})
	watch(imgBasemapVisible, val => {
		if (themeName == 'img') {
			setBaseMapVisible(val)
		}
	})

	/**
	 * 监听专题数量变化获取最新图例信息
	 */
	watch(
		() => mapStore.loadedThemes,
		async () => {
			let themeIds = mapStore.loadedThemes.map(m => m.themeInfo.id)
			// 移除不存在的专题
			legendDatas.value.forEach((m, index) => {
				if (!themeIds.includes(m.key)) {
					legendDatas.value.splice(index, 1)
				}
			})
			// 获取最新的专题图例
			let legendThemeIds = legendDatas.value.map(m => m.key)
			let unLoadThemeIds = themeIds.filter(m => !legendThemeIds.includes(m))
			if (unLoadThemeIds.length > 0) {
				let themes = mapStore.loadedThemes.filter(m => unLoadThemeIds.includes(m.themeInfo.id))
				await Promise.all(
					themes.map(async theme => {
						let legendObj = {
							title: theme.themeInfo.name,
							key: theme.themeInfo.id,
							children: []
						}
						if (theme?.serviceInfo?.length) {
							await Promise.all(
								theme.serviceInfo.map(async service => {
									if (!service?.serviceType?.includes('arcgis') || !service.serviceUrl) {
										return
									}
									let serviceObj = {
										title: service.serviceName,
										key: service.serviceId,
										children: []
									}
									const serviceInfo = await gisApi.getLegendInfo(service.serviceUrl)
									if (!serviceInfo?.error) {
										const meta = mapStore.metaDataDeserialized(service.metaData)
										const layers = meta?.layers?.length
											? serviceInfo.layers.filter(lyr => meta.layers.includes(lyr.layerId))
											: serviceInfo.layers
										if (layers?.length) {
											serviceObj.children = layers.map(lyr => {
												return {
													title: lyr.layerName,
													key: lyr.layerId,
													children: lyr.legend.map(m => {
														return {
															key: m.url,
															contentType: 'image/png',
															label: m.label,
															imageData: m.imageData,
															height: m.height,
															width: m.width
														}
													})
												}
											})
										}
									}
									legendObj.children.push(serviceObj)
								})
							)
						}
						legendDatas.value.push(legendObj)
					})
				)
			}
		},
		{
			immediate: true,
			deep: true
		}
	)

	watch(
		() => systemStore.menuItemData,
		() => {
			nextTick(() => {
				getFormData()
			})
		},
		{ deep: true, immediate: true }
	)

	onMounted(() => {
		getDicScale()
	})

	const getDisasterDatas = (districtName) => {
		disasterData.value = {
			preTotalPopulation: 0,
			lastTotalPopulation: 0,
			preDensity: 0,
			lastDensity: 0,
			preDisasterPopulation: 0,
			lastDisasterPopulation: 0,
		}
		fetch(DataNames["人口经济"])
			.then((res) => res.json())
			.then((res) => {
				let item = res.find(n => n.name == areaName.value);
				if (item) {
					disasterData.value = {
						preTotalPopulation: (parseFloat(item.常住人口) * 10000).toFixed(2),
						lastTotalPopulation: (parseFloat(item.常住人口 - Math.random()) * 10000).toFixed(2),
						preDensity: (parseFloat(item.人口密)).toFixed(2),
						lastDensity: (parseFloat(item.人口密)).toFixed(2),
						preDisasterPopulation: 0,
						lastDisasterPopulation: 0,
					}
				}
			})
			.catch((error) => {
					console.log(error);
			});
	}

	const changeBtn = val => {
		curBtn.value = val
	}

	// 扁平化处理区域数据，用于搜索
	const flatAreaList = computed(() => {
		const result = []
		const flatten = list => {
			list.forEach(item => {
				result.push({ value: item.name })
				if (item.children?.length) {
					flatten(item.children)
				}
			})
		}
		flatten(areaList.value)
		return result
	})

	/**
	 * 搜索建议方法
	 * @param {string} queryString - 搜索关键字
	 * @param {function} cb - 回调函数，用于返回搜索结果
	 */
	const querySearch = (queryString, cb) => {
		const results = queryString
			? flatAreaList.value.filter(item => item.value.toLowerCase().includes(queryString.toLowerCase()))
			: flatAreaList.value

		cb(results)
	}

	const handleSelect = item => {
		areaName.value = item.value
		flyToDistrict(item.value)
		if (router.currentRoute.value.name !== 'disasterSurvey') {
			getDisasterDatas(item.value)
		}
	}
	const areaSelect = item => {
		areaName.value = item.name
		flyToDistrict(item.name)
		popoverVisible.value = false
		toolBtn.value = []
		if (router.currentRoute.value.name === 'disasterSurvey') {
			return
		}
		getDisasterDatas(item.value)
		showListPop.value = true
	}

	/**
	 * 飞跃到执行行政区划
	 */
	const flyToDistrict = name => {
		let cityNames = ['福州市', '厦门市', '莆田市', '三明市', '泉州市', '漳州市', '南平市', '龙岩市', '宁德市']
		let url = (prefix? ('/' + prefix) : '') +'/datas/县界.json'
		let flag = false
		if (cityNames.some(n => n.includes(name))) {
			url = (prefix? ('/' + prefix) : '') +'/datas/行政区划.json'
			flag = true
		}
		mapStore.activeMap.featureManager.removeFeatures()
		fetch(url)
			.then(res => res.json())
			.then(res => {
				if (res?.features?.length) {
					let dist = res.features.find(n => (flag ? n.attributes['地级'] : n.attributes['地名']) == name)
					if (dist) {
						let geojson = GeoJSONUtil.writeFeatureObject(EsrijsonUtil.readFeature(dist))
						mapStore.activeMap.flyToManager.flyToRegion(geojson, true)
					}
				}
			})
			.catch(error => {
				console.log(error)
			})
	}

	// 切换主题
	const changeTheme = theme => {
		mapStore.changeTheme(theme)
	}

	// 切换地图
	const changeMap = map => {
		mapStore.changeMap(map)
	}

	// 切换图层
	const changeLayer = layer => {
		mapStore.changeLayer(layer)
	}

	// 切换比例尺
	const changeScale = scale => {
		mapStore.changeScale(scale)
	}

	const yToolList = name => {
		if (appStore.switchObj.is2D === '3d') {
			return ['放大', '缩小', '图例'].includes(name)
		} else {
			return true
		}
	}

	const getFormData = () => {
		if (systemStore.menuItemData) {
			const configure = JSON.parse(systemStore.menuItemData.configure)
			monitorList.value = configure?.lists ? flattenArray(configure.lists) : []
		}
	}

	function flattenArray(arr, childrenKey = 'children') {
		return _.flatMapDeep(arr, function (item) {
			if (item[childrenKey] && item[childrenKey].length) {
				return [_.omit(item, childrenKey)].concat(item[childrenKey])
			}
			return item
		})
	}

	const toolBtnHandle = item => {
		if (toolBtn.value.includes(item)) {
			toolBtn.value.splice(toolBtn.value.indexOf(item), 1)
		} else {
			toolBtn.value.push(item)
		}
		if (item === '全景监测') {
			popoverVisible.value = !popoverVisible.value
		}
	}
	const getAssetsFile = url => {
		if (url === '') {
			return ''
		}
		return new URL(`./img/${url}.png`, import.meta.url).href
	}

	const visibleChange2 = val => {
		if (!val && toolBtn.value.indexOf('基础分析') !== -1) {
			toolBtn.value.splice(toolBtn.value.indexOf('基础分析'), 1)
		}
	}
	const handleCommand2 = command => {
		// router.push(command)
		commandTxt.value = command
	}
	/**
	 * 初始化获取比例尺数据字典
	 */
	const getDicScale = () => {
		if (!mapStore.getScales?.length) {
			api
				.queryDictionary({
					zdlxdm: 'yztyy_blc'
				})
				.then(res => {
					if (res?.data?.length) {
						res.data.forEach(scale => {
							mapStore.setScale(scale.zdbm, scale.zdz)
						})
					}
				})
				.catch(err => ElMessage.error(err))
		}
	}

	/**
	 * 列表点击事件
	 */
	const itemClick = (item, index) => {
		appStore.setToolBarSelect('add', [item.name])
		itemAlias.value = item.name
		itemIdx.value = index
		if (item?.childrens?.length === 0) {
			dropdownRef.value[index].handleClose()
		}
		if (item.name === '清空') {
			clearSelect()
		}
		toolBarHandle(item)
	}

	/**
	 * 下拉列表点击事件
	 */
	const dropdownHandle = (item, index, child, childIndex) => {
		appStore.setToolBarSelect('add', [item.name, child.name])
		itemIdx.value = index
		childIdx.value = childIndex
		itemAlias.value = item.name
		dropdownAlias.value = child.name
		toolBarHandle(item, child)
	}
	/**
	 * 清除选中状态
	 */
	const clearSelect = () => {
		itemIdx.value = -1
		childIdx.value = -1
		itemAlias.value = ''
		dropdownAlias.value = ''
		appStore.setToolBarSelect('delAll')
	}

	/**
	 * 监听坐标变化
	 */
	emitter.on('coordinates', coordinate => {
		coords.value = coordinate
	})

	/**
	 * 监听比例尺变化
	 */
	emitter.on('scale', scaleVal => {
		scale.value = '1:' + scaleVal
	})

	/**
	 * 监听地图初始化
	 */
	emitter.on('mapMounted', map => {
		coords.value = map.olView.getCenter()
	})

	/**
	 * 关闭输入坐标弹窗
	 */
	function flyClose() {
		xCoord.value = null
		yCoord.value = null
		closeByCoodinates()
		clearSelect()
		appStore.setToolBarSelect('delAll')
	}
	/**
	 * 坐标飞跃
	 */
	function flyToCoords() {
		let x = unref(xCoord)
		let y = unref(yCoord)
		if (!x || !y || !/^\d+(\.\d+)?$/.test(x) || !/^\d+(\.\d+)?$/.test(y)) {
			ElMessage({
				showClose: true,
				message: '请正确输入坐标',
				type: 'warning'
			})
		} else {
			mapStore.activeMap.flyToManager.flyTo([x, y], 13)
			mapStore.activeMap.featureManager.addFeature(
				'Temp_' + Math.floor(Math.random() * 10000),
				{
					type: 'Feature',
					geometry: {
						type: 'Point',
						coordinates: [x, y]
					}
				},
				true
			)
		}
	}

	/**
	 * 选择比例尺缩放功能
	 */
	function scaleChangeHandler(val) {
		mapStore.activeMap.flyToManager.flyToScale(val)
	}
	/**
	 * 工具栏菜单--y轴
	 */
	const toolYhandle = (item, args) => {
		switch (item.name) {
			case '鹰眼':
				overviewMapHandler()
				break
			case '放大':
				zoomInHandler()
				break
			case '缩小':
				zoomOutHandler()
				break
			case '底图切换':
				handleBaseMap()
				break
			case '图例':
				handleLegend()
				break
			case '倾斜':
				perspectionHandler(parseInt(item.configure))
				break
			default:
				break
		}
	}
	/**
	 * 放大功能
	 */
	function zoomInHandler() {
		new ZoomInOut({
			map: mapStore.activeMap
		}).zoomIn()
	}
	/**
	 * 倾斜功能
	 */
	function perspectionHandler(val) {
		mapStore.activeMap.setPerspective(isPerspective.value ? 0 : val)
		isPerspective.value = !isPerspective.value
	}
	/**
	 * 缩小功能
	 */
	function zoomOutHandler() {
		new ZoomInOut({
			map: mapStore.activeMap
		}).zoomOut()
	}
	/**
	 * 鹰眼图
	 */
	function overviewMapHandler() {
		isOverview = !isOverview
		if (isOverview) {
			overviewCtrl.value = new OverviewMap({
				projection: mapStore.activeMap.projection,
				collapsed: false,
				collapsible: false
			})
			overviewCtrl.value.setMap(mapStore.activeMap)
		} else {
			mapStore.activeMap.ctrlManager.removeControl('OverviewMap')
		}
	}
	/**
	 * 底图切换
	 */
	function handleBaseMap() {
		const flag = !appStore.switchObj.baseMap
		appStore.setSwitchObj('baseMap', flag)
	}
	function baseMapChange(val) {
		const cfg = mapStore?.getMapConfig
		// 电子 ← imageTheme，影像 ← mapTheme（与系统配置字段实际绑定内容对齐）
		const themeId = val === 'vec' ? cfg?.imageTheme : cfg?.mapTheme
		if (!cfg) {
			ElMessage.warning('系统地图配置未加载，请稍后重试')
			return
		}
		if (!themeId) {
			ElMessage.warning(
				val === 'vec'
					? '未配置电子底图，请在系统配置「底图配置-地图」中选择系统底图专题'
					: '未配置影像底图，请在系统配置「底图配置-影像」中选择系统底图专题'
			)
			return
		}
		if (!mapStore.activeMap?.lyrManager) {
			ElMessage.warning('地图未初始化完成')
			return
		}
		getBaseMapInfo(themeId, val)
	}
	/**
	 * 图例
	 */
	function handleLegend() {
		if (appStore.switchObj.is2D === '3d') {
			show3dToolbar.value = !show3dToolbar.value
		} else {
			const flag = !appStore.switchObj.legend
			appStore.setSwitchObj('legend', flag)
		}
	}
	/**
	 * 图例点击事件
	 */
	function legendNodeClick(data, node, index) {
		console.log('图例点击事件:', data)
	}
	/**
	 * 按系统底图专题切换底图
	 */
	const getBaseMapInfo = (themeId, basemapType) => {
		themeApi2
			.getLayersById({ themeId })
			.then(res => {
				if (res?.data?.length) {
					mapStore.loadBaseMapTheme(
						{
							name: BASE_LAYER_NAME,
							themeId: themeId
						},
						res.data
					)
					if (basemapType) {
						mapStore.setBasemapType(basemapType)
						themeName = basemapType
					}
				} else {
					ElMessage.error('获取底图信息失败：该专题下未绑定服务，请在目录树「系统底图配置」中绑定服务')
				}
			})
			.catch(err => {
				ElMessage.error(err?.message || '获取底图信息失败')
			})
	}
	/**
	 * 设置底图透明度
	 */
	const setBaseMapVisible = visible => {
		let gropLyr = mapStore.activeMap.lyrManager.getLayer(BASE_LAYER_NAME)
		gropLyr.subLyrs.forEach(lyr => {
			lyr.layer.setVisible(visible)
		})
	}
</script>

<style lang="scss" scoped>
	.toolBtns {
		position: fixed;
		top: 95px;
		left: 300px;
		z-index: 2023;
		height: 32px;
		border-radius: 16px;
		transition: left 0.25s ease;
		& .itemBtn {
			position: relative;
			padding: 0 20px;
			height: 32px;
			background-color: white;
			&:nth-child(1) {
				border-radius: 16px 0 0 16px;
			}
			&:nth-child(2) {
				border-radius: 0 16px 16px 0;
			}
			cursor: pointer;
			& .itemBtnLabel {
				font-family: 'Microsoft YaHei';
				font-size: 14px;
				font-weight: bold;
				color: #303133;
			}
			&.cur {
				& .itemBtnLabel {
					color: var(--el-color-primary);
				}
			}
		}
	}
	.parentName {
		font-weight: bold;
		font-size: 16px;
	}
	.toolbar {
		padding: 0 16px;
		background-color: #fff;
		height: 32px;
		border-radius: 16px;
		/* 勿用 overflow:hidden：teleported=false 时会裁切测量/绘制/分屏下拉 */
		overflow: visible;
		& .toolbarLists {
			position: relative;
			border-radius: 16px;
		}
		& .toolBar_close {
			position: absolute;
			top: 0;
			right: -20px;
			color: var(--el-text-color-regular);
		}
		& .toolbarItem {
			height: 32px;
			font-size: 20px;
			padding: 0 8px;
			white-space: nowrap;
			cursor: pointer;
			& a {
				height: 100%;
				font-size: 18px;
			}
			&.cur {
				& i.i-link {
					font-weight: bold;
				}
			}
		}
	}
	.flyTip {
		font-size: 12px;
	}
	.unit {
		font-size: 16px;
	}
	.toolbar_y {
		position: fixed;
		bottom: 24px;
		z-index: 2020;

		& .toolbar_y-item {
			position: relative;
			width: 24px;
			height: 24px;
			line-height: 24px;
			border-radius: 4px;
			text-align: center;
			background-color: #fff;
			cursor: pointer;
			font-size: 18px;
			&:not(:last-child) {
				margin-bottom: 8px;
			}
			& i {
				color: var(--el-text-color-regular);
			}
		}
		& .baseMap {
			& .baseMap_item {
				position: relative;
				width: 110px;
				height: 76px;

				&.active {
					& p {
						color: black;
					}
				}
				& img {
					width: 90px;
					height: 50px;
					object-fit: cover;
					box-shadow: 1px 4px 4px rgba(4, 0, 0, 0.3);
				}
				& .switch_line {
					position: absolute;
					top: 0;
					left: 10px;
					width: 90px;
					height: 24px;
					line-height: 24px;
					text-align: center;
					color: white;
					background-color: rgba(0, 0, 0, 0.7);
				}
			}
			& p {
				font-size: 14px;
				text-align: center;
				font-weight: 600;
			}
		}
	}
	.scaleOrCoord {
		position: fixed;
		bottom: 16px;
		z-index: 2020;
		height: 32px;
		padding: 0 12px;
		border-radius: 6px;
		font-size: 12px;
		white-space: nowrap;
		color: #333333;
		background: rgba(255, 255, 255, 0.7);
	}

	.area_box {
		position: absolute;
		top: 50px;
		left: 0;
		width: 360px;
		border-radius: 8px;
		background-color: var(--el-bg-color);
	}
	.area_list {
		height: 306px;
		& .item {
			& ul {
				width: calc(100% - 70px);
				padding-bottom: 8px;
			}
		}
		& .item_name {
			width: 70px;
			font-weight: 600;
			color: var(--el-text-color-regular);
			cursor: pointer;
		}
		& .item_child {
			width: calc(100% / 3);
			margin-bottom: 8px;
			height: 20px;
			line-height: 20px;
			cursor: pointer;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
			color: var(--el-text-color-regular);
		}
	}
</style>
