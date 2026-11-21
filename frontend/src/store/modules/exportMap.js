import { defineStore } from 'pinia'
import { initMapData, initPaper } from '@/views/exportMap/modules/constant.js'

const initFormDataItem = initMapData['A4_a']

export const useExportMapStore = defineStore('exportMap', {
	state: () => ({
		formData: {
			name: '',
			paper: {
				...initPaper,
				type: 'A4_a'
			},
			map: {
				...initFormDataItem.map
			},
			thumbnial: {
				...initFormDataItem.thumbnial
			},
			text: {
				modules: [
					{
						id: 1,
						label: '标题',
						prop: 'title'
					},
					{
						id: 2,
						label: '制作人',
						prop: 'producer'
					},
					{
						id: 6,
						label: '制图单位',
						prop: 'explain3'
					},
					{
						id: 3,
						label: '打印时间',
						prop: 'time'
					},
					{
						id: 4,
						label: '说明1',
						prop: 'explain'
					},
					{
						id: 5,
						label: '说明2',
						prop: 'explain2'
					}
				],
				title: {
					...initFormDataItem.text.title
				},
				producer: {
					...initFormDataItem.text.producer,
					opacity: 100
				},
				time: {
					...initFormDataItem.text.time,
					opacity: 100
				},
				explain: {
					...initFormDataItem.text.explain,
					opacity: 100
				},
				explain2: {
					...initFormDataItem.text.explain2,
					opacity: 100
				},
				explain3: {
					...initFormDataItem.text.explain3,
					opacity: 100
				}
			},
			mapFactor: {
				modules: [
					{
						id: 1,
						label: '指北针',
						prop: 'compass'
					},
					{
						id: 2,
						label: '比例尺',
						prop: 'plottingScale',
						fontSize: 16
					},
					{
						id: 3,
						label: '图例',
						prop: 'legend'
					}
				],
				compass: {
					...initFormDataItem.mapFactor.compass,
					// value: compassImage,
					value: '',
					opacity: 100
				},
				plottingScale: {
					...initFormDataItem.mapFactor.plottingScale
				},
				legend: {
					...initFormDataItem.legend,
					type: 1,
					opacity: 100
				}
			}
		},
		paperProps: {
			scale: 1,
			style: {}
		},
		isPreview: false,
		saveType: 'add'
	}),
	actions: {
		setFormData({ type, data, key, value }) {
			if (type === 'all') {
				this.formData = data
			} else if (type === 'other') {
				const keys = key.split('.')
				if (keys.length === 1) {
					this.formData[key] = value
				} else {
					this.formData[keys[0]][keys[1]] = value
				}
			}
			localStorage.setItem('exportFormData', JSON.stringify(this.formData))
		},
		setPaperProps(data) {
			this.paperProps = data
			localStorage.setItem('paperProps', JSON.stringify(this.paperProps))
		},
		setPreview(data) {
			this.isPreview = data
		},
		setSaveType(data) {
			this.saveType = data
		}
	}
})
