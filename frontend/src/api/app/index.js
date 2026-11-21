import request from '@/utils/request.js'

const prefix1 = '/web-disaster-authentication/web/disaster/authentication'
const prefix2 = '/web-disaster-common/web/disaster/common'

export function appApi() {
	return {
		// 用户登录认证及用户信息和token生成接口
		login: data => {
			const { yzm, requestCode, password, username } = data
			return request({
				url: prefix1 + '/user/verify/login',
				method: 'post',
				data: { password, username },
				headers: {
					yzm,
					requestCode
				}
			})
		},
		// 验证用户是否已登录系统
		checkLogin: params => {
			return request({
				url: prefix1 + '/user/verify/check/login',
				method: 'get',
				params
			})
		},
		// 登出系统
		loginOut: params => {
			return request({
				url: prefix1 + '/user/verify/loginOut',
				method: 'get',
				params
			})
		},
		// 用户密码修改接口
		userChangePassword: data => {
			return request({ url: prefix1 + '/user/changePassword', method: 'post', data })
		},
		// 根据角色ID获取对应用户信息接口
		roleId: params => {
			return request({
				url: prefix1 + '/user/verify/querybyroleid',
				method: 'get',
				params
			})
		},
		// 获取新token
		refreshToken: params => {
			return request({
				url: prefix1 + '/user/verify/refreshToken',
				method: 'get',
				params
			})
		},
		// 获取校验码接口
		checkCode: ({ requestCode }) => {
			return request({
				url: prefix2 + '/checkcode/generate?requestCode=' + requestCode,
				method: 'post',
				responseType: 'blob'
			})
		}
	}
}
