package com.sirchen.file2db;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.sirchen.file2db.inds.ExcelInDataSource;
import com.sirchen.file2db.inds.IInDataSource;

import sun.org.mozilla.javascript.internal.NativeArray;


/**
 * 此类的作用是根据xml的配置信息将各种规则的数据信息导入到数据库中。
 * 
 * @author Dream.Lee
 * 
 */
public class DBManager {


	private CommonInDataSource ds;
	private JScript js;
	private Controller controller = new Controller();
	private float precent;
	private int totalsize;
	private List<Thread> threadpool;
	private Map<String, String> context = new HashMap<String, String>();
	private static Map<String, String> outMap = new HashMap<String, String>();
	private Connection conn = null;
	private Lock lock;
	private Map<String,Object> bindO=new HashMap<String, Object>();
	private String errorPath;
	
	
	/**
	 * 
	 */
	private List<IMiddleWare> middleWares = new ArrayList<IMiddleWare>();
	private int successCount = 0;
	private IOutput os;

	public Lock getLock() {
		return lock;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public synchronized int getSuccessCount() {
		return successCount;
	}

	public synchronized void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	public synchronized int getErrorCount() {
		return errorCount;
	}

	public synchronized void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	private int errorCount = 0;

	private XmlConfig para;

	
	public void bindObject(String key,Object value){
		bindO.put(key, value);
	}
	
	/**
	 * 根据配置文件导入，导入之前必须设置数据集。
	 * 
	 * @param xls
	 * @param config
	 * @throws IOException
	 * @throws DocumentException
	 * @throws URLFormatException
	 */
	public void insertIntoDabase(String config) throws IOException,
			DocumentException, URLFormatException {
		if (ds == null) {
			throw new RuntimeException("数据源未设置");
		}
		insertIntoDabase(new FileInputStream(config));
	}

	/**
	 * 
	 * @param xls
	 * @param config
	 * @throws IOException
	 * @throws DocumentException
	 * @throws URLFormatException
	 */
	public void insertIntoDabase(String xls, String config) throws IOException,
			DocumentException, URLFormatException {
		IInDataSource ds = new ExcelInDataSource(xls);
		setDs(ds);
		insertIntoDabase(new FileInputStream(config));
	}

	/**
	 * 将xls的内容根据xml上的配置导入到数据库。
	 * 
	 * @param xls
	 * @param config
	 * @throws IOException
	 * @throws DocumentException
	 * @throws URLFormatException
	 */
	public void insertIntoDabase(InputStream xls, InputStream config)
			throws IOException, DocumentException, URLFormatException {
		CommonInDataSource ds = new ExcelInDataSource((FileInputStream) xls);
		setDs(ds);
		insertIntoDabase(config);
	}

	/**
	 * 根据xml的配置导入数据源中的数据。
	 * 
	 * @param config
	 *            配置文件的流
	 * @throws IOException
	 * @throws DocumentException
	 * @throws URLFormatException
	 */
	public void insertIntoDabase(InputStream config) throws IOException,
			DocumentException, URLFormatException {
		para = XmlConfigParser.getInstance().parseConfig(config);
		Parser par = new DBManager().new Parser();
		try {
			conn = connectToDatabase(par.parseDiver(para.getConnurl()));
			conn.setAutoCommit(false);
			String[] cret = check(para);
			if (cret.length == 0) {
				execute(para, conn);
				if (lock != null && !lock.isRun()) {
					conn.rollback();
				} else {
					conn.commit();
				}
			} else {
				CheckException ce = new CheckException();
				ce.setErrorHeads(cret);
				for (String he : ce.getErrorHeads()) {
					System.out.println(he);
				}
				throw ce;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			config.close();
		}
	}

	/**
	 * 回滚数据。
	 */
	public void rollback() {
		if (conn != null) {
			try {
				conn.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public XmlConfig getParameter(InputStream config) {
		try {
			return para = XmlConfigParser.getInstance().parseConfig(config);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 执行导入
	 */
	public void execute() {
		Parser par = new DBManager().new Parser();
		Connection conn = null;
		try {
			conn = connectToDatabase(par.parseDiver(para.getConnurl()));
			conn.setAutoCommit(false);
			String[] cret = check(para);
			if (cret.length == 0) {
				execute(para, conn);
			} else {
				CheckException ce = new CheckException();
				ce.setErrorHeads(cret);
				for (String he : ce.getErrorHeads()) {
					System.out.println(he);
				}
				throw ce;
			}
			if (lock != null && !lock.isRun()) {
				System.out.println("rollback");
				conn.rollback();
			} else {
				conn.commit();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URLFormatException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 检查表头ͷ
	 * 
	 * @param para
	 * @return
	 */
	private String[] check(XmlConfig para) {
		List<String> heads = para.getCheckHeads();
		if (heads == null)
			return new String[] {};
		List<String> ret = new ArrayList<String>();
		for (String head : heads) {
			if (!ds.getNames().contains(head)) {
				ret.add(head);
			}
		}
		return ret.toArray(new String[ret.size()]);
	}

	/**
	 * 导入真正执行的方法。
	 * 
	 * @param workbook
	 * @param para
	 * @param conn
	 * @throws SQLException
	 */
	private void execute(XmlConfig para, Connection conn) throws SQLException {
		js = new JScript(para.getScript().trim());
		// 绑定配置文件中用到的对象。
		js.bindObject("ds", ds);
		js.bindObject("conn", conn);
		for(Entry<String, Object> entry:bindO.entrySet()){
			js.bindObject(entry.getKey(), entry.getValue());
		}
		// 解析错误输出信息
		os = parseOutput(para);
		// 分步进行导入
		for (Step step : para.getSteps()) {
			int end = 0;
			ds.setSheet(step.getSheet());
			if ("last".equals(step.getFinishIndex())) {
				end = ds.getLines()-step.getStartIndex();
			} else {
				end = Integer.parseInt(step.getFinishIndex())-step.getStartIndex();
			}
			totalsize += end;
		}
		for (Step step : para.getSteps()) {
			if (!Step.POLICY_IGORE.equals(step.getCoverPolicy())
					&& step.getCoverPolicy() != null) {
				this.executeWithBlock(step, para, conn);
			} else {
				this.executeNoBlock(step, para, conn);
			}
			ds.reset();
			// 导入完成后是否进行相关操作
			if (step.getComplementClass() != null) {
				try {
					if (step.getComplementClass().startsWith(".")) {
						StackTraceElement stack[] = (new Throwable())
								.getStackTrace();
						StackTraceElement ste = stack[2];
						String cls = ste.getClassName();
						cls = cls.substring(0, cls.lastIndexOf("."))
								+ step.getComplementClass();
						step.setComplementClass(cls);
					}
					IComplementCall call = (IComplementCall) Class.forName(
							step.getComplementClass()).newInstance();
					call.afterImport(conn);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		for (IMiddleWare ware : middleWares) {
			ware.output(os);
		}
		
		if(errorPath!=null){
			InputStream is=os.getInputStream();
			FileOutputStream fos=null;
			try {
				fos=new FileOutputStream(errorPath);
				byte[] buffer=new byte[1024];
				int len=0;
				while((len=is.read(buffer))!=-1){
					fos.write(buffer, 0, len);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(fos!=null){
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private IOutput parseOutput(XmlConfig para) {
		IOutput os = null;
		try {
			SAXReader reader = new SAXReader();
			Document doc = reader.read(JScript.class
					.getResourceAsStream("config.xml"));
			for (Iterator iter = doc.getRootElement().elementIterator("output"); iter
					.hasNext();) {
				Element e = (Element) iter.next();
				outMap.put(e.attributeValue("key"), e.attributeValue("class"));
			}
			String cname = outMap.get(para.getOutkey()==null?"excel":para.getOutkey());
			Class<?> c = Class.forName(cname);
			os = (IOutput) c.newInstance();
			js.bindObject("output", os);
			js.bindObject("context", context);
		} catch (DocumentException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return os;
	}

	/**
	 * 阻塞导入
	 * 
	 * @param step
	 * @param para
	 * @param conn
	 * @throws SQLException
	 */
	private void executeWithBlock(Step step, XmlConfig para, Connection conn)
			throws SQLException {
		Statement pst = conn.createStatement();
		int start = step.getStartIndex();
		int end = 0;
		if ("last".equals(step.getFinishIndex())) {
			end = ds.getLines();
		} else {
			end = Integer.parseInt(step.getFinishIndex());
		}
		outer: for (int i = start; (!(step.getFinishCondition() == null ? false
				: Boolean.parseBoolean(String.valueOf(getValueFromExpress(step
						.getFinishCondition().replaceAll("stepIndex", "" + i),
						para)))) && i < end); i++) {
			if (lock != null && !lock.isRun())
				break outer;
			precent += 1.0 / totalsize;
			List<String> para1 = new ArrayList<String>();
			List<Object> values = new ArrayList<Object>();
			ds.setSheet(step.getSheet());
			Map<String, CodeFinder> code = new HashMap<String, CodeFinder>();
			this.parseContent(para, step.getMethods(), para1, values, code, i);
			// 导入之前进行数据检查，检查通过则导入到数据库，否则记录到错误输出中。
			if (step.getCheckFunc() != null) {
				try {
					boolean checkRet = Boolean.parseBoolean(String.valueOf(js
							.invoke(step.getCheckFunc(), values)));
					if (!checkRet) {
						errorCount++;
						continue;
					}
				} catch (ScriptException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
			try {
				boolean block = false;
				String condition = "";
				if (!Step.POLICY_IGORE.equals(step.getCoverPolicy())
						&& step.getCoverPolicy() != null) {
					// 判断阻塞ͻ
					controller.setWorking(true);
					String sql = "select * from " + step.getMethods().getName()
							+ " where ";
					if (step.getCoverWhereCondition() != null) {
						Class<?> c = Class.forName(step
								.getCoverWhereCondition());
						IWhereCondition where = (IWhereCondition) c
								.newInstance();
						condition = where.getWhereCondition(values);
					} else if (step.getCoverCondition() != null) {
						String[] cs = step.getCoverCondition().split(",");
						for (int w = 0; w < cs.length - 1; w++) {
							for (int ii = 0; ii < para1.size() - 1; ii++) {
								if (para1.get(ii).equals(cs[w].trim())) {
									condition += cs[w] + "='" + values.get(ii)
											+ "' and ";
								}
							}
						}
						String wt = cs[cs.length - 1];
						for (int ii = 0; ii < para1.size() - 1; ii++) {
							if (para1.get(ii).equals(wt.trim())) {
								condition += wt + "='" + values.get(ii) + "'";
							}
						}
					}
					sql += condition;
					System.out.println(sql);
					ResultSet rs = pst.executeQuery(sql);
					if (rs.next()) {
						block = true;
					}
					rs.close();
				}
				IUpdateAround around = null;
				if (step.getUpdateAroundClass() != null) {
					Class<?> c = Class.forName(step.getUpdateAroundClass());
					around = (IUpdateAround) c.newInstance();
				}

				if (Step.POLICY_COVERALL.equals(step.getCoverPolicy())) {
					controller.setSkipType(Controller.COVERALL);
				}

				if (block) {
					switch (controller.getSkipType()) {
					case Controller.NORMAL:
						// 
						if (condition != null) {
							Pattern p = Pattern.compile("=(\\S+)");
							Matcher m = p.matcher(condition);
							StringBuilder sb = new StringBuilder();
							while (m.find()) {
								System.out.println(m.group(1));
								sb.append(m.group(1)).append(",");
							}
							controller.updateAll(sb.toString());
						} else {
							controller.updateAll(values.get(0).toString());
						}
						synchronized (controller.getLock()) {
							// 阻塞线程
							controller.getLock().wait();
						}
						block = false;
						if (!controller.isSkip()
								&& (controller.getSkipType() == Controller.COVERALL || controller
										.getSkipType() == Controller.NORMAL)) {
							// 选择覆盖，将原来的Insert语句变成update语句。
							if (step.getCoverWhereCondition() != null) {
								step.getMethods().setWhereCondition(
										step.getCoverWhereCondition());
							}
							String sql = step.getMethods().getExecuteString(
									para1, values, code, "update");
							sql = parseParamter(sql);
							int size = 0;
							if (around != null) {
								around.before(conn, values);
								try {
									size = pst.executeUpdate(sql);
								} catch (Exception e) {
									if (step.isCount()) {
										errorCount++;
									}
								}
								around.after(conn, values);
							} else {
								try {
									size = pst.executeUpdate(sql);
								} catch (Exception e) {
									if (step.isCount()) {
										errorCount++;
									}
								}
							}

							if (step.isCount()) {
								if (size != 0) {
									successCount++;
								} else {
									errorCount++;
								}
							}
							System.out.println(sql);
						}
						continue outer;
					case Controller.SKIPALL:
						continue outer;
					case Controller.COVERALL:
						if (step.getCoverWhereCondition() != null) {
							step.getMethods().setWhereCondition(
									step.getCoverWhereCondition());
						}
						String sql = step.getMethods().getExecuteString(para1,
								values, code, "update");
						sql = parseParamter(sql);
						int size = 0;
						if (around != null) {
							around.before(conn, values);
							try {
								size = pst.executeUpdate(sql);
							} catch (Exception e) {
								if (step.isCount()) {
									errorCount++;
								}
							}
							around.after(conn, values);
						} else {
							try {
								size = pst.executeUpdate(sql);
							} catch (Exception e) {
								if (step.isCount()) {
									errorCount++;
								}
							}
						}
						if (step.isCount()) {
							if (size != 0) {
								successCount++;
							} else {
								errorCount++;
							}
						}
						continue outer;
					}
				}
				String sql = step.getMethods().getExecuteString(para1, values,
						code, step.getMode());
				System.out.println("sql:"
						+ sql);
				sql = parseParamter(sql);

				int size = 0;
				size = pst.executeUpdate(sql);
				if (step.isCount()) {
					if (size != 0) {
						successCount++;
					} else {
						errorCount++;
					}
				}
				if (step.getOnReturn() != null
						&& step.getOnReturn().length() != 0) {
					js
							.invoke(step.getOnReturn(), new Object[] { size,
									values });
				}
				if (size == 0) {
					// 执行配置文件中配置的onNoData事件。
					if (step.getOnNoData() != null
							&& step.getOnNoData().length() != 0) {
						js.invoke(step.getOnNoData(), new Object[] { values });
					}
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				try {
					// 执行onException函数
					if (step.getOnException() != null) {
						js.invoke(step.getOnException(), new Object[] { e1,
								values });
					}
					if (step.isCount()) {
						errorCount++;
					}
				} catch (ScriptException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
				continue;
			} catch (ScriptException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		if (lock != null && !lock.isRun()) {
			conn.rollback();
		} else {
			conn.commit();
			pst.close();
		}
		
		//主要用于有结束条件的时候 设置进度为100%；
		precent=1; 
	}

	private String parseParamter(String express) {
		Pattern p = Pattern.compile(".*(\\{(.+)\\}).*");
		Matcher m = p.matcher(express);
		if (m.matches()) {
			String value = context.get(m.group(2));
			express = express.replaceAll(m.group(1), value);
			return parseParamter(express);
		}
		return express;
	}

	/**
	 * 非阻塞导入。
	 * 
	 * @param step
	 * @param para
	 * @param conn
	 * @throws SQLException
	 */
	private void executeNoBlock(Step step, XmlConfig para, Connection conn)
			throws SQLException {
		Statement pst = conn.createStatement();
		int start = step.getStartIndex();
		int end = 0;
		ds.setSheet(step.getSheet());
		if ("last".equals(step.getFinishIndex())) {
			end = ds.getLines();
		} else {
			end = Integer.parseInt(step.getFinishIndex());
		}
		if (step.getThreads() == 1) {
			for (int i = start; (!(step.getFinishCondition() == null ? false
					: Boolean.parseBoolean(String.valueOf(getValueFromExpress(
							step.getFinishCondition().replaceAll("stepIndex",
									"" + i), para)))) && i < end); i++) {
				if (lock != null && !lock.isRun())
					break;
				precent += 1.0 / totalsize;
				System.out.println("size:" + totalsize);
				System.out.println(precent);
				List<String> para1 = new ArrayList<String>();
				List<Object> values = new ArrayList<Object>();
				ds.setSheet(step.getSheet());
				Map<String, CodeFinder> code = new HashMap<String, CodeFinder>();
				this.parseContent(para, step.getMethods(), para1, values, code,i);
				if (step.getCheckFunc() != null) {
					try {
						boolean checkRet = Boolean
								.parseBoolean(String.valueOf(js.invoke(step
										.getCheckFunc(), values)));
						if (!checkRet) {
							errorCount++;
							continue;
						}
					} catch (ScriptException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
				try {
					String sql = step.getMethods().getExecuteString(para1,
							values, code, step.getMode());
					System.out.println("sql:"
							+ sql);
					sql = parseParamter(sql);
					System.out.println("当前导入第\t" + i + "行");
					int size = pst.executeUpdate(sql);
					if (size != 0 && step.isCount()) {
						successCount++;
					}
					if (step.getOnReturn() != null
							&& step.getOnReturn().length() != 0) {
						js.invoke(step.getOnReturn(), new Object[] { size,
								values });
					}
					if (size == 0) {
						errorCount++;
						// 执行onNoData函数
						if (step.getOnNoData() != null
								&& step.getOnNoData().length() != 0) {
							js.invoke(step.getOnNoData(),
									new Object[] { values });
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					try {
						// 执行onException函数
						if (step.getOnException() != null) {
							js.invoke(step.getOnException(), new Object[] { e1,
									values });
						}
					} catch (ScriptException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
					if (step.isCount()) {
						errorCount++;
					}
					continue;
				}
			}
			
		}
		// 多线程导入
		else {
			int tsize = step.getThreads();
			int dsize = end - start;
			int length = dsize / tsize;
			// ExecutorService exec=Executors.newCachedThreadPool();
			CountDownLatch startC = new CountDownLatch(1);
			CountDownLatch finishC = new CountDownLatch(tsize);
			threadpool = new ArrayList<Thread>();
			for (int i = 0; i < tsize; i++) {
				ExecuteThread thread = new ExecuteThread(finishC, ds);
				thread.setLock(lock);
				thread.setStartIndex(start + i * length);
				if (i == tsize - 1) {
					thread.setFinishIndex(start + i * length + length + dsize
							% tsize);
				} else {
					thread.setFinishIndex(start + i * length + length);
				}
				thread.setDs(ds);
				thread.setJs(js);
				thread.setPara(para);
				thread.setPst(pst);
				thread.setStep(step);
				thread.setDb(this);
				thread.setSize(totalsize);
				thread.setDaemon(true);
				thread.start();
				threadpool.add(thread);
				// exec.submit(thread);
			}
			// exec.shutdown();
			startC.countDown();
			try {
				// 当所有线程结束后，主线程结束。
				finishC.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if (lock != null && !lock.isRun()) {
					conn.rollback();
				} else {
					conn.commit();
					pst.close();
				}
			}
		}
		
		
		//主要用于有结束条件的时候 将进度设置为100%
		precent=1; 

	}

	/**
	 * 解析内容。主要是将text用#分成一个一个的字段及对应的表达式。
	 * 
	 * @param wb
	 * @param method
	 * @param para
	 * @param values
	 * @param stepIndex
	 */
	void parseContent(XmlConfig p, IDBMethod method, List<String> para,
			List<Object> values, Map<String, CodeFinder> codes, int stepIndex) {
		String content = method.getContent().replaceAll("stepIndex",
				"" + stepIndex);
		content = content.replaceAll("\\s+", "");
		if (content.trim().indexOf("#") != -1) {
			String ss[] = content.trim().split("#");
			for (String s : ss) {
				String[] sa = s.split("=");
				if (sa[0].indexOf("&") != -1) {
					String[] tmp = sa[0].split("&");
					String column = getValueFromExpress(tmp[0], p) == null ? ""
							: getValueFromExpress(tmp[0], p).toString();
					para.add(column);
					String[] code = tmp[1].trim().substring(1,
							tmp[1].length() - 1).split(",");
					if (code.length != 3)
						throw new RuntimeException("code表达式不正确！");
					CodeFinder c = new CodeFinder();
					c.setTablename(getValueFromExpress(code[0], p) == null ? ""
							: getValueFromExpress(code[0], p).toString());
					// c.setColumnTo(getValueFromExpress(code[1], wb, p,sheet)
					// ==
					// null ? ""
					// : getValueFromExpress(code[1], wb, p,sheet).toString());
					c.setColumnTo(code[1]);
					c
							.setColumnFrom(getValueFromExpress(code[2], p) == null ? ""
									: getValueFromExpress(code[2], p)
											.toString());
					c.setValue(getValueFromExpress(sa[1], p) == null ? ""
							: getValueFromExpress(sa[1], p).toString());
					codes.put(column, c);
				} else {
					para.add(getValueFromExpress(sa[0], p) == null ? ""
							: getValueFromExpress(sa[0], p).toString());
				}
				values
						.add(getValueFromExpress(sa[1], p) == null ? ""
								: getValueFromExpress(sa[1], p).getClass() == NativeArray.class ? getValueFromExpress(
										sa[1], p)
										: String.valueOf(getValueFromExpress(
												sa[1], p)));
			}
		}
	}

	/**
	 * 取得表达式的值，此方法会递归调用。
	 * 
	 * @param express
	 * @param p
	 * @return
	 */
	private Object getValueFromExpress(String express, XmlConfig p) {
		Pattern pmethod = Pattern.compile("@(\\w+)\\((([^;]+)*(;([^;]+))*)\\)");
		Pattern pexcel = Pattern.compile("\\$C\\{(.+),([^,\\}]+)\\}");
		Matcher m1 = pmethod.matcher(express);
		Matcher m2 = pexcel.matcher(express);
		// 匹配函数和单元格取值。
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
		}
		// 匹配$C{x,y}
		else if (m2.matches()) {
			return String.valueOf(ds.getValue(
					(int) Double
							.parseDouble(getValueFromExpress(m2.group(1), p)
									.toString()), (int) Double
							.parseDouble(getValueFromExpress(m2.group(2), p)
									.toString())));
		} else {
			try {
				return JScript.eval(express);
			} catch (ScriptException e) {
				return express;
			}
		}
	}

	/**
	 * 连接数据库。
	 * 
	 * @param driver
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private Connection connectToDatabase(Driver driver)
			throws ClassNotFoundException, SQLException {
		Connection conn = null;
		if ("{oracle}".equals(driver.getDriver())) {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String url="jdbc:oracle:thin:@ip:port:{db}"
				.replace("ip", driver.getServer()).replace("{db}",
						driver.getDatabase()).replace("port", driver.getPort());
			url=url.replaceAll("\\s+", "");
			System.out.println(url);
			conn = DriverManager.getConnection(url, driver.getUsername(), driver.getPassword());
		}else if("{mysql}".equals(driver.getDriver())){
			//连接mysql数据库
		}
		return conn;
	}

	/**
	 * 解析数据库连接字符串。
	 * 
	 * @author Dream.Lee
	 * 
	 */
	public class Parser {

		public Driver parseDiver(String url) throws URLFormatException {
			Driver driver = new Driver();
			String[] paras = url.split(";");
			for (String s : paras) {
				if (s.toLowerCase().indexOf("server") != -1
						&& s.toLowerCase().indexOf("server") < s.indexOf("=")) {
					String[] ss = s.split("=");
					if (ss.length < 2)
						throw new URLFormatException("server的配置有错");
					driver.setServer(ss[1]);
				} else if (s.toLowerCase().indexOf("driver") != -1) {
					String[] ss = s.split("=");
					if (ss.length < 2)
						throw new URLFormatException("driver的配置有错");
					driver.setDriver(ss[1]);
				} else if (s.toLowerCase().indexOf("uid") != -1) {
					String[] ss = s.split("=");
					if (ss.length < 2)
						throw new URLFormatException("uid的配置有错");
					driver.setUsername(ss[1]);
				} else if (s.toLowerCase().indexOf("pwd") != -1) {
					String[] ss = s.split("=");
					if (ss.length < 2)
						throw new URLFormatException("pwd的配置有错");
					driver.setPassword(ss[1]);
				} else if (s.toLowerCase().indexOf("database") != -1) {
					String[] ss = s.split("=");
					if (ss.length < 2)
						throw new URLFormatException("database的配置有错");
					driver.setDatabase(ss[1]);
				}else if(s.toLowerCase().indexOf("port")!=-1){
					String[] ss = s.split("=");
					if (ss.length < 2)
						throw new URLFormatException("database的配置有错");
					driver.setPort(ss[1]);
				}
			}
			return driver;
		}
	}

	/**
	 * 数据库配置信息。
	 * 
	 * @author Dream.Lee
	 * 
	 */
	public class Driver {
		private String driver;
		private String server;
		private String database;
		private String username;
		private String password;
		private String port="1521";

		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public String getDriver() {
			return driver;
		}

		public void setDriver(String driver) {
			this.driver = driver;
		}

		public String getServer() {
			return server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		public String getDatabase() {
			return database;
		}

		public void setDatabase(String database) {
			this.database = database;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		@Override
		public String toString() {
			return "driver:" + driver + " database:" + database + " username:"
					+ username + " pwd:" + password + " server:" + server;
		}

	}

	public void setDs(CommonInDataSource ds) {
		this.ds = ds;
	}

	public Controller getController() {
		return controller;
	}

	public float getPrecent() {
		return precent;
	}

	void setPrecent(float p) {
		this.precent = p;
	}

	public List<Thread> getThreadpool() {
		return threadpool;
	}

	/**
	 * 添加输出中间件。
	 * 
	 * @param middleWare
	 */
	public void appendMiddleWare(IMiddleWare middleWare) {
		middleWares.add(middleWare);
	}

	public void cleanup() {
		for (IMiddleWare ware : middleWares) {
			ware.output(os);
		}
	}

	public String getErrorPath() {
		return errorPath;
	}

	public void setErrorPath(String errorPath) {
		this.errorPath = errorPath;
	}
}
