package com.sirchen.file2db;

import java.util.List;

/**
 * @author chen
 * @Description 
 * @Date 2015-2-7
 * @version V1.0
 */
public class XmlConfig {
	private String connurl;
	private String source;
	private String script;
	private String outkey;
	private List<Step> steps;
	private List<String> checkHeads;
	/**
	 * @return the connurl
	 */
	public String getConnurl() {
		return connurl;
	}
	/**
	 * @param connurl the connurl to set
	 */
	public void setConnurl(String connurl) {
		this.connurl = connurl;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * @return the script
	 */
	public String getScript() {
		return script;
	}
	/**
	 * @param script the script to set
	 */
	public void setScript(String script) {
		this.script = script;
	}
	/**
	 * @return the outkey
	 */
	public String getOutkey() {
		return outkey;
	}
	/**
	 * @param outkey the outkey to set
	 */
	public void setOutkey(String outkey) {
		this.outkey = outkey;
	}
	/**
	 * @return the steps
	 */
	public List<Step> getSteps() {
		return steps;
	}
	/**
	 * @param steps the steps to set
	 */
	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}
	/**
	 * @return the checkHeads
	 */
	public List<String> getCheckHeads() {
		return checkHeads;
	}
	/**
	 * @param checkHeads the checkHeads to set
	 */
	public void setCheckHeads(List<String> checkHeads) {
		this.checkHeads = checkHeads;
	}
	
}
