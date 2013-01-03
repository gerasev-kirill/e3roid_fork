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
package com.e3roid.drawable.controls;

import android.graphics.Rect;
import android.view.MotionEvent;

import com.e3roid.E3Scene;
import com.e3roid.drawable.Shape;
import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.texture.Texture;
import com.e3roid.event.ControllerEventListener;
import com.e3roid.event.SceneUpdateListener;

/**
 * On-Screen analog touch controller
 */
public class StickController extends Sprite implements SceneUpdateListener {

	/**
	 * Constant for indicating center position
	 */
	public static final int CENTER = 0;
	/**
	 * Constant for indicating left position
	 */
	public static final int LEFT   = 1;
	/**
	 * Constant for indicating right position
	 */
	public static final int RIGHT  = 2;
	/**
	 * Constant for indicating up position
	 */
	public static final int UP     = 3;
	/**
	 * Constant for indicating down position
	 */
	public static final int DOWN   = 4;
	
	protected final E3Scene scene;
	protected final Sprite knob;
	protected int knobCenterX;
	protected int knobCenterY;
	protected int updateInterval = 200;
	protected boolean fastUpdate = true;
	
	protected int moveX;
	protected int moveY;
	
	protected Rect knobPadding = new Rect();
	protected ControllerEventListener listener;
	protected int margin = 10;
	
	protected int identifier = 0;

	/**
	 * Constructs analog on-screen controller with given texture and position
	 * @param baseTexture base texture
	 * @param knobTexture knob texture
	 * @param x x position
	 * @param y y position
	 * @param scene E3Scene
	 */
	public StickController(Texture baseTexture, Texture knobTexture, int x, int y, E3Scene scene) {
		this(baseTexture, knobTexture, x, y, scene, null);
	}
	
	/**
	 * Constructs analog on-screen controller with given textute, position and listener
	 * @param baseTexture base texture
	 * @param knobTexture knob texture
	 * @param x x position
	 * @param y y position
	 * @param scene E3Scene
	 * @param listener ControllerEventListener
	 */
	public StickController(Texture baseTexture, Texture knobTexture,
			int x, int y, E3Scene scene, ControllerEventListener listener) {
		super(baseTexture, x, y);
		this.scene = scene;
		this.listener = listener;
		
		this.knob = new Sprite(knobTexture, 0, 0);
		this.addChild(knob);
		
		updateKnobCenter();
		
		setUpdateInterval(updateInterval);
	}
	
	protected void updateKnobCenter() {
		float[] baseCoord = getLocalCenterCoordinates();
		float[] knobCoord = knob.getLocalCenterCoordinates();
		
		knobCenterX = (int)(getRealX() + baseCoord[0] - knobCoord[0]);
		knobCenterY = (int)(getRealY() + baseCoord[1] - knobCoord[1]);
		knob.move(knobCenterX, knobCenterY);
		
		moveX = knobCenterX;
		moveY = knobCenterY;
	}

	/**
	 * Move controller to given position
	 */
	@Override
	public void move(int x, int y) {
		super.move(x, y);
		updateKnobCenter();
	}
	
	/**
	 * Move controller to given x
	 */
	@Override
	public void moveX(int x) {
		super.moveX(x);
		updateKnobCenter();
	}
	
	/**
	 * Move controler to given y
	 */
	@Override
	public void moveY(int y) {
		super.moveY(y);
		updateKnobCenter();
	}

	/**
	 * Set padding
	 * @param padding padding space
	 */
	public void setPadding(int padding) {
		setPadding(padding, padding, padding, padding);
	}
	
	/**
	 * Set padding
	 * @param left padding left
	 * @param top padding top
	 * @param right padding right
	 * @param bottom padding bottom
	 */
	public void setPadding(int left, int top, int right, int bottom) {
		knobPadding.left   = left;
		knobPadding.right  = getWidth() - right;
		knobPadding.top    = top;
		knobPadding.bottom = getHeight() - bottom;
	}

	/**
	 * Set event update interval
	 * @param interval update interval
	 */
	public void setUpdateInterval(int interval) {
		this.updateInterval = interval;
		scene.registerUpdateListener(interval, this);
	}
	
	/**
	 * Set margin
	 * @param margin margin space
	 */
	public void setMargin(int margin) {
		this.margin = margin;
	}
	
	/**
	 * Draw controller immediately when controller event occurs.
	 * @param enable true to enable immediate update
	 */
	public void updateImmediately(boolean enable) {
		this.fastUpdate = enable;
	}

	/**
	 * Indicates whether given position is inside the knob.
	 * @param localX local x coordinate
	 * @param localY local y coordinate
	 * @return true if the position is inside the knob
	 */
	public boolean isKnobArea(int localX, int localY) {
		if (localX >= knobPadding.left  && localX <= knobPadding.right &&
			localY >= knobPadding.top   && localY <= knobPadding.bottom) {
			return true;
		}
		return false;
	}

	/**
	 * Called when on-screen touch event occurs
	 */
	@Override
	public boolean onTouchEvent(E3Scene scene, Shape shape,
			MotionEvent motionEvent, int localX, int localY) {
		int pointerCount = motionEvent.getPointerCount();
		
		for (int i = 0; i < pointerCount; i++) {
			int actionType = motionEvent.getAction() & MotionEvent.ACTION_MASK;
			if (actionType == MotionEvent.ACTION_MOVE || 
					actionType == MotionEvent.ACTION_DOWN ||
					actionType == MotionEvent.ACTION_POINTER_DOWN) {
				int x = scene.getEngine().getContext().getTouchEventX(scene, motionEvent, i);
				int y = scene.getEngine().getContext().getTouchEventY(scene, motionEvent, i);
				
				if (!contains(x, y)) continue;
				float[] knobCoord = knob.getLocalCenterCoordinates();
				if (x >= getRealX() && x <= getRealX() + getWidth()) {
					moveX = (int)(x - knobCoord[0]);
				}
				if (y >= getRealY() && y <= getRealY() + getHeight()) {
					moveY = (int)(y - knobCoord[1]);
				}
				if (fastUpdate) {
					if (isKnobCenter() && updateControl()) {
						updateControlEvent();
					}
				}
			}
		}
		return false;
	}
	
