package com.e3roid.drawable;

import java.util.ArrayList;

import android.view.MotionEvent;
import javax.microedition.khronos.opengles.GL10;

import com.e3roid.E3Activity;
import com.e3roid.E3Engine;
import com.e3roid.E3Scene;
import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.Shape;
import com.e3roid.drawable.modifier.AlphaModifier;
import com.e3roid.drawable.modifier.ProgressModifier;
import com.e3roid.drawable.modifier.function.Linear;
import com.e3roid.drawable.sprite.AnimatedSprite;
import com.e3roid.drawable.texture.Texture;
import com.e3roid.drawable.texture.TiledTexture;

import com.e3roid.interfaces.IRun;
import com.e3roid.interfaces.IWidget;



/**
 * A drawable class that represents menu of the scene.
 */
public class Menu implements  IWidget{

	private ArrayList<Sprite> menuItems = new ArrayList<Sprite>();
	private ArrayList<Sprite> backgroundItems = new ArrayList<Sprite>();
	private ArrayList<Sprite> removedItems = new ArrayList<Sprite>();
	private ArrayList<Sprite> loadableItems = new ArrayList<Sprite>();
	
	private E3Engine engine;
	private boolean removed = false;
	private int totalHeight = 0;
	private int totalWidth  = 0;

	private Sprite[] menuSprites;
	private IRun linkToClass;
	private E3Scene scene;
	private int indexItem;
	public boolean  effect;
	
	
	public  Menu(Sprite[] spites, E3Scene scene, IRun linkToClass){
		this.linkToClass=linkToClass;
		this.menuSprites=spites;		
		this.scene=scene;
		this.effect=false;
		for (int i=0; i<menuSprites.length; i++){
			scene.addEventListener(menuSprites[i]);
		}
	}
	
	public Menu(E3Scene scene, IRun linkToClass){
		this.linkToClass=linkToClass;
		this.scene=scene;
		indexItem=-1;
		this.effect=false;
	}
	
