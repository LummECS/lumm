/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/

package dk.sidereal.lumm.architecture;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import dk.sidereal.lumm.architecture.listeners.OnDisposeListener;
import dk.sidereal.lumm.architecture.listeners.OnEnableListener;
import dk.sidereal.lumm.architecture.listeners.OnPauseListener;
import dk.sidereal.lumm.architecture.listeners.OnResizeListener;
import dk.sidereal.lumm.components.input.Clickable;
import dk.sidereal.lumm.components.triggers.Hoverable;
import dk.sidereal.lumm.architecture.core.AppData;

/**
 * An object used for handling multiple {@link LummObject} instances at the same
 * time, rendering them using {@link #camera} and {@link #spriteBatch}, with the
 * possibility of adding a shader and sorting the objects, as well as access to
 * the mouse position translated to {@link #camera}'s coordinate system.
 * <p>
 * GameBatch objects are added in
 * {@link LummScene#addSceneLayer(LummSceneLayer)} , and are iterated over in
 * {@link LummScene#render(float)}.
 *
 * @author Claudiu
 */
public abstract class LummSceneLayer implements IEnableable {

    /**
     * This key can only be used if {@link LummScene#onCreateSceneLayers()} is
     * not used to add other scene layers. Read more there.
     *
     */
    public static final String DEFAULT_SCENE_LAYER = "Default";

    // region fields

    public OnPauseListener<LummSceneLayer> onPauseListener;
    public OnResizeListener<LummSceneLayer> onResizeListener;
    public OnDisposeListener<LummSceneLayer> onDisposeListener;
    public OnEnableListener<LummSceneLayer> onEnableInHierarchyListener;

    /**
     * Area that the camera sees and outside of which images are not rendered.
     */
    public Rectangle renderingArea;

    public enum ShaderType {
        Initialisation, Update, Resize
    }

    ;

    private boolean enabled;

    /**
     * Sprite batch on which to render all objects that are using the
     * {@link LummSceneLayer} that the {@link SpriteBatch} is tied to.
     * <p>
     * The shader found in {@link #shaderProgram} will be applied to this
     * rendering batch.
     */
    public SpriteBatch spriteBatch;

    /** Shape renderer focused on the {@link LummSceneLayer}'s camera. */
    public ShapeRenderer shapeRenderer;

    /**
     * Camera used for rendering objects tied to {@link #spriteBatch}.
     * <p>
     * The camera's position can be made to target a certain {@link LummObject}
     * by assigning {@link #target}.
     */
    public OrthographicCamera camera;

    /**
     * Variable used for sorting the {@link LummSceneLayer} rendering order in
     * the scene. Is used in {@link LummScene#sceneLayerComparator}.
     */
    public int priorityLevel;

    /**
     * Name of the gamebatch.
     * <p>
     * Is set in the constructor and used in {@link LummObject#setGameBatch}.
     */
    public String name;

    /**
     * List of {@link LummObject} entries tied to the {@link LummSceneLayer}.
     */
    public List<LummObject> objects;

    /**
     * Whether to translate the mouse position to the {@link #camera}'s
     * projection matrix. This will make using certain IO-related behaviors such
     * as {@link Clickable} or {@link Hoverable} work in {@link LummObject}
     * objects tied to the {@link LummSceneLayer}
     */
    public boolean translateMousePosition;

    /**
     * If not null, the {@link LummSceneLayer} will be using the referencing
     * batch's {@link #camera}, {@link #mousePosition}. Will be set in
     * {@link #GameBatch(LummScene, String, LummSceneLayer)}.
     */
    public LummSceneLayer referencingBatch;

    /**
     * Whether to sort the GameObjects in {@link #objects} before updating them
     * or not. Set in {@link #setSort(boolean, Comparator)}
     */
    private boolean sort;

    private static Comparator<LummObject> defaultComparator = new Comparator<LummObject>() {

        @Override
        public int compare(LummObject o1, LummObject o2) {

            if (o1.position.getZ() != o2.position.getZ()) {
                return (int) (o1.position.getZ() - o2.position.getZ());
            } else {
                return (int) o2.position.getY() - (int) o1.position.getY();
            }

        }
    };

