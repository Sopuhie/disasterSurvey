
import { coastalProtectionDatas } from './coastalProtection.js'
import { fishPortDatas } from './fishPort.js'
import { maricultureDatas } from './mariculture.js'
import { touristAreaDatas } from './touristArea.js'

export const loadTreeDatas = (lyrName, shoreName) => {
    switch(lyrName) {
        case 'Coastal':
            return coastalProtectionDatas(shoreName);
        case 'Fishing':
            return fishPortDatas(shoreName);
        case 'Seawater':
            return maricultureDatas(shoreName);
        case 'Marina':
            return touristAreaDatas(shoreName);
    }
}
