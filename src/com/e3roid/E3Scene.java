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

import javax.microedition.khronos.opengles.GL10;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.SystemClock;
import android.view.MotionEvent;

import com.e3roid.drawable.Drawable;
import com.e3roid.drawable.Layer;
import com.e3roid.drawable.Shape;
import com.e3roid.interfaces.IWidget;
import com.e3roid.lifecycle.E3LifeCycle;
import com.e3roid.event.FrameListener;
import com.e3roid.event.SceneEventListener;
import com.e3roid.event.SceneUpdateListener;
import com.e3roid.opengl.GLHelper;

/**
 * Represents drawable scene that contains background and layers 
 */
public class E3Scene implements E3LifeCycle {

	protected static final String WIDGET_LAYER = "com.e3roid.drawable.Widget";
	
	protected E3Engine engine;
	protected Layer backgroundLayer = new Layer();
	protected ArrayList<Layer> layers = new ArrayList<Layer>();
	protected ArrayList<Layer> removedLayers = new ArrayList<Layer>();
	protected ArrayList<Layer> loadableLayers = new ArrayList<Layer>();
	protected ArrayList<Shape> huds = new ArrayList<Shape>();
	protected ArrayList<Shape> removedHuds = new ArrayList<Shape>();
	protected ArrayList<Shape> loadableHuds = new ArrayList<Shape>();
	protected ArrayList<SceneEventListener> eventListeners = new ArrayList<SceneEventListener>();
	protected HashMap<SceneUpdateListener, UpdateHandler> updateListeners = new HashMap<SceneUpdateListener, UpdateHandler>();
	protected HashMap<String, Layer> namedLayers = new HashMap<String, Layer>();
	protected ArrayList<FrameListener> frameListeners = new ArrayList<FrameListener>();
	
	// onUpdateScene event listeners property.
	// if updateIntervalMsec = 0, update event won't happen.
	protected long lastUpdateMsec = 0;
	protected long updateIntervalMsec = 0;
	protected long pausedTimeElapsed = 0;
	
	protected String currentLayerName;
	protected boolean paused = false;
	protected boolean reloadScene = false;
	protected boolean reloadHUD = false;
	
	protected float[] color = {
			1.0f, 1.0f, 1.0f, 1.0f
		};
	
	/**
	 * Create scene with background.
	 */
	public E3Scene() {
		addLayer(backgroundLayer);
	}
	
	/**
	 * Called to draw current frame. This method is responsible for drawing the current frame. 
	 * @param gl the OpenGL interface
	 */
	public void onDraw(GL10 gl) {

		// invoke frame listeners
		if (!frameListeners.isEmpty()) {
			for (FrameListener listener : frameListeners) {
				listener.beforeOnDraw(this, gl);
			}
		}
		
		// initialize layers
		if (!loadableLayers.isEmpty()) {
			for (Layer layer : loadableLayers) {
				layer.onLoadEngine(engine);
				layer.onLoadSurface(gl);
				layers.add(layer);
			}		
			loadableLayers.clear();
		}
		
		// initialize HUD
		if (!loadableHuds.isEmpty()) {
			for (Shape hud : loadableHuds) {
				hud.onLoadEngine(engine);
				hud.onLoadSurface(gl);
				huds.add(hud);
			}
			loadableHuds.clear();
		}
        
		// Called when onResume() after onPause()
		if (reloadScene && reloadHUD) {
			for (Shape hud : huds) {
				hud.onLoadSurface(gl, true);
			}
			reloadScene = false;
			reloadHUD   = false;
		}
		
        if (currentLayerName != null && namedLayers.containsKey(currentLayerName)) {
        	prepareDraw(gl);
        	
        	Layer namedLayer = namedLayers.get(currentLayerName);
        	
        	if (namedLayer != null) {
        		if (!namedLayer.isLoaded() && !namedLayer.isEngineLoaded()) {
        			namedLayer.onLoadEngine(engine);
        			namedLayer.onLoadSurface(gl);
        		}
        	
        		if (!namedLayer.isRemoved()) {
        			namedLayer.onDraw(gl);
        		} else {
    				namedLayers.remove(namedLayer);
        		}
        	}
        } else if (!paused){
        	prepareDraw(gl);
        	
        	// check update handler
        	if (updateIntervalMsec > 0 && SystemClock.uptimeMillis() - lastUpdateMsec > updateIntervalMsec) {
        		for (SceneUpdateListener listener : updateListeners.keySet()) {
        			UpdateHandler handler = updateListeners.get(listener);
        			if (handler.interval() <= 0) continue;
        			long now = SystemClock.uptimeMillis();
        			long elapsed = handler.elapsed(now);
        			if (elapsed > handler.interval()) {
        				listener.onUpdateScene(this, elapsed);
        				handler.update(now);
        			}
        		}
        		lastUpdateMsec = SystemClock.uptimeMillis();
        	}

        	// draw layers
        	for (Layer layer : layers) {
        		layer.onDraw(gl);
        	}

        	// draw HUD
        	GLHelper.switchToProjectionMatrix(gl, true);
        	engine.getCamera().switchToOrtho(gl);
        	engine.getCamera().lookAtOrthoCenter(gl);
        	GLHelper.switchToModelViewMatrix(gl, true);
        	for (Shape hud : huds) {
        		hud.onDraw(gl);
        	}
        }
        
        // remove layers
        if (!removedLayers.isEmpty()) {
			for (Layer layer : removedLayers) {
				layer.onDispose();
				layers.remove(layer);
			}
			removedLayers.clear();        	
        }
        // remove HUD
		if (!removedHuds.isEmpty()) {
			for (Shape hud : removedHuds) {
				hud.onDispose();
				huds.remove(hud);
			}
			removedHuds.clear();
		}
		
		// invoke frame listeners
		if (!frameListeners.isEmpty()) {
			for (FrameListener listener : frameListeners) {
				listener.afterOnDraw(this, gl);
			}
		}
	}
	
