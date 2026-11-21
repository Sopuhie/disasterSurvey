package com.santech.framework.utils.excel.poi;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
	
	private final static String excel2003L =".xls";    //2003- 版本的excel  
    private final static String excel2007U =".xlsx";   //2007+ 版本的excel 
    
    /**
     * 获取io流中的数据，组成List<List<Object>> list 对象
     * @param in  
     * @param fileName
     * @return
     * @throws Exception
     */
    public static List<List<Object>> getBankListByExcel(InputStream in,String fileName)throws Exception{
    	List<List<Object>> list = null;
    	
    	Workbook work = getWorkbook(in,fileName);
    	if(work == null){
    		throw new Exception("创建Excel工作簿为空！");
    	}
    	Sheet sheet = null; //页数
    	Row row = null;	//行数
    	Cell cell = null;	//列数
    	
    	list = new ArrayList<List<Object>>();
    	// 遍历Excel 中所有的sheet
    	for(int i=0;i<work.getNumberOfSheets();i++){
    		sheet = work.getSheetAt(i);
    		if(sheet == null){
    			continue;
    		}
    		// 遍历当前sheet中的所有行
    		for(int j=sheet.getFirstRowNum();j<=sheet.getLastRowNum();j++){
    			row = sheet.getRow(j);
    			if(row == null ){//|| row.getFirstCellNum() == j(如果不解析第一行则加此代码)
    				continue;
    			}
    			//遍历所有的列
    			List<Object> li = new ArrayList<Object>();
    			for(int y=row.getFirstCellNum();y<row.getLastCellNum();y++){
    				cell = row.getCell(y);
    				li.add(getValue(cell));
    			}
    			list.add(li);
    		}
    	}
    	return list;
    }

	
    /**
     * 根据文件后缀适应上传的文件版本
     * @param inStr
     * @param fileName
     * @return
     * @throws Exception
     */
	private static Workbook getWorkbook(InputStream inStr, String fileName) throws Exception{
		 Workbook wb = null;  
	     String fileType = fileName.substring(fileName.lastIndexOf("."));  
	     if(excel2003L.equals(fileType)){  
	    	 wb = new HSSFWorkbook(inStr);  //2003-  
	     }else if(excel2007U.equals(fileType)){  
	         wb = new XSSFWorkbook(inStr);  //2007+  
	     }else{  
	         throw new Exception("解析的文件格式有误！");  
	     }  
	     return wb;  
	}
	
	/**
	 * 对表中的数值进行格式化
	 * @param cell
	 * @return
	 */
	
	// 解决Excel类型问题，获得数值
	private static String getValue(Cell cell) {
		String value = "";
		if(cell == null){
			return value;
		}
		switch (cell.getCellType()) {
		//数值型
		case NUMERIC:
			if(HSSFDateUtil.isCellDateFormatted(cell)){
				// 如果是date类型，则获取cell的date值
				Date date = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				value = sdf.format(date);
			}else{
				//纯数字
				BigDecimal big = new BigDecimal(cell.getNumericCellValue());
				value = big.toString();
				// 解决1234.0  去掉后面的0
				if(null!=value&&!"".equals(value.trim())){
					String[] item = value.split("[.]");
					if(1<item.length&&"0".equals(item[1])){
						value = item[0];
					}
				}
			}
			break;
		//	字符串类型
		case STRING:
			value = cell.getStringCellValue().toString();
			break;
		//	公式类型
		case FORMULA:
			value = String.valueOf(cell.getNumericCellValue());
			if(value.equals("NaN")){// 如果获取的数值为非法值，则转换为获取字符型
				value = cell.getStringCellValue().toString();
			}
			break;
		//	布尔型	
		case BOOLEAN:
			value = " "+cell.getBooleanCellValue();
			break;
		default:
			value = cell.getStringCellValue().toString();
			break;
		}
		if("null".endsWith(value.trim())){
			value = "";
		}
		return value;
	}

}