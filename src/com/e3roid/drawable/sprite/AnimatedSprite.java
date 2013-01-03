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

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.SystemClock;
import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.texture.TiledTexture;
import com.e3roid.event.AnimationEventListener;
import com.e3roid.opengl.FastFloatBuffer;
import com.e3roid.util.IntPair;

/**
 * A AnimatedSprite is used to animate the sprite.
 */
public class AnimatedSprite extends Sprite {

	private boolean animated = false;
	private int maxCount = 0;
	private ArrayList<Frame> frames = new ArrayList<Frame>();
	private HashMap<IntPair, FastFloatBuffer> cache = new HashMap<IntPair, FastFloatBuffer>();
	private final TiledTexture texture;
	private AnimationEventListener eventListener;
	
	private long duration = 200;
	private long lastTimeMillis = 0;
	private int  currentFrame = 0;
	private int  currentCount = 0;
	private boolean reloadTile = false;
	
	/**
	 * Constructs animated sprite with given texture and position.
	 */
	public AnimatedSprite(TiledTexture texture, int x, int y) {
		this(texture, x, y, null);
	}
	
	/**
	 * Constructs animated sprite with given texture, position and listener.
	 */
	public AnimatedSprite(TiledTexture texture, int x, int y, AnimationEventListener listener) {
		super(texture);
		this.texture = texture;
		this.eventListener = listener;
		setSize(texture.getTileWidth(), texture.getTileHeight());
		setPosition(x, y);
		useDefaultRotationAndScaleCenter();
		createBuffers();
	}

	/**
	 * Called to draw the sprite.
	 * This method is responsible for drawing the sprite. 
	 */
	@Override
	public void onDraw(GL10 _gl) {
		GL11 gl = (GL11)_gl;
		
		if (animated && isVisible()) {
			long now = SystemClock.uptimeMillis();
			Frame frame = getCurrentFrame();
			long wait = frame.getDuration() == 0 ? duration : frame.getDuration();
		
			if (now - lastTimeMillis > wait) {
				IntPair cacheKey = new IntPair(frame.getX(), frame.getY());
				nextFrame();
				loadTextureBuffer(gl, cache.get(cacheKey));
				if (maxCount > 0 && currentFrame == 0) currentCount++;
				lastTimeMillis = now;
			}
			if (maxCount > 0 && currentCount >= maxCount) {
				this.stop();
			}
		}
		if (reloadTile) {
			IntPair cacheKey = new IntPair(texture.getTileIndexX(), texture.getTileIndexY());
			loadTextureBuffer(gl, cache.get(cacheKey));
			reloadTile = false;
		}
		
		super.onDraw(_gl);
	}
	
	private void nextFrame() {
		currentFrame++;
		if (frames.size() <= currentFrame) {
			currentFrame = 0;
		}
	}
	
	private void createAnimationBuffers() {
		int saveX = texture.getTileIndexX();
		int saveY = texture.getTileIndexY();
		
		for (Frame frame : frames) {
			createCache(frame.getX(), frame.getY());
		}
		
		texture.setTileIndex(saveX, saveY);
	}
	
	private void createCache(int x, int y) {
		IntPair cacheKey = new IntPair(x, y);
		if (cache.containsKey(cacheKey)) {
			return;
		}
		texture.setTileIndex(x, y);
		float[] coords = {
			texture.getCoordStartX(), texture.getCoordStartY(),
			texture.getCoordStartX(), texture.getCoordEndY(),
			texture.getCoordEndX(), texture.getCoordEndY(),
			texture.getCoordEndX(), texture.getCoordStartY()
		};
		cache.put(cacheKey, FastFloatBuffer.createBuffer(coords));
	}
	
	/**
	 * Called when this shape is removed.
	 */
	@Override
	public void onRemove() {
		super.onRemove();
		reset();
		animated = false;
	}
	
	/**
	 * Called when this shape is disposed.
	 */
	@Override
	public void onDispose() {
		super.onDispose();
		reset();
		animated = false;
	}

	/**
	 * Returns current animation frame.
	 * @return current animation frame
	 */
	public Frame getCurrentFrame() {
		return frames.get(currentFrame);
	}
	
	/**
	 * Returns current animation frame index
	 * @return current animation frame index
	 */
	public int getCurrentFrameIndex() {
		return this.currentFrame;
	}
	
	/**
	 * Returns current loop count of the animation.
	 * If max loop count is not set, currentCount always returns 0.
	 * @return current loop count of the animation
	 */
	public int getCurrentCount() {
		return this.currentCount;
	}
	
	/**
	 * Set tile index of the animation.
	 * This method is useful in case of changing the tile index while animation is stopped.
	 * @param xindex x index of the tile
	 * @param yindex y index of the tile
	 */
	public void setTile(int xindex, int yindex) {
		createCache(xindex, yindex);
		texture.setTileIndex(xindex, yindex);
		reloadTile = true;
	}
	
	/**
	 * Reset animation
	 */
	public void reset() {
		lastTimeMillis = 0;
		currentFrame = 0;
		currentCount = 0;
	}
	
	/**
	 * Stop animation
	 */
	public void stop() {
		if (eventListener != null) {
			eventListener.animationFinished(this);
		}
		this.animated = false;
	}
	
	/**
	 * Start animation
	 */
	public void start() {
		if (eventListener != null) {
			eventListener.animationStarted(this);
		}
		this.animated = true;
	}
	
	/**
	 * Returns whether the sprite is animated or not.
	 * @return whether the sprite is animated or not
	 */
	public boolean isAnimated() {
		return this.animated;
	}
	
	/**
	 * Set event listener of the animation
	 * @param listener
	 */
	public void setEventListener(AnimationEventListener listener) {
		this.eventListener = listener;
	}
	
	/**
	 * Start animation with given duration
	 * 
	 * @param duration duration of the animation
	 * @param frames animation frames
	 */
	public void animate(long duration, ArrayList<Frame> frames) {
		animate(duration, 0, frames);
	}
	
	/**
	 * Start animation with given loop count
	 * 
	 * @param duration duration of the animation
	 * @param count max loop count
	 * @param frames animation frames
	 */
	public void animate(long duration, int count, ArrayList<Frame> frames) {
		this.duration = duration;
		this.maxCount    = count;
		this.frames   = frames;
		createAnimationBuffers();
		reset();
		if (eventListener != null && animated) {
			eventListener.animationFinished(this);
		}
		start();
	}
	
	/**
	 * A Frame for defining the animation frame.
	 */
	public static class Frame {
		private int tileX = 0;
		private int tileY = 0;
		private long duration = 0;
		public Frame(int x, int y) {
			this.tileX = x;
			this.tileY = y;
		}
		public Frame(int x, int y, long duration) {
			this.tileX = x;
			this.tileY = y;
			this.duration = duration;
		}
		public int getX() {
			return this.tileX;
		}
		public int getY() {
			return this.tileY;
		}
		public long getDuration() {
			return this.duration;
		}
	}
}
