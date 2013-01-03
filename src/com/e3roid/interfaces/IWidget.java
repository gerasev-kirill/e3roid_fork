package com.e3roid.interfaces;

import com.e3roid.E3Activity;
import com.e3roid.drawable.Drawable;
import com.e3roid.drawable.Sprite;

public interface IWidget extends Drawable {
	public  void add(Sprite widgetItem);
	public  void remove(Sprite widgetItem);
	public  void layoutVerticalCenter(E3Activity context);
	public  void layoutHorizontalCenter(E3Activity context);
}
