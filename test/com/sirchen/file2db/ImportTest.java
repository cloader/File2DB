package com.sirchen.file2db;

import java.io.IOException;

import org.dom4j.DocumentException;

import com.sirchen.file2db.inds.ExcelInDataSource;

/**
 * @author chen
 * @Description 
 * @Date 2015-2-7
 * @version V1.0
 */
public class ImportTest {
	public static void main(String[] args) {
		ExcelInDataSource ds=new ExcelInDataSource(ImportTest.class.getResource("datasource.xls").getFile());
		DBManager db = new DBManager();
		db.setErrorPath("e:\\error.xls");
		db.setDs(ds);
		try {
			db.insertIntoDabase(DBManager.class.getResourceAsStream("dsfp.import.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (URLFormatException e) {
			e.printStackTrace();
		}
	}
}
