<template>
	<template v-for="item in data">
		<el-sub-menu v-if="item.children?.length > 0" :index="item.path" :key="'sub_' + item.path">
			<template #title>
				<img v-if="item.icon" :src="getAssetsFile(isSubpath(item, item.icon))" width="24" height="24" alt="" />
				<span :class="['menuTitle', { active: routePath.includes(item.path) }]">{{ item.name }}</span>
			</template>
			<menuItem :data="item.children" :routePath="routePath"></menuItem>
		</el-sub-menu>
		<el-menu-item v-else :key="item.path" :index="item.path">
			<img v-if="item.icon" :src="getAssetsFile(isSubpath(item, item.icon))" width="24" height="24" alt="" />
			<span>{{ item.name }}</span>
		</el-menu-item>
	</template>
</template>
<script setup name="menuItem">
	import { getAssetsFile } from '@/utils/index.js'

	const props = defineProps({
		data: {
			type: Array,
			default: () => []
		},
		routePath: {
			type: String,
			default: ''
		}
	})

	function isSubpath(item, icon) {
		let path = props.routePath
		if (item.path === '/') {
			return path === '/' ? `${icon}2` : icon
		} else {
			const parent = path.replace(/^\//, '').split('/')
			if (`/${parent[0]}` === item.path) {
				return `${icon}2`
			}
			return icon
		}
	}
</script>
<style lang="scss" scoped>
	.menuTitle {
		&.active {
			color: #1890ff;
		}
	}
</style>