	protected void prepareDraw(GL10 gl) {
		gl.glClearDepthf(1.0f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        GLHelper.clearColor(gl, color[0], color[1], color[2], color[3]);
        
        GLHelper.switchToModelViewMatrix(gl);
	}
	
	/**
	 * Resume drawing 
	 */
	public void resume() {
		for (Layer layer : layers) {
			layer.onResume();
		}
		for (Shape hud : huds) {
			hud.onResume();
		}
		for (Layer layer : namedLayers.values()) {
			layer.onResume();
		}
		this.lastUpdateMsec = SystemClock.uptimeMillis() - this.pausedTimeElapsed;
		this.paused = false;
	}
	
	/**
	 * Pause drawing
	 */
	public void pause() {
		for (Layer layer : layers) {
			layer.onPause();
		}
		for (Shape hud : huds) {
			hud.onPause();
		}
		for (Layer layer : namedLayers.values()) {
			layer.onPause();
		}
		this.pausedTimeElapsed = SystemClock.uptimeMillis() - this.lastUpdateMsec;
		this.paused = true;
	}

	/*
	 * Reload all buffers of the scene.
	 */
	public void forceReloadScene() {
		reloadScene = true;
		reloadLayers();
	}
	
	protected void reloadLayers() {
		if (reloadScene) {
			for (Layer layer : layers) {
				layer.setLoaded(false);
			}
			for (Layer layer : namedLayers.values()) {
				layer.setLoaded(false);
			}
			reloadHUD = true;
		}
	}
	
	/**
	 * Called when the activity's onResume event occurs.
	 * This is system resume event, use resume() to resume drawing.
	 */
	@Override
	public void onResume() {
		reloadLayers();
	}

	/**
	 * Called when the activity's onPause event occurs.
	 * This is system pause event, use pause() to pause drawing.
	 */
	@Override
	public void onPause() {
		this.reloadScene = true;
	}

	/**
	 * Called when the activity's onDispose event occurs.
	 */
	@Override
	public void onDispose() {
		for (Layer layer : layers) {
			layer.onDispose();
		}
		for (Shape hud : huds) {
			hud.onDispose();
		}
		for (Layer layer : namedLayers.values()) {
			layer.onDispose();
		}
	}

	/**
	 * Add drawable layer to the scene
	 * 
	 * @param layer Layer to add to the scene.
	 */
	public void addLayer(Layer layer) {
		loadableLayers.add(layer);
	}

	/**
	 * Remove drawable layer of the scene
	 * 
	 * @param layer Layer to remove from the scene.
	 */
	public void removeLayer(Layer layer) {
		layer.onRemove();
		removedLayers.add(layer);
	}
	
	/**
	 * Add drawable layer with given name. 
	 * Use setCurrentLayerName to activate the layer.
	 * 
	 * @param name Name of the layer
	 * @param layer
	 */
	public void addNamedLayer(String name, Layer layer) {
		namedLayers.put(name, layer);
	}
	
	/**
	 * Remove drawable layer with given name.
	 * 
	 * @param name Name of the layer
	 */
	public void removeNamedLayer(String name) {
		Layer layer = namedLayers.get(name);
		if (layer != null) {
			layer.onRemove();
		}
	}

	/**
	 * Change current layer to layer with given name  
	 * @param name Name of the layer
	 */
	public void setCurrentLayerName(String name) {
		this.currentLayerName = name;
	}
	
	/**
	 * Show widget if widget exists. 
	 */
	public void showWidget() {
		setCurrentLayerName(WIDGET_LAYER);
	}

	/**
	 * Hide widget and reset to default layer
	 */
	public void hideWidget() {
		resetNamedLayer();
	}
	
	/**
	 * Indicates whether widget is visible or not. 
	 * @return
	 */
	public boolean isWidgetVisible() {
		return WIDGET_LAYER.equals(getCurrentLayerName());
	}
	
	/**
	 * Returns current layer name if named layer exists.
	 * @return current layer name or null if no named layer exists.
	 */
	public String getCurrentLayerName() {
		return currentLayerName;
	}

	/**
	 * reset layer name and change to default to layer
	 */
	public void resetNamedLayer() {
		this.currentLayerName = null;
	}
	
	/**
	 * Returns the layer for background.
	 * @return
	 */
	public Layer getBackgroundLayer() {
		return backgroundLayer;
	}

	/**
	 * Returns top layer of this scene. Top layer is rendered first.
	 * @return
	 */
	public Layer getTopLayer() {
		if (layers.size() == 0) {
			return backgroundLayer;
		}
		return layers.get(layers.size() - 1);
	}

	/**
	 * Add HUD Shape that is not owned by any layer.
	 * @param shape Shape object to add to HUD.
	 */
	public void addHUD(Shape shape) {
		loadableHuds.add(shape);
	}

	/**
	 * Remove HUD Shape
	 * @param shape Shape object to remove from HUD
	 */
	public void removeHUD(Shape shape) {
		shape.onRemove();
		removedHuds.add(shape);
	}
	
	/**
	 * Set background color
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public void setBackgroundColor(float red, float green, float blue, float alpha) {
		this.color[0] = red;
		this.color[1] = green;
		this.color[2] = blue;
		this.color[3] = alpha;
	}
	
	/**
	 * Set background color
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void setBackgroundColor(float red, float green, float blue) {
		setBackgroundColor(red, green, blue, this.color[3]);
	}

	/**
	 * Called when touch screen motion event occurs.
	 * 
	 * @param event MotionEvent
	 * @return True if the event was handled, false otherwise. 
	 */
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = false;
		for (SceneEventListener listener : eventListeners) {
			handled = listener.onSceneTouchEvent(this, event);
			if (handled) break;
		}
		return handled;
	}
	
