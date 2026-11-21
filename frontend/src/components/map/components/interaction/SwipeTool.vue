<template />

<script setup>
	import { SwipeTool, SwipeType } from '../../primitive/interaction/SwipeTool'
	import emitter from '../../utils/eventbus'

	const map = inject('map')
	let swipeTool = null

	const props = defineProps({
		swipeMode: {
			type: String,
			default: () => {
				return SwipeType.Horizontal
			}
		},
		lyrNames: {
			type: Array,
			default: () => {
				return []
			}
		},
		splitRate: {
			type: Number,
			required: true,
			default: () => {
				return 50
			}
		}
	})

	emitter.on('mapMounted', map => {
		swipeTool = new SwipeTool({
			map: map
		})
	})

	watch(
		() => props.splitRate,
		() => {
			swipeMap()
		}
	)

	watch(
		() => props.swipeMode,
		() => {
			swipeMap()
		}
	)

	function swipeMap() {
		swipeTool.swipeRate({
			swipeLayers: props.lyrNames,
			swipeMode: props.swipeMode,
			splitRate: props.splitRate
		})
	}

	defineExpose({})
</script>
