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
import java.util.ArrayList;
import java.util.List;

import android.view.MotionEvent;

import com.e3roid.E3Engine;
import com.e3roid.E3Scene;
import com.e3roid.event.SceneEventListener;
import com.e3roid.opengl.GLHelper;

/**
 * A drawable class that represents layer of the scene.
 */
public class Layer implements Drawable, SceneEventListener {

	private ArrayList<Drawable> drawables = new ArrayList<Drawable>();
	private ArrayList<Drawable> removedDrawables = new ArrayList<Drawable>();
	private ArrayList<Drawable> loadableDrawables = new ArrayList<Drawable>();
	private boolean removed = false;
	private boolean loaded  = false;
	private E3Engine engine;
	
	private int[] translate = {
		0, 0, 0	
	};
	
	/**
	 * Called to draw the layer.
	 * This method is responsible for drawing the layer. 
	 */
	@Override
	public void onDraw(GL10 gl) {
		// initialize drawables
		if (!loadableDrawables.isEmpty()) {
			for (Drawable drawable : loadableDrawables) {
				drawable.onLoadEngine(engine);
				drawable.onLoadSurface(gl);
				if (drawable instanceof Background) {
					if (drawables.size() > 0 && drawables.get(0) instanceof Background) {
						if (drawables.get(0).equals(drawable)) {
							((Background)drawable).show();
						} else {
							if (drawables.contains(drawable)) {
								((Background)drawable).show();
							} else {
								((Background)drawables.get(0)).hide();
								this.drawables.add(0, drawable);
							}
						}
					} else {
						this.drawables.add(0, drawable);
					}
				} else {
					this.drawables.add(drawable);
				}
			}
			loadableDrawables.clear();
		}
		
		// Called when onResume() after onPause()
		if (!isLoaded()) {
			for (Drawable drawable : drawables) {
				drawable.onLoadSurface(gl, true);
			}
			setLoaded(true);
		}
		
		// Layer translation
        GLHelper.switchToProjectionMatrix(gl);
		gl.glTranslatef(translate[0], translate[1], translate[2]);
		GLHelper.switchToModelViewMatrix(gl);
		
		// draw drawables
		for (Drawable drawable : drawables) {
			drawable.onDraw(gl);
		}
		
		// remove drawables
		if (!removedDrawables.isEmpty()) {
			for (Drawable drawable : removedDrawables) {
				drawables.remove(drawable);
				drawable.onDispose();
			}
			removedDrawables.clear();
		}
	}
	
	/**
	 * Returns x position of the layer
	 * @return x position of the layer
	 */
	public int getX() {
		return translate[0];
	}
	
	/**
	 * Returns y position of the layer
	 * @return y position of the layer
	 */
	public int getY() {
		return translate[1];
	}
	
	/**
	 * Returns z position of the layer
	 * @return z position of the layer
	 */
	public int getZ() {
		return translate[2];
	}
	
	/**
	 * Move to given X
	 * 
	 * @param x x position of the scene
	 */
	public void moveX(int x) {
		this.translate[0] = x;
	}

	/**
	 * Move to given Y
	 * 
	 * @param y y position of the scene
	 */
	public void moveY(int y) {
		this.translate[1] = y;
	}
	
	/**
	 * Move to given Z. This operation has no effects on the orthogonal scene.
	 * 
	 * @param z z position of the scene.
	 */
	public void moveZ(int z) {
		this.translate[2] = z;
	}
	
	/**
	 * Reset current position of the layer
	 */
	public void moveReset() {
		this.translate[0] = 0;
		this.translate[1] = 0;
		this.translate[2] = 0;
	}
	
	/**
	 * Translate with given parameters
	 */
	public void translate(int x, int y, int z) {
		this.translate[0] = x;
		this.translate[1] = y;
		this.translate[2] = z;
	}
	
	/**
	 * Set background for this layer
	 * @param drawable Background
	 */
	public void setBackground(Background drawable) {
		loadableDrawables.add(0, drawable);
	}

