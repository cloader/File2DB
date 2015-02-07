package com.sirchen.file2db;

import java.sql.Connection;

/**
 * 
 * @author Dream.Lee
 *
 */
public interface IComplementCall {
	
	/**
	 * 在配置了complement后,当执行完一步操作后将调用的方法。
	 * @param conn
	 */
	public void afterImport(Connection conn);

}
