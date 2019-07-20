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

package dk.sidereal.lumm.architecture.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummConfiguration;
import dk.sidereal.lumm.architecture.LummModule;
import dk.sidereal.lumm.architecture.listeners.OnDisposeListener;

/**
 * UnifiedAssetManager is responsible for handling interaction with the LibGDX
 * asset managers. The class provides convenience for multiple asset managers
 * available with different import types.
 * <p>
 * AssetLoader will create an asset manager for all import types if not
 * specified otherwise. The import types are designated by the
 * {@link FileHandleResolver} classes passed to the constructor, and are not
 * handled by the framework.
 * <p>
 * If the project is to be runnable on WebGL, framework assets must be loaded
 * internally (not within the jar) and placed accordingly.
 *
 * @author Claudiu Bele
 */
public class Assets extends LummModule {

    // region fields

    /**
     * Map containing a {@link FileHandleResolver} as a key and an
     * {@link AssetManager} as a map. Different AssetManager instances will be
     * used based on where we want the files to be taken from.
     */
    public HashMap<Class<? extends FileHandleResolver>, AssetManager> managers;

    public static Class<? extends FileHandleResolver> defaultResolver;

    /**
     * The resolvers to use for file handling in the application. Passed to the
     * constructor, which saves them up until {@link #onCreate()} is called.
     */
    private Class<? extends FileHandleResolver>[] resolvers;

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

    public String frameworkAssetsFolder;

    public Class<? extends FileHandleResolver> frameworkAssetsResolver;

    // endregion

    // region constructors

    /**
     * Default constructor.
     *
     * Creates an asset manager for all File Handler resolvers. All file handle
     * types will be supported using this feature given that they are supported
     * by the platform the game runs in. Will call the
     * {@link #UnifiedAssetManager(boolean, Class...)} constructor.
     */
    @SuppressWarnings("unchecked")
    public Assets(LummConfiguration config) {

        super(config);

        setUpdateFrequency(0);
        this.resolvers = new Class[]{AbsoluteFileHandleResolver.class, ExternalFileHandleResolver.class,
                InternalFileHandleResolver.class, LocalFileHandleResolver.class, ClasspathFileHandleResolver.class};
        this.isExecutable = config.isExecutable;

    }

