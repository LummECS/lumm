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

package com.sidereal.lumm.components.renderer.sprite;

import com.badlogic.gdx.graphics.Color;
import com.sidereal.lumm.components.renderer.DrawerBuilder;

/**
 * Builder for {@link SpriteDrawer}
 *
 * @author Claudiu Bele
 */
public class SpriteBuilder extends DrawerBuilder<SpriteDrawer> {

    // region fields

    private String filepath;

    private float sizeX, sizeY;

    private float offsetX, offsetY;

    private float originX, originY;

    private float rotationDegrees;

    private Color tintColor;

    private float transparency;

    // endregion fields

    // region constructors

    public SpriteBuilder(String filepath) {

        super();
        this.filepath = filepath;
        this.transparency = 1;
    }

    // endregion constructors

    // region methods

    @Override
    protected SpriteDrawer build(String name) {

        SpriteDrawer drawer = new SpriteDrawer(renderer, name, filepath);
        if (sizeX != 0 && sizeY != 0)
            drawer.setSize(sizeX, sizeY);
        drawer.setOffsetPosition(offsetX, offsetY);
        drawer.setOrigin(originX, originY);
        if (tintColor != null)
            drawer.setColor(tintColor);
        drawer.setRotation(rotationDegrees, false);
        drawer.setTransparency(transparency, false);
        return drawer;

    }

    // region setters and getters
    public SpriteBuilder setSize(float sizeX, float sizeY) {

        this.sizeX = sizeX;
        this.sizeY = sizeY;
        return this;
    }

    public SpriteBuilder setSizeAndCenter(float sizeX, float sizeY) {

        return setSize(sizeX, sizeY).setOffsetPosition(-sizeX / 2f, -sizeY / 2f);
    }

    public SpriteBuilder setOffsetPosition(float offsetX, float offsetY) {

        this.offsetX = offsetX;
        this.offsetY = offsetY;
        return this;
    }

    public SpriteBuilder setOrigin(float originX, float originY) {

        this.originX = originX;
        this.offsetY = originY;
        return this;
    }

    public SpriteBuilder setRotation(float rotationDegrees) {

        this.rotationDegrees = rotationDegrees;
        return this;
    }

    public SpriteBuilder setColor(Color tintColor) {

        this.tintColor = tintColor;
        return this;
    }

    public SpriteBuilder setTransparency(float transparency) {

        this.transparency = transparency;
        return this;
    }

    // endregion

    // endregion methods
}
