package com.e3roid.drawable;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Typeface;

import com.e3roid.E3Activity;
import com.e3roid.E3Engine;
import com.e3roid.E3Scene;
import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.sprite.TextSprite;
import com.e3roid.interfaces.IWidget;



/**
 * A drawable class that represents menu of the scene.
 */
public class ProgressBar implements  IWidget{

	private E3Engine engine;
	private boolean removed = false;
	private int totalHeight = 0;
	private int totalWidth  = 0;

	private E3Scene scene;
	public boolean  effect;
	private ArrayList<Sprite> loadableItems=new ArrayList<Sprite>();
	private ArrayList<Sprite> backgroundItems=new ArrayList<Sprite>();
	private ArrayList<Sprite> removedItems=new ArrayList<Sprite>();
	private ArrayList<Sprite> widgetItems=new ArrayList<Sprite>();
	private E3Activity context;
	private TextSprite progressSprite;
	
	
	public  ProgressBar(E3Scene scene, E3Activity context){
		this.context=context;
		this.scene=scene;
	}
	
	public  ProgressBar(E3Scene scene, Sprite progressItem,E3Activity context){
		this.scene=scene;
		this.context=context;
		this.add(progressItem);
	}
	
	public void setProgressSprite(int textSize, int bg, int fg){
		progressSprite = new TextSprite("0 %", textSize, fg, bg, 50, Typeface.DEFAULT, this.context);
		this.add(progressSprite);
	}
	public void setProgress(int percent){
		progressSprite.setText(String.valueOf(percent)+"%");
	}
	/**
	 * Add sprite as menu item to the menu
	 * @param widgetItem Sprite
	 */
	public void add(Sprite progressItem) {
		loadableItems.add(progressItem);
		totalHeight += progressItem.getHeight();
		totalWidth  += progressItem.getWidth();
	}

	public void addToBackgroundLayer(Sprite s){
		backgroundItems.add(s);
	}
	
	/**
	 * Remove menu item from the menu
	 * @param widgetItem Sprite
	 */
	public void remove(Sprite widgetItem) {
		this.scene.removeEventListener(widgetItem);
		removedItems.add(widgetItem);
		totalHeight -= widgetItem.getHeight();
		totalWidth  -= widgetItem.getWidth();
	}
	
	/**
	 * Move menu to the vertical center
	 * @param context E3Activity
	 */
	public void layoutVerticalCenter(E3Activity context) {
		int startY = (context.getHeight() - totalHeight) / 2;	
		
		startY = moveVerticalCenter(widgetItems, startY, context);
		startY = moveVerticalCenter(loadableItems, startY, context);
	}
	
	private int moveVerticalCenter(ArrayList<Sprite> items, int startY, E3Activity context) {
		for (Sprite widgetItem : items) {
			widgetItem.move(
					(context.getWidth() - widgetItem.getWidth()) / 2,
					startY);
			startY += widgetItem.getHeight();
		}
		return startY;
	}
	
	/**
	 * Move menu to the horizontal center.
	 * @param context E3Activity
	 */
	public void layoutHorizontalCenter(E3Activity context) {
		int startX = (context.getWidth() - totalWidth) / 2;	
		
		startX = moveHorizontalCenter(widgetItems, startX, context);
		startX = moveHorizontalCenter(loadableItems, startX, context);
	}

	private int moveHorizontalCenter(ArrayList<Sprite> items, int startX, E3Activity context) {
		for (Sprite widgetItem : items) {
			widgetItem.move(
					startX,
					(context.getHeight() - widgetItem.getHeight()) / 2);
			startX += widgetItem.getWidth();
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
		for (Sprite widgetItem : widgetItems) {
			widgetItem.onResume();
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
		for (Sprite widgetItem : widgetItems) {
			widgetItem.onPause();
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
		for (Sprite widgetItem : widgetItems) {
			widgetItem.onDispose();
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
			for (Sprite widgetItem : loadableItems) {
				widgetItem.onLoadEngine(engine);
				widgetItem.onLoadSurface(gl);
				widgetItems.add(widgetItem);
			}
			loadableItems.clear();
		}
		for (Sprite Item : backgroundItems){ 
			Item.onDraw(gl);
		}
		for (Sprite widgetItem : widgetItems) {
			widgetItem.onDraw(gl);
		}

		if (!removedItems.isEmpty()) {
			for (Sprite widgetItem : removedItems) {
				widgetItems.remove(widgetItem);
				widgetItem.onDispose();
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
			for (Sprite widgetItem : widgetItems) {
				widgetItem.onLoadSurface(gl, true);
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