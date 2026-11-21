import Swipe from "ol-ext/control/Swipe"
import { LayerType } from "../layer/Layer";
import _ from "lodash"

export class SwipeTool2 {
  constructor(option) {
    this.innerOption = option;
    
  }

  innerOption = null;
  swipeTool = null;
  isStarted = false;

  //这里做约束必须具有这个属性
  get name() {
    return "SwipeTool2";
  }

  //这里做约束必须具有这个属性
  get option() {
    return this.innerOption;
  }

  start({ map, leftLyrs, rightLyrs, swipeMode }) {
    if (this.isStarted) {
      this.finish(map);
    }
    this.create(map, swipeMode);
    this.addLayer(map, leftLyrs);
    this.addLayer(map, rightLyrs, true);
    this.isStarted = true;
  }

  collectOlLayers(lyr) {
    if (!lyr) return [];
    if (lyr.type == LayerType.Group) {
      const subs = (lyr.getLayers?.() || lyr.subLyrs || [])
        .map(sub => sub?.layer?.ol || sub?.ol)
        .filter(Boolean);
      if (subs.length) return subs;
    }
    return lyr.ol ? [lyr.ol] : [];
  }

  addLayer(map, lyrNames, isRight = false) {
    const names = (Array.isArray(lyrNames) ? lyrNames : [lyrNames]).filter(Boolean);
    const olLyrs = _.flattenDeep(
      names.map(lyrName => this.collectOlLayers(map.layerManager.getLayer(lyrName)))
    );
    olLyrs.forEach(olLyr => {
      this.swipeTool.addLayer(olLyr, isRight);
    });
  }

  create(map, swipeMode) {
    this.swipeTool = new Swipe({
      position: 0.5,
      orientation: swipeMode || SwipeType.Vertical
    });
    map.ctrlManager.addControl(this.name, { ol: this.swipeTool });
  }

  finish(map) {
    if (this.isStarted && this.swipeTool) {
      this.swipeTool.removeLayers();
      map?.ctrlManager?.removeControl(this.name);
      this.swipeTool = null;
      this.isStarted = false;
    }
  }

  setSwipeType(type) {
    if (this.isStarted && this.swipeTool) {
      this.swipeTool.set('orientation', type);
    }
  }

}

export const SwipeType = {
  Horizontal: "horizontal",
  Vertical: "vertical",
};
