package com.e3roid.script;

public interface ScriptEngine {
	<T> T call(String name, Object... args);
}
