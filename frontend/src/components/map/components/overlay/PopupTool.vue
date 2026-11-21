<template />

<script setup>
	import PopupTool from '../../primitive/overlay/PopupTool.js'
	import GeoJSONUtil from '../../primitive/util/GeojsonUtil.js'
	import emitter from '../../utils/eventbus'

	const map = inject('map')
	let popupTool = null

	let coordinates = ref([])
	let content = ref('')

	const props = defineProps({
		options: {
			// {
			//   popupClass: PopupClass;
			//   onclose: () => {};
			//   onshow: () => {};
			//   positioning: Positioning;
			//   toCenter: boolean,
			//   autoPan: {
			//     animation: {
			//       duration: number
			//     }
			//   };
			// }
			type: Object,
			default: () => {
				return null
			}
		},
		layers: {
			type: Array,
			default: () => {
				return []
			}
		},
		contentFun: {
			type: Function,
			default: () => {
				return features => {}
			}
		}
	})

	watch(
		() => coordinates,
		val => {
			if (unref(val)?.length) {
				popupTool?.show(unref(map), unref(val), content.value)
			} else {
				popupTool?.close(unref(map))
			}
		},
		{
			immediate: true,
			deep: true
		}
	)

	emitter.on('mapMounted', map => {
		if (map) {
			popupTool = new PopupTool(toRaw(props.options))
			if (props.layers?.length) {
				let lyrs = props.layers.map(lyr => {
					return map.layerManager.getLayer(lyr).ol
				})
				map.olMap.on('click', evt => {
					let pixel = map.olMap.getEventPixel(evt.originalEvent)
					let feats = map.olMap.getFeaturesAtPixel(pixel, {
						layerFilter: layer => {
							if (lyrs?.length) {
								return lyrs.includes(layer)
							} else {
								return true
							}
						}
					})
					if (feats && feats.length) {
						content.value = props.contentFun(GeoJSONUtil.writeFeatures(feats))
						coordinates.value = evt.coordinate
					}
				})
			}
		}
	})

	function close() {
		coordinates.value = []
	}

	defineExpose({
		close
	})
</script>
