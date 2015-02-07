package com.sirchen.file2db;

import java.io.InputStream;
import java.util.List;


/**
 * @author chen
 * @Description 
 * @Date 2015-2-7
 * @version V1.0
 */
public interface IDataSource {
	public Object getValue(int row,int column);
	public int getLines();
	public int getColums();
	public List<String> getNames();
	public InputStream getInputStream();
	public void reset();

}
