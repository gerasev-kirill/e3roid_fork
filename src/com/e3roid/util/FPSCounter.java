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
package com.e3roid.util;

import java.util.ArrayList;
import android.os.SystemClock;
import com.e3roid.lifecycle.E3LifeCycle;

/**
 * FPS counter
 */
public class FPSCounter implements E3LifeCycle {
	public static final int DURATION_MSEC_DEFAULT = 5 * 1000;
	
	private int duration = DURATION_MSEC_DEFAULT;
	private int frames   = 0;
	private long lastTimeMillis = SystemClock.uptimeMillis();
	private boolean paused = false;
	private float currentFPS = 0;
	
	private float minFPS = Float.MAX_VALUE;
	private float maxFPS = Float.MIN_VALUE;
	
	private ArrayList<FPSListener> listeners = new ArrayList<FPSListener>();
	
	public FPSCounter() {
		// default constructor
	}
	public FPSCounter(int duration) {
		setDurationMsec(duration);
	}
	public float getFPS() {
		return this.currentFPS;
	}
	
	public void setDurationMsec(int msec) {
		this.duration = msec;
	}
	public void onFrame() {
		if (this.paused) return;
		
		long now = SystemClock.uptimeMillis();
		
		this.frames++;
		long elapsedMillis = now - this.lastTimeMillis;
		this.currentFPS = this.frames * 1000.0f / elapsedMillis;
		if(elapsedMillis >= this.duration) {
			this.minFPS = Math.min(this.minFPS, currentFPS);
			this.maxFPS = Math.max(this.maxFPS, currentFPS);
			invokeListeners(currentFPS, minFPS, maxFPS);
			this.lastTimeMillis = now;
			this.frames = 0;
		}
	}

	private void invokeListeners(float fps, float min, float max) {
		for (FPSListener listener : this.listeners) {
			listener.onFPS(fps, min, max);
		}
	}
	
	public int getFrameCount() {
		return this.frames;
	}
	
	public void addListener(FPSListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public void onPause() {
		this.paused = true;
	}
	@Override
	public void onResume() {
		this.resetCount();
	}
	@Override
	public void onDispose() {
		resetCount();
	}
	
	public void resetCount() {
		this.paused = false;
		this.lastTimeMillis = SystemClock.uptimeMillis();
		this.frames = 0;
		this.minFPS = Integer.MAX_VALUE;
		this.maxFPS = Integer.MIN_VALUE;
		this.currentFPS = 0;
	}
}
