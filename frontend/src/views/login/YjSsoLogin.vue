<template>
	<div class="yj-sso-login flex faj">
		<p>{{ tip }}</p>
	</div>
</template>

<script setup>
	import { authApi } from '@/api/auth/index.js'
	import { useAppStore } from '@/store/modules/app.js'
	import resMsg from '@/utils/msg.js'
	import {
		getQueryParam,
		applySsoLoginData,
		clearSsoCallbackQuery,
		redirectYjssoRetry,
		jumpToYjssoLogin
	} from '@/utils/sso.js'

	const api = authApi()
	const appStore = useAppStore()
	const router = useRouter()
	const tip = ref('一级单点登录中…')

	const doLogin = async () => {
		localStorage.setItem('pType', 'st')
		const ticket = getQueryParam('st')
		if (!ticket) {
			tip.value = '缺少 ticket，即将跳转一级授权…'
			jumpToYjssoLogin()
			return
		}
		try {
			const res = await api.yjssoSsoLogin({ ticket })
			if (applySsoLoginData(res, appStore)) {
				tip.value = '登录成功，正在进入系统…'
				clearSsoCallbackQuery()
				router.replace('/provinceSystem')
			} else {
				tip.value = '登录失败，即将重新授权…'
				resMsg({ message: res?.resultMsg || '一级单点登录失败', type: 'error', duration: 3000 })
				redirectYjssoRetry()
			}
		} catch (err) {
			tip.value = '登录失败，即将重新授权…'
			resMsg({ message: err?.data?.resultMsg || '一级单点登录失败', type: 'error', duration: 3000 })
			redirectYjssoRetry()
		}
	}

	onMounted(() => {
		doLogin()
	})
</script>

<style scoped>
	.yj-sso-login {
		width: 100%;
		height: 100vh;
		font-size: 16px;
		color: #333;
	}
</style>
