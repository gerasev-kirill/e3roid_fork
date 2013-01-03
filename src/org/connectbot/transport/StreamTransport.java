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
package org.connectbot.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamTransport extends AbsTransport {

	private boolean connected = false;
	private final InputStream in;
	private final OutputStream out;
	
	public StreamTransport(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}
	
	@Override
	public void connect() {
		this.connected = true;
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		if (in != null) {
			return in.read(buffer, offset, length);
		} else {
			throw new IOException("session closed");
		}
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		if (out != null) out.write(buffer);
	}

	@Override
	public void write(int c) throws IOException {
		if (out != null) out.write(c);
	}

	@Override
	public void flush() throws IOException {
		if (out != null) out.flush();
	}

	@Override
	public void close() {
		try {
			if (in != null) in.close();
		} catch (IOException e) {
			// nothing to do
		}
		try {
			if (out != null) out.close();
		} catch (IOException e) {
			// nothing to do
		}
		this.connected = false;
	}

	@Override
	public void setDimensions(int columns, int rows, int width, int height) {
		// nothing to do
	}

	@Override
	public boolean isConnected() {
		return this.connected;
	}

	@Override
	public boolean isSessionOpen() {
		return this.connected;
	}

}
