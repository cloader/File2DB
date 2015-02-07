package com.sirchen.file2db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


public class TxtOutput implements IOutput {

	private ByteArrayOutputStream bos;
	public boolean init=false;
	
	public TxtOutput(){
		bos=new ByteArrayOutputStream();
	}
	
	@Override
	public void appendRecord(List<? extends Object> data) {
		for(Object o:data){
			try {
				bos.write((o.toString()+"\t").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			bos.write(System.getProperty("line.separator").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(bos.toByteArray());
	}

	@Override
	public String getMimeType() {
		return "text/plain";
	}

	@Override
	public boolean hasError() {
		return false;
	}

	@Override
	public void initOnce(List<String> titles) {
		if(!init){
			appendRecord(titles);
			init=true;
		}
	}

	@Override
	public void initOnce(String[] titles) {
		initOnce(Arrays.asList(titles));
	}

	@Override
	public void initTitle(List<String> titles) {
		appendRecord(titles);
	}
	
	public static void main(String[] args) {
		System.out.println(System.getProperty("line.separator").replace("\\", "\\\\"));
	}

	@Override
	public String getExtension() {
		return "txt";
	}

	@Override
	public void initTimes(String[] titles, int times) {
		this.initTimes(Arrays.asList(titles), times);
	}

	@Override
	public void initTimes(List<String> titles, int times) {
		
	}

}
