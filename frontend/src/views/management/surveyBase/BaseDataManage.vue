<!-- 灾情调查/灾害评估基础数据通用 CRUD（真接口优先，空数据回落 MOCK） -->
<template>
	<div class="survey-base max-wh flex fc">
		<FilterContainer>
			<template #left>
				<el-input
					class="mr-12"
					v-model="keyword"
					:placeholder="schema.searchPlaceholder || '请输入关键词'"
					clearable
					style="width: 200px"
					@keyup.enter="searchHandle" />
				<el-select
					v-if="schema.extraSearch"
					class="mr-12"
					v-model="extraFilter"
					:placeholder="schema.extraSearch.placeholder"
					clearable
					style="width: 150px">
					<el-option
						v-for="opt in schema.extraSearch.options"
						:key="String(opt.value)"
						:label="opt.label"
						:value="opt.value" />
				</el-select>
				<el-button type="primary" plain @click="searchHandle">
					<el-icon class="el-icon--left"><Search /></el-icon>搜索
				</el-button>
				<el-button type="primary" plain @click="addHandle">
					<el-icon class="el-icon--left"><Plus /></el-icon>新增
				</el-button>
				<template v-if="enableMockTools">
					<el-button type="success" plain :loading="syncing" @click="syncHandle">同步当前类型</el-button>
					<el-button
						v-if="isHazardType"
						type="success"
						:loading="syncingAll"
						@click="syncAllHazardsHandle">
						一键同步四类承灾体
					</el-button>
					<el-button type="warning" plain @click="resetHandle">恢复默认数据</el-button>
					<el-tag class="ml-8" size="small" :type="dataSource === 'remote' ? 'success' : 'info'">
						{{ dataSource === 'remote' ? '当前：后端真实数据' : '当前：本地数据（后端无数据或不可用）' }}
					</el-tag>
					<el-tag v-if="hazardSyncSummary" class="ml-8" size="small" type="warning">
						{{ hazardSyncSummary }}
					</el-tag>
				</template>
			</template>
		</FilterContainer>

		<PageContainer class="mt-10 survey-base__page" :loading="loading" height="0">
			<div class="tableBox" ref="tableBox">
				<ZTable :tableData="tableData" :tableHeaders="schema.columns" :tableHeight="tableHeight">
					<template #idx="{ scope }">
						{{ paginationIdxVal(scope.$index) }}
					</template>
					<template #geojson="{ scope }">
						<el-tag v-if="hasGeojson(scope.row.geojson)" type="success" size="small">已录入</el-tag>
						<span v-else style="color: #909399">未录入</span>
					</template>
					<template #points="{ scope }">
						<el-tag v-if="hasJsonArray(scope.row.points)" type="success" size="small">
							{{ (scope.row.points || []).length }} 点
						</el-tag>
						<span v-else style="color: #909399">未录入</span>
					</template>
					<template #action="{ scope }">
						<el-button type="primary" link @click="editHandle(scope.row)">编辑</el-button>
						<el-button type="danger" link @click="delHandle(scope.row)">删除</el-button>
					</template>
				</ZTable>
			</div>
			<ZPagination
				:disabled="loading"
				:total="total"
				:pageSize="page.size"
				:currentPage="page.current"
				@sizeChange="sizeChange"
				@currentChange="currentChange" />
		</PageContainer>

		<el-drawer
			v-model="dialogVisible"
			class="survey-base-drawer"
			:title="dialogTitle"
			direction="rtl"
			:size="drawerSize"
			:z-index="3200"
			append-to-body
			:close-on-click-modal="true"
			destroy-on-close
			@closed="onDialogClosed">
			<div class="survey-base__drawer-body">
				<el-form ref="formRef" class="survey-base__form" :model="form" :rules="rules" label-width="120px">
					<el-row v-for="(row, rowIndex) in formRows" :key="'form_row_' + rowIndex" :gutter="16">
						<el-col v-for="field in row.fields" :key="field.prop" :span="row.span">
							<el-form-item :label="field.label" :prop="field.prop">
								<el-select v-if="field.type === 'select'" v-model="form[field.prop]" style="width: 100%" clearable>
									<el-option
										v-for="opt in field.options || []"
										:key="String(opt.value)"
										:label="opt.label"
										:value="opt.value" />
								</el-select>
								<el-input-number
									v-else-if="field.type === 'number'"
									v-model="form[field.prop]"
									:controls="false"
									style="width: 100%" />
								<el-input
									v-else-if="field.type === 'textarea' || field.type === 'json'"
									v-model="form[field.prop]"
									type="textarea"
									:rows="field.rows || 6"
									:placeholder="field.placeholder || ''"
									resize="vertical" />
								<el-input v-else v-model="form[field.prop]" clearable :placeholder="field.placeholder || ''" />
							</el-form-item>
						</el-col>
					</el-row>
				</el-form>
			</div>
			<template #footer>
				<div class="survey-base__drawer-footer">
					<el-button @click="dialogVisible = false">取消</el-button>
					<el-button type="primary" :loading="saving" @click="saveHandle">保存</el-button>
				</div>
			</template>
		</el-drawer>
	</div>
