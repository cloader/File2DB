package com.sirchen.file2db;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author chen
 * @Description 
 * @Date 2015-2-7
 * @version V1.0
 */
public class XmlConfigParser {
	private XmlConfigParser() {
	};

	private static XmlConfigParser instance;

	public static XmlConfigParser getInstance() {
		if (instance == null) {
			instance = new XmlConfigParser();
		}
		return instance;
	}
	public XmlConfig parseConifg(String content) throws UnsupportedEncodingException, DocumentException{
		SAXReader reader = new SAXReader();
		if (content.startsWith("?")) {
			content = content.substring(1);
		}
		Document doc = reader.read(new ByteArrayInputStream(content
				.getBytes("UTF-8")));
		XmlConfig para = new XmlConfig();
		Element root = doc.getRootElement();
		para.setOutkey(root.attributeValue("outKey")==null?"excel":root.attributeValue("outKey"));
		if(root.element("properties")!=null){
			String url="";
			String path=root.element("properties").attributeValue("src");
			if(path.startsWith("classpath")){
				path=path.substring(10);
			}
			Properties p=new Properties();
			try {
				p.load(this.getClass().getResourceAsStream(path));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			for(Iterator iter=root.element("properties").elementIterator("property");iter.hasNext();){
				Element e=(Element)iter.next();
				url+=e.attributeValue("key")+"="+p.getProperty(e.attributeValue("match"))+";";
			}
			para.setConnurl(url);
		}else{
			para.setConnurl(root.element("connurl") == null ? null : root.element("connurl").getText());
		}
		para.setScript(root.element("code") == null ? null : root.element("code").getText());
		para.setSource(root.element("source") == null ? null : root.element(
				"source").getText());
		
		Element steps = root.element("steps");
		List<Step> stepL = new ArrayList<Step>();
		
		Element check=root.element("check");
		if(check!=null){
			List<String> heads=new ArrayList<String>();
			
			for(Iterator<Element> iter=check.elementIterator("head");iter.hasNext();){
				heads.add(iter.next().getTextTrim());
			}
			para.setCheckHeads(heads);
		}
		
		for (Iterator<Element> iter = steps.elementIterator("step"); iter
				.hasNext();) {
			Element step = iter.next();
			Step sp = new Step();
			
			sp.setStartIndex(Integer
					.parseInt(step.attribute("startIndex") == null ? "0" : step
							.attribute("startIndex").getText()));
			sp.setFinishIndex(step.attribute("finishIndex") == null ? "0"
					: step.attribute("finishIndex").getText());
			sp.setFinishCondition(step.attribute("finishCondition") == null ? ""
							: step.attribute("finishCondition").getText());
			sp.setSheet(Integer.parseInt(step.attribute("sheet") == null ? "0"
					: step.attribute("sheet").getText()));
			sp.setOnException(step.attributeValue("onException"));
			sp.setOnNoData(step.attributeValue("onNoData"));
			sp.setOnReturn(step.attributeValue("onReturn"));
			sp.setCoverCondition(step.attributeValue("coverCondition"));
			sp.setWhereCondition(step.attributeValue("whereCondition"));
			sp.setCoverWhereCondition(step.attributeValue("coverWhereCondition"));
			sp.setCoverPolicy(step.attributeValue("coverPolicy"));
			sp.setCheckFunc(step.attributeValue("checkFunc"));
			sp.setMode(step.attributeValue("mode") == null ? "insert" : step
					.attributeValue("mode"));
			sp.setComplementClass(step.attributeValue("complement"));
			sp.setUpdateAroundClass(step.attributeValue("updateAround"));
			sp.setThreads(step.attributeValue("threads") == null ? 1 : "system"
					.equals(step.attributeValue("threads")) ? Runtime
					.getRuntime().availableProcessors() : Integer.parseInt(step
					.attributeValue("threads"))>0?Integer.parseInt(step.attributeValue("threads")):1);
			sp.setCount(Boolean.parseBoolean(step.attributeValue("count")));
			IDBMethod method = null;
			if (step.element("proc") != null) {
				
				method = new ProcedureMethod();
				Element e = step.element("proc");
				method.setName(e.attribute("name").getText());
				List<String> para1 = new ArrayList<String>();
				for (Iterator<Element> it = e.elementIterator("parameter"); it
						.hasNext();) {
					para1.add(it.next().getText());
				}
				method.setPara(para1);
			} else if (step.element("meta") != null) {
				Element e = step.element("meta");
				method = new MetaMethod();
				method.setContent(e.getText());
			} else if (step.element("sql") != null) {
				Element e = step.element("sql");
				method = new SQLMethod();
				method.setName(e.attribute("name").getText());
				method.setContent(e.getText());
			} else {
				throw new RuntimeException("未知锟斤拷签!");
			}
			sp.setMethods(method);
			stepL.add(sp);
		}
		para.setSteps(stepL);
		return para;
	}
	public XmlConfig parseConfig(InputStream is) throws IOException,
	DocumentException {
		String ret = "";
		BufferedReader reader=new BufferedReader(new InputStreamReader(is,"UTF-8"));
		String line;
		while((line=reader.readLine())!=null){
			ret+=line+"\n";
		}
		is.close();
		return parseConifg(ret);
		}
}
