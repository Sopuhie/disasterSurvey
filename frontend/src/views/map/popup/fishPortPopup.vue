<template>
	<ZDialogCom :wrapStyle="wrapStyle">
		<template #header>
			<HeadPopCom title="渔港" @close="close"></HeadPopCom>
		</template>
		<div class="popBody oy">
			<div class="listWrap max-w noSelect">
				<p class="pt-10 pb-10" style="color: white; font-weight: bold; font-size: 18px">渔港：</p>
				<VerticalTable :rows="2" :tableData="transformToTableData(listData)"></VerticalTable>
			</div>
			<div class="flex faj">
				<img
					width="500"
					height="300"
					src="https://q8.itc.cn/q_70/images01/20240409/79b85e50a3224dab9898b8a9bfa09974.png"
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
		}
	})

	const emits = defineEmits(['close'])

	const tableData = ref({
		编号: 120,
		分类: 0,
		行政区: '福建省-福州市-长乐区',
		名称: '长乐海堤机场堤段',
		类型: ' ',
		长度: 2420,
		归属: '',
		管辖单位: '',
		时间: ''
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
				tableData.value = newValue.properties
				// let data = {};
				// let heads = getHeaders("Fishing");
				// for (let [key, val] of Object.entries(newValue.properties)) {
				// 	let label = heads.find(item => item.prop == key)?.label || '';
				// 	if (label) {
				// 		data[label] = val
				// 	}
				// }
				// tableData.value = data;
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
