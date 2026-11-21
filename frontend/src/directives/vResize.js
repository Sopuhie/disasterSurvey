export const vResize = {
	mounted(el, binding) {
		if (!binding.value || typeof binding.value !== 'object') {
			console.warn('v-resize directive expects an object with "target" and "direction" properties')
			return
		}

		const { target, direction, onMouseUp: onMouseUpCallback } = binding.value
		if (!target || (direction !== 'x' && direction !== 'y')) {
			console.warn('v-resize directive expects "target" to be a valid selector and "direction" to be either "x" or "y"')
			return
		}

		const targetEl = document.querySelector(target)
		if (!targetEl) {
			console.warn(`Target element "${target}" not found`)
			return
		}

		let timer = null
		const screenWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth
		const screenHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight
		const onMouseDown = e => {
			e.preventDefault()
			document.addEventListener('mousemove', onMouseMove)
			document.addEventListener('mouseup', onMouseUp)
		}

		const onMouseMove = e => {
			if (direction === 'x') {
				const newWidth = screenWidth - e.clientX
				targetEl.style.width = `${newWidth}px`
			} else {
				const newHeight = screenHeight - e.clientY - 40
				targetEl.style.height = `${newHeight}px`
			}
		}

		const onMouseUp = () => {
			clearTimeout(timer)
			document.removeEventListener('mousemove', onMouseMove)
			document.removeEventListener('mouseup', onMouseUp)
			const newWidth = parseInt(document.defaultView.getComputedStyle(targetEl).width, 10)
			const newHeight = parseInt(document.defaultView.getComputedStyle(targetEl).height, 10)
			onMouseUpCallback({
				element: targetEl,
				width: newWidth,
				height: newHeight,
				direction
			})
			if (typeof onMouseUpCallback === 'function') {
				timer = setTimeout(() => {
					onMouseUpCallback()
				}, 500)
			}
		}

		el.addEventListener('mousedown', onMouseDown)
	}
}