</template>

<script setup>
	import FilterContainer from '@/components/filterContainer/index.vue'
	import PageContainer from '@/components/pageContainer/index.vue'
	import ZTable from '@/components/table/index.vue'
	import ZPagination from '@/components/pagination/index.vue'
	import { Search, Plus } from '@element-plus/icons-vue'
	import { useRoute } from 'vue-router'
	import { HAZARD_SYNC_TYPES, surveyBaseApi } from '@/api/surveyBase/index.js'
	import { SURVEY_BASE_TYPES } from '@/api/surveyBase/schemas.js'
	import { enableMockTools } from '@/config/index.js'
	import { ElMessage, ElMessageBox, ElLoading, ElNotification } from 'element-plus'

	const props = defineProps({
		dataType: { type: String, default: '' }
	})

	const route = useRoute()
	const api = surveyBaseApi()

	const dataType = computed(() => props.dataType || route.meta?.dataType || '')
	const schema = computed(() => SURVEY_BASE_TYPES[dataType.value] || { columns: [], fields: [], searchPlaceholder: '' })

	const tableBox = ref(null)
	const formRef = ref(null)
	const loading = ref(false)
	const saving = ref(false)
	const syncingAll = ref(false)
	const hazardSyncSummary = ref('')
	const isHazardType = computed(() => HAZARD_SYNC_TYPES.some(item => item.type === dataType.value))

	const refreshHazardSyncSummary = () => {
		const status = api.getHazardSyncStatus?.()
		if (!status?.results?.length) {
			hazardSyncSummary.value = ''
			return
		}
		const ok = status.results.filter(r => r.ok && (r.fail || 0) === 0).length
		const partial = status.results.filter(r => r.ok && (r.fail || 0) > 0).length
		const bad = status.results.filter(r => !r.ok).length
		const time = status.at ? new Date(status.at).toLocaleString() : ''
		hazardSyncSummary.value = `四类上次同步：成功 ${ok} / 部分失败 ${partial} / 失败 ${bad}（${time}）`
	}
	const syncing = ref(false)
	const dataSource = ref('mock')
	const tableHeight = ref(360)
	const tableData = ref([])
	const total = ref(0)
	const keyword = ref('')
	const extraFilter = ref('')
	const dialogVisible = ref(false)
	const dialogTitle = ref('新增')
	const editingId = ref(null)
	const form = reactive({})
	const page = reactive({ current: 1, size: 20 })

	const rules = computed(() => {
		const map = {}
		;(schema.value.fields || []).forEach(field => {
			if (field.required) {
				map[field.prop] = [{ required: true, message: `请输入${field.label}`, trigger: 'blur' }]
			}
		})
		return map
	})

	/** 与受灾整合台账一致 */
	const drawerSize = '640px'

	/** 数值字段两列一行，文本/下拉/多行单独一行 */
	const formRows = computed(() => {
		const rows = []
		const fields = schema.value.fields || []
		let numberBuf = []
		const flushNumbers = () => {
			while (numberBuf.length) {
				const pair = numberBuf.splice(0, 2)
				// 单独一个数值字段时拉满整行，与右侧对齐
				rows.push({ span: pair.length === 1 ? 24 : 12, fields: pair })
			}
		}
		fields.forEach(field => {
			if (field.type === 'number') {
				numberBuf.push(field)
				if (numberBuf.length >= 2) flushNumbers()
				return
			}
			flushNumbers()
			rows.push({ span: 24, fields: [field] })
		})
		flushNumbers()
		return rows
	})

	const syncTableHeight = () => {
		const h = tableBox.value?.clientHeight
		if (h && h > 0) tableHeight.value = h
	}

	let resizeObs = null
	const getPageData = async () => {
		if (!dataType.value) return
		loading.value = true
		try {
			const queryParams = {
				keyword: keyword.value,
				current: page.current,
				size: page.size
			}
			if (schema.value.extraSearch?.prop) {
				queryParams[schema.value.extraSearch.prop] = extraFilter.value
			}
			const res = await api.queryPage(dataType.value, queryParams)
			tableData.value = res?.data?.records || []
			total.value = res?.data?.total || 0
			dataSource.value = res?.source === 'remote' ? 'remote' : 'mock'
		} catch (err) {
			ElMessage.error(err?.message || '查询失败')
		} finally {
			loading.value = false
			nextTick(syncTableHeight)
		}
	}

	const searchHandle = () => {
		page.current = 1
		getPageData()
	}

	const hasGeojson = value => {
		if (value == null || value === '') return false
		if (value === '__HAS_GEO__') return true
		if (typeof value === 'object') return Object.keys(value).length > 0
		return String(value).trim().length > 0
	}

	const hasJsonArray = value => Array.isArray(value) && value.length > 0

	const stringifyGeojson = value => {
		if (value == null || value === '') return ''
		if (typeof value === 'object') {
			try {
				return JSON.stringify(value, null, 2)
			} catch (e) {
				return ''
			}
		}
		return String(value)
	}

	const normalizeGeojsonPayload = raw => {
		const text = String(raw ?? '').trim()
		if (!text) return ''
		let parsed
		try {
			parsed = JSON.parse(text)
		} catch (e) {
			throw new Error('GeoJSON 不是合法 JSON，请检查格式')
		}
		const type = parsed?.type
		const okTypes = ['Feature', 'FeatureCollection', 'Point', 'MultiPoint', 'LineString', 'MultiLineString', 'Polygon', 'MultiPolygon', 'GeometryCollection']
		if (!okTypes.includes(type)) {
			throw new Error('GeoJSON 需为 Feature / FeatureCollection 或 Geometry')
		}
		return parsed
	}

	const normalizeJsonPayload = (raw, label = 'JSON') => {
		const text = String(raw ?? '').trim()
		if (!text) return null
		try {
			return JSON.parse(text)
		} catch (e) {
			throw new Error(`${label} 不是合法 JSON`)
		}
	}

	const buildEmptyForm = () => {
		const next = {}
		;(schema.value.fields || []).forEach(field => {
			next[field.prop] = field.defaultValue ?? (field.type === 'number' ? undefined : '')
		})
		return next
	}

	const fillForm = row => {
		Object.keys(form).forEach(key => delete form[key])
		const base = buildEmptyForm()
		const merged = { ...base, ...(row || {}) }
		;(schema.value.fields || []).forEach(field => {
			if (field.prop === 'geojson' || field.type === 'json') {
				merged[field.prop] = stringifyGeojson(merged[field.prop])
			}
		})
		Object.assign(form, merged)
	}

	const addHandle = () => {
		editingId.value = null
		dialogTitle.value = `新增${schema.value.title || ''}`
		fillForm()
		dialogVisible.value = true
	}

	const editHandle = async row => {
		editingId.value = row.id
		dialogTitle.value = `编辑${schema.value.title || ''}`
		// 列表行可能已去掉完整 geojson，编辑时按 id 回取全量
		let full = row
		try {
			const detail = await api.getById(dataType.value, row.id)
			if (detail) full = detail
		} catch (e) {
			/* 用列表行兜底 */
		}
		fillForm(full)
		dialogVisible.value = true
	}

	const saveHandle = async () => {
		await formRef.value?.validate?.()
		saving.value = true
		try {
			const payload = { ...form }
			;(schema.value.fields || []).forEach(field => {
				if (field.prop === 'geojson') {
					payload.geojson = normalizeGeojsonPayload(payload.geojson)
				} else if (field.type === 'json') {
					const parsed = normalizeJsonPayload(payload[field.prop], field.label)
					if (field.required && (parsed == null || (Array.isArray(parsed) && !parsed.length))) {
						throw new Error(`请填写${field.label}`)
					}
					payload[field.prop] = parsed
				}
			})
			if (editingId.value) {
				payload.id = editingId.value
				await api.update(dataType.value, payload)
				ElMessage.success('更新成功')
			} else {
				await api.create(dataType.value, payload)
				ElMessage.success('新增成功')
			}
			dialogVisible.value = false
			getPageData()
		} catch (err) {
			if (err !== false) ElMessage.error(err?.message || '保存失败')
		} finally {
			saving.value = false
		}
	}

	const delHandle = row => {
		ElMessageBox.confirm(`确定删除「${row.name || row.year || row.id}」吗？`, '提示', {
			type: 'warning',
			confirmButtonText: '确定',
			cancelButtonText: '取消'
		})
			.then(async () => {
				await api.remove(dataType.value, row.id)
				ElMessage.success('删除成功')
				if (tableData.value.length <= 1 && page.current > 1) page.current -= 1
				getPageData()
			})
			.catch(() => {})
	}

	const syncHandle = () => {
		const withGeoTip = isHazardType.value
			? '将一并写入本地系统 JSON 中的空间数据（geojson）。数据量较大时可能需要一两分钟。'
			: ''
		ElMessageBox.confirm(
			`将把当前类型的本地台账批量写入数据库。${withGeoTip}需已登录且后端可用；写入成功后列表将优先显示服务器数据。`,
			'同步当前类型',
			{ type: 'warning', confirmButtonText: '开始同步', cancelButtonText: '取消' }
		)
			.then(async () => {
				syncing.value = true
				try {
					const res = await api.syncMockToRemote(dataType.value)
					const s = res?.data || {}
					const geoHint = s.withGeo != null ? `，含空间 ${s.withGeo}` : ''
					ElMessage.success(`同步完成：成功 ${s.success ?? 0}，失败 ${s.fail ?? 0}${geoHint}`)
					page.current = 1
					await getPageData()
					refreshHazardSyncSummary()
				} catch (err) {
					ElMessage.error(err?.message || '同步失败（请确认已登录且后端已启动）')
				} finally {
					syncing.value = false
				}
			})
			.catch(() => {})
	}

	const formatHazardSyncReport = status => {
		const lines = (status?.results || []).map(r => {
			if (!r.ok) return `【${r.label}】失败：${r.message || '未知错误'}`
			if ((r.fail || 0) > 0) {
				return `【${r.label}】部分完成：成功 ${r.success}，失败 ${r.fail}，含空间 ${r.withGeo}`
			}
			return `【${r.label}】完成：成功 ${r.success}，含空间 ${r.withGeo}`
		})
		return lines.join('\n')
	}

	const syncAllHazardsHandle = () => {
		ElMessageBox.confirm(
			'将按顺序把「海堤、渔港、海水养殖区、滨海旅游区」四类本地台账（含空间 geojson）全部写入数据库。过程可能需要几分钟，请勿关闭页面。结束后会列出每一类的成功/失败情况。',
			'一键同步四类承灾体',
			{ type: 'warning', confirmButtonText: '开始全部同步', cancelButtonText: '取消' }
		)
			.then(async () => {
				syncingAll.value = true
				const loadingInst = ElLoading.service({
					lock: true,
					text: '准备同步四类承灾体…',
					background: 'rgba(0, 0, 0, 0.35)'
				})
				try {
					const res = await api.syncAllHazardsToRemote(info => {
						loadingInst.setText(
							`正在同步（${info.index}/${info.total}）：${info.label}${info.phase === 'done' ? ' 完成' : info.phase === 'error' ? ' 失败' : '…'}`
						)
					})
					const status = res?.data || {}
					refreshHazardSyncSummary()
					const report = formatHazardSyncReport(status)
					const html = (report || '同步结束')
						.split('\n')
						.map(line => `<div style="margin:4px 0;line-height:1.5;">${line}</div>`)
						.join('')
					await ElMessageBox.alert(html, '四类承灾体同步结果', {
						confirmButtonText: '知道了',
						dangerouslyUseHTMLString: true
					})
					page.current = 1
					await getPageData()
					const allOk = (status.results || []).every(r => r.ok && !(r.fail > 0))
					ElNotification({
						title: allOk ? '四类已全部入库' : '同步结束（请查看明细）',
						message: allOk ? '海堤/渔港/养殖/旅游均已写入数据库' : '存在失败或部分失败项，请根据弹窗明细重试失败类型',
						type: allOk ? 'success' : 'warning',
						duration: 6000
					})
				} catch (err) {
					ElMessage.error(err?.message || '一键同步失败（请确认已登录且后端已启动）')
				} finally {
					loadingInst.close()
					syncingAll.value = false
				}
			})
			.catch(() => {})
	}

	const resetHandle = () => {
		ElMessageBox.confirm('将清空当前本地修改并恢复为系统 JSON 默认数据，是否继续？', '提示', {
			type: 'warning',
			confirmButtonText: '恢复',
			cancelButtonText: '取消'
		})
			.then(async () => {
				await api.reset()
				ElMessage.success('已恢复系统 JSON 默认数据')
				page.current = 1
				getPageData()
			})
			.catch(() => {})
	}

	const sizeChange = val => {
		page.size = val
		page.current = 1
		getPageData()
	}
	const currentChange = val => {
		page.current = val
		getPageData()
	}
	const paginationIdxVal = index => {
		return page.current > 1 ? index + 1 + page.size * (page.current - 1) : index + 1
	}
	const onDialogClosed = () => {
		formRef.value?.resetFields?.()
	}

	watch(
		() => dataType.value,
		() => {
			keyword.value = ''
			extraFilter.value = ''
			page.current = 1
			getPageData()
		}
	)

	onMounted(() => {
		refreshHazardSyncSummary()
		getPageData()
		nextTick(syncTableHeight)
		setTimeout(syncTableHeight, 300)
		if (typeof ResizeObserver !== 'undefined' && tableBox.value) {
			resizeObs = new ResizeObserver(() => syncTableHeight())
			resizeObs.observe(tableBox.value)
		}
	})
	onUnmounted(() => {
		resizeObs?.disconnect()
	})
