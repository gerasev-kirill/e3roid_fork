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
package com.e3roid.drawable.sprite;

import javax.microedition.khronos.opengles.GL10;

import org.connectbot.TerminalContainer;
import org.connectbot.service.TerminalBridge;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.Toast;

import com.e3roid.E3Activity;
import com.e3roid.E3Scene;
import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.texture.BitmapTexture;
import com.e3roid.event.SceneOnKeyListener;
import com.e3roid.event.TextInputListener;

/**
 * A Sprite for displaying a terminal console used with TerminalBridge.
 */
public class TerminalSprite extends Sprite implements TerminalContainer, SceneOnKeyListener, TextInputListener {

	private BitmapTexture texture;
	private TerminalBridge bridge;
	private E3Activity context;

	private Toast notification = null;
	private String lastNotification = null;
	private volatile boolean notifications = true;
	  
	/**
	 * Constructs full-screen terminal sprite that connects with given TerminalBridge
	 * @param bridge TerminalBridge
	 * @param context E3Activity
	 */
	public TerminalSprite(TerminalBridge bridge, E3Activity context) {
		this(0, 0, context.getWidth(), context.getHeight(), bridge, context);
	}

	/**
	 * Constructs terminal sprite that connects with given TerminalBridge
	 * @param x x position
	 * @param y y position
	 * @param width width of the terminal
	 * @param height height of the terminal
	 * @param bridge TerminalBridge
	 * @param context E3Activity
	 */
	public TerminalSprite(int x, int y, int width, int height, TerminalBridge bridge, E3Activity context) {
		this.bridge = bridge;
		this.context = context;
		
		setPosition(x, y);
		setSize(width, height);
		
		bridge.parentChanged(this);
		
        texture = new BitmapTexture(bridge.getBitmap(), getWidth(), getHeight(), context);
        texture.recycleBitmap(false);
        
		createTexture();
		createBuffers();
	}
	
	/**
	 * Set foreground color of the terminal
	 * @param color foreground color
	 */
	public void setFgColor(int color) {
		bridge.setFgColor(color);
	}
	
	/**
	 * Sets background color of the terminal
	 * @param color background color
	 */
	public void setBgColor(int color) {
		bridge.setBgColor(color);
	}

	/**
	 * Sets font size of the terminal
	 * @param size font size
	 */
	public void setFontSize(int size) {
		bridge.setFontSize(size);
	}
	
	/**
	 * Sets encoding of the terminal
	 * @param encoding encoding
	 */
	public void setEncoding(String encoding) {
		bridge.setCharset(encoding);
	}

	
	/**
	 * Called to draw the sprite.
	 * This method is responsible for drawing the sprite. 
	 */
	@Override
	public void onDraw(GL10 gl) {
		
		if (bridge.getBitmap() != null) {
			if (bridge.onDraw()) {
				createTexture();
				texture.loadTexture(gl, true);
			}
		}
		
		super.onDraw(gl);
	}

	protected void createTexture() {
		texture.setBitmap(bridge.getBitmap());
		updateTexture(texture);
	}

	/**
	 * Called when this shape is disposed.
	 */
	@Override
	public void onDispose() {
		bridge.dispatchDisconnect(true);
		bridge.parentDestroyed();
	}

	/**
	 * Returns context that associated with the sprite.
	 */
	@Override
	public Context getContext() {
		return context;
	}

	/**
	 * Notify messages using a Toast.
	 */
	@Override
	public void notifyUser(String message) {
	    if (!notifications) {
	        return;
	      }

	      if (notification != null) {
	        // Don't keep telling the user the same thing.
	        if (lastNotification != null && lastNotification.equals(message)) {
	          return;
	        }

	        notification.setText(message);
	        notification.show();
	      } else {
	        notification = Toast.makeText(context, message, Toast.LENGTH_SHORT);
	        notification.show();
	      }

	      lastNotification = message;
	}

	/**
	 * Requests redraw for this sprite.
	 */
	@Override
	public void postInvalidate() {
		// nothing to do
	}
	
	/**
	 * Called when a key was pressed down and not handled by any of the views inside of the activity.
	 * 
	 * @param scene e3roid scene on which the event occurs.
	 * @param keyCode The value in event.getKeyCode()
	 * @param event Description of the key event
	 * @return Return true to prevent this event from being propagated further, or false to indicate that you have not handled this event and it should continue to be propagated.
	 */
	@Override
	public boolean onKeyDown(E3Scene scene, int keyCode, KeyEvent event) {
		return bridge.getKeyHandler().onKey(context.getView(), keyCode, event);
	}
	
	/**
	 * Called when a key was released and not handled by any of the views inside of the activity.
	 * 
	 * @param scene e3roid scene on which the event occurs.
	 * @param keyCode The value in event.getKeyCode()
	 * @param event Description of the key event
	 * @return Return true to prevent this event from being propagated further, or false to indicate that you have not handled this event and it should continue to be propagated.
	 */
	@Override
	public boolean onKeyUp(E3Scene scene, int keyCode, KeyEvent event) {
		return bridge.getKeyHandler().onKey(context.getView(), keyCode, event);
	}

	/**
	 * Called from InputConnection to be committed with given text.
	 * @param text The committed text.
	 * @param newCursorPosition the new cursor position around the text.
	 * @return Returns true on success, false if the input connection is no longer valid. 
	 */
	@Override
	public boolean onCommitText(CharSequence text, int newCursorPosition) {
		return bridge.getKeyHandler().onCommitText(context.getView(), text, newCursorPosition);
	}
}
