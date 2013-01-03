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
package com.e3roid.drawable.controls;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.Toast;

import com.e3roid.E3Activity;
import com.e3roid.E3Scene;
import com.e3roid.drawable.Shape;
import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.texture.Texture;
import com.e3roid.event.ControllerEventListener;
import com.e3roid.interfaces.IController;

/**
 * On-Screen analog touch controller
 */
public class DelusionController extends Sprite implements  IController{
	
	private int direction=0;
	protected final E3Scene scene;
	protected Sprite object;
	protected int objectCenterX;
	protected int objectCenterY;
	protected int updateInterval = 200;
	protected boolean fastUpdate = true;
	private ControllerEventListener listener;
	protected int moveX;
	protected int moveY;
	private int childCenterX;
	private int childCenterY;
	

	/**
	 * Constructs analog on-screen controller with given textute, position and listener
	 * @param baseTexture base texture
	 * @param objectTexture object texture
	 * @param x x position
	 * @param y y position
	 * @param scene E3Scene
	 * @param listener ControllerEventListener
	 */
	public DelusionController(Texture baseTexture, Texture objectTexture,
			int x, int y, E3Scene scene, ControllerEventListener listener) {
		super(baseTexture, x, y);
		this.scene = scene;
		this.listener=listener;
		this.object = new Sprite(objectTexture, 0, 0)	;
		this.addChild(object);
		this.setChildPosition(this.object);
		setAlpha(0.7f);
	}
	
	private void setChildPosition(Sprite child){
		float[] baseCoord = getLocalCenterCoordinates();
		float[] objectCoord = child.getLocalCenterCoordinates();
		
		childCenterX = (int)(getRealX() + baseCoord[0] - objectCoord[0]);
		childCenterY = (int)(getRealY() + baseCoord[1] - objectCoord[1]);
		child.move(childCenterX, childCenterY);
	}
	
	private int[] getFromPolicyXY(Sprite texture, int positionPolicy, E3Activity context){
		int x=0,y=0;
		int tx=0,ty=0;
		tx=texture.getWidth();
		ty=texture.getHeight();
		if (positionPolicy==IController.TOP_LEFT_CORNER){
			x=this.getRealX();
			y=this.getRealY();
		}
		else if (positionPolicy==IController.TOP_RIGHT_CORNER){
			x=this.getRealX()+this.getWidth()-tx;
			y=this.getRealY();
		}
		else if (positionPolicy==IController.BOTTOM_LEFT_CORNER){
			x=this.getRealX();
			y=this.getRealY()+this.getHeight()-ty;
		}
		else if (positionPolicy==IController.BOTTOM_RIGHT_CORNER){
			x=this.getRealX()+this.getWidth()-tx;
			y=this.getRealY()+this.getHeight()-ty;
		}
		return new int[] {x,y};
	}
	

	
	public void setObjectToQuarter(Texture baseTexture, int posPolicy, E3Activity context){
		Sprite object2 = new  Sprite(baseTexture);
		this.addChild(object2);
		//this.scene.forceReloadScene();
		int xy[]=this.getFromPolicyXY(object2, posPolicy,context);
		object2.move(xy[0], xy[1]);
		this.scene.forceReloadScene();
	}
	
	public void updateTexture(Texture baseTexture){
		super.setNewTexture(baseTexture);
//		this.scene.forceReloadScene();
		this.setChildPosition(this.object);
//		this.object.show();
		this.scene.forceReloadScene();
	}
	
	public void updateTexture(Texture baseTexture, Texture objectTexture){
		this.object = new Sprite(objectTexture, 0, 0);
		this.updateTexture(baseTexture);
	}
	
	public void updateTextureObject(Texture objectTexture){
		this.removeChild(this.object);
		object = new Sprite(objectTexture, 0, 0);
	//	this.scene.forceReloadScene();
		super.addChild(object);
		this.setChildPosition(this.object);
    	this.scene.forceReloadScene();
	}
	
	
	public boolean onSceneTouchEvent(E3Scene scene, MotionEvent motionEvent) {
		int actionType = motionEvent.getAction() & MotionEvent.ACTION_MASK;
		if (actionType == MotionEvent.ACTION_UP || 
				actionType == MotionEvent.ACTION_CANCEL ||
				actionType == MotionEvent.ACTION_POINTER_UP ||
				actionType == MotionEvent.ACTION_OUTSIDE){ 
		}
		super.onSceneTouchEvent(scene, motionEvent);
		return false;
	}

	public boolean onTouchEvent(E3Scene scene, Shape shape,
				MotionEvent motionEvent, int localX, int localY) {
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				int w=this.getWidth();
				int h=this.getHeight();
				int p1x=this.object.getRealX();
				int p1y=this.object.getRealY()-this.object.getHeight()/2;
				int p2x=p1x+this.object.getWidth();
				int p2y=p1y+this.object.getHeight();
				
				if (localX<p2x && localX>p1x && localY<p2y && localY>p1y){
					this.direction=IController.CENTER;
				}
				else if (localX>w/2 && localY<h/2){
					this.direction=IController.TOP_RIGHT_CORNER;
				}
				else if (localX<w/2 && localY<h/2 ){
					this.direction=IController.TOP_LEFT_CORNER;
				}
				else if (localX<w/2 && localY>h/2){
					this.direction=IController.BOTTOM_LEFT_CORNER;
				}
				else if (localX > w/2 && localY> h/2){
					this.direction=IController.BOTTOM_RIGHT_CORNER;
				}
				this.listener.onControlUpdate(this, localX, localY, true);
				return true;
			}
			return false;
		}
	
	
	public int getDirection() {
		return this.direction;
	}	
}
