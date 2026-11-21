package com.santech.framework.utils.tool;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class FileUtil {

    /**
     * MultipartFile 转 File
     *
     * @param file
     * @throws Exception
     */
    public static File multipartFileToFile(MultipartFile file) throws Exception {

        File toFile = null;
        if (file.equals("") || file.getSize() <= 0) {
            toFile = null;
        } else {
            InputStream ins = null;
            ins = file.getInputStream();
            toFile = new File(file.getOriginalFilename());
            inputStreamToFile(ins, toFile);
            ins.close();
        }
        return toFile;
    }

    //获取流文件
    public static void inputStreamToFile(InputStream ins, File file) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (null != os) {
                    os.close();
                }
                if (null != ins) {
                    ins.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
    }

    /**
     * 删除本地临时文件
     *
     * @param file
     */
    public static boolean delFile(File file) {
        boolean b = false;
        if (file != null) {
            File del = new File(file.toURI());
            b = del.delete();
        }
        return b;
    }

    /**
     * 写文件
     *
     * @param content
     * @param path    文件路径
     */
    public static boolean writeFile(String content, String path) throws Exception {
        boolean result = false;
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            File file = new File(path);
            //路径目录不存在时创建目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            fos = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
            bw.write(content);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ie) {
                result = false;
            }
            result = true;
        }
        return result;
    }

    /**
     * 将Byte数组转换成文件
     *
     * @param bytes
     * @param filePath
     * @param fileName
     */
    public static boolean getFileByBytes(byte[] bytes, String filePath, String fileName) {
        boolean result;
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {// 判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath + "/" + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            result = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result = false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    /***
      * @Description: 上传文件
      * @param path
      * @param multipartFile
      * @return: boolean
      **/
    public static boolean writeFile(String path, MultipartFile multipartFile) {
        boolean result = false;
        try {
            // 文件上传路径
            if (!cn.hutool.core.io.FileUtil.exist(path)) {
                cn.hutool.core.io.FileUtil.mkdir(path);
            }

            // 文件名
            String fileName = cn.hutool.core.io.FileUtil.cleanInvalid(multipartFile.getOriginalFilename());

            // 创建file对象
            File file = cn.hutool.core.io.FileUtil.file(path + fileName);
            // 创建文件
            File touchFile = cn.hutool.core.io.FileUtil.touch(file);
            //将文件流写入文件中
            cn.hutool.core.io.FileUtil.writeFromStream(multipartFile.getInputStream(), touchFile);
            result = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * @param path          文件路径
     * @param newFileName   生成的文件名
     * @param multipartFile 文件
     * @Description: 上传文件
     * @return: boolean
     **/
    public static boolean writeFile(String path, String newFileName, MultipartFile multipartFile) {
        boolean result = false;
        try {
            // 文件上传路径
            if (!cn.hutool.core.io.FileUtil.exist(path)) {
                cn.hutool.core.io.FileUtil.mkdir(path);
            }

            // 文件名
            String fileName = cn.hutool.core.io.FileUtil.cleanInvalid(multipartFile.getOriginalFilename());

            // 创建file对象
            File file = cn.hutool.core.io.FileUtil.file(path + "/" + newFileName + "." + fileName.substring(fileName.lastIndexOf(".") + 1));
            if (!cn.hutool.core.io.FileUtil.exist(file)) {
                // 创建文件
                File touchFile = cn.hutool.core.io.FileUtil.touch(file);
                //将文件流写入文件中
                cn.hutool.core.io.FileUtil.writeFromStream(multipartFile.getInputStream(), touchFile);
                result = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * @param multipartFile
     * @Description: 获取上传文件名称
     * @return: java.lang.String
     **/
    public static String getMultipartFileName(MultipartFile multipartFile) {
        return cn.hutool.core.io.FileUtil.cleanInvalid(multipartFile.getOriginalFilename()).split("\\.")[0];
    }

    /**
     * @param multipartFile
     * @Description: 获取上传文件后缀 如pdf
     * @return: java.lang.String
     **/
    public static String getMultipartFileType(MultipartFile multipartFile) {
        return cn.hutool.core.io.FileUtil.cleanInvalid(multipartFile.getOriginalFilename()).split("\\.")[1];
    }

    /**
     * @param fileName 文件名
     * 根据文件名后缀获取文件类型分类
     */
    public static String getFileTypeClassification(String fileName) {
        String fileType = "other";
        String suffix = FileNameUtil.getSuffix(fileName);
        if ("png".equalsIgnoreCase(suffix) || "jpg".equalsIgnoreCase(suffix) || "jpeg".equalsIgnoreCase(suffix) || "gif".equalsIgnoreCase(suffix) || "bmp".equalsIgnoreCase(suffix)) {
            fileType = "img";
        } else if ("pdf".equalsIgnoreCase(suffix)) {
            fileType = "pdf";
        } else if ("zip".equalsIgnoreCase(suffix) || "rar".equalsIgnoreCase(suffix) || "7z".equalsIgnoreCase(suffix)) {
            fileType = "zip";
        }  else if ("doc".equalsIgnoreCase(suffix) || "docx".equalsIgnoreCase(suffix)) {
            fileType = "doc";
        } else if ("xls".equalsIgnoreCase(suffix) || "xlsx".equalsIgnoreCase(suffix)) {
            fileType = "xls";
        } else if ("sql".equalsIgnoreCase(suffix)) {
            fileType = "sql";
        } else if ("json".equalsIgnoreCase(suffix)) {
            fileType = "json";
        }
        return fileType;
    }

    /**
     * @param file
     * @param list 记录所有文件路径
     * @Description: 遍历文件路径
     * @return: java.util.List<java.lang.String>
     **/
    public static List<String> traverseFilePath(File file, List<String> list) {
        File[] fs = file.listFiles();
        if (ObjectUtil.isNotEmpty(fs)) {
            for (File f : fs) {
                //若是目录，则递归打印该目录下的文件
                if (f.isDirectory()) {
                    list.add(f.getPath());
                    traverseFilePath(f, list);
                }
                //若是文件，直接打印
                if (f.isFile()) {
                    list.add(f.getPath());
                }
            }
        }
        return list;
    }

    /**
     * @param file
     * @param list       记录所有文件路径
     * @param level      遍历的层级
     * @Description: 遍历文件路径
     **/
    public static List<String> traverseFilePath(File file, List<String> list, int level) {
        File[] fs = file.listFiles();
        if (ObjectUtil.isNotEmpty(fs)) {
            level -= 1;
            for (File f : fs) {
                //若是目录，则递归打印该目录下的文件
                if (f.isDirectory()) {
                    if (level >= 0) {
                        list.add(f.getPath());
                        traverseFilePath(f, list, level);
                    } else {
                        continue;
                    }
                }
                //若是文件，直接打印
                if (f.isFile()) {
                    list.add(f.getPath());
                }
            }
        }
        return list;
    }

    /**
     * @param file
     * @param list       记录所有文件路径
     * @param level      遍历的层级
     * @param filterList 过滤的条件，即只遍历某些文件夹
     * @Description: 遍历文件路径
     **/
    public static List<String> traverseFilePath(File file, List<String> list, int level, List<String> filterList) {
        File[] fs = file.listFiles();
        if (ObjectUtil.isNotEmpty(fs)) {
            if (filterList.size() > 0) {
                for (File f : fs) {
                    String a = f.getPath().substring(f.getPath().indexOf(file.getPath()) + file.getPath().length() + 1);
                    if (filterList.contains(a)) {
                        list.add(f.getPath());
                        traverseFilePath(f,list,level-1);
                    }
                }
            } else {
                traverseFilePath(file,list,level);
            }
        }
        return list;
    }

    /**
     * @param path
     * @Description: 判断文件路径是否存在，不存在则创建
     * @return: boolean
     **/
    public static boolean mkdirFile(String path) {
        boolean result = false;
        File file = new File(path);
        if (!file.exists()) {
            result = file.mkdirs();
        } else {
            result = true;
        }
        return result;
    }

    /**
     * 复制文件
     */
    public static File copy(String srcPath, String destPath, boolean isOverride) {
        return cn.hutool.core.io.FileUtil.copy(srcPath, destPath, isOverride);
    }

    /**
     * 重命名文件
     */
    public static File rename(File file, String newName, boolean isOverride) {
        return cn.hutool.core.io.FileUtil.rename(file, newName, isOverride);
    }

    /**
     * 移动文件
     */
    public static void move(File src, File dest, boolean isOverride) {
        cn.hutool.core.io.FileUtil.move(src, dest, isOverride);
    }

    /**
     * 判断文件是否存在，如果path为null，则返回false
     *
     * @param path 文件路径
     * @return 如果存在返回true
     */
    public static boolean exist(String path) {
        return cn.hutool.core.io.FileUtil.exist(path);
    }

    /**
     * 判断文件是否存在，如果file为null，则返回false
     *
     * @param file 文件
     * @return 如果存在返回true
     */
    public static boolean exist(File file) {
        return cn.hutool.core.io.FileUtil.exist(file);
    }

    /**
     * 将流的内容写入文件<br>
     *
     * @param in           输入流
     * @param fullFilePath 文件绝对路径
     * @return 目标文件
     * @throws IORuntimeException IO异常
     */
    public static File writeFromStream(InputStream in, String fullFilePath) throws IORuntimeException {
        return cn.hutool.core.io.FileUtil.writeFromStream(in, fullFilePath);
    }

    /**
     * @param fileHome
     * @param relativePath
     * @Description: 获取文件路径树
     * @return: java.util.List<cn.hutool.core.lang.tree.Tree < java.lang.String>>
     **/
    public static List<Tree<String>> filePathTree(String fileHome, String relativePath) {
        List<Tree<String>> treeNodes = null;
        String filePath = fileHome + relativePath;
        File file = new File(filePath);

        //获取文件路径
        List<String> list = new LinkedList<>();
        list = FileUtil.traverseFilePath(file, list);

        List<String> resultList = new LinkedList<>();
        for (String s : list) {
            s = s.replace("\\", "/").replace(filePath + "/", "");
            resultList.add(s);
        }
        //文件路径树
        List<Map<String, String>> mapList = new LinkedList<>();
        Map<String, String> map = null;
        for (String path : resultList) {
            map = new LinkedHashMap<>();
            if (path.indexOf("/") == -1) {
                map.put("parentId", "-1");
                map.put("name", path);
            } else {
                map.put("parentId", path.substring(0, path.lastIndexOf("/")));
                map.put("name", path.substring(path.lastIndexOf("/") + 1));
            }
            map.put("id", path);
            map.put("path", relativePath + "/" + path);
            mapList.add(map);
        }
        treeNodes = toTree(mapList);
        return treeNodes;
    }

    /**
     * @param list
     * @Description: 列表转化为树
     * @return: java.util.List<cn.hutool.core.lang.tree.Tree < java.lang.String>>
     **/
    private static List<Tree<String>> toTree(List<Map<String, String>> list) {
        List<Tree<String>> treeNodes = TreeUtil.build(list, "-1",
                (treeNode, tree) -> {
                    tree.setId(treeNode.get("id"));
                    tree.setParentId(treeNode.get("parentId"));
                    tree.setName(treeNode.get("name"));
                    // 扩展属性 ...
                    tree.putExtra("path", treeNode.get("path"));
                });
        return treeNodes;
    }

    /***
     * @Description: 判断路径下是否有文件，没有就新建一个，建后得记得删的
     * @param path
     * @param blank
     * @return: boolean
     **/
    public static boolean mkdirBlankFile(String path, String blank) {
        boolean result = false;
        File[] fs = new File(path).listFiles();
        if (ObjectUtil.isEmpty(fs)) {
            result = FileUtil.mkdirFile(path + blank);
        }
        return result;
    }

    /***
     * @Description: 删除文件（如果文件存在的话）
     * @param filePath
     * @return: boolean
     **/
    public static boolean delFile(String filePath) {
        boolean result = false;
        File file = new File(filePath);
        if (file.exists()) {
            result = file.delete();
        } else {
            result = true;
        }
        return result;
    }

    /***
     * @Description: 替换 \
     * @param filePath
     * @return: java.lang.String
     **/
    public static String replaceSlash(String filePath) {
        return filePath.replace("\\\\", "/").replace("\\", "/");
    }

    /**
     * 删除文件或者文件夹<br>
     * 路径如果为相对路径，会转换为ClassPath路径！ 注意：删除文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹<br>
     * 某个文件删除失败会终止删除操作
     *
     * @param fullFileOrDirPath 文件或者目录的路径
     * @return 成功与否
     * @throws IORuntimeException IO异常
     */
    public static boolean del(String fullFileOrDirPath) {
        boolean b = false;
        try {
            b = cn.hutool.core.io.FileUtil.del(fullFileOrDirPath);
        } catch (IORuntimeException e) {
            log.error(e.getMessage(), e);
        }
        return b;
    }

    /***
      * @Description: 判断文件路径是否存在，不存在创建目录
      * @param filePath
      * @return: void
      **/
    public static void mkdirPF(String filePath){
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    /**
     * 文件分片
     * @param file 要分片的文件
     * @param chunkFolder 分片目录
     * @param size 分片大小
     */
    public static void chunk(File file,String chunkFolder,long size) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos;
            FileChannel inputChannel = fis.getChannel();
            FileChannel outputChannel;
            if (!cn.hutool.core.io.FileUtil.exist(chunkFolder)) {
                cn.hutool.core.io.FileUtil.mkdir(chunkFolder);
            }
            // 分片总数
            int chunks = (int) Math.ceil(cn.hutool.core.io.FileUtil.size(file) / size);
            long startPoint = 0;
            for (int i = 0; i <= chunks; i++) {
                String chunkPath = chunkFolder + File.separator + i;
                File chunkFile = new File(chunkPath);
                boolean newFile = chunkFile.createNewFile();
                fos = new FileOutputStream(chunkPath);
                outputChannel = fos.getChannel();
                inputChannel.transferTo(startPoint,size,outputChannel);
                startPoint += size;
                outputChannel.close();
                fos.close();
            }
            inputChannel.close();
            fis.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 下载文件名重新编码
     *
     * @param response 响应对象
     * @param realFileName 真实文件名
     * @return
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) throws UnsupportedEncodingException
    {
        String percentEncodedFileName = percentEncode(realFileName);

        StringBuilder contentDispositionValue = new StringBuilder();
        contentDispositionValue.append("attachment; filename=")
                .append(percentEncodedFileName)
                .append(";")
                .append("filename*=")
                .append("utf-8''")
                .append(percentEncodedFileName);

//        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
        response.setHeader("Content-disposition", contentDispositionValue.toString());
        response.setHeader("download-filename", percentEncodedFileName);
    }

    /**
     * 百分号编码工具方法
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) throws UnsupportedEncodingException
    {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        return encode.replaceAll("\\+", "%20");
    }

    /**
     * 根据文件后缀名获取文件(递归)
     * @param file 文件
     * @param suffix 后缀名（".xxx"）
     * @return
     */
    public static String searchFileBySuffix(File file,String suffix){
        String path = "";
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File f:files){
                if (f.isDirectory()){
                    path= FileUtil.searchFileBySuffix(f,suffix);
                }
                if(f.getName().endsWith(suffix)){
                    path = f.getPath();
                    break;
                }
            }
        }
        return path;
    }

    /**
     * 解压
     *
     * @param inputStream
     * @param descDir
     */
    public static void unzipWithStream(InputStream inputStream, String descDir) {
        if (!descDir.endsWith(File.separator)) {
            descDir = descDir + File.separator;
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream, Charset.forName("GBK"))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String zipEntryNameStr = zipEntry.getName();
                String outPath = (descDir + zipEntryNameStr).replace("\\", "/");
                File outFile = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                if (!outFile.exists()) {
                    outFile.mkdirs();
                }
                if (new File(outPath).isDirectory()) {
                    continue;
                }
                writeFile(outPath, zipInputStream);
                zipInputStream.closeEntry();
            }
            log.info("======解压成功=======");
        } catch (IOException e) {
            e.printStackTrace();
            log.info("压缩包处理异常，异常信息:{}" , e);
        }
    }

    //将流写到文件中
    public static void writeFile(String filePath, ZipInputStream zipInputStream) {
        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] bytes = new byte[4096];
            int len;
            while ((len = zipInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            log.info("解压文件时，写出到文件出错");
        }
    }
    
    /**
     * 当前时间毫秒
     */
    public static  Long currentTimeMillis() {
        try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        return System.currentTimeMillis();
    }
    
    /**
     * @param type
     * @Description: 文件分类
     * @return: java.lang.String
     **/
    public static String getFileClassification(String type) {
        String result = "other";
        if ("png".equalsIgnoreCase(type) || "jpg".equalsIgnoreCase(type) || "jpeg".equalsIgnoreCase(type) || "gif".equalsIgnoreCase(type) || "bmp".equalsIgnoreCase(type)) {
            result = "img";
        } else if ("pdf".equalsIgnoreCase(type)) {
            result = "pdf";
        } else if ("zip".equalsIgnoreCase(type)) {
            result = "zip";
        } else if ("rar".equalsIgnoreCase(type)) {
            result = "rar";
        } else if ("doc".equalsIgnoreCase(type) || "docx".equalsIgnoreCase(type)) {
            result = "doc";
        } else if ("xls".equalsIgnoreCase(type) || "xlsx".equalsIgnoreCase(type)) {
            result = "xls";
        } else if ("sql".equalsIgnoreCase(type)) {
            result = "sql";
        } else if ("json".equalsIgnoreCase(type)) {
            result = "json";
        }
        return result;
    }

}
