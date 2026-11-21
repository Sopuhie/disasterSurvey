<template>
	<div class="w-278 max-h treePanelBox" v-if="paramsId !== 'basic'">
		<div class="max-w pb-10 pl-10 pr-10">
			<el-input v-model="filterText" style="width: 100%" placeholder="请输入名称" :prefix-icon="Search" />
		</div>
		<div class="treeBox pl-10 pr-10 oy" style="height: calc(100% - 42px)">
			<el-tree
				ref="treeRef"
				:data="treeData"
				node-key="id"
				:props="defaultProps"
				default-expand-all
				:filter-node-method="filterNode">
				<template #default="{ node, data }">
					<div class="custom-tree-node" @click.stop>
						<span v-if="!node.isLeaf">{{ node.label }}</span>
						<el-radio v-else :label="data.id" v-model="selectedName" size="large">
							{{ node.label }}
						</el-radio>
					</div>
				</template>
			</el-tree>
		</div>
	</div>
</template>
<script setup>
	import { Search } from '@element-plus/icons-vue'
	import { useRouter } from 'vue-router'
	import loadDatas from '@/views/map/loadDatas'
	import { loadTreeDatas } from '@/views/map/treeDatas/index.js'
	import emitter from '@/components/map/utils/eventbus'

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	import { onMounted } from 'vue'

	const defaultProps = {
		children: 'children',
		label: 'label'
	}
	const treeRef = ref(null)
	const treeData = ref([])
	const filterText = ref('')
	const selectedName = ref('')
	let pathName = {
		'/survey/0': 'Coastal',
		'/survey/1': 'Fishing',
		'/survey/2': 'Seawater',
		'/survey/3': 'Marina',
		'/disasterSurvey/0': 'Coastal',
		'/disasterSurvey/1': 'Fishing',
		'/disasterSurvey/2': 'Seawater',
		'/disasterSurvey/3': 'Marina'
	}

	const route = useRoute()
	const paramsId = computed(() => route.params.id)

	watch(paramsId, async val => {
		treeData.value = await loadTreeDatas(pathName[route.fullPath])
	})

	watch(filterText, val => {
		treeRef.value.filter(val)
	})

	watch(selectedName, val => {
		emitter.emit('changedselectedHandle', val)
		switch (pathName[route.fullPath]) {
			case 'Coastal':
				return loadDatas('海岸防护工程隐患整治效果评估', val)
			case 'Fishing':
				return loadDatas('渔港隐患整治效果评估', val)
			case 'Seawater':
				return loadDatas('海水养殖区隐患整治效果评估', val)
			case 'Marina':
				return loadDatas('滨海旅游区隐患整治效果评估', val)
		}
	})

	onMounted(async () => {
		const router = useRouter()
		treeData.value = await loadTreeDatas(pathName[router.currentRoute.value.path])

		switch (route.name) {
			case 'Coastal':
				selectedName.value = '城头海堤'
				break
			case 'Fishing':
				selectedName.value = '屿头乡养殖单元'
				break
			case 'Seawater':
				selectedName.value = '沙埔镇围海养殖'
				break
			case 'Marina':
				selectedName.value = '坛南湾景区'
				break
		}
	})

	const filterNode = (value, data) => {
		if (!value) return true
		return data.label.includes(value)
	}
</script>

<style lang="scss" scoped>
	.treePanelBox {
		padding: 10px 0;
		border-radius: 3px;
		background: white;
	}
</style>