	public Menu(Texture[] textures,  E3Scene scene, final IRun linkToClass){
		this.linkToClass=linkToClass;
		menuSprites=new Sprite [textures.length];
		this.scene=scene;
		this.effect=false;
		for (int i=0; i<textures.length;i++){
			indexItem=i;
			menuSprites[i]= new Sprite(textures[i]){
					public boolean onTouchEvent(E3Scene scene, Shape shape,
							MotionEvent motionEvent, int localX, int localY) {
						if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
							linkToClass.runMethodByIndex(indexItem);
						}
						return false;
					}
			};
			this.scene.addEventListener(menuSprites[i]);
		}
	}
	
	private AnimatedSprite makeAnimation(TiledTexture texture, int X, int Y,  int countFrames, int msecAnim, final boolean useEffects){
		// Add animation frames from tile.
		ArrayList<AnimatedSprite.Frame> frames = new ArrayList<AnimatedSprite.Frame>();
		final IRun linkToClass=this.linkToClass;
		indexItem=indexItem+1;
		final int tmp=indexItem;
		int j=0;
		
		while (j!=countFrames){
			frames.add(new AnimatedSprite.Frame(j, 0));
			j=j+1;
		}
		
		
		AnimatedSprite sprite = new AnimatedSprite(texture, X, Y){
				public boolean onTouchEvent(E3Scene scene, Shape shape,
						MotionEvent motionEvent, int localX, int localY) {
					if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
						if (useEffects==true ){ 
							ProgressModifier modifier =  new ProgressModifier(new AlphaModifier(0, 1, 0), 1000, Linear.getInstance());
							this.addModifier(modifier);
						}
						linkToClass.runMethodByIndex(tmp);
					}
					return false;
				}
			};
		sprite.animate(msecAnim, frames);
		return sprite;
	}
	
	
	/**
	 * Add sprite as menu item to the menu
	 * @param menuItem Sprite
	 */
	public void add(Sprite menuItem) {
		loadableItems.add(menuItem);
		totalHeight += menuItem.getHeight();
		totalWidth  += menuItem.getWidth();
		this.scene.addEventListener(menuItem);
	}

	public void addToBackgroundLayer(Sprite s){
		backgroundItems.add(s);
	}
	
	/**
	 * Remove menu item from the menu
	 * @param menuItem Sprite
	 */
	

	public void add(TiledTexture texture,  int countFrames, int msecAnim){
		Sprite sp=this.makeAnimation(texture, 0, 0, countFrames, msecAnim, this.effect);
		this.add(sp);
		this.scene.addEventListener(sp);
	}
	
	public void add(TiledTexture texture,  int countFrames, int msecAnim, boolean useEffects){
		Sprite sp=this.makeAnimation(texture, 0, 0, countFrames, msecAnim, useEffects);
		this.add(sp);
		this.scene.addEventListener(sp);
	}
	
	public void add(Texture menuItem){
		final IRun linkToClass=this.linkToClass;
		indexItem=indexItem+1;
		final int tmp=indexItem;
		final boolean effect = this.effect;
		
		Sprite sp=new Sprite(menuItem){
			public boolean onTouchEvent(E3Scene scene, Shape shape,
					MotionEvent motionEvent, int localX, int localY) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					if (effect==true ){ 
						ProgressModifier modifier =  new ProgressModifier(new AlphaModifier(0, 1, 0), 1000, Linear.getInstance());
						this.addModifier(modifier);
					}
					linkToClass.runMethodByIndex(tmp);
				}
				return false;
			}
		};
		this.add(sp);
		this.scene.addEventListener(sp);
	}
	
	public void add(Texture menuItem, final boolean useEffects){
		final IRun linkToClass=this.linkToClass;
		indexItem=indexItem+1;
		final int tmp=indexItem;
		
		Sprite sp=new Sprite(menuItem){
			public boolean onTouchEvent(E3Scene scene, Shape shape,
					MotionEvent motionEvent, int localX, int localY) {
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					if (useEffects==true ){ 
						ProgressModifier modifier =  new ProgressModifier(new AlphaModifier(0, 1, 0), 1000, Linear.getInstance());
						this.addModifier(modifier);
					}
					linkToClass.runMethodByIndex(tmp);
				}
				return false;
			}
		};
		this.add(sp);
		this.scene.addEventListener(sp);
	}
	
	public void remove(Sprite menuItem) {
		this.scene.removeEventListener(menuItem);
		removedItems.add(menuItem);
		totalHeight -= menuItem.getHeight();
		totalWidth  -= menuItem.getWidth();
	}
	
	/**
	 * Move menu to the vertical center
	 * @param context E3Activity
	 */
	public void layoutVerticalCenter(E3Activity context) {
		int startY = (context.getHeight() - totalHeight) / 2;	
		
		startY = moveVerticalCenter(menuItems, startY, context);
		startY = moveVerticalCenter(loadableItems, startY, context);
	}
	
	private int moveVerticalCenter(ArrayList<Sprite> items, int startY, E3Activity context) {
		for (Sprite menuItem : items) {
			menuItem.move(
					(context.getWidth() - menuItem.getWidth()) / 2,
					startY);
			startY += menuItem.getHeight();
		}
		return startY;
	}
	
	/**
	 * Move menu to the horizontal center.
	 * @param context E3Activity
	 */
	public void layoutHorizontalCenter(E3Activity context) {
		int startX = (context.getWidth() - totalWidth) / 2;	
		
		startX = moveHorizontalCenter(menuItems, startX, context);
		startX = moveHorizontalCenter(loadableItems, startX, context);
	}

	private int moveHorizontalCenter(ArrayList<Sprite> items, int startX, E3Activity context) {
		for (Sprite menuItem : items) {
			menuItem.move(
					startX,
					(context.getHeight() - menuItem.getHeight()) / 2);
			startX += menuItem.getWidth();
		}
		return startX;
	}
	
	/**
	 * Called when the parent layer is resumed.
	 * This method has no relation to Activity#onResume().
	 */
	@Override
	public void onResume() {
		for (Sprite Item : backgroundItems) {
			Item.onResume();
		}
		for (Sprite menuItem : menuItems) {
			menuItem.onResume();
		}
	}

	/**
	 * Called when the parent layer is paused.
	 * This method has no relation to Activity#onPause().
	 */
	@Override
	public void onPause() {
		for (Sprite Item : backgroundItems) {
			Item.onPause();
		}
		for (Sprite menuItem : menuItems) {
			menuItem.onPause();
		}
	}

	/**
	 * Called when this shape is disposed.
	 */
	@Override
	public void onDispose() {
		for (Sprite Item : backgroundItems) {
			Item.onDispose();
		}
		for (Sprite menuItem : menuItems) {
			menuItem.onDispose();
		}
	}

	/**
	 * Called to draw the shape.
	 * This method is responsible for drawing the shape. 
	 */
	@Override
	public void onDraw(GL10 gl) {
		if (!loadableItems.isEmpty()) {
			for (Sprite Item : backgroundItems){ 
				Item.onLoadEngine(engine);
				Item.onLoadSurface(gl);
			}
			for (Sprite menuItem : loadableItems) {
				menuItem.onLoadEngine(engine);
				menuItem.onLoadSurface(gl);
				menuItems.add(menuItem);
			}
			loadableItems.clear();
		}
		for (Sprite Item : backgroundItems){ 
			Item.onDraw(gl);
		}
		for (Sprite menuItem : menuItems) {
			menuItem.onDraw(gl);
		}

		if (!removedItems.isEmpty()) {
			for (Sprite menuItem : removedItems) {
				menuItems.remove(menuItem);
				menuItem.onDispose();
			}
			removedItems.clear();
		}
	}

	/**
	 * Called when this shape is removed.
	 */
	@Override
	public void onRemove() {
		this.removed = true;
	}

	/**
	 * Called when the shape is created or recreated.
	 * @param gl GL object
	 */
	@Override
	public void onLoadSurface(GL10 gl) {
		onLoadSurface(gl, false);
	}

	/**
	 * Called when the shape is created or recreated.
	 * @param gl GL object
	 * @param force force load when already loaded
	 */
	@Override
	public void onLoadSurface(GL10 gl, boolean force) {
		if (force) {
			for (Sprite Item : backgroundItems){ 
				Item.onLoadSurface(gl, true);
			}
			for (Sprite menuItem : menuItems) {
				menuItem.onLoadSurface(gl, true);
			}
		}
	}
	
	/**
	 * Called when e3roid engine has been loaded.
	 */
	@Override
	public void onLoadEngine(E3Engine engine) {
		this.engine = engine;
	}

	/**
	 * Returns whether shape is removed not not.
	 */
	@Override
	public boolean isRemoved() {
		return removed;
	}

	/**
	 * Returns whether the shape is collided with given x coordinate or not.
	 * 
	 * @param globalX global x coordinate
	 * @return whether x axis is collided or not.
	 */
	@Override
	public boolean contains(int x, int y) {
		return false;
	}
}