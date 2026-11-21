<template>
	<div class="forgotPassword flex faj" v-if="firstLoginFlag || store.openForgotPassword">
		<div class="forgotPassword-con maxwh pl-36 pr-36 pt-50 flex fa">
			<div class="item l flex fc">
				<p class="icon">
					<svg
						t="1712133950097"
						class="icon"
						viewBox="0 0 1024 1024"
						version="1.1"
						xmlns="http://www.w3.org/2000/svg"
						p-id="5873"
						width="88"
						height="88">
						<path
							d="M128 480h64a32 32 0 0 1 32 32v16a32 32 0 0 1-32 32H128a32 32 0 0 1-32-32v-16a32 32 0 0 1 32-32zM864 464h64a32 32 0 0 1 32 32v16a32 32 0 0 1-32 32h-64a32 32 0 0 1-32-32v-16a32 32 0 0 1 32-32zM624.672 464c-42.672-17.168-103.968-73.344-103.968-73.344s-45.76 41.44-83.44 63.536l-111.376 22.48-8.368 189.344c0 112.224 203.184 234.448 203.184 234.448s203.184-122.24 203.184-234.448V471.904c0 0.016-56.56 9.264-99.216-7.904z m-70.288 213.856v88.16a32.368 32.368 0 0 1-32.512 32.384 32.528 32.528 0 0 1-32.512-32.384v-88.16a81.296 81.296 0 0 1 32.512-155.792 81.28 81.28 0 0 1 32.512 155.792z"
							fill="#95EAFF"
							p-id="5874"></path>
						<path
							d="M200.192 544H81.344a16 16 0 0 0 0 32h118.848a16 16 0 0 0 0-32zM952.192 528h-118.848a16 16 0 0 0 0 32h118.848a16 16 0 0 0 0-32zM200.192 464H81.344a16 16 0 0 0 0 32h118.848a16 16 0 0 0 0-32zM817.344 464a16 16 0 0 0 16 16h118.848a16 16 0 0 0 0-32h-118.848a16 16 0 0 0-16 16z"
							fill="#49A3FE"
							p-id="5875"></path>
						<path d="M832 272m-48 0a48 48 0 1 0 96 0 48 48 0 1 0-96 0Z" fill="#49A3FE" p-id="5876"></path>
						<path
							d="M512 512a96 96 0 0 0-96 96 95.84 95.84 0 0 0 48 82.944v77.184A48.016 48.016 0 0 0 512 816c26.592 0 48-21.392 48-47.872v-77.184A95.84 95.84 0 0 0 608 608a96 96 0 0 0-96-96z m25.6 154.688a16 16 0 0 0-9.6 14.656v86.784a16.016 16.016 0 0 1-32 0v-86.784a15.968 15.968 0 0 0-9.6-14.656A64.032 64.032 0 0 1 512 544a64 64 0 0 1 25.6 122.688z"
							fill="#49A3FE"
							p-id="5877"></path>
						<path
							d="M878.112 125.856H145.888c-49.568 0-89.888 40.048-89.888 89.456v385.36c0 49.424 40.368 89.456 90.096 89.456h30.944a16 16 0 0 0 0-32H146.096c-32.112 0-58.096-25.76-58.096-57.456V215.296c0-31.68 25.936-57.456 57.888-57.456h732.224c31.984 0 57.888 25.744 57.888 57.456v385.36c0 31.648-26 57.456-57.936 57.456h-50.096a16 16 0 0 0 0 32h50.096c49.552 0 89.936-40.064 89.936-89.456V215.296c0-49.408-40.272-89.44-89.888-89.44z"
							fill="#0F13AE"
							p-id="5878"></path>
						<path
							d="M339.168 476.672c69.744 0 134.304-32.208 179.968-86.528 45.664 54.32 110.224 86.528 179.968 86.528 12.752 0 25.392-1.056 37.824-3.168a16.016 16.016 0 0 0-5.328-31.568c-10.688 1.808-21.536 2.72-32.496 2.72-64.672 0-124.64-31.984-164.672-86.176a15.84 15.84 0 0 0-15.296-6.112 15.872 15.872 0 0 0-15.296 6.112c-40.032 54.192-100 86.176-164.672 86.176-10.96 0-21.824-0.912-32.496-2.72a15.84 15.84 0 0 0-16.592 8.32 15.76 15.76 0 0 0-8.864 13.344l-9.184 207.68c-0.016 11.2 1.392 21.872 4.112 32.688 12.704 50.464 53.216 102.496 112.752 154.768a877.28 877.28 0 0 0 78.048 60.672c9.312 6.448 17.952 12.208 25.728 17.2 4.704 3.008 8.096 5.12 9.968 6.24a16 16 0 0 0 16.496 0c1.872-1.12 5.264-3.232 9.968-6.24a840.416 840.416 0 0 0 25.728-17.2 873.648 873.648 0 0 0 78.048-60.672c59.536-52.256 100.048-104.304 112.752-154.768 2.72-10.816 4.128-21.488 4.128-31.984v-159.744a16 16 0 0 0-32 0V672c0 7.792-1.056 15.84-3.152 24.176-10.72 42.624-47.76 90.192-102.832 138.528a838.88 838.88 0 0 1-75.168 58.416c-8.976 6.224-17.296 11.76-24.768 16.544l-0.944 0.592-0.944-0.592a835.152 835.152 0 0 1-24.768-16.544 844.8 844.8 0 0 1-75.168-58.416c-55.072-48.336-92.096-95.904-102.832-138.528A97.536 97.536 0 0 1 304 672l8.72-197.056c8.752 1.024 17.552 1.728 26.448 1.728z"
							fill="#0F13AE"
							p-id="5879"></path>
					</svg>
				</p>
				<span class="mt-66 mb-24" style="font-size: 36px; height: 44px; line-height: 44px">修改密码</span>
				<span style="font-size: 14px">初次登录，需要修改默认密码后，方可访问系统</span>
			</div>
			<div class="item pl-24">
				<el-form
					ref="ruleFormRef"
					label-position="top"
					label-width="auto"
					:model="ruleForm"
					:rules="rules"
					status-icon
					style="max-width: 600px">
					<el-form-item label="当前密码" prop="originPassword">
						<el-input
							v-model="ruleForm.originPassword"
							type="password"
							clearable
							autocomplete="off"
							placeholder="输入您的密码" />
					</el-form-item>
					<el-form-item label="新密码" prop="targetPassword">
						<el-input
							v-model="ruleForm.targetPassword"
							type="password"
							clearable
							autocomplete="off"
							placeholder="输入您的密码" />
					</el-form-item>
					<el-form-item label="确认密码" prop="password">
						<el-input
							v-model="ruleForm.password"
							type="password"
							clearable
							autocomplete="off"
							placeholder="输入您的密码" />
					</el-form-item>
					<el-form-item style="width: 100%">
						<div class="flex jend mt24" style="width: 100%">
							<el-button size="large" round v-if="store.openForgotPassword" @click="close">
								<span>取消</span>
							</el-button>
							<el-button size="large" round @click="toLogin">
								<span>返回登录页</span>
							</el-button>
							<el-button class="ml16" type="primary" size="large" round @click="submitForm(ruleFormRef)">
								<span>完 成</span>
							</el-button>
						</div>
					</el-form-item>
				</el-form>
			</div>
		</div>
	</div>
