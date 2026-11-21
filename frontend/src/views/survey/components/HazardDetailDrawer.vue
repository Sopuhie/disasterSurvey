<!-- 调查对象详情：明细 / 现场资料 / 附件 / 核对归档 -->
<template>
	<el-drawer
		v-model="visible"
		class="hazard-detail-drawer survey-base-drawer"
		:title="drawerTitle"
		direction="rtl"
		size="720px"
		:z-index="3300"
		append-to-body
		destroy-on-close
		@closed="onClosed">
		<div v-loading="loading" class="hdd-body">
			<template v-if="dossier">
				<div class="hdd-status">
					<el-tag :type="verifyTagType" size="small">核对：{{ dossier.verifyStatus }}</el-tag>
					<el-tag :type="archiveTagType" size="small" class="ml-8">归档：{{ dossier.archiveStatus }}</el-tag>
					<span v-if="dossier.archiveNo" class="hdd-archive-no">底账编号 {{ dossier.archiveNo }}</span>
				</div>

				<el-tabs v-model="activeTab" class="hdd-tabs">
					<el-tab-pane label="调查明细" name="detail">
						<section class="hdd-section">
							<div class="wb-title"><span class="txt">对象基础信息</span></div>
							<div class="wb-kv">
								<p v-for="row in profileRows" :key="row.label">
									<span>{{ row.label }}</span>{{ row.value }}
								</p>
							</div>
						</section>
						<section class="hdd-section">
							<div class="wb-title"><span class="txt">调查成果摘要</span></div>
							<div class="wb-kpis">
								<div class="wb-kpi">
									<span>{{ summaryMonitorLabel }}</span>
									<b>
										{{ formatNum(dossier.surveySummary?.monitorValue) }}
										<small>{{ summaryUnit }}</small>
									</b>
								</div>
								<div class="wb-kpi green">
									<span>{{ summaryPlanLabel }}</span>
									<b>
										{{ formatNum(dossier.surveySummary?.planValue) }}
										<small>{{ summaryUnit }}</small>
									</b>
								</div>
								<div class="wb-kpi orange">
									<span>{{ SURVEY_LABELS.disasterLevel }}</span>
									<b>{{ dossier.surveySummary?.disasterLevelLabel || formatNum(dossier.surveySummary?.disasterLevel) }}</b>
								</div>
								<div class="wb-kpi purple">
									<span>{{ SURVEY_KPI_NAMES.affectedPopulation }}</span>
									<b>{{ formatNum(dossier.surveySummary?.affectedPopulation) }} <small>人</small></b>
								</div>
								<div class="wb-kpi red">
									<span>{{ SURVEY_KPI_NAMES.economicLoss }}</span>
									<b>{{ formatNum(dossier.surveySummary?.economicLoss) }} <small>万元</small></b>
								</div>
							</div>
							<div v-if="dossier.surveySummary?.overlayNote || dossier.surveySummary?.compareNote" class="hdd-notes">
								<p v-if="dossier.surveySummary.overlayNote">
									<strong>叠置说明：</strong>{{ dossier.surveySummary.overlayNote }}
								</p>
								<p v-if="dossier.surveySummary.compareNote">
									<strong>对比说明：</strong>{{ dossier.surveySummary.compareNote }}
								</p>
							</div>
						</section>
					</el-tab-pane>

					<el-tab-pane label="现场资料" name="materials">
						<div class="hdd-toolbar">
							<el-input
								v-model="materialKeyword"
								clearable
								placeholder="按标题/摘要/采集人查询"
								style="width: 200px"
								@keyup.enter="filterMaterials" />
							<el-select
								v-model="materialCategory"
								clearable
								placeholder="资料类别"
								style="width: 130px"
								@change="filterMaterials">
								<el-option v-for="opt in materialCategories" :key="opt" :label="opt" :value="opt" />
							</el-select>
							<el-button type="primary" plain @click="filterMaterials">查询</el-button>
							<el-button type="primary" @click="openMaterialUpload">上传资料</el-button>
						</div>
						<div class="hdd-list">
							<div v-for="item in materials" :key="item.id" class="hdd-card">
								<div class="hdd-card__head">
									<div>
										<p class="hdd-card__title">{{ item.title }}</p>
										<p class="hdd-card__meta">
											{{ item.category }} · {{ item.collectTime }} · {{ item.collector }}
											<span v-if="item.fileName"> · {{ item.fileName }}</span>
										</p>
									</div>
									<el-tag :type="item.verified ? 'success' : 'info'" size="small">
										{{ item.verified ? '已核' : '待核' }}
									</el-tag>
								</div>
								<p class="hdd-card__summary">{{ item.summary }}</p>
								<p class="hdd-card__meta">地点：{{ item.location || '-' }}</p>
								<div class="hdd-card__verify">
									<el-button type="primary" link @click="toggleMaterial(item, !item.verified)">
										{{ item.verified ? '取消核对' : '核对通过' }}
									</el-button>
								</div>
								<div class="hdd-card__actions">
									<el-button v-if="canPreview(item)" type="primary" link @click="viewFile(item)">
										查看
									</el-button>
									<el-button v-if="canPreview(item)" type="primary" link @click="downloadFile(item)">
										下载
									</el-button>
									<el-button type="danger" link @click="removeFile(item, 'material')">删除</el-button>
								</div>
							</div>
							<el-empty v-if="!materials.length" description="暂无现场资料，可点击上传" :image-size="72" />
						</div>
					</el-tab-pane>

					<el-tab-pane label="附件" name="attachments">
						<div class="hdd-toolbar">
							<el-input
								v-model="attachKeyword"
								clearable
								placeholder="按附件名/上传人查询"
								style="width: 220px"
								@keyup.enter="filterAttachments" />
							<el-button type="primary" plain @click="filterAttachments">查询</el-button>
							<el-button type="primary" @click="openAttachUpload">上传附件</el-button>
						</div>
						<div class="hdd-list">
							<div v-for="item in attachments" :key="item.id" class="hdd-card">
								<div class="hdd-card__head">
									<div>
										<p class="hdd-card__title">{{ item.name }}</p>
										<p class="hdd-card__meta">
											{{ item.type }} · {{ item.size }} · {{ item.uploadTime }} · {{ item.uploader }}
										</p>
									</div>
									<el-tag :type="item.verified ? 'success' : 'info'" size="small">
										{{ item.verified ? '已核' : '待核' }}
									</el-tag>
								</div>
								<p v-if="item.remark" class="hdd-card__summary">{{ item.remark }}</p>
								<div class="hdd-card__verify">
									<el-button type="primary" link @click="toggleAttachment(item, !item.verified)">
										{{ item.verified ? '取消核对' : '核对通过' }}
									</el-button>
								</div>
								<div class="hdd-card__actions">
									<el-button v-if="canPreview(item)" type="primary" link @click="viewFile(item)">
										查看
									</el-button>
									<el-button v-if="canPreview(item)" type="primary" link @click="downloadFile(item)">
										下载
									</el-button>
									<el-button type="danger" link @click="removeFile(item, 'attachment')">删除</el-button>
								</div>
							</div>
							<el-empty v-if="!attachments.length" description="暂无附件，可点击上传" :image-size="72" />
						</div>
					</el-tab-pane>

					<el-tab-pane label="核对归档" name="archive">
						<section class="hdd-section">
							<div class="wb-title"><span class="txt">资料核对</span></div>
							<p class="hdd-hint">
								现场资料已核 {{ materialVerifiedCount }}/{{ dossier.materials?.length || 0 }}，
								附件已核 {{ attachmentVerifiedCount }}/{{ dossier.attachments?.length || 0 }}
							</p>
							<el-form label-width="96px" class="hdd-form">
								<el-form-item label="核对状态">
									<el-select v-model="verifyForm.verifyStatus" style="width: 100%">
										<el-option
											v-for="opt in verifyOptions"
											:key="opt.value"
											:label="opt.label"
											:value="opt.value" />
									</el-select>
								</el-form-item>
								<el-form-item label="核对人">
									<el-input v-model="verifyForm.verifiedBy" placeholder="请输入核对人" />
								</el-form-item>
								<el-form-item label="核对意见">
									<el-input
										v-model="verifyForm.verifyRemark"
										type="textarea"
										:rows="3"
										placeholder="记录核查结论、疑点与责任说明" />
								</el-form-item>
								<el-form-item>
									<el-button type="primary" :loading="saving" @click="submitVerify">保存核对结果</el-button>
								</el-form-item>
							</el-form>
							<div v-if="dossier.verifiedAt" class="hdd-foot-meta">
								上次核对：{{ dossier.verifiedBy || '-' }} · {{ dossier.verifiedAt }}
								<span v-if="dossier.verifyRemark"> · {{ dossier.verifyRemark }}</span>
							</div>
						</section>

						<section class="hdd-section">
							<div class="wb-title"><span class="txt">成果归档</span></div>
							<p class="hdd-hint">核对通过或标注存疑后，可将本对象调查成果写入底账，供核查与交接追溯。</p>
							<el-form label-width="96px" class="hdd-form">
								<el-form-item label="归档说明">
									<el-input
										v-model="archiveForm.archiveRemark"
										type="textarea"
										:rows="3"
										placeholder="归档范围、交接对象、后续责任等" />
								</el-form-item>
								<el-form-item label="归档人">
									<el-input v-model="archiveForm.archivedBy" placeholder="请输入归档人" />
								</el-form-item>
								<el-form-item>
									<el-button
										type="warning"
										:loading="archiving"
										:disabled="dossier.archiveStatus === '已归档'"
										@click="submitArchive">
										{{ dossier.archiveStatus === '已归档' ? '已归档' : '确认归档留底' }}
									</el-button>
								</el-form-item>
							</el-form>
							<div v-if="dossier.archivedAt" class="hdd-foot-meta">
								归档记录：{{ dossier.archiveNo }} · {{ dossier.archivedBy || '-' }} · {{ dossier.archivedAt }}
								<span v-if="dossier.archiveRemark"> · {{ dossier.archiveRemark }}</span>
							</div>
						</section>
					</el-tab-pane>
				</el-tabs>
			</template>
			<el-empty v-else-if="!loading" description="暂无对象详情" :image-size="84" />
		</div>
	</el-drawer>

	<el-dialog v-model="materialUploadVisible" title="上传现场资料" width="520px" append-to-body destroy-on-close>
		<el-form label-width="88px">
			<el-form-item label="资料标题" required>
				<el-input v-model="materialForm.title" placeholder="如：堤顶损毁踏勘" />
			</el-form-item>
			<el-form-item label="资料类别">
				<el-select v-model="materialForm.category" style="width: 100%" allow-create filterable>
					<el-option v-for="opt in materialCategoryOptions" :key="opt" :label="opt" :value="opt" />
				</el-select>
			</el-form-item>
			<el-form-item label="采集人">
				<el-input v-model="materialForm.collector" placeholder="采集人" />
			</el-form-item>
			<el-form-item label="地点">
				<el-input v-model="materialForm.location" placeholder="采集地点" />
			</el-form-item>
			<el-form-item label="摘要">
				<el-input v-model="materialForm.summary" type="textarea" :rows="3" placeholder="资料说明" />
			</el-form-item>
			<el-form-item label="文件" required>
				<input type="file" @change="onMaterialFileChange" />
				<p v-if="materialForm.file" class="hdd-file-name">{{ materialForm.file.name }}</p>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button @click="materialUploadVisible = false">取消</el-button>
			<el-button type="primary" :loading="uploading" @click="submitMaterialUpload">上传</el-button>
		</template>
	</el-dialog>

	<el-dialog v-model="attachUploadVisible" title="上传附件" width="480px" append-to-body destroy-on-close>
		<el-form label-width="88px">
			<el-form-item label="上传人">
				<el-input v-model="attachForm.uploader" placeholder="上传人" />
			</el-form-item>
			<el-form-item label="备注">
				<el-input v-model="attachForm.remark" type="textarea" :rows="2" placeholder="附件说明" />
			</el-form-item>
			<el-form-item label="文件" required>
				<input type="file" @change="onAttachFileChange" />
				<p v-if="attachForm.file" class="hdd-file-name">{{ attachForm.file.name }}</p>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button @click="attachUploadVisible = false">取消</el-button>
			<el-button type="primary" :loading="uploading" @click="submitAttachUpload">上传</el-button>
		</template>
	</el-dialog>

	<el-dialog v-model="previewVisible" :title="previewTitle" width="720px" append-to-body destroy-on-close @closed="onPreviewClosed">
		<div class="hdd-preview">
			<img v-if="previewIsImage" :src="previewUrl" alt="preview" />
			<iframe v-else-if="previewIsPdf" :src="previewUrl" title="pdf-preview" />
			<p v-else class="hdd-hint">该文件类型请下载后查看。</p>
		</div>
		<template #footer>
			<el-button type="primary" @click="downloadCurrentPreview">下载</el-button>
			<el-button @click="previewVisible = false">关闭</el-button>
		</template>
	</el-dialog>
