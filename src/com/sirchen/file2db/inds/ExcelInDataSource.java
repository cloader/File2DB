package com.sirchen.file2db.inds;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.sirchen.file2db.CommonInDataSource;

/**
 * @author chen
 * @Description 
 * @Date 2015-2-7
 * @version V1.0
 */
public class ExcelInDataSource extends CommonInDataSource{
	private HSSFWorkbook wb;
	private int sheetIndex;
	private String sheetIndexs;
	private HSSFSheet sheet;
	private List<String> names;
	private ByteArrayInputStream bis;
	
	public ExcelInDataSource(String fileName) {
		super(fileName);
		this.sheetIndex=0;
		init();
	}
	public ExcelInDataSource(String fileName,int sheetIndex) {
		super(fileName);
		this.sheetIndex=sheetIndex;
		init();
	}
	public ExcelInDataSource(InputStream is) {
		super(is);
		this.sheetIndex=0;
		init();
	}
	
	public ExcelInDataSource(InputStream is,int sheetIndex) {
		super(is);
		this.sheetIndex=sheetIndex;
		init();
	}
	
	public void init(){
		try {
			if(this.wb==null){
				this.wb = new HSSFWorkbook(this.getInputStream());
			}
			if (this.sheetIndexs == null) {
				this.sheet = this.wb.getSheetAt(0);
			} else
				this.sheet = this.wb.getSheet(this.sheetIndexs);
			names = new ArrayList<String>();
			for (int i = 0; i < getColumns(); i++) {
				names.add(String.valueOf(getValue(0, i)));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public   Object getValue(int row, int column) {
		HSSFRow row1 = this.sheet.getRow(row);
		if (row1 == null)
			return "";
		HSSFCell cell = row1.getCell(column);
		if(cell==null){
			return "";
		}
		switch(cell.getCellType()){
		case HSSFCell.CELL_TYPE_STRING:
			return cell.getStringCellValue()==null?"":cell.getStringCellValue();
		case HSSFCell.CELL_TYPE_NUMERIC:
			//处理日期类型
			if(HSSFDateUtil.isCellDateFormatted(cell)){
				Date date=cell.getDateCellValue();
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
				return sdf.format(date);
			}
			if(cell.getNumericCellValue()==(long)cell.getNumericCellValue()){
				return (long)cell.getNumericCellValue();
			}
			return cell.getNumericCellValue();
		//处理表达式
		case HSSFCell.CELL_TYPE_FORMULA:
			String value=null;
			try{
				value = String.valueOf(cell.getNumericCellValue());
			}catch(Exception e){
				value=cell.getStringCellValue();
			}
			if(value.equals("NaN")){
			    value = cell.getRichStringCellValue().toString();  
			}  
			return value==null?"":value;
		}
		return "";
	}

	@Override
	public int getLines() {
		return this.sheet.getRow(0)==null?0:(this.sheet.getRow(0).getLastCellNum()+ 1);
	}

	@Override
	public int getColumns() {
		return this.sheet.getLastRowNum() + 1;
	}
	
	@Override
	public List<String> getNames() {
		return names;
	}
	
	public int getSheet() {
		return this.sheetIndex;
	}

	public void setSheet(int sheet1) {
		this.sheetIndex = sheet1;
		if (this.wb != null)
			this.sheet = this.wb.getSheetAt(sheet1);
		names = new ArrayList<String>();
		for (int i = 0; i < getColumns(); i++) {
			names.add(String.valueOf(getValue(0, i)));
		}
	}

	@Override
	public void reset() {
		
	}
}