	/** 
	 * Called when the scene touch event occurs
	 */
	@Override
	public boolean onSceneTouchEvent(E3Scene scene, MotionEvent motionEvent) {
		int actionType = motionEvent.getAction() & MotionEvent.ACTION_MASK;
		if (actionType == MotionEvent.ACTION_UP || 
				actionType == MotionEvent.ACTION_CANCEL ||
				actionType == MotionEvent.ACTION_POINTER_UP ||
				actionType == MotionEvent.ACTION_OUTSIDE){ 
			moveToCenter();
		}
		super.onSceneTouchEvent(scene, motionEvent);
		return false;
	}

	/**
	 * Called when the scene update event occurs
	 */
	@Override
	public void onUpdateScene(E3Scene scene, long elapsedMsec) {
		updateControlEvent();
	}
	
	protected void updateControlEvent() {
		boolean hasChanged = updateControl();
		if (hasChanged || !isKnobCenter()) {
			onControlUpdate(getRelativeKnobX(), getRelativeKnobY(), hasChanged);
			if (listener != null) {
				listener.onControlUpdate(this, getRelativeKnobX(), getRelativeKnobY(), hasChanged);
			}
		}
	}
	
	protected boolean updateControl() {
		if (moveX != knob.getRealX() || moveY != knob.getRealY()) {
			knob.move(moveX, moveY);
			return true;
		}
		return false;
	}

	/**
	 * Called when control update event occurs.
	 * Override this method to handle controller update.
	 * 
	 * @param relativeX controller value X
	 * @param relativeY controller value Y
	 * @param hasChanged indicates whether the value has changed or not.
	 */
	public void onControlUpdate(int relativeX, int relativeY, boolean hasChanged) {
		// do nothing by default
	}
	
	/**
	 * Returns knob position x
	 * @return knob position x
	 */
	public int getKnobX() {
		return this.moveX;
	}
	
	/**
	 * Returns knob position y
	 * @return knob position y
	 */
	public int getKnobY() {
		return this.moveY;
	}

	/**
	 * Set knob position
	 * @param x knob position x
	 * @param y knob position y
	 */
	public void setKnobPosition(int x, int y) {
		this.moveX = x;
		this.moveY = y;
	}

	/**
	 * Returns knob center position x
	 * @return knob center position x
	 */
	public int getKnobCenterX() {
		return this.knobCenterX;
	}
	
	/**
	 * Returns knob center position y
	 * @return knob center position y
	 */
	public int getKnobCenterY() {
		return this.knobCenterY;
	}

	/**
	 * Returns sprite for the knob.
	 * @return sprite for the knob
	 */
	public Sprite getKnobSprite() {
		return knob;
	}
	
	/**
	 * Returns relative knob x that takes value from -100 to +100
	 */
	public int getRelativeKnobX() {
		float unit  = (getWidth() / 2.0f) - knobPadding.left;
		float axisX = getRawX()  + (getWidth() / 2.0f);
		float knobX = getKnobX() + (knob.getWidth() / 2.0f);
		
		int x = (int)Math.round((knobX - axisX) / unit * 100);
		if (x > 0) x = Math.min(100, x);
		if (x < 0) x = Math.max(-100, x);
		return x;
	}
	
	/**
	 * Returns relative knob y that takes value from -100 to +100
	 */
	public int getRelativeKnobY() {
		float unit  = (getHeight() / 2.0f) - knobPadding.top;
		float axisY = getRawY()  + (getHeight() / 2.0f);
		float knobY = getKnobY() + (knob.getHeight() / 2.0f);
		
		int y =(int)Math.round((knobY - axisY) / unit * 100);
		if (y > 0) y = Math.min(100, y);
		if (y < 0) y = Math.max(-100, y);
		return y;
	}

	/**
	 * Moves knob to center
	 */
	public void moveToCenter() {
		this.moveX = this.knobCenterX;
		this.moveY = this.knobCenterY;
	}
	
	/**
	 * Indicates whether the knob is at center.
	 * @return whether the knob is at center
	 */
	public boolean isKnobCenter() {
		return moveX == knobCenterX && moveY == knobCenterY;
	}

	/**
	 * Indicates whether the given global y is inside the controller.
	 */
	@Override
	public boolean containsY(int globalY) {
		return (globalY >= getRealY() -margin && globalY<= getRealY() + getHeight() + margin);
	}

	/**
	 * Returns the knob direction
	 * @return the knob direction
	 */
	public int getDirection() {
		int relativeX = getRelativeKnobX();
		int relativeY = getRelativeKnobY();
		
		if (Math.abs(relativeX) > Math.abs(relativeY)) {
			if (relativeX > 0) {
				return RIGHT;
			} else if (relativeX < 0) {
				return LEFT;
			} else {
				return CENTER;
			}
		} else {
			if (relativeY > 0) {
				return DOWN;
			} else if (relativeY < 0) {
				return UP;
			} else {
				return CENTER;
			}
		}
	}
	
	/**
	 * Returns identifier for the control. Default value is 0.
	 * @return identifier for the control
	 */
	public int getIdentifier() {
		return this.identifier;
	}
	
	/**
	 * Set identifier for the control.
	 * @param id identifier for the control.
	 * @return
	 */
	public void setIdentifier(int id) {
		this.identifier = id;
	}
}