    /**
     * The sorting algorithm to run if {@link #sort} is true on {@link #objects}
     * . Set in {@link #setSort(boolean, Comparator)}
     * <p>
     * By default, will first render objects at the top of the screen, with a
     * lower level.
     */
    private Comparator<LummObject> objectComparator;

    /**
     * The {@link LummScene} that the batch is found in. Set up in
     * {@link #initialiseBatch(LummScene, String)}.
     */
    private LummScene scene;

    /**
     * Object used for translating the mouse position to the camera's coordinate
     * system. After translating it, will be used to set variables in
     * {@link #mousePosition}.
     */
    private Vector3 tempMousePosition;

    /**
     * The position of the mouse, translated to the camera's coordinate system.
     */
    public Vector2 mousePosition;

    /**
     * Shader to run every frame on {@link #spriteBatch}. Is set in
     * {@link #setShader(ShaderProgram, AbstractEvent, ShaderType)}.
     */
    private ShaderProgram shaderProgram;

    /**
     * Event with the purpose of passing uniform values to the shader. Will be
     * called based on {@link #shaderType}.
     */
    private AbstractEvent shaderUniformValuesEvent;

    /**
     * The type of shader. Affects when {@link #shaderUniformValuesEvent} runs.
     * <p>
     * If {@link #shaderType} is set to {@link ShaderType#Initialisation}, the
     * event is running when set in
     * {@link #setShader(ShaderProgram, AbstractEvent, ShaderType)} only.
     * <p>
     * If {@link #shaderType} is set to {@link ShaderType#Resize}, the event is
     * running whenever {@link #onResizeInternal()} is called.
     * <p>
     * If {@link #shaderType} is set to {@link ShaderType#Update}, the event
     * runs every frame in {@link #onRenderInternal()}.
     */
    private ShaderType shaderType;

    // endregion

    // region constructors

    /**
     * Constructor for a GameBatch that calls
     * {@link #initialiseBatch(LummScene, String)}, as well as set up the camera
     * and the mouse position as the batch does not reference anything.
     *
     * @param scene
     *            The scene in which to add the game batch.
     * @param name
     *            name of the gameBatch, used for retrieval of Batch using
     *            {@link LummScene#getSceneLayer(String)}.
     */
    public LummSceneLayer(LummScene scene, String tag) {

        initialiseBatch(scene, tag);

        this.tempMousePosition = new Vector3();
        this.mousePosition = new Vector2();

        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.translateMousePosition = false;

        this.renderingArea = new Rectangle();
    }

    /**
     * Constructor for a GameBatch that uses resources from the <code>
     * referencingBatch</code> parameter to keep consistency.
     * <p>
     * Throws a {@link GdxRuntimeException} if passed {@link LummSceneLayer} is
     * null.
     *
     * @param scene
     *            The scene in which to add the game batch.
     * @param name
     *            name of the gameBatch, used for retrieval of Batch using
     *            {@link LummScene#getSceneLayer(String)}.
     * @param referencingBatch
     */
    public LummSceneLayer(LummScene scene, String tag, LummSceneLayer referencingBatch) {

        initialiseBatch(scene, tag);

        if (referencingBatch == null)
            throw new GdxRuntimeException("Referencing batch in constructor for the " + tag
                    + " batch was null. Use the constructor without a batch as a parameter if no referencing batch is "
                    + "needed");

        this.referencingBatch = referencingBatch;
        this.camera = referencingBatch.camera;
        this.mousePosition = referencingBatch.mousePosition;
        this.renderingArea = referencingBatch.renderingArea;
    }

