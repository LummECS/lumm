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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

import java.lang.ref.WeakReference;

import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummObject;
import dk.sidereal.lumm.architecture.concrete.ConcreteLummComponent;

/**
 * Class for rendering objects of different types. It contains {@link Drawer}
 * instances that require a {@link DrawerBuilder} to build them.
 * <p>
 * All of the {@link Drawer}s are added using
 * {@link #addDrawer(String, DrawerBuilder)} to {@link #drawerList} and
 * {@link #drawerMap},the list being iterated over and drawn in order of the
 * indexes in the array. The order can be changed using
 * {@link #placeAtEnd(String)} and {@link #placeAtStart(String)}.
 * <p>
 * When we want to remove a {@link Drawer}, we call
 * {@link #removeDrawer(String)}, which will call
 * {@link Drawer#drawInternal(float)}.
 *
 * @author Claudiu Bele
 */
public class Renderer extends ConcreteLummComponent {

    // region fields

    /**
     * Map of Drawers. Adding values in
     * {@link #addDrawer(String, DrawerBuilder)} and removing them in
     * {@link #removeDrawer(String)}.
     */
    private ObjectMap<String, Drawer> drawerMap;

    /**
     * List of drawers. The order in which { Drawer} objects are found in the
     * list decides which one is rendered first, rendering first from index 0 to
     * n.
     */
    private Array<Drawer> drawerList;

    // endregion fields

    // region constructors

    /**
     * Initialises the data types.
     *
     * @param obj
     *            the object that the behavior is attached to.
     */
    public Renderer(LummObject obj) {

        super(obj);
        drawerList = new Array<Drawer>();
        drawerMap = new ObjectMap<String, Drawer>();
    }

    // endregion constructors

    // region methods

    /**
     * Draws each {@link Drawer} object found in {@link #drawerList} in the
     * order that they are found in the list. Use {@link #placeAtStart(String)}
     * or {@link #placeAtEnd(String)} to adjust
     */
    @Override
    public final void onRender() {

        for (int i = 0; i < drawerList.size; i++) {
            Drawer drawer = drawerList.get(i);
            float delta = (drawer.getUseRealDeltaTime()) ? Lumm.time.getRealDeltaTime() : Lumm.time.getDeltaTime();
            drawer.drawInternal(delta);
        }
    }

    /**
     * Adds a drawer to the renderer.
     * <p>
     * The parameters as well as the {@link Renderer} instance itself are used
     * for constructing {@link Drawer} out of a {@link DrawerBuilder}.
     * <p>
     * Calls {@link #addDrawer(String, DrawerBuilder, boolean)} with the boolean
     * parameter set to false, which creates a weak reference to the builder
     * signalling that it can be removed by the GC
     *
     * @param drawerName
     *            name of the drawer, will be used for retrieval of drawer using
     *            {@link #getDrawer(String)} and
     *            {@link #getDrawer(String, Class)}.
     * @param builder
     *            builder to crate a drawer with
     * @return the drawer resulted on calling
     *         {@link DrawerBuilder#buildInternal(Renderer, String)}
     */
    public final <T extends Drawer> T addDrawer(String drawerName, DrawerBuilder<T> builder) {
        return addDrawer(drawerName, builder, true);
    }

    /**
     * Adds a drawer to the renderer.
     * <p>
     * The parameters as well as the {@link Renderer} instance itself are used
     * for constructing {@link Drawer} out of a {@link DrawerBuilder}.
     * <p>
     * Is called from {@link #addDrawer(String, DrawerBuilder)} with the
     * <code>destroyAfterCreation</code> parameter set to true, but can also be
     * called by the user on a renderer.
     *
     * @param drawerName
     *            name of the drawer, will be used for retrieval of drawer using
     *            {@link #getDrawer(String)} and
     *            {@link #getDrawer(String, Class)}.
     * @param builder
     *            builder to crate a drawer with
     * @param destroyAfterCreation
     *            Whether to create a weak reference to the builder after the
     *            drawer has been created
     * @return the drawer resulted on calling
     *         {@link DrawerBuilder#buildInternal(Renderer, String)}
     */
    public final <T extends Drawer> T addDrawer(String drawerName, DrawerBuilder<T> builder,
                                                boolean destroyAfterCreation) {
        T drawer = builder.buildInternal(this, drawerName);
        drawerList.add(drawer);
        drawerMap.put(drawerName, drawer);

        // create a weak reference to the builder
        if (destroyAfterCreation)
            new WeakReference<DrawerBuilder<T>>(builder);

        return drawer;

    }

