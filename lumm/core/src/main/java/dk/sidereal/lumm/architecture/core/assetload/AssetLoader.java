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

package dk.sidereal.lumm.architecture.core.assetload;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummObject;
import dk.sidereal.lumm.architecture.LummScene;
import dk.sidereal.lumm.architecture.concrete.ConcreteLummObject;
import dk.sidereal.lumm.architecture.core.Assets;
import dk.sidereal.lumm.architecture.listeners.OnResizeListener;

/**
 * Utility class that makes loading assets using {@link Assets} easier.
 *
 * @author Claudiu Bele
 */
public class AssetLoader extends ConcreteLummObject {

    // region fields
    private LummScene nextscene;

    private AssetLoadHandler assetLoadHandler;

    // endregion fields

    // region constructors

    /**
     * Creates an asset loader. The params have to be the following:
     * <p>
     * At index 0, the scene
     * <p>
     * At index 1, the
     *
     * @param scene
     * @param params
     */
    public AssetLoader(LummScene scene, LummScene nextScene) {

        super(scene);

        this.assetLoadHandler = new LoadingPercentage(scene);
        this.nextscene = nextScene;

        onResizeListener = new OnResizeListener<LummObject>() {

            @Override
            public void onResize(LummObject caller, float x, float y, float oldX, float oldY) {
                assetLoadHandler.onResizeListener.onResize(caller, x, y, oldX, oldY);

            }
        };

    }

    // endregion constructors

    // region methods

    @Override
    public void onUpdate() {

        if (Lumm.assets.getProgress() == 1 && assetLoadHandler.canGoToNextScene()) {
            goToNextScene();
        }

        assetLoadHandler.updateProgress(Lumm.assets.getProgress());

    }


    public void setAssetLoadHandler(AssetLoadHandler handler) {
        if (handler == null)
            throw new NullPointerException(
                    "AssetLoader.setAssetLoadHandler parameter handler of type AssetLoadHandler is null. One is mandatory for switching scenes");

        this.assetLoadHandler = handler;
    }

    // region Import
    public <T> void load(String filepath, Class<T> classType, AssetLoaderParameters<T> loadParams) {

        load(filepath, classType, Assets.defaultResolver, loadParams);
    }

    public <T> void load(String filepath, Class<T> classType) {

        load(filepath, classType, Assets.defaultResolver);
    }

    public <T> void load(String filepath, Class<T> classType, Class<? extends FileHandleResolver> resolver) {

        Lumm.assets.load(filepath, classType, resolver);
    }

    public <T> void load(String filepath, Class<T> classType, Class<? extends FileHandleResolver> resolver,
                         AssetLoaderParameters<T> loadParams) {
        Lumm.assets.load(filepath, classType, resolver, loadParams);
    }

    public <T> void load(String generalPath, String[] fileNames, Class<T> fileType, String extension) {

        load(generalPath, fileNames, fileType, extension, Assets.defaultResolver);
    }

    public <T> void load(String generalPath, String[] fileNames, Class<T> fileType, String extension,
                         Class<? extends FileHandleResolver> resolver) {

        for (int i = 0; i < fileNames.length; i++) {
            Lumm.assets.load(generalPath + fileNames[i] + extension, fileType, resolver);
        }
    }

    public <T> void load(String generalPath, String[] fileNames, Class<T> fileType, String extension,
                         Class<? extends FileHandleResolver> resolver, AssetLoaderParameters<T> loadParams) {
        for (int i = 0; i < fileNames.length; i++) {
            Lumm.assets.load(generalPath + fileNames[i] + extension, fileType, resolver, loadParams);
        }
    }

    public final void goToNextScene() {
        Lumm.setScene(nextscene);
    }

    // endregion

    // endregion methods

}
