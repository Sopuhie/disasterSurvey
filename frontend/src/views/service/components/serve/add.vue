<template>
	<div class="add maxwh">
		<el-cascader
			v-model="addFormMode.serviceType"
			:options="cascaderData"
			class="mb-24"
			:clearable="true"
			:filterable="true"
			style="width: 100%" />
		<FormScheme
			v-if="formData && addFormMode.serviceType?.length"
			:formData="formData"
			:rules="rules"
			:formMode="addFormMode"></FormScheme>
	</div>
</template>

<script setup>
	import FormScheme from '@/components/form/scheme.vue'
	import { addFormData } from '@/views/service/components/serve/formData.js'
	import { getTimeFormatted } from '@/utils/index.js'
	import { setFormData2 } from '@/utils/formValidation.js'
	import { useAppStore } from '@/store/modules/app.js'
	const store = useAppStore()

	import { systemApi } from '@/api/system/index.js'
	const sApi = systemApi()

	const props = defineProps({
		itemData: {
			type: Object,
			default: () => null
		},
		title: {
			type: String,
			default: '添加资源'
		}
	})

	const rules = reactive({
		serviceName: [{ required: true, message: '请填写资源名称', trigger: 'blur' }],
		serviceUrl: [{ required: true, message: '请填写地址', trigger: 'blur' }]
	})

	const addFormMode = ref({
		serviceName: '',
		serviceUrl: '',
		screenshot: '',
		servicePlatform: '',
		cjr: '',
		createtime: getTimeFormatted('YYYY-MM-DD HH:mm:ss'),
		unit: '',
		serviceType: [],
		description: '',
		extentMeta: {}
	})

	const formData = ref(null)
	const configure = ref([])
	const cascaderData = ref([])

	const buildFormData = () => {
		return JSON.parse(JSON.stringify(addFormData))
	}

	watch(
		addFormMode,
		newValue => {
			if (props.title === '添加资源') {
				if (newValue.serviceType.length > 0) {
					const serviceType = newValue.serviceType[1]
					formData.value = buildFormData()
					setParams(formData.value, configure.value, serviceType)
				} else {
					formData.value = buildFormData()
				}
			}
		},
		{ deep: true }
	)

	onMounted(() => {
		formData.value = buildFormData()
		getPageData()
		addFormMode.value.cjr = store.userInfo.username
		if (props.itemData) {
			Object.keys(addFormMode.value).forEach(key => {
				addFormMode.value[key] =
					props.itemData[key] == undefined ? addFormMode.value[key] : props.itemData[key]
			})
			addFormMode.value = setFormData2(addFormMode.value, 'extentMeta')
			addFormMode.value.createtime = props.itemData.cjsj
		}
	})

	const getPageData = async () => {
		const mode = {
			adminFlag: '',
			current: 1,
			size: 20,
			eq_nodetype: '',
			eq_system: '',
			eq_visible: '',
			like_alias: '',
			like_name: '平台服务',
			loginUserId: ''
		}
		sApi
			.actionQueryWithPage(mode)
			.then(res => {
				if (res?.data?.length) {
					configure.value[0] = JSON.parse(res.data[0].configure)
					if (getTreeData(configure.value).length > 0) {
						cascaderData.value = getTreeData(configure.value)[0].children
					}
					if (props.itemData) {
						setParams(formData.value, configure.value, addFormMode.value.serviceType)
					}
				}
			})
			.catch(() => {})
	}

	const getTreeData = data => {
		if (data?.length) {
			filterChildren(data)
			return data
		}
		return []
	}
	function filterChildren(data) {
		data.forEach(item => {
			item.value = item.zdbm ? item.zdbm : ''
			item.label = item.zdz ? item.zdz : ''
			if (item.children?.length > 0) {
				filterChildren(item.children)
			}
		})
	}
	const setParams = (data, paramsData, type) => {
		paramsData.forEach(item => {
			if (item.zdbm === type && item?.params?.length) {
				let obj = { span: 12, children: [] }
				item.params.forEach(c => {
					obj.children.push({
						type: 'input',
						label: c.label,
						prop: `extentMeta.${c.prop}`,
						childAttrs: {
							type: 'text',
							placeholder: `请输入${c.label}`,
							clearable: true,
							readonly: false,
							disabled: false
						}
					})
				})
				data.fields.push(obj)
			} else if (item.children?.length) {
				setParams(data, item.children, type)
			}
		})
		return data
	}

	defineExpose({ addFormMode })
</script>

<style lang="scss" scoped>
	:deep() {
		.el-divider__text {
			color: rgba(255, 255, 255, 0.88);
			font-size: 12px;
			white-space: nowrap;
			text-align: center;
			border-block-start: 0 rgba(5, 5, 5, 0.06);
		}
	}
</style>
