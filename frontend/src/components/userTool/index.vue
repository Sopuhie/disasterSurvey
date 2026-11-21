<template>
	<div class="userTool flex fa">
		<i class="iconfont icon-home cp" style="color: #2875e0; font-size: 18px" @click="toPath"></i>

		<el-dropdown class="ml-10" @command="handleCommand" v-if="appStore.userInfo">
			<span class="el-dropdown-link">
				<i class="iconfont icon-user cp" style="color: #2875e0; font-size: 18px"></i>
			</span>
			<template #dropdown>
				<el-dropdown-menu>
					<el-dropdown-item>{{ appStore.userInfo?.username }}</el-dropdown-item>
					<!-- <el-dropdown-item command="ChangePassword">修改密码</el-dropdown-item> -->
					<!-- <el-dropdown-item command="theme">{{ themeTxt }}</el-dropdown-item> -->
					<el-dropdown-item command="logoOut">退出系统</el-dropdown-item>
				</el-dropdown-menu>
			</template>
		</el-dropdown>
	</div>
</template>

<script setup>
	const router = useRouter()

	import { useAppStore } from '@/store/modules/app.js'
	const appStore = useAppStore()

	const themeTxt = ref('主题一')

	const toPath = () => {
		router.push('/')
	}

	const handleCommand = command => {
		switch (command) {
			case 'theme':
				themeTxt.value = themeTxt.value === '主题二' ? '主题一' : '主题二'
				appStore.setAppTheme(themeTxt.value === '主题二' ? 'theme2' : 'theme1')
				break
			case 'ChangePassword':
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
			title: '提示',
			message: '此操作将退出登录, 是否继续?',
			showCancelButton: true,
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
				router.push('/login')
				window.localStorage.clear()
				window.sessionStorage.clear()
				setTimeout(() => {
					// 防止退出后，可以点击回退按钮，返回页面
					historyPush()
				}, 100)
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
	.userTool {
		position: fixed;
		top: 8px;
		right: 24px;
		z-index: 2024;
		height: 40px;
		border-radius: 4px;
		padding: 0 16px;
		box-shadow: 0px 6px 10px 0px rgba(0, 0, 0, 0.16);
		background-color: white;
	}
</style>
