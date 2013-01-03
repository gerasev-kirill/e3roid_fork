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
package com.e3roid.script.lua;

import android.content.Context;
import org.luaj.vm2.LuaValue;

import com.e3roid.script.ScriptEngine;

/**
 * An engine for scripting with Lua.
 */
public class LuaScriptEngine implements ScriptEngine {
	
	private final LuaValue engineValue;

	public LuaScriptEngine(LuaValue luaValue) {
		this.engineValue = luaValue;
	}
	
	@Override
	public <T> T call(String name, Object... args) {
		LuaValue[] argsValue = new LuaValue[args.length];
		
		for (int i = 0; i < args.length; i++) {
			argsValue[i] = toLuaValue(args[i]);
		}

		LuaValue value = engineValue.get(name).invoke(argsValue).arg1();
		
		return inferValue(value);
	}
	
	public boolean has(String name) {
		return engineValue.get(name) != LuaValue.NIL;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T inferValue(LuaValue value) {
		T tValue = null;
		if (value.isint()) {
			tValue = (T)Integer.valueOf(value.toint());
		} else if (value.isnumber()) {
			tValue = (T)Double.valueOf(value.todouble());
		} else if (value.isboolean()) {
			tValue = (T)Boolean.valueOf(value.toboolean());
		} else if (value.isstring()) {
			tValue = (T)value.tojstring();
		} else if (value.isuserdata()) {
			tValue = (T)value.touserdata();
		} else {
			tValue = (T)value;
		}
		return tValue;
	}
	
	public static LuaValue toLuaValue(Object obj) {
		LuaValue argValue = null;
		if (obj.getClass() == java.lang.String.class) {
			argValue = LuaValue.valueOf((String)obj);
		} else if (obj.getClass() == java.lang.Boolean.class) {
			argValue = LuaValue.valueOf((Boolean)obj);
		} else if (obj.getClass() == java.lang.Double.class) {
			argValue = LuaValue.valueOf((Double)obj);
		} else if (obj.getClass() == java.lang.Integer.class) {
			argValue = LuaValue.valueOf((Integer)obj);
		} else {
			argValue = LuajavaLib.toUserdata(obj, obj.getClass());
		}
		return argValue;
	}
	
	public static LuaScriptEngine loadFromAsset(String filename, Context context) {
		return load(filename, AndroidPlatform.MODE_ASSET, context);
	}
	
	public static LuaScriptEngine loadFromData(String filename, Context context) {
		return load(filename, AndroidPlatform.MODE_DATA_PRIVATE, context);
	}
	
	public static LuaScriptEngine load(String filename, int mode, Context context) {
		LuaValue luaValue = AndroidPlatform.standardGlobals(context, mode);
		luaValue.get("dofile").call(LuaValue.valueOf(filename));
		return new LuaScriptEngine(luaValue);
	}
	
	public static LuaScriptEngine loadForDebug(String filename, int mode, Context context) {
		LuaValue luaValue = AndroidPlatform.debugGlobals(context, mode);
		luaValue.get("dofile").call(LuaValue.valueOf(filename));
		return new LuaScriptEngine(luaValue);
	}
	
	public static LuaScriptEngine loadFromAssetForDebug(String filename, Context context) {
		return loadForDebug(filename, AndroidPlatform.MODE_ASSET, context);
	}
	
	public static LuaScriptEngine loadFromDataForDebug(String filename, Context context) {
		return loadForDebug(filename, AndroidPlatform.MODE_DATA_PRIVATE, context);
	}
}
