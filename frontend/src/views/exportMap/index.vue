<template>
	<el-drawer
		v-model="drawerVisible"
		class="export-map-drawer"
		direction="rtl"
		size="calc(100% - 160px)"
		:with-header="false"
		:modal="true"
		:append-to-body="true"
		:close-on-click-modal="true"
		:close-on-press-escape="true"
		:destroy-on-close="false"
		:z-index="2800"
		@opened="syncMapAreaSize"
		@closed="onDrawerClosed">
	<div class="export-map-panel">
		<div class="map-area" ref="mapAreaRef">
			<div class="screenshotPop" :style="printSize()" ref="screenshotPop">
				<div class="screenshotBox max-wh">
					<p
						ref="titleRef"
						v-if="formData.title"
						class="title flex noWrap moveableTarget moveableTarget1"
						:style="[formData.titleStyle, getXyz('text.title')]"
						@mouseenter="handleMouseEnter(0)"
						@mouseleave="handleMouseLeave(0)">
						{{ formData.title }}
					</p>
					<Moveable
						ref="titleMoveableRef"
						:target="'.moveableTarget1'"
						:snappable="true"
						:snapContainer="'.screenshotBox'"
						:throttleDrag="1"
						:bounds="bounds"
						:resizable="resizable[0]"
						:origin="false"
						:draggable="true"
						@render="onRender" />
					<div class="screenshot max-wh flex faj" id="screenshotMap">
						<OLMap ref="map" :style="getXyz('map')" :getMap="getMapHandler" :mapOptions="MapDefaultOption"></OLMap>
						<div
							ref="scaleBoxRef"
							v-if="formData.element.includes('比例尺')"
							class="scaleBox flex faj fc moveableTarget2"
							:style="getXyz('mapFactor.plottingScale')"
							:class="formData.module"
							@mouseenter="handleMouseEnter(1)"
							@mouseleave="handleMouseLeave(1)">
							<p class="scale-rf">{{ scaleRfTxt }}</p>
							<img style="object-fit: contain; width: 40px; height: 4px" :src="scaleUrl" alt="" />
							<p class="scale-dist">{{ scaleTxt }}</p>
						</div>
						<Moveable
							ref="scaleBoxMoveableRef"
							:target="'.moveableTarget2'"
							:snappable="true"
							:snapContainer="'.screenshotBox'"
							:throttleDrag="1"
							:bounds="bounds"
							:resizable="resizable[1]"
							:origin="false"
							:draggable="true"
							@render="onRender" />
						<img
							ref="compassRef"
							v-if="formData.element.includes('指北针')"
							class="compassImg moveableTarget3"
							:class="formData.module"
							:style="[{ 'object-fit': 'contain' }, getXyz('mapFactor.compass')]"
							:src="compassUrl || defaultCompassUrl"
							alt="指北针"
							@error="compassUrl = defaultCompassUrl"
							@mouseenter="handleMouseEnter(2)"
							@mouseleave="handleMouseLeave(2)" />
						<Moveable
							ref="compassMoveableRef"
							:target="'.moveableTarget3'"
							:snappable="true"
							:snapContainer="'.screenshotBox'"
							:throttleDrag="1"
							:bounds="bounds"
							:resizable="resizable[2]"
							:origin="false"
							:draggable="true"
							@render="onRender" />
						<div
							ref="scaleRef"
							class="legend moveableTarget4"
							:style="getXyz('mapFactor.legend')"
							:class="formData.module"
							v-if="formData.element.includes('图例')"
							@mouseenter="handleMouseEnter(3)"
							@mouseleave="handleMouseLeave(3)">
							<ul class="flex fw">
								<template v-for="(item, index) in legendList">
									<li class="flex fa cp pt-4 pb-4 pl-8 pr-8" v-if="item.choose" :key="'legend_' + index + '_' + item.fontSize + '_' + item.fill">
										<img
											:src="getAssetsFile(item.icon)"
											alt="Selected Image"
											:style="{
												width: (Number(item.fontSize) || 18) + 'px',
												height: (Number(item.fontSize) || 18) + 'px',
												objectFit: 'contain'
											}" />
										<span
											class="ml-10"
											:style="{
												fontSize: (Number(item.fontSize) || 18) + 'px',
												color: item.fill || '#333',
												lineHeight: 1.2
											}">
											{{ item.title }}
										</span>
									</li>
								</template>
							</ul>
						</div>
						<Moveable
							ref="scaleMoveableRef"
							:target="'.moveableTarget4'"
							:snappable="true"
							:snapContainer="'.screenshotBox'"
							:throttleDrag="1"
							:bounds="bounds"
							:resizable="resizable[3]"
							:origin="false"
							:draggable="true"
							@render="onRender" />
						<p
							v-if="formData.producer"
							ref="producerRef"
							class="producer flex fa noWrap moveableTarget5"
							:style="getXyz('text.producer')"
							@mouseenter="handleMouseEnter(4)"
							@mouseleave="handleMouseLeave(4)">
							<span class="pucd noWrap">制作人：</span>
							{{ formData.producer }}
						</p>
						<Moveable
							v-if="formData.producer"
							ref="producerMoveableRef"
							:target="'.moveableTarget5'"
							:snappable="true"
							:snapContainer="'.screenshotBox'"
							:throttleDrag="1"
							:bounds="bounds"
							:resizable="resizable[4]"
							:origin="false"
							:draggable="true"
							@render="onRender" />
						<p
							v-if="formData.unit"
							ref="unitRef"
							class="unit flex fa noWrap moveableTarget6"
							:style="getXyz('text.explain3')"
							@mouseenter="handleMouseEnter(5)"
							@mouseleave="handleMouseLeave(5)">
							<span class="pucd noWrap">制作单位：</span>
							{{ formData.unit }}
						</p>
						<Moveable
							v-if="formData.unit"
							ref="unitMoveableRef"
							:target="'.moveableTarget6'"
							:snappable="true"
							:snapContainer="'.screenshotBox'"
							:throttleDrag="1"
							:bounds="bounds"
							:resizable="resizable[5]"
							:origin="false"
							:draggable="true"
							@render="onRender" />
						<p
							v-if="formData.createTime"
							ref="createTimeRef"
							class="createTime flex fa noWrap moveableTarget7"
							:style="getXyz('text.time')"
							@mouseenter="handleMouseEnter(6)"
							@mouseleave="handleMouseLeave(6)">
							<span class="pucd">制作时间：</span>
							{{ formData.createTime }}
						</p>
						<Moveable
							v-if="formData.createTime"
							ref="createTimeMoveableRef"
							:target="'.moveableTarget7'"
							:snappable="true"
							:snapContainer="'.screenshotBox'"
							:throttleDrag="1"
							:bounds="bounds"
							:resizable="resizable[6]"
							:origin="false"
							:draggable="true"
							@render="onRender" />
						<p
							v-if="formData.description"
							ref="descriptionRef"
							class="description flex noWrap moveableTarget8"
							:style="getXyz('text.explain')"
							@mouseenter="handleMouseEnter(7)"
							@mouseleave="handleMouseLeave(7)">
							<span class="pucd">说明：</span>
							{{ formData.description }}
						</p>
						<Moveable
							v-if="formData.description"
							ref="descriptionMoveableRef"
							:target="'.moveableTarget8'"
							:snappable="true"
							:snapContainer="'.screenshotBox'"
							:throttleDrag="1"
							:bounds="bounds"
							:resizable="resizable[7]"
							:origin="false"
							:draggable="true"
							@render="onRender" />
					</div>
				</div>
			</div>
		</div>
	<div class="rightPop">
		<div class="head zDialog__header noSelect flex fa jsb">
			<span style="font-weight: bold; font-size: 16px">专题出图</span>
			<el-icon class="cp" size="20" @click="closeDrawer"><Close /></el-icon>
		</div>
		<div class="tabs-bar flex fa jsb p-12 pb-0">
			<el-button-group class="ml-4">
				<el-button v-btn :type="curStep === 1 ? 'primary' : ''" @click="curStep = 1">在线制图</el-button>
				<el-button v-btn :type="curStep === 2 ? 'primary' : ''" @click="curStep = 2">出图日志</el-button>
			</el-button-group>
			<el-icon class="cp" size="18" @click="showModulesPop = true"><Setting /></el-icon>
		</div>
		<div class="con oy" v-if="curStep === 1">
			<template v-if="step === 1">
				<div class="con_item" style="height: 118px">
					<div class="item_title flex fa jsb">
						<span class="title">图纸设置</span>
						<span></span>
					</div>
					<div class="p-12">
						<el-form :model="formData" label-width="auto" status-icon>
							<el-form-item label="模板：">
								<el-select v-model="formData.module">
									<el-option v-for="item in lists" :key="item.icon" :label="item.label" :value="item.icon" />
								</el-select>
							</el-form-item>
						</el-form>
					</div>
				</div>
				<div class="con_item con_item2 oy" style="height: calc(100% - 119px)">
					<div class="item_title flex fa jsb">
						<span class="title">地图标注</span>
						<el-icon class="cp" color="#9FD1FF" @click="restBz"><Delete /></el-icon>
					</div>
					<div class="p-12">
						<el-form :model="formData" label-width="auto" status-icon>
							<el-form-item label="符号标注：">
								<el-select v-model="formData.symbolsSize" style="width: 60px">
									<el-option v-for="item in symbolsOptions" :key="item.value" :label="item.label" :value="item.value" />
								</el-select>
							</el-form-item>
							<el-form-item>
								<ul class="symbolsBox flex fw ml-24">
									<li
										class="flex faj cp mb-10 mr-6"
										:class="{ cur: symbolsIdx === index }"
										v-for="(item, index) in symbolsList"
										:key="'dot_' + item.id"
										@click="symbolsChange(item, index)">
										<el-tooltip effect="dark" :content="item.label" placement="top">
											<img
												:src="svgToDataUrl(item.svg)"
												style="width: 28px; height: 28px; object-fit: contain"
												:alt="item.label" />
										</el-tooltip>
									</li>
								</ul>
							</el-form-item>
							<el-form-item label="图形标注：">
								<ul class="graphicsBox flex fw">
									<li
										class="cp flex faj mr-12 mb-10"
										:class="{ cur: formData.graphics === item.value }"
										v-for="item in graphicsList"
										:key="'graphics_' + item.id"
										@click="graphicsChange(item)">
										<el-tooltip effect="dark" :content="item.label" placement="top">
											<img
												class="cp"
												style="width: 28px; height: 28px; object-fit: contain"
												:src="svgToDataUrl(item.svg)"
												:alt="item.label" />
										</el-tooltip>
									</li>
								</ul>
							</el-form-item>
							<el-form-item label="填充：">
								<div class="fill-colors flex fa fw">
									<span
										v-for="c in commonFillColors"
										:key="c"
										class="fill-swatch cp"
										:class="{ cur: isSameFill(c), light: isLightFill(c) }"
										:style="{ background: c }"
										:title="c"
										@click="selectFill(c)"></span>
									<span class="fill-more cp flex faj" title="更多颜色">
										<span
											class="fill-swatch more-preview"
											:class="{ light: isLightFill(formData.fill) }"
											:style="{ background: formData.fill || '#ffffff' }"></span>
										<span class="more-txt">更多</span>
										<el-color-picker
											v-model="formData.fill"
											show-alpha
											:predefine="commonFillColors"
											@change="fillChange" />
									</span>
								</div>
							</el-form-item>
							<el-form-item label="边框：">
								<el-input-number
									v-model="formData.border2"
									size="small"
									:min="2"
									controls-position="right"
									style="width: 70px"
									@change="borderChange" />
								<span class="ml-10">mm</span>
							</el-form-item>
							<el-form-item label="文字标注：">
								<el-button v-btn @click="addText">
									<el-icon><Plus /></el-icon>
									<span>添加文字标注</span>
								</el-button>
							</el-form-item>
							<el-form-item v-if="formData.texts.length > 0">
								<ul>
									<li class="flex fa mb-10" v-for="(item, index) in formData.texts" :key="item.id || 'text_' + index">
										<el-input
											:ref="el => setTextInputRef(el, index)"
											v-model="item.text"
											size="small"
											placeholder="请输入文字"
											style="width: 160px"
											@input="onTextAnnotateInput(item)" />
										<el-input-number
											class="ml-10"
											v-model="item.fontSize2"
											size="small"
											:min="9"
											controls-position="right"
											style="width: 70px"
											@change="syncTextAnnotate" />
										<span class="ml-10">pt</span>
										<div class="text_color flex faj fc ml-15 cp">
											<span>A</span>
											<p class="bg_color" :style="{ background: item.color }"></p>
											<el-color-picker v-model="item.color" size="small" @change="syncTextAnnotate" />
										</div>
										<p class="text_del flex faj ml-12">
											<el-icon @click="delText(index)"><Minus /></el-icon>
										</p>
									</li>
								</ul>
							</el-form-item>
							<!-- <el-form-item label="字体样式：">
								<div class="text_style">
									<span class="b" @click="setStyle('B')">B</span>
									<span class="i" @click="setStyle('I')">I</span>
									<span class="u" @click="setStyle('U')">U</span>
								</div>
							</el-form-item> -->
						</el-form>
					</div>
				</div>
			</template>
			<template v-else>
				<div class="con_item">
					<div class="item_title flex fa jsb">
						<span class="title">图件设置</span>
						<span></span>
					</div>
					<div class="p-12">
						<el-form :model="formData" label-width="auto" status-icon>
							<el-form-item label="标题：">
								<div class="flex fa max-w">
									<el-input v-model="formData.title" class="mr-10" />
									<el-tooltip effect="dark" content="复位" placement="top">
										<el-button v-btn :icon="Refresh" link type="primary" @click="refreshHandler('title')" />
									</el-tooltip>
								</div>
							</el-form-item>
							<el-form-item label="标题样式：">
								<div class="text_style flex fa">
									<span class="b" @click="setTitleStyle('B')">B</span>
									<span class="i" @click="setTitleStyle('I')">I</span>
									<span class="u" @click="setTitleStyle('U')">U</span>
									<el-input-number
										class="mr-10"
										v-model="titleFontSize"
										:min="10"
										:max="72"
										style="width: 80px"
										controls-position="right"
										size="small"
										@change="handleFontSizeChange" />
									<el-color-picker v-model="formData.titleStyle.color" />
								</div>
							</el-form-item>
							<el-form-item label="制作人：">
								<div class="flex fa max-w">
									<el-input class="mr-10" v-model="formData.producer" />
									<el-tooltip effect="dark" content="复位" placement="top">
										<el-button v-btn :icon="Refresh" link type="primary" @click="refreshHandler('producer')" />
									</el-tooltip>
								</div>
							</el-form-item>
							<el-form-item label="制作单位：">
								<div class="flex fa max-w">
									<el-input class="mr-10" v-model="formData.unit" />
									<el-tooltip effect="dark" content="复位" placement="top">
										<el-button v-btn :icon="Refresh" link type="primary" @click="refreshHandler('unit')" />
									</el-tooltip>
								</div>
							</el-form-item>
							<el-form-item label="制作时间：">
								<div class="flex fa max-w">
									<el-date-picker
										class="mr-10"
										v-model="formData.createTime"
										type="date"
										format="YYYY-MM-DD"
										value-format="YYYY-MM-DD"
										style="width: 92%" />
									<el-tooltip effect="dark" content="复位" placement="top">
										<el-button v-btn :icon="Refresh" link type="primary" @click="refreshHandler('createTime')" />
									</el-tooltip>
								</div>
							</el-form-item>
							<el-form-item label="说明：">
								<div class="flex fa max-w">
									<el-input class="mr-10" v-model="formData.description" />
									<el-tooltip effect="dark" content="复位" placement="top">
										<el-button v-btn :icon="Refresh" link type="primary" @click="refreshHandler('description')" />
									</el-tooltip>
								</div>
							</el-form-item>
						</el-form>
					</div>
				</div>
				<div class="con_item con_item2">
					<div class="item_title flex fa jsb">
						<span class="title">图件打印</span>
						<span></span>
					</div>
					<div class="p-12">
						<el-form :model="formData" label-width="auto" status-icon>
							<el-form-item label="要素启用：">
								<el-checkbox-group v-model="formData.element">
									<el-checkbox label="指北针" value="指北针" />
									<el-checkbox label="比例尺" value="比例尺" />
									<el-checkbox label="图例" value="图例" />
									<el-checkbox label="经纬网" value="经纬网" />
								</el-checkbox-group>
							</el-form-item>
							<el-form-item label="指北针要素：">
								<div class="flex fa">
									<ul class="symbolsBox flex fw mr-10">
										<li
											class="flex faj cp mr-6"
											:class="{ cur: formData.compass === item.url }"
											v-for="item in compassList"
											:key="'compass_' + item.label"
											@click="compassChange(item)">
											<img class="cp" style="width: 28px; height: 28px; object-fit: contain" :src="item.url" alt="" />
										</li>
										<li class="cp flex fa_end">
											<el-tooltip effect="dark" content="设置" placement="top">
												<el-icon @click="openSymbolSetting('compass')"><Tools /></el-icon>
											</el-tooltip>
										</li>
									</ul>
									<el-tooltip effect="dark" content="复位" placement="top">
										<el-button v-btn :icon="Refresh" link type="primary" @click="refreshHandler('compass')" />
									</el-tooltip>
								</div>
							</el-form-item>
							<el-form-item label="比例尺：">
								<div class="flex fa max-w">
									<el-select class="mr-10" v-model="formData.scale" style="width: 80%">
										<el-option v-for="item in scaleOptions" :key="item.value" :label="item.label" :value="item.value" />
									</el-select>
									<el-tooltip effect="dark" content="设置" placement="top">
										<el-icon class="cp" @click="openSymbolSetting('scale2')"><Tools /></el-icon>
									</el-tooltip>
								</div>
							</el-form-item>
							<el-form-item label="比例尺图标：">
								<div class="flex fa max-w">
									<ul class="symbolsBox flex fw mr-10">
										<li
											class="flex faj cp mr-6"
											:class="{ cur: formData.scale === item }"
											v-for="item in scaleList"
											:key="'scale_' + item"
											@click="compassChange(item)">
											<img class="cp" style="width: 28px; height: 28px; object-fit: contain" :src="item.url" alt="" />
										</li>
										<li class="cp flex fa_end">
											<el-tooltip effect="dark" content="设置" placement="top">
												<el-icon @click="openSymbolSetting('scale')"><Tools /></el-icon>
											</el-tooltip>
										</li>
									</ul>
									<el-tooltip effect="dark" content="复位" placement="top">
										<el-button v-btn :icon="Refresh" link type="primary" @click="refreshHandler('scaleBox')" />
									</el-tooltip>
								</div>
							</el-form-item>
							<!-- <el-form-item label="经纬度要素：">
								<el-input-number v-model="formData.longitude" :min="1" :max="10" style="width: 126px" />
								<el-select class="ml-10" v-model="formData.latitude" style="width: 70px">
									<el-option label="°" value="°"></el-option>
									<el-option label="′" value="′"></el-option>
									<el-option label="″" value="″"></el-option>
								</el-select>
								<p style="color: #f45555">注：整数(1~10)</p>
							</el-form-item> -->
						</el-form>
						<div class="flex fa">
							<p class="openLegend cp mr-10" @click="showLegendChange = true">图例选择</p>
							<el-tooltip effect="dark" content="复位" placement="top">
								<el-button v-btn :icon="Refresh" link type="primary" @click="refreshHandler('scale')" />
							</el-tooltip>
						</div>
						<div class="flex fa mt-10">
							<p class="cp mr-10">全部复位</p>
							<el-button v-btn :icon="Refresh" link type="primary" @click="refreshHandler('all')" />
						</div>
					</div>
				</div>
			</template>
		</div>
		<div class="float flex faj" v-if="curStep === 1">
			<el-button v-btn type="primary" @click="floatBtnHandle('step')">{{ step === 1 ? '下一步' : '上一步' }}</el-button>
			<el-button v-btn type="primary" v-show="step === 2" @click="floatBtnHandle('ok')">图片输出</el-button>
		</div>
		<div class="logCon p-12" v-if="curStep === 2">
			<LogConCom ref="LogConComRef"></LogConCom>
		</div>
	</div>
	</div>
	</el-drawer>
	<!-- 子弹窗挂到 body，避免被 drawer 蒙版压住 -->
	<Teleport to="body">
	<!-- 图例选择弹窗 -->
	<div class="legendChangeMask" v-if="showLegendChange">
		<div class="legendPop">
			<div class="head zDialog__header flex fa jsb pl-10 pr-10">
				<span class="title">图例选择</span>
				<p class="flex fa cp">
					<el-icon @click="showLegendChange = false"><Close /></el-icon>
				</p>
			</div>
			<div class="con p-10">
				<div class="conBody oy" ref="legendConRef">
					<ul class="conHead legend-grid">
						<li></li>
						<li>图标</li>
						<li>名称</li>
						<li>颜色</li>
						<li>大小</li>
						<li>删除</li>
					</ul>
					<ul ref="legendListRef">
						<li class="legend-grid legend-row mb-20" v-for="(item, index) in legendList" :key="index">
							<div class="legend-cell">
								<el-checkbox v-model="item.choose" />
							</div>
							<div class="legend-cell legend-row-field">
								<el-select
									class="demo-select"
									v-model="item.icon"
									style="width: 66px"
									teleported
									placement="bottom-start"
									:popper-options="{ strategy: 'fixed' }"
									popper-class="legend-popper-above">
									<template #prefix>
										<img :src="getAssetsFile(item.icon)" alt="Selected Image" width="20" height="20" />
									</template>
									<el-option class="flex faj" v-for="l in legendIconList" :key="l.id" :label="l.label" :value="l.value">
										<img :src="getAssetsFile(l.value)" alt="Selected Image" width="20" height="20" />
									</el-option>
								</el-select>
							</div>
							<div class="legend-cell">
								<el-input v-model="item.title" style="width: 100%" />
							</div>
							<div class="legend-cell legend-row-field">
								<el-color-picker
									v-model="item.fill"
									popper-class="legend-popper-above"
									@change="onLegendStyleChange" />
							</div>
							<div class="legend-cell legend-size">
								<el-input-number
									v-model="item.fontSize"
									:min="9"
									:max="72"
									:step="1"
									controls-position="right"
									style="width: 90px"
									@change="onLegendStyleChange" />
								<span class="ml-2">px</span>
							</div>
							<div class="legend-cell">
								<el-icon class="cp" size="22" color="#f45555" @click="delLegendList(index)"><RemoveFilled /></el-icon>
							</div>
						</li>
					</ul>
					<div class="flex fa cp" style="width: 52px; margin-left: 8px" @click="addLegendList">
						<el-icon color="#31A3FE"><CirclePlusFilled /></el-icon>
						<span style="color: #31a3fe; margin-left: 10px">添加</span>
					</div>
				</div>
			</div>
			<div class="footer flex fa jend">
				<el-button v-btn type="primary" @click="showLegendChange = false">确认</el-button>
				<el-button v-btn @click="showLegendChange = false">取消</el-button>
			</div>
		</div>
	</div>
	<!-- 图标管理弹窗 -->
	<div class="legendChangeMask" v-if="showSymbolsChange">
		<div class="legendPop symbolsPop">
			<div class="head zDialog__header flex fa jsb pl-10 pr-10">
				<span class="title">图标管理</span>
				<p class="flex fa cp">
					<el-icon @click="showSymbolsChange = false"><Close /></el-icon>
				</p>
			</div>
			<div class="con p-10">
				<ul class="flex fw">
					<el-upload
						v-model:file-list="symbolsListArr"
						list-type="picture-card"
						:http-request="uploadImage"
						:before-remove="beforeRemove">
						<el-icon><Plus /></el-icon>
					</el-upload>
				</ul>
			</div>
		</div>
	</div>
	<!-- 比例尺管理弹窗 -->
	<div class="legendChangeMask" v-if="showScaleChange">
		<div class="legendPop scalePop">
			<div class="head zDialog__header flex fa jsb pl-10 pr-10">
				<span class="title">比例尺管理</span>
				<p class="flex fa cp">
					<el-icon @click="showScaleChange = false"><Close /></el-icon>
				</p>
			</div>
			<div class="con p-10 oy">
				<ul class="flex jsb fw">
					<li class="mb-12 max-w flex fa" v-for="(item, index) in scaleOptions" :key="'scaleOptions_' + index">
						<el-input v-model="item['value']" style="width: 220px" @change="scaleOptionChange(item, index)"></el-input>
						<el-icon class="ml-10 cp" color="#5fe951" size="20" @click="addScaleOption(item, index)">
							<CirclePlusFilled />
						</el-icon>
						<el-icon class="ml-10 cp" color="#f45555" size="20" @click="delScaleOption(index)">
							<RemoveFilled />
						</el-icon>
					</li>
				</ul>
			</div>
		</div>
	</div>
	<div class="legendChangeMask" v-if="showOutputPop">
		<OutputPop @close="showOutputPop = false" @save="addOutput"></OutputPop>
	</div>
	<!-- 模样 -->
	<ModulesCom @cancel="showModulesPop = false" v-if="showModulesPop"></ModulesCom>
	</Teleport>
