<template>
	<ZDialogCom
		title="投影转换工具"
		toPath="cancel"
		showSwitch
		:footer="false"
		:wrapStyle="wrapStyle"
		:bodyStyle="{ padding: '16px' }"
		@cancel="cancel">
		<div class="max-wh">
			<el-form ref="ruleFormRef" :rules="rules" :model="made" label-width="auto">
				<el-form-item prop="data" label="输入数据：">
					<el-col :span="18">
						<el-input v-model="made.data" />
					</el-col>
					<el-col :span="1"></el-col>
					<el-col :span="5">
						<el-button v-btn type="primary" @click="uploadHandle">
							<el-icon><More /></el-icon>
						</el-button>
						<input
							type="file"
							ref="fileInput"
							accept=".zip,application/zip"
							style="display: none"
							@change="handleFileChange" />
					</el-col>
				</el-form-item>
				<!-- <el-form-item prop="projector" label="输入数据投影：">
					<el-input v-model="made.projector" />
				</el-form-item> -->
				<el-form-item prop="coordinates" label="投影转换坐标系：">
					<el-select v-model="made.coordinates">
						<el-option v-for="(item, cIdx) in projectInfo" :key="cIdx" :label="item.name" :value="item" />
					</el-select>
				</el-form-item>
				<el-form-item>
					<div class="max-w flex jend">
						<el-button v-btn type="primary" plain @click="submit(ruleFormRef)"> 执行转换 </el-button>
					</div>
				</el-form-item>
			</el-form>
		</div>
	</ZDialogCom>
</template>

