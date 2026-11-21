<template>
	<div class="attrTree">
		<template v-if="data?.length > 0">
			<div class="attrTree-item" v-for="(item, index) in data" :key="'attrTree_' + index">
				<div class="attrTree-title cp flex fa" @click="handleTitle(item)">
					<span :class="item.choose ? 'open' : 'close'">
						<el-icon><CaretRight /></el-icon>
					</span>
					<p class="pl-8">{{ item.label }}</p>
				</div>
				<div
					v-show="item.choose"
					class="attrTree-child"
					v-for="(child, cIdx) in item.children"
					:key="'attrTreeChild_' + cIdx">
					<div class="attrTree-title cp flex fa pl-10 mb-8" @click="handleChildTitle(item, child)">
						<span :class="child.choose ? 'open' : 'close'">
							<el-icon><CaretRight /></el-icon>
						</span>
						<p class="pl-8">{{ child.label }}</p>
					</div>
					<div
						v-show="child.choose"
						class="attrTree-child"
						v-for="(features, fIdx) in child.children"
						:key="'attrTreeChild_' + fIdx">
						<div class="attrTree-title cp flex fa pl-32 mb-8" @click="handleChildTitle(child, features)">
							<span :class="features.choose ? 'open' : 'close'">
								<el-icon><CaretRight /></el-icon>
							</span>
							<p class="pl-8">{{ features.label }}</p>
						</div>
						<transition name="fade" mode="out-in">
							<AttrTable v-if="features.choose" :itemData="features"></AttrTable>
						</transition>
					</div>
					<!-- <transition name="fade" mode="out-in">
						<AttrTable v-if="child.choose" :itemData="child.features[0]"></AttrTable>
					</transition> -->
				</div>
			</div>
		</template>
	</div>
</template>

<script setup name="AttrTree">
	import AttrTable from '@/components/spatialQuery/attrTable.vue'
	import { setIdentifyData } from '@/utils/index.js'
	import EsrijsonUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
	import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	const props = defineProps({
		treeDatas: {
			type: Array,
			default: () => []
		},
		// 默认开启手风琴模式
		accordion: {
			type: Boolean,
			default: true
		}
	})

	const data = ref([])

	watch(
		() => props.treeDatas,
		newVal => {
			if (newVal.length > 0) {
				data.value = setIdentifyData(newVal)
			}
		},
		{
			deep: true,
			immediate: true
		}
	)

	onMounted(() => {
		data.value = setIdentifyData(props.treeDatas)
	})

	const handleTitle = item => {
		item.choose = !item.choose
		accordionData(data.value, item)
	}
	const handleChildTitle = (item, child) => {
		child.choose = !child.choose
		accordionData(data.value, item)
		if (!child.children?.length) {
			let olFeature = EsrijsonUtil.readFeature(child)
			if (olFeature) {
				let geojson = GeoJSONUtil.writeFeature(olFeature)
				mapStore.activeMap.featureManager.removeFeatures()
				mapStore.activeMap.flyToManager.flyToRegion(geojson, true)
			}
		}
	}
	// 手风琴模式
	function accordionData(d, i) {
		if (props.accordion) {
			d.forEach(t => {
				if (i.themeName !== t.themeName) {
					t.choose = false
					t.children.forEach(child => {
						child.choose = false
					})
				}
			})
		}
	}
</script>

<style lang="scss" scoped>
	.attrTree {
		&-title {
			height: 32px;
			line-height: 32px;
			color: var(--el-text-color-regular);

			& span.open {
				transition: transform 0.3s ease;
				transform: rotate(90deg);
			}
			& span.close {
				transition: transform 0.3s ease;
				transform: rotate(0deg);
			}
		}
	}
</style>
