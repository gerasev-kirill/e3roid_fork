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

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.e3roid.util.Debug;

import android.graphics.Bitmap;
import android.opengl.GLU;
import android.opengl.GLUtils;

/**
 * Utility class for wrap up the OpenGL calls
 *  The design of interface was inspired by the code by NicolasGramlich from AndEngine(www.andengine.org).
 */
public class GLHelper {	
	private static final int[] BUFFER_TO_DELETE  = new int[1];
	private static final int[] TEXTURE_TO_DELETE = new int[1];
	
	private static int currentBufferID  = -1;
	private static int currentMatrix    = -1;
	private static int currentElementBufferID = -1;
	private static int currentTextureID = -1;
	
	private static FastFloatBuffer currentTextureBuffer = null;
	private static FastFloatBuffer currentVertexBuffer  = null;
	
	private static int srcBlendMode = -1;
	private static int dstBlendMode = -1;
	
	private static boolean useLighting = false;
	private static boolean useDither    = false;
	private static boolean useDepthTest = false;
	private static boolean useMultiSample = false;
	private static boolean useBlend = false;
	private static boolean useCulling = false;
	private static boolean useColorArray = false;
	private static boolean useTextures = false;
	private static boolean useVertexArray = false;
	private static boolean useTexCoordArray = false;
	private static boolean logGLError = false;

	private static float colorRed   = -1;
	private static float colorGreen = -1;
	private static float colorBlue  = -1;
	private static float colorAlpha = -1;

	private static float clearColorRed   = -1;
	private static float clearColorGreen = -1;
	private static float clearColorBlue  = -1;
	private static float clearColorAlpha = -1;
	
	private static float lineWidth = 1.0f;
	
	private static final boolean USE_LITTLE_ENDIAN = (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN);
	
	public static void reset(GL10 gl) {
		BUFFER_TO_DELETE[0]  = -1;
		TEXTURE_TO_DELETE[0] = -1;
		
		currentBufferID = -1;
		currentMatrix = -1;
		currentElementBufferID = -1;
		
		currentTextureBuffer = null;
		currentVertexBuffer  = null;
		
		srcBlendMode = -1;
		dstBlendMode = -1;
		
		useLighting  = false;
		useDither    = false;
		useDepthTest = false;
		useMultiSample = false;
		useBlend = false;
		useCulling = false;
		useColorArray = false;
		useTextures = false;
		useVertexArray = false;
		useTexCoordArray = false;
		
		colorRed   = -1;
		colorGreen = -1;
		colorBlue  = -1;
		colorAlpha = -1;

		clearColorRed   = -1;
		clearColorGreen = -1;
		clearColorBlue  = -1;
		clearColorAlpha = -1;
		
		lineWidth = 1.0f;
	}
	