</template>

<script setup>
	import { ElMessage, ElMessageBox } from 'element-plus'
	import { surveyApi } from '@/api/survey/index.js'
	import { VERIFY_STATUS_OPTIONS } from '@/views/survey/mock/hazardDossier.js'
	import { HAZARD_TYPES } from '@/views/survey/constants.js'
	import { monitorPlanLabels, SURVEY_KPI_NAMES, SURVEY_LABELS } from '@/shared/metricUnits.js'

	const props = defineProps({
		modelValue: { type: Boolean, default: false },
		typeId: { type: String, default: '' },
		hazard: { type: Object, default: null },
		overlayNote: { type: String, default: '' },
		compareNote: { type: String, default: '' }
	})

	const emit = defineEmits(['update:modelValue', 'archived'])

	const api = surveyApi()
	const visible = computed({
		get: () => props.modelValue,
		set: val => emit('update:modelValue', val)
	})

	const loading = ref(false)
	const saving = ref(false)
	const archiving = ref(false)
	const uploading = ref(false)
	const activeTab = ref('detail')
	const dossier = ref(null)
	const materials = ref([])
	const attachments = ref([])
	const materialKeyword = ref('')
	const materialCategory = ref('')
	const attachKeyword = ref('')
	const materialUploadVisible = ref(false)
	const attachUploadVisible = ref(false)
	const previewVisible = ref(false)
	const previewUrl = ref('')
	const previewTitle = ref('')
	const previewIsImage = ref(false)
	const previewIsPdf = ref(false)
	const previewNeedRevoke = ref(false)
	const previewItem = ref(null)
	const materialForm = reactive({
		title: '',
		category: '现场踏勘',
		collector: '调查核查员',
		location: '',
		summary: '',
		file: null
	})
	const attachForm = reactive({
		uploader: '调查核查员',
		remark: '',
		file: null
	})
	const verifyForm = reactive({
		verifyStatus: '已核对',
		verifiedBy: '调查核查员',
		verifyRemark: ''
	})
	const archiveForm = reactive({
		archiveRemark: '',
		archivedBy: '调查归档员'
	})

	const verifyOptions = VERIFY_STATUS_OPTIONS
	const materialCategoryOptions = ['现场踏勘', '影像对照', '设施核查', '其他']

	const typeLabel = computed(() => HAZARD_TYPES.find(item => item.id === props.typeId)?.label || '承灾体')
	const drawerTitle = computed(() => {
		const name = props.hazard?.name || props.hazard?.dangerName || ''
		return name ? `${typeLabel.value}详情 · ${name}` : `${typeLabel.value}详情`
	})

	const formatNum = value => (value === null || value === undefined || value === '' ? '-' : value)
	const summaryLabels = computed(() => monitorPlanLabels(props.typeId))
	const summaryMonitorLabel = computed(() => summaryLabels.value.monitor)
	const summaryPlanLabel = computed(() => summaryLabels.value.plan)
	const summaryUnit = computed(() => summaryLabels.value.unit)

	const profileRows = computed(() => {
		const p = dossier.value?.profile || {}
		const rows = [
			{ label: '名称', value: p.name || '-' },
			{ label: '类型', value: p.type || typeLabel.value },
			{ label: '行政区划', value: p.division || '-' },
			{ label: '乡镇', value: p.town || '-' },
			{ label: '经度', value: formatNum(p.lon) },
			{ label: '纬度', value: formatNum(p.lat) },
			{ label: '长度(km)', value: formatNum(p.lengthKm || p.length) },
			{ label: '面积', value: formatNum(p.areaKm2 || p.area || p.面积) },
			{ label: '海堤序列', value: p.HDXL || '' },
			{ label: '重现期', value: p.Return_per || p.standard || '' },
			{ label: '堤顶高程', value: p.Dam_elevat || '' },
			{ label: '建成时间', value: p.time || p.builtYear || '' },
			{ label: '养殖方式', value: p.method || '' },
			{ label: '拥有者', value: p.host || '' },
			{ label: '管理单位', value: p.Management || p.manager || '' },
			{ label: '状态', value: p.status || '' }
		]
		return rows.filter(item => item.value !== '' && item.value !== null && item.value !== undefined)
	})

	const materialCategories = computed(() => {
		const set = new Set((dossier.value?.materials || []).map(item => item.category).filter(Boolean))
		return [...set]
	})

	const materialVerifiedCount = computed(
		() => (dossier.value?.materials || []).filter(item => item.verified).length
	)
	const attachmentVerifiedCount = computed(
		() => (dossier.value?.attachments || []).filter(item => item.verified).length
	)

	const verifyTagType = computed(() => {
		const status = dossier.value?.verifyStatus
		if (status === '已核对') return 'success'
		if (status === '存疑') return 'warning'
		return 'info'
	})
	const archiveTagType = computed(() => (dossier.value?.archiveStatus === '已归档' ? 'success' : 'info'))

	const syncForms = () => {
		if (!dossier.value) return
		verifyForm.verifyStatus = dossier.value.verifyStatus === '待核对' ? '已核对' : dossier.value.verifyStatus
		verifyForm.verifiedBy = dossier.value.verifiedBy || '调查核查员'
		verifyForm.verifyRemark = dossier.value.verifyRemark || ''
		archiveForm.archiveRemark = dossier.value.archiveRemark || ''
		archiveForm.archivedBy = dossier.value.archivedBy || dossier.value.verifiedBy || '调查归档员'
	}

	const filterMaterials = async () => {
		if (!props.typeId || !props.hazard?.name) {
			materials.value = []
			return
		}
		materials.value = await api.queryHazardDossierMaterials({
			typeId: props.typeId,
			hazardName: props.hazard.name,
			keyword: materialKeyword.value,
			category: materialCategory.value
		})
	}

	const filterAttachments = async () => {
		if (!props.typeId || !props.hazard?.name) {
			attachments.value = []
			return
		}
		attachments.value = await api.queryHazardDossierAttachments({
			typeId: props.typeId,
			hazardName: props.hazard.name,
			keyword: attachKeyword.value
		})
	}

	const loadDossier = async () => {
		if (!props.typeId || !props.hazard?.name) {
			dossier.value = null
			return
		}
		loading.value = true
		try {
			dossier.value = await api.getHazardDossier({
				typeId: props.typeId,
				hazard: props.hazard,
				overlayNote: props.overlayNote,
				compareNote: props.compareNote
			})
			syncForms()
			await Promise.all([filterMaterials(), filterAttachments()])
		} catch (err) {
			dossier.value = null
			ElMessage.error(err?.message || '加载对象详情失败')
		} finally {
			loading.value = false
		}
	}

	const canPreview = item => !!(item?.fileUrl || item?.filePath)

	const isImageItem = item => {
		const type = String(item?.fileType || item?.type || '').toLowerCase()
		const name = String(item?.fileName || item?.name || '').toLowerCase()
		return (
			['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', '图片'].some(ext => type.includes(ext)) ||
			/\.(jpg|jpeg|png|gif|bmp|webp)$/.test(name)
		)
	}

	const isPdfItem = item => {
		const type = String(item?.fileType || item?.type || '').toLowerCase()
		const name = String(item?.fileName || item?.name || '').toLowerCase()
		return type.includes('pdf') || name.endsWith('.pdf')
	}

	const openMaterialUpload = () => {
		materialForm.title = ''
		materialForm.category = '现场踏勘'
		materialForm.collector = verifyForm.verifiedBy || '调查核查员'
		materialForm.location = props.hazard?.name || ''
		materialForm.summary = ''
		materialForm.file = null
		materialUploadVisible.value = true
	}

	const openAttachUpload = () => {
		attachForm.uploader = verifyForm.verifiedBy || '调查核查员'
		attachForm.remark = ''
		attachForm.file = null
		attachUploadVisible.value = true
	}

	const onMaterialFileChange = e => {
		materialForm.file = e?.target?.files?.[0] || null
		if (materialForm.file && !materialForm.title) {
			materialForm.title = materialForm.file.name.replace(/\.[^.]+$/, '')
		}
	}

	const onAttachFileChange = e => {
		attachForm.file = e?.target?.files?.[0] || null
	}

	const submitMaterialUpload = async () => {
		if (!materialForm.file) {
			ElMessage.warning('请选择文件')
			return
		}
		if (!materialForm.title.trim()) {
			ElMessage.warning('请填写资料标题')
			return
		}
		uploading.value = true
		try {
			dossier.value = await api.uploadHazardDossierFile({
				typeId: props.typeId,
				hazardName: props.hazard.name,
				kind: 'material',
				file: materialForm.file,
				meta: {
					title: materialForm.title.trim(),
					category: materialForm.category,
					collector: materialForm.collector,
					location: materialForm.location,
					summary: materialForm.summary
				}
			})
			materialUploadVisible.value = false
			await filterMaterials()
			ElMessage.success('现场资料已上传')
		} catch (err) {
			ElMessage.error(err?.message || '上传失败')
		} finally {
			uploading.value = false
		}
	}

	const submitAttachUpload = async () => {
		if (!attachForm.file) {
			ElMessage.warning('请选择文件')
			return
		}
		uploading.value = true
		try {
			dossier.value = await api.uploadHazardDossierFile({
				typeId: props.typeId,
				hazardName: props.hazard.name,
				kind: 'attachment',
				file: attachForm.file,
				meta: {
					uploader: attachForm.uploader,
					remark: attachForm.remark
				}
			})
			attachUploadVisible.value = false
			await filterAttachments()
			ElMessage.success('附件已上传')
		} catch (err) {
			ElMessage.error(err?.message || '上传失败')
		} finally {
			uploading.value = false
		}
	}

	const resolveFile = async item => api.openHazardDossierFile(item)

	const viewFile = async item => {
		try {
			const hit = await resolveFile(item)
			previewItem.value = item
			previewTitle.value = hit.name
			previewUrl.value = hit.url
			previewNeedRevoke.value = !!hit.revoke
			previewIsImage.value = isImageItem(item) || String(hit.url).startsWith('data:image')
			previewIsPdf.value = isPdfItem(item)
			if (!previewIsImage.value && !previewIsPdf.value) {
				await downloadFile(item)
				if (hit.revoke) URL.revokeObjectURL(hit.url)
				return
			}
			previewVisible.value = true
		} catch (err) {
			ElMessage.error(err?.message || '无法查看文件')
		}
	}

	const downloadFile = async item => {
		try {
			const hit = await resolveFile(item)
			const a = document.createElement('a')
			a.href = hit.url
			a.download = hit.name || 'download'
			a.target = '_blank'
			a.rel = 'noopener'
			document.body.appendChild(a)
			a.click()
			a.remove()
			if (hit.revoke) {
				setTimeout(() => URL.revokeObjectURL(hit.url), 1500)
			}
		} catch (err) {
			ElMessage.error(err?.message || '下载失败')
		}
	}

	const downloadCurrentPreview = () => {
		if (previewItem.value) downloadFile(previewItem.value)
	}

	const onPreviewClosed = () => {
		if (previewNeedRevoke.value && previewUrl.value) {
			URL.revokeObjectURL(previewUrl.value)
		}
		previewUrl.value = ''
		previewItem.value = null
		previewNeedRevoke.value = false
	}

	const removeFile = async (item, kind) => {
		try {
			await ElMessageBox.confirm(`确认删除「${item.title || item.name}」？`, '删除确认', {
				type: 'warning'
			})
			dossier.value = await api.removeHazardDossierFile({
				typeId: props.typeId,
				hazardName: props.hazard.name,
				kind,
				itemId: item.id
			})
			if (kind === 'attachment') await filterAttachments()
			else await filterMaterials()
			ElMessage.success('已删除')
		} catch (err) {
			if (err === 'cancel' || err === 'close') return
			ElMessage.error(err?.message || '删除失败')
		}
	}

	const toggleMaterial = async (item, verified) => {
		try {
			dossier.value = await api.toggleMaterialVerified({
				typeId: props.typeId,
				hazardName: props.hazard.name,
				materialId: item.id,
				verified
			})
			await filterMaterials()
			ElMessage.success(verified ? '现场资料已核对' : '已取消核对')
		} catch (err) {
			ElMessage.error(err?.message || '操作失败')
		}
	}

	const toggleAttachment = async (item, verified) => {
		try {
			dossier.value = await api.toggleAttachmentVerified({
				typeId: props.typeId,
				hazardName: props.hazard.name,
				attachmentId: item.id,
				verified
			})
			await filterAttachments()
			ElMessage.success(verified ? '附件已核对' : '已取消核对')
		} catch (err) {
			ElMessage.error(err?.message || '操作失败')
		}
	}

	const submitVerify = async () => {
		saving.value = true
		try {
			dossier.value = await api.verifyHazardDossier({
				typeId: props.typeId,
				hazardName: props.hazard.name,
				...verifyForm
			})
			syncForms()
			ElMessage.success('核对结果已保存')
		} catch (err) {
			ElMessage.error(err?.message || '保存失败')
		} finally {
			saving.value = false
		}
	}

	const submitArchive = async () => {
		archiving.value = true
		try {
			dossier.value = await api.archiveHazardDossier({
				typeId: props.typeId,
				hazardName: props.hazard.name,
				...archiveForm
			})
			syncForms()
			ElMessage.success(`已归档留底：${dossier.value.archiveNo}`)
			emit('archived', dossier.value)
		} catch (err) {
			ElMessage.error(err?.message || '归档失败')
		} finally {
			archiving.value = false
		}
	}

	const onClosed = () => {
		activeTab.value = 'detail'
		materialKeyword.value = ''
		materialCategory.value = ''
		attachKeyword.value = ''
	}

	watch(
		() => [props.modelValue, props.typeId, props.hazard?.name],
		([open]) => {
			if (open) loadDossier()
		},
		{ immediate: true }
	)
