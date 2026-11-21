<template>
	<div class="editPanel max-h p-16 oy">
		<el-form :model="exportMapStore.formData" label-position="top" style="width: 100%">
			<el-form-item prop="name" class="input-auto">
				<span>模板命名</span>
				<el-input v-model="exportMapStore.formData.name" size="small"></el-input>
			</el-form-item>
			<el-form-item label="纸张设置" prop="paper">
				<el-form :model="exportMapStore.formData.paper" class="form-container50">
					<el-form-item prop="type">
						<el-radio-group v-model="exportMapStore.formData.paper.type" @change="changePaperType">
							<el-radio v-for="pItem in paperTypes" :label="pItem.value" :key="pItem.value">{{ pItem.label }}</el-radio>
						</el-radio-group>
					</el-form-item>
					<el-form-item class="form-item50" prop="top">
						<span>上边距</span>
						<el-input maxlength="3" class="input-w" size="small" v-model="exportMapStore.formData.paper.top"></el-input>
						<span>mm</span>
					</el-form-item>
					<el-form-item class="form-item50" prop="bottom">
						<span>下边距</span>
						<el-input
							maxlength="3"
							class="input-w"
							size="small"
							v-model="exportMapStore.formData.paper.bottom"></el-input>
						<span>mm</span>
					</el-form-item>
					<el-form-item class="form-item50" prop="left">
						<span>左边距</span>
						<el-input
							maxlength="3"
							class="input-w"
							size="small"
							v-model="exportMapStore.formData.paper.left"></el-input>
						<span>mm</span>
					</el-form-item>
					<el-form-item class="form-item50" prop="right">
						<span>右边距</span>
						<el-input
							maxlength="3"
							class="input-w"
							size="small"
							v-model="exportMapStore.formData.paper.right"></el-input>
						<span>mm</span>
					</el-form-item>
				</el-form>
			</el-form-item>
			<el-form-item label="地图" prop="map">
				<el-form :model="exportMapStore.formData.map" class="form-container50">
					<el-form-item class="form-item50" prop="height">
						<span>高度</span>
						<el-input
							maxlength="3"
							class="input-w"
							size="small"
							v-model="exportMapStore.formData.map.height"></el-input>
						<span>mm</span>
					</el-form-item>
					<el-form-item class="form-item50" prop="width">
						<span>宽度</span>
						<el-input maxlength="3" class="input-w" size="small" v-model="exportMapStore.formData.map.width"></el-input>
						<span>mm</span>
					</el-form-item>
				</el-form>
			</el-form-item>
			<el-form-item prop="map">
				<div slot="label" class="mr-18">
					缩略图
					<el-switch v-model="exportMapStore.formData.thumbnial.show" @change="handleHhumbnialChange" />
				</div>
				<el-form
					:model="exportMapStore.formData.thumbnial"
					class="form-container50"
					v-if="exportMapStore.formData.thumbnial.show">
					<el-form-item class="form-item50" prop="height">
						<span>高度</span>
						<el-input
							maxlength="3"
							class="input-w"
							size="small"
							v-model="exportMapStore.formData.thumbnial.height"></el-input>
						<span>mm</span>
					</el-form-item>
					<el-form-item class="form-item50" prop="width">
						<span>宽度</span>
						<el-input
							maxlength="3"
							class="input-w"
							size="small"
							v-model="exportMapStore.formData.thumbnial.width"></el-input>
						<span>mm</span>
					</el-form-item>
				</el-form>
			</el-form-item>
			<el-form-item label="文本" prop="text">
				<el-form-item prop="type">
					<el-checkbox-group v-model="exportMapStore.formData.text.modules" @change="changeCheckbox('text')">
						<el-checkbox v-for="item in textModules" :label="item" :key="item.id">{{ item.label }}</el-checkbox>
					</el-checkbox-group>
				</el-form-item>
				<template v-for="mItem in exportMapStore.formData.text.modules">
					<el-form-item :label="mItem.label" :prop="mItem.prop">
						<el-form class="form-container50" :model="exportMapStore.formData.text[mItem.prop]">
							<el-form-item class="form-item50" prop="height">
								<span>高度</span>
								<el-input
									maxlength="3"
									class="input-w"
									size="small"
									v-model="exportMapStore.formData.text[mItem.prop].height"></el-input>
								<span>mm</span>
							</el-form-item>
							<el-form-item class="form-item50" prop="width">
								<span>宽度</span>
								<el-input
									maxlength="3"
									class="input-w"
									size="small"
									v-model="exportMapStore.formData.text[mItem.prop].width"></el-input>
								<span>mm</span>
							</el-form-item>
							<el-form-item class="form-item50 input-auto" prop="color">
								<span>颜色</span>
								<el-color-picker
									size="small"
									v-model="exportMapStore.formData.text[mItem.prop].color"
									show-alpha></el-color-picker>
							</el-form-item>
							<el-form-item class="form-item50" prop="fontSize">
								<span>字号</span>
								<el-input-number
									class="input-w92"
									v-model="exportMapStore.formData.text[mItem.prop].fontSize"
									size="small"
									controls-position="right"
									:min="1"
									:max="50"></el-input-number>
								<span>pt</span>
							</el-form-item>
							<el-form-item style="flex: 1" label="背底透明度" prop="opacity">
								<el-slider
									v-model="exportMapStore.formData.text[mItem.prop].opacity"
									:format-tooltip="formatTooltip"></el-slider>
							</el-form-item>
						</el-form>
					</el-form-item>
				</template>
			</el-form-item>
			<el-form-item label="图素" prop="mapFactor">
				<el-form-item prop="type">
					<el-checkbox-group v-model="exportMapStore.formData.mapFactor.modules" @change="changeCheckbox('mapFactor')">
						<el-checkbox v-for="item in mapFactorModules" :label="item" :key="item.id">{{ item.label }}</el-checkbox>
					</el-checkbox-group>
				</el-form-item>
				<template v-for="mItem in exportMapStore.formData.mapFactor.modules">
					<el-form-item :label="mItem.label" :prop="mItem.prop" class="max-w">
						<el-form class="form-container50 max-w" :model="exportMapStore.formData.mapFactor[mItem.prop]">
							<template v-if="mItem.prop === 'legend'">
								<el-form-item prop="type" style="width: 100%">
									<el-radio-group v-model="exportMapStore.formData.mapFactor[mItem.prop].type">
										<el-radio v-for="item in legendTypes" :label="item.id" :key="item.id">{{ item.label }}</el-radio>
									</el-radio-group>
								</el-form-item>
								<el-form-item class="form-item50" prop="height">
									<span>高度</span>
									<el-input
										maxlength="3"
										class="input-w"
										size="small"
										v-model="exportMapStore.formData.mapFactor[mItem.prop].height"></el-input>
									<span>mm</span>
								</el-form-item>
								<el-form-item class="form-item50" prop="width">
									<span>宽度</span>
									<el-input
										maxlength="3"
										class="input-w"
										size="small"
										v-model="exportMapStore.formData.mapFactor[mItem.prop].width"></el-input>
									<span>mm</span>
								</el-form-item>
								<el-form-item prop="unit">
									<span style="margin-right: 8px">最大图例规模</span>
									<el-select
										v-model="exportMapStore.formData.mapFactor[mItem.prop].maxLength"
										size="small"
										placeholder="请选择">
										<el-option v-for="lItem in lengthList" :key="lItem.value" :label="lItem.label" :value="lItem.value">
										</el-option>
									</el-select>
								</el-form-item>
								<el-form-item class="max-w mt-8" label="背底透明度" prop="opacity">
									<el-slider
										v-model="exportMapStore.formData.mapFactor[mItem.prop].opacity"
										:format-tooltip="formatTooltip"></el-slider>
								</el-form-item>
							</template>
							<template v-else>
								<el-form-item class="form-item50" prop="height" v-if="exportMapStore.formData.mapFactor[mItem.prop]">
									<span>高度</span>
									<el-input
										maxlength="3"
										class="input-w"
										size="small"
										v-model="exportMapStore.formData.mapFactor[mItem.prop].height"></el-input>
									<span>mm</span>
								</el-form-item>
								<el-form-item class="form-item50" prop="width" v-if="exportMapStore.formData.mapFactor[mItem.prop]">
									<span>宽度</span>
									<el-input
										maxlength="3"
										class="input-w"
										size="small"
										v-model="exportMapStore.formData.mapFactor[mItem.prop].width"></el-input>
									<span>mm</span>
								</el-form-item>
								<el-form-item v-if="mItem.prop === 'compass'" style="flex: 1" label="背底透明度" prop="opacity">
									<el-slider
										v-model="exportMapStore.formData.mapFactor[mItem.prop].opacity"
										:format-tooltip="formatTooltip"></el-slider>
								</el-form-item>
								<el-form-item v-if="mItem.prop === 'plottingScale'" class="flex fa" prop="unit" style="width: 100%">
									<span style="margin-right: 8px">比例尺描述单位</span>
									<el-select
										v-model="exportMapStore.formData.mapFactor[mItem.prop].unit"
										style="width: 200px"
										size="small"
										placeholder="请选择">
										<el-option v-for="sItem in scaleUnits" :key="sItem.value" :label="sItem.label" :value="sItem.value">
										</el-option>
									</el-select>
								</el-form-item>
								<el-form-item class="form-item50 mb-18" prop="fontSize" v-if="mItem.prop == 'plottingScale'">
									<span>字号</span>
									<el-input-number
										class="input-w92"
										v-model="exportMapStore.formData.mapFactor[mItem.prop].fontSize"
										size="small"
										controls-position="right"
										:min="1"
										:max="50"></el-input-number>
									<span>pt</span>
								</el-form-item>
							</template>
						</el-form>
					</el-form-item>
				</template>
			</el-form-item>
		</el-form>
	</div>
