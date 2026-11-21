package com.santech.framework.utils.excel.poi;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.sax.Excel03SaxReader;
import cn.hutool.poi.excel.sax.Excel07SaxReader;
import cn.hutool.poi.excel.sax.handler.RowHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel工具类
 */
@Slf4j
public class ExcelOperateUtil {
    /**
     * 快速读取
     * @param path
     * @return
     */
    public static List<List<List<Object>>> readExcel(String path){
        try{
            List<List<List<Object>>> resultList=new ArrayList<>();
            ExcelReader reader =ExcelUtil.getReader(ResourceUtil.getStream(path));;
            reader.isXlsx();
            int count= reader.getSheetCount();
            List<List<Object>> readAll=null;
            for (int i=0;i<count;i++){
                readAll=new ArrayList<>();
                if (reader.isXlsx()){
                    Excel07SaxReader reader07 = new Excel07SaxReader(createRowHandler(readAll));
                    reader07.read(path, i);
                }else{
                    Excel03SaxReader reader03 = new Excel03SaxReader(createRowHandler(readAll));
                    reader03.read(path, i);
                }

                resultList.add(readAll);
            }
            return resultList;
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return null;
        }
    }
    private static RowHandler createRowHandler(List<List<Object>> resultList) {
        return new RowHandler() {
            @Override
            public void handle(int sheetIndex, long rowIndex, List<Object> rowlist) {
                resultList.add(rowlist);
                //Console.log("[{}] [{}] {}", sheetIndex, rowIndex, rowlist);
            }
        };
    }
}
