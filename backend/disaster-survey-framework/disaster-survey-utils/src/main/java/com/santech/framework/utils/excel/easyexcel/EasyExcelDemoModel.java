package com.santech.framework.utils.excel.easyexcel;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

import lombok.Data;

/**
 * @Description: EasyExcel测试类
 **/

@Data
public class EasyExcelDemoModel implements Serializable{
	
	private static final long serialVersionUID = -7687785299843311087L;

	@ExcelProperty("字符串标题")
    private String string;
	
	@DateTimeFormat("yyyy年MM月dd日HH时mm分ss秒")
    @ExcelProperty(value="日期标题", converter = DateConverter.class)
    private Date date;
	
	@NumberFormat("#.##%")
    @ExcelProperty("数字标题")
    private Double doubleData;
    /**
     * 忽略这个字段
     */
    @ExcelIgnore
    private String ignore;
    
    @ColumnWidth(50)
    @ExcelProperty({"主标题", "子字符串标题"})
    private String childString;
    
    @ColumnWidth(50)
    @DateTimeFormat("yyyy年MM月dd日HH时mm分ss秒")
    @ExcelProperty(value={"主标题", "子日期标题"}, converter = DateConverter.class)
    private Date childDate;
}
