package com.santech.framework.utils.excel.easyexcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;

public class EasyExcelUtils {
	private static final  Logger logger = LoggerFactory.getLogger(EasyExcelUtils.class);
	
	 //写文件,类中所有字段均写出
	 public static <T> boolean writeExcelSingleSheetByAllFields(String absolutefileName, String sheetName,Class<T> dataClassName,List<T> datas){
		 try {
			 EasyExcel.write(absolutefileName,dataClassName).sheet(sheetName).doWrite(datas);
			 return true;
		} catch (Exception e) {
			logger.error("写入Excel数据出现异常，异常详情：",e);
			return false;
		}
	 }
	 //写文件,类中所有字段均写出,待定，该方法需要特定场景特定使用，不能作为公共方法
	 public static <T> boolean writeExcelMultiSheetByAllFields(String absolutefileName, List<String> sheetNames,Map<String,List<T>> datas){
		 try (ExcelWriter excelWriter = EasyExcel.write(absolutefileName).build()) {
			 for(int i=0;i<sheetNames.size();i++) {
				 //可以通过枚举来进行实现相关对应
				 /*String sheetName = sheetNames.get(i);
				// 每次都要创建writeSheet 这里注意必须指定sheetNo 而且sheetName必须不一样。
	             WriteSheet writeSheet = EasyExcel.writerSheet(i, sheetName).head(dataClassName).build();
	             // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
	             excelWriter.write(datas.get(sheetName), writeSheet);*/
			 }
			 return true;
		} catch (Exception e) {
			logger.error("写入Excel数据出现异常，异常详情：",e);
			return false;
		}
	 }
	 
	//写文件，类中除了部分字段外的字段写出
	 public static <T> boolean writeExcelSingleSheetExcludeFields(String absolutefileName, String sheetName,Class<T> dataClassName,Set<String> excludeColumnFiledNames,List<T> datas){
		 try {
			 EasyExcel.write(absolutefileName,dataClassName).excludeColumnFiledNames(excludeColumnFiledNames).sheet(sheetName).doWrite(datas);
			 return true;
		} catch (Exception e) {
			logger.error("写入Excel数据出现异常，异常详情：",e);
			return false;
		}
	 }
	//写文件，类中目标字段的写出
	 public static <T> boolean writeExcelSingleSheetIncludeFields(String absolutefileName, String sheetName,Class<T> dataClassName,Set<String> includeColumnFiledNames,List<T> datas){
		 try {
			 EasyExcel.write(absolutefileName,dataClassName).includeColumnFiledNames(includeColumnFiledNames).sheet(sheetName).doWrite(datas);
			 return true;
		} catch (Exception e) {
			logger.error("写入Excel数据出现异常，异常详情：",e);
			return false;
		}
	 }
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	//读文件，并全量获取第一个sheet的数据
	 public static <T> List<T> readExcelFileSingleSheet(String absolutefileName, Class<T> dataClassName){
		 try {
			 List<T> resultDatas = new ArrayList<T>();
			 EasyExcel.read(absolutefileName, dataClassName, new AnalysisEventListener<T>() {
					//解析一行运行一次此方法。
			        @Override
			        public void invoke(T data, AnalysisContext analysisContext) {
			        	resultDatas.add(data);
			        }

					//解析所有数据完成，运行此方法。
			        @Override
			        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
			        	logger.info("完成【{}】的第一个sheet的数据读取",absolutefileName);
			        }
			    }).sheet().doRead();
			 return resultDatas;
		} catch (Exception e) {
			logger.error("读取Excel数据出现异常，异常详情：",e);
			return new ArrayList<T>();
		}
	 }	 
	 
	 //读文件，并全量获取目标sheet名的sheet的数据
	 public static <T> List<T> readExcelFileBySheetName(String absolutefileName, Class<T> dataClassName,String sheetName){
		 try {
	         List<T> resultDatas = new ArrayList<T>();
			 EasyExcel.read(absolutefileName, dataClassName, new AnalysisEventListener<T>() {
					//解析一行运行一次此方法。
			        @Override
			        public void invoke(T data, AnalysisContext analysisContext) {
			        	resultDatas.add(data);
			        }

					//解析所有数据完成，运行此方法。
			        @Override
			        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
			        	logger.info("完成【{}】的sheet名为【{}】的数据读取",absolutefileName,sheetName);
			        }
			    }).sheet(sheetName).doRead();
			return resultDatas;
	     }catch (Exception e) {
			logger.error("读取Excel数据出现异常，异常详情：",e);
			return new ArrayList<T>();
		 }
	 }	 
	 
	 //读文件，并全量获取目标sheet号的sheet的数据
	 public static <T> List<T> readExcelFileBySheetNo(String absolutefileName, Class<T> dataClassName,int sheetNo){
		 try {
	         List<T> resultDatas = new ArrayList<T>();
			 EasyExcel.read(absolutefileName, dataClassName, new AnalysisEventListener<T>() {
					//解析一行运行一次此方法。
			        @Override
			        public void invoke(T data, AnalysisContext analysisContext) {
			        	resultDatas.add(data);
			        }
					//解析所有数据完成，运行此方法。
			        @Override
			        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
			        	logger.info("完成【{}】的第【{}】个sheet的数据读取",absolutefileName,sheetNo);
			        }
			    }).sheet(sheetNo).doRead();
			return resultDatas;
	     }catch (Exception e) {
			logger.error("读取Excel数据出现异常，异常详情：",e);
			return new ArrayList<T>();
		 }
	 }	 
	 
	//读文件，并全量获取第一个sheet的数据
	 public static <T> List<T> readExcelIOSingleSheet(MultipartFile file, Class<T> dataClassName){
		 try {
			 List<T> resultDatas = new ArrayList<T>();
			 EasyExcel.read(file.getInputStream(), dataClassName, new AnalysisEventListener<T>() {
					//解析一行运行一次此方法。
			        @Override
			        public void invoke(T data, AnalysisContext analysisContext) {
			        	resultDatas.add(data);
			        }
					//解析所有数据完成，运行此方法。
			        @Override
			        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
			        	logger.info("完成上传的名为【{}】的excel文件的第一个sheet的数据读取",file.getName());
			        }
			    }).sheet().doRead();
			 return resultDatas;
		} catch (Exception e) {
			logger.error("读取Excel数据出现异常，异常详情：",e);
			return new ArrayList<T>();
		}
	 }	 
	 
	 //读文件，并全量获取目标sheet名的sheet的数据
	 public static <T> List<T> readExcelIOBySheetName(MultipartFile file, Class<T> dataClassName,String sheetName){
		 try {
	         List<T> resultDatas = new ArrayList<T>();
			 EasyExcel.read(file.getInputStream(), dataClassName, new AnalysisEventListener<T>() {
					//解析一行运行一次此方法。
			        @Override
			        public void invoke(T data, AnalysisContext analysisContext) {
			        	resultDatas.add(data);
			        }

					//解析所有数据完成，运行此方法。
			        @Override
			        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
			        	logger.info("完成上传的名为【{}】的excel文件的的sheet名为【{}】的数据读取",file.getName(),sheetName);
			        }
			    }).sheet(sheetName).doRead();
			return resultDatas;
	     }catch (Exception e) {
			logger.error("读取Excel数据出现异常，异常详情：",e);
			return new ArrayList<T>();
		 }
	 }	 
	 
	 //读文件，并全量获取目标sheet号的sheet的数据
	 public static <T> List<T> readExcelIOBySheetNo(MultipartFile file, Class<T> dataClassName,int sheetNo){
		 try {
	         List<T> resultDatas = new ArrayList<T>();
			 EasyExcel.read(file.getInputStream(), dataClassName, new AnalysisEventListener<T>() {
					//解析一行运行一次此方法。
			        @Override
			        public void invoke(T data, AnalysisContext analysisContext) {
			        	resultDatas.add(data);
			        }
					//解析所有数据完成，运行此方法。
			        @Override
			        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
			        	logger.info("完成上传的名为【{}】的excel文件的的第【{}】个sheet的数据读取",file.getName(),sheetNo);
			        }
			    }).sheet(sheetNo).doRead();
			return resultDatas;
	     }catch (Exception e) {
			logger.error("读取Excel数据出现异常，异常详情：",e);
			return new ArrayList<T>();
		 }
	 }	
	 
	
	//读文件并进行保存，这个需要明确具体类，不能用泛型进行普试化
