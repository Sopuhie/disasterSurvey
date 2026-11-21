import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve, dirname } from 'path'
import { readFileSync } from 'fs'
import { fileURLToPath } from 'url'
import compression from 'vite-plugin-compression'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

const __dirname = dirname(fileURLToPath(import.meta.url))
const configSwitcher = JSON.parse(readFileSync(resolve(__dirname, 'public/config.json'), 'utf-8'))
const configEnv = configSwitcher.baseURL
	? 'legacy'
	: configSwitcher.env === 'prod'
		? 'prod'
		: 'test'
const runtimeConfig = configEnv === 'legacy'
	? configSwitcher
	: JSON.parse(readFileSync(resolve(__dirname, `public/config.${configEnv}.json`), 'utf-8'))
const { prefix } = runtimeConfig
/** Vite base 必须以 / 结尾，否则 import.meta.env.BASE_URL 为 /disaster，拼 config.json 会变成 /disasterconfig.json */
const viteBase = prefix
	? `/${String(prefix).replace(/^\/+|\/+$/g, '')}/`
	: '/'
const port = 1415

// https://vitejs.dev/config/
export default defineConfig(async () => ({
	plugins: [
		vue(),
		AutoImport({
			resolvers: [ElementPlusResolver()],
			imports: ['vue', 'vue-router']
		}),
		Components({
			resolvers: [ElementPlusResolver()]
		}),
		compression({
			algorithm: 'gzip',
			ext: '.gz',
			threshold: 10240, //  默认为10KB
			deleteOriginFile: false,
			filter: /\.(js|css|json|html|ico|svg)(\?.*)?$/i,
			compressionOptions: { level: 9 }, // 指定gzip压缩级别，默认为9（最高级别）
			verbose: true,
			disable: false
		})
		// viteImagemin({
		// 	gifsicle: { optimizationLevel: 7, interlaced: false },
		// 	optipng: { optimizationLevel: 7 },
		// 	mozjpeg: { quality: 20 },
		// 	pngquant: { quality: [0.8, 0.9], speed: 4 },
		// 	svgo: {
		// 		plugins: [{ name: 'removeViewBox' }, { name: 'removeEmptyAttrs', active: false }]
		// 	}
		// })
	],
	optimizeDeps: {
		exclude: ['monaco-editor']
	},
	clearScreen: false,
	base: viteBase,
	server: {
		host: '0.0.0.0',
		publicPath: '',
		port,
		hmr: true,
		strictPort: true,
		/**
		 * 本地联调代理：
		 * - 后端 context-path=/zuul，端口见 application.yaml（默认 10401）
		 * - 若 config.*.json 的 baseURL 指向绝对地址（含 /zuul/），Axios 直连后端，可不经本代理
		 * - 若 baseURL 为相对路径 /zuul/ 或仅写 /api/v1，则由本代理转发，避免 404
		 */
		proxy: {
			'/zuul': {
				target: 'http://127.0.0.1:10401',
				changeOrigin: true
			},
			'/api/v1': {
				target: 'http://127.0.0.1:10401',
				changeOrigin: true,
				rewrite: path => `/zuul${path}`
			},
			'/web-disaster-evaluate': {
				target: 'http://127.0.0.1:10401',
				changeOrigin: true,
				rewrite: path => `/zuul${path}`
			},
			'/web-disaster-authentication': {
				target: 'http://127.0.0.1:10401',
				changeOrigin: true,
				rewrite: path => `/zuul${path}`
			}
		}
	},
	resolve: {
		alias: {
			'@': resolve(__dirname, './src')
		}
	},
	css: {
		preprocessorOptions: {
			css: { charset: false },
			scss: {
				additionalData: `@import "@/assets/css/fromMixin.scss";`
			}
		}
	},
	build: {
		minify: 'terser',
		outDir: 'disaster',
		chunkSizeWarningLimit: 10240,
		terserOptions: {
			compress: {
				pure_funcs: ['console.log'],
				// drop_console: true,
				drop_debugger: true
			}
		},
		rollupOptions: {
			output: {
				chunkFileNames: 'static/js/[name]-[hash].js',
				entryFileNames: 'static/js/[name]-[hash].js',
				assetFileNames: 'static/[ext]/[name]-[hash].[ext]'
				// manualChunks: id => {
				// 	// 将 node_modules 中的代码单独打包成一个 JS 文件
				// 	if (id.includes('node_modules')) {
				// 		return id.toString().split('node_modules/')[1].split('/')[0].toString()
				// 	}
				// }
			}
		}
	}
}))
