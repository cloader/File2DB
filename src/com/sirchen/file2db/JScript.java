package com.sirchen.file2db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


/**
 * <h3>用来操作js的类</h3><br>
 * 此类会向传入的js字符串中加入一些系统内定的函数。这些函数在pubFunc.xml文件中申明，
 * 配置文件中指明了js函数的代码以及绑定的java类。java类必须包含一个不含参数的构造方法（以后会扩展成任意的构造方法）.
 * @author Dream.Lee
 */
public class JScript {
	
	private ScriptEngine engine;
	private Invocable inv;
	private Map<String,String> bindings;
	
	/**
	 * 通过字符串在构造js文件，字符串的内容必须符合js的语法。
	 * @param script 装有js编码的字符串
	 */
	public JScript(String script){
		init(script);
	}
	
	/**
	 * 通过一个文件来构造js文件.此文件可以不是以.js结尾，只要里面的内容符合js语法就可以。
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @see JScript(InputStream)
	 */
	public JScript(File file) throws FileNotFoundException, IOException{
		this(new FileInputStream(file));
	}
	
	/**
	 * 用一个输入流来构造对象，此方法不会关闭流。
	 * @param is js代码的输入流
	 * @throws IOException
	 */
	public JScript(InputStream is) throws IOException{
		String ret="";
		byte[] buffer=new byte[1024];
		int len=0;
		while((len=is.read(buffer))!=-1){
			ret+=new String(buffer,0,len);
		}
		init(ret);
	}
	
	/**
	 * 初始化方法。真正构造js引擎的方法。
	 * @param str
	 */
	private void init(String str){
		ScriptEngineManager manager=new ScriptEngineManager();
		engine=manager.getEngineByName("JavaScript");
		//读取公共的函数
		String pub=parsePubFunc();
		str+=pub;
		try {
			engine.eval(str);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		bindObj();
		inv=(Invocable)engine;
	}
	
	/**
	 * 解析公用的方法，将其加入到传入的js代码中。
	 * @return 加入了公共代码的js代码段.
	 */
	@SuppressWarnings("unchecked")
	private String parsePubFunc(){
		SAXReader reader=new SAXReader();
		String funcStr="";
		bindings=new HashMap<String,String>();
		try {
			Document doc=reader.read(JScript.class.getResourceAsStream("config.xml"));
			Element funcs=doc.getRootElement().element("functions");
			for(Iterator iter=funcs.elementIterator("function");iter.hasNext();){
				Element e=(Element)iter.next();
				funcStr+=e.elementText("code");
				Element bins=e.element("bindings");
				if(bins!=null){
					for(Iterator iter1=bins.elementIterator("bind");iter1.hasNext();){
						Element bind=(Element)iter1.next();
						String r=bind.attributeValue("property");
						String cs=bind.attributeValue("class");
						bindings.put(r, cs);
					}
				}
			}
		} catch (DocumentException e1) {
			e1.printStackTrace();
		}
		//绑定对象
		return funcStr;
	}
	
	/**
	 * 绑定java对象。
	 */
	private void bindObj(){
		Bindings binds=engine.getBindings(ScriptContext.ENGINE_SCOPE);
		Set<String> keys=bindings.keySet();
		bindStatic(binds);
		for(String key:keys){
			try {
				Class<?> cls=Class.forName(bindings.get(key));
				Object o=cls.newInstance();
				binds.put(key, o);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void bindObject(String key,Object value){
		engine.getBindings(ScriptContext.ENGINE_SCOPE).put(key, value);
	}
	
	
	private void bindStatic(Bindings binds) {
		binds.put("Text", new TextOpetor());
		
	}

	
	static ScriptEngineManager manager=new ScriptEngineManager();
	static ScriptEngine engineStatic=manager.getEngineByName("JavaScript");
	/**
	 * 执行js语句块,如果传入的字符串不符合语法，则抛出异常。
	 * @param par
	 * @return
	 * @throws ScriptException
	 */
	public static Object eval(String par) throws ScriptException{
		return engineStatic.eval(par);
	}
	
	/**
	 * 执行包含对java代码调用的js方法
	 * @param par js代码
	 * @param binds 对象的绑定关系。Map的键为js引用的字符串，值为java对象。
	 * @return 执行后的返回值。
	 * @throws ScriptException
	 */
	public static Object eval(String par,Map<String,Object> binds) throws ScriptException{
		ScriptEngineManager manager=new ScriptEngineManager();
		ScriptEngine engine=manager.getEngineByName("JavaScript");
		Bindings bind=engine.getBindings(ScriptContext.ENGINE_SCOPE);
		Set<String> keys=binds.keySet();
		for(String key:keys){
			bind.put(key, binds.get(key));
		}
		return engine.eval(par,bind);
	}
	
	
	/**
	 * 执行js方法
	 * @param <T>
	 * @param name
	 * @param args
	 * @return
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	public <T> Object invoke(String name,Object... args) throws ScriptException, NoSuchMethodException{
		return (T)inv.invokeFunction(name, args);
	}
	
	/**
	 * 执行js方法。可以用过参数c来指定返回值的类型。
	 * @param <T>
	 * @param name
	 * @param c
	 * @param args
	 * @return
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 */
	public <T> T invoke(String name,Class<T> c,Object... args) throws ScriptException, NoSuchMethodException{
		return c.cast(inv.invokeFunction(name, args));
	}
	
	/**
	 * 执行某对象上的方法
	 * @param <T>
	 * @param o 被调用对象
	 * @param name 被调用方法的名字
	 * @param args 被调用方法的参数
	 * @return
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 */
	@SuppressWarnings("unchecked")
	public <T> Object invoke(Object o,String name,Object... args) throws ScriptException, NoSuchMethodException{
		return (T)inv.invokeMethod(o, name, args);
	}
	
	/**
	 * 执行某对象上的方法
	 * @param <T> 
	 * @param o 被调用对象
	 * @param name 被调用方法的名字
	 * @param c 返回值的类型
	 * @param args 被调用方法的参数
	 * @return
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 */
	public <T> T invoke(Object o,String name,Class<T> c,Object... args) throws ScriptException, NoSuchMethodException{
		return c.cast(inv.invokeMethod(o, name, args));
	}
	
	public static void main(String[] args) throws ScriptException, NoSuchMethodException, IOException {
		ScriptEngineManager manager=new ScriptEngineManager();
		ScriptEngine engine=manager.getEngineByName("JavaScript");
		Bindings bind=engine.getBindings(ScriptContext.ENGINE_SCOPE);
		ExcelOutput e=new ExcelOutput();
		bind.put("output", e);
		List<String> l=new ArrayList<String>(); 
		bind.put("l", l);
		engine.eval("output.initOnce(['编码','名称','备注','错误原因']);"+
				"l.add('名称重复');"+
				"output.appendRecord(l);");
		FileOutputStream fos=new FileOutputStream("d:\\test.xls");
		InputStream is=e.getInputStream();
		byte[] buffer=new byte[1024];
		int len=0;
		while((len=is.read(buffer))!=-1){
			fos.write(buffer,0,len);
		}
		fos.close();
		is.close();
		
	}
}
