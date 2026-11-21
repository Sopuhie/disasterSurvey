<template>
	<div class="add max-wh">
		<FilterContainer class="pt-4 pb-4">
			<template #left>
				<p class="flex fa">
					<span>服务类型：</span>
					<el-cascader
						v-model="addFormMode.serviceType"
						:options="cascaderData"
						:clearable="true"
						:filterable="true"
						style="width: 300px" />
				</p>
			</template>
		</FilterContainer>
		<PageContainer class="oy flex">
			<div style="width: 50%">
				<h3 class="mb-12">添加资源</h3>
				<div class="pl-12">
					<p class="pb-12">本地上传并注册到资源 (大于10MB的文件请单独上传，小文件支持多个) :</p>
					<el-upload
						ref="uploadRef"
						v-model:file-list="fileLists"
						list-type="picture-card"
						:auto-upload="false"
						accept=".zip,application/zip"
						:before-upload="handleBeforeUpload"
						:http-request="uploadFile"
						:on-remove="handleRemove"
						:on-change="handleChange">
						<el-icon><Plus /></el-icon>
						<template #file="{ file }">
							<div class="max-wh">
								<img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
								<span class="el-upload-list__item-actions">
									<span class="el-upload-list__item-delete" @click="handleRemove(file)">
										<el-icon><Delete /></el-icon>
									</span>
								</span>
							</div>
						</template>
					</el-upload>
				</div>
			</div>
			<div style="width: 50%">
				<h3 class="mb-12">空间信息</h3>
				<div class="pl-12">
					<FormScheme
						v-if="formData1"
						ref="formSchemeRef1"
						:formData="formData1"
						:rules="rules1"
						:formMode="form"></FormScheme>
				</div>
				<h3 class="mb-12" v-if="formData && addFormMode.serviceType?.length">服务信息</h3>
				<div class="pl-12">
					<FormScheme
						v-if="formData && addFormMode.serviceType?.length"
						ref="formSchemeRef"
						:formData="formData"
						:rules="rules"
						:formMode="addFormMode"></FormScheme>
				</div>
			</div>
		</PageContainer>
	</div>
</template>

