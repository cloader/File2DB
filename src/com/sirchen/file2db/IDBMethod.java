package com.sirchen.file2db;

import java.util.List;
import java.util.Map;
public interface IDBMethod {
	public void setName(String name);

	public String getName();

	public List<String> getPara();

	public void setPara(List<String> para);

	public String getContent();

	public void setContent(String content);

	public String getExecuteString(List<String> para, List<Object> values,
			Map<String, CodeFinder> codes,String mode);
	
	public void setWhereCondition(String whereCondition);
}
