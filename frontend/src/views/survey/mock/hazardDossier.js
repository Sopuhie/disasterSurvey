/**
 * 调查对象详情台账（现场资料 / 附件 / 核对 / 归档）
 * 按承灾体名称种子化演示数据，本地修改写入 localStorage。
 */

const STORAGE_KEY = 'survey_hazard_dossier_v1'

const MATERIAL_TEMPLATES = {
	seawall: [
		{ category: '现场踏勘', title: '堤顶与迎潮面损毁踏勘', summary: '记录越浪痕迹、护面块体位移及堤顶道路中断情况。' },
		{ category: '影像对照', title: '灾前灾后遥感对照说明', summary: '对照灾前影像圈定冲刷坑与漫堤区段。' },
		{ category: '设施核查', title: '排水闸与照明设施核查', summary: '核验闸门启闭、堤顶照明及应急封堵落实情况。' }
	],
	fishing: [
		{ category: '现场踏勘', title: '码头与系泊设施核查', summary: '记录面板冲刷、系缆桩偏位及港池振荡影响。' },
		{ category: '设施核查', title: '供电供水设施检查', summary: '排查配电箱进水、供水管线破损情况。' },
		{ category: '影像对照', title: '港池范围遥感复核', summary: '复核港池淤积与防波堤内侧淘空范围。' }
	],
	mariculture: [
		{ category: '现场踏勘', title: '养殖单元损毁清点', summary: '统计网箱走锚、筏架损毁及苗种流失情况。' },
		{ category: '设施核查', title: '岸基看护设施核查', summary: '检查看护房进水、供电中断与应急物资。' },
		{ category: '影像对照', title: '养殖区淹没范围复核', summary: '对照遥感解译结果确认淹没与漂散范围。' }
	],
	tourist: [
		{ category: '现场踏勘', title: '亲水平台与步道核查', summary: '记录平台损坏、护栏冲毁与沙滩流失情况。' },
		{ category: '设施核查', title: '游客服务设施检查', summary: '核验服务中心渗水、临时封闭与疏散落实。' },
		{ category: '影像对照', title: '岸线蚀退对照说明', summary: '对照高潮线内移与观景带受损范围。' }
	]
}

const ATTACH_TEMPLATES = [
	{ name: '现场照片.zip', type: '压缩包', size: '12.6 MB' },
	{ name: '踏勘记录表.xlsx', type: '表格', size: '268 KB' },
	{ name: '灾损示意图.pdf', type: '文档', size: '1.4 MB' },
	{ name: '核对意见.docx', type: '文档', size: '96 KB' }
]

const hashText = text => {
	let h = 0
	const s = String(text || '')
	for (let i = 0; i < s.length; i += 1) h = (h * 31 + s.charCodeAt(i)) >>> 0
	return h
}

const uid = prefix => `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`

const clone = data => JSON.parse(JSON.stringify(data))

const readAll = () => {
	try {
		const raw = localStorage.getItem(STORAGE_KEY)
		if (raw) {
			const parsed = JSON.parse(raw)
			if (parsed && typeof parsed === 'object') return parsed
		}
	} catch (e) {
		/* ignore */
	}
	return {}
}

const writeAll = store => {
	localStorage.setItem(STORAGE_KEY, JSON.stringify(store))
}

const dossierKey = (typeId, name) => `${typeId}::${name}`

const seedMaterials = (typeId, name) => {
	const templates = MATERIAL_TEMPLATES[typeId] || MATERIAL_TEMPLATES.seawall
	const seed = hashText(`${typeId}-${name}`)
	return templates.map((item, index) => ({
		id: `mat_${seed}_${index}`,
		title: `${name}${item.title}`,
		category: item.category,
		collectTime: `2024-09-${16 + (seed % 3)} ${10 + index}:30`,
		collector: ['陈伟', '林晓', '黄敏', '郑洁'][(seed + index) % 4],
		location: name,
		summary: item.summary,
		verified: (seed + index) % 3 !== 0
	}))
}

