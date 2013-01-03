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

import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.connectbot.service.TerminalManager;

import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.content.Context;
import android.util.DisplayMetrics;

import com.e3roid.lifecycle.E3LifeCycle;
import com.e3roid.lifecycle.E3Service;
import com.e3roid.opengl.Camera;
import com.e3roid.opengl.GLHelper;
import com.e3roid.opengl.GLSurfaceView.Renderer;
import com.e3roid.opengl.RenderSurfaceView;
import com.e3roid.util.Debug;
import com.e3roid.util.FPSCounter;
import com.e3roid.util.IntPair;

/**
 * A base engine for e3roid framework that responsible for rendering.
 */
public class E3Engine implements E3LifeCycle, Renderer {

	/**
	 * Constant for indicating unlimited FPS
	 */
	public static final int REFRESH_DEFAULT = 0;
	/**
	 * Constant for indicating limited FPS
	 */
	public static final int REFRESH_LIMITED = 1;
	/**
	 * Constant for stretching scene resolution
	 */
	public static final int RESOLUTION_STRETCH_SCENE = 2;
	/**
	 * Constant for expanding scene resolution
	 */
	public static final int RESOLUTION_EXPAND_SCENE  = 3;
	/**
	 * Constant for keeping fixed ratio scene resolution
	 */
	public static final int RESOLUTION_FIXED_RATIO = 4;
	/**
	 * Constant for keeping exact scene resolution
	 */
	public static final int RESOLUTION_EXACT = 5;
	/**
	 * Constant for keeping fixed ratio with auto rotation scene resolution
	 */
	public static final int RESOLUTION_FIXED_RATIO_WITH_ROTATION = 6;
	
	private final E3Activity context;
	private final DisplayMetrics displayMetrics;
	private E3Scene scene = null;	
	private boolean fullScreen = false;
	private boolean screenOrientationLandscape = false;
	private boolean screenOrientationPortrait  = false;
	private FPSCounter fpsCounter;
	private FPSCounter refreshFPSCounter;
	private Camera camera = new Camera();
	private boolean matrixChanged = false;
	private boolean useVBO = true;
	private boolean stopped = false;
	
	private TerminalManager terminalManager = null;
	
	private ArrayList<E3LifeCycle> lifeCycles = new ArrayList<E3LifeCycle>();
	protected ArrayList<Runnable> postedEvents = new ArrayList<Runnable>();
	protected HashMap<Long, Thread> services = new HashMap<Long, Thread>();
	
	private int  refreshMode  = REFRESH_DEFAULT;
	private int  preferredFPS = 0;
	private int  resolutionPolicy = RESOLUTION_EXPAND_SCENE;
	
	private int width;
	private int height;
	
	/**
	 * Construct e3roid engine. 
	 * The actual scene width and height may be changed depending on the screen resolution.
	 * 
	 * @param context The Context the view is running in
	 * @param width width of the scene. 
	 * @param height height of the scene.
	 */
	public E3Engine(E3Activity context, int width, int height) {
		this(context, width, height, RESOLUTION_EXPAND_SCENE);
	}
	/**
	 * Construct e3roid engine. 
	 * The actual scene width and height may be changed depending 
	 * on the screen resolution and given resolution policy.
	 * 
	 * @param context The Context the view is running in
	 * @param width width of the scene
	 * @param height height of the scene
	 * @param resolutionPolicy the screen resolution policy
	 */
	public E3Engine(E3Activity context, int width, int height, int resolutionPolicy) {
		this.context = context;
		this.displayMetrics = context.getDisplayMetrics();
		this.resolutionPolicy = resolutionPolicy;
		
		updateResolution(width, height);		
		initializeFPSCounter();
	}

	/**
	 * Called when the surface is created or recreated.
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLHelper.reset(gl);
		GLHelper.hintPerspectiveCorrectionAndNicest(gl);
		GLHelper.enableLighting(gl, false);
		GLHelper.enableDither(gl, false);
		GLHelper.enableMultiSample(gl, false);
		GLHelper.enableColorArray(gl, false);
		GLHelper.enableDepthTest(gl, false);

		GLHelper.enableTextures(gl, true);
		GLHelper.enableTexCoordArray(gl, true);
		GLHelper.enableBlend(gl, true);
		GLHelper.blendMode(gl, GL10.GL_SRC_ALPHA,GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		GLHelper.enableVertexArray(gl, true);
		GLHelper.enableCulling(gl, true);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glCullFace(GL10.GL_BACK);
	}
	
	/**
	 * Called after the surface is created and whenever the OpenGL ES surface size changes.
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		gl.glViewport(0, 0, w, h);
		updateResolution(width, height);
	}
	
	/**
	 * Called when the surface is lost.
	 */
	@Override
	public void onSurfaceLost() {
		fpsCounter.resetCount();
		refreshFPSCounter.resetCount();
	}
	
