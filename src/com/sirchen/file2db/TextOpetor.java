package com.sirchen.file2db;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 
 * @author Dream.Lee
 */
public class TextOpetor {
	
	public String path;
	
	public TextOpetor(String path) {
		super();
		this.path = path;
	}
	
	public TextOpetor(){}
	
	public void clear() throws UnsupportedEncodingException, IOException{
		FileOutputStream fos=new FileOutputStream(path);
		fos.write("".getBytes("UTF-8"));
		fos.close();
	}
	
	public void appendLine(String line,boolean html,boolean append) throws IOException{
		StringBuilder sb=new StringBuilder();
		if(append){
			FileInputStream fis=null;
			try{
				fis=new FileInputStream(path);
			}catch(IOException e){
				e.printStackTrace();
			}
			byte[] buffer=new byte[1024];
			int length=0;
			
			if(fis!=null){
				while((length=fis.read(buffer))!=-1){
					sb.append(new String(buffer,0,length,"UTF-8"));
				}
			}
			fis.close();
		}
		FileOutputStream fos=new FileOutputStream(path);
		if(html){
			line=sb.toString()+"<br/>"+line+"<br/>";
		}else{
			line=sb.toString()+"\r\n"+line+"\r\n";
		}
		fos.write(line.getBytes("UTF-8"));
		fos.close();
	}

}
