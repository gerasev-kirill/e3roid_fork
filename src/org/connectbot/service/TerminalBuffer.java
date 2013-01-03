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
package org.connectbot.service;

import java.util.LinkedList;

/**
 * Implementation of a Video Display Unit (VDU) buffer. This class contains
 * all methods to manipulate the buffer that stores characters and their
 * attributes as well as the regions displayed.
 */
public abstract class TerminalBuffer {

	public final static int KEY_PAUSE = 1;
	public final static int KEY_F1 = 2;
	public final static int KEY_F2 = 3;
	public final static int KEY_F3 = 4;
	public final static int KEY_F4 = 5;
	public final static int KEY_F5 = 6;
	public final static int KEY_F6 = 7;
	public final static int KEY_F7 = 8;
	public final static int KEY_F8 = 9;
	public final static int KEY_F9 = 10;
	public final static int KEY_F10 = 11;
	public final static int KEY_F11 = 12;
	public final static int KEY_F12 = 13;
	public final static int KEY_UP = 14;
	public final static int KEY_DOWN =15 ;
	public final static int KEY_LEFT = 16;
	public final static int KEY_RIGHT = 17;
	public final static int KEY_PAGE_DOWN = 18;
	public final static int KEY_PAGE_UP = 19;
	public final static int KEY_INSERT = 20;
	public final static int KEY_DELETE = 21;
	public final static int KEY_BACK_SPACE = 22;
	public final static int KEY_HOME = 23;
	public final static int KEY_END = 24;
	public final static int KEY_NUM_LOCK = 25;
	public final static int KEY_CAPS_LOCK = 26;
	public final static int KEY_SHIFT = 27;
	public final static int KEY_CONTROL = 28;
	public final static int KEY_ALT = 29;
	public final static int KEY_ENTER = 30;
	public final static int KEY_NUMPAD0 = 31;
	public final static int KEY_NUMPAD1 = 32;
	public final static int KEY_NUMPAD2 = 33;
	public final static int KEY_NUMPAD3 = 34;
	public final static int KEY_NUMPAD4 = 35;
	public final static int KEY_NUMPAD5 = 36;
	public final static int KEY_NUMPAD6 = 37;
	public final static int KEY_NUMPAD7 = 38;
	public final static int KEY_NUMPAD8 = 39;
	public final static int KEY_NUMPAD9 = 40;
	public final static int KEY_DECIMAL = 41;
	public final static int KEY_ADD = 42;
	public final static int KEY_ESCAPE = 43;

	/*  Attributes bit-field usage:
	 *
	 *  8421 8421 8421 8421 8421 8421 8421 8421
	 *  |||| |||| |||| |||| |||| |||| |||| |||`- Bold
	 *  |||| |||| |||| |||| |||| |||| |||| ||`-- Underline
	 *  |||| |||| |||| |||| |||| |||| |||| |`--- Invert
	 *  |||| |||| |||| |||| |||| |||| |||| `---- Low
	 *  |||| |||| |||| |||| |||| |||| |||`------ Invisible
	 *  |||| |||| |||| |||| ||`+-++++-+++------- Foreground Color
	 *  |||| |||| |`++-++++-++------------------ Background Color
	 *  |||| |||| `----------------------------- Fullwidth character
	 *  `+++-++++------------------------------- Reserved for future use
	 */

	public final static int NORMAL = 0x00;
	public final static int BOLD = 0x01;
	public final static int UNDERLINE = 0x02;
	public final static int INVERT = 0x04;
	public final static int LOW = 0x08;
	public final static int INVISIBLE = 0x10;
	public final static int FULLWIDTH = 0x8000000;

	public final static int DELETE_IS_DEL = 0;
	public final static int DELETE_IS_BACKSPACE = 1;

	public final static int COLOR_FG_SHIFT = 5;
	public final static int COLOR_BG_SHIFT = 14;
	public final static int COLOR = 0x7fffe0;    /* 0000 0000 0111 1111 1111 1111 1110 0000 */
	public final static int COLOR_FG = 0x3fe0;   /* 0000 0000 0000 0000 0011 1111 1110 0000 */
	public final static int COLOR_BG = 0x7fc000; /* 0000 0000 0111 1111 1100 0000 0000 0000 */

	public final static int DEFAULT_TAB_STOP = 4;

	private int width;
	private int height;
	private int tabStop = DEFAULT_TAB_STOP;
	private boolean updated = false;

	private LinkedList<TerminalLine> lines = new LinkedList<TerminalLine>();

	public TerminalBuffer() {
		this(80, 24);
	}

