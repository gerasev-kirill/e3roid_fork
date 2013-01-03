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

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.e3roid.drawable.texture.Texture;
import com.e3roid.opengl.FastFloatBuffer;
import com.e3roid.opengl.GLHelper;
import com.e3roid.util.Debug;

/**
 * A Sprite class is used to draw 2D rectangle shape with texture.
 */
public class Sprite extends Shape {

	protected Texture texture;
	protected final int[] GENERATED_TEXTURE_BUFFER_ID = new int[1];
	
	protected FastFloatBuffer coordBuffer;
		
	/**
	 *  Default constructor for subclass.
	 */
	protected Sprite() {
		
	}
	
	/**
	 * Constructs sprite with given texture.
	 * @param texture texture
	 */
	public Sprite(Texture texture) {
		this(texture, 0, 0);
	}
	
	/**
	 * Constructs sprite with given position.
	 * Texture width and height are set automatically.
	 */
	public Sprite(Texture texture, int x, int y) {
		this.texture = texture;
		setSize(texture.getWidth(), texture.getHeight());
		setPosition(x, y);
		useDefaultRotationAndScaleCenter();
		createBuffers();
	}
	
	/**
	 * Constructs sprite with given position and size.
	 * Texture width and height are set by parameters.
	 */
	public Sprite(Texture texture, int x, int y, int w, int h) {
		this.texture = texture;
		setSize(w, h);
		setPosition(x, y);
		useDefaultRotationAndScaleCenter();
		createBuffers();
	}

	/**
	 * Called when the sprite is created or recreated.
	 */
	@Override
	public void onLoadSurface(GL10 _gl, boolean force) {
		if (!force && isLoaded()) return;
		
		super.onLoadSurface(_gl, force);
		
		GL11 gl = (GL11)_gl;
		
		if (force && texture.isLoaded()) {
			texture.unloadTexture(gl);
		}
		
		if (!texture.isLoaded()) {
			texture.loadTexture(gl);
		}
		
		if (useVBO) {
			gl.glGenBuffers(1, GENERATED_TEXTURE_BUFFER_ID, 0);
		}
		loadTextureBuffer(gl, coordBuffer);
	}
	
	protected void callRootOnLoadSurface(GL10 gl, boolean force) {
		super.onLoadSurface(gl, force);
	}

	@Override
	protected void createBuffers() {
		super.createBuffers();
		final float[] SPRITE_COORDS = {
				texture.getCoordStartX(), texture.getCoordStartY(),
				texture.getCoordStartX(), texture.getCoordEndY(),
				texture.getCoordEndX(), texture.getCoordEndY(),
				texture.getCoordEndX(), texture.getCoordStartY()
				};

		coordBuffer = FastFloatBuffer.createBuffer(SPRITE_COORDS);
	}
	
	protected void loadTextureBuffer(GL11 gl) {
		loadTextureBuffer(gl, this.coordBuffer);
	}
	
	protected void loadTextureBuffer(GL11 gl, FastFloatBuffer coordBuffer) {
		if (useVBO) {
			GLHelper.bindBuffer(gl, GENERATED_TEXTURE_BUFFER_ID[0]);
			GLHelper.bufferFloatData(gl, coordBuffer.capacity(), coordBuffer, GL11.GL_STATIC_DRAW);
		}
		this.coordBuffer = coordBuffer;
	}
	
	/**
	 * Called to draw the sprite.
	 * This method is responsible for drawing the sprite. 
	 */
	@Override
	public void onDraw(GL10 _gl) {
		GL11 gl = (GL11)_gl;
		
		for(Shape child : children) {
			child.onDraw(_gl);
		}
		
		processModifiers(gl);
		
        if (isRemoved() && isLoaded()) {
        	unload(gl);
			return;
		}
			
		if (!isVisible() || isRemoved()) {
			return;
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.getTextureID());
		
	    gl.glLoadIdentity();
		gl.glPushMatrix();
		processBeforeModifiers(gl);
		GLHelper.setColor(gl, color[0], color[1], color[2], color[3]);
		applyParams(gl);
		if (useVBO) {			
			GLHelper.bindBuffer(gl, GENERATED_HARDWAREID[0]);
			GLHelper.vertexZeroPointer(gl);

			GLHelper.bindBuffer(gl, GENERATED_TEXTURE_BUFFER_ID[0]);
			GLHelper.texCoordZeroPointer(gl);
			
			GLHelper.bindElementBuffer(gl, GENERATED_HARDWAREID[1]);
			gl.glDrawElements(GL11.GL_TRIANGLE_FAN, RECTANGLE_POINTS, GL11.GL_UNSIGNED_SHORT, 0);			
		} else {
			GLHelper.vertexPointer(gl, vertexBuffer);
			GLHelper.texCoordPointer(gl, coordBuffer);
			gl.glDrawElements(GL11.GL_TRIANGLE_FAN, RECTANGLE_POINTS, GL11.GL_UNSIGNED_SHORT, indiceBuffer);
		}
		processAfterModifiers(gl);
		gl.glPopMatrix();
		
		GLHelper.checkError(gl);
		
		// unbind buffers
		GLHelper.bindBuffer(gl, 0);
		GLHelper.bindElementBuffer(gl, 0);
	}
	
	/**
	 * Called when this shape is removed.
	 */
	@Override
	public void onRemove() {
		super.onRemove();
		this.coordBuffer = null;
	}
		
	protected void unload(GL10 gl) {
		if (!texture.isReusable() && texture.isLoaded()) {
			texture.unloadTexture(gl);
			Debug.d(String.format("%s is unloaded.", texture.describe()));
		}
		setLoaded(false);
	}
	
	protected void updateTexture(Texture texture) {
		this.texture = texture;
	}
	
}
