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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.RandomAccessFile;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.IoLib;

import android.content.Context;

/**
 * IO library implementation for use on Android platform.
 */
public class AndroidIoLib extends IoLib {
	
	private final Context context;
	private final int mode;
	
	public AndroidIoLib(Context context, int mode) {
		this.context = context;
		this.mode    = mode;
	}

	@Override
	protected File wrapStdin() throws IOException {
		return new FileImpl(BaseLib.instance.STDIN);
	}
	
	@Override
	protected File wrapStdout() throws IOException {
		return new FileImpl(BaseLib.instance.STDOUT);
	}
	
	@Override
	protected File openFile(String filename, boolean readMode, 
			boolean appendMode, boolean updateMode, boolean binaryMode) throws IOException {
		File file = null;
		
		int scope = Context.MODE_PRIVATE;
		
		if (mode == AndroidPlatform.MODE_WORLD_READABLE) {
			scope = Context.MODE_WORLD_READABLE;
		} else if (mode == AndroidPlatform.MODE_WORLD_WRITEABLE) {
			scope = Context.MODE_WORLD_WRITEABLE;
		}
		
		if (readMode && mode == AndroidPlatform.MODE_ASSET) {
			file = new FileImpl(context.getAssets().open(filename));
		} else if (readMode) {
			file = new FileImpl(context.openFileInput(filename));
		} else if (appendMode) {
			file = new FileImpl(context.openFileOutput(filename, scope|Context.MODE_APPEND));
		} else if (updateMode) {
			file = new FileImpl(context.openFileOutput(filename, scope));
		} else {
			notimplemented();
		}
		
		return file;
	}
	
	@Override
	protected File openProgram(String prog, String mode) throws IOException {
		final Process p = Runtime.getRuntime().exec(prog);
		return "w".equals(mode)? 
				new FileImpl( p.getOutputStream() ):  
				new FileImpl( p.getInputStream() ); 
	}

	@Override
	protected File tmpFile() throws IOException {
		java.io.File f = java.io.File.createTempFile(".lua.",".bin");
		f.deleteOnExit();
		return new FileImpl( new RandomAccessFile(f,"rw") );
	}

	private static void notimplemented() {
		throw new LuaError("not implemented");
	}
	
	private final class FileImpl extends File {
		private final RandomAccessFile file;
		private final InputStream is;
		private final OutputStream os;
		private boolean closed = false;
		private boolean nobuffer = false;
		private FileImpl( RandomAccessFile file, InputStream is, OutputStream os ) {
			this.file = file;
			this.is = is!=null? is.markSupported()? is: new BufferedInputStream(is): null;
			this.os = os;
		}
		private FileImpl( RandomAccessFile f ) {
			this( f, null, null );
		}
		private FileImpl( InputStream i ) {
			this( null, i, null );
		}
		private FileImpl( OutputStream o ) {
			this( null, null, o );
		}
		@Override
		public String tojstring() {
			return "file ("+this.hashCode()+")";
		}
		@Override
		public boolean isstdfile() {
			return file == null;
		}
		@Override
		public void close() throws IOException  {
			closed = true;
			if ( file != null ) {
				file.close();
			}
		}
		@Override
		public void flush() throws IOException {
			if ( os != null )
				os.flush();
		}
		@Override
		public void write(LuaString s) throws IOException {
			if ( os != null )
				os.write( s.m_bytes, s.m_offset, s.m_length );
			else if ( file != null )
				file.write( s.m_bytes, s.m_offset, s.m_length );
			else
				notimplemented();
			if ( nobuffer )
				flush();
		}
		@Override
		public boolean isclosed() {
			return closed;
		}
		@Override
		public int seek(String option, int pos) throws IOException {
			if ( file != null ) {
				if ( "set".equals(option) ) {
					file.seek(pos);
				} else if ( "end".equals(option) ) {
					file.seek(file.length()+pos);
				} else {
					file.seek(file.getFilePointer()+pos);
				}
				return (int) file.getFilePointer();
			}
			notimplemented();
			return 0;
		}
		@Override
		public void setvbuf(String mode, int size) {
			nobuffer = "no".equals(mode);
		}

		// get length remaining to read
		@Override
		public int remaining() throws IOException {
			return file!=null? (int) (file.length()-file.getFilePointer()): -1;
		}
		
		// peek ahead one character
		@Override
		public int peek() throws IOException {
			if ( is != null ) {
				is.mark(1);
				int c = is.read();
				is.reset();
				return c;
			} else if ( file != null ) {
				long fp = file.getFilePointer();
				int c = file.read();
				file.seek(fp);
				return c;
			}
			notimplemented();
			return 0;
		}		
		
		// return char if read, -1 if eof, throw IOException on other exception 
		@Override
		public int read() throws IOException {
			if ( is != null ) 
				return is.read();
			else if ( file != null ) {
				return file.read();
			}
			notimplemented();
			return 0;
		}

		// return number of bytes read if positive, -1 if eof, throws IOException
		@Override
		public int read(byte[] bytes, int offset, int length) throws IOException {
			if (file!=null) {
				return file.read(bytes, offset, length);
			} else if (is!=null) {
				return is.read(bytes, offset, length);
			} else {
				notimplemented();
			}
			return length;
		}
	}
}