</template>

<script setup>
	import { JSEncrypt } from 'jsencrypt'

	const router = useRouter()

	import { useAppStore } from '@/store/modules/app.js'
	const store = useAppStore()

	import { appApi } from '@/api/app/index.js'
	const http = appApi()

	const firstLoginFlag = ref(0)
	const ruleFormRef = ref(null)
	const ruleForm = reactive({
		loginUser: '',
		originPassword: '',
		targetPassword: '',
		password: '',
		userId: ''
	})

	const validatePass0 = (rule, value, callback) => {
		if (value === '') {
			callback(new Error('请输入密码'))
		} else {
			if (ruleForm.originPassword !== '') {
				if (!ruleFormRef.value) return
				ruleFormRef.value.validateField('originPassword', () => null)
			}
			callback()
		}
	}
	const validatePass = (rule, value, callback) => {
		if (value === '') {
			callback(new Error('请输入密码'))
		} else {
			if (ruleForm.targetPassword !== '') {
				if (!ruleFormRef.value) return
				ruleFormRef.value.validateField('targetPassword', () => null)
			}
			callback()
		}
	}
	const validatePass2 = (rule, value, callback) => {
		if (value === '') {
			callback(new Error('请再次输入密码'))
		} else if (value !== ruleForm.password) {
			callback(new Error('两次输入不匹配！'))
		} else {
			callback()
		}
	}
	const rules = reactive({
		originPassword: [{ validator: validatePass0, trigger: 'blur' }],
		targetPassword: [{ validator: validatePass, trigger: 'blur' }],
		password: [{ validator: validatePass2, trigger: 'blur' }]
	})

	onMounted(() => {
		firstLoginFlag.value = store.userInfo?.firstLoginFlag === 0 ? true : false
		// 更新token
		const username = store.userInfo?.username || 'system'
		http.refreshToken({ username }).then(res => {
			store.setToken(res.data)
		})
	})

	const close = () => {
		store.setOpenForgotPassword(false)
	}
	const toLogin = () => {
		router.push('/login')
	}
	const submitForm = formEl => {
		if (!formEl) return
		formEl.validate(valid => {
			if (valid) {
				submit()
			} else {
				return false
			}
		})
	}
	const emits = defineEmits(['getCode'])
	const submit = () => {
		const encryptor = new JSEncrypt()
		encryptor.setPublicKey(
			'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDEC0vA9smJGtwB8mY5meFEnmkqfqkhBn1PKHV2FcA8GCnajkV/R7Lj6lnrVwgiyKiFT/JBVUagAy9shYB04fUTA+oHlgOj96KXf/ztkw4A9xgTb+4FWo0M2+znYEN8J/5/cJqMPtlB6fjJaNJ1XnLIYCJPwru03LVN4Z7Ijlpv4wIDAQAB'
		)
		let form = { ...toRaw(ruleForm) }
		form.userId = store.userInfo.userId
		form.loginUser = store.userInfo.username || 'system'
		form.originPassword = encryptor.encrypt(form.originPassword)
		form.targetPassword = encryptor.encrypt(form.targetPassword)
		http
			.userChangePassword(form)
			.then(res => {
				if (res.resultCode === 'S') {
					store.setOpenForgotPassword(false)
					emits('getCode')
					// router.push('/login')
					ElMessage({ message: res.resultMsg, type: 'success' })
				}
			})
			.catch(err => {
				ElMessage.error(err.message)
			})
	}
</script>

<style lang="scss" scoped>
	.forgotPassword {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		z-index: 1000;
		background: rgba(0, 0, 0, 0.8);
		&-con {
			width: 50%;
			height: 50%;
			position: relative;
			border-radius: 36px;
			overflow: hidden;
			background-color: white;
			& .item {
				width: 50%;
				height: 100%;
			}
			& .l {
				& .icon {
					position: absolute;
					top: 8px;
					left: 16px;
				}
				& span {
					color: #1f1f1f;
					display: inline-block;
				}
			}
		}
	}
</style>
