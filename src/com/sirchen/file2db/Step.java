package com.sirchen.file2db;
/**
 * @author chen
 * @Description 
 * @Date 2015-2-7
 * @version V1.0
 */
public class Step {
public static final String UPDATE="update";
	
	public static final String INSERT="insert";
	
	/**
	 * 冲突处理策略
	 */
	public static final String POLICY_STEP="step";
	
	public static final String POLICY_IGORE="igore";
	
	public static final String POLICY_COVERALL="cover_all";
	
	/**
	 * 开始的行号
	 */
	private int startIndex;
	/**
	 * 结束的行号
	 */
	private String finishIndex;
	/**
	 * 结束的条件，优先级高于finishIndex。一般为一个js方法
	 */
	private String finishCondition;
	
	/**
	 * 执行该步的具体方法
	 */
	private IDBMethod method;
	
	private String mode;
	
	/**
	 * excel中的第几个sheet（excel专用）
	 */
	private int sheet;
	
	/**
	 * 出现异常时调用的事件方法
	 */
	private String onException;
	
	/**
	 * 执行后对数据库无影响的事件方法
	 */
	private String onNoData;
	
	/**
	 * 模式为update时where条件的生成策略
	 */
	private String whereCondition;
	
	/**
	 * 冲突检测的where条件生成策略。
	 */
	private String coverWhereCondition;
	
	/**
	 * 执行sql语句之前的检查方法。
	 */
	private String checkFunc;
	
	/**
	 * 
	 */
	private String onReturn;
	
	/**
	 * 冲突处理策略
	 */
	private String coverPolicy;
	
	/**
	 * 发生冲突的条件
	 */
	private String coverCondition;
	
	/**
	 * 完成后调用的对象
	 */
	private String complementClass;
	
	/**
	 * 遇冲突后改执行update时调用的对象
	 */
	private String updateAroundClass;
	
	/**
	 * 执行用到的线程数，默认为1
	 */
	private int threads;
	
	/**
	 * 此步操作是否要记录成功和失败条数
	 */
	private boolean count=false;

	public boolean isCount() {
		return count;
	}

	public void setCount(boolean count) {
		this.count = count;
	}

	public String getCoverPolicy() {
		return coverPolicy;
	}

	public void setCoverPolicy(String coverPolicy) {
		this.coverPolicy = coverPolicy;
	}

	public String getCoverCondition() {
		return coverCondition;
	}

	public void setCoverCondition(String coverCondition) {
		this.coverCondition = coverCondition;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public String getFinishIndex() {
		return finishIndex;
	}

	public void setFinishIndex(String finishIndex) {
		this.finishIndex = finishIndex;
	}

	public String getFinishCondition() {
		return finishCondition;
	}

	public void setFinishCondition(String finishCondition) {
		this.finishCondition = finishCondition;
	}

	public IDBMethod getMethods() {
		return method;
	}

	public void setMethods(IDBMethod method) {
		this.method = method;
	}

	public int getSheet() {
		return sheet;
	}

	public void setSheet(int sheet) {
		this.sheet = sheet;
	}

	public String getOnException() {
		return onException;
	}

	public void setOnException(String onException) {
		this.onException = onException;
	}

	public String getOnNoData() {
		return onNoData;
	}

	public void setOnNoData(String onNoData) {
		this.onNoData = onNoData;
	}

	public String getOnReturn() {
		return onReturn;
	}

	public void setOnReturn(String onReturn) {
		this.onReturn = onReturn;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getComplementClass() {
		return complementClass;
	}

	public void setComplementClass(String complementClass) {
		this.complementClass = complementClass;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public String getUpdateAroundClass() {
		return updateAroundClass;
	}

	public void setUpdateAroundClass(String updateAroundClass) {
		this.updateAroundClass = updateAroundClass;
	}

	public String getWhereCondition() {
		return whereCondition;
	}

	public void setWhereCondition(String whereCondition) {
		this.whereCondition = whereCondition;
	}

	public String getCoverWhereCondition() {
		return coverWhereCondition;
	}

	public void setCoverWhereCondition(String coverWhereCondition) {
		this.coverWhereCondition = coverWhereCondition;
	}

	public String getCheckFunc() {
		return checkFunc;
	}

	public void setCheckFunc(String checkFunc) {
		this.checkFunc = checkFunc;
	}
}
