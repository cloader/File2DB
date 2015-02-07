package com.sirchen.file2db;

public class CheckException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String[] errorHeads;
	public String[] getErrorHeads() {
		return errorHeads;
	}
	
	public CheckException(String msg){
		super(msg);
	}
	
	public void setErrorHeads(String[] errorHeads) {
		this.errorHeads = errorHeads;
	}
	public CheckException() {
		super("检查出错");
	}

}
