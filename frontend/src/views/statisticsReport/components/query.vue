<template>
	<div class="query">
		<ul>
			<li class="item" v-for="(item, idx) in lists" :key="'query_' + idx" @click="listItemHandle(idx)">
				<el-tree-select
					v-model="item.key"
					:data="fieldsData"
					render-after-expand
					clearable
					filterable
					placeholder="请选择"
					style="width: 108px"
					:teleported="false"
					:fit-input-width="false"
					popper-class="key-tree-select-popper"
					@change="onChange"
					@visible-change="listItemHandle(idx)"></el-tree-select>
				<el-select v-model="item.relation" placeholder="请选择" style="width: 70px" @focus="relationFocus(idx)">
					<el-option :value="o" v-for="(o, oIdx) in relation" :key="`relation_${oIdx}`">{{ o }}</el-option>
				</el-select>
				<el-autocomplete
					class="queryAutocomplete"
					v-model="item.val"
					:popper-append-to-body="false"
					teleported
					placement="bottom-end"
					:fetch-suggestions="querySearchAsync"
					placeholder="请选择"
					style="width: 100px">
				</el-autocomplete>
				<!-- 删除 -->
				<span class="del">
					<el-icon v-if="idx !== 0" @click="del(idx)"><Minus /></el-icon>
				</span>
				<el-dropdown class="tips type" v-show="item.type" trigger="click" @command="handleCommand">
					<span class="el-dropdown-link">
						{{ item.type === 'AND' ? '并且' : '或者' }}
						<el-icon class="el-icon--right">
							<arrow-down />
						</el-icon>
					</span>
					<template #dropdown>
						<el-dropdown-menu>
							<el-dropdown-item command="AND">并且</el-dropdown-item>
							<el-dropdown-item command="OR">或者</el-dropdown-item>
						</el-dropdown-menu>
					</template>
				</el-dropdown>
				<span v-show="idx === 0 && lists.length > 1" class="tips fisst"></span>
				<span v-show="idx === lists.length - 1 && lists.length > 1" class="tips last"></span>
				<span v-show="idx !== 0 && idx !== lists.length - 1 && lists.length > 1" class="tips con"></span>
			</li>
		</ul>
	</div>
</template>

