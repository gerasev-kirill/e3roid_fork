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
package com.e3roid;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.Gravity;
import android.graphics.Typeface;
import android.widget.FrameLayout.LayoutParams;

import com.e3roid.event.SceneEventListener;
import com.e3roid.event.SceneOnKeyListener;
import com.e3roid.opengl.RenderSurfaceView;

/**
 *  A base activity class of the e3roid engine.
 *  The design of interface was inspired by the code by Nicolas Gramlich from AndEngine(www.andengine.org).
 *  
 *  @see android.app.Activity
 */
public abstract class E3Activity extends Activity implements SceneEventListener {
	/**
	 * Constant for landscape orientation 
	 */
	public static final int ORIENTATION_LANDSCAPE = 0;
	/**
	 * Constant for portrait orientation
	 */
	public static final int ORIENTATION_PORTRAIT  = 1;
	/**
	 * Constant for square orientation
	 */
	public static final int ORIENTATION_SQUARE    = 2;
	/**
	 * Constant for undefined orientation
	 */
	public static final int ORIENTATION_UNDEFINED = 3;
	
	protected E3Engine engine;
	protected RenderSurfaceView surfaceView;
	
	protected boolean paused  = false;
	protected boolean loaded  = false;
	protected boolean focused = false;
	
	protected int[] viewLocation = new int[2];
	
	protected SceneOnKeyListener sceneOnKeyListener;
	protected boolean overrideOnKeyEvent = false;
	
	//
	// protected methods
	//
	/**
	 * Load surface view and set content view of the scene.
	 */
	protected void onSetContentView() {
		this.surfaceView = onLoadSurfaceView();
		this.surfaceView.setRenderer(this.engine);
		this.setContentView(this.surfaceView, onLayoutParams());
	}
	
	/**
	 * Apply engine options like orientation and full screen.
	 */
	protected void onApplyEngineOptions() {
		this.applyScreenOrientation();
		if (engine.isFullScreen()) {
			this.requestFullScreen();
		}
	}

	/**
	 * Create surface view to render the scene.
	 * @return RenderSurfaceView
	 */
	protected RenderSurfaceView onLoadSurfaceView() {
		return new RenderSurfaceView(this);
	}
	
	/**
	 * Returns render surface view
	 * @return RenderSurfaceView
	 */
	public RenderSurfaceView getView() {
		return this.surfaceView;
	}
		
	/**
	 * Create layout parameters
	 */
	protected LayoutParams onLayoutParams() {
		LayoutParams layoutParams = new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		layoutParams.gravity = Gravity.CENTER;
		return layoutParams;
	}
	
