package com.sirchen.file2db;

public class URLFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public URLFormatException(String exc){
		super("数据库连接字符串格式错误,"+exc+"!");
	}
}
