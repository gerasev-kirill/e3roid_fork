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

import com.e3roid.drawable.Shape;

public abstract class ProgressAware extends ShapeModifier {

	public static final int PARAM_SINGLE = 0;
	public static final int PARAM_DOUBLE = 1;
	public static final int PARAM_TRIPLE = 2;
	
	private boolean finished = false;
	private boolean finishIfDone = true;
	
	protected float progress;
	
	protected float currentValueA;
	protected float minValueA;
	protected float maxValueA;
	
	protected float currentValueB;
	protected float minValueB;
	protected float maxValueB;
	
	protected float currentValueC;
	protected float minValueC;
	protected float maxValueC;
	
	protected int paramMode = PARAM_SINGLE;
	
	public ProgressAware(float current, float minValue, float maxValue) {
		this.currentValueA = current;
		this.minValueA = minValue;
		this.maxValueA = maxValue;
		this.paramMode = PARAM_SINGLE;
	}
	
	public ProgressAware(
			float currentA, float minValueA, float maxValueA,
			float currentB, float minValueB, float maxValueB) {
		this.currentValueA = currentA;
		this.minValueA = minValueA;
		this.maxValueA = maxValueA;
		
		this.currentValueB = currentB;
		this.minValueB = minValueB;
		this.maxValueB = maxValueB;
		
		this.paramMode = PARAM_DOUBLE;
	}

	public ProgressAware(
			float currentA, float minValueA, float maxValueA,
			float currentB, float minValueB, float maxValueB,
			float currentC, float minValueC, float maxValueC) {
		this.currentValueA = currentA;
		this.minValueA = minValueA;
		this.maxValueA = maxValueA;
		
		this.currentValueB = currentB;
		this.minValueB = minValueB;
		this.maxValueB = maxValueB;
		
		this.currentValueC = currentC;
		this.minValueC = minValueC;
		this.maxValueC = maxValueC;
		
		this.paramMode = PARAM_TRIPLE;
	}
	
	public float getMinimumValueA() {
		return minValueA;
	}

	public float getMaximumValueA() {
		return maxValueA;
	}
	
	public float getMinimumValueB() {
		return minValueB;
	}

	public float getMaximumValueB() {
		return maxValueB;
	}
	
	public float getMinimumValueC() {
		return minValueC;
	}

	public float getMaximumValueC() {
		return maxValueC;
	}
	
	public float getSpanA() {
		return maxValueA - minValueA;
	}
	public float getSpanB() {
		return maxValueB - minValueB;
	}
	public float getSpanC() {
		return maxValueC - minValueC;
	}
	
	public float getProgressValueA(float percent) {
		return minValueA + (percent * getSpanA());		
	}
	
	public float getProgressValueB(float percent) {
		return minValueB + (percent * getSpanB());		
	}
	
	public float getProgressValueC(float percent) {
		return minValueC + (percent * getSpanC());		
	}
	
	public void updateProgressValueA(float value, float percent) {
		currentValueA = value;
		progress     = percent;
	}
	public void updateProgressValueB(float value, float percent) {
		currentValueB = value;
		progress     = percent;
	}
	public void updateProgressValueC(float value, float percent) {
		currentValueC = value;
		progress     = percent;
	}
	
	public boolean isFinished() {
		return this.finished;
	}
	
	public boolean isFinishIfDone() {
		return this.finishIfDone;
	}
	
	public int getParameterMode() {
		return this.paramMode;
	}
	
	public void setFinishIfDone(boolean finish) {
		this.finishIfDone = finish;
	}
	
	public void done(Shape shape) {
		this.progress = 1;
		this.currentValueA = getMaximumValueA();
		this.currentValueB = getMaximumValueB();
		this.currentValueC = getMaximumValueC();
		if (isFinishIfDone()) {
			finish(shape);
		}
	}
	
	@Override
	public void reverse() {
		float min = this.minValueA;
		float max = this.maxValueA;
		this.minValueA = max;
		this.maxValueA = min;
		
		min = this.minValueB;
		max = this.maxValueB;
		this.minValueB = max;
		this.maxValueB = min;

		min = this.minValueC;
		max = this.maxValueC;
		this.minValueC = max;
		this.maxValueC = min;
	}
	
	@Override
	public void reset() {
		this.progress = 0;
		this.finished = false;
	}
	
	@Override
	public void onLoad(Shape shape, GL10 gl) {
		
	}

	@Override
	public void onUnload(Shape shape, GL10 gl) {
		
	}
	
	@Override
	public void finish(Shape shape) {
		this.finished = true;
	}
}
