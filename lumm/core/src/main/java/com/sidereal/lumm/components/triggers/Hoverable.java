/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
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

package com.sidereal.lumm.components.triggers;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.sidereal.lumm.architecture.AbstractEvent;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummObject;
import com.sidereal.lumm.architecture.concrete.ConcreteLummComponent;

/**
 * Behavior that handles hovering over an area. Can handle being inside as well
 * as outside of the designated area.
 *
 * @author Claudiu Bele
 */
public class Hoverable extends ConcreteLummComponent {

    // region fields

    private Rectangle area;

    private AbstractEvent insideEvent;

    private AbstractEvent outsideEvent;

    private float offsetX, offsetY;

    private Sprite debugSprite;

    private static Sprite debugSpriteSource;

    // endregion fields

    // region constructors

    public Hoverable(LummObject obj) {

        super(obj);

        if (Lumm.debug.isEnabled())
            this.debugSprite = new Sprite(debugSpriteSource);
        setDebugToggleKeys(Keys.SHIFT_LEFT, Keys.X);
    }

    @Override
    protected void initialiseClass() {

        if (!Lumm.debug.isEnabled())
            return;

        debugSpriteSource = new Sprite(Lumm.assets.get(Lumm.assets.frameworkAssetsFolder + "White.png", Texture.class));
        debugSpriteSource.setColor(new Color(0.7f, 0, 1, 0.5f));
    }

    // endregion constructors

    // region methods

    @Override
    public void onDebug() {

        debugSprite.setBounds(area.x, area.y, area.width, area.height);
        debugSprite.draw(object.getSceneLayer().spriteBatch);
    }

    @Override
    public void onUpdate() {

        // adapt the area based on the position of the object
        // so the position of the object is in the middle of the area
        area.x = object.position.getX() + offsetX;
        area.y = object.position.getY() + offsetY;

        // mouse click is inside the area of the object
        if (area.contains(object.getSceneLayer().mousePosition)) {
            if (insideEvent != null)
                insideEvent.run();
        } else if (outsideEvent != null)
            outsideEvent.run();

    }

    public Rectangle getArea() {

        return area;
    }

    public void setAreaSize(float width, float height) {

        offsetX = -width / 2;
        offsetY = -height / 2;
        this.area = new Rectangle(object.position.getX() - width / 2, object.position.getY() - height / 2, width,
                height);
    }

    public void setAreaSize(float width, float height, float offsetX, float offsetY) {

        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.area = new Rectangle(object.position.getX() + offsetX, object.position.getY() + offsetY, width, height);
    }

    public void setArea(Rectangle area) {

        this.area = area;
    }

    public AbstractEvent getEvent() {

        return insideEvent;
    }

    public void setEventOnInside(AbstractEvent event) {

        this.insideEvent = event;
    }

    public void setEventOnOutside(AbstractEvent event) {

        this.outsideEvent = event;
    }

    // endregion methods
}
