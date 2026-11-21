import { DataNames } from "/public/map.js";
import GeoJSONUtil from "@/components/map/primitive/util/GeojsonUtil.js";
import EsriJSONUtil from "@/components/map/primitive/util/EsrijsonUtil.js";

import { useMapStore } from "@/store/modules/map.js";
const mapStore = useMapStore();

let lyrName = "fishPortLyr";
let fieldName = "dangerName";
let fieldName2 = "division";

/**
 * 滨海旅游区
 */
export const touristAreaDatas = (shoreName) => {
  return fetch(DataNames["滨海旅游区"])
  .then((res) => res.json())
  .then((res) => {
      if (res?.features?.length) {
          let feats = res?.features.map(m => {
              return {
                  name: m.attributes[fieldName],
                  district: m.attributes[fieldName2],
              }
          });
          if (feats?.length) {
              // 将数据转换为树形结构
              let datas = transformToTree(feats);
              return datas;
          } else {
              return []
          }
      }
  })
  .catch((error) => {
      console.log(error);
      return []
  });
}

const transformToTree = (data) => {
  // 创建根节点（福建省）
  const root = {
      id: '0',
      label: '福建省',
      children: []
  };

  // 用于记录已添加的市级节点
  const cityMap = {};

  // 市级节点ID计数器
  let cityId = 1;

  // 遍历所有数据项
  data.forEach(item => {
      // 解析行政区域信息
      const districts = item.district.split('-');

      // 获取市级名称（第二个部分）
      const cityName = districts[1];

      // 如果该市尚未添加，则创建新的市级节点
      if (!cityMap[cityName]) {
          const cityNode = {
              id: String(cityId++),
              label: cityName,
              children: []
          };
          cityMap[cityName] = cityNode;
          root.children.push(cityNode);
      }

      // 将当前项添加到对应市的children中
      cityMap[cityName].children.push({
          id: item.name,
          label: item.name
      });
  });

  return [root];
}