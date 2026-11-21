/**
 * 本地反馈挂件（平台 frontUrl 不可达时的前端联调回退）
 * 行为对齐平台 feedback-widget：右侧悬浮按钮 + 420px 滑出面板
 * 完整提交能力仍依赖平台 iframe / openapi；此处保证入口可见并可打开面板
 */
;(function (global) {
	if (global.Feedback) return

	var state = {
		opts: null,
		btn: null,
		panel: null,
		mask: null,
		iframe: null,
		opened: false
	}

	function css(el, obj) {
		for (var k in obj) el.style[k] = obj[k]
	}

	function ensureDom() {
		if (state.btn) return
		var btn = document.createElement('button')
		btn.type = 'button'
		btn.id = 'feedback-entry-btn'
		btn.textContent = (state.opts && state.opts.entryText) || '意见反馈'
		css(btn, {
			position: 'fixed',
			right: '0',
			top: '50%',
			transform: 'translateY(-50%)',
			zIndex: '99990',
			writingMode: 'vertical-rl',
			letterSpacing: '4px',
			padding: '16px 10px',
			border: 'none',
			borderRadius: '8px 0 0 8px',
			cursor: 'pointer',
			color: '#fff',
			background: (state.opts && state.opts.entryColor) || '#1677ff',
			boxShadow: '0 4px 12px rgba(0,0,0,.18)',
			fontSize: '14px',
			lineHeight: '1.4'
		})
		btn.addEventListener('click', function () {
			openPanel()
		})
		document.body.appendChild(btn)
		state.btn = btn

		var mask = document.createElement('div')
		css(mask, {
			position: 'fixed',
			inset: '0',
			background: 'rgba(0,0,0,.35)',
			zIndex: '99991',
			display: 'none'
		})
		mask.addEventListener('click', closePanel)
		document.body.appendChild(mask)
		state.mask = mask

		var panel = document.createElement('div')
		css(panel, {
			position: 'fixed',
			top: '0',
			right: '0',
			width: '420px',
			maxWidth: '100vw',
			height: '100%',
			background: '#fff',
			zIndex: '99992',
			boxShadow: '-4px 0 24px rgba(0,0,0,.15)',
			transform: 'translateX(100%)',
			transition: 'transform .25s ease',
			display: 'flex',
			flexDirection: 'column'
		})
		var head = document.createElement('div')
		css(head, {
			height: '48px',
			display: 'flex',
			alignItems: 'center',
			justifyContent: 'space-between',
			padding: '0 16px',
			borderBottom: '1px solid #eee',
			fontWeight: '600'
		})
		head.innerHTML =
			'<span>意见反馈</span><button type="button" style="border:none;background:transparent;font-size:18px;cursor:pointer;line-height:1">×</button>'
		head.querySelector('button').addEventListener('click', closePanel)
		panel.appendChild(head)

		var body = document.createElement('div')
		css(body, { flex: '1', position: 'relative', overflow: 'hidden' })
		var iframe = document.createElement('iframe')
		iframe.title = 'feedback'
		iframe.setAttribute('sandbox', 'allow-scripts allow-same-origin allow-forms allow-popups allow-downloads')
		iframe.setAttribute('allow', 'clipboard-read; clipboard-write')
		css(iframe, { width: '100%', height: '100%', border: '0' })
		body.appendChild(iframe)
		panel.appendChild(body)

		document.body.appendChild(panel)
		state.panel = panel
		state.iframe = iframe
	}

	function buildIframeSrc(opts) {
		var base = (opts.baseUrl || '').replace(/\/?$/, '/')
		var q = [
			'sysCode=' + encodeURIComponent(opts.sysCode || ''),
			'sysName=' + encodeURIComponent(opts.sysName || ''),
			'userId=' + encodeURIComponent((opts.user && opts.user.userId) || ''),
			'name=' + encodeURIComponent((opts.user && opts.user.name) || ''),
			'phone=' + encodeURIComponent((opts.user && opts.user.phone) || ''),
			'sign=' + encodeURIComponent(opts.sign || ''),
			'timestamp=' + encodeURIComponent(opts.timestamp || ''),
			'apiBase=' + encodeURIComponent(opts.apiBase || '')
		].join('&')
		return base + 'feedback.html?' + q
	}

	function refreshThen(cb) {
		var opts = state.opts
		if (!opts || typeof opts.refresh !== 'function') {
			cb()
			return
		}
		opts.refresh(function (next) {
			if (next && next.sign) {
				opts.sign = next.sign
				opts.timestamp = next.timestamp
			}
			cb()
		})
	}

	function openPanel() {
		ensureDom()
		refreshThen(function () {
			state.iframe.src = buildIframeSrc(state.opts)
			state.mask.style.display = 'block'
			state.panel.style.transform = 'translateX(0)'
			state.opened = true
		})
	}

	function closePanel() {
		if (!state.panel) return
		state.panel.style.transform = 'translateX(100%)'
		state.mask.style.display = 'none'
		state.opened = false
	}

	function destroy() {
		closePanel()
		;[state.btn, state.panel, state.mask].forEach(function (el) {
			if (el && el.parentNode) el.parentNode.removeChild(el)
		})
		state.btn = state.panel = state.mask = state.iframe = null
		state.opts = null
		state.opened = false
	}

	global.Feedback = {
		init: function (opts) {
			state.opts = opts || {}
			if (opts && opts.entry === false) return
			ensureDom()
			if (state.btn) {
				state.btn.textContent = opts.entryText || '意见反馈'
				state.btn.style.background = opts.entryColor || '#1677ff'
			}
		},
		open: openPanel,
		close: closePanel,
		destroy: destroy
	}
})(typeof window !== 'undefined' ? window : this)
