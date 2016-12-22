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

package com.sidereal.lumm.architecture;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.sidereal.lumm.architecture.core.AppData;
import com.sidereal.lumm.architecture.core.Assets;
import com.sidereal.lumm.architecture.core.Debug;
import com.sidereal.lumm.architecture.core.Input;
import com.sidereal.lumm.architecture.core.Net;
import com.sidereal.lumm.architecture.core.Audio;
import com.sidereal.lumm.architecture.core.Time;

import com.sidereal.lumm.util.LummException;

/**
 * Bridge between LibGDX {@link ApplicationListener} and Lumm's {@link Lumm}.
 * <p>
 * Creates a {@link Lumm} instance using a {@link LummConfiguration} instance
 * for changing framework settings, as well as a {@link LummScene} instance to
 * redirect to. The {@link LummConfiguration} parameter is not necessary, as
 * there are 2 constructors that can be called.
 *
 * @author Claudiu Bele
 */
public class Lumm extends Game {

    // region fields

    @SuppressWarnings("unused")
    private static final String version = "Beta 0.5.2";

    public static Audio audio;

    public static Time time;

    public static Debug debug;

    public static Assets assets;

    public static AppData data;

    public static Input input;

    public static Net net;

    /** Array containing all of the functionality modules. */
    static ObjectMap<Class<? extends LummModule>, LummModule> modules;

    private static Lumm instance;

    static boolean disposed;

    private LummScene targetScene;

    private UpdateThread updateThread;

    private LummConfiguration configuration;

    private boolean inBackground;

    private boolean paused;


    // endregion fields

    // region constructors

    /**
     * Lumm constructor that does not require a {@link LummConfiguration}
     * parameter. This constructor calls
     * {@link #Lumm(LummScene, LummConfiguration)} by creating a new
     * <code>Lumm</code> instance (thus the application has default
     * configuration).
     * <P>
     * Is to be used in the platform-dependent main methods.
     *
     * @throws LummException
     *             see {@link #Lumm(LummScene, LummConfiguration)} for exception
     * @param initialScene
     *            The scene to switch to after initialisation
     */
    public Lumm(LummScene initialScene) {
        this(initialScene, new LummConfiguration());
    }

    /**
     * Lumm constructor that requires scene and a configuration. If the
     * configuration is null, a new instance is created with default values.
     * <p>
     * The default application modules are configured, by calling the configure
     * method in them with the LummConfiguration parameters. All custom modules
     * found in {@link LummConfiguration#modules} are added to
     * {@link Lumm#modules}.
     *
     *
     * @throws LummException
     *             when the <code>initialscene</code> parameter is null
     * @param initialScene
     * @param cfg
     */
    public Lumm(LummScene initialScene, LummConfiguration cfg) {

        if (initialScene == null)
            throw new LummException("Lumm(LummScene,LummConfiguration) constructor "
                    + "parameter 'initialScene' of type GameScene is null");
        if (cfg == null)
            cfg = new LummConfiguration();
        this.configuration = cfg;

        updateThread = new UpdateThread();
        Lumm.modules = new ObjectMap<Class<? extends LummModule>, LummModule>();

        addModule(Lumm.debug = new Debug(configuration));
        addModule(Lumm.assets = new Assets(configuration));
        addModule(Lumm.data = new AppData(configuration));
        addModule(Lumm.input = new Input(configuration));
        addModule(Lumm.net = new Net(configuration));
        addModule(Lumm.time = new Time(configuration));
        addModule(Lumm.audio = new Audio(configuration));
        for (int i = 0; i < cfg.modules.size(); i++) {
            try {
                if (!containsModule(cfg.modules.get(i))) {
                    addModule((LummModule) cfg.modules.get(i).getConstructors()[0].newInstance(cfg));
                }

            } catch (InstantiationException e) {
                Lumm.net.logThrowable(e);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Lumm.net.logThrowable(e);
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                Lumm.net.logThrowable(e);
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                Lumm.net.logThrowable(e);
                e.printStackTrace();
            } catch (SecurityException e) {
                Lumm.net.logThrowable(e);
                e.printStackTrace();

            }
        }

        Lumm.instance = this;
        Lumm.disposed = false;

        targetScene = initialScene;
    }

    // endregion

