import emitter from "../../../utils/eventbus";

export default function (map) {
  map.on("pointermove", (evt) => {
    if (!evt.dragging) {
      emitter.emit("coordinates", evt.coordinate);
    }
  });
}
