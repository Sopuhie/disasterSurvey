<template>
	<div>
		<el-dialog
			title="提示"
			v-model="props.isShow"
			:modal="false"
			width="36%"
			:close-on-click-modal="false"
			align-center
			draggable
			:before-close="props.close">
			<!-- 不显示坐标系，直接读取数据里的数据，然后转换成当前系统坐标 -->
			<!-- <el-select v-model="selectedProjection" clearable placeholder="请选择">
				<el-option v-for="item in projections" :key="item.value" :label="item.label" :value="item.value"> </el-option>
			</el-select> -->
			<!-- <el-upload
				class="upload-demo"
				:action="uploadUrl"
				method="post"
				:before-upload="beforeUploadHandle"
				:on-success="uploadSuccessHandle"
				:on-error="uploadErrorHandle"
				:limit="1"
				:file-list="fileList"> -->
			<el-upload
				action="#"
				:http-request="httpRequest"
				:before-upload="beforeUploadHandle"
				:on-error="uploadErrorHandle"
				:multiple="true"
				:limit="10">
				<el-button v-btn class="mt-10" type="primary" plain>点击上传</el-button>
				<template #tip>
					<div class="el-upload__tip">
						{{ `支持 TXT、SHP 和 DWG 格式数据的导入，请上传 20 M以下的文件。` }} <br />
						{{ `使用 CGCS2000_3_Degree_GK_Zone_38 平面坐标（WKID：4526）` }} <br />
					</div>
					<div class="el-upload__tip">为了更好的服务体验，上传数据不要超过100条。</div>
				</template>
			</el-upload>
			<template #footer>
				<span class="dialog-footer">
					<el-button v-btn type="primary" plain @click="props.close">关 闭</el-button>
				</span>
			</template>
		</el-dialog>
	</div>
</template>

