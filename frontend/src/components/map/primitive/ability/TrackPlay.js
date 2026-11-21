import Path from "ol-ext/featureanimation/Path";
import OLFeature from 'ol/Feature'
import { Point } from "ol/geom";
import { buildVectorLayer } from "../layer/VectorLayer";
import { buildStyle } from "../style/Style";
import GeoJSONUtil from "../util/GeojsonUtil.js";

export class TrackPlay {

  constructor(option) {
    this.innerOption = option;
  }

  innerOption = null;
  trackLyr = null;
  LAYER_NAME = "trackPlayLyr"
  playPath = null;

  // option 参数
  // map: 地图对象
  // geojson: 线数据
  // lyrStyle: 图层样式
  // rotation: 沿线改变方向
  // easing: 缓解
  // speed: 播放速度 0~3
  // revers： 反转
  start(option) {
    this.innerOption = option;
    this.trackLyr = buildVectorLayer({
      name: this.LAYER_NAME,
      geojson: {
        type: "FeatureCollection",
        features: [option.geojson]
      },
      lyrStyle: option.lyrStyle
    });
    option.map.lyrManager.addLayer(this.LAYER_NAME, this.trackLyr);
    this.playPath = GeoJSONUtil.readFeature(option.geojson);
    this.animateFeature();
  }

  finish() {}

  f = null;
  anim = null;
  animateFeature(){
    if (this.playPath) {
      this.f = new OLFeature(new Point([0,0]));
      if (this.innerOption.rotation) this.f.setStyle(buildStyle(this.innerOption.styleTriangle));
      else this.f.setStyle(buildStyle(this.innerOption.lyrStyle));
      // this.anim = new Path({
      //   path: this.playPath,
      //   rotate: this.innerOption.rotation,
      //   easing: this.innerOption.easing,
      //   speed: this.innerOption.speed,
      //   revers: this.innerOption.revers
      // });
      // this.trackLyr.ol.animateFeature( this.f, this.anim );
    }
  }
}

export const Easing = {
  Linear: "linear",
  EaseOut: "easeOut",
  EaseIn: "easeIn",
  InAndOut: "inAndOut",
};