</template>

<script setup>
	import { Refresh } from '@element-plus/icons-vue'
	import Moveable from 'vue3-moveable'
	import OutputPop from '@/views/exportMap/outputPop.vue'
	import OLMap from '@/components/map/components/Map.vue'
	import LogConCom from '@/views/exportMap/logCon.vue'
	import ModulesCom from '@/views/exportMap/modules/index.vue'
	import { MapDefaultOption } from '/public/map.js'
	import { getWindowSize } from '@/utils/index.js'
	import axios from 'axios'
	import {
		symbolsOptions,
		legendIconList,
		resetFormData,
		fixedSymbolList,
		fixedGraphicsList,
		svgToDataUrl,
		commonFillColors
	} from './tool.js'
	import { paperTypeSize } from '@/views/exportMap/modules/constant.js'
	import { MapInchesPerUnit, MapDotsPerInch, GeoCoordSystemCodes } from '@/components/map/primitive/config/MapDefaultConfig.js'
	import defaultCompassUrl from './imgs/compass.svg?url'

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()
	import { useExportStore } from '@/store/modules/export.js'
	const exportStore = useExportStore()
	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()
	import { useExportMapStore } from '@/store/modules/exportMap.js'
	const emStore = useExportMapStore()

	import { drawGeometry, drawPoint, drawText, drawGraticule, exportMap } from '@/views/exportMap/mapOperation/index.js'
	import emitter from '@/components/map/utils/eventbus.js'
	import { reactive, ref } from 'vue'

	import { mappingApi } from '@/views/exportMap/api.js'
	const api = mappingApi()

	const router = useRouter()

	const titleMoveableRef = ref(null)
	const compassMoveableRef = ref(null)
	const scaleMoveableRef = ref(null)
	const scaleBoxMoveableRef = ref(null)
	const producerMoveableRef = ref(null)
	const unitMoveableRef = ref(null)
	const createTimeMoveableRef = ref(null)
	const descriptionMoveableRef = ref(null)
	const LogConComRef = ref(null)
	const titleRef = ref(null)
	const compassRef = ref(null)
	const scaleRef = ref(null)
	const scaleBoxRef = ref(null)
	const producerRef = ref(null)
	const unitRef = ref(null)
	const createTimeRef = ref(null)
	const descriptionRef = ref(null)
	const hoverTimer = ref(null)
	const curStep = ref(1)
	const showModulesPop = ref(false)
	const drawerVisible = ref(true)
	const mapAreaRef = ref(null)
	const mapAreaSize = ref({ w: 0, h: 0 })
	const syncMapAreaSize = () => {
		const area = mapAreaRef.value
		if (!area) return
		mapAreaSize.value = { w: area.clientWidth, h: area.clientHeight }
	}
	const resizable = ref([false, false, false, false, false, false, false, false])
	const bounds = { left: 0, top: 0, right: 0, bottom: 0, position: 'css' }
	const onRender = e => {
		e.target.style.cssText += e.cssText
	}

	// 鼠标进入时启用 resizable
	const handleMouseEnter = i => {
		clearTimeout(hoverTimer.value)
		resizable.value = [false, false, false, false, false, false, false, false]
		resizable.value[i] = true
	}
	// 鼠标离开时禁用 resizable
	const handleMouseLeave = i => {
		clearTimeout(hoverTimer.value)
		hoverTimer.value = setTimeout(() => {
			resizable.value[i] = false
		}, 1000)
	}
	const refreshHandler = async ref => {
		switch (ref) {
			case 'title':
				titleRef.value.style.transform = 'translate(0px, 0px)'
				await nextTick()
				titleMoveableRef.value?.updateRect()
				break
			case 'compass':
				compassRef.value.style.transform = 'translate(0px, 0px)'
				compassRef.value.style.width = '28px'
				compassRef.value.style.height = '28px'
				await nextTick()
				compassMoveableRef.value?.updateRect()
				break
			case 'scale':
				scaleRef.value.style.transform = 'translate(0px, 0px)'
				scaleRef.value.style.width = '100px'
				scaleRef.value.style.height = 'unset'
				await nextTick()
				scaleMoveableRef.value?.updateRect()
				break
			case 'scaleBox':
				scaleBoxRef.value.style.transform = 'translate(0px, 0px)'
				scaleBoxRef.value.style.width = '60px'
				scaleBoxRef.value.style.height = '34px'
				await nextTick()
				scaleBoxMoveableRef.value?.updateRect()
				break
			case 'producer':
				producerRef.value.style.transform = 'translate(0px, 0px)'
				await nextTick()
				producerMoveableRef.value?.updateRect()
				break
			case 'unit':
				unitRef.value.style.transform = 'translate(0px, 0px)'
				await nextTick()
				unitMoveableRef.value?.updateRect()
				break
			case 'createTime':
				createTimeRef.value.style.transform = 'translate(0px, 0px)'
				await nextTick()
				createTimeMoveableRef.value?.updateRect()
				break
			case 'description':
				descriptionRef.value.style.transform = 'translate(0px, 0px)'
				await nextTick()
				descriptionMoveableRef.value?.updateRect()
				break
			case 'all':
				titleRef.value.style.transform = 'translate(0px, 0px)'
				await nextTick()
				titleMoveableRef.value?.updateRect()
				compassRef.value.style.transform = 'translate(0px, 0px)'
				compassRef.value.style.width = '28px'
				compassRef.value.style.height = '28px'
				await nextTick()
				compassMoveableRef.value?.updateRect()
				scaleRef.value.style.transform = 'translate(0px, 0px)'
				scaleRef.value.style.width = '100px'
				scaleRef.value.style.height = 'unset'
				await nextTick()
				scaleMoveableRef.value?.updateRect()
				scaleBoxRef.value.style.transform = 'translate(0px, 0px)'
				scaleBoxRef.value.style.width = '60px'
				scaleBoxRef.value.style.height = '34px'
				await nextTick()
				scaleBoxMoveableRef.value?.updateRect()
				if (producerRef.value) {
					producerRef.value.style.transform = 'translate(0px, 0px)'
					await nextTick()
					producerMoveableRef.value?.updateRect()
				}
				if (unitRef.value) {
					unitRef.value.style.transform = 'translate(0px, 0px)'
					await nextTick()
					unitMoveableRef.value?.updateRect()
				}
				if (createTimeRef.value) {
					createTimeRef.value.style.transform = 'translate(0px, 0px)'
					await nextTick()
					createTimeMoveableRef.value?.updateRect()
				}
				if (descriptionRef.value) {
					descriptionRef.value.style.transform = 'translate(0px, 0px)'
					await nextTick()
					descriptionMoveableRef.value?.updateRect()
				}
				break
			default:
				break
		}
	}

	const step = ref(1)
	const titleFontSize = ref(30)
	const formData = ref({
		module: 'default',
		direction: 'h',
		size: 'A3',
		symbols: '',
		symbolsSize: '中',
		graphics: '',
		texts: [],
		border2: 1,
		borderColor: '#1BBCFF',
		fill: '#ffffff',
		title: '专题出图标题',
		producer: '',
		unit: '',
		createTime: new Date().toISOString().slice(0, 10),
		description: '简述信息',
		element: ['指北针', '比例尺', '图例'],
		compass: defaultCompassUrl,
		scale: '',
		longitude: 1,
		latitude: '°',
		titleStyle: { fontSize: '30px', fontWeight: 600, fontStyle: 'normal', textDecoration: 'none', color: '#000000' }
	})

	const symbolsIdx = ref(-1)
	const showSymbolsChange = ref(false)
	const showScaleChange = ref(false)
	const symbolsUploadType = ref('')
	const compassUrl = ref(defaultCompassUrl)
	const scaleUrl = ref('')
	const symbolsListArr = ref([])
	/** 符号/图形标注使用前端固定矢量图标，不再依赖后端上传库 */
	const symbolsList = ref(fixedSymbolList)
	const graphicsList = ref(fixedGraphicsList)
	const compassList = ref([{ url: defaultCompassUrl, label: '默认指北针' }])
	const scaleList = ref([])
	const scaleOptions = ref([
		{ label: '1:5000', value: '1:5000' },
		{ label: '1:10000', value: '1:10000' },
		{ label: '1:25000', value: '1:25000' },
		{ label: '1:50000', value: '1:50000' },
		{ label: '1:100000', value: '1:100000' },
		{ label: '1:250000', value: '1:250000' },
		{ label: '1:500000', value: '1:500000' },
		{ label: '1:1000000', value: '1:1000000' },
		{ label: '1:2500000', value: '1:2500000' },
		{ label: '1:5000000', value: '1:5000000' }
	])
	const legendList = reactive([
		{ title: '图例1', fill: '#1BBCFF', fontSize: 18, choose: false, icon: 'legend_1' },
		{ title: '图例2', fill: '#FFC107', fontSize: 18, choose: false, icon: 'legend_2' },
		{ title: '图例3', fill: '#FF5722', fontSize: 18, choose: false, icon: 'legend_3' },
		{ title: '图例4', fill: '#9C27B0', fontSize: 18, choose: false, icon: 'legend_4' },
		{ title: '图例5', fill: '#673AB7', fontSize: 18, choose: false, icon: 'legend_5' }
	])
	const legendConRef = ref(null)
	const legendListRef = ref(null)
	const showLegendChange = ref(false)
	const onLegendStyleChange = () => {
		nextTick(() => scaleMoveableRef.value?.updateRect?.())
	}
	watch(showLegendChange, open => {
		document.body.classList.toggle('legend-dialog-open', !!open)
	})
	onUnmounted(() => {
		document.body.classList.remove('legend-dialog-open')
	})

	const lists = ref([{ label: '默认模板', icon: 'A4_a' }])

	const showOutputPop = ref(false)

	const screenshotPop = ref(null)
	const scaleTxt = ref('')
	const scaleRfTxt = ref('1:1000000')
	let scaleSyncing = false
	let scaleView = null

	const niceLength = meters => {
		if (!Number.isFinite(meters) || meters <= 0) return 0
		const mag = Math.pow(10, Math.floor(Math.log10(meters)))
		const norm = meters / mag
		const niceNorm = norm >= 7.5 ? 10 : norm >= 3.5 ? 5 : norm >= 1.5 ? 2 : 1
		return niceNorm * mag
	}

	const formatBarText = meters => {
		const nice = niceLength(meters)
		if (!nice) return ''
		if (nice >= 1000) {
			const km = nice / 1000
			return Number.isInteger(km) ? `${km}公里` : `${parseFloat(km.toFixed(1))}公里`
		}
		return `${Math.round(nice)}米`
	}

	const updateScaleFromMap = () => {
		const map = exportStore.outMap
		const view = map?.olView
		if (!view) return
		const resolution = view.getResolution()
		if (!resolution) return
		const projection = view.getProjection()
		let units = projection?.getUnits?.()
		if (!units) {
			units = GeoCoordSystemCodes.includes(projection?.getCode?.()) ? 'degrees' : 'm'
		}
		const rf = resolution * (MapInchesPerUnit[units] || MapInchesPerUnit.m) * MapDotsPerInch
		const rfNum = Math.max(1, Math.round(rf))
		scaleRfTxt.value = `1:${rfNum.toLocaleString('en-US')}`
		const metersPerUnit = projection?.getMetersPerUnit?.() || (units === 'degrees' ? 111319.49 : 1)
		const barPx = Math.max(scaleBoxRef.value?.clientWidth || 0, 40)
		scaleTxt.value = formatBarText(resolution * metersPerUnit * barPx)
		const nextScale = `1:${rfNum}`
		if (formData.value.scale !== nextScale) {
			scaleSyncing = true
			formData.value.scale = nextScale
			nextTick(() => {
				scaleSyncing = false
			})
		}
	}

	const unbindScaleListener = () => {
		if (scaleView) {
			scaleView.un('change:resolution', updateScaleFromMap)
			scaleView = null
		}
		exportStore.outMap?.olMap?.un('moveend', updateScaleFromMap)
	}

	const bindScaleListener = map => {
		unbindScaleListener()
		if (!map?.olView) return
		scaleView = map.olView
		scaleView.on('change:resolution', updateScaleFromMap)
		map.olMap?.on('moveend', updateScaleFromMap)
		updateScaleFromMap()
	}

	const onExportTextPlaced = id => {
		const item = formData.value.texts.find(t => t.id === id)
		if (item) item.placed = true
	}

	onMounted(() => {
		getModuleList()
		emitter.on('exportTextPlaced', onExportTextPlaced)
		nextTick(() => {
			syncMapAreaSize()
			window.addEventListener('resize', syncMapAreaSize)
		})
	})

	/**
	 * 获取模样数据
	 */
	const getModuleList = () => {
		const form = {
			current: 0,
			size: 0,
			eq_isshare: '',
			like_name: '',
			loginUser: appStore.userInfo?.username || 'system'
		}
		api
			.queryWithPage(form)
			.then(res => {
				if (res.resultCode === 'S') {
					if (res.data?.length > 0) {
						const data = [{ label: '默认模板', icon: 'default' }]
						res?.data?.forEach(item => {
							const content = JSON.parse(item.content)
							item.icon = content.paper.type
							item.label = item.name
						})
						lists.value = data.concat(res.data)
					}
				}
			})
			.catch(err => {
				// ElMessage({ message: err, type: 'error' })
			})
	}

	/**
	 * 设置打印纸张尺寸，按纸张比例适配 drawer 内地图区域
	 */
	const printSize = () => {
		const win = getWindowSize()
		const maxW = Math.max(360, mapAreaSize.value.w ? mapAreaSize.value.w - 48 : win.w.value - 160 - 366 - 48)
		const maxH = Math.max(260, mapAreaSize.value.h ? mapAreaSize.value.h - 48 : win.h.value - 48)
		const paperKey = emStore.formData?.paper?.type || (formData.value.direction === 'v' ? 'A4_c' : 'A4_a')
		const paper = paperTypeSize[paperKey] || paperTypeSize.A4_a
		const aspect = paper.w / paper.h
		let width = maxW
		let height = width / aspect
		if (height > maxH) {
			height = maxH
			width = height * aspect
		}
		return {
			width: Math.round(width) + 'px',
			height: Math.round(height) + 'px'
		}
	}
	const getLayoutItem = type => {
		const keys = String(type || '').split('.')
		if (keys.length === 1) return emStore.formData?.[type]
		return emStore.formData?.[keys[0]]?.[keys[1]]
	}
	const getXyz = type => {
		const paperKey = emStore.formData?.paper?.type || (formData.value.direction === 'v' ? 'A4_c' : 'A4_a')
		const paper = paperTypeSize[paperKey] || paperTypeSize.A4_a
		const box = printSize()
		const boxW = parseFloat(box.width) || paper.w
		const boxH = parseFloat(box.height) || paper.h
		const scaleX = boxW / paper.w
		const scaleY = boxH / paper.h
		const f = getLayoutItem(type) || {}
		const width = Number(f.width) || 0
		const height = Number(f.height) || 0
		const x = Number(f.x) || 0
		const y = Number(f.y) || 0
		return {
			position: 'absolute',
			boxSizing: 'border-box',
			width: `${Math.max(width * scaleX, 24)}px`,
			height: `${Math.max(height * scaleY, 18)}px`,
			left: `${x * scaleX}px`,
			top: `${y * scaleY}px`,
			zIndex: f.z || 1
		}
	}

	watch(
		() => formData.value.module,
		val => {
			if (['default', 'A3_a', 'A4_a'].includes(val)) {
				formData.value.direction = 'h'
			} else {
				formData.value.direction = 'v'
			}
			let itemList = []
			if ('default' === val) {
				resetFormData()
			} else {
				itemList = lists.value.filter(item => item.icon === val)
				if (itemList.length > 0) {
					emStore.setFormData({
						type: 'all',
						data: { id: itemList[0]['id'], ...JSON.parse(itemList[0]['content']) }
					})
				}
			}
		},
		{ immediate: true }
	)

	const syncTextAnnotate = () => {
		const list = formData.value.texts || []
		list.forEach(item => {
			const next = parseInt(item.fontSize2 || 16) * 1.333 + 'px'
			if (item.fontSize !== next) item.fontSize = next
		})
		exportStore.updateDrawedTexts(list)
	}

	watch(
		() => formData.value.texts,
		() => syncTextAnnotate(),
		{ deep: true }
	)

	watch(
		() => formData.value.element,
		val => {
			drawGraticule(val?.length && val.includes('经纬网'))
		},
		{ immediate: true, deep: true }
	)

	watch(
		() => formData.value.scale,
		val => {
			if (scaleSyncing || !val || !exportStore.outMap) return
			exportStore.outMap.flyToManager.flyToScale(val.slice(2))
		}
	)

	watch(
		() => exportStore.isExportInfo,
		val => {
			if (val) {
				exportMap(screenshotPop.value)
			}
		},
		{ immediate: true, deep: true }
	)

	onMounted(() => {
		// 指北针/比例尺仍走后端；符号与图形标注使用固定矢量图标
		axios.all([getSymbolQueryCompass(), getSymbolQueryScale()])
	})

	const scaleOptionChange = (val, index) => {
		val.label = val.value
	}
	const addScaleOption = (item, i) => {
		// 在i的位置后面插入item
		scaleOptions.value.splice(i + 1, 0, { label: '1:', value: '1:' })
	}

	const delScaleOption = (val, index) => {
		scaleOptions.value.splice(index, 1)
		if (scaleOptions.value.length === 0) {
			scaleOptions.value.push({ label: '1:5000', value: '1:5000' })
		}
	}
	// 边界锁定
	const graphicsBtn = () => {
		exportStore.outMap.flyToManager.flyToRegion({
			type: 'Feature',
			geometry: {
				type: 'Polygon',
				coordinates: [
					[
						[118.263486, 25.339843],
						[120.932152, 25.339843],
						[120.932152, 26.540409],
						[118.263486, 26.540409],
						[118.263486, 25.339843]
					]
				]
			},
			properties: null
		})
	}
	async function getSymbolQueryDot() {
		symbolsListArr.value = []
		const res = await api.symbolQuery({ folderName: 'symbol_dot' })
		if (res.resultCode === 'S') {
			const url = 'http://fzsqi.com:10101/zuul/web-disaster-common/web/disaster/common/openfile/'
			symbolsList.value = res.data.map(item => {
				return { url: `${url}${item}`, label: item }
			})
			symbolsList.value.forEach(item => {
				symbolsListArr.value.push({ name: item.url.split('/').pop(), url: item.url, label: item.label })
			})
		}
	}
	async function getSymbolQueryGraphics() {
		symbolsListArr.value = []
		const res = await api.symbolQuery({ folderName: 'symbol_graphics' })
		if (res.resultCode === 'S') {
			const url = 'http://fzsqi.com:10101/zuul/web-disaster-common/web/disaster/common/openfile/'
			graphicsList.value = res.data.map(item => {
				return { url: `${url}${item}`, label: item }
			})
			graphicsList.value.forEach(item => {
				symbolsListArr.value.push({ name: item.url.split('/').pop(), url: item.url, label: item.label })
			})
		}
	}
	async function getSymbolQueryCompass() {
		const localItem = { url: defaultCompassUrl, label: '默认指北针' }
		try {
			const res = await api.symbolQuery({ folderName: 'symbol_compass' })
			if (res.resultCode === 'S' && res.data?.length) {
				const url = 'http://fzsqi.com:10101/zuul/web-disaster-common/web/disaster/common/openfile/'
				const remote = res.data.map(item => ({ url: `${url}${item}`, label: item }))
				compassList.value = [localItem, ...remote]
			} else {
				compassList.value = [localItem]
			}
		} catch (err) {
			compassList.value = [localItem]
		}
		compassUrl.value = compassUrl.value || defaultCompassUrl
	}
	async function getSymbolQueryScale() {
		symbolsListArr.value = []
		const res = await api.symbolQuery({ folderName: 'symbol_scale' })
		if (res.resultCode === 'S') {
			const url = 'http://fzsqi.com:10101/zuul/web-disaster-common/web/disaster/common/openfile/'
			scaleList.value = res.data.map(item => {
				return { url: `${url}${item}`, label: item }
			})
			scaleList.value.forEach(item => {
				symbolsListArr.value.push({ name: item.url.split('/').pop(), url: item.url, label: item.label })
			})
			scaleUrl.value = scaleList.value[0]?.['url'] || ''
		}
	}

	// 打开图标管理弹窗
	const openSymbolSetting = type => {
		symbolsListArr.value = []
		let list = []
		if (type === 'dot') {
			list = symbolsList.value
		}
		if (type === 'graphics') {
			list = graphicsList.value
		}
		if (type === 'compass') {
			list = compassList.value
		}
		if (type === 'scale') {
			list = scaleList.value
		}
		if (type === 'scale2') {
			showScaleChange.value = true
			return
		}

		if (list.length > 0) {
			list.forEach(item => {
				symbolsListArr.value.push({ name: item.url.split('/').pop(), url: item.url, label: item.label })
			})
		}

		symbolsUploadType.value = type
		showSymbolsChange.value = true
	}
	// 图标删除
	const beforeRemove = (uploadFile, uploadFiles) => {
		const result = new Promise((resolve, reject) => {
			ElMessageBox.confirm('此操作将删除该图片, 是否继续?', '提示', {
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			})
				.then(() => {
					let index = uploadFiles.indexOf(uploadFile)
					uploadFiles.splice(index, 1)
					api
						.symbolDelete({ symbolFilePath: uploadFile.label })
						.then(() => {
							const type = symbolsUploadType.value
							symbolsListArr.value = []
							if (type === 'dot') {
								getSymbolQueryDot()
							}
							if (type === 'graphics') {
								getSymbolQueryGraphics()
							}
							if (type === 'compass') {
								getSymbolQueryCompass()
							}
							resolve(true)
						})
						.catch(() => {
							reject(false)
						})
				})
				.catch(() => {
					reject(false)
				})
		})
		return result
	}
	// 图标上传
	const uploadImage = async (item, fileList) => {
		const typeList = ['image/jpeg', 'image/png', 'image/jpg']
		const isLt2M = item.file.size / 1024 / 1024 < 0.1
		if (typeList.indexOf(item.file.type) < 0) {
			ElMessage.error('上传图片只能是 jpg / png / jpeg 格式!')
			fileList.pop()
			return false
		}
		if (!isLt2M) {
			ElMessage.error('上传图片大小不能超过 100 kb!')
			fileList.pop()
			return false
		}
		let formData = new FormData()
		formData.append('files', item.file)
		formData.append('typeName', 'symbol_' + symbolsUploadType.value)
		api
			.symbolUpload(formData)
			.then(res => {
				const type = symbolsUploadType.value
				if (type === 'dot') {
					getSymbolQueryDot()
				}
				if (type === 'graphics') {
					getSymbolQueryGraphics()
				}
				if (type === 'compass') {
					getSymbolQueryCompass()
				}
			})
			.catch(err => {
				// ElMessage({ message: err, type: 'error' })
			})
	}

	const getMapHandler = map => {
		nextTick(() => {
			exportStore.setOutMap(map)
			const mainView = mapStore.activeMap?.olView
			if (mainView) {
				map.olView.setCenter(mainView.getCenter())
				map.olView.setResolution(mainView.getResolution())
			}
			let lyrs = mapStore.activeMap?.layers
			if (lyrs) {
				lyrs.forEach(l => {
					if (l.layer.clone && l.layer.clone instanceof Function) {
						map.lyrManager.addLayer(l.lyrName, l.layer.clone())
					}
				})
			}
			bindScaleListener(map)
		})
	}

	const emits = defineEmits(['close', 'cancel', 'save'])

	/** 先收起 drawer 动画，再在 closed 回调中真正卸载 */
	const closeDrawer = () => {
		drawerVisible.value = false
	}

	const onDrawerClosed = () => {
		unbindScaleListener()
		exportStore.closeExport()
		emits('cancel')
	}

	const close = () => {
		closeDrawer()
	}

	onBeforeUnmount(() => {
		window.removeEventListener('resize', syncMapAreaSize)
		emitter.off('exportTextPlaced', onExportTextPlaced)
		unbindScaleListener()
	})
	const addOutput = () => {
		showOutputPop.value = false
		const userInfo = JSON.parse(localStorage.getItem('userInfo'))
		const obj = {
			content: JSON.stringify(formData.value),
			department: '',
			status: 'S',
			statusMsg: '',
			topic: emStore.formData.name || '默认出图',
			userId: userInfo.userId,
			userName: userInfo.username || 'system'
		}
		api
			.logSave(obj)
			.then(res => {
				if (res.resultCode === 'S') {
					exportStore.setIsExportInfo(unref(formData))
					emits('save', formData)
				}
			})
			.catch(err => {})
	}
	/**
	 * 图例选择--添加
	 */
	const addLegendList = () => {
		legendList.push({ title: '图例', fill: '#1BBCFF', fontSize: 18, choose: false, icon: 'legend_1' })
		nextTick(() => {
			legendConRef.value.scrollTop = legendListRef.value.scrollHeight + 44
		})
	}
	const delLegendList = index => {
		legendList.splice(index, 1)
	}
	/**
	 * 符号标注选择
	 */
	const symbolsChange = (item, index) => {
		drawPoint(item, formData.value.symbolsSize)
		symbolsIdx.value = index
		formData.value.symbols = item
	}
	/**
	 * 图形标注选择
	 */
	const graphicsChange = item => {
		drawGeometry(item.value, {
			color: formData.value.fill,
			width: formData.value.border2
		})
		formData.value.graphics = item.value
	}
	const textInputRefs = ref([])
	const setTextInputRef = (el, index) => {
		if (el) textInputRefs.value[index] = el
	}
	/**
	 * 添加文字标注：仅在面板下方出现输入框，不默认上图 ABC
	 */
	const addText = () => {
		const obj = {
			id: `id_${Date.now()}_${formData.value.texts.length}`,
			text: '',
			fontSize2: 16,
			color: 'blue',
			borderColor: 'white',
			fontWeight: 2,
			fontStyle: 'normal',
			placed: false
		}
		formData.value.texts.push(obj)
		nextTick(() => {
			const idx = formData.value.texts.length - 1
			const input = textInputRefs.value[idx]
			input?.focus?.() || input?.$el?.querySelector?.('input')?.focus?.()
		})
	}
	/**
	 * 有内容后才进入地图落点；已落点或图层已有该文字时只同步样式，避免再次编辑生成重复要素
	 */
	const onTextAnnotateInput = item => {
		const val = String(item.text || '').trim()
		if (!val) return
		if (item.placed || exportStore.hasTextFeature(item.id)) {
			item.placed = true
			syncTextAnnotate()
			return
		}
		drawText(item)
	}
	/**
	 * 指北针要素选择
	 */
	const compassChange = item => {
		const url = item?.url || item || defaultCompassUrl
		formData.value.compass = url
		compassUrl.value = url
	}
	const delText = index => {
		formData.value.texts.splice(index, 1)
		textInputRefs.value.splice(index, 1)
		nextTick(() => syncTextAnnotate())
	}
	const borderChange = val => {
		if (formData.value.texts.length > 0) {
			formData.value.texts.forEach(item => {
				formData.value.border = `${val}px`
				item.borderWidth = `${val}px`
			})
		}
	}
	const normalizeHex = c => String(c || '').trim().toLowerCase()
	const isSameFill = c => normalizeHex(formData.value.fill) === normalizeHex(c)
	const isLightFill = c => {
		const hex = normalizeHex(c).replace('#', '')
		if (!/^[0-9a-f]{6}$/i.test(hex) && !/^[0-9a-f]{3}$/i.test(hex)) return false
		const full =
			hex.length === 3
				? hex
						.split('')
						.map(ch => ch + ch)
						.join('')
				: hex
		const r = parseInt(full.slice(0, 2), 16)
		const g = parseInt(full.slice(2, 4), 16)
		const b = parseInt(full.slice(4, 6), 16)
		return (r * 299 + g * 587 + b * 114) / 1000 > 210
	}
	const selectFill = c => {
		formData.value.fill = c
		fillChange(c)
	}
	const fillChange = val => {
		if (formData.value.texts.length > 0) {
			formData.value.texts.forEach(item => {
				item.background = val
			})
		}
	}
	/**
	 * 重置地图标注
	 */
	const restBz = () => {
		symbolsIdx.value = 0
		formData.value = Object.assign(formData.value, {
			symbols: '',
			symbolsSize: '中',
			graphics: '',
			texts: [],
			border2: 1,
			borderColor: '#1BBCFF',
			fill: '#ffffff'
		})
	}
	const setStyle = style => {
		if (formData.value.texts.length === 0) {
			return
		}
		formData.value.texts.forEach(item => {
			if (style === 'B') {
				item.fontWeight = item.fontWeight === 400 ? 700 : 400
			}
			if (style === 'I') {
				item.fontStyle = item.fontStyle === 'normal' ? 'italic' : 'normal'
			}
			if (style === 'U') {
				item.textDecoration = item.textDecoration === 'none' ? 'underline' : 'none'
			}
		})
	}
	/**
	 * 设置标题样式
	 */
	const setTitleStyle = style => {
		if (style === 'B') {
			formData.value.titleStyle.fontWeight = formData.value.titleStyle.fontWeight === 400 ? 700 : 400
		}
		if (style === 'I') {
			formData.value.titleStyle.fontStyle = formData.value.titleStyle.fontStyle === 'normal' ? 'italic' : 'normal'
		}
		if (style === 'U') {
			formData.value.titleStyle.textDecoration =
				formData.value.titleStyle.textDecoration === 'none' ? 'underline' : 'none'
		}
	}
	/**
	 * 设置标题字体大小
	 */
	const handleFontSizeChange = val => {
		formData.value.titleStyle.fontSize = val + 'px'
	}

	const floatBtnHandle = type => {
		if (type === 'ok') {
			showOutputPop.value = true
		}

		if (type === 'step') {
			step.value = step.value === 1 ? 2 : 1
		}
	}
	const getAssetsFile = (value, name) => {
		if (name === '符号标注') {
			return new URL(`./imgs/${value}${formData.value.direction === value ? '_cur' : ''}.png`, import.meta.url).href
		} else if (name === '图形标注') {
			return new URL(`./imgs/${value}${formData.value.graphics === value ? '_cur' : ''}.png`, import.meta.url).href
		} else {
			return new URL(`./imgs/${value}.png`, import.meta.url).href
		}
	}
