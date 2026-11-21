<!-- 资源目录 -->
<template>
	<div class="comResourceCatalog CompA flex fc max-w oy">
		<div
			class="mb-10 flex fa jsb"
			style="padding: 0 10px; width: 100%; height: 40px; font-weight: bold; text-align: center">
			<el-button
				v-btn
				size="large"
				style="padding: 0 10px"
				:type="btnCur === '资源列表' ? 'primary' : 'default'"
				@click="changeTree('资源列表')"
				>资源列表</el-button
			>
			<el-button
				v-btn
				size="large"
				style="padding: 0 10px"
				:type="btnCur === '已选数据' ? 'primary' : 'default'"
				@click="changeTree('已选数据')">
				<span>已选数据</span>
				<span
					v-if="selectTreeData.length > 0"
					class="flex faj pl-8 pr-8 ml-8"
					style="color: white; height: 18px; border-radius: 88px; background: #f52530"
					>{{ selectTreeData.length }}</span
				>
			</el-button>
		</div>
		<div class="pl-10 pr-10">
			<el-input v-model="searchValue" placeholder="搜索资源名称" style="width: 100%" clearable>
				<template #prefix>
					<i class="iconfont icon-zoomOut"></i>
				</template>
			</el-input>
		</div>
		<div class="treeBox mt-10 oy" v-show="btnCur === '资源列表'">
			<el-tree
				ref="treeRef"
				:data="treeData"
				node-key="id"
				show-checkbox
				auto-expand-parent
				highlight-current
				:default-expanded-keys="expandedKeys"
				:default-checked-keys="mapStore.checkedKeys"
				:props="defaultProps"
				:filter-node-method="filterNode"
				@check="checkHandle">
				<template #default="{ node, data }">
					<span class="custom-tree-node">
						<span class="label1" @mouseenter="el => handleMouse(el, data)" :title="data.showTitle ? data.name : ''">{{
							node.label
						}}</span>
					</span>
				</template>
			</el-tree>
		</div>
		<div class="atlas" v-show="btnCur === '已选数据'" @click.stop>
			<div class="mt-8 mb-8 oy">
				<el-tree
					ref="selectTreeRef"
					:data="selectTreeData"
					node-key="id"
					auto-expand-parent
					draggable
					:props="defaultProps"
					@node-click="handleNodeClick">
					<template #default="{ node, data }">
						<span class="custom-tree-node" v-if="data.type === 'theme'">
							<span>{{ node.label }}</span>
						</span>
						<span class="pr-8" style="width: calc(100% - 24px)" v-else>
							<div class="flex fa jsb">
								<span
									style="
										display: inline-block;
										overflow: hidden;
										text-overflow: ellipsis;
										white-space: nowrap;
										width: calc(100% - 32px);
									"
									:title="node.label"
									>{{ node.label }}</span
								>
								<span class="flex faj" @click.stop="atlasTagClick(data)">
									<el-icon class="cp" size="16" v-if="data.choose"><Hide /></el-icon>
									<el-icon class="cp" size="16" v-else><View /></el-icon>
								</span>
							</div>
							<div>
								<el-slider v-model="data.opacity" :min="0" :max="100" size="small" @change="opacityChange(data)" />
							</div>
						</span>
					</template>
				</el-tree>
			</div>
		</div>
	</div>
</template>

