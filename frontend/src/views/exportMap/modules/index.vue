<template>
	<ZDialogCom
		title="模板"
		toPath="cancel"
		:footer="false"
		:wrapStyle="wrapStyle"
		:bodyStyle="{ padding: '16px' }"
		@cancel="closeHandler">
		<div class="max-wh">
			<div class="flex fa jend mb-12">
				<el-input
					v-model="form.like_name"
					placeholder="请输入模板名称"
					:prefix-icon="Search"
					clearable
					@keyup.enter.native="getModuleList"
					style="width: 300px"></el-input>
			</div>
			<ul class="modulesBox flex fw oy">
				<li
					:class="['moduleItem w-258 h-258 mb-10', { 'flex faj add cp': index === 0, cur: idx === index }]"
					v-for="(item, index) in list"
					:key="'module_' + index"
					@click="moduleHandler(item, index)">
					<span v-if="index === 0">新建模样</span>
					<div v-else class="max-wh flex fa fc jsb">
						<p class="imgBox p-4 noSelect">
							<img :src="getAssetsFile(item.icon)" alt="" />
						</p>
						<p class="txt flex fa jsb max-w pl-10 pr-10">
							<span class="name textEs">{{ item.name }}</span>
							<span>
								<el-icon class="cp" @click.stop="editHandler(item, index)"><EditPen /></el-icon>
								<el-icon class="cp ml-10" @click.stop="deleteHandler(item, index)"><Delete /></el-icon>
							</span>
						</p>
					</div>
				</li>
			</ul>
		</div>
	</ZDialogCom>
	<AddAndEditCom v-if="showAD" @preview="showAD = true" @cancel=";(showAD = false), getModuleList()"></AddAndEditCom>
</template>

<script setup>
	import { Search } from '@element-plus/icons-vue'
	import ZDialogCom from '@/components/dialog/index.vue'
	import AddAndEditCom from '@/views/exportMap/modules/addAndEdit.vue'
	import { getWindowSize } from '@/utils/index.js'
	import { resetFormData } from '@/views/exportMap/tool.js'
	import { mappingApi } from '@/views/exportMap/api.js'
	const api = mappingApi()

	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()
	import { useExportMapStore } from '@/store/modules/exportMap.js'
	const exportMapStore = useExportMapStore()

	const wrapStyle = ref({
		width: '1102px',
		height: `${getWindowSize().h.value - 200}px`,
		top: '100px',
		left: `${(getWindowSize().w.value - 1102) / 2}px`
	})
	const form = ref({
		current: 0,
		size: 0,
		eq_isshare: '',
		like_name: '',
		loginUser: ''
	})

	const list = ref([{ name: '新建模样', icon: 'A4_a' }])
	const idx = ref(0)
	const showAD = ref(false)

	watch(
		() => form.value.like_name,
		val => {
			if (val === '') {
				getModuleList()
			}
		}
	)

	onMounted(() => {
		getModuleList()
	})

	const emits = defineEmits(['cancel'])
	/**
	 * 获取模样数据
	 */
	const getModuleList = () => {
		form.value.loginUser = appStore.userInfo?.username || 'system'
		api
			.queryWithPage(form.value)
			.then(res => {
				if (res.resultCode === 'S') {
					const data = [{ name: '新建模样', icon: 'A4_a' }]
					res?.data?.forEach(item => {
						const content = JSON.parse(item.content)
						item.icon = content.paper.type
					})
					list.value = res.data?.length > 0 ? data.concat(res.data) : data
				}
			})
			.catch(err => {
				// ElMessage({ message: err, type: 'error' })
			})
	}
	/**
	 * 模样选中事件
	 */
	const moduleHandler = (item, index) => {
		idx.value = index
		if (index === 0) {
			showAD.value = true
			exportMapStore.setSaveType('add')
		}
	}
	/**
	 * 关闭弹窗
	 */
	const closeHandler = () => {
		resetFormData()
		emits('cancel')
	}
	/**
	 * 编辑模板
	 */
	const editHandler = (item, index) => {
		exportMapStore.setFormData({ type: 'all', data: { id: item.id, ...JSON.parse(item.content) } })
		showAD.value = true
		exportMapStore.setSaveType('edit')
	}
	/**
	 * 删除模板
	 */
	const deleteHandler = (item, index) => {
		ElMessageBox.confirm('确定删除该模板吗?', '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		})
			.then(() => {
				api
					.delete({ templateId: item.id })
					.then(res => {
						list.value.splice(index, 1)
					})
					.catch(err => {
						// ElMessage({ message: err, type: 'error' })
					})
			})
			.catch(() => {})
	}

	const getAssetsFile = value => {
		return new URL(`../imgs/${value}.png`, import.meta.url).href
	}
</script>

<style lang="scss" scoped>
	.modulesBox {
		height: calc(100% - 44px);
	}
	.moduleItem {
		border-radius: 4px;
		border: 1px solid #c1c1c1;
		overflow: hidden;
		&:not(:nth-child(4n)) {
			margin-right: 10px;
		}
		&.cur,
		&:hover {
			border: 1px solid #1890ff;
			& .txt {
				color: #1890ff;
			}
		}
		&.add {
			background-color: #f9f9f9;
		}
		& .imgBox {
			max-width: 100%;
			max-height: calc(100% - 36px);
			&:hover {
				& img {
					transform: scale(1.3);
				}
			}
		}
		& img {
			max-width: 100%;
			max-height: 100%;
		}
		& .txt {
			position: relative;
			z-index: 1;
			height: 36px;
			background-color: #f9f9f9;
			& i:hover {
				color: #1890ff;
			}
			& .name {
				width: calc(100% - 50px);
			}
		}
	}
</style>