//	 public static <T> boolean readExcelAndSave(String absolutefileName, Class<T> dataClassName,DatabaseHandler database){
//		 try {
//			// 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
//	        EasyExcel.read(absolutefileName, dataClassName, new AnalysisEventListener<T>() {
//	            /**
//	             * 单次缓存的数据量
//	             */
//	            public static final int BATCH_COUNT = 100;
//	            /**
//	             *临时存储
//	             */
//	            private List<T> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
//
//	            @Override
//	            public void invoke(T data, AnalysisContext context) {
//	                cachedDataList.add(data);
//	                if (cachedDataList.size() >= BATCH_COUNT) {
//	                	saveData();
//	                    // 存储完成清理 list
//	                    cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
//	                }
//	            }
//	            @Override
//	            public void doAfterAllAnalysed(AnalysisContext context) {
//	                saveData();
//	            }
//	            /**
//	             * 加上存储数据库
//	             */
//	            private void saveData() {
//	                logger.info("{}条数据，开始存储数据库！", cachedDataList.size());
//	                // TOTO
//	                logger.info("存储数据库成功！");
//	            }
//	        }).sheet().doRead();
//			 return true;
//		} catch (Exception e) {
//			logger.error("读取Excel数据并存储保存出现异常，异常详情：",e);
//			return false;
//		}
//	 }
	 
	 public static void main(String[] args) {
		 String targetExcel = "D:\\home\\lyyzt_files\\test.xlsx";
		 List<EasyExcelDemoModel> list = ListUtils.newArrayList();
	        for (int i = 0; i < 10; i++) {
	        	EasyExcelDemoModel data = new EasyExcelDemoModel();
	            data.setString("字符串" + i);
	            data.setDate(new Date());
	            data.setDoubleData(0.56);
	            data.setChildString("子字符串" + i);
	            data.setChildDate(new Date());
	            list.add(data);
	        }
	        
	        boolean result = writeExcelSingleSheetByAllFields(targetExcel, "test",EasyExcelDemoModel.class,list);
	        System.out.println(result);
	}

}