
import emitter from "@/components/map/utils/eventbus.js";
import { State } from "@/components/map/primitive/interaction/DrawTool.js";

import { useExportStore } from "@/store/modules/export.js";
const exportStore = useExportStore();

/**
 * 地图对象
 */
let drawTool = null

/**
 * 绘制图标大小
 */
let iconSize = {
  大: [48, 48],
	中: [32, 32],
	小: [16, 16],
}
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

/**
 * @param {object|string} item 固定符号对象 { id, label, svg, color } 或兼容旧字符串
 * @param {'大'|'中'|'小'} size
 */
const drawPoint = (item, size) => {
  drawTool = exportStore.outMap.drawTool;
  const isize = iconSize[size] || iconSize['中'];
  let iconStr = ''
  if (item && typeof item === 'object' && item.svg) {
    iconStr = item.svg
      .replace(/width="\d+"/, `width="${isize[0]}"`)
      .replace(/height="\d+"/, `height="${isize[1]}"`)
  } else {
    // 兼容旧逻辑：默认定位点
    iconStr = `<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="${isize[0]}" height="${isize[1]}"><path d="M448 981.333a64 42.667 0 1 0 128 0 64 42.667 0 1 0-128 0z" fill="#A93529"/><path d="M554.667 593.941v376.726A10.667 10.667 0 0 1 544 981.333h-64a10.667 10.667 0 0 1-10.667-10.666V593.94c-144.682-20.757-256-144.853-256-295.274C213.333 133.717 347.051 0 512 0s298.667 133.717 298.667 298.667c0 150.421-111.318 274.517-256 295.274z" fill="#F64336"/><path d="M426.667 298.667a85.333 85.333 0 1 0 170.666 0 85.333 85.333 0 1 0-170.666 0z" fill="#FFFFFF"/></svg>`
  }
  drawStyle = {
    image: {
      rotateWithView: true,
      src: 'data:image/svg+xml;utf8,' + encodeURIComponent(iconStr),
      imgSize: isize,
      offset: [0, 0]
    },
  }
  exportStore.setDrawType('Point');
  startDrawTool();
}

emitter.on("drawend", feature => {
  if (exportStore.isOpenExport && exportStore.drawType == 'Point') {
    exportStore.setDrawedPoint(feature, drawStyle);
    closeDrawTool();
  }
})

const startDrawTool = () => {
  let drawOption = {
    drawType: "Point",
    skipOverlay: true,
    drawingStyle: drawStyle,
    drawedStyle: drawStyle
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

export default drawPoint
