import drawCoastProtection from './coastalProtection.js'
import drawFishPort from './fishPort.js'
import drawMariculture from './mariculture.js'
import drawTouristArea from './touristArea.js'

import { useMapStore } from '@/store/modules/map.js'
const mapStore = useMapStore()

import { useAppStore } from '@/store/modules/app.js'
const appStore = useAppStore()

const loadDatas = (lyrName, data) => {
	// 移除数据
	mapStore.loadLyrNames.forEach(lyr => {
		if (mapStore.activeMap?.layerManager.getLayer(lyr)) {
			mapStore.activeMap?.layerManager.removeLayer(lyr)
		}
	})

	switch (lyrName) {
		case '海岸防护工程隐患整治效果评估':
			return drawCoastProtection(data)
		case '渔港隐患整治效果评估':
			return drawFishPort(data)
		case '海水养殖区隐患整治效果评估':
			return drawMariculture(data)
		case '滨海旅游区隐患整治效果评估':
			return drawTouristArea(data)
	}
}

export default loadDatas
