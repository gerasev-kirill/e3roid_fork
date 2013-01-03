package com.e3roid.drawable.tmx;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import com.e3roid.util.SAXUtil;

public class TMXObject {

	private final String name;
	private final String type;
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private ArrayList<TMXProperty> properties = new ArrayList<TMXProperty>();
	
	public TMXObject(Attributes attrs) {
		this.name = SAXUtil.getString(attrs, "name");
		this.type = SAXUtil.getString(attrs, "type");
		this.x    = SAXUtil.getInt(attrs, "x", 0);
		this.y    = SAXUtil.getInt(attrs, "y", 0);
		this.width  = SAXUtil.getInt(attrs, "width", 0);
		this.height = SAXUtil.getInt(attrs, "height", 0);
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void addObjectProperty(TMXProperty prop) {
		this.properties.add(prop);
	}
	
	public ArrayList<TMXProperty> getObjectProperties() {
		return this.properties;
	}
}
