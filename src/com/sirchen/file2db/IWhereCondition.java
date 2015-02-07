package com.sirchen.file2db;

import java.util.List;

/**
 * 该接口用于返回update后的where条件。
 * @author Dream.Lee
 * @version 2011-4-29
 */
public interface IWhereCondition {
	
	/**
	 * 返回where后的条件，返回的String对象会直接加到where后面。
	 * @param values 配置文件中=后面的所有值的集合
	 * @return 具体where条件
	 */
	public String getWhereCondition(List<Object> values);

}