	/**
	 * Called to draw the current frame.
	 * This method is responsible for drawing the current frame. 
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		if (scene == null) return;
		if (stopped) return;
		if (this.matrixChanged) {
			camera.reloadMatrix(gl);
			matrixChanged = false;
		}
		refreshFPSCounter.onFrame();
		if (refreshMode == REFRESH_LIMITED) {
			if (preferredFPS <= 0) {
				throw new IllegalArgumentException("preferredFPS must be set while refreshMode equals REFRESH_LIMITED.");
			}
			waitForFPS();
		}
		
		camera.look(gl);
		
		synchronized(postedEvents) {
			if (!postedEvents.isEmpty()) {
				for (Runnable runnable : postedEvents) {
					runnable.run();
				}
				postedEvents.clear();
			}
		}
		scene.onDraw(gl);
		fpsCounter.onFrame();
	}
	
	/**
	 * Update the scene size
	 */
	public void setSize(int width, int height) {
		setSize(width, height, this.resolutionPolicy);
	}
	
	/**
	 * Update the scene size with given resolution policy
	 */
	public void setSize(int width, int height, int resolutionPolicy) {
		this.resolutionPolicy = resolutionPolicy;
		updateResolution(width, height);
	}

	/**
	 * Indicates whether current scene is using perspective look.
	 * @return
	 */
	public boolean isPerspective() {
		return camera.isPerspective();
	}
	/**
	 * Enables perspective look (experimental)
	 * @param enable
	 */
	public void enablePerspective(boolean enable) {
		enablePerspective(enable, true);
	}
	/**
	 * Enables perspective look with default camera (experimental)
	 * @param enable
	 * @param defaultLook
	 */
	public void enablePerspective(boolean enable, boolean defaultLook) {
		if (isPerspective() == enable) return;
		
		camera.enablePerspective(enable);
		
		if (defaultLook) {
			camera.defaultLook();
		}
	}
	
	/**
	 * Post the specified action to event queue to run on the scene update thread.
	 * 
	 * @param runnable the action to run on the scene update thread.
	 */
	public void postUpdate(Runnable runnable) {
		synchronized(postedEvents) {
			postedEvents.add(runnable);
		}
	}
	
	/**
	 * Determine resolution of the screen. 
	 * If resolution policy equals RESOLUTION_KEEP_RATIO,
	 * the width might be changed depending on the real screen aspect ratio.
	 */
	protected void updateResolution(int w, int h) {
		
		IntPair dim = determineResolution(w, h);
		
		this.width  = dim.getInt1();
		this.height = dim.getInt2();
		
		camera.setView(this.width, this.height);

		this.matrixChanged = true;
	}
	
	protected IntPair determineResolution(int w, int h) {
		float userAspect = (float)w / (float)h;
		float flipAspect = (float)h / (float)w;
		float realAspect = (float)displayMetrics.widthPixels / (float)displayMetrics.heightPixels;
		if (resolutionPolicy == RESOLUTION_EXPAND_SCENE) {
			if (realAspect != userAspect) {
				if (realAspect == flipAspect) {
					return new IntPair(h, w);
				} else if (realAspect < userAspect) {
					h = (int)(w / realAspect);
				} else {
					w = (int)(h * realAspect);
				}
			}
		} else if (resolutionPolicy == RESOLUTION_FIXED_RATIO_WITH_ROTATION) {
			// reverse the aspect if screen rotation is detected
			if ((realAspect < 1 && userAspect > 1) || (realAspect > 1 && userAspect < 1)) {
				int tmpHeight = h;
				h = w;
				w = tmpHeight;
			}
		}
		return new IntPair(w, h);
	}
	
