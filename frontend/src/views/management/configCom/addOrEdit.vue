<!-- 系统配置-添加/编辑 -->
<template>
	<div class="addOrEdit max-wh noSelect">
		<el-tabs class="demo-tabs max-wh" v-model="state.activeName" type="card" @tab-click="tabsHandle">
			<el-tab-pane :label="tab.label" :name="tab.name" v-for="(tab, tabIdx) in formList" :key="'tabs_' + tabIdx">
				<template v-if="tab.data?.length && state.activeName === tab.name">
					<div class="formItem flex item" v-for="(item, index) in tab.data" :key="'form_' + index">
						<span class="formLabel" :class="{ asterisk: item.required }">{{ item.label }}</span>
						<div class="formWidget">
							<template v-if="item.component === 'el-select'">
								<el-select v-model="form[item.prop]" v-bind="item.attrs" @change="formValidation" @blur="formValidation">
									<el-option
										v-for="option in item.options"
										:key="option.value"
										:label="option.label"
										:value="option.value" />
								</el-select>
							</template>
							<template v-if="item.component === 'el-tree-select'">
								<el-tree-select
									node-key="id"
									v-model="form[item.prop]"
									v-bind="item.attrs"
									:data="item.options"
									:render-after-expand="false" />
							</template>
							<template v-if="item.component === 'el-input'">
								<el-input v-model="form[item.prop]" v-bind="item.attrs" @change="formValidation" @blur="formValidation"></el-input>
							</template>
						</div>
						<p class="errInfo" v-if="!item.rules">{{ item.errInfo }}</p>
					</div>
				</template>
			</el-tab-pane>
		</el-tabs>
	</div>
</template>