<script setup>
	import ZDialogCom from '@/components/dialog/index.vue'
	import { getWindowSize, getFileName } from '@/utils/index.js'
	import shp from 'shpjs'
	import proj4 from 'proj4'

	const wrapStyle = reactive({
		width: '490px',
		height: `${getWindowSize().h.value - 193}px`,
		top: '95px',
		left: `${getWindowSize().w.value - 510}px`
	})
	const ruleFormRef = ref(null)
	const fileInput = ref(null)
	const made = ref({
		data: '',
		// projector: 'GCS_China_Geodetic_Coordinate_System_2000',
		coordinates: 'GCS_China_Geodetic_Coordinate_System_2000'
	})
	const rules = {
		data: [{ required: true, message: '请输入数据', trigger: ['blur', 'change'] }],
		projector: [{ required: true, message: '请输入输入数据投影', trigger: 'blur' }],
		coordinates: [{ required: true, message: '请选择投影转换坐标系', trigger: 'change' }]
	}

	const projectInfo = ref([
		{
			name: 'WGS84',
			key: 'EPSG:4326',
			proj: '+proj=longlat +datum=WGS84 +no_defs +type=crs'
		},
		{
			name: 'CGCS2000',
			key: 'EPSG:4490',
			proj: '+proj=longlat +ellps=GRS80 +no_defs +type=crs'
		},
		{
			name: 'WGS84投影坐标',
			key: 'EPSG:3857',
			proj: '+proj=merc +a=6378137 +b=6378137 +lat_ts=0 +lon_0=0 +x_0=0 +y_0=0 +k=1 +units=m +nadgrids=@null +wktext +no_defs +type=crs'
		}
	])

	onMounted(() => {
		window.onresize = () => {
			return (() => {
				wrapStyle.height = `${getWindowSize().h.value - 80}px`
				wrapStyle.left = `${getWindowSize().w.value - 360}px`
			})()
		}
	})

	onUnmounted(() => {
		window.onresize = null
	})

	const emits = defineEmits(['cancel'])
	function cancel() {
		emits('cancel')
	}

	// 计算
	const submit = async formEl => {
		if (!formEl) return
		await formEl.validate((valid, fields) => {
			if (valid) {
				let sourceProj = new proj4.Proj(projectInfo.value[0].proj)
				let targetProj = new proj4.Proj(made.value.coordinates.proj)
				let res = null
				if (made.value.data.type == 'FeatureCollection') {
					res = made.value.data.features.map(m => {
						let geo = convertGeoJSON(m.geometry, sourceProj, targetProj)
						m.geometry = geo;
						return m
					})
				} else {
					res = convertGeoJSON(made.value.data, sourceProj, targetProj)
				}
				if (res) {
					try {
							// 将JSON对象转换为字符串，缩进2个空格便于阅读
							const jsonString = JSON.stringify(res, null, 2);

							// 创建Blob对象，指定MIME类型为application/json
							const blob = new Blob([jsonString], { type: 'application/json' });

							// 创建下载链接
							const url = URL.createObjectURL(blob);
							const a = document.createElement('a');
							a.href = url;
							a.download = `${'转换文件' || 'data'}.json`; // 确保文件名以.json结尾

							// 模拟点击下载
							document.body.appendChild(a);
							a.click();

							// 清理资源
							setTimeout(() => {
									document.body.removeChild(a);
									URL.revokeObjectURL(url);
							}, 0);

							return true;
					} catch (error) {
							console.error('下载JSON失败:', error);
							return false;
					}
				}
			} else {
				console.log('error submit!', fields)
			}
		})
	}

	// 转换GeoJSON坐标的递归函数
	function convertGeoJSON(geojson, sourceProj, targetProj) {
		if (!geojson) return geojson

		// 深度复制对象
		const result = JSON.parse(JSON.stringify(geojson))

		// 根据GeoJSON类型处理坐标
		if (result.type === 'Point') {
			const converted = proj4.transform(sourceProj, targetProj, result.coordinates)
			result.coordinates = [converted.x, converted.y]
		} else if (result.type === 'LineString' || result.type === 'MultiPoint') {
			result.coordinates = result.coordinates.map(coord => {
				const converted = proj4.transform(sourceProj, targetProj, coord)
				return [converted.x, converted.y]
			})
		} else if (result.type === 'Polygon' || result.type === 'MultiLineString') {
			result.coordinates = result.coordinates.map(ring =>
				ring.map(coord => {
					const converted = proj4.transform(sourceProj, targetProj, coord)
					return [converted.x, converted.y]
				})
			)
		} else if (result.type === 'MultiPolygon') {
			result.coordinates = result.coordinates.map(polygon =>
				polygon.map(ring =>
					ring.map(coord => {
						const converted = proj4.transform(sourceProj, targetProj, coord)
						return [converted.x, converted.y]
					})
				)
			)
		} else if (result.type === 'Feature') {
			result.geometry = convertGeoJSON(result.geometry, sourceProj, targetProj)
		} else if (result.type === 'FeatureCollection') {
			result.features = result.features.map(feature => convertGeoJSON(feature, sourceProj, targetProj))
		}

		return result
	}

	const uploadHandle = () => {
		fileInput.value?.click()
	}

	async function handleFileChange(event) {
		const file = event.target.files[0]
		const validTypes = ['application/zip', 'application/x-zip-compressed']
		if (file && validTypes.includes(file.type)) {
			const fileSize = 100
			if (file.size / 1024 / 1024 > fileSize) {
				ElMessage.error(`文件大小不能超过 ${fileSize}MB!`)
				return false
			}
			// // 获取完整文件名
			// const fullFileName = file.name
			// // 获取文件后缀
			// const fileExtension = fullFileName.split('.').pop().toLowerCase()
			// // 获取文件名（不包含后缀）
			// const fileNameWithoutExtension = getFileName(fullFileName)
			// made.value.data = fullFileName

			// loadShapefileFromPublic(fullFileName)
			// console.log('完整文件名:', fullFileName)
			// console.log('文件后缀:', fileExtension)
			// console.log('文件名(无后缀):', fileNameWithoutExtension)
			file.arrayBuffer().then(file => {
				shp(file).then(geojson => {
					made.value.data = geojson
				})
			})
		} else {
			ElMessage.error('请选择 .zip 类型的文件')
		}
	}
</script>

<style lang="scss" scoped></style>
