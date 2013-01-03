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

import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLUtils;

import com.e3roid.opengl.GLHelper;
import com.e3roid.util.MathUtil;

public abstract class Texture {

	private final Context context;
	private final int[] GENERATED_TEXTUREID = new int[1];
	
	private int width;
	private int height;
	
	// OpenGL texture width and height must be power of two.
	// (i.e. 32, 64, 128, 256, 512, 1024)
	private int glWidth;
	private int glHeight;
	
	private int textureID = -1;
	private Option option = Option.DEFAULT;

	private boolean loaded   = false;
	private boolean reusable = false;
	private boolean recycleBitmap = true;
	
	public Texture(Context context) {
		this(context, Option.DEFAULT);
	}
	public Texture(Context context, Texture.Option option) {
		this.context = context;
		this.option = option;
	}
	public Texture(int width, int height, Context context) {
		this(width, height, context, Option.DEFAULT);
	}
	public Texture(int width, int height, Context context, Texture.Option option) {
		this.context = context;
		this.option = option;
		this.width  = width;
		this.height = height;
		this.glWidth  = MathUtil.nextPowerOfTwo(width);
		this.glHeight = MathUtil.nextPowerOfTwo(height);
	}
	
	protected abstract Bitmap loadBitmap();
	public abstract String describe();

	public void loadTexture(GL10 gl) {
		loadTexture(gl, false);
	}
	
	public void loadTexture(GL10 gl, boolean reload) {
		if (!reload) {
			this.textureID = generateTextureID(gl);
		}
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
		applyOptions(gl);
		
		final Bitmap holder = Bitmap.createBitmap(glWidth, glHeight, Bitmap.Config.ARGB_8888);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, holder, 0);
		holder.recycle();
		
		Bitmap bitmap = loadBitmap();
		if (bitmap == null) return;

		try {
			GLHelper.texSubImage2D(gl, GL10.GL_TEXTURE_2D, 0, 
					0, 0, bitmap, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE);		
			this.loaded = true;
		} finally {
			if (recycleBitmap) {
				bitmap.recycle();
			}
		}
	}
		
	public void unloadTexture(GL10 gl) {
		GLHelper.deleteTexture(gl, this.textureID);
		this.textureID = -1;
		this.loaded = false;
	}
	
	protected int generateTextureID(final GL10 gl) {
		if (textureID > 0) {
			GLHelper.deleteTexture(gl, this.textureID);
			this.textureID = -1;
		}
		gl.glGenTextures(1, GENERATED_TEXTUREID, 0);
		return GENERATED_TEXTUREID[0];
	}
	
	protected void applyOptions(GL10 gl) {
		int minFilter = GL10.GL_NEAREST;
		int magFilter = GL10.GL_NEAREST;
		int wrapS     = GL10.GL_CLAMP_TO_EDGE;
		int wrapT     = GL10.GL_CLAMP_TO_EDGE;
		int texEnv    = GL10.GL_MODULATE;
		
		switch(option) {
		case DEFAULT:
			break;
		case BILINEAR:
			minFilter = GL10.GL_LINEAR;
			magFilter = GL10.GL_LINEAR;
			wrapS     = GL10.GL_CLAMP_TO_EDGE;
			wrapT     = GL10.GL_CLAMP_TO_EDGE;
			texEnv    = GL10.GL_REPLACE;
			break;
		case REPEATING:
			minFilter = GL10.GL_NEAREST;
			magFilter = GL10.GL_NEAREST;
			wrapS     = GL10.GL_REPEAT;
			wrapT     = GL10.GL_REPEAT;
			texEnv    = GL10.GL_REPLACE;
			break;
		case REPEATING_BILINEAR:
			minFilter = GL10.GL_LINEAR;
			magFilter = GL10.GL_LINEAR;
			wrapS     = GL10.GL_REPEAT;
			wrapT     = GL10.GL_REPEAT;
			texEnv    = GL10.GL_REPLACE;
			break;
		}
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, minFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, magFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, wrapS);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, wrapT);
		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, texEnv);
		
		GLHelper.checkError(gl);
	}
	
	public boolean isLoaded() {
		return this.loaded;
	}
	
	protected void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	
	protected void setSize(int w, int h) {
		this.width  = w;
		this.height = h;
	}
	
	protected void setGLSize(int w, int h) {
		this.glWidth  = w;
		this.glHeight = h;
	}
	
	public void recycleBitmap(boolean recycle) {
		this.recycleBitmap = recycle;
	}
	
	public boolean isRecycleBitmap() {
		return this.recycleBitmap;
	}
	
	public void setReusable(boolean reusable) {
		this.reusable = reusable;
	}
	
	public boolean isReusable() {
		return this.reusable;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public int getGLWidth() {
		return this.glWidth;
	}
	
	public int getGLHeight() {
		return this.glHeight;
	}
	
	public float getCoordStartX() {
		return 0;
	}
	public float getCoordStartY() {
		return 0;
	}
	public float getCoordEndX() {
		return (float)((float)width / (float)glWidth);
	}
	public float getCoordEndY() {
		return (float)((float)height / (float)glHeight);
	}
	
	public int getTextureID() {
		return this.textureID;
	}
	
	protected void setTextureID(int id) {
		this.textureID = id;
	}
	
	public void setOption(Option option) {
		this.option = option;
	}
	
	public static enum Option {
		DEFAULT, BILINEAR, REPEATING, REPEATING_BILINEAR;
	}
	
	protected Context getContext() {
		return this.context;
	}	
}
