/*
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the name of the contributor nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.e3roid.opengl;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Convenient work-around for poor {@link FloatBuffer#put(float[])} performance.
 * This should become unnecessary in gingerbread, 
 * @see <a href="http://code.google.com/p/android/issues/detail?id=11078">Issue 11078</a>
 * 
 * @author ryanm
 */
public class FastFloatBuffer
{
	/**
	 * Underlying data - give this to OpenGL
	 */
	public ByteBuffer bytes;

	private FloatBuffer floats;

	private IntBuffer ints;
	
	private int bufferID = -1;
	private boolean loaded = false;

	/**
	 * Use a {@link SoftReference} so that the array can be collected
	 * if necessary
	 */
	private static SoftReference<int[]> intArray = new SoftReference<int[]>( new int[ 0 ] );

	public static FastFloatBuffer createBuffer(float[] data) {
		FastFloatBuffer buffer = new FastFloatBuffer(data.length);
		buffer.put(data);
		buffer.position(0);
		return buffer;
	}
		
	/**
	 * Constructs a new direct native-ordered buffer
	 * 
	 * @param capacity
	 *           the number of floats
	 */
	public FastFloatBuffer( int capacity )
	{
		bytes =
				ByteBuffer.allocateDirect( ( capacity * 4 ) ).order( ByteOrder.nativeOrder() );
		floats = bytes.asFloatBuffer();
		ints = bytes.asIntBuffer();
	}

	/**
	 * See {@link FloatBuffer#flip()}
	 */
	public void flip()
	{
		bytes.flip();
		floats.flip();
		ints.flip();
	}

	/**
	 * See {@link FloatBuffer#put(float)}
	 * 
	 * @param f
	 */
	public void put( float f )
	{
		bytes.position( bytes.position() + 4 );
		floats.put( f );
		ints.position( ints.position() + 1 );
	}

	/**
	 * It's like {@link FloatBuffer#put(float[])}, but about 10 times
	 * faster
	 * 
	 * @param data
	 */
	public void put( float[] data )
	{
		int[] ia = intArray.get();
		if( ia == null || ia.length < data.length )
		{
			ia = new int[ data.length ];
			intArray = new SoftReference<int[]>( ia );
		}

		for( int i = 0; i < data.length; i++ )
		{
			ia[ i ] = Float.floatToRawIntBits( data[ i ] );
		}

		bytes.position( bytes.position() + 4 * data.length );
		floats.position( floats.position() + data.length );
		ints.put( ia, 0, data.length );
	}

	/**
	 * For use with pre-converted data. This is 50x faster than
	 * {@link #put(float[])}, and 500x faster than
	 * {@link FloatBuffer#put(float[])}, so if you've got float[] data
	 * that won't change, {@link #convert(float...)} it to an int[]
	 * once and use this method to put it in the buffer
	 * 
	 * @param data
	 *           floats that have been converted with
	 *           {@link Float#floatToIntBits(float)}
	 */
	public void put( int[] data )
	{
		bytes.position( bytes.position() + 4 * data.length );
		floats.position( floats.position() + data.length );
		ints.put( data, 0, data.length );
	}

	/**
	 * Converts float data to a format that can be quickly added to the
	 * buffer with {@link #put(int[])}
	 * 
	 * @param data
	 * @return the int-formatted data
	 */
	public static int[] convert( float... data )
	{
		int[] id = new int[ data.length ];
		for( int i = 0; i < data.length; i++ )
		{
			id[ i ] = Float.floatToRawIntBits( data[ i ] );
		}

		return id;
	}

	/**
	 * See {@link FloatBuffer#put(FloatBuffer)}
	 * 
	 * @param b
	 */
	public void put( FastFloatBuffer b )
	{
		bytes.put( b.bytes );
		floats.position( bytes.position() >> 2 );
		ints.position( bytes.position() >> 2 );
	}

	/**
	 * @return See {@link FloatBuffer#capacity()}
	 */
	public int capacity()
	{
		return floats.capacity();
	}

	/**
	 * @return See {@link FloatBuffer#position()}
	 */
	public int position()
	{
		return floats.position();
	}

	/**
	 * See {@link FloatBuffer#position(int)}
	 * 
	 * @param p
	 */
	public void position( int p )
	{
		bytes.position( 4 * p );
		floats.position( p );
		ints.position( p );
	}

	/**
	 * @return See {@link FloatBuffer#slice()}
	 */
	public FloatBuffer slice()
	{
		return floats.slice();
	}

	/**
	 * @return See {@link FloatBuffer#remaining()}
	 */
	public int remaining()
	{
		return floats.remaining();
	}

	/**
	 * @return See {@link FloatBuffer#limit()}
	 */
	public int limit()
	{
		return floats.limit();
	}

	/**
	 * See {@link FloatBuffer#clear()}
	 */
	public void clear()
	{
		bytes.clear();
		floats.clear();
		ints.clear();
	}
	
	public void setBufferID(int id) {
		this.bufferID = id;
	}
	
	public int getBufferID() {
		return this.bufferID;
	}
	
	public boolean isLoaded() {
		return this.loaded;
	}
	
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
}
