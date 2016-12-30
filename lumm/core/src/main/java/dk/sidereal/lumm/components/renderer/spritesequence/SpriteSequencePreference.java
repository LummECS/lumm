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

package dk.sidereal.lumm.components.renderer.spritesequence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Settings related to a list of sprites to draw in a
 * {@link SpriteSequenceDrawer} by calling
 * {@link SpriteSequenceDrawer#addPreferences(String, SpriteOrderSettings)}
 *
 * @author Claudiu Bele
 */
public class SpriteSequencePreference {

    // region fields

    private float timePerAnimation;

    private Vector2 size;

    private Vector2 positionOffset;

    private boolean placeAtStart;

    private boolean placeAtEnd;

    private boolean flipX;

    private boolean flipY;

    private Color tintColor;

    private float transparency;

    // endregion fields

    // region constructors

    public SpriteSequencePreference() {

        this.timePerAnimation = 1;
        this.size = new Vector2(100, 100);
        this.positionOffset = new Vector2(100, 100);
        this.placeAtEnd = false;
        this.placeAtStart = false;
        this.transparency = 1;
        this.tintColor = Color.WHITE.cpy();
    }

    // endregion constructors

    // region methods

    // region builders
    public SpriteSequencePreference setTimePerAnimation(float value) {

        // we don't want it to be 0 so as to not divide by 0 when calculating
        // new sprite index value
        this.timePerAnimation = Math.max(0.01f, value);
        return this;
    }

    /**
     * Sets the size to the one given by the parameters as well as the position
     * offset to minus half the width and minus half the height, to center the
     * object around {@link #MISSING()}.
     *
     * @param width
     * @param height
     * @return
     */
    public SpriteSequencePreference setSizeAndCenter(float width, float height) {

        this.size.set(width, height);
        this.positionOffset.set(-width / 2f, -height / 2f);
        return this;
    }

    public SpriteSequencePreference setSize(float width, float height) {

        this.size.set(width, height);
        return this;
    }

    public SpriteSequencePreference setPositionOffset(float x, float y) {

        this.positionOffset.set(x, y);
        return this;
    }

    public SpriteSequencePreference setFlipX(boolean value) {

        this.flipX = value;
        return this;
    }

    public SpriteSequencePreference setFlipY(boolean value) {

        this.flipY = value;
        return this;
    }

    public SpriteSequencePreference setPlaceAtEnd(boolean value) {

        this.placeAtEnd = value;
        return this;
    }

    public SpriteSequencePreference setPlaceAtStart(boolean value) {

        this.placeAtStart = value;
        return this;
    }

    public SpriteSequencePreference setTintColor(Color tintColor) {

        this.tintColor = tintColor;
        return this;
    }

    public SpriteSequencePreference setTransparency(float transparency) {

        this.transparency = transparency;
        return this;
    }

    // endregion

    // region getters
    public float getTimePerAnimation() {

        return this.timePerAnimation;
    }

    public Vector2 getSize() {

        return size;
    }

    public Vector2 getPositionOffset() {

        return positionOffset;
    }

    public boolean isPlaceAtStart() {

        return placeAtStart;
    }

    public boolean isPlaceAtEnd() {

        return placeAtEnd;
    }

    public boolean isFlipX() {

        return flipX;
    }

    public boolean isFlipY() {

        return flipY;
    }

    public Color getTintColor() {

        return tintColor;
    }

    public float getTransparency() {

        return transparency;
    }
    // endregion

    // endregion methods

}
