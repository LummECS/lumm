/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.sidereal.lumm.architecture.core.assetload;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummObject;
import com.sidereal.lumm.architecture.LummScene;
import com.sidereal.lumm.architecture.listeners.OnResizeListener;
import com.sidereal.lumm.ui.TextBuilder;

/**
 * Default {@link AssetLoadHandler} passed to the
 * {@link LoadingScreenConfiguration#assetLoadingHandler} in the
 * {@link LoadingScreenConfiguration} default constructor. Shows the progress in
 * loading assets by displaying a percentage.
 * <p>
 * As soon as the game is done with loading assets, new scene will be loaded.
 * 
 * @author Claudiu Bele
 */
public class LoadingPercentage extends AssetLoadHandler {

	// region fields

	TextBuilder text;

	// endregion fields

	// region constructors

	public LoadingPercentage(LummScene scene, Object... params) {

		super(scene);
	}

	@Override
	public void onCreate(Object... params) {

		if (text != null)
			return;
		if (!Lumm.assets.contains(Lumm.assets.frameworkAssetsFolder + "Blocks4.fnt")
				|| !Lumm.assets.contains(Lumm.assets.frameworkAssetsFolder + "Blocks4_0.png"))
			return;

		text = new TextBuilder(getScene(), true);
		text.setScale(Gdx.graphics.getWidth() / 600f);
		text.setColor(Color.WHITE);
		text.setAlpha(1);

		text.position.set(getSceneLayer().camera.position.x, getSceneLayer().camera.viewportHeight / 10 * 1.6f);

		
		onResizeListener = new OnResizeListener<LummObject>() {
			
			@Override
			public void onResize(LummObject caller, float x, float y, float oldX, float oldY) {
				if (text != null) {
					text.position.setX(Gdx.graphics.getWidth() / 2f);
					text.position.setY(Gdx.graphics.getHeight() / 10 * 1.6f);
				}				
			}
		};
	}

	// endregion constructors

	// region methods

	@Override
	public void updateProgress(float progress) {

		if (text == null)
			onCreate();
		else {
			text.setText("Loading \r\n" + ((int) (progress * 1000) / 10f) + " %", Color.WHITE);
		}
	}


	@Override
	public boolean canGoToNextScene() {

		return true;
	}

	// endregion methods
}
