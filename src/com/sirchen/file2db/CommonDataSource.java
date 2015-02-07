package com.sirchen.file2db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * @author chen
 * @Description 
 * @Date 2015-2-7
 * @version V1.0
 */
public abstract class CommonDataSource implements IDataSource {
	
	private InputStream inputStream=null;
	public CommonDataSource(String fileName){
		try {
			this.inputStream=new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public CommonDataSource(InputStream is){
		this.inputStream=is;
	}
	
	@Override
	public Object getValue(int row, int column) {
		return null;
	}
	@Override
	public int getLines() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.sirchen.file2db.IDataSouce#getColums()
	 */
	@Override
	public int getColums() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.sirchen.file2db.IDataSouce#getNames()
	 */
	@Override
	public List<String> getNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return this.inputStream;
	}

	@Override
	public void reset() {

	}

}
