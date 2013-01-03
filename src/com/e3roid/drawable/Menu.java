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

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import com.e3roid.E3Activity;
import com.e3roid.E3Engine;

/**
 * A drawable class that represents menu of the scene.
 */
public class Menu implements Drawable {

	private ArrayList<Sprite> menuItems = new ArrayList<Sprite>();
	private ArrayList<Sprite> removedItems = new ArrayList<Sprite>();
	private ArrayList<Sprite> loadableItems = new ArrayList<Sprite>();
	
	private E3Engine engine;
	private boolean removed = false;
	private int totalHeight = 0;
	private int totalWidth  = 0;

	/**
	 * Add sprite as menu item to the menu
	 * @param menuItem Sprite
	 */
	public void add(Sprite menuItem) {
		loadableItems.add(menuItem);
		totalHeight += menuItem.getHeight();
		totalWidth  += menuItem.getWidth();
	}

	/**
	 * Remove menu item from the menu
	 * @param menuItem Sprite
	 */
	public void remove(Sprite menuItem) {
		removedItems.add(menuItem);
		totalHeight -= menuItem.getHeight();
		totalWidth  -= menuItem.getWidth();
	}
	
	/**
	 * Move menu to the vertical center
	 * @param context E3Activity
	 */
	public void layoutVerticalCenter(E3Activity context) {
		int startY = (context.getHeight() - totalHeight) / 2;	
		
		startY = moveVerticalCenter(menuItems, startY, context);
		startY = moveVerticalCenter(loadableItems, startY, context);
	}
	
	private int moveVerticalCenter(ArrayList<Sprite> items, int startY, E3Activity context) {
		for (Sprite menuItem : items) {
			menuItem.move(
					(context.getWidth() - menuItem.getWidth()) / 2,
					startY);
			startY += menuItem.getHeight();
		}
		return startY;
	}
	
	/**
	 * Move menu to the horizontal center.
	 * @param context E3Activity
	 */
	public void layoutHorizontalCenter(E3Activity context) {
		int startX = (context.getWidth() - totalWidth) / 2;	
		
		startX = moveHorizontalCenter(menuItems, startX, context);
		startX = moveHorizontalCenter(loadableItems, startX, context);
	}

	private int moveHorizontalCenter(ArrayList<Sprite> items, int startX, E3Activity context) {
		for (Sprite menuItem : items) {
			menuItem.move(
					startX,
					(context.getHeight() - menuItem.getHeight()) / 2);
			startX += menuItem.getWidth();
		}
		return startX;
	}
	
	/**
	 * Called when the parent layer is resumed.
	 * This method has no relation to Activity#onResume().
	 */
	@Override
	public void onResume() {
		for (Sprite menuItem : menuItems) {
			menuItem.onResume();
		}
	}

	/**
	 * Called when the parent layer is paused.
	 * This method has no relation to Activity#onPause().
	 */
	@Override
	public void onPause() {
		for (Sprite menuItem : menuItems) {
			menuItem.onPause();
		}
	}

	/**
	 * Called when this shape is disposed.
	 */
	@Override
	public void onDispose() {
		for (Sprite menuItem : menuItems) {
			menuItem.onDispose();
		}
	}

	/**
	 * Called to draw the shape.
	 * This method is responsible for drawing the shape. 
	 */
	@Override
	public void onDraw(GL10 gl) {
		if (!loadableItems.isEmpty()) {
			for (Sprite menuItem : loadableItems) {
				menuItem.onLoadEngine(engine);
				menuItem.onLoadSurface(gl);
				menuItems.add(menuItem);
			}
			loadableItems.clear();
		}
		for (Sprite menuItem : menuItems) {
			menuItem.onDraw(gl);
		}
		if (!removedItems.isEmpty()) {
			for (Sprite menuItem : removedItems) {
				menuItems.remove(menuItem);
				menuItem.onDispose();
			}
			removedItems.clear();
		}
	}

	/**
	 * Called when this shape is removed.
	 */
	@Override
	public void onRemove() {
		this.removed = true;
	}

	/**
	 * Called when the shape is created or recreated.
	 * @param gl GL object
	 */
	@Override
	public void onLoadSurface(GL10 gl) {
		onLoadSurface(gl, false);
	}

	/**
	 * Called when the shape is created or recreated.
	 * @param gl GL object
	 * @param force force load when already loaded
	 */
	@Override
	public void onLoadSurface(GL10 gl, boolean force) {
		if (force) {
			for (Sprite menuItem : menuItems) {
				menuItem.onLoadSurface(gl, true);
			}
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
	 * Returns whether shape is removed not not.
	 */
	@Override
	public boolean isRemoved() {
		return removed;
	}

	/**
	 * Returns whether the shape is collided with given x coordinate or not.
	 * 
	 * @param globalX global x coordinate
	 * @return whether x axis is collided or not.
	 */
	@Override
	public boolean contains(int x, int y) {
		return false;
	}
}
