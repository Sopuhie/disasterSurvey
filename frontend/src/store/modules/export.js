import { defineStore } from 'pinia'
import Translate from 'ol/interaction/Translate'
import buildLayer, { LayerType } from '../../components/map/primitive/layer/Layer'
import GeoJSONUtil from "@/components/map/primitive/util/GeojsonUtil.js";
import EsrijsonUtil from "@/components/map/primitive/util/EsrijsonUtil.js";
import { buildStyle } from '@/components/map/primitive/style/Style';

export const useExportStore = defineStore('export', {
	state: () => ({
		outmap: null,
		outputlyrname: 'OutputLayer',
		outputlyr: null,
		drawtype: 'Point',
		drawedGeometries: [],
		drawedTexts: [],
		drawedPoints: [],
		isexportinfo: {},
		textTranslate: null
	}),
	actions: {
		/**
		 * 初始化设置地图对象
		 */
		setOutMap(map) {
			this.outmap = map;
			this.outputlyr = buildLayer(LayerType.Vector, {
				name: this.outputlyrname,
			});
			this.outmap.lyrManager.addLayer(this.outputlyrname, this.outputlyr);
			this.enableTextTranslate()
		},
		/**
		 * 文字标注可拖拽移动（类似指南针 DOM 拖拽，这里对 OL 文字要素启用 Translate）
		 */
		enableTextTranslate() {
			this.disableTextTranslate()
			if (!this.outmap?.olMap || !this.outputlyr?.ol) return
			const translate = new Translate({
				layers: [this.outputlyr.ol],
				filter: feature => !!feature.get('styleId'),
				hitTolerance: 16
			})
			translate.on('translateend', evt => {
				evt.features.forEach(f => {
					const styleId = f.get('styleId')
					if (!styleId) return
					const geojson = GeoJSONUtil.writeFeatureObject(f)
					geojson.properties = Object.assign(geojson.properties || {}, { styleId })
					const idx = this.drawedTexts.findIndex(d => d.feature?.properties?.styleId === styleId)
					if (idx > -1) {
						this.drawedTexts[idx] = { id: this.drawedTexts[idx].id, feature: geojson }
					}
				})
			})
			this.outmap.olMap.addInteraction(translate)
			this.textTranslate = translate
		},
		disableTextTranslate() {
			if (this.textTranslate && this.outmap?.olMap) {
				this.outmap.olMap.removeInteraction(this.textTranslate)
			}
			this.textTranslate = null
		},
		/**
		 * 关闭出图模式（仅清理专题制图内部图层与状态，不影响主地图/数据管理）
		 */
		closeExport() {
			this.disableTextTranslate()
			if (this.outmap?.drawTool) {
				try {
					this.outmap.drawTool.finish?.()
					this.outmap.drawTool.clear?.()
				} catch (e) {
					// ignore
				}
			}
			if (this.outmap?.lyrManager) {
				this.outmap.lyrManager.removeLayer(this.outputlyrname)
			}
			this.outmap = null
			this.outputlyr = null
			this.drawedGeometries = []
			this.drawedTexts = []
			this.drawedPoints = []
			this.drawtype = 'Point'
			this.isexportinfo = {}
		},
		/**
		 * 绘制图形
		 */
		setDrawedGeometry(feature, style) {
			this.saveData(this.drawedGeometries, feature, style);
		},
		/**
		 * 绘制点
		 */
		setDrawedPoint(feature, style) {
			this.saveData(this.drawedPoints, feature, style);
		},
		/**
		 * 是否已有该 styleId 的文字要素
		 */
		hasTextFeature(styleId) {
			if (!styleId || !this.outputlyr?.ol) return false
			return this.outputlyr.ol.getSource().getFeatures().some(f => f.get('styleId') === styleId)
		},
		/**
		 * 组装文字标注样式
		 */
		buildTextAnnotateStyle(style) {
			if (style?.text && (style.text.text != null || style.text.font)) {
				return {
					text: {
						font: style.text.font || `${style.fontStyle || 'normal'} ${style.fontSize2 || 16}px sans-serif`,
						fill: style.text.fill || { color: style.color || '#0000ff' },
						text: style.text.text ?? style.text ?? '',
						stroke: style.text.stroke || {
							color: style.borderColor || '#ffffff',
							width: style.fontWeight || 2
						}
					}
				}
			}
			return {
				text: {
					font: `${style?.fontStyle || 'normal'} ${style?.fontSize2 || 16}px sans-serif`,
					fill: { color: style?.color || '#0000ff' },
					text: style?.text || '',
					stroke: {
						color: style?.borderColor || '#ffffff',
						width: style?.fontWeight || 2
					}
				}
			}
		},
		/**
		 * 绘制文本：同一 styleId 只保留一个要素，再次落点/编辑时更新而非重复添加
		 */
		setDrawedText(feature, style) {
			const styleId = style?.id
			feature.properties = Object.assign(feature.properties || {}, {
				styleId
			})
			const textStyle = this.buildTextAnnotateStyle(style)
			if (styleId && this.outputlyr?.ol) {
				const source = this.outputlyr.ol.getSource()
				const existing = source.getFeatures().find(f => f.get('styleId') === styleId)
				if (existing) {
					const olFeat = GeoJSONUtil.readFeature(feature)
					existing.setGeometry(olFeat.getGeometry())
					existing.set('styleId', styleId)
					existing.setStyle(buildStyle(textStyle))
					const idx = this.drawedTexts.findIndex(d => d.feature?.properties?.styleId === styleId)
					if (idx > -1) {
						this.drawedTexts[idx] = { id: this.drawedTexts[idx].id, feature }
					}
					return
				}
			}
			this.saveData(this.drawedTexts, feature, { ...textStyle, id: styleId })
		},
		/**
		 * 更新文本（字号/颜色/文案同步；列表中已删除的从图层移除）
		 */
		updateDrawedTexts(styles = []) {
			if (!this.outputlyr?.ol) return
			const source = this.outputlyr.ol.getSource()
			if (!source) return
			const styleIds = (styles || []).map(m => m.id).filter(Boolean)
			const feats = source.getFeatures() || []
			const txtFeats = feats.filter(f => f.get('styleId'))
			// 需要移除的 features
			txtFeats
				.filter(f => !styleIds.includes(f.get('styleId')))
				.forEach(f => source.removeFeature(f))
			// 同步仍存在的文本样式
			txtFeats
				.filter(f => styleIds.includes(f.get('styleId')))
				.forEach(f => {
					const style = styles.find(m => m.id == f.get('styleId'))
					if (!style) return
					f.setStyle(buildStyle(this.buildTextAnnotateStyle(style)))
				})
			// 同步内存列表
			this.drawedTexts = this.drawedTexts.filter(d =>
				styleIds.includes(d.feature?.properties?.styleId || d.styleId)
			)
		},
		/**
		 * 设置绘制类型
		 */
		setDrawType(type) {
			this.drawtype = type;
		},
		/**
		 * 存储数据
		 */
		saveData(array, feature, style) {
			let id = Date.parse(new Date());
			feature.properties = Object.assign(feature.properties || {}, {
				id: id
			});
			array.push({
				id,
				feature
			})
			this.saveFeatureToLyr(feature, style);
		},
		/**
		 * 添加数据到输出图层
		 */
		saveFeatureToLyr(feature, style) {
			if (this.outputlyr) {
				let olFeat = GeoJSONUtil.readFeature(feature)
				const styleId = style?.id || feature?.properties?.styleId
				if (styleId) {
					olFeat.set('styleId', styleId)
				}
				// 文字标注只应用 text 样式，避免把 id 等字段传入导致异常
				const olStyle = style?.text ? this.buildTextAnnotateStyle(style) : style
				olFeat.setStyle(buildStyle(olStyle))
				this.outputlyr.ol.getSource().addFeature(olFeat)
			}
		},
		/**
		 * 设置是否导出专题图
		 */
		setIsExportInfo(info) {
			this.isexportinfo = info;
		}
	},
	getters: {
		outMap() {
			return this.outmap;
		},
		outputLyr() {
			return this.outputlyr;
		},
		outputLyrName() {
			return this.outputlyrname;
		},
		isOpenExport() {
			return this.outmap != null;
		},
		drawType() {
			return this.drawtype;
		},
		isExportInfo() {
			return this.isexportinfo;
		}
	}
})
