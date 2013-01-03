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
package com.e3roid.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;

/**
 * A utility class for creating Bitmap.
 */
public class BitmapUtil {
	public static Bitmap getBitmapFromAsset(String name, Context context) {
		InputStream is = null;
		Bitmap bitmap  = null;
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inPreferredConfig = Config.ARGB_8888;
		
		try {
			if (name.startsWith("/")) {
				is = new FileInputStream(name);
			} else {
				is = context.getAssets().open(name);
			}
			bitmap = BitmapFactory.decodeStream(is, null, bitmapOptions);
		} catch (Exception e) {
			Debug.e("Failed to load bitmap " + name, e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// nothing to do
				}
			}
		}
		return bitmap;
	}
	public static Bitmap getBitmapFromResource(int resourceID, int width, int height, Context context) {
		Drawable drawable = context.getResources().getDrawable(resourceID);
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

	public static Bitmap getTileBitmapFromAsset(String assetName, 
			int tileWidth, int tileHeight, int xindex, int yindex, int border, Context context) {
		return getTileBitmap(getBitmapFromAsset(assetName, context),
				tileWidth, tileHeight, xindex, yindex, border, context);
	}
	
	public static Bitmap getTileBitmap(Bitmap bitmap, 
			int tileWidth, int tileHeight, int xindex, int yindex, int border, Context context) {		
		int xstart = border + ((tileWidth + border) * xindex);
		int ystart = border + ((tileHeight + border) * yindex);

		return Bitmap.createBitmap(bitmap, xstart, ystart, tileWidth, tileHeight);
	}
}
