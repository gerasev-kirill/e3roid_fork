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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import javax.microedition.khronos.opengles.GL10;

import org.xml.sax.Attributes;

import android.graphics.Rect;

import com.e3roid.E3Engine;
import com.e3roid.drawable.Drawable;
import com.e3roid.drawable.Shape;
import com.e3roid.drawable.sprite.TiledSprite;
import com.e3roid.opengl.GLHelper;
import com.e3roid.util.Debug;
import com.e3roid.util.Base64;
import com.e3roid.util.Base64InputStream;
import com.e3roid.util.SAXUtil;

public class TMXLayer implements Drawable {
	
	private final String name;
	private final int columns;
	private final int rows;
	private final TMXTiledMap tiledMap;
	private final TMXTile[][] tiles;

	private E3Engine engine;
	private int tileCount = 0;
	private boolean removed = false;
	private boolean useVBO  = true;
	private boolean useLoop = false;
	private boolean stopOnTheEdge = true;
	
	private int x = 0;
	private int y = 0;
	
	private int width;
	private int height;
	private int sceneWidth;
	private int sceneHeight;
	
	private TMXTile tileToDraw;
	private TiledSprite spriteToDraw;
	
	private ArrayList<Shape> children = new ArrayList<Shape>();

	public TMXLayer(TMXTiledMap tiledMap, Attributes atts) {
		this.tiledMap = tiledMap;
		this.name = SAXUtil.getString(atts, "name");
		this.columns = SAXUtil.getInt(atts, "width");
		this.rows    = SAXUtil.getInt(atts, "height");
		this.tiles = new TMXTile[rows][columns];
	}
	
	public void addChild(Shape shape) {
		children.add(shape);
	}
	
	public void removeChild(Shape shape) {
		children.remove(shape);
	}

	public int setX(int x) {
		if (!useLoop && stopOnTheEdge) {
			if (x < 0) x = 0;
			if (x > getMaxX()) x = getMaxX();
		}
		if (x % width == 0) x = 0;
		this.x = x;
		return this.x;
	}
	
	public int setY(int y) {
		if (!useLoop && stopOnTheEdge) {
			if (y < 0) y = 0;
			if (y > getMaxY()) y = getMaxY();
		}
		if (y % height == 0) y = 0;
		this.y = y;
		return y;
	}
	
	public void setPosition(int x, int y) {
		this.setX(x);
		this.setY(y);
	}
	
	public void scroll(int x, int y) {
		int relativeX = this.x - setX(x);
		int relativeY = this.y - setY(y);
		
		for (Shape child : children) {
			child.moveRelative(relativeX, relativeY);
		}
	}
	
	public void setSceneSize(int width, int height) {
		this.sceneWidth  = width;
		this.sceneHeight = height;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}

	public int getColumnAtPosition(int x) {
		x = x % width;
		return x / (width / columns);
	}
	
	public int getRowAtPosition(int y) {
		y = y % height;
		return y / (height / rows);
	}
	
	public TMXTile getTileAt(int column, int row) {
		if (row >= tiles.length) return null;
		if (column >= tiles[row].length) return null;
		return tiles[row][column];
	}
	
	public TMXTile getTileFromPosition(int x, int y) {
		int column = getColumnAtPosition(x + this.x);
		int row = getRowAtPosition(y + this.y);
		return getTileAt(column, row);
	}
	
	public ArrayList<TMXTile> getTileFromRect(Rect rect) {
		return getTileFromRect(rect, 0, 0);
	}
	
	public ArrayList<TMXTile> getTileFromRect(Rect rect, int xstep, int ystep) {
		ArrayList<TMXTile> tiles = new ArrayList<TMXTile>();
		
		rect.left   += xstep;
		rect.right  += xstep;
		rect.top    += ystep;
		rect.bottom += ystep;
		
		TMXTile leftTop     = getTileFromPosition(rect.left,  rect.top);
		TMXTile leftBottom  = getTileFromPosition(rect.left,  rect.bottom);
		TMXTile rightTop    = getTileFromPosition(rect.right, rect.top);
		TMXTile rightBottom = getTileFromPosition(rect.right, rect.bottom);
		
		if (!TMXTile.isEmpty(leftTop)) tiles.add(leftTop);
		if (!TMXTile.isEmpty(leftBottom)  && !tiles.contains(leftBottom))  tiles.add(leftBottom);
		if (!TMXTile.isEmpty(rightTop)    && !tiles.contains(rightTop))    tiles.add(rightTop);
		if (!TMXTile.isEmpty(rightBottom) && !tiles.contains(rightBottom)) tiles.add(rightBottom);
		
		return tiles;
	}
	