	/**
	 * Returns first drawable at given coordinates.
	 * 
	 * @param x The x position within the scene
	 * @param y The y position within the scene.
	 * @return Drawable object if exist, null otherwise.
	 */
	public Drawable findDrawableAt(int x, int y) {
		for (Layer layer : layers) {
			Drawable drawable = layer.findDrawableAt(x, y);
			if (drawable != null) return drawable;
		}
		return null;
	}
	
	/**
	 * Returns all drawables at given coordinates.
	 * 
	 * @param x The x position within the scene
	 * @param y The y position within the scene.
	 * @return Drawable objects if exist, empty list otherwise.
	 */
	public List<Drawable> findDrawablesAt(int x, int y) {
		ArrayList<Drawable> drawables = new ArrayList<Drawable>();
		for (Layer layer : layers) {
			drawables.addAll(layer.findDrawablesAt(x, y));
		}
		return drawables;
	}

	/**
	 * Called when e3roid engine is loaded.
	 * 
	 * @param engine E3Engine
	 */
	public void onLoadEngine(E3Engine engine) {
		this.engine = engine;
	}

	/**
	 * Returns E3Engine of the scene.
	 * @return
	 */
	public E3Engine getEngine() {
		return this.engine;
	}

	/**
	 * Returns width of the scene.
	 * @return width of the scene
	 */
	public int getWidth() {
		return this.engine.getWidth();
	}
	
	/**
	 * Returns height of the scene.
	 * @return height of the scene
	 */
	public int getHeight() {
		return this.engine.getHeight();
	}
	
	/**
	 * Returns the X coordinate of this event for the given motion event.
	 * 
	 * @param event MotionEvent
	 * @param viewLocationX x position of the view
	 * @param measuredWidth measured width of the view
	 * @return the X coordinate of this event within the scene.
	 */
	public int getEventX(MotionEvent event, int viewLocationX, float measuredWidth) {
		return getEventX(event, viewLocationX, measuredWidth, 0);
	}
	
