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

import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;

import android.content.Context;

/**
 * AndroidPlatform class represents Lue scripting environment for Android.
 */
public class AndroidPlatform {	
	
	public static final int MODE_ASSET  = 0;
	public static final int MODE_DATA_PRIVATE    = 1;
	public static final int MODE_WORLD_READABLE  = 2;
	public static final int MODE_WORLD_WRITEABLE = 3;
	
	/**
	 * Create a standard set of globals for Android including all the libraries.
	 * 
	 * @return Table of globals initialized with the standard Android libraries
	 */
	public static LuaTable standardGlobals(Context context) {
		return standardGlobals(context, MODE_ASSET);
	}
	public static LuaTable standardGlobals(Context context, int mode) {
		LuaTable _G = new LuaTable();
		_G.load(new AndroidBaseLib(context, mode));
		_G.load(new PackageLib());
		_G.load(new TableLib());
		_G.load(new StringLib());
		_G.load(new CoroutineLib());
		_G.load(new AndroidMathLib());
		_G.load(new AndroidIoLib(context, mode));
		_G.load(new AndroidOsLib());
		_G.load(new LuajavaLib());
		LuaThread.setGlobals(_G);
		LuaC.install();
		return _G;		
	}
	
	public static LuaTable debugGlobals(Context context) {
		return debugGlobals(context, MODE_ASSET);
	}
	public static LuaTable debugGlobals(Context context, int mode) {
		LuaTable _G = standardGlobals(context, mode);
		_G.load(new DebugLib());
		return _G;
	}

}