	@Override
	public void onLoadSurface(GL10 gl) {
		onLoadSurface(gl, false);
	}
	
	@Override
	public void onLoadSurface(GL10 gl, boolean force) {	
		ArrayList<TMXTileSet> tileSets = tiledMap.getTileSets();
		for (TMXTileSet tileSet : tileSets) {
			TiledSprite sprite = tileSet.getSprite();
			sprite.enableVBO(useVBO);
			sprite.onLoadEngine(engine);
			sprite.onLoadSurface(gl, force);
		}
	}

	@Override
	public void onDraw(GL10 gl) {
		
		int columnCount = (int)Math.ceil((double)sceneWidth / (double)tiledMap.getTileWidth());
		int rowCount    = (int)Math.ceil((double)sceneHeight / (double)tiledMap.getTileHeight());
		
		int firstColumn = Math.max(0, Math.min(columns, (x / tiledMap.getTileWidth())));
		int lastColumn  = Math.min(firstColumn + columnCount + 1, columns);
		
		int firstRow = Math.max(0, Math.min(rows, (y / tiledMap.getTileHeight())));
		int lastRow = Math.min(firstRow + rowCount + 1, rows);
		
		drawTile(firstColumn, lastColumn, firstRow, lastRow, gl);
		
		if (useLoop) {
			int spareColumn = columnCount - (lastColumn - firstColumn);
			int spareRow    = rowCount - (lastRow - firstRow);
						
			if (spareColumn >= 0 || spareRow >= 0) {

				firstColumn = 0;
				firstRow    = 0;
				
				if (spareColumn <= 0) {
					lastColumn  = columnCount;
				} else {
					lastColumn = spareColumn + 1;
				}
				if (spareRow <= 0) {
					lastRow = rowCount;
				} else {
					lastRow = spareRow + 1;
				}
				
				drawTile(firstColumn, lastColumn, firstRow, lastRow, gl);
			}
		}
	}
	
