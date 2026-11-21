import { defineStore } from 'pinia'

const catalogTreeData = JSON.parse(localStorage.getItem('catalogTreeData'))
const menuTreeData = JSON.parse(localStorage.getItem('menuTreeData'))
const menuItemData = JSON.parse(localStorage.getItem('menuItemData'))
const monitorItemData = JSON.parse(localStorage.getItem('monitorItemData'))

export const useSystemStore = defineStore('system', {
	state: () => ({
		catalogTreeData: catalogTreeData || [],
		menuTreeData: menuTreeData || [],
		menuItemData: menuItemData || null,
		monitorItemData: monitorItemData || null,
		excludePaths: ['/statistics-report', '/build-enquiry']
	}),

	actions: {
		setCatalogTreeData(v) {
			this.catalogTreeData = v
			localStorage.setItem('catalogTreeData', JSON.stringify(v))
		},
		setMenuTreeData(v) {
			this.menuTreeData = v
			localStorage.setItem('menuTreeData', JSON.stringify(v))
		},
		setMenuItemData(v) {
			this.menuItemData = v
			localStorage.setItem('menuItemData', JSON.stringify(v))
		},
		setMonitorItemData(v) {
			this.monitorItemData = v
			localStorage.setItem('monitorItemData', JSON.stringify(v))
		}
	},
	getters: {}
})
