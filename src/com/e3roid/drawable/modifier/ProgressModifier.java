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
package com.e3roid.drawable.modifier;

import javax.microedition.khronos.opengles.GL10;

import android.os.SystemClock;
import com.e3roid.drawable.Shape;
import com.e3roid.event.ModifierEventListener;
import com.e3roid.drawable.modifier.function.Progressive;

public class ProgressModifier extends ShapeModifier {
	
	private final ProgressAware modifier;
	private final Progressive functionA;
	private final Progressive functionB;
	private final Progressive functionC;
	private final long duration;
	
	private long lastTimeMillis = 0;
	private boolean durationDone = false;
	private long pausedTimeElapsed = 0;
	
	private ModifierEventListener eventListener;
	
	public ProgressModifier(ProgressAware modifier, long duration) {
		this(modifier, duration, null, null, null, null);
	}
	public ProgressModifier(ProgressAware modifier, long duration, Progressive function) {
		this(modifier, duration, function, null, null, null);
	}
	public ProgressModifier(ProgressAware modifier, long duration, 
			Progressive functionA, Progressive functionB) {
		this(modifier, duration, functionA, functionB, null, null);
	}
	public ProgressModifier(ProgressAware modifier, long duration, 
			Progressive functionA, Progressive functionB, Progressive functionC) {
		this(modifier, duration, functionA, functionB, functionC, null);
	}
	public ProgressModifier(ProgressAware modifier, long duration,
			Progressive functionA, ModifierEventListener eventListener) {
		this(modifier, duration, functionA, null, null, eventListener);
	}
	public ProgressModifier(ProgressAware modifier, long duration,
			Progressive functionA, Progressive functionB, ModifierEventListener eventListener) {
		this(modifier, duration, functionA, functionB, null, eventListener);
	}
	public ProgressModifier(ProgressAware modifier, long duration,
				Progressive functionA, Progressive functionB, Progressive functionC,
				ModifierEventListener eventListener) {
		this.modifier  = modifier;
		this.functionA  = functionA;
		this.functionB  = functionB;
		this.functionC  = functionC;
		this.duration  = duration;
		this.eventListener = eventListener;
		this.modifier.hasParentShape(false);
	}
	
	public void setEventListener(ModifierEventListener eventListener) {
		this.eventListener = eventListener;
	}
	
	@Override
	public boolean isDurationDone() {
		return this.durationDone;
	}
	
	@Override
	public void reverse() {
		this.modifier.reverse();
	}
	
	@Override
	public void reset() {
		this.durationDone = false;
		this.lastTimeMillis = SystemClock.uptimeMillis();
		this.modifier.reset();
	}

	@Override
	public void onLoad(Shape shape, GL10 gl) {
		if (eventListener != null) {
			eventListener.onModifierStart(this, shape);
		}
		this.lastTimeMillis = SystemClock.uptimeMillis();
		this.durationDone = false;
		modifier.onLoad(shape, gl);
	}
	
	@Override
	public void onPause() {
		this.pausedTimeElapsed = SystemClock.uptimeMillis() - this.lastTimeMillis;
	}
	
	@Override
	public void onResume() {
		this.lastTimeMillis = SystemClock.uptimeMillis() - this.pausedTimeElapsed;
	}
	
	@Override
	public void onBeforeUpdate(Shape shape, GL10 gl) {
		long elapsed = SystemClock.uptimeMillis() - this.lastTimeMillis;
		if (elapsed <= duration) {
			float progress = 0;
			if (functionA != null) {
				progress = functionA.getProgress(elapsed, duration, 0, 1);
				modifier.updateProgressValueA(modifier.getProgressValueA(progress), progress);
			}
			if (modifier.getParameterMode() != ProgressAware.PARAM_SINGLE) {
				if (functionB != null) {
					progress = functionB.getProgress(elapsed, duration, 0, 1);
				}
				modifier.updateProgressValueB(modifier.getProgressValueB(progress), progress);
			}
			if (modifier.getParameterMode() == ProgressAware.PARAM_TRIPLE) {
				if (functionC != null) {
					progress = functionC.getProgress(elapsed, duration, 0, 1);
				}
				modifier.updateProgressValueC(modifier.getProgressValueC(progress), progress);
			}
		} else {
			modifier.done(shape);
			durationDone = true;
		}
		modifier.onBeforeUpdate(shape, gl);
	}

	@Override
	public void onAfterUpdate(Shape shape, GL10 gl) {
		modifier.onAfterUpdate(shape, gl);
		if (modifier.isFinishIfDone() && modifier.isFinished()) {
			this.finish(shape);
			return;
		}
	}

	@Override
	public void onUnload(Shape shape, GL10 gl) {
		modifier.onUnload(shape, gl);
		if (eventListener != null) {
			eventListener.onModifierFinished(this, shape);
		}
	}
}