<script setup>
	import emitter from '@/components/map/utils/eventbus.js'
	import * as turf from '@turf/turf'
	import { themeApi } from '@/api/theme/index.js'
	import { useMapStore } from '@/store/modules/map.js'
	import { useSystemStore } from '@/store/modules/system.js'
	import buildLayer, { LayerType } from '@/components/map/primitive/layer/Layer'
	import { demoCatalogTree, demoExpandedKeys, flattenDemoThemes } from './mock/demoCatalog.js'

	const api = themeApi()
	const mapStore = useMapStore()
	const systemStore = useSystemStore()

	const defaultProps = {
		children: 'childrens',
		label: 'name',
		id: 'id',
		disabled: 'disabled'
	}
	const btnCur = ref('资源列表')
	const searchValue = ref('')
	const treeRef = ref(null)
	/** 演示数据 + 目录树管理中配置的正式专题目录 */
	const treeData = computed(() => {
		const demo = JSON.parse(JSON.stringify(demoCatalogTree))
		const official = Array.isArray(systemStore.catalogTreeData) ? systemStore.catalogTreeData : []
		return [...demo, ...official]
	})
	const expandedKeys = computed(() => {
		const keys = [...demoExpandedKeys]
		const walk = nodes => {
			;(nodes || []).forEach(item => {
				if (item?.id != null && item.type === 'folder') keys.push(item.id)
				if (item?.childrens?.length) walk(item.childrens)
			})
		}
		walk(systemStore.catalogTreeData)
		return keys
	})
	const selectTreeData = ref([])

	watch(searchValue, val => {
		treeRef.value?.filter(val)
	})

	watch(
		() => systemStore.catalogTreeData,
		() => {
			nextTick(() => {
				if (treeRef.value?.setExpandedKeys) {
					treeRef.value.setExpandedKeys(expandedKeys.value)
				}
				checkedNodes()
			})
		},
		{ deep: true }
	)

	onMounted(() => {
		nextTick(() => {
			if (treeRef.value?.setExpandedKeys) {
				treeRef.value.setExpandedKeys(expandedKeys.value)
			}
			checkedNodes()
		})
	})

	const changeTree = val => {
		btnCur.value = val
	}

	function handleMouse(el, data) {
		let display = window.getComputedStyle(el.target, '::after').display

		if (display == 'block') {
			data.showTitle = true
		} else {
			data.showTitle = false
		}
	}
	/**
	 * 专题是否显示
	 * @param item 专题服务信息
	 */
	const atlasTagClick = item => {
		item.choose = !item.choose
		let lyr = mapStore.activeMap.lyrManager.getLayer(item.themeName)
		if (lyr) {
			let subLyrs = lyr.getLayers()
			if (subLyrs?.length) {
				let subLyr = subLyrs.find(m => m.lyrName == item.serviceName)
				if (subLyr?.layer) {
					subLyr.layer.setVisible(!item.choose)
				}
			}
		}
	}
	/**
	 * 树节点点击事件
	 * @param data
	 * @param node
	 * @param event
	 */
	const handleNodeClick = data => {
		if (data.childrens?.length > 0) {
			return false
		}
		if (data.type === 'theme') {
			let dataArr = mapStore.loadedThemes.find(n => n.themeInfo.id == data.id)
			if (dataArr?.serviceInfo?.length) {
				dataArr.serviceInfo.forEach(item => {
					if (!item.hasOwnProperty('choose')) {
						item.choose = false
					}
					item.opacity = 100
					item.id = item.serviceId
					item.name = item.serviceName
					item.type = 'layer'
				})
				data.childrens = dataArr.serviceInfo
			}
			return false
		} else {
			return false
		}
	}
	/**
	 * 监听分屏变化，切换目录树
	 */
	emitter.on('activemap', ({ mapName, map }) => {
		checkedNodes()
	})

	/**
	 * 勾选复选框加载专题（演示矢量 / 正式配置专题分流）
	 */
	const checkHandle = (data, info) => {
		try {
			const checkedKeys = info?.checkedKeys || treeRef.value?.getCheckedKeys?.() || []
			const isDemoNode =
				data?.demo || data?.url || String(data?.id || '').startsWith('demo-') || data?.layerName?.startsWith?.('demo-')
			if (isDemoNode) {
				syncDemoLayers(checkedKeys)
				return
			}
			if (data?.type === 'theme') {
				loadTheme(data)
				return
			}
			// 文件夹勾选时：同步其下正式专题的加载/卸载
			if (data?.type === 'folder') {
				syncOfficialThemes(checkedKeys)
			}
		} catch (err) {
			ElMessage({
				showClose: true,
				message: err.message || String(err),
				type: 'warning'
			})
		}
	}

	const isDemoTheme = theme =>
		!theme ||
		theme.demo ||
		theme.url ||
		String(theme.id || '').startsWith('demo-') ||
		String(theme.layerName || '').startsWith('demo-')

	const flattenOfficialThemes = (nodes = [], acc = []) => {
		;(nodes || []).forEach(item => {
			if (item?.type === 'theme' && !isDemoTheme(item)) acc.push(item)
			if (item?.childrens?.length) flattenOfficialThemes(item.childrens, acc)
		})
		return acc
	}

	const syncOfficialThemes = checkedKeys => {
		const keySet = new Set(checkedKeys)
		flattenOfficialThemes(systemStore.catalogTreeData).forEach(theme => {
			const loaded = mapStore.loadedThemes?.find(
				m => m.themeInfo.id === theme.id || m.themeInfo.themeId == theme.themeId
			)
			if (keySet.has(theme.id) && !loaded) {
				loadTheme(theme, { forceLoad: true })
			} else if (!keySet.has(theme.id) && loaded) {
				loadTheme(theme, { forceUnload: true })
			}
		})
	}

	const syncDemoLayers = checkedKeys => {
		const keySet = new Set(checkedKeys)
		flattenDemoThemes(demoCatalogTree).forEach(theme => {
			const loaded = mapStore.loadedThemes.find(m => m.themeInfo.id === theme.id)
			if (keySet.has(theme.id) && !loaded) {
				loadDemoTheme(theme)
			} else if (!keySet.has(theme.id) && loaded) {
				unloadDemoTheme(theme)
			}
		})
		checkedNodes()
	}

	const loadDemoTheme = themeInfo => {
		const map = mapStore.activeMap
		if (!map) {
			ElMessage.warning('地图尚未初始化')
			return
		}
		const lyrName = themeInfo.layerName
		if (map.lyrManager.getLayer(lyrName)) {
			map.lyrManager.removeLayer(lyrName)
		}
		const group = buildLayer(LayerType.Group, {
			name: lyrName,
			zIndex: themeInfo.zIndex ?? 40,
			layers: [
				{
					type: LayerType.Vector,
					lyrOption: {
						name: themeInfo.name,
						url: themeInfo.url,
						lyrStyle: themeInfo.lyrStyle,
						zIndex: themeInfo.zIndex ?? 40
					}
				}
			]
		})
		map.lyrManager.addLayer(lyrName, group)
		mapStore.saveLoadThemeInfo(themeInfo, [
			{
				serviceId: `${themeInfo.id}-lyr`,
				serviceName: themeInfo.name,
				themeName: lyrName,
				opacity: 100,
				choose: false
			}
		])
		flyToDemoLayer(group)
	}

	const unloadDemoTheme = themeInfo => {
		const map = mapStore.activeMap
		const lyrName = themeInfo.layerName
		if (map?.lyrManager.getLayer(lyrName)) {
			map.lyrManager.removeLayer(lyrName)
		}
		const key = mapStore.mapName
		if (mapStore.loadedthemes[key]) {
			mapStore.loadedthemes[key] = mapStore.loadedthemes[key].filter(m => m.themeInfo.id !== themeInfo.id)
		}
	}

	const flyToDemoLayer = group => {
		const map = mapStore.activeMap
		const sub = group.getLayers()?.[0]?.layer
		const source = sub?.ol?.getSource?.()
		if (!map || !source) return
		const tryFit = () => {
			const extent = source.getExtent()
			if (!extent || !Number.isFinite(extent[0])) return
			try {
				map.flyToManager.flyToRegion(turf.bboxPolygon(extent))
			} catch (err) {
				map.olView.fit(extent, { padding: [80, 80, 80, 80], duration: 800, maxZoom: 12 })
			}
		}
		if (source.getFeatures().length) {
			tryFit()
			return
		}
		source.once('featuresloadend', tryFit)
		source.once('featuresloaderror', () => {
			ElMessage.warning('矢量数据加载失败')
		})
	}

	/**
	 * 加载 / 卸载正式专题（目录树管理绑定的服务）
	 * @param {{ forceLoad?: boolean, forceUnload?: boolean }} opts 文件夹批量同步时强制方向，避免 toggle 错乱
	 */
	const loadTheme = (themeInfo, opts = {}) => {
		const themeId = themeInfo.themeId ?? themeInfo.id
		const loaded = mapStore.loadedThemes?.find(m => m.themeInfo.themeId == themeId || m.themeInfo.id == themeInfo.id)
		const shouldUnload = opts.forceUnload || (!opts.forceLoad && loaded)
		if (shouldUnload) {
			if (loaded) {
				try {
					mapStore.unLoadTheme(loaded.themeInfo || themeInfo)
				} catch (err) {
					console.warn(err)
				}
			}
			checkedNodes()
			return
		}
		api
			.getLayersById({ themeId })
			.then(res => {
				if (res?.data?.length) {
					mapStore.loadTheme({ ...themeInfo, themeId }, res.data)
					checkedNodes()
				} else {
					ElMessage.warning(`专题「${themeInfo.name || themeId}」未绑定服务`)
					checkedNodes()
				}
			})
			.catch(err => {
				console.error(err)
				ElMessage.warning(`加载专题「${themeInfo.name || themeId}」失败`)
				checkedNodes()
			})
	}

	/**
	 * 勾选专题目录树
	 */
	const checkedNodes = () => {
		if (treeRef?.value) {
			let nodeIds = mapStore.loadedThemes.map(m => m.themeInfo.id)
			selectTreeData.value = []
			mapStore.loadedThemes.forEach(item => {
				if (item?.themeInfo) {
					selectTreeData.value.push(JSON.parse(JSON.stringify(item.themeInfo)))
				}
			})

			treeRef.value.setCheckedKeys(nodeIds)
			mapStore.setCheckedKeys(nodeIds)
		}
	}

	const opacityChange = value => {
		if (value?.themeName && value?.opacity) {
			let lyrInfo = mapStore.activeMap.layerManager.getLayer(value?.themeName)
			lyrInfo.setOpacity(value?.opacity / 100)
		}
	}

	const filterNode = (value, data) => {
		if (!value) return true
		return data.name.includes(value)
	}
</script>

<style lang="scss" scoped>
	.comResourceCatalog {
		overflow: hidden;
		height: calc(100% - 50px);
		background-color: var(--el-bg-color);
	}
	.atlas {
		padding: 0 12px 16px;
		flex-shrink: 0;
		height: calc(100% - 90px);
		overflow: hidden;
		overflow-y: auto;
		:deep() {
			.el-tree-node__content {
				padding: 8px 0 8px 18px;
				height: unset !important;
			}
		}
	}
	.treeBox {
		flex-grow: 1;
		padding: 0 16px;
		min-height: calc(100% - 102px);
		border-radius: 12px;
	}
	.custom-tree-node {
		width: 100%;
		display: grid;
		grid-template-columns: auto min-content;
		overflow: hidden;
	}
	.custom-tree-node > span {
		overflow: hidden;
		text-overflow: ellipsis;
	}
	.label1 {
		--show: false;
		animation: check 1s;
		animation-timeline: scroll(x self);
	}
	.label1::after {
		display: none;
	}
	@keyframes check {
		from,
		to {
			--show: true;
		}
	}
	@container style(--show:true) {
		.label1:hover::after {
			display: block;
		}
	}
</style>
