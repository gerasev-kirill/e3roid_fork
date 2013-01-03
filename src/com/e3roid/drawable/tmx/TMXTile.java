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

import android.graphics.Rect;

public class TMXTile {

	private int gid;
	private int atlasColumn;
	private int atlasRow;
	private final int column;
	private final int row;
	private final int width;
	private final int height;
	
	public TMXTile(int gid, int column, int row, 
			int atlasColumn, int atlasRow, int width, int height) {
		this.gid = gid;
		this.column = column;
		this.row    = row;
		this.atlasColumn = atlasColumn;
		this.atlasRow    = atlasRow;
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public static TMXTile getEmptyTile(int column, int row) {
		return new TMXTile(0, column, row, -1, -1, 0, 0);
	}
	
	public static boolean isEmpty(TMXTile tile) {
		if (tile == null) return true;
		return tile.getGID() == 0;
	}
	
	public int getGID() {
		return this.gid;
	}
	
	public int getColumn() {
		return this.column;
	}
	
	public int getRow() {
		return this.row;
	}
	
	public int getAtlasColumn() {
		return this.atlasColumn;
	}
	
	public int getAtlasRow() {
		return this.atlasRow;
	}
	
	public Rect getRect() {
		int left   = getColumn() * getWidth();
		int right  = left + getWidth();
		int top    = getRow() * getHeight();
		int bottom = top + getHeight();
		
		return new Rect(left, right, top, bottom);
	}
	
	public void setGID(int gid) {
		this.gid = gid;
	}
	
	public void setAtlas(int column, int row) {
		this.atlasColumn = column;
		this.atlasRow = row;
	}
	
	public void setEmpty() {
		this.gid = 0;
	}
}
