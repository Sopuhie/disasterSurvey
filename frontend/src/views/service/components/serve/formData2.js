const addFormData = {
	key: 'formScheme-test',
	showFormBtn: false,
	formBtn: [
		{ btnTxt: '取消', type: 'cancel', attrs: { type: 'default' } },
		{ btnTxt: '重置', type: 'reset', attrs: { type: 'default' } },
		{ btnTxt: '确定', type: 'submit', attrs: { type: 'primary' } }
	],
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
					label: '资源名称',
					prop: 'serviceName',
					childAttrs: {
						type: 'text',
						placeholder: '请输入资源名称',
						clearable: true,
						readonly: false,
						disabled: false
					}
				}
			]
		},
		{
			span: 24,
			children: [
				{
					type: 'input',
					label: '发布人',
					prop: 'cjr',
					childAttrs: {
						type: 'text',
						placeholder: '请输入发布人',
						clearable: false,
						readonly: true,
						disabled: false
					}
				}
			]
		},
		{
			span: 24,
			children: [
				{
					type: 'select',
					label: '发布平台',
					prop: 'servicePlatform',
					childAttrs: {
						type: 'text',
						placeholder: '请选择发布平台',
						clearable: false,
						readonly: false,
						disabled: false
					},
					options: [
						{
							label: 'GEOSERVER 平台',
							value: 'ZYGL_GEOSERVER'
						}
					]
				}
			]
		},
		{
			span: 24,
			children: [
				{
					type: 'input',
					label: '发布机构',
					prop: 'unit',
					childAttrs: {
						type: 'text',
						placeholder: '请输入发布机构',
						clearable: true,
						readonly: false,
						disabled: false
					}
				}
			]
		},
		{
			span: 24,
			children: [
				{
					type: 'input',
					label: '创建时间',
					prop: 'createtime',
					childAttrs: {
						type: 'text',
						placeholder: '请输入创建时间',
						clearable: true,
						readonly: false,
						disabled: false
					}
				}
			]
		},
		{
			span: 24,
			children: [
				{
					type: 'input',
					label: '描述说明',
					prop: 'description',
					childAttrs: {
						type: 'textarea',
						rows: 4,
						placeholder: '请输入描述',
						readonly: false,
						disabled: false,
						resize: 'none'
					}
				}
			]
		}
	]
}

export { addFormData }
