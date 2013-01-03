package com.e3roid.drawable.tmx;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import com.e3roid.util.SAXUtil;

public class TMXObjectGroup {
	private final String name;
	private final int width;
	private final int height;
	private ArrayList<TMXObject> objects = new ArrayList<TMXObject>();
	
	public TMXObjectGroup(Attributes attrs) {
		this.name = SAXUtil.getString(attrs, "name");
		this.width  = SAXUtil.getInt(attrs, "width", 0);
		this.height = SAXUtil.getInt(attrs, "height", 0);
	}
	
	public ArrayList<TMXObject> getObjects() {
		return objects;
	}
	
	public void addObject(TMXObject object) {
		objects.add(object);
	}
	
	public String getName() {
		return name;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