	/**
	 * Measure the view and its content to determine the measured width and the measured height.
	 * 
	 * @param view the view that measures to.
	 * @param widthMeasureSpec  horizontal space requirements as imposed by the parent. 
	 * @param heightMeasureSpec vertical space requirements as imposed by the parent.
	 */
	public void onMeasure(RenderSurfaceView view, int widthMeasureSpec, int heightMeasureSpec) {
		if (resolutionPolicy == RESOLUTION_EXACT) {
			view.updateMeasuredDimension(width, height);
			return;
		}
		int specWidth = MeasureSpec.getSize(widthMeasureSpec);
		int specHeight = MeasureSpec.getSize(heightMeasureSpec);
		if (resolutionPolicy == RESOLUTION_FIXED_RATIO) {
			float userAspect = (float)width / (float)height;
			float realAspect = (float)specWidth / (float)specHeight; 
			
			if(realAspect < userAspect) {
				specHeight = Math.round(specWidth / userAspect);
			} else {
				specWidth = Math.round(specHeight * userAspect);
			}
		} else if (resolutionPolicy == RESOLUTION_FIXED_RATIO_WITH_ROTATION) {
			float userAspect = (float)width / (float)height;
			float realAspect = (float)specWidth / (float)specHeight; 
			
			// reverse the aspect if screen rotation is detected
			if ((realAspect < 1 && userAspect > 1) || (realAspect > 1 && userAspect < 1)) {
				userAspect = (float)height / (float)width;
			}
			
			if(realAspect < userAspect) {
				specHeight = Math.round(specWidth / userAspect);
			} else {
				specWidth = Math.round(specHeight * userAspect);
			}
		}
		IntPair spec = determineResolution(specWidth, specHeight);
		view.updateMeasuredDimension(spec.getInt1(), spec.getInt2());
	}
	
	private void waitForFPS() {
		try {
			if (refreshFPSCounter.getFPS() == 0) return;
			if (refreshFPSCounter.getFrameCount() == 0) return;
			if (refreshFPSCounter.getFPS() > this.preferredFPS) {
				float currentMSec   = (float)1000.0 / refreshFPSCounter.getFPS();
				float preferredMSec = (float)1000.0 / (float)this.preferredFPS;
				float waitMsec = (float)preferredMSec - currentMSec;
				Thread.sleep((long)waitMsec * refreshFPSCounter.getFrameCount());
			} else {
				refreshFPSCounter.resetCount();
			}
		} catch (InterruptedException e) {
			// do nothing
		}
	}
	
	private void initializeFPSCounter() {
		fpsCounter = new FPSCounter();
		refreshFPSCounter = new FPSCounter();
		lifeCycles.add(fpsCounter);
		lifeCycles.add(refreshFPSCounter);		
	}
	
	/**
	 * Called after onRestart(), or onPause(), 
	 * for your activity to start interacting with the user. 
	 */
	@Override
	public void onResume() {
		for (E3LifeCycle lifeCycle : lifeCycles) {
			lifeCycle.onResume();
		}
	}
	
	/**
	 * Called as part of the activity lifecycle when an activity is going into the background,
	 * but has not (yet) been killed. 
	 */
	@Override
	public void onPause() {
		for (E3LifeCycle lifeCycle : lifeCycles) {
			lifeCycle.onPause();
		}
	}
	
	/**
	 * Perform any final cleanup before an activity is destroyed.
	 */
	@Override
	public void onDispose() {
		for (E3LifeCycle lifeCycle : lifeCycles) {
			lifeCycle.onDispose();
		}
	}
    
	/**
	 * Called after e3roid scene has been loaded.
	 * 
	 * @param scene scene of this engine
	 */
	public void onLoadScene(E3Scene scene) {
		this.scene = scene;
		lifeCycles.add(scene);
	}

	/**
	 * Called when screen touch event occurs.
	 * 
	 * @param event the motion event
	 * @return true if the scene has handled this event, false otherwise.
	 */
	public boolean onTouchEvent(MotionEvent event) {
		if (this.scene != null) {
			return scene.onTouchEvent(event);
		}
		return false;
	}

	/**
	 * Request full screen mode.
	 */
	public void requestFullScreen() {
		this.fullScreen = true;
	}
	
	/**
	 * Request landscape orientation
	 */
	public void requestLandscape() {
		this.screenOrientationLandscape = true;
		this.screenOrientationPortrait  = false;
	}
	
	/**
	 * Request portrait orientation
	 */
	public void requestPortrait() {
		this.screenOrientationLandscape = false;
		this.screenOrientationPortrait  = true;
	}

	/**
	 * Set FPS refresh mode
	 * @param mode One of REFRESH_DEFAULT or REFRESH_LIMITED
	 */
	public void setRefreshMode(int mode) {
		this.refreshMode = mode;
	}
	
