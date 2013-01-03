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

public class ColorModifier extends ProgressAware {

	public ColorModifier(
			float red, float green, float blue) {
		this(red, red, red, green, green, green, blue, blue, blue);
	}
	public ColorModifier(
			float currentRed, float minRed, float maxRed,
			float currentGreen, float minGreen, float maxGreen,
			float currentBlue, float minBlue, float maxBlue
			) {
		super(currentRed,   minRed,   maxRed, 
			  currentGreen, minGreen, maxGreen,
			  currentBlue,  minBlue,  maxBlue);
	}
	
	public static ColorModifier getClearColorModifier() {
		return new ColorModifier(1, 1, 1);
	}
	
	@Override
	public void onBeforeUpdate(Shape shape, GL10 gl) {
		shape.setColor(currentValueA, currentValueB, currentValueC, shape.getAlpha());
	}

	@Override
	public void onAfterUpdate(Shape shape, GL10 gl) {
		// do nothing
	}

}
