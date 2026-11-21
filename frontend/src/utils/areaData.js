/** 行政区名称工具：仅供运维组装 / 匹配，不再承载写死名录 */

const strip = value => String(value ?? '').trim()

const coreName = value => strip(value).replace(/(特别行政区|自治区|自治州|地区|盟|市|州|县|区|旗)$/g, '')

/** 行政区名称近似匹配（长乐市 / 长乐区 等） */
export const adminNameMatch = (a, b) => {
	const x = strip(a)
	const y = strip(b)
	if (!x || !y) return false
	if (x === y) return true
	if (x.includes(y) || y.includes(x)) return true
	const cx = coreName(x)
	const cy = coreName(y)
	return !!cx && !!cy && cx === cy
}

/**
 * 由运维市/县台账组装下拉树（只使用传入台账，不回落本地写死名录）
 */
export function buildAreaTree(counties = [], cities = []) {
	const cityNames = []
	const pushCity = name => {
		const text = strip(name)
		if (!text) return
		if (cityNames.some(item => adminNameMatch(item, text))) return
		cityNames.push(text)
	}
	;(cities || []).forEach(item => pushCity(item.name))
	;(counties || []).forEach(item => pushCity(item.city))

	return cityNames.map(cityName => {
		const childMap = new Map()
		;(counties || []).forEach(item => {
			if (!adminNameMatch(item.city, cityName)) return
			const name = strip(item.name)
			if (!name || [...childMap.keys()].some(key => adminNameMatch(key, name))) return
			childMap.set(name, { name })
		})
		return {
			name: cityName,
			children: [...childMap.values()]
		}
	})
}
