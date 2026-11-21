<!-- 系统配置-添加/编辑 -->
<template>
	<div class="addOrEdit max-wh noSelect">
		<template v-if="formSchema?.length">
			<div class="formItem flex item" v-for="(item, index) in formSchema[0]['data']" :key="'form_' + index">
				<span class="formLabel" :class="{ asterisk: item.required }">{{ item.label }}</span>
				<div class="formWidget">
					<template v-if="item.component === 'el-select'">
						<el-select
							v-model="form[item.prop]"
							:placeholder="item.placeholder"
							v-bind="item.attrs"
							@change="formValidation"
							@blur="formValidation">
							<el-option v-for="item in item.options" :key="item.value" :label="item.label" :value="item.value" />
						</el-select>
					</template>
					<template v-if="item.component === 'el-input'">
						<el-input
							v-model="form[item.prop]"
							:placeholder="item.placeholder"
							v-bind="item.attrs"
							@change="formValidation"
							@blur="formValidation"></el-input>
					</template>
					<template v-if="item.component === 'el-radio-group'">
						<el-radio-group v-model="form[item.prop]">
							<el-radio :label="opt.value" v-for="opt in item.options" :key="opt.value">{{ opt.label }}</el-radio>
						</el-radio-group>
					</template>
				</div>
				<p class="errInfo" v-if="!item.rules">{{ item.errInfo }}</p>
			</div>
		</template>
	</div>
</template>

<script setup>
	import { validation } from '@/utils/formValidation.js'
	import { _debounce } from '@/utils/index.js'

	const props = defineProps({
		title: { type: String, default: '添加配置' },
		type: { type: Boolean, default: true },
		itemData: {
			type: Object,
			default: () => {
				return null
			}
		},
		routerData: {
			type: Array,
			default: () => []
		}
	})

	// 表单验证方法
	const formValidation = _debounce(() => {
		const list = JSON.parse(JSON.stringify(formSchema))
		list.forEach(item => {
			item.data = validation(item.data, form.value)
		})
		formList.value = list
	}, 200)

	let formSchema = [
		{
			label: '配置项',
			name: 'first',
			data: [
				{
					label: '标题',
					prop: 'title',
					required: true,
					component: 'el-input',
					placeholder: '',
					attrs: { clearable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请输入标题'
				},
				{
					label: '图标',
					prop: 'icon',
					required: false,
					component: 'el-input',
					placeholder: 'elementPlus图标',
					attrs: { clearable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: ''
				},
				{
					label: '路径',
					prop: 'path',
					required: true,
					component: 'el-input',
					placeholder: '',
					attrs: { clearable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请输入路径'
				},
				{
					label: '路由名称',
					prop: 'name',
					required: true,
					component: 'el-input',
					placeholder: '',
					attrs: { clearable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请输入路由名称'
				},
				{
					label: '组件',
					prop: 'component',
					required: true,
					component: 'el-input',
					placeholder: '请输入iframe或views ',
					attrs: { clearable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请输入iframe或views'
				},
				{
					label: '重定向',
					prop: 'redirect',
					required: false,
					component: 'el-input',
					placeholder: '',
					attrs: { clearable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: ''
				},
				{
					label: '嵌入页面',
					prop: 'iframe',
					required: false,
					component: 'el-input',
					placeholder: '',
					attrs: { clearable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: ''
				},
				{
					label: '上级路由',
					prop: 'parentId',
					required: false,
					component: 'el-select',
					placeholder: '请选择上级路由',
					options: [
						{ label: '一张图项目', value: '1' },
						{ label: '临沂项目', value: '2' }
					],
					attrs: { clearable: true, filterable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请选择上级路由'
				},
				{
					label: '排序',
					prop: 'sortIndex',
					required: true,
					component: 'el-input',
					placeholder: '',
					attrs: { clearable: true, style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: '请输入排序'
				},
				{
					label: '不放入缓存',
					prop: 'noCache',
					required: false,
					component: 'el-radio-group',
					placeholder: '',
					options: [
						{ label: '是', value: 1 },
						{ label: '否', value: 0 }
					],
					attrs: { style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: ''
				},
				{
					label: '隐藏',
					prop: 'hidden',
					required: false,
					component: 'el-radio-group',
					placeholder: '',
					options: [
						{ label: '是', value: 1 },
						{ label: '否', value: 0 }
					],
					attrs: { style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: ''
				},
				{
					label: '始终显示',
					prop: 'alwaysShow',
					required: false,
					component: 'el-radio-group',
					placeholder: '',
					options: [
						{ label: '是', value: 1 },
						{ label: '否', value: 0 }
					],
					attrs: { style: { width: '100%' } },
					rules: true,
					regMatch: null,
					errInfo: ''
				}
			]
		}
	]

	const formList = ref([])
	const form = ref({
		routerId: '',
		title: '',
		icon: '',
		path: '',
		name: '',
		noCache: 0,
		component: '',
		redirect: '',
		parentId: '-1',
		hidden: 0,
		alwaysShow: 1,
		sortIndex: '',
		iframe: ''
	})

	onMounted(() => {
		formList.value = formSchema
		if (props.routerData?.length) {
			let options = []
			props.routerData.forEach(item => {
				if (item.parentId === '-1') {
					options.push({ label: item.title, value: item.routerId })
				}
			})
			// 更新"上级路由"下拉选项数据
			formSchema[0]['data'][7]['options'] = options
		}
		if (props.itemData) {
			Object.keys(form.value).forEach(key => {
				form.value[key] = props.itemData[key] == undefined ? form.value[key] : props.itemData[key]
			})
		}
	})

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