<script setup>
	import { validation } from '@/utils/formValidation.js'
	import { _debounce, setTreeProps } from '@/utils/index.js'
	import { useAppStore } from '@/store/modules/app.js'
	import { serviceApi } from '@/api/service/index.js'
	import { systemApi } from '@/api/system/index.js'
	const store = useAppStore()
	const sApi = serviceApi()
	const s2Api = systemApi()

	const props = defineProps({
		title: { type: String, default: '添加配置' },
		type: { type: Boolean, default: true },
		itemData: {
			type: Object,
			default: () => {
				return null
			}
		}
	})

	const formValidation = _debounce(() => {
		const list = JSON.parse(JSON.stringify(formSchema))
		list.forEach(item => {
			item.data = validation(item.data, form)
		})
		formList.value = list
	}, 200)

	const formSchema = [
		{
			label: '配置项',
			name: 'first',
			data: [
				{
					label: '名称',
					prop: 'name',
					required: true,
					component: 'el-input',
					attrs: { clearable: true, placeholder: '请输入名称', style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请输入名称'
				},
				{
					label: '别名',
					prop: 'aliasName',
					required: true,
					component: 'el-input',
					attrs: { clearable: true, placeholder: '请输入别名', style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请输入别名'
				}
			]
		},
		{
			label: '基本配置',
			name: 'second',
			data: [
				{
					label: '系统标题',
					prop: 'title',
					required: true,
					component: 'el-input',
					attrs: { clearable: true, placeholder: '请输入系统标题', style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请输入系统标题'
				},
				{
					label: '系统图标',
					prop: 'logo',
					required: true,
					component: 'el-input',
					attrs: { clearable: true, placeholder: '请输入系统图标', style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请输入系统图标'
				},
				{
					label: '系统风格',
					prop: 'style',
					required: false,
					component: 'el-select',
					options: [
						{ label: '简约', value: 'simple' },
						{ label: '传统', value: 'traditional' }
					],
					attrs: {
						clearable: true,
						placeholder: '请选择',
						filterable: true,
						style: { width: '100%' }
					},
					rules: true,
					regMatch: null,
					errInfo: '请选择系统风格'
				},
				{
					label: '框架版本',
					prop: 'version',
					required: false,
					component: 'el-select',
					options: [
						{ label: '初始版', value: 'Original' },
						{ label: '基础版', value: 'Basic' },
						{ label: '简单版', value: 'Simple' },
						{ label: '高级版', value: 'Advanced' },
						{ label: '旗舰版', value: 'Ultimate' },
						{ label: '移动版', value: 'Mobile' }
					],
					attrs: {
						clearable: true,
						placeholder: '请选择',
						filterable: true,
						style: { width: '100%' }
					},
					rules: true,
					regMatch: null,
					errInfo: '请选择框架版本'
				},
				{
					label: '默认目录',
					prop: 'catalog',
					required: false,
					component: 'el-select',
					options: [
						{ label: '矢量旧版', value: '1' },
						{ label: '栅格旧版', value: '2' },
						{ label: '矢量', value: '3' },
						{ label: '栅格', value: '4' }
					],
					attrs: {
						clearable: true,
						placeholder: '请选择',
						filterable: true,
						style: { width: '100%' }
					},
					rules: true,
					regMatch: null,
					errInfo: '请选择默认目录'
				}
			]
		},
		{
			label: '底图配置',
			name: 'third',
			data: [
				{
					label: '地图',
					// 电子底图：与工具栏「电子」一致，读取 imageTheme
					prop: 'imageTheme',
					required: true,
					component: 'el-tree-select',
					options: [],
					attrs: {
						props: { children: 'children', label: 'label', value: 'id' },
						placeholder: '请选择电子底图专题',
						clearable: true,
						style: { width: '100%' }
					},
					rules: true,
					regMatch: null,
					errInfo: '请选择地图'
				},
				{
					label: '影像',
					// 影像底图：与工具栏「影像」一致，读取 mapTheme
					prop: 'mapTheme',
					required: true,
					component: 'el-tree-select',
					options: [],
					attrs: {
						props: { children: 'children', label: 'label', value: 'id' },
						placeholder: '请选择影像底图专题',
						clearable: true,
						style: { width: '100%' }
					},
					rules: true,
					regMatch: null,
					errInfo: '请选择影像'
				}
			]
		},
		{
			label: '图形配置',
			name: 'fourth',
			data: [
				{
					label: '空间参考标识',
					prop: 'code',
					required: true,
					component: 'el-select',
					options: [],
					attrs: { clearable: true, placeholder: '请选择', filterable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请选择空间参考标识'
				},
				{
					label: '最大级别',
					prop: 'maxZoom',
					required: true,
					component: 'el-select',
					options: Array.from({ length: 21 }).map((_, i) => ({ value: i, label: String(i) })),
					attrs: { clearable: true, placeholder: '请选择', filterable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请选择最大级别'
				},
				{
					label: '最小级别',
					prop: 'minZoom',
					required: true,
					component: 'el-select',
					options: Array.from({ length: 21 }).map((_, i) => ({ value: i, label: String(i) })),
					attrs: { clearable: true, placeholder: '请选择', filterable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请选择最小级别'
				},
				{
					label: '初始级别',
					prop: 'zoom',
					required: true,
					component: 'el-select',
					options: Array.from({ length: 21 }).map((_, i) => ({ value: i, label: String(i) })),
					attrs: { clearable: true, placeholder: '请选择', filterable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请选择初始级别'
				},
				{
					label: '范围',
					prop: 'extent',
					required: false,
					component: 'el-input',
					attrs: { rows: '1', placeholder: '请输入范围', type: 'textarea', resize: 'none', style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请输入范围'
				},
				{
					label: '中心点',
					prop: 'center',
					required: true,
					component: 'el-input',
					attrs: { clearable: true, placeholder: '请输入', style: { width: '100%' } }
				},
				{
					label: '原点',
					prop: 'origin',
					required: false,
					component: 'el-input',
					attrs: { clearable: true, placeholder: '请输入', style: { width: '100%' } }
				},
				{
					label: '单位',
					prop: 'unit',
					required: true,
					component: 'el-select',
					options: [
						{ value: 'm', label: 'm' },
						{ value: 'dd', label: 'dd' }
					],
					attrs: { clearable: true, placeholder: '请选择', filterable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请选择单位'
				},
				{
					label: '级别',
					prop: 'resolutions',
					required: false,
					component: 'el-input',
					attrs: { rows: '11', placeholder: '请输入', type: 'textarea', resize: 'none', style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请输入级别'
				}
			]
		}
	]

	const formList = ref([])
	const form = ref({
		project: '',
		name: '',
		aliasName: '',
		title: '',
		theme: '',
		version: '',
		catalog: '',
		count: '',
		logo: '',
		mapTheme: '',
		imageTheme: '',
		map3dTheme: '',
		code: '',
		projection: '',
		maxZoom: 0,
		minZoom: 0,
		style: 'simple',
		zoom: 0,
		sortindex: 0,
		extent: '',
		center: '',
		origin: '',
		unit: '',
		resolutions: '',
		cjr: ''
	})
	const state = reactive({ activeName: 'first' })
	const defaultForm = {
		project: '',
		name: '',
		aliasName: '',
		title: '',
		theme: '',
		version: '',
		catalog: '',
		count: '',
		logo: '',
		mapTheme: '',
		imageTheme: '',
		map3dTheme: '',
		code: '',
		projection: '',
		maxZoom: 0,
		minZoom: 0,
		style: 'simple',
		zoom: 0,
		sortindex: 0,
		extent: '',
		center: '',
		origin: '',
		unit: '',
		resolutions: '',
		cjr: ''
	}

	const applyItemDataToForm = itemData => {
		form.value = {
			...defaultForm,
			cjr: store.userInfo.username,
			...(itemData || {})
		}
	}

	onMounted(() => {
		formList.value = formSchema
		applyItemDataToForm(props.itemData)
		getDict()
		getThemeTree('系统底图配置')
	})

	watch(
		() => props.itemData,
		newVal => {
			applyItemDataToForm(newVal)
		}
	)

	async function getThemeTree(themename) {
		const res = await sApi.themeTree({ project: '', themename })
		if (res?.data) {
			const themeTree = setTreeProps(res.data, 'nodeName', 'nodeId', 'nodeId')
			themeTree.forEach(item => (item.children = []))
			formList.value[2].data.forEach(item => {
				item.options = themeTree
			})
		}
		return res
	}

	const getDict = () => {
		s2Api
			.dictTypeQuery({ like_zdlxbm: 'projectionConfig' })
			.then(res => {
				if (res.data?.length > 0) {
					const data = res.data[0]
					s2Api
						.dictDetailQuery({ flid: data.id })
						.then(r => {
							if (r.data?.length > 0) {
								formList.value[3].data[0].options = setTreeProps(r.data, 'zdbm', 'id', 'zdbm')
							}
						})
						.catch(err => ElMessage.error(err.message))
				}
			})
			.catch(err => ElMessage.error(err.message))
	}

	const tabsHandle = () => {
		formList.value = formSchema
	}

	defineExpose({ form })
</script>

<style lang="scss">
	.grade_box.status {
		display: block;
		position: relative;
		box-shadow: none;
		background: #fff;
		border: 1px solid #dcdfe6;
		margin-top: 4px;
		height: 200px;
		border-radius: 4px;
	}
	.viewport {
		position: relative;
		height: 100%;
	}
	.el-checkbox {
		line-height: 40px;
	}
</style>