    // region utility

    /**
     * Switches the order of the elements in {@link #drawerList} which handles
     * rendering {@link Drawer} instances in the order that they are given, thus
     * placing an element at start will make it render first.
     *
     * @param name
     *            The passed value in
     */
    public final void placeAtStart(String name) {

        drawerList.insert(0, drawerList.removeIndex(drawerList.indexOf(drawerMap.get(name), false)));
    }

    /**
     * Switches the order of the elements in {@link #drawerList} which handles
     * rendering {@link Drawer} instances in the order that they are given, thus
     * placing an element at end will make it render last.
     *
     * @param name
     *            the drawer key for {@link #drawerMap} passed in
     *            {@link #addDrawer(String, DrawerBuilder)}.
     */
    public final void placeAtEnd(String name) {

        drawerList.insert(drawerList.size - 1, drawerList.removeIndex(drawerList.indexOf(drawerMap.get(name), false)));

    }

    // endregion

    // region setters and getters

    /**
     * Returns the drawer tied to the passed value/
     *
     * @param name
     *            The key to the drawer used in
     *            {@link #addDrawer(String, DrawerBuilder)} for
     *            {@link #drawerMap}.
     * @return The value tied to the <code>name</code> parameter from
     *         {@link #drawerMap}.
     */
    public Drawer getDrawer(String name) {

        if (!drawerMap.containsKey(name))
            return null;
        return drawerMap.get(name);
    }

    /**
     * Returns a drawer casted as the passed class.
     * <p>
     * Will return null if the value can't be found by key in {@link #drawerMap}
     * and a {@link GdxRuntimeException} if the object is casted as a different
     * {@link Drawer} class than it is.
     *
     * @param name
     *            The key to the value we want to retrieve from
     *            {@link #drawerMap}
     * @param drawerClass
     *            The {@link Drawer} subclass we want to cast to and return
     * @return The casted value if found and passed class meets object's class,
     *         {@link GdxRuntimeException} if class to cast to doesn't meet
     *         object's class.
     */
    @SuppressWarnings("unchecked")
    public <T extends Drawer> T getDrawer(String name, Class<T> drawerClass) {

        if (!drawerMap.containsKey(name))
            return null;

        if (!drawerMap.get(name).getClass().equals(drawerClass))
            throw new GdxRuntimeException("You are trying to get the Drawer " + name + " as a " + drawerClass.getName()
                    + " but the value is of class " + drawerMap.get(name).getClass().getName());
        return (T) drawerMap.get(name);

    }

    /**
     * Removes a drawer from {@link #drawerList} and {@link #drawerMap}.
     * <p>
     * If a {@link Drawer} is found in {@link #drawerMap} with the parameter
     * passed as a key, {@link Drawer#dispose()} will also be called on the
     * found value.
     *
     * @param name
     *            name of the drawer that is the key to the { Drawer} value in
     *            {@link #addDrawer(String, DrawerBuilder)} added in
     * @return
     */
    public boolean removeDrawer(String name) {

        if (!drawerMap.containsKey(name))
            return false;

        drawerMap.get(name).dispose();
        drawerList.removeIndex(drawerList.indexOf(drawerMap.get(name), false));
        drawerMap.remove(name);
        return true;
    }

    // endregion

    // endregion methods
}
