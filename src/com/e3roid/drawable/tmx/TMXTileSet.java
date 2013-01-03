/*
 * Copyright (c) 2010-2011 e3roid project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * * Neither the name of the project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.e3roid.drawable.tmx;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;

import android.content.Context;

import com.e3roid.drawable.sprite.TiledSprite;
import com.e3roid.drawable.texture.TiledTexture;
import com.e3roid.util.SAXUtil;

public class TMXTileSet {
	
	private static final String NAME = "name";
	private static final String TILE_WIDTH  = "tilewidth";
	private static final String TILE_HEIGHT = "tileheight";
	private static final String SPACING = "spacing";
	private static final String MARGIN  = "margin";
	
	private final int firstGID;
	private final String name;
	private final int tileWidth;
	private final int tileHeight;
	private final int spacing;
	private final int margin;
	
	private final Context context;
	
	private TiledSprite sprite;
	private TiledTexture texture;
	private String imageSource;	
	private final HashMap<Integer, ArrayList<TMXProperty>> tileProperties = new HashMap<Integer, ArrayList<TMXProperty>>();
	
	public TMXTileSet(int firstGID, Attributes atts, Context context) {
		this.firstGID = firstGID;
		this.name = SAXUtil.getString(atts, NAME);
		this.tileWidth  = SAXUtil.getInt(atts, TILE_WIDTH);
		this.tileHeight = SAXUtil.getInt(atts, TILE_HEIGHT);
		this.spacing    = SAXUtil.getInt(atts, SPACING, 0);
		this.margin     = SAXUtil.getInt(atts, MARGIN, 0);
		this.context = context;
	}

	public void addTileProperty(int id, TMXProperty property) {
		int gid = firstGID + id;
		ArrayList<TMXProperty> properties = tileProperties.get(gid);
		if (properties == null) {
			properties = new ArrayList<TMXProperty>();
		}
		properties.add(property);
		tileProperties.put(gid, properties);
	}
		
	public ArrayList<TMXProperty> getTileProperty(int gid) {
		return tileProperties.get(gid);
	}

	public void setImageSource(String source) {
		this.imageSource = source;
	}
	
	public String getimageSource() {
		return this.imageSource;
	}
	
	public TiledSprite getSprite(int gid) {
		sprite = getSprite();
		
		int index = gid - firstGID;
		int column = index % getCount(texture.getWidth(), texture.getTileWidth(), spacing);
		int row    = index / getCount(texture.getWidth(), texture.getTileWidth(), spacing);

		sprite.setTile(column, row);
		
		return sprite;
	}
	
	public TiledSprite getSprite() {
		if (texture == null || sprite == null) {
			texture = new TiledTexture(imageSource, tileWidth, tileHeight, 0, 0, spacing, margin, context);
			texture.setReusable(true);
			sprite = new TiledSprite(texture, 0, 0, 0, 0);
		}
		return sprite;
	}
	
	private int getCount(int total, int unit, int spacing) {
		return (total / (unit + spacing));
	}
	
	public int getFirstGID() {
		return this.firstGID;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getTileWidth() {
		return this.tileWidth;
	}
	
	public int getTileHeight() {
		return this.tileHeight;
	}
	
	public int getSpacing() {
		return this.spacing;
	}
	
	public int getMargin() {
		return this.margin;
	}
}
