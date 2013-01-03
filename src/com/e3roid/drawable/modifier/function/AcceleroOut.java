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

public class AcceleroOut implements Progressive {

	private static AcceleroOut instance1;
	private static AcceleroOut instance2;
	private static AcceleroOut instance3;
	private static AcceleroOut instance4;
	private static AcceleroOut instance5;
	
	private int power = 1;
	
	public AcceleroOut(int power) {
		this.power = power;
	}

	public static AcceleroOut getInstance() {
		if (instance1 == null) {
			instance1 = new AcceleroOut(1);
		}
		return instance1;
	}
	public static AcceleroOut getSquareInstance() {
		if (instance2 == null) {
			instance2 = new AcceleroOut(2);
		}
		return instance2;
	}
	public static AcceleroOut getCubicInstance() {
		if (instance3 == null) {
			instance3 = new AcceleroOut(3);
		}
		return instance3;
	}
	public static AcceleroOut getBiquadInstance() {
		if (instance4 == null) {
			instance4 = new AcceleroOut(4);
		}
		return instance4;
	}
	public static AcceleroOut getQuintInstance() {
		if (instance5 == null) {
			instance5 = new AcceleroOut(5);
		}
		return instance5;
	}

	@Override
	public float getProgress(float elapsed, float duration,
			float minValue, float maxValue) {
		return (float)(-maxValue * ((elapsed = elapsed / duration - 1) * Math.pow(elapsed, power) - 1) + minValue);
	}

}