    /**
     * Common initialisation code between the two constructors of
     * {@link LummSceneLayer}, initialising variables in the scene.
     * <p>
     * If the scene or the batch name are null, a {@link GdxRuntimeException} is
     * thrown.
     *
     * @param scene
     *            The scene in which to add the game batch.
     * @param name
     *            name of the gameBatch, used for retrieval of Batch using
     *            {@link LummScene#getSceneLayer(String)}.
     */
    private final void initialiseBatch(LummScene scene, String name) {

        if (scene == null)
            throw new GdxRuntimeException("GameScene parameter passed to GameBatch is null");
        if (name == null)
            throw new GdxRuntimeException("String parameter passed to GameBatch is null");

        this.scene = scene;
        this.name = name;

        this.shapeRenderer = new ShapeRenderer();
        this.shapeRenderer.setAutoShapeType(true);
        this.spriteBatch = new SpriteBatch();
        this.objects = new ArrayList<LummObject>();
        this.priorityLevel = 0;
        this.sort = true;
        setSort(true, defaultComparator);

    }

    // endregion

    // region methods

    /** Updates the area within which objects will be drawn. */
    private final void updateRenderingArea() {

        renderingArea.set(camera.position.x - ((camera.viewportWidth / 2f) * camera.zoom),
                camera.position.y - ((camera.viewportHeight / 2f) * camera.zoom), camera.viewportWidth * camera.zoom,
                camera.viewportHeight * camera.zoom);
    }

