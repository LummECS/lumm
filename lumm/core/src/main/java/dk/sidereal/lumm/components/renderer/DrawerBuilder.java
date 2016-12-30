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

package dk.sidereal.lumm.components.renderer;

/**
 * Builder class that helps with creating a Drawer, used for customisation.
 * {@link Renderer} uses {@link Renderer#addDrawer(DrawerBuilder)} to create a
 * drawer from the Builder.
 *
 * @author Claudiu Bele
 *
 * @param <T>
 *            extension of {@link Drawer}
 */
public abstract class DrawerBuilder<T extends Drawer> {

    // region fields

    /**
     * The renderer in which the object exists. Is passed from the Renderer when
     * calling{
     *
     * @link {@link Renderer#addDrawer(String, DrawerBuilder)} , which calls
     *       {@link #buildInternal(Renderer)} with the renderer we are in as a
     *       parameter.
     */
    public Renderer renderer;

    /**
     * Whether the texture to be rendered using real time between frames (
     * {@link Time#getRealDeltaTime()}) or the time adjusted by the game speed (
     * {@link Time#getDeltaTime()}, which is adjusted by multiplying it by
     * {@link Time#getTimeScale()}).
     * <p>
     * Should be passed to {@link #build(String)} when constructing the
     * {@link Drawer} that the builder targets.
     */
    private boolean useRealDeltaTime;

    // endregion fields

    // region methods

    /**
     * Returns an instance of a Drawer tied to the {@link DrawerBuilder}. The
     * method is used in {@link Renderer#addDrawer(DrawerBuilder)}.
     * <p>
     * Calls the {@link #build(Object...)} method that is customizable,
     * afterwards setting the renderer using the 1st passed parameter in .
     *
     * @param renderer
     *            passed by the renderer that calls
     *            {@link Renderer#addDrawer(String, DrawerBuilder)}
     * @param name
     *            name used for getting a {@link Drawer} using
     *            {@link Renderer#getDrawer(String)}
     * @return
     */
    final T buildInternal(Renderer renderer, String name) {

        this.renderer = renderer;
        T drawer = build(name);
        drawer.setRenderer(this.renderer);
        return drawer;
    }

    /**
     * Returns an instance of a Drawer tied to the {@link DrawerBuilder} with
     * the {@link Drawer#name} set to the one passed in
     * {@link Renderer#addDrawer(String, DrawerBuilder)}. The method is used in
     * {@link #buildInternal(Object...)}.
     *
     * @return A {@link Drawer} instance.
     */
    protected abstract T build(String name);

    protected DrawerBuilder<T> setUseRealTime(boolean useRealTime) {
        this.useRealDeltaTime = useRealTime;
        return this;
    }

    protected boolean getUseRealTime() {
        return this.useRealDeltaTime;
    }

    // endregion methods
}
