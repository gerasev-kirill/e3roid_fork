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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import org.xml.sax.helpers.DefaultHandler;

import com.e3roid.util.SAXUtil;

public class TMXTiledMapLoader extends DefaultHandler {
	
	private static final String TAG_DATA = "data";
	private static final String TAG_DATA_ATTRIBUTE_ENCODING = "encoding";
	private static final String TAG_DATA_ATTRIBUTE_COMPRESSION = "compression";
	private static final String TAG_IMAGE = "image";
	private static final String TAG_LAYER = "layer";
	private static final String TAG_MAP = "map";
	private static final String TAG_PROPERTY = "property";
	private static final String TAG_TILESET = "tileset";
	private static final String TAG_TILESET_ATTRIBUTE_SOURCE = "source";
	private static final String TAG_TILESET_ATTRIBUTE_FIRSTGID = "firstgid";
	private static final String TAG_TILE = "tile";
	private static final String TAG_TILE_ATTRIBUTE_ID = "id";
	private static final String TAG_IMAGE_ATTRIBUTE_SOURCE = "source";

	private static final String TAG_OBJECTGROUP = "objectgroup";
	private static final String TAG_OBJECT = "object";
	
	private StringBuilder characters = new StringBuilder();
	
	private Context context;
	private TMXTiledMap tmxTiledMap;
	private String encoding;
	private String compression;
	private int lastTileSetTileID;
	
	private boolean inTileset = false;
	private boolean inTile = false;
	private boolean inData = false;
	private boolean inObject = false;
	
	public TMXTiledMap loadFromAsset(String filename, Context context) throws TMXException {
		try {
			return load(context.getAssets().open(filename), context);
		} catch (IOException e) {
			throw new TMXException(e);
		}
	}
	
	public TMXTiledMap load(InputStream inputStream, Context context) throws TMXException {
		try {
			this.context = context;
			
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);

			xr.parse(new InputSource(new BufferedInputStream(inputStream)));
		} catch (Exception e) {
			throw new TMXException(e);
		}

		return this.tmxTiledMap;
	}
	
	@Override
	public void startElement(String uri, String localName, 
			String qName, Attributes atts) throws SAXException {
		if(localName.equals(TAG_MAP)){
			this.tmxTiledMap = new TMXTiledMap(atts);
		} else if(localName.equals(TAG_TILESET)){
			this.inTileset = true;
			String tsxTileSetSource = SAXUtil.getString(atts, TAG_TILESET_ATTRIBUTE_SOURCE);
			if(tsxTileSetSource == null) {
				this.tmxTiledMap.addTileSet(new TMXTileSet(
						SAXUtil.getInt(atts, TAG_TILESET_ATTRIBUTE_FIRSTGID), atts, context));
			}
		} else if(localName.equals(TAG_IMAGE)){
			ArrayList<TMXTileSet> tmxTileSets = this.tmxTiledMap.getTileSets();
			tmxTileSets.get(tmxTileSets.size() - 1).setImageSource(SAXUtil.getString(atts, TAG_IMAGE_ATTRIBUTE_SOURCE));
		} else if(localName.equals(TAG_TILE)) {
			this.inTile = true;
			if(this.inTileset) {
				this.lastTileSetTileID = SAXUtil.getInt(atts, TAG_TILE_ATTRIBUTE_ID);
			} else if(this.inData) {
				ArrayList<TMXLayer> tmxLayers = this.tmxTiledMap.getLayers();
				tmxLayers.get(tmxLayers.size() - 1).setup(atts);
			}
		} else if(localName.equals(TAG_PROPERTY)) {
			if (this.inTile) {
				ArrayList<TMXTileSet> tmxTileSets = this.tmxTiledMap.getTileSets();
				tmxTileSets.get(tmxTileSets.size() - 1).addTileProperty(this.lastTileSetTileID, new TMXProperty(atts));
			} else if (this.inObject) {
				ArrayList<TMXObjectGroup> groups = this.tmxTiledMap.getObjectGroups();
				TMXObjectGroup lastGroup = groups.get(groups.size() - 1);

				ArrayList<TMXObject> objects = lastGroup.getObjects();
				objects.get(objects.size() - 1).addObjectProperty(new TMXProperty(atts));
			}
		} else if(localName.equals(TAG_LAYER)){
			this.tmxTiledMap.addTMXLayer(new TMXLayer(this.tmxTiledMap, atts));
		} else if(localName.equals(TAG_DATA)){
			this.inData = true;
			this.encoding    = SAXUtil.getString(atts, TAG_DATA_ATTRIBUTE_ENCODING);
			this.compression = SAXUtil.getString(atts, TAG_DATA_ATTRIBUTE_COMPRESSION);
		} else if (localName.equals(TAG_OBJECTGROUP)) {
			this.tmxTiledMap.addObjectGroup(new TMXObjectGroup(atts));
		} else if (localName.equals(TAG_OBJECT)) {
			this.inObject = true;
			ArrayList<TMXObjectGroup> groups = this.tmxTiledMap.getObjectGroups();
			groups.get(groups.size() - 1).addObject(new TMXObject(atts));
		}
	}
	

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(localName.equals(TAG_TILESET)){
			this.inTileset = false;
		} else if(localName.equals(TAG_TILE)) {
			this.inTile = false;
		} else if(localName.equals(TAG_DATA)){
			boolean binarySaved = this.compression != null && this.encoding != null;
			if(binarySaved) {
				ArrayList<TMXLayer> tmxLayers = this.tmxTiledMap.getLayers();
				try {
					tmxLayers.get(tmxLayers.size() - 1).extract(this.characters.toString().trim(), this.encoding, this.compression);
				} catch (IOException e) {
					throw new SAXException(e);
				}
				this.compression = null;
				this.encoding = null;
			}
			this.inData = false;
		} else if (localName.equals(TAG_OBJECT)) {
			this.inObject = false;
		}

		this.characters.setLength(0);
	}
	
	@Override
	public void characters(char[] characters, int start, int length) throws SAXException {
		this.characters.append(characters, start, length);
	}
}