	/**
	 * Returns the X coordinate of this event for the given motion event.
	 * 
	 * @param event MotionEvent
	 * @param viewLocationX x position of the view
	 * @param measuredWidth measured width of the view
	 * @param pointer touch pointer
	 * @return the X coordinate of this event within the scene.
	 */
	public int getEventX(MotionEvent event, int viewLocationX, float measuredWidth, int pointer) {
		float x = event.getX(pointer) - viewLocationX;
		return (int)(x * ((float)engine.getWidth() / measuredWidth));
	}
	
	/**
	 * Returns the Y coordinate of this event for the given motion event.
	 * 
	 * @param event MotionEvent
	 * @param viewLocationY y position of the view
	 * @param measuredWidth measured width of the view
	 * @return the Y coordinate of this event within the scene.
	 */
	public int getEventY(MotionEvent event, int viewLocationY, float measuredHeight) {
		return getEventY(event, viewLocationY, measuredHeight, 0);
	}
	
	/**
	 * Returns the Y coordinate of this event for the given motion event.
	 * 
	 * @param event MotionEvent
	 * @param viewLocationY y position of the view
	 * @param measuredWidth measured width of the view
	 * @param pointer touch pointer
	 * @return the Y coordinate of this event within the scene.
	 */
	public int getEventY(MotionEvent event, int viewLocationY, float measuredHeight, int pointer) {
		float y = event.getY(pointer) - viewLocationY;
		return (int)(y * ((float)engine.getHeight() / measuredHeight));
	}
	
	/**
	 * Add scene event listener
	 * 
	 * @param listener SceneEventListener
	 */
	public void addEventListener(SceneEventListener listener) {
		eventListeners.add(listener);
	}
	
	/**
	 * Remove scene event listener.
	 * 
	 * @param listener SceneEventListener
	 */
	public void removeEventListener(SceneEventListener listener) {
		eventListeners.remove(listener);
	}
	
	/**
	 * Add given IWidget item to widget of the scene.
	 * 
	 * @param widgetItem the widget item
	 */
	public void addWidget(IWidget widgetItem) {
		Layer layer = namedLayers.get(WIDGET_LAYER);
		if (layer == null) {
			layer = new Layer();
		}
		
		layer.add(widgetItem);
		addNamedLayer(WIDGET_LAYER, layer);
	}

	/**
	 * Remove given widget item from widget of the scene 
	 * 
	 * @param widgetItem the widget item
	 */
	public void removeWidget(IWidget widgetItem) {
		Layer layer = namedLayers.get(WIDGET_LAYER);
		
		if (layer != null) {
			layer.remove(widgetItem);
			if (layer.size() == 0) {
				removeNamedLayer(WIDGET_LAYER);
			}
		}
	}

	/**
	 * Add frame listener
	 * @param listener FrameListener
	 */
	public void addFrameListener(FrameListener listener) {
		frameListeners.add(listener);
	}

	/**
	 * Remove frame listener
	 * @param listener FrameListener
	 */
	public void removeFrameListener(FrameListener listener) {
		frameListeners.remove(listener);
	}
	
	/**
	 * Returns all Shapes in the HUD
	 */
	public List<Shape> getHUDs() {
		return huds;
	}
	
	/**
	 * Register update event listener.
	 * For performance reasons, listening interval accuracy is around intervalMsec / 100.
	 * If you want to update listeners more accurate or want to listen on every frame,
	 * implement Drawable class and add it to the layer,
	 * or implement FrameListener and add it to the scene.
	 * 
	 * @param intervalMsec update interval milliseconds
	 * @param listener SceneUpdateListener
	 */
	public void registerUpdateListener(int intervalMsec, SceneUpdateListener listener) {
		UpdateHandler handler = new UpdateHandler(intervalMsec);
		updateListeners.put(listener, handler);
		int interval = intervalMsec > 100 ? intervalMsec / 100 : 1;
		if (this.updateIntervalMsec == 0) {
			this.updateIntervalMsec = interval;
		} else {
			this.updateIntervalMsec = Math.min(this.updateIntervalMsec, interval);
		}
	}

	/**
	 * Unregister update event listener
	 * 
	 * @param listener SceneUpdateListener
	 */
	public void unregisterUpdateListener(SceneUpdateListener listener) {
		updateListeners.remove(listener);
	}
	
	class UpdateHandler {
		private int  intervalMsec = 0;
		private long lastTimeMillis = SystemClock.uptimeMillis();
		UpdateHandler(int interval) {
			intervalMsec = interval;
		}
		int interval() {
			return intervalMsec;
		}
		void update(long msec) {
			lastTimeMillis = msec;
		}
		long elapsed(long msec) {
			return msec - lastTimeMillis; 
		}
		void reset() {
			lastTimeMillis = 0;
		}
	}
}
