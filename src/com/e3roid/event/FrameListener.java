package com.e3roid.event;

import javax.microedition.khronos.opengles.GL10;

import com.e3roid.E3Scene;

/**
 * onDraw listener that can listen on every frame
 */
public interface FrameListener {
	void beforeOnDraw(E3Scene scene, GL10 gl);
	void afterOnDraw(E3Scene scene, GL10 gl);
}
