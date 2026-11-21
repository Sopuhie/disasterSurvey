// 表单校验
const validation = (data, formMode) => {
	data.forEach(item => {
		if (item.required) {
			if (formMode[item.prop] !== '' || formMode[item.prop]?.length > 0) {
				const regex = item.regMatch
				if (regex) {
					item.rules = regex.test(formMode[item.prop])
				}
			} else {
				item.rules = false
			}
		}
		if (item.data?.length) {
			validation(item.data, formMode)
		}
	})
	return data
}

/**
 * 重构form对象
 * 把'abc.de':'aaa'转换为abc:{de:'aaa'}
 * @param {Object} form
 * @returns
 */

const setFormData = form => {
	for (const key in form) {
		if (Object.hasOwnProperty.call(form, key)) {
			if (key.split('.').length > 1) {
				form[key.split('.')[0]][key.split('.')[1]] = form[key]
				delete form[key]
			}
		}
	}
	return form
}
// 反转
const setFormData2 = (form, value) => {
	for (const key in form[value]) {
		if (Object.hasOwnProperty.call(form[value], key)) {
			form[`${value}.${key}`] = form[value][key]
		}
	}
	return form
}

export { validation, setFormData, setFormData2 }
