package com.sirchen.file2db;

import java.util.List;
import java.util.Map;

public class MetaMethod implements IDBMethod{
	
	private String sql;

	@Override
	public String getContent() {
		return sql;
	}

	@Override
	public String getExecuteString(List<String> para, List<Object> values,
			Map<String, CodeFinder> codes, String mode) {
		return getContent();
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public List<String> getPara() {
		return null;
	}

	@Override
	public void setContent(String content) {
		this.sql=content.trim();
	}

	@Override
	public void setName(String name) {
		
	}

	@Override
	public void setPara(List<String> para) {
		
	}

	@Override
	public void setWhereCondition(String whereCondition) {
		// TODO Auto-generated method stub
		
	}

}
