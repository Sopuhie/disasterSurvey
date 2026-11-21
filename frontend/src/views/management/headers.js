const applicationHeaders = [
	{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 80, align: 'center' },
	{ prop: 'id', label: 'ID', width: 100, align: 'center' },
	{ prop: 'name', label: '用户名称', minWidth: 156 },
	{ prop: 'alias', label: '操作对象', minWidth: 220 },
	{ prop: 'city', label: '操作类型', minWidth: 156 },
	{ prop: 'state', label: '操作内容', minWidth: 220 },
	{ prop: 'state', label: '状态', minWidth: 156, align: 'center' },
	{ prop: 'state', label: '状态信息', minWidth: 156, align: 'center' },
	{ prop: 'ip', label: 'IP地址', minWidth: 160, align: 'center' },
	{ prop: 'state', label: '操作时间', width: 220, align: 'center' }
]
const actionHeaders = [
	{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 120, align: 'center' },
	{ prop: 'id', label: 'ID', width: 200, align: 'center' },
	{ prop: 'name', label: '功能名称', width: 240, align: 'center' },
	{ prop: 'alias', label: '别名', width: 240, align: 'center' },
	{ prop: '', slot: 'parentId', label: '所属上级', minWidth: 120, align: 'center' },
	{ prop: '', slot: 'state', label: '类型', width: 120, align: 'center' },
	{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 200, align: 'center' }
]
const configHeaders = [
	{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 80, align: 'center' },
	{ prop: 'name', label: '名称', width: 220, align: 'center' },
	{ prop: 'aliasName', label: '别名', minWidth: 120, align: 'center' },
	{ prop: 'title', label: '系统标题', minWidth: 200, align: 'center' },
	{ prop: 'cjr', label: '创建人', width: 120, align: 'center' },
	{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 120, align: 'center' }
]
const routerHeaders = [
	{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 80, align: 'center' },
	{ prop: 'title', label: '标题', width: 100, align: 'center' },
	{ prop: 'icon', label: '图标', width: 220, align: 'center' },
	{ prop: 'path', label: '路径', width: 220, align: 'center' },
	{ prop: 'name', label: '路由名称', width: 156, align: 'center' },
	{ prop: 'component', label: '组件', width: 220, align: 'center' },
	{ prop: 'redirect', label: '重定向', width: 220, align: 'center' },
	{ prop: 'iframe', label: '嵌入页面', width: 220, align: 'center' },
	{ prop: '', slot: 'noCache', label: '不放入缓存', width: 100, align: 'center' },
	{ prop: '', slot: 'hidden', label: '隐藏', width: 100, align: 'center' },
	{ prop: '', slot: 'alwaysShow', label: '始终显示', width: 100, align: 'center' },
	{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 160, align: 'center' }
]
const annexHeaders = [
	{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 80, align: 'center' },
	{ prop: 'name', label: '资源名称', align: 'center' },
	{ prop: '', slot: 'status', label: '资源状态', align: 'center' },
	{ prop: '', slot: 'level', label: '文件等级', align: 'center' },
	{ prop: 'description', label: '描述', align: 'center' },
	{ prop: '', slot: 'action', fixed: 'right', label: '操作', width: 200, align: 'center' }
]

const loginHeaders = [
	{ prop: '', slot: 'idx', fixed: 'left', label: '序号', width: 80, align: 'center' },
	{ prop: 'id', label: 'ID', width: 220, align: 'center' },
	{ prop: 'userName', label: '用户名称', minWidth: 100 },
	{ prop: 'target', label: '操作对象', minWidth: 220 },
	{ prop: 'type', label: '操作类型', minWidth: 100, align: 'center' },
	{ prop: 'status', label: '状态', minWidth: 100, align: 'center' },
	{ prop: 'statusMsg', label: '状态信息', minWidth: 100, align: 'center' },
	{ prop: 'ipAddress', label: 'IP地址', minWidth: 140, align: 'center' },
	{ prop: 'createTime', label: '操作时间', width: 200, align: 'center' }
]

export { applicationHeaders, actionHeaders, configHeaders, routerHeaders, annexHeaders, loginHeaders }
