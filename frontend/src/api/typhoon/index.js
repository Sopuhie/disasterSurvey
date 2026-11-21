import axios from 'axios'
import request from '@/utils/authRequest.js'

const prefix5 = 'https://tf.istrongcloud.com/data/'

export function typhoonApi() {
	return {
		/**
     * 获取当年台风信息
     */
		getCurrentYearTyphoonInfos: () => {
      return request({
        url: prefix5 + `/complex/path.json?rand=${Math.floor(Math.random()*(1 - 1000000) + 1000000)}`,
        method: 'get',
      })
    },
    /**
     * 获取年份信息
     */
    getYearInfos: () => {
      return request({
        url: prefix5 + `/complex/years.json?rand=${Math.floor(Math.random()*(1 - 1000000) + 1000000)}`,
        method: 'get',
      })
    },
    /**
     * 根据年份获取台风信息
     */
    getTyphoonInfosByYear: (year) => {
      return request({
        url: prefix5 + `/complex/${year}.json?rand=${Math.floor(Math.random()*(1 - 1000000) + 1000000)}`,
        method: 'get',
      })
    },
    /**
     * 根据编号获取某个台风信息
     */
    getTyphoonInfosById: (typhoonNum) => {
      // return request({
      //   url: prefix5 + `/complex/${typhoonNum}.json?rand=${Math.floor(Math.random()*(1 - 1000000) + 1000000)}`,
      //   method: 'get',
      // })
      return axios.get(prefix5 + `/complex/${typhoonNum}.json?rand=${Math.floor(Math.random()*(1 - 1000000) + 1000000)}`).then(response => {
        return response.data
      })
    }
	}
}
