package com.sirchen.file2db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sun.org.mozilla.javascript.internal.NativeArray;


/**
 * 此类的作用是根据配置文件及数据源的信息生成可执行的sql语句。
 * @author Dream.Lee
 * @version 2010-12-01
 */
public class SQLMethod implements IDBMethod {

	private String name;
	
	private String content;
	
	private String whereCondition;
	
	private IWhereCondition where;
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name=name;
	}

	public List<String> getPara() {
		return null;
	}

	public void setPara(List<String> para) {
		
	}

	public String getExecuteString(List<String> para,List<Object> values,Map<String,CodeFinder> codes,String mode) {
		if(values.size()<para.size()) return null;
		if("insert".equals(mode)){
			return insertMethod(para, values, codes);
		}else{
			return updateMethod(para, values, codes);
		}
	}
	
	/**
	 * 用update时，必须第一行为条件
	 * @param para
	 * @param values
	 * @param codes
	 * @return
	 */
	private String updateMethod(List<String> para,List<Object> values,Map<String,CodeFinder> codes){
		String sql="update "+name+" set ";
		if(codes.size()==0){
			for(int i=1;i<para.size()-1;i++){
				if(values.get(i).getClass()==String.class){
					String v=(String)values.get(i);
					if(para.get(i).startsWith("*")){
						continue;
					}
					if(v.startsWith("!")){
						sql+=para.get(i)+"="+((String)values.get(i)).substring(1)+",";
					}else{
						sql+=para.get(i)+"='"+values.get(i)+"',";
					}
				}else{
					sql+=para.get(i)+"='"+values.get(i)+"',";
				}
			}
			if(values.get(values.size()-1).getClass()==String.class){
				String v=(String)values.get(values.size()-1);
				if(!para.get(values.size()-1).startsWith("*")){
					if(v.startsWith("!")){
						sql+=para.get(values.size()-1)+"="+((String)values.get(values.size()-1)).substring(1)+" ";
					}else{
						sql+=para.get(para.size()-1)+"='"+values.get(para.size()-1)+"' ";
					}
				}
			}else{
				sql+=para.get(para.size()-1)+"='"+values.get(para.size()-1)+"' ";
			}
			//where条件
			if(where!=null){
				sql+="where "+where.getWhereCondition(values);
			}else{
				if(values.get(0).getClass()==String.class){
					String v=(String)values.get(0);
					if(v.startsWith("!")){
						sql+="where "+para.get(0)+"="+((String)values.get(0)).substring(1)+"";
					}else{
						sql+="where "+para.get(0)+"='"+values.get(0)+"'";
					}
				}else{
					sql+="where "+para.get(0)+"='"+values.get(0)+"'";
				}
			}
		}else{
			for(int i=1;i<para.size()-1;i++){
				if(codes.get(para.get(i))==null){
					String value=(String)values.get(i);
					if(para.get(i).startsWith("*")){
						continue;
					}
					if(value.startsWith("!")){
						value=value.substring(1);
					}else{
						value="'"+value+"'";
					}
					sql+=para.get(i)+"="+value+",";
				}else{
					CodeFinder cf=codes.get(para.get(i));
					if(cf.getColumnTo().indexOf(":")!=-1){
						String column=cf.getColumnTo().split(":")[0];
						String defaultV=cf.getColumnTo().split(":")[1];
						String value=cf.getValue();
						if(value.startsWith("!")){
							value=value.substring(1);
						}else{
							value="'"+value+"'";
						}
						sql+=para.get(i)+"=(select "+column+" from "+cf.getTablename()+" where "+cf.getColumnFrom()+
						"='"+cf.getValue()+"' union select "+defaultV+" from "+cf.getTablename()+" where not exists (select "+column+" from "+cf.getTablename()+" where " +
								cf.getColumnFrom()+"="+value+")),";
					}else{
						String value=cf.getValue();
						if(value.startsWith("!")){
							value=value.substring(1);
						}else{
							value="'"+value+"'";
						}
						sql+=para.get(i)+"=(select "+cf.getColumnTo()+" from "+cf.getTablename()+" where "+cf.getColumnFrom()+
						"="+value+"),";
					}
				}
			}
			if(codes.get(para.get(para.size()-1))==null){
				if(!values.get(para.size()-1).toString().startsWith("*")){
					String value=(String)values.get(para.size()-1);
					if(value.startsWith("!")){
						value=value.substring(1);
					}else{
						value="'"+value+"'";
					}
					sql+=para.get(para.size()-1)+"="+value+" ";
				}
			}else{
				CodeFinder cf=codes.get(para.get(para.size()-1));
				if(cf.getColumnTo().indexOf(":")!=-1){
					String column=cf.getColumnTo().split(":")[0];
					String defaultV=cf.getColumnTo().split(":")[1];
					sql+=para.get(para.size()-1)+"=(select "+column+" from "+cf.getTablename()+" where "+cf.getColumnFrom()+
					"='"+cf.getValue()+"' union select "+defaultV+" from "+cf.getTablename()+" where not exists (select "+column+" from "+cf.getTablename()+" where " +
							cf.getColumnFrom()+"='"+cf.getValue()+"')) ";
				}else{
					sql+=para.get(para.size()-1)+"=(select "+cf.getColumnTo()+" from "+cf.getTablename()+" where "+cf.getColumnFrom()+
					"='"+cf.getValue()+"') ";
				}
			}
			if(where!=null){
				if(codes.get(para.get(0))==null){
					String value=(String)values.get(0);
					if(value.startsWith("!")){
						value=value.substring(1);
					}else{
						value="'"+value+"'";
					}
					sql+=","+para.get(0)+"="+value+" ";
				}else{
					CodeFinder cf=codes.get(para.get(0));
					if(cf.getColumnTo().indexOf(":")!=-1){
						String column=cf.getColumnTo().split(":")[0];
						String defaultV=cf.getColumnTo().split(":")[1];
						sql+=","+para.get(0)+"=(select "+column+" from "+cf.getTablename()+" where "+cf.getColumnFrom()+
						"='"+cf.getValue()+"' union select "+defaultV+" from "+cf.getTablename()+" where not exists (select "+column+" from "+cf.getTablename()+" where " +
								cf.getColumnFrom()+"='"+cf.getValue()+"')) ";
					}else{
						sql+=","+para.get(0)+"=(select "+cf.getColumnTo()+" from "+cf.getTablename()+" where "+cf.getColumnFrom()+
						"='"+cf.getValue()+"') ";
					}
				}
				sql+=" where "+where.getWhereCondition(values);
			}else{
				if(values.get(0).getClass()==String.class){
					String v=(String)values.get(0);
					if(v.startsWith("!")){
						sql+="where "+para.get(0)+"="+((String)values.get(0)).substring(1)+"";
					}else{
						sql+="where "+para.get(0)+"='"+values.get(0)+"'";
					}
				}else{
					sql+="where "+para.get(0)+"='"+values.get(0)+"'";
				}
			}
		}
		System.out.println(sql);
		return sql;
	}
	
	
	private String insertMethod(List<String> para,List<Object> values,Map<String,CodeFinder> codes){
		String sql="";
		sql+="insert into "+name+"(";
		for(int i=0;i<para.size()-1;i++){
			if("".equals(para.get(i))||para.get(i).length()==0||para.get(i).startsWith("*")){
				continue;
			}
			sql+=para.get(i)+",";
		}
		//如不用插入码表类信息
		if(codes.size()==0){
			sql+=para.get(para.size()-1)+") values(";
			for(int i=0;i<para.size()-1;i++){
				if("".equals(para.get(i))||para.get(i).length()==0||para.get(i).startsWith("*")){
					continue;
				}
				if(values.get(i).getClass()==double.class){
					sql+=""+((values.get(i)==null||"null".equals(values.get(i))?"":values.get(i)))+",";
				}else if(String.valueOf(values.get(i)).startsWith("!")){
					sql+=String.valueOf(values.get(i)).substring(1)+",";
				}else{
					sql+="'"+((values.get(i)==null||"null".equals(values.get(i))?"":values.get(i)))+"',";
				}
			}
			if(!"".equals(para.get(para.size()-1))&&!(para.get(para.size()-1).length()==0)&&!para.get(para.size()-1).startsWith("*")){
				if(values.get(para.size()-1).getClass()==double.class){
					sql+=""+((values.get(para.size()-1)==null||"null".equals(values.get(para.size()-1))?"":values.get(para.size()-1)))+")";
				}else if(String.valueOf(values.get(para.size()-1)).startsWith("!")){
					sql+=String.valueOf(values.get(para.size()-1)).substring(1)+")";
				}else{
					sql+="'"+((values.get(para.size()-1)==null||"null".equals(values.get(para.size()-1))?"":values.get(para.size()-1)))+"')";
				}
			}else{
				sql+=")";
			}
		//需要码表类信息
		}else{
			sql+=para.get(para.size()-1)+") select ";
			for(int i=0;i<para.size()-1;i++){
				if("".equals(para.get(i))||para.get(i).length()==0||para.get(i).startsWith("*")){
					continue;
				}
				if(codes.get(para.get(i))==null){
					if(values.get(i).getClass()==double.class){
						sql+=""+(values.get(i)==null||values.get(i).equals("null")?"":values.get(i))+",";
					}else if(String.valueOf(values.get(i)).startsWith("!")){
						sql+=String.valueOf(values.get(i)).substring(1)+",";
					}else{
						sql+="'"+(values.get(i)==null||values.get(i).equals("null")?"":values.get(i))+"',";
					}
				}else{
					CodeFinder c=codes.get(para.get(i));
					sql+=c.getTablename()+"."+(c.getColumnTo().split(":").length==0?c.getColumnTo():c.getColumnTo().split(":")[0])+",";
				}
			}
			//处理最后一个参数
			if(!"".equals(para.get(para.size()-1))&&!(para.get(para.size()-1).length()==0)&&!para.get(para.size()-1).startsWith("*")){
				if(codes.get(para.get(para.size()-1))==null){
					if(values.get(para.size()-1).getClass()==double.class){
						sql+=""+values.get(para.size()-1)+"";
					}else if(String.valueOf(values.get(para.size()-1)).startsWith("!")){
						sql+=String.valueOf(values.get(para.size()-1)).substring(1)+"";
					}else{
						sql+="'"+(values.get(para.size()-1)==null||values.get(para.size()-1).equals("null")?"":values.get(para.size()-1))+"'";
					}
				}else{
					CodeFinder c=codes.get(para.get(para.size()-1));
					sql+=c.getTablename()+"."+(c.getColumnTo().split(":").length==0?c.getColumnTo():c.getColumnTo().split(":")[0])+"";
				}
			}else{
				sql+="";
			}
			
			sql+=" from ";
			String where=" where ";
			Set<String> keys=codes.keySet();
			List<String> ks=new ArrayList<String>();
			for(String k:keys){
				ks.add(k);
			}
			for(int i=0;i<ks.size()-1;i++){
				CodeFinder cf=codes.get(ks.get(i));
				sql+=cf.getTablename()+",";
				//具体where条件
				if(values.get(i).getClass()==NativeArray.class){
					NativeArray na=(NativeArray)values.get(i);
					long length=na.getLength();
					if(length>0){
						where+=cf.getTablename()+"."+cf.getColumnFrom()+" in ";
						String tmpStr="(select '"+na.get(0, null)+"' dream from dual ";
						for(long wt=1;wt<length;wt+=1){
							tmpStr+=" union select '"+na.get((int)wt, null)+"' dream from dual ";
						}
						tmpStr+=") ";
						where +=tmpStr;
					}else{
						where+=cf.getTablename()+"."+cf.getColumnFrom()+"='"+(cf.getValue()==null||"null".equals(cf.getValue())?"":cf.getValue().trim())+"' and ";
					}
				}else{
					where+=cf.getTablename()+"."+cf.getColumnFrom()+"='"+(cf.getValue()==null||"null".equals(cf.getValue())?"":cf.getValue().trim())+"' and ";
				}
				
			}
			CodeFinder cf=codes.get(ks.get(ks.size()-1));
			sql+=cf.getTablename()+" ";
			//如果方法的返回值为js中的Array,则形成多条插入的语句
			if(values.get(para.size()-1).getClass()==NativeArray.class){
				NativeArray na=(NativeArray)values.get(para.size()-1);
				long length=na.getLength();
				if(length>0){
					where+=cf.getTablename()+"."+cf.getColumnFrom()+" in ";
					String tmpStr="(select '"+na.get(0, null)+"' dream from dual ";
					for(long wt=1;wt<length;wt+=1){
						tmpStr+=" union select '"+na.get((int)wt, null)+"' dream from dual ";
					}
					tmpStr+=") ";
					where +=tmpStr;
				}
				else{
					where+=cf.getTablename()+"."+cf.getColumnFrom()+"='"+(cf.getValue()==null||"null".equals(cf.getValue())?"":cf.getValue().trim())+"' ";
				}
			}else{
				where+=cf.getTablename()+"."+cf.getColumnFrom()+"='"+(cf.getValue()==null||"null".equals(cf.getValue())?"":cf.getValue().trim())+"'";
			}
			
			sql+=where;
			
			//处理union后面的语句
			Set<String> codekeys=codes.keySet();
			String sqltmp=sql.substring(sql.indexOf(")")+1);
			String union="";
			for(String key:codekeys){
				CodeFinder cff=codes.get(key);
				if(cff.getColumnTo().indexOf(":")!=-1){
					String uniontmp=sqltmp.replace(cff.getTablename()+"."+cff.getColumnTo().split(":")[0], cff.getColumnTo().split(":")[1]);
					String toR=cff.getTablename()+"."+cff.getColumnFrom()+"='"+(cff.getValue()==null||"null".equals(cff.getValue())?"":cff.getValue().trim())+"'";
//					String rV="'"+(cff.getValue()==null||"null".equals(cff.getValue())?"":cff.getValue().trim())+"'";
					String rV="";
					rV+=" NOT EXISTS (SELECT DISTINCT "+cff.getColumnFrom()+" FROM "+cff.getTablename()+" WHERE "+cff.getColumnFrom()+"='"+
					(cff.getValue()==null||"null".equals(cff.getValue())?"":cff.getValue().trim())+"')";
					uniontmp=uniontmp.replace(toR, rV);
					union+=" union "+uniontmp;
				}
			}
			sql+=union;
		}
		return sql;
	}

	public String getWhereCondition() {
		return whereCondition;
	}

	public void setWhereCondition(String whereCondition) {
		if(this.whereCondition!=null&&this.whereCondition.equals(whereCondition)){
			return;
		}
		this.whereCondition = whereCondition;
		try {
			Class<?> c=Class.forName(whereCondition);
			where=(IWhereCondition)c.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
