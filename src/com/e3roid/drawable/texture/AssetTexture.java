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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import com.e3roid.opengl.GLHelper;
import com.e3roid.opengl.TGA;
import com.e3roid.opengl.TGA.ImageTGA;
import com.e3roid.util.BitmapUtil;
import com.e3roid.util.Debug;
import com.e3roid.util.MathUtil;

public class AssetTexture extends Texture {
	/**
	 * Extension for TGA formatted image.
	 */
	public static String tgaExtension = ".tga";

	private final String assetName;
	private boolean useTGA = false;
	private boolean flipped = false;
	private boolean useFullpath = false;
	
	/*
	 * Initialize texture.
	 * Bitmap width and height are set automatically.
	 */
	public AssetTexture(String name, Context context) {
		this(name, context, Option.DEFAULT);
	}
	
	public AssetTexture(String name, Context context, Texture.Option option) {
		super(context, option);
		this.assetName = name;
		
		// if image name starts with "/", image is assumed to use full path 
		if (name.startsWith("/")) {
			useFullpath = true;
		}
		
		// TGA formatted image file support
		if (name.toLowerCase().endsWith(tgaExtension)) {
			initializeTGA(name);
		} else {
			initializeBitmap(name);
		}
	}
	
	/**
	 * Constructs bitmap texture from asset
	 * @param assetName asset name
	 * @param context Context
	 * @param option Texture.Option
	 */
	private void initializeBitmap(String assetName) {
		InputStream is = null;
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inJustDecodeBounds = true;
		try {
			if (useFullpath) {
				is = new FileInputStream(assetName);
			} else {
				is = getContext().getAssets().open(assetName);
			}
			BitmapFactory.decodeStream(is, null, bitmapOptions);
		} catch (Exception e) {
			Debug.e("Failed to load Texture " + assetName, e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// do nothing
			}
		}
		setSize(bitmapOptions.outWidth, bitmapOptions.outHeight);
		setGLSize(MathUtil.nextPowerOfTwo(getWidth()), MathUtil.nextPowerOfTwo(getHeight()));
	}
	
	/**
	 * Constructs TGA texture from asset
	 * @param assetName asset name
	 * @param context Context
	 * @param option Texture.Option
	 */
	private void initializeTGA(String assetName) {
		
		this.useTGA  = true;
		this.flipped = true;
		
		ImageTGA image = null;
		InputStream is = null;
		
		try {
			if (useFullpath) {
				is = new FileInputStream(assetName);
			} else {
				is = getContext().getAssets().open(assetName);
			}
			image = TGA.inJustDecodeBounds(is);
		} catch (Exception e) {
			Debug.e("Failed to load TGA texture " + assetName, e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// do nothing
			}
		}
		
		if (image == null) {
			throw new IllegalArgumentException("Failed to load TGA texture: " + assetName);
		}
				
		setSize(image.width, image.height);
		setGLSize(MathUtil.nextPowerOfTwo(getWidth()), MathUtil.nextPowerOfTwo(getHeight()));
	}
	
	/*
	 * Initialize texture.
	 * Bitmap width and height are set by parameters.
	 * This is faster than using auto detection.
	 */
	public AssetTexture(String name, int width, int height, Context context) {
		this(name, width, height, context, Option.DEFAULT);
	}
	
	public AssetTexture(String name, int width, int height, Context context, Texture.Option option) {
		super(width, height, context, option);
		this.assetName = name;
		
		// TGA formatted image file support
		if (name.toLowerCase().endsWith(tgaExtension)) {
			this.useTGA  = true;
			this.flipped = true;
		}
	}

	@Override
	public void loadTexture(GL10 gl, boolean reload) {
		if (useTGA) {
			loadTGATexture(gl, reload);
		} else {
			super.loadTexture(gl, reload);
		}
	}
	
	/**
	 * load TGA texture
	 */
	private void loadTGATexture(GL10 gl, boolean reload) {
		
		ImageTGA image = null;
		InputStream is = null;
		try {
			if (useFullpath) {
				is = new FileInputStream(assetName);
			} else {
				is = getContext().getAssets().open(assetName);
			}
			image = TGA.load(is);
		} catch (Exception e) {
			Debug.e("Failed to load Texture " + assetName, e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// do nothing
			}
		}

		if (image == null) {
			throw new IllegalArgumentException("Failed to load TGA info: " + assetName);
		}
		
		if (!reload) {
			setTextureID(generateTextureID(gl));
		}
		gl.glBindTexture(GL10.GL_TEXTURE_2D, getTextureID());
		applyOptions(gl);
		
		int format = image.pixelDepth == 32 ? GL10.GL_RGBA : GL10.GL_RGB;
		
		Bitmap holder = null;
		if (format == GL10.GL_RGB) {
			holder = Bitmap.createBitmap(getGLWidth(), getGLHeight(), Bitmap.Config.RGB_565);
		} else {
			holder = Bitmap.createBitmap(getGLWidth(), getGLHeight(), Bitmap.Config.ARGB_8888);
		}
		
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, holder, 0);
		holder.recycle();
		
		gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 
				0, 0, image.width, image.height, format, GL10.GL_UNSIGNED_BYTE, ByteBuffer.wrap(image.imageData));
		
		GLHelper.checkError(gl);
		
		setLoaded(true);
	}
	
	/**
	 * Returns first coordinates Y
	 */
	@Override
	public float getCoordStartY() {
		if (!flipped) {
			return super.getCoordStartY();
		} else {
			return super.getCoordEndY();
		}
	}
	
	/**
	 * Returns last coordinates Y
	 */
	@Override
	public float getCoordEndY() {
		if (!flipped) {
			return super.getCoordEndY();
		} else {
			return super.getCoordStartY();
		}
	}
	
	public String getName() {
		return this.assetName;
	}
	
	@Override
	protected Bitmap loadBitmap() {
		if (useTGA) {
			return null;
		} else {
			return BitmapUtil.getBitmapFromAsset(assetName, getContext());
		}
	}

	@Override
	public String describe() {
		return "AssetTexture: " + this.assetName;
	}
	
	/**
	 * flip the image.
	 */
	public void flip() {
		this.flipped = !this.flipped;
	}
	
	/**
	 * Returns this texture is flipped or not.
	 * @return
	 */
	public boolean isFlipped() {
		return this.flipped;
	}
}
