package com.sirchen.file2db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import com.sirchen.file2db.inds.IInDataSource;

/**
 * @author chen
 * @Description 
 * @Date 2015-2-7
 * @version V1.0
 */
public abstract class CommonInDataSource implements IInDataSource {
	private InputStream inputStream=null;
	public CommonInDataSource(String fileName){
		try {
			this.inputStream=new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public CommonInDataSource(InputStream is){
		this.inputStream=is;
	}
	
	public abstract Object getValue(int row, int column);
	
	public abstract int getLines();

	public abstract int getColumns();

	public abstract List<String> getNames();

	public InputStream getInputStream() {
		return this.inputStream;
	}

	public abstract void reset();

}
