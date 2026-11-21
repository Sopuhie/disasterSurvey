<!-- 已开图集 -->
<template>
	<div class="comResourceCatalog max-wh">
		<div class="lists oy" v-if="loadedThemes.length > 0">
			<draggable
				v-model="loadedThemes"
				item-key="id"
				v-bind="dragOptions"
				:component-data="{
					tag: 'div',
					type: 'transition-group',
					name: !dragging ? 'flip-list' : null
				}"
				@start="dragging = true"
				@end="datadragEnd">
				<template #item="{ element }">
					<div class="list-group-item m-16 flex jsb fc">
						<p class="atlas flex fas jsb">
							<span class="title">{{ element.name }}</span>
							<span class="atias_icon flex fa">
								<el-icon class="del_btn" @click.stop="del(element)"><Close /></el-icon>
							</span>
						</p>
						<p class="progress flex fa jsb">
							<el-slider style="width: 70%" v-model="element.opacity" @change="silderChange(element)"></el-slider>
							<span class="silder_txt" v-if="!element.sliderIpt" @click="silderTxtHandle(element)"
								>{{ element.opacity }}%</span
							>
							<el-input
								v-if="element.sliderIpt"
								ref="ipt"
								style="width: 50px"
								:maxLength="3"
								v-model.number="element.opacity"
								autofocus
								@blur="blur(element)"
								@change="sliderIptChange" />
						</p>
					</div>
				</template>
			</draggable>
		</div>
		<EmptyCom v-else></EmptyCom>
		<div class="atlas_footer pl-24 pr-24 flex fa jsb" v-if="loadedThemes.length > 0">
			<p>当前地图加载专题：{{ loadedThemes.length }}个</p>
			<el-button v-btn size="small" @click="closeAll">全部清除</el-button>
		</div>
	</div>
</template>

<script setup>
	import draggable from 'vuedraggable'
	import EmptyCom from '@/components/empty/index.vue'
	import emitter from '@/components/map/utils/eventbus.js'

	import { useMapStore } from '@/store/modules/map.js'
	import { onActivated } from 'vue'
	const mapStore = useMapStore()
	/**
	 * 已开专题集合
	 */
	const loadedThemes = ref([])
	const dragging = ref(false)
	const ipt = ref(null)
	const dragOptions = reactive({
		animation: 200,
		group: 'description',
		disabled: false,
		ghostClass: 'ghost'
	})

	onMounted(() => {
		loadedThemesHandler()
	})

	onActivated(() => {
		loadedThemesHandler()
	})

	/**
	 *
	 */
	function loadedThemesHandler() {
		const data = mapStore.loadedThemes.map(m => m.themeInfo)
		if (data.length > 0) {
			data.forEach(item => {
				item.opacity = 100
				item.sliderIpt = false
			})
			loadedThemes.value = data.reverse()
		}
	}

	/**
	 * 监听分屏变化，切换目录树
	 */
	emitter.on('activemap', ({ mapName, map }) => {
		loadedThemes.value = mapStore.loadedThemes.map(m => m.themeInfo).reverse()
	})
	/**
	 * 展示透明度框点击事件
	 */
	const silderTxtHandle = item => {
		item.sliderIpt = true
		nextTick(() => {
			ipt.value.focus()
		})
	}
	/**
	 * 透明度输入框失去焦点事件
	 */
	const blur = themeInfo => {
		themeInfo.sliderIpt = false
		// itemIdx.value = -1
		if (mapStore.loadedThemes.find(m => m.themeInfo.themeId == themeInfo.themeId)) {
			let lyr = mapStore.activeMap.lyrManager.getLayer(themeInfo.name)
			lyr.setOpacity(themeInfo.opacity / 100)
		}
	}
	/**
	 * 滑块事件
	 */
	const silderChange = themeInfo => {
		if (mapStore.loadedThemes.find(m => m.themeInfo.themeId == themeInfo.themeId)) {
			let lyr = mapStore.activeMap.lyrManager.getLayer(themeInfo.name)
			lyr.setOpacity(themeInfo.opacity / 100)
		}
	}
	/**
	 * 透明度输入框事件
	 */
	const sliderIptChange = item => {
		// console.log("透明度输入框事件", item)
	}
	/**
	 * 拖拽结束
	 */
	const datadragEnd = () => {
		dragging.value = false
		// 设置图层层级
		// 注意  loadedThemes.value的顺序就是图层层级
		for (let index = 0, count = loadedThemes.value.length; index < count; index++) {
			let lyrName = loadedThemes.value[index].name
			let lyr = mapStore.activeMap.lyrManager.getLayer(lyrName)
			lyr.setZIndex(count - index)
		}
		mapStore.activeMap.olMap.render()
	}

	/**
	 * 删除事件
	 */
	const del = themeInfo => {
		if (mapStore.loadedThemes.find(m => m.themeInfo.themeId == themeInfo.themeId)) {
			mapStore.unLoadTheme(themeInfo)
			loadedThemes.value = mapStore.loadedThemes.map(m => m.themeInfo)
		}
	}

	/**
	 * 全部清除
	 */
	const closeAll = () => {
		mapStore.loadedThemes.forEach(m => {
			mapStore.unLoadTheme(m.themeInfo)
		})
		loadedThemes.value = []
		mapStore.setCheckedKeys([])
	}
</script>

<style lang="scss" scoped>
	.comResourceCatalog {
		overflow: hidden;
		background-color: white;
		& .lists {
			width: 100%;
			height: calc(100% - 46px);
		}
		& .list-group-item {
			position: relative;
			left: 0;
			top: 0;
			width: calc(100% - 22px);
			height: 128px;
			border-radius: 4px;
			margin: 20px 10px 16px;
			border: 1px solid rgba(3, 11, 46, 0.08);
			cursor: pointer;
			background-image: url('@/assets/images/tplImg2.png');
			background-repeat: no-repeat;
			background-position: center center;
			background-size: 100% 100%;
			&:hover {
				border: 1px solid rgba(61, 119, 219, 0.38);
				box-shadow: 0px 3px 6px 0px rgba(3, 11, 46, 0.16);
				& .edit_btn {
					color: #366cd6;
				}
				& .del_btn {
					color: #ea4949;
				}
			}
			& .atlas {
				overflow: hidden;
				padding: 4px 6px 4px 0;
				min-height: 30px;
				background: rgba(255, 255, 255, 0.49);
				border-radius: 4px 4px 0 0;
				height: auto;
				& .title {
					display: inline-block;
					padding-left: 18px;
					background: transparent;
					width: 226px;
					font-size: 14px;
					max-height: 44px;
					overflow: hidden;
				}
				& i {
					vertical-align: middle;
					display: inline-block;
					width: 22px;
					height: 22px;
					color: #53535b;
					font-size: 18px;
					margin-left: 6px;
				}
			}
			& .progress {
				width: 100%;
				height: 40px;
				line-height: 40px;
				padding: 0 16px;
				background: rgba(255, 255, 255, 0.6);
				border-radius: 0 0 4px 4px;
			}
		}
		& .atlas_footer {
			width: 100%;
			height: 46px;
			background: #fafafa;
		}
	}
</style>
