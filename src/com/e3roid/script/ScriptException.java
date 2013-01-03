package com.e3roid.script;

/**
 * Wrapper class for script exception
 */
public class ScriptException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ScriptException(String message) {
		super(message);
	}
	
	public ScriptException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ScriptException(Throwable cause) {
		super(cause);
	}
}
