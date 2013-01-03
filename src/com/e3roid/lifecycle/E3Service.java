package com.e3roid.lifecycle;

/**
 * E3Service represents service which is runnable in the threads.
 */
public abstract class E3Service implements E3LifeCycle, Runnable {
	
	private long id;
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return this.id;
	}
}
