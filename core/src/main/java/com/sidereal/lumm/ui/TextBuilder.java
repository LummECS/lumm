/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.sidereal.lumm.ui;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummObject;
import com.sidereal.lumm.architecture.LummScene;
import com.sidereal.lumm.architecture.concrete.ConcreteLummObject;
import com.sidereal.lumm.architecture.listeners.OnDisposeListener;

/**
 * Abstract object used for drawing text on the screen. Supports customisation
 * for different font, font colors, scale, transparency;
 * 
 * @author Claudiu Bele
 */
public class TextBuilder extends ConcreteLummObject {

	// region static

	/**
	 * Gets the Blocks4 font made by Claudiu Bele, which can be already found in
	 * the framework and doesn't require creating additional files.
	 * 
	 * @return
	 */
	public static BitmapFont getFont(String fontDataPath) {

		return new BitmapFont(Lumm.assets.get(fontDataPath, BitmapFont.class,ClasspathFileHandleResolver.class).getData().getFontFile());
	}

	// endregion static

	// region fields

	public enum Allign {
		Left, Center, Right
	};

	public enum Anchor {
		Top, Middle, Bottom
	}

	public static class Paragraph {

		public String text;

		public Color color;

		public Paragraph(String text, Color color) {

			this.text = text;
			this.color = color;
		}
	}

	public Color color;

	public String text;

	private BitmapFont font;

	private GlyphLayout glyphLayout;

	public float alpha;

	public float scale;

	public Vector2 bounds;

	private boolean wrapText;

	private float lineSpacing;

	public float windowSize;

	private Allign allignment;

	private Anchor anchor;

	private ArrayList<Paragraph> rawParagraphs;

	private ArrayList<Paragraph> paraGraphsToWrite;

	// endregion fields

	// region Constructors
	public TextBuilder(LummScene scene, boolean wrap) {

		this(scene, wrap, Lumm.assets.frameworkAssetsFolder + "Blocks.fnt");
	}

	public TextBuilder(LummScene scene, boolean wrap, String fontDataPath) {

		super(scene);

		font = getFont(fontDataPath);
		this.glyphLayout = new GlyphLayout();

		this.color = Color.WHITE;
		this.bounds = new Vector2();

		rawParagraphs = new ArrayList<Paragraph>();
		paraGraphsToWrite = new ArrayList<Paragraph>();

		windowSize = Gdx.graphics.getDisplayMode().width;
		setAlpha(-1);
		setScale(1f);
		position.setRelative(0, 0, 5);

		lineSpacing = 5f;
		windowSize = 600;
		this.wrapText = wrap;
		allignment = Allign.Center;
		anchor = Anchor.Middle;

		onDisposeListener = new OnDisposeListener<LummObject>() {

			@Override
			public void onDispose(LummObject caller) {

				paraGraphsToWrite = null;
				rawParagraphs = null;
				bounds = null;
				color = null;

				font = null;
			}
		};
		
		generateBounds();
	}

	// endregion

	// region methods

	public final ArrayList<Paragraph> wrapText(String data, Color color) {

		ArrayList<Paragraph> resultingLines = new ArrayList<Paragraph>();
		if (!wrapText) {
			resultingLines.add(new Paragraph(data, color));
			return resultingLines;
		}

		// splitting the string into lines
		String[] lines = data.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String[] words = lines[i].split(" ");
			String currText = "";
			for (int j = 0; j < words.length; j++) {
				// curr text + new word exceeds line limit, cut it

				glyphLayout.setText(font, currText);
				if (glyphLayout.width > windowSize) {
					// trim line
					currText = currText.trim();
					if (currText == null)
						currText = "";

					// this is a one word line,
					if (currText.equals("")) {
						resultingLines.add(new Paragraph(words[j], color));
					}
					// not a one word line
					else {
						resultingLines.add(new Paragraph(currText, color));
						currText = "";
						j--;
					}

				} else {

					// append curr word to the string
					currText += words[j] + " ";
				}
			}

			currText = currText.trim();
			resultingLines.add(new Paragraph(currText, color));

		}
		glyphLayout.setText(font, "X");
		bounds.set(windowSize, glyphLayout.height * resultingLines.size() + (resultingLines.size()) * lineSpacing);

