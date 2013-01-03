package com.e3roid.drawable.sprite;

import java.util.Hashtable;

import android.content.Context;
import android.view.MotionEvent;

import com.e3roid.E3Scene;
import com.e3roid.drawable.Shape;
import com.e3roid.drawable.modifier.AlphaModifier;
import com.e3roid.drawable.modifier.ProgressModifier;
import com.e3roid.drawable.modifier.function.Linear;
import com.e3roid.drawable.texture.TiledTexture;
import com.e3roid.interfaces.IRun;
import com.e3roid.util.E3Hash;

public class Loader {
	private E3Hash e3Hash;
	private Context context;
	private Hashtable texturesHash;
	private IRun linkToClass;
	
	
	public Loader(E3Hash hashOfItems, Context context, IRun linkToClass){
		this.e3Hash=hashOfItems;
		this.context=context;
		this.linkToClass=linkToClass;
	}
	
	
	
	public TiledTexture getTexture(String nameOfTexture){
		if (texturesHash.containsKey(nameOfTexture)){
			return (TiledTexture) texturesHash.get(nameOfTexture);
		}
		else{
			Hashtable h=(Hashtable) this.e3Hash.get(nameOfTexture);
			TiledTexture t= new TiledTexture((String)h.get("path"), (Integer)h.get("x"), (Integer)h.get("y"), 0, 0, 0, 0, this.context);
			texturesHash.put(nameOfTexture, t);
			return t;
		}
	}
	
	public AnimatedSprite getAnimatedSprite(String nameOfTexture){
		TiledTexture t=this.getTexture(nameOfTexture);
		Hashtable h = (Hashtable) e3Hash.get(nameOfTexture);
		final String name=nameOfTexture;
		return new AnimatedSprite(t,(Integer)h.get("x"),(Integer)h.get("y")){
			public boolean onTouchEvent(E3Scene scene, Shape shape,
					MotionEvent motionEvent, int localX, int localY) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					linkToClass.runMethodByKey(name);
				}
				return false;
			}
		};
	}
}