    /**
     * Method called after initialising the LibGDX backend. The modules'
     * (default and custom) {@link LummModule#onCreate()} method is called, in
     * which LibGDX data can be accessed.
     * <p>
     * The method also sets the screen to render as the one passed in the
     * constructors.
     */
    @Override
    public void create() {

        // create all custom modules, now they can access Gdx functionality for
        // proper setup

        // supported only for Desktop.
        if (!Gdx.app.getType().equals(ApplicationType.Desktop))
            configuration.runInBackground = false;

        List<Entry<Class<? extends LummModule>, LummModule>> itemsToProcess = new ArrayList<Entry<Class<? extends LummModule>, LummModule>>();

        Entries<Class<? extends LummModule>, LummModule> entries = modules.entries();
        while (entries.hasNext()) {

            Entry<Class<? extends LummModule>, LummModule> mapEntry = entries.next();
            Entry<Class<? extends LummModule>, LummModule> entry = new Entry<Class<? extends LummModule>, LummModule>();
            entry.key = mapEntry.key;
            entry.value = mapEntry.value;
            itemsToProcess.add(entry);
        }

        int previousNumModulesToInitialize = -1;
        boolean checkCircularDependencies = false;
        int index = 0;
        while (itemsToProcess.size() > 0) {

            if (index > itemsToProcess.size() - 1) {
                index = 0;
                if (previousNumModulesToInitialize == -1) {
                    previousNumModulesToInitialize = itemsToProcess.size();
                } else if (previousNumModulesToInitialize == itemsToProcess.size()) {
                    if (!checkCircularDependencies) checkCircularDependencies = true;
                    else {
                        Exception e = new Exception("Circular dependency in modules");


                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < itemsToProcess.size(); i++) {
                            builder.append("#");
                            builder.append(i);
                            builder.append(": Module ");
                            builder.append(itemsToProcess.get(i).value.getClass().getName());
                            builder.append(" with dependencies: ");

                            List<Class<? extends LummModule>> dependencies = itemsToProcess.get(i).value.getDependencies();
                            for (int j = 0; j < dependencies.size(); j++) {

                                builder.append(dependencies.get(j).getName());
                                if (j != dependencies.size() - 1)
                                    builder.append(", ");
                            }
                            builder.append("; ");

                        }
                        Lumm.debug.logError(builder.toString(), e);
                        Gdx.app.exit();

                    }
                }
            }

            LummModule moduleWithDependencies = itemsToProcess.get(index).value;
            List<Class<? extends LummModule>> dependencies = moduleWithDependencies.getDependencies();
            if (dependencies == null) {
                moduleWithDependencies.onCreateInternal();
                itemsToProcess.remove(index);
                continue;
            }

            boolean allDependenciesGood = true;
            for (int i = 0; i < dependencies.size(); i++) {
                LummModule dependency = modules.get(dependencies.get(i));

                if (dependency == null) {
                    NullPointerException e = new NullPointerException();
                    Lumm.debug.logError(
                            "LummModule" + moduleWithDependencies + " has dependency class "
                                    + dependencies.get(i).getName()
                                    + " which was not added to the LummConfiguration. Use LummConfiguration.addModule(LummModule) to add the dependency.",
                            e);
                    Gdx.app.exit();
                } else {
                    if (!dependency.isInitialized())
                        allDependenciesGood = false;
                }
            }

            if (allDependenciesGood) {
                moduleWithDependencies.onCreateInternal();
                itemsToProcess.remove(index);
                continue;
            }

            index++;

        }

