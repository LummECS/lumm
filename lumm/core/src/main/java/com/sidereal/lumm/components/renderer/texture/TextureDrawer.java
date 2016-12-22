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

package com.sidereal.lumm.components.renderer.texture;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.components.renderer.Drawer;
import com.sidereal.lumm.components.renderer.DrawerBuilder;
import com.sidereal.lumm.components.renderer.Renderer;

/**
 * Draws a texture at the specified location. Uses {@link TextureBuilder} for
 * building. For more complex single-image operations, use {@link SpriteDrawer}.
 *
 *
 * @author Claudiu Bele.
 */
public class TextureDrawer extends Drawer {

    // region fields

    /** Filepath to the current texture found in {@link #texture}. */
    private String filePath;

    /** {@link Texture} to draw. Set in */
    private Texture texture;

    /**
     * Rectangle designating the area in which the texture is drawn. Set in
     * {@link #updateBoundingRectangle()} which is called in
     * {@link #setOffsetPosition(Vector2)} or {@link #setSize(Vector2)}.
     */
    private Rectangle boundingRectangle;

    /**
     * The offset position. Set to (0,0) by default in
     * {@link TextureBuilder#TextureBuilder(String)}. Signifies the bottom-left
     * corner of the image.
     */
    private Vector2 offsetPosition;

    /**
     * Size of the texture. Set to the texture's width by default in
     * {@link TextureDrawer#TextureDrawer(Renderer, String, String)}.
     */
    private Vector2 size;

    // endregion fields

    // region constructors

    /**
     * Creates a new instance of {@link TextureDrawer}, with the only Mandatory
     * parameter being the texture file path.
     *
     * @param renderer
     *            passed from {@link Renderer#addDrawer(String, DrawerBuilder)}.
     * @param name
     *            passed from {@link Renderer#addDrawer(String, DrawerBuilder)}
     * @param filePath
     *            Path to the texture.
     */
    public TextureDrawer(Renderer renderer, String name, String filePath) {

        super(renderer, name, false);
        offsetPosition = new Vector2();
        size = new Vector2();

        boundingRectangle = new Rectangle();
        setTexture(filePath);

    }

    // endregion constructors

    // region methods

    private void updateBoundingRectangle() {

        if (offsetPosition == null)
            return;
        if (size == null)
            return;

        boundingRectangle.set(renderer.object.position.getX() + offsetPosition.x,
                renderer.object.position.getY() + offsetPosition.y, size.x, size.y);
    }

    public void setOffsetPosition(float offsetX, float offsetY) {

        this.offsetPosition.set(offsetX, offsetY);
    }

    public void setSize(float sizeX, float sizeY) {

        this.size.set(sizeX, sizeY);
    }

    public void setTexture(String filepath) {

        if (filepath == null)
            throw new NullPointerException("Passed null String parameter to TextureDrawer.setTexture(String)");

        if (filepath.equals(this.filePath))
            return;

        // asset is not loaded
        if (!Lumm.assets.contains(filepath)) {
            Lumm.assets.load(filepath, Texture.class);
            Lumm.assets.finishLoading();
            texture = Lumm.assets.get(filepath, Texture.class);
        }
        // asset is loaded
        else {
            Texture targetTexture = Lumm.assets.get(filepath, Texture.class);
            if (!targetTexture.equals(texture)) {
                texture = targetTexture;
            }
        }

        this.filePath = filepath;
    }

    public Texture getTexture() {
        return texture;
    }

    @Override
    protected void dispose() {

    }

    @Override
    protected void draw(float delta) {

        float targetX = renderer.object.position.getX() + offsetPosition.x;
        float targetY = renderer.object.position.getY() + offsetPosition.y;
        renderer.object.getSceneLayer().spriteBatch.draw(texture, targetX, targetY, size.x, size.y);

    }

    @Override
    protected boolean isOutOfBounds() {

        updateBoundingRectangle();

        return renderer.object.getSceneLayer().renderingArea.overlaps(boundingRectangle) == false;
    }

    // endregion
}
