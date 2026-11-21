<template>
	<div class="login max-wh flex faj">
		<video id="background-video" playsinline autoplay muted loop>
			<source src="@/assets/images/bg.mp4" type="video/mp4" />
		</video>
		<div class="mask"></div>
		<div class="logo">
			<!-- <img src="@/assets/images/logo.png" height="66" alt="logo" /> -->
		</div>
		<div class="loginForm_box flex faj fc">
			<div class="loginForm_head noSelect">
				<p class="cn flex faj">{{ appTitle }}</p>
				<p class="en flex faj">{{ appTitleEn }}</p>
			</div>
			<div class="loginForm pt-32">
				<el-form ref="formRef" size="large" :model="form" label-width="auto" hide-required-asterisk>
					<el-form-item prop="username" :rules="[{ required: true, message: '请输入账号', trigger: 'blur' }]">
						<div class="form_item_box">
							<p class="form_label">用户名 | USER NAME</p>
							<el-input v-model.number="form.username" type="text" autocomplete="off" size="small" />
						</div>
					</el-form-item>
					<el-form-item prop="password" :rules="[{ required: true, message: '请输入密码', trigger: 'blur' }]">
						<div class="form_item_box">
							<p class="form_label">密码 | KEY</p>
							<el-input v-model.number="form.password" type="password" autocomplete="off" size="small" />
						</div>
					</el-form-item>
					<el-form-item
						prop="yzm"
						class="yzm-wrap flex"
						:rules="[{ required: true, message: '请输入验证码', trigger: 'blur' }]">
						<div class="form_item_box" style="width: 242px">
							<p class="form_label">验证码 | Verification Code</p>
							<el-input v-model="form.yzm" size="small" @keyup.enter.native="onSubmit(formRef)" />
						</div>
						<div class="yzm-box" @click="getCode">
							<img v-if="imgCode" :src="imgCode" alt="img" />
						</div>
					</el-form-item>
				</el-form>
				<el-button
					v-btn
					class="max-w"
					type="primary"
					style="
						border: 1px solid var(--el-text-color-primary);
						background-image: linear-gradient(to right, #585ae6, #adf0d6);
					"
					size="large"
					round
					@click="onSubmit(formRef)">
					<span>登录 | Sign Up</span>
				</el-button>
			</div>
		</div>
	</div>
	<!-- 修改密码 -->
	<FP v-if="store.openForgotPassword" @getCode="getCode"></FP>
</template>

<script setup>
	import FP from '@/components/forgotPassword/index.vue'
	import { appTitle, appTitleEn } from '@/config'
	import { JSEncrypt } from 'jsencrypt'
	import { appApi } from '@/api/app/index.js'
	import { useAppStore } from '@/store/modules/app.js'
	const store = useAppStore()

	const router = useRouter()

	const api = appApi()

	const formRef = ref(null)
	const imgCode = ref('')
	const form = reactive({
		username: 'NTocean_disaster',
		password: 'NTocean_disasterH#d3g@eH',
		requestCode: '',
		yzm: '',
		checked: false
	})

	onMounted(() => {
		localStorage.clear()
		getCode()
	})

	// 获取验证码
	const getCode = async () => {
		const requestCode = Date.now()
		form.requestCode = requestCode
		api
			.checkCode({ requestCode })
			.then(async res => {
				const imgBlob = new Blob([res], { type: 'image/jpeg' })
				await getImageBase64(imgBlob).then(res => {
					imgCode.value = res
				})
			})
			.catch(err => {})
	}

	const getImageBase64 = blob => {
		return new Promise((resolve, reject) => {
			const reader = new FileReader()
			reader.readAsDataURL(blob)
			reader.onload = () => {
				const base64 = reader.result
				resolve(base64)
			}
			reader.onerror = error => reject(error)
		})
	}

	// 登录
	const onSubmit = async formEl => {
		if (!formEl) return
		await formEl.validate(async (valid, fields) => {
			if (valid) {
				const encryptor = new JSEncrypt()
				encryptor.setPublicKey(
					'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDEC0vA9smJGtwB8mY5meFEnmkqfqkhBn1PKHV2FcA8GCnajkV/R7Lj6lnrVwgiyKiFT/JBVUagAy9shYB04fUTA+oHlgOj96KXf/ztkw4A9xgTb+4FWo0M2+znYEN8J/5/cJqMPtlB6fjJaNJ1XnLIYCJPwru03LVN4Z7Ijlpv4wIDAQAB'
				)
				const loginForm = { ...form }
				loginForm.password = encryptor.encrypt(form.password)
				const checkLogin = await api.checkLogin({ username: loginForm.username  || 'system' })
				// const checkCode = checkLogin?.data?.checkCode
				// if (checkCode === 'NO') {
				// 	login(loginForm)
				// } else {
				// 	const tip = checkLogin?.data?.tip
				// 	ElMessageBox.confirm(tip, '提示', {
				// 		confirmButtonText: '确定',
				// 		cancelButtonText: '取消',
				// 		type: 'Warning',
				// 		customClass: 'checkLogin-tip'
				// 	})
				// 		.then(() => {
				// 			login(loginForm)
				// 		})
				// 		.catch(() => {})
				// }
				login(loginForm)
			}
		})
	}
	const login = loginForm => {
		const loading = ElLoading.service({
			lock: true,
			text: 'Loading',
			background: 'rgba(0, 0, 0, 0.7)'
		})
		api
			.login(loginForm)
			.then(res => {
				const userInfo = res.data.userInfo
				store.setUserInfo(userInfo)
				store.setToken(res.data.token)
				loading.close()
				const firstLoginFlag = userInfo?.firstLoginFlag === 0 ? true : false
				if (firstLoginFlag) {
					store.setOpenForgotPassword(firstLoginFlag)
				} else {
					router.push('/')
				}
			})
			.catch(err => {
				getCode()
				loading.close()
			})
	}
</script>

<style lang="scss" scoped>
	.login {
		position: fixed;
		top: 0;
		left: 0;
		z-index: 2025;
		& .mask {
			position: fixed;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			z-index: 2;
		}
		& .loginForm_box {
			position: fixed;
			z-index: 99;
			padding-top: 56px;
			overflow: hidden;
		}
		& .loginForm_head {
			width: 375px;

			color: white;
			padding-bottom: 70px;
			& .cn {
				line-height: 1.5;
				font-size: 56px;
				margin-bottom: 16px;
				font-weight: bold;
				font-family: 'SourceHanSansCN-Regular';
			}
			& .en {
				letter-spacing: 20px;
				margin-left: 22px;
				font-size: 24px;
				font-family: 'Microsoft YaHei';
			}
		}
		& .loginForm {
			width: 410px;
			height: 378px;
			padding: 20px 24px;
			border-radius: 30px;
			background-color: rgba(29, 34, 50, 0.5);
		}
		& .form_item_box {
			position: relative;
			padding: 0 16px;
			width: 100%;
			height: 60px;
			border-radius: 8px;
			background-color: rgba(67, 68, 81, 1);
		}
		& .form_label {
			position: absolute;
			top: 0;
			left: 16px;
			z-index: 1;
			font-size: 14px;
			color: #bbbbbb;
			pointer-events: none;
		}
		:deep() {
			.el-input__wrapper {
				padding: 0;
				background-color: rgba(67, 68, 81, 1);
				box-shadow: none;
			}
			.el-form-item.is-error .el-input__wrapper,
			.el-form-item.is-error .el-input__wrapper.is-focus,
			.el-form-item.is-error .el-input__wrapper:focus,
			.el-form-item.is-error .el-input__wrapper:hover,
			.el-form-item.is-error .el-select__wrapper,
			.el-form-item.is-error .el-select__wrapper.is-focus,
			.el-form-item.is-error .el-select__wrapper:focus,
			.el-form-item.is-error .el-select__wrapper:hover,
			.el-form-item.is-error .el-textarea__inner,
			.el-form-item.is-error .el-textarea__inner.is-focus,
			.el-form-item.is-error .el-textarea__inner:focus,
			.el-form-item.is-error .el-textarea__inner:hover {
				box-shadow: none;
			}
			.el-input__inner {
				height: 60px;
				color: #bbbbbb !important;
				padding: 32px 0 0;
				font-size: 16px;
			}
		}
		& .logo {
			position: fixed;
			top: 90px;
			left: 176px;
			z-index: 3;
			object-fit: contain;
		}

		.yzm-box {
			width: 110px;
			height: 60px;
			border: 1px solid #dcdfe6;
			background-color: #f6f6f6;
			border-radius: 4px;
			margin-left: 10px;
			cursor: pointer;
			img {
				width: 100%;
				height: 100%;
			}
		}
	}
	#background-video {
		width: 100%;
		height: 100%;
		object-fit: cover;
	}
	:deep() {
		.el-form-item--large {
			margin-bottom: 32px;
		}
	}
</style>