</script>

<style lang="scss" scoped>
	.hdd-body {
		height: 100%;
		overflow: auto;
		padding-right: 4px;
	}
	.hdd-status {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		gap: 4px;
		margin-bottom: 12px;
	}
	.ml-8 {
		margin-left: 8px;
	}
	.hdd-archive-no {
		margin-left: 8px;
		font-size: 12px;
		color: #8a97a8;
	}
	.hdd-tabs {
		:deep(.el-tabs__header) {
			margin-bottom: 12px;
		}
	}
	.hdd-section {
		margin-bottom: 18px;
	}
	.hdd-toolbar {
		display: flex;
		flex-wrap: wrap;
		gap: 8px;
		margin-bottom: 12px;
	}
	.hdd-list {
		display: flex;
		flex-direction: column;
		gap: 10px;
	}
	.hdd-card {
		padding: 12px 14px;
		border: 1px solid #e6edf5;
		border-radius: 8px;
		background: #f8fbfe;
		&__head {
			display: flex;
			justify-content: space-between;
			gap: 12px;
			align-items: flex-start;
		}
		&__title {
			margin: 0;
			font-size: 14px;
			font-weight: 600;
			color: #1a3a5c;
		}
		&__meta {
			margin: 4px 0 0;
			font-size: 12px;
			color: #8a97a8;
		}
		&__summary {
			margin: 8px 0 0;
			font-size: 13px;
			color: #3d4a5c;
			line-height: 1.5;
		}
		&__verify {
			margin-top: 6px;
		}
		&__actions {
			display: flex;
			align-items: center;
			justify-content: flex-end;
			gap: 4px;
			margin-top: 10px;
			padding-top: 8px;
			border-top: 1px solid #edf2f8;
		}
	}
	.hdd-hint {
		margin: 0 0 12px;
		font-size: 12px;
		color: #8a97a8;
		line-height: 1.5;
	}
	.hdd-notes {
		margin-top: 12px;
		padding: 10px 12px;
		border-radius: 8px;
		background: #f4f7fb;
		font-size: 13px;
		color: #3d4a5c;
		line-height: 1.6;
		p {
			margin: 0;
			& + p {
				margin-top: 6px;
			}
		}
	}
	.hdd-form {
		max-width: 520px;
	}
	.hdd-foot-meta {
		margin-top: 8px;
		font-size: 12px;
		color: #8a97a8;
		line-height: 1.5;
	}
	.hdd-file-name {
		margin: 6px 0 0;
		font-size: 12px;
		color: #5c6b7a;
		word-break: break-all;
	}
	.hdd-preview {
		min-height: 240px;
		display: flex;
		align-items: center;
		justify-content: center;
		background: #f6f9fd;
		border-radius: 8px;
		overflow: auto;
		img {
			max-width: 100%;
			max-height: 60vh;
			object-fit: contain;
		}
		iframe {
			width: 100%;
			height: 60vh;
			border: 0;
		}
	}
</style>

<style lang="scss">
	.hazard-detail-drawer.el-drawer {
		z-index: 3300 !important;
		.el-drawer__header {
			margin-bottom: 0;
			padding: 0 16px;
			height: 56px;
			box-sizing: border-box;
			background-color: #bce4ff;
			background-image: url('@/assets/imgs/headBg.png');
			background-repeat: no-repeat;
			background-size: auto 100%;
			background-position: right center;
			.el-drawer__title {
				font-size: 16px;
				font-weight: 700;
				color: #1a3a5c;
				line-height: 56px;
			}
			.el-drawer__close-btn {
				color: #1a3a5c;
			}
		}
		.el-drawer__body {
			padding: 12px 16px;
			overflow: hidden;
		}
	}
	.el-overlay:has(.hazard-detail-drawer) {
		z-index: 3300 !important;
	}
</style>
