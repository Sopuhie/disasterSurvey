import { DataNames } from '/public/map.js'
import buildLayer, { LayerType } from '@/components/map/primitive/layer/Layer'
import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
import EsriJSONUtil from '@/components/map/primitive/util/EsrijsonUtil.js'
import FixedPopup from 'ol-ext/overlay/FixedPopup.js'

import { useMapStore } from '@/store/modules/map.js'
const mapStore = useMapStore()

let lyrName = 'fishPortLyr'
let fieldName = 'dangerName'

/**
 * 渔港
 */
const drawFishPort = data => {
	if (mapStore.activeMap) {
		if (mapStore.activeMap.layerManager.getLayer(lyrName)) {
			mapStore.activeMap.layerManager.removeLayer(lyrName)
		} else {
			// 岸段数据展示
			fetch(DataNames['渔港'])
				.then(res => res.json())
				.then(res => {
					if (res?.features?.length) {
						let feat = res?.features.find(m => m.attributes[fieldName] == data)
						if (feat) {
							let feature = GeoJSONUtil.writeFeatureObject(EsriJSONUtil.readFeature(feat))
							mapStore.activeMap.layerManager.addLayer(
								lyrName,
								buildLayer(LayerType.Vector, {
									name: lyrName,
									geojson: JSON.stringify({
										type: 'FeatureCollection',
										features: [feature]
									}),
									lyrStyle: fishPortStyle
								})
							)
							mapStore.activeMap.featureManager.removeFeatures()
							mapStore.activeMap.flyToManager.flyToRegion(feature)
						}
					}
				})
				.catch(error => {
					console.log(error)
				})
		}
	}
}

/**
 * 海堤样式
 */
let fishPortStyle = feature => {
	return {
		fill: {
			color: 'rgba(204, 136, 0, 0.2)'
		},
		stroke: {
			color: 'rgba(204, 136, 0, 1)',
			width: 3
		},
		text: {
			font: 'normal 16px black',
			fill: {
				color: 'blue'
			},
			text: '', //feature.properties.stationName,
			offsetY: 35,
			stroke: {
				color: 'rgba(255,255,255,1)',
				width: 2
			}
		}
	}
}

export default drawFishPort
