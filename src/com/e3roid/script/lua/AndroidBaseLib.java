/*******************************************************************************
* Copyright (c) 2009 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
******************************************************************************/
package com.e3roid.script.lua;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

/** 
 * Base library implementation, targeted for Android platforms.  
 * 
 * Implements the same library functions as org.luaj.lib.BaseLib, 
 * but looks in the current package for files loaded via 
 * loadfile(), dofile() and require(). 
 *  
 * @see org.luaj.lib.BaseLib
 */
public class AndroidBaseLib extends org.luaj.vm2.lib.BaseLib {

	private final Context context;
	private final int mode;
	
	/** Construct a Android base library instance */
	public AndroidBaseLib(Context context, int mode) {
		this.context = context;
		this.mode = mode;
	}

	/** 
	 * Try to open a file in the current package directory
	 * 
	 * @see org.luaj.vm2.lib.BaseLib
	 * @see org.luaj.vm2.lib.ResourceFinder
	 * 
	 * @param filename
	 * @return InputStream, or null if not found. 
	 */
	@Override
	public InputStream findResource(String filename) {
		try {
			if (mode == AndroidPlatform.MODE_ASSET) {
				return context.getAssets().open(filename);
			} else {
				return context.openFileInput(filename);
			}
		} catch (IOException e) {
			return null;
		}
	}
}
