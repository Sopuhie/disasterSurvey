package com.santech.module.biz.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class ShpFileUtil {
	private static final Logger logger = LoggerFactory.getLogger(ShpFileUtil.class);
	/**
     * MultipartFile 转 File
     * @param file
     * @throws Exception
     */
    public static File multipartFileToFile(MultipartFile file,String absoluteDirPath) throws Exception {

        File dir = new File(absoluteDirPath);
        if(!dir.exists()) {
        	dir.mkdirs();
        }
        InputStream ins = file.getInputStream();
        File toFile = new File(absoluteDirPath+File.separator+file.getOriginalFilename());
        inputStreamToFile(ins, toFile);
        ins.close();
        return toFile;
    }

    //将数据流写入目标文件
    private static void inputStreamToFile(InputStream ins, File file) {
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
        	logger.error(e.getMessage(), e);
        } finally {
            try {
                if (null != os) {
                    os.close();
                }
                if (null != ins) {
                    ins.close();
                }
            } catch (Exception e) {
            	logger.error(e.getMessage(), e);
            }

        }
    }
	 public static List<String> unzipAndReturnShpPath(String zipFilePath, String destDir) {
	        FileOutputStream fos = null;
	        InputStream is = null;
	        ZipFile zf = null;
	        List<String> lists = new ArrayList<>();
	        try {
	            zf = new ZipFile(new File(zipFilePath),Charset.forName("GBK"));
	            Enumeration en = zf.entries();
	            while (en.hasMoreElements()) {
	                ZipEntry zn = (ZipEntry) en.nextElement();
	                if (!zn.isDirectory()) {
	                    is = zf.getInputStream(zn);
	                    File f = new File(destDir + File.separator + zn.getName());
	                    if (!f.getParentFile().exists()) {
	                        f.getParentFile().mkdirs();
	                    }
	                    if (!f.exists()) {
	                        f.createNewFile();
	                    }
	                    String str  = f.getAbsolutePath();
	                    if(str.endsWith(".shp")){
	                        lists.add(str);
	                    }
	                    fos = new FileOutputStream(destDir + File.separator + zn.getName());
	                    int len = 0;
	                    byte bufer[] = new byte[BUFFERSIZE];
	                    while (-1 != (len = is.read(bufer))) {
	                        fos.write(bufer, 0, len);
	                    }
	                    fos.flush();
						is.close();
						fos.close();
	                }
	            }
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        } finally {
	            try {
	                if (null != is) {
	                    is.close();
	                }
	                if (null != fos) {
	                    fos.close();
	                }
	                if(zf != null) {
	                	zf.close();
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        return lists;
	    }
	
	 /**
     * 需要注意的是当删除某一目录时。
     * @param [folder]
     * @return void
     */
    public static void deleteFolder(File folder) throws Exception {
        if (!folder.exists()) {
            throw new Exception("文件不存在");
        }
        if (!folder.isDirectory()) {
            throw new Exception("非文件夹文件");
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    //递归直到目录下没有文件
                    deleteFolder(file);
                } else {
                    //删除
                    file.delete();
                }
            }
        }
        //删除
        folder.delete();
    }
	
    private static int BUFFERSIZE = 2 << 10;
    
    /**
     * @Title getZIP
	 * @Description (将目标目录或文件进行压缩成zip,并写入zip流信息中）
     * @param rootFile (根目录文件对象或目标文件对象)
     * @param zos (zip输出流)
     * @param name (当前路径或文件名称)
     * @param buf (一次写入的瓶子容器)
     * @param KeepDirStructure  (是否保留原来的目录结构,true:保留目录结构; 
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败))
     * @return (方法的执行情况，true写入成功，false写入失败)
     */
    public static boolean getZIP(File rootFile,ZipOutputStream zos, String name,boolean keepDirStructure,byte[] buf) {
		try {
			if (rootFile.isFile()) {//是文件
				InputStream in = new FileInputStream(rootFile);
				zos.putNextEntry(new ZipEntry(name));
				int len;
				while ((len = in.read(buf)) != -1) {
					zos.write(buf, 0, len);
				}
				zos.closeEntry();
				in.close();
			} else {
				File[] files= rootFile.listFiles();
				if (files != null && files.length > 0) {
					for (int i = 0; i < files.length; i++) {
						File entry = files[i];
						String childName = entry.getName();
						if (keepDirStructure) {
							childName = name + File.separator + entry.getName();
						}
						getZIP(entry, zos, childName, keepDirStructure,buf);
					}
				} else {
					if (keepDirStructure) {
						// 需要保留原来的文件结构时,需要对空文件夹进行处理
						// 空文件夹的处理
						zos.putNextEntry(new ZipEntry(name + File.separator));
						zos.closeEntry();
					}
				}
			} 
			return true;
		} catch (Exception e) {
			logger.error("文件压缩异常，详情：",e);
			return false;
		}
	}

}
