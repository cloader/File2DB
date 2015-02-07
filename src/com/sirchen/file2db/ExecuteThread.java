package com.sirchen.file2db;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import com.sirchen.file2db.inds.IInDataSource;

/**
 * 多线程执行导入时用到的线程类。
 * @author Dream.Lee
 *
 */
public class ExecuteThread extends Thread {
	private CountDownLatch finish;
	
	private int startIndex;
	private int finishIndex;
	
	private JScript js;
	private CommonInDataSource ds;
	private Step step;
	private XmlConfig para;
	private DBManager db;
	private Statement pst;
	private int size;
	private boolean run=true;
	private Lock lock;
	
	public Lock getLock() {
		return lock;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public JScript getJs() {
		return js;
	}

	public void setJs(JScript js) {
		this.js = js;
	}

	public IInDataSource getDs() {
		return ds;
	}

	public void setDs(CommonInDataSource ds) {
		this.ds = ds;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getFinishIndex() {
		return finishIndex;
	}

	public void setFinishIndex(int finishIndex) {
		this.finishIndex = finishIndex;
	}

	public ExecuteThread(CountDownLatch finish,CommonInDataSource ds) {
		this.finish = finish;
		this.ds=ds;
	}
	
	@Override
	public void run() {		
		try {
			for (int i = startIndex; i < finishIndex&&run; i++) {
				synchronized(db){
					float p=db.getPrecent();
					p+=1.0/size;
					db.setPrecent(p);
					System.out.println(db.getPrecent());
				}
				if(lock!=null&&!lock.isRun()) break;
				List<String> para1 = new ArrayList<String>();
				List<Object> values = new ArrayList<Object>();
				Map<String, CodeFinder> code = new HashMap<String, CodeFinder>();
				parseContent(para, step.getMethods(), para1, values, code, i);
				try {
					System.out.println("sql:"
							+ step.getMethods().getExecuteString(para1, values,
									code, step.getMode()));
					System.out.println("当前正执行：\t" + i + "行");
					int size = pst.executeUpdate(step.getMethods()
							.getExecuteString(para1, values, code, step.getMode()));
					synchronized(db){
						if(step.isCount()){
							db.setSuccessCount(db.getSuccessCount()+1);
						}
					}
					if (step.getOnReturn() != null
							&& step.getOnReturn().length() != 0) {
						js.invoke(step.getOnReturn(), new Object[] { size,
										values });
					}
					if (size == 0) {
						// 处理onNoData事件
						if (step.getOnNoData() != null
								&& step.getOnNoData().length() != 0) {
							js.invoke(step.getOnNoData(), new Object[] { values });
						}
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					try {
						// 处理onException事件
						// 将异常对象和数据作为参数传递给回事件方法
						if (step.getOnException() != null) {
							js.invoke(step.getOnException(), new Object[] { e1,
									values });
						}
					} catch (ScriptException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
					synchronized(db){
						if(step.isCount()){
							db.setErrorCount(db.getErrorCount()+1);
						}
					}
					continue;
				} catch (ScriptException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} 
			}
		} finally{
			finish.countDown();
		}
	}

	public XmlConfig getPara() {
		return para;
	}

	public void setPara(XmlConfig para) {
		this.para = para;
	}

	public DBManager getDb() {
		return db;
	}
	
	public void setDb(DBManager db) {
		this.db = db;
	}
	
	public Statement getPst() {
		return pst;
	}
	
	public void setPst(Statement pst) {
		this.pst = pst;
	}
	
	private  Object getValue(int row, int column) {
		return ds.getValue(row, column);
	}
	
	void parseContent(XmlConfig p, IDBMethod method, List<String> para,
			List<Object> values, Map<String, CodeFinder> codes, int stepIndex) {
		String content = method.getContent().replaceAll("stepIndex",
				"" + stepIndex);
		// 消除所有换行符
		content = content.replaceAll("\\s+", "");
		if(content.trim().indexOf("#")!=-1){
			String ss[] = content.trim().split("#");
			for (String s : ss) {
				String[] sa = s.split("=");
				if (sa[0].indexOf("&") != -1) {
					String[] tmp = sa[0].split("&");
					String column = getValueFromExpress(tmp[0], p) == null ? ""
							: getValueFromExpress(tmp[0], p).toString();
					para.add(column);
					String[] code = tmp[1].trim().substring(1, tmp[1].length() - 1)
							.split(",");
					if (code.length != 3)
						throw new RuntimeException("参数个数不正确!应该为三个参数，分别为(表名,插入列名,比较列名)");
					CodeFinder c = new CodeFinder();
					c.setTablename(getValueFromExpress(code[0], p) == null ? ""
							: getValueFromExpress(code[0], p).toString());
					c.setColumnTo(code[1]);
					c.setColumnFrom(getValueFromExpress(code[2], p) == null ? ""
							: getValueFromExpress(code[2], p).toString());
					c.setValue(getValueFromExpress(sa[1], p) == null ? ""
							: getValueFromExpress(sa[1], p).toString());
					codes.put(column, c);
				} else {
					para.add(getValueFromExpress(sa[0], p) == null ? ""
							: getValueFromExpress(sa[0], p).toString());
				}
				values.add(getValueFromExpress(sa[1], p) == null ? ""
						: getValueFromExpress(sa[1], p));
			}
		}
		
	}
	/**
	 * 解析表达式，并返回表达式的值。
	 * @param express
	 * @param p
	 * @return
	 */
	public Object getValueFromExpress(String express, XmlConfig p) {
		Pattern pmethod = Pattern.compile("@(\\w+)\\((([^;]+)*(;([^;]+))*)\\)");
		Pattern pexcel = Pattern.compile("\\$C\\{(.+),([^,\\}]+)\\}");
		Matcher m1 = pmethod.matcher(express);
		Matcher m2 = pexcel.matcher(express);
		if (m1.matches()) {
			String method = m1.group(1);
			String args = m1.group(2);
			String[] arg = args.split(";");
			Object[] real = new Object[arg.length];
			for (int i = 0; i < arg.length; i++) {
				real[i] = getValueFromExpress(arg[i], p);
			}
			try {
				return js.invoke(method, Object.class, real);
			} catch (ScriptException e) {
				e.printStackTrace();
				return "";
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				return "";
			}
		} else if (m2.matches()) {
			return String.valueOf(getValue(
					(int) Double
							.parseDouble(getValueFromExpress(m2.group(1), p)
									.toString()), (int) Double
							.parseDouble(getValueFromExpress(m2.group(2), p)
									.toString())));
		} else {
			try {
				if(express.matches("\\d+")){
					return express;
				}
				return JScript.eval(express);
			} catch (ScriptException e) {
				return express;
			}
		}
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setRun(boolean run) {
		this.run = run;
	}
	
}
