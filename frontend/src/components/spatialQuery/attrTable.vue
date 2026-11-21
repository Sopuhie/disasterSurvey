<template>
	<ul class="attrTable max-wh" v-if="itemData">
		<li class="attrTable-item flex fa" v-for="(value, key, index) in itemData.attributes" :key="'attrTable_' + index">
			<p class="label flex fa">{{ key }}</p>
			<p class="value flex fa">{{ value }}</p>
		</li>
	</ul>
</template>

<script setup name="AttrTable">
	const props = defineProps({
		itemData: {
			type: Object,
			default: () => {
				return null
			}
		}
	})
</script>

<style lang="scss" scoped>
	.attrTable {
		border-top: 1px solid var(--el-text-color-regular);
		&-item {
			border-left: 1px solid var(--el-text-color-regular);
			border-right: 1px solid var(--el-text-color-regular);
			border-bottom: 1px solid var(--el-text-color-regular);
			& p {
				width: 50%;
				min-height: 32px;
				height: 100%;
				line-height: 1.2;
				padding: 4px 8px;
				user-select: text;
				color: var(--el-text-color-regular);
				word-break: break-all;
				&:last-child {
					border-left: 1px solid var(--el-text-color-regular);
				}
			}
		}
	}
</style>
