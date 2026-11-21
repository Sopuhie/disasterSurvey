<!-- 添加/编辑 -->
<template>
	<div class="add">
		<template v-if="formList.length">
			<div
				class="formItem flex"
				:class="{ item: !item.vif, yItem: item.vif && item.vif(form) === true }"
				v-for="(item, index) in formList[0]['data']"
				:key="'form_' + index">
				<template v-if="item.component !== 'el-row'">
					<span class="formLabel" :class="{ asterisk: item.required }" v-if="!item.vif || item?.vif(form)">{{
						item.label
					}}</span>
					<div class="formWidget">
						<template v-if="item.component === 'el-select'">
							<el-select
								v-model="form[item.prop]"
								:placeholder="item.placeholder"
								v-bind="item.attrs"
								@change="formValidation"
								@blur="formValidation">
								<el-option v-for="opt in item.options" :key="opt.value" :label="opt.label" :value="opt.value" />
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
							<el-input
								v-model="form[item.prop]"
								:placeholder="item.placeholder"
								v-bind="item.attrs"
								@change="formValidation"
								@blur="formValidation"></el-input>
						</template>
						<template v-if="item.component === 'el-radio-group' && (!item.vif || item?.vif(form))">
							<el-radio-group v-model="form[item.prop]">
								<el-radio :label="opt.value" v-for="opt in item.options" :key="opt.value">{{ opt.label }}</el-radio>
							</el-radio-group>
						</template>
					</div>
				</template>
				<template v-else>
					<div class="max-w flex fa" v-if="item.data?.length">
						<div class="formItem formChild flex" v-for="(child, childIdx) in item.data" :Key="'form_child_' + childIdx">
							<span class="formLabel" :class="{ asterisk: child.required }">{{ child.label }}</span>
							<div class="formWidget">
								<template v-if="child.component === 'el-input'">
									<el-input
										v-model="form[child.prop]"
										:placeholder="child.placeholder"
										v-bind="child.attrs"
										@change="formValidation"
										@blur="formValidation"></el-input>
								</template>
							</div>
							<p class="errInfo" v-if="!child.rules">{{ child.errInfo }}</p>
						</div>
					</div>
				</template>
				<p class="errInfo" v-if="!item.rules">{{ item.errInfo }}</p>
			</div>
		</template>
	</div>
</template>

<script setup>
	import { validation } from '@/utils/formValidation.js'
	import { setTreeProps, deepCopy, _debounce } from '@/utils/index.js'

	import { useAppStore } from '@/store/modules/app.js'
	const store = useAppStore()

	import { systemApi } from '@/api/system/index.js'
	const sApi = systemApi()

	const props = defineProps({
		itemData: {
			type: Object,
			default: () => {
				return null
			}
		},
		options: {
			type: Array,
			default: () => []
		}
	})

	// 表单验证方法
	const formValidation = _debounce(() => {
		let list = deepCopy(formSchema)
		list.forEach(item => {
			item.data = validation(item.data, form)
		})
		formList.value = list
	}, 200)

	const formSchema = [
		{
			name: 'first',
			data: [
				{
					label: '所属子系统',
					prop: 'system',
					required: false,
					component: 'el-select',
					placeholder: '请选择所属子系统',
					options: [],
					attrs: { clearable: true, filterable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: ''
				},
				{
					label: '所属上级',
					prop: 'parentId',
					required: false,
					component: 'el-tree-select',
					options: [],
					attrs: {
						showCheckbox: true,
						checkStrictly: true,
						props: { children: 'children', label: 'label', value: 'id' },
						placeholder: '请选择',
						clearable: true,
						style: { width: '100%' }
					},
					rules: true,
					regMatch: null,
					errInfo: '请选择所属上级'
				},
				{
					label: '功能类型',
					prop: 'nodetype',
					required: true,
					component: 'el-radio-group',
					placeholder: '',
					options: [
						{ label: '菜单', value: 'menu' },
						{ label: '按钮', value: 'action' },
						{ label: '目录', value: 'folder' },
						{ label: '侧边菜单', value: 'leftmenu' },
						{ label: '外部模块', value: 'outmodule' }
					],
					attrs: { style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请选择功能类型'
				},
				{
					label: '按钮分组',
					prop: 'actiongroup',
					required: false,
					component: 'el-radio-group',
					placeholder: '',
					vif: function (form) {
						return form.nodetype === 'action'
					},
					options: [
						{ label: '地图基础工具组', value: 'basictools' },
						{ label: '一张图应用工具组', value: 'maptools' },
						{ label: '成果审查功能组', value: 'checktools' }
					],
					attrs: { style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请选择功能类型'
				},
				{
					label: '',
					prop: '',
					required: true,
					component: 'el-row',
					data: [
						{
							label: '功能名称',
							prop: 'name',
							required: true,
							component: 'el-input',
							placeholder: '',
							attrs: { clearable: true, style: { width: '100%' } },
							rules: true,
							regMatch: null,
							errInfo: '请输入功能名称'
						},
						{
							label: '别名',
							prop: 'alias',
							required: true,
							component: 'el-input',
							placeholder: '',
							attrs: { clearable: true, style: { width: '100%' } },
							rules: true,
							regMatch: null,
							errInfo: '请输入别名'
						}
					]
				},
				{
					label: '执行函数',
					prop: 'executetool',
					required: false,
					component: 'el-input',
					placeholder: '',
					attrs: { clearable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: ''
				},
				{
					label: '',
					prop: '',
					required: true,
					component: 'el-row',
					data: [
						{
							label: '图标',
							prop: 'image',
							required: false,
							component: 'el-input',
							placeholder: '',
							attrs: { clearable: true, style: { width: '100%' } },
							rules: true,
							regMatch: null,
							errInfo: ''
						},
						{
							label: '排序',
							prop: 'sortindex',
							required: true,
							component: 'el-input',
							placeholder: '请输入排序',
							attrs: { clearable: true, style: { width: '100%' } },
							rules: true,
							regMatch: null,
							errInfo: '请输入排序'
						}
					]
				},
				{
					label: '路由地址',
					prop: 'routerPath',
					required: false,
					component: 'el-input',
					placeholder: '',
					attrs: { clearable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: ''
				},
				{
					label: '备注',
					prop: 'description',
					required: false,
					component: 'el-input',
					placeholder: '',
					attrs: { rows: '4', type: 'textarea', resize: 'none', style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: ''
				}
			]
		}
	]

	const form = ref({
		actiongroup: 'basictools',
		project: '',
		system: '',
		parentId: '',
		nodetype: '',
		viewtype: '',
		name: '',
		alias: '',
		executetool: '',
		image: '',
		sortindex: '',
		description: '',
		cjr: ''
	})
	const formList = ref([])

	onMounted(() => {
		getActionTree()
		formList.value = formSchema
		formList.value[0].data[0].options = props.options
		form.value.cjr = store.userInfo.username || 'system'

		if (props.itemData) {
			Object.keys(form.value).forEach(key => {
				form.value[key] = props.itemData[key] == undefined ? form.value[key] : props.itemData[key]
			})
		}
	})

	const getActionTree = () => {
		sApi
			.actionTree()
			.then(res => {
				const data = setTreeProps(res.data, 'name', 'id', 'id')
				formList.value[0].data[1].options = data
			})
			.catch(err => {
				ElMessage.error(err.message)
			})
	}

	defineExpose({ form })
</script>

<style lang="scss" scoped></style>
