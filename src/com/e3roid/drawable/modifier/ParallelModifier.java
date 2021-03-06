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
package com.e3roid.drawable.modifier;

import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;

import com.e3roid.drawable.Shape;
import com.e3roid.event.ModifierEventListener;

public class ParallelModifier extends ShapeModifier {

	private ArrayList<ShapeModifier> modifiers = new ArrayList<ShapeModifier>();
	private ArrayList<ShapeModifier> removableModifiers = new ArrayList<ShapeModifier>();
	private ModifierEventListener eventListener;
	
	private boolean useLoop    = false;
	private boolean useReverse = false;
	private int maxLoopCount = 0;
	private int currentLoopCount = 0;
	
	public ParallelModifier(ShapeModifier... modifiers) {
		add(modifiers);
	}
	
	/*
	 * set infinite loop
	 */
	public void setLoop() {
		setLoop(0, false);
	}
	/*
	 * set infinite loop with reverse effect
	 */
	public void setLoop(boolean reverse) {
		setLoop(0, reverse);
	}
	/*
	 * set loop count
	 */
	public void setLoop(int count) {
		setLoop(count, false);
	}
	/*
	 * set loop count with reverse effect
	 */
	public void setLoop(int count, boolean reverse) {
		this.currentLoopCount = 0;
		this.maxLoopCount = count;
		this.useReverse = reverse;
		this.useLoop = true;
	}
		
	public void setEventListener(ModifierEventListener eventListener) {
		this.eventListener = eventListener;
	}
	
	public void add(ShapeModifier... modifiers) {
		for(ShapeModifier modifier : modifiers) {
			modifier.hasParentShape(false);
			this.modifiers.add(modifier);
		}
	}
	
	@Override
	public void onResume() {
		for (ShapeModifier modifier : modifiers) {
			modifier.onResume();
		}
	}

	@Override
	public void onPause() {
		for (ShapeModifier modifier : modifiers) {
			modifier.onPause();
		}
	}
	
	@Override
	public void onLoad(Shape shape, GL10 gl) {
		if (eventListener != null) {
			eventListener.onModifierStart(this, shape);
		}
		for(ShapeModifier modifier : modifiers) {
			modifier.onLoad(shape, gl);
		}
	}

	@Override
	public void onBeforeUpdate(Shape shape, GL10 gl) {
		for(ShapeModifier modifier : modifiers) {
			modifier.onBeforeUpdate(shape, gl);
		}
	}

	@Override
	public void onAfterUpdate(Shape shape, GL10 gl) {
		for(ShapeModifier modifier : modifiers) {
			modifier.onAfterUpdate(shape, gl);
			if (modifier.isDurationDone()) {
				removableModifiers.add(modifier);
			}
		}
		if (useLoop) {
			// when all modifiers is finished, next loop starts.
			if (removableModifiers.size() == modifiers.size()) {
				for (int i = 0; i < modifiers.size(); i++) {
					ShapeModifier modifier = modifiers.get(i);
					modifier.reset();
					if (useReverse) {
						modifier.reverse();
					}
					modifiers.set(i, modifier);
				}
				if (maxLoopCount > 0) {
					currentLoopCount++;
					if (currentLoopCount >= maxLoopCount) {
						for(ShapeModifier modifier : modifiers) {
							modifier.onUnload(shape, gl);
						}
						modifiers.clear();
					}
				}
				removableModifiers.clear();
			}
		} else {
			if (!removableModifiers.isEmpty()) {
				for(ShapeModifier modifier : removableModifiers) {
					modifier.onUnload(shape, gl);
					modifiers.remove(modifier);
				}
				removableModifiers.clear();
			}
		}
		
		if (modifiers.isEmpty()) {
			this.finish(shape);
		}
	}

	@Override
	public void onUnload(Shape shape, GL10 gl) {
		if (eventListener != null) {
			eventListener.onModifierFinished(this, shape);
		}
	}

	@Override
	public void reset() {
		this.currentLoopCount = 0;
	}

}
