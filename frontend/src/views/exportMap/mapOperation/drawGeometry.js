
import GeoJSONUtil from "@/components/map/primitive/util/GeojsonUtil.js";
import EsrijsonUtil from "@/components/map/primitive/util/EsrijsonUtil.js";
import emitter from "@/components/map/utils/eventbus.js";
import { State } from "@/components/map/primitive/interaction/DrawTool.js";

import { useExportStore } from "@/store/modules/export.js";
const exportStore = useExportStore();

import * as turf from '@turf/turf';

/**
 * 地图对象
 */
let drawTool = null 

/**
 * 绘制类型
 */
let drawType = "Polygon";

/**
 * 绘制样式
 */
let drawStyle = {
  stroke: {
    color: "rgba(61, 151, 248, 0.9)",
    width: 2
  },
  fill: {
    color: "rgba(61, 151, 248, 0.7)"
  },
}

const drawGeometry = (type, style) => {
  drawTool = exportStore.outMap.drawTool;
  drawType = type;
  setDrawStyle(style);
  exportStore.setDrawType('Geometry');
  startDrawTool();
}
 
const setDrawStyle = (style) => {
  if (style.color) {
    let sanitizedHex = style.color.replace("#", "");
    // 解析红、绿、蓝值
    let r = parseInt(sanitizedHex.substring(0, 2), 16);
    let g = parseInt(sanitizedHex.substring(2, 4), 16);
    let b = parseInt(sanitizedHex.substring(4, 6), 16);
  
    drawStyle.fill.color = `rgba(${r}, ${g}, ${b}, 0.5)`;
    drawStyle.stroke.color = `rgba(${r}, ${g}, ${b}, 0.8)`;
    drawStyle.stroke.width = style.width || drawStyle.stroke.width;
  }
}

emitter.on("drawend", feature => {
  if (exportStore.isOpenExport && exportStore.drawType == 'Geometry') {
    exportStore.setDrawedGeometry(feature, drawStyle);
    closeDrawTool();
  }
})

const startDrawTool = () => {
  let drawOption = {
    drawType: drawType,
    skipOverlay: true,
    drawingStyle: drawStyle,
    drawedStyle: {
      fill: {
        color: "rgba(255, 255, 255, 0.0001)"
      },
    }
  }
  if (drawTool.state() == State.Drawding) {
    drawTool.toggle(drawOption)
  } else {
    drawTool.start(drawOption)
  }
}

const closeDrawTool = () => {
  if (drawTool.state() == State.Drawding) {
    drawTool.finish();
    drawTool.clear();
  }
}

export default drawGeometry