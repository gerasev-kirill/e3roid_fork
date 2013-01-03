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
package com.e3roid.drawable;

import java.util.ArrayList;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Rect;
import android.view.MotionEvent;

import com.e3roid.E3Engine;
import com.e3roid.E3Scene;
import com.e3roid.opengl.FastFloatBuffer;
import com.e3roid.opengl.GLHelper;
import com.e3roid.drawable.modifier.ShapeModifier;
import com.e3roid.event.ModifierEventListener;
import com.e3roid.event.SceneEventListener;
import com.e3roid.event.ShapeEventListener;

/**
 * A Shape class is used to draw 2D rectangle shape.
 */
public class Shape implements Drawable,ShapeEventListener,SceneEventListener,ModifierEventListener {

	/**
	 * Constant for indicating X axis
	 */
	public static final int AXIS_X = 0;
	/**
	 * Constant for indicating Y axis
	 */
	public static final int AXIS_Y = 1;
	/**
	 * Constant for indicating Z axis
	 */
	public static final int AXIS_Z = 2;

	protected int width  = 0;
	protected int height = 0;
	
	protected boolean removed = false;
	protected boolean loaded  = false;
	protected boolean visible = true;
	
	protected int x = 0;
	protected int y = 0;
	protected int z = 0;
	
	protected FastFloatBuffer vertexBuffer;
	protected ShortBuffer     indiceBuffer;

	protected static final int RECTANGLE_POINTS = 4;
	protected static final short[] RECTANGLE_INDICE = {0,1,2,3};
	protected final int[] GENERATED_HARDWAREID = new int[2];
	protected boolean useVBO = true;
	
	protected Rect rect = new Rect();
	
	protected ArrayList<ShapeEventListener> listeners = new ArrayList<ShapeEventListener>();
	protected ArrayList<ShapeModifier> modifiers = new ArrayList<ShapeModifier>();
	protected ArrayList<ShapeModifier> loadableModifiers = new ArrayList<ShapeModifier>();
	protected ArrayList<ShapeModifier> removedModifiers = new ArrayList<ShapeModifier>();
	protected ArrayList<Shape> children = new ArrayList<Shape>();
	
	/**
	 * Color red, green, blue, alpha
	 */
	protected float[] color = {
		1.0f, 1.0f, 1.0f, 1.0f
	};

	/**
	 *  Translate x, y, x
	 */
	protected float[] translateParams = {
		0, 0, 0
	};
	
	/**
	 *  Rotate angle, center x, center y, axis
	 */
	protected float[] rotateParams = {
		0, 0, 0, AXIS_Z
	};

	/**
	 *  Scale x, y, center x, center y
	 */
	protected float[] scaleParams = {
		1, 1, 0, 0
	};
	
	/**
	 *  Default rotation center x and y
	 */
	protected float[] rotateCenter = {
		0, 0
	};
	
	/**
	 *  Default scale center x and y
	 */
	protected float[] scaleCenter = {
			0, 0
	};
	
	/**
	 *  Default constructor for subclass.
	 */
	public Shape() {
		
	}
	
	/**
	 * Construct shape for given position and size.
	 * 
	 * @param x x coordinate of shape position
	 * @param y y coordinate of shape position
	 * @param w width of the shape
	 * @param h height of the shape
	 */
	public Shape(int x, int y, int w, int h) {
		setPosition(x, y);
		setSize(w, h);
		useDefaultRotationAndScaleCenter();
		createBuffers();
	}
	
	/**
	 * Called when the shape is created or recreated.
	 * @param _gl GL object
	 */
	@Override
	public void onLoadSurface(GL10 _gl) {
		onLoadSurface(_gl, false);
	}
	
	/**
	 * Called when the shape is created or recreated.
	 * @param _gl GL object
	 * @param force force load whenever already loaded
	 */
	@Override
	public void onLoadSurface(GL10 _gl, boolean force) {
		GL11 gl = (GL11)_gl;

		if (!force && isLoaded()) return;
		
		if (force && isLoaded()) {
			unloadBuffer(gl);
		}
		
		for(Shape child : children) {
			child.onLoadSurface(_gl, force);
		}
		
		if (useVBO) {
			gl.glGenBuffers(GENERATED_HARDWAREID.length, GENERATED_HARDWAREID, 0);
		}
		loadVertexBuffer(gl);
		
		setLoaded(true);
	}
	