        setScene(targetScene);
    }

    /**
     * Method called when every frame that handles scene transitions, updating
     * modules( default and custom) as well as calling
     * {@link LummScene#render(float)} from the parent class implementation of
     * {@link Game#render()}.
     */
    @Override
    public void render() {

        try {

            if (getScene() != targetScene)
                handleSceneTrasition();

            updateThread.run();
            super.render();
        } catch (Exception e) {
            Lumm.net.logThrowable(e);
        }
    }

    /**
     * Disposes all currently-loaded assets. Is called when the program is
     * exited.
     */
    @Override
    public void dispose() {
        Lumm.debug.log("Called  Lumm.dispose", null);
        try {

            for (Entry<Class<? extends LummModule>, LummModule> entry : modules) {
                if (entry.value.onDisposeListener != null)
                    entry.value.onDisposeListener.onDispose(entry.value);
            }
            LummScene currScene = getScene();
            currScene.dispose();

        } catch (Exception e) {
            Lumm.net.logThrowable(e);
            e.printStackTrace();
        }
    }

    /**
     * Method for changing the screen. The transition between scenes and the
     * objects they share will be handled automatically in
     * {@link #handleSceneTrasition()}. If the passed parameter is not an
     * instance of {@link LummScene} , an exception is thrown
     *
     * @throws {@link
     *             LummException} if the passed parameter is not an instance of
     *             {@link LummScene}.
     * @param scene
     *            The screen to change to
     */
    public static boolean setScene(LummScene scene) {

        try {

            if (instance.getScreen() != null && instance.getScreen().equals(scene))
                return false;

            if (instance.getScreen() == null) {
                setSceneInternal(scene);
                instance.updateThread.run();
            } else {
                instance.targetScene = (LummScene) scene;

            }

            return true;

        } catch (Exception e) {
            Lumm.net.logThrowable(e);
            e.printStackTrace();
        }
        return false;
    }

    private static void setSceneInternal(LummScene scene) {

        scene.onCreateSceneLayersInternal();
        instance.setScreen(scene);
        scene.onCreate(scene.runParameters);

    }

    /**
     * Returns the current scene that is being rendered.
     *
     * @return the current scene
     */
    public static LummScene getScene() {

        return (LummScene) instance.getScreen();
    }

    /**
     * Returns whether or not the application is in background or not. Becomes
     * true when the app goes into background and back to false when going into
     * foreground.
     *
     * @return boolean value for whether or not the application is focused.
     */
    public static boolean isInBackground() {

        return instance.inBackground;
    }

    public static boolean isPaused() {
        return instance.paused;
    }

    public static void pause(boolean pause) {

        try {
            // already in the state we are after, don't handle it
            if (instance.paused == pause)
                return;
            instance.paused = pause;

            for (Entry<Class<? extends LummModule>, LummModule> entry : modules.entries()) {

                if (entry.value.onPauseListener != null)
                    entry.value.onPauseListener.onPause(entry.value, pause);

            }

            if (getScene() != null) {

                getScene().onPauseInternal(pause);
            }

        } catch (Exception e) {
            Lumm.net.logThrowable(e);
            e.printStackTrace();
        }
    }

    // region methods

    /**
     * Method called when unfocusing the window. Sets {@link Lumm#inBackground}
     * to true, which can be accessed using {@link #isInBackground()} for
     * background actions to run ( nothing happens by default)
     */
    @Override
    public void pause() {
        Lumm.debug.log("Called Lumm.pause", null);

        inBackground = true;

        if (!paused && !configuration.runInBackground) {
            pause(true);
            super.pause();
        }
    }

    /**
     * Method called when focusing the window. Sets {@link Lumm#inBackground} to
     * false, which can be accessed using {@link #isInBackground()} for
     * background actions to run ( nothing happens by default)
     */
    @Override
    public void resume() {

        Lumm.debug.log("Called Lumm.resume", null);


        inBackground = false;

        if (paused && !configuration.runInBackground) {
            pause(false);
            super.resume();
        }
    }

    /**
     * Adds a module to run throughout the entire application. This method is to
     * be used at runtime, as for initialisation, you can add {@link LummModule}
     * to {@link LummConfiguration} using
     * {@link LummConfiguration#addModule(LummModule)},
     *
     * <p>
     * The module will be accessible from game scenes, batches, objects and
     * behaviors through {@link #getModule(Class)}.
     *
     * @param module
     * @return
     */
    public static <T extends LummModule> Class<Lumm> addModule(T module) {

        if (module == null)
            throw new NullPointerException("LummConfiguration.addModule parameter 'module' of type Module is null");

        if (modules.get(module.getClass()) != null)
            throw new LummException("LummConfiguration.addModule parameter type " + module.getClass()
                    + " is already in the list of modules");

        modules.put(module.getClass(), module);
        return Lumm.class;

    }

    /**
     * Returns a custom module, retrieved by passing the class we want the
     * Module we want to retrieve has.
     *
     * @throws LummException
     *             if the module class parameter does not implement the Module
     *             interface or is not in the list of modules
     * @param moduleClass
     *            The class of the module we want to return.
     *
     * @return Module implementation, or an exception
     */
    @SuppressWarnings("unchecked")
    public static <T extends LummModule> T getModule(Class<T> moduleClass) {

        if (!moduleClass.getInterfaces()[0].equals(LummModule.class))
            throw new LummException(
                    "Lumm.getModule parameter 'moduleClass' does not" + " implement the Module interface");

        if (modules.get(moduleClass) == null)
            throw new LummException("Lumm.getModule parameter type " + moduleClass.getClass()
                    + " can't be found in the list of modules");

        return (T) modules.get(moduleClass);

    }

    /**
     * Returns a boolean for whether or not the system contains a custom module
     * with the passed parameter as a class.
     *
     * @param moduleClass
     *            the Module implementation class to find in the list of custom
     *            modules
     * @return a boolean for whether or not the system contains desired module
     */
    public static boolean containsModule(Class<? extends LummModule> moduleClass) {

        return modules.get(moduleClass) != null;
    }

    /**
     * Internal method for passing objects from one scene to another, as well as
     * disposing objects from the old scene that are not required in the next
     * scene.
     */
    private void handleSceneTrasition() {
        LummScene currScene = Lumm.getScene();

        if (currScene != null) {
            // iterate over gamebatches to find all objects to remove
            for (int i = 0; i < currScene.sceneLayers.size(); i++) {
                LummSceneLayer gamebatch = currScene.sceneLayers.get(i);

                // iterate over the objects in the gamebatch, finding if it has
                // to
                // be removed
                // or not.
                for (int j = 0; j < gamebatch.objects.size(); j++) {
                    LummObject obj = gamebatch.objects.get(j);

                    if (currScene.toKeepForNextScene.contains(obj) || obj.isPersistent()) {

                        obj.setScene(targetScene);

                        j--; // decreasing index for object in gamebatch list
                        // due to
                        // calling setGameBatch
                        // the object is removed from the previous gamebatch
                        // that it
                        // was in.

                        obj.onSceneChange();
                    }
                }
                // end of iterating over gamebatch objects
            }
        }
        Lumm.setSceneInternal(targetScene);
        if (currScene != null)
            currScene.dispose();
    }

    public static void handleException(Exception e) {
        Lumm.net.logThrowable(e);
        //TODO make a list of lsiteners, L-net code will add a listener
    }

    public static void exit() {
        Gdx.app.exit();
    }

    // endregion methods


}
