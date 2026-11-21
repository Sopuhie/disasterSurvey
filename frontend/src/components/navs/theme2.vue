<template>
	<div class="navBar theme2 flex fa jsb">
		<div class="nav_bar_logo flex fa">
			<img src="/logo.png" alt="" />
			<div class="ml-12 mr-12 title">
				{{ appTitleEn }}
			</div>
		</div>
		<div class="info flex fa">
			<img class="mr-40 cp"
			src="@/assets/imgs/logout.png"
			width="40"
			height="40"
			alt="" @click="loginOut" />
			<span class="el-dropdown-link">
				<el-image :src="avatarSrc" fit="cover">
					<template #error>
						<img :src="defaultAvatar" alt="" />
					</template>
				</el-image>
			</span>
		</div>
	</div>
</template>

<script setup>
	import { appTitleEn } from '@/config'
	import { BASE_LAYER_NAME } from '@/components/map/primitive/config/LayerConstantConfig.js'
	import buildLayer, { LayerType } from '@/components/map/primitive/layer/Layer'
	import { appApi } from '@/api/app/index.js'
	import defaultAvatar from '@/assets/imgs/usrIcon.png';

	const api = appApi()

	const route = useRoute()
	const router = useRouter()

	import { useAppStore } from '@/store/modules/app.js'
	import { destroyFeedback } from '@/utils/feedback.js'
	const appStore = useAppStore()

	import { useMapStore } from '@/store/modules/map.js'
	const mapStore = useMapStore()

	const drawer = ref(false)
	const themeTxt = ref('主题二')
	const routePath = ref('')
	const isDark = ref(true)
	const navIdx = ref(0)
	const avatarSrc = computed(() => appStore?.userInfo?.photo || defaultAvatar);
	const emits = defineEmits(['goToDetail'])

	const toggleDark = () => {
		if (isDark.value) {
			document.documentElement.className = 'blue'
			document.documentElement.style.setProperty('--theme-mode', 'blue')
			appStore.setAppThemeVal('blue')
		} else {
			document.documentElement.style.setProperty('--theme-mode', 'dark')
			document.documentElement.className = 'dark'
			appStore.setAppThemeVal('dark')
		}
		toggleBasemap(isDark.value)
	}
	const getNavItemClass = executetool => {
		return executetool === routePath.value ? `${executetool} cur` : executetool
	}

	const handleNavItemClick = (item, index) => {
		if (navIdx.value === index) return
		navIdx.value = index
		appStore.setCurNavObj(item.title)
		emits('goToDetail', false)
		router.push(item.path)
	}
	/**
	 * 切换电子地图颜色
	 */
	const toggleBasemap = isDark => {
		let lyrOption = {
			name: BASE_LAYER_NAME,
			layers: mapStore.baseMapTheme.serviceInfo.map(m => {
				return {
					type: m.serviceType,
					lyrOption: {
						url: m.serviceUrl,
						name: m.serviceName,
						...(mapStore.metaDataDeserialized(m.metaData) || null)
					}
				}
			})
		}

		lyrOption.zIndex = -200
		lyrOption.layers.forEach(lyr => {
			lyr.lyrOption.zIndex = -200
			lyr.lyrOption.tileLoadFunction = (imageTile, src) => {
				const img = new Image()
				img.setAttribute('crossOrigin', 'anonymous')
				// 使用滤镜 将白色修改为深色
				img.onload = () => {
					const canvas = document.createElement('canvas')
					const w = img.width
					const h = img.height
					canvas.width = w
					canvas.height = h
					const context = canvas.getContext('2d')
					if (isDark) {
						context.filter =
							'grayscale(98%) invert(100%) sepia(20%) hue-rotate(180deg) saturate(100%) brightness(80%) contrast(90%)'
					} else {
						context.filter =
							'grayscale(98%) invert(100%) sepia(20%) hue-rotate(180deg) saturate(25000%) brightness(80%) contrast(90%)'
					}
					context.drawImage(img, 0, 0, w, h, 0, 0, w, h)
					imageTile.getImage().src = canvas.toDataURL('image/png')
				}
				img.src = src
			}
		})

		mapStore.activeMap.lyrManager.removeLayer(BASE_LAYER_NAME)
		mapStore.activeMap.lyrManager.addLayer(BASE_LAYER_NAME, buildLayer(LayerType.Group, lyrOption))
	}

	const lists = computed(() => {
		let data = [
			{ title: '基础数据服务', path: '/provinceSystem' },
			{ title: '灾情调查', path: '/disasterSurvey' },
			{ title: '灾害评估', path: '/disasterAssessment' },
			{ title: '运维管理', path: '/management' }
		]
		return data
	})

	// 监听当前路由
	watch(
		() => route.path,
		newRoute => {
			navIdx.value = lists.value.findIndex(item => newRoute.includes(item.path))
		},
		{ immediate: true, immediate: true }
	)

	onMounted(() => {
		navIdx.value = lists.value.findIndex(item => item.path === route.path)
	})

	const handleCommand = command => {
		switch (command) {
			case 'theme':
				themeTxt.value = themeTxt.value === '主题二' ? '主题一' : '主题二'
				appStore.setAppTheme(themeTxt.value === '主题二' ? 'theme2' : 'theme1')
				break
			case 'account':
				drawer.value = true
				break
			case 'logoOut':
				loginOut()
				break
			default:
				break
		}
	}

	// 退出登录
	const loginOut = () => {
		ElMessageBox({
			closeOnClickModal: false,
			closeOnPressEscape: false,
			showCancelButton: false,
			title: '提示',
			message: '此操作将退出登录, 是否继续?',
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			buttonSize: 'default',
			beforeClose: (action, instance, done) => {
				if (action === 'confirm') {
					instance.confirmButtonLoading = true
					instance.confirmButtonText = '退出中'
					setTimeout(() => {
						done()
						setTimeout(() => {
							instance.confirmButtonLoading = false
						}, 300)
					}, 700)
				} else {
					done()
				}
			}
		})
			.then(async () => {
				const username = appStore.userInfo?.username || 'system'
				api
					.loginOut({ username })
					.then(async res => {
						if (res.resultCode === 'S') {
							destroyFeedback()
							router.push('/login')
							window.localStorage.clear()
							window.sessionStorage.clear()
							setTimeout(() => {
								// 防止退出后，可以点击回退按钮，返回页面
								historyPush()
							}, 100)
						}
					})
					.catch(err => {})
			})
			.catch(() => {})
	}
	// 防止退出后，可以点击回退按钮，返回页面
	const historyPush = () => {
		history.pushState(null, null, document.URL)
		window.addEventListener(
			'popstate',
			function (e) {
				history.pushState(null, null, document.URL)
			},
			false
		)
	}
