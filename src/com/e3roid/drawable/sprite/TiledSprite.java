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
package com.e3roid.drawable.sprite;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.texture.TiledTexture;
import com.e3roid.opengl.FastFloatBuffer;
import com.e3roid.opengl.GLHelper;
import com.e3roid.util.IntPair;

/**
 * Represents tiled sprite that can use with sprite sheets.
 * TiledSprite needs VBO enabled.
 */
public class TiledSprite extends Sprite {
	
	private final TiledTexture texture;
	private boolean tileIndexChanged = false;
	private int xindex = 0;
	private int yindex = 0;
	private HashMap<IntPair, FastFloatBuffer> bufferCache = new HashMap<IntPair, FastFloatBuffer>();
	private final int[] GENERATED_TILED_ID = new int[1];

	/**
	 * Constructs titled sprite with given texture and position.
	 * @param texture texture
	 * @param x initial x coordinate 
	 * @param y initial y coordinate
	 */
	public TiledSprite(TiledTexture texture, int x, int y) {
		this(texture, x, y, 0, 0);
	}
	
	/**
	 * Constructs tiled sprite with given texture, position and tile index.
	 * @param texture texture
	 * @param x initial x coordinate
	 * @param y initial y coordinate
	 * @param xindex tile index x
	 * @param yindex tile index y
	 */
	public TiledSprite(TiledTexture texture, int x, int y, int xindex, int yindex) {
		super.texture = texture;
		this.texture = texture;
		setSize(texture.getTileWidth(), texture.getTileHeight());
		setPosition(x, y);
		setTile(xindex, yindex);
		useDefaultRotationAndScaleCenter();
		createBuffers();
	}

	/**
	 * Set tile index
	 * @param xindex tile index x
	 * @param yindex tile index y
	 */
	public void setTile(int xindex, int yindex) {
		if (this.xindex != xindex || this.yindex != yindex) {
			tileIndexChanged = true;
		}
		this.xindex = xindex;
		this.yindex = yindex;
	}
	
	/**
	 * Returns tile index x
	 * @return tile index x
	 */
	public int getTileIndexX() {
		return xindex;
	}
	
	/**
	 * Returns tile index y
	 * @return tile index y
	 */
	public int getTileIndexY() {
		return yindex;
	}

	/**
	 * Called when the sprite is created or recreated.
	 */
	@Override
	public void onLoadSurface(GL10 _gl, boolean force) {
		if (!force && isLoaded()) return;
		
		super.callRootOnLoadSurface(_gl, force);
		
		GL11 gl = (GL11)_gl;
		
		if (force && texture.isLoaded()) {
			texture.unloadTexture(gl);
		}
		
		if (!texture.isLoaded()) {
			texture.loadTexture(gl);
		}
		
		if (force) {
			bufferCache.clear();
			tileIndexChanged = true;
		}
		
		if (useVBO) {
			gl.glGenBuffers(1, GENERATED_TEXTURE_BUFFER_ID, 0);
		}
		
		loadTextureBuffer(gl, coordBuffer);
	}
	
    /**
     * Called to draw the sprite.
     * This method is responsible for drawing the sprite. 
     */
    @Override
    public void onDraw(GL10 _gl) {
            GL11 gl = (GL11)_gl;
            if (tileIndexChanged) {
                    loadTextureBuffer(gl, reloadTile(gl));
                    tileIndexChanged = false;
            }
            GENERATED_TEXTURE_BUFFER_ID[0] = coordBuffer.getBufferID();
            super.onDraw(gl);
    }
    
	/**
	 * Called to draw the sprite.
	 * This method is responsible for drawing the sprite. 
	 * This disables rotate, scale, child shapes, colors, and modifiers for faster drawing.
	 */
	public void onFasterDraw(GL10 _gl) {
		GL11 gl = (GL11)_gl;
		
		if (tileIndexChanged) {
			loadTextureBuffer(gl, reloadTile(gl));
			tileIndexChanged = false;
		}
        if (isRemoved() && isLoaded()) {
        	unload(gl);
			return;
		}
			
		if (!isVisible() || isRemoved()) {
			return;
		}

		GLHelper.bindTexture(gl, texture.getTextureID());
		
	    gl.glLoadIdentity();
		gl.glTranslatef(translateParams[0], translateParams[1], translateParams[2]);
		GLHelper.bindBuffer(gl, GENERATED_HARDWAREID[0]);
		GLHelper.vertexZeroPointer(gl);

		GLHelper.bindBuffer(gl, coordBuffer.getBufferID());
		GLHelper.texCoordZeroPointer(gl);

		GLHelper.bindElementBuffer(gl, GENERATED_HARDWAREID[1]);
		gl.glDrawElements(GL11.GL_TRIANGLE_FAN, RECTANGLE_POINTS, GL11.GL_UNSIGNED_SHORT, 0);			
	}
		
	@Override
	protected void loadTextureBuffer(GL11 gl, FastFloatBuffer coordBuffer) {
		if (!coordBuffer.isLoaded()) {
			GLHelper.bindBuffer(gl, coordBuffer.getBufferID());
			GLHelper.bufferFloatData(gl, coordBuffer.capacity(), coordBuffer, GL11.GL_STATIC_DRAW);
			coordBuffer.setLoaded(true);
		}
		this.coordBuffer = coordBuffer;
	}
	
	/**
	 * Reloads tile coordinate buffers
	 * @return
	 */
	public FastFloatBuffer reloadTile(GL11 gl) {
		texture.setTileIndex(xindex, yindex);
		IntPair key = new IntPair(xindex, yindex);
		if (bufferCache.containsKey(key)) {
			return bufferCache.get(key);
		}
		float[] coords = {
				texture.getCoordStartX(), texture.getCoordStartY(),
				texture.getCoordStartX(), texture.getCoordEndY(),
				texture.getCoordEndX(), texture.getCoordEndY(),
				texture.getCoordEndX(), texture.getCoordStartY()
			};
		FastFloatBuffer data =  FastFloatBuffer.createBuffer(coords);
		data.setBufferID(generateTextureID(gl));
		bufferCache.put(key, data);
		return data;
	}
	
	protected int generateTextureID(GL11 gl) {
		gl.glGenBuffers(1, GENERATED_TILED_ID, 0);
		return GENERATED_TILED_ID[0];
	}
}
