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

package dk.sidereal.lumm.components.renderer.sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummObject;
import dk.sidereal.lumm.components.renderer.Drawer;
import dk.sidereal.lumm.components.renderer.Renderer;

/**
 * Renders textures with different settings. Uses {@link SpriteBuilder} as
 * builder.
 *
 * @author Claudiu Bele
 */
public class SpriteDrawer extends Drawer {

    // region fields

    /** File path to the texture */
    private String filepath;

    /** Angle at which to rotate. Will rotate around {@link #origin}. */
    private float degrees;

    /** Texture to manipulate and draw. */
    private Sprite sprite;

    /** Color for tinting the texture */
    private Color tintColor;

    /**
     * Texture transparency. Object transparency and {@link #tintColor#a } also
     * taken into account.
     */
    private float transparency;

    /** image size */
    private Vector2 size;

    /** offset position from {@link LummObject}. */
    private Vector2 positionOffset;

    /** the origin of the texture, used for rotation */
    private Vector2 origin;

    // endregion fields

    // region constructors

    public SpriteDrawer(Renderer renderer, String name, String filepath) {

        super(renderer, name, false);
        this.filepath = filepath;
        sprite = new Sprite(Lumm.assets.get(filepath, Texture.class));
        tintColor = Color.WHITE.cpy();
        transparency = 1;
        size = new Vector2(sprite.getWidth(), sprite.getHeight());
        positionOffset = new Vector2();
        origin = new Vector2(size.x / 2f, size.y / 2f);
        setSprite(filepath);
    }

    // endregion constructors

    // region methods

    @Override
    protected void dispose() {

    }

    @Override
    protected void draw(float delta) {

        float targetX = renderer.object.position.getX() + positionOffset.x;
        if (sprite.getX() != targetX)
            sprite.setX(targetX);

        float targetY = renderer.object.position.getY() + positionOffset.y;
        if (sprite.getY() != targetY)
            sprite.setY(targetY);

        sprite.draw(renderer.object.getSceneLayer().spriteBatch);
    }

    // region setters and getters

    public SpriteDrawer setSprite(String filepath) {

        if (filepath == null)
            throw new NullPointerException("Passed null String parameter to SpriteDrawer.setTexture(String)");

        // asset is not loaded
        if (!Lumm.assets.contains(filepath)) {
            Lumm.assets.load(filepath, Texture.class);
            Lumm.assets.finishLoading();
            sprite = new Sprite(Lumm.assets.get(filepath, Texture.class));
        }
        // asset is loaded
        else {
            Texture targetTexture = Lumm.assets.get(filepath, Texture.class);
            if (!targetTexture.equals(sprite.getTexture())) {
                sprite = new Sprite(targetTexture);
            } else
                return this;
        }

        this.filepath = filepath;
        setOrigin(origin.x, origin.y);
        setRotation(degrees, true);
        setColor(tintColor);
        setTransparency(transparency, true);
        setSize(size.x, size.y);
        return this;
    }

    public SpriteDrawer setColor(Color c) {

        tintColor = c;
        updateColor();
        return this;

    }

    public SpriteDrawer setTransparency(float value, boolean forced) {

        if (transparency == value && !forced)
            return this;
        transparency = value;
        updateColor();
        return this;
    }

    public SpriteDrawer setSize(float x, float y) {
        if (this.size.x == x && this.size.y == y)
            return this;
        this.size.set(x, y);
        sprite.setSize(this.size.x, this.size.y);
        return this;
    }

    public SpriteDrawer setSizeAndCenter(float x, float y) {

        setSize(x, y).setOffsetPosition(-x / 2f, -y / 2f);
        return this;
    }

    public SpriteDrawer setOffsetPosition(float x, float y) {

        if (this.positionOffset.x == x && this.positionOffset.y == y)
            return this;

        this.positionOffset.set(x, y);
        return this;
    }

    public SpriteDrawer setOrigin(float x, float y) {

        if (this.origin.x == x && this.origin.y == y)
            return this;

        this.origin.set(x, y);
        sprite.setOrigin(origin.x, origin.y);
        return this;
    }

    public SpriteDrawer setRotation(float degrees, boolean forced) {

        if (this.degrees == degrees && !forced)
            return this;
        this.degrees = degrees;
        sprite.setRotation(degrees);
        return this;
    }

    private final void updateColor() {

        Color spriteColor = sprite.getColor();
        if (spriteColor.r != tintColor.r || spriteColor.g != tintColor.g || spriteColor.b != tintColor.b
                || spriteColor.a != tintColor.a * transparency) {
            sprite.setColor(tintColor.r, tintColor.g, tintColor.b, tintColor.a * transparency);
        }
    }

    public String getFilepath() {

        return filepath;
    }

    public float getDegrees() {

        return degrees;
    }

    public Sprite getSprite() {

        return sprite;
    }

    public Color getColor() {

        return tintColor;
    }

    public float getTransparency() {

        return transparency;
    }

    public Vector2 getSize() {

        return size;
    }

    public Vector2 getOffset() {

        return positionOffset;
    }

    public Vector2 getOrigin() {

        return origin;
    }

    @Override
    protected boolean isOutOfBounds() {
        return renderer.object.getSceneLayer().renderingArea.overlaps(sprite.getBoundingRectangle()) == false;
    }

    // endregion

    // endregion methods
}
