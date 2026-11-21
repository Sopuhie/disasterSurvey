<template>
	<div class="serveDetails maxwh">
		<div class="top" v-if="data">
			<img :src="data.screenshot ? data.screenshot : screenshot" alt="" />
			<div class="top_r">
				<p class="title">{{ data.serviceName }}</p>
				<p class="values">
					<span>创建日期：</span>
					<span class="value">{{ data.cjsj }}</span>
					<span>资源类型：</span>
					<span class="blue value"> [ {{ data.serviceType }} ] </span>
				</p>
			</div>
		</div>
		<div class="bottom" v-if="data">
			<div class="bottom_item" style="margin-right: 16px">
				<p class="bottom_item_t">基本信息</p>
				<p><span class="title">资源ID：</span><span>{{ data.id }}</span></p>
				<p><span class="title">资源名称：</span><span>{{ data.serviceName }}</span></p>
				<p><span class="title">发布人：</span><span>{{ data.zhgxr }}</span></p>
				<p><span class="title">发布机构：</span><span>{{ data.unit }}</span></p>
				<p><span class="title">描述说明：</span><span>{{ data.description }}</span></p>
			</div>
			<div class="bottom_item" style="margin-left: 16px">
				<p class="bottom_item_t">空间信息</p>
				<p><span class="title">空间参考：</span><el-input :value="data.extentMeta && data.extentMeta.code" disabled /></p>
				<p><span class="title">wkid：</span><el-input :value="data.extentMeta && data.extentMeta.srid" disabled /></p>
				<p><span class="title">原点：</span><el-input :value="data.extentMeta && data.extentMeta.origin" disabled /></p>
				<p><span class="title">投影：</span><el-input :value="data.extentMeta && data.extentMeta.projection" disabled /></p>
			</div>
		</div>
	</div>
</template>

<script setup>
	import screenshot from '@/assets/imgs/map.png'
	defineProps({
		data: {
			type: Object,
			default: () => null
		}
	})
</script>

<style lang="scss" scoped>
	.serveDetails {
		max-width: 960px;
		& .top {
			display: flex;
			margin-bottom: 16px;
			& img {
				width: 120px;
				height: 120px;
			}
			& .top_r {
				margin-left: 20px;
				& .title {
					font-weight: bold;
				}
				& p {
					line-height: 40px;
				}
				& .values {
					white-space: nowrap;
				}
				& .value {
					margin-right: 20px;
				}
				& .blue {
					color: rgb(12, 133, 240);
				}
			}
		}
		& .bottom {
			display: flex;
			max-height: calc(100% - 136px);
			& .bottom_item {
				width: calc(50% - 16px);
				background: var(--el-color-primary-light-9);
				flex: 1;
				& p {
					display: flex;
					align-items: center;
					white-space: nowrap;
					padding: 0 10px;
					line-height: 36px;
					& .title {
						display: inline-block;
						text-align-last: justify;
						text-justify: distribute-all-lines;
						width: 106px;
						overflow: hidden;
					}
					& input {
						width: 260px;
					}
				}
				& .bottom_item_t {
					background: var(--el-color-primary-light-8);
					line-height: 30px;
					padding-left: 10px;
				}
			}
		}
	}
</style>
