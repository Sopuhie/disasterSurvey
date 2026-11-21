<!-- 添加文件资源 -->
<template>
	<div class="add max-wh">
		<FilterContainer class="pt-4 pb-4">
			<template #left>
				<p class="flex fa">
					<span>资源类型：</span>
					<el-select v-model="form.type" placeholder="请选择资源类型" clearable style="width: 200px">
						<el-option v-for="item in typeOptions" :key="item.zdbm" :label="item.zdz" :value="item.zdbm" />
					</el-select>
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
						multiple
						:disabled="type === 'edit'"
						:before-upload="handleBeforeUpload"
						:http-request="uploadFile"
						:on-remove="handleRemove"
						:on-change="handleChange">
						<el-icon><Plus /></el-icon>
						<template #file="{ file }">
							<div class="maxwh">
								<img class="el-upload-list__item-thumbnail" :src="file.url" alt="" />
								<span class="el-upload-list__item-actions">
									<span class="el-upload-list__item-delete" @click="handleRemove(file)">
										<el-icon><Delete /></el-icon>
									</span>
								</span>
							</div>
						</template>
					</el-upload>
					<p class="pt-10 pb-10">
						<el-button type="primary" :disabled="type === 'edit'" @click="submitUpload">上传文件</el-button>
					</p>
					<ul v-if="form.details.length > 0">
						<li class="flex fa jsb" v-for="(item, index) in form.details" :key="index">
							<span>{{ item.fileName }}</span>
							<el-icon style="color: var(--el-color-success)"><CircleCheck /></el-icon>
						</li>
					</ul>
				</div>
			</div>
			<div style="width: 50%">
				<h3 class="mb-12">填写元数据</h3>
				<div class="pl-12">
					<el-form ref="dataForm" :model="form" label-width="100px" class="orginal_dataForm">
						<el-row>
							<el-col :span="24">
								<el-form-item label="资源名称">
									<el-input v-model="form.name" placeholder="请输入资源名称" />
								</el-form-item>
								<el-form-item label="文件等级">
									<el-select v-model="form.level" placeholder="请选择文件等级" clearable>
										<el-option v-for="item in levelOptions" :key="item.zdbm" :label="item.zdz" :value="item.zdbm" />
									</el-select>
								</el-form-item>
								<el-form-item label="状态">
									<el-select v-model="form.status" placeholder="请选择状态" clearable>
										<el-option
											v-for="item in statusOptions"
											:key="item.value"
											:label="item.label"
											:value="item.value" />
									</el-select>
								</el-form-item>
								<el-form-item label="资源集描述">
									<el-input v-model="form.description" type="textarea" resize="none" />
								</el-form-item>
								<el-form-item label="注册人">
									<el-input v-model="form.cjr" readonly />
								</el-form-item>
								<el-form-item label="注册时间">
									<el-input v-model="form.cjsj" readonly />
								</el-form-item>
							</el-col>
						</el-row>
					</el-form>
				</div>
			</div>
		</PageContainer>
	</div>
</template>