    @Override
    public void onCreate() {

        managers = new HashMap<Class<? extends FileHandleResolver>, AssetManager>();

        if (defaultResolver == null)
            getResolver();
        try {

            // create asset managers using all passed resolvers
            for (int i = 0; i < resolvers.length; i++) {
                managers.put(resolvers[i], new AssetManager(resolvers[i].newInstance()));
            }
            // the FileHandle Resolver for framework files was not passed, will
            // add it.
            if (!managers.containsKey(ClasspathFileHandleResolver.class) && Gdx.app.getType() != ApplicationType.WebGL)
                managers.put(ClasspathFileHandleResolver.class,
                        new AssetManager(ClasspathFileHandleResolver.class.newInstance()));

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        boolean supportsClassImports = Gdx.app.getType() != ApplicationType.WebGL;
        if (supportsClassImports) {
            frameworkAssetsFolder = "com/sidereal/lumm/util/assets/";
            frameworkAssetsResolver = ClasspathFileHandleResolver.class;
        } else {
            frameworkAssetsFolder = "lumm/";
            frameworkAssetsResolver = InternalFileHandleResolver.class;
        }

        /**
         * Loads default framework assets.
         * <p>
         * The assets are stored inside the .jar file, however on webGL
         * Gdx.files.classPath can't be used, so the FileHandleResolver has to
         * be set to the internal one.
         */
//        load(frameworkAssetsFolder + "noClip.png", Texture.class, frameworkAssetsResolver);
//        load(frameworkAssetsFolder + "Blocks4.fnt", BitmapFont.class, frameworkAssetsResolver);
//        load(frameworkAssetsFolder + "Blocks.fnt", BitmapFont.class, frameworkAssetsResolver);
//        load(frameworkAssetsFolder + "AudioListener.png", Texture.class, frameworkAssetsResolver);

        onDisposeListener = new OnDisposeListener<LummModule>() {

            @Override
            public void onDispose(LummModule caller) {
                for (Entry<Class<? extends FileHandleResolver>, AssetManager> entry : managers.entrySet()) {
                    entry.getValue().dispose();
                }

            }
        };

    }


    // endregion

    // region methods

    // region determining filetype

    /**
     * Returns the type of file that the application uses for importing.
     * <p>
     * Do not use before creating the application ( Lumm has been initialised )
     * as the framework has no way of telling whether the application is of type
     * WebGL or not( checking for it is necessary as the WebGL implementation
     * does not support {@link FileType#Classpath}.
     *
     * @see {@link <a
     *      href="https://github.com/libgdx/libgdx/wiki/File-handling">LibGDX
     *      file handling</a>}
     * @see #getResolver() returns the {@link FileHandleResolver} to extract
     *      assets from, based on platform and
     *      {@link LummConfiguration#isExecutable}.
     * @return the import file type
     */
    public FileType getFileType() {

        if (Gdx.app.getType().equals(ApplicationType.WebGL)) {
            return FileType.Internal;
        }

        if (isExecutable)
            return FileType.Classpath;
        else
            return FileType.Internal;
    }

    /**
     * Returns the {@link FileHandleResolver} that the application uses for
     * importing.
     * <p>
     * Do not use before creating the application ( Lumm has been initialised )
     * as the framework has no way of telling whether the application is of type
     * WebGL or not( checking for it is necessary as the WebGL implementation
     * does not support {@link FileType#Classpath}.
     * <p>
     *
     * @see {@link <a
     *      href="https://github.com/libgdx/libgdx/wiki/File-handling">LibGDX
     *      file handling</a>}
     * @see #getFileType() returns the {@link FileType} to use for importing
     *      assets outside of {@link Assets} assets from, based on platform and
     *      {@link LummConfiguration#isExecutable}.
     * @return the resolver used for loading assets in the project.
     */
    public Class<? extends FileHandleResolver> getResolver() {

        if (Gdx.app.getType().equals(ApplicationType.WebGL)) {
            defaultResolver = InternalFileHandleResolver.class;
            return defaultResolver;
        }

        if (isExecutable)
            defaultResolver = ClasspathFileHandleResolver.class;
        else
            defaultResolver = InternalFileHandleResolver.class;
        return defaultResolver;

    }

    //

    // endregion

    // region load

    /**
     * Loads an asset using {@link #defaultResolver} as the resolver
     *
     * @param filepath
     * @param classType
     */
    public <T> void load(String filepath, Class<T> classType) {

        load(filepath, classType, defaultResolver);
    }

    /**
     * Loads an asset it has not been loaded.
     *
     * @param filepath
     * @param classType
     * @param resolver
     */
    public <T> void load(String filepath, Class<T> classType, Class<? extends FileHandleResolver> resolver) {
        load(filepath, classType, resolver, null);
    }

    public <T> void load(String filepath, Class<T> classType, Class<? extends FileHandleResolver> resolver,
                         AssetLoaderParameters<T> loadParameters) {

        if (getResolver(filepath) != null)
            return;

        AssetManager assetManager = managers.get(resolver);

        Lumm.debug.log("System is now loading file at " + filepath, null);

        if (!assetManager.isLoaded(filepath, classType)) {
            if (loadParameters != null)
                assetManager.load(filepath, classType, loadParameters);
            else
                assetManager.load(filepath, classType);

        }
    }

    /**
     * Forces the managers to finish loading instead of trying to load every
     * single frame.
     */
    public void finishLoading() {

        for (Entry<Class<? extends FileHandleResolver>, AssetManager> entry : managers.entrySet()) {
            entry.getValue().finishLoading();
        }
    }

    // endregion

    // region unload

    /**
     * Unloads an asset from the memory. Finds the resolver tied to the asset,
     * and if exists, unloads the asset from the manager tied to the resolver
     * from {@link #managers}
     * <p>
     * Returns true based on whether or not the value has been found in any of
     * the asset managers.
     *
     * @param filePath
     * @return
     */
    public boolean unload(String filePath) {

        Class<? extends FileHandleResolver> resolver = getResolver(filePath);
        if (resolver == null)
            return false;
        if (!managers.get(resolver).isLoaded(filePath))
            return false;

        managers.get(resolver).unload(filePath);
        return true;
    }

    /**
     * Unloads an asset from the memory. Finds the manager tied to the passed
     * FileHandleREsolver parameter and if that manager has the specified file
     * is loaded, unload it.
     *
     * @param filePath
     * @param resolver
     * @return
     */
    public boolean unload(String filePath, Class<? extends FileHandleResolver> resolver) {

        AssetManager assetManager = managers.get(resolver);
        if (assetManager.isLoaded(filePath)) {
            assetManager.unload(filePath);
            return true;
        }

        return false;
    }

    // endregion

    // region get

    /**
     * Returns an asset if existent at the specified path with
     * {@link #defaultResolver} as the resolver.
     *
     * @param filepath
     * @param classType
     * @return
     */
    public <T> T get(String filepath, Class<T> classType) {

        if (getResolver(filepath) == null)
            return get(filepath, classType, defaultResolver);
        else
            return get(filepath, classType, getResolver(filepath));
    }

    /**
     * Returns an asset if existent at the specificied path. If the asset does
     * not exist, it will load a default file found in the framework ( currently
     * available for textures and bitmap fonts).
     *
     *
     * @param filepath
     * @param classType
     * @param resolver
     * @return
     */
    public <T> T get(String filepath, Class<T> classType, Class<? extends FileHandleResolver> resolver) {

        AssetManager assetManager = managers.get(resolver);

        if (assetManager.isLoaded(filepath, classType)) {
            return assetManager.get(filepath, classType);
        } else {
            Lumm.debug.logDebug("Asset at path " + filepath + " of type " + classType + " is loaded on demand", null);
            assetManager.load(filepath, classType);
            assetManager.finishLoading();
            return assetManager.get(filepath, classType);
        }

    }

    // endregion

    /**
     * Updates the load of assets from all managers found in {@link #managers}.
     * <p>
     * The workload is distributed based on last frame's duration between the
     * managers that are not done with loading all their required assets.
     */
    @Override
    public void onUpdate() {

        // loading assets.
        if (getProgress() != 1) {

            // count the number of managers that are in progress, in order to
            // distribute allocated time
            // over which
            int countManagersInProgress = 0;
            for (Entry<Class<? extends FileHandleResolver>, AssetManager> entry : managers.entrySet()) {
                if (entry.getValue().getProgress() != 1) {
                    countManagersInProgress++;
                }
            }

            // divide the last frame's time by the number of managers that are
            // still
            // in progress
            // , multiplying that time to match the parameter for updating of a
            // manager, which is in milliseconds.
            int individualManagerAllocatedTime = (int) ((Lumm.time.getRealDeltaTime() / countManagersInProgress)
                    * 1000);

            for (Entry<Class<? extends FileHandleResolver>, AssetManager> entry : managers.entrySet()) {
                if (entry.getValue().getProgress() != 1) {
                    entry.getValue().update(individualManagerAllocatedTime);
                }
            }

        }

    }

    public <T> boolean contains(String filePath) {

        return getResolver(filePath) != null;
    }

    /**
     * Gets the overall progress of asset loading, by using each
     * {@link AssetManager}'s assets that finished loading and the ones in
     * process of loading. Will not be using {@link AssetManager#getProgress()}
     * because with 0 assets done out of 0, it would return 100%
     *
     * @return the progress, a float value between 0 and 1.
     */
    public float getProgress() {

        int doneSum = 0;
        int totalSum = 0;
        for (Entry<Class<? extends FileHandleResolver>, AssetManager> entry : managers.entrySet()) {
            int remaining = entry.getValue().getQueuedAssets();
            int done = entry.getValue().getLoadedAssets();
            doneSum += done;
            totalSum += done + remaining;
        }
        return (float) doneSum / totalSum;
    }

    /**
     * Returns the first resolver that has a reference to the
     * <code>filePath</code> parameter. A resolver will be returned if the
     * manager tied to it in {@link #managers} has the file at the specified
     * filePath loaded.
     *
     * @param filePath
     * @return
     */
    public Class<? extends FileHandleResolver> getResolver(String filePath) {

        for (Entry<Class<? extends FileHandleResolver>, AssetManager> entry : managers.entrySet()) {
            if (entry.getValue().isLoaded(filePath))
                return entry.getKey();
        }
        return null;
    }

    @Override
    public List<Class<? extends LummModule>> getDependencies() {
        return null;
    }

    // endregion

}
