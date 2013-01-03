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
package com.e3roid.util;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.connectbot.transport.StreamTransport;

import android.util.Log;

/**
 * Helpers to report debugging messages.
 */
public class Debug {

	public static final String ENGINE_TAG = "E3roid";
	public static Level level = Level.DEBUG;
	
	private static PipedOutputStream outputStream;
	private static StreamTransport streamTransport;
	
	public static void setLevel(Level l) {
		level  = l;
	}
	
	public static void v(String message) {
		Debug.v(message, null);
	}
	
	public static void v(String message, boolean writeOut) {
		Debug.v(message, null, writeOut);
	}
	
	public static void v(String message, Throwable th) {
		Debug.v(message, th, true);
	}
	
	public static void v(String message, Throwable th, boolean writeOut) {
		if (level.lessThanOrEqualTo(Level.VERBOSE)) {
			Log.v(ENGINE_TAG, message, th);
			if (writeOut) writeOut(message, th);
		}
	}
	
	public static void d(String message) {
		Debug.d(message, null);
	}
	
	public static void d(String message, boolean writeOut) {
		Debug.d(message, null, writeOut);
	}
	
	public static void d(String message, Throwable th) {
		Debug.d(message, th, true);
	}
	
	public static void d(String message, Throwable th, boolean writeOut) {
		if (level.lessThanOrEqualTo(Level.DEBUG)) {
			Log.d(ENGINE_TAG, message, th);
			if (writeOut) writeOut(message, th);
		}
	}
	
	public static void i(String message) {
		Debug.i(message, null);
	}
	
	public static void i(String message, boolean writeOut) {
		Debug.i(message, null, writeOut);
	}
	
	public static void i(String message, Throwable th) {
		Debug.i(message, th, true);
	}
	
	public static void i(String message, Throwable th, boolean writeOut) {
		if (level.lessThanOrEqualTo(Level.INFO)) {
			Log.i(ENGINE_TAG, message, th);
			if (writeOut) writeOut(message, th);
		}
	}
	
	public static void w(String message) {
		Debug.w(message, null);
	}
	
	public static void w(String message, boolean writeOut) {
		Debug.w(message, null, writeOut);
	}
	
	public static void w(String message, Throwable th) {
		Debug.w(message, th, true);
	}
	
	public static void w(String message, Throwable th, boolean writeOut) {
		if (level.lessThanOrEqualTo(Level.WARNING)) {
			Log.w(ENGINE_TAG, message, th);
			if (writeOut) writeOut(message, th);
		}
	}
	
	public static void e(String message) {
		Debug.e(message, null);
	}
	
	public static void e(String message, boolean writeOut) {
		Debug.e(message, null, writeOut);
	}
	
	public static void e(Throwable th) {
		Debug.e(th.getClass().getName(), th);
	}
	
	public static void e(Throwable th, boolean writeOut) {
		Debug.e(th.getClass().getName(), th, writeOut);
	}
	
	public static void e(String message, Throwable th) {
		Debug.e(message, th, true);
	}
	
	public static void e(String message, Throwable th, boolean writeOut) {
		if (level.lessThanOrEqualTo(Level.ERROR)) {
			Log.e(ENGINE_TAG, message, th);
			if (writeOut) writeOut(message, th);
		}
	}
	
	public static String getStackTraceString(Throwable th) {
        if (th == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        return sw.toString().replace("\n", "\r\n");
	}
	
	public static void writeOut(String message, Throwable th) {
		if (outputStream != null) {
			
			StringBuffer buffer = new StringBuffer();
			buffer.append(message);
			buffer.append("\r\n");
			buffer.append(getStackTraceString(th));
			
			try {
				outputStream.write(buffer.toString().getBytes());
				outputStream.flush();
			} catch (IOException e) {
				// do nothing
			}
		}
	}
	
	public static StreamTransport connect() throws IOException {
		if (streamTransport != null) return streamTransport;
		if (outputStream == null) outputStream = new PipedOutputStream();
		streamTransport = new StreamTransport(new PipedInputStream(outputStream), null);
		return streamTransport;
	}
	
	public static void disconnect() {
		if (streamTransport != null) {
			streamTransport.close();
		}
		streamTransport = null;
		outputStream = null;
	}
	
	public static enum Level {
		NONE, ERROR, WARNING, INFO, DEBUG, VERBOSE;
		private boolean lessThanOrEqualTo(Level level) {
			return compareTo(level) >= 0;
		}
	}
}
