<template>
	<div class="pageTabs flex fa jsb">
		<ul class="tabs flex fa">
			<li
				class="item"
				v-for="(item, index) in data"
				:key="index"
				:class="{ active: tabIdx === index }"
				@click="tabChange(item, index)">
				{{ item.name }}
			</li>
		</ul>
		<div>
			<slot name="right"></slot>
		</div>
	</div>
</template>

<script setup>
	const tabIdx = ref(0)
	const props = defineProps({
		data: {
			type: Array,
			default: () => []
		}
	})

	const emit = defineEmits(['tabChange'])

	const tabChange = (item, index) => {
		tabIdx.value = index
		emit('tabChange', item)
	}
</script>

<style lang="scss" scoped>
	.pageTabs {
		height: 40px;
		padding: 0 10px;
		border-radius: 3px;
		overflow: hidden;
		background: #fff;
		box-shadow: 0 0 1px 1px #ccc;
		& .tabs {
			height: 100%;
			& .item {
				position: relative;
				padding: 0 18px;
				cursor: pointer;
				&.active {
					font-size: 15px;
					color: #2a76d3;
					font-weight: 700;
				}
				&:not(:last-child):before {
					content: '';
					width: 1px;
					height: 40%;
					position: absolute;
					right: 0;
					top: 30%;
					background: #ccc;
				}
			}
		}
	}
</style>
