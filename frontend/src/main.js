import { loadAppConfig } from '@/config'

loadAppConfig()
	.then(() => import('./bootstrap.js'))
	.catch(err => {
		console.error(err)
		document.body.innerHTML =
			'<div style="padding:24px;font-family:sans-serif;color:#c00;">配置文件加载失败，请检查 public/config.json 与 config.test.json / config.prod.json 是否存在且可访问。</div>'
	})
