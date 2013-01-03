package com.e3roid.event;

import android.view.KeyEvent;

import com.e3roid.E3Scene;

/**
 * Interface definition for a callback to be invoked when a key event is dispatched to this view. 
 * The callback will be invoked before the key event is given to the view. 
 */
public interface SceneOnKeyListener {
	boolean onKeyDown(E3Scene scene, int keyCode, KeyEvent event);
	boolean onKeyUp(E3Scene scene, int keyCode, KeyEvent event);
}