const seedAttachments = (typeId, name) => {
	const seed = hashText(`${typeId}-${name}-att`)
	return ATTACH_TEMPLATES.map((item, index) => ({
		id: `att_${seed}_${index}`,
		name: `${name}-${item.name}`,
		type: item.type,
		size: item.size,
		uploadTime: `2024-09-${17 + (seed % 2)} ${14 + index}:05`,
		uploader: ['现场调查组', '区县应急办', '海洋局核查组'][(seed + index) % 3],
		remark: index === 0 ? '含现场正射照片与标注图' : '',
		verified: (seed + index) % 2 === 0
	}))
}

export const buildProfile = hazard => {
	if (!hazard) return {}
	const {
		name,
		dangerName,
		type,
		division,
		town,
		lon,
		lat,
		length,
		lengthKm,
		area,
		areaKm2,
		HDXL,
		Return_per,
		Dam_elevat,
		time,
		method,
		host,
		Management,
		manager,
		status,
		monitorValue,
		planValue,
		disasterLevel,
		disasterLevelLabel,
		affectedPopulation,
		economicLoss,
		standard,
		builtYear,
		grade,
		level,
		berths,
		capacity,
		species,
		面积
	} = hazard
	return {
		name: name || dangerName || '',
		dangerName: dangerName || name || '',
		type: type || '',
		division: division || '',
		town: town || '',
		lon,
		lat,
		length: length ?? lengthKm,
		lengthKm: lengthKm ?? length,
		area: area ?? areaKm2 ?? 面积,
		areaKm2: areaKm2 ?? area,
		HDXL: HDXL || '',
		Return_per: Return_per || '',
		Dam_elevat: Dam_elevat || '',
		time: time || '',
		method: method || '',
		host: host || '',
		Management: Management || '',
		manager: manager || host || Management || '',
		status: status || '',
		monitorValue,
		planValue,
		disasterLevel,
		disasterLevelLabel,
		affectedPopulation,
		economicLoss,
		standard: standard || '',
		builtYear,
		grade: grade || '',
		level: level || '',
		berths,
		capacity: capacity || '',
		species: species || '',
		面积: 面积 || ''
	}
}

export function createHazardDossier(typeId, hazard, extras = {}) {
	const name = hazard?.name || hazard?.dangerName || ''
	const profile = buildProfile(hazard)
	return {
		id: uid('dossier'),
		hazardType: typeId,
		hazardName: name,
		profile,
		surveySummary: {
			monitorValue: profile.monitorValue,
			planValue: profile.planValue,
			disasterLevel: profile.disasterLevel,
			disasterLevelLabel: profile.disasterLevelLabel,
			affectedPopulation: profile.affectedPopulation,
			economicLoss: profile.economicLoss,
			overlayNote: extras.overlayNote || '',
			compareNote: extras.compareNote || '',
			updatedAt: extras.updatedAt || ''
		},
		materials: seedMaterials(typeId, name),
		attachments: seedAttachments(typeId, name),
		verifyStatus: '待核对',
		verifyRemark: '',
		verifiedBy: '',
		verifiedAt: '',
		archiveStatus: '未归档',
		archiveNo: '',
		archivedBy: '',
		archivedAt: '',
		archiveRemark: '',
		createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
		updatedAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
	}
}

