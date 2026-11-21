<template>
	<ZDialogCom :wrapStyle="wrapStyle">
		<template #header>
			<HeadPopCom title="海水养殖区" @close="close"></HeadPopCom>
		</template>
		<div class="popBody oy">
			<div class="listWrap max-w noSelect">
				<p class="pt-10 pb-10" style="color: white; font-weight: bold; font-size: 18px">海水养殖区：</p>
				<VerticalTable :rows="2" :tableData="transformToTableData(listData)"></VerticalTable>
			</div>
			<div class="flex faj">
				<img
					width="500"
					height="300"
					src="http://pic.people.com.cn/NMediaFile/2021/0406/MAIN202104061342000530947393449.JPG"
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
		},
	})

	const emits = defineEmits(['close'])

	const tableData = ref({
		编号: 3,
		名称: '屿头乡养殖单元',
		行政区: '福建省-福州市-平潭',
		长度: 0.0311278311,
		面积: 5.3234289999999999e-5,
		方法: ' ',
		类型: ' ',
		城镇: ' ',
		经度: 119.5682521,
		纬度: 25.64143451,
		单位: '平潭县屿头乡屿南村民委员会',
		关联数: 0
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
				tableData.value = newValue.properties;

				// let data = {};
				// let heads = getHeaders("Seawater");
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
