

import html2canvas from "html2canvas";

import { useExportStore } from "@/store/modules/export.js";
const exportStore = useExportStore();

const exportScreenshot = (div) => {
  if (div) {
    html2canvas(div, {
      useCORS: true
    }).then((canvas) => {
      let size = exportStore.isExportInfo.resolution.split("x").map(Number);
      var cs = document.createElement('canvas');
      let ctx = cs.getContext('2d');
      cs.width = size[0];
      cs.height = size[1];
      ctx.drawImage(canvas, 0, 0, cs.width, cs.height);
      var url = cs.toDataURL("image/png");
  
      let aLink = document.createElement("a");
      aLink.style.display = "none";
      url.replace(/^data:image\/png/, "data:application/octet-stream")
      aLink.href = url;
      aLink.download = exportStore.isExportInfo.fileName + "." + exportStore.isExportInfo.suffix;
      document.body.appendChild(aLink);
      aLink.click();
      document.body.removeChild(aLink);
    });
  }
}
 
export default exportScreenshot