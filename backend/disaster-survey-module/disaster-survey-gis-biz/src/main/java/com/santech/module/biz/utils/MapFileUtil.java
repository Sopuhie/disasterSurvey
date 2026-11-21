package com.santech.module.biz.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapFileUtil {

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

    
    /**
     * 需要注意的是当删除某一目录时，必须保证该目录下没有其他文件才能正确删除，否则将删除失败。
     * @param [folder]
     * @return void
     */
    public static void deleteFolder(File folder) throws Exception {
        if (folder.isDirectory()&&folder.exists()) {
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
     * 解压zip文件
     *
     * @param zipFile 目标文件
     * @param descDir 解压后存放的位置
     * @return true/false
     */
    public static boolean unZip(File zipFile, String descDir) {
        boolean flag = false;
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        ZipFile zip = null;
        OutputStream out = null;
        try {
            // 指定编码，否则压缩包里面不能有中文目录
            zip = new ZipFile(zipFile, Charset.forName("GBK"));
            for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                InputStream in = zip.getInputStream(entry);
                if(descDir.contains(zipEntryName)){
                    continue;
                }
                String outPath = (descDir+zipEntryName).replace("/", File.separator);
                // 判断路径是否存在,不存在则创建文件路径
                File file = new File(outPath.substring(0,
                        outPath.lastIndexOf(File.separator)));
                if (!file.exists()) {
                    file.mkdirs();
                }
                // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                if (new File(outPath).isDirectory()) {
                    continue;
                }

                out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[2048];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
                in.close();
                out.close();
            }
            flag = true;
            // 必须关闭，否则无法删除该zip文件
            zip.close();
            if (zipFile.exists()){
                zipFile.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
        	try {
                if (null != out) {
                	out.close();
                }
                if (null != zip) {
                	zip.close();
                }
                if (zipFile.exists()){
                    zipFile.delete();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return flag;
    }

    private static int BUFFERSIZE = 2 << 10;

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
     * @param zipPathDir  压缩包路径 ，如 /home/data/zip-folder/
     * @param zipFileName 压缩包名称 ，如 测试文件.zip
     * @param fileList    要压缩的文件列表（绝对路径），如 /home/person/test/测试.doc，/home/person/haha/测试.doc
     * @return
     */
    public static void compressFiles(String zipPathDir, String zipFileName, List<String> fileList) throws Exception{

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File(zipPathDir + zipFileName)))) {
            File zipFile = new File(zipPathDir);
            if (!zipFile.exists()) {
                zipFile.mkdirs();
            }
            for (String filePath : fileList) {
                File file = new File(filePath);

                if (file.exists()) {
                    int index = file.getName().lastIndexOf('.');
                    ZipEntry zipEntry = new ZipEntry(file.getName().substring(0, index) + file.getName().substring(index));
                    zos.putNextEntry(zipEntry);
                    byte[] buffer = new byte[2048];
                    compressSingleFile(file, zos, buffer);
                }
            }
            zos.flush();
        } catch (Exception e) {
        	throw new RuntimeException("压缩所有文件成zip包出错" + e);
        }
    }

    //压缩单个文件
    private static void compressSingleFile(File file, ZipOutputStream zos, byte[] buffer) throws Exception{
        int len;
        try (FileInputStream fis = new FileInputStream(file)) {
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
                zos.flush();
            }
            zos.closeEntry();
        } catch (IOException e) {
        	throw new RuntimeException("====压缩单个文件异常====" + e);
        }
    }
    
    
    /**
     * @param fileList目标文件容器
     * @param path目标文件夹绝对路径
     * @param suffixs 目标文件的后缀名,例如:(txt,dwg)
     * @throws Exception
     */
    public static List<File> getTargetFiles(List<File> fileList,String targetDirPath, List<String> suffixs) {
        try {
            File targetDir = new File(targetDirPath);
            if(!targetDir.exists()) {
            	return fileList;
            }
            File[] allFiles = targetDir.listFiles();
            if(allFiles.length>0) {
            	 for (int i = 0; i < allFiles.length; i++) {
                     File file = allFiles[i];
                     if (file.isFile()) {
                         if (suffixs.contains(FileUtil.getSuffix(file.getName()).toLowerCase())) {
                             fileList.add(file);
                         }
                     } else {
                    	 fileList = getTargetFiles(fileList, file.getAbsolutePath(), suffixs);
                     }
                 }
            }
           
        } catch (Exception e) {
        	log.error("获取目标文件夹的目标文件出现异常，详情：",e);
        }
        return fileList;
    }
    
    /**
     * 写入txt，如果文件存在，则在文件内追加
     * @param path 需要写入txt的路径
     * @param contents 需要写入的内容
     * @param appendflag 继续追加标识,true 继续追加，false 清空原内容重新写入
     * @param encode 数据写入编码格式，如GBK、UTF-8
     */
    public  static void writeInfoTotxtByLine(String path, JSONArray contents,boolean appendflag,String encode) {
        FileOutputStream  fileOut = null;
        OutputStreamWriter writer = null;
        BufferedWriter bw = null;
        try {
            //将写入转化为流的形式
        	File file = new File(path);
        	//路径目录不存在时创建目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        	if(file.exists()&&!appendflag){
        		//如果文件存在，且不再该文件追加，则先删除文件
                 file.delete();
            }
            fileOut = new FileOutputStream(file, true);
            writer = new OutputStreamWriter(fileOut,encode);
            bw = new BufferedWriter(writer);
            //一次写一行
            for (int i =0;i<contents.size();i++) {
                bw.write(contents.getString(i));
                bw.newLine();  //换行用
            }
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
            	contents.clear();//数据写完便释放
                if (bw != null) {
                    bw.close();
                }
                if (writer != null) {
                	writer.close();
                }
                if (fileOut != null) {
                	fileOut.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 写入txt，如果文件存在，则在文件内追加
     * @param path 需要写入txt的路径
     * @param list 需要写入的字符串的list
     * @param appendflag 继续追加标识,true 继续追加，false 清空原内容重新写入
     * @param encode 数据写入编码格式，如GBK、UTF-8
     */
    public  static void writeInfoTotxtByLine(String path, List<String> list,boolean appendflag,String encode) {
        FileOutputStream  fileOut = null;
        OutputStreamWriter writer = null;
        BufferedWriter bw = null;
        try {
            //将写入转化为流的形式
        	File file = new File(path);
        	//路径目录不存在时创建目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        	if(file.exists()&&!appendflag){
        		//如果文件存在，且不再该文件追加，则先删除文件
                 file.delete();
            }
            fileOut = new FileOutputStream(file, true);
            writer = new OutputStreamWriter(fileOut,encode);
            bw = new BufferedWriter(writer);
            //一次写一行
            for (String s : list) {
                bw.write(s);
                bw.newLine();  //换行用
            }
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
            	list = null;//数据写完便释放
                if (bw != null) {
                    bw.close();
                }
                if (writer != null) {
                	writer.close();
                }
                if (fileOut != null) {
                	fileOut.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
  //对象需为文件
    public static List<String> parseBaseInfoTXTContent(File txtFile,String encode){
	    List<String> result = new ArrayList<String>();
		InputStream input = null;
		InputStreamReader read = null;
		BufferedReader bufferedReader = null;
	    try {
	    	input = new FileInputStream(txtFile);
	        if(input!=null) {
	        	 read = new InputStreamReader(input,encode);
	             bufferedReader = new BufferedReader(read);
	             String lineTxt = null;
	             while((lineTxt = bufferedReader.readLine()) != null){
	            	 result.add(lineTxt.trim().replaceAll("\uFEFF",""));
	             }
	        }
	    } catch (Exception e) {
	    	result.clear();//如果出现异常，则清空已存的数据
	    } finally {
	    	try {
	    		if(bufferedReader!=null) {
		    		bufferedReader.close();
		    	}
	    		if(read!=null) {
		    		read.close();
		    	}
		    	if(input!=null) {
		    		input.close();
		    	}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    return result;
    }
    
    /**
     * 删除缓存文件
     * @param paths 绝对路径集合
     * @return
     */
    public static void deleteCatcheFileMx(List<String> paths) {
        try {
        	if(paths!=null&&paths.size()>0) {
        		for(String path:paths) {
        			File file = new File(path);
        			if(file.exists()){
        				 file.delete();
                    }
            	}
        	}
        }catch (Exception e){
        	e.printStackTrace();
        }
    }
    
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
			log.error("文件压缩异常，详情：",e);
			return false;
		}
	}

}
