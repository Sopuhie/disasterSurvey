/**
 * 从福建省民政厅行政区划名录文本生成 public/datas/乡镇.json
 * 用法：node scripts/build-fujian-towns.js [名录文本路径]
 *
 * 默认名录可先用浏览器打开并另存，或传入抓取结果路径：
 * https://mzt.fujian.gov.cn/gk/tzgg/202507/t20250718_6968735.htm
 */
const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')
const input =
	process.argv[2] ||
	path.join(
		process.env.USERPROFILE || '',
		'.cursor/projects/e-projects-disaster-survey/agent-tools/7fce671f-7f99-42fa-ae21-0f8cd316ab2f.txt'
	)
const outFile = path.join(root, 'public/datas/乡镇.json')
const countyFile = path.join(root, 'public/datas/县界.json')

const raw = fs.readFileSync(input, 'utf8')
const lines = raw
	.split(/\r?\n/)
	.map(line => line.replace(/^[\s>]+/, '').trim())
	.filter(Boolean)
const start = lines.findIndex(line => line === '福建省')
if (start < 0) throw new Error('未找到「福建省」起点')
const body = lines.slice(start)

const pairs = []
for (let i = 0; i < body.length - 1; i++) {
	const name = body[i]
	const code = body[i + 1]
	if (/^\d{6,12}$/.test(code) && !/^\d+$/.test(name)) {
		pairs.push({ name, code })
		i++
	}
}

const cities = new Map()
const counties = new Map()
let curCity = null
let curCounty = null
const towns = []

for (const { name, code } of pairs) {
	if (code.length === 6) {
		if (code.endsWith('00')) {
			if (code.endsWith('0000')) continue
			curCity = { name, code }
			cities.set(code, name)
			curCounty = null
		} else {
			curCounty = { name, code }
			counties.set(code, {
				name,
				city: cities.get(`${code.slice(0, 4)}00`) || curCity?.name || ''
			})
		}
		continue
	}
	if (code.length >= 9) {
		const countyCode = code.slice(0, 6)
		const cityCode = `${code.slice(0, 4)}00`
		towns.push({
			name,
			code,
			county: counties.get(countyCode)?.name || curCounty?.name || '',
			city: counties.get(countyCode)?.city || cities.get(cityCode) || curCity?.name || ''
		})
	}
}

const centroid = rings => {
	let sx = 0
	let sy = 0
	let n = 0
	const ring = (rings && rings[0]) || []
	for (const pt of ring) {
		if (Array.isArray(pt) && pt.length >= 2) {
			sx += Number(pt[0]) || 0
			sy += Number(pt[1]) || 0
			n += 1
		}
	}
	return n ? { lon: sx / n, lat: sy / n } : { lon: 119.3, lat: 26.08 }
}

const centerByName = new Map()
try {
	const countyJson = JSON.parse(fs.readFileSync(countyFile, 'utf8'))
	for (const feature of countyJson.features || []) {
		const attrs = feature.attributes || {}
		const nm = String(attrs.地名 || attrs.县级 || '').trim()
		const geom = feature.geometry || {}
		let lon = 119.3
		let lat = 26.08
		if (geom.x != null) {
			lon = Number(geom.x)
			lat = Number(geom.y)
		} else if (geom.rings) {
			const c = centroid(geom.rings)
			lon = c.lon
			lat = c.lat
		}
		if (nm) centerByName.set(nm, { lon, lat })
	}
} catch (e) {
	console.warn('县界中心点回填失败，将使用默认坐标', e.message)
}

const matchCenter = county => {
	if (centerByName.has(county)) return centerByName.get(county)
	for (const [key, value] of centerByName) {
		if (key.includes(county) || county.includes(key)) return value
		const a = key.replace(/[市区县]$/, '')
		const b = county.replace(/[市区县]$/, '')
		if (a && b && a === b) return value
	}
	return { lon: 119.3, lat: 26.08 }
}

const rows = towns.map((item, index) => {
	const center = matchCenter(item.county)
	const jitter = ((index % 17) - 8) * 0.004
	const jitter2 = ((index % 13) - 6) * 0.004
	return {
		name: item.name,
		county: item.county,
		city: item.city,
		divisionCode: item.code,
		population: 0,
		density: 0,
		statArea: 0,
		areaKm2: 0,
		lon: Number((center.lon + jitter).toFixed(6)),
		lat: Number((center.lat + jitter2).toFixed(6)),
		source: '福建省民政厅行政区划名录(2025-06-30)',
		fromSystem: true
	}
})

fs.writeFileSync(outFile, JSON.stringify(rows))
console.log(`已生成 ${rows.length} 条乡镇 -> ${outFile}`)