</script>

<style lang="scss" scoped>
	.survey-base {
		min-height: 0;
		&__page {
			flex: 1;
			min-height: 0;
			display: flex;
			flex-direction: column;
			height: 100% !important;
		}
		.tableBox {
			flex: 1;
			min-height: 0;
			height: calc(100% - 52px);
			overflow: hidden;
		}
		&__form {
			:deep(.el-form-item) {
				margin-bottom: 16px;
			}
			/* 数值框默认居中，改为与文本框一致左对齐 */
			:deep(.el-input-number .el-input__inner) {
				text-align: left;
			}
		}
		&__drawer-body {
			height: 100%;
			padding-bottom: 8px;
			overflow: auto;
		}
		&__drawer-footer {
			display: flex;
			justify-content: flex-end;
			gap: 8px;
		}
	}
</style>

<style lang="scss">
	/* 运维面板 z-index:3001，drawer 需更高才能盖住表格 */
	.survey-base-drawer.el-drawer {
		z-index: 3200 !important;
		.el-drawer__header {
			margin-bottom: 0;
			padding: 0 16px;
			height: 56px;
			box-sizing: border-box;
			/* 原图不拉伸，按高度适配后右对齐，露出右侧装饰 */
			background-color: #bce4ff;
			background-image: url('@/assets/imgs/headBg.png');
			background-repeat: no-repeat;
			background-size: auto 100%;
			background-position: right center;
			color: #303133;
			.el-drawer__title {
				position: relative;
				z-index: 1;
				font-size: 16px;
				font-weight: 700;
				color: #1a3a5c;
				line-height: 56px;
				text-shadow: 0 0 6px rgba(188, 228, 255, 0.9);
			}
			.el-drawer__close-btn {
				position: relative;
				z-index: 1;
				color: #1a3a5c;
				&:hover {
					color: #0d2137;
				}
			}
		}
		.el-drawer__body {
			display: flex;
			flex-direction: column;
			padding: 12px 16px;
			overflow: hidden;
		}
		.el-drawer__footer {
			padding: 12px 16px;
			border-top: 1px solid var(--el-border-color-lighter);
		}
	}
	.el-overlay:has(.survey-base-drawer),
	.el-overlay.survey-base-drawer-overlay {
		z-index: 3200 !important;
	}
</style>
