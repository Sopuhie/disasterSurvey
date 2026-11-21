<template>
	<div class="popHead flex fa jsb pl-21 pr-14">
		<span class="title">{{ title }}</span>
		<el-icon class="cp" size="20" style="color: #9dbbd9" v-if="closeBtn" @click.stop="closeHandle">
			<CircleClose />
		</el-icon>
	</div>
</template>
<script setup>
	const props = defineProps({
		title: {
			type: String,
			default: '标题'
		},
		closeBtn: {
			type: Boolean,
			default: true
		}
	})
	const emits = defineEmits(['close'])
	const closeHandle = () => {
		emits('close')
	}
</script>
<style lang="scss" scoped>
	.popHead {
		width: 100%;
		height: 50px;
		background: white;
		border-bottom: 1px solid #c8c8c8;
		// background-image: url('@/assets/imgs/headPop.png');
		// background-size: cover;
		// background-repeat: no-repeat;
		// background-position: center;
		& .title {
			font-family: PingFangSC, PingFang SC;
			font-size: 20px;
			font-weight: 500;

			// background: linear-gradient(to bottom, #ffffff, #c3dff4, #78d9ff);
			// background-clip: text;
			// -webkit-background-clip: text;
			// color: transparent;
		}
	}
</style>
