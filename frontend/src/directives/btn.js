// 解决按钮点击后不会失去焦点问题
export default {
	install: app => {
		app.directive('btn', {
			mounted(el) {
				el.addEventListener('click', () => {
					el.blur()
				})
			}
		})
	}
}