</script>

<style lang="scss" scoped>
	.theme2 {
		position: relative;
		top: 0;
		left: 0;
		z-index: 2024;
		width: 100%;
		height: 75px;
		background-image: url('@/assets/imgs/headBg.png');
		background-size: 100% 100%;
		background-position: center;
		background-repeat: no-repeat;
	}
	.info {
		width: 19.0625%;
		height: 75px;
	}
	.el-dropdown-link {
		width: 40px;
		height: 40px;
		line-height: 40px;
		border-radius: 50%;
		overflow: hidden;
	}
	.menuItem {
		position: relative;
		width: 158px;
		height: 48px;
		&.cur {
			&::before {
				content: '';
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 100%;
				background: linear-gradient(to right, rgba(26, 83, 152, 0.8), rgba(6, 32, 74, 0.8));
				transform: skew(-20deg);
				z-index: 1;
			}
			& .nav-item {
				&::before {
					content: '';
					position: absolute;
					top: 0;
					left: 0;
					width: 100%;
					height: 100%;
					background: linear-gradient(to right, rgba(0, 149, 255, 0.8), rgba(0, 98, 255, 0.8));
					transform: skew(-20deg);
					z-index: -1;
				}
			}
		}

		&::before {
			content: '';
			position: absolute;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			background: linear-gradient(to right, rgba(26, 83, 152, 0.8), rgba(6, 32, 74, 0.8));
			transform: skew(-20deg);
			z-index: 1;
		}

		& .nav-item {
			position: absolute;
			display: block;
			width: 100%;
			height: 100%;
			line-height: 48px;
			text-align: center;
			font-size: 18px;
			font-weight: bold;
			background: linear-gradient(to right, #ffffff, #a6c6ee);
			-webkit-background-clip: text;
			background-clip: text;
			color: white;
			z-index: 2;
		}
	}

	.nav_bar_logo {
		min-width: 360px;
		max-width: 600px;
		height: 100%;
		padding-left: 24px;
		white-space: nowrap;
		user-select: none;
		& .title {
			font-family: 'youSheBiaoTiHei';
			font-size: 38px;
			font-weight: 400;
			color: #2875F4;
			text-shadow: 0px 4px 8px rgba(3, 4, 47, 0.3);
		}
		& img {
			height: 46px;
		}
		& p.title_max {
			padding-left: 4px;
			color: #fff;
			padding-top: 2px;
			letter-spacing: 2.5px;
			font-size: 14px;
			font-family: 'Microsoft YaHei';
		}
		& p.title_min {
			padding-left: 4px;
			color: #fff;
			font-size: 10px;
			height: 12px;
			line-height: 12px;
			font-family: 'Arial';
		}

		& span {
			font-size: 18px;
			padding-left: 10px;
			font-weight: bold;
			color: #fff;
			line-height: 56px;
			letter-spacing: 1px;
			font-family: PingFangSC-Semibold, PingFang SC;
		}
	}

	.nav_bar_right {
		width: calc(98% - 720px);
		height: 100%;
	}
</style>
