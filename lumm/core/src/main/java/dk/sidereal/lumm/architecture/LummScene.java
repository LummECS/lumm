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

package dk.sidereal.lumm.architecture;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import dk.sidereal.lumm.architecture.concrete.ConcreteLummSceneLayer;
import dk.sidereal.lumm.architecture.listeners.OnEnableListener;
import dk.sidereal.lumm.architecture.listeners.OnUpdateListener;

/**
 * Scene of the game. Handles updating all of the objects tied to the scene by
 * updating the {@link LummSceneLayer} objects found in {@link #sceneLayers}.
 * The gamebatches added to the scene are handled in
 * {@link #onCreateSceneLayers()} which can be overrided.
 * <p>
 * To set the current scene, initialise a scene and call
 * {@link Lumm#setScene(Screen)}. For passing objects from one scene to another,
 * use {@link #passObjectsToNextScene(ArrayList)} or
 * {@link #passObjectToNextScene(LummObject)}.
 * <p>
 * Pausing and unpausing can be easily customised from scene to scene through
 * the use of {@link #eventOnPause} and {@link #eventOnUnpause}.
 * <p>
 * The object also deals with drawing and clearing the rendering batches.
 *
 * @see {@link LummSceneLayer} for scene "layers"
 * @see {@link LummObject} for individual entities
 *
 * @author Claudiu Bele
 */
public abstract class LummScene implements Screen {

    public OnEnableListener<LummScene> onEnableInHierarchyListener;
    public OnUpdateListener<LummScene> onUpdateListener;

    // region fields
    /** list of game batches created in {@link #onCreateSceneLayers()} */
    protected ArrayList<LummSceneLayer> sceneLayers;

    /**
     * The default game batch. The particle game batches use this game batche's
     * camera to properly render things where they should
     */
    protected LummSceneLayer defaultBatch;

    /** Color used for clearing the screen */
    protected Color bgColor;

    /**
     * List in which we will put objects that we want to remove, removing them
     * at the end of the frame.
     */
    private ArrayList<LummObject> toRemove;

    /**
     * List of objects that will not be disposed, and will be passed and updated
     * in the next scene, being added to {@link #objectsList} and
     * {@link #objectsMap} as well as having the {@link LummObject#gameBatch}
     * set to the one with the same tag in the new scene
     */
    ArrayList<LummObject> toKeepForNextScene;

    /**
     * Array containing the parameters passed when creating the scene using
     * {@link LummScene#LummScene(Object...)}. Passed automatically to
     * {@link LummScene#onCreate(Object...)}.
     * <p>
     * Array is empty when creating the {@link LummScene} using
     * {@link LummScene#LummScene()}
     *
     */
    Object[] runParameters;

    /**
     * List in which we will put objects that we want to add, adding them at the
     * end of the frame.
     */
    private ArrayList<LummObject> toAdd;

    /**
     * Objects Organised in a map, by type, the value baing another map, where
     * the key is the name of the Object
     * <p>
     * To get a specific object you need his {@link LummObject#name} and
     * {@link LummObject#type}
     */
    public HashMap<String, HashMap<String, LummObject>> objectsMap;

    /**
     * Comparator used for sorting the game batches. Used in
     * {@link LummScene#render(float) } , is assigned in
     * {@link GameScene#GameScene(Object[]...)} to sorting based on
     * {@link LummSceneLayer#priorityLevel}.
     */
    protected Comparator<LummSceneLayer> sceneLayerComparator;

    // endregion

    /**
     * Base constructor for LummScene. Nor this or
     * {@link LummScene#LummScene(Object...)} should be overriden, as the native
     * backend might not be started if done to the first scene. Because of this,
     * use {@link #onCreate(Object...)} for instantiation of {@link LummObject}
     * instances and everything else.
     */
    public LummScene() {
        this(new Object[]{});
    }

    // region Constructor

    /**
     * Base constructor for LummScene. Nor this or {@link LummScene#LummScene()}
     * should be overriden, as the native backend might not be started if done
     * to the first scene. Because of this, use {@link #onCreate()} for
     * instantiation of {@link LummObject} instances and everything else.
     */
    public LummScene(Object... params) {

        this.sceneLayers = new ArrayList<LummSceneLayer>();
        this.sceneLayerComparator = new Comparator<LummSceneLayer>() {

            @Override
            public int compare(LummSceneLayer o1, LummSceneLayer o2) {

                return o1.priorityLevel - o2.priorityLevel;
            }
        };

        this.toAdd = new ArrayList<LummObject>();
        this.toRemove = new ArrayList<LummObject>();
        this.objectsMap = new HashMap<String, HashMap<String, LummObject>>();
        this.toKeepForNextScene = new ArrayList<LummObject>();
        this.bgColor = new Color(1, 1, 1, 1);
        this.runParameters = params;
    }

