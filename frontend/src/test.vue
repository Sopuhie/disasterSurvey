<template>
	<div class="max-wh test">
		<h2>test</h2>
	</div>
</template>

<script setup lang="ts">
	import {
		ref,
		toRefs,
		reactive,
		defineProps,
		defineEmits,
		computed,
		watch,
		watchEffect,
		onUnmounted,
		provide,
		inject
	} from 'vue'

	const count = ref(0)
	const state = reactive({
		counter: 0
	})

	// --------- props
	// 1
	// const props = defineProps(['title'])

	// 2
	// type Props = {
	// 	title: string
	// }
	// defineProps<Props>()

	// 3
	const props = defineProps({
		// 可以设置传来值的类型
		// title: String
		// 可以设置传来值的多种类型
		// title: [String,Number]
		// 可以设置传来值的类型和默认值
		title: {
			type: String,
			default: ''
		}
	})

	// --------- emit
	const emit = defineEmits(['change', 'delete'])
	const handleClick = () => {
		emit('change', 5) // 传给父组件的值的方法
	}
	const handleDelete = () => {
		emit('delete') // 传给父组件的值的方法
	}

	// --------- watch
	// 1
	// 监听name改变
	watch(count, () => {
		console.log(`count变成了${count}`)
		setTimeout(() => {
			console.log(count.value)
		}, 1000)
	},{deep:true,immediate:true})
	// 2
	watchEffect(() => console.log(count.value))

	// --------- computed
	const howCount = computed(() => '现在count值为：' + count.value)

	// --------- onUnmounted
	const timer = setInterval(() => {
		state.counter++
	}, 1000)

	onUnmounted(() => {
		clearInterval(timer)
	})

	// --------- defineExpose
	// 将方法、变量暴露给父组件使用，父组件才可通过ref API拿到子组件暴露的数据
	defineExpose({
		// 解构state
		...toRefs(state),
		// 声明方法
		changeName() {
			state.counter = 2
		}
	})

	// --------- 路由useRoute和useRouter
	import { useRoute, useRouter, onBeforeRouteLeave, onBeforeRouteUpdate } from 'vue-router'
	// 必须先声明调用
	const route = useRoute()
	const router = useRouter()

	// 路由信息
	console.log(route.query)
	// 路由跳转
	router.push('/newPage')

	// 路由导航守卫
	// 添加一个导航守卫，在当前组件将要离开时触发。
	onBeforeRouteLeave((to, from, next) => {
		next()
	})

	// 添加一个导航守卫，在当前组件更新时触发。
	// 在当前路由改变，但是该组件被复用时调用。
	onBeforeRouteUpdate((to, from, next) => {
		next()
	})

	// --------- pinia
	import { useStore } from '@/store/store.js'
	const store = useStore()

	function update() {
		// 通过 actions 定义的方法修改 state
		store.updateData({ name: 'Tom', sex: '女' })

		// 通过 store 直接修改
		store.data = { name: 'Tom', sex: '女' }

		// 同时改变多个状态
		store.$patch(state => {
			state.data = { name: 'Tom', sex: '女' }
			state.count = 2
		})

		// 替换整个 state
		store.$state = {
			age: 18,
			type: '美女'
		}

		// 重置状态
		store.$reset()
	}

	// --------- provide和inject
	// 父组件
	// 声明provide
	provide('provideState', {
		count,
		changeName: () => {
			count.value = 4
		}
	})

	// 子组件
	const provideState = inject('provideState', {})
	// 子组件触发name改变
	provideState.changeName()

	// --------- 自定义指令directive
	// v-demo
	import { createApp } from 'vue'
	const app = createApp({})
	app.directive('demo', {
		// 在绑定元素的 attribute 前或事件监听器应用前调用
		created(el, binding, vnode, prevVnode) {},
		// 在元素被插入到 DOM 前调用
		beforeMount(el, binding, vnode, prevVnode) {},
		// 在绑定元素的父组件
		// 及他自己的所有子节点都挂载完成后调用
		mounted(el, binding, vnode, prevVnode) {},
		// 绑定元素的父组件更新前调用
		beforeUpdate(el, binding, vnode, prevVnode) {},
		// 在绑定元素的父组件
		// 及他自己的所有子节点都更新后调用
		updated(el, binding, vnode, prevVnode) {},
		// 绑定元素的父组件卸载前调用
		beforeUnmount(el, binding, vnode, prevVnode) {},
		// 绑定元素的父组件卸载后调用
		unmounted(el, binding, vnode, prevVnode) {}
	})

	// --------- v-bind() CSS变量注入
	// 在css中使用变量
	// 常量声明样式

	// 使用常量声明的样式
	// background-color: v-bind(background);
	const background = 'red'

	// --------- 对 await 的支持
	// 不必再配合 async 就可以直接使用 await 了，这种情况下，组件的 setup 会自动变成 async setup
	const post = await fetch('/api').then(() => {})

	// --------- 定义组件的name
	// 1  安装插件：vite-plugin-vue-setup-extend
	// 2  配置 vite.config.ts
	// import VueSetupExtend from 'vite-plugin-vue-setup-extend'
	// export default defineConfig({
	// 	plugins: [VueSetupExtend()]
	// })
  // 3 <script setup name="ComponentName"></script>
</script>

<style lang="scss" scoped></style>