	private void drawTile(int firstColumn, int lastColumn, int firstRow, int lastRow, GL10 gl) {
		GLHelper.resetCurrentTextureID();
		for (int i = firstRow; i < lastRow; i++) {
			for (int j = firstColumn; j < lastColumn; j++) {
				if (removed) break;
				tileToDraw = tiles[i][j]; 
				if (TMXTile.isEmpty(tileToDraw)) {
					continue;
				}
				spriteToDraw = tiledMap.getSpriteByGID(tileToDraw.getGID());
				if (spriteToDraw == null) continue;
				
				int sceneX = j * spriteToDraw.getWidth()  - x;
				int sceneY = i * spriteToDraw.getHeight() - y;
				
				if (useLoop) {
					int shownWidth  = width  - x;
					int shownHeight = height - y;
					int nextX = sceneX + width;
					int nextY = sceneY + height;
					if (shownWidth < sceneWidth && nextX >= shownWidth && nextX < sceneWidth) {
						sceneX = nextX;
					}
					if (shownHeight < sceneHeight && nextY >= shownHeight && nextY < sceneHeight) {
						sceneY = nextY;
					}
				}
				
				spriteToDraw.setTile(tileToDraw.getAtlasColumn(), tileToDraw.getAtlasRow());
				spriteToDraw.move(sceneX, sceneY);
				spriteToDraw.onFasterDraw(gl);
			}
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getColumns() {
		return this.columns;
	}
	
	public int getRows() {
		return this.rows;
	}
	
	public void extract(String data, String encoding, String compression) throws IOException {
		DataInputStream dataIn = null;
		try{
			InputStream in = new ByteArrayInputStream(data.getBytes("UTF-8"));

			if(encoding != null && encoding.equals("base64")) {
				in = new Base64InputStream(in, Base64.DEFAULT);
			}
			if(compression != null){
				if(compression.equals("gzip")) {
					in = new GZIPInputStream(in);
				} else {
					throw new IllegalArgumentException("compression '" + compression + "' is not supported.");
				}
			}
			dataIn = new DataInputStream(in);

			int expectedTileCount = columns * rows;
			while(this.tileCount < expectedTileCount) {
				this.addTile(readGID(dataIn));
			}
		} finally {
			try {
				dataIn.close();
			} catch (IOException e) {
				Debug.e(e);
			}
		}
	}
	
	private void addTile(int gid) {
		int column = tileCount % columns;
		int row    = tileCount / columns;

		if (gid == 0) {
			tiles[row][column] = TMXTile.getEmptyTile(column, row);
		} else {
			TiledSprite sprite = tiledMap.getSpriteByGID(gid);
			if (sprite == null) return;
			tiles[row][column] = new TMXTile(gid, column, row, 
					sprite.getTileIndexX(), sprite.getTileIndexY(), 
					sprite.getWidth(), sprite.getHeight());
			
			this.width  = columns * sprite.getWidth();
			this.height = rows * sprite.getHeight();
		}
		
		this.tileCount++;
	}
	
	private int readGID(DataInputStream dataIn) throws IOException {
		int lowestByte = dataIn.read();
		int secondLowestByte  = dataIn.read();
		int secondHighestByte = dataIn.read();
		int highestByte = dataIn.read();

		if(lowestByte < 0 || secondLowestByte < 0 || secondHighestByte < 0 || highestByte < 0) {
			throw new IllegalArgumentException("Couldn't read gid from stream.");
		}

		return lowestByte | secondLowestByte <<  8 |secondHighestByte << 16 | highestByte << 24;
	}
	
	public void setup(Attributes atts) {
		this.addTile(SAXUtil.getInt(atts, "gid"));
	}

	@Override
	public void onResume() {
		ArrayList<TMXTileSet> tileSets = tiledMap.getTileSets();
		for (TMXTileSet tileSet : tileSets) {
			TiledSprite sprite = tileSet.getSprite();
			sprite.onResume();
		}
	}

	@Override
	public void onPause() {
		ArrayList<TMXTileSet> tileSets = tiledMap.getTileSets();
		for (TMXTileSet tileSet : tileSets) {
			TiledSprite sprite = tileSet.getSprite();
			sprite.onPause();
		}
	}

	@Override
	public void onDispose() {
		ArrayList<TMXTileSet> tileSets = tiledMap.getTileSets();
		for (TMXTileSet tileSet : tileSets) {
			TiledSprite sprite = tileSet.getSprite();
			sprite.onDispose();
		}
		tiledMap.onDispose();
		this.removed = true;
	}

	@Override
	public void onRemove() {
		ArrayList<TMXTileSet> tileSets = tiledMap.getTileSets();
		for (TMXTileSet tileSet : tileSets) {
			TiledSprite sprite = tileSet.getSprite();
			sprite.onRemove();
		}		
		tiledMap.onRemove();
		this.removed = true;
	}

	@Override
	public void onLoadEngine(E3Engine engine) {
		this.engine = engine;
		this.useVBO = engine.useVBO();
	}

	@Override
	public boolean isRemoved() {
		return this.removed;
	}

	@Override
	public boolean contains(int x, int y) {
		return false;
	}
	
	public int getTileWidth() {
		return tiledMap.getTileWidth();
	}
	
	public int getTileHeight() {
		return tiledMap.getTileHeight();
	}
	
	public boolean useLoop() {
		return this.useLoop;
	}
	
	public void loop(boolean useLoop) {
		this.useLoop = useLoop;
	}
	
	public void stopOnTheEdge(boolean stop) {
		this.stopOnTheEdge = stop;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public int getSceneWidth() {
		return this.sceneWidth;
	}
	
	public int getSceneHeight() {
		return this.sceneHeight;
	}
	
	public int getMaxX() {
		return this.width - this.sceneWidth;
	}
	
	public int getMaxY() {
		return this.height - this.sceneHeight;
	}
}