export const hazardDossierStore = {
	get(typeId, name) {
		const store = readAll()
		const key = dossierKey(typeId, name)
		return store[key] ? clone(store[key]) : null
	},
	save(dossier) {
		const store = readAll()
		const key = dossierKey(dossier.hazardType, dossier.hazardName)
		const next = {
			...dossier,
			updatedAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
		}
		store[key] = next
		writeAll(store)
		return clone(next)
	},
	ensure(typeId, hazard, extras = {}) {
		const name = hazard?.name || hazard?.dangerName || ''
		if (!typeId || !name) return null
		const existed = this.get(typeId, name)
		if (existed) {
			// 同步最新监测/属性快照，保留核对归档态
			const profile = buildProfile(hazard)
			return this.save({
				...existed,
				profile: { ...existed.profile, ...profile },
				surveySummary: {
					...existed.surveySummary,
					monitorValue: profile.monitorValue ?? existed.surveySummary?.monitorValue,
					planValue: profile.planValue ?? existed.surveySummary?.planValue,
					disasterLevel: profile.disasterLevel ?? existed.surveySummary?.disasterLevel,
					disasterLevelLabel: profile.disasterLevelLabel ?? existed.surveySummary?.disasterLevelLabel,
					affectedPopulation: profile.affectedPopulation ?? existed.surveySummary?.affectedPopulation,
					economicLoss: profile.economicLoss ?? existed.surveySummary?.economicLoss,
					overlayNote: extras.overlayNote || existed.surveySummary?.overlayNote || '',
					compareNote: extras.compareNote || existed.surveySummary?.compareNote || ''
				}
			})
		}
		return this.save(createHazardDossier(typeId, hazard, extras))
	},
	listArchived(keyword = '') {
		const store = readAll()
		const text = String(keyword || '').trim()
		return Object.values(store)
			.filter(item => item.archiveStatus === '已归档')
			.filter(item => {
				if (!text) return true
				const blob = [item.hazardName, item.archiveNo, item.profile?.division, item.verifyStatus].join('|')
				return blob.includes(text)
			})
			.sort((a, b) => String(b.archivedAt || '').localeCompare(String(a.archivedAt || '')))
			.map(clone)
	},
	/** 后端不可用时：本地落库（文件转 dataURL，便于预览） */
	async uploadLocal({ typeId, hazardName, kind, file, meta = {} }) {
		if (!typeId || !hazardName || !file) return null
		let dossier = this.get(typeId, hazardName)
		if (!dossier) {
			dossier = createHazardDossier(typeId, { name: hazardName })
		}
		const dataUrl = await readFileAsDataUrl(file)
		const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
		const sizeLabel = formatFileSize(file.size)
		const suffix = (file.name.split('.').pop() || '').toLowerCase()
		if (kind === 'attachment') {
			const item = {
				id: uid('att'),
				name: file.name,
				type: guessAttachType(suffix),
				size: sizeLabel,
				uploadTime: now,
				uploader: meta.uploader || '本地上传',
				remark: meta.remark || '',
				verified: false,
				fileName: file.name,
				fileType: suffix,
				fileSize: file.size,
				filePath: '',
				fileUrl: dataUrl,
				localOnly: true
			}
			dossier.attachments = [item, ...(dossier.attachments || [])]
		} else {
			const item = {
				id: uid('mat'),
				title: meta.title || file.name.replace(/\.[^.]+$/, ''),
				category: meta.category || '现场踏勘',
				collectTime: now,
				collector: meta.collector || '本地上传',
				location: meta.location || hazardName,
				summary: meta.summary || '',
				verified: false,
				fileName: file.name,
				fileType: suffix,
				fileSize: file.size,
				size: sizeLabel,
				filePath: '',
				fileUrl: dataUrl,
				localOnly: true
			}
			dossier.materials = [item, ...(dossier.materials || [])]
		}
		return this.save(dossier)
	},
	removeItem(typeId, hazardName, kind, itemId) {
		const dossier = this.get(typeId, hazardName)
		if (!dossier || !itemId) return null
		const key = kind === 'attachment' ? 'attachments' : 'materials'
		dossier[key] = (dossier[key] || []).filter(item => item.id !== itemId)
		return this.save(dossier)
	}
}

const readFileAsDataUrl = file =>
	new Promise((resolve, reject) => {
		const reader = new FileReader()
		reader.onload = () => resolve(reader.result)
		reader.onerror = () => reject(new Error('读取文件失败'))
		reader.readAsDataURL(file)
	})

const formatFileSize = size => {
	const n = Number(size) || 0
	if (n < 1024) return `${n} B`
	if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`
	return `${(n / 1024 / 1024).toFixed(2)} MB`
}

const guessAttachType = suffix => {
	const s = String(suffix || '').toLowerCase()
	if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(s)) return '图片'
	if (['zip', 'rar', '7z'].includes(s)) return '压缩包'
	if (['xls', 'xlsx', 'csv'].includes(s)) return '表格'
	if (['doc', 'docx', 'pdf', 'txt'].includes(s)) return '文档'
	return s ? s.toUpperCase() : '文件'
}

export const VERIFY_STATUS_OPTIONS = [
	{ label: '待核对', value: '待核对' },
	{ label: '已核对', value: '已核对' },
	{ label: '存疑', value: '存疑' }
]

export const ARCHIVE_STATUS_OPTIONS = [
	{ label: '未归档', value: '未归档' },
	{ label: '已归档', value: '已归档' }
]
