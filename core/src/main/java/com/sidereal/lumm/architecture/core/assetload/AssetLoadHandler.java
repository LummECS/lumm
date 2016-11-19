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

import com.sidereal.lumm.architecture.LummScene;
import com.sidereal.lumm.architecture.concrete.ConcreteLummObject;

/**
 * Class used for customizing the handling of the Loading Screen and progress
 * given by {@link AssetLoader}.
 * 
 * @author Claudiu Bele
 */
public abstract class AssetLoadHandler extends ConcreteLummObject {

	// region constructors

	public AssetLoadHandler(LummScene scene) {

		super(scene);
	}

	// endregion constructors

	// region methods

	public abstract void updateProgress(float progress);


	@Override
	public abstract void onCreate(Object... params);

	public abstract boolean canGoToNextScene();

	// endregion methods
}
