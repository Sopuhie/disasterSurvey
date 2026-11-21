<!-- 查询范围 -->
<template>
	<div class="scope" v-if="radioArr.length > 1">
		<el-radio-group v-model="radio" @change="radioChange">
			<el-radio :label="item.value" v-for="item in radioArr" :key="item.value">{{ item.label }}</el-radio>
		</el-radio-group>
		<p class="item" v-show="radio === 2">
			<el-select v-model="areaDefault" placeholder="请选择" style="width: 100%" @change="handleChange">
				<el-option :value="item.code" v-for="(item, idx) in regionInfo" :key="`regionInfo_${idx}`">{{
					item.name
				}}</el-option>
			</el-select>
		</p>
		<!-- 不要多余动作 -->
		<!-- <p class="item" v-show="radio === 3">
			<el-button v-btn icon="Plus" @click="importRangeHandler"> 导入范围文件 </el-button>
		</p>
		<p class="item flex fa" v-show="radio === 4">
			<span class="txt">
				选择绘制形状
				<small>(提示：按ESC退出绘制)</small>
				：
			</span>
			<span class="irregular" @click="scopeItemHandle('irregular')"></span>
		</p> -->
	</div>
</template>

<script setup>
	import { onMounted } from 'vue'
	import { DrawType } from '@/components/map/primitive/interaction/DrawTool'

	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	const radio = ref(1)
	const regionInfo = ref([])
	const areaDefault = ref(undefined)

	const props = defineProps({
		idx: {
			type: Number,
			default: 0
		},
		radioArr: {
			type: Array,
			default: () => {
				return [
					{ label: '数据范围', value: 1 },
					{ label: '行政区划', value: 2 },
					{ label: '导入', value: 3 },
					{ label: '绘制', value: 4 }
				]
			}
		},
		parentResource: {
			type: String,
			default: ''
		},
		// 是否显示绘制结果弹窗
		showDraw: {
			type: Boolean,
			default: true
		}
	})
	watch(
		() => appStore.switchObj.showUploadData,
		val => {
			if (!val) radio.value = 1
		}
	)

	onMounted(() => {
		if (props.radioArr.length === 1) {
			switch (radioArr[0]) {
				case '数据范围':
					radio.value = 1
					break
				case '行政区划':
					radio.value = 2
					break
				case '导入':
					radio.value = 3
					break
				case '绘制':
					radio.value = 4
					break
			}
		}
	})

	/**
	 * 输入范围选择
	 */
	const radioChange = value => {
		// 表示导入范围
		if (value == 3) {
			appStore.setSwitchObj('showUploadData', true)
		}
		// 表示绘制范围
		if (value == 4) {
			drawPolygon()
		}
	}

	const handleChange = () => {}

	/**
	 * 导入范围文件
	 */
	const importRangeHandler = () => {
		appStore.setSwitchObj('showUploadData', true)
	}

	/**
	 * 绘制形状
	 */
	const scopeItemHandle = () => {
		drawPolygon()
	}

	/**
	 * 绘制面
	 */
	function drawPolygon() {
		mapStore.activeMap.drawTool.start({
			drawType: DrawType.Polygon,
			drawingStyle: feature => {
				return {
					stroke: {
						color: 'green',
						width: 2
					},
					fill: {
						color: 'rgba(255, 0, 255, 0.3)'
					},
					image: {
						radius: 5,
						stroke: {
							color: 'rgba(0, 0, 0, 0.7)'
						},
						fill: {
							color: 'rgba(255, 255, 255, 0.2)'
						}
					}
				}
			}
		})
	}
</script>

<style lang="scss" scoped>
	.scope {
		& .item {
			height: 52px;
			padding-top: 20px;
			& .txt {
				font-size: 14px;
				line-height: 22px;
				font-weight: 400;
				& small {
					font-style: normal;
					font-size: 12px;
					color: #999999;
					line-height: 22px;
					font-weight: 400;
				}
			}
		}
		& .disc {
			display: flex;
			width: 24px;
			height: 24px;
			border: 2px solid #a3c7f3;
			border-radius: 50%;
			cursor: pointer;
			&.cur,
			&:hover {
				border: 2px solid #2b77e1;
			}
			margin-left: 16px;
		}
		& .square {
			display: flex;
			width: 24px;
			height: 24px;
			border: 2px solid #a3c7f3;
			cursor: pointer;
			&.cur,
			&:hover {
				border: 2px solid #2b77e1;
			}
			margin-left: 16px;
		}
		& .irregular {
			display: flex;
			width: 27px;
			height: 27px;
			cursor: pointer;
			background-image: url('@/assets/images/polygon.png');
			background-repeat: no-repeat;
			background-position: center center;
			background-size: 27px 27px;
		}
	}
</style>