<script setup>
	import axios from 'axios'
	import EsrijsonUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
	import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
	import { transfromGeojson } from '@/components/map/primitive/util/ProjectionUtil.js'

	import { systemApi } from '@/api/system/index.js'
	const api = systemApi()

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	import { useAppStore } from '@/store/modules/app.js'
	import { onMounted } from 'vue'
	import { onBeforeMount } from 'vue'
	const appStore = useAppStore()

	const uploadUrl = ref('')
	const selectedProjection = ref(null)

	/**
	 * 自定义属性
	 */
	const props = defineProps({
		isShow: {
			type: Boolean,
			default: () => {
				return false
			}
		},
		close: {
			type: Function,
			default: () => {}
		}
	})

	onMounted(() => {
		if (!mapStore.getParseConfig) {
			api
				.queryDictionary({
					zdlxdm: 'parseConfig'
				})
				.then(res => {
					if (res?.data?.length) {
						let datas = res.data.map(m => {
							return {
								type: m.zdbm,
								url: m.zdz
							}
						})
						mapStore.setParseConfig(datas)
					}
				})
				.catch(err => ElMessage.error(err))
		}
	})
	onBeforeMount(() => {
		// selectedProjection.value = mapStore.activeMap.projection
		props.close()
	})
	/**
	 * 投影坐标集合
	 */
	const projections = computed(() => {
		let projs = []
		for (let [lebel, value] of Object.entries(mapStore.getProjections)) {
			projs.push({ lebel, value: lebel })
		}
		return projs
	})

	/**
	 * 上传之前判断，调用哪个接口
	 */
	function beforeUploadHandle(file) {
		if (file.name.toLowerCase().includes('.zip')) {
			uploadUrl.value = mapStore.getParseConfig.find(item => item.type.toUpperCase() == 'SHP').url
		}
		if (file.name.toLowerCase().includes('.dwg')) {
			uploadUrl.value = mapStore.getParseConfig.find(item => item.type.toUpperCase() == 'CAD').url
		}
		if (file.name.toLowerCase().includes('.txt')) {
			uploadUrl.value = mapStore.getParseConfig.find(item => item.type.toUpperCase() == 'TXT').url
		} else {
			uploadUrl.value = mapStore.getParseConfig.find(item => item.type.toUpperCase() == 'SHP').url
		}
	}
	/**
	 * 自定义上传
	 * @param {*} data
	 */
	function httpRequest(data) {
		var formData = new FormData()
		formData.append('spaceDataFile', data.file)
		axios
			.post(uploadUrl.value, formData, {
				headers: {
					'Content-Type': 'multipart/form-data',
					'Authentication-Token': appStore.token,
					token: appStore.token
				}
			})
			.then(res => {
				if (res.data.resultCode == 'S') {
					let parseFeatures = []
					if (res.data.data?.length) {
						res.data.data.forEach(n => {
							if (n.cadData?.features?.length) {
								let parseRes = []
								let parFeats = JSON.parse(n.cadData?.features)
								if (parFeats?.length) {
									parFeats.forEach(m => {
										if (m && m != null && m != 'null' && m instanceof Array) {
											m.forEach(f => {
												// 只需要面数据，线数据过滤
												if (f.geometry?.type?.toLowerCase().includes('polygon')) {
													parseRes.push(f)
												}
											})
										}
									})
								}
								parseFeatures = parseFeatures.concat(parseRes)
							}
							if (n?.shpData?.features?.length) {
								n.shpData.wkid = n.shpData.wkid && n.shpData.wkid != 0 ? n.shpData.wkid : mapStore.activeMap.projection
								let res = n.shpData.features.map(k => {
									return transfromGeojson(k, 'EPSG:' + n.shpData.wkid, mapStore.activeMap.projection)
								})
								parseFeatures = parseFeatures.concat(res)
							}
							if (n?.txtData?.features?.length) {
								let res = n.txtData.features.map(k => {
									return transfromGeojson(
										Object.assign({ type: 'Feature' }, JSON.parse(k)),
										'EPSG:' + n.txtData.spatialReference.wkid,
										mapStore.activeMap.projection
									)
								})
								parseFeatures = parseFeatures.concat(res)
							}
						})
					}
					if (parseFeatures?.length) {
						let datas = parseFeatures.map((feat, index) => {
							return {
								source: data.file.name,
								geometry: feat,
								name: '图斑' + (mapStore.getImportDatas.length + 1),
								color: '#ff0000',
								isVisible: true,
								check: true,
								geometryType: {
									Point: '点',
									LineString: '线',
									Polygon: '面'
								}[feat.geometry.type]
							}
						})
						mapStore.setImportDatas(datas)
						appStore.setSwitchObj('importData', true)
						appStore.setSwitchObj('showUploadData', false)
					}
					ElMessage({
						message: res.data.resultMsg,
						type: 'success'
					})
				}
				if (res.resultCode == 'F') {
					ElMessage({
						message: res.resultMsg,
						type: 'error'
					})
				}
			})
			.catch(err => {
				console.log(err)
			})
	}

	/**
	 * 上传成功返回结果
	 */
	function uploadSuccessHandle(response, file) {
		let olFeatures = EsrijsonUtil.readFeatures(response, {
			dataProjection: unref(selectedProjection)
				? mapStore.getProjections[unref(selectedProjection)]
				: mapStore.activeMap.projection,
			featureProjection: mapStore.activeMap.projection
		})
		if (olFeatures?.length) {
			olFeatures.forEach(feat => {
				let geojson = GeoJSONUtil.writeFeature(feat)
				mapStore.activeMap.flyToManager.flyToRegion(geojson)
			})
			let datas = olFeatures.map((feat, index) => {
				let geojson = GeoJSONUtil.writeFeature(feat)
				return {
					color: '#ff0000',
					source: file.name,
					geometry: geojson,
					name: '图斑' + (mapStore.getImportDatas.length + 1),
					isVisible: true,
					check: true,
					geometryType: {
						Point: '点',
						LineString: '线',
						Polygon: '面'
					}[geojson.geometry.type]
				}
			})
			mapStore.setImportDatas(datas)
		}

		appStore.setSwitchObj('importData', true)
	}

	/**
	 * 上传失败
	 */
	function uploadErrorHandle(err, file) {
		ElMessage({
			showClose: true,
			message: `解析失败，请检查数据`,
			type: 'warning'
		})
	}
</script>
