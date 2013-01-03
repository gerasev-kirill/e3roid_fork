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
package com.e3roid.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.e3roid.E3Scene;
import com.e3roid.drawable.Shape;
import com.e3roid.util.MathUtil;

/**
 * A composition class for connecting Shape and Box2D Body.
 */
public class PhysicsShape {
	
	protected Shape shape;
	protected Body  body;
	protected float halfShapeWidth;
	protected float halfShapeHeight;
	protected float pixelToMeterRatio = PhysicsWorld.PIXEL_TO_METER_RATIO_DEFAULT;
	protected boolean updatePosition = true;
	protected boolean updateRotation = true;
	
	public PhysicsShape(Shape shape, Body body) {
		this.shape = shape;
		this.body  = body;
		
		float[] halfCoord = shape.getLocalCenterCoordinates();
		this.halfShapeWidth  = halfCoord[0];
		this.halfShapeHeight = halfCoord[1];
	}
	
	public void setPixelToMeterRatio(float ratio) {
		this.pixelToMeterRatio = ratio;
	}
	
	public void onUpdate(E3Scene scene, long elapsedMsec) {
		// change position
		if (updatePosition) {
			Vector2 position = body.getPosition();
			shape.move(
				(int)(position.x * pixelToMeterRatio - this.halfShapeWidth),
				(int)(position.y * pixelToMeterRatio - this.halfShapeHeight));
		}
		// change angle
		if (updateRotation) {
			float angle = body.getAngle();
			shape.rotate(MathUtil.radToDeg(angle));
		}
	}
	
	public Body getBody() {
		return this.body;
	}
	
	public Shape getShape() {
		return this.shape;
	}
	
	public void enableUpdatePosition(boolean enable) {
		this.updatePosition = enable;
	}
	
	public void enableUpdateRotation(boolean enable) {
		this.updateRotation = enable;
	}
}
