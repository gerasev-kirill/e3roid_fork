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
package com.e3roid.drawable.sprite;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.e3roid.drawable.Sprite;
import com.e3roid.drawable.texture.BitmapTexture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;

/**
 * A TextSprite class is used to display texts as sprite.
 */
public class TextSprite extends Sprite {
	private final Context context;
	private Paint textPaint = new Paint();
	private Paint backPaint = new Paint();
	private FontMetrics fontMetrics;
	private Typeface typeFace = Typeface.DEFAULT;
	private int textSize;
	private boolean antiAlias = true;
	private int textColor = Color.BLACK;
	private int backColor = Color.TRANSPARENT;
	private int preferredWidth = 0;
	
	private String text;
	
	private boolean textChanged = false;
	private boolean sizeChanged = false;
	
	private static final int PADDING_LEFT  = 5;
	private static final int PADDING_RIGHT = 5;
	
	private int paddingLeft;
	private int paddingRight;

	/**
	 * Constructs text sprite with given text size.
	 * @param text text
	 * @param textSize text size
	 * @param context context
	 */
	public TextSprite(String text, int textSize, Context context) {
		this(text, textSize, Color.BLACK, Color.TRANSPARENT, 0, Typeface.DEFAULT, context);
	}
	
	/**
	 * Constructs text sprite with given size, color and typeface.
	 * @param text text
	 * @param textSize text size
	 * @param color foreground color
	 * @param backColor background color
	 * @param typeface typeface
	 * @param context context
	 */
	public TextSprite(String text, int textSize, int color, int backColor, Typeface typeface, Context context) {
		this(text, textSize, color, backColor, 0, typeface, context);
	}
	
	/**
	 * Constructs text sprite with given size, color, width and typeface.
	 * @param text text
	 * @param textSize text size
	 * @param color foreground color
	 * @param backColor background color
	 * @param preferredWidth preferred width
	 * @param typeface typeface
	 * @param context context
	 */
	public TextSprite(String text, int textSize, int color, int backColor,
						int preferredWidth, Typeface typeface, Context context) {
		this.context = context;
		this.preferredWidth = preferredWidth;
		
		this.paddingLeft  = PADDING_LEFT;
		this.paddingRight = PADDING_RIGHT;
		
		setPosition(0, 0);
		setText(text);
		setTextSize(textSize);
		setColor(color);
		setBackColor(backColor);
		setTypeface(typeface);
		
		preparePaint();
		
		textChanged = false;
	}
	
	private void preparePaint() {
		textPaint.setColor(this.textColor);
		textPaint.setTypeface(this.typeFace);
		textPaint.setTextSize(this.textSize);
		textPaint.setAntiAlias(this.antiAlias);
		
		backPaint.setColor(this.backColor);
		backPaint.setStyle(Style.FILL);
		fontMetrics = textPaint.getFontMetrics();
		
		setSize(getTextWidth(), getTextHeight());
	}

	/**
	 * Draw text and update texture.
	 */
	public void createLabel() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        canvas.drawRect(0, 0, getWidth(), getHeight(), backPaint);
        canvas.drawText(text, getPreferredPaddingLeft(), Math.abs(fontMetrics.ascent), textPaint);
        
        texture = new BitmapTexture(bitmap, getWidth(), getHeight(), context);
        texture.recycleBitmap(false);
                
        updateTexture(texture);
	}

	/**
	 * Returns text width of the sprite.
	 * @return text width of the sprite
	 */
	public int getTextWidth() {
		int width =  measureTextWidthWithPadding();
		if (preferredWidth > width) return preferredWidth;
		return width;
	}
	
	protected int measureTextWidthWithPadding() {
		return measureTextWidth() + paddingLeft + paddingRight;
	}
	
	protected int measureTextWidth() {
		return (int)Math.ceil(textPaint.measureText(text));
	}
	
	private int getPreferredPaddingLeft() {
		int width =  measureTextWidthWithPadding();
		if (preferredWidth < width) return paddingLeft;
		return (int)((preferredWidth - measureTextWidth()) * 0.5f);
	}
	
	/**
	 * Returns text height of the sprite.
	 * @return text height of the sprite
	 */
	public int getTextHeight() {
		return (int)Math.ceil(Math.abs(fontMetrics.ascent) +
				Math.abs(fontMetrics.descent) + Math.abs(fontMetrics.leading));
	}
	
	/**
	 * Called when the sprite is created or recreated.
	 */
	@Override
	public void onLoadSurface(GL10 gl) {
		onLoadSurface(gl, false);
	}
	
	/**
	 * Called when the sprite is created or recreated.
	 */
	@Override
	public void onLoadSurface(GL10 gl, boolean force) {
		if (textChanged) {
			preparePaint();
		}
		createLabel();
		createBuffers();
		super.onLoadSurface(gl, force);
	}
	
	/**
	 * Called to draw the sprite.
	 * This method is responsible for drawing the sprite. 
	 */
	@Override
	public void onDraw(GL10 gl) {
		if (textChanged) {
			preparePaint();
			createLabel();
			texture.loadTexture(gl, false);

			if (sizeChanged) {
				createBuffers();
				loadVertexBuffer((GL11)gl);
				loadTextureBuffer((GL11)gl);
			}
			sizeChanged = false;
			textChanged = false;
		}
		super.onDraw(gl);
	}
	
	/**
	 * Called when this sprite is removed.
	 */
	@Override
	public void onRemove() {
		((BitmapTexture)texture).recycleBitmap();
		super.onRemove();
	}
	
	/**
	 * Called when this sprite is disposed.
	 */
	@Override
	public void onDispose() {
		super.onDispose();
	}
	
	/**
	 *  Reload label text.
	 *  If the label size needs to be changed, use reload(true).
	 *  
	 *  @deprecated  Calling reload() is not necessary because TextSprite is reloaded automatically when text attributes have been changed.
	 */
	public void reload() {
		reload(false);
	}
	
	/**
	 *  Reload label text.
	 *  if resized equals true, vertex/indices/texture buffers are re-created.
	 *  @param resized true if text size needs to be changed. 
	 */
	public void reload(boolean resized) {
		this.sizeChanged = resized;
		this.textChanged = true;
	}
	
	/**
	 * Returns text of the sprite
	 * @return text of the sprite
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Set text of the sprite
	 * @param text text
	 */
	public void setText(String text) {
		this.text = text;
		this.textChanged = true;
	}
	
	/**
	 * Set text size 
	 * @param size text size
	 */
	public void setTextSize(int size) {
		this.textSize = size;
		this.textChanged = true;
	}
		
	/**
	 * Set anti-alias
	 * @param enable
	 */
	public void setAntiAlias(boolean enable) {
		this.antiAlias = enable;
		this.textChanged = true;
	}

	/**
	 * Set foreground color
	 * @param textColor
	 */
	public void setColor(int textColor) {
		this.textColor = textColor;
		this.textChanged = true;
	}
	
	/**
	 * Set background color
	 * @param backColor
	 */
	public void setBackColor(int backColor) {
		this.backColor = backColor;
		this.textChanged = true;
	}

	/**
	 * Set typeface
	 * @param typeface
	 */
	public void setTypeface(Typeface typeface) {
		this.typeFace = typeface;
		this.textChanged = true;
	}
	
	public void setPaddingLeft(int left) {
		this.paddingLeft = left;
		reload(true);
	}
	
	public int getPaddingLeft() {
		return paddingLeft;
	}
	
	public void setPaddingRight(int right) {
		this.paddingRight = right;
		reload(true);
	}
	
	public int getPaddingRight() {
		return this.paddingRight;
	}
}
