<template />

<script setup>
	import { MeasureTool, MeasureType } from '../../primitive/interaction/MeasureTool'
	import emitter from '../../utils/eventbus'

	const map = inject('map')
	let meaTool = null
	let isMeasured = false

	const props = defineProps({
		measureType: {
			type: String,
			default: () => {
				return MeasureType.Area
			}
		}
	})

	emitter.on('mapMounted', map => {
		// meaTool = new MeasureTool({
		//   map: map.value,
		// });
		meaTool = map.measureTool
		// meaTool.setDrawingStyle((feature) => {
		//   // console.log(feature)
		//   return {
		//     stroke: {
		//       color: '',
		//       width: 4
		//     }
		//   }
		// })

		meaTool.setDrawedStyle(feature => {
			// console.log(feature)
			return {
				stroke: {
					color: '#' + Math.floor(Math.random() * 16777215).toString(16),
					width: 2
				}
			}
		})
	})

	watch(
		() => props.measureType,
		() => {
			if (isMeasured) {
				meaTool.toggle(props.measureType)
			}
		}
	)

	const start = () => {
		meaTool.start(unref(props.measureType))
		isMeasured = true
	}

	const finish = () => {
		if (isMeasured) {
			meaTool.finish()
			isMeasured = false
		}
	}

	const clear = () => {
		if (isMeasured) {
			meaTool.clear()
		}
	}

	const close = () => {
		meaTool.close()
		isMeasured = false
	}

	defineExpose({
		start,
		finish,
		clear,
		close
	})
</script>