		return resultingLines;
	}

	// region adding and setting text
	public final void addText(String data, Color lineColor) {

		rawParagraphs.add(new Paragraph(data, lineColor));
		paraGraphsToWrite.addAll(wrapText(data, lineColor));
	
		if (text == "")
			text = data;
		else
			text += "\n " + data;
		
		generateBounds();

	}

	public final void addText(ArrayList<String> data, ArrayList<Color> paragraphColors) {

		for (int i = 0; i < data.size(); i++) {
			if (paragraphColors == null || paragraphColors.size() != data.size() || paragraphColors.get(i) == null)

				addText(data.get(i), Color.WHITE);
			else
				addText(data.get(i), paragraphColors.get(i));
		}
	}

	public void setText(String data, Color targetColor) {

		if (text != null && text.equals(data))
			return;
		text = data;

		paraGraphsToWrite.clear();
		if (wrapText) {
			paraGraphsToWrite = wrapText(data, targetColor);
		} else {
			paraGraphsToWrite.add(new Paragraph(data, targetColor));
			
		}
		rawParagraphs.clear();
		rawParagraphs.add(new Paragraph(data, targetColor));
		setColor(targetColor);
		generateBounds();

	}

	// endregion

	// region getters and setters
	public void setWindowSize(float size) {

		this.windowSize = size;
		paraGraphsToWrite.clear();
		for (int i = 0; i < rawParagraphs.size(); i++) {
			paraGraphsToWrite.addAll(wrapText(rawParagraphs.get(i).text, rawParagraphs.get(i).color));
		}
		generateBounds();

	}

	public final void clearText() {

		paraGraphsToWrite.clear();
		rawParagraphs.clear();
		generateBounds();

	}

	public final void setAllign(Allign allignment) {

		this.allignment = allignment;
	}

	public final void setAnchor(Anchor anchor) {

		this.anchor = anchor;
	}

	public final void setFont(BitmapFont font) {

		this.font = font;
		generateBounds();

	}

	public final void setFont(String fontPath) {
		font = getFont(fontPath);
	}

	public final BitmapFont getFont() {
		return font;
	}

	public final void setAlpha(float alpha) {

		if (font.getColor().a == alpha && this.alpha == alpha)
			return;

		if (alpha != -1) {
			alpha = Math.max(0, Math.min(1, alpha));
			color.a = alpha;
			font.setColor(color);
		}
		this.alpha = alpha;
	}

	public final void setScale(float scale) {

		this.scale = Math.max(0.1f, Math.min(10, scale));
		font.getData().setScale(this.scale);
		generateBounds();

	}

	public final void setColor(Color color) {

		if (alpha != -1)
			color.a = alpha;
		this.color = color;
		font.setColor(color);
	}

	// endregion
	@Override
	public void onRender() {

		float newX, newY;

		float currLineOffset = 0;
		if(paraGraphsToWrite == null)
			return;
		for (int i = 0; i < paraGraphsToWrite.size(); i++) {

			glyphLayout.setText(font, paraGraphsToWrite.get(i).text);
			if (allignment.equals(Allign.Center)) {
				newX = position.getX() - glyphLayout.width / 2;
			} else if (allignment.equals(Allign.Left)) {
				newX = position.getX();
			} else {
				newX = position.getX() - glyphLayout.width;
			}

			if (anchor.equals(Anchor.Top)) {
				newY = position.getY();
				newY -= currLineOffset;
				currLineOffset += glyphLayout.height;
				currLineOffset += lineSpacing;
			}

			else if (anchor.equals(Anchor.Middle)) {
				newY = position.getY() + glyphLayout.height / 2;

				if (paraGraphsToWrite.size() != 0) {
					glyphLayout.setText(font, "X");
					newY += (((paraGraphsToWrite.size() - 1) / 2f) - i) * (glyphLayout.height + lineSpacing);
				}
			} else {
				newY = position.getY() + glyphLayout.height / 2;
				newY += currLineOffset;
				currLineOffset += glyphLayout.height;
				currLineOffset += lineSpacing;
			}

			setColor(paraGraphsToWrite.get(i).color);
			if (alpha != -1)
				color.a = alpha;
			glyphLayout.setText(font, paraGraphsToWrite.get(i).text);
			font.draw(getSceneLayer().spriteBatch, glyphLayout, (int) newX, (int) newY);

		}
		generateBounds();

	}

	public void generateBounds() {
		
		glyphLayout.setText(font, "X");
		if(paraGraphsToWrite == null)
			paraGraphsToWrite = new ArrayList<Paragraph>();
		bounds.set(windowSize,
				glyphLayout.height * paraGraphsToWrite.size() + (paraGraphsToWrite.size()) * lineSpacing);
	}

	public void setLineSpacing(float lineSpacing) {
		this.lineSpacing = lineSpacing;
		generateBounds();
	}

	
	// endregion methods
}
