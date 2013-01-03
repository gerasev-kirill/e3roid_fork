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
package com.e3roid.drawable.modifier.function;

public class AcceleroIn implements Progressive {

	private static AcceleroIn instance1;
	private static AcceleroIn instance2;
	private static AcceleroIn instance3;
	private static AcceleroIn instance4;
	private static AcceleroIn instance5;
	
	private int power = 1;
	
	public AcceleroIn(int power) {
		this.power = power;
	}

	public static AcceleroIn getInstance() {
		if (instance1 == null) {
			instance1 = new AcceleroIn(1);
		}
		return instance1;
	}
	public static AcceleroIn getSquareInstance() {
		if (instance2 == null) {
			instance2 = new AcceleroIn(2);
		}
		return instance2;
	}
	public static AcceleroIn getCubicInstance() {
		if (instance3 == null) {
			instance3 = new AcceleroIn(3);
		}
		return instance3;
	}
	public static AcceleroIn getBiquadInstance() {
		if (instance4 == null) {
			instance4 = new AcceleroIn(4);
		}
		return instance4;
	}
	public static AcceleroIn getQuintInstance() {
		if (instance5 == null) {
			instance5 = new AcceleroIn(5);
		}
		return instance5;
	}

	@Override
	public float getProgress(float elapsed, float duration,
			float minValue, float maxValue) {
		return (float)(maxValue * (elapsed /= duration) * Math.pow(elapsed, power) + minValue);
	}

}
