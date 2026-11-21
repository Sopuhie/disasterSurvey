<!-- 附件预览 -->
<template>
	<div class="filePreview max-wh flex pl-10 pr-10">
		<div class="filePreview-wrapper" :class="{ max: flagStatus }">
			<ul v-if="fileList.length > 0" class="filePreview-list max-wh oy" v-show="flagStatus">
				<li
					class="textEs"
					v-for="(item, index) in fileList"
					:key="index"
					:class="{ active: index === currentIndex }"
					@click="handleClick(item, index)">
					{{ item.fileName }}
				</li>
			</ul>
			<div class="sidebarPanelFlag" @click.stop="flagSwitch">
				<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" color="white">
					<path
						fill="currentColor"
						d="M609.408 149.376 277.76 489.6a32 32 0 0 0 0 44.672l331.648 340.352a29.12 29.12 0 0 0 41.728 0 30.592 30.592 0 0 0 0-42.752L339.264 511.936l311.872-319.872a30.592 30.592 0 0 0 0-42.688 29.12 29.12 0 0 0-41.728 0z"></path>
				</svg>
			</div>
		</div>
		<div class="filePreview-content" :class="{ max: !flagStatus }">
			<DocxCom v-if="fileType === 'docx'" :fileUrl="fileUrl"></DocxCom>
			<XlsxCom v-else-if="fileType === 'xlsx'" :fileUrl="fileUrl"></XlsxCom>
			<PdfCom v-else-if="fileType === 'pdf'" :fileUrl="fileUrl"></PdfCom>
			<TxtCom
				v-else-if="['txt', 'json', 'js', 'css'].includes(fileType)"
				:fileType="fileType"
				:fileUrl="fileUrl"></TxtCom>
			<ImagesCom
				v-else-if="['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(fileType)"
				:fileUrl="fileUrl"></ImagesCom>
			<div class="fileBtn" v-if="fileName">
				<el-button v-btn type="primary" circle @click="handleDownload">
					<el-icon><Download /></el-icon>
				</el-button>
			</div>
			<div v-if="fileName && !accept.includes(fileType)" class="max-wh flex faj">当前文件不支持预览，请下载查看</div>
			<div v-if="fileName === ''" class="max-wh flex faj">请先选择文件</div>
		</div>
	</div>
</template>

<script setup>
	import DocxCom from '@/components/filerPreview/docx.vue'
	import XlsxCom from '@/components/filerPreview/excel.vue'
	import PdfCom from '@/components/filerPreview/pdf.vue'
	import TxtCom from '@/components/filerPreview/txt.vue'
	import ImagesCom from '@/components/filerPreview/images.vue'

	const currentIndex = ref(0)
	const flagStatus = ref(true)
	const fileType = ref('')
	const fileUrl = ref('')
	const fileName = ref('')
	const accept = reactive([
		'docx',
		'xlsx',
		'pdf',
		'txt',
		'json',
		'js',
		'css',
		'jpg',
		'jpeg',
		'png',
		'gif',
		'webp',
		'bmp'
	])

	const props = defineProps({
		fileList: {
			type: Array,
			default: () => []
		},
		fileBaseUrl: {
			type: String,
			default: ''
		}
	})
	onMounted(() => {
		if (props.fileList.length > 0) {
			handleClick(props.fileList[0], 0)
		}
	})

	// 点击文件
	const handleClick = (item, index) => {
		currentIndex.value = index
		fileName.value = item.fileName
		fileUrl.value = `${props.fileBaseUrl}${item.filePath}`
		fileType.value = item.fileType
	}

	// 下载文件
	const handleDownload = () => {
		if (fileType.value === 'pdf') {
			window.open(fileUrl.value + '#navpanes=0&view=FitH', '_blank')
		} else {
			const link = document.createElement('a')
			link.target = '_blank'
			link.href = fileUrl.value
			link.setAttribute('download', '')
			document.body.appendChild(link)
			link.click()
			document.body.removeChild(link)
		}
	}

	// 侧边栏开关
	const flagSwitch = () => {
		flagStatus.value = !flagStatus.value
	}
</script>

<style lang="scss" scoped>
	.filePreview {
		background-color: var(--el-bg-color);

		& .filePreview-wrapper {
			position: relative;
			width: 1px;
			height: 100%;
			border-right: 1px solid var(--el-border-color);
			&.max {
				width: 200px;
			}
			& li {
				height: 36px;
				line-height: 36px;
				cursor: pointer;
				&.active {
					color: var(--el-color-primary);
				}
			}
			& .sidebarPanelFlag {
				position: absolute;
				left: 100%;
				top: calc(50% - 25px);
				z-index: 100;
				width: 8px;
				height: 50px;
				text-align: center;
				line-height: 50px;
				border-radius: 0 69px 69px 0;
				background: #919eab;
				cursor: pointer;
			}
		}
		& .filePreview-content {
			position: relative;
			width: calc(100% - 200px);
			height: 100%;
			&.max {
				width: 100%;
			}
		}

		& .fileBtn {
			position: absolute;
			bottom: 16px;
			right: 16px;
			z-index: 99;
		}
	}
</style>