    /**
     * Runs every frame, u the mouse position to the camera's location. Gets
     * called from {@link LummScene#render(float)}
     */
    final void updateMousePosition() {
        if (translateMousePosition) {
            tempMousePosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);

            // translate the pixel to world
            camera.unproject(tempMousePosition);

            // updating mouse position.
            mousePosition.x = tempMousePosition.x;
            mousePosition.y = tempMousePosition.y;
        }
    }

    /**
     * Resizes the GameBatch, updating the camera's size, as well as the objects
     * that depend on it, such as {@link #shaderProgram}, {@link #spriteBatch}
     * or {@link #shapeRenderer}.
     * <p>
     * All of the objects tied to the GameBatch ( the ones in {@link #objects})
     * are being resized by calling
     * {@link LummObject#onResize(float, float, float, float)} on all of them.
     */
    final void onResizeInternal() {

        float oldWidth = camera.viewportWidth;
        float oldHeight = camera.viewportHeight;

        // set up new width
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);

        shapeRenderer.setProjectionMatrix(camera.combined);

        // run resize event
        if (this.shaderUniformValuesEvent != null && this.shaderType.equals(ShaderType.Resize)
                && this.shaderProgram.isCompiled()) {
            shaderProgram.begin();
            this.shaderUniformValuesEvent.run(shaderProgram);
            shaderProgram.end();
        }
        if (onResizeListener != null)
            onResizeListener.onResize(this, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), oldWidth, oldHeight);

        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).isInScene() && objects.get(i).isEnabledInHierarchyInternal()) {
                if (objects.get(i).onResizeListener != null) {

                    objects.get(i).onResizeListener.onResize(objects.get(i), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), oldWidth,
                            oldHeight);
                }

                for (int j = 0; j < objects.get(i).components.size(); j++) {
                    if (objects.get(i).components.get(j).onResizeListener != null && objects.get(i).components.get(j).isEnabledInHierarchy()) {
                        objects.get(i).components.get(j).onResizeListener.onResize(objects.get(i).components.get(j), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), oldWidth, oldHeight);
                    }
                }
            }
        }
    }

    /**
     * Handles the internal pausing and unpausing of the app. Will call
     * {@link #onPause(boolean)} as well as the object's components'
     * {@link LummComponent#onPause(boolean)}
     */
    final void onPauseInternal(boolean value) {

        if (onPauseListener != null)
            onPauseListener.onPause(this, value);

        // iterate through scene layer objects
        for (int j = 0; j < objects.size(); j++) {
            LummObject object = objects.get(j);
            object.onPauseInternal(value);
        }

    }

    /** Method that updates all objects in {@link #objects}. */
    final void onRenderInternal() {

        try {

            if (objects == null || scene == null)
                return;

            // batch's scene is not the same as the current scene.
            if (Lumm.getScene() != null && !this.scene.equals(Lumm.getScene())) {
                scene = null;
                return;
            }
            if (objects.size() == 0)
                return;

            if (sort) {
                try {
                    Collections.sort(objects, objectComparator);
                } catch (Exception e) {
                    Lumm.debug.logError("Unable to sort list of objects in the batch", e);
                    e.printStackTrace();
                }
            }

            updateRenderingArea();

            if (shaderUniformValuesEvent != null && shaderType.equals(ShaderType.Update)
                    && shaderProgram.isCompiled()) {
                shaderProgram.begin();
                shaderUniformValuesEvent.run(shaderProgram);
                shaderProgram.end();
            }

            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();

            // update objects
            if (objects != null) {
                for (int i = 0; i < objects.size(); i++) {
                    objects.get(i).onRenderInternal();
                    if (objects == null)
                        return;
                }
                if (Lumm.debug.isEnabled()) {
                    for (int i = 0; i < objects.size(); i++) {
                        objects.get(i).onDebugInternal();
                    }
                }

            }
            spriteBatch.end();
        } catch (Exception e) {
            Lumm.net.logThrowable(e);
        }
    }

    final void onDisposeInternal() {

        if (onDisposeListener != null)
            onDisposeListener.onDispose(this);

        for (int i = 0; i < objects.size(); i++) {
            objects.get(i).onDisposeInternal();
        }

        if (spriteBatch.isDrawing())
            spriteBatch.end();

        spriteBatch.dispose();

        if (shapeRenderer.isDrawing())
            shapeRenderer.end();

        shapeRenderer.dispose();
        new WeakReference<LummSceneLayer>(this);

    }

    /**
     * Handles setting the shader for the {@link #spriteBatch}.
     *
     * @param program
     *            The shader to run every frame
     * @param uniformValuesEvent
     *            the event for setting uniforms.
     * @param type
     *            The type of shader, which affects when
     *            <code>uniformValuesEvent</code> gets called.
     */
    public final void setShader(ShaderProgram program, AbstractEvent uniformValuesEvent, ShaderType type) {

        // not using shaders in the settings.
        if (!((Boolean) Lumm.data.getSettings(AppData.Settings.USE_SHADERS)))
            return;

        // don't handle shader if it GLSL failed compilation
        if (!program.isCompiled()) {
            Lumm.debug.logError("Shader couldn't compile, exception: " + program.getLog(), null);
            return;
        }

        // handling null variables
        if (program == null || type == null) {
            this.shaderProgram = null;
            this.shaderUniformValuesEvent = null;
            this.spriteBatch.setShader(null);

            return;
        }
        ShaderProgram.pedantic = false;
        this.shaderProgram = program;
        this.shaderType = type;

        this.shaderUniformValuesEvent = uniformValuesEvent;
        spriteBatch.setShader(this.shaderProgram);

        Lumm.debug.logError("Shader compiled successfully? " + this.shaderProgram.isCompiled(), null);

        // run initialisation or resize event
        if (this.shaderUniformValuesEvent != null
                && (shaderType.equals(ShaderType.Initialisation) || shaderType.equals(ShaderType.Resize))
                && this.shaderProgram.isCompiled()) {
            shaderProgram.begin();
            this.shaderUniformValuesEvent.run(shaderProgram);
            shaderProgram.end();
        }

    }

    /**
     * Sets whether or not sorting should be enabled, and maybe the comparator
     * used in sorting
     * <p>
     * If the <code>enabled</code> parameter is set to true, the
     * <code>comparator</code> can be null only if the value of the internal
     * comparator has been set before, either in the {@link LummSceneLayer}
     * comparator or using {@link #setSort(boolean, Comparator)}. Passing a null
     * <code>comparator</code> parameter will imply that you don't want the
     * comparator to change
     *
     * @param enabled
     *            Whether to enable sorting or not
     * @param comparator
     *            The Comparator to use, use null if already defined and you
     *            don't want to change it.
     */
    public void setSort(boolean enabled, Comparator<LummObject> comparator) {

        sort = enabled;
        if (comparator != null)
            objectComparator = comparator;

    }

    @Override
    public boolean isEnabled() {

        return enabled;

    }

    @Override
    public boolean isEnabledInHierarchy() {
        if (enabled)
            return Lumm.getScene().equals(scene);
        else
            return false;

    }

    @Override
    public boolean setEnabled(boolean enabled) {

        this.enabled = enabled;
        return true;
    }

    // endregion
}