</script>

<style lang="scss" scoped>
	.export-map-panel {
		display: flex;
		width: 100%;
		height: 100%;
		overflow: hidden;
		background: #f0f2f5;
	}
	.map-area {
		flex: 1;
		min-width: 0;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 24px;
		box-sizing: border-box;
		overflow: auto;
	}
	.rightPop {
		flex-shrink: 0;
		width: 366px;
		height: 100%;
		display: flex;
		flex-direction: column;
		background-color: white;
		border-left: 1px solid #e5e5e5;
		box-shadow: -2px 0 8px rgba(0, 0, 0, 0.06);
		& .head {
			flex-shrink: 0;
			height: 44px;
			padding: 0 10px;
			color: white;
			border-bottom: 1px solid #ebeef5;
			background-color: #276fd3;
		}
		& .tabs-bar {
			flex-shrink: 0;
		}
		& .con {
			flex: 1;
			min-height: 0;
			overflow-y: auto;
			& .con_item2 {
				border-top: 1px solid #e5e5e5;
			}
			:deep(.el-form-item) {
				margin-bottom: 14px;
			}
			:deep(.el-checkbox-group) {
				display: flex;
				flex-wrap: wrap;
				gap: 4px 12px;
			}
		}
		& .item_title {
			height: 44px;
			padding: 0 12px;
			user-select: none;
			& .title {
				font-family: 'Microsoft YaHei Bold';
				font-weight: 700;
				font-size: 16px;
				color: #000;
			}
		}
		& .float {
			flex-shrink: 0;
			height: 64px;
			border: 1px solid #e5e5e5;
		}
		& .logCon {
			flex: 1;
			min-height: 0;
			overflow: auto;
		}
		& .symbolsBox {
			& li {
				width: 30px;
				height: 30px;
				border: 1px solid #ffffff;
				&.cur {
					border: 1px solid #386bf3;
				}
			}
		}
		& .graphicsBox {
			& li {
				width: 30px;
				height: 30px;
				border: 1px solid #ffffff;
				border-radius: 2px;
				&.cur {
					border: 1px solid #386bf3;
				}
			}
		}
		& .fill-colors {
			gap: 6px;
			& .fill-swatch {
				display: inline-block;
				width: 22px;
				height: 22px;
				border-radius: 3px;
				border: 1px solid #d9d9d9;
				box-sizing: border-box;
				&.light {
					border-color: #bfbfbf;
				}
				&.cur {
					outline: 2px solid #386bf3;
					outline-offset: 1px;
				}
			}
			& .fill-more {
				position: relative;
				gap: 4px;
				height: 22px;
				padding: 0 4px 0 0;
				border-radius: 3px;
				overflow: hidden;
				& .more-preview {
					margin: 0;
				}
				& .more-txt {
					font-size: 12px;
					color: #386bf3;
					line-height: 1;
					white-space: nowrap;
				}
				&:hover .more-txt {
					color: #165dba;
				}
				:deep(.el-color-picker) {
					position: absolute;
					inset: 0;
					opacity: 0;
					cursor: pointer;
				}
				:deep(.el-color-picker__trigger) {
					width: 100%;
					height: 100%;
					padding: 0;
					border: none;
				}
			}
		}
		& .text_color {
			position: relative;
			z-index: 1;
			width: 13px;
			overflow: hidden;
			& span {
				font-size: 16px;
				font-weight: 600;
				line-height: 1.2;
			}
			& .bg_color {
				height: 2px;
				width: 100%;
				background-color: #1a1b1f;
			}
			:deep() {
				.el-color-picker {
					position: absolute;
					top: 0;
					left: 0;
					opacity: 0;
				}
			}
		}
		& .text_style {
			& span {
				font-size: 16px;
				font-weight: 600;
				line-height: 1.2;
				margin-right: 12px;
				user-select: none;
				cursor: pointer;
				&.i {
					font-style: italic;
				}
				&.u {
					text-decoration: underline;
				}
			}
		}
		& .text_del {
			width: 22px;
			height: 22px;
			cursor: pointer;
			i {
				display: flex;
				align-items: center;
				justify-content: center;
				width: 14px;
				font-size: 14px;
				padding: 2px;
				background: #cccccc;
				color: white;
				border-radius: 2px;
			}
			&:hover i {
				background: #ed5353;
				color: white;
			}
		}
		& .openLegend {
			width: 58px;
			color: #2e90ec;
			border-bottom: 1px solid #2e90ec;
		}
	}
	.screenshotPop {
		position: relative;
		z-index: 1;
		flex-shrink: 0;
		background: #fff;
		border-radius: 2px;
		box-shadow: 0 8px 28px rgba(0, 0, 0, 0.18);
		overflow: hidden;
		& .screenshotBox {
			position: relative;
			width: 100%;
			height: 100%;
			background: #fff;
		}
		& .head {
			width: 100%;
			height: 40px;
			position: absolute;
			& .title {
				font-size: 16px;
				font-weight: bold;
			}
		}
		& .screenshot {
			position: relative;
			background-color: white;
			& .legend {
				padding: 6px 4px;
				overflow: auto;
				background-color: rgba(255, 255, 255, 0.94);
				border: 1px solid #d9d9d9;
				box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
			}
			& .scaleBox {
				min-width: 72px;
				padding: 4px 8px;
				background: transparent;
				background-color: transparent;
				border: none;
				box-shadow: none;
				.scale-rf,
				.scale-dist {
					margin: 0;
					line-height: 1.2;
					font-size: 16px;
					white-space: nowrap;
				}
			}
			& .compassImg {
				background: transparent;
				background-color: transparent;
			}
		}
		.title,
		.producer,
		.unit,
		.createTime,
		.description {
			display: flex;
			align-items: center;
			overflow: hidden;
			padding: 0 6px;
			white-space: nowrap;
			text-overflow: ellipsis;
			background: rgba(255, 255, 255, 0.92);
		}
		.title {
			justify-content: center;
			font-weight: 600;
		}
	}
	.legendChangeMask {
		position: fixed;
		left: 0px;
		top: 0px;
		z-index: 4100;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
	}
	.legendPop {
		position: fixed;
		top: calc(50% - 243px);
		left: calc(50% - 325px);
		width: 650px;
		height: 486px;
		border-radius: 4px;
		background-color: #fff;
		overflow: visible;
		& .head {
			height: 52px;
			font-size: 18px;
			padding: 0 10px;
			color: white;
			background-color: #3c7dd7;
		}
		& .con {
			height: calc(100% - 108px);
			overflow: visible;
		}
		& .legend-grid {
			display: grid;
			grid-template-columns: 56px 80px minmax(0, 1fr) 64px 120px 56px;
			column-gap: 8px;
			align-items: center;
			width: 100%;
			box-sizing: border-box;
		}
		& .conHead {
			position: sticky;
			top: 0;
			z-index: 5;
			height: 44px;
			margin: 0 0 12px;
			padding: 0;
			list-style: none;
			background: #fff;
			& li {
				display: flex;
				align-items: center;
				justify-content: center;
				height: 100%;
				text-align: center;
				color: #333;
				font-weight: 500;
			}
		}
		& .w10 {
			width: 10%;
		}
		& .w15 {
			width: 15%;
		}
		& .w18 {
			width: 18%;
		}
		& .conBody {
			height: 100%;
			overflow-x: hidden;
			overflow-y: auto;
			& .legend-row {
				position: relative;
				z-index: 1;
				margin: 0 0 20px;
				padding: 0;
				list-style: none;
				&:focus-within,
				&:hover {
					z-index: 20;
				}
			}
			& .legend-cell {
				display: flex;
				align-items: center;
				justify-content: center;
				min-width: 0;
			}
			& .legend-size {
				justify-content: center;
				gap: 2px;
			}
			& .legend-row-field {
				position: relative;
				z-index: 1;
			}
		}
		& .footer {
			height: 56px;
			padding: 0 16px;
		}
		&.symbolsPop {
			height: 280px;
			& .con {
				height: calc(100% - 52px);
			}
		}
		&.scalePop {
			left: calc(50% - 170px);
			width: 340px;
			min-height: 280px;
			& .con {
				height: calc(100% - 52px);
			}
		}
	}
	.producer {
		font-size: 14px;
	}
	.unit {
		font-size: 14px;
	}
	.createTime {
		font-size: 14px;
	}
	.description {
		font-size: 14px;
	}
	.pucd {
		text-align: justify;
		text-align-last: justify;
	}
	.moveableTarget {
		position: relative;
		top: 0;
		left: 0;
		z-index: 100;
	}
	:deep() {
		.moveable-line {
			height: 0px !important;
			// display: none !important;
		}
		.el-upload-list--picture-card .el-upload-list__item,
		.el-upload--picture-card {
			width: 66px;
			height: 66px;
		}
		.el-upload-list--picture-card .el-upload-list__item-actions:hover span.el-upload-list__item-preview {
			display: none !important;
		}
		.el-upload-list--picture-card .el-upload-list__item-actions span + span {
			margin-left: 0px !important;
		}
	}
