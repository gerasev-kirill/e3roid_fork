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

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.World;

import com.e3roid.E3Scene;
import com.e3roid.drawable.Shape;
import com.e3roid.event.SceneUpdateListener;

/**
 * A wrapper class for physics world.
 */
public class PhysicsWorld implements SceneUpdateListener {

	protected World world;
	protected ArrayList<PhysicsShape> shapes = new ArrayList<PhysicsShape>();
	
	public static final int VELOCITY_ITERATIONS_DEFAULT = 8;
	public static final int POSITION_ITERATIONS_DEFAULT = 8;
	public static final float PIXEL_TO_METER_RATIO_DEFAULT = 32.0f;
	
	protected int velocityIters = VELOCITY_ITERATIONS_DEFAULT;
	protected int positionIters = POSITION_ITERATIONS_DEFAULT;
	
	public PhysicsWorld(Vector2 gravity, boolean allowSleep) {
		this(gravity, allowSleep, VELOCITY_ITERATIONS_DEFAULT, POSITION_ITERATIONS_DEFAULT);
	}

	public PhysicsWorld(Vector2 gravity, boolean allowSleep, int velocityIterations, int positionIterations) {
		this.world = new World(gravity, allowSleep);
		this.velocityIters = velocityIterations;
		this.positionIters = positionIterations;
	}
	
	@Override
	public void onUpdateScene(E3Scene scene, long elapsedMsec) {
		world.step(msec2sec(elapsedMsec), velocityIters, positionIters);
		for (PhysicsShape shape : shapes) {
			shape.onUpdate(scene, elapsedMsec);
		}
	}
	
	private float msec2sec(long msec) {
		return (float)msec / 1000.0f;
	}
	
	public void addShape(PhysicsShape shape) {
		shapes.add(shape);
	}
	
	public void removeShape(PhysicsShape shape) {
		destroyBody(shape.getBody());
		shapes.remove(shape);
	}
	
	public void removeShape(Shape shape) {
		PhysicsShape pShape = findShape(shape);
		if (pShape == null) return;
		removeShape(pShape);
	}
	
	public PhysicsShape findShape(Shape shape) {
		for (PhysicsShape pShape : shapes) {
			if (pShape.getShape() == shape) {
				return pShape;
			}
		}
		return null;
	}
	
	public void setGravity(Vector2 gravity) {
		this.world.setGravity(gravity);
	}
	
	public Body createBody(BodyDef pDef) {
		return this.world.createBody(pDef);
	}

	public Joint createJoint(JointDef pDef) {
		return this.world.createJoint(pDef);
	}

	public void destroyBody(Body pBody) {
		this.world.destroyBody(pBody);
	}

	public void destroyJoint(Joint pJoint) {
		this.world.destroyJoint(pJoint);
	}

	public void dispose() {
		this.world.dispose();
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public int getPositionIterations() {
		return this.positionIters;
	}

	public void setPositionIterations(final int positionIterations) {
		this.positionIters = positionIterations;
	}

	public int getVelocityIterations() {
		return this.velocityIters;
	}

	public void setVelocityIterations(final int velocityIterations) {
		this.velocityIters = velocityIterations;
	}
}
