/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

package com.sidereal.lumm.components.renderer.ninepatch;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.sidereal.lumm.components.renderer.DrawerBuilder;

/**
 * Builder for {@link NinepatchDrawer}. Can change size, offset and color before
 * creating an instance of {@link NinepatchDrawer}.
 * 
 * @author Claudiu Bele
 */
public class NinepatchBuilder extends DrawerBuilder<NinepatchDrawer> {

	// region fields

	private int paddingLeft, paddingRight, paddingTop, paddingBottom;

	private float sizeX, sizeY;

	private float offsetX, offsetY;

	private float scaleX, scaleY;

	private String filepath;

	private Color color;

	// endregion fields

	// region constructors
	/**
	 * Creates a NinePatchBuilder, which will be used for building a
	 * {@link NinepatchDrawer}.
	 * 
	 * @param filePath
	 *            path to the asset in memory.
	 * @param paddingLeft
	 *            the padding of the area to the left that does not scale
	 * @param paddingRight
	 *            the padding of the area to the left that does not scale
	 * @param paddingTop
	 *            the padding of the area to the left that does not scale
	 * @param paddingBottom
	 *            the padding of the area to the left that does not scale
	 */
	public NinepatchBuilder(String filePath, int paddingLeft, int paddingRight, int paddingTop, int paddingBottom) {

		this.filepath = filePath;
		this.paddingLeft = paddingLeft;
		this.paddingRight = paddingRight;
		this.paddingTop = paddingTop;
		this.paddingBottom = paddingBottom;
	}

	// endregion

	// region methods

	@Override
	protected NinepatchDrawer build(String name) {

		NinepatchDrawer drawer = new NinepatchDrawer(renderer, name, filepath, paddingLeft, paddingRight, paddingTop,
				paddingBottom);
		drawer.setSize(sizeX, sizeY);
		drawer.setOffsetPosition(offsetX, offsetY);
		if (color != null)
			drawer.setColor(color);
		drawer.setScale(scaleX, scaleY);
		return drawer;
	}

	/**
	 * Sets the padding on all 4 sides. Will not be handled if the image is null
	 * or the padding on all sides is equal to the parameters for individual
	 * sides
	 * 
	 * @param left
	 *            the padding on the left side
	 * @param right
	 *            the padding on the right side
	 * @param top
	 *            the padding on the top side
	 * @param bottom
	 *            the padding on the bottom side
	 */
	public NinepatchBuilder setPadding(int left, int right, int top, int bottom) {

		this.paddingLeft = left;
		this.paddingRight = right;
		this.paddingTop = top;
		this.paddingBottom = bottom;
		return this;
	}

	/**
	 * Sets the padding of the image after it has been built. If not set, the
	 * padding will be set to the texture's width and height.
	 * <p>
	 * Areas set in the constructor will not be scaled.
	 * 
	 * @param width
	 *            width of the ninepatch
	 * @param height
	 *            width of the height
	 * @return
	 */
	public NinepatchBuilder setSize(float width, float height) {

		this.sizeX = width;
		this.sizeY = height;
		return this;
	}

	/**
	 * Sets the offset of the texture, in relation to {@link #MISSING()}
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public NinepatchBuilder setOffsetPosition(float x, float y) {

		this.offsetX = x;
		this.offsetY = y;
		return this;
	}

	/**
	 * Sets the color used for tinting the nine patch when building the
	 * {@link NinepatchDrawer}.
	 * <p>
	 * The value for the alpha channel can be used for setting the transparency.
	 * 
	 * @param color
	 *            The color of the ninepatch
	 * @return
	 */
	public NinepatchBuilder setColor(Color color) {

		this.color = color;
		return this;
	}

	/**
	 * Sets the scale of the nine patch. This method internally handles using
	 * the {@link NinePatch#scale(float, float)} method with parameters that
	 * match the target scale, regardless of previous scale sets. The scaling is
	 * in accordance to the default scale values, which are 1,1.
	 * <p>
	 * Example: To scale the width of an element to be 3 times the default value
	 * but you want the height to be different, use <code>setScale(3,1);</code>
	 * 
	 * @param scaleX
	 * @param scaleY
	 * @return NinepatchBuilder, for the purpose of chaining
	 */
	public NinepatchBuilder setScale(float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		return this;

	}

	// endregion methods

}