	protected void loadVertexBuffer(GL11 gl) {
		if (useVBO) {
			GLHelper.bindBuffer(gl, GENERATED_HARDWAREID[0]);
			GLHelper.bufferFloatData(gl, vertexBuffer.capacity(), vertexBuffer, GL11.GL_STATIC_DRAW);
			GLHelper.bindElementBuffer(gl, GENERATED_HARDWAREID[1]);
			GLHelper.bufferElementShortData(gl, RECTANGLE_INDICE.length, indiceBuffer, GL11.GL_STATIC_DRAW);
		}
	}
	
	protected void processModifiers(GL10 gl) {
		if (!loadableModifiers.isEmpty()) {
			for (ShapeModifier modifier : loadableModifiers) {
				modifier.onLoad(this, gl);
				modifiers.add(modifier);
			}
			loadableModifiers.clear();
		}
		if (!removedModifiers.isEmpty()) {
			for (ShapeModifier modifier : removedModifiers) {
				modifier.onUnload(this, gl);
				modifiers.remove(modifier);
			}
			removedModifiers.clear();
		}
		if (isRemoved() && !modifiers.isEmpty()) {
			for (ShapeModifier modifier : modifiers) {
				modifier.onUnload(this, gl);
			}
			modifiers.clear();
		}
	}
	
	protected void processBeforeModifiers(GL10 gl) {
		for (ShapeModifier modifier : modifiers) {
			modifier.onBeforeUpdate(this, gl);
		}
	}
	
	protected void processAfterModifiers(GL10 gl) {
		for (ShapeModifier modifier : modifiers) {
			modifier.onAfterUpdate(this, gl);
		}
	}
	
    protected void checkGLError(GL10 gl) {
        int error = gl.glGetError();
        if (error != GL10.GL_NO_ERROR) {
            throw new RuntimeException("GLError 0x" + Integer.toHexString(error));
        }
    }
    
    protected void unloadBuffer(GL11 gl) {
    	if (GENERATED_HARDWAREID[0] > 0) {
    		GLHelper.deleteBuffer(gl, GENERATED_HARDWAREID[0]);
    	}
    	if (GENERATED_HARDWAREID[1] > 0) {
    		GLHelper.deleteElementBuffer(gl, GENERATED_HARDWAREID[1]);
    	}
    }
    
	/**
	 * Called to draw the shape.
	 * This method is responsible for drawing the shape. 
	 */
	@Override
	public void onDraw(GL10 _gl) {
		GL11 gl = (GL11)_gl;
		
		if (!isLoaded()) {
			onLoadSurface(_gl);
		}
		
		for(Shape child : children) {
			child.onDraw(_gl);
		}
		
		processModifiers(gl);
		
		if (removed && loaded && useVBO) {
			unloadBuffer(gl);
			setLoaded(false);
			return;
		}
		
		if (!isVisible() || isRemoved()) {
			return;
		}
		
		GLHelper.enableTextures(gl, false);
        
	    gl.glLoadIdentity();
		gl.glPushMatrix();
		processBeforeModifiers(gl);
		GLHelper.setColor(gl, color[0], color[1], color[2], color[3]);
		applyParams(gl);
		if (useVBO) {
			GLHelper.bindBuffer(gl, GENERATED_HARDWAREID[0]);
			GLHelper.bindElementBuffer(gl, GENERATED_HARDWAREID[1]);
			GLHelper.vertexZeroPointer(gl);
			gl.glDrawElements(GL11.GL_TRIANGLE_FAN, RECTANGLE_POINTS, GL11.GL_UNSIGNED_SHORT, 0);
		} else {
			GLHelper.vertexPointer(gl, vertexBuffer);
			gl.glDrawElements(GL11.GL_TRIANGLE_FAN, RECTANGLE_POINTS, GL11.GL_UNSIGNED_SHORT, indiceBuffer);
		}
		processAfterModifiers(gl);
		gl.glPopMatrix();
		
		GLHelper.checkError(gl);
		
		// unbind buffers
		GLHelper.bindBuffer(gl, 0);
		GLHelper.bindElementBuffer(gl, 0);
		GLHelper.enableTextures(gl, true);
	}

	/**
	 * Called when the parent layer is resumed.
	 * This method has no relation to Activity#onResume().
	 */
	@Override
	public void onResume() {
		for (ShapeModifier modifier : modifiers) {
			modifier.onResume();
		}
		for(Shape child : children) {
			child.onResume();
		}
	}

	/**
	 * Called when the parent layer is paused.
	 * This method has no relation to Activity#onPause().
	 */
	@Override
	public void onPause() {
		for (ShapeModifier modifier : modifiers) {
			modifier.onPause();
		}
		for(Shape child : children) {
			child.onPause();
		}
	}

	/**
	 * Called when this shape is removed.
	 */
	@Override
	public void onRemove() {
		this.removed = true;
		this.vertexBuffer = null;
		this.indiceBuffer = null;
		this.listeners.clear();
		this.loadableModifiers.clear();
		this.modifiers.clear();
		this.removedModifiers.clear();
		for(Shape child : children) {
			child.onRemove();
		}
	}
	
	/**
	 * Called when e3roid engine has been loaded.
	 */
	@Override
	public void onLoadEngine(E3Engine engine) {
		this.useVBO = engine.useVBO();
		for(Shape child : children) {
			child.onLoadEngine(engine);
		}
	}
	
	/**
	 * Called when this shape is disposed.
	 */
	@Override
	public void onDispose() {
		this.removed = true;
		this.loaded  = false;
		for(Shape child : children) {
			child.onDispose();
		}
	}

	/**
	 * Set current axis of this shape. This axis is reference axis that is used with translation.
	 * Use move(x,y) to move within the scene instead of setPosition(x,y).
	 * 
	 * @param x
	 * @param y
	 */
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Set color of the shape.
	 * 
	 * @param red Red parameter that takes float value from 0 to 1.
	 * @param green Green parameter that takes float value from 0 to 1.
	 * @param blue Blue parameter that takes float value from 0 to 1.
	 */
	public void setColor(float red, float green, float blue) {
		setRed(red);
		setGreen(green);
		setBlue(blue);
	}
	
	/**
	 * Set color of the shape with alpha color.
	 * 
	 * @param red Red color that takes float value from 0 to 1.
	 * @param green Green color that takes float value from 0 to 1.
	 * @param blue Blue color that takes float value from 0 to 1.
	 * @param alpha Alpha color that takes float value from 0 to 1.
	 */
	public void setColor(float red, float green, float blue, float alpha) {
		setRed(red);
		setGreen(green);
		setBlue(blue);
		setAlpha(alpha);
	}
	
	/**
	 * Reset current color. Default color is white. 
	 */
	public void resetColor() {
		setRed(1);
		setGreen(1);
		setBlue(1);
		setAlpha(1);
	}
	
	/**
	 * Set red color of this shape.
	 * 
	 * @param color red color
	 */
	public void setRed(float color) {
		this.color[0] = color;
		for(Shape child : children) {
			child.setRed(color);
		}
	}
	
	/**
	 * Set green color of this shape.
	 * 
	 * @param color green color
	 */
	public void setGreen(float color) {
		this.color[1] = color;
		for(Shape child : children) {
			child.setGreen(color);
		}
	}
	/**
	 * Set blue color of this shape.
	 * 
	 * @param color blue color
	 */
	public void setBlue(float color) {
		this.color[2] = color;
		for(Shape child : children) {
			child.setBlue(color);
		}
	}
	/**
	 * Set alpha color of this shape.
	 * 
	 * @param color alpha color
	 */
	public void setAlpha(float alpha) {
		this.color[3] = alpha;
		for(Shape child : children) {
			child.setAlpha(alpha);
		}
	}
	
	/**
	 * Set size of the shape.
	 * 
	 * @param w width
	 * @param h height
	 */
	protected void setSize(int w, int h) {
		this.width  = w;
		this.height = h;
	}

	/**
	 * Returns width of the shape without applying scaling.
	 * @return width
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Returns height of the shape without applying scaling.
	 * @return height
	 */
	public int getHeight() {
		return this.height;
	}
	
	/**
	 * Returns scaled width of the shape.
	 * @return scaled width of the shape.
	 */
	public float getWidthScaled() {
		return getWidth() * scaleParams[0];
	}

	/**
	 * Returns scaled height of the shape.
	 * @return scaled height of the shape.
	 */
	public float getHeightScaled() {
		return getHeight() * scaleParams[1];
	}

	/**
	 * Returns X position of reference axis
	 * @return X position of reference axis
	 */
	public int getRawX() {
		return this.x;
	}
	
	/**
	 * Returns Y position of reference axis
	 * @return Y position of reference axis
	 */
	public int getRawY() {
		return this.y;
	}
	
	/**
	 * Returns color of the shape that contains RGB+Alpha.
	 * @return color of the shape.
	 */
	public float[] getColor() {
		return this.color;
	}
	
	/**
	 * Returns color of red
	 * @return color of red
	 */
	public float getRed() {
		return color[0];
	}
	
	/**
	 * Returns color of green
	 * @return color of green
	 */
	public float getGreen() {
		return color[1];
	}
	
	/**
	 * Returns color of blue
	 * @return color of blue
	 */
	public float getBlue() {
		return color[2];
	}
	
	/**
	 * Returns color of alpha
	 * @return color of alpha
	 */
	public float getAlpha() {
		return color[3];
	}
	
	/**
	 * returns whether the alpha color is 0(transparent) or not.
	 * @return whether the alpha color is 0(transparent) or not.
	 */
	public boolean isTransparent() {
		return getAlpha() == 0;
	}
	
	/**
	 * Enables VBO rendering.
	 */
	public void enableVBO(boolean enable) {
		this.useVBO = enable;
		for(Shape child : children) {
			child.enableVBO(enable);
		}
	}
	
	/**
	 * Returns whether shape is removed not not.
	 */
	@Override
	public boolean isRemoved() {
		return this.removed;
	}

	/**
	 * Returns whether shape is loaded or not.
	 * @return
	 */
	public boolean isLoaded() {
		return this.loaded;
	}

	/**
	 * Set the sprite is loaded or not
	 * @param loaded
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
		for(Shape child : children) {
			child.setLoaded(loaded);
		}
	}
	
	/**
	 * Set default rotation and scale center position
	 * @param localX local center position x
	 * @param localY local center position y
	 */
	public void setRotationAndScaleCenter(float localX, float localY) {
		setRotationCenter(localX, localY);
		setScaleCenter(localX, localY);
	}
	
	/**
	 * Set rotation and scale center position to default position
	 */
	public void useDefaultRotationAndScaleCenter() {
		float[] coord = getLocalCenterCoordinates();
		setRotationAndScaleCenter(coord[0], coord[1]);
	}
	
	/**
	 * Returns default local center coordinates used with rotation and scale.
	 *  
	 * @return default local center coordinates
	 */
	public float[] getLocalCenterCoordinates() {
		float[] coord = new float[2];
		coord[0] = getWidth()  * 0.5f;
		coord[1] = getHeight() * 0.5f;
		return coord;
	}
	
	/**
	 * Returns default global center coordinates used with rotation and scale.
	 *  
	 * @return default global center coordinates
	 */
	public float[] getGlobalCenterCoordinates() {
		float[] coord = getLocalCenterCoordinates();
		coord[0] = coord[0] + getRealX();
		coord[1] = coord[1] + getRealY();
		return coord;
	}
	
	/**
	 *  Set rotation center x and y
	 *  
	 * @param localX rotation center local x  
	 * @param localY rotation center local y
	 */
	public void setRotationCenter(float localX, float localY) {
		rotateCenter[0] = localX;
		rotateCenter[1] = localY;
	}
	
	/**
	 *  Set scale center x and y
	 *  
	 * @param localX scale center local x  
	 * @param localY scale center local y
	 */
	public void setScaleCenter(float localX, float localY) {
		scaleCenter[0] = localX;
		scaleCenter[1] = localY;
	}
	
	/**
	 * Returns rotation center x
	 * @return rotation center x
	 */
	public float getRotationCenterX() {
		return rotateCenter[0];
	}

	/**
	 * Returns rotation center y
	 * @return rotation center y
	 */
	public float getRotationCenterY() {
		return rotateCenter[1];
	}

	/**
	 * Returns scale center x
	 * @return scale center x
	 */
	public float getScaleCenterX() {
		return scaleCenter[0];
	}

	/**
	 * Returns scale center y
	 * @return scale center y
	 */
	public float getScaleCenterY() {
		return scaleCenter[1];
	}

	/**
	 * Translate with given parameters
	 */
	public void translate(float x, float y, float z) {
		translateParams[0] = x;
		translateParams[1] = y;
		translateParams[2] = z;
	}
	
	/**
	 *  Rotate with given angle and default rotation center coordinates.
	 * @param angle rotation angle
	 */
	public void rotate(float angle) {
		rotate(angle, AXIS_Z);
	}
	
	/**
	 * Rotate with given angle, axis and default rotation center coordinates.
	 * @param angle rotation angle
	 * @param axis rotation axis
	 */
	public void rotate(float angle, int axis) {
		rotate(angle, rotateCenter[0], rotateCenter[1], axis);
	}

	/**
	 * Rotate with given angle and center coordinates
	 * 
	 * @param angle  angle
	 * @param localX center x
	 * @param localY center y
	 */
	public void rotate(float angle, float localX, float localY) {
		rotate(angle, localX, localY, AXIS_Z);
	}

	/**
	 * Rotate with given angle, axis and center coordinates
	 * @param angle angle
	 * @param localX center x
	 * @param localY center y
	 * @param axis axis
	 */
	public void rotate(float angle, float localX, float localY, int axis) {
		rotateParams[0] = angle;
		rotateParams[1] = getRawX() + localX;
		rotateParams[2] = getRawY() + localY;
		rotateParams[3] = axis;
	}
	
	/**
	 * Useful function for perspective rotation(experimental)
	 */
	public void standUp(float angle) {
		rotateParams[0] = angle;
		rotateParams[1] = getRawX();
		rotateParams[2] = getRawY() + getHeight();
		rotateParams[3] = AXIS_X;
	}

	/**
	 * Scale with given parameters.
	 * 
	 * @param x scale for x axis
	 * @param y scale for y axis
	 * @param localX center x
	 * @param localY center y
	 */
	public void scale(float x, float y, float localX, float localY) {
		scaleParams[0] = x;
		scaleParams[1] = y;
		scaleParams[2] = getRawX() + localX;
		scaleParams[3] = getRawY() + localY;
	}
	
	/**
	 * Scale with default center x and center y.
	 * @param x scale for x axis
	 * @param y scale for y axis
	 */
	public void scale(float x, float y) {
		scale(x, y, scaleCenter[0], scaleCenter[1]);
	}

	/**
	 * Move to given coordinates.
	 * 
	 * @param x x position of the scene
	 * @param y y position of the scene
	 */
	public void move(int x, int y) {
		for(Shape child : children) {
			child.moveRelative(x - getRealX(), y - getRealY());
		}
		translate(x - getRawX(), y - getRawY(), z);
	}
	
	/**
	 * Move to given X
	 * 
	 * @param x x position of the scene
	 */
	public void moveX(int x) {
		for(Shape child : children) {
			child.moveRelativeX(x - getRealX());
		}
		translateParams[0] = x - getRawX();
	}
	
	/**
	 * Move to given Y
	 * 
	 * @param y y position of the scene
	 */
	public void moveY(int y) {
		for(Shape child : children) {
			child.moveRelativeY(y - getRealY());
		}
		translateParams[1] = y - getRawY();
	}
	
	/**
	 * Move to given Z. This operation has no effects on the orthogonal scene.
	 * 
	 * @param z z position of the scene.
	 */
	public void moveZ(int z) {
		this.z = z;
	}
	
	/**
	 * Move relatively against current position.
	 */
	public void moveRelative(int x, int y) {
		moveRelativeX(x);
		moveRelativeY(y);
	}
	
	/**
	 * Move relatively against current x
	 */
	public void moveRelativeX(int x) {
		moveX(x + getRealX());
	}
	
	/**
	 * Move relatively against current y
	 */
	public void moveRelativeY(int y) {
		moveY(y + getRealY());
	}
	
	protected void createBuffers() {
		float[] positions = {x,y,z, x,y+height,z, x+width,y+height,z, x+width,y,z};
		vertexBuffer = FastFloatBuffer.createBuffer(positions);		
		indiceBuffer = ShortBuffer.wrap(RECTANGLE_INDICE);
	}
	
	protected void applyParams(GL10 gl) {
		// translate
		gl.glTranslatef(translateParams[0], translateParams[1], translateParams[2]);
		
		// rotate
		gl.glTranslatef(rotateParams[1], rotateParams[2], 0);
		if (rotateParams[3] == AXIS_X) {
			gl.glRotatef(rotateParams[0], 1, 0, 0);
		} else if (rotateParams[3] == AXIS_Y) {
			gl.glRotatef(rotateParams[0], 0, 1, 0);
		} else {
			gl.glRotatef(rotateParams[0], 0, 0, 1);
		}
		gl.glTranslatef(-rotateParams[1], -rotateParams[2], 0);
		
		// scale
		gl.glTranslatef(scaleParams[2], scaleParams[3], 0);
		gl.glScalef(scaleParams[0], scaleParams[1], 1);
		gl.glTranslatef(-scaleParams[2], -scaleParams[3], 0);
		
		GLHelper.checkError(gl);
	}

	/**
	 * Set visible status of the shape.
	 * The visible status is no relation to alpha color.
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
		for(Shape child : children) {
			child.setVisible(visible);
		}
	}
	
	/**
	 * Returns visible status of the shape.
	 * @return visible status of the shape
	 */
	public boolean isVisible() {
		return this.visible;
	}
	
	/**
	 * Returns angle of the shape.
	 * @return angle of the shape
	 */
	public float getAngle() {
		return rotateParams[0];
	}

	/**
	 * Returns scale x of the shape
	 * @return scale x of the shape
	 */
	public float getScaleX() {
		return scaleParams[0];
	}
	
	/**
	 * Returns scale y of the shape
	 * @return scale y of the shape 
	 */
	public float getScaleY() {
		return scaleParams[1];
	}
	
	/**
	 * Returns x position of the shape in the scene.
	 * @return
	 */
	public int getRealX() {
		return getRawX() + (int)translateParams[0];
	}
	
	/**
	 * Returns y position of the shape in the scene.
	 * @return
	 */
	public int getRealY() {
		return getRawY() + (int)translateParams[1];
	}

	/**
	 * Returns rectangle of the shape.
	 * @return rectangle of the shape
	 */
	public Rect getRect() {
		rect.left   = this.getRealX();
		rect.right  = rect.left + getWidth();
		rect.top    = this.getRealY();
		rect.bottom = rect.top + getHeight();
		
		return rect;
	}

	/**
	 * Returns collision rectangle of the shape
	 * Override this method to change the collision area.
	 * 
	 * @return collision rectangle of the shape
	 */
	public Rect getCollisionRect() {
		return getRect();
	}

	/**
	 * Utility method to hide the shape.
	 */
	public void hide() {
		this.visible = false;
		for(Shape child : children) {
			child.hide();
		}
	}

	/**
	 * Utility method to show the shape.
	 */
	public void show() {
		this.visible = true;
		for(Shape child : children) {
			child.show();
		}
	}

	/**
	 * Add event listener for the shape.
	 * @param listener ShapeEventListener
	 */
	public void addListener(ShapeEventListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Remove event listener for the shape
	 * @param listener ShapeEventListener
	 */
	public void removeListener(ShapeEventListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Returns whether the shape is collided with given x coordinate or not.
	 * 
	 * @param globalX global x coordinate
	 * @return whether x axis is collided or not.
	 */
	public boolean containsX(int globalX) {
		return (globalX >= getRealX() && globalX <= getRealX() + getWidth());
	}
	
	/**
	 * Returns whether the shape is collided with given y coordinate or not.
	 * 
	 * @param globalY global y coordinate
	 * @return whether y axis is collided or not.
	 */
	public boolean containsY(int globalY) {
		return (globalY >= getRealY() && globalY<= getRealY() + getHeight());
	}

	/**
	 * Returns whether the shape is collided with given coordinates.
	 */
	@Override
	public boolean contains(int globalX, int globalY) {
		return containsX(globalX) && containsY(globalY);
	}

	/**
	 * Returns whether the shape is collided with given shape.
	 * @param shape shape to collide
	 * @return whether the shape is collided with given shape
	 */
	public boolean collidesWith(Shape shape) {
		return collidesWith(shape.getCollisionRect());
	}
	
	/**
	 * Returns whether the shape is collided with given rectangle.
	 * @param rectB rectangle to collide
	 * @return whether the shape is collided with given rectangle
	 */
	public boolean collidesWith(Rect rectB) {
		Rect rectA = this.getCollisionRect();
		
		return (rectA.left < rectB.right && rectB.left < rectA.right 
				&& rectA.top < rectB.bottom && rectB.top < rectA.bottom);
	}

	/**
	 * Add modifier to the shape
	 * @param modifier ShapeModifier
	 */
	public void addModifier(ShapeModifier modifier) {
		loadableModifiers.add(modifier);
	}
	
	/**
	 * Remove modifier from the shape
	 * @param modifier ShapeModifier
	 */
	public void removeModifier(ShapeModifier modifier) {
		removedModifiers.add(modifier);
	}
	
	/**
	 * Clear all attached modifier from the shape.
	 */
	public void clearModifier() {
		for (ShapeModifier modifier : modifiers) {
			removeModifier(modifier);
		}
	}

	/**
	 * Add child shape to the shape.
	 * @param shape child shape
	 */
	public void addChild(Shape shape) {
		children.add(shape);
	}
	
	/**
	 * Remove child shape from the shape
	 * @param shape child shape
	 */
	public void removeChild(Shape shape) {
		children.remove(shape);
	}
	
	/**
	 * Handles scene touch event and calls onTouchEvent if the event occurs on the shape. 
	 */
	@Override
	public boolean onSceneTouchEvent(E3Scene scene, MotionEvent motionEvent) {
		if (isRemoved() || isTransparent() || !isVisible()) return false;
		
		int pointerCount = motionEvent.getPointerCount();
		
		for (int i = 0; i < pointerCount; i++) {
			int globalX = scene.getEngine().getContext().getTouchEventX(scene, motionEvent, i);
			int globalY = scene.getEngine().getContext().getTouchEventY(scene, motionEvent, i);
			int localX = globalX - getRealX();
			int localY = globalY - getRealY();
			
			if (contains(globalX, globalY)) {
				this.onTouchEvent(scene, this, motionEvent, localX, localY);
				for (ShapeEventListener listener : listeners) {
					listener.onTouchEvent(scene, this, motionEvent, localX, localY);
				}
			}
		}
		
		return false;
	}

	/**
	 * Called when the touch event occurs on the shape.
	 */
	@Override
	public boolean onTouchEvent(E3Scene scene, Shape shape,
			MotionEvent motionEvent, int localX, int localY) {
		return false;
	}

	/**
	 * Called when the shape modifier is started.
	 */
	@Override
	public void onModifierStart(ShapeModifier modifer, Shape shape) {
		// do nothing by default
	}

	/**
	 * Called when the shape modifier is finished.
	 */
	@Override
	public void onModifierFinished(ShapeModifier modifier, Shape shape) {
		// do nothing by default
	}
}
