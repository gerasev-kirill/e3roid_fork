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

public class SpanModifier extends ShapeModifier {

	private ProgressAware modifier;
	
	// maxCount=0 means infinite loop
	private int  maxCount = 0;
	private long span = 0;
	
	private int  currentCount  = 0;
	private long lastTimeMillis = 0;
	private boolean modifying = false;
	private boolean waitForFinish = false;
	private ModifierEventListener eventListener;
	
	public SpanModifier(long span, ProgressAware modifier) {
		this(0, span, modifier, null);
	}
	public SpanModifier(long span, ProgressAware modifier, ModifierEventListener listener) {
		this(0, span, modifier, listener);
	}
	public SpanModifier(int count, long span, ProgressAware modifier) {
		this(count, span, modifier, null);
	}
	public SpanModifier(int count, long span, ProgressAware modifier, ModifierEventListener listener) {
		this.modifier = modifier;
		this.maxCount = count;
		this.span = span;
		this.lastTimeMillis = SystemClock.uptimeMillis();
		this.modifier.hasParentShape(false);
		this.eventListener = listener;
	}

	private boolean spanElapsed() {
		return SystemClock.uptimeMillis() - lastTimeMillis > span;
	}
	
	private void updateMinimum() {
		modifier.updateProgressValueA(modifier.getMinimumValueA(), 0);
		if (modifier.getParameterMode() != ProgressAware.PARAM_SINGLE) {
			modifier.updateProgressValueB(modifier.getMinimumValueB(), 0);
		}
		if (modifier.getParameterMode() == ProgressAware.PARAM_TRIPLE) {
			modifier.updateProgressValueC(modifier.getMinimumValueC(), 0);
		}
	}
	
	private void updateMaximum() {
		modifier.updateProgressValueA(modifier.getMaximumValueA(), 1);
		if (modifier.getParameterMode() != ProgressAware.PARAM_SINGLE) {
			modifier.updateProgressValueB(modifier.getMaximumValueB(), 1);
		}
		if (modifier.getParameterMode() == ProgressAware.PARAM_TRIPLE) {
			modifier.updateProgressValueC(modifier.getMaximumValueC(), 1);
		}
	}
	
	@Override
	public void onResume() {
		modifier.onResume();
	}

	@Override
	public void onPause() {
		modifier.onPause();
	}
	
	@Override
	public void onLoad(Shape shape, GL10 gl) {
		if (eventListener != null) {
			eventListener.onModifierStart(this, shape);
		}
		modifier.onLoad(shape, gl);
	}
	
	@Override
	public void onBeforeUpdate(Shape shape, GL10 gl) {
		if (spanElapsed()) {
			if (modifying) {
				if (maxCount > 0) {
					currentCount++;
					if (currentCount >= maxCount) {
						this.waitForFinish = true;
						return;
					}
				}
				updateMinimum();
			} else {
				updateMaximum();
			}
			this.lastTimeMillis = SystemClock.uptimeMillis();
			this.modifying = !this.modifying;
		}
		modifier.onBeforeUpdate(shape, gl);
	}

	@Override
	public void onAfterUpdate(Shape shape, GL10 gl) {
		modifier.onAfterUpdate(shape, gl);
		if (this.waitForFinish) {
			this.finish(shape);
		}
	}

	@Override
	public void onUnload(Shape shape, GL10 gl) {
		if (eventListener != null) {
			eventListener.onModifierFinished(this, shape);
		}
		modifier.onUnload(shape, gl);
	}
	
	@Override
	public boolean isDurationDone() {
		return this.waitForFinish;
	}
	
	@Override
	public void reset() {
		this.currentCount = 0;
	}
	
}
