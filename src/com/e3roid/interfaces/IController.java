package com.e3roid.interfaces;

public interface IController {
	public static final int CENTER = 0;
	public static final int LEFT   = 1;
	public static final int RIGHT  = 2;
	public static final int UP     = 3;
	public static final int DOWN   = 4;
	
	public static final int TOP_RIGHT_CORNER = 1;
	public static final int TOP_LEFT_CORNER = 2;
	public static final int BOTTOM_LEFT_CORNER = 3;
	public static final int BOTTOM_RIGHT_CORNER = 4;

	public int getDirection() ;
}
