<template>
	<ZDialogCom :wrapStyle="wrapStyle">
		<template #header>
			<HeadPopCom title="渔港" @close="close"></HeadPopCom>
		</template>
		<div class="popBody oy">
			<div class="listWrap max-w noSelect pt-10">
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
	import ZDialogCom from '@/components/dialog/index2.vue'
	import HeadPopCom from '@/components/dialog/head.vue'
	import VerticalTable from '@/components/verticalTable/index.vue'
	import { getWindowSize, transformToTableData } from '@/utils/index.js'
	import { buildPopupListData } from './fieldLabels.js'

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

	const listData = ref([])

	watch(
		() => props.selectedFeature,
		newValue => {
			if (newValue?.properties) {
				listData.value = buildPopupListData(newValue.properties)
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
