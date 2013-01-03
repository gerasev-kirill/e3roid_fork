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

import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.content.Context;

/**
 * Represents texture created by bitmap.
 */
public class BitmapTexture extends Texture {

	private Bitmap bitmap;
	
	public BitmapTexture(Bitmap bitmap, int width, int height, Context context) {
		this(bitmap, width, height, context, Option.DEFAULT);
	}
	public BitmapTexture(Bitmap bitmap, int width, int height, Context context,
			Option option) {
		super(width, height, context, option);
		this.bitmap = bitmap;
	}
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	public Bitmap getBitmap() {
		return this.bitmap;
	}
	
	public void recycleBitmap() {
		this.bitmap.recycle();
	}
	
	@Override
	protected Bitmap loadBitmap() {
		return bitmap;
	}
	
	@Override
	public String describe() {
		return "BitmapTexture: " + bitmap.toString();
	}
	
	public static BitmapTexture createTextureFromTile(Bitmap tile, 
			int width, int height, Context context) {
		return createTextureFromTile(tile, width, height, TileOption.FILL, context);
	}
	public static BitmapTexture createTextureFromTile(Bitmap tile, 
			int width, int height, TileOption option, Context context) {
		
		int tileWidth  = tile.getWidth();
		int tileHeight = tile.getHeight();
		int xcount = width  / tileWidth;
		int ycount = height / tileHeight;
		
		// If TileOption equals FILL, extra tile will be added.
		if (option == TileOption.FILL) {
			if (width % tileWidth != 0) {
				xcount = xcount + 1;
			}
			if (height % tileHeight != 0) {
				ycount = ycount + 1;
			}
		// If TileOption equals STRETCH, tile will be stretched. 
		} else if (option == TileOption.STRETCH) {
			width  = tileWidth  * xcount;
			height = tileHeight * ycount;
		}
		
		Bitmap holder = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		
		Canvas canvas = new Canvas(holder);
		Rect src = new Rect(0, 0, tileWidth, tileHeight);
		for (int i = 0; i < xcount; i++) {
			for (int j = 0; j < ycount; j++) {
				int startX = i * tileWidth;
				int startY = j * tileHeight;
				Rect dst = new Rect(startX, startY, startX + tileWidth, startY + tileHeight);
				canvas.drawBitmap(tile, src, dst, null);
			}
		}
		
		return new BitmapTexture(holder, width, height, context);
	}
	
	public static enum TileOption {
		FILL, STRETCH, DEFAULT
	}
}
