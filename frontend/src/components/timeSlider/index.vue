<!-- 时间滑块 -->
<template>
	<div class="time-slider noSelect flex fa" :class="{ isCollapse: appStore.switchObj.sidebarFlag }" :style="sLeft">
		<div class="current-tag" @mousedown="startTagDrag">{{ currentYear }}-{{ currentMonth }}-1</div>
		<div class="slider-container">
			<div class="flex fa">
				<div class="year-select">
					<el-select v-model="currentYear" size="large" @change="handleYearChange">
						<el-option v-for="year in yearOptions" :key="year" :label="`${year}`" :value="year" />
					</el-select>
				</div>
				<div class="timeline">
					<div class="timeline-line">
						<div class="line-bg"></div>
						<div class="line-active" :style="{ width: `${((currentMonthValue - 1) / 11) * 100}%` }"></div>
						<div
							class="slider-button"
							:style="{ left: `${((currentMonthValue - 1) / 11) * 100}%` }"
							@mousedown="startDrag"></div>
						<div class="scale-marks">
							<div v-for="i in 12" :key="i - 1" class="scale-mark cp" @click="handleScaleClick(i)"></div>
						</div>
					</div>
					<div class="month-marks">
						<div
							v-for="month in 12"
							:key="month"
							class="mark"
							:class="{ active: currentMonthValue === month }"
							@click="handleScaleClick(month)">
							{{ month }}月
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
	import { ref, computed, onMounted } from 'vue'
	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()

	import { useSystemStore } from '@/store/modules/system.js'
	const systemStore = useSystemStore()

	const route = useRoute()
	const sLeft = computed(() => {
		if (appStore.switchObj.sidebarFlag) {
			return { left: '84px' }
		} else {
			return { left: '300px' }
		}
	})
	// // 获取最近10年的年份选项
	// const getYearOptions = () => {
	// 	const currentYear = new Date().getFullYear()
	// 	const years = []
	// 	for (let i = 0; i < 10; i++) {
	// 		years.push(currentYear - i)
	// 	}
	// 	return years
	// }

	// const yearOptions = ref(getYearOptions())

	const config = ref(null)
	const yearOptions = ref([])
	const currentYear = ref(new Date().getFullYear())
	const currentMonthValue = ref(new Date().getMonth() + 1)
	const currentMonth = ref(currentMonthValue.value.toString().padStart(2, '0'))

	const isDragging = ref(false)
	const startX = ref(0)
	const startLeft = ref(0)

	onMounted(() => {
		setTimeout(() => {
			const menuData = toRaw(systemStore?.menuTreeData)
			if (menuData?.length > 0) {
				const itemData = menuData.filter(item => item.name === '工具栏菜单')
				const configure = itemData[0]['childrens'].filter(item => item.name === 'y')[0]['childrens']
				config.value = JSON.parse(configure.find(n => n.name == '底图切换').configure)
				yearOptions.value = config.value.map(m => m.year)
			}
		}, 1 * 1000)
	})

	const handleYearChange = year => {
		changeSelectedHandle(year, currentMonthValue.value)
	}

	const startDrag = e => {
		isDragging.value = true
		startX.value = e.clientX
		startLeft.value = currentMonthValue.value - 1
		document.addEventListener('mousemove', onDrag)
		document.addEventListener('mouseup', stopDrag)
	}

	const onDrag = e => {
		if (!isDragging.value) return

		const timeline = document.querySelector('.timeline-line')
		const timelineRect = timeline.getBoundingClientRect()
		const timelineLeft = timelineRect.left
		const timelineWidth = timelineRect.width

		// 计算鼠标在时间轴上的相对位置
		const relativeX = e.clientX - timelineLeft
		const percentPosition = Math.max(0, Math.min(1, relativeX / timelineWidth))

		// 计算最接近的月份（1-12）
		const exactMonth = percentPosition * 11 + 1
		const newMonth = Math.round(exactMonth)

		// 只有当距离足够接近目标月份时才更新
		const threshold = 0.2 // 调整这个值可以改变缓冲效果的强度
		if (Math.abs(exactMonth - newMonth) < threshold) {
			currentMonthValue.value = Math.max(1, Math.min(12, newMonth))
			currentMonth.value = currentMonthValue.value.toString().padStart(2, '0')
			changeSelectedHandle(currentYear.value, currentMonthValue.value)
		}
	}

	const stopDrag = () => {
		isDragging.value = false
		document.body.style.cursor = ''
		document.removeEventListener('mousemove', onDrag)
		document.removeEventListener('mouseup', stopDrag)
	}

	onUnmounted(() => {
		document.removeEventListener('mousemove', onDrag)
		document.removeEventListener('mouseup', stopDrag)
		document.body.style.cursor = ''
	})

	const emit = defineEmits(['change'])

	// 添加 tag 拖拽相关的变量和方法
	const startTagDrag = e => {
		isDragging.value = true
		startX.value = e.clientX
		startLeft.value = currentMonthValue.value - 1

		// 添加拖拽时的鼠标样式
		document.body.style.cursor = 'ew-resize'

		document.addEventListener('mousemove', onTagDrag)
		document.addEventListener('mouseup', stopTagDrag)

		// 阻止文本选中
		e.preventDefault()
	}

	const onTagDrag = e => {
		if (!isDragging.value) return

		const timeline = document.querySelector('.timeline-line')
		const timelineRect = timeline.getBoundingClientRect()
		const timelineLeft = timelineRect.left
		const timelineWidth = timelineRect.width

		// 计算鼠标在时间轴上的相对位置
		const relativeX = e.clientX - timelineLeft
		const percentPosition = Math.max(0, Math.min(1, relativeX / timelineWidth))

		// 计算最接近的月份（1-12）
		const exactMonth = percentPosition * 11 + 1
		const newMonth = Math.round(exactMonth)

		// 只有当距离足够接近目标月份时才更新
		const threshold = 0.2 // 调整这个值可以改变缓冲效果的强度
		if (Math.abs(exactMonth - newMonth) < threshold) {
			currentMonthValue.value = Math.max(1, Math.min(12, newMonth))
			currentMonth.value = currentMonthValue.value.toString().padStart(2, '0')
			changeSelectedHandle(currentYear.value, currentMonthValue.value)
		}
	}

	const stopTagDrag = () => {
		isDragging.value = false
		document.body.style.cursor = ''
		document.removeEventListener('mousemove', onTagDrag)
		document.removeEventListener('mouseup', stopTagDrag)
	}
	// 添加刻度点击处理函数
	const handleScaleClick = month => {
		currentMonthValue.value = month
		currentMonth.value = month.toString().padStart(2, '0')
		changeSelectedHandle(currentYear.value, month)
	}

	onUnmounted(() => {
		document.removeEventListener('mousemove', onTagDrag)
		document.removeEventListener('mouseup', stopTagDrag)
		document.body.style.cursor = ''
	})

	const changeSelectedHandle = (year, month) => {
		let item = config.value.find(n => n.year.includes(year));
		let selMon = item.months.find(n => n.name.includes(month));
		emit('change', {
			themeName: selMon?.themeName || ''
		})
	}
