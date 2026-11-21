<template>
	<el-form ref="ruleFormRef" :model="formMode" :rules="rules" v-bind="formData['attrs']">
		<el-row :gutter="12" v-for="(item, index) in formData['fields']" :key="`${item['key']}_${index}`">
			<el-col :span="item['span']" v-for="(child, cIdx) in item['children']" :key="`${item['key']}_col_${cIdx}`">
				<!-- button -->
				<template v-if="child['type'] === 'button'">
					<el-form-item :label="child.label" :prop="child.prop">
						<el-button v-btn v-bind="child['childAttrs']">{{ child['__txt__']['default'] }}</el-button>
					</el-form-item>
				</template>
				<!-- input -->
				<template v-if="child['type'] === 'input'">
					<el-form-item :label="child.label" :prop="child.prop">
						<el-input v-model="formMode[child['prop']]" v-bind="child['childAttrs']" />
					</el-form-item>
				</template>
				<!-- radio -->
				<template v-if="child['type'] === 'radio'">
					<el-form-item :label="child.label" :prop="child.prop">
						<el-radio-group v-model="formMode[child['prop']]">
							<el-radio :label="r.value" v-for="r in child['options']">{{ r.label }}</el-radio>
						</el-radio-group>
					</el-form-item>
				</template>
				<!-- select -->
				<template v-if="child['type'] === 'select'">
					<el-form-item :label="child.label" :prop="child.prop">
						<el-select v-model="formMode[child['prop']]" v-bind="child['childAttrs']">
							<el-option :label="s.label" :value="s.value" v-for="s in child['options']" />
						</el-select>
					</el-form-item>
				</template>
				<!-- cascader -->
				<template v-if="child['type'] === 'cascader'">
					<el-form-item :label="child.label" :prop="child.prop">
						<el-cascader v-model="formMode[child['prop']]" :options="child['options']" v-bind="child['childAttrs']" />
					</el-form-item>
				</template>
			</el-col>
		</el-row>
		<template v-if="formData['showFormBtn']">
			<el-form-item v-show="false">
				<div class="flex fa jend" style="width: 100%; height: 64px; border-top: 1px solid #4c4d4f">
					<el-button
						v-btn
						v-for="(item, index) in formData['formBtn']"
						:key="`${item['key']}_formBtn_${index}`"
						v-bind="item['attrs']"
						@click="onSubmit(ruleFormRef, item['type'])"
						>{{ item['btnTxt'] }}</el-button
					>
				</div>
			</el-form-item>
		</template>
	</el-form>
</template>

<script setup>
	const props = defineProps({
		formData: {
			type: Object,
			default: () => {}
		},
		formMode: {
			type: Object,
			default: () => {}
		},
		rules: {
			type: Object,
			default: () => {}
		}
	})
	const ruleFormRef = ref(null)

	const emits = defineEmits(['update:formMode', 'submitForm', 'cancelForm', 'otherForm'])
	watch(
		() => props.formMode,
		newValue => {
			emits('update:formMode', newValue)
		},
		{
			deep: true,
			immediate: true
		}
	)

	const onSubmit = async (formEl, type) => {
		if (type === 'submit') {
			submitForm(formEl)
		} else if (type === 'reset') {
			resetForm(formEl)
		} else if (type === 'cancel') {
			emits('cancelForm')
		} else {
			emits('otherForm', props.formMode)
		}
	}
	const submitForm = async formEl => {
		if (!formEl) return
		await formEl.validate(valid => {
			if (valid) {
				emits('submitForm', props.formMode)
			}
		})
	}
	const resetForm = formEl => {
		if (!formEl) return
		formEl.resetFields()
	}
</script>

<style lang="scss" scoped></style>