    /**
     * Creates the Scene. Add all the wanted objects and the logic to them here.
     * <p>
     * This method is called in the constructor of the scene, and you have to
     * make all the objects and assign values to them here, after which you have
     * to add them to the objects list.
     * <p>
     * In the render method, we call the update of each update in the arraylist,
     * which calls the update of each of it's behavior.
     *
     * @param params
     */
    protected abstract void onCreate(Object[] params);

    /**
     * Method for making the scene layers to use in the scene. After setting up
     * a {@link LummSceneLayer} object (by modifying using another SceneLayer as
     * layer to be in sync with, shader and whether to translate mouse position
     * to name a few) call {@link #addSceneLayer(LummSceneLayer)}.
     * <p>
     * {@link LummObject} instances made for this scene will by default be
     * assigned to be rendered on the {@link #defaultBatch}. If the value of
     * that value is not set, the first {@link LummSceneLayer} that was used as
     * a parameter in a {@link #addSceneLayer(LummSceneLayer)} will become
     * {@link #defaultBatch}.
     * <p>
     * If no <code>LummSceneLayer</code> objects are added to the scene in this
     * method, the scene will create a layer with the name {@link LummSceneLayer#DEFAULT_SCENE_LAYER }
     * <p>
     * The target <code>LummSceneLayer</code> of a <code>Lummobject</code> can
     * be set using {@link LummObject#setGameBatch(String)}.
     */
    protected abstract void onCreateSceneLayers();

    /**
     * Method in which to handle calls to {@link Lumm#pause(boolean)}, app going
     * out of focus which leads to pausing, or app coming back into focus which
     * leads to unpausing.
     * <p>
     * Similar calls to {@link LummSceneLayer}, {@link LummObject} and
     * {@link LummComponent} will be done internally, there is no need to be
     * handled in this method.
     *
     * @param value
     */
    protected abstract void onPause(boolean value);

    // endregion

    // region methods

    /**
     * Adds a gameBatch to {@link #sceneLayers}, which is updated in
     * {@link #render(float)}.
     * <p>
     * The Gamebatch will be added if it is not already in the list.
     *
     * @param layer
     *            The batch that you want to add
     */
    public final void addSceneLayer(LummSceneLayer layer) {

        if (!sceneLayers.contains(layer)) {
            sceneLayers.add(layer);
            Collections.sort(sceneLayers, sceneLayerComparator);
        }
    }

    /**
     * Attempts to remove a specific gameBatch from the list.
     *
     * @param batch
     *            The batch that you want removed
     * @return Whether or not the batch was in {@link #sceneLayers}
     */
    public final boolean removeSceneLayer(LummSceneLayer batch) {

        return sceneLayers.remove(batch);
    }

    /**
     * Returns the game batch with the required name or null if not found in the
     * list
     *
     * @param name
     *            The name of the gamebatch
     * @return
     */
    public final LummSceneLayer getSceneLayer(String name) {

        for (int i = 0; i < sceneLayers.size(); i++) {
            if (sceneLayers.get(i).name.equals(name)) {
                return sceneLayers.get(i);
            }
        }
        return null;
    }

    /**
     * Stores a {@link LummObject} object for adding it to the next scene, in
     * the event that the scene is changed. The object still exists in the
     * current scene until a new scene is added.
     * <p>
     * The {@link LummSceneLayer} that the object will try to added to in the
     * new scene will be the one with the same name as the current
     * <code>LummSceneLayer</code> the object is tied to. If such a layer does
     * not exist, the object will be set to the {@link #defaultBatch} of the new
     * scene, and can afterwards be changed if necessary in
     * {@link LummObject#onSceneChange()}.
     *
     * <p>
     * If the object has any children, the children will also be passed to the
     * next scene without the need of calling this method for all of them.
     * <p>
     * In order for an object to persist through more scenes, this method has to
     * be called in every scene, thus a persistent alternative exists for
     * objects that need to persist from instantiation to app exit.
     *
     *
     * @param obj
     */
    public final <T extends LummObject> void passObjectToNextScene(T obj) {

        if (!toKeepForNextScene.contains(obj)) {
            toKeepForNextScene.add(obj);

            synchronized (obj.children) {
                if (obj.children != null) {
                    for (int i = 0; i < obj.children.size(); i++) {
                        passObjectToNextScene(obj.children.get(i));
                    }
                }
            }
            return;
        }
    }

