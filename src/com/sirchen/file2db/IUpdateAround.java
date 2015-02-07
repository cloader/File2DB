package com.sirchen.file2db;

import java.sql.Connection;
import java.util.List;

/**
 * 该接口用于当执行insert时遇到冲突改执行update时调用的。<br/> 该接口的两个方法分别在执行update之前和之后调用。
 * 
 * @author Dream.Lee
 * @version 2011-4-29
 */
public interface IUpdateAround {

	/**
	 * 在执行update之前调用
	 * 
	 * @param conn
	 *            数据库连接对象
	 * @param para
	 *            配置文件中=后内容的集合
	 */
	public void before(Connection conn, List<Object> para);

	/**
	 * 在执行update之后调用
	 * 
	 * @param conn
	 *            数据库连接对象
	 * @param para
	 *            配置文件中=后内容的集合
	 */
	public void after(Connection conn, List<Object> para);

}
