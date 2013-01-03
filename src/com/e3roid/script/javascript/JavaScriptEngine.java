package com.e3roid.script.javascript;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import com.e3roid.E3Activity;
import com.e3roid.lifecycle.E3LifeCycle;
import com.e3roid.script.ScriptEngine;
import com.e3roid.script.ScriptException;

/**
 * An engine for scripting with JavaScript.
 */
public class JavaScriptEngine implements ScriptEngine, E3LifeCycle {

	private Context context;
	private Scriptable scriptable;
	private Object jsValue;
	private boolean reloadOnResume = false;
	
	public JavaScriptEngine(InputStream in, String source) {
		jsValue = evaluate(new InputStreamReader(in), source);
	}
	
	public Object evaluate(Reader reader, String source) {
		try {
			loadContext();
			return (context.evaluateReader(scriptable, reader, source, 1, null));
		} catch (IOException e) {
			throw new ScriptException(e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T call(String name, Object... args) {
		Object fObj = scriptable.get(name, scriptable);
		
		if (fObj instanceof Function) {
			Function f = (Function)fObj;
			return (T)f.call(context, scriptable, scriptable, args);
		} else {
			throw new ScriptException(name + " is undefined or not a function.");
		}
	}
	
	public Object get(String name) {
		return scriptable.get(name, scriptable);
	}

	public static JavaScriptEngine loadFromAsset(String filename, E3Activity container) {
		try {
			return load(container.getAssets().open(filename), filename, container);
		} catch (IOException e) {
			throw new ScriptException(e);
		}
	}
	
	public static JavaScriptEngine loadFromData(String filename, E3Activity container) {
		try {
			return load(container.openFileInput(filename), filename, container);
		} catch (FileNotFoundException e) {
			throw new ScriptException(e);
		}
	}
	
	public static JavaScriptEngine load(InputStream in, String source, E3Activity container) {
		JavaScriptEngine engine = new JavaScriptEngine(in, source);
		container.getEngine().addLifeCycle(engine);
		return engine;
	}
	
	public Object getEvaluatedValue() {
		return this.jsValue;
	}
	
	public Scriptable getScriptable() {
		return this.scriptable;
	}
	
	public Context getContext() {
		return this.context;
	}
	
	public boolean unload(E3Activity container) {
		exitContext();
		return container.getEngine().removeLifeCycle(this);
	}
	
	public boolean exitContext() {
		if (Context.getCurrentContext() != null) {
			Context.exit();
			this.context = null;
			return true;
		}
		return false;
	}
	
	public void loadContext() {
		if (context == null) {
			context = Context.enter();
			context.setOptimizationLevel(-1);
			scriptable = context.initStandardObjects();
		}
	}
	
	public void reloadOnResume(boolean reload) {
		this.reloadOnResume = reload;
	}
	
	@Override
	public void onResume() {
		loadContext();
	}

	@Override
	public void onPause() {
		if (reloadOnResume) {
			exitContext();
		}
	}

	@Override
	public void onDispose() {
		exitContext();
	}
}
