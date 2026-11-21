
import { coastalProtectionHeaders, coastalProtectionDatas } from './coastalProtection.js'
import { fishPortHeaders, fishPortDatas } from './fishPort.js'
import { maricultureHeaders, maricultureDatas } from './mariculture.js'
import { touristAreaHeaders, touristAreaDatas } from './touristArea.js'

export const loadDatas = (lyrName, shoreName) => {
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

export const getHeaders = (lyrName) => {
    switch(lyrName) {
        case 'Coastal':
            return coastalProtectionHeaders;
        case 'Fishing':
            return fishPortHeaders;
        case 'Seawater':
            return maricultureHeaders;
        case 'Marina':
            return touristAreaHeaders;
    }
}