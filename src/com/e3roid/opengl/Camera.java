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
package com.e3roid.opengl;

import javax.microedition.khronos.opengles.GL10;
import android.os.SystemClock;

import com.e3roid.drawable.modifier.function.Linear;
import com.e3roid.drawable.modifier.function.Progressive;

import android.opengl.GLU;

/**
 * A Camera class wraps up the OpenGL matrix and lookat calls.
 */
public class Camera {

	private boolean usePerspective = false;
	private int width;
	private int height;
	
	private float eyeX = 0;
	private float eyeY = 0;
	private float eyeZ = 0;
	private float centerX = 0;
	private float centerY = 0;
	private float centerZ = -1;
	
	private boolean zooming = false;
	
	private int zoomFromWidth;
	private int zoomFromHeight;
	private int zoomCenterX;
	private int zoomCenterY;
	private float currentZoom = 1.0f;
	private float targetZoom  = 1.0f;
	
	private long zoomLastTime = 0;
	private long zoomDuration = 0;
	private Progressive zoomFunction;
	
	public Camera() {
		
	}
	public Camera(int width, int height) {
		setView(width, height);
	}
	
	public void setView(int width, int height) {
		this.width  = width;
		this.height = height;
	}
	
	public void reloadMatrix(GL10 gl) {
        GLHelper.switchToProjectionMatrix(gl, true);
        if (usePerspective) {
        	float ratio = (float)width/height;
        	float zfar = Math.max(height, width);
        	gl.glFrustumf(-ratio, ratio, 1, -1, 1, zfar+(zfar/10));
        } else {
        	gl.glOrthof(0, width, height, 0, -1f, 1f);
        }
	}
	
	public void zoom(float zoomFactor, long duration, int centerX, int centerY) {
		zoom(zoomFactor, duration, centerX, centerY, Linear.getInstance());
	}
	public void zoom(float zoomFactor, long duration, int centerX, int centerY, Progressive zoomFunction) {
		this.zoomFunction = zoomFunction;
		this.targetZoom = zoomFactor;
		
		this.zoomCenterX = centerX;
		this.zoomCenterY = centerY;
		
		this.zoomDuration = duration;
		this.zoomLastTime = SystemClock.uptimeMillis();
		
		this.zoomFromWidth  = width;
		this.zoomFromHeight = height;
		
		zooming = true;
	}
	
	public void switchToOrtho(GL10 gl) {
    	gl.glOrthof(0, width, height, 0, -1f, 1f);
	}
	
	public void lookAtOrthoCenter(GL10 gl) {
		GLU.gluLookAt(gl, 0, 0, 0, 0, 0, -1, 0, 1, 0);
	}
	
	public void look(GL10 gl) {
		reloadMatrix(gl);
		
		if (zooming) {
			long elapsed = SystemClock.uptimeMillis() - this.zoomLastTime;
			if (elapsed <= zoomDuration) {
				currentZoom = (float) (1.0 + (zoomFunction.getProgress(elapsed, zoomDuration, 0, 1) * (targetZoom - 1.0)));
			} else {
				currentZoom = targetZoom;
				zooming = false;
			}
			setView((int)(zoomFromWidth / currentZoom), (int)(zoomFromHeight / currentZoom));
			
			float eyePointX = zoomCenterX - (zoomFromWidth / currentZoom / 2);
			float eyePointY = zoomCenterY - (zoomFromHeight / currentZoom / 2);
			
			this.eyeX = eyePointX;
			this.eyeY = eyePointY;
			this.centerX = eyePointX;
			this.centerY = eyePointY;
		}
		
		if (usePerspective) {
			lookPerspective(gl);
		} else {
			lookOrtho(gl);
		}
	}
	
	public void lookOrtho(GL10 gl) {
		GLU.gluLookAt(gl, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, 0, 1, 0);
	}
	
	public void lookPerspective(GL10 gl) {
		GLU.gluLookAt(gl, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, 0, 1, 0);
	}
	
	public void defaultLook() {
		if (usePerspective) {
			eyeX = width * 0.5f;
			eyeY = height + (height / 10);
			eyeZ = height * 0.5f;
			
			centerX = width * 0.5f;
			centerY = height * 0.5f;
			centerZ = 0;
		} else {
			eyeX = 0;
			eyeY = 0;
			eyeZ = 0;
			centerX = 0;
			centerY = 0;
			centerZ = -1;
		}
	}
	
	public void enablePerspective(boolean enable) {
		this.usePerspective = enable;
	}
	
	public boolean isPerspective() {
		return this.usePerspective;
	}
	
	public void moveEye(float x, float y, float z) {
		eyeX = x;
		eyeY = y;
		eyeZ = z;
	}
	
	public void moveCenter(float x, float y, float z) {
		centerX = x;
		centerY = y;
		centerZ = z;
	}
	
	public float getEyeX() {
		return eyeX;
	}
	
	public float getEyeY() {
		return eyeY;
	}
	
	public float getEyeZ() {
		return eyeZ;
	}
	
	public float getCenterX() {
		return centerX;
	}
	
	public float getCenterY() {
		return centerY;
	}
	
	public float getCenterZ() {
		return centerZ;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
}
