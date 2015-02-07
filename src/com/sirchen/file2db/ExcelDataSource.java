package com.sirchen.file2db;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * @author chen
 * @Description 
 * @Date 2015-2-7
 * @version V1.0
 */
public class ExcelDataSource extends CommonDataSource{
	private HSSFWorkbook wb;
	private int sheetIndex;
	private String sheetIndexs;
	private HSSFSheet sheet;
	private List<String> names;
	private ByteArrayInputStream bis;
	/**
	 * @param is
	 */
	public ExcelDataSource(String fileName) {
		super(fileName);
	}
	
	public ExcelDataSource(InputStream is) {
		super(is);
	}
	
	public void init(){
		
	}
}
