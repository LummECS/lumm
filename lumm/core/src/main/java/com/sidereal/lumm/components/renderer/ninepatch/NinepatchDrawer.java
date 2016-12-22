/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.sidereal.lumm.components.renderer.ninepatch;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.core.Assets;
import com.sidereal.lumm.components.renderer.Drawer;
import com.sidereal.lumm.components.renderer.Renderer;

/**
 * {@link Drawer} extension that can draw ninepatch images. Images do not have
 * to be processed ( as in have meta data), the size will be set manually in the
 * parameters
 *
 * @author Claudiu Bele
 */
public class NinepatchDrawer extends Drawer {

    // region fields

    NinePatch image;

    private Vector2 positionOffset;

    private Vector2 size;

    private Vector2 scale;

    private int paddingLeft;

    private int paddingRight;

    private int paddingTop;

    private int paddingBottom;

    private Rectangle boundingRectangle;

    private Color tintColor;

    // endregion fields

    // region constructors

    public NinepatchDrawer(Renderer renderer, String name, String filepath, int left, int right, int top, int bottom) {

        super(renderer, name, false);

        paddingLeft = left;
        paddingRight = right;
        paddingTop = top;
        paddingBottom = bottom;
        setTexture(filepath);

        if (!Lumm.assets.contains(filepath)) {
            Lumm.assets.load(filepath, Texture.class);
            Lumm.assets.finishLoading();
        }

        size = new Vector2(image.getTexture().getWidth(), image.getTexture().getHeight());
        positionOffset = new Vector2();
        scale = new Vector2(1, 1);
        boundingRectangle = new Rectangle();

    }

    // endregion constructors

    // region methods

    @Override
    protected void dispose() {

    }

    @Override
    protected void draw(float delta) {

        image.draw(renderer.object.getSceneLayer().spriteBatch, renderer.object.position.getX() + positionOffset.x,
                renderer.object.position.getY() + positionOffset.y, size.x, size.y);

    }

    @Override
    protected boolean isOutOfBounds() {

        boundingRectangle.set(renderer.object.position.getX() + positionOffset.x,
                renderer.object.position.getY() + positionOffset.y, size.x, size.y);

        return !renderer.object.getSceneLayer().renderingArea.overlaps(boundingRectangle);
    }

    /**
     * Sets the texture to use for the ninepatch. The {@link #paddingLeft},
     * {@link #paddingRight}, {@link #sizeTop} and {@link #paddingBottom} are
     * used when assigning {@link #image} using the following NinePatch
     * constructor: {@link NinePatch#NinePatch(Texture, int, int, int, int)}
     * <p>
     * If the texture was not loaded, the texture will be loaded by the
     * {@link Assets} ,being forced to finish loading the texture before
     * assigning it to {@link #image}
     *
     * @param filepath
     *            filepath to the image
     */
    public void setTexture(String filepath) {
        if (filepath == null)
            throw new NullPointerException("Passed null String parameter to NinePatchDrawer.setTexture(String)");

        // asset is not loaded
        if (!Lumm.assets.contains(filepath)) {
            Lumm.assets.load(filepath, Texture.class);
            Lumm.assets.finishLoading();
            image = new NinePatch(Lumm.assets.get(filepath, Texture.class), paddingLeft, paddingRight, paddingTop,
                    paddingBottom);
        }
        // asset is loaded
        else {
            Texture targetTexture = Lumm.assets.get(filepath, Texture.class);
            if (image == null || !targetTexture.equals(image.getTexture())) {
                image = new NinePatch(targetTexture, paddingLeft, paddingRight, paddingTop, paddingBottom);
            }
        }

    }

    /**
     * Sets the size the ninepatch to draw. If not assigned, will be set to
     * {@link Texture#getWidth()} and {@link Texture#getHeight()}.
     * <p>
     * Can be used at runtime, and will be used when building the object, after
     * using {@link NinepatchBuilder#setSize(float, float)} in the
     * {@link NinepatchBuilder}.
     *
     * @param width
     *            new width of the texture
     * @param height
     *            new height of the texture
     */
    public void setSize(float width, float height) {

        if (size == null)
            size = new Vector2(width, height);
        else
            size.set(width, height);
    }

    /**
     * Offset at which to draw the texture, relative to {@link #MISSING()}.
     * <p>
     * Can be used at runtime, and will be used when building the object, after
     * using {@link NinepatchBuilder#setOffsetPosition(float, float)} in the
     * {@link NinepatchBuilder}.
     *
     * @param offsetX
     *            number of units to push the object on the X axis, can be
     *            negative.
     * @param offsetY
     *            number of units to push the object on the Y axis, can be
     *            negative.
     */
    public void setOffsetPosition(float offsetX, float offsetY) {

        if (positionOffset == null)
            positionOffset = new Vector2(offsetX, offsetY);
        else
            positionOffset.set(offsetX, offsetY);
    }

    /**
     * Sets the color of the ninepatch, using {@link NinePatch#setColor(Color)}.
     * <p>
     * Can be used at runtime, and will be used when building the object, after
     * using {@link NinepatchBuilder#setColor(Color)} in the
     * {@link NinepatchBuilder}.
     *
     * @param color
     */
    public void setColor(Color color) {

        tintColor = color;
        image.setColor(tintColor);
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
     */
    public void setScale(float scaleX, float scaleY) {
        // the real scaling to be used in the system
        scale.x = scaleX / scale.x;
        scale.y = scaleY / scale.y;

        // apply the scaling;
        image.scale(scale.x, scale.y);

    }

    // endregion methods

}