<script setup>
	import FormScheme from '@/components/form/scheme.vue'
	import FilterContainer from '@/components/filterContainer/index.vue'
	import PageContainer from '@/components/pageContainer/index.vue'
	import { addFormData1 } from '@/views/service/components/serve/formData1.js'
	import { addFormData } from '@/views/service/components/serve/formData2.js'
	import { getTimeFormatted, deepCopy, setTreeProps } from '@/utils/index.js'
	import { setFormData } from '@/utils/formValidation.js'
	import { useAppStore } from '@/store/modules/app.js'
	const store = useAppStore()
	import { serviceApi } from '@/api/service/index.js'
	const api = serviceApi()
	import { systemApi } from '@/api/system/index.js'
	const sApi = systemApi()

	const formSchemeRef1 = ref(null)
	const uploadRef = ref(null)
	const formSchemeRef = ref(null)
	const fileLists = ref([])
	const form = ref({
		cjr: '',
		cjsj: getTimeFormatted('YYYY-MM-DD HH:mm:ss w'),
		workSpace: '',
		dataSetName: '',
		srs: 'EPSG:4326',
		serverPlatform: 'ZYGL_GEOSERVER',
		shpFile: null
	})
	const addFormMode = ref({
		serviceName: '',
		serviceUrl: '',
		servicePlatform: 'ZYGL_GEOSERVER',
		cjr: '',
		createtime: getTimeFormatted('YYYY-MM-DD HH:mm:ss'),
		unit: '',
		serviceType: [],
		description: '',
		extentMeta: {}
	})
	const formData1 = ref(null)
	const formData = ref(null)
	const configure = ref([])
	const cascaderData = ref([])
	const rules1 = reactive({
		workSpace: [{ required: true, message: '请输入工作区', trigger: 'blur' }],
		dataSetName: [{ required: true, message: '请输入数据存储', trigger: 'blur' }],
		srs: [{ required: true, message: '请输入投影', trigger: 'blur' }]
	})
	const rules = reactive({
		serviceName: [{ required: true, message: '请填写资源名称', trigger: 'blur' }]
	})

	watch(
		addFormMode,
		newValue => {
			if (newValue.serviceType.length > 0) {
				formData.value = JSON.parse(JSON.stringify(addFormData))
			} else {
				formData.value = JSON.parse(JSON.stringify(addFormData))
			}
		},
		{ deep: true }
	)
	const emit = defineEmits(['close'])

	onMounted(() => {
		getDict()
		formData1.value = JSON.parse(JSON.stringify(addFormData1))
		formData.value = JSON.parse(JSON.stringify(addFormData))
		getPageData()
		form.value.cjr = store.userInfo.username
		addFormMode.value.cjr = store.userInfo.username
	})

	const addShp = async () => {
		if (addFormMode.value.serviceType.length === 0) return ElMessage.error('请选择服务类型')
		if (fileLists.value.length === 0) return ElMessage.error('请选择文件')
		let from1 = false
		await formSchemeRef1.value.submitForm().then(res => {
			from1 = res
		})
		if (!from1) return ElMessage.error('请选输入空间相关信息')
		const loading = ElLoading.service({ lock: true, text: '发布中...', background: 'rgba(0, 0, 0, 0.7)' })
		formSchemeRef.value.submitForm().then(res => {
			if (res) {
				let deepMode = deepCopy(addFormMode.value)
				deepMode.serviceType = deepMode.serviceType[1]
				deepMode = setFormData(deepMode)
				let fd = new FormData()
				fd.append('serverPlatform', form.value.serverPlatform)
				fd.append('serviceType', deepMode.serviceType)
				fd.append('shpFile', fileLists.value[0].raw)
				fd.append('dataSetName', form.value.dataSetName)
				fd.append('srs', form.value.srs)
				fd.append('workSpace', form.value.workSpace)
				api
					.shpPublish(fd)
					.then(res2 => {
						if (res2.resultCode === 'S' && res2.data.length > 0) {
							deepMode.extentMeta.origin = `[${res2.data[0].minX}, ${res2.data[0].minY}]`
							deepMode.serviceUrl = res2.data[0].serviceUrl
							deepMode.extentMeta.layers = res2.data[0].layerName
							deepMode.extentMeta.layer = res2.data[0].layerName
							api
								.serviceAdd(deepMode)
								.then(saveRes => {
									ElMessage({ type: 'success', message: saveRes.resultMsg })
									loading.close()
									emit('close')
								})
								.catch(() => loading.close())
						}
					})
					.catch(() => loading.close())
			} else {
				loading.close()
				ElMessage.error('请选输入服务相关信息')
			}
		})
	}

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
						cascaderData.value = getTreeData(configure.value)[0].children.filter(
							item => item.zdz === 'GEOSERVER 平台'
						)
					}
				}
			})
			.catch(() => {})
	}
	const getTreeData = data => {
		if (!data?.length) return []
		filterChildren(data)
		return data
	}
	function filterChildren(data) {
		data.forEach(item => {
			item.value = item.zdbm ? item.zdbm : ''
			item.label = item.zdz ? item.zdz : ''
			if (item.children?.length > 0) filterChildren(item.children)
		})
	}
	const getDict = () => {
		sApi
			.dictDetailQuery({ zdlxdm: 'projectionConfig' })
			.then(res => {
				if (res.data?.length > 0) {
					formData1.value.fields[2].children[0].options = setTreeProps(res.data, 'zdbm', 'id', 'zdbm')
				}
			})
			.catch(err => ElMessage.error(err.message))
	}
	const handleBeforeUpload = () => {}
	const uploadFile = params => {
		if (params.file == null) {
			ElMessage({ message: '请选择需要上传的文件', type: 'warning' })
			return false
		}
	}
	const handleRemove = file => {
		fileLists.value = fileLists.value.filter(item => item.uid !== file.uid)
	}
	const handleChange = (file, fileList) => {
		fileLists.value = fileList
	}
	defineExpose({ form, addShp })
</script>

<style lang="scss" scoped></style>
