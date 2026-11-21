<!-- 右键菜单 -->
<template>
	<div ref="containerRef">
		<slot></slot>
		<Teleport to="body">
			<Transition @beforeEnter="handleBeforeEnter" @enter="handleEnter" @afterEnter="handleAfterEnter">
				<div v-if="showMenu" class="context-menu" :style="{ left: x + 'px', top: y + 'px' }">
					<div class="menu-list">
						<div class="menu-item" v-for="(item, i) in menu" :key="item.label" @click="handleClick(item)">{{}}</div>
					</div>
				</div>
			</Transition>
		</Teleport>
	</div>
</template>
<script setup>
	import { ref } from 'vue'
	import userContextMenu from './userContextMenu.js'

	const props = defineProps({
		menu: {
			type: Array,
			default: () => []
		}
	})

	const containerRef = ref(null)
	const emit = defineEmits(['select'])
	const { x, y, showMenu } = userContextMenu(containerRef, emit)

	const handleClick = item => {
		showMenu.value = false
		emit('select', item)
	}

	const handleBeforeEnter = el => {
		el.style.opacity = 0
	}

	const handleEnter = el => {
		el.style.height = 'auto'
		const h = el.clientHeight
		el.style.height = 0
		requestAnimationFrame(() => {
			requestAnimationFrame(() => {
				el.style.height = h + 'px'
				el.style.transition = '0.5s'
			})
		})
	}

	const handleAfterEnter = el => {
		el.style.transition = 'none'
	}
</script>
<style lang="scss" scoped>
	.context-menu {
		& .menu-list {
		}
		& .menu-item {
		}
	}
</style>
