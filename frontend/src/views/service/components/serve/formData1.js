const addFormData1 = {
	key: 'formScheme-test1',
	showFormBtn: false,
	formBtn: [],
	attrs: {
		class: 'mt12',
		size: 'default',
		labelPosition: 'right',
		labelWidth: 'auto',
		disabled: false
	},
	fields: [
		{
			span: 24,
			children: [
				{
					type: 'input',
					label: '工作区',
					prop: 'workSpace',
					childAttrs: {
						type: 'text',
						placeholder: '请输入工作区',
						clearable: true
					}
				}
			]
		},
		{
			span: 24,
			children: [
				{
					type: 'input',
					label: '数据存储',
					prop: 'dataSetName',
					childAttrs: {
						type: 'text',
						placeholder: '请输入数据存储',
						clearable: true
					}
				}
			]
		},
		{
			span: 24,
			children: [
				{
					type: 'select',
					label: '投影',
					prop: 'srs',
					childAttrs: { placeholder: '请选择投影', clearable: true },
					options: []
				}
			]
		}
	]
}

export { addFormData1 }
