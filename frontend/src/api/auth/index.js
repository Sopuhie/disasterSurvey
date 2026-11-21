import request from '@/utils/authRequest.js'
import { isZwzt } from '@/config'

const prefix3 = '/web-santech-authentication/web/santech/authentication'
const prefix5 = '/web-disaster-authentication/web/disaster/authentication/user/verify/sso'
const prefixYjsso = '/web-disaster-authentication/web/disaster/authentication/user/verify/yjsso'
const st = localStorage.getItem('pType') === 'st' || false

export function authApi() {
	return {
		// ----------------------------------------------------
		// 用户登录认证及用户信息和token生成接口
		verifyLogin: params => {
			if (isZwzt) {
				return request({
					url: prefix5 + '/zwzt/login',
					method: 'get',
					params
				})
			} else {
				return request({
					url: prefix3 + (st ? '/user/verify/sso/hsfj/login' : '/user/verify/login'),
					method: 'get',
					params
				})
			}
		},
		// 一级登录-认证并获取登录用户信息
		yjssoSsoLogin: params => {
			return request({
				url: prefixYjsso + '/ssoLogin',
				method: 'get',
				params
			})
		},
		// 一级登录-接收授权码回调接口（一般由中转/后端直调，前端通常不需要）
		yjssoSsoCode: params => {
			return request({
				url: prefixYjsso + '/ssoCode',
				method: 'get',
				params
			})
		},
		// 登出系统
		verifyLogout: params => {
			return request({
				url: prefix3 + (st ? '/user/verify/sso/hsfj/logout' : '/user/verify/logout'),
				method: 'get',
				params
			})
		},
		// 获取新token
		verifyRefreshToken: params => {
			if (isZwzt) {
				return request({
					url: prefix5 + '/zwzt/refreshToken',
					method: 'get',
					params
				})
			} else {
				return request({
					url: prefix3 + (st ? '/user/verify/sso/hsfj/refreshToken' : '/user/verify/refreshToken'),
					method: 'get',
					params
				})
			}
		}
	}
}