<script setup>
	import { arcGisApi } from '@/api/gis/arcgis.js'

	const api = arcGisApi()

	const lists = ref([
		{
			type: '',
			id: '',
			name: '',
			relationType: '',
			key: '',
			val: '',
			relation: ''
		},
		{
			type: 'AND',
			id: '',
			name: '',
			relationType: '',
			key: '',
			val: '',
			relation: ''
		}
	])
	const keys = ref([])
	const relation = ref([])
	const itemIdx = ref(0)
	const relationString = reactive(['=', 'like'])
	const relationDouble = reactive(['=', '<', '>', '≤', '≥', 'like'])
	const lyrFieldUniqueVals = ref([])
	const lyrFieldName = ref('')

	const props = defineProps({
		lyrUrl: {
			type: String,
			default: () => ''
		},
		fields: {
			type: Array,
			default: () => []
		}
	})

	const fieldsData = computed(() => {
		return props.fields
	})

	const listItemHandle = idx => {
		itemIdx.value = idx
	}

	const onChange = async fieldName => {
		lyrFieldName.value = fieldName
		const item = props.fields.find(o => o.key === fieldName)
		if (item) {
			const listIdx = lists.value[itemIdx.value]
			listIdx['name'] = item.value
			listIdx['relationType'] = item.type
			listIdx['id'] = item.key
			listIdx['relation'] = ''
			listIdx['val'] = ''

			// 这里做唯一值获取
			if (props.lyrUrl && !lyrFieldUniqueVals.value.find(n => n.field == fieldName)) {
				let res = await api.uniqueValueQuery(props.lyrUrl, [fieldName])
				if (res?.features.length) {
					let datas = res.features.map(feat => {
						return {
							value: feat.attributes[fieldName],
							link: feat.attributes[fieldName]
						}
					})
					// 去重
					const uniqueData = Array.from(new Set(datas.map(item => item.value))).map(value =>
						datas.find(item => item.value === value)
					)
					lyrFieldUniqueVals.value.push({
						field: fieldName,
						value: uniqueData
					})
				}
			}
		}
	}
	const getName = id => {
		let name = 0
		props.fields.forEach(item => {
			if (item.children?.length) {
				item.children.forEach(o => {
					if (o.key === id) {
						name = o.name
					}
				})
			}
		})
		return name
	}
	// 运算符选择
	const relationFocus = idx => {
		if (lists.value[idx]['key']) {
			setTjzdDatas(lists.value[idx]['relationType'])
		}
	}
	const valFocus = () => {
		keys.value = []
		const eventKey = lists.value[itemIdx.value]['id']
		const name = getName(eventKey)
		if (!eventKey) return
		const id = eventKey.split('--')[0]

		// GIS数据--类别代码
		// 类别代码 like '%U'
		// 类别名称 like '%用'
		// where = "lbdm like '%U' AND lbmc like '%用'"
		keys.value = [
			'A5',
			'R2',
			'B2',
			'W1',
			'A7',
			'H22',
			'A6',
			'B9',
			'G2',
			'S3',
			'B1',
			'U32',
			'B3',
			'H9',
			'U9',
			'A1',
			'A9',
			'A2',
			'S9',
			'U3',
			'U',
			'B4',
			'G3',
			'U22',
			'U31',
			'U12',
			'S4',
			'M',
			'U15',
			'H3',
			'U1',
			'E1',
			'U11',
			'S2',
			'U2',
			'飞地',
			'A4',
			'U21',
			'E2',
			'A3',
			'G1',
			'U13',
			'H14',
			'H4'
		]
	}

	/**
	 * AND / OR 菜单点击事件
	 * @param command {String} 点击的菜单项
	 */
	const handleCommand = command => {
		const listIdx = lists.value[itemIdx.value]
		listIdx['type'] = command
	}

	let timeout = null
	const querySearchAsync = (queryString, cb) => {
		let datas = lyrFieldUniqueVals.value.find(m => m.field == lyrFieldName.value)?.value
		const results = queryString ? datas.filter(createFilter(queryString)) : datas

		clearTimeout(timeout)
		timeout = setTimeout(() => {
			cb(results)
		}, 400 * Math.random())
	}
	const createFilter = queryString => {
		return restaurant => {
			if (typeof restaurant === 'string') {
				return restaurant?.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
			} else {
				return restaurant
			}
		}
	}
	// 重置
	const reset = () => {
		lists.value = [
			{ type: '', name: '', key: '', val: '', relation: '' },
			{ type: 'AND', name: '', key: '', val: '', relation: '' }
		]
	}
	// 新增
	const add = () => {
		lists.value.push({
			type: 'AND',
			id: '',
			key: '',
			val: '',
			relation: '',
			relationType: ''
		})
		keys.value = []
	}
	// 删除
	const del = idx => {
		lists.value.splice(idx, 1)
	}
	/**
	 * 设置查询条件的运算符的数据
	 * @param {String} value 字段类型
	 */
	const setTjzdDatas = value => {
		relation.value = []
		if (value.indexOf('String') !== -1) {
			relation.value = relationString
		} else {
			relation.value = relationDouble
		}
	}
	onBeforeUnmount(() => {
		timeout && clearTimeout(timeout)
	})
	defineExpose({
		reset,
		add,
		lists
	})
</script>

<style lang="scss" scoped>
	.query {
		& .item {
			position: relative;
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 32px 0 0 42px;

			& .del {
				display: inline-block;
				width: 22px;
				height: 100%;
				cursor: pointer;

				i {
					display: flex;
					align-items: center;
					justify-content: center;
					width: 18px;
					font-size: 18px;
					padding: 2px;
					background: #cccccc;
					color: white;
					border-radius: 2px;
				}

				&:hover i {
					background: #ed5353;
					color: white;
				}
			}
		}
		& .tips {
			position: absolute;
			display: inline-block;
			height: 1px;
			width: 1px;

			&:before {
				content: '';
				position: absolute;
				top: 50%;
				left: -25px;
				width: 25px;
				border-top: 1px dashed #999999;
			}

			&.type {
				position: absolute;
				top: 4px;
				left: 0px;
				width: 48px;
				height: 22px;
				font-size: 12px;
				font-family: PingFangSC-Regular, PingFang SC;
				font-weight: 400;
				line-height: 22px;

				&:before {
					content: '';
					border-top: 0px dashed #999999;
				}
			}

			&.fisst {
				&:after {
					content: '';
					position: absolute;
					top: 0px;
					left: -25px;
					height: 24px;
					border-left: 1px dashed #999999;
				}
			}

			&.con {
				&:after {
					content: '';
					position: absolute;
					top: -24px;
					left: -25px;
					height: 48px;
					border-left: 1px dashed #999999;
				}
			}

			&.last {
				&:after {
					content: '';
					position: absolute;
					top: -24px;
					left: -25px;
					height: 24px;
					border-left: 1px dashed #999999;
				}
			}
		}
	}
</style>