</style>

<style lang="scss">
	/* append-to-body：盖住标题栏(2024)/工具栏(2020+)，低于模板弹窗(3000)/图例弹层(4100) */
	.export-map-drawer.el-drawer {
		z-index: 2800 !important;
		height: 100% !important;
		top: 0 !important;
		bottom: 0 !important;
		box-shadow: -4px 0 24px rgba(0, 0, 0, 0.18);
	}
	.el-overlay:has(.export-map-drawer),
	.el-overlay.export-map-drawer-overlay {
		z-index: 2800 !important;
	}
	.export-map-drawer .el-drawer__body {
		padding: 0;
		height: 100%;
		overflow: hidden;
	}
	/* 图例弹窗内下拉/取色器需高于蒙版 4100（取色器仍 teleport 到 body） */
	.legend-popper-above,
	.el-popper.legend-popper-above,
	.el-select__popper.legend-popper-above,
	.el-color-picker__panel.legend-popper-above,
	.el-color-dropdown.legend-popper-above {
		z-index: 5300 !important;
	}
	body.legend-dialog-open .el-popper,
	body.legend-dialog-open .el-color-dropdown,
	body.legend-dialog-open .el-select__popper,
	body.legend-dialog-open .el-picker__popper {
		z-index: 5300 !important;
	}
</style>