	/**
	 * Apply screen orientation
	 */
	protected void applyScreenOrientation() {
		if (engine.isScreenOrientationLandscape()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);			
		} else if (engine.isScreenOrientationPortrait()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);			
		}
	}

	/**
	 * Request full screen feature with no title.
	 */
	protected void requestFullScreen() {
		Window window = this.getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		window.requestFeature(Window.FEATURE_NO_TITLE);
	}

	/**
	 * Called as part of the activity lifecycle when an activity is going into the background,
	 * but has not (yet) been killed. 
	 */
	protected void pause() {
		this.paused = true;
		this.engine.onPause();
		this.surfaceView.onPause();
		this.onUserPaused();
	}
	
	/**
	 * Called after onRestart(), or onPause(), 
	 * for your activity to start interacting with the user. 
	 */
	protected void resume() {
		this.paused = false;
		if (!this.loaded) {
			this.onLoadResources();
			E3Scene scene =  this.onLoadScene();
			this.engine.onLoadScene(scene);
			scene.onLoadEngine(engine);
			this.onLoadComplete();
			this.loaded = true;
		}
		this.engine.onResume();
		this.surfaceView.onResume();
		this.onUserResumed();
	}
	
	/**
	 * Perform any final cleanup before an activity is destroyed.
	 */
	protected void dispose() {
		this.onUnloadResources();
		this.onUserDisposed();
		this.engine.onDispose();
		this.loaded  = false;
		this.paused  = false;
		this.focused = false;
	}
	
	//
	// public methods
	//
	/**
	 * Returns display metrics data of the display.
	 */
	public DisplayMetrics getDisplayMetrics() {
		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics;
	}

	/**
	 * Create a new typeface from the specified font data.
	 */
	public Typeface getTypeface(String path) {
		return Typeface.createFromAsset(getAssets(), path);
	}
	
	/**
	 * Return context of this activity.
	 * 
	 * @return Context interface to global information about an application environment.
	 */
	public Context getContext() {
		return this;
	}
	
	/**
	 * Returns e3roid engine of this activity.
	 * The engine is available after onLoadEngine() is called.
	 * 
	 * @return e3roid engine 
	 */
	public E3Engine getEngine() {
		return this.engine;
	}

	/**
	 * Returns width of the scene.
	 * This method is available after onLoadEngine() is called.
	 * 
	 * @return width of the scene.
	 */
	public int getWidth() {
		return engine.getWidth();
	}
	
	/**
	 * Returns height of the scene.
	 * This method is available after onLoadEngine() is called.
	 * 
	 * @return height of the scene.
	 */
	public int getHeight() {
		return engine.getHeight();
	}
	
	/**
	 * Post the specified action to event queue to run on the scene update thread.
	 * 
	 * @param runnable the action to run on the scene update thread.
	 */
	public void postUpdate(Runnable runnable) {
		this.engine.postUpdate(runnable);
	}
	
	/**
	 * Overall orientation of the screen.
	 * 
	 * @return One of ORIENTATION_LANDSCAPE, ORIENTATION_PORTRAIT, ORIENTATION_SQUARE, ORIENTATION_UNDEFINED
	 */
	public int getScreenOrientation() {
		Configuration config = getResources().getConfiguration();
		if(config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			return ORIENTATION_LANDSCAPE;
		} else if(config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			return ORIENTATION_PORTRAIT;
		} else if(config.orientation == Configuration.ORIENTATION_SQUARE) {
			return ORIENTATION_SQUARE;
		}
		return ORIENTATION_UNDEFINED;
	}

	/**
	 * Returns the X coordinate of this event for the given scene.
	 * 
	 * @param scene e3roid scene on which the event occurs.
	 * @param event MotionEvent being processed.
	 * @return the X coordinate of this event for the given scene
	 */
	public int getTouchEventX(E3Scene scene, MotionEvent event) {
		return getTouchEventX(scene, event, 0);
	}
	
	/**
	 * Returns the X coordinate of this event for the given scene.
	 * 
	 * @param scene e3roid scene on which the event occurs.
	 * @param event MotionEvent being processed.
	 * @param pointer touch pointer
	 * @return the X coordinate of this event for the given scene
	 */
	public int getTouchEventX(E3Scene scene, MotionEvent event, int pointer) {
		surfaceView.getLocationOnScreen(viewLocation);
		return scene.getEventX(event, viewLocation[0], surfaceView.getMeasuredWidth(), pointer);
	}
	
	/**
	 * Returns the Y coordinate of this event for the given scene.
	 * 
	 * @param scene e3roid scene on which the event occurs.
	 * @param event MotionEvent being processed.
	 * @return the Y coordinate of this event for the given scene
	 */
	public int getTouchEventY(E3Scene scene, MotionEvent event) {
		return getTouchEventY(scene, event, 0);
	}
	
	/**
	 * Returns the Y coordinate of this event for the given scene.
	 * 
	 * @param scene e3roid scene on which the event occurs.
	 * @param event MotionEvent being processed.
	 * @param pointer touch pointer
	 * @return the Y coordinate of this event for the given scene
	 */
	public int getTouchEventY(E3Scene scene, MotionEvent event, int pointer) {
		surfaceView.getLocationOnScreen(viewLocation);
		return scene.getEventY(event, viewLocation[1], surfaceView.getMeasuredHeight(), pointer);
	}
	
	/**
	 * Called when a key was pressed down and not handled by any of the views inside of the activity.
	 * 
	 * @param scene e3roid scene on which the event occurs.
	 * @param keyCode The value in event.getKeyCode()
	 * @param event Description of the key event
	 * @return Return true to prevent this event from being propagated further, or false to indicate that you have not handled this event and it should continue to be propagated.
	 */
	public boolean onKeyDown(E3Scene scene, int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * Called when a key was released and not handled by any of the views inside of the activity.
	 * 
	 * @param scene e3roid scene on which the event occurs.
	 * @param keyCode The value in event.getKeyCode()
	 * @param event Description of the key event
	 * @return Return true to prevent this event from being propagated further, or false to indicate that you have not handled this event and it should continue to be propagated.
	 */
	public boolean onKeyUp(E3Scene scene, int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}
	
	/**
	 * Called when the trackball was moved and not handled by any of the views inside of the activity.
	 * 
	 * @param scene e3roid scene on which the event occurs.
	 * @param event The trackball event being processed.
	 * @return
	 */
	public boolean onTrackballEvent(E3Scene scene, MotionEvent motionEvent) {
		return super.onTrackballEvent(motionEvent);
	}
	
	//
	// Override methods
	//
	/**
	 * Called when the activity is starting.
	 * Overriding this method is not recommended.
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.paused = true;
		this.engine = this.onLoadEngine();
		onApplyEngineOptions();
		onSetContentView();
	}

	/**
	 * Called after onRestart(), or onPause(), 
	 * for your activity to start interacting with the user. 
	 * 
	 * Overriding this method is not recommended, override resume() instead if necessary.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (this.paused && this.focused) {
			this.resume();
		}
	}

	/**
	 * Called when the current Window of the activity gains or loses focus.
	 */
	@Override
	public void onWindowFocusChanged(final boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			if (this.paused) {
				this.resume();
			}
		} else {
			if (!this.paused) {
				this.pause();
			}
		}
		this.focused = hasFocus;
	}

	/**
	 * Called as part of the activity lifecycle when an activity is going into the background,
	 * but has not (yet) been killed. 
	 * 
	 * Overriding this method is not recommended, override resume() instead if necessary.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (!this.paused) {
			this.pause();
		}
	}

	/**
	 * Perform any final cleanup before an activity is destroyed.
	 * 
	 * Overriding this method is not recommended, override dispose() instead if necessary.
	 */
	@Override
	protected void onDestroy() {
		this.dispose();
		super.onDestroy();
	}
	
	/**
	 * Called when a touch screen event was not handled by any of the views under it.
	 * 
	 * Overriding this method is not recommended, use SceneEventListener if necessary.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = false;
		if (this.engine != null) {
			handled = this.engine.onTouchEvent(event);
		}
        try {
        	Thread.sleep(16);
        } catch (Exception e) { }
		return handled;
	}
	
	/**
	 * Sets key input listener for receiving keyboard input.
	 * @param listener SceneOnKeyListener
	 * @param override Override the key event. If true, the activity key event will not pump up when listener returns true.
	 */
	public void setOnKeyListener(SceneOnKeyListener listener, boolean override) {
		this.overrideOnKeyEvent = override;
		this.sceneOnKeyListener = listener;
	}
	
	/**
	 * Called when a key was pressed down and not handled by any of the views inside of the activity.
	 * 
	 * Overriding this method is not recommended, override onKeyDown(E3Scene,int,KeyEvent) if necessary.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (sceneOnKeyListener != null) {
			boolean done = sceneOnKeyListener.onKeyDown(getEngine().getScene(), keyCode, event);
			if (overrideOnKeyEvent && done) return done;
		}
		return onKeyDown(getEngine().getScene(), keyCode, event);
	}
	
	/**
	 * Called when a key was released and not handled by any of the views inside of the activity.
	 * 
	 * Overriding this method is not recommended, override onKeyUp(E3Scene,int,KeyEvent) if necessary.
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (sceneOnKeyListener != null) {
			boolean done = sceneOnKeyListener.onKeyUp(getEngine().getScene(), keyCode, event);
			if (done) return done;
		}
		return onKeyUp(getEngine().getScene(), keyCode, event);
	}
	
	/**
	 * Called when the trackball was moved and not handled by any of the views inside of the activity.
	 * 
	 * Overriding this method is not recommended, override onTrackballEvent(E3Scene,MotionEvent) if necessary.
	 */
	@Override
	public boolean onTrackballEvent(MotionEvent motionEvent) {
		return onTrackballEvent(getEngine().getScene(), motionEvent);
	}

	/**
	 * Called when the scene touch event occurs.
	 * Override this method to handle touch event.
	 */
	@Override
	public boolean onSceneTouchEvent(E3Scene scene, MotionEvent motionEvent) {
		return super.onTouchEvent(motionEvent);
	}

	/**
	 * Called when loading e3roid engine. 
	 * @return E3Engine
	 */
	public abstract E3Engine onLoadEngine();
	
	/**
	 * Called when loading e3roid scene
	 * @return E3Scene
	 */
	public abstract E3Scene onLoadScene();
	
	/**
	 * Called when loading resources.
	 */
	public abstract void onLoadResources();
	
	/**
	 * Called before the activity disposes.
	 * Override this method to unload resources if necessary.
	 */
	public void onUnloadResources() {
		// nothing to do
	}

	/**
	 * Called after loading E3Engine has completed. 
	 * Override this method to finish initialization after engine has loaded if necessary.
	 */
	public void onLoadComplete() {
		// nothing to do
	}

	/**
	 * Called before the activity is going to background.
	 * Override this method to release foreground resources if necessary.
	 */
	public void onUserPaused() {
		// nothing to do
	}

	/**
	 * Called before the activity is going to foreground.
	 * Override this method to prepare to restart the activity if necessary.
	 */
	public void onUserResumed() {
		// nothing to do
	}
	
	/**
	 * Called before the activity is going to dispose.
	 * Override this method to dispose the activity if necessary.
	 */
	public void onUserDisposed() {
		
	}
}
