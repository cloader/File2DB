package com.sirchen.file2db;

import java.io.InputStream;
import java.util.List;

/**
 * 错误输入接口。
 * @author Dream.Lee
 *
 */
public interface IOutput {
	
	/**
	 * 文件的Mime类型
	 * @return
	 */
	public String getMimeType();
	
	/**
	 * 输入的流对象。
	 * @return
	 */
	public InputStream getInputStream();
	
	/**
	 * 向输出中添加错误的具体信息。
	 * @param data
	 */
	public void appendRecord(List<? extends Object> data);
	
	/**
	 * 初始化头部
	 * @param titles
	 */
	public void initTitle(List<String> titles);
	
	/**
	 * 不会重复初始化的方法，多次调用该方法不会重复初始化数据。
	 * 
	 * @param titles
	 */
	public void initOnce(List<String> titles);
	
	public void initOnce(String[] titles);
	
	public void initTimes(String[] titles,int times);
	
	public void initTimes(List<String> titles,int times);
	
	/**
	 * 导入过程中是否有错误
	 * @return
	 */
	public boolean hasError();
	
	public String getExtension();

}
