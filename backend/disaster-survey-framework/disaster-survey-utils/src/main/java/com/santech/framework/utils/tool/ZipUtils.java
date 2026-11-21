package com.santech.framework.utils.tool;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.io.FileUtil;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    static final int BUFFER = 8192;

    /**
     * 压缩
     *
     * @param srcPath 待压缩路径
     * @param dstPath 压缩输出路径
     * @throws IOException
     */
    public static void compress(String srcPath, String dstPath) throws IOException {
        File srcFile = new File(srcPath);
        File dstFile = new File(dstPath);
        if (!srcFile.exists()) {
            throw new FileNotFoundException(srcPath + "不存在！");
        }
        FileOutputStream out = null;
        ZipOutputStream zipOut = null;
        CheckedOutputStream cos = null;
        try {
            out = new FileOutputStream(dstFile);
            cos = new CheckedOutputStream(out, new CRC32());
            zipOut = new ZipOutputStream(cos);
            String baseDir = "";
            compress(srcFile, zipOut, baseDir);
        } finally {
            if (null != zipOut) {
                zipOut.close();
                out = null;
            }

            if (null != out) {
                out.close();
            }

            if (null != cos) {
                cos.close();
            }
        }
    }

    /**
     * 压缩
     *
     * @param file    待压缩File对象
     * @param zipOut  ZipOutputStream对象
     * @param baseDir 压缩基础路径
     * @throws IOException
     */
    private static void compress(File file, ZipOutputStream zipOut, String baseDir) throws IOException {
        if (file.isDirectory()) {
            compressDirectory(file, zipOut, baseDir);
        } else {
            compressFile(file, zipOut, baseDir);
        }
    }


    /**
     * 压缩目录
     *
     * @param dir     待压缩目录路径
     * @param zipOut  ZipOutputStream对象
     * @param baseDir 压缩基础路径
     * @throws IOException
     */
    private static void compressDirectory(File dir, ZipOutputStream zipOut, String baseDir) throws IOException {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            compress(files[i], zipOut, baseDir + dir.getName() + "/");
        }
    }


    /**
     * 压缩文件
     *
     * @param file    待压缩File对象
     * @param zipOut  ZipOutputStream对象
     * @param baseDir 压缩基础路径
     * @throws IOException
     */
    private static void compressFile(File file, ZipOutputStream zipOut, String baseDir) throws IOException {
        if (!file.exists()) {
            return;
        }
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            ZipEntry entry = new ZipEntry(baseDir + file.getName());
            zipOut.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                zipOut.write(data, 0, count);
            }

        } finally {
            if (null != bis) {
                bis.close();
            }
        }
    }

    /**
     * 使用给定密码解压指定的ZIP压缩文件到指定目录
     * <p>
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     * @param zip 指定的ZIP压缩文件
     * @param dest 解压目录
     * @param passwd ZIP文件的密码
     * @return 解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    public static File [] unzip(String zip, String dest, String passwd) throws ZipException {
        File zipFile = new File(zip);
        return unzip(zipFile, dest, passwd);
    }

    /**
     * 使用给定密码解压指定的ZIP压缩文件到当前目录
     * @param zip 指定的ZIP压缩文件
     * @param passwd ZIP文件的密码
     * @return  解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    public static File[] unzip(String zip, String passwd) throws ZipException {
        File zipFile = new File(zip);
        File parentDir = zipFile.getParentFile();
        String dest = parentDir.getAbsolutePath() + File.separator + FileUtil.getPrefix(zipFile);
        return unzip(zipFile, dest, passwd);
    }

    /**
     * 使用给定密码解压指定的ZIP压缩文件到指定目录
     * <p>
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     * @param zipFile 指定的ZIP压缩文件
     * @param dest 解压目录
     * @param passwd ZIP文件的密码
     * @return  解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    public static File[] unzip(File zipFile, String dest, String passwd) throws ZipException {
        ZipFile zFile = new ZipFile(zipFile);
        zFile.setCharset(Charset.forName("GBK"));
        if (!zFile.isValidZipFile()) {
            throw new ZipException("压缩文件不合法,可能被损坏.");
        }
        File destDir = new File(dest);
        if (destDir.isDirectory() && !destDir.exists()) {
            destDir.mkdir();
        }
        if (zFile.isEncrypted() && StrUtil.isNotEmpty(passwd)) {
            zFile.setPassword(passwd.toCharArray());
        }
        zFile.extractAll(dest);

        List<FileHeader> headerList = zFile.getFileHeaders();
        List<File> extractedFileList = new ArrayList<File>();
        for(FileHeader fileHeader : headerList) {
            if (!fileHeader.isDirectory()) {
                extractedFileList.add(new File(destDir,fileHeader.getFileName()));
            }
        }
        File [] extractedFiles = new File[extractedFileList.size()];
        extractedFileList.toArray(extractedFiles);
        return extractedFiles;
    }
}
