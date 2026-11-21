import OLVectorLayer from "ol/layer/Vector";
import OLVectorSource from "ol/source/Vector";
import OLGeoJSON from "ol/format/GeoJSON";
import OLEsriJSON from "ol/format/EsriJSON";
import { buildStyle } from "../style/Style";
import { LayerType } from "./Layer";
import { getCenter as OLGetCenter, extend as OLExtend } from 'ol/extent';
import { unByKey } from "ol/Observable";

export class VectorLayer {
  constructor(option, olLyr) {
    this.innerOption = option;
    this.innerLyr = olLyr;
  }

  innerOption = null;
  innerLyr = null;

  //这里做约束必须具有这个属性
  get type() {
    return LayerType.Vector;
  }

  //这里做约束必须具有这个属性
  get ol() {
    return this.innerLyr;
  }

  //这里做约束必须具有这个属性
  get lyrName() {
    return this.innerOption.name;
  }

  //这里做约束必须具有这个属性
  get option() {
    return this.innerOption;
  }

  //这里做约束必须具有这个方法
  clone() {
    return buildVectorLayer(this.innerOption);
  }

  //这里做约束必须具有这个方法
  setVisible(isVisible) {
    this.innerLyr.setVisible(isVisible);
  }

  //这里做约束必须具有这个方法
  setOpacity(opacity) {
    this.innerLyr.setOpacity(opacity);
  }

  //这里做约束必须具有这个方法
  setZIndex(zIndex) {
    this.innerLyr.setZIndex(zIndex);
  }
  
  clear() {
    this.innerLyr.getSource().clear();
  }
}

export function buildVectorLayer(lyrOption) {
  let sourOptions = null;
  if (lyrOption.url) {
    sourOptions = {
      format: new OLEsriJSON(),
      url: lyrOption.url,
    };
  }
  let source = new OLVectorSource(sourOptions);
  if (lyrOption.geojson) {
    source.addFeatures(new OLGeoJSON().readFeatures(lyrOption.geojson));
  }
  if (lyrOption.esrijson) {
    source.addFeatures(new OLEsriJSON().readFeatures(lyrOption.esrijson));
  }
  let layer = new OLVectorLayer({
    source: source,
  });
  if (lyrOption.lyrStyle) {
    layer.setStyle(buildStyle(lyrOption.lyrStyle));
  }
  if (lyrOption?.zooms?.length) {
    lyr.setMinZoom(lyrOption?.zooms[0]);
    if (lyrOption?.zooms.length >= 2) {
      lyr.setMaxZoom(lyrOption?.zooms[lyrOption?.zooms.length - 1]);
    }
  }
  return new VectorLayer(lyrOption, layer);
}
