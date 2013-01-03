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

import com.e3roid.E3Engine;
import com.e3roid.event.TextInputListener;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

/**
 * An implementation of GLSurfaceView that uses the dedicated surface for displaying OpenGL rendering.
 */
public class RenderSurfaceView extends GLSurfaceView {

	protected E3Engine engine;
	protected boolean useSoftInput = false;
	protected boolean hideSoftInputWhenDone = true;
	protected TextInputListener textInputListener;
	
	/**
	 * Constructs the view with given context
	 * @param context Context of this view
	 */
	public RenderSurfaceView(Context context) {
		super(context);
	}

	/**
	 * Constructs the view with given parameters.
	 * 
	 * @param context Context of this view
	 * @param attrs AttributeSet of this view
	 */
	public RenderSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * Set text input listener for receiving soft input.
	 * @param textInputListener TextInputListener
	 */
	public void setTextInputListener(TextInputListener textInputListener) {
		this.textInputListener = textInputListener;
	}

	/**
	 * Set Renderer for this view.
	 * 
	 * @param engine E3Engine renderer
	 */
	public void setRenderer(E3Engine engine) {
		this.engine = engine;
		super.setRenderer(engine);
	}
	
	/**
	 * Measure the view and its content to determine the measured width and the measured height.
	 * @see android.view.SurfaceView#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (engine == null) throw new IllegalStateException("engine must be set by calling setRenderer()");
		engine.onMeasure(this, widthMeasureSpec, heightMeasureSpec);
	}
	
	/**
	 * Update measured dimension of the view.
	 * 
	 * @param widthMeasure  the measured width of the view
	 * @param heightMeasure the measured height of the view
	 */
	public void updateMeasuredDimension(int measuredWidth, int measuredHeight) {
		this.setMeasuredDimension(measuredWidth, measuredHeight);
	}
	
	/**
	 * Enable soft input area
	 * @param enable
	 */
	public void enableSoftInput(boolean enable) {
		this.useSoftInput = enable;
		
        setFocusable(enable);
        setFocusableInTouchMode(enable);
	}

	/**
	 * Hide soft input window when IME_ACTION_DONE is performed.
	 * @param hide
	 */
	public void hideSoftInputWhenDone(boolean hide) {
		this.hideSoftInputWhenDone = hide;
	}

	/**
	 * Show soft input to interact with the view.
	 */
	public void showSoftInput() {
		showSoftInput(InputMethodManager.SHOW_IMPLICIT);
	}
	
	/**
	 * Show soft input to interact with the view with given flags.
	 * @param flags Provides additional operating flags.
	 */
	public void showSoftInput(int flags) {
        InputMethodManager manager = (InputMethodManager)
        	getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(this, flags);
	}
	
	/**
	 * Hide soft input
	 */
	public void hideSoftInput() {
		hideSoftInput(0);
	}
	
	/**
	 * Hide soft input with given flags
	 * @param flags
	 */
	public void hideSoftInput(int flags) {
		InputMethodManager im = (InputMethodManager)
			getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		im.hideSoftInputFromWindow(getWindowToken(), flags);
	}
	
	/**
	 * Returns true if the view is currently active in the input method.
	 * @return true if the view is currently active in the input method
	 */
	public boolean isSoftInputActive() {
		InputMethodManager im = (InputMethodManager)
			getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		return im.isActive(this);
	}
	
	/**
	 * This method toggles the input method window display.
	 * @param showFlags Provides additional operating flags
	 * @param hideFlags Provides additional operating flags
	 */
	public void toggleSoftInput(int showFlags, int hideFlags) {
		InputMethodManager im = (InputMethodManager)
			getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		im.toggleSoftInput(showFlags, hideFlags);
	}

	/**
	 * Create a new InputConnection for an InputMethod to interact with the view.
	 */
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		if (!useSoftInput) return super.onCreateInputConnection(outAttrs);
		
		outAttrs.initialCapsMode = 0;
		outAttrs.initialSelEnd = outAttrs.initialSelStart = -1;
		outAttrs.inputType = (InputType.TYPE_CLASS_TEXT |
				InputType.TYPE_TEXT_VARIATION_NORMAL |
				InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
		outAttrs.imeOptions = (EditorInfo.IME_ACTION_DONE |
				EditorInfo.IME_FLAG_NO_EXTRACT_UI);

		return onLoadInputConnection();
	}
	
	/**
	 * Load InputConnection to interact with the view.
	 * @return InputConnection
	 */
	protected InputConnection onLoadInputConnection() {
		return new TextInputConnection(this);
	}
	
	/**
	 * TextInputConnection to activate soft input.
	 */
	private class TextInputConnection extends BaseInputConnection {
		public TextInputConnection(RenderSurfaceView view) {
			super(view, false);
		}

		@Override
		public boolean performEditorAction(int actionCode) {
			if (hideSoftInputWhenDone && actionCode == EditorInfo.IME_ACTION_DONE) {
				hideSoftInput();
			}
			return super.performEditorAction(actionCode);
		}
		
		@Override
		public boolean commitText(CharSequence text, int newCursorPosition) {
			if (textInputListener != null) {
				if (textInputListener.onCommitText(text, newCursorPosition)) {
					return true;
				}
			}
			return super.commitText(text, newCursorPosition);
		}
	}

}
