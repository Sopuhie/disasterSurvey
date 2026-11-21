import { coastalProtectionDatas } from './coastalProtection.js'
import { fishPortDatas } from './fishPort.js'
import { maricultureDatas } from './mariculture.js'
import { touristAreaDatas } from './touristArea.js'
import { seawaveDatas } from './seawave.js'
import { seabreezeDatas } from './seabreeze.js'
import { economicDatas } from './economic.js'

export const loadTreeDatas = async (lyrName, shoreName) => {
	switch (lyrName) {
		case 'Coastal':
			return await coastalProtectionDatas(shoreName)
		case 'Fishing':
			return await fishPortDatas(shoreName)
		case 'Seawater':
			return await maricultureDatas(shoreName)
		case 'Marina':
			return await touristAreaDatas(shoreName)
		case 'Seawave':
			return await seawaveDatas(shoreName)
		case 'Seabreeze':
			return await seabreezeDatas(shoreName)
		case 'Economic':
			return await economicDatas(shoreName)
	}
}
