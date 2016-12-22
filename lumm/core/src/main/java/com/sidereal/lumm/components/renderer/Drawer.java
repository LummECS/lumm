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

package com.sidereal.lumm.components.renderer;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Responsible for drawing in an individual matter an image or set of images
 * over a number of frames. A {@link Drawer} instance is retrieved using
 * {@link DrawerBuilder#build(String)}.
 *
 * @author Claudiu Bele
 */
public abstract class Drawer {

    // region fields

    public ShaderProgram targetShader;

    /**
     * Whether the renderer to use the real raw delta (if applicable) is passed
     * in {@link DrawerBuilder#build(String)} from the builder if the
     * constructor requests the value, otherwise set to false.
     * <p>
     * If the constructor does not require a variable to assign to this field ,
     * the value should not matter.
     */
    private boolean useRealDeltaTime;

    /**
     * The renderer that the Drawer is attached to. Set in
     * {@link #Drawer(Renderer, String, boolean)}.
     */
    protected Renderer renderer;

    /** If the Drawer is enabled, it will be drawn. */
    private boolean enabled;

    /**
     * Name of the drawer. Assigned in
     * {@link Renderer#addDrawer(String, DrawerBuilder)}.
     */
    protected String name;

    // endregion

    // region Constructor

    /**
     * Base constructor for Drawer. In the extensions of a Drawer, all
     * constructors must contain the non-optional parameters, such as the
     * filepath to the animation in {@link SCMLDrawer#SpriterDrawer(Renderer)}
     * and the name of the Drawer, passed in
     * {@link Renderer#addDrawer(String, DrawerBuilder)}.
     *
     * @param renderer
     * @param name
     *            The name of the drawer.
     * @param useRawDelta
     */
    public Drawer(Renderer renderer, String name, boolean useRawDelta) {

        this.enabled = true;
        this.name = name;
        this.useRealDeltaTime = useRawDelta;
    }

    // endregion

    // region methods

    // region internal
    final void drawInternal(float delta) {

        // if(targetShader != null &&
        // renderer.object.getGameBatch().spriteBatch.)

        if (enabled && !isOutOfBounds())
            draw(delta);
    }

    public boolean getUseRealDeltaTime() {
        return useRealDeltaTime;
    }

    // endregion

    // region abstract

    /**
     * Disposes values that might need to be disposed. It is up to the
     * implementation of Drawer classes to handle this.
     * <p>
     * It is called from
     * {@link com.sidereal.lumm.components.renderer.Renderer#removeDrawer(String)}
     */
    protected abstract void dispose();

    /**
     * Called from within {@link #drawInternal(float)} by the
     * {@link com.sidereal.lumm.components.renderer.Renderer}.
     *
     * @param delta
     *            time between frames
     */
    protected abstract void draw(float delta);

    /**
     * Whether or not the object is out of bounds. If it is, do not render.
     * Handled in {@link #drawInternal(float)}.
     *
     * @return Whether the object is visible on the screen
     */
    protected abstract boolean isOutOfBounds();

    // endregion

    // region getters and setters
    public Renderer getRenderer() {

        return renderer;
    }

    public void setRenderer(Renderer renderer) {

        this.renderer = renderer;
    }

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    // endregion

    // endregion methods

}
