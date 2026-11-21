export default {
	mounted(el, binding) {
		// 如果绑定的值中的isDrag属性为false，则返回false
		if (binding?.value?.isDrag === false) return false

		// 获取body的宽度和高度
		const bodyRect = document.body.getBoundingClientRect()
		const bodyWidth = bodyRect.width
		const bodyHeight = bodyRect.height
		// 获取所有class为zDialog的元素
		const zDialog = document.querySelectorAll('.zDialog')
		let zIndexArray = []
		let maxZIndex = 0

		// 获取当前元素的header元素
		let dragHeader = el.querySelector('.zDialog__header')
		// 设置指针样式为move
		dragHeader.style.cursor = 'move'
		// 当鼠标按下时执行
		dragHeader.onmousedown = e => {
			zIndexArray = []
			for (let i = 0; i < zDialog.length; i++) {
				zIndexArray.push(zDialog[i].style.zIndex)
				maxZIndex = Math.max(...zIndexArray)
			}
			// 遍历所有zDialog元素

			for (let i = 0; i < zDialog.length; i++) {
				if (zDialog[i] === el) {
					zDialog[i].style.zIndex = maxZIndex + 1
				}
			}

			// 禁止选择网页上的文字
			document.onselectstart = function () {
				return false
			}

			// 获取鼠标按下时的坐标
			const x = e.clientX - el.offsetLeft
			const y = e.clientY - el.offsetTop
			// 当鼠标移动时执行
			document.onmousemove = function (e) {
				// 阻止默认事件
				e.preventDefault()

				// 判断鼠标移动后的坐标是否超出body的边界
				if (e.pageX - x < 0) {
					el.style.left = '0px'
				} else if (e.pageX - x > bodyWidth - 56) {
					el.style.left = bodyWidth - 56 + 'px'
				} else {
					el.style.left = e.pageX - x + 'px'
				}

				if (e.pageY - y < 0) {
					el.style.top = '0px'
				} else if (e.pageY - y > bodyHeight - 56) {
					el.style.top = bodyHeight - 56 + 'px'
				} else {
					el.style.top = e.pageY - y + 'px'
				}
			}
			// 当鼠标松开时执行
			document.onmouseup = function () {
				// 允许选择网页上的文字
				document.onselectstart = function () {
					return true
				}

				// 移除鼠标移动和松开事件
				document.onmousemove = null
				document.onmouseup = null
			}
			return false
		}
	}
}
