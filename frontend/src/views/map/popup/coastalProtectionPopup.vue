<template>
	<ZDialogCom :wrapStyle="wrapStyle">
		<template #header>
			<HeadPopCom title="海岸防护工程" @close="close"></HeadPopCom>
		</template>
		<div class="popBody oy">
			<div class="listWrap max-w noSelect">
				<p class="pt-10 pb-10" style="font-weight: bold; font-size: 18px">海岸防护工程：</p>
				<VerticalTable :rows="2" :tableData="transformToTableData(listData)"></VerticalTable>
			</div>
			<div class="flex faj">
				<img
					width="500"
					height="300"
					src="https://k.sinaimg.cn/n/sinakd20240905s/225/w1080h745/20240905/e3fb-a3fa7ac3fb3b6feb473f4c1db1060e4c.jpg/w700d1q75cms.jpg"
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

	const emits = defineEmits(['close'])

	const props = defineProps({
		selectedFeature: {
			type: Object,
			default: () => {}
		}
	})

	const tableData = ref({
		编号: 120,
		类型: 0,
		行政区: '福建省-福州市-马尾区',
		名称: '马尾堤段',
		归属单位: ' ',
		长度: 0,
		起始位置: '',
		关联: '',
		创建时间: ''
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
				// let heads = getHeaders("Coastal");
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