</script>

<style lang="scss" scoped>
	.time-slider {
		position: fixed;
		bottom: 20px;
		z-index: 2024;
		height: 68px;
		padding: 0 16px;
		border-radius: 8px;
		background-color: rgba(255, 255, 255, 0.7);
		&.isCollapse {
			left: 84px;
		}
		.current-tag {
			display: inline-block;
			background: #ff9f43;
			color: white;
			padding: 4px 16px;
			border-radius: 4px;
			font-size: 12px;
			margin-bottom: 8px;
			position: absolute;
			top: -32px;
			left: calc((100% - 156px) * (v-bind(currentMonthValue) - 1) / 11 + 140px);
			transform: translateX(-50%);
			transition: left 0.2s ease-out;
			cursor: ew-resize;
			user-select: none;
			white-space: nowrap;
			z-index: 10;

			&:hover {
				opacity: 0.9;
			}

			&:after {
				content: '';
				position: absolute;
				bottom: -4px;
				left: 50%;
				transform: translateX(-50%);
				width: 2px;
				height: 4px;
				background: #ff9f43;
			}
		}

		.slider-container {
			border-radius: 4px;

			.year-select {
				width: 100px;
				height: 40px;
				flex-shrink: 0;
			}

			.timeline {
				width: 624px;
				position: relative;
				margin-left: 24px;
				padding: 8px 16px 0;
				background-color: white;

				.timeline-line {
					position: relative;
					height: 4px;
					margin: 0;
					margin-bottom: 4px;

					.line-bg {
						position: absolute;
						width: 100%;
						height: 100%;
						background: rgba(180, 180, 180, 0.5);
						border-radius: 2px;
					}

					.line-active {
						position: absolute;
						height: 100%;
						background: #4992ff;
						border-radius: 2px;
						transition: width 0.3s;
					}

					.slider-button {
						position: absolute;
						width: 12px;
						height: 12px;
						background: #ff9f43;
						border: 2px solid #ff9f43;
						border-radius: 50%;
						top: 50%;
						transform: translate(-50%, -50%);
						cursor: pointer;
						transition: left 0.2s ease-out;
						z-index: 1;

						&:hover {
							transform: translate(-50%, -50%) scale(1.2);
						}
					}

					.scale-marks {
						position: absolute;
						width: 100%;
						height: 100%;
						display: flex;
						justify-content: space-between;

						.scale-mark {
							width: 2px;
							height: 8px;
							background: rgba(255, 255, 255, 0.5);
							transform: translateY(-2px);
						}
					}
				}

				.month-marks {
					display: flex;
					justify-content: space-between;
					position: relative;
					padding: 0;
					margin: 0 -12px;

					.mark {
						color: white;
						color: var(--el-text-color-regular);
						font-size: 12px;
						cursor: pointer;
						width: 24px;
						text-align: center;
						white-space: nowrap;
						flex-shrink: 0;
						position: relative;
						left: 0px;

						&.active {
							color: #ff9f43;
						}
					}
				}
			}
		}
	}
</style>
