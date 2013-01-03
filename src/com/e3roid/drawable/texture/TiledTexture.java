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
package com.e3roid.drawable.texture;


import android.content.Context;

public class TiledTexture extends AssetTexture {
	private int xindex;
	private int yindex;
	private final int tileWidth;
	private final int tileHeight;
	private final int border;
	private final int margin;
	
	public TiledTexture(String name, int width, int height, Context context) {
		this(name, width, height, 0, 0, 0, 0, context, Option.DEFAULT);
	}
	public TiledTexture(String name, int width, int height, int xindex, int yindex, int border, Context context) {
		this(name, width, height, xindex, yindex, border, border, context, Option.DEFAULT);
	}
	public TiledTexture(String name, int width, int height, int xindex, int yindex, int border, int margin, Context context) {
		this(name, width, height, xindex, yindex, border, margin, context, Option.DEFAULT);
	}
	public TiledTexture(String name, int width, int height,
			int xindex, int yindex, int border, Context context, Texture.Option option) {
		this(name, width, height, xindex, yindex, border, border, context, option);
	}
	public TiledTexture(String name, int width, int height,
			int xindex, int yindex, int border, int margin, Context context, Texture.Option option) {
		super(name, context, option);
		this.xindex = xindex;
		this.yindex = yindex;
		this.tileWidth = width;
		this.tileHeight = height;
		this.border = border;
		this.margin = margin;
	}
	
	public void setTileIndex(int xindex, int yindex) {
		this.xindex = xindex;
		this.yindex = yindex;
	}

	@Override
	public float getCoordStartX() {
		float start = margin + ((tileWidth + border) * xindex);
		return start / getGLWidth();
	}
	@Override
	public float getCoordStartY() {
		float start = margin + ((tileHeight + border) * yindex);
		if (isFlipped()) {
			start = getHeight() - start;
		}
		return start / getGLHeight();
	}
	@Override
	public float getCoordEndX() {
		float start = margin + ((tileWidth + border) * xindex) + tileWidth;
		return start / getGLWidth();
	}
	
	@Override
	public float getCoordEndY() {
		float start = margin + ((tileHeight + border) * yindex) + tileHeight;
		if (isFlipped()) {
			start = getHeight() - start;
		}
		return start / getGLHeight();
	}
	
	public int getTileWidth() {
		return this.tileWidth;
	}
	
	public int getTileHeight() {
		return this.tileHeight;
	}
	
	public int getTileIndexX() {
		return this.xindex;
	}
	
	public int getTileIndexY() {
		return this.yindex;
	}
	
	public int getColumnCount() {
		return getWidth() / (tileWidth + border);
	}
	
	public int getRowCount() {
		return getHeight() / (tileHeight + border);
	}
	
	@Override
	public String describe() {
		return "TiledTexture: " + this.getName();
	}
}
