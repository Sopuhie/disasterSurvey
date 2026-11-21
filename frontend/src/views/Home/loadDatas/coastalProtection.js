import { DataNames } from "/public/map.js";
import buildLayer, { LayerType } from "@/components/map/primitive/layer/Layer";
import GeoJSONUtil from '@/components/map/primitive/util/GeojsonUtil.js'
import EsriJSONUtil from '@/components/map/primitive/util/EsrijsonUtil.js'

import FixedPopup from 'ol-ext/overlay/FixedPopup.js'

import { useMapStore } from "@/store/modules/map.js";
const mapStore = useMapStore();

let lyrName = "coastProtectionLyr";
let fieldName = "dangerName";

/**
 * 海岸防护工程
 */
const drawCoastProtection = (data) => {
    if (mapStore.activeMap) {
        if (mapStore.activeMap.layerManager.getLayer(lyrName)) {
            mapStore.activeMap.layerManager.removeLayer(lyrName);
        } else {
            // 岸段数据展示
            fetch(DataNames["海岸防护工程"])
                .then((res) => res.json())
                .then((res) => {
                    if (res?.features?.length) {
                        let feat = res?.features.find(m => m.attributes[fieldName] == data);
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
                                    lyrStyle: coastProtectionStyle
                                })
                            );
                            mapStore.activeMap.featureManager.removeFeatures()
                            mapStore.activeMap.flyToManager.flyToRegion(feature)
                        }
                    }
                })
                .catch((error) => {
                    console.log(error);
                });
        }
    }
};

/**
 * 海堤样式
 */
let coastProtectionStyle = (feature) => {
    let clr = 'blue';
    if (feature.properties.length > 0 && feature.properties.length <= 3300) {
        clr = 'yellow';
    }
    if (feature.properties.length > 3300 && feature.properties.length <= 78055176) {
        clr = 'orange';
    }
    if (feature.properties.length > 78055176) {
        clr = 'orange';
    }
    return {
        stroke: {
            color: clr, //"rgba(255, 140, 26,1)",
            width: 3
        },
        text: {
            font: "normal 16px black",
            fill: {
                color: "blue",
            },
            text:"", // feature?.properties?.dangerName || "",//feature.properties.stationName,
            offsetY: 35,
            stroke: {
                color: "rgba(255,255,255,1)",
                width: 2,
            },
        },
    };
};

export default drawCoastProtection;