    /**
     * Stores a list of {@link LummObject} objects for adding it to the next
     * scene, in the event that the scene is changed. The object still exists in
     * the current scene until a new scene is added.
     * <p>
     * The {@link LummSceneLayer} that the object will try to added to in the
     * new scene will be the one with the same name as the current
     * <code>LummSceneLayer</code> the object is tied to. If such a layer does
     * not exist, the object will be set to the {@link #defaultBatch} of the new
     * scene, and can afterwards be changed if necessary in
     * {@link LummObject#onSceneChange()}.
     *
     * <p>
     * If the object has any children, the children will also be passed to the
     * next scene without the need of calling this method for all of them.
     * <p>
     * In order for an object to persist through more scenes, this method has to
     * be called in every scene, thus a persistent alternative exists for
     * objects that need to persist from instantiation to app exit.
     *
     *
     * @param obj
     */
    public final <T extends LummObject> void passObjectsToNextScene(ArrayList<T> objects) {

        for (int i = 0; i < objects.size(); i++) {
            passObjectToNextScene(objects.get(i));
        }
    }

    /**
     * Puts an object in a queue to be added at the end of the frame.
     *
     * @param obj
     */
    public void addObject(LummObject obj) {

        toAdd.add(obj);
    }

    /**
     * Puts the object in a queue to be removed at the end of the frame
     *
     * @param obj
     */
    public void removeobject(LummObject obj) {

        obj.inScene = false;

        synchronized (toRemove) {
            toRemove.add(obj);
        }
    }

    /**
     * Retrieves the object with the expected type and name , or if it can't be
     * found, returns null. This method is very expensive as it queries all
     * objects (until the first one is found) in all gamebatches for a potential
     * match. If no object is found, null will be returned.
     *
     * @param type
     *            The type of the object to find
     * @param name
     *            The name of the object to find
     * @return the first object with the given type and name, otherwise null.
     */
    public LummObject getObject(String type, String name) {

        for (int i = 0; i < sceneLayers.size(); i++) {
            for (int j = 0; j < sceneLayers.get(i).objects.size(); i++) {
                if (sceneLayers.get(i).objects.get(j).getName().equals(name)
                        && sceneLayers.get(i).objects.get(j).getType().equals(type))
                    return sceneLayers.get(i).objects.get(j);
            }
        }

        return null;
    }

    // endregion

    // region inheritance methods

    /**
     * Called when the screen renders itself
     * <p>
     * This method shouldn't be changed or called manually, as it is public
     * because the parent class demands it to be public
     */
    @Override
    public final void render(float delta) {

        try {

            Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            if (sceneLayers == null)
                return;
            for (int i = 0; i < sceneLayers.size(); i++) {
                sceneLayers.get(i).updateMousePosition();
                sceneLayers.get(i).onRenderInternal();
            }

            for (int i = 0; i < sceneLayers.size(); i++) {
                // camera view is different from screen size => resize
                if ((Gdx.graphics.getWidth() != sceneLayers.get(i).camera.viewportWidth
                        || Gdx.graphics.getHeight() != sceneLayers.get(i).camera.viewportHeight)
                        && sceneLayers.get(i).referencingBatch == null)

                {
                    sceneLayers.get(i).onResizeInternal();
                }
            }

            addObjects();
            removeObjects();
        } catch (Exception e) {
            Lumm.net.logThrowable(e);
        }
    }

    /**
     * Called when the current scene changes on the scene that the game changes
     * to. This method should not be called manually, as it is public because
     * the parent class demands it to be public
     */
    @Override
    public final void show() {

    }

    @Override
    public final void resize(int width, int height) {

    }

    @Override
    public final void hide() {

    }

    /**
     * Pauses the system. Do not call this manually, it is exposed due to the
     * parent class and will be called internally
     *
     * <p>
     * In order to pause the game, call {@link Lumm#pause(boolean)} with true
     * for pausing, false for unpausing.
     * <p>
     * {@link LummSceneLayer} will react to it using
     * {@link LummSceneLayer#onPause(boolean)}, {@link LummObject} will react to
     * it using {@link LummObject#onPause(boolean)} and components will react to
     * it using {@link LummComponent#onPause(boolean)}.
     */
    @Override
    public final void pause() {
        for (int i = 0; i < sceneLayers.size(); i++) {
            sceneLayers.get(i).onPauseInternal(true);
        }
    }

