<!-- 系统配置-创建项目 -->
<template>
	<div class="create max-wh">
		<el-tabs class="demo-tabs max-wh" v-model="state.activeFName" type="card" @tab-click="tabsHandle">
			<el-tab-pane :label="f.label" :name="f.name" v-for="(f, tabIdx) in formList" :key="'tabsF_' + tabIdx">
				<el-tabs v-if="state.activeFName === 'first'" class="demo-tabs max-wh" v-model="state.activeName" type="card" @tab-click="tabsHandle">
					<el-tab-pane :label="tab.label" :name="tab.name" v-for="(tab, idx) in f.data" :key="'tabs_' + idx">
						<template v-if="tab.data?.length && state.activeName === tab.name">
							<div class="formItem flex item" v-for="(item, index) in tab.data" :key="'form_' + index">
								<span class="formLabel" :class="{ asterisk: item.required }">{{ item.label }}</span>
								<div class="formWidget">
									<template v-if="item.component === 'el-select'">
										<el-select v-model="form[item.prop]" :placeholder="item.placeholder" v-bind="item.attrs">
											<el-option
												v-for="option in item.options"
												:key="option.value"
												:label="option.label"
												:value="option.value" />
										</el-select>
									</template>
									<template v-if="item.component === 'el-input'">
										<el-input v-model="form[item.prop]" :placeholder="item.placeholder" v-bind="item.attrs"></el-input>
									</template>
								</div>
							</div>
						</template>
					</el-tab-pane>
				</el-tabs>
				<div class="max-wh oy" v-else>
					<el-tree
						ref="refTree"
						:data="data"
						show-checkbox
						accordion
						highlight-current
						node-key="id"
						:default-expanded-keys="defaultExpandedKeys"
						:props="defaultProps"
						@node-click="handleNodeClick" />
				</div>
			</el-tab-pane>
		</el-tabs>
	</div>
</template>

<script setup>
	const defaultProps = { children: 'children', label: 'label' }
	const data = [
		{
			id: 1,
			label: 'Level one 1',
			children: [{ id: 4, label: 'Level two 1-1', children: [{ id: 9, label: 'Level three 1-1-1' }] }]
		}
	]
	const formList = ref([
		{ label: '项目配置', name: 'first', data: [] },
		{ label: '功能配置', name: 'second', data: [] }
	])
	const refTree = ref(null)
	const defaultExpandedKeys = ref([1, 4])
	const state = reactive({ activeFName: 'first', activeName: 'first' })
	const form = reactive({})

	const tabsHandle = () => {}
	const handleNodeClick = () => {}

	defineExpose({ form })
</script>

<style lang="scss" scoped></style>