	/**
	 * Set preferred FPS on REFRESH_LIMITED mode
	 * @param fps
	 */
	public void setPreferredFPS(int fps) {
		this.preferredFPS = fps;
	}

	/**
	 * Returns FPS counter
	 * @return FPS counter
	 */
	public FPSCounter getFPSCounter() {
		return this.fpsCounter;
	}

	/**
	 * Returns context of the engine
	 * @return context of the engine
	 */
	public E3Activity getContext() {
		return this.context;
	}
	
	/**
	 * Returns scene of the engine
	 * @return scene of the engine
	 */
	public E3Scene getScene() {
		return this.scene;
	}

	/**
	 * Returns width of the engine
	 * @return width of the engine
	 */
	public int getWidth() {
		return this.width;
	}
	
	/**
	 * Returns height of the engine
	 * @return height of the engine
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Returns display metrics data
	 * @return display metrics data
	 */
	public DisplayMetrics getDisplayMetrics() {
		return this.displayMetrics;
	}

	/**
	 * Returns camera of the engine
	 * @return camera of the engine
	 */
	public Camera getCamera() {
		return this.camera;
	}
		
	/**
	 * Indicates whether the screen is full screen or not.
	 * @return
	 */
	public boolean isFullScreen() {
		return this.fullScreen;
	}
	
	/**
	 * Indicates whether the orientation is landscape or not.
	 * @return
	 */
	public boolean isScreenOrientationLandscape() {
		return screenOrientationLandscape;
	}
	
	/**
	 * Indicates whether the orientation is portrait or not.
	 * @return
	 */
	public boolean isScreenOrientationPortrait() {
		return screenOrientationPortrait;
	}
	
	/**
	 * Set enabled state of Vertex Buffer Object.
	 * @param enable true if VBO is enabled, false otherwise 
	 */
	public void enableVBO(boolean enable) {
		this.useVBO = enable;
	}

	/**
	 * Indicates whether VBO is enabled not not.
	 * @return
	 */
	public boolean useVBO() {
		return this.useVBO;
	}

	/**
	 * Stop the engine. The scene rendering will be stopped.
	 */
	public void stop() {
		this.stopped = true;
	}
	
	/**
	 * Start the engine. The scene rendering will be started.
	 */
	public void start() {
		this.stopped = false;
	}
	
	/**
	 * Starts new thread as service.
	 * @param service E3Service
	 * @return service id
	 */
	public long registerService(E3Service service) {
		long serviceId = service.getId();
		if (services.containsKey(serviceId)) {
			Thread thc = services.get(serviceId);
			if (thc != null && thc.isAlive()) {
				Debug.d(String.format("Requested service is already started: id=%d", service.getId()));
				return service.getId();
			}
		}
		
		Thread th = new Thread(service);
		th.start();
		
		long id = th.getId();
		
		service.setId(id);
		services.put(id, th);
		lifeCycles.add(service);
		
		return id;
	}
	
	/**
	 * Stop service thread and remove from life cycle.
	 * @param service E3Service
	 * @return service has been removed or not
	 */
	public boolean unregisterService(E3Service service) {
		long serviceId = service.getId();
		if (services.containsKey(serviceId)) {
						
			Thread th = services.get(serviceId);
			if (th.isAlive()) {
				th.interrupt();
			}
			services.remove(serviceId);
			
			service.onDispose();
			
			if (lifeCycles.contains(service)) {
				lifeCycles.remove(service);
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns terminal manager for terminal console
	 * @param context Context
	 * @return terminal manager
	 */
	public TerminalManager getTerminalManager(Context context) {
		if (terminalManager == null) {
			terminalManager = new TerminalManager(context);
			addLifeCycle(terminalManager);
		}
		return terminalManager;
	}
	
	/**
	 * Add object to e3roid life cycle
	 * @param lifeCycle E3LifeCycle
	 * @return added or not
	 */
	public boolean addLifeCycle(E3LifeCycle lifeCycle) {
		if (lifeCycles.contains(lifeCycle)) return false;
		return lifeCycles.add(lifeCycle);
	}
	
	/**
	 * Remove object from e3roid life cycle
	 * @param lifeCycle E3LifeCycle
	 * @return removed or not
	 */
	public boolean removeLifeCycle(E3LifeCycle lifeCycle) {
		return lifeCycles.remove(lifeCycle);
	}
}
