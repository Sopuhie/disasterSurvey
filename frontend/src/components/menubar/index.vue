<template>
	<div class="menubar flex faj ml-24" @mouseenter.stop="showMenu = true" @mouseleave.stop="menubarLeave">
		<i class="iconfont icon-grid cp" style="color: #2875e0; font-size: 18px"></i>
		<span class="pl4 cp">专题应用</span>
		<div class="menus flex" v-show="show || showMenu" @mouseenter.stop="menuEnter" @mouseleave.stop="menuLeave">
			<div class="menusLeft">
				<ul v-if="lists.length">
					<li
						class="flex fa nowrap cp"
						:class="{ cur: hoverIdx === index }"
						v-for="(item, index) in lists"
						:key="'menuLists_' + index"
						@mouseenter.stop="enter(index)">
						<i class="iconfont icon-grid" :class="item.image"></i>
						<span>{{ item.name }}</span>
					</li>
				</ul>
			</div>
			<div class="menusRight p-16 oy">
				<ul class="max-w flex fw fr" v-if="lists.length">
					<li v-for="(item, index) in lists[hoverIdx]['childrens']" :key="'menuChild_' + index" @click="toPath(item)">
						<img v-if="item.image" :src="`https://10.10.15.97:18284/cdn/static/zhzyfx-zq/${item.image}.png`" alt="" />
						<span>{{ item.name ? item.name : '--' }}</span>
					</li>
				</ul>
			</div>
		</div>
	</div>
</template>

<script setup>
	const router = useRouter()

	import { useSystemStore } from '@/store/modules/system.js'
	const store = useSystemStore()

	const showMenu = ref(false)
	const show = ref(false)
	const hoverIdx = ref(0)
	const timer = ref(null)
	const time = ref(500)
	const second = ref(0)

	const lists = computed(() => {
		let data = []
		const menuData = toRaw(store?.menuTreeData)
		if (menuData?.length > 0) {
			const itemData = menuData.filter(item => item.name === '前端菜单')
			const configure = itemData[0]['childrens'].filter(item => item.name === '导航栏菜单')[0]['childrens']
			data = configure.filter(item => item.name !== '首页')
		}
		return data
	})

	onMounted(() => {})

	// 页面跳转
	const toPath = item => {
		console.log(item)
		reset()
		router.push(item.executetool)
	}

	const enter = index => {
		hoverIdx.value = index
	}

	const menubarLeave = () => {
		setTimeout(() => {
			if (!show.value) {
				showMenu.value = false
				clearInterval(timer.value)
			} else {
				showMenu.value = true
			}
		}, time.value)
	}

	const menuEnter = () => {
		clearInterval(timer.value)
		show.value = true
		showMenu.value = true
		time.value = 500
		second.value = 0
	}

	const menuLeave = () => {
		second.value = 0
		clearInterval(timer.value)
		timer.value = setInterval(() => {
			second.value += 100
			if (second.value > time.value) {
				clearInterval(timer.value)
				reset()
			}
		}, 100)
	}

	const reset = () => {
		timer.value = null
		show.value = false
		showMenu.value = false
	}
</script>

<style lang="scss" scoped>
	.menubar {
		position: fixed;
		top: 8px;
		right: 124px;
		z-index: 2024;
		height: 40px;
		border-radius: 4px;
		padding: 0 16px;
		box-shadow: 0px 6px 10px 0px rgba(0, 0, 0, 0.16);
		background-color: white;
	}
	.menus {
		position: absolute;
		top: 48px;
		right: 0px;
		width: 648px;
		height: 336px;
		border-radius: 4px;
		font-family: PingFangSC-Regular;
		font-size: 14px;
		background: rgba(255, 255, 255, 0.95);
		box-shadow: 0px 6px 10px 0px rgba(0, 0, 0, 0.16);
		& .menusLeft {
			width: 124px;
			height: 100%;
			padding: 12px 8px;
			background-image: url('@/assets/images/menu-list.png');
			background-repeat: no-repeat;
			background-position: left top;
			background-size: 100% 100%;
			& li {
				height: 40px;
				line-height: 40px;
				border-radius: 2px;
				margin-bottom: 8px;
				padding: 0 12px;
				color: white;
				&.cur {
					color: #2875e0;
					background: rgba(255, 255, 255, 0.9);
					& i {
						color: #2875e0;
					}
				}
				& i {
					width: 20px;
					margin-right: 4px;
					color: #ffffff;
				}
			}
		}
		& .menusRight {
			width: calc(100% - 124px);
			height: 100%;

			& li {
				display: flex;
				align-items: center;
				width: 50%;
				height: 40px;
				line-height: 40px;
				padding: 0 16px;
				margin-bottom: 20px;
				white-space: nowrap;
				color: #2875e0;
				cursor: pointer;
				& i {
					display: flex;
					width: 20px;
					margin-right: 4px;
					color: #ffffff;
				}
				&:hover {
					background: #e9f1fb;
				}
				& span {
					white-space: nowrap;
					overflow: hidden;
					text-overflow: ellipsis;
					width: calc(100% - 32px);
				}
			}
		}
	}
</style>
