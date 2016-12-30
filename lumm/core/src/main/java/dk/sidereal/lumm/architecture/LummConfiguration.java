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

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.ObjectMap;
import dk.sidereal.lumm.architecture.core.Assets;
import dk.sidereal.lumm.architecture.core.Debug;
import dk.sidereal.lumm.architecture.core.Net;
import dk.sidereal.lumm.architecture.core.Debug.Log;
import dk.sidereal.lumm.util.LummException;

/**
 * The main settings of the Game made with Lumm. Has to be passed to
 * {@link Lumm} in the {@link Lumm#Lumm(LummScene, LummConfiguration)}
 * constructor. If the {@link LummConfiguration} passed is null, the constructor
 * will create a new instance of {@link LummConfiguration}, holding the default
 * values to variables.
 * <p>
 * Adding additional variables in future versions will by default make the
 * framework behave as before they were added, thus this class will be safe to
 * update from the git server.
 *
 * @author Claudiu Bele
 */
public class LummConfiguration {

    // region fields

    /**
     * List of modules to add to {@link Lumm} when an instance of
     * {@link LummConfiguration} is passed to the constructor. Elements should
     * be added using {@link #addModule(LummModule)}.
     */
    final List<Class<? extends LummModule>> modules;

    /** List of parameters that modules can use in order to be customized. */
    private final ObjectMap<String, Object> parameters;

    /**
     * Whether debugging can be toggled on or off in the application. In
     * {@link Lumm#create()} the value will be applied to {@link Lumm#debug}.
     */
    public boolean debugEnabled;

    /** The root path to all data related to the app. */
    public String rootDataPath;

    /**
     * Whether the application is an executable or not (false by default).
     * Affects the root path for loading assets, so people responsible with
     * graphics can change them and start the app to see changes instead of
     * assets being loaded from the jar file
     * <p>
     * If value is set to true, files will be taken using
     * {@link FileType#Classpath} ( from the executable ), otherwise
     * {@link FileType#Internal} will be used, files being handled at a location
     * relative to the executable.
     * <p>
     *
     * @see Assets#getFileType() returns the FileType used in importing tied to
     *      the application
     * @see Assets#getResolver()
     */
    public boolean isExecutable;

    /**
     * Whether to prioritise external storage when saving data or not, if the
     * device allows.
     */
    public boolean prioritiseExternalStorage;

    /** Initial scene to be displayed after the game is loaded */
    public LummScene initialScene;

    /** Names of input processors to use in {@link Input} */
    public String[] inputProcessorNames;

    /**
     * Whether to run the app in background if possible ( depends on platform ).
     * Internally, it will affect whether {@link Game#pause()}
     * {@link Game#resume()} are called when the app goes into background
     */
    public boolean runInBackground;

    /**
     * The app key to be used for LNet authentication. L-Net can save logs,
     * crashes and facilitate server-client TCP socket communication
     */
    public String applicationLNetKey;

    /** If set to true, the LNet session will be started as soon as possible. Otherwise {@link Net#startLNet()} has to be explicitly called
     *
     */
    public boolean startLNetSessionOnStartup;

    /**
     * App version. Will be used in LNet. If the version is not set L-Net
     * functionality will not work.
     */
    public String applicationVersion;

    /**
     * Whether to call {@link Debug#startLog(String)} as soon as
     * {@link Lumm#debug} is initialized. Is set to false by default. If set to
     * true, a log will be started with logtype {@link Log#LOG_ALL }.
     */
    public boolean startDebugLogOnStartup;

    /** Whether to use the LNet when ending a log, the data being sent to the server afterwards
     *
     */
    public boolean useLNetOnFinishLogListener;

    // endregion fields

    // region constructors

    public LummConfiguration() {

        inputProcessorNames = new String[]{"Default"};
        debugEnabled = true;
        prioritiseExternalStorage = false;
        isExecutable = false;
        rootDataPath = "Lumm app";
        parameters = new ObjectMap<String, Object>();
        modules = new ArrayList<Class<? extends LummModule>>();
        runInBackground = false;
        startDebugLogOnStartup = false;

    }

    /**
     * Adds a module to the list of modules available in the game. Modules can
     * also be added at runtime using {@link Lumm#addModule(Object)}.
     * <p>
     * The module will be accessible from game scenes, batches, objects and
     * behaviors through {@link Lumm#getModule(Class)}.
     *
     * @param module
     * @return
     */
    public LummConfiguration addModule(Class<? extends LummModule> module) {

        if (module == null)
            throw new NullPointerException("LummConfiguration.addModule parameter 'module' of type Module is null");

        if (modules.contains(module))
            throw new LummException("LummConfiguration.addModule parameter type " + module.getClass()
                    + " is already in the list of modules");

        modules.add(module);
        return this;
    }

    public <T extends Object> LummConfiguration addModuleParameter(String key, T value) {
        if (key == null)
            throw new LummException("Attempted to add module parameter while key was null");
        parameters.put(key, value);
        return this;
    }

    public Object getModuleParameter(String key) {
        return parameters.get(key);
    }

    // endregion

}