	/**
	 * Add drawable to this layer
	 * @param drawable Drawable
	 */
	public void add(Drawable drawable) {
		if (drawables.contains(drawable)) {
			return;
		}
		this.loadableDrawables.add(drawable);
	}
	
	/**
	 * Remove drawable from this layer
	 * @param drawable Drawable
	 */
	public void remove(Drawable drawable) {
		drawable.onRemove();
		removedDrawables.add(drawable);
	}
	
	/**
	 * Called when the layer is resumed.
	 * This method has no relation to Activity#onResume().
	 */
	@Override
	public void onResume() {
		for (Drawable drawable : drawables) {
			drawable.onResume();
		}
	}

	/**
	 * Called when the layer is paused.
	 * This method has no relation to Activity#onPause().
	 */
	@Override
	public void onPause() {
		for (Drawable drawable : drawables) {
			drawable.onPause();
		}		
	}

	/**
	 * Called when the layer is created or recreated.
	 * @param gl GL object
	 */
	@Override
	public void onLoadSurface(GL10 gl) {
		onLoadSurface(gl, false);
	}
	
	/**
	 * Called when the layer is created or recreated.
	 * @param gl GL object
	 * @param force force load when already loaded
	 */
	@Override
	public void onLoadSurface(GL10 gl, boolean force) {
		loaded = true;
	}
	
	/**
	 * Called when the layer is disposed.
	 */
	@Override
	public void onDispose() {
		for (Drawable drawable : drawables) {
			drawable.onDispose();
		}
	}

	/**
	 * Called when e3roid engine has been loaded.
	 */
	@Override
	public void onLoadEngine(E3Engine engine) {
		this.engine = engine;
	}

	/**
	 * Called when the layer is removed.
	 */
	@Override
	public void onRemove() {
		for (Drawable drawable : drawables) {
			remove(drawable);
		}
		this.removed = true;
	}
	
	/**
	 * Returns whether the layer is removed not not.
	 */
	@Override
	public boolean isRemoved() {
		return this.removed;
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
	
	/**
	 * Returns whether the layer is loaded or not.
	 */
	public boolean isLoaded() {
		return this.loaded;
	}
	
	/**
	 * Set the layer is loaded or not
	 * @param loaded
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	
	/**
	 * Returns whether the engine is loaded or not
	 */
	public boolean isEngineLoaded() {
		return this.engine != null;
	}

	/*
	 * Returns drawable children
	 */
	public List<Drawable> getDrawables() {
		return drawables;
	}
	
	/*
	 * Sets drawable children
	 */
	public void setDrawables(ArrayList<Drawable> drawables) {
		this.drawables = drawables;
	}
	
	/**
	 * Returns first drawable with given position.
	 * @param x x position 
	 * @param y y position
	 * @return Drawable returns null if no drawables are found
	 */
	public Drawable findDrawableAt(int x, int y) {
		for (Drawable drawable : drawables) {
			if (drawable.contains(x, y)) {
				return drawable;
			}
		}
		return null;
	}
	
	/**
	 * Returns all drawables at given coordinates.
	 * 
	 * @param x The x position within the scene
	 * @param y The y position within the scene.
	 * @return Drawable objects if exist, empty list otherwise.
	 */
	public List<Drawable> findDrawablesAt(int x, int y) {
		ArrayList<Drawable> drawables = new ArrayList<Drawable>();
		for (Drawable drawable : drawables) {
			if (drawable.contains(x, y)) {
				drawables.add(drawable);
			}
		}
		return drawables;
	}

	/**
	 * Returns number of drawables in the layer
	 * @return number of drawables in the layer
	 */
	public int size() {
		return drawables.size();
	}

	/**
	 * Default Scene touch event.
	 * Layer will not react the scene touch event by default.
	 */
	@Override
	public boolean onSceneTouchEvent(E3Scene scene, MotionEvent motionEvent) {
		return false;
	}
}
