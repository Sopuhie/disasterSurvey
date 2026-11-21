import { ref, onMounted, onUnmounted } from 'vue'

export function useContentMenu() {
	const x = ref(0)
	const y = ref(0)
	const visible = ref(false)

	function openMenu(e) {
		e.preventDefault()
		e.stopPropagation()
		visible.value = true
		x.value = e.clientX
		y.value = e.clientY
	}
	function closeMenu() {
		visible.value = false
	}
	onMounted(() => {
		container.addEventListener('click', openMenu)
		window.addEventListener('click', closeMenu)
		window.addEventListener('contextmenu', closeMenu)
	})
	onUnmounted(() => {
		container.removeEventListener('click', openMenu)
	})
	return {
		x,
		y,
		visible
	}
}
