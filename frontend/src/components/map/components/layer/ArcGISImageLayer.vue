<template />

<script setup>
	import buildLayer, { LayerType } from '../../primitive/layer/Layer'
	import emitter from '../../utils/eventbus'

	const ArcGISImageLyr = ref(null)

	const props = defineProps({
		lyrOption: {
			// name: String,
			// url: String,
			// zIndex?: Number,
			// resolutions?: Number[] || system,
			// projection? Object || String || system,
			// params?: Object || {},
			// imageLoadFunction?: Function
			type: Object,
			default: () => {
				return null
			}
		},
		isVisble: {
			type: Boolean,
			default: () => {
				return true
			}
		}
	})

	emitter.on('mapMounted', map => {
		if (map) {
			const lyrOpt = toRaw(props.lyrOption)
			lyrOpt.projection = lyrOpt.projection || map.projection
			lyrOpt.resolutions = lyrOpt.resolutions || map.resolutions
			ArcGISImageLyr.value = buildLayer(LayerType.ArcGISImage, lyrOpt)
			if (!ArcGISImageLyr) {
				throw new Error('initialize ArcGISImage layer failed')
			}
			if (map.layerManager.getLayer(lyrOpt.name)) {
				map.layerManager.removeLayer(lyrOpt.name)
			}
			map.layerManager.addLayer(lyrOpt.name, unref(ArcGISImageLyr))
		}
	})
</script>
