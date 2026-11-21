<template>
	<ZDialogCom :wrapStyle="wrapStyle">
		<template #header>
			<HeadPopCom title="滨海旅游区" @close="close"></HeadPopCom>
		</template>
		<div class="popBody oy">
			<div class="listWrap max-w noSelect">
				<p class="pt-10 pb-10" style="color: white; font-weight: bold; font-size: 18px">滨海旅游区：</p>
				<VerticalTable :rows="2" :tableData="transformToTableData(listData)"></VerticalTable>
			</div>
			<div class="flex faj">
				<img
					width="500"
					height="300"
					src="http://pic.enorth.com.cn/005/017/836/00501783619_92ae1336.jpg"
					alt="Smiley face" />
			</div>
		</div>
	</ZDialogCom>
</template>

<script setup>
	import ZDialogCom from '@/components/dialog/index.vue'
	import HeadPopCom from '@/components/dialog/head.vue'
	import VerticalTable from '@/components/verticalTable/index.vue'
	import { getWindowSize, transformToTableData } from '@/utils/index.js'
	import GeoJSONUtil from "@/components/map/primitive/util/GeojsonUtil.js";
	import { getHeaders } from '@/views/map/loadBaseDatas/index.js'

	const wrapStyle = ref({
		width: '557px',
		height: 'calc(100% - 127px)',
		top: '87px',
		left: `${(getWindowSize().w.value - 557) / 2}px`
	})

	const props = defineProps({
		selectedFeature: {
			type: Object,
			default: () => {}
		},
	})

	const emits = defineEmits(['close'])

	const tableData = ref({
		编号: 37,
		行政区: '福建省-福州市-罗源县',
		名称: '下沙海滨度假村海滩',
		经度: 119.61168499999999,
		纬度: 25.817340000000002,
		面积: '',
		管理单位: ''
	})

	const listData = ref([])
	watch(
		() => tableData,
		newValue => {
			if (newValue) {
				listData.value = Object.entries(tableData.value).map(([title, value]) => ({
					title,
					value,
					key: ''
				}))
			}
		},
		{
			deep: true,
			immediate: true
		}
	)

	watch(
		() => props.selectedFeature,
		newValue => {
			if (newValue) {
				let feature = GeoJSONUtil.writeFeatureObject(newValue.properties.features[0]);

				// let data = {};
				// let heads = getHeaders("Marina");
				// for (let [key, val] of Object.entries(feature.properties)) {
				// 	let label = heads.find(item => item.prop == key)?.label || '';
				// 	if (label) {
				// 		data[label] = val
				// 	}
				// }
				tableData.value = feature.properties;
			}
		},
		{
			deep: true,
			immediate: true
		}
	)

	const close = () => {
		emits('close')
	}
</script>

<style lang="scss" scoped>
	.listWrap {
		padding: 0 24px 30px;
	}
	.chartBox {
		padding: 0 24px 24px;
	}
</style>
