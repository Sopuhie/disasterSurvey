<template>
	<div class="stepBox">
		<div class="itemTitle">
			<span class="txt"> 图纸设置 </span>
			<span></span>
		</div>
		<div class="flex fa mt-10">
			<span class="mr-10">模板选取</span>
			<el-input v-model="mode" style="width: 240px" placeholder="请选择模板" :suffix-icon="Search" />
		</div>
		<div class="flex fa mt-10">
			<span class="mr-10">设置图框</span>
			<span class="choose_dir h" :class="{ cur: printType === 1 }" @click="printTypeChange(1, 'h')">A4</span>
			<span class="choose_dir v" :class="{ cur: printType === 2 }" @click="printTypeChange(2, 'v')">A4</span>
			<span class="choose_dir h" :class="{ cur: printType === 3 }" @click="printTypeChange(3, 'h')">A3</span>
			<span class="choose_dir v" :class="{ cur: printType === 4 }" @click="printTypeChange(4, 'v')">A3</span>
		</div>
		<div class="flex fa mt-10">
			<span class="mr-10">图例布局</span>
			<template v-if="printType === 1 || printType === 3">
				<span class="choose_layout" :class="{ cur: layoutType === 1 }" @click="layoutTypeChange(1)">
					<img src="../img/layout1.png" alt="" />
				</span>
				<span class="choose_layout" :class="{ cur: layoutType === 2 }" @click="layoutTypeChange(2)">
					<img src="../img/layout4.png" alt="" />
				</span>
			</template>
			<template v-if="printType === 2 || printType === 4">
				<span class="choose_layout" :class="{ cur: layoutType === 1 }" @click="layoutTypeChange(1)">
					<img src="../img/layout2.png" alt="" />
				</span>
				<span class="choose_layout" :class="{ cur: layoutType === 2 }" @click="layoutTypeChange(2)">
					<img src="../img/layout3.png" alt="" />
				</span>
			</template>
		</div>
		<div class="flex fa mt-10">
			<span class="mr-10 nowrap">图纸比例</span>
			<el-select v-model="scale" placeholder="请选择比例" style="width: 240px">
				<el-option label="1:1" value="1"></el-option>
				<el-option label="1:2" value="2"></el-option>
				<el-option label="1:3" value="3"></el-option>
			</el-select>
		</div>
		<div class="flex fa mt-10">
			<span class="mr-10">图纸工具</span>
			<span class="choose_tool" @click="toolChange('zoomIn')">
				<i class="iconfont icon-zoomIn" style="display: inline-block"></i>
			</span>
			<span class="choose_tool" @click="toolChange('zoomOut')">
				<i class="iconfont icon-zoomOut-1" style="display: inline-block"></i>
			</span>
		</div>
	</div>
</template>

<script setup>
	import { zoomInHandler, zoomOutHandler } from '../index.js'
	const mode = ref('默认模板')
	const printType = ref(1)
	const layoutType = ref(1)
	const scale = ref(1)
	const printDirection = ref('h')

	const emits = defineEmits(['typeChange', 'layoutChange'])
	/**
	 * 打印类型切换
	 */
	const printTypeChange = (type, direction) => {
		printType.value = type
		printDirection.value = type % 2 === 0 ? 'v' : 'h'
		layoutType.value = 1
		emits('typeChange', direction)
	}
	/**
	 * 图例布局切换
	 */
	const layoutTypeChange = type => {
		layoutType.value = type
		emits('layoutChange', type)
	}
	/**
	 * 图纸工具切换
	 */
	const toolChange = type => {
		type === 'zoomIn' ? zoomInHandler() : zoomOutHandler()
	}
</script>

<style lang="scss" scoped>
	.choose_dir {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 24px;
		background: #f9f9f9;
		border: 1px solid rgba(0, 17, 61, 0.08);
		border-radius: 4px;
		cursor: pointer;
		&.h {
			width: 40px;
			height: 32px;
		}
		&.v {
			width: 40px;
			height: 48px;
		}
		&.cur {
			border-color: #2875e0;
		}
	}
	.choose_layout {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 24px;
		background: #f9f9f9;
		border: 1px solid rgba(0, 17, 61, 0.08);
		border-radius: 4px;
		width: 44px;
		height: 42px;
		cursor: pointer;
		&.cur {
			border-color: #2875e0;
		}
		& img {
			width: 100%;
			height: 100%;
			object-fit: cover;
		}
	}
	.choose_tool {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 45px;
		height: 45px;
		background: #ffffff;
		box-shadow: 0 0 4px 1px rgba(7, 18, 56, 0.1);
		border-radius: 4px;
		color: #004395;
		font-size: 20px;
		margin-right: 10px;
		cursor: pointer;
	}
</style>
