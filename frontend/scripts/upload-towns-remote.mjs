/**
 * 将 public/datas/乡镇.json 分批写入后端 bizdata/batchSave
 * 用法: node scripts/upload-towns-remote.mjs <access_token>
 */
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const ROOT = path.resolve(__dirname, '..')
const BASE = 'http://fzsqi.com:10140/zuul'
const API = `${BASE}/web-disaster-evaluate/web/disaster/evaluate/bizdata/batchSave`
const CHUNK = 15

const token = process.argv[2] || process.env.ACCESS_TOKEN || ''
if (!token) {
	console.error('缺少 access_token：node scripts/upload-towns-remote.mjs <token>')
	process.exit(1)
}

const raw = JSON.parse(fs.readFileSync(path.join(ROOT, 'public/datas/乡镇.json'), 'utf8'))
const rows = raw.map(item => ({
	name: item.name,
	county: item.county,
	city: item.city,
	divisionCode: item.divisionCode || item.code || '',
	population: Number(item.population) || 0,
	density: Number(item.density) || 0,
	statArea: Number(item.statArea) || 0,
	areaKm2: Number(item.areaKm2) || 0,
	lon: Number(item.lon) || 0,
	lat: Number(item.lat) || 0,
	source: item.source || '乡镇.json',
	fromSystem: true,
	geojson: item.geojson || ''
}))

console.log(`准备上传 ${rows.length} 条，每批 ${CHUNK}`)

let success = 0
let fail = 0
const totalBatches = Math.ceil(rows.length / CHUNK)

for (let i = 0; i < rows.length; i += CHUNK) {
	const list = rows.slice(i, i + CHUNK)
	const batchNo = Math.floor(i / CHUNK) + 1
	try {
		const res = await fetch(API, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json;charset=UTF-8',
				access_token: token,
				roles: 'rl0',
				peopleId: 'U001',
				username: 'NTocean_disaster'
			},
			body: JSON.stringify({ dataType: 'town', list })
		})
		const text = await res.text()
		let json
		try {
			json = JSON.parse(text)
		} catch {
			throw new Error(`HTTP ${res.status} 非JSON: ${text.slice(0, 180)}`)
		}
		if (json.resultCode && json.resultCode !== 'S') {
			throw new Error(json.resultMsg || JSON.stringify(json).slice(0, 200))
		}
		const data = json.data || {}
		const ok = Number(data.success ?? list.length)
		const bad = Number(data.fail ?? 0)
		success += ok
		fail += bad
		console.log(`[${batchNo}/${totalBatches}] ok=${ok} fail=${bad}`)
	} catch (e) {
		fail += list.length
		console.error(`[${batchNo}/${totalBatches}] 失败: ${e.message || e}`)
	}
}

console.log(`完成：成功 ${success}，失败 ${fail}`)
process.exit(fail ? 1 : 0)