	public static void bindBuffer(GL11 gl, int bufferID) {
		if (GLHelper.currentBufferID != bufferID) {
			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, bufferID);
			GLHelper.currentBufferID = bufferID;
			checkError(gl);
		}
	}
	
	public static void bindTexture(GL11 gl, int textureID) {
		if (GLHelper.currentTextureID != textureID) {
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
			GLHelper.currentTextureID = textureID;
			checkError(gl);
		}
	}
	
	public static void resetCurrentTextureID() {
		GLHelper.currentTextureID = -1;
	}
	
	public static void deleteBuffer(GL11 gl, int bufferID) {
		GLHelper.BUFFER_TO_DELETE[0] = bufferID;
		gl.glDeleteBuffers(1, BUFFER_TO_DELETE, 0);
		checkError(gl);
	}
		
	public static void deleteTexture(GL10 gl, int textureID) {
		GLHelper.TEXTURE_TO_DELETE[0] = textureID;
		gl.glDeleteTextures(1, TEXTURE_TO_DELETE, 0);
		checkError(gl);
	}
	
	public static void bindElementBuffer(GL11 gl, int elementID) {
		if (GLHelper.currentElementBufferID != elementID) {
			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, elementID);
			GLHelper.currentElementBufferID = elementID;
			checkError(gl);
		}		
	}
	
	public static void deleteElementBuffer(GL11 gl, int elementID) {
		GLHelper.BUFFER_TO_DELETE[0] = elementID;
		gl.glDeleteBuffers(1, BUFFER_TO_DELETE, 0);
		checkError(gl);
	}
	
	public static void texCoordPointer(GL10 gl, FastFloatBuffer textureBuffer) {
		if (GLHelper.currentTextureBuffer != textureBuffer) {
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer.bytes);			
			GLHelper.currentTextureBuffer = textureBuffer;
			checkError(gl);
		}
	}
    /**
     * Passes an empty buffer for vertices, for use with VBO
     */
	public static void texCoordZeroPointer(GL11 gl) {
		gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
		checkError(gl);
	}
	public static void vertexPointer(GL10 gl, FastFloatBuffer vertexBuffer) {
		if (GLHelper.currentVertexBuffer != vertexBuffer) {
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer.bytes);
			GLHelper.currentVertexBuffer = vertexBuffer;
			checkError(gl);
		}
	}
	public static void vertexZeroPointer(GL11 gl) {
		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
		checkError(gl);
	}
	
	public static void blendMode(GL10 gl, int srcBlendMode, int dstBlendMode) {
		if(GLHelper.srcBlendMode != srcBlendMode || GLHelper.dstBlendMode != dstBlendMode) {
			gl.glBlendFunc(srcBlendMode, dstBlendMode);
			GLHelper.srcBlendMode = srcBlendMode;
			GLHelper.dstBlendMode = dstBlendMode;
			checkError(gl);
		}
	}

	public static void switchToModelViewMatrix(final GL10 gl) {
		GLHelper.switchToModelViewMatrix(gl, false);
		checkError(gl);
	}
	
	public static void switchToModelViewMatrix(final GL10 gl, boolean loadIdentity) {
		if(GLHelper.currentMatrix != GL10.GL_MODELVIEW) {
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			GLHelper.currentMatrix = GL10.GL_MODELVIEW;
			checkError(gl);
		}
		if (loadIdentity) {
			gl.glLoadIdentity();
			checkError(gl);
		}
	}

	public static void switchToProjectionMatrix(GL10 gl) {
		GLHelper.switchToProjectionMatrix(gl, false);
		checkError(gl);
	}
	
	public static void switchToProjectionMatrix(GL10 gl, boolean loadIdentity) {
		if(GLHelper.currentMatrix != GL10.GL_PROJECTION) {
			gl.glMatrixMode(GL10.GL_PROJECTION);
			GLHelper.currentMatrix = GL10.GL_PROJECTION;
			checkError(gl);
		}
		if (loadIdentity) {
			gl.glLoadIdentity();
			checkError(gl);
		}
	}

	public static void hintPerspectiveCorrectionAndFastest(GL10 gl) {
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		checkError(gl);
	}
	
	public static void hintPerspectiveCorrectionAndNicest(GL10 gl) {
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		checkError(gl);
	}

	public static void bufferFloatData(GL11 gl, int size, FastFloatBuffer buffer, int usage) {
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, 4 * size, buffer.bytes, usage);
		checkError(gl);
	}
	
	public static void bufferElementShortData(GL11 gl, int size, ShortBuffer buffer, int usage) {
		gl.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, 2 * size, buffer, usage);
		checkError(gl);
	}
		
	public static void setColor(GL10 gl, float red, float green, float blue, float alpha) {
		if (red != GLHelper.colorRed || green != GLHelper.colorGreen 
				|| blue != GLHelper.colorBlue || alpha != GLHelper.colorAlpha) {
			gl.glColor4f(red, green, blue, alpha);
			GLHelper.colorRed   = red;
			GLHelper.colorGreen = green;
			GLHelper.colorBlue  = blue;
			GLHelper.colorAlpha = alpha;
			checkError(gl);
		}
	}
	
	public static void lineWidth(GL10 gl, float width) {
		if (lineWidth == width) return;
		gl.glLineWidth(width);
		GLHelper.lineWidth = width;
		checkError(gl);
	}
	
	public static void clearColor(GL10 gl, float red, float green, float blue, float alpha) {
		if (red != GLHelper.clearColorRed || green != GLHelper.clearColorGreen
				|| blue != GLHelper.clearColorBlue || alpha != GLHelper.clearColorAlpha) {
			gl.glClearColor(red, green, blue, alpha);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			GLHelper.clearColorRed   = red;
			GLHelper.clearColorGreen = green;
			GLHelper.clearColorBlue  = blue;
			GLHelper.clearColorAlpha = alpha;
			checkError(gl);
		}
	}
	
	public static void texSubImage2D(GL10 gl, int target, int level, 
			int xoffset, int yoffset, Bitmap bitmap, int format, int type) {
		GLHelper.texSubImage2D(gl, target, level, xoffset, yoffset, bitmap, format, type, true);
	}
	
	public static void texSubImage2D(GL10 gl, int target, int level, 
			int xoffset, int yoffset, Bitmap bitmap, int format, int type, boolean usePreMultiplyAlpha) {
		if (usePreMultiplyAlpha) {
			GLUtils.texSubImage2D(target, level, xoffset, yoffset, bitmap, format, type);
		} else {
			int[] pixels = GLHelper.getPixels(bitmap);
			Buffer pixelBuffer = GLHelper.convertARGBtoRGBABuffer(pixels);
			gl.glTexSubImage2D(target, level, xoffset, yoffset, bitmap.getWidth(), bitmap.getHeight(), format, type, pixelBuffer);
		}
		checkError(gl);
	}
	
	public static void enableColorArray(GL10 gl, boolean enable) {
		if (enable == GLHelper.useColorArray) return;
		if (enable) {
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);		
		}
		else {
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		}
		GLHelper.useColorArray = enable;
		checkError(gl);
	}

	public static void enableVertexArray(GL10 gl, boolean enable) {
		if (enable == GLHelper.useVertexArray) return;
		if (enable) {
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);		
		}
		else {
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);		
		}
		GLHelper.useVertexArray = enable;
		checkError(gl);
	}
	
	public static void enableTexCoordArray(GL10 gl, boolean enable) {
		if (enable == GLHelper.useTexCoordArray) return;
		if (enable) {
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);		
		}
		else {
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
		GLHelper.useTexCoordArray = enable;
		checkError(gl);
	}
	
	public static void enableLighting(GL10 gl, boolean enable) {
		if (enable == GLHelper.useLighting) return;
		if (enable) {
			gl.glEnable(GL10.GL_LIGHTING);		
		}
		else {
			gl.glDisable(GL10.GL_LIGHTING);		
		}
		GLHelper.useLighting = enable;		
		checkError(gl);
	}
	
	public static void enableBlend(GL10 gl, boolean enable) {
		if (enable == GLHelper.useBlend) return;
		if (enable) {
			gl.glEnable(GL10.GL_BLEND);		
		}
		else {
			gl.glDisable(GL10.GL_BLEND);		
		}
		GLHelper.useBlend = enable;
		checkError(gl);
	}
	
	public static void enableCulling(GL10 gl, boolean enable) {
		if (enable == GLHelper.useCulling) return;
		if (enable) {
			gl.glEnable(GL10.GL_CULL_FACE);
		}
		else {
			gl.glDisable(GL10.GL_CULL_FACE);
		}
		GLHelper.useCulling = enable;
		checkError(gl);
	}
	
	public static void enableTextures(GL10 gl, boolean enable) {
		if (enable == GLHelper.useTextures) return;
		if (enable) {
			gl.glEnable(GL10.GL_TEXTURE_2D);
		}
		else {
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}
		GLHelper.useTextures = enable;
		checkError(gl);
	}
	
	public static void enableDither(GL10 gl, boolean enable) {
		if (enable == GLHelper.useDither) return;
		if (enable) {
			gl.glEnable(GL10.GL_DITHER);
		}
		else {
			gl.glDisable(GL10.GL_DITHER);
		}
		GLHelper.useDither = enable;
		checkError(gl);
	}
	
	public static void enableDepthTest(GL10 gl, boolean enable) {
		if (enable == GLHelper.useDepthTest) return;
		if (enable) {
			gl.glEnable(GL10.GL_DEPTH_TEST);
		}
		else {
			gl.glDisable(GL10.GL_DEPTH_TEST);
		}
		GLHelper.useDepthTest = enable;
		checkError(gl);
	}
	
	public static void enableMultiSample(GL10 gl, boolean enable) {
		if (enable == GLHelper.useMultiSample) return;
		if (enable) {
			gl.glEnable(GL10.GL_MULTISAMPLE);
		}
		else {
			gl.glDisable(GL10.GL_MULTISAMPLE);
		}
		GLHelper.useMultiSample = enable;
		checkError(gl);
	}

	public static void logGLError(boolean enable) {
		logGLError = enable;
	}

	public static void checkError(GL10 gl) {
		if (!logGLError) return;
		int error = gl.glGetError();
		if (error != GL10.GL_NO_ERROR) {
			String method = Thread.currentThread().getStackTrace()[3].getMethodName();
			Debug.d("Error: " + error + " (" + GLU.gluErrorString(error) + "): " + method);
		}
	}
	
	public static Buffer convertARGBtoRGBABuffer(int[] pixcels) {
		for(int i = pixcels.length - 1; i >= 0; i--) {
			int pixel = pixcels[i];

			int red = ((pixel >> 16) & 0xFF);
			int green = ((pixel >> 8) & 0xFF);
			int blue = ((pixel) & 0xFF);
			int alpha = (pixel >> 24);

			if(USE_LITTLE_ENDIAN) {
				pixcels[i] = alpha << 24 | blue << 16 | green << 8 | red;
			} else {
				pixcels[i] = red << 24 | green << 16 | blue << 8 | alpha;
			}
		}
		return IntBuffer.wrap(pixcels);
	}

	public static int[] getPixels(Bitmap bitmap) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		
		int[] pixels = new int[w * h];
		bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
		
		return pixels;
	}
	public static int[] getPixels(Bitmap bitmap, int xoffset, int yoffset, int width, int height) {
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, xoffset, yoffset, width, height);
		return pixels;
	}
}