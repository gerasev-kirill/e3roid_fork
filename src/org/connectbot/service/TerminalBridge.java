/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.FontMetrics;
import android.text.ClipboardManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.connectbot.TerminalContainer;
import org.connectbot.transport.AbsTransport;
import org.connectbot.util.Colors;
import org.connectbot.util.PreferenceConstants;
import org.connectbot.util.SelectionArea;

import com.e3roid.util.Debug;

/**
 * Provides a bridge between a MUD terminal buffer and a possible TerminalView. This separation
 * allows us to keep the TerminalBridge running in a background service. A TerminalView shares down
 * a bitmap that we can use for rendering when available.
 * 
 * @author ConnectBot Dev Team
 * @author raaar
 * @author modified by e3roid
 * 
 */
public class TerminalBridge implements OnSharedPreferenceChangeListener {

	private final static int FONT_SIZE_STEP = 2;

	private final int[] color = new int[Colors.defaults.length];

	private final TerminalManager manager;

	private int mDefaultFgColor;
	private int mDefaultBgColor;

	private String encoding;

	private AbsTransport transport;

	private final Paint defaultPaint;

	private Relay relay;
	private Thread relayThread;

	private Bitmap bitmap = null;
	private final TerminalBuffer buffer;

	private TerminalContainer parent = null;
	private final Canvas canvas = new Canvas();

	private boolean forcedSize = false;
	private int columns;
	private int rows;

	private final TerminalKeyListener keyListener;

	private boolean selectingForCopy = false;
	private final SelectionArea selectionArea;
	private ClipboardManager clipboard;

	public int charWidth = -1;
	public int charHeight = -1;
	private int charTop = -1;

	private float fontSize = -1;

	private int id;

	private final List<FontSizeChangedListener> fontSizeChangedListeners;

	/**
	 * Flag indicating if we should perform a full-screen redraw during our next rendering pass.
	 */
	private boolean fullRedraw = false;

	private final PromptHelper promptHelper;

	/**
	 * Create a new terminal bridge suitable for unit testing.
	 */
	public TerminalBridge() {
		buffer = new TerminalBuffer() {
			@Override
			public void write(byte[] b) {
			}

			@Override
			public void write(int b) {
			}

			@Override
			public void debug(String s) {
			}
		};

		manager = null;

		defaultPaint = new Paint();

		selectionArea = new SelectionArea();

		fontSizeChangedListeners = new LinkedList<FontSizeChangedListener>();

		transport = null;

		keyListener = new TerminalKeyListener(manager, this, buffer, null);

		mDefaultFgColor = 0;
		mDefaultBgColor = 0;
		promptHelper = null;

		updateCharset();
	}

	/**
	 * Create new terminal bridge with following parameters.
	 */
	public TerminalBridge(final TerminalManager manager, AbsTransport t, int id)
	throws IOException {
		this.id = id;
		this.manager = manager;
		transport = t;

		String string = manager.getStringParameter(PreferenceConstants.FONTSIZE, null);
		if (string != null) {
			fontSize = Float.parseFloat(string);
		} else {
			fontSize = PreferenceConstants.DEFAULT_FONT_SIZE;
		}

		mDefaultFgColor =
			manager.getIntParameter(PreferenceConstants.COLOR_FG, PreferenceConstants.DEFAULT_FG_COLOR);
		mDefaultBgColor =
			manager.getIntParameter(PreferenceConstants.COLOR_BG, PreferenceConstants.DEFAULT_BG_COLOR);

		// create prompt helper to relay password and hostkey requests up to gui
		promptHelper = new PromptHelper(this);

		// create our default paint
		defaultPaint = new Paint();
		defaultPaint.setAntiAlias(true);
		defaultPaint.setTypeface(Typeface.MONOSPACE);
		defaultPaint.setFakeBoldText(true); // more readable?

		fontSizeChangedListeners = new LinkedList<FontSizeChangedListener>();

		setFontSize(fontSize);

		// create terminal buffer and handle outgoing data
		// this is probably status reply information
		buffer = new TerminalBuffer() {
			@Override
			public void debug(String s) {
				Debug.d(s, false);
			}

			@Override
			public void write(byte[] b) {
				try {
					if (b != null && transport != null) {
						transport.write(b);
					}
				} catch (IOException e) {
					Debug.e("Problem writing outgoing data in Terminal thread", e, false);
				}
			}

			@Override
			public void write(int b) {
				try {
					if (transport != null) {
						transport.write(b);
					}
				} catch (IOException e) {
					Debug.e("Problem writing outgoing data in Terminal thread", e, false);
				}
			}
		};

		resetColors();

		selectionArea = new SelectionArea();

		keyListener = new TerminalKeyListener(manager, this, buffer, encoding);

		updateCharset();

		manager.registerOnSharedPreferenceChangeListener(this);

	}

	/**
	 * Spawn thread to open connection and start login process.
	 */
	protected void connect() {
		transport.setBridge(this);
		transport.setManager(manager);
		transport.connect();

		buffer.reset();

		// create thread to relay incoming connection data to buffer
		relay = new Relay(this, transport, buffer, encoding);
		relayThread = new Thread(relay);
		relayThread.setDaemon(true);
		relayThread.setName("Relay");
		relayThread.start();

		// force font-size to make sure we resizePTY as needed
		setFontSize(fontSize);

	}

	private void updateCharset() {
		encoding =
			manager.getStringParameter(PreferenceConstants.ENCODING, Charset.defaultCharset().name());
		if (relay != null) {
			relay.setCharset(encoding);
		}
		keyListener.setCharset(encoding);
	}

	/**
	 * Inject a specific string into this terminal. Used for post-login strings and pasting clipboard.
	 */
	public void injectString(final String string) {
		if (string == null || string.length() == 0) {
			return;
		}

		Thread injectStringThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					transport.write(string.getBytes(encoding));
				} catch (Exception e) {
					Debug.e("Couldn't inject string to remote host: ", e, false);
				}
			}
		});
		injectStringThread.setName("InjectString");
		injectStringThread.start();
	}

	/**
	 * @return whether a session is open or not
	 */
	public boolean isSessionOpen() {
		if (transport != null) {
			return transport.isSessionOpen();
		}
		return false;
	}

	/**
	 * Force disconnection of this terminal bridge.
	 */
	public void dispatchDisconnect(boolean immediate) {

		relay.stop();
		
		if (relayThread != null && relayThread.isAlive()) {
			relayThread.interrupt();
		}
		
		if (immediate) {
			manager.closeConnection(TerminalBridge.this, true);
		} else {
			if (transport != null && transport.isConnected()) {
				manager.closeConnection(this, true);
			} else {
				manager.closeConnection(this, false);
			}
		}
	}

	public void setSelectingForCopy(boolean selectingForCopy) {
		this.selectingForCopy = selectingForCopy;
	}

	public boolean isSelectingForCopy() {
		return selectingForCopy;
	}

	public SelectionArea getSelectionArea() {
		return selectionArea;
	}

	/**
	 * Request a different font size. Will make call to parentChanged() to make sure we resize PTY if
	 * needed.
	 */
	/* package */final void setFontSize(float size) {
		if (size <= 0.0) {
			return;
		}

		defaultPaint.setTextSize(size);
		fontSize = size;

		// read new metrics to get exact pixel dimensions
		FontMetrics fm = defaultPaint.getFontMetrics();
		charTop = (int) Math.ceil(fm.top);

		float[] widths = new float[1];
		defaultPaint.getTextWidths("X", widths);
		charWidth = (int) Math.ceil(widths[0]);
		charHeight = (int) Math.ceil(fm.descent - fm.top);

		// refresh any bitmap with new font size
		if (parent != null) {
			parentChanged(parent);
		}

		for (FontSizeChangedListener ofscl : fontSizeChangedListeners) {
			ofscl.onFontSizeChanged(size);
		}
		forcedSize = false;
	}

	/**
	 * Add an {@link FontSizeChangedListener} to the list of listeners for this bridge.
	 * 
	 * @param listener
	 *          listener to add
	 */
	public void addFontSizeChangedListener(FontSizeChangedListener listener) {
		fontSizeChangedListeners.add(listener);
	}

	/**
	 * Remove an {@link FontSizeChangedListener} from the list of listeners for this bridge.
	 * 
	 * @param listener
	 */
	public void removeFontSizeChangedListener(FontSizeChangedListener listener) {
		fontSizeChangedListeners.remove(listener);
	}

	/**
	 * Something changed in our parent {@link TerminalView}, maybe it's a new parent, or maybe it's an
	 * updated font size. We should recalculate terminal size information and request a PTY resize.
	 */
	public final synchronized void parentChanged(TerminalContainer parent) {
		if (manager != null && !manager.isResizeAllowed()) {
			Debug.d("Resize is not allowed now", false);
			return;
		}

		this.parent = parent;
		final int width = parent.getWidth();
		final int height = parent.getHeight();

		// Something has gone wrong with our layout; we're 0 width or height!
		if (width <= 0 || height <= 0) {
			return;
		}

		clipboard = (ClipboardManager) parent.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
		keyListener.setClipboardManager(clipboard);

		if (!forcedSize) {
			// recalculate buffer size
			int newColumns, newRows;

			newColumns = width / charWidth;
			newRows = height / charHeight;

			// If nothing has changed in the terminal dimensions and not an intial
			// draw then don't blow away scroll regions and such.
			if (newColumns == columns && newRows == rows) {
				return;
			}

			columns = newColumns;
			rows = newRows;
		}

		// reallocate new bitmap if needed
		boolean newBitmap = (bitmap == null);
		if (bitmap != null) {
			newBitmap = (bitmap.getWidth() != width || bitmap.getHeight() != height);
		}

		if (newBitmap) {
			discardBitmap();
			bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			canvas.setBitmap(bitmap);
		}

		// clear out any old buffer information
		defaultPaint.setColor(Color.BLACK);
		canvas.drawPaint(defaultPaint);

		// Stroke the border of the terminal if the size is being forced;
		if (forcedSize) {
			int borderX = (columns * charWidth) + 1;
			int borderY = (rows * charHeight) + 1;

			defaultPaint.setColor(Color.GRAY);
			defaultPaint.setStrokeWidth(0.0f);
			if (width >= borderX) {
				canvas.drawLine(borderX, 0, borderX, borderY + 1, defaultPaint);
			}
			if (height >= borderY) {
				canvas.drawLine(0, borderY, borderX + 1, borderY, defaultPaint);
			}
		}

		try {
			// request a terminal pty resize
			synchronized (buffer) {
				buffer.setScreenSize(columns, rows);
			}

			if (transport != null) {
				transport.setDimensions(columns, rows, width, height);
			}
		} catch (Exception e) {
			Debug.e("Problem while trying to resize screen or PTY", e, false);
		}

		// force full redraw with new buffer size
		fullRedraw = true;
		redraw();

		Debug.i(String.format("parentChanged() now width=%d, height=%d", columns, rows), false);
	}

	/**
	 * Somehow our parent {@link TerminalView} was destroyed. Now we don't need to redraw anywhere,
	 * and we can recycle our internal bitmap.
	 */
	public synchronized void parentDestroyed() {
		parent = null;
		discardBitmap();
	}

	private void discardBitmap() {
		if (bitmap != null) {
			bitmap.recycle();
		}
		bitmap = null;
	}

	/**
	 * Draw the terminal.
	 * @return updated or not
	 */
	public boolean onDraw() {
		synchronized (buffer) {
			if (!buffer.isUpdated() && !fullRedraw) return false;

			defaultPaint.setColor(mDefaultBgColor);
			canvas.drawPaint(defaultPaint);
			
			// walk through all lines in the buffer
			for (int l = 0; l < buffer.getHeight(); l++) {

				// check if this line is dirty and needs to be repainted
				// also check for entire-buffer dirty flags
				if (!buffer.isUpdated(l)) {
					continue;
				}

				// write the text string starting at 'c' for 'addr' number of characters
				defaultPaint.setColor(mDefaultFgColor);
				char[] charToDraw = buffer.getCharAt(l);
				canvas.drawText(charToDraw, 0, charToDraw.length, 
						0, (l * charHeight) - charTop, defaultPaint);
			}

			// reset entire-buffer flags
			buffer.setUpdated(false);
		}
		fullRedraw = false;
		
		return true;
	}

	public void redraw() {
		if (parent != null) {
			parent.postInvalidate();
		}
	}

	/**
	 * Resize terminal to fit [rows]x[cols] in screen of size [width]x[height]
	 * 
	 * @param rows
	 * @param cols
	 * @param width
	 * @param height
	 */
	public synchronized void resizeComputed(int cols, int rows, int width, int height) {
		float size = 8.0f;
		float step = 8.0f;
		float limit = 0.125f;

		int direction;

		while ((direction = fontSizeCompare(size, cols, rows, width, height)) < 0) {
			size += step;
		}

		if (direction == 0) {
			Debug.d(String.format("Fontsize: found match at %f", size), false);
			return;
		}

		step /= 2.0f;
		size -= step;

		while ((direction = fontSizeCompare(size, cols, rows, width, height)) != 0 && step >= limit) {
			step /= 2.0f;
			if (direction > 0) {
				size -= step;
			} else {
				size += step;
			}
		}

		if (direction > 0) {
			size -= step;
		}

		columns = cols;
		this.rows = rows;
		setFontSize(size);
		forcedSize = true;
	}

	private int fontSizeCompare(float size, int cols, int rows, int width, int height) {
		// read new metrics to get exact pixel dimensions
		defaultPaint.setTextSize(size);
		FontMetrics fm = defaultPaint.getFontMetrics();

		float[] widths = new float[1];
		defaultPaint.getTextWidths("X", widths);
		int termWidth = (int) widths[0] * cols;
		int termHeight = (int) Math.ceil(fm.descent - fm.top) * rows;

		Debug.d(String.format("Fontsize: font size %f resulted in %d x %d", size, termWidth, termHeight), false);

		// Check to see if it fits in resolution specified.
		if (termWidth > width || termHeight > height) {
			return 1;
		}

		if (termWidth == width || termHeight == height) {
			return 0;
		}

		return -1;
	}
	public void setColor(int index, int red, int green, int blue) {
		// Don't allow the system colors to be overwritten for now. May violate specs.
		if (index < color.length && index >= 16) {
			color[index] = 0xff000000 | red << 16 | green << 8 | blue;
		}
	}

	public final void resetColors() {
		System.arraycopy(Colors.defaults, 0, color, 0, Colors.defaults.length);
	}

	public TerminalKeyListener getKeyHandler() {
		return keyListener;
	}

	public void increaseFontSize() {
		setFontSize(fontSize + FONT_SIZE_STEP);
	}

	public void decreaseFontSize() {
		setFontSize(fontSize - FONT_SIZE_STEP);
	}

	public int getForegroundColor() {
		return mDefaultFgColor;
	}

	public int getBackgroundColor() {
		return mDefaultBgColor;
	}

	public TerminalBuffer getVDUBuffer() {
		return buffer;
	}

	public PromptHelper getPromptHelper() {
		return promptHelper;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public AbsTransport getTransport() {
		return transport;
	}

	public Paint getPaint() {
		return defaultPaint;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (PreferenceConstants.ENCODING.equals(key)) {
			updateCharset();
		} else if (PreferenceConstants.FONTSIZE.equals(key)) {
			String string = manager.getStringParameter(PreferenceConstants.FONTSIZE, null);
			if (string != null) {
				fontSize = Float.parseFloat(string);
			} else {
				fontSize = PreferenceConstants.DEFAULT_FONT_SIZE;
			}
			setFontSize(fontSize);
			fullRedraw = true;
		} else if (PreferenceConstants.COLOR_FG.equals(key)) {
			mDefaultFgColor =
				manager.getIntParameter(PreferenceConstants.COLOR_FG,
						PreferenceConstants.DEFAULT_FG_COLOR);
			fullRedraw = true;
		} else if (PreferenceConstants.COLOR_BG.equals(key)) {
			mDefaultBgColor =
				manager.getIntParameter(PreferenceConstants.COLOR_BG,
						PreferenceConstants.DEFAULT_BG_COLOR);
			fullRedraw = true;
		}
	}
	
	public void setCharset(String encoding) {
		if (relay != null) {
			relay.setCharset(encoding);
		}
		keyListener.setCharset(encoding);
	}
	
	public void setFontSize(int size) {
		this.fontSize = size;
	}
	
	public void setBgColor(int color) {
		this.mDefaultBgColor = color;
	}
	
	public void setFgColor(int color) {
		this.mDefaultFgColor = color;
	}

	public int getId() {
		return this.id;
	}
}
