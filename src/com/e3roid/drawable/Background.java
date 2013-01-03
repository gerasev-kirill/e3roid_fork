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
package com.e3roid.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import com.e3roid.drawable.texture.BitmapTexture;
import com.e3roid.drawable.texture.Texture;
import com.e3roid.drawable.texture.TiledTexture;

/**
 * Represents background with texture support.
 */
public class Background extends Sprite {
	/**
	 * Constructs tiled background with given tiled texture.
	 * @param texture TiledTexture
	 */
	public Background(TiledTexture texture) {
		super(texture, 0, 0, texture.getTileWidth(), texture.getTileHeight());
	}

	/**
	 * Constructs tiled background with given bitmap.
	 * @param tile Bitmap tile
	 * @param width width of the tile
	 * @param height height of the tile
	 * @param context Context
	 */
	public Background(Bitmap tile, int width, int height, Context context) {
		this(tile, width, height, BitmapTexture.TileOption.FILL, context);
	}
	
	/**
	 * Constructs tiled background with given bitmap.
	 * @param tile Bitmap tile
	 * @param width width of the tile
	 * @param height height of the tile
	 * @param tileOption BitmapTexture.TileOption
	 * @param context Context
	 */
	public Background(Bitmap tile, int width, int height, BitmapTexture.TileOption tileOption, Context context) {
		this(tile, width, height, BitmapTexture.TileOption.FILL, Texture.Option.DEFAULT, context);
	}
	
	/**
	 * Constructs tiled background with given bitmap.
	 * @param tile Bitmap tile
	 * @param width width of the tile
	 * @param height height of the tile
	 * @param tileOption BitmapTexture.TileOption
	 * @param textureOption Texture.Option
	 * @param context Context
	 */
	public Background(Bitmap tile, int width, int height, 
			BitmapTexture.TileOption tileOption, Texture.Option textureOption, Context context) {
		BitmapTexture texture = 
			BitmapTexture.createTextureFromTile(tile, width, height, tileOption, context);
		texture.recycleBitmap(false);
		texture.setOption(textureOption);
		setPosition(0, 0);
		setSize(width, height);
		useDefaultRotationAndScaleCenter();
		updateTexture(texture);
		createBuffers();
	}
	
	/**
	 * Returns whether the layer is collided with given x coordinate or not.
	 * 
	 * @param globalX global x coordinate
	 * @return whether x axis is collided or not.
	 */
	@Override
	public boolean contains(int x, int y) {
		return false;
	}
}