    /**
     * Pauses the system. Do not call this manually, it is exposed due to the
     * parent class and will be called internally
     *
     * <p>
     * In order to pause the game, call {@link Lumm#pause(boolean)} with true
     * for pausing, false for unpausing.
     * <p>
     * {@link LummSceneLayer} will react to it using
     * {@link LummSceneLayer#onPause(boolean)}, {@link LummObject} will react to
     * it using {@link LummObject#onPause(boolean)} and components will react to
     * it using {@link LummComponent#onPause(boolean)}.
     */
    @Override
    public final void resume() {
        for (int i = 0; i < sceneLayers.size(); i++) {
            sceneLayers.get(i).onPauseInternal(false);
        }
    }

    @Override
    public final void dispose() {

        for (int i = 0; i < sceneLayers.size(); i++) {
            sceneLayers.get(i).onDisposeInternal();
        }
        sceneLayers = null;
        sceneLayerComparator = null;
        defaultBatch = null;
        objectsMap = null;
        new WeakReference<LummScene>(this);
    }

    // endregion

    // region internal

    /**
     * Adds objects currently in a queue to be added to the scene. Called every
     * frame when the scene is the focused scene.
     */
    private final void addObjects() {

        for (int i = 0; i < toAdd.size(); i++) {
            toAdd.get(i).inScene = true;

            if (objectsMap.containsKey(toAdd.get(i).getType())
                    && objectsMap.get(toAdd.get(i).getType()).containsValue(toAdd.get(i))) {
            } else {
                if (!objectsMap.containsKey(toAdd.get(i).getType())) {
                    objectsMap.put(toAdd.get(i).getType(), new HashMap<String, LummObject>());
                }
                objectsMap.get(toAdd.get(i).getType()).put(toAdd.get(i).getName(), toAdd.get(i));
            }

        }
        toAdd.clear();

    }

    /**
     * Internally removes objects currently in a queue to be removed from the
     * scene. Called every frame when the scene is the focused scene, removing
     * objects flagged for removal.
     */
    private final void removeObjects() {

        // handles removing objects that were to be removed during this
        // frame
        for (int i = 0; i < toRemove.size(); i++) {
            // Checks whether or not the object we want to remove exists
            if (objectsMap.containsKey(toRemove.get(i).getType())) {

                if (objectsMap.get(toRemove.get(i).getType()).containsKey(toRemove.get(i).getName())) {

                    objectsMap.get(toRemove.get(i).getType()).remove(toRemove.get(i).getName());

                }
            }

            if (toRemove.get(i).getSceneLayer().objects.contains(toRemove.get(i)))
                toRemove.get(i).getSceneLayer().objects.remove(toRemove.get(i));
            toRemove.get(i).onDisposeInternal();
            new WeakReference<LummObject>(toRemove.get(i));

        }
        if (toRemove.size() > 0)
            toRemove.clear();
    }

    final void onCreateSceneLayersInternal() {
        onCreateSceneLayers();
        if (sceneLayers.size() == 0) {
            defaultBatch = new ConcreteLummSceneLayer(this, LummSceneLayer.DEFAULT_SCENE_LAYER);
            defaultBatch.translateMousePosition = true;
            addSceneLayer(defaultBatch);
        } else if (defaultBatch == null) {
            defaultBatch = sceneLayers.get(0);
        }
    }

    final void onPauseInternal(boolean value) {
        onPause(value);
        if (value)
            pause();
        else
            resume();
    }

    final void onEnableInternal(boolean value) {

        if (onEnableInHierarchyListener != null)
            onEnableInHierarchyListener.onEnable(this, value);

        for (int i = 0; i < sceneLayers.size(); i++) {
            if (sceneLayers.get(i).onEnableInHierarchyListener != null)
                sceneLayers.get(i).onEnableInHierarchyListener.onEnable(sceneLayers.get(i), value);
        }

    }

    final void onUpdateInternal() {

        if (onUpdateListener != null)
            onUpdateListener.onUpdate(this);

        try {

            for (int i = 0; i < sceneLayers.size(); i++) {

                LummSceneLayer currBatch = sceneLayers.get(i);
                for (int j = 0; j < currBatch.objects.size(); j++) {
                    currBatch.objects.get(j).onUpdateInternal();
                }

            }
        } catch (Exception e) {
            Lumm.net.logThrowable(e);
            e.printStackTrace();
        }

    }

    // endregion

    // endregion
}
