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

import com.e3roid.E3Scene;
import com.e3roid.drawable.texture.Texture;
import com.e3roid.event.ControllerEventListener;

/**
 * Digital on-screen controller
 */
public class DigitalController extends StickController {

	/**
	 * Constructs digital on-screen controller with given texture and position
	 * @param baseTexture base texture
	 * @param knobTexture knob texture
	 * @param x x position
	 * @param y y position
	 * @param scene E3Scene
	 */
	public DigitalController(Texture baseTexture, Texture knobTexture, int x, int y, E3Scene scene) {
		this(baseTexture, knobTexture, x, y, scene, null);
	}

	/**
	 * Constructs digital on-screen controller with given texture, position and listener
	 * @param baseTexture base texture
	 * @param knobTexture knob texture
	 * @param x x position
	 * @param y y position
	 * @param scene E3Scene
	 * @param listener ControllerEventListener
	 */
	public DigitalController(Texture baseTexture, Texture knobTexture,
			int x, int y, E3Scene scene, ControllerEventListener listener) {
		super(baseTexture, knobTexture, x, y, scene, listener);
	}
	
	@Override
	protected boolean updateControl() {
		if (moveX == knob.getRealX() && moveY == knob.getRealY()) return false;
			
		int relativeX = getRelativeKnobX();
		int relativeY = getRelativeKnobY();
		float[] centerCoord = getLocalCenterCoordinates();
		
		if (Math.abs(relativeX) > Math.abs(relativeY)) {
			if (relativeX > 0) {
				moveX = knobCenterX + (int)centerCoord[0];
				moveY = knobCenterY;
			} else if (relativeX < 0) {
				moveX = knobCenterX - (int)centerCoord[0];
				moveY = knobCenterY;
			} else {
				moveToCenter();
			}
		} else {
			if (relativeY > 0) {
				moveX = knobCenterX;
				moveY = knobCenterY + (int)centerCoord[1];
			} else if (relativeY < 0) {
				moveX = knobCenterX;
				moveY = knobCenterY - (int)centerCoord[1];
			} else {
				moveToCenter();
			}
		}
		
		return super.updateControl();
	}

	/**
	 * Returns the knob direction
	 * @return the knob direction
	 */
	@Override
	public int getDirection() {
		if (getRelativeKnobY() == -100) return UP;
		if (getRelativeKnobY() ==  100) return DOWN;
		if (getRelativeKnobX() == -100) return LEFT;
		if (getRelativeKnobX() ==  100) return RIGHT;
		return CENTER;
	}
}
