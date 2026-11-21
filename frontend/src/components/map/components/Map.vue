<template>
	<div ref="mapDiv" class="olmap max-wh" style="background: black">
		<slot />
	</div>
</template>

<script setup>
	import OLProtoMap from '../primitive/map/OLProtoMap.js'
	import emitter from '../utils/eventbus.js'

	// 定义变量
	let map = ref(null)
	const mapDiv = ref(null)

	// 自定义属性
	const props = defineProps({
		mapOptions: {
			type: Object,
			default: () => {
				return null
			}
		},
		getMap: {
			type: Function,
			default: null
		}
	})

	watch(
		() => props.mapOptions,
		v => {
			if (v) {
				nextTick(() => {
					const mapOptions = unref(v)
					map.value = new OLProtoMap(mapOptions, toRef(mapDiv).value)
					if (props.getMap && props.getMap instanceof Function) {
						props.getMap(unref(map))
					} else {
					}
					emitter.emit('mapMounted', unref(map))
				})
			}
		},
		{
			immediate: true
		}
	)

	// 生命钩子函数
	onMounted(() => {})

	const setMapRange = range => {
		map.value.rangePostrender(toRaw(range))
	}

	// val in (0 - 30)
	const setPerspective = val => {
		map.value.setPerspective(val)
	}

	// 向外提供地图对象
	provide('map', map)

	// 向外提供地图方法
	defineExpose({
		setMapRange,
		setPerspective
	})
</script>

<style></style>
