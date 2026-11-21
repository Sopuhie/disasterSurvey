package com.santech.framework.utils.excel.poi;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

public abstract class AbstractExcelExportTemplate<T> implements ExcelExportTemplate<T> {
    /**
     * 默认表格宽度
     */
    private static final int DEFAULT_COLUMN_WIDTH = 7000;
    /**
     * excel文件对象
     */
    protected Workbook workbook;
    /**
     * excel sheet列表
     */
    protected List<Sheet> sheets = new ArrayList<Sheet>();
    /**
     * 标题栏
     */
    protected String[][] titles;
    protected CellStyle captionRowSytle;
    /**
     * 默认标题行样式
     */
    protected CellStyle titleRowStyle;
    /**
     * 默认内容行样式
     */
    protected CellStyle bodyRowStyle;
    /**
     * 各个sheet是否包含抬头,key:sheet坐标，value:包含true,否则false
     */
    protected Map<Integer, Boolean> hasCaptionMap = new HashMap<Integer, Boolean>();
    /**
     * 默认单元格宽度
     */
    protected int columnWidth = DEFAULT_COLUMN_WIDTH;
    /**
     * 导出的数据
     */
    protected List<T> data;
    
    /**
     * 当目标sheet有标题行，但无标题栏时，设置标题行的合并单元格的范围
     */
    protected int lastColumn = 0;

    @Override
    public void export(OutputStream outputStream, List<T> data) throws Exception {
        Validate.notNull(outputStream);
        this.buildWorkbook(data).write(outputStream);
    }

    public Workbook buildWorkbook(List<T> data) throws Exception {
        /*if (this.workbook != null)
            return this.workbook;*/
        String[] sheetNames = this.getSheetNames();
        this.data = data;
        this.workbook = new HSSFWorkbook();
        this.titles = this.getTitles();
        this.captionRowSytle = createCaptionCellStyle();
        this.titleRowStyle = createTitleCellStyle();
        this.bodyRowStyle = createBodyCellStyle();
        this.afterCreateWorkBook();

        for (int i = 0; i < sheetNames.length; i++) {
            Sheet sheet = workbook.createSheet(sheetNames[i]);
            this.sheets.add(sheet);
            afterBuildSheet(i);
            buildCaption(i,this.lastColumn);
            buildTitle(i);
            afterBuildTitle(i);
            buildBody(i);
            afterBuildBody(i);
        }

        return this.workbook;
    }

    /**
     * 创建单元格
     * 
     * @param row
     * @param index
     * @param cellValue
     * @param cellStyle
     */
    protected void createStyledCell(Row row, int index, String cellValue, CellStyle cellStyle) {
        Cell cell = row.createCell(index);
        cell.setCellValue(cellValue);
        cell.setCellStyle(cellStyle);
    }

    /**
     * 在创建完毕HSSFWorkBook对象和样式对象后作的处理操作，通常用来对默认的样式进行重新定义
     */
    protected void afterCreateWorkBook() {
    }

    /**
     * 获取excel抬头样式
     * 
     * @return
     */
    protected CellStyle createCaptionCellStyle() {
        Font font = workbook.createFont();
        font.setColor(Font.COLOR_NORMAL);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(false);
        font.setFontHeight((short) 200);
        cellStyle.setFont(font);
        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        return cellStyle;
    }

    /**
     * 获取excel表头样式
     * 
     * @return
     */
    protected CellStyle createTitleCellStyle() {
        Font font = workbook.createFont();
        font.setColor(Font.COLOR_NORMAL);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(false);
        font.setFontHeight((short) 200);
        cellStyle.setFont(font);
        cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        BorderStyle border = BorderStyle.THIN;
        setCellBorder(cellStyle, border, border, border, border);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        return cellStyle;
    }

    /**
     * 设置单元格的border
     * 
     * @param cellStyle
     * @param top
     * @param bottom
     * @param left
     * @param right
     */
    protected void setCellBorder(CellStyle cellStyle, BorderStyle top, BorderStyle bottom, BorderStyle left, BorderStyle right) {
        cellStyle.setBorderBottom(bottom);
        cellStyle.setBorderLeft(left);
        cellStyle.setBorderRight(right);
        cellStyle.setBorderTop(top);
    }

