package com.sirchen.file2db;

import java.util.ArrayList;
import java.util.List;

/**
 * 导出控制类。用来逐条控制导出
 * @author Dream.Lee
 *
 */
public class Controller {
	
	private boolean working;
	
	private Object lock=new Object();
	
	private boolean skip;
	
	private int skipType=0;
	
	/**
	 * 普通模式
	 */
	public final static int NORMAL=0;
	
	/**
	 * 全部跳过
	 */
	public final static int SKIPALL=1;
	
	/**
	 * 全部覆盖
	 */
	public final static int COVERALL=2;
	
	/**
	 * 订阅者列表
	 */
	private List<IObserver> observers=new ArrayList<IObserver>();
	
	public Object getLock() {
		return lock;
	}

	public void setLock(Object lock) {
		this.lock = lock;
	}
	
	public void next(){
		skip=false;
		synchronized(lock){
			lock.notify();
		}
	}
	
	//覆盖所有
	public void coverAll(){
		skipType=Controller.COVERALL;
		synchronized(lock){
			lock.notify();
		}
	}
	
	public void skip(){
		skip=true;
		synchronized(lock){
			lock.notify();
		}
	}
	
	//跳过所有
	public void skipAll(){
		skipType=Controller.SKIPALL;
		synchronized(lock){
			lock.notify();
		}
	}
	

	public boolean isSkip() {
		return skip;
	}
	
	public void addObserver(IObserver ob){
		observers.add(ob);
	}
	
	public IObserver removeObserver(int index){
		return observers.remove(index);
	}
	
	public boolean removeObserver(IObserver ob){
		return observers.remove(ob);
	}
	
	/**
	 * 更新所有。
	 */
	public void updateAll(String msg){
		for(IObserver o:observers){
			o.updateblock(msg);
		}
	}

	public boolean isWorking() {
		return working;
	}

	public void setWorking(boolean working) {
		this.working = working;
	}

	public int getSkipType() {
		return skipType;
	}

	public void setSkipType(int skipType) {
		this.skipType = skipType;
	}
}
