import SparkMD5 from 'spark-md5'

const getChunkList = async files => {
	const file = files
	const fileSize = file.size // 文件大小
	const fileName = file.name

	let chunkSize = 0
	if (fileSize <= 5 * 1024 * 1024) {
		// 0-5M，不分片
		chunkSize = fileSize
	} else if (fileSize <= 20 * 1024 * 1024) {
		// 5-20M，每个分片大小1M
		chunkSize = 1024 * 1024
	} else if (fileSize <= 50 * 1024 * 1024) {
		// 20-50M，每个分片大小2M
		chunkSize = 2 * 1024 * 1024
	} else if (fileSize <= 100 * 1024 * 1024) {
		// 50-100M，每个分片大小4M
		chunkSize = 4 * 1024 * 1024
	} else if (fileSize <= 200 * 1024 * 1024) {
		// 100-200M，每个分片大小6M
		chunkSize = 6 * 1024 * 1024
	} else if (fileSize <= 500 * 1024 * 1024) {
		// 200-500M，每个分片大小10M
		chunkSize = 10 * 1024 * 1024
	} else if (fileSize <= 1024 * 1024 * 1024) {
		// 500M-1G，每个分片大小20M
		chunkSize = 20 * 1024 * 1024
	} else {
		// 1G以上，每个分片大小20M
		chunkSize = 20 * 1024 * 1024
	}
	const totalChunks = Math.ceil(fileSize / chunkSize)
	let start = 0
	let end = Math.min(chunkSize, fileSize)
	let index = 0 // 分片索引,从0开始
	const chunks = [] // 存储当前文件的分片信息的数组

	while (start < fileSize) {
		const chunk = file.slice(start, end)
		const reader = new FileReader()
		const promise = new Promise((resolve, reject) => {
			reader.onload = e => {
				//读取文件分片信息，使用SparkMD5库计算分片的MD5值
				const spark = new SparkMD5.ArrayBuffer()
				spark.append(e.target.result)
				resolve(spark.end())
			}
			reader.onerror = err => {
				reject(err)
			}
		})
		reader.readAsArrayBuffer(chunk)
		try {
			const md5 = await promise
			const chunkInfo = { chunk, md5, index }
			chunks.push(chunkInfo)
		} catch (err) {
			reject(err)
		}
		//更新循环起止位置
		start = end
		end = Math.min(start + chunkSize, fileSize)
		index++
	}
	// 将当前文件的分片信息数组存入总的数组中
	return [chunks, totalChunks, fileSize, fileName, chunkSize] // 返回存储所有文件的分片信息的数组
}

const getMd5Hash = async fileChunkList => {
	return new Promise((resolve, reject) => {
		const spark = new SparkMD5.ArrayBuffer()
		const sliceVal = 100 / fileChunkList.length
		let percentage = 0
		let count = 0

		const loadNext = index => {
			const reader = new FileReader()
			reader.readAsArrayBuffer(fileChunkList[index].file)
			reader.onload = ev => {
				count++
				spark.append(ev.target.result)
				if (count === fileChunkList.length) {
					resolve({
						percentage: 100,
						hash: spark.end()
					})
				} else {
					percentage += sliceVal
					// resolve({ percentage })
					loadNext(count)
				}
			}
		}

		loadNext(0)
	})
}

export { getChunkList, getMd5Hash }