    /**
     * 获取excel内容样式
     * 
     * @return
     */
    protected CellStyle createBodyCellStyle() {
        Font font = workbook.createFont();
        // font.setColor(HSSFColor.BLUE_GREY.index);
        font.setFontHeight((short) 200);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(false);
        cellStyle.setFont(font);
        cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        BorderStyle border = BorderStyle.THIN;
        setCellBorder(cellStyle, border, border, border, border);
        return cellStyle;
    }

    /**
     * 获取第n个excel sheet
     * 
     * @param sheetIndex
     * @return
     */
    protected Sheet getSheet(int sheetIndex) {
        return this.sheets.get(sheetIndex);
    }

    /**
     * 创建sheet完毕后做的操作
     * 
     * @param sheetIndex
     */
    protected void afterBuildSheet(int sheetIndex) {
    }

    /**
     * 在sheet的第一行插入标题
     * 
     * @param sheetIndex
     */
    protected void buildCaption(int sheetIndex,int lastColumn) {
        Sheet sheet = getSheet(sheetIndex);
        String[] captions = this.getCaptions();
        hasCaptionMap.put(sheetIndex, false);
        if (captions != null && captions.length >= sheetIndex + 1) {
            String caption = captions[sheetIndex];
            if (StringUtils.isNotBlank(caption)) {
                Row row = sheet.createRow(0);
                if(lastColumn<1) {
                	 //不传指定合并的宽度则取标题栏的最大长度-1
                	 lastColumn = calculateLastColumn(sheetIndex);
                }
                if(lastColumn > 0) {
                    CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, lastColumn);
                    sheet.addMergedRegion(cellRangeAddress);
                }
                createStyledCell(row, 0, caption, this.captionRowSytle);
                hasCaptionMap.put(sheetIndex, true);
            }
        }
    }

    /**
     * 计算最后一列数据的栏位，用来设置标题合并单元格的宽度
     * 
     * @param sheetIndex
     * @return
     */
    protected int calculateLastColumn(int sheetIndex) {
        if (this.titles != null && sheetIndex <= this.titles.length - 1 && this.titles[sheetIndex] != null) {
            return this.titles[sheetIndex].length - 1;
        } else {
            return 1;
        }
    }

    /**
     * 创建sheet中数据的标题
     * 
     * @param sheetIndex
     */
    protected void buildTitle(int sheetIndex) {
        if (this.titles.length < sheetIndex + 1) {
            return;
        }
        String[] ts = this.titles[sheetIndex];
        if (ts == null) {
            return;
        }
        Sheet sheet = this.getSheet(sheetIndex);
        int titleStartIndex = this.getTitleStartIndex(sheetIndex);
        Row rowTitle = sheet.createRow(titleStartIndex);
        for (int i = 0; i < ts.length; i++) {
            sheet.setColumnWidth(i, columnWidth);
            createStyledCell(rowTitle, i, ts[i], this.titleRowStyle);
        }
    }

    /**
     * 获取各个sheet内容部分起始行index,默认为从第一行开始
     * 
     * @param sheetIndex
     *            sheet的index
     * @return
     */
    protected int getBodyStartIndex(int sheetIndex) {
        int captionRow = getTitleStartIndex(sheetIndex);
        ;
        int titleRow = 0;
        if (this.titles != null && this.titles.length >= sheetIndex + 1) {
            if (titles[sheetIndex] != null && titles[sheetIndex].length > 0) {
                titleRow = 1;
            }
        }
        return captionRow + titleRow;
    }

    protected int getTitleStartIndex(int sheetIndex) {
        return this.hasCaptionMap.get(sheetIndex) ? 1 : 0;
    }

    /**
     * 创建sheet中数据的标题之后做的操作
     * 
     * @param sheetIndex
     */
    protected void afterBuildTitle(int sheetIndex) {
    }

    /**
     * 创建sheet中数据的内容
     * 
     * @param sheetIndex
     */
    protected abstract void buildBody(int sheetIndex);

    /**
     * 创建sheet中数据的内容之后做的操作
     * 
     * @param sheetIndex
     */
    protected void afterBuildBody(int sheetIndex) {
    }

    @Override
    public String[] getCaptions() {
        return new String[] {};
    }

    @Override
    public int getRowAccessWindowSize() {
        return 200;
    }
}
