<template></template>

<script setup>
	import { SplitScreen, ViewMode } from '../../primitive/control/SplitScreen.js'
	import emitter from '../../utils/eventbus'

	let splitScn = null
	const map = inject('map')

	const props = defineProps({
		mapMode: {
			type: String,
			default: () => {
				return ViewMode.ONE_MAP
			}
		}
	})

	watch(
		() => props.mapMode,
		newMapMode => {
			if (splitScn) {
				splitScn.setViewMode(newMapMode)
			}
		},
		{
			deep: true,
			immediate: true
		}
	)

	emitter.on('mapMounted', map => {
		let mapEl = map.olMap.getTargetElement()
		splitScn = new SplitScreen({
			map: map,
			mapOption: map.mapOptions,
			mainContainer: mapEl,
			width: mapEl.offsetWidth,
			height: mapEl.offsetHeight
		})
	})

	defineExpose({})
</script>
