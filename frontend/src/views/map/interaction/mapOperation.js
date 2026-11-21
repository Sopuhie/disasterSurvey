import buildLayer, { LayerType } from '@/components/map/primitive/layer/Layer'
import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
import EsriJSONUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
import FixedPopup from 'ol-ext/overlay/FixedPopup.js'
import { DataNames } from '/public/map.js'

import { useAppStore } from '@/store/modules/app.js'
const appStore = useAppStore()

import { useMapStore } from '@/store/modules/map.js'
const mapStore = useMapStore()

/**
 *
 */
export const flyToPosition = () => {
	if (mapStore.activeMap) {
		let url = DataNames['海岸防护工程']
		if (appStore.moduleName == '/') {
		}
		if (appStore.moduleName == '/Fishing') {
			url = DataNames['渔港']
		}
		if (appStore.moduleName == '/Seawater') {
			url = DataNames['海水养殖区']
		}
		if (appStore.moduleName == '/Marina') {
			url = DataNames['滨海旅游区']
		}

		fetch(url)
			.then(res => res.json())
			.then(res => {
				if (res?.features?.length) {
					let feature = res.features[Math.ceil(Math.random() * res.features.length)]
					let geojson = GeoJSONUtil.writeFeatureObject(EsriJSONUtil.readFeature(feature))
					mapStore.activeMap.featureManager.removeFeatures()
					mapStore.activeMap.featureManager.addFeature('Temp_' + Math.floor(Math.random() * 10000), geojson, true)
					mapStore.activeMap.flyToManager.flyToRegion(geojson)
					let t = setTimeout(() => {
						mapStore.activeMap.featureManager.removeFeatures()
					}, 3 * 1000)
				}
			})
			.catch(error => {
				console.log(error)
			})
	}
}
