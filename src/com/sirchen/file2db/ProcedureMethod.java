package com.sirchen.file2db;

import java.util.List;
import java.util.Map;

public class ProcedureMethod implements IDBMethod {
	
	private String name;
	private String content;
	private List<String> para;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<String> getPara() {
		return para;
	}

	public void setPara(List<String> para) {
		this.para=para;
	}

	public String getExecuteString(List<String> para,List<Object> values,Map<String,CodeFinder> codes,String mode) {
		String ret="{call "+name+"(";
		if(para.size()>1){
			for(int i=0;i<para.size()-1;i++){
				ret+="?,";
			}
			ret+="?";
		}else if(para.size()==1){
			ret+="?";
		}
		ret+=")";
		return ret;
	}

	@Override
	public void setWhereCondition(String whereCondition) {
		// TODO Auto-generated method stub
		
	}

}
