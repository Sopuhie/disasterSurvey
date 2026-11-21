/**
 * 从 public/datas/乡镇.json 加载乡镇行政区台账（含 geojson 边界）。
 * - 属性：福建省民政厅《行政区划名录及代码》（截至 2025-06-30）
 * - 几何：map.ruiduobao.com 乡镇矢量（入库时写入 geojson，并由 IndexedDB 持久化）
 */
import { prefix } from '@/config'

const withPrefix = path => (prefix ? '/' + prefix : '') + path

const strip = value => String(value ?? '').trim()

const toNumber = (value, fallback = 0) => {
	const n = Number(value)
	return Number.isFinite(n) ? n : fallback
}

export async function loadTownRowsFromJson() {
	const res = await fetch(withPrefix('/datas/乡镇.json'))
	if (!res.ok) throw new Error(`加载乡镇.json 失败：${res.status}`)
	const list = await res.json()
	if (!Array.isArray(list)) throw new Error('乡镇.json 格式错误')
	return list
		.map(item => {
			const name = strip(item?.name)
			const county = strip(item?.county)
			const city = strip(item?.city)
			if (!name || !county || !city) return null
			return {
				name,
				county,
				city,
				divisionCode: strip(item.divisionCode || item.code),
				population: toNumber(item.population),
				density: toNumber(item.density),
				statArea: toNumber(item.statArea),
				areaKm2: toNumber(item.areaKm2),
				lon: toNumber(item.lon),
				lat: toNumber(item.lat),
				source: item.source || '乡镇.json',
				fromSystem: true,
				geojson: item.geojson || ''
			}
		})
		.filter(Boolean)
}