</template>
<script setup>
	import { initMapData, paperTypeSize, initPaper } from '@/views/exportMap/modules/constant.js'
	import {
		textModules,
		mapFactorModules,
		paperTypes,
		scaleUnits,
		lengthList,
		legendTypes,
		mmToPx,
		ptToPx
	} from '@/views/exportMap/tool.js'
	import { useExportMapStore } from '@/store/modules/exportMap.js'
	const exportMapStore = useExportMapStore()

	const mapModules = ref([])

	onMounted(() => {
		if (exportMapStore.saveType === 'edit') {
			changePaperType(exportMapStore.formData.paper.type, 'edit')
		}
	})

	const changePaperType = (val, type) => {
		if (!type) {
			forFormData(val)
		}

		const [width, height] = forPaperSize(val)
		changePaperStyle({ width, height })
	}
	const handleHhumbnialChange = val => {
		exportMapStore.setFormData({ type: 'other', key: 'thumbnial.show', value: val })
		changePaperLayout()
	}

	// 打印纸布局变化
	const changePaperLayout = () => {
		mapModules.value = []
		nextTick(() => {
			mapModules.value = forPaperLayout(exportMapStore.formData)
		})
	}

	const forPaperLayout = fData => {
		const { map, text, mapFactor, thumbnial } = fData
		const initFontSize = ptToPx(9)
		let list = []
		const textList = text.modules.map(item => {
			const { prop, label } = item
			return {
				...text[prop],
				label,
				prop: 'text.' + prop
			}
		})
		const mapFactorList = mapFactor.modules.map(item => {
			const { prop, label } = item
			return {
				...mapFactor[prop],
				label,
				prop: 'mapFactor.' + prop
			}
		})
		list.push({
			...map,
			prop: 'map',
			label: '地图'
		})
		list.push({
			...thumbnial,
			prop: 'thumbnial',
			label: '缩略图'
		})
		list = list.concat(textList, mapFactorList)
		return list.map(item => {
			const { width, height, x: left, y: top, fontSize, ...rest } = item
			const [w, h, x, y] = mmToPx(width, height, left, top)
			const f = fontSize ? ptToPx(fontSize) : initFontSize
			return { ...rest, w, h, x, y, f }
		})
	}

	// 纸张格式改变，数据跟着变
	const forFormData = key => {
		const initFormDataItem = initMapData[key]
		const newFormData = {
			name: '',
			paper: {
				...initPaper,
				type: key
			},
			map: {
				...initFormDataItem.map
			},
			thumbnial: {
				...initFormDataItem.thumbnial
			},
			text: {
				modules: textModules,
				title: {
					...initFormDataItem.text.title,
					opacity: 100
				},
				producer: {
					...initFormDataItem.text.producer,
					opacity: 100
				},
				time: {
					...initFormDataItem.text.time,
					opacity: 100
				},
				explain: {
					...initFormDataItem.text.explain,
					opacity: 100
				},
				explain2: {
					...initFormDataItem.text.explain2,
					opacity: 100
				},
				explain3: {
					...initFormDataItem.text.explain3,
					opacity: 100
				}
			},
			mapFactor: {
				modules: mapFactorModules,
				compass: {
					...initFormDataItem.mapFactor.compass,
					// value: compassImage,
					value: '',
					opacity: 100
				},
				plottingScale: {
					...initFormDataItem.mapFactor.plottingScale
				},
				legend: {
					...initFormDataItem.legend,
					type: 1,
					opacity: 100
				}
			}
		}
		if (exportMapStore.saveType === 'edit') {
			newFormData.name = exportMapStore.formData.name
			newFormData.id = exportMapStore.formData.id
		}
		exportMapStore.setFormData({ type: 'all', data: newFormData })
	}

	const forPaperSize = val => {
		const { w, h } = paperTypeSize[val]
		return mmToPx(w, h)
	}

	// 改变纸的样式
	let paperStyleRetry = 0
	const changePaperStyle = paperStyle => {
		nextTick(() => {
			const panel = document.getElementsByClassName('mapPanel')[0]
			const clientHeight = panel?.clientHeight || 0
			const clientWidth = panel?.clientWidth || 0
			const { width, height } = paperStyle
			if (!clientWidth || !clientHeight || !width || !height) {
				if (paperStyleRetry < 20) {
					paperStyleRetry += 1
					setTimeout(() => changePaperStyle(paperStyle), 80)
				}
				return
			}
			paperStyleRetry = 0
			const pad = 24
			const scaleVal = Math.min((clientWidth - pad) / width, (clientHeight - pad) / height, 1)
			exportMapStore.setPaperProps({
				style: {
					width: width + 'px',
					height: height + 'px',
					transform: `translate(-50%, -50%) scale(${scaleVal})`,
					transformOrigin: 'center center'
				},
				scale: scaleVal
			})
		})
	}
	const formatTooltip = val => {
		return val + '%'
	}
</script>
<style lang="scss" scoped>
	.editPanel {
		width: 25%;
	}

	.input-w {
		width: 72px;
		margin: 0 8px;
	}
	.input-w92 {
		width: 92px;
		margin: 0 8px;
	}
	.input-auto {
		:deep() {
			.el-form-item__content {
				display: flex;
				align-items: center;
				& > span {
					flex-shrink: 0;
					margin-right: 8px;
				}
			}
		}
	}
	.form-container50 {
		display: flex;
		flex-flow: wrap;
		gap: 8px 0;
		.form-item50 {
			flex: 1 1 50%;
			margin-bottom: 8px;
		}
		:deep() {
			.el-radio {
				margin-right: 12px;
				.el-radio__label {
					padding-left: 4px;
				}
			}
		}
	}
	.form-content50 {
		:deep() {
			& > .el-form-item__content {
				display: flex;
				flex-flow: wrap;
				.form-item50 {
					flex: 1 1 50%;
				}
			}
		}
	}
</style>
