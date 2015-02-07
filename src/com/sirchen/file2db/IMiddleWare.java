package com.sirchen.file2db;


/**
 * 输出中间件接口。
 * 此接口的实现类最好采用，动态方法加静态方法相结合的设计。
 * 就是将传入的IOutput对象存于静态变量中。再用静态方法取得感兴趣的对象。<br/>
 * @author Dream.Lee
 * @version 2011-3-23
 */
public interface IMiddleWare {
	/**
	 * 系统会调用此方法，将IOutput传入此方法。
	 * @param o
	 */
	public String output(IOutput o);
	
	public void output(String key,IOutput o);
	
	public IOutput removeOutput(String key);
	
	
}
