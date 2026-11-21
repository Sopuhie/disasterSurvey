// src/utils/routeHistory.js
import { ref } from 'vue'

const routeHistory = ref([])

export const useRouteHistory = () => {
	const addRoute = route => {
		routeHistory.value.push({
			path: route.path,
			fullPath: route.fullPath,
			name: route.name,
			params: route.params,
			query: route.query
		})
		if (routeHistory.value.length > 10) {
			// 限制历史记录长度，避免占用过多内存
			routeHistory.value.shift()
		}
	}

	const getPreviousRoute = () => {
		return routeHistory.value.length > 1 ? routeHistory.value[routeHistory.value.length - 2] : null
	}

	return {
		addRoute,
		getPreviousRoute,
		routeHistory
	}
}
