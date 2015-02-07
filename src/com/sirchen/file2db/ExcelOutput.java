package com.sirchen.file2db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;


/**
 * 以Excel输出未导入成功的记录。
 * @author Dream.Lee
 * @version 2011-3-23
 */
public class ExcelOutput implements IOutput{
	
	private HSSFWorkbook wb;
	private HSSFSheet sheet;
	private boolean isInit=false;
	private boolean error=false;
	private int initTimes=0;
	
	public ExcelOutput(){
		this("错误信息");
	}
	
	public ExcelOutput(String sheetname){
		init(sheetname);
	}

	/**
	 * 初始化方法。
	 * @param sheetname
	 */
	private void init(String sheetname) {
		wb=new HSSFWorkbook();
		sheet=wb.createSheet(sheetname);
	}

	@Override
	public synchronized InputStream getInputStream() {
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		if(wb!=null){
			try {
				wb.write(bos);
				ByteArrayInputStream bis=new ByteArrayInputStream(bos.toByteArray());
				return bis;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getMimeType() {
		return "application/msexcel";
	}

	/**
	 * 追加一行记录
	 */
	@Override
	public synchronized void appendRecord(List<? extends Object> data) {
		int current=sheet.getLastRowNum()+1;
		HSSFRow row=sheet.createRow(current);
		for(int i=0;i<data.size();i++){
			HSSFCell cell=row.createCell(i);
			cell.setCellValue((String)data.get(i));
		}
		error=true;
	}

	/**
	 * 初始化表头
	 */
	@Override
	public synchronized void initTitle(List<String> titles) {
		if(sheet!=null){
			HSSFRow row=sheet.createRow(0);
			for(int i=0;i<titles.size();i++){
				CellStyle style=wb.createCellStyle();
				Font font=wb.createFont();
				font.setFontName("宋体");
				font.setFontHeightInPoints((short)10);
				font.setColor(HSSFColor.RED.index);
				style.setFont(font);
				style.setFillForegroundColor(HSSFColor.RED.index);
				HSSFCell cell=row.createCell(i);
				cell.setCellStyle(style);
				cell.setCellValue(titles.get(i));
			}
			initTimes++;
		}
	}

	@Override
	public synchronized void initOnce(List<String> titles) {
		if(!isInit){
			initTitle(titles);
			isInit=true;
		}
	}

	@Override
	public boolean hasError() {
		return error;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initOnce(String[] titles) {
		initOnce(Arrays.asList(titles));
	}

	@Override
	public String getExtension() {
		return "xls";
	}

	@Override
	public void initTimes(String[] titles, int times) {
		this.initTimes(Arrays.asList(titles), times);
	}

	@Override
	public void initTimes(List<String> titles, int times) {
		if(times>initTimes){
			for(String t:titles){
				System.out.print(t);
			}
			int current=sheet.getLastRowNum()+1;
			HSSFRow row=sheet.createRow(current);
			for(int i=0;i<titles.size();i++){
				CellStyle style=wb.createCellStyle();
				Font font=wb.createFont();
				font.setFontName("宋体");
				font.setFontHeightInPoints((short)10);
				font.setColor(HSSFColor.RED.index);
				style.setFont(font);
				style.setFillForegroundColor(HSSFColor.RED.index);
				HSSFCell cell=row.createCell(i);
				cell.setCellStyle(style);
				cell.setCellValue(titles.get(i));
			}
			initTimes++;
		}
	}

}
