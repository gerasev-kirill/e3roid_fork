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
package com.e3roid.script.lua;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import org.luaj.vm2.LuaError;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.e3roid.E3Activity;
import com.e3roid.E3Engine;
import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.texture.BitmapTexture;

public class LuaScriptCanvas extends Sprite {
	
	private final LuaScriptEngine luaEngine;
	private final E3Activity context;
	private final boolean supportsOnLoadEngine;
	
	private Canvas canvas;
	private Bitmap bitmap;
	private BitmapTexture texture;
	private boolean sizeChanged = false;
	
	public LuaScriptCanvas(LuaScriptEngine engine, E3Activity context) {
		this(engine, 0, 0, context);
	}
	
	public LuaScriptCanvas(LuaScriptEngine engine, int x, int y, E3Activity context) {
		this(engine, x, y, context.getWidth(), context.getHeight(), context);
	}
	
	public LuaScriptCanvas(LuaScriptEngine engine, int x, int y, int width, int height, E3Activity context) {
		
		this.context   = context;
		this.luaEngine = engine;
		
		this.supportsOnLoadEngine = engine.has("onLoadEngine");
		
		if (!engine.has("onDraw")) {
			throw new LuaError("function onDraw(GL10) must be defined in Lua script.");
		}
		
		setPosition(x, y);
		setSize(width, height);
		createTexture();
		createBuffers();
	}
	
	protected void createTexture() {
		if (sizeChanged) {
			bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
			texture = new BitmapTexture(bitmap, getWidth(), getHeight(), context);
			texture.recycleBitmap(false);
			canvas = new Canvas(bitmap);
		} else {
			texture.setBitmap(bitmap);
		}
		updateTexture(texture);
	}

	@Override
	public void onLoadEngine(E3Engine engine) {
		if (supportsOnLoadEngine) {
			luaEngine.call("onLoadEngine", this, context);
		}
		super.onLoadEngine(engine);
	}
	
	@Override
	public void onDraw(GL10 gl) {
		if (luaEngine.call("onDraw", this, context)) {
			createTexture();
			texture.loadTexture(gl, true);
			if (sizeChanged) {
				loadVertexBuffer((GL11)gl);
				loadTextureBuffer((GL11)gl);
				sizeChanged = false;
			}
		}
		super.onDraw(gl);
	}
	
	@Override
	public void onDispose() {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		this.sizeChanged = true;
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
}
