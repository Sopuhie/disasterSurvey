/**
 * 漫堤分析种子：从 public/datas/漫堤分析.json 抽取代表性样本写入运维台账。
 */
import { DataNames } from '/public/map.js'
import { mapOvertoppingRows } from '@/views/assessment/utils/assessmentEngine.js'

const PREFERRED_CITIES = ['宁德市', '福州市', '莆田市', '泉州市', '漳州市', '厦门市']
const SEED_LIMIT = 80

export async function loadOvertopSeedRows() {
	const url = DataNames['漫堤分析']
	if (!url) return []
	try {
		const res = await fetch(url)
		if (!res.ok) return []
		const json = await res.json()
		const raw = Array.isArray(json) ? json[0]?.data || json : json?.data || []
		const mapped = mapOvertoppingRows(raw)
		const preferred = mapped.filter(item => PREFERRED_CITIES.some(city => item.city?.includes(city)))
		const pool = preferred.length ? preferred : mapped
		return pool.slice(0, SEED_LIMIT).map(item => ({
			id: item.id,
			city: item.city,
			name: item.name,
			hazardCode: '',
			location: item.location,
			tideStation: item.tideStation,
			warnTide: item.warnTide,
			tideHeight: item.tideHeight,
			protectPop: Number(item.protectPop) || 0,
			crestElev: item.crestElev,
			hasWall: item.hasWall,
			wallElev: item.wallElev,
			warnLevel: item.warnLevel || '不超',
			lon: '',
			lat: '',
			remark: '系统漫堤分析种子（潮位/高程 m，保护人口 人）'
		}))
	} catch (e) {
		return []
	}
}
