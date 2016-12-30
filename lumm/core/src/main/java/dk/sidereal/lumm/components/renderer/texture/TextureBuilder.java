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

package dk.sidereal.lumm.components.renderer.texture;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import dk.sidereal.lumm.components.renderer.DrawerBuilder;

/**
 * Builder for {@link TextureDrawer}. Required parameter is a string for the
 * filepath used for retrieving a {@link Texture}.
 *
 * @author Claudiu Bele
 */
public class TextureBuilder extends DrawerBuilder<TextureDrawer> {

    // region fields

    /** Path to the texture. Set in the constructor */
    private String filePath;

    /**
     * Offset position from the {@link #MISSING()}. Is set by Default to 0,0 the
     * bottom-left corner of the texture being rendered at that position.
     */
    private float offsetX, offsetY;

    /**
     * Size of the texture to draw. Is set by default to the the texture's width
     * and height if no value is passed
     */
    private float sizeX, sizeY;

    // endregion fields

    // region constructors

    public TextureBuilder(String filePath) {

        if (filePath == null)
            throw new NullPointerException("Passed null String parameter to TextBuilder.TextBuilder(String)");
        this.filePath = filePath;
    }

    // endregion constructors

    // region methods

    @Override
    protected TextureDrawer build(String name) {

        TextureDrawer drawer = new TextureDrawer(renderer, name, filePath);
        drawer.setOffsetPosition(offsetX, offsetY);
        if (sizeX != 0 && sizeY != 0)
            drawer.setSize(sizeX, sizeY);
        return drawer;
    }

    public TextureBuilder setOffsetPosition(float offsetX, float offsetY) {

        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return this;
    }

    /**
     * Sets the size. It can be null, as if it is, it will be set to the
     * texture's size in {@link TextureDrawer#setSize(Vector2)} automatically.
     *
     * @param sizeX
     *            the width
     * @param sizeY
     *            the height
     * @return
     */
    public TextureBuilder setSize(float sizeX, float sizeY) {

        this.sizeX = sizeX;
        this.sizeY = sizeY;
        return this;
    }

    /**
     * Sets the size. It can be null, as if it is, it will be set to the
     * texture's size in {@link TextureDrawer#setSize(Vector2)} automatically.
     *
     * @param size
     * @return
     */
    public TextureBuilder setSize(Vector2 size) {

        if (size == null)
            return this;
        return setSize(size.x, size.y);
    }

    public TextureBuilder setOffsetPosition(Vector2 position) {

        if (position == null)
            throw new NullPointerException("Passed null Vector2 parameter to TextBuilder.setOffsetPosition(Vector2)");
        return setOffsetPosition(position.x, position.y);
    }

    // endregion methods

}
