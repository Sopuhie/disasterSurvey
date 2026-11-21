/**
 * 从 map.ruiduobao.com 拉取乡镇边界，写入 public/datas/乡镇.json 的 geojson 字段，
 * 并生成精简几何索引 public/datas/乡镇_geom_index.json（code -> Feature）。
 *
 * 用法：node scripts/enrich-towns-geojson.js
 */
const fs = require('fs')
const path = require('path')
const https = require('https')

const root = path.resolve(__dirname, '..')
const townsFile = path.join(root, 'public/datas/乡镇.json')
const indexFile = path.join(root, 'public/datas/乡镇_geom_index.json')
const progressFile = path.join(root, 'scripts/_town_geo_progress.json')

const CONCURRENCY = 6
const BASE = 'https://map.ruiduobao.com'

const get = url =>
	new Promise((resolve, reject) => {
		const req = https.get(
			url,
			{
				headers: {
					'User-Agent': 'Mozilla/5.0',
					Referer: 'https://map.ruiduobao.com/'
				},
				timeout: 60000
			},
			res => {
				if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
					res.resume()
					get(new URL(res.headers.location, url).href).then(resolve, reject)
					return
				}
				const chunks = []
				res.on('data', c => chunks.push(c))
				res.on('end', () =>
					resolve({ status: res.statusCode, body: Buffer.concat(chunks).toString('utf8') })
				)
			}
		)
		req.on('error', reject)
		req.setTimeout(60000, () => req.destroy(new Error('timeout')))
	})

const sleep = ms => new Promise(r => setTimeout(r, ms))

const simplifyCoords = (coords, step = 3) => {
	if (!Array.isArray(coords)) return coords
	if (typeof coords[0]?.[0] === 'number') {
		if (coords.length <= 24) return coords
		const out = []
		for (let i = 0; i < coords.length; i += step) out.push(coords[i])
		const first = out[0]
		const last = out[out.length - 1]
		if (!first || !last || first[0] !== last[0] || first[1] !== last[1]) out.push([...first])
		return out
	}
	return coords.map(c => simplifyCoords(c, step))
}

const simplifyGeometry = geometry => {
	if (!geometry) return null
	return {
		...geometry,
		coordinates: simplifyCoords(geometry.coordinates, 3)
	}
}

const centroidOfGeometry = geometry => {
	const push = (pt, acc) => {
		if (Array.isArray(pt) && typeof pt[0] === 'number') {
			acc.sx += pt[0]
			acc.sy += pt[1]
			acc.n += 1
		} else if (Array.isArray(pt)) pt.forEach(p => push(p, acc))
	}
	const acc = { sx: 0, sy: 0, n: 0 }
	push(geometry?.coordinates, acc)
	if (!acc.n) return null
	return { lon: Number((acc.sx / acc.n).toFixed(6)), lat: Number((acc.sy / acc.n).toFixed(6)) }
}

const fetchTownFeature = async code => {
	const metaRes = await get(`${BASE}/getGsonDB?code=${encodeURIComponent(code)}`)
	const meta = JSON.parse(metaRes.body)
	if (meta.status !== 'success' || !meta.filepath) {
		throw new Error(meta?.error?.message || meta?.message || 'export failed')
	}
	const fileRes = await get(`${BASE}${meta.filepath}`)
	if (fileRes.status !== 200) throw new Error(`file ${fileRes.status}`)
	const fc = JSON.parse(fileRes.body)
	const feature = fc?.features?.[0]
	if (!feature?.geometry) throw new Error('empty geometry')
	const geometry = simplifyGeometry(feature.geometry)
	const center = centroidOfGeometry(geometry)
	return {
		type: 'Feature',
		geometry,
		properties: {
			name: feature.properties?.地名 || feature.properties?.name || '',
			code: String(code),
			source: 'map.ruiduobao.com'
		},
		center
	}
}

const runPool = async (items, worker, concurrency) => {
	let idx = 0
	const runners = Array.from({ length: concurrency }, async () => {
		while (idx < items.length) {
			const cur = idx++
			await worker(items[cur], cur)
		}
	})
	await Promise.all(runners)
}

async function main() {
	const towns = JSON.parse(fs.readFileSync(townsFile, 'utf8'))
	let progress = {}
	if (fs.existsSync(progressFile)) {
		try {
			progress = JSON.parse(fs.readFileSync(progressFile, 'utf8'))
		} catch (e) {
			progress = {}
		}
	}
	const index = fs.existsSync(indexFile) ? JSON.parse(fs.readFileSync(indexFile, 'utf8')) : {}

	const pending = towns.filter(t => t.divisionCode && !index[t.divisionCode])
	console.log(`总计 ${towns.length}，已有几何 ${Object.keys(index).length}，待拉取 ${pending.length}`)

	let ok = 0
	let fail = 0
	await runPool(
		pending,
		async town => {
			const code = String(town.divisionCode)
			try {
				const feature = await fetchTownFeature(code)
				index[code] = feature
				progress[code] = 'ok'
				ok += 1
				if (ok % 20 === 0) {
					fs.writeFileSync(indexFile, JSON.stringify(index))
					fs.writeFileSync(progressFile, JSON.stringify(progress))
					console.log(`进度 ok=${ok} fail=${fail} / ${pending.length}`)
				}
			} catch (e) {
				fail += 1
				progress[code] = e.message || 'fail'
				console.warn(`失败 ${code} ${town.name}:`, e.message)
				await sleep(300)
			}
		},
		CONCURRENCY
	)

	fs.writeFileSync(indexFile, JSON.stringify(index))
	fs.writeFileSync(progressFile, JSON.stringify(progress))

	const enriched = towns.map(town => {
		const code = String(town.divisionCode || '')
		const feature = index[code]
		if (!feature) return { ...town, geojson: town.geojson || '' }
		const center = feature.center || {}
		const { center: _c, ...pureFeature } = feature
		return {
			...town,
			lon: center.lon || town.lon,
			lat: center.lat || town.lat,
			geojson: JSON.stringify(pureFeature),
			hasGeometry: true
		}
	})
	fs.writeFileSync(townsFile, JSON.stringify(enriched))
	const withGeo = enriched.filter(t => t.geojson).length
	console.log(`完成：写入 ${townsFile}，含几何 ${withGeo}/${enriched.length}；索引 ${indexFile}`)
}

main().catch(err => {
	console.error(err)
	process.exit(1)
})