	public TerminalBuffer(int width, int height) {
		setScreenSize(width, height);
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setScreenSize(int width, int height) {

		int h = lines.size() - height;
		if (h > 0) {
			for (int i = 0; i < h; i++) {
				lines.removeFirst();
			}
		}

		this.width  = width;
		this.height = height;
	}

	public void putString(String str) {
		putString(str.toCharArray());
	}

	public void putString(char[] chars) {
		putString(chars, null, 0, chars.length);
	}

	public void putString(char[] chars, boolean[] fullWidth, int start, int length) {

		int count = 0;

		if (lines.size() == 0) {
			lines.add(new TerminalLine(width));
		} else {
			count = lines.getLast().getColumn();
		}
		
		char lastChar = 0;
		for (int i = start; i < length; i++) {
			char c = chars[i];
			
			if (c == 0) break;
			
			if (count >= width) {
				lines.add(new TerminalLine(width));
				count = 0;
			}

			lastChar = c;
			
			boolean isWide = false;
			if (fullWidth != null && fullWidth[i] == true) {
				isWide = true;
				count++;
			} else {
				switch(c) {
				case '\r':
					continue;
				case '\n':
					count = width + 1; // reset to zero in next loop
					continue;
				case '\t':
					for (int j = 0; j < tabStop; j++) {
						putChar(' ', false, lines.getLast());
						count++;
					}
					continue;
				}
			}

			putChar((char) c, isWide, lines.getLast());
			count++;			
		}

		if (lastChar == '\n') {
			lines.add(new TerminalLine(width));
		}
		
		scrollLine();

		updated = true;
	}
	
	protected void addNewLine() {
		lines.add(new TerminalLine(width));
		scrollLine();
		updated = true;
	}
	
	protected void scrollLine() {
		if (lines.size() > height) {
			int rows = lines.size() - height;
			for (int i = 0; i < rows; i++) {
				lines.removeFirst();
			}
		}
	}

	public TerminalLine putChar(char c, boolean isWide, TerminalLine line) {
		if (isWide) {
			line.addChar(c, FULLWIDTH);
		} else {
			line.addChar(c);
		}
		return line;
	}
	
	public TerminalLine deleteChar() {
		TerminalLine line = lines.getLast();
		line.deleteChar();
		updated = true;
		return line;
	}

	public char[] getCharAt(int row) {
		if (row >= lines.size()) {
			return new char[0];
		}
		return lines.get(row).getChars();
	}

	public char getCharAt(int row, int column) {
		if (row >= lines.size()) {
			return ' ';
		}
		return lines.get(row).getCharAt(column);
	}

	public int getAttributeAt(int row, int column) {
		if (row >= lines.size()) {
			return 0;
		}
		return lines.get(row).getAttributeAt(column);
	}

	public String getString(int row) {
		if (row >= lines.size()) {
			return "";
		}
		return lines.get(row).getString();
	}

	public boolean isUpdated() {
		return updated;
	}

	public boolean isUpdated(int row) {
		return (lines.size() > row);
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}
	
	/**
	 * Handle key Typed events for the terminal TODO
	 */
	public void keyTyped(int keyCode, char keyChar, int modifiers) {
	    boolean control = (modifiers & TerminalBuffer.KEY_CONTROL) != 0;
	    boolean shift = (modifiers & TerminalBuffer.KEY_SHIFT) != 0;
	    boolean alt = (modifiers & TerminalBuffer.KEY_ALT) != 0;
	    
		switch (keyCode) {
		case KEY_ENTER:
			addNewLine();
			write('\n');
			break;
		case KEY_DELETE:
			deleteChar();
			write('\b');
			break;
		}
	}

	/**
	 * Handle special key typed events for the terminal
	 */
	public void keyPressed(int keyCode, char keyChar, int modifiers) {
		keyTyped(keyCode, keyChar, modifiers);
	}

	public abstract void debug(String notice);
	public abstract void write(byte[] b);
	public abstract void write(int b);

	public void reset() {
		lines.clear();
	}

	class TerminalLine {
		private int[]  charAttributes;
		private char[] charArray;

		private int col = 0;

		public TerminalLine(int width) {
			charArray = new char[width];
			charAttributes = new int[width];

			clear();
		}

		private void clear() {
			for (int i = 0; i < width; i++) {
				charArray[i] = ' ';
				charAttributes[i] = 0;
			}
		}

		public void addChar(char c, int attr) {
			if (col >= charArray.length) return;
			charArray[col] = c;
			charAttributes[col] = attr;
			col++;
		}

		public void addChar(char c) {
			if (col >= charArray.length) return;
			charArray[col] = c;
			col++;
		}
		
		public void deleteChar() {
			col--;
			charArray[col] = ' ';
		}

		public char getCharAt(int index) {
			return charArray[index];
		}

		public char[] getChars() {
			return charArray;
		}

		public int getAttributeAt(int index) {
			return charAttributes[index];
		}

		public void setChar(int index, char c) {
			charArray[index] = c;
		}

		public void setAttribute(int index, int attr) {
			charAttributes[index] = attr;
		}

		public String getString() {
			return new String(charArray);
		}
		
		public int getColumn() {
			return col;
		}
	}

}
