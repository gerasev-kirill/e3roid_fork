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

import android.util.FloatMath;

public class ElasticOut implements Progressive {

	private static ElasticOut instance;
	
	public static ElasticOut getInstance() {
		if (instance == null) {
			instance = new ElasticOut();
		}
		return instance;
	}
	
	@Override
	public float getProgress(float elapsed, float duration, float minValue,
			float maxValue) {
		float i = 0.0f;
		float j = 0.0f;
		float k = 0.0f;
		if(elapsed == 0) {
			return minValue;
		}
		if((elapsed /= duration) == 1) {
			return minValue + maxValue;
		}
		if(j == 0) {
			j = duration * 0.3f;
		}
		if(k == 0 || (maxValue > 0 && k < maxValue) || (maxValue < 0 && k < -maxValue)) {
			k = maxValue;
			i = j / 4;
		} else {
			i = (float) (j / (float)Math.PI * 2.0f * Math.asin(maxValue / k));
		}
		return (float) (k * Math.pow(2, -10 * elapsed) * FloatMath.sin((elapsed * duration - i) * (float)Math.PI * 2.0f / j) + maxValue + minValue);
	}

}
