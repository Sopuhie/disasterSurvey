<template>
	<OLMap ref="map" style="width: 100%; height: 100%" :mapOptions="MapDefaultOption">
		<coastalProtectionPopup
			ref="coastalProtectionPopupRef"
			v-if="curSubTabIdx == 'coastalProtectionPopupRef'"
			:selectedFeature="selectedFeature"
			@close="closeConPop" />
		<fishPortPopup ref="fishPortPopupRef" v-if="curSubTabIdx == 'fishPortPopupRef'" :selectedFeature="selectedFeature" @close="closeConPop" />
		<mariculturePopup ref="mariculturePopupRef" v-if="curSubTabIdx == 'mariculturePopupRef'" :selectedFeature="selectedFeature" @close="closeConPop" />
		<touristAreaPopup ref="touristAreaPopupRef" v-if="curSubTabIdx == 'touristAreaPopupRef'" :selectedFeature="selectedFeature" @close="closeConPop" />
	</OLMap>
</template>

<script setup>
	import OLMap from '../../components/map/components/Map.vue'
	import emitter from '@/components/map/utils/eventbus'
	import { BASE_LAYER_NAME } from '@/components/map/primitive/config/LayerConstantConfig.js'
	import buildLayer, { LayerType } from '@/components/map/primitive/layer/Layer'
	import { MapDefaultOption, DistrictData } from '/public/map.js'
	import { tiandituVecTileUrl } from '@/config'
	import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
	import EsriJSONUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
	import coastalProtectionPopup from './popup/coastalProtectionPopup.vue'
	import fishPortPopup from './popup/fishPortPopup.vue'
	import mariculturePopup from './popup/mariculturePopup.vue'
	import touristAreaPopup from './popup/touristAreaPopup.vue'

	import { useMapStore } from '@/store/modules/map.js'
import { ref } from 'vue'
	const mapStore = useMapStore()

	const curSubTabIdx = ref('')
	const selectedFeature = ref(null)

	onMounted(() => {
		emitter.on('mapMounted', map => {
			if (map) {
				let baseLyr = buildLayer(LayerType.Group, {
					name: '天地图底图',
					zIndex: -100,
					layers: [
						{
							type: LayerType.XYZ,
							name: '天地图底图1',
							lyrOption: {
								url: tiandituVecTileUrl
							}
						},
						{
							type: LayerType.Vector,
							lyrOption: {
								name: '蒙版',
								url: DistrictData['蒙版'],
								lyrStyle: {
									fill: {
										color: 'rgba(0,0,0,0.25)'
									},
								}
							}
						},
						{
							type: LayerType.Vector,
							lyrOption: {
								name: '市界',
								url: DistrictData['市界'],
								lyrStyle: {
									stroke: {
										color: 'grey',
										width: 1
									}
								}
							}
						},
						{
							type: LayerType.Vector,
							lyrOption: {
								name: '行政区内陆轮廓',
								url: DistrictData['行政区内陆轮廓'],
								lyrStyle: {
									stroke: {
										color: 'gray',
										width: 1
									}
								}
							}
						},
						{
							type: LayerType.Vector,
							lyrOption: {
								name: '近海',
								url: DistrictData['近海'],
								lyrStyle: {
									stroke: {
										color: 'rgba(0,0,200,0.8)',
										width: 1.5
									}
								}
							}
						},
						{
							type: LayerType.Vector,
							lyrOption: {
								name: '界线',
								url: DistrictData['界线'],
								lyrStyle: {
									stroke: {
										color: 'rgba(0,0,200,0.8)',
										width: 1.5
									}
								}
							}
						},
					]
				})
				mapStore.activeMap.lyrManager.addLayer(BASE_LAYER_NAME, baseLyr)
			}
		})
		emitter.on('mapClick', args => {
			if (args?.length) {
				let feature = GeoJSONUtil.writeFeatureObject(EsriJSONUtil.readFeature(args[0].feature))
				mapStore.activeMap.featureManager.removeFeatures()
				mapStore.activeMap.featureManager.addFeature('Temp_' + Math.floor(Math.random() * 10000), feature, true)
				mapStore.activeMap.flyToManager.flyToRegion(feature)
				let t = setTimeout(() => {
					mapStore.activeMap.featureManager.removeFeatures();
				}, 3 * 1000);
				selectedFeature.value = feature;
				switch (args[0].lyrName) {
					case 'coastProtectionLyr':
						curSubTabIdx.value = 'coastalProtectionPopupRef'
						return
					case 'fishPortLyr':
						curSubTabIdx.value = 'fishPortPopupRef'
						return
					case 'maricultureLyr':
						curSubTabIdx.value = 'mariculturePopupRef'
						return
					case 'touristAreaLyr':
						curSubTabIdx.value = 'touristAreaPopupRef'
						return
				}
			}
		})
	})

	const closeConPop = () => {
		curSubTabIdx.value = ''
	}
</script>

<style lang="scss" scoped></style>