<script setup>
	import FilterContainer from '@/components/filterContainer/index.vue'
	import PageContainer from '@/components/pageContainer/index.vue'
	import { getTimeFormatted } from '@/utils/index.js'
	import { getMd5Hash } from '@/utils/getChunkList.js'

	import { useAppStore } from '@/store/modules/app.js'
	const store = useAppStore()

	import { serviceApi } from '@/api/service/index.js'
	const api = serviceApi()

	import { systemApi } from '@/api/system/index.js'
	const sApi = systemApi()

	const statusOptions = [
		{ label: '开放', value: '0' },
		{ label: '不开放', value: '1' }
	]
	const levelOptions = ref([])
	const typeOptions = ref([])
	const uploadRef = ref(null)
	const fileLists = ref([])
	const progress = ref(0)
	const chunkSize = ref(5 * 1024 * 1024)
	const form = ref({
		cjr: '',
		cjsj: '',
		description: '',
		details: [],
		level: '',
		name: '',
		status: '',
		type: '1',
		zhgxr: '',
		zhgxsj: ''
	})

	const props = defineProps({
		itemData: {
			type: Object,
			default: () => {
				return null
			}
		},
		type: {
			type: String,
			default: 'add'
		}
	})

	onMounted(() => {
		// 获取文件等级字典
		getDict()
		getDict2()
		form.value.cjr = store.userInfo.username || 'system'
		form.value.cjsj = getTimeFormatted('YYYY-MM-DD HH:mm:ss w')
		if (props.itemData) {
			Object.keys(form.value).forEach(key => {
				form.value[key] = props.itemData[key] == undefined ? form.value[key] : props.itemData[key]
			})
		}
	})

	// 获取字典--文件等级
	const getDict = () => {
		sApi
			.dictTypeQuery({ like_zdlxbm: 'zygl_filelevel' })
			.then(res => {
				if (res.data?.length > 0) {
					const data = res.data[0]
					sApi
						.dictDetailQuery({ flid: data.id })
						.then(res => {
							if (res.data?.length > 0) {
								levelOptions.value = res.data
							}
						})
						.catch(err => {})
				}
			})
			.catch(err => {})
	}
	// 获取字典--文件资源类型
	const getDict2 = () => {
		sApi
			.dictTypeQuery({ like_zdlxbm: 'zygl_wjzylx' })
			.then(res => {
				if (res.data?.length > 0) {
					const data = res.data[0]
					sApi
						.dictDetailQuery({ flid: data.id })
						.then(res => {
							if (res.data?.length > 0) {
								typeOptions.value = res.data
							}
						})
						.catch(err => {
							ElMessage.error(err.message)
						})
				}
			})
			.catch(err => {
				ElMessage.error(err.message)
			})
	}

	// 文件上传
	const submitUpload = () => {
		if (fileLists.value.length == 0) {
			ElMessage.error('请选择文件')
			return
		} else {
			const chunkSize = 1024 * 1024 * 5
			// 是否有大于10MB的文件
			let bigFileIds = []
			for (var i = 0; i < fileLists.value.length; i++) {
				if (fileLists.value[i].raw && fileLists.value[i].size > chunkSize) {
					bigFileIds.push(fileLists.value[i].name)
				}
				if (fileLists.value[i].name.length > 256) {
					ElMessage.error(`文件 ${fileLists.value[i].name} 文件名字超过256位，请先重命名。`)
					return
				}
			}
			// 部分是大文件
			if (bigFileIds.length > 0 && bigFileIds.length < fileLists.value.length) {
				ElMessage.error(bigFileIds.join(',') + '文件大于10MB，请单独上传。')
				uploadRef.value?.clearFiles()
				return
			}
			// 大文件
			else if (bigFileIds.length === fileLists.value.length) {
				// 文件分片上传
				shardUpload()
			}
			// 全是小文件
			else {
				filesUpload()
			}
		}
	}

	// 多文件上传
	const filesUpload = () => {
		const loading = ElLoading.service({
			lock: true,
			text: '文件上传中...',
			background: 'rgba(0, 0, 0, 0.7)'
		})
		let formData = new FormData()
		for (var i = 0; i < fileLists.value.length; i++) {
			if (fileLists.value[i].raw) {
				formData.append('files', fileLists.value[i].raw)
			}
		}

		api
			.fileMultipleUpload(formData)
			.then(res => {
				if (res?.data) {
					const data = res.data
					if (data?.successFiles?.length) {
						const sucFiles = data.successFiles
						sucFiles.forEach(item => {
							form.value.details.push({ ...item })
						})
					}
					uploadRef.value?.clearFiles()
					ElMessage({
						message: '上传成功！',
						type: 'success'
					})
				} else {
					ElMessage({
						message: '上传失败！',
						type: 'error'
					})
				}
				loading.close()
			})
			.catch(err => {
				loading.close()
			})
	}

	// 大文件分片上传
	const shardUpload = async () => {
		const loading = ElLoading.service({
			lock: true,
			text: '文件上传中...',
			background: 'rgba(0, 0, 0, 0.7)'
		})
		const targetPath = '/ht/serve'
		for (let i = 0; i < fileLists.value.length; i += 1) {
			const file = fileLists.value[i]
			const fileChunkList = createFileChunk(file)
			const fileHash = await getMd5Hash(fileChunkList)

			const { shouldUpload, uploadChunkList } = await verifyUpload(fileHash.hash)
			if (!shouldUpload) {
				ElMessage({ message: `秒传 ${file.name} 成功!`, type: 'success' })
				return
			}
			const uploadDataList = fileChunkList.map(({ file }, index) => ({
				index,
				hash: fileHash.hash + '-' + index,
				chunk: file,
				size: file.size,
				percentage: uploadChunkList.includes(index) ? 100 : 0
			}))
			await uploadChunks(fileHash.hash, file, targetPath, uploadChunkList, uploadDataList)
		}
		// 生成文件切片
		function createFileChunk(file) {
			const size = chunkSize.value
			const fileChunkList = []
			let start = 0
			while (start < file.size) {
				fileChunkList.push({ file: file.raw.slice(start, start + size) })
				start += size
			}
			return fileChunkList
		}

		// 生成文件 hash（web-worker）
		function calculateHash(fileChunkList) {
			return new Promise(resolve => {
				worker.postMessage(fileChunkList)
				worker.addEventListener('message', ev => {
					const { percentage, hash } = ev.data
					// 切片md5转换进度
					this.hashPercentage = percentage
					if (hash) {
						resolve(hash)
						worker.terminate()
					}
				})
			})
		}

		async function verifyUpload(fileMd5) {
			const res = await api.fileChunkCheck({ fileMd5 })
			let resolveData = {}
			if (res) {
				const { data, resultCode, resultMsg } = res
				if (resultCode === 'S') {
					const uploadChunkList = data ? data.map(item => item.chunkNumber) : []
					resolveData = {
						shouldUpload: true,
						uploadChunkList
					}
				} else {
					loading.close()
					ElMessage.error(resultMsg)
					resolveData = {
						shouldUpload: false
					}
				}
			} else {
				// 校验出错
				resolveData = {
					shouldUpload: false
				}
				loading.close()
			}
			return resolveData
		}

		// 上传切片，同时过滤已上传的切片
		async function uploadChunks(fileHash, file, targetPath, uploadChunkList = [], uploadDataList = []) {
			const requestList = uploadDataList.reduce((cur, pre) => {
				const { chunk, hash, index } = pre
				if (!uploadChunkList.includes(index)) {
					const uploadPromise = chunkUpload({
						...pre,
						fileHash,
						chunkSize: chunkSize.value,
						fileName: file.name,
						totalSize: file.size,
						totalChunks: uploadDataList.length
					})
					cur.push(uploadPromise)
				}
				return cur
			}, [])
			requestList.forEach((request, index, list) => {
				request.then(item => {
					const _progress = progress.value + 100 / list.length // 更新后的进度
					// 进度条到100%(所有分片上传完成)后,需要等一下还有一个合并和一个更新接口,此时将进度置为99%
					if (_progress >= 100) {
						progress.value = 99
					} else {
						progress.value = _progress
					}
					progress.value = +progress.value.toFixed(2)
				})
			})
			await Promise.all(requestList)
			// 之前上传的切片数量 + 本次上传的切片数量 = 所有切片数量时
			if (uploadChunkList.length + requestList.length === uploadDataList.length) {
				// 调用合并接口，通知服务端合并切片
				const mergeData = await api.fileChunkMerge({
					fileMd5: fileHash,
					targetPath,
					totalChunks: uploadDataList.length
				})
				loading.close()
				if (mergeData?.resultCode === 'S') {
					const res = mergeData.data
					form.value.details.push({
						fileName: res.fileName,
						filePath: res.filePath,
						fileSize: res.fileSize,
						fileType: res.fileType
					})

					ElMessage({
						message: '上传成功！',
						type: 'success'
					})
				}
			} else {
				// xx文件上传出错，重新上传
				ElMessage.error(file.name + '文件上传出错，请重新上传！')
			}
		}

		function chunkUpload(state) {
			const { size, index, chunk, fileHash, fileName, chunkSize, totalChunks, totalSize } = state
			const formData = new FormData()
			formData.append('chunkSize', chunkSize)
			formData.append('upfile', chunk)
			formData.append('chunkNumber', index)
			formData.append('fileMd5', fileHash)
			formData.append('filename', fileName)
			formData.append('totalChunks', totalChunks)
			formData.append('totalSize', totalSize)
			formData.append('currentChunkSize', size)
			return api.fileChunkUpload(formData)
		}
	}

	const handleBeforeUpload = file => {
		// 获取上传文件大小
		const fileSize = Number(file.size / 1024 / 1024)
		// if (fileSize > 100) {
		// 	ElMessage({ message: '文件大小不能超过100MB，请重新上传。', type: 'warning' })
		// 	return false
		// }
	}

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

	defineExpose({
		form
	})
</script>

<style lang="scss" scoped></style>
