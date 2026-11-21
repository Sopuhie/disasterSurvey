package com.santech.framework.utils.tool;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.math.BigInteger;
import java.util.List;

/**
 * Word工具lei
 */
public class WordUtil {

    //默认字体大小
    private static final int DEFAULT_FONTSIZE = 12;
    //默认字体
    private static final String DEFAULT_FONTFAMILY = "宋体";

    /**
     * 表格初始化
     * 列宽自动分割
     * @param comTable
     */
    public static void tableInit(XWPFTable comTable ){
        CTTblWidth comTableWidth = comTable.getCTTbl().addNewTblPr().addNewTblW();
        comTableWidth.setType(STTblWidth.DXA);
        comTableWidth.setW(BigInteger.valueOf(9072));
        CTTblPr tblPr = getTableCTTblPr(comTable);
        CTJc cTJc = tblPr.addNewJc();
        cTJc.setVal(STJc.CENTER);
    }

    /**
     * 新加n列空白格
     * @param xwpfTableRow
     * @param n
     */
    public static void addNewTableCell(XWPFTableRow xwpfTableRow,int n){
        for(int i=0;i<n;i++){
            xwpfTableRow.addNewTableCell();
        }
    }

    /**
     * 跨列合并
     * @param table
     * @param row    所合并的行
     * @param fromCell    起始列
     * @param toCell    终止列
     * @Description
     */
    public static void mergeCellsHorizontal(XWPFTable table, int row, int fromCell, int toCell) {
        for(int cellIndex = fromCell; cellIndex <= toCell; cellIndex++ ) {
            XWPFTableCell cell = table.getRow(row).getCell(cellIndex);
            if(cellIndex == fromCell) {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }
    /**
     * 跨行合并
     * @param table
     * @param col    合并的列
     * @param fromRow    起始行
     * @param toRow    终止行
     * @Description
     */
    public static void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for(int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            //第一个合并单元格用重启合并值设置
            if(rowIndex == fromRow) {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                //合并第一个单元格的单元被设置为“继续”
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /**
     * 设置表格行高
     * @param infoTable
     * @param heigth 高度
     * @param vertical 表格内容的显示方式：居中、靠右...
     */
    public static void setTableHeight(XWPFTable infoTable, int heigth, STVerticalJc.Enum vertical) {
        List<XWPFTableRow> rows = infoTable.getRows();
        for(XWPFTableRow row : rows) {
            CTTrPr trPr = row.getCtRow().addNewTrPr();
            CTHeight ht = trPr.addNewTrHeight();
            ht.setVal(BigInteger.valueOf(heigth));
            List<XWPFTableCell> cells = row.getTableCells();
            for(XWPFTableCell tableCell : cells ) {
                CTTcPr cttcpr = tableCell.getCTTc().addNewTcPr();
                cttcpr.addNewVAlign().setVal(vertical);
            }
        }
    }

    /**
     *空白模块带序号
     * 1
     * 2
     * @param comTable  表格
     * @param currentRowNum  当前行数
     * @param row  行数
     * @param fromCell  开始cell
     * @param toCell  结束cell
     */
    public static int blankModule(XWPFTable comTable, int currentRowNum,int row, int fromCell, int toCell ){
        XWPFTableRow xwpfTableRow;
        for(int i=1;i<=row;i++){
            xwpfTableRow = comTable.createRow();
            currentRowNum ++;
            WordUtil.mergeCellsHorizontal(comTable, currentRowNum, fromCell, toCell);
            cellText(xwpfTableRow.getCell(0), String.valueOf(i));
            cellText(xwpfTableRow.getCell(1),"");
            cellText(xwpfTableRow.getCell(2),"");
        }
        return currentRowNum;
    }

    /**
     *空白模块为一行
     * @param comTable  表格
     * @param currentRowNum  当前行数
     * @param row  行数
     * @param fromCell  开始cell
     * @param toCell  结束cell
     */
    public static int blankModuleRow(XWPFTable comTable, int currentRowNum,int row, int fromCell, int toCell  ){
        for(int i=1;i<=row;i++){
            comTable.createRow();
            currentRowNum ++;
            WordUtil.mergeCellsHorizontal(comTable, currentRowNum, fromCell, toCell);
        }
        WordUtil.mergeCellsVertically(comTable, 0, currentRowNum-row+1, currentRowNum);
        return currentRowNum;
    }

    /**
     * @Description: 得到Table的CTTblPr,不存在则新建
     */
    public static CTTblPr getTableCTTblPr(XWPFTable table) {
        CTTbl ttbl = table.getCTTbl();
        // 表格属性
        CTTblPr tblPr = ttbl.getTblPr() == null ? ttbl.addNewTblPr() : ttbl.getTblPr();
        return tblPr;
    }

    /**
     * 填充cell
     * @param xwpfTableCell
     * @param text
     */
    public static void cellText(XWPFTableCell xwpfTableCell, String text){
        XWPFParagraph cellParagraph = xwpfTableCell.getParagraphArray(0);
        cellParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun cellParagraphRun = cellParagraph.createRun();
        cellParagraphRun.setFontSize(DEFAULT_FONTSIZE);
        cellParagraphRun.setFontFamily(DEFAULT_FONTFAMILY);
        cellParagraphRun.setText(text);
    }

    /**
     * 填充cell
     * @param xwpfTableCell
     * @param text
     * @param underlinePatterns
     */
    public static void cellText(XWPFTableCell xwpfTableCell, String text,UnderlinePatterns underlinePatterns){
        XWPFParagraph cellParagraph = xwpfTableCell.getParagraphArray(0);
        cellParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun cellParagraphRun = cellParagraph.createRun();
        cellParagraphRun.setFontSize(DEFAULT_FONTSIZE);
        cellParagraphRun.setFontFamily(DEFAULT_FONTFAMILY);
        cellParagraphRun.setText(text);
        cellParagraphRun.setUnderline(underlinePatterns);
    }

    /**
     * 填充cell
     * @param xwpfTableCell
     * @param text
     * @param paragraphAlignment
     */
    public static void cellText(XWPFTableCell xwpfTableCell, String text, ParagraphAlignment paragraphAlignment){
        XWPFParagraph cellParagraph = xwpfTableCell.getParagraphArray(0);
        cellParagraph.setAlignment(paragraphAlignment);
        XWPFRun cellParagraphRun = cellParagraph.createRun();
        cellParagraphRun.setFontSize(DEFAULT_FONTSIZE);
        cellParagraphRun.setFontFamily(DEFAULT_FONTFAMILY);
        cellParagraphRun.setText(text);
    }

    /**
     * 填充cell
     * @param xwpfTableCell
     * @param text
     * @param paragraphAlignment
     * @param rgbStr   颜色值，如  FFFFFF
     */
    public static void cellText(XWPFTableCell xwpfTableCell, String text, ParagraphAlignment paragraphAlignment, String rgbStr){
        XWPFParagraph cellParagraph = xwpfTableCell.getParagraphArray(0);
        cellParagraph.setAlignment(paragraphAlignment);
        XWPFRun cellParagraphRun = cellParagraph.createRun();
        cellParagraphRun.setFontSize(DEFAULT_FONTSIZE);
        cellParagraphRun.setFontFamily(DEFAULT_FONTFAMILY);
        cellParagraphRun.setText(text);
        cellParagraphRun.setColor(rgbStr);
    }


}
