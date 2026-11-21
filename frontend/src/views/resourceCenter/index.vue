<template>
	<div class="resourceCenter pageFixed">
		<PageTabs :data="tabList">
			<template #right>
				<el-button v-btn type="primary" size="small" @click="reBack">返回</el-button>
			</template>
		</PageTabs>
	</div>
</template>

<script setup>
	import PageTabs from '@/components/pageTabs/index.vue'

	const router = useRouter()

	const tabList = reactive([{ name: '服务管理', value: 1 }])

	function reBack() {
		router.push('/')
	}
</script>

<style lang="scss" scoped></style>
