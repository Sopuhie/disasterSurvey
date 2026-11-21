export const pickerOptions = {
	shortcuts: [
		{
			text: '一周内',
			onClick(picker) {
				const end = new Date()
				const start = new Date()
				start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
				picker.$emit('pick', [start, end])
			}
		},
		{
			text: '一月内',
			onClick(picker) {
				const end = new Date()
				const start = new Date()
				start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
				picker.$emit('pick', [start, end])
			}
		},
		{
			text: '一年内',
			onClick(picker) {
				const end = new Date()
				const start = new Date()
				start.setTime(start.getTime() - 3600 * 1000 * 24 * 365)
				picker.$emit('pick', [start, end])
			}
		}
	]
}
