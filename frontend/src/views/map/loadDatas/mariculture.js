import { DataNames } from "/public/map.js";
import buildLayer, { LayerType } from "@/components/map/primitive/layer/Layer";
import GeoJSONUtil from "@/components/map/primitive/util/GeojsonUtil.js";
import EsriJSONUtil from "@/components/map/primitive/util/EsrijsonUtil.js";
import FixedPopup from 'ol-ext/overlay/FixedPopup.js'
import { getCenter } from "@/components/map/primitive/util/CalculationUtil.js";

import { useMapStore } from "@/store/modules/map.js";
const mapStore = useMapStore();

let lyrName = "maricultureLyr";
let fieldName = "dangerName";

/**
 * 海水养殖区
 */
const drawMariculture = (data) => {
    if (mapStore.activeMap) {
        if (mapStore.activeMap.layerManager.getLayer(lyrName)) {
            mapStore.activeMap.layerManager.removeLayer(lyrName);
        } else {
            // 岸段数据展示
            fetch(DataNames["海水养殖区"])
                .then((res) => res.json())
                .then((res) => {
                    let feat = res?.features.find(m => m.attributes[fieldName] == data);
                    if (feat) {
                        let feature = GeoJSONUtil.writeFeatureObject(EsriJSONUtil.readFeature(feat))
                        let lyr = buildLayer(LayerType.Vector, {
                            name: lyrName,
                            geojson: JSON.stringify({
                                type: 'FeatureCollection',
                                features: [feature]
                            }),
                            lyrStyle: maricultureStyle
                        })
                        lyr.ol.set("lyrName", lyrName)
                        mapStore.activeMap.layerManager.addLayer(
                            lyrName,
                            lyr
                        );
                        mapStore.activeMap.featureManager.removeFeatures()
                        mapStore.activeMap.flyToManager.flyToRegion(feature)
                        // mapStore.activeMap.flyToManager.flyTo(getCenter(feature), 14);
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
let maricultureStyle = (feature) => {
    return {
        fill: {
            color: "rgba(255, 204, 0, 0.2)",
        },
        stroke: {
            color: "rgba(255, 204, 0, 0.5)",
            width: 3
        },
        text: {
            font: "normal 16px black",
            fill: {
                color: "blue",
            },
            text: feature.properties.dangerName,
            offsetY: 35,
            stroke: {
                color: "rgba(255,255,255,1)",
                width: 2,
            },
        },
    };
};

export default drawMariculture;
