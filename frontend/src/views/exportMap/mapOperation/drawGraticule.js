
import GeoJSONUtil from "@/components/map/primitive/util/GeojsonUtil.js";
import EsrijsonUtil from "@/components/map/primitive/util/EsrijsonUtil.js";
import emitter from "@/components/map/utils/eventbus.js";
import { State } from "@/components/map/primitive/interaction/DrawTool.js";
import { Graticule } from "@/components/map/primitive/control/Graticule"

import { useExportStore } from "@/store/modules/export.js";
const exportStore = useExportStore();

import * as turf from '@turf/turf';

/**
 * 网格工具
 */
let graticuleTool = null;

/**
 * 
 * 网格工具名称
 */
let toolName = 'GraticuleTool';

const drawGraticule = (isVisible) => {
  if (exportStore.outMap) {
    if (isVisible) {
      if (!exportStore.outMap.ctrlManager.getControl(toolName)) {
        exportStore.outMap.ctrlManager.addControl(toolName, getGraticuleTool());
      }
    } else {
      exportStore.outMap.ctrlManager.removeControl(toolName);
    }
  }
}

const getGraticuleTool = () => {
  if (!graticuleTool) {
    graticuleTool = new Graticule({
      projection: exportStore.outMap.projection,
      step: 0.5,
      formatCoord: (coord) => {
        return coord.toFixed(1)+"°"
      },
      border: true,
      grid: false,
      coord: true
    });
  }
  return graticuleTool;
}

export default drawGraticule